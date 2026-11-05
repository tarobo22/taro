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
  private var soundEnabled = true
  private var hapticsEnabled = true
  private var musicEnabled = false
  private var music: MediaPlayer? = null
  private val pool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
  private val ready = mutableSetOf<Int>()
  private val sounds = mutableMapOf<String, Int>()
  init {
    context.addLifecycleEventListener(this)
    pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) synchronized(ready) { ready.add(id) } }
    mapOf("tap" to R.raw.yeoun_tap, "select" to R.raw.yeoun_select, "confirm" to R.raw.yeoun_confirm,
      "back" to R.raw.yeoun_back, "tick" to R.raw.yeoun_back, "shuffle" to R.raw.yeoun_shuffle,
      "flip" to R.raw.yeoun_flip).forEach { (name, resource) -> sounds[name] = pool.load(context, resource, 1) }
  }
  override fun getName() = "YeounFeedback"
  @ReactMethod fun configure(sound: Boolean, haptics: Boolean, background: Boolean) {
    soundEnabled = sound; hapticsEnabled = haptics; musicEnabled = background
    UiThreadUtil.runOnUiThread { updateMusic() }
  }
  private fun updateMusic() {
    if (!musicEnabled) { music?.pause(); return }
    if (context.currentActivity?.hasWindowFocus() != true) return
    if (music == null) music = MediaPlayer.create(context, R.raw.yeoun_music)?.apply { isLooping = true; setVolume(0.08f, 0.08f) }
    music?.start()
  }
  @ReactMethod fun play(kind: String) {
    val id = sounds[kind]
    if (soundEnabled && id != null && synchronized(ready) { ready.contains(id) }) pool.play(id, 0.55f, 0.55f, 1, 0, 1f)
    if (hapticsEnabled) UiThreadUtil.runOnUiThread {
      val pattern = if (kind == "confirm" && Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CLOCK_TICK
      context.currentActivity?.window?.decorView?.performHapticFeedback(pattern)
    }
  }
  override fun onHostResume() { updateMusic() }
  override fun onHostPause() { music?.pause() }
  override fun onHostDestroy() { music?.pause() }
  override fun invalidate() { context.removeLifecycleEventListener(this); music?.release(); music = null; pool.release(); super.invalidate() }
}
