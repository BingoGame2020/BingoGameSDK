package com.bingo.sdk.inner.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.activity.BingoNoticeWebActivity;
import com.bingo.sdk.activity.BingoWebActivity;
import com.bingo.sdk.utils.ResourceManager;

public class ViewUtil {


    public static void setPrivacySpan(final Context context, TextView textView, String content) {
        textView.setHighlightColor(Color.TRANSPARENT);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(content);
        //第一个 < 出现的位置
        final int firstStart = content.indexOf("<");

        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
//                LogUtil.e("用户协议:" + widget);
                Intent intent = new Intent(context, BingoNoticeWebActivity.class);
                String url = BingoSdkCore.getInstance().getGameConfig().getGame().getPrivacyUrl();
                intent.putExtra("url", CommonUtil.filterNull(url));
                intent.putExtra("title", "冰果用户协议");
                context.startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {

                //设置文件颜色
                ds.setColor(context.getResources().getColor(ResourceManager.getColor(context, "color_bingo_gray_text")));
                // 去掉下划线
                ds.setUnderlineText(true);
            }
        }, firstStart, firstStart + 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //同意<用户协议>和<隐私政策>
        //+6 表示用户协议加上括号总共6个字的长度
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void setPayTipPan(final Context context, TextView textView) {
        String content = context.getString(ResourceManager.getString(context, "tip_pay_result"));
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(content);
        //第一个 < 出现的位置
        final int firstStart = content.indexOf("[支付遇到问题");

        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(context, BingoWebActivity.class);
                String url = BingoSdkCore.getInstance().getGameConfig().getGame().getCustomerUrl();
                intent.putExtra("url", CommonUtil.filterNull(url));
                context.startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {

                //设置文件颜色
                ds.setColor(context.getResources().getColor(ResourceManager.getColor(context, "color_bingo_common_text_red")));
                // 去掉下划线
                ds.setUnderlineText(true);
            }
        }, firstStart, firstStart + 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //同意<用户协议>和<隐私政策>
        //+6 表示用户协议加上括号总共6个字的长度
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    /**
     * 拦截超链接
     * 在sdk内部打开网址,不使用系统浏览器
     *
     * @param tv 要拦截的textview
     */
    private void interceptHyperLink(Context context, TextView tv, String title) {
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tv.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable spannable = (Spannable) tv.getText();
            URLSpan[] urlSpans = spannable.getSpans(0, end, URLSpan.class);
            if (urlSpans.length == 0) {
                return;
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            // 循环遍历并拦截 所有http://开头的链接
            for (URLSpan uri : urlSpans) {
                String url = uri.getURL();
                if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0) {
                    CustomUrlSpan customUrlSpan = new CustomUrlSpan(context, url, title);
                    spannableStringBuilder.setSpan(customUrlSpan, spannable.getSpanStart(uri),
                            spannable.getSpanEnd(uri), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            tv.setText(spannableStringBuilder);
        }

    }


    private class CustomUrlSpan extends ClickableSpan {

        private Context context;
        private String title;
        private String url;

        private CustomUrlSpan(Context context, String url, String title) {
            this.context = context;
            this.url = url;
            this.title = title;
        }

        @Override
        public void onClick(View widget) {
            // 在这里可以做任何自己想要的处理
            Intent intent = new Intent(context, BingoNoticeWebActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("title", title);
            context.startActivity(intent);
        }
    }
}
