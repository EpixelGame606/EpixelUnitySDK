package com.epixel.sdk.unity.analytics;

import java.util.Map;

public interface IAnalytics {
    void logEvent(String eventName, String dataJson);
    void logEvent(String eventName, Map<String, String> data);
}
