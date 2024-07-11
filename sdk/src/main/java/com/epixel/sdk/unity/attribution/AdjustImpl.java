package com.epixel.sdk.unity.attribution;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustPlayStoreSubscription;
import com.adjust.sdk.LogLevel;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.epixel.sdk.unity.UnitySDK;
import com.epixel.sdk.unity.SDKConfig;
import com.epixel.sdk.unity.utils.SDKLog;
import com.epixel.sdk.unity.utils.Utils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdjustImpl implements IAdjust {
    private SDKConfig mSDKConfig;
    private boolean mInitialized = false;
    private boolean mNeedResume = false;

    @Override
    public void init(Application application, SDKConfig sdkConfig) {
        mInitialized = false;
        mSDKConfig = sdkConfig;
        if (!hasAdjust()) {
            // 未配置adjust
            return;
        }
        application.registerActivityLifecycleCallbacks(new AdjustImpl.AdjustLifecycleCallbacks());
//        String facebookAppId = sdkConfig.facebookAppId;
        String adjustToken = sdkConfig.adjustToken;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AdjustConfig adjustConfig = new AdjustConfig(application, adjustToken, AdjustConfig.ENVIRONMENT_PRODUCTION);
//            adjustConfig.setFbAppId(facebookAppId);
            adjustConfig.setLogLevel(LogLevel.VERBOSE);
//            adjustConfig.setNeedsCost(true);
            adjustConfig.setOnAttributionChangedListener(adjustAttribution -> {
                SDKLog.i("Adjust", adjustAttribution.toString());
            });
            adjustConfig.setOnEventTrackingSucceededListener(adjustEventSuccess -> {
                SDKLog.i("Adjust", adjustEventSuccess.toString());
            });
            adjustConfig.setOnEventTrackingFailedListener(adjustEventFailure -> {
                SDKLog.e("Adjust", adjustEventFailure.toString());
            });
            adjustConfig.setOnSessionTrackingSucceededListener(adjustSessionSuccess -> {
                SDKLog.i("Adjust", adjustSessionSuccess.toString());
            });
            adjustConfig.setOnSessionTrackingFailedListener(adjustSessionFailure -> {
                SDKLog.e("Adjust", adjustSessionFailure.toString());
            });
//            adjustConfig.setSendInBackground(true);
            String deviceId = getDeviceId(application);
            SDKLog.i("Adjust", "set device id " + deviceId);
            adjustConfig.setExternalDeviceId(deviceId);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Adjust.initSdk(adjustConfig);
                mInitialized = true;
                if (mNeedResume) {
                    Adjust.onResume();
                    mNeedResume = false;
                }
            });
        });
    }

    private boolean hasAdjust() {
        return !TextUtils.isEmpty(mSDKConfig.adjustToken);
    }
    private String getDeviceId(Context context) {
        SharedPreferences sp = Utils.getSharedPreferences(context);
        String deviceId = sp.getString("sdk_adjust_device_id", null);
        if (TextUtils.isEmpty(deviceId)) {
            try {
                AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
                deviceId = info.getId();
            } catch (Exception e) {
                deviceId = UUID.randomUUID().toString();
            }
            sp.edit().putString("sdk_adjust_device_id", deviceId).apply();
            return deviceId;
        }
        else {
            return deviceId;
        }
    }

    @Override
    public void trackAdRevenue(String adsTuKey, String network, String placement, double revenue, String currency) {
//        if (!hasAdjust()) {
//            return;
//        }
//        AdjustAdRevenue adRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX);
//        adRevenue.setAdRevenueUnit(adsTuKey);
//        adRevenue.setRevenue(revenue, currency);
//        adRevenue.setAdRevenueNetwork(network);
//        adRevenue.setAdRevenuePlacement(placement);
//        Adjust.trackAdRevenue(adRevenue);
    }

    @Override
    public void trackInAppPurchase(String productId,
                                   double revenue,
                                   String currency,
                                   String orderId,
                                   String purchaseToken) {
//        if (!hasAdjust()) {
//            return;
//        }
//        AdjustEvent adjustEvent = new AdjustEvent(mSDKConfig.adjustPurchaseEvent);
//        adjustEvent.setProductId(productId);
//        adjustEvent.setRevenue(revenue, currency);
//        adjustEvent.setOrderId(orderId);
//        adjustEvent.setPurchaseToken(purchaseToken);
//        Adjust.trackEvent(adjustEvent);
    }
    @Override
    public void trackSubscription(long price,
                                  String currency,
                                  String productId,
                                  String orderId,
                                  String signature,
                                  String purchaseToken) {
        if (!hasAdjust()) {
            return;
        }
        AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(price, currency, productId, orderId, signature, purchaseToken);
        Adjust.trackPlayStoreSubscription(subscription);
    }

    @Override
    public void onResume() {
        if (!hasAdjust()) {
            return;
        }
        if (mInitialized) {
            Adjust.onResume();
            mNeedResume = false;
        }
        else {
            mNeedResume = true;
        }
    }

    @Override
    public void onPause() {
        if (!hasAdjust()) {
            return;
        }
        if (mInitialized) {
            Adjust.onPause();
        }
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            UnitySDK.getAdjust().onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            UnitySDK.getAdjust().onPause();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}
