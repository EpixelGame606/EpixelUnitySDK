package com.epixel.sdk.unity.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class Utils {

    private static SharedPreferences sSharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = context.getSharedPreferences("my_sdk", Context.MODE_PRIVATE);
        }
        return  sSharedPreferences;
    }

    public static boolean isMainProcess(Context context) {
        String processName = null;

        if (Build.VERSION.SDK_INT >= 28) {
            processName = Application.getProcessName();
        }
        else {
            Context applicationContext = context.getApplicationContext();
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
                    ((ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            if (runningAppProcesses != null && runningAppProcesses.size() != 0) {
                for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (runningAppProcessInfo.pid == android.os.Process.myPid()) {
                        processName = runningAppProcessInfo.processName;
                        break;
                    }
                }
            }
        }
        return TextUtils.isEmpty(processName) || context.getPackageName().equals(processName);
    }
}
