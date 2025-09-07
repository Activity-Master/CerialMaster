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
 * Tests covering ManagerSnapshot and the estimation/timeRemaining fields at all levels.
 */
public class MultiTimedComPortSenderSnapshotTest {

    @Test
    public void managerSnapshot_and_estimations_across_levels() throws Exception {
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
        Config baseCfg = new Config(1, 12, 160);

        // Plan: Port 20 has two messages; Port 21 has one. We'll complete first messages early and let one timeout.
        MessageSpec A20 = new MessageSpec("A20-SNP", "PAYLOAD-A20", new Config(1, 12, 180));
        MessageSpec B20 = new MessageSpec("B20-SNP", "PAYLOAD-B20", new Config(2, 12, 160));
        MessageSpec A21 = new MessageSpec("A21-SNP", "PAYLOAD-A21", new Config(0, 8, 140));

        // Subscribe to ensure streams are alive
        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(256));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        // Kick off with a group name
        String groupName = "Batch-Snapshot-1";
        var allUni = manager.enqueueGroupsWithName(groupName, Map.of(
                20, List.of(A20, B20),
                21, List.of(A21)
        ), baseCfg);

        // Let first messages start
        Thread.sleep(60);

        // Capture an in-flight snapshot; should show non-zero timeRemaining at aggregate and per-port
        ManagerSnapshot snapMid = manager.snapshot();
        assertNotNull(snapMid);
        assertNotNull(snapMid.aggregate);
        assertEquals(groupName, snapMid.aggregate.groupName);
        assertTrue(snapMid.aggregate.startedAtEpochMs > 0);
        assertNull(snapMid.aggregate.finishedAtEpochMs, "Aggregate should not be finished yet");
        assertTrue(snapMid.aggregate.maxTimeRemainingMs >= 0);
        assertEquals(snapMid.aggregate.maxTimeRemainingMs, snapMid.aggregate.timeRemainingMs);
        assertNotNull(snapMid.perSender);
        assertTrue(snapMid.perSender.containsKey(20));
        assertTrue(snapMid.perSender.containsKey(21));
        var s20mid = snapMid.perSender.get(20);
        var s21mid = snapMid.perSender.get(21);
        assertNotNull(s20mid);
        assertNotNull(s21mid);
        // Verify alias for timeRemainingMs
        assertEquals(s20mid.worstCaseRemainingMs, s20mid.timeRemainingMs);
        assertEquals(s21mid.worstCaseRemainingMs, s21mid.timeRemainingMs);
        // Verify sending is at most 1
        assertTrue(s20mid.sending == null || s20mid.getSending().getId() != null);
        assertTrue(s21mid.sending == null || s21mid.getSending().getId() != null);
        // Verify waiting/completed bounds
        assertTrue(s20mid.waiting.size() <= 25);
        assertTrue(s20mid.completed.size() <= 100);
        assertTrue(s21mid.waiting.size() <= 25);
        assertTrue(s21mid.completed.size() <= 100);
        // Verify message-level fields include timeRemainingMs and estimates (when sending or waiting)
        if (s20mid.sending != null) {
            assertTrue(s20mid.getSending().getTimeRemainingMs() >= 0);
            assertNotNull(s20mid.getSending().getEstimatedFinishedAtEpochMs());
            // originallyEstimated may be null if not started yet (should be set when started) — allow null-or-positive
        }
        if (!s20mid.waiting.isEmpty()) {
            MessageStat w = s20mid.waiting.get(0);
            assertTrue(w.getTimeRemainingMs() >= 0);
            assertNotNull(w.getEstimatedFinishedAtEpochMs());
        }

        // Perform early completions
        TimedComPortSender s20 = manager.getSenders().get(20);
        TimedComPortSender s21 = manager.getSenders().get(21);
        assertNotNull(s20);
        assertNotNull(s21);
        s20.complete();
        s21.complete();

        // Allow B20 to start and finish via timeout
        Thread.sleep(50);
        Thread.sleep(220);

        // Wait for run completion and aggregate
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertEquals(2, results.size());

        // Final snapshot after completion
        ManagerSnapshot snapEnd = manager.snapshot();
        assertNotNull(snapEnd.aggregate.finishedAtEpochMs);
        assertEquals(0, snapEnd.aggregate.maxTimeRemainingMs);
        assertEquals(0, snapEnd.aggregate.timeRemainingMs);
        assertEquals(100.0, snapEnd.aggregate.percentCompleteOverall, 0.0001);
        // Estimated finished should collapse to finished when done
        assertEquals(snapEnd.aggregate.finishedAtEpochMs, snapEnd.aggregate.estimatedFinishedAtEpochMs);
        assertNotNull(snapEnd.aggregate.originallyEstimatedFinishedAtEpochMs);
        assertTrue(snapEnd.aggregate.originallyEstimatedFinishedAtEpochMs >= snapEnd.aggregate.startedAtEpochMs);

        // Per-port end assertions: remaining 0 and alias equals 0
        var s20end = snapEnd.perSender.get(20);
        var s21end = snapEnd.perSender.get(21);
        assertEquals(0, s20end.worstCaseRemainingMs);
        assertEquals(0, s21end.worstCaseRemainingMs);
        assertEquals(0, s20end.timeRemainingMs);
        assertEquals(0, s21end.timeRemainingMs);
        assertNotNull(s20end.estimatedFinishedAtEpochMs);
        assertNotNull(s20end.originallyEstimatedFinishedAtEpochMs);
        // If group ended, estimated should equal endedAt
        if (s20end.groupEndedAtEpochMs != null) {
            assertEquals(s20end.groupEndedAtEpochMs, s20end.estimatedFinishedAtEpochMs);
        }

        // Completed list contains terminal messages with timeRemainingMs == 0 and estimates present
        if (!s20end.completed.isEmpty()) {
            MessageStat last = s20end.completed.get(s20end.completed.size() - 1);
            assertEquals(0L, last.getTimeRemainingMs());
            assertNotNull(last.getEstimatedFinishedAtEpochMs());
        }

        // Ensure streams emitted
        statusSub.awaitNextItems(1);
        progressSub.awaitNextItems(1);
    }
}
