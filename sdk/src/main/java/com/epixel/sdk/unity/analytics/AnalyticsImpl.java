package com.epixel.sdk.unity.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

// implement firebase analytics
public class AnalyticsImpl implements IAnalytics {

    // send game event to Firebase analytics
    @Override
    public void logEvent(String eventName, String dataJson) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(dataJson);
        } catch (JSONException e) {
            return;
        }
        Bundle bundle = new Bundle();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object object = jsonObject.opt(key);
            if (object != null) {
                bundle.putString(key, object.toString());
            }
        }
        UnityPlayer.currentActivity.runOnUiThread(() -> {
            Context context = UnityPlayer.currentActivity.getApplicationContext();
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.logEvent(eventName, bundle);
        });
    }

    // send game event to Firebase analytics
    @Override
    public void logEvent(String eventName, Map<String, String> data) {
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }
        Context context = UnityPlayer.currentActivity.getApplicationContext();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(eventName, bundle);
    }
}
