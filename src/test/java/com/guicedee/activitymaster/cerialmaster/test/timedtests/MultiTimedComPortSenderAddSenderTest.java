package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.SenderSnapshot;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for addSender(...) convenience on MultiTimedComPortSender.
 *
 * Verifies that:
 * - addSender registers a sender for a COM port and attaches streams
 * - repeated addSender returns the same instance (no duplicate)
 * - initial benign status/progress items are emitted and can be awaited
 * - a debounced per-sender publish occurs to sender-1-tasks shortly after adding
 *
 * Port used: 20 (within 20-23 requirement)
 */
public class MultiTimedComPortSenderAddSenderTest {

    static final class PubEvent {
        final long at;
        final String address;
        final Object payload;
        PubEvent(long at, String address, Object payload) {
            this.at = at; this.address = address; this.payload = payload;
        }
    }

    static class TestableMulti extends MultiTimedComPortSender {
        final List<PubEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        @Override
        protected void publishToAddress(String address, Object payload) {
            events.add(new PubEvent(System.currentTimeMillis(), address, payload));
        }
    }

    @Test
    public void addSender_registers_and_emits_initial_events() throws Exception {
        // Ensure COM20 is available through the service (registers with side-effects)
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        try {
            ComPortConnection<?> c20 = svc.getComPortConnectionDirect(20).await().atMost(Duration.ofSeconds(50));
            c20.connect(); c20.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        TestableMulti mgr = new TestableMulti();

        // Subscribe to manager streams; they should emit a benign initial item per design
        var statusSub = mgr.status().subscribe().withSubscriber(AssertSubscriber.create(128));
        var progressSub = mgr.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(256));

        // Add the sender via convenience method (delegates to getOrCreateSender and attaches streams)
        TimedComPortSender s1 = mgr.addSender(20);
        assertNotNull(s1, "Expected sender to be created for COM20");
        assertTrue(mgr.getSenders().containsKey(20), "Manager should contain COM20 after addSender");

        // Calling addSender again should return the same instance
        TimedComPortSender s2 = mgr.addSender(20);
        assertSame(s1, s2, "Repeated addSender should return the same sender instance");

        // Ensure we receive at least one item on both streams (allow up to 35s due to env timing)
        statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
        progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));

        // Allow time for the debounced per-sender snapshot publish (300-700ms randomized)
        Thread.sleep(900);

        // First sender added should be at index 1 => address sender-1-tasks
        List<PubEvent> perSender = new ArrayList<>();
        for (PubEvent e : mgr.events) if ("sender-1-tasks".equals(e.address)) perSender.add(e);
        assertTrue(perSender.size() >= 1, "Expected at least one per-sender publish after addSender and debounce");

        // Snapshot payload type should be TimedComPortSender.SenderSnapshot
        Object lastPayload = perSender.get(perSender.size() - 1).payload;
        assertNotNull(lastPayload);
        assertTrue(lastPayload instanceof SenderSnapshot, "Payload should be a SenderSnapshot");

        // Finally, verify a simple snapshot call includes the sender
        MultiTimedComPortSender.ManagerSnapshot snap = mgr.snapshot();
        assertNotNull(snap);
        assertTrue(snap.perSender.containsKey(20));
    }
}
