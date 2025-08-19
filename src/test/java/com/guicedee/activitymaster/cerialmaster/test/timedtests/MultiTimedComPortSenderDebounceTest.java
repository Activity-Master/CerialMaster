package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.SenderSnapshot;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for debounced event publishing from MultiTimedComPortSender.
 * These tests avoid any real serial access by configuring attemptFn to complete immediately.
 */
public class MultiTimedComPortSenderDebounceTest {

    static final class PubEvent {
        final long at;
        final String address;
        final Object payload;
        PubEvent(long at, String address, Object payload) {
            this.at = at; this.address = address; this.payload = payload;
        }
    }

    static class TestableMulti extends MultiTimedComPortSender {
        final List<PubEvent> events = new CopyOnWriteArrayList<>();
        @Override
        protected void publishToAddress(String address, Object payload) {
            events.add(new PubEvent(System.currentTimeMillis(), address, payload));
        }
    }

    @Test
    public void aggregate_is_debounced_and_no_duplicate_for_same_state() throws Exception {
        TestableMulti mgr = new TestableMulti();
        long t0 = System.currentTimeMillis();
        // Two identical requests close together should result in a single publish within ~300-700ms
        mgr.setGroupName("G-1");
        Thread.sleep(10);
        mgr.setGroupName("G-1");
        // Allow enough time for the debounced task to execute
        Thread.sleep(900);
        // Filter events for aggregate address
        List<PubEvent> agg = new ArrayList<>();
        for (PubEvent e : mgr.events) if ("server-task-updates".equals(e.address)) agg.add(e);
        assertTrue(agg.size() >= 1, "Expected at least one aggregate publish");
        // Ensure first event occurred at least ~300ms after first request
        long firstDelay = agg.get(0).at - t0;
        assertTrue(firstDelay >= 250 && firstDelay <= 1100, "Publish should be debounced into ~300-700ms window (observed=" + firstDelay + ")");
        // Because state didn't change between requests, only one publish should have occurred (no duplicate consecutive identical)
        assertEquals(1, agg.size(), "Expected only one aggregate publish for identical consecutive requests");
    }

    @Test
    public void sender_publishes_debounced_and_sends_latest_snapshot() throws Exception {
        TestableMulti mgr = new TestableMulti();
        int port = 77;
        TimedComPortSender sender = mgr.getOrCreateSender(port, new TimedComPortSender.Config(1, 5, 40));
        sender.setAttemptFn((c, a) -> java.util.concurrent.CompletableFuture.completedFuture(true));

        // Enqueue two messages to the same port so that state changes during the debounce window
        TimedComPortSender.MessageSpec m1 = new TimedComPortSender.MessageSpec("M1", "Title-1", "PAY", new TimedComPortSender.Config(1, 5, 40));
        TimedComPortSender.MessageSpec m2 = new TimedComPortSender.MessageSpec("M2", "Title-2", "PAY", new TimedComPortSender.Config(1, 5, 40));
        Map<Integer, List<TimedComPortSender.MessageSpec>> byPort = Map.of(port, List.of(m1, m2));

        // Start the run
        var all = mgr.enqueueGroupsWithName("Run-DB", byPort, new TimedComPortSender.Config(1, 5, 40));

        // Quickly complete the first message so the second becomes active before the debounce timer fires
        Thread.sleep(20);
        sender.complete(); // completes current (first) message
        // Do NOT complete the second immediately; let it be the latest 'sending' when the publish fires

        // Wait enough for at least one debounced publish to happen
        Thread.sleep(900);

        // Find per-sender address events (sender is first managed => index 1)
        List<PubEvent> perSender = new ArrayList<>();
        for (PubEvent e : mgr.events) if ("sender-1-tasks".equals(e.address)) perSender.add(e);
        assertTrue(perSender.size() >= 1, "Expected at least one per-sender publish");

        // Inspect the last published snapshot payload to ensure it reflects the latest state (second message active or completed)
        Object lastPayload = perSender.get(perSender.size() - 1).payload;
        assertNotNull(lastPayload, "Expected non-null payload for per-sender publish");
        assertTrue(lastPayload instanceof SenderSnapshot, "Payload should be a SenderSnapshot");
        SenderSnapshot snap = (SenderSnapshot) lastPayload;
        // Either sending is present with id M2, or M2 appears in completed if it finished quickly
        boolean ok = false;
        if (snap.sending != null && "M2".equals(snap.sending.id)) ok = true;
        if (!ok) {
            ok = snap.completed.stream().anyMatch(ms -> "M2".equals(ms.id));
        }
        assertTrue(ok, "Expected the latest publish to reflect the later message M2 as active or completed");

        // Finish the run cleanly to avoid leaks
        sender.complete();
        all.await().atMost(Duration.ofSeconds(5));
    }

    @Test
    public void sender_no_duplicate_when_no_state_change() throws Exception {
        TestableMulti mgr = new TestableMulti();
        int port = 88;
        TimedComPortSender sender = mgr.getOrCreateSender(port, new TimedComPortSender.Config(1, 5, 40));
        sender.setAttemptFn((c, a) -> java.util.concurrent.CompletableFuture.completedFuture(true));

        // Trigger a single status change (pause) which will schedule a publish
        sender.pause();
        // Repeated pause should not emit another status (already paused), so no additional scheduling/change
        sender.pause();

        Thread.sleep(900);

        // Check published events for this sender (index 1 for this isolated manager)
        List<PubEvent> perSender = new ArrayList<>();
        for (PubEvent e : mgr.events) if ("sender-1-tasks".equals(e.address)) perSender.add(e);
        assertTrue(perSender.size() >= 1, "Expected at least one per-sender publish after pause");
        assertEquals(1, perSender.size(), "Expected only one per-sender publish due to no subsequent state change");
    }
}
