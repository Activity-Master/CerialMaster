package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderTitleTest {

    @Test
    public void messageStat_contains_title_field() throws Exception {
        // Create or get a connection and sender for a dummy port
        ComPortConnection<?> conn = ComPortConnection.getOrCreate(30, null);
        TimedComPortSender sender = conn.getOrCreateTimedSender(new Config(1, 5, 40));

        // Make attempts always succeed quickly so we can complete promptly
        sender.setAttemptFn((c, attempt) -> java.util.concurrent.CompletableFuture.completedFuture(true));

        // Build a message with an explicit title
        String id = "MSG-1";
        String title = "MyTitle-1";
        String payload = "PAYLOAD-1";
        MessageSpec spec = new MessageSpec(id, title, payload, new Config(1, 5, 40));

        // Start and then complete shortly thereafter
        var resultUni = sender.start(spec);
        Thread.sleep(15);
        sender.complete();
        // wait a little to finalize
        Thread.sleep(60);

        // Snapshot and assert title appears in completed stats
        SenderSnapshot snap = sender.snapshot(10, 10);
        boolean found = snap.completed.stream().anyMatch(ms -> id.equals(ms.getId()) && title.equals(ms.getTitle()));
        assertTrue(found, "Expected completed MessageStat to contain the provided title");

        // Also verify the terminal result contains the title
        MessageResult res = resultUni.await().atMost(Duration.ofSeconds(2));
        assertNotNull(res);
        assertEquals(id, res.id);
        assertEquals(title, res.title);
    }
}
