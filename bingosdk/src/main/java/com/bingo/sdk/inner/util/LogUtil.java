package com.bingo.sdk.inner.util;


import android.text.TextUtils;
import android.util.Log;

import com.bingo.sdk.BuildConfig;

/**
 * Created by OuYang on 2018/5/31.
 */
public class LogUtil {

    private static boolean enabled = BuildConfig.DEBUG;

    public static void setEnabled(boolean enabled) {
        LogUtil.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setDefaultTag(String defaultTag) {
        if (!TextUtils.isEmpty(defaultTag))
            DEFAULT_TAG = defaultTag;
    }

    public static String getDefaultTag() {
        return DEFAULT_TAG;
    }

    /**
     * 已弃用,现根据打包类型决定是否开启
     * <p>
     * 设置log 开关</p>
     * 建议在Application 中设置开启
     * <pre>
     * LogUtil.init(BuildConfig.DEBUG,"your_default_log_tag");
     * </pre>
     *
     * @param enabled    是否开启log
     * @param defaultTag 默认log tag
     */
    public static void init(boolean enabled, String defaultTag) {
        setEnabled(enabled);
        setDefaultTag(defaultTag);
    }

    private static String DEFAULT_TAG = "Bingo";

    public static void i(String tag, String msg) {
        if (enabled) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        if (enabled) {
            Log.i(DEFAULT_TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (enabled) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        if (enabled) {
            Log.d(DEFAULT_TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (enabled) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        if (enabled) {
            Log.e(DEFAULT_TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (enabled) {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg) {
        if (enabled) {
            Log.w(DEFAULT_TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (enabled) {
            Log.v(tag, msg);
        }
    }

    public static void v(String msg) {
        if (enabled) {
            Log.v(DEFAULT_TAG, msg);
        }
    }

    /**
     * 用于打印异常
     *
     * @param e exception
     */
    public static void printStackTrace(Exception e) {
        if (enabled) {
            e.printStackTrace();
        }
    }

    /**
     * 用于打印错误
     *
     * @param e exception
     */
    public static void printStackTrace(Error e) {
        if (enabled) {
            e.printStackTrace();
        }
    }
}
