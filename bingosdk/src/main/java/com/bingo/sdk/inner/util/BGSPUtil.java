package com.bingo.sdk.inner.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;


public class BGSPUtil {
    public static final String KEY_PROVINCE = "province";
    public static final String KEY_CITY = "city";
    public static final String KEY_DEVICE_CODE = "device_code";
    private static final String preferenceName = "bingoSdkSetting";
    public static final String KEY_OAID = "oaid";
    /**
     * 上次实名框弹出时间
     */
    public static final String KEY_REALNAME_SHOW_TIME = "real_name_show_time";
    /**
     * 记录获取验证码倒计时界面关闭的时间,用来判断下次重新打开是否需要重新开始 还是继续上次的倒计时
     */
    public static final String KEY_VERIFY_CODE_EXIT_TIME = "verify_code_exit_time";
    /**
     * 记录获取验证码倒计时界面关闭的剩余倒计时,用于判断倒计时开始的时间
     */
    public static final String KEY_VERIFY_CODE_LEFT_TIME = "verify_code_left_time";

    /**
     * 当前登录的用户token
     */
    public static final String KEY_CURRENT_ACCOUNT_TOKEN = "current_token";

    /**
     * 当前登录的用户id
     */
    public static final String KEY_CURRENT_ACCOUNT_USER_NAME = "current_user_name";

    /**
     * 浮标位置
     */
    public static final String KEY_FLOATING_VIEW_X = "floating_x";
    public static final String KEY_FLOATING_VIEW_Y = "floating_y";


    public static void save(Context context, String key, Object value) {
//        LogUtil.i(TAG, "save: key -> " + key + "\tvalue -> " + value);
        SharedPreferences preference = getReference(context);
        SharedPreferences.Editor editor = preference.edit();
        if (value instanceof String)
            editor.putString(key, value.toString());
        else if (value instanceof Integer)
            editor.putInt(key, Integer.parseInt(value.toString()));
        else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof JSONArray) {
            editor.putString(key, value.toString());
        } else if (value instanceof JSONObject) {
            editor.putString(key, value.toString());
        } else {
            throw new IllegalArgumentException("unsupported object");
        }
        editor.apply();
    }

    private static SharedPreferences getReference(Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }


    public static Object get(Context context, String key) {
        Map<String, ?> all = getReference(context).getAll();
        return all.get(key);
    }
}
