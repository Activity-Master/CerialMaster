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
 * Validates that when alwaysSucceed is enabled, the message completes successfully
 * after the full budget of (retries x delayMs) + timeoutMs elapses, using the Pause5Seconds spec.
 */
public class Pause5SecondsAlwaysSucceedTest {

    @Test
    public void completesAfterBudget_whenAlwaysSucceedTrue() throws Exception {
        // Ensure serial stack is available; otherwise, skip to keep CI stable in environments without COM access
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        int port = 22; // choose a stable port used across tests
        try {
            ComPortConnection<?> c = svc.getComPortConnectionDirect(port).await().atMost(Duration.ofSeconds(50));
            c.connect();
            c.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();

        // Use provided Pause5Seconds spec and enable alwaysSucceed. Keep defaults for timeout to keep total around ~8s
        Pause5Seconds<?> spec = new Pause5Seconds<>(port, 1);
        // Ensure per-message config exists and enable alwaysSucceed via config (propagates to runtime)
        Config cfg = spec.getConfig();
        if (cfg == null) {
            cfg = new Config().setAssignedRetry(5).setAssignedDelayMs(1000).setAssignedTimeoutMs(3000);
            spec.setConfig(cfg);
        }
        cfg.setAlwaysSucceed(true);
        spec.setAlwaysSucceed(true); // also set on message to be explicit

        Map<Integer, List<MessageSpec>> byPort = Map.of(port, List.of(spec));

        long expectedMinMs = (long) cfg.getAssignedRetry() * cfg.assignedDelayMs + cfg.assignedTimeoutMs;

        Instant start = Instant.now();
        var allUni = manager.enqueueGroupsWithName("Pause5Seconds-AlwaysSucceed", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(60));
        var result = results.get(port);
        assertNotNull(result, "Result for port must be present");
        assertFalse(result.results.isEmpty(), "Expected one message result");
        assertEquals(TimedComPortSender.State.Completed, result.results.get(0).terminalState, "Should be marked Completed due to alwaysSucceed");

        // Verify aggregate finished
        var agg = aggUni.await().atMost(Duration.ofSeconds(60));
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        // Allow some tolerance for scheduling/CI timing jitter; should be at least budget minus 500ms
        assertTrue(elapsed >= Math.max(0, expectedMinMs - 500),
                "Elapsed (" + elapsed + ") should be >= budget (" + expectedMinMs + ") minus tolerance");
    }
}
