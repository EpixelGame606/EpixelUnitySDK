package com.epixel.sdk.unity.billing;

public interface BillingUpdateListener {
    void onUpdate(int responseCode, String purchaseJson);
}
