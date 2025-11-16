package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Asserts that the Pause5Seconds spec actually consumes ~5 seconds of wait time
 * derived from (assignedRetry x assignedDelayMs) with timeoutMs = 0.
 *
 * The spec currently configures:
 * - assignedRetry = 5
 * - assignedDelayMs = 1000
 * - assignedTimeoutMs = 0
 *
 * Given the sender retries while attemptNum <= assignedRetry, this yields ~5 seconds
 * of delay across the retries, then immediately enters the timeout window (0ms) and
 * completes as TimedOut (since alwaysSucceed is false here).
 */
public class Pause5SecondsWaitsFiveSecondsTest {

    @Test
    public void pauseSpec_waits_aboutFiveSeconds() throws Exception {
        // Ensure serial stack is available; otherwise, skip in CI where serial is not present
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        int port = 23; // pick a stable test port
        try {
            ComPortConnection<?> c = svc.getComPortConnectionDirect(port).await().atMost(Duration.ofSeconds(50));
            c.connect();
            c.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        // Build the Pause5Seconds spec which encodes 5x 1s delay and 0 timeout
        Pause5Seconds<?> spec = new Pause5Seconds<>(port, 1);
        Config cfg = spec.getConfig();
        assertNotNull(cfg, "Pause5Seconds must provide a default Config");
        // Explicitly ensure the spec reflects the expected values
        assertEquals(5, cfg.getAssignedRetry());
        assertEquals(1000L, cfg.assignedDelayMs);
        assertEquals(0L, cfg.assignedTimeoutMs);

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Map<Integer, java.util.List<MessageSpec>> byPort = Map.of(port, java.util.List.of(spec));

        long expectedMs = (long) cfg.getAssignedRetry() * cfg.assignedDelayMs + cfg.assignedTimeoutMs; // 5000ms

        Instant start = Instant.now();
        var allUni = manager.enqueueGroupsWithName("Pause5Seconds-Measure", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(30));
        GroupResult gr = results.get(port);
        assertNotNull(gr);
        assertFalse(gr.results.isEmpty());
        // With timeout=0 and no alwaysSucceed/external completion, terminal should be TimedOut
        TimedComPortSender.State term = gr.results.get(0).terminalState;
        assertTrue(term == TimedComPortSender.State.TimedOut || term == TimedComPortSender.State.Completed,
                "Terminal should be TimedOut (or Completed in rare environments); was " + term);

        // Verify aggregate finished
        var agg = aggUni.await().atMost(Duration.ofSeconds(30));
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        // Allow small negative tolerance for scheduler jitter; still must be ~5 seconds
        long lowerBound = Math.max(0, expectedMs - 400);
        long upperBound = expectedMs + 3500; // generous upper bound to avoid flakes under load/CI
        assertTrue(elapsed >= lowerBound, "Elapsed (" + elapsed + ") should be >= " + lowerBound + " ms");
        assertTrue(elapsed <= upperBound, "Elapsed (" + elapsed + ") should be <= " + upperBound + " ms");
    }
}
