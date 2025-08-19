package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.AggregateProgress;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates how to mark a message as retrieved (success) using only its id.
 * You do not need to keep a reference to the message payload/spec beforehand.
 *
 * Flow:
 * - Pre-register a Uni awaiting the terminal result for a given id via TimedComPortSender.onMessageResult(id).
 * - Enqueue a group containing that id on the MultiTimedComPortSender.
 * - When the external system recognizes the id as received (simulated by test via progress "Starting"),
 *   call sender.complete() to mark it as Completed.
 */
public class MultiTimedComPortSenderCompleteByIdTest {

    @Test
    public void completeById_withoutKnowingMessagePayload() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> conn22;
        try {
            conn22 = svc.getComPortConnectionDirect(22).await().atMost(Duration.ofSeconds(50));
            conn22.connect();
            conn22.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        // Keep retries 0 and a small timeout; we will complete early using only the id
        TimedComPortSender.Config baseCfg = new TimedComPortSender.Config(0, 10, 200);

        String messageId = "COMPLETE-BY-ID-24";
        TimedComPortSender.MessageSpec spec = new TimedComPortSender.MessageSpec(messageId, "PAYLOAD-ID-ONLY", new TimedComPortSender.Config(0, 10, 200));

        // Subscribe to manager streams (ensures they are active and also gives us debug if needed)
        var statusSub = manager.status().subscribe().withSubscriber(AssertSubscriber.create(128));
        var progressSub = manager.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(512));

        // Obtain the per-port sender and PRE-REGISTER a completion Uni by id — we don't need to know the message content
        TimedComPortSender sender22 = manager.getOrCreateSender(22, baseCfg);
        var messageResultUni = sender22.onMessageResult(messageId);

        // Simulate an external receiver: when we see the message with id start, mark it completed
        manager.messageProgress().subscribe().with(mp -> {
            if (mp != null && mp.progress != null && messageId.equals(mp.progress.id) && mp.progress.note != null && mp.progress.note.contains("Starting")) {
                // We only know the id; mark current message on this sender as completed
                sender22.complete();
            }
        });

        // Enqueue the group containing the spec; no need to keep the spec reference beyond enqueue
        var allUni = manager.enqueueGroups(Map.of(22, List.of(spec)), baseCfg);
        var aggUni = manager.currentRunAggregateUni();

        // Await the specific message's result via the pre-registered id-based Uni
        TimedComPortSender.MessageResult msgResult = messageResultUni.await().atMost(Duration.ofSeconds(10));
        assertNotNull(msgResult);
        assertEquals(messageId, msgResult.id);
        assertEquals(TimedComPortSender.State.Completed, msgResult.terminalState);

        // Also wait for the entire run to finish and check aggregate shows success
        Map<Integer, TimedComPortSender.GroupResult> results = allUni.await().atMost(Duration.ofSeconds(10));
        assertNotNull(results);
        AggregateProgress agg = aggUni.await().atMost(Duration.ofSeconds(10));
        assertNotNull(agg);
        assertEquals(100.0, agg.percentCompleteOverall, 0.0001);
        assertFalse(agg.anyFailures, "No failures expected when completing by id");

        // Ensure streams emitted some events
        statusSub.awaitNextItems(1);
        progressSub.awaitNextItems(1);
    }
}
