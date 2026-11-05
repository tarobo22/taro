package com.yeoun.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class YeounReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != "${context.packageName}.DAILY_REMINDER") return
    try { YeounReminderScheduler.deliver(context) } catch (_: Exception) { /* No journal or identifiers are logged. */ }
  }
}

class YeounReminderRestoreReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)) return
    try { YeounReminderScheduler.restore(context) } catch (_: Exception) { /* User can check the local setting in-app. */ }
  }
}
