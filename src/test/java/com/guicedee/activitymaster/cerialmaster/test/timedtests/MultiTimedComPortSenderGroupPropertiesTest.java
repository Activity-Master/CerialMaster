package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that per-run custom properties (Map<String, Boolean>) can be provided when enqueuing
 * groups across multiple timed COM port senders, and that they are exposed in the aggregate and snapshot.
 *
 * Port used: 20 (within 20-23 requirement)
 */
public class MultiTimedComPortSenderGroupPropertiesTest {

    @Test
    public void properties_are_attached_to_aggregate_and_snapshot() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        try {
            ComPortConnection<?> c20 = svc.getComPortConnectionDirect(20).await().atMost(Duration.ofSeconds(50));
            c20.connect(); c20.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config cfg = new Config(1, 10, 200);

        // One simple message for COM20
        MessageSpec spec = new MessageSpec("PROPS-20", "PAYLOAD-20", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(20, List.of(spec));

        // Custom properties to attach to the run
        Map<String, Boolean> props = new LinkedHashMap<>();
        props.put("simulate", Boolean.TRUE);
        props.put("auditEnabled", Boolean.FALSE);

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(128));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        String groupName = "Group-With-Props";
        var allUni = manager.enqueueGroupsWithName(groupName, byPort, cfg, props);
        var aggUni = manager.currentRunAggregateUni();

        // Wait for completion
        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(results);
        var agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertNotNull(agg);
        assertEquals(groupName, agg.groupName);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertNotNull(agg.properties);
        assertEquals(props.size(), agg.properties.size());
        assertEquals(props.get("simulate"), agg.properties.get("simulate"));
        assertEquals(props.get("auditEnabled"), agg.properties.get("auditEnabled"));

        // Check snapshot also exposes properties
        ManagerSnapshot snap = manager.snapshot();
        assertNotNull(snap);
        assertNotNull(snap.aggregate);
        assertNotNull(snap.aggregate.properties);
        assertEquals(props.get("simulate"), snap.aggregate.properties.get("simulate"));
        assertEquals(props.get("auditEnabled"), snap.aggregate.properties.get("auditEnabled"));

        // Ensure streams emitted at least one item (allow up to 35 seconds due to environment timing)
        statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
        progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
    }
}
