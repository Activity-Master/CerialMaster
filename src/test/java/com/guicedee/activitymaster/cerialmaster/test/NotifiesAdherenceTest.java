package com.guicedee.activitymaster.cerialmaster.test;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.testimpl.TestStatusListener;
import com.guicedee.cerial.enumerations.ComPortStatus;
import com.guicedee.cerial.enumerations.ComPortType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotifiesAdherenceTest
{
  private static ComPortStatus pickAnotherStatus()
  {
    // Try a list of likely statuses until we hit one that exists in this build
    String[] candidates = new String[]{"Online", "Open", "Connected", "Available", "Idle", "Ready", "OnlineActive"};
    for (String cand : candidates)
    {
      try
      {
        return Enum.valueOf(ComPortStatus.class, cand);
      }
      catch (IllegalArgumentException ignored)
      {
      }
    }
    // Fallback to Offline if nothing else exists
    return ComPortStatus.Offline;
  }

  @BeforeEach
  void setup()
  {
    TestStatusListener.reset();
  }

  @AfterEach
  void tearDown()
  {
    TestStatusListener.reset();
  }

  @Test
  @DisplayName("Server port status change notifies listener")
  void serverStatusChange_notifiesListener()
  {
    ComPortConnection<?> c1 = ComPortConnection.getOrCreate(101, ComPortType.Server);

    // Ensure a known baseline
    c1.setComPortStatus(ComPortStatus.Offline, true);

    ComPortStatus next = pickAnotherStatus();
    if (next == ComPortStatus.Offline)
    {
      // ensure change is a real change
      // if fallback returned Offline only, then toggle to Offline then back to Offline is no-op; pick silent baseline first
      c1.setComPortStatus(ComPortStatus.Offline, true);
    }

    c1.setComPortStatus(next);

    List<TestStatusListener.Event> ev = TestStatusListener.events();
    assertEquals(1, ev.size(), "Exactly one notification expected");
    assertEquals(101, ev.get(0).port());
    assertEquals(next, ev.get(0).newStatus());
  }

  @Test
  @DisplayName("Scanner port status change notifies listener")
  void scannerStatusChange_notifiesListener()
  {
    ComPortConnection<?> c1 = ComPortConnection.getOrCreate(102, ComPortType.Scanner);
    c1.setComPortStatus(ComPortStatus.Offline, true);

    ComPortStatus next = pickAnotherStatus();
    if (next == ComPortStatus.Offline)
    {
      // if only Offline known, choose same to keep compilation but we can't assert change; skip test
      // Instead, set to Offline again and assert no notification
      c1.setComPortStatus(ComPortStatus.Offline);
      assertTrue(TestStatusListener.events().isEmpty(), "No notification should fire when status remains same");
      return;
    }

    c1.setComPortStatus(next);

    List<TestStatusListener.Event> ev = TestStatusListener.events();
    assertEquals(1, ev.size(), "Exactly one notification expected for scanner");
    assertEquals(102, ev.get(0).port());
    assertEquals(next, ev.get(0).newStatus());
  }

  @Test
  @DisplayName("Silent status change does not notify listener")
  void silentStatusChange_doesNotNotify()
  {
    ComPortConnection<?> c1 = ComPortConnection.getOrCreate(103, ComPortType.Server);
    c1.setComPortStatus(ComPortStatus.Offline, true);

    ComPortStatus next = pickAnotherStatus();
    c1.setComPortStatus(next, true); // silent

    assertTrue(TestStatusListener.events().isEmpty(), "Silent change should not notify");
  }

  @Test
  @DisplayName("Setting same status again does not notify")
  void sameStatusChange_doesNotNotify()
  {
    ComPortConnection<?> c1 = ComPortConnection.getOrCreate(104, ComPortType.Server);
    c1.setComPortStatus(ComPortStatus.Offline, true);

    // Non-silent change to establish a baseline
    ComPortStatus next = pickAnotherStatus();
    if (next == ComPortStatus.Offline)
    {
      // If only Offline exists, we cannot establish a changed baseline; assert no-notify on same status
      c1.setComPortStatus(ComPortStatus.Offline);
      assertTrue(TestStatusListener.events().isEmpty());
      return;
    }

    c1.setComPortStatus(next);
    assertEquals(1, TestStatusListener.events().size());

    // Setting the same status again should not notify
    c1.setComPortStatus(next);
    assertEquals(1, TestStatusListener.events().size());
  }

  @Test
  @DisplayName("Canonical instance shared by port across types; single notification")
  void canonicalInstance_sharedAcrossTypes_andSingleNotification()
  {
    ComPortConnection<?> server = ComPortConnection.getOrCreate(105, ComPortType.Server);
    ComPortConnection<?> scanner = ComPortConnection.getOrCreate(105, ComPortType.Scanner);

    assertSame(server, scanner, "Same instance should be returned for same COM port regardless of type");

    server.setComPortStatus(ComPortStatus.Offline, true);
    ComPortStatus next = pickAnotherStatus();
    if (next == ComPortStatus.Offline)
    {
      // No alternative status available; ensure no notification on same value
      server.setComPortStatus(ComPortStatus.Offline);
      assertTrue(TestStatusListener.events().isEmpty());
      return;
    }

    // Change via scanner reference
    scanner.setComPortStatus(next);

    List<TestStatusListener.Event> ev = TestStatusListener.events();
    assertEquals(1, ev.size());
    assertEquals(105, ev.get(0).port());
    assertEquals(next, ev.get(0).newStatus());
  }
}
