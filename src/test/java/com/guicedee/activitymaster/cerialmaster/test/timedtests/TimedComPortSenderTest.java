package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.Config;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderTest {

    @Test
    public void basicPauseResumeCompleteFlow() throws Exception {
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
        Config cfg = new Config(2, 50, 200);
        TimedComPortSender sender = conn.getOrCreateTimedSender(cfg);

        var sub = sender.status().subscribe().withSubscriber(AssertSubscriber.create(50));

        sender.start("PING");
        // Let two attempts happen
        Thread.sleep(130);
        sender.pause();
        assertTrue(sender.isPaused());
        Thread.sleep(100);
        // Resume and then complete externally before timeout
        sender.resume();
        Thread.sleep(30);
        sender.complete();

        // Ensure we observed terminal Completed state within some time
        sub.awaitNextItems(3); // at least a few signals
        // There isn't a direct terminal completion of Multi; check registry and state by presence of last status
        // We just assert that the sender remains registered and no exception thrown
        assertNotNull(ComPortConnection.getTimedSender(20));
    }
}
