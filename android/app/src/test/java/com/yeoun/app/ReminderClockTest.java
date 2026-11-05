package com.yeoun.app;

import static org.junit.Assert.*;
import java.time.Instant;
import java.util.TimeZone;
import org.junit.Test;

public class ReminderClockTest {
  private long at(String iso) { return Instant.parse(iso).toEpochMilli(); }
  @Test public void sameDayUsesDeviceTimezone() {
    assertEquals(at("2026-11-06T11:15:00Z"), ReminderClock.next(at("2026-11-06T01:00:23Z"), TimeZone.getTimeZone("Asia/Seoul"),20,15));
  }
  @Test public void atTheChosenMinuteMovesToTomorrow() {
    assertEquals(at("2026-11-07T11:00:00Z"), ReminderClock.next(at("2026-11-06T11:00:00Z"), TimeZone.getTimeZone("Asia/Seoul"),20,0));
  }
  @Test public void midnightRollsIntoNextMonth() {
    assertEquals(at("2026-12-31T15:00:00Z"), ReminderClock.next(at("2026-12-31T14:59:59Z"), TimeZone.getTimeZone("Asia/Seoul"),0,0));
  }
  @Test public void springDstUsesCalendarDayInsteadOfAdding24Hours() {
    assertEquals(at("2026-03-08T07:30:00Z"), ReminderClock.next(at("2026-03-07T12:00:00Z"), TimeZone.getTimeZone("America/New_York"),2,30));
  }
  @Test public void fallDstKeepsChosenLocalHour() {
    assertEquals(at("2026-11-01T13:00:00Z"), ReminderClock.next(at("2026-10-31T14:00:00Z"), TimeZone.getTimeZone("America/New_York"),8,0));
  }
  @Test public void invalidTimeCannotReachTheAlarmManager() {
    assertThrows(IllegalArgumentException.class,()->ReminderClock.next(0,TimeZone.getTimeZone("UTC"),24,0));
    assertThrows(IllegalArgumentException.class,()->ReminderClock.next(0,TimeZone.getTimeZone("UTC"),0,-1));
  }
}
