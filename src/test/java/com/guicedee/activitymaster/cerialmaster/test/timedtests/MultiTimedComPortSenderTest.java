package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MultiTimedComPortSenderTest {

    @Test
    public void multiPort_enqueue_pause_resume_cancel_propagates_and_completes() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> conn20;
        ComPortConnection<?> conn21;
        try {
            conn20 = svc.getComPortConnectionDirect(20).await().atMost(Duration.ofSeconds(50));
            conn20.connect();
            conn20.disconnect();
            conn21 = svc.getComPortConnectionDirect(21).await().atMost(Duration.ofSeconds(50));
            conn21.connect();
            conn21.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        TimedComPortSender.Config baseCfg = new TimedComPortSender.Config(1, 15, 100);

        TimedComPortSender.MessageSpec A20 = new TimedComPortSender.MessageSpec("A20", "PAYLOAD-A20", new TimedComPortSender.Config(0, 10, 120));
        TimedComPortSender.MessageSpec B20 = new TimedComPortSender.MessageSpec("B20", "PAYLOAD-B20", new TimedComPortSender.Config(0, 10, 120));
        TimedComPortSender.MessageSpec A21 = new TimedComPortSender.MessageSpec("A21", "PAYLOAD-A21", new TimedComPortSender.Config(0, 10, 120));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(200));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(200));

        // Track manager status states to verify cascades
        CopyOnWriteArrayList<MultiTimedComPortSender.ManagerStatus> managerStates = new CopyOnWriteArrayList<>();
        manager.status().subscribe().with(managerStates::add);

        var allUni = manager.enqueueGroups(Map.of(
                20, List.of(A20, B20),
                21, List.of(A21)
        ), baseCfg);

        // Allow messages to start
        Thread.sleep(40);

        // Pause all -> expect underlying senders to be paused as well
        manager.pauseAll().await().atMost(Duration.ofSeconds(50));
        TimedComPortSender s20 = manager.getSenders().get(20);
        TimedComPortSender s21 = manager.getSenders().get(21);
        assertNotNull(s20);
        assertNotNull(s21);
        assertTrue(s20.isPaused());
        assertTrue(s21.isPaused());

        // Resume all -> senders should resume and continue
        manager.resumeAll().await().atMost(Duration.ofSeconds(50));
        assertFalse(s20.isPaused());
        assertFalse(s21.isPaused());

        // Complete first messages externally
        s20.complete();
        s21.complete();

        // Let second message on 20 start and then cancel all
        Thread.sleep(40);
        manager.cancelAll("test-cancel").await().atMost(Duration.ofSeconds(50));

        // Await combined result map without throwing; map should contain both ports
        Map<Integer, TimedComPortSender.GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertTrue(results.containsKey(20));
        assertTrue(results.containsKey(21));

        // Ensure we saw some events
        statusSub.awaitNextItems(1);
        progressSub.awaitNextItems(1);

        // Manager should have emitted pause and resume and cancel states at least once
        boolean sawPause = managerStates.stream().anyMatch(ms -> ms.comPort == null && ms.state == TimedComPortSender.State.Paused);
        boolean sawResume = managerStates.stream().anyMatch(ms -> ms.comPort == null && ms.state == TimedComPortSender.State.Running);
        boolean sawCancel = managerStates.stream().anyMatch(ms -> ms.comPort == null && ms.state == TimedComPortSender.State.Cancelled);
        assertTrue(sawPause && sawResume && sawCancel);
    }
}
