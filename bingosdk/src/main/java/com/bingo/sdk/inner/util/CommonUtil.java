package com.bingo.sdk.inner.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bingo.sdk.utils.ResourceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些常用的方法
 */
public class CommonUtil {

    /**
     * 点击EditText 以外的其他地方 隐藏 软键盘
     * 建议在setContentView之后调用
     *
     * @param activity  activity
     * @param container 根布局(所要监听的区域)
     */
    public static void autoHideKeyboard(final Activity activity, final View container) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (!(container instanceof EditText)) {

            container.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    //这两句可能会影响一些控件的ripple点击效果,松开手指按下效果不恢复
                    //尚不清楚注释掉会引起什么问题,等待后续测试
//                    container.requestFocus();
//                    container.setFocusableInTouchMode(true);
                    hideSoftKeyboard(activity);
                    return false;
                }

            });
        }


        //If a layout container, iterate over children and seed recursion.
        if (container instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) container).getChildCount(); i++) {

                View innerView = ((ViewGroup) container).getChildAt(i);
                autoHideKeyboard(activity, innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View focus = activity.getCurrentFocus();
            if (focus != null) {
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String filterNull(String str) {
        if (TextUtils.isEmpty(str))
            return "";
        else
            return str.trim();
    }

    public static void setTextViewUnderline(TextView textView) {
        if (textView == null) {
            return;
        }

        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

    }

    public static boolean isMobileNumber(String mobile) {
        if (TextUtils.isEmpty(mobile) || mobile.length() != 11) {
            return false;
        }
        String reg = "(13|14|15|17|18|19)[0-9]{9}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    //dpi转px
    public static int dp2px(Context context, float dpi) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, context.getResources().getDisplayMetrics());
    }

    //px转dp
    public static int px2dp(Context context, float px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }

    //sp转px
    public static float sp2px(Context context, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    //px转sp
    public static float px2sp(Context context, float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }

    /**
     * 设置控件背景为 ?selectableItemBackground
     *
     * @param context
     * @param view
     */
    public static void setSelectableBackground(Context context, View view) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(ResourceManager.getAndroidResId(context, "attr", "selectableItemBackground"), outValue, true);
        view.setBackgroundResource(outValue.resourceId);
    }

    public static boolean isCardNumberValid(String idCardNo) {
        if (null == idCardNo) {
            return false;
        }
        idCardNo = idCardNo.toLowerCase();
        if (idCardNo.length() != 18 && idCardNo.length() != 15) {
            return false;
        }
        String[] RC = {"1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2"};
        boolean checkBirthday;
        String birthday;

        try {
            if (idCardNo.length() == 18) {// 18位
                birthday = idCardNo.substring(6, 14);
                checkBirthday = isDate(birthday);

                int[] W = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                int sum = 0;
                for (int i = 0; i < idCardNo.length() - 1; i++) {
                    int c = Integer.parseInt(idCardNo.substring(i, i + 1));
                    sum += W[i] * c;
                }
                int r = sum % 11;
                return RC[r].equals(idCardNo.substring(17)) && checkBirthday;

            } else {//15位
                birthday = "19" + idCardNo.substring(6, 12);
                checkBirthday = isDate(birthday);
                return checkBirthday;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 是否为合法日期
     *
     * @param dateString 日期
     * @return 是否合法
     */
    private static boolean isDate(String dateString) {
        boolean convertSuccess = true;
        // 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        String format = "yyyyMMdd";
        SimpleDateFormat smf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            smf.setLenient(false);
            smf.parse(dateString);
        } catch (ParseException e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 判断两个日期是否是同一天
     *
     * @param date1
     * @param date2
     * @return true:是, false:否
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);

        int c1Y = c1.get(Calendar.YEAR);
        int c1M = c1.get(Calendar.MONTH);
        int c1D = c1.get(Calendar.DAY_OF_MONTH);

//        LogUtil.i("日期1: " + c1Y + "-" + c1M + "-" + c1D);

        int c2Y = c2.get(Calendar.YEAR);
        int c2M = c2.get(Calendar.MONTH);
        int c2D = c2.get(Calendar.DAY_OF_MONTH);
//        LogUtil.i("日期2: " + c2Y + "-" + c2M + "-" + c2D);

        boolean isSameDay = c1Y == c2Y && c1M == c2M && c1D == c2D;
//        LogUtil.i("是否同一天: " + isSameDay);
        return isSameDay;

    }

    public static boolean isLandScape(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 解析获取 http://whois.pconline.com.cn/ipJson.jsp 返回的省份
     *
     * @param result
     */
    public static String getProvince(String result) {
        String reg = "(?<=(\"pro\":\"))[\\u4e00-\\u9fa5]+?(?=(\"))";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * 解析获取 http://whois.pconline.com.cn/ipJson.jsp 返回的城市
     *
     * @param result
     */
    public static String getCity(String result) {
        String reg = "(?<=(\"city\":\"))[\\u4e00-\\u9fa5]+?(?=(\"))";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }


    /**
     * 开启沉浸模式
     *
     * @param view 界面根view
     */
    public static void hideNav(@NonNull View view) {
        LogUtil.i("hide navigation bar ");
        if (view != null)
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


}
