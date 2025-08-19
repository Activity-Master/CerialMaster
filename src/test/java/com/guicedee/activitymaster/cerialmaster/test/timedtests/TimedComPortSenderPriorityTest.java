package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderPriorityTest {

    @Test
    public void priorityRunsImmediatelyAfterCurrent_andPerMessageUniCompletes() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> conn;
        try {
            conn = svc.getComPortConnectionDirect(20).await().indefinitely();
            conn.connect();
            conn.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }
        TimedComPortSender sender = conn.getOrCreateTimedSender(new TimedComPortSender.Config(0, 8, 250));

        // Track the order that messages start
        List<String> startOrder = new CopyOnWriteArrayList<>();
        var progressSub = sender.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(256));
        sender.messageProgress().subscribe().with(mp -> {
            if (mp != null) {
                System.out.println("[MessageProgress] id=" + mp.id + ", attempt=" + mp.attempt + ", state=" + mp.state + ", note=" + mp.note);
                if (mp.note != null && mp.note.contains("Starting") && mp.id != null) {
                    startOrder.add(mp.id);
                }
            }
        });

        // Enqueue a group A, B, C
        var A = new TimedComPortSender.MessageSpec("A", "PAYLOAD-A", new TimedComPortSender.Config(0, 12, 300));
        var B = new TimedComPortSender.MessageSpec("B", "PAYLOAD-B", new TimedComPortSender.Config(0, 12, 300));
        var C = new TimedComPortSender.MessageSpec("C", "PAYLOAD-C", new TimedComPortSender.Config(0, 12, 300));
        var groupUni = sender.enqueueGroup(List.of(A, B, C));

        // Wait (bounded) for A to start
        for (int i = 0; i < 200 && !startOrder.contains("A"); i++) { Thread.sleep(10); }
        System.out.println("[TEST] StartOrder after A wait: " + startOrder);

        // Enqueue a priority message P while A is running
        var P = new TimedComPortSender.MessageSpec("P", "PAYLOAD-P", new TimedComPortSender.Config(0, 8, 300));
        var pUni = sender.enqueuePriority(P);

        // Complete A externally so next should be P (priority) before B, then C
        sender.complete();

        // Wait (bounded) for P to start
        for (int i = 0; i < 200 && !startOrder.contains("P"); i++) { Thread.sleep(10); }
        System.out.println("[TEST] StartOrder after P wait: " + startOrder);

        // Now complete P
        sender.complete();

        // Let B start and progress sufficiently (bounded wait for start), then allow time to elapse
        for (int i = 0; i < 200 && !startOrder.contains("B"); i++) { Thread.sleep(10); }
        System.out.println("[TEST] StartOrder after B wait: " + startOrder);
        Thread.sleep(200); // additional time for B to progress (and likely time out)

        // At this point C should start. Wait (bounded) for C to enter execution, then enqueue priority Q which must run after C
        for (int i = 0; i < 200 && !startOrder.contains("C"); i++) { Thread.sleep(10); }
        System.out.println("[TEST] StartOrder after C wait: " + startOrder);
        var Q = new TimedComPortSender.MessageSpec("Q", "PAYLOAD-Q", new TimedComPortSender.Config(0, 8, 300));
        var qUni = sender.enqueuePriority(Q);

        // Complete C externally to advance to Q
        sender.complete();

        // Wait for the whole group to finish (A, B, C only)
        TimedComPortSender.GroupResult groupResult = groupUni.await().indefinitely();
        assertNotNull(groupResult);
        assertEquals(3, groupResult.results.size());

        // Verify order: A first, then P, then B, then C, then Q (starts). We track starts; assert first occurrences
        List<String> uniqueStartOrder = new ArrayList<>();
        for (String id : startOrder) {
            if (!uniqueStartOrder.contains(id)) uniqueStartOrder.add(id);
        }
        assertTrue(uniqueStartOrder.size() >= 5, "Should have at least 5 start events");
        assertEquals("A", uniqueStartOrder.get(0));
        assertEquals("P", uniqueStartOrder.get(1));
        assertEquals("B", uniqueStartOrder.get(2));
        assertEquals("C", uniqueStartOrder.get(3));
        assertEquals("Q", uniqueStartOrder.get(4));

        // Per-message Uni for P should complete successfully
        TimedComPortSender.MessageResult pResult = pUni.await().indefinitely();
        assertNotNull(pResult);
        assertEquals("P", pResult.id);
        assertEquals(TimedComPortSender.State.Completed, pResult.terminalState);
        assertEquals("PAYLOAD-P", pResult.payload);

        // Per-message Uni for Q should complete successfully after C
        TimedComPortSender.MessageResult qResult = qUni.await().indefinitely();
        assertNotNull(qResult);
        assertEquals("Q", qResult.id);
        assertEquals(TimedComPortSender.State.Completed, qResult.terminalState);
        assertEquals("PAYLOAD-Q", qResult.payload);

        // Ensure some progress items to the subscriber (non-zero)
        progressSub.awaitNextItems(1);
    }
}
