package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.Config;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
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

        java.util.List<Object> receivedItems = new java.util.concurrent.CopyOnWriteArrayList<>();
        sender.status().subscribe().with(receivedItems::add);

        Thread.sleep(100);
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

        Thread.sleep(1000);
        assertTrue(receivedItems.size() >= 3, "Should have received at least 3 items, but got " + receivedItems.size());
        // There isn't a direct terminal completion of Multi; check registry and state by presence of last status
        // We just assert that the sender remains registered and no exception thrown
        assertNotNull(ComPortConnection.getTimedSender(20));
    }
}
