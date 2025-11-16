package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scenario test:
 * - 4 timed COM port senders (ports 20,21,22,23) run 5 messages each
 * - Pause the multi during run and print current statistics (manager snapshot and per-sender snapshots)
 * - Resume one sender manually (port 20) and ensure it completes while others remain paused
 * - Verify multi does NOT complete while others paused
 * - Resume the multi, finish all remaining, and verify the multi combined Uni completes
 */
public class MultiTimedComPortSenderFourPortsPauseResumeTest {

    @Test
    public void fourPorts_pauseMulti_resumeSingle_then_resumeAll_and_complete() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> c20, c21, c22, c23;
        try {
            c20 = svc.getComPortConnectionDirect(20).await().atMost(Duration.ofSeconds(50));
            c21 = svc.getComPortConnectionDirect(21).await().atMost(Duration.ofSeconds(50));
            c22 = svc.getComPortConnectionDirect(22).await().atMost(Duration.ofSeconds(50));
            c23 = svc.getComPortConnectionDirect(23).await().atMost(Duration.ofSeconds(50));
            // Perform a quick connect/disconnect to validate access
            c20.connect(); c20.disconnect();
            c21.connect(); c21.disconnect();
            c22.connect(); c22.disconnect();
            c23.connect(); c23.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        // Base config: modest retry/delay/timeout to make the test reasonably fast
        Config baseCfg = new Config(1, 12, 140);

        // Build 5 messages per port
        Map<Integer, List<MessageSpec>> byPort = new LinkedHashMap<>();
        byPort.put(20, buildSpecs("P20-", 5));
        byPort.put(21, buildSpecs("P21-", 5));
        byPort.put(22, buildSpecs("P22-", 5));
        byPort.put(23, buildSpecs("P23-", 5));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(1024));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(4096));

        String groupName = "Batch-4x5";
        var allUni = manager.enqueueGroupsWithName(groupName, byPort, baseCfg);
        var aggUni = manager.currentRunAggregateUni();

        // Let first messages start on each port
        Thread.sleep(60);

        // Pause all senders via manager
        manager.pauseAll().await().atMost(Duration.ofSeconds(10));

        // Capture and print ManagerSnapshot (aggregate + per-sender summaries)
        ManagerSnapshot snap1 = manager.snapshot();
        System.out.println("[SNAPSHOT-PAUSED] Aggregate: group=" + snap1.aggregate.groupName
                + ", started=" + snap1.aggregate.startedAtEpochMs
                + ", finished=" + snap1.aggregate.finishedAtEpochMs
                + ", totalPorts=" + snap1.aggregate.totalPorts
                + ", totalMessages=" + snap1.aggregate.totalMessages
                + ", percentOverall=" + snap1.aggregate.percentCompleteOverall
                + ", timeRemainingMs=" + snap1.aggregate.timeRemainingMs);
        for (Map.Entry<Integer, SenderSnapshot> e : snap1.perSender.entrySet()) {
            SenderSnapshot s = e.getValue();
            System.out.println("[SNAPSHOT-PAUSED] Port=" + e.getKey()
                    + ", completed=" + s.messagesCompleted + "/" + s.totalPlannedMessages
                    + ", percent=" + s.percentComplete
                    + ", tasksRemaining=" + s.tasksRemaining
                    + ", timeRemainingMs=" + s.timeRemainingMs
                    + ", sending=" + (s.sending == null ? "-" : s.getSending().getId())
                    + ", waitingCount=" + s.waiting.size()
                    + ", completedCount=" + s.completed.size());
        }

        // Also demonstrate direct per-sender snapshot records via the senders map
        TimedComPortSender s20 = manager.getSenders().get(20);
        TimedComPortSender s21 = manager.getSenders().get(21);
        TimedComPortSender s22 = manager.getSenders().get(22);
        TimedComPortSender s23 = manager.getSenders().get(23);
        assertNotNull(s20); assertNotNull(s21); assertNotNull(s22); assertNotNull(s23);

        SenderSnapshot s20before = s20.snapshot(25, 100);
        System.out.println("[SENDER-SNAPSHOT-20-PAUSED] completed=" + s20before.messagesCompleted
                + "/" + s20before.totalPlannedMessages + ", sending=" + (s20before.sending == null ? "-" : s20before.sending.getId())
                + ", waiting=" + s20before.waiting.size() + ", timeRemainingMs=" + s20before.timeRemainingMs);

        // Resume only port 20; the manager remains in pausedAll state for others
        s20.resume();

        // Drive port 20 to completion by completing each active message as it runs
        for (int i = 0; i < 5; i++) {
            Thread.sleep(35); // allow message to enter waiting window
            s20.complete();
        }
        // Give a small buffer for the last message to finalize
        Thread.sleep(80);

        // Assert port 20 finished its group
        SenderSnapshot s20after = s20.snapshot(25, 100);
        assertEquals(5, s20after.messagesCompleted);
        assertEquals(100.0, s20after.percentComplete, 0.0001);
        assertEquals(0, s20after.timeRemainingMs);

        // Manager should NOT be complete yet since other ports are still paused
        ManagerSnapshot snapAfterPort20 = manager.snapshot();
        assertNull(snapAfterPort20.aggregate.finishedAtEpochMs, "Aggregate should not be finished while others paused");
        assertTrue(snapAfterPort20.aggregate.percentCompleteOverall < 100.0, "Overall not 100% yet");

        // Now resume the multi to let remaining ports proceed
        manager.resumeAll().await().atMost(Duration.ofSeconds(10));

        // Drive the remaining three ports to finish quickly via completes
        for (int i = 0; i < 5; i++) {
            Thread.sleep(30);
            s21.complete(); s22.complete(); s23.complete();
        }
        // Allow buffer time for finalization across ports
        Thread.sleep(200);

        // Await combined result and aggregate completion
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertEquals(4, results.size());

        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(agg);
        assertEquals(groupName, agg.groupName);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertEquals(0, agg.timeRemainingMs);
        assertNotNull(agg.finishedAtEpochMs);

        // Print final aggregate summary
        System.out.println("[FINAL-AGGREGATE] group=" + agg.groupName + ", totalPorts=" + agg.totalPorts
                + ", totalMessages=" + agg.totalMessages + ", percentOverall=" + agg.percentCompleteOverall
                + ", timeSavedMs=" + agg.timeSavedMs + ", finishedAt=" + agg.finishedAtEpochMs);

        // Ensure streams emitted some items (allow up to 35 seconds due to environment timing)
        System.out.println("[DEBUG] Before await: status items=" + statusSub.getItems().size() + ", progress items=" + progressSub.getItems().size());
        try {
            statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
            progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
            System.out.println("[DEBUG] After await: status items=" + statusSub.getItems().size() + ", progress items=" + progressSub.getItems().size());
        } catch (AssertionError ae) {
            System.out.println("[DEBUG] Await timed out or failed. Observed so far: status items=" + statusSub.getItems().size() + ", progress items=" + progressSub.getItems().size());
            throw ae;
        }
    }

    private static List<MessageSpec> buildSpecs(String prefix, int count) {
        List<MessageSpec> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            // Slightly varied configs are fine; keep timeouts consistent for speed
            list.add(new MessageSpec(prefix + i, "PAYLOAD-" + prefix + i,
                    new Config(1, 10, 140)));
        }
        return list;
    }
}
