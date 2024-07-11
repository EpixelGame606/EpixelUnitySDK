package com.epixel.sdk.unity.billing;

public interface IBillingClient {
    void initialize(String consumableProducts,
                    String subscriptionProducts,
                    BillingInitListener initListener,
                    BillingUpdateListener updateListener);
    void queryProducts(QueryProductListener listener);
    void queryPurchases(BillingUpdateListener listener);
    void launchPurchase(String productId);
    void acknowledgePurchase(String productId, String orderId, String purchaseToken, BillingConsumeListener consumeListener);

}
