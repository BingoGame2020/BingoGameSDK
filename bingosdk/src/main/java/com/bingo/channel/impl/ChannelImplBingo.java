package com.bingo.channel.impl;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.activity.BingoChangeAccountActivity;
import com.bingo.sdk.activity.BingoLoginActivity;
import com.bingo.sdk.activity.BingoWebActivity;
import com.bingo.sdk.activity.BingoWebPayActivity;
import com.bingo.sdk.adapter.BingoAccountPanelAdapter;
import com.bingo.sdk.bean.InitOptions;
import com.bingo.sdk.bean.RechargeOptions;
import com.bingo.sdk.bean.RoleEventOptions;
import com.bingo.sdk.callback.BingoExitCallBack;
import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.db.BingoDBManager;
import com.bingo.sdk.impl.BaseInterface;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.EmptyBean;
import com.bingo.sdk.inner.bean.FloatWindow;
import com.bingo.sdk.inner.bean.PaymentMethod;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.consts.CommonCallBackCode;
import com.bingo.sdk.inner.interf.CommonCallback;
import com.bingo.sdk.inner.interf.LoginCallBack;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.DialogUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.view.FloatingActionView;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.ApiStatusCode;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.HttpUtil;
import com.bingo.sdk.web.ResponseCallBack;
import com.bingo.sdk.worker.BingoScheduledWorkerHelper;

import org.json.JSONObject;

public class ChannelImplBingo implements BaseInterface {
    private FloatingActionView floatView;
    private ProgressDialog progressDialog;
    public boolean isLogin;
    private BingoSDKCallBack coreCallBack;

    @Override
    public void init(Activity activity, InitOptions opts, BingoSDKCallBack callBack) {
        this.coreCallBack = callBack;
    }

    @Override
    public void pay(final Activity activity, final RechargeOptions opts) {
        progressDialog = DialogUtil.getInstance().showProgressDialog(activity, "请稍后");
        JSONObject json = ApiUtil.getBaseParams(activity);
        BingoHttpClient.postJson(ApiConfig.GET_PAYMENT, json, new ResponseCallBack<PaymentMethod>() {
            @Override
            public void onSuccess(final PaymentMethod data) {
                LogUtil.e("是否走平台支付: " + data.isPlatformPay());
                //切到主线程
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                if (data.isPlatformPay()) {

                    BingoWebPayActivity.setPayCallBack(coreCallBack);

                    JSONObject json = ApiUtil.getPayOrderParams(activity, opts);
                    Intent intent = new Intent(activity, BingoWebPayActivity.class);
                    intent.putExtra("url", ApiConfig.URL_PAY);
                    intent.putExtra("data", json.toString());
                    activity.startActivity(intent);

                } else {
                    //todo 走渠道支付
                    channelPay(activity, opts);
                }
            }


            @Override
            public void onFailed(int code, final String msg) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                ToastUtil.showShortToast(activity, "支付请求失败: " + msg);
            }


            @Override
            public void onError(int code, final String error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                ToastUtil.showShortToast(activity, "支付请求错误: " + error);

            }
        });

    }

    @Override
    public void channelPay(Activity activity, RechargeOptions opts) {

    }

    @Override
    public void login(final Activity activity) {
        Intent intent = new Intent(activity, BingoLoginActivity.class);
        activity.startActivity(intent);

        //这里new一个callback是因为需要用调用方法的时候传入的activity对象来显示弹框
        //如果用BingoLoginActivity对象,会因为activity销毁而报错
        BingoLoginActivity.setLoginCallBack(new LoginCallBack() {
            @Override
            public void onLogin(boolean success, String msg, final UserInfo userInfo) {
                if (success) {
                    isLogin = true;
                    coreCallBack.onLoginFinished(ApiStatusCode.CODE_SUCCESS, AccountUtil.getCallBackCPAccount(activity));
                    showFloat(activity);
                    DialogUtil.getInstance().showTopEnterDialog(activity, "登录成功:" + userInfo.getUserName(), new CommonCallback() {
                        @Override
                        public void onCallBack(int code, String msg) {
                            if (code == CommonCallBackCode.CHANGE_ACCOUNT) {
                                changeAccount(activity);
                            } else if (code == CommonCallBackCode.WELCOME_DIALOG_DISMISS) {
                                showRealNameDialog(activity, userInfo);
                            }
                        }
                    });

                } else {
                    isLogin = false;
                    coreCallBack.onLoginFinished(ApiStatusCode.CODE_FAILED, msg);
                }
            }


        });

    }


    @Override
    public void autoLogin(final Activity activity) {
        Account loginAccount = AccountUtil.getCurrentLoginAccount(activity);
        LogUtil.i("上次登录的账号:" + loginAccount);
        HttpUtil.autoLogin(activity, loginAccount.getUid(), new LoginCallBack() {

            @Override
            public void onLogin(boolean success, String msg, final UserInfo userInfo) {
                if (success) {
                    isLogin = true;
                    coreCallBack.onLoginFinished(ApiStatusCode.CODE_SUCCESS, AccountUtil.getCallBackCPAccount(activity));
                    showFloat(activity);
                    DialogUtil.getInstance().showTopEnterDialog(activity, "登录成功:" + userInfo.getUserName(), new CommonCallback() {
                        @Override
                        public void onCallBack(int code, String msg) {
                            if (code == CommonCallBackCode.CHANGE_ACCOUNT) {
                                changeAccount(activity);
                            } else if (code == CommonCallBackCode.WELCOME_DIALOG_DISMISS) {
                                //显示实名认证弹框
                                showRealNameDialog(activity, userInfo);
                            }
                        }
                    });


                } else {
                    isLogin = false;
                    coreCallBack.onLoginFinished(ApiStatusCode.CODE_FAILED, msg);
                    login(activity);//自动登录失败 确定需要自动登录? 而不是用户点击再登录
                }
            }

        });
    }

    private void showRealNameDialog(final Activity activity, final UserInfo userInfo) {
//
//        if (userInfo.getIsRealName() == 0) {
//            DialogUtil.getInstance().showRealNameDialog(activity, userInfo.isForceRealName(), new RealNameCallBack() {
//                @Override
//                public void onSuccess() {
//                    coreCallBack.onRealNameFinished(ApiStatusCode.CODE_SUCCESS);
//                }
//
//                @Override
//                public void onFailed() {
//                    coreCallBack.onRealNameFinished(ApiStatusCode.CODE_FAILED);
//                }
//            });
//        }
    }

    @Override
    public void changeAccount(final Activity activity) {
        logout(activity);
        Intent intent = new Intent(activity, BingoChangeAccountActivity.class);
        activity.startActivity(intent);

        BingoChangeAccountActivity.setLoginCallBack(new LoginCallBack() {

            @Override
            public void onLogin(boolean success, String msg, UserInfo userInfo) {

                LogUtil.i("Impl onChangeAccount:" + success + "\tcallback: " + coreCallBack);
                if (success) {
                    isLogin = true;
                    if (coreCallBack != null)
                        coreCallBack.onChangeAccount(ApiStatusCode.CODE_SUCCESS, AccountUtil.getCallBackCPAccount(activity));
                    //这里new一个callback是因为需要用调用方法的时候传入的activity对象来显示弹框
                    //如果用BingoLoginActivity对象,会因为activity销毁而报错

                    DialogUtil.getInstance().showTopEnterDialog(activity, "登录成功:" + userInfo.getUserName(), new CommonCallback() {
                        @Override
                        public void onCallBack(int code, String msg) {
                            if (code == CommonCallBackCode.CHANGE_ACCOUNT) {
                                changeAccount(activity);
                            }
                        }
                    });
                } else {
                    isLogin = false;
                    if (coreCallBack != null)
                        coreCallBack.onChangeAccount(ApiStatusCode.CODE_FAILED, msg);
                }
            }

        });

    }

    @Override
    public void logout(final Activity activity) {
        if (!isLogin()) {
            return;
        }
        BingoHttpClient.postJson(ApiConfig.LOG_OUT, null, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(EmptyBean data) {
                LogUtil.e("注销成功");
                AccountUtil.clearCurrentLoginAccount(activity);
                BingoScheduledWorkerHelper.getInstance().cancelOnlineWorker();
                isLogin = false;
                if (coreCallBack != null) {
                    coreCallBack.onLogout();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                LogUtil.e("注销失败: " + msg);
                ToastUtil.showShortToast(activity, "注销失败" + msg);
            }

            @Override
            public void onError(int code, String error) {
                LogUtil.e("注销失败: " + error);
                ToastUtil.showShortToast(activity, "注销失败" + error);
            }
        });


    }

    @Override
    public void onExit(Activity activity, BingoExitCallBack callBack) {
        if (floatView != null && floatView.isAccountPanelShowing()) {
            floatView.dismissPopupWindow();
            return;
        }
        DialogUtil.getInstance().showExitDialog(activity, callBack);
    }

    @Override
    public void onApplicationCreate(Application app) {
    }


    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {
        showFloat(activity);

    }

    @Override
    public void onPause(Activity activity) {
        hideFloat();
    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onRestart(Activity activity) {

    }

    @Override
    public void onNewIntent(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {
        destroyFloat();
        BingoDBManager.getInstance().close();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        LogUtil.e("Bingo impl onActivityResult:" + requestCode + "\t" + resultCode + "\t" + data);
    }

    @Override
    public void onRoleEvent(Activity activity, int type, RoleEventOptions options) {
        JSONObject json = ApiUtil.getLogEventParams(activity, options, type);
        BingoHttpClient.postJson(ApiConfig.LOG_EVENT, json, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(EmptyBean data) {
                LogUtil.i("打点成功");
            }

            @Override
            public void onFailed(int code, String msg) {
                LogUtil.i("打点失败:" + msg);
            }

            @Override
            public void onError(int code, String error) {
                LogUtil.i("打点错误: " + error);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }


    public void showFloat(Activity activity) {
        if (!isLogin) {
            LogUtil.i("未登录,忽略悬浮窗显示");
            return;
        }
        if (BingoSdkCore.getInstance().getGameConfig().getGame().getFloatWindowsStatus() != 1) {
            LogUtil.e("配置不显示悬浮窗,忽略");
            return;
        }

        LogUtil.e("showFloat:" + activity + "\t" + floatView);
        if (floatView == null) {
            floatView = FloatingActionView.getInstance(activity);
            setFloatingViewListener(floatView, activity);
        }
        LogUtil.e("是否被添加: " + floatView.isAdded());
        floatView.display(activity);
    }

    private void setFloatingViewListener(FloatingActionView floatView, final Activity activity) {
        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == ResourceManager.getId(activity, "tv_change_account")) {
                    changeAccount(activity);

                }

            }
        });

        floatView.setOnItemClickListener(new BingoAccountPanelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FloatWindow window) {
                if (isLogin) {
                    Intent intent = new Intent(activity, BingoWebActivity.class);
                    intent.putExtra("url", window.getH5Url());
                    activity.startActivity(intent);
                } else {
                    changeAccount(activity);
                }
            }
        });
    }

    public void hideFloat() {
        if (floatView != null) {
            floatView.disappear();
        }
    }

    public void destroyFloat() {
        if (floatView != null) {
            floatView.destroy();
            floatView = null;
        }
    }

    public boolean isLogin() {
        return isLogin;
    }

}