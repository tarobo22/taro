package com.yeoun.app

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import java.util.TimeZone

/** One device-local alarm. No network, account identifier, journal body, or AI request. */
internal object YeounReminderScheduler {
  private const val PREFS = "yeoun_local_reminders"
  private const val CHANNEL = "yeoun_daily_reflection"
  private const val REQUEST = 8201
  private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
  private fun manager(context: Context) = context.getSystemService(NotificationManager::class.java)
  private fun channel(context: Context) {
    if (Build.VERSION.SDK_INT >= 26) manager(context).createNotificationChannel(
      NotificationChannel(CHANNEL, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
        .apply { description = context.getString(R.string.reminder_channel_description); enableVibration(false) })
  }
  private fun allowed(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
    if (!manager(context).areNotificationsEnabled()) return false
    return Build.VERSION.SDK_INT < 26 || manager(context).getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
  }
  private fun pending(context: Context) = PendingIntent.getBroadcast(context, REQUEST,
    Intent(context, YeounReminderReceiver::class.java).setAction("${context.packageName}.DAILY_REMINDER"),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  private fun cancel(context: Context) {
    context.getSystemService(AlarmManager::class.java).cancel(pending(context))
    manager(context).cancel(REQUEST)
  }
  private fun arm(context: Context, hour: Int, minute: Int): Long {
    val next = ReminderClock.next(System.currentTimeMillis(), TimeZone.getDefault(), hour, minute)
    // Daily reflection does not require exact-alarm access or a continuously running service.
    context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending(context))
    return next
  }
  @Synchronized fun read(context: Context): WritableMap {
    channel(context)
    val store = prefs(context)
    val permission = allowed(context)
    if (!permission && store.getBoolean("enabled", false)) {
      cancel(context)
      check(store.edit().putBoolean("enabled", false).remove("next_at").commit())
    }
    return Arguments.createMap().apply {
      putString("permission", if (permission) "granted" else "denied")
      putBoolean("enabled", store.getBoolean("enabled", false))
      if (store.contains("hour")) putInt("hour", store.getInt("hour", 0)) else putNull("hour")
      if (store.contains("minute")) putInt("minute", store.getInt("minute", 0)) else putNull("minute")
      if (store.getBoolean("enabled", false) && store.contains("next_at")) putDouble("nextAt", store.getLong("next_at", 0).toDouble()) else putNull("nextAt")
    }
  }
  @Synchronized fun schedule(context: Context, hour: Int, minute: Int): WritableMap {
    require(hour in 0..23 && minute in 0..59)
    channel(context)
    check(allowed(context)) { "Notification permission is disabled" }
    val next = arm(context, hour, minute)
    if (!prefs(context).edit().putInt("hour", hour).putInt("minute", minute).putLong("next_at", next).putBoolean("enabled", true).commit()) {
      cancel(context); error("Could not save reminder")
    }
    return read(context)
  }
  @Synchronized fun disable(context: Context, clear: Boolean = false): WritableMap {
    cancel(context)
    val editor = prefs(context).edit()
    if (clear) editor.clear() else editor.putBoolean("enabled", false).remove("next_at")
    check(editor.commit()) { "Could not save reminder" }
    return read(context)
  }
  @Synchronized fun restore(context: Context) {
    channel(context)
    val store = prefs(context)
    if (!store.getBoolean("enabled", false)) return
    if (!allowed(context)) { disable(context); return }
    val next = arm(context, store.getInt("hour", 0), store.getInt("minute", 0))
    check(store.edit().putLong("next_at", next).commit())
  }
  @Synchronized fun deliver(context: Context, preview: Boolean = false) {
    channel(context)
    val store = prefs(context)
    if (!preview && !store.getBoolean("enabled", false)) return
    if (!allowed(context)) { disable(context); return }
    val open = PendingIntent.getActivity(context, REQUEST,
      Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context)
    manager(context).notify(REQUEST, builder.setSmallIcon(R.drawable.yeoun_notification)
      .setContentTitle(context.getString(R.string.app_name))
      .setContentText(context.getString(R.string.reminder_body)).setContentIntent(open)
      .setVisibility(Notification.VISIBILITY_PRIVATE).setAutoCancel(true).build())
    if (!preview) restore(context)
  }
}
