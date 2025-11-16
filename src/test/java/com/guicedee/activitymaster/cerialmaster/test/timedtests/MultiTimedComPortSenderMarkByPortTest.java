package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for convenience methods on MultiTimedComPortSender to mark the current message
 * on a specific COM port as completed or errored, without needing the message id.
 *
 * Ports used: 20 and 21
 */
public class MultiTimedComPortSenderMarkByPortTest {

    @Test
    public void completeCurrentMessageByPort() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> c20;
        try {
            c20 = svc.getComPortConnectionDirect(20).await().atMost(Duration.ofSeconds(50));
            c20.connect(); c20.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config cfg = new Config(1, 10, 200);

        // One simple message for COM20
        MessageSpec m = new MessageSpec("BY-PORT-COMPLETE-20", "PAYLOAD-A20", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(20, List.of(m));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(128));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        // When the message on COM20 starts, invoke the convenience API
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && Integer.valueOf(20).equals(mp.comPort) && mp.progress != null && mp.progress.id != null && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                manager.markCompleted(20);
            }
        });

        var allUni = manager.enqueueGroupsWithName("MarkByPort-Complete", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        // Wait for the group to finish
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertTrue(results.containsKey(20));
        GroupResult gr = results.get(20);
        assertEquals(1, gr.results.size());
        assertEquals(TimedComPortSender.State.Completed, gr.results.get(0).terminalState);

        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(agg);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertFalse(agg.anyFailures);
        assertNotNull(agg.finishedAtEpochMs);

        // Ensure streams emitted at least one item
        statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
        progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
    }

    @Test
    public void errorCurrentMessageByPort() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> c21;
        try {
            c21 = svc.getComPortConnectionDirect(21).await().atMost(Duration.ofSeconds(50));
            c21.connect(); c21.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config cfg = new Config(1, 10, 200);

        // One simple message for COM21
        MessageSpec m = new MessageSpec("BY-PORT-ERROR-21", "PAYLOAD-A21", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(21, List.of(m));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(128));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        // When the message on COM21 starts, invoke the convenience API to error it
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && Integer.valueOf(21).equals(mp.comPort) && mp.progress != null && mp.progress.id != null && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                manager.markErrored(21, "Test error");
            }
        });

        var allUni = manager.enqueueGroupsWithName("MarkByPort-Error", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        // Wait for the group to finish
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertTrue(results.containsKey(21));
        GroupResult gr = results.get(21);
        assertEquals(1, gr.results.size());
        assertEquals(TimedComPortSender.State.Error, gr.results.get(0).terminalState);

        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(agg);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertTrue(agg.anyFailures, "Aggregate should report failures due to errored message");
        assertNotNull(agg.finishedAtEpochMs);

        // Ensure streams emitted at least one item
        statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
        progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
    }
}
