package com.epixel.sdk.unity.ads;

public interface UnityAdListener {
    void onRewarded(String adKey, int amount, String label);
    void onLoaded(String adKey);
    void onDisplay(String adKey);
    void onHidden(String adKey);
    void onClick(String adKey);
    void onLoadFailed(String adKey, int errorCode);
    void onDisplayFailed(String adKey, int errorCode);
    void onRevenue(String adKey, double revenue);
}
