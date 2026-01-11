package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.*;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TimedComPortSenderPriorityTest
{

  static class MockConnection extends ComPortConnection<MockConnection> {
    public MockConnection(int port) {
      super(port, com.guicedee.cerial.enumerations.ComPortType.Device);
    }
    @Override public MockConnection connect() {
      setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.Running);
      return this;
    }
    @Override public MockConnection disconnect() { return this; }
    @Override public void write(String message, boolean... checkForEndOfCharacter) { }
  }

  @Test
  public void priorityRunsImmediatelyAfterCurrent_andPerMessageUniCompletes() throws Exception
  {
    ComPortConnection<?> oldConn = ComPortConnection.PORT_CONNECTIONS.get(20);
    TimedComPortSender oldSender = ComPortConnection.getTimedSender(20);
    try {
      MockConnection mock = new MockConnection(20);
      ComPortConnection.PORT_CONNECTIONS.put(20, mock);
      ComPortConnection.TIMED_SENDERS.remove(20);

      doTest();
    } finally {
      if (oldConn != null) ComPortConnection.PORT_CONNECTIONS.put(20, oldConn);
      else ComPortConnection.PORT_CONNECTIONS.remove(20);
      
      if (oldSender != null) ComPortConnection.TIMED_SENDERS.put(20, oldSender);
      else ComPortConnection.TIMED_SENDERS.remove(20);
    }
  }

  private void doTest() throws Exception
  {
    ComPortConnection<?> conn = ComPortConnection.PORT_CONNECTIONS.get(20);
    // Use a long timeout so we have plenty of time to call complete() manually
    TimedComPortSender sender = conn.getOrCreateTimedSender(new Config(0, 100, 2000));

    // Track the order that messages start
    List<String> startOrder = new CopyOnWriteArrayList<>();
    
    // Subscribe using the shared multi that MultiTimedComPortSender (if any) or other tests might use
    sender.messageProgress()
        .subscribe()
        .with(mp -> {
          if (mp != null)
          {
            if (mp.note != null && mp.note.contains("Starting") && mp.id != null)
            {
              System.out.println("[DEBUG_LOG] id=" + mp.id + " STARTED");
              startOrder.add(mp.id);
            }
          }
        })
    ;

    // Enqueue a group A, B, C
    var A = new MessageSpec("A", "PAYLOAD-A", new Config(0, 1000, 5000));
    var B = new MessageSpec("B", "PAYLOAD-B", new Config(0, 1000, 5000));
    var C = new MessageSpec("C", "PAYLOAD-C", new Config(0, 1000, 5000));
    var groupUni = sender.enqueueGroup(List.of(A, B, C));

    // Wait (bounded) for A to start
    waitForStart(startOrder, "A");
    System.out.println("[TEST] A started. Enqueueing P.");

    // Enqueue a priority message P while A is running
    var P = new MessageSpec("P", "PAYLOAD-P", new Config(0, 1000, 5000));
    var pUni = sender.enqueuePriority(P);

    // Complete A externally so next should be P (priority) before B, then C
    sender.complete();

    // Wait (bounded) for P to start
    waitForStart(startOrder, "P");
    System.out.println("[TEST] P started. Completing P.");

    // Now complete P
    sender.complete();

    // Wait (bounded) for B to start
    waitForStart(startOrder, "B");
    System.out.println("[TEST] B started. Completing B.");
    sender.complete();

    // Wait (bounded) for C to start
    waitForStart(startOrder, "C");
    System.out.println("[TEST] C started. Enqueueing Q.");
    
    var Q = new MessageSpec("Q", "PAYLOAD-Q", new Config(0, 1000, 5000));
    var qUni = sender.enqueuePriority(Q);

    // Complete C externally to advance to Q
    sender.complete();

    // Wait (bounded) for Q to start
    waitForStart(startOrder, "Q");
    System.out.println("[TEST] Q started. Completing Q.");
    sender.complete();

    // Wait for the whole group to finish (A, B, C only)
    GroupResult groupResult = groupUni.await().indefinitely();
    assertNotNull(groupResult);
    assertEquals(3, groupResult.results.size());

    // Verify order: A, P, B, C, Q
    System.out.println("[TEST] Final StartOrder: " + startOrder);
    assertEquals("A", startOrder.get(0));
    assertEquals("P", startOrder.get(1));
    assertEquals("B", startOrder.get(2));
    assertEquals("C", startOrder.get(3));
    assertEquals("Q", startOrder.get(4));

    // Per-message Uni for P should complete successfully
    MessageResult pResult = pUni.await().indefinitely();
    assertNotNull(pResult);
    assertEquals("P", pResult.id);
    assertEquals(TimedComPortSender.State.Completed, pResult.terminalState);

    // Per-message Uni for Q should complete successfully after C
    MessageResult qResult = qUni.await().indefinitely();
    assertNotNull(qResult);
    assertEquals("Q", qResult.id);
    assertEquals(TimedComPortSender.State.Completed, qResult.terminalState);
  }

  private void waitForStart(List<String> order, String id) throws InterruptedException {
      for (int i = 0; i < 500; i++) {
          if (order.contains(id)) return;
          Thread.sleep(10);
      }
      throw new RuntimeException("Timeout waiting for message " + id + " to start. Current order: " + order);
  }
}
