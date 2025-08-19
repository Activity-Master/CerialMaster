package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MultiTimedComPortSenderOffsetDateTimeTest {

    @Test
    public void aggregate_has_offsetDateTime_fields() {
        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        int port = 50;
        // Ensure sender exists and set attempt function to avoid serial access
        TimedComPortSender sender = manager.getOrCreateSender(port, new TimedComPortSender.Config(1, 5, 40));
        sender.setAttemptFn((c, a) -> java.util.concurrent.CompletableFuture.completedFuture(true));

        TimedComPortSender.MessageSpec spec = new TimedComPortSender.MessageSpec("AGG-1", "Title-AGG-1", "PAYLOAD", new TimedComPortSender.Config(1, 5, 40));
        Map<Integer, List<TimedComPortSender.MessageSpec>> byPort = Map.of(port, List.of(spec));

        var allUni = manager.enqueueGroupsWithName("Group-ODT", byPort, new TimedComPortSender.Config(1, 5, 40));
        var aggUni = manager.currentRunAggregateUni();

        // Drive to completion quickly
        try {
            Thread.sleep(15);
            sender.complete();
            Thread.sleep(80);
        } catch (InterruptedException ignored) {}

        // Await completion (guard timeouts to prevent flakiness)
        var results = allUni.await().atMost(Duration.ofSeconds(5));
        assertNotNull(results);
        var agg = aggUni.await().atMost(Duration.ofSeconds(5));
        assertNotNull(agg);
        assertNotNull(agg.startedAt);
        assertNotNull(agg.finishedAt);
        if (agg.estimatedFinishedAtEpochMs != null) {
            assertNotNull(agg.estimatedFinishedAt);
        }
        if (agg.originallyEstimatedFinishedAtEpochMs != null) {
            assertNotNull(agg.originallyEstimatedFinishedAt);
        }
    }
}

/**
 * Can you please create an extensive and comprehensive guide on all the capabilities and usage of the com port senders and com port registry,
 * as well as the timed com ports for any AI to be able to pick and use immediately and without error.
 */