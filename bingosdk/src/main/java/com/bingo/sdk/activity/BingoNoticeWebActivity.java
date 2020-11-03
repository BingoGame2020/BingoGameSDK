package com.bingo.sdk.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;

import java.net.URISyntaxException;

/**
 * 游戏公告
 */
public class BingoNoticeWebActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private AppCompatImageView iv_back, iv_forward, iv_refresh;
    private Button btn_back_game;
    private ObjectAnimator animator;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceManager.getLayout(this, "activity_bingo_notice_web"));
        initData();
        initView();
        setListener();
        setWebView();
        reLayout();
        CommonUtil.hideNav(getWindow().getDecorView());
    }

    private void initData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("url")) {
                url = extras.getString("url");
                if (TextUtils.isEmpty(url)) {
                    ToastUtil.showShortToast(this, "URL为空");
                    finish();
                }
            }
        }
    }

    /**
     * 设置大小
     */
    private void reLayout() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int height;
        int width;
        if (CommonUtil.isLandScape(this)) {
            height = (int) (metrics.heightPixels * 0.8);
            width = (int) (metrics.widthPixels * 0.8);
        } else {
            width = (int) (metrics.widthPixels * 0.9);
            height = (int) (metrics.heightPixels * 0.8);
        }

        lp.height = height;
        lp.width = width;
        getWindow().setAttributes(lp);
        setFinishOnTouchOutside(false);
    }


    private void initView() {
        webView = findViewById(ResourceManager.getId(this, "webView"));

        iv_back = findViewById(ResourceManager.getId(this, "iv_back"));
        iv_forward = findViewById(ResourceManager.getId(this, "iv_forward"));
        iv_refresh = findViewById(ResourceManager.getId(this, "iv_refresh"));
        btn_back_game = findViewById(ResourceManager.getId(this, "btn_back_game"));

    }

    private void setListener() {

        iv_back.setOnClickListener(this);
        iv_forward.setOnClickListener(this);
        iv_refresh.setOnClickListener(this);
        btn_back_game.setOnClickListener(this);

    }


    private void setWebView() {
        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        setting.setSupportZoom(true);
        setting.setSaveFormData(true);
        setting.setBuiltInZoomControls(false);
        setting.setUseWideViewPort(true);
        setting.setSupportMultipleWindows(false);
        setting.setLoadWithOverviewMode(true);
        setting.setAppCacheEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//允许加载非https的内容,5.0以下默认是允许的,而5.0及以上默认禁止
        setting.setAppCachePath(getDir("appcache", 0).getPath());
        setting.setGeolocationDatabasePath(getDir("geolocation", 0).getPath());
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    LogUtil.e("load progress 100");
                    setEnabled(iv_refresh);
                    if (animator != null && animator.isRunning()) {
                        animator.end();
                    }

                    if (view.canGoBack()) {
                        //返回按钮设置成白色
                        setEnabled(iv_back);
                    } else {
                        setDisable(iv_back);
                    }

                    if (view.canGoForward()) {
                        //返回按钮设置成白色
                        setEnabled(iv_forward);
                    } else {
                        setDisable(iv_forward);
                    }
                }

            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtil.e("onPageStarted: " + url);
                setDisable(iv_refresh);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                //忽略ssl证书错误信息
//                handler.proceed();//注: 忽略证书错误信息将导致无法再play store上架
                final AlertDialog.Builder builder = new AlertDialog.Builder(BingoNoticeWebActivity.this, ResourceManager.getAndroidResId(BingoNoticeWebActivity.this, "style", "Theme_DeviceDefault_Light_Dialog"));
                builder.setMessage(ResourceManager.getString(BingoNoticeWebActivity.this, "bingo_error_ssl_cert_invalid"));
                builder.setPositiveButton(ResourceManager.getString(BingoNoticeWebActivity.this, "bingo_common_continue"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(ResourceManager.getString(BingoNoticeWebActivity.this, "bingo_common_dialog_cancel"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                    //用浏览器打开中转地址，调起微信客户端发起支付
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(getPackageManager()) != null) {  //存在
                            startActivity(intent);
                        } else {    //不存在
                            LogUtil.d("package not exists");
                            if ("weixin".equals(uri.getScheme())) {
                                ToastUtil.showShortToast(BingoNoticeWebActivity.this, "请安装微信客户端后再使用");
                                return false;
//                                finish();
                            }
//
//                            Pattern pattern = Pattern.compile(reg);
//                            Matcher matcher = pattern.matcher(url);
//                            if (matcher.find()) {
//                                String packageName = matcher.group();
//                                try {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
//                                } catch (android.content.ActivityNotFoundException anfe) {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
//                                }
//                            }
                        }
                        return true;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }

                return false;
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = request.getUrl();
                    String scheme = uri.getScheme();
                    if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                        try {
                            String uriString = uri.toString();
                            Intent intent = Intent.parseUri(uriString, Intent.URI_INTENT_SCHEME);
                            if (intent.resolveActivity(getPackageManager()) != null) {  //存在
                                startActivity(intent);
                            } else {    //不存在
                                LogUtil.d("package not exists");
                                if ("weixin".equals(uri.getScheme())) {
                                    ToastUtil.showShortToast(BingoNoticeWebActivity.this, "请安装微信客户端后再使用");
                                    return false;
//                                    finish();
                                }
//
//                                Pattern pattern = Pattern.compile(reg);
//                                Matcher matcher = pattern.matcher(uriString);
//                                if (matcher.find()) {
//                                    String packageName = matcher.group();
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
//                                    }
//                                }

                            }
                            return true;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
                return false;
            }

        });


        webView.loadUrl(url);
    }

    private void setDisable(AppCompatImageView view) {
        view.setColorFilter(ContextCompat.getColor(BingoNoticeWebActivity.this, ResourceManager.getColor(this, "color_bingo_gray_disabled")));
        view.setEnabled(false);
    }

    private void setEnabled(AppCompatImageView view) {
        view.setColorFilter(0);
        view.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResourceManager.getId(this, "tv_title")) {
            finish();
        } else if (id == ResourceManager.getId(this, "iv_refresh")) {
            if (webView != null) {
                webView.reload();
            }
            animate();
        } else if (id == ResourceManager.getId(this, "iv_back")) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            }
        } else if (id == ResourceManager.getId(this, "iv_forward")) {
            if (webView != null && webView.canGoForward()) {
                webView.goForward();
            }
        } else if (id == ResourceManager.getId(this, "btn_back_game")) {
            //导航栏返回按钮
            finish();
        }
    }


    private void animate() {
        animator = ObjectAnimator.ofFloat(iv_refresh, View.ROTATION, 360);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1000);
        animator.start();

    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}