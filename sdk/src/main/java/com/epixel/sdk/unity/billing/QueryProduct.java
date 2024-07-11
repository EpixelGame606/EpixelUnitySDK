package com.epixel.sdk.unity.billing;

import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class QueryProduct {

    private boolean inAppFinished = false;
    private boolean subscriptionFinished = false;

    private BillingClient mBillingClient;

    private ArrayList<ProductDetails> mResult = new ArrayList<>();

    private QueryProductListener mListener;

    public void query(BillingClient billingClient,
                      List<String> inAppProducts,
                      List<String> subscriptionProducts,
                      QueryProductListener listener) {
        mBillingClient = billingClient;
        mListener = listener;
        if (inAppProducts.size() > 0) {
            inAppFinished = false;
            queryInAppProducts(inAppProducts);
        }
        else {
            inAppFinished = true;
        }

        if (subscriptionProducts.size() > 0) {
            subscriptionFinished = false;
            querySubscriptionProducts(subscriptionProducts);
        }
        else {
            subscriptionFinished = true;
        }
    }

    private void queryInAppProducts(List<String> productIdList) {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        for (String productId : productIdList) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP).build();
            products.add(product);
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();
        mBillingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                mResult.addAll(list);
            }
            else {
                Log.w("[IAPAndroid]", "query product error " + billingResult.getResponseCode() + "," + billingResult.getDebugMessage());
            }
            inAppFinished = true;
            checkResult();
        });
    }

    private void querySubscriptionProducts(List<String> productIdList) {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        for (String productId : productIdList) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS).build();
            products.add(product);
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();
        mBillingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                mResult.addAll(list);
            }
            else {
                Log.w("[IAPAndroid]", "query product error " + billingResult.getResponseCode() + "," + billingResult.getDebugMessage());
            }
            subscriptionFinished = true;
            checkResult();
        });
    }

    private void checkResult() {
        if (inAppFinished && subscriptionFinished) {
            if (mListener != null) {
                String json = BillingBridgeHelper.productDetailToJson(mResult);
                mListener.onResponse(json);
            }
        }
    }
}
