package com.epixel.sdk.unity;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

// basic configs for Adjust and Applovin Ads
public class SDKConfig {
    public String facebookAppId;
    public String adjustToken;
    public String adjustPurchaseEvent;
    public String applovinSdkKey;
    public String privacyPolicy;
    public String termsOfService;

    public List<AdUnitConfig> rewardAdUnits = new ArrayList<>();
    public List<AdUnitConfig> interstitialAdUnits = new ArrayList<>();
    public List<AdUnitConfig> bannerAdUnits = new ArrayList<>();

    public String defaultRewardAdUnit;
    public String defaultInterstitialAdUnit;
    public String defaultBannerAdUnit;

    public AdUnitConfig findRewardUnitId(String adKey) {
        for (AdUnitConfig config : UnitySDK.getSdkConfig().rewardAdUnits) {
            if (TextUtils.equals(config.adKey, adKey)) {
                return config;
            }
        }
        if (!TextUtils.isEmpty(defaultRewardAdUnit)) {
            AdUnitConfig defaultConfig = new AdUnitConfig();
            defaultConfig.adKey = adKey;
            defaultConfig.adUnitId = defaultRewardAdUnit;
            return defaultConfig;
        }
        return null;
    }

    public AdUnitConfig findInterstitialUnitId(String adKey) {
        for (AdUnitConfig config : UnitySDK.getSdkConfig().interstitialAdUnits) {
            if (TextUtils.equals(config.adKey, adKey)) {
                return config;
            }
        }
        if (!TextUtils.isEmpty(defaultInterstitialAdUnit)) {
            AdUnitConfig defaultConfig = new AdUnitConfig();
            defaultConfig.adKey = adKey;
            defaultConfig.adUnitId = defaultInterstitialAdUnit;
            return defaultConfig;
        }
        return null;
    }

    public AdUnitConfig findBannerUnitId(String adKey) {
        for (AdUnitConfig config : UnitySDK.getSdkConfig().bannerAdUnits) {
            if (TextUtils.equals(config.adKey, adKey)) {
                return config;
            }
        }
        if (!TextUtils.isEmpty(defaultBannerAdUnit)) {
            AdUnitConfig defaultConfig = new AdUnitConfig();
            defaultConfig.adKey = adKey;
            defaultConfig.adUnitId = defaultBannerAdUnit;
            return defaultConfig;
        }
        return null;
    }
}
