package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderOffsetDateTimeTest {

    @Test
    public void messageStat_has_offsetDateTime_fields() throws Exception {
        ComPortConnection<?> conn = ComPortConnection.getOrCreate(40, null);
        TimedComPortSender sender = conn.getOrCreateTimedSender(new Config(1, 5, 40));
        sender.setAttemptFn((c, attempt) -> java.util.concurrent.CompletableFuture.completedFuture(true));

        String id = "MSG-ODT-1";
        String title = "Title-ODT-1";
        String payload = "PAYLOAD";
        MessageSpec spec = new MessageSpec(id, title, payload, new Config(1, 5, 40));

        sender.start(spec);
        Thread.sleep(15);
        sender.complete();
        Thread.sleep(60);

        SenderSnapshot snap = sender.snapshot(10, 10);
        assertNotNull(snap);
        // If a group was started/ended, the OffsetDateTime fields should reflect the ms fields
        if (snap.groupStartedAtEpochMs != null) {
            assertNotNull(snap.groupStartedAt, "Expected groupStartedAt OffsetDateTime when ms present");
        }
        if (snap.groupEndedAtEpochMs != null) {
            assertNotNull(snap.groupEndedAt, "Expected groupEndedAt OffsetDateTime when ms present");
        }
        if (snap.estimatedFinishedAtEpochMs != null) {
            assertNotNull(snap.estimatedFinishedAt, "Expected estimatedFinishedAt OffsetDateTime when ms present");
        }
        if (snap.originallyEstimatedFinishedAtEpochMs != null) {
            assertNotNull(snap.originallyEstimatedFinishedAt, "Expected originallyEstimatedFinishedAt OffsetDateTime when ms present");
        }

        // Completed message should also include ODT fields corresponding to ms
        boolean foundAny = false;
        for (MessageStat ms : snap.completed) {
            if (id.equals(ms.getId())) {
                foundAny = true;
                if (ms.getStartedAtEpochMs() != null) assertNotNull(ms.getStartedAt());
                if (ms.getFinishedAtEpochMs() != null) assertNotNull(ms.getFinishedAt());
                if (ms.getEstimatedFinishedAtEpochMs() != null) assertNotNull(ms.getEstimatedFinishedAt());
                if (ms.getOriginallyEstimatedFinishedAtEpochMs() != null) assertNotNull(ms.getOriginallyEstimatedFinishedAt());
            }
        }
        assertTrue(foundAny, "Expected to find completed message stats for the test id");
    }
}
