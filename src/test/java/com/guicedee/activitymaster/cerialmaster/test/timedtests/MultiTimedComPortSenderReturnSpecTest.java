package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that when marking the current message as completed/errored, the original MessageSpec is returned.
 * Ports used: 20 and 21
 */
public class MultiTimedComPortSenderReturnSpecTest {

    @Test
    public void complete_returnsOriginalSpec_byPort20() throws Exception {
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

        MessageSpec spec = new MessageSpec("RET-SPEC-COMP-20", "TITLE-20", "PAYLOAD-20", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(20, List.of(spec));

        // When it starts, call the API that returns the spec
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && Integer.valueOf(20).equals(mp.comPort)
                    && mp.progress != null && spec.getId().equals(mp.progress.id)
                    && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                Optional<MessageSpec> ret = manager.markCompletedReturningSpec(20);
                assertTrue(ret.isPresent(), "Expected returned spec present on completion");
                assertEquals(spec.getId(), ret.get().getId());
                assertEquals(spec.getTitle(), ret.get().getTitle());
                assertEquals(spec.getPayload(), ret.get().getPayload());
            }
        });

        var allUni = manager.enqueueGroupsWithName("ReturnSpec-Complete-20", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(TimedComPortSender.State.Completed, results.get(20).results.get(0).terminalState);
        var agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
    }

    @Test
    public void error_returnsOriginalSpec_byPort21() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        try {
            ComPortConnection<?> c21 = svc.getComPortConnectionDirect(21).await().atMost(Duration.ofSeconds(50));
            c21.connect(); c21.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config cfg = new Config(1, 10, 200);

        MessageSpec spec = new MessageSpec("RET-SPEC-ERR-21", "TITLE-21", "PAYLOAD-21", cfg);
        Map<Integer, List<MessageSpec>> byPort = Map.of(21, List.of(spec));

        // When it starts, call the API that returns the spec for error
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && Integer.valueOf(21).equals(mp.comPort)
                    && mp.progress != null && spec.getId().equals(mp.progress.id)
                    && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                Optional<MessageSpec> ret = manager.markErroredReturningSpec(21, "Test error");
                assertTrue(ret.isPresent(), "Expected returned spec present on error");
                assertEquals(spec.getId(), ret.get().getId());
                assertEquals(spec.getTitle(), ret.get().getTitle());
                assertEquals(spec.getPayload(), ret.get().getPayload());
            }
        });

        var allUni = manager.enqueueGroupsWithName("ReturnSpec-Error-21", byPort, cfg);
        var aggUni = manager.currentRunAggregateUni();

        Map<Integer, GroupResult> results = allUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(TimedComPortSender.State.Error, results.get(21).results.get(0).terminalState);
        var agg = aggUni.await().atMost(Duration.ofSeconds(50));
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertTrue(agg.anyFailures);
    }
}
