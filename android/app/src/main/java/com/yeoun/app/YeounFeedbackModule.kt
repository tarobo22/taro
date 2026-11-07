package com.yeoun.app

import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import android.os.Build
import android.view.HapticFeedbackConstants
import com.facebook.react.bridge.*
import com.facebook.react.ReactPackage
import com.facebook.react.uimanager.ViewManager

class YeounFeedbackPackage : ReactPackage {
  override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> = listOf(YeounFeedbackModule(context))
  override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}

class YeounFeedbackModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context), LifecycleEventListener {
  private var soundEnabled = false
  private var hapticsEnabled = true
  private var musicEnabled = false
  @Volatile private var captureMuted = false
  @Volatile private var foreground = true
  private var music: MediaPlayer? = null
  private val streams = linkedSetOf<Int>()
  private val pool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
  private val ready = mutableSetOf<Int>()
  private val sounds = mutableMapOf<String, Int>()
  init {
    context.addLifecycleEventListener(this)
    pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) synchronized(ready) { ready.add(id) } }
    mapOf("tap" to R.raw.yeoun_tap, "select" to R.raw.yeoun_select, "confirm" to R.raw.yeoun_confirm,
      "boundary" to R.raw.yeoun_back, "back" to R.raw.yeoun_back, "tick" to R.raw.yeoun_back, "shuffle" to R.raw.yeoun_shuffle,
      "flip" to R.raw.yeoun_flip, "meow" to R.raw.yeoun_meow).forEach { (name, resource) -> sounds[name] = pool.load(context, resource, 1) }
  }
  override fun getName() = "YeounFeedback"
  @ReactMethod fun configure(sound: Boolean, haptics: Boolean, background: Boolean) {
    soundEnabled = sound; hapticsEnabled = haptics; musicEnabled = background
    if (!sound) stopEffects()
    UiThreadUtil.runOnUiThread { updateMusic() }
  }
  @ReactMethod fun setCaptureMuted(muted: Boolean) {
    captureMuted = muted
    if (muted) stopEffects()
    UiThreadUtil.runOnUiThread { updateMusic() }
  }
  private fun stopEffects() {
    synchronized(streams) { streams.forEach { pool.stop(it) }; streams.clear() }
  }
  private fun updateMusic() {
    if (!musicEnabled || captureMuted || !foreground) { music?.pause(); return }
    if (context.currentActivity?.hasWindowFocus() != true) return
    if (music == null) music = MediaPlayer.create(context, R.raw.yeoun_music)?.apply { isLooping = true; setVolume(0.08f, 0.08f) }
    music?.start()
  }
  @ReactMethod fun play(kind: String) {
    if (captureMuted || !foreground) return
    val id = sounds[kind]
    if (soundEnabled && id != null && synchronized(ready) { ready.contains(id) }) {
      synchronized(streams) {
        if (!captureMuted && foreground) {
          val gain = if (kind == "tick") 0.18f else if (kind == "boundary") 0.22f else 0.45f
          val stream = pool.play(id, gain, gain, 1, 0, 1f)
          if (stream != 0) streams.add(stream)
          while (streams.size > 3) { val old = streams.first(); pool.stop(old); streams.remove(old) }
        }
      }
    }
    if (hapticsEnabled && kind != "meow") UiThreadUtil.runOnUiThread {
      if (captureMuted || !foreground || !hapticsEnabled) return@runOnUiThread
      val pattern = when {
        kind == "confirm" && Build.VERSION.SDK_INT >= 30 -> HapticFeedbackConstants.CONFIRM
        kind == "tick" && Build.VERSION.SDK_INT >= 34 -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        kind == "select" && Build.VERSION.SDK_INT >= 34 -> HapticFeedbackConstants.SEGMENT_TICK
        kind == "boundary" -> HapticFeedbackConstants.CLOCK_TICK
        else -> HapticFeedbackConstants.CONTEXT_CLICK
      }
      context.currentActivity?.window?.decorView?.performHapticFeedback(pattern)
    }
  }
  override fun onHostResume() { foreground = true; updateMusic() }
  override fun onHostPause() { foreground = false; stopEffects(); music?.pause() }
  override fun onHostDestroy() { foreground = false; stopEffects(); music?.pause() }
  override fun invalidate() { context.removeLifecycleEventListener(this); music?.release(); music = null; pool.release(); super.invalidate() }
}
