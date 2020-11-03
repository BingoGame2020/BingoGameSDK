package com.bingo.sdk.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.constants.WebMethod;
import com.bingo.sdk.impl.WebViewJavaCallBack;
import com.bingo.sdk.inner.bean.PayResult;
import com.bingo.sdk.inner.consts.PayResultStatus;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.inner.util.ViewUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.ApiStatusCode;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.ResponseCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingoWebPayActivity extends AppCompatActivity {
    private final String WECHAT_SCHEME = "weixin://wap/pay?";//微信拉起支付的协议
    private final String ALIPAY_SCHEME = "alipays://platformapi/startApp?";//支付宝拉起支付的协议

    private final String WECHAT_DOWNLOAD_URL = "https://weixin.qq.com/";
    private final String ALIPAY_DOWNLOAD_URL = "https://mobile.alipay.com/";

    private final int TYPE_WECHAT = 1;
    private final int TYPE_ALIPAY = 2;
    private final int TYPE_OTHER = 3;
    private boolean isWechatOpened = false;
    private int queryTimes = 0;//查询次数
    private int maxQueryTimes = 3;//自动查询最大次数

    private WebView webView;
    private String reg = "(?<=(package=)).*?(?=(;))";
    private String url;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebViewJavaCallBack jsHandler;
    private static BingoSDKCallBack coreCallback;
    private String data;
    private TextView tv_title;
    private WebViewJavaCallBack.OnWebViewListener webListener;
    private WebChromeClient chromeClient;
    private WebViewClient webClient;
    private String orderInfo;//h5下单信息
    private long DELAY_TIME = 1000;
    private int REQUEST_PAY = 703;
    private ProgressDialog progressDialog;
    private JSONObject orderJsonObject;
    private AlertDialog.Builder userOperationBuilder;
    private AlertDialog userOperationDialog;

    public static void setPayCallBack(BingoSDKCallBack coreCallBack) {
        coreCallback = coreCallBack;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceManager.getLayout(this, "activity_bingo_web"));
        initData();
        setupActionBar();
        initListener();
        initWebView();
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(ResourceManager.getId(this, "toolbar"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        tv_title = findViewById(ResourceManager.getId(this, "tv_title"));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("url"))
                url = extras.getString("url");
            if (extras.containsKey("data")) {
                data = extras.getString("data");
            }

        }
        if (TextUtils.isEmpty(url)) {
            ToastUtil.showShortToast(this, "URL is empty");
            finish();
        }
        if (TextUtils.isEmpty(data)) {
            ToastUtil.showShortToast(this, "Pay data error");
            finish();
        }
    }

    private void initListener() {
        webListener = new WebViewJavaCallBack.OnWebViewListener() {
            @Override
            public void onEvent(int type, String result) {
                if (type == WebMethod.Pay.PAGE_CLOSE) {
                    if (coreCallback != null) {
                        coreCallback.onPayFinished(ApiStatusCode.CODE_FAILED, "取消支付");
                    }
                    finish();
                } else if (type == WebMethod.Pay.ORDER_INFO) {
                    //h5下单成功,同步订单信息到客户端
                    orderInfo = result;

                }
            }
        };


        chromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    LogUtil.e("加载完成");
                    jsHandler.callJS(1, data);//前面的type暂时没用
                }
            }
        };

        webClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                return false;
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = request.getUrl();
                    String scheme = uri.getScheme();
                    LogUtil.e("shouldOverrideUrlLoading url: " + uri + "\tscheme: " + scheme);

                    String url = uri.toString();
                    if (url.startsWith(WECHAT_SCHEME)) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        try {
                            startActivityForResult(intent, REQUEST_PAY);
                            LogUtil.e("can go back:" + webView.canGoBack());
                            //在拉起微信程序的时候,会先经过一个微信的处理订单的地址, 然后才到自定义协议拉起
                            //但是那个界面是白屏,并且会存在5秒钟,这里调起的时候手动关闭那个界面,避免白屏等待
                            webView.goBack();
//                            queryOrderStatus();
                            isWechatOpened = true;

                        } catch (ActivityNotFoundException e) {
                            LogUtil.e("can go back:" + webView.canGoBack());
                            webView.goBack();
                            isWechatOpened = false;
                            showNotInstallDialog(TYPE_WECHAT, WECHAT_DOWNLOAD_URL);
                        } catch (Exception e) {
                            LogUtil.e("can go back:" + webView.canGoBack());
                            webView.goBack();
                            showCallErrorDialog(e.getMessage());
                            isWechatOpened = false;
                        }


                        return true;
                    } else if (url.startsWith(ALIPAY_SCHEME)) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            showNotInstallDialog(TYPE_ALIPAY, ALIPAY_DOWNLOAD_URL);
                        } catch (Exception e) {
                            showCallErrorDialog(e.getMessage());
                        }

                        return true;
                    } else if (!url.startsWith("http") || !url.startsWith("https")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        try {
                            startActivityForResult(intent, REQUEST_PAY);
                        } catch (ActivityNotFoundException e) {
                            showNotInstallDialog(TYPE_OTHER, null);
                        } catch (Exception e) {
                            showCallErrorDialog(e.getMessage());
                        }

                        return true;
                    }


                    return false;
                }
                return false;

            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                //忽略ssl证书错误信息
//                handler.proceed();//注: 忽略证书错误信息将导致无法再play store上架
                final AlertDialog.Builder builder = new AlertDialog.Builder(BingoWebPayActivity.this, ResourceManager.getAndroidResId(BingoWebPayActivity.this, "style"
                        , "Theme_DeviceDefault_Light_Dialog"));
                builder.setMessage(ResourceManager.getString(BingoWebPayActivity.this, "bingo_error_ssl_cert_invalid"));
                builder.setPositiveButton(ResourceManager.getString(BingoWebPayActivity.this, "bingo_common_continue"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(ResourceManager.getString(BingoWebPayActivity.this, "bingo_common_dialog_cancel"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }


        };
    }

    /**
     * 查询订单状态, h5生成的订单信息,通过js回调给到了客户端
     */
    private void queryOrderStatus() {
        try {
            if (TextUtils.isEmpty(orderInfo)) {
                ToastUtil.showShortToast(BingoWebPayActivity.this, "订单信息获取失败");
                return;
            }
//            {"payRecordNumber":"2020102903520114174","rechargeType":2}
            orderJsonObject = new JSONObject(orderInfo);
            doQuery();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doQuery() {
        if (orderJsonObject == null) {
            return;
        }

        showWaitingDialog();
        queryTimes++;
        BingoHttpClient.postJson(ApiConfig.GET_PAY_RESULT, orderJsonObject, new ResponseCallBack<PayResult>() {
            @Override
            public void onSuccess(PayResult data) {
                LogUtil.e("查询支付结果成功: " + data);
                if (data.getStatus() == PayResultStatus.SUCCESSFUL) {
                    dismissWaitingDialog();
                    dismissUserOperationDialog();
                    if (coreCallback != null) {
                        coreCallback.onPayFinished(ApiStatusCode.CODE_SUCCESS, data.getPayRecordNumber());
                    }
                    finish();
                } else {
                    //失败状态或者其他
                    if (queryTimes < maxQueryTimes) {
                        //如果次数小于5,3秒后继续查询
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doQuery();
                            }
                        }, DELAY_TIME);
                    } else {
                        showUserOperationDialog(PayResultStatus.getCodeDesc(data.getStatus()));
                    }
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                LogUtil.e("查询支付结果失败: " + msg);
                if (queryTimes < maxQueryTimes) {
                    //如果次数小于5,3秒后继续查询
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doQuery();
                        }
                    }, DELAY_TIME);
                } else {
                    showUserOperationDialog(msg);
                }
            }

            @Override
            public void onError(int code, String error) {
                LogUtil.e("查询支付结果错误:" + code + "\t" + error);
                if (queryTimes < maxQueryTimes) {
                    //如果次数小于5,3秒后继续查询
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doQuery();
                        }
                    }, DELAY_TIME);
                } else {
                    showUserOperationDialog(error);
                }
            }
        });

    }

    private void showWaitingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在查询订单状态");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissWaitingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * 多次查询后失败,给用户手动操作
     */
    private void showUserOperationDialog(String status) {
        dismissWaitingDialog();
        if (userOperationBuilder == null) {
            userOperationBuilder = new AlertDialog.Builder(this);
        }

        View view = LayoutInflater.from(this).inflate(ResourceManager.getLayout(this, "layout_bingo_pay_result_error"), null);
        Button btn_done = view.findViewById(ResourceManager.getId(this, "btn_done"));
        Button btn_cancel = view.findViewById(ResourceManager.getId(this, "btn_cancel"));
        TextView tv_tip = view.findViewById(ResourceManager.getId(this, "tv_tip"));
        TextView tv_status = view.findViewById(ResourceManager.getId(this, "tv_status"));
        final TextView tv_order = view.findViewById(ResourceManager.getId(this, "tv_order"));
        tv_order.setText("订单号:" + orderJsonObject.optString("payRecordNumber"));
        tv_order.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copy(tv_order);
                return true;
            }
        });

        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == ResourceManager.getId(BingoWebPayActivity.this, "btn_done")) {
                    dismissUserOperationDialog();
                    doQuery();
                } else if (id == ResourceManager.getId(BingoWebPayActivity.this, "btn_cancel")) {
                    if (coreCallback != null) {
                        coreCallback.onPayFinished(ApiStatusCode.CODE_FAILED, "取消支付");
                    }
                    dismissUserOperationDialog();
                    finish();
                }
            }
        };
        btn_done.setOnClickListener(l);
        btn_cancel.setOnClickListener(l);
        tv_status.setText("支付状态: " + status);

        ViewUtil.setPayTipPan(this, tv_tip);

        userOperationBuilder.setView(view);
        userOperationDialog = userOperationBuilder.show();
        userOperationDialog.setCancelable(false);


    }

    private void dismissUserOperationDialog() {
        if (userOperationDialog != null && userOperationDialog.isShowing()) {
            userOperationDialog.dismiss();
            userOperationDialog = null;
            userOperationBuilder = null;
        }
    }

    /**
     * 显示拉起支付异常的弹框
     *
     * @param message
     */
    private void showCallErrorDialog(String message) {

    }

    /**
     * 显示设备未安装微信或支付宝的弹框
     *
     * @param type
     */
    private void showNotInstallDialog(int type, final String downloadUrl) {
        String msg;
        if (type == TYPE_WECHAT) {
            msg = "未检测到微信客户端,请安装后重试";
        } else if (type == TYPE_ALIPAY) {
            msg = "未检测到支付宝客户端,请安装后重试";
        } else {
            msg = "未检测到客户端,请安装后重试";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        if (type == TYPE_OTHER) {
            builder.setPositiveButton("应用商店", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Pattern pattern = Pattern.compile(reg);
                    Matcher matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        String packageName = matcher.group();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showShortToast(BingoWebPayActivity.this, "打开应用商店失败,请自行下载安装");
                        }
                    }
                }
            });
        } else {
            builder.setPositiveButton("立即安装", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(downloadUrl));
                    startActivity(intent);
                }
            });
        }
        builder.setNegativeButton("取消", null);
        builder.show();

    }

    private void initWebView() {
        webView = findViewById(ResourceManager.getId(this, "webView"));

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
        CookieManager.getInstance().flush();
        jsHandler = new WebViewJavaCallBack(webView);
        jsHandler.setOnWebViewListener(webListener);

        webView.addJavascriptInterface(jsHandler, "android");


        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", ApiConfig.URL_TEST);//微信H5支付需要添加,否则会提示参数格式错误
        webView.loadUrl(url, extraHeaders);

        webView.setWebChromeClient(chromeClient);

        webView.setWebViewClient(webClient);
    }


    @Override
    public void onBackPressed() {
//        if (webView != null && webView.canGoBack()) {
//            webView.goBack();
//        } else {
//            super.onBackPressed();
//        }
        if (coreCallback != null) {
            coreCallback.onPayFinished(ApiStatusCode.CODE_FAILED, "取消支付");
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            //这里如果不清空会保存数据,使得app cache变大
//            webView.clearFormData();
//            webView.clearCache(true);
            webView.destroy();
            webView = null;
            jsHandler = null;
            //activity开启了单独进程才需要结束
//            Process.killProcess(Process.myPid());
        }
        if (jsHandler != null) {
            jsHandler.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.e("onActivityResult: " + requestCode + "\t" + resultCode + "\t" + data);
        if (requestCode == REQUEST_PAY) {
            if (isWechatOpened)
                queryOrderStatus();
        }
    }


    public void copy(final TextView textView) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String content = textView.getText().toString();
        String order = content.split(":")[1];
        LogUtil.e("复制的订单号: " + order);
        ClipData data = ClipData.newPlainText("data", CommonUtil.filterNull(order));
        clipboard.setPrimaryClip(data);
        ToastUtil.showShortToast(BingoWebPayActivity.this, "已复制到剪贴板");
    }
}