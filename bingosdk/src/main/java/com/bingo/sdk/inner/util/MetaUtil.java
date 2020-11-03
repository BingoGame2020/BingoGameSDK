package com.bingo.sdk.inner.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MetaUtil {

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }



    public static String getString(Context context, String key, String defaultValue) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null && metaData.containsKey(key)) {
                return metaData.getString(key);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static int getInteger(Context context, String key) {
        return getInteger(context, key, -1);
    }


    public static int getInteger(Context context, String key, int defaultValue) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null && metaData.containsKey(key)) {
                return metaData.getInt(key);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }


    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null && metaData.containsKey(key)) {
                return metaData.getBoolean(key);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
