package com.yeoun.app

import com.android.billingclient.api.*
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ViewManager
import android.os.Handler
import android.os.Looper

/** Google Play owns product descriptions, prices, consent and the payment sheet. */
class YeounBillingModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context), PurchasesUpdatedListener {
  private var pendingPurchase: Promise? = null
  private val handler = Handler(Looper.getMainLooper())
  private val purchaseTimeout = Runnable { finishError("PURCHASE_RESULT_PENDING", "구매 결과를 확인하지 못했어요. 구매 복원으로 다시 확인해 주세요.") }
  private val client = BillingClient.newBuilder(context).setListener(this)
    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
    .enableAutoServiceReconnection().build()
  override fun getName() = "YeounBilling"

  private fun connected(promise: Promise, operation: () -> Unit) {
    if (client.isReady) { operation(); return }
    client.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) operation()
        else if (pendingPurchase === promise) finishError("STORE_UNAVAILABLE", "Google Play에 연결하지 못했어요.")
        else promise.reject("STORE_UNAVAILABLE", "Google Play에 연결하지 못했어요.")
      }
      override fun onBillingServiceDisconnected() { }
    })
  }
  private fun query(id: String, kind: String, callback: (BillingResult, ProductDetails?) -> Unit) {
    val product = QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(kind).build()
    client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()) { result, data ->
      callback(result, data.productDetailsList.firstOrNull { it.productId == id })
    }
  }
  private fun validKind(kind: String) = kind == BillingClient.ProductType.INAPP || kind == BillingClient.ProductType.SUBS

  @ReactMethod fun products(requests: ReadableArray, promise: Promise) {
    if (requests.size() > 100) { promise.reject("INVALID_PRODUCTS", "상품 수를 확인해 주세요."); return }
    if (requests.size() == 0) { promise.resolve(Arguments.createArray()); return }
    connected(promise) {
      val output = Arguments.createArray()
      fun next(index: Int) {
        if (index >= requests.size()) { promise.resolve(output); return }
        val item = requests.getMap(index)
        val id = item?.getString("product_id") ?: ""
        val kind = item?.getString("kind") ?: ""
        if (!validKind(kind) || !id.matches(Regex("[A-Za-z0-9._-]{1,150}"))) {
          promise.reject("INVALID_PRODUCTS", "등록된 상품을 확인해 주세요."); return
        }
        query(id, kind) { result, detail ->
          if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            promise.reject("STORE_UNAVAILABLE", "스토어 상품을 확인하지 못했어요."); return@query
          }
          if (detail != null) output.pushMap(productMap(detail, kind))
          next(index + 1)
        }
      }
      next(0)
    }
  }
  private fun productMap(detail: ProductDetails, kind: String): WritableMap {
    val offers = Arguments.createArray()
    if (kind == BillingClient.ProductType.INAPP) {
      for (offer in detail.oneTimePurchaseOfferDetailsList.orEmpty()) {
        // Rental and preorder products require a separate entitlement model and are not offered here.
        if (offer.rentalDetails != null || offer.preorderDetails != null) continue
        val value = Arguments.createMap();value.putString("offer_token", offer.offerToken)
        val phases = Arguments.createArray();val phase = Arguments.createMap()
        phase.putString("price", offer.formattedPrice);phase.putString("period", "");phase.putInt("cycles", 1)
        phase.putInt("recurrence", 3);phases.pushMap(phase);value.putArray("phases", phases);offers.pushMap(value)
      }
    } else {
      for (offer in detail.subscriptionOfferDetails.orEmpty()) {
        val value = Arguments.createMap();value.putString("offer_token", offer.offerToken)
        val phases = Arguments.createArray()
        for (price in offer.pricingPhases.pricingPhaseList) {
          val phase = Arguments.createMap();phase.putString("price", price.formattedPrice)
          phase.putString("period", price.billingPeriod);phase.putInt("cycles", price.billingCycleCount)
          phase.putInt("recurrence", price.recurrenceMode);phases.pushMap(phase)
        }
        value.putArray("phases", phases);offers.pushMap(value)
      }
    }
    return Arguments.createMap().apply {
      putString("product_id", detail.productId);putString("kind", kind)
      putString("title", detail.name);putString("description", detail.description);putArray("offers", offers)
    }
  }

  @ReactMethod fun purchase(id: String, kind: String, accountId: String, offerToken: String, promise: Promise) {
    if (pendingPurchase != null) { promise.reject("PURCHASE_BUSY", "진행 중인 구매를 먼저 확인해 주세요."); return }
    if (!validKind(kind) || !id.matches(Regex("[A-Za-z0-9._-]{1,150}")) || !accountId.matches(Regex("[0-9a-f]{64}"))) {
      promise.reject("INVALID_PURCHASE", "계정과 상품을 확인해 주세요."); return
    }
    pendingPurchase = promise
    handler.postDelayed(purchaseTimeout, 120_000)
    connected(promise) {
      if (pendingPurchase !== promise) return@connected
      query(id, kind) { result, detail ->
        if (pendingPurchase !== promise) return@query
        if (result.responseCode != BillingClient.BillingResponseCode.OK || detail == null) {
          finishError("STORE_UNAVAILABLE", "상품을 다시 확인해 주세요."); return@query
        }
        val valid = if (kind == BillingClient.ProductType.INAPP) detail.oneTimePurchaseOfferDetailsList.orEmpty()
          .any { it.offerToken == offerToken && it.rentalDetails == null && it.preorderDetails == null }
          else detail.subscriptionOfferDetails.orEmpty().any { it.offerToken == offerToken }
        if (!valid) { finishError("OFFER_CHANGED", "상품 조건이 바뀌었어요. 새로 확인해 주세요."); return@query }
        val activity = context.currentActivity
        if (activity == null) { finishError("NO_ACTIVITY", "앱으로 돌아와 다시 시도해 주세요."); return@query }
        val item = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(detail).setOfferToken(offerToken).build()
        activity.runOnUiThread {
          if (pendingPurchase !== promise) return@runOnUiThread
          val launched = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(item))
            .setObfuscatedAccountId(accountId).build())
          if (launched.responseCode != BillingClient.BillingResponseCode.OK) {
            if (launched.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) finishCancelled()
            else finishError("PURCHASE_UNAVAILABLE", "구매 화면을 열지 못했어요. 구매 복원도 확인해 주세요.")
          }
        }
      }
    }
  }
  private fun finishError(code: String, message: String) { handler.removeCallbacks(purchaseTimeout);val promise = pendingPurchase;pendingPurchase = null;promise?.reject(code, message) }
  private fun finishCancelled() { handler.removeCallbacks(purchaseTimeout);val promise = pendingPurchase;pendingPurchase = null;promise?.resolve(Arguments.createMap().apply {
    putString("state", "cancelled");putArray("purchases", Arguments.createArray())
  }) }
  override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
    if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) { finishCancelled();return }
    if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) {
      finishError("PURCHASE_UNAVAILABLE", "구매를 확인하지 못했어요. 구매 복원을 눌러 다시 확인해 주세요.");return
    }
    handler.removeCallbacks(purchaseTimeout);val promise = pendingPurchase;pendingPurchase = null
    promise?.resolve(Arguments.createMap().apply { putString("state", "completed");putArray("purchases", purchaseList(purchases)) })
  }
  private fun purchaseList(purchases: List<Purchase>): WritableArray = Arguments.createArray().apply {
    for (purchase in purchases) for (id in purchase.products) pushMap(Arguments.createMap().apply {
      putString("product_id", id);putString("purchase_token", purchase.purchaseToken)
      putString("state", if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) "purchased" else "pending")
    })
  }
  @ReactMethod fun restore(promise: Promise) {
    connected(promise) {
      val result = mutableListOf<Purchase>()
      fun next(kind: String) {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(kind).build()) { response, purchases ->
          if (response.responseCode != BillingClient.BillingResponseCode.OK) {
            promise.reject("RESTORE_UNAVAILABLE", "스토어 구매를 복원하지 못했어요.");return@queryPurchasesAsync
          }
          result.addAll(purchases)
          if (kind == BillingClient.ProductType.INAPP) next(BillingClient.ProductType.SUBS) else promise.resolve(purchaseList(result))
        }
      }
      next(BillingClient.ProductType.INAPP)
    }
  }
  override fun invalidate() { finishError("APP_CLOSED", "구매 복원으로 다시 확인해 주세요.");client.endConnection();super.invalidate() }
}

class YeounBillingPackage : ReactPackage {
  override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> = listOf(YeounBillingModule(context))
  override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}
