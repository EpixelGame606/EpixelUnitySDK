package com.epixel.sdk.unity.ads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.epixel.sdk.unity.AdUnitConfig;
import com.epixel.sdk.unity.R;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.epixel.sdk.unity.UnitySDK;
import com.epixel.sdk.unity.SDKConfig;
import com.epixel.sdk.unity.utils.SDKLog;
import com.unity3d.player.UnityPlayer;

import java.util.HashMap;

public class AdsImpl implements IAds {

    private boolean mInitialized = false;
    private boolean mInitializeFinished = false;

    private UnityAdListener mUnityAdListener;
    private String mPreloadRewardedAdTu = null;
    private String mPreloadInterstitialAdTu = null;
    private String mPreloadBannerAdTu = null;

    // connect reward ads in game with specific applovin ad unit
    @Override
    public void addRewardAdUnit(String adKey, String adUnitId) {
        SDKConfig sdkConfig = UnitySDK.getSdkConfig();
        AdUnitConfig adUnitConfig = new AdUnitConfig();
        adUnitConfig.adKey = adKey;
        adUnitConfig.adUnitId = adUnitId;
        sdkConfig.rewardAdUnits.add(adUnitConfig);
    }

    // connect interstitial ads in game with specific applovin ad unit
    @Override
    public void addInterstitialAdUnit(String adKey, String adUnitId) {
        SDKConfig sdkConfig = UnitySDK.getSdkConfig();
        AdUnitConfig adUnitConfig = new AdUnitConfig();
        adUnitConfig.adKey = adKey;
        adUnitConfig.adUnitId = adUnitId;
        sdkConfig.interstitialAdUnits.add(adUnitConfig);
    }

    // connect banner ads in game with specific applovin ad unit
    @Override
    public void addBannerAdUnit(String adKey, String adUnitId) {
        SDKConfig sdkConfig = UnitySDK.getSdkConfig();
        AdUnitConfig adUnitConfig = new AdUnitConfig();
        adUnitConfig.adKey = adKey;
        adUnitConfig.adUnitId = adUnitId;
        sdkConfig.bannerAdUnits.add(adUnitConfig);
    }

    // register listener from Unity game
    @Override
    public void setListener(UnityAdListener listener) {
        if (listener != null) {
            mUnityAdListener = listener;
        }
    }

    // init applovin on main thread
    @Override
    public void init() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this::requestConsent);
    }

    // check if rewarded ad is ready
    @Override
    public boolean isRewardedAdReady(String adKey) {
        if (!mInitializeFinished) {
            return false;
        }
        AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findRewardUnitId(adKey);
        if (adUnitConfig == null) {
            return false;
        }
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(adUnitConfig.adUnitId, UnityPlayer.currentActivity);
        return rewardedAd.isReady();
    }

    // load a rewarded ad
    @Override
    public void loadRewardedAd(String adKey) {
        if (!mInitializeFinished) {
            mPreloadRewardedAdTu = adKey;
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findRewardUnitId(adKey);
            if (adUnitConfig == null) {
                SDKLog.e("no ad unit id for " + adKey);
                return;
            }
            RewardedAdListener adListener = new RewardedAdListener(adKey, mUnityAdListener);
            MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(adUnitConfig.adUnitId, UnityPlayer.currentActivity);
            rewardedAd.setListener(adListener);
            rewardedAd.setRevenueListener(adListener);
            rewardedAd.loadAd();
        });
    }

    // show rewarded ad
    public void showRewardedAd(String adKey) {
        if (!mInitializeFinished) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findRewardUnitId(adKey);
            if (adUnitConfig == null) {
                SDKLog.e("no ad unit id for " + adKey);
                if (mUnityAdListener != null) {
                    mUnityAdListener.onDisplayFailed(adKey, -1);
                }
                return;
            }
            RewardedAdListener adListener = new RewardedAdListener(adKey, mUnityAdListener);
            MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(adUnitConfig.adUnitId, UnityPlayer.currentActivity);
            rewardedAd.setListener(adListener);
            rewardedAd.setRevenueListener(adListener);
            rewardedAd.showAd(UnityPlayer.currentActivity);
        });

    }

    private final HashMap<String, MaxInterstitialAd> mInterstitialAds = new HashMap<>();

    // load a interstitial ad
    @Override
    public void loadInterstitialAd(String adKey) {
        if (!mInitializeFinished) {
            mPreloadInterstitialAdTu = adKey;
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findInterstitialUnitId(adKey);
            if (adUnitConfig == null) {
                SDKLog.e("no ad unit id for " + adKey);
                if (mUnityAdListener != null) {
                    mUnityAdListener.onLoadFailed(adKey, -1);
                }
                return;
            }
            InterstitialAdListener adListener = new InterstitialAdListener(adKey, mUnityAdListener);
            MaxInterstitialAd interstitialAd = new MaxInterstitialAd(adUnitConfig.adUnitId, UnityPlayer.currentActivity);
            interstitialAd.setListener(adListener);
            interstitialAd.setRevenueListener(adListener);
            interstitialAd.loadAd();
            mInterstitialAds.put(adUnitConfig.adUnitId, interstitialAd);
        });
    }

    // check if interstitial ad is ready to show
    @Override
    public boolean isInterstitialAdReady(String adKey) {
        if (!mInitializeFinished) {
            return false;
        }
        AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findInterstitialUnitId(adKey);
        if (adUnitConfig == null) {
            return false;
        }
        MaxInterstitialAd ad = mInterstitialAds.get(adUnitConfig.adUnitId);
        return ad != null && ad.isReady();
    }

    // show interstitial ad
    @Override
    public void showInterstitialAd(String adKey) {
        if (!mInitializeFinished) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findInterstitialUnitId(adKey);
            if (adUnitConfig == null) {
                SDKLog.e("no ad unit id for " + adKey);
                if (mUnityAdListener != null) {
                    mUnityAdListener.onDisplayFailed(adKey, -1);
                }
                return;
            }
            MaxInterstitialAd interstitialAd = mInterstitialAds.get(adUnitConfig.adUnitId);
            if (interstitialAd != null && interstitialAd.isReady()) {
                InterstitialAdListener adListener = new InterstitialAdListener(adKey, mUnityAdListener);
                interstitialAd.setListener(adListener);
                interstitialAd.setRevenueListener(adListener);
                interstitialAd.showAd(UnityPlayer.currentActivity);
                mInterstitialAds.remove(adUnitConfig.adUnitId);
            }
            else {
                SDKLog.e("interstitial ad not load");
                if (mUnityAdListener != null) {
                    mUnityAdListener.onDisplayFailed(adKey, -1);
                }
            }
        });
    }

    private final HashMap<String, MaxAdView> mBannerAds = new HashMap<>();
    // show banner ad on bottom of the game
    @Override
    public void showBannerAd(String adKey) {
        if (!mInitializeFinished) {
            mPreloadBannerAdTu = adKey;
            return;
        }
        if (mBannerAds.containsKey(adKey)) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findBannerUnitId(adKey);
            if (adUnitConfig == null) {
                SDKLog.e("no ad unit id for " + adKey);
                if (mUnityAdListener != null) {
                    mUnityAdListener.onLoadFailed(adKey, -1);
                }
                return;
            }
            Activity activity = UnityPlayer.currentActivity;
            MaxAdView maxAdView = new MaxAdView(adUnitConfig.adUnitId, activity);
            BannerAdListener adListener = new BannerAdListener(adKey, mUnityAdListener);
            maxAdView.setListener(adListener);
            maxAdView.setRevenueListener(adListener);
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int heightPx = activity.getResources().getDimensionPixelSize(R.dimen.banner_height);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, heightPx);
            layoutParams.gravity = Gravity.BOTTOM;
            maxAdView.setLayoutParams(layoutParams);
            maxAdView.setBackgroundColor(Color.TRANSPARENT);

            ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.addView(maxAdView);

            maxAdView.loadAd();

            mBannerAds.put(adUnitConfig.adUnitId, maxAdView);
        });
    }

    // hide banner ad
    @Override
    public void hideBannerAd(String adKey) {
        mPreloadBannerAdTu = null;
        if (!mInitializeFinished) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AdUnitConfig adUnitConfig = UnitySDK.getSdkConfig().findBannerUnitId(adKey);
            if (adUnitConfig == null) {
                return;
            }
            MaxAdView maxAdView = mBannerAds.get(adUnitConfig.adUnitId);
            if (maxAdView != null) {
                mBannerAds.remove(adUnitConfig.adUnitId);
                ViewGroup parent = (ViewGroup)maxAdView.getParent();
                if (parent != null) {
                    parent.removeView(maxAdView);
                }
                maxAdView.destroy();
            }
        });
    }

    // init applovin ads
    private void doInit(Context context) {
        if (mInitialized) {
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            SDKConfig sdkConfig = UnitySDK.getSdkConfig();
            AppLovinSdk.getInstance(context).setMediationProvider(AppLovinMediationProvider.MAX);
            AppLovinSdkSettings settings = AppLovinSdk.getInstance(context).getSettings();
//            settings.setVerboseLogging(true);
            settings.getTermsAndPrivacyPolicyFlowSettings().setEnabled(true);
            settings.getTermsAndPrivacyPolicyFlowSettings().setPrivacyPolicyUri(Uri.parse(sdkConfig.privacyPolicy));
            settings.getTermsAndPrivacyPolicyFlowSettings().setTermsOfServiceUri(Uri.parse(sdkConfig.termsOfService));
            SDKLog.i("Ads", "start applovin initialization");
            AppLovinSdk.initializeSdk(context, applovinSdkConfiguration -> {
                // Start loading ads
                SDKLog.i("Ads", "applovin initialized");
                mInitializeFinished = true;
                if (!TextUtils.isEmpty(mPreloadRewardedAdTu)) {
                    loadRewardedAd(mPreloadRewardedAdTu);
                    mPreloadRewardedAdTu = null;
                }
                if (!TextUtils.isEmpty(mPreloadInterstitialAdTu)) {
                    loadInterstitialAd(mPreloadInterstitialAdTu);
                    mPreloadInterstitialAdTu = null;
                }
                if (!TextUtils.isEmpty(mPreloadBannerAdTu)) {
                    showBannerAd(mPreloadBannerAdTu);
                    mPreloadBannerAdTu = null;
                }
            });
        });
        mInitialized = true;
    }

    // request consent before loading ads
    private void requestConsent() {
        Activity activity = UnityPlayer.currentActivity;
        Context context = activity.getApplicationContext();

//        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(context)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId("4F518F8A773C9B921981CF1E40598A4E")
//                .build();

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
//                .setConsentDebugSettings(debugSettings)
                .build();
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(context);
        consentInformation.requestConsentInfoUpdate(activity, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        formError -> {
                            if (formError != null) {
                                SDKLog.e("show consent failed " + formError.getErrorCode() + ", " + formError.getMessage());
                            }
                            boolean canRequestAds = consentInformation.canRequestAds();
                            SDKLog.i("has consent after request " + canRequestAds);
                            doInit(context);
                        }),
                formError -> {
                    SDKLog.e("consent update failed");
                    doInit(context);
                }
        );
        boolean canRequestAds = consentInformation.canRequestAds();
        SDKLog.i("has consent on start " + canRequestAds);
        if (canRequestAds) {
            doInit(context);
        }
    }

    // open applovin debug tool
    public void showDebugger() {
        AppLovinSdk.getInstance(UnityPlayer.currentActivity).showMediationDebugger();
    }

}
