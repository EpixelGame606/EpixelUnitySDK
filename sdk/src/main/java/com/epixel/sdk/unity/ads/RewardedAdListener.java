package com.epixel.sdk.unity.ads;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.epixel.sdk.unity.UnitySDK;

import java.util.HashMap;

// listen to events from applovin rewarded ad
public class RewardedAdListener implements MaxRewardedAdListener, MaxAdRevenueListener {
    private final UnityAdListener mListener;
    private final String mAdKey;

    public RewardedAdListener(String adKey, UnityAdListener listener) {
        mAdKey = adKey;
        mListener = listener;
    }

    @Override
    public void onUserRewarded(@NonNull MaxAd maxAd, @NonNull MaxReward maxReward) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_reward", data);

        if (mListener != null) {
            mListener.onRewarded(mAdKey, maxReward.getAmount(), maxReward.getLabel());
        }
    }

    @Override
    public void onAdLoaded(@NonNull MaxAd maxAd) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_loaded", data);

        if (mListener != null) {
            mListener.onLoaded(mAdKey);
        }
    }

    @Override
    public void onAdDisplayed(@NonNull MaxAd maxAd) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_shown", data);
        if (mListener != null) {
            mListener.onDisplay(mAdKey);
        }
    }

    @Override
    public void onAdHidden(@NonNull MaxAd maxAd) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_close", data);

        if (mListener != null) {
            mListener.onHidden(mAdKey);
        }
    }

    @Override
    public void onAdClicked(@NonNull MaxAd maxAd) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_click", data);

        if (mListener != null) {
            mListener.onClick(mAdKey);
        }
    }

    @Override
    public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError maxError) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", adUnitId);
        data.put("error_code", String.valueOf(maxError.getCode()));
        String message = maxError.getMessage();
        if (message != null) {
            data.put("error_message", message);
        }
        UnitySDK.getAnalytics().logEvent("sdk_ad_load_failed", data);
        if (mListener != null) {
            mListener.onLoadFailed(mAdKey, maxError.getCode());
        }
    }

    @Override
    public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        data.put("error_code", String.valueOf(maxError.getCode()));
        String message = maxError.getMessage();
        if (message != null) {
            data.put("error_message", message);
        }
        UnitySDK.getAnalytics().logEvent("sdk_ad_show_failed", data);

        if (mListener != null) {
            mListener.onDisplayFailed(mAdKey, maxError.getCode());
        }
    }

    @Override
    public void onAdRevenuePaid(@NonNull MaxAd maxAd) {
        HashMap<String, String> data = new HashMap<>();
        data.put("ad_tag", mAdKey);
        data.put("ad_unit_id", maxAd.getAdUnitId());
        data.put("placement", maxAd.getPlacement());
        data.put("network", maxAd.getNetworkName());
        data.put("revenue", String.valueOf(maxAd.getRevenue()));
        UnitySDK.getAnalytics().logEvent("sdk_ad_revenue", data);

        if (mListener != null) {
            mListener.onRevenue(mAdKey, maxAd.getRevenue());
        }
        UnitySDK.getAdjust().trackAdRevenue(
                maxAd.getAdUnitId(),
                maxAd.getNetworkName(),
                maxAd.getPlacement(),
                maxAd.getRevenue(),
                "USD");
    }
}
