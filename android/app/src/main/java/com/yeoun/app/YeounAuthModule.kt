package com.yeoun.app

import android.util.Base64
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ViewManager
import java.security.MessageDigest
import java.security.SecureRandom

class YeounAuthPackage : ReactPackage {
  override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> = listOf(YeounAuthModule(context))
  override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}
class YeounAuthModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  override fun getName() = "YeounAuth"
  @ReactMethod fun createPkce(promise: Promise) {
    try {
      val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
      val flags = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
      val verifier = Base64.encodeToString(random, flags)
      val challenge = Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)), flags)
      promise.resolve(Arguments.createMap().apply { putString("verifier", verifier); putString("challenge", challenge) })
    } catch (error: Exception) { promise.reject("PKCE_UNAVAILABLE", "안전한 계정 연결을 시작하지 못했어요.") }
  }
}
