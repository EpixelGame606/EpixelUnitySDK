package com.epixel.sdk.unity.billing;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.epixel.sdk.unity.UnitySDK;
import com.epixel.sdk.unity.utils.SDKLog;
import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MyBillingClient implements IBillingClient {

    private Handler mMainHandler;

    private BillingClient mBillingClient;
    private BillingInitListener mInitListener;
    private BillingUpdateListener mUpdateListener;

    private final BillingClientStateListener mClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingServiceDisconnected() {
            SDKLog.w("IAP", "billing service disconnected");
            reconnectService();
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                onServiceConnected();
            } else {
                SDKLog.w("IAP", "billing service setup failed");
            }
        }
    };

    private int mReconnectionCount = 0;

    private List<String> mConsumableProducts = new ArrayList<>();
    private List<String> mSubscriptionProducts = new ArrayList<>();
    private HashMap<String, Purchase> mPurchaseCache = new HashMap<>();

    private Handler getMainHandler() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
        return mMainHandler;
    }

    // initialize billing client on game start
    @Override
    public void initialize(String consumableProducts,
                           String subscriptionProducts,
                           BillingInitListener initListener,
                           BillingUpdateListener updateListener) {
        mInitListener = initListener;
        mUpdateListener = updateListener;
        PurchasesUpdatedListener purchaseUpdateListener = MyBillingClient.this::onPurchasesUpdated;
        mConsumableProducts = Arrays.asList(consumableProducts.split(","));
        mSubscriptionProducts = Arrays.asList(subscriptionProducts.split(","));
        PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build();
        mBillingClient = BillingClient.newBuilder(UnityPlayer.currentActivity)
                .enablePendingPurchases(pendingPurchasesParams)
                .setListener(purchaseUpdateListener)
                .build();
        mBillingClient.startConnection(mClientStateListener);
    }

    // reconnect google service if connection lost
    private void reconnectService() {
        if (mReconnectionCount < 3) {
            mReconnectionCount++;
            mBillingClient.startConnection(mClientStateListener);
        }
    }

    // google service connected
    private void onServiceConnected() {
        mReconnectionCount = 0;
        if (mInitListener != null) {
            mInitListener.onInit();
        }
    }

    // query information about in-app products
    @Override
    public void queryProducts(QueryProductListener listener) {
        QueryProduct queryProduct = new QueryProduct();
        queryProduct.query(mBillingClient, mConsumableProducts, mSubscriptionProducts, listener);
    }

    // query purchases
    @Override
    public void queryPurchases(BillingUpdateListener listener) {
        QueryPurchase queryPurchase = new QueryPurchase(this);
        queryPurchase.query(mBillingClient, listener);
    }

    // try start purchase flow
    @Override
    public void launchPurchase(String productId) {
        HashMap<String, String> data = new HashMap<>();
        data.put("product_id", productId);
        UnitySDK.getAnalytics().logEvent("sdk_launch_purchase", data);

        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        String productTypeStr;
        if (mSubscriptionProducts.contains(productId)) {
            productTypeStr = BillingClient.ProductType.SUBS;
        }
        else {
            productTypeStr = BillingClient.ProductType.INAPP;
        }
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productTypeStr).build();
        products.add(product);
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();
        mBillingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            int responseCode = billingResult.getResponseCode();
            if (responseCode == BillingClient.BillingResponseCode.OK && list != null && list.size() > 0) {
                ProductDetails productDetails = list.get(0);
                launchPurchase(productDetails);
            }
            else {
                HashMap<String, String> failData = new HashMap<>();
                failData.put("product_id", productId);
                failData.put("error_code", String.valueOf(responseCode));
                failData.put("message", "query failed");
                failData.put("debug_message", billingResult.getDebugMessage());
                UnitySDK.getAnalytics().logEvent("sdk_purchase_fail", failData);

                if (mUpdateListener != null) {
                    String json = BillingBridgeHelper.purchaseToJson(productId);
                    mUpdateListener.onUpdate(responseCode, json);
                }
            }
        });
    }

    // try start purchase flow
    private void launchPurchase(ProductDetails productDetails) {
        BillingFlowParams.ProductDetailsParams productDetailsParams;
        if (BillingClient.ProductType.SUBS.equals(productDetails.getProductType())) {
            List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
            if (subscriptionOfferDetails != null && subscriptionOfferDetails.size() > 0) {
                String offerToken = subscriptionOfferDetails.get(0).getOfferToken();
                SDKLog.i("IAP", "subscription offer token " + offerToken);
                productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build();
            } else {
                HashMap<String, String> failData = new HashMap<>();
                failData.put("product_id", productDetails.getProductId());
                failData.put("error_code", "100");
                failData.put("message", "no subscription offer");
                UnitySDK.getAnalytics().logEvent("sdk_purchase_fail", failData);

                String json = BillingBridgeHelper.purchaseToJson(productDetails.getProductId());
                if (mUpdateListener != null) {
                    mUpdateListener.onUpdate(100, json);
                }
                return;
            }
        } else {
            productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build();
        }
        List<BillingFlowParams.ProductDetailsParams> paramList = new ArrayList<>();
        paramList.add(productDetailsParams);
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(paramList)
                .build();

        getMainHandler().post(() -> {
            BillingResult billingResult = mBillingClient.launchBillingFlow(UnityPlayer.currentActivity, params);
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                SDKLog.w("IAP", "launch billing flow error " + billingResult.getResponseCode() + ", " + billingResult.getDebugMessage());
                HashMap<String, String> failData = new HashMap<>();
                failData.put("product_id", productDetails.getProductId());
                failData.put("error_code", String.valueOf(billingResult.getResponseCode()));
                failData.put("message", "launch billing flow error");
                failData.put("debug_message", billingResult.getDebugMessage());
                UnitySDK.getAnalytics().logEvent("sdk_purchase_fail", failData);

                String json = BillingBridgeHelper.purchaseToJson(productDetails.getProductId());
                if (mUpdateListener != null) {
                    mUpdateListener.onUpdate(billingResult.getResponseCode(), json);
                }
            }
        });
    }

    // listen purchase updates
    private void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
        SDKLog.i("IAP", "purchase updated: " + billingResult.getResponseCode() + "," + mUpdateListener);
        if (list != null && list.size() > 0) {
            String json = BillingBridgeHelper.purchaseToJson(list);
            SDKLog.i("IAP", "purchase updated: " + json);
            updatePurchaseList(list);

            for (Purchase purchase : list) {
                String productId = purchase.getProducts().get(0);
                String orderId = purchase.getOrderId();
                boolean isAck = purchase.isAcknowledged();
                int purchaseState = purchase.getPurchaseState();
                HashMap<String, String> data = new HashMap<>();
                data.put("product_id", productId);
                data.put("order_id", orderId);
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    data.put("error_code", String.valueOf(billingResult.getResponseCode()));
                    data.put("message", "launch billing flow error");
                    data.put("debug_message", billingResult.getDebugMessage());
                    UnitySDK.getAnalytics().logEvent("sdk_purchase_fail", data);
                }
                else if (purchaseState == Purchase.PurchaseState.PURCHASED && !isAck) {
                    UnitySDK.getAnalytics().logEvent("sdk_purchase_success", data);
                }
                else if (purchaseState == Purchase.PurchaseState.PENDING) {
                    UnitySDK.getAnalytics().logEvent("sdk_purchase_pending", data);
                }
            }

            if (mUpdateListener != null) {
                mUpdateListener.onUpdate(billingResult.getResponseCode(), json);
            }
        }
        else {
            String json = BillingBridgeHelper.purchaseToJson("");
            SDKLog.i("IAP", "purchase updated: " + json);
            if (mUpdateListener != null) {
                mUpdateListener.onUpdate(billingResult.getResponseCode(), json);
            }
        }
    }

    void updatePurchaseList(List<Purchase> purchaseList) {
        for (Purchase purchase : purchaseList) {
            String purchaseToken = purchase.getPurchaseToken();
            if (!TextUtils.isEmpty(purchaseToken)) {
                mPurchaseCache.put(purchaseToken, purchase);
            }
        }
    }

    // acknowledge purchase after users get product in game
    @Override
    public void acknowledgePurchase(String productId, String orderId, String purchaseToken, BillingConsumeListener consumeListener) {
        HashMap<String, String> data = new HashMap<>();
        data.put("product_id", productId);
        data.put("order_id", orderId);
        data.put("purchase_token", purchaseToken);
        UnitySDK.getAnalytics().logEvent("sdk_purchase_ack", data);

        HashMap<String, String> resultData = new HashMap<>();
        resultData.put("product_id", productId);
        resultData.put("order_id", orderId);
        resultData.put("purchase_token", purchaseToken);

        if (mSubscriptionProducts.contains(productId)) {
            AcknowledgePurchaseParams ackParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchaseToken)
                            .build();

            mBillingClient.acknowledgePurchase(ackParams, billingResult -> {
                SDKLog.i("IAP", "purchase acknowledge result: " + billingResult.getResponseCode());
                if (consumeListener != null) {
                    resultData.put("error_code", String.valueOf(billingResult.getResponseCode()));
                    resultData.put("debug_message", billingResult.getDebugMessage());
                    UnitySDK.getAnalytics().logEvent("sdk_purchase_ack_result", resultData);
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        consumeListener.onSuccess();
                    } else {
                        consumeListener.onError();
                    }
                }
            });
            trackSubscriptionByAdjust(productId, purchaseToken);
        } else {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchaseToken)
                            .build();

            mBillingClient.consumeAsync(consumeParams, (billingResult, token) -> {
                SDKLog.i("IAP", "purchase consume result: " + billingResult.getResponseCode());
                resultData.put("error_code", String.valueOf(billingResult.getResponseCode()));
                resultData.put("debug_message", billingResult.getDebugMessage());
                UnitySDK.getAnalytics().logEvent("sdk_purchase_ack_result", resultData);
                if (consumeListener != null) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        consumeListener.onSuccess();
                    } else {
                        consumeListener.onError();
                    }
                }
            });
            trackInAppPurchaseByAdjust(productId, purchaseToken);
        }
    }

    // send revenue data to Adjust
    private void trackInAppPurchaseByAdjust(String productId, String purchaseToken) {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        QueryProductDetailsParams.Product productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP).build();
        products.add(productParams);
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();
        mBillingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Purchase purchase = mPurchaseCache.get(purchaseToken);
                ProductDetails product = list.size() > 0 ? list.get(0) : null;
                if (purchase != null && product != null) {
                    ProductDetails.OneTimePurchaseOfferDetails details = product.getOneTimePurchaseOfferDetails();
                    if (details != null) {
                        double price = details.getPriceAmountMicros() / 1000000d * purchase.getQuantity();
                        UnitySDK.getAdjust().trackInAppPurchase(productId, price, details.getPriceCurrencyCode(), purchase.getOrderId(), purchase.getPurchaseToken());
                    }
                }
            }
        });
    }

    // send revenue data to Adjust
    private void trackSubscriptionByAdjust(String productId, String purchaseToken) {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        QueryProductDetailsParams.Product productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS).build();
        products.add(productParams);
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();
        mBillingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Purchase purchase = mPurchaseCache.get(purchaseToken);
                ProductDetails product = list.size() > 0 ? list.get(0) : null;
                if (purchase != null && product != null) {
                    List<ProductDetails.SubscriptionOfferDetails> subsDetails = product.getSubscriptionOfferDetails();
                    ProductDetails.SubscriptionOfferDetails details = subsDetails != null && subsDetails.size() > 0 ? subsDetails.get(0) : null;
                    if (details != null) {
                        List<ProductDetails.PricingPhase> pricePhases = details.getPricingPhases().getPricingPhaseList();
                        if (pricePhases.size() > 0) {
                            long price = pricePhases.get(0).getPriceAmountMicros();
                            String currency = pricePhases.get(0).getPriceCurrencyCode();
                            UnitySDK.getAdjust().trackSubscription(price, currency, productId, purchase.getOrderId(), purchase.getSignature(), purchase.getPurchaseToken());
                        }
                    }
                }
            }
        });
    }
}
