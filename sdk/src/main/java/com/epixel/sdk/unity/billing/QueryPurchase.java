package com.epixel.sdk.unity.billing;

import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryPurchasesParams;
import com.epixel.sdk.unity.UnitySDK;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryPurchase {

    private int mResponseCode = BillingClient.BillingResponseCode.OK;
    private boolean inAppFinished = false;
    private boolean subscriptionFinished = false;
    private BillingClient mBillingClient;

    private ArrayList<Purchase> mResult = new ArrayList<>();

    private BillingUpdateListener mListener;

    private MyBillingClient mMyBillingClient;

    public QueryPurchase(MyBillingClient billingClient) {
        mMyBillingClient = billingClient;
    }

    public void query(BillingClient billingClient,
                      BillingUpdateListener listener) {
        // 游戏中调用restore功能
        UnitySDK.getAnalytics().logEvent("sdk_purchase_restore", new HashMap<>());
        mBillingClient = billingClient;
        mListener = listener;
        queryInAppProducts();
        querySubscriptionProducts();
    }

    private void queryInAppProducts() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        mBillingClient.queryPurchasesAsync(params, (billingResult, list) -> {
            if (mResponseCode == BillingClient.BillingResponseCode.OK) {
                mResponseCode = billingResult.getResponseCode();
            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                mResult.addAll(list);
            }
            else {
                Log.w("[IAPAndroid]", "query purchase error " + billingResult.getResponseCode() + "," + billingResult.getDebugMessage());
            }
            inAppFinished = true;
            checkResult();
        });
    }

    private void querySubscriptionProducts() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();
        mBillingClient.queryPurchasesAsync(params, (billingResult, list) -> {
            if (mResponseCode == BillingClient.BillingResponseCode.OK) {
                mResponseCode = billingResult.getResponseCode();
            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                mResult.addAll(list);
            }
            else {
                Log.w("[IAPAndroid]", "query purchase error " + billingResult.getResponseCode() + "," + billingResult.getDebugMessage());
            }
            subscriptionFinished = true;
            checkResult();
        });
    }

    private void checkResult() {
        if (inAppFinished && subscriptionFinished) {
            mMyBillingClient.updatePurchaseList(mResult);
            for (Purchase purchase : mResult) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                    // 记录查询到的已成功购买但未发放的商品订单
                    HashMap<String, String> data = new HashMap<>();
                    data.put("product_id", purchase.getProducts().get(0));
                    data.put("order_id", purchase.getOrderId());
                    data.put("purchase_token", purchase.getPurchaseToken());
                    UnitySDK.getAnalytics().logEvent("sdk_restore_query", data);
                }
            }
            if (mListener != null) {
                String json = BillingBridgeHelper.purchaseToJson(mResult);
                mListener.onUpdate(mResponseCode, json);
            }
        }
    }
}
