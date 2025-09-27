package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that enqueueGroupsWithName completes even when MessageSpec ids are null.
 * Uses a stub attempt function to avoid touching real serial ports.
 */
public class MultiTimedComPortSenderNullIdSpecTest {

    @Test
    public void completes_with_null_ids_when_chained() {
        MultiTimedComPortSender manager = new MultiTimedComPortSender();

        // Prepare four senders with stub attempt functions
        int[] ports = new int[] { 20, 21, 22, 23 };
        for (int p : ports) {
            TimedComPortSender s = manager.addSender(p, new Config(0, 1, 5));
            // Stub attempt: immediately claim attempt success; timeout window is tiny, we will also force completes
            s.setAttemptFn((conn, attempt) -> CompletableFuture.completedFuture(true));
        }

        // Build specs with NULL ids
        Map<Integer, List<MessageSpec>> byPort = new LinkedHashMap<>();
        for (int p : ports) {
            List<MessageSpec> specs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                // Intentionally leave id null
                MessageSpec spec = new MessageSpec(null, "PAYLOAD-" + p + "-" + i, new Config(0, 1, 5));
                specs.add(spec);
            }
            byPort.put(p, specs);
        }

        String name = "NullId-Group";
        var allUni = manager.enqueueGroupsWithName(name, byPort, new Config(0, 1, 5));
        var aggUni = manager.currentRunAggregateUni();

        // Since attempts succeed immediately and timeout is small, we can drive completion by marking completes
        // to avoid waiting for timeout windows, ensuring quick test.
        Map<Integer, TimedComPortSender> senders = manager.getSenders();
        for (int i = 0; i < 3; i++) {
            for (int p : ports) {
                TimedComPortSender s = senders.get(p);
                // give a tiny scheduling window
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                s.complete();
            }
        }

        // Await all results. If ids are handled correctly, this should complete quickly rather than hang.
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(5));
        assertEquals(4, results.size());

        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(5));
        assertNotNull(agg);
        assertEquals(name, agg.groupName);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertEquals(0, agg.timeRemainingMs);
    }
}
