package com.epixel.sdk.unity.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.epixel.sdk.unity.utils.SDKLog;

public class FirebaseImpl implements IFirebase {

    // initialize firebase
    public void init(Context context) {
        FirebaseApp.initializeApp(context);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean updated = task.getResult();
                SDKLog.i("Firebase", "fetch remote config updated: " + updated);
            }
            else {
                SDKLog.e("Firebase", "fetch remote config failed");
            }
        });
    }
}
