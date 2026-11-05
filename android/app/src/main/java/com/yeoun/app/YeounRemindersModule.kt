package com.yeoun.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class YeounRemindersModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  override fun getName() = "YeounReminders"
  private fun answer(promise: Promise, operation: () -> Any?) {
    try { promise.resolve(operation()) } catch (error: Exception) {
      promise.reject("REMINDER_UNAVAILABLE", "알림 설정을 저장하지 못했어요. 기기의 알림 권한을 확인해 주세요.")
    }
  }
  @ReactMethod fun read(promise: Promise) = answer(promise) { YeounReminderScheduler.read(context) }
  @ReactMethod fun schedule(hour: Double, minute: Double, promise: Promise) = answer(promise) {
    require(hour.isFinite() && minute.isFinite() && hour == hour.toInt().toDouble() && minute == minute.toInt().toDouble())
    YeounReminderScheduler.schedule(context, hour.toInt(), minute.toInt())
  }
  @ReactMethod fun disable(promise: Promise) = answer(promise) { YeounReminderScheduler.disable(context) }
  @ReactMethod fun clear(promise: Promise) = answer(promise) { YeounReminderScheduler.disable(context, true) }
  @ReactMethod fun preview(promise: Promise) = answer(promise) { YeounReminderScheduler.deliver(context, true); null }
  @ReactMethod fun openSettings(promise: Promise) = answer(promise) {
    val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
      .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
      else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    null
  }
}
