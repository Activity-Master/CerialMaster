package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates binding to each message to print its id and retry status, and attaching a Uni for the
 * whole group to print overall success/failure.
 */
public class TimedComPortSenderBindAndPrintTest {

    @Test
    public void bindPerMessage_andPrintRetryStatus_andGroupOutcome() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> conn;
        try {
            conn = svc.getComPortConnectionDirect(20).await().indefinitely();
            conn.connect();
            conn.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }
        TimedComPortSender sender = conn.getOrCreateTimedSender(new Config(2, 15, 120));

        // Subscribe to messageProgress to print each message id and its retry status per attempt
        var progressSub = sender.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(200));
        sender.messageProgress().subscribe().with(mp -> {
            if (mp != null && mp.id != null) {
                // Print id and retry/attempt status message
                System.out.println("[MessageProgress] id=" + mp.id + ", attempt=" + mp.attempt + ", state=" + mp.state + ", note=" + mp.note);
            }
        });

        // Create three messages for a group
        var A = new MessageSpec("A", "PAYLOAD-A", new Config(1, 15, 150));
        var B = new MessageSpec("B", "PAYLOAD-B", new Config(1, 15, 80));
        var C = new MessageSpec("C", "PAYLOAD-C", new Config(2, 15, 150));

        // Register per-message result listeners that print terminal outcomes
        sender.onMessageResult("A").subscribe().with(res -> {
            System.out.println("[MessageResult] id=" + res.id + ", terminalState=" + res.terminalState + ", attempts=" + res.attempts);
        });
        sender.onMessageResult("B").subscribe().with(res -> {
            System.out.println("[MessageResult] id=" + res.id + ", terminalState=" + res.terminalState + ", attempts=" + res.attempts);
        });
        sender.onMessageResult("C").subscribe().with(res -> {
            System.out.println("[MessageResult] id=" + res.id + ", terminalState=" + res.terminalState + ", attempts=" + res.attempts);
        });

        // Enqueue the group and attach a Uni to print group success/failure when done
        var groupUni = sender.enqueueGroup(List.of(A, B, C));
        groupUni.subscribe().with(group -> {
            boolean allCompleted = group.results.stream().allMatch(r -> r.terminalState == TimedComPortSender.State.Completed);
            System.out.println("[GroupResult] overall=" + (allCompleted ? "SUCCESS" : "FAILURE")
                    + ", details=" + group.results.size() + " messages");
        });

        // Drive outcomes: complete A externally; allow B to timeout; complete C externally
        Thread.sleep(40); // let A start and enter waiting-for-completion
        sender.complete();

        Thread.sleep(120); // allow B to proceed and timeout (B timeout is 80ms)

        Thread.sleep(40); // C starts and enters waiting window
        sender.complete();

        // Await some progress signals to ensure stream emitted
        progressSub.awaitNextItems(3);

        // Also wait for group to fully complete and inspect outcome programmatically
        GroupResult result = groupUni.await().indefinitely();
        assertEquals(3, result.results.size());
        assertEquals("A", result.results.get(0).id);
        assertEquals(TimedComPortSender.State.Completed, result.results.get(0).terminalState);
        assertEquals("B", result.results.get(1).id);
        assertEquals(TimedComPortSender.State.TimedOut, result.results.get(1).terminalState);
        assertEquals("C", result.results.get(2).id);
        assertEquals(TimedComPortSender.State.Completed, result.results.get(2).terminalState);
    }
}
