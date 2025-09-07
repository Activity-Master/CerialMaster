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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MultiTimedComPortSenderManualRetryTest
{

    @Test
    public void manualRetryResetsAggregateAndCompletes() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> c23;
        try {
            c23 = svc.getComPortConnectionDirect(23).await().atMost(Duration.ofSeconds(50));
            c23.connect(); c23.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config baseCfg = new Config(0, 10, 140); // 0 retries, 140ms timeout

        // Single message that will time out on first run if we don't complete early
        MessageSpec M = new MessageSpec("AUTO-RETRY-1", "PAYLOAD", new Config(0, 10, 140));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(256));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(1024));

        // Track starts for the message; on the second start (manual retry run), complete it to succeed
        AtomicInteger starts = new AtomicInteger(0);
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && "AUTO-RETRY-1".equals(mp.progress.id) && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                int s = starts.incrementAndGet();
                if (s >= 2) {
                    // Second time we see it start: trigger completion so retry succeeds
                    TimedComPortSender s23 = manager.getSenders().get(23);
                    if (s23 != null) s23.complete();
                }
            }
        });

        var combinedUni = manager.enqueueGroups(Map.of(23, List.of(M)), baseCfg);
        var aggUni1 = manager.currentRunAggregateUni();

        // Let first attempt run and timeout
        Thread.sleep(250);

        // The primary combinedUni will complete at end of first run
        Map<Integer, GroupResult> firstResults = combinedUni.await().atMost(Duration.ofSeconds(10));
        assertNotNull(firstResults);
        assertTrue(firstResults.containsKey(23));

        // Get first run aggregate
        AggregateProgress agg1 = aggUni1.await().atMost(Duration.ofSeconds(10));
        assertNotNull(agg1);
        assertNotNull(agg1.finishedAtEpochMs);
        assertTrue(agg1.anyFailures);

        // Manually retry failed messages via single-call API; this should RESET aggregate tracking
        var combinedUni2 = manager.retryLastFailures();
        var aggUni2 = manager.currentRunAggregateUni();

        // Let second attempt start and auto-complete via our subscriber (starts >= 2)
        Thread.sleep(80);

        // Await second run completion and aggregate
        Map<Integer, GroupResult> secondResults = combinedUni2.await().atMost(Duration.ofSeconds(10));
        assertNotNull(secondResults);
        AggregateProgress agg2 = aggUni2.await().atMost(Duration.ofSeconds(10));
        assertNotNull(agg2);
        assertEquals(100.0, agg2.percentCompleteOverall, 0.0001);
        assertEquals(0, agg2.timeRemainingMs);
        assertNotNull(agg2.finishedAtEpochMs);
        // Verify reset: new run started after the previous finished
        assertTrue(agg2.startedAtEpochMs >= agg1.finishedAtEpochMs);

        // Streams emitted
        statusSub.awaitNextItems(1);
        progressSub.awaitNextItems(1);
    }
}
