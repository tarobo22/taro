package com.yeoun.app

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.ReactPackage

class YeounSpeechPackage : ReactPackage {
  override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> = listOf(YeounSpeechModule(context))
  override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}

class YeounSpeechModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  private var recognizer: SpeechRecognizer? = null
  override fun getName() = "YeounSpeech"
  private fun emit(text: String = "", message: String = "", final: Boolean = false, error: Boolean = false) {
    if (context.hasActiveReactInstance()) context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("YeounSpeech", Arguments.createMap().apply { putString("text", text); putString("message", message); putBoolean("final", final); putBoolean("error", error) })
  }
  @ReactMethod fun isAvailable(promise: Promise) {
    UiThreadUtil.runOnUiThread { promise.resolve(SpeechRecognizer.isRecognitionAvailable(context)) }
  }
  @ReactMethod fun start(language: String) {
    UiThreadUtil.runOnUiThread {
      recognizer?.destroy()
      if (!SpeechRecognizer.isRecognitionAvailable(context)) { emit(message = "이 기기에서 음성 인식을 사용할 수 없어요. 텍스트로 입력해 주세요.", error = true); return@runOnUiThread }
      try {
        recognizer = if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else SpeechRecognizer.createSpeechRecognizer(context)
        recognizer?.setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) { emit(message = "듣고 있어요. 천천히 말해 주세요.") }
          override fun onBeginningOfSpeech() {}
          override fun onRmsChanged(rmsdB: Float) {}
          override fun onBufferReceived(buffer: ByteArray?) {}
          override fun onEndOfSpeech() {}
          override fun onError(code: Int) { emit(message = if (code == SpeechRecognizer.ERROR_NO_MATCH || code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) "음성을 인식하지 못했어요. 다시 누르거나 직접 입력해 주세요." else "음성 인식을 마쳤어요. 입력 내용을 확인해 주세요. (오류 $code)", error = true) }
          override fun onResults(results: Bundle?) { emit(text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: "", message = "입력 내용을 확인한 뒤 보내 주세요.", final = true) }
          override fun onPartialResults(results: Bundle?) { emit(text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: "", message = "듣고 있어요.") }
          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        })
      } catch (error: Exception) { emit(message = "음성 입력을 시작하지 못했어요. 권한과 기기의 음성 인식 설정을 확인해 주세요.", error = true) }
    }
  }
  @ReactMethod fun stop() { UiThreadUtil.runOnUiThread { recognizer?.cancel(); recognizer?.destroy(); recognizer = null } }
  @ReactMethod fun addListener(eventName: String) {}
  @ReactMethod fun removeListeners(count: Int) {}
  override fun invalidate() { UiThreadUtil.runOnUiThread { recognizer?.destroy(); recognizer = null }; super.invalidate() }
}
