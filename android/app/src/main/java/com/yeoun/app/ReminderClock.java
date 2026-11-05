package com.yeoun.app;

import java.util.Calendar;
import java.util.TimeZone;

/** Pure wall-clock policy. Calendar days preserve the user's time across DST changes. */
public final class ReminderClock {
  private ReminderClock() {}
  public static long next(long now, TimeZone zone, int hour, int minute) {
    if (hour < 0 || hour > 23 || minute < 0 || minute > 59) throw new IllegalArgumentException("Invalid reminder time");
    Calendar target = Calendar.getInstance(zone);
    target.setTimeInMillis(now);
    target.set(Calendar.HOUR_OF_DAY, hour);
    target.set(Calendar.MINUTE, minute);
    target.set(Calendar.SECOND, 0);
    target.set(Calendar.MILLISECOND, 0);
    if (target.getTimeInMillis() <= now) {
      // Add the date before setting a potentially nonexistent spring-DST wall time.
      target.setTimeInMillis(now);
      target.add(Calendar.DAY_OF_YEAR, 1);
      target.set(Calendar.HOUR_OF_DAY, hour);
      target.set(Calendar.MINUTE, minute);
      target.set(Calendar.SECOND, 0);
      target.set(Calendar.MILLISECOND, 0);
    }
    return target.getTimeInMillis();
  }
}
