package com.epixel.sdk.unity.attribution;

import android.app.Application;

import com.epixel.sdk.unity.SDKConfig;

public interface IAdjust {
    void init(Application application, SDKConfig sdkConfig);
    void trackAdRevenue(String adsTuKey,
                        String network,
                        String placement,
                        double revenue,
                        String currency);
    void trackInAppPurchase(String productId,
                            double revenue,
                            String currency,
                            String orderId,
                            String purchaseToken);
    void trackSubscription(long price,
                           String currency,
                           String productId,
                           String orderId,
                           String signature,
                           String purchaseToken);
    void onResume();
    void onPause();
}
