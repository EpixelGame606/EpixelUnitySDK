package com.epixel.sdk.unity.ads;

public interface IAds {
    void addRewardAdUnit(String adKey, String adUnitId);
    void addInterstitialAdUnit(String adKey, String adUnitId);
    void addBannerAdUnit(String adKey, String adUnitId);
    void setListener(UnityAdListener listener);
    void init();
    boolean isRewardedAdReady(String adKey);
    void loadRewardedAd(String adKey);
    void showRewardedAd(String adKey);
    void loadInterstitialAd(String adKey);
    boolean isInterstitialAdReady(String adKey);
    void showInterstitialAd(String adKey);
    void showBannerAd(String adKey);
    void hideBannerAd(String adKey);
}
