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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that when marking the current message as errored by id, the original MessageSpec is returned.
 * Port used: 22
 */
public class MultiTimedComPortSenderErrorByIdReturnSpecTest {

    @Test
    public void errorById_returnsOriginalSpec() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        try {
            ComPortConnection<?> c22 = svc.getComPortConnectionDirect(22).await().atMost(Duration.ofSeconds(50));
            c22.connect(); c22.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config cfg = new Config(1, 10, 200);

        String id = "RET-SPEC-ERR-BY-ID-22";
        MessageSpec spec = new MessageSpec(id, "TITLE-22", "PAYLOAD-22", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(22, List.of(spec));

        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(256));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && mp.progress != null && id.equals(mp.progress.id)
                    && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                Optional<MessageSpec> ret = manager.markErroredReturningSpec(id, "Test by-id error");
                assertTrue(ret.isPresent(), "Expected returned spec present on error by id");
                assertEquals(spec.getId(), ret.get().getId());
                assertEquals(spec.getTitle(), ret.get().getTitle());
                assertEquals(spec.getPayload(), ret.get().getPayload());
            }
        });

        var allUni = manager.enqueueGroupsWithName("ReturnSpec-Error-ById-22", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(TimedComPortSender.State.Error, results.get(22).results.get(0).terminalState);
        var agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertTrue(agg.anyFailures);

        // Ensure streams emitted at least one item (allow up to 35 seconds due to environment timing)
        statusSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
        progressSub.awaitNextItems(1, 1, Duration.ofSeconds(35));
    }
}
