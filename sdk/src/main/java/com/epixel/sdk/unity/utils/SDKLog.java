package com.epixel.sdk.unity.utils;

import android.text.TextUtils;
import android.util.Log;

public class SDKLog {

    private static final boolean showLog = false;

    public static void i(String log) {
        if (showLog) {
            Log.i("[MySDK]", log);
        }
    }

    public static void w(String log) {
        if (showLog) {
            Log.w("[MySDK]", log);
        }
    }

    public static void e(String log) {
        if (showLog) {
            Log.e("[MySDK]", log);
        }
    }

    public static void i(String tag, String log) {
        if (showLog) {
            Log.i(formatTag(tag), log);
        }
    }

    public static void w(String tag, String log) {
        if (showLog) {
            Log.w(formatTag(tag), log);
        }
    }

    public static void e(String tag, String log) {
        if (showLog) {
            Log.e(formatTag(tag), log);
        }
    }

    private static String formatTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return "[MySDK]";
        }
        else {
            return "[MySDK." + tag + "]";
        }
    }
}
