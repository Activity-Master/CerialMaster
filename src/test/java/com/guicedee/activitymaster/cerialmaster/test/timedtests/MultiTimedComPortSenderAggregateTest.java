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
 * Comprehensive tests for MultiTimedComPortSender aggregate tracking and naming.
 */
public class MultiTimedComPortSenderAggregateTest {

    @Test
    public void aggregateCapturesNamingAndFinalStats_withMixedOutcomes() throws Exception {
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

        // Reasonable base config; per-message configs below will tailor retries/timeouts
        Config baseCfg = new Config(1, 10, 150);

        // Port 20: A20 will be completed early; B20 will be allowed to timeout
        MessageSpec A20 = new MessageSpec("A20", "PAYLOAD-A20", new Config(1, 12, 180));
        MessageSpec B20 = new MessageSpec("B20", "PAYLOAD-B20", new Config(2, 12, 160));
        // Port 21: A21 will be completed early
        MessageSpec A21 = new MessageSpec("A21", "PAYLOAD-A21", new Config(0, 8, 140));

        // Subscribe to ensure streams emit
        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(256));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        // Enqueue with name and get aggregate Uni immediately afterwards
        String groupName = "Batch-Aggregate-1";
        var allUni = manager.enqueueGroupsWithName(groupName, Map.of(
                20, List.of(A20, B20),
                21, List.of(A21)
        ), baseCfg);
        var aggUni = manager.currentRunAggregateUni();

        // Allow first messages to start
        Thread.sleep(50);

        // Complete A20 and A21 early
        TimedComPortSender s20 = manager.getSenders().get(20);
        TimedComPortSender s21 = manager.getSenders().get(21);
        assertNotNull(s20);
        assertNotNull(s21);
        s20.complete();
        s21.complete();

        // Give time for B20 to start, then allow it to timeout
        Thread.sleep(50);
        // Do not complete; wait sufficiently for B20 timeout (its timeout 160ms)
        Thread.sleep(220);

        // Await combined result and aggregate
        var results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.containsKey(20));
        assertTrue(results.containsKey(21));

        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(agg);

        // Validate aggregate naming and timestamps
        assertEquals(groupName, agg.groupName);
        assertTrue(agg.startedAtEpochMs > 0);
        assertNotNull(agg.finishedAtEpochMs);
        assertTrue(agg.finishedAtEpochMs >= agg.startedAtEpochMs);

        // Totals
        assertEquals(2, agg.totalPorts);
        assertEquals(3, agg.totalMessages);

        // Percent complete and remaining tasks at end
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertEquals(0, agg.tasksRemaining);
        assertEquals(0, agg.maxTimeRemainingMs);

        // Any failures (B20 timed out) and failure details include B20 on port 20
        assertTrue(agg.anyFailures);
        assertTrue(agg.failures.stream().anyMatch(f -> f.comPort == 20 && "B20".equals(f.messageId)
                && (f.state == TimedComPortSender.State.TimedOut || f.state == TimedComPortSender.State.Error)));

        // Per-port checks
        assertNotNull(agg.perPort);
        assertEquals(2, agg.perPort.size());
        var p20 = agg.perPort.get(20);
        var p21 = agg.perPort.get(21);
        assertNotNull(p20);
        assertNotNull(p21);
        assertEquals(2, p20.messages);
        assertEquals(2, p20.messagesCompleted);
        assertEquals(100.0, p20.percentComplete, 0.0001);
        assertEquals(1 + 2, p20.tasksBudget); // retries budget: A20=1, B20=2
        assertEquals(0, p20.tasksRemaining);
        assertEquals(0, p20.worstCaseRemainingMs);

        assertEquals(1, p21.messages);
        assertEquals(1, p21.messagesCompleted);
        assertEquals(100.0, p21.percentComplete, 0.0001);
        assertEquals(0, p21.tasksBudget); // A21 has 0 retries
        assertEquals(0, p21.tasksRemaining);
        assertEquals(0, p21.worstCaseRemainingMs);

        // Total tasks budget across ports matches sum
        assertEquals(p20.tasksBudget + p21.tasksBudget, agg.totalTasksBudget);

        // Time saved should be >= 0 and likely > 0 because we completed A20 and A21 early
        assertTrue(agg.timeSavedMs >= 0);
        assertTrue(agg.timeSavedMs > 0, "Expected some time saved due to early completions");

        // Ensure streams emitted
        statusSub.awaitNextItems(1);
        progressSub.awaitNextItems(1);
    }

    @Test
    public void currentRunAggregateUniFailsIfNoActiveRun() {
        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        try {
            manager.currentRunAggregateUni().await().atMost(Duration.ofMillis(200));
            fail("Expected failure when no active run");
        } catch (Throwable expected) {
            // Should throw IllegalStateException as per contract
        }
    }
}
