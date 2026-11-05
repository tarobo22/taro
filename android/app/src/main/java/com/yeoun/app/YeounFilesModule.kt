package com.yeoun.app

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.security.NetworkSecurityPolicy
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/** Private files stay behind Android's document grants. No path, token or audio is logged. */
class YeounFilesModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context), LifecycleEventListener {
  private sealed class Operation(val promise: Promise) {
    var requestCode: Int = 0
    @Volatile var cancelled = false
    class Audio(promise: Promise, val maxBytes: Long, val maxDuration: Long) : Operation(promise)
    class Download(promise: Promise, val source: URL, val bearer: String, val filename: String, val maxBytes: Long) : Operation(promise)
  }
  private class FileFailure(val code: String, override val message: String) : Exception(message)
  private val guard = Any()
  private val worker = Executors.newSingleThreadExecutor()
  private var operation: Operation? = null
  private var pickerSequence = 20000
  @Volatile private var closed = false
  @Volatile private var connection: HttpURLConnection? = null

  private val results = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      val pending = synchronized(guard) { operation } ?: return
      if (requestCode != pending.requestCode) return
      if (resultCode != Activity.RESULT_OK) { finish(pending, if (pending is Operation.Audio) null else false); return }
      val uri = data?.data
      if (uri == null || uri.scheme != "content") { fail(pending, "E_DOCUMENT", "선택한 파일의 권한을 확인하지 못했어요."); return }
      if (pending is Operation.Audio) {
        val flags = (data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try { context.contentResolver.takePersistableUriPermission(uri, flags) } catch (_: SecurityException) { /* temporary grant remains valid */ }
      }
      worker.execute {
        try {
          if (closed) return@execute
          when (pending) {
            is Operation.Audio -> inspectAudio(pending, uri)
            is Operation.Download -> streamDownload(pending, uri)
          }
        } catch (failure: FileFailure) { fail(pending, failure.code, failure.message) }
        catch (_: Exception) { fail(pending, "E_FILE", "파일을 처리하지 못했어요. 다시 선택하거나 글 입력을 이용해 주세요.") }
      }
    }
  }
  init { context.addActivityEventListener(results); context.addLifecycleEventListener(this) }
  override fun getName() = "YeounFiles"

  @ReactMethod fun chooseAudio(maxBytes: Double, maxDurationMs: Double, promise: Promise) {
    if (!maxBytes.isFinite() || !maxDurationMs.isFinite() || maxBytes <= 0 || maxDurationMs <= 0) {
      promise.reject("E_LIMIT", "음성 파일 제한을 확인하지 못했어요."); return
    }
    val pending = Operation.Audio(promise, minOf(maxBytes.toLong(), AUDIO_LIMIT), minOf(maxDurationMs.toLong(), AUDIO_DURATION))
    if (!begin(pending)) return
    launch(pending, Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "audio/*"
      putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/mp4", "video/webm", "application/ogg"))
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }, pending.requestCode)
  }

  @ReactMethod fun downloadPrivate(source: String, bearer: String, filename: String, maxBytes: Double, promise: Promise) {
    val url = try { URL(source) } catch (_: Exception) { promise.reject("E_URL", "다운로드 연결을 확인하지 못했어요."); return }
    if (!permitted(url) || !bearer.matches(Regex("[a-fA-F0-9]{64}")) || !filename.matches(Regex("[A-Za-z0-9._-]{1,120}")) || !maxBytes.isFinite() || maxBytes <= 0) {
      promise.reject("E_DOWNLOAD", "안전한 파일 저장 연결을 확인하지 못했어요."); return
    }
    val pending = Operation.Download(promise, url, bearer, filename, minOf(maxBytes.toLong(), EXPORT_LIMIT))
    if (!begin(pending)) return
    launch(pending, Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/zip"
      putExtra(Intent.EXTRA_TITLE, filename)
      addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }, pending.requestCode)
  }

  private fun begin(pending: Operation): Boolean = synchronized(guard) {
    if (closed || operation != null) { pending.promise.reject("E_BUSY", "진행 중인 파일 선택을 먼저 마쳐 주세요."); false }
    else { pickerSequence = if (pickerSequence >= 50000) 20000 else pickerSequence + 1; pending.requestCode = pickerSequence; operation = pending; true }
  }
  private fun launch(pending: Operation, intent: Intent, code: Int) {
    UiThreadUtil.runOnUiThread {
      val activity = context.currentActivity
      if (activity == null || activity.isFinishing || activity.isDestroyed) { fail(pending, "E_ACTIVITY", "앱을 연 뒤 파일을 다시 선택해 주세요."); return@runOnUiThread }
      try { activity.startActivityForResult(intent, code) }
      catch (_: Exception) { fail(pending, "E_PICKER", "이 기기에서 파일 선택 화면을 열지 못했어요.") }
    }
  }

  private fun inspectAudio(pending: Operation.Audio, uri: Uri) {
    var name = "voice-audio"
    var declaredSize = -1L
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) name = cursor.getString(nameIndex)
        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) declaredSize = cursor.getLong(sizeIndex)
      }
    }
    if (declaredSize > pending.maxBytes) throw FileFailure("E_AUDIO_SIZE", "음성 파일은 10MiB 이내로 선택해 주세요.")
    var actualSize = 0L
    context.contentResolver.openInputStream(uri)?.use { input ->
      val buffer = ByteArray(16 * 1024)
      while (true) {
        if (closed || pending.cancelled || Thread.currentThread().isInterrupted) return
        val count = input.read(buffer); if (count == -1) break
        actualSize += count
        if (actualSize > pending.maxBytes) throw FileFailure("E_AUDIO_SIZE", "음성 파일은 10MiB 이내로 선택해 주세요.")
      }
    } ?: throw FileFailure("E_AUDIO", "선택한 음성 파일을 읽지 못했어요.")
    if (actualSize <= 0) throw FileFailure("E_AUDIO", "내용이 있는 음성 파일을 선택해 주세요.")
    val metadata = MediaMetadataRetriever()
    val duration: Long
    val mime: String
    try {
      metadata.setDataSource(context, uri)
      duration = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
      if (metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != "yes") throw FileFailure("E_AUDIO", "음성이 포함된 파일을 선택해 주세요.")
      mime = context.contentResolver.getType(uri) ?: metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "audio/mp4"
    } finally { metadata.release() }
    if (duration <= 0 || duration > pending.maxDuration) throw FileFailure("E_AUDIO_DURATION", "음성 파일은 60초 이내로 선택해 주세요.")
    finish(pending, Arguments.createMap().apply {
      putString("uri", uri.toString()); putString("name", name); putString("mimeType", mime)
      putDouble("sizeBytes", actualSize.toDouble()); putDouble("durationMs", duration.toDouble())
    })
  }

  private fun permitted(url: URL): Boolean = url.userInfo == null && url.ref == null && url.host.isNotBlank() &&
    (url.protocol == "https" || url.protocol == "http" && NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(url.host))

  private fun streamDownload(pending: Operation.Download, destination: Uri) {
    var request: HttpURLConnection? = null
    var complete = false
    try {
      // Redirects are rejected so a bearer can never move to a different origin.
      request = pending.source.openConnection() as HttpURLConnection
      connection = request
      request.instanceFollowRedirects = false
      request.connectTimeout = 30_000
      request.readTimeout = 30_000
      request.requestMethod = "GET"
      request.useCaches = false
      request.setRequestProperty("Authorization", "Bearer " + pending.bearer)
      request.setRequestProperty("Accept", "application/zip")
      val status = request.responseCode
      if (status == 410) throw FileFailure("E_EXPIRED", "내보내기 파일이 만료됐어요. 새로 요청해 주세요.")
      if (status != 200) throw FileFailure("E_DOWNLOAD", "파일을 내려받지 못했어요. 연결을 확인해 주세요.")
      if (request.contentType?.substringBefore(';')?.trim()?.lowercase() != "application/zip" || request.contentLengthLong > pending.maxBytes) throw FileFailure("E_DOWNLOAD", "내보내기 파일 형식이나 크기를 확인하지 못했어요.")
      request.inputStream.use { input ->
        context.contentResolver.openOutputStream(destination, "wt")?.use { output ->
          val buffer = ByteArray(16 * 1024)
          var total = 0L
          while (true) {
            if (closed || pending.cancelled || Thread.currentThread().isInterrupted) throw FileFailure("E_CANCELLED", "파일 저장을 중단했어요.")
            val count = input.read(buffer); if (count == -1) break
            total += count
            if (total > pending.maxBytes) throw FileFailure("E_DOWNLOAD_SIZE", "파일이 너무 커서 저장을 중단했어요.")
            output.write(buffer, 0, count)
          }
          if (total <= 0) throw FileFailure("E_DOWNLOAD", "파일 내용을 확인하지 못했어요.")
          output.flush()
        } ?: throw FileFailure("E_SAVE", "선택한 위치에 파일을 저장하지 못했어요.")
      }
      complete = true
      finish(pending, true)
    } finally {
      request?.disconnect(); connection = null
      if (!complete) try { DocumentsContract.deleteDocument(context.contentResolver, destination) } catch (_: Exception) { /* no URI logging */ }
    }
  }

  private fun finish(pending: Operation, result: Any?) {
    val current = synchronized(guard) { if (operation !== pending) false else { operation = null; true } }
    if (current && !closed) pending.promise.resolve(result)
  }
  private fun fail(pending: Operation, code: String, message: String) {
    val current = synchronized(guard) { if (operation !== pending) false else { operation = null; true } }
    if (current && !closed) pending.promise.reject(code, message)
  }
  override fun onHostResume() {}
  override fun onHostPause() {}
  override fun onHostDestroy() { cancelPending() }
  private fun cancelPending() {
    val pending = synchronized(guard) { val current = operation; operation = null; current }
    pending?.cancelled = true
    connection?.disconnect()
    pending?.promise?.reject("E_CANCELLED", "파일 선택이나 저장이 중단됐어요. 앱을 열고 다시 시도해 주세요.")
  }
  override fun invalidate() {
    closed = true; cancelPending(); worker.shutdownNow()
    context.removeActivityEventListener(results); context.removeLifecycleEventListener(this)
    super.invalidate()
  }
  companion object {
    private const val AUDIO_LIMIT = 10L * 1024 * 1024
    private const val AUDIO_DURATION = 60_000L
    private const val EXPORT_LIMIT = 64L * 1024 * 1024
  }
}
