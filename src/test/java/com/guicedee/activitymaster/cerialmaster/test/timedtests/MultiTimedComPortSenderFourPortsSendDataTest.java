package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.fazecast.jSerialComm.SerialPort;
import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test that sends data on COM20-23 and verifies reception on COM10-13.
 * This test requires virtual null-modem pairs configured as:
 *   20 -> 10, 21 -> 11, 22 -> 12, 23 -> 13
 * If the environment doesn't provide these ports, the test will be skipped.
 */
public class MultiTimedComPortSenderFourPortsSendDataTest {

    private static SerialPort openReceiver(int portNum) {
        SerialPort port = SerialPort.getCommPort("COM" + portNum);
        port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        if (!port.openPort()) {
            return null;
        }
        port.clearRTS();
        port.clearDTR();
        // clear any stale bytes
        try { while (port.bytesAvailable() > 0) { byte[] tmp = new byte[Math.min(256, port.bytesAvailable())]; port.readBytes(tmp, tmp.length); } } catch (Exception ignored) {}
        return port;
    }

    @Test
    public void sendDataToFourPorts_and_receive_on_paired_ports() throws Exception {
        int[][] pairs = new int[][] { {20,10}, {21,11}, {22,12}, {23,13} };

        // Try opening receivers; skip test if not available
        List<SerialPort> receivers = new ArrayList<>();
        for (int[] p : pairs) {
            SerialPort sp = openReceiver(p[1]);
            if (sp == null) {
                for (SerialPort r : receivers) { try { r.closePort(); } catch (Throwable ignored) {} }
                Assumptions.assumeTrue(false, "Required receiver port COM" + p[1] + " not available; skipping.");
                return;
            }
            receivers.add(sp);
        }

        try {
            ICerialMasterService<?> svc = IGuiceContext.get(ICerialMasterService.class);
            // Ensure senders can open and close their ports
            for (int[] p : pairs) {
                ComPortConnection<?> c = svc.getComPortConnectionDirect(p[0]).await().atMost(Duration.ofSeconds(30));
                c.connect(); c.disconnect();
            }
        } catch (Throwable t) {
            for (SerialPort r : receivers) { try { r.closePort(); } catch (Throwable ignored) {} }
            Assumptions.assumeTrue(false, "Serial sender ports not available: " + t.getMessage());
            return;
        }

        MultiTimedComPortSender manager = new MultiTimedComPortSender();
        Config baseCfg = new Config(1, 10, 400); // small delay, reasonable timeout to allow reads

        Map<Integer, List<MessageSpec>> byPort = new LinkedHashMap<>();
        Map<Integer, List<String>> expectedByReceiver = new LinkedHashMap<>();
        for (int[] p : pairs) {
            int sender = p[0];
            int recv = p[1];
            List<MessageSpec> specs = new ArrayList<>();
            List<String> exps = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                String payload = "MSG-" + sender + "-#" + i; // no newline; write(...) in sender uses direct string
                specs.add(new MessageSpec("ID-" + sender + "-" + i, payload, new Config(1, 10, 400)));
                exps.add(payload);
            }
            byPort.put(sender, specs);
            expectedByReceiver.put(recv, exps);
        }

        // Start background reader tasks that will capture exactly 3 messages per receiver by simple delimiterless grouping with small gaps
        ExecutorService exec = Executors.newCachedThreadPool();
        Map<Integer, CompletableFuture<List<String>>> received = new HashMap<>();
        for (int[] p : pairs) {
            int recv = p[1];
            SerialPort rport = receivers.get(Arrays.asList(pairs).indexOf(p));
        }
        // Build a map from receiver port number to its SerialPort reference
        Map<Integer, SerialPort> recvMap = new HashMap<>();
        for (int i = 0; i < pairs.length; i++) {
            recvMap.put(pairs[i][1], receivers.get(i));
        }
        for (int[] p : pairs) {
            int recv = p[1];
            SerialPort r = recvMap.get(recv);
            CompletableFuture<List<String>> fut = new CompletableFuture<>();
            received.put(recv, fut);
            exec.submit(() -> {
                List<String> msgs = new ArrayList<>();
                StringBuilder current = new StringBuilder();
                long lastByteAt = System.nanoTime();
                try {
                    byte[] buf = new byte[256];
                    while (msgs.size() < 3) {
                        int available = r.bytesAvailable();
                        if (available > 0) {
                            int toRead = Math.min(buf.length, available);
                            int read = r.readBytes(buf, toRead);
                            if (read > 0) {
                                lastByteAt = System.nanoTime();
                                String chunk = new String(buf, 0, read, StandardCharsets.UTF_8);
                                current.append(chunk);
                                // Heuristic: small inter-chunk idle gap or known token boundary length not reliable; we'll split once timeout gap occurs
                            }
                        } else {
                            // If idle for > 100ms and we have some bytes, consider one message completed (since we send small payloads spaced by delays)
                            if (current.length() > 0) {
                                long idleMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastByteAt);
                                if (idleMs > 120) {
                                    msgs.add(current.toString());
                                    current.setLength(0);
                                }
                            }
                            Thread.sleep(5);
                        }
                    }
                    // push remainder if any
                    if (current.length() > 0 && msgs.size() < 3) {
                        msgs.add(current.toString());
                    }
                    fut.complete(msgs);
                } catch (Throwable t) {
                    fut.completeExceptionally(t);
                }
            });
        }

        // Enqueue and await completion
        Uni<Map<Integer, GroupResult>> uni = manager.enqueueGroups(byPort, baseCfg);
        Map<Integer, GroupResult> results = uni.await().atMost(Duration.ofSeconds(60));
        assertEquals(4, results.size());

        // Await receivers up to 10 seconds each
        for (int[] p : pairs) {
            int recv = p[1];
            List<String> got = received.get(recv).get(10, TimeUnit.SECONDS);
            List<String> expected = expectedByReceiver.get(recv);
            assertNotNull(got, "No data received on COM" + recv);
            assertEquals(3, got.size(), "Expected 3 messages on COM" + recv + " got=" + got.size());
            // Because we don't have explicit delimiters, we check that each expected payload appears in the concatenated stream in order
            String all = String.join("|", got);
            int pos = -1;
            for (String e : expected) {
                int idx = all.indexOf(e);
                assertTrue(idx >= 0, "Expected payload not found on COM" + recv + ": " + e + " in " + all);
                assertTrue(idx > pos, "Payload order incorrect on COM" + recv);
                pos = idx;
            }
        }

        // Cleanup
        uni = null;
        for (SerialPort r : receivers) {
            try { r.closePort(); } catch (Throwable ignored) {}
        }
        exec.shutdownNow();
    }
}
