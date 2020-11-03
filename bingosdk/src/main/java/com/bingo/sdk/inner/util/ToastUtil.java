package com.bingo.sdk.inner.util;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bingo.sdk.utils.ResourceManager;


public class ToastUtil {


    private static View view;
    private static TextView tv_tip_content;
    private static Toast toast;

    public static void showShortToast(Context context, int content) {
        showTipToast(context, content, Toast.LENGTH_SHORT);
    }

    public static void showShortToast(Context context, String content) {
        showTipToast(context, content, Toast.LENGTH_SHORT);
    }


    public static void showLongToast(Context context, int content) {
        showTipToast(context, content, Toast.LENGTH_LONG);
    }


    public static void showLongToast(Context context, String content) {
        showTipToast(context, content, Toast.LENGTH_LONG);
    }


    private static void showTipToast(Context context, String content, int time) {
        try {
            initView(context);
            tv_tip_content.setText(content);
            toast.setDuration(time);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1)
                SafeToastUtil.hook(toast);
            toast.show();
            context = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showTipToast(Context context, int content, int time) {
        try {
            initView(context);
            tv_tip_content.setText(context.getString(content));
            toast.setDuration(time);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1)
                SafeToastUtil.hook(toast);
            toast.show();
            context = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void initView(Context context) {
        if (view == null)
            view = LayoutInflater.from(context).inflate(ResourceManager.getLayout(context, "layout_bingo_toast_tip"), null);
        if (tv_tip_content == null)
            tv_tip_content = view.findViewById(ResourceManager.getId(context, "tv_tip_content"));
        if (toast == null) {
            toast = new Toast(context);
            toast.setView(view);
            toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.dp2px(context, 108));
        }
    }


}
