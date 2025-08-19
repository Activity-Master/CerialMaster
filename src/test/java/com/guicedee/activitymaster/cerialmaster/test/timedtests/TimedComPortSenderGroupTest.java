package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderGroupTest {

    @Test
    public void enqueueGroup_fifo_and_groupUniCompletes() throws Exception {
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
        TimedComPortSender sender = conn.getOrCreateTimedSender(new TimedComPortSender.Config(0, 5, 100));

        // Subscribe to messageProgress to observe ids flowing
        var progressSub = sender.messageProgress().subscribe().withSubscriber(AssertSubscriber.create(100));

        // Prepare a FIFO group of three messages, each with its own config
        var msgA = new TimedComPortSender.MessageSpec("A", "PAYLOAD-A", new TimedComPortSender.Config(0, 10, 120));
        var msgB = new TimedComPortSender.MessageSpec("B", "PAYLOAD-B", new TimedComPortSender.Config(0, 10, 80));
        var msgC = new TimedComPortSender.MessageSpec("C", "PAYLOAD-C", new TimedComPortSender.Config(0, 10, 150));

        var groupUni = sender.enqueueGroup(List.of(msgA, msgB, msgC));

        // Let A enter waiting-for-completion, then complete externally
        Thread.sleep(30);
        sender.complete(); // A Completed

        // Let B timeout naturally
        Thread.sleep(120); // > msgB timeout 80ms cushion

        // C: complete within its timeout window
        Thread.sleep(30);
        sender.complete();

        TimedComPortSender.GroupResult groupResult = groupUni.await().indefinitely();
        assertNotNull(groupResult);
        assertEquals(3, groupResult.results.size());

        assertEquals("A", groupResult.results.get(0).id);
        assertEquals(TimedComPortSender.State.Completed, groupResult.results.get(0).terminalState);
        assertEquals("B", groupResult.results.get(1).id);
        assertEquals(TimedComPortSender.State.TimedOut, groupResult.results.get(1).terminalState);
        assertEquals("C", groupResult.results.get(2).id);
        assertEquals(TimedComPortSender.State.Completed, groupResult.results.get(2).terminalState);

        // Ensure progress stream published some entries with IDs
        progressSub.awaitNextItems(3);
    }
}
