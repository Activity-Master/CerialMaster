package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.Config;
import com.guicedee.activitymaster.cerialmaster.client.TimedComPortSender;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderDemoTest {

    @Test
    public void openCom20_and_sendFiveMessages_withVariousOutcomes() throws Exception {
        ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
        ComPortConnection<?> conn = null;
        try {
            conn = svc.getComPortConnectionDirect(20).await().indefinitely();
            // Probe availability; if it fails due to environment, skip this test.
            conn.connect();
            conn.disconnect();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Serial not available or blocked in test environment: " + t.getMessage());
            return;
        }

        Config defaultCfg = new Config(2, 30, 200);
        TimedComPortSender sender = conn.getOrCreateTimedSender(defaultCfg);

        var sub = sender.status().subscribe().withSubscriber(AssertSubscriber.create(200));

        Runnable awaitSomeSignals = () -> sub.awaitNextItems(1);

        // 1) Immediate external completion
        sender.start("MSG-1", new Config(0, 10, 200));
        Thread.sleep(20);
        sender.complete();
        Thread.sleep(20);

        // 2) Allow timeout by not completing
        sender.start("MSG-2", new Config(1, 20, 120));
        Thread.sleep(200); // enough time to attempt and time out

        // 3) Start and complete during window
        sender.start("MSG-3", new Config(1, 20, 200));
        Thread.sleep(40);
        sender.complete();
        Thread.sleep(20);

        // 4) Start, let it wait, then complete
        sender.start("MSG-4", new Config(0, 10, 200));
        Thread.sleep(50);
        sender.complete();
        Thread.sleep(20);

        // 5) Start and complete quickly
        sender.start("MSG-5", new Config(0, 10, 150));
        Thread.sleep(10);
        sender.complete();
        Thread.sleep(20);

        assertNotNull(ComPortConnection.getTimedSender(20));
        awaitSomeSignals.run();
    }
}
