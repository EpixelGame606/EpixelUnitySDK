package com.epixel.sdk.unity;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.epixel.sdk.unity.analytics.AnalyticsImpl;
import com.epixel.sdk.unity.analytics.IAnalytics;
import com.google.gson.Gson;
import com.epixel.sdk.unity.ads.AdsImpl;
import com.epixel.sdk.unity.ads.IAds;
import com.epixel.sdk.unity.attribution.AdjustImpl;
import com.epixel.sdk.unity.attribution.IAdjust;
import com.epixel.sdk.unity.billing.IBillingClient;
import com.epixel.sdk.unity.billing.MyBillingClient;
import com.epixel.sdk.unity.firebase.FirebaseImpl;
import com.epixel.sdk.unity.firebase.IFirebase;
import com.epixel.sdk.unity.utils.SDKLog;
import com.epixel.sdk.unity.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class UnitySDK {

    private static SDKConfig sdkConfig;

    private static IFirebase firebaseImpl;
    private static IAdjust adjustImpl;
    private static IBillingClient billingClient;

    private static IAds adsImpl;
    private static IAnalytics analyticsImpl;

    // initialize epixel SDK
    public static void initBaseSDK(Application application) {

        String configStr = loadConfigFile(application, "sdk_config.json");

        if (!TextUtils.isEmpty(configStr)) {
            sdkConfig = new Gson().fromJson(configStr, SDKConfig.class);
        }
        if (sdkConfig == null) {
            throw new IllegalStateException("no sdk config");
        }

        if (Utils.isMainProcess(application)) {
            getFirebase().init(application);
            getAdjust().init(application, sdkConfig);
        }
    }

    private static String loadConfigFile(Context context, String inFile) {
        String initConfig = null;
        InputStream stream = null;
        try {
            stream = context.getAssets().open(inFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            initConfig = new String(buffer);
        } catch (IOException e) {
            // nothing
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // nothing
                }
            }
        }
        return initConfig;
    }

    public static SDKConfig getSdkConfig() {
        return sdkConfig;
    }

    public static IFirebase getFirebase() {
        if (firebaseImpl == null) {
            firebaseImpl = new FirebaseImpl();
        }
        return firebaseImpl;
    }

    // bridge adjust to Unity game
    public static IAdjust getAdjust() {
        if (adjustImpl == null) {
            adjustImpl = new AdjustImpl();
        }
        return adjustImpl;
    }

    // bridge in-app billing to Unity game
    public static IBillingClient getBillingClient() {
        if (billingClient == null) {
            billingClient = new MyBillingClient();
        }
        return billingClient;
    }

    // bridge applovin ads to Unity game
    public static IAds getAds() {
        if (adsImpl == null) {
            adsImpl = new AdsImpl();
        }
        return adsImpl;
    }

    // bridge firebase analytics to Unity game
    public static IAnalytics getAnalytics() {
        if (analyticsImpl == null) {
            analyticsImpl = new AnalyticsImpl();
        }
        return analyticsImpl;
    }
}
