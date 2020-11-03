package com.bingo.sdk.web;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.db.BingoDBManager;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.EmptyBean;
import com.bingo.sdk.inner.bean.Game;
import com.bingo.sdk.inner.bean.GameConfig;
import com.bingo.sdk.inner.bean.OnlineBean;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.consts.CommonCallBackCode;
import com.bingo.sdk.inner.interf.CommonCallback;
import com.bingo.sdk.inner.interf.LoginCallBack;
import com.bingo.sdk.inner.interf.RealNameCallBack;
import com.bingo.sdk.inner.log.GDTLogHelper;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.BGSPUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.DialogUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bytedance.applog.GameReportHelper;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    private static Gson gson = new Gson();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static ProgressDialog autoLoginProgressDialog;

    public static void reportStartData(Application application) {
        JSONObject json = ApiUtil.getStartReportData(application.getApplicationContext());
//        LogUtil.e("收集数据:\n" + json.toString());
        BingoHttpClient.postJson(ApiConfig.GAME_START_REPORT, json, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(EmptyBean data) {
                LogUtil.i("启动数据上报成功");
            }

            @Override
            public void onFailed(int code, String msg) {
                LogUtil.i("启动数据上报失败");
            }

            @Override
            public void onError(int code, String error) {
                LogUtil.i("启动数据上报错误");
            }
        });
    }

    public static void realName(final Context context, String cardNumber, String name, final RealNameCallBack callBack) {
        JSONObject json = new JSONObject();
        try {
            json.put("realName", name);
            json.put("idCard", cardNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BingoHttpClient.postJson(ApiConfig.REAL_NAME, json, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(EmptyBean data) {
                LogUtil.e("实名认证成功: ");
                if (callBack != null) {
                    callBack.onSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.showShortToast(context, "实名认证失败:" + msg);
                if (callBack != null) {
                    callBack.onFailed();
                }
            }

            @Override
            public void onError(int code, String error) {
                ToastUtil.showShortToast(context, "实名认证失败:" + error);
                if (callBack != null) {
                    callBack.onFailed();
                }
            }
        });

    }

    /**
     * 启动的时候通过请求ip获取大致地址(不走定位权限),然后保存到sp里面
     *
     * @param application
     */
    public static void getDeviceAddrByIP(final Application application) {
        String url = "http://whois.pconline.com.cn/ipJson.jsp";
        Map<String, String> map = new HashMap<>();
        BingoHttpClient.getString(url, map, new StringResponseCallBack() {
            @Override
            public void onSuccess(String result) {
                LogUtil.i("请求IP结果: " + result.trim());
                String province = CommonUtil.getProvince(result);
                String city = CommonUtil.getCity(result);
                LogUtil.i("解析省份: " + province + "\t解析城市: " + city);
                if (TextUtils.isEmpty(province) && TextUtils.isEmpty(city))
                    return;
                BGSPUtil.save(application, BGSPUtil.KEY_PROVINCE, province);
                BGSPUtil.save(application, BGSPUtil.KEY_CITY, city);
            }

            @Override
            public void onFailed(int code, String result) {
                LogUtil.e("失败:" + code + "\t" + result);
            }
        });
    }

    public static void autoLogin(final Activity context, String uid, final LoginCallBack callback) {
        Account account = BingoDBManager.getInstance().getByUid(context, uid);
        if (account == null || TextUtils.isEmpty(account.getP_key()) || TextUtils.isEmpty(account.getPassword())) {
            callback.onLogin(false, "自动登录失败", null);
            return;
        }
        final String password = AccountUtil.decodePwd(account.getP_key(), account.getPassword());
        JSONObject params = ApiUtil.getBaseParams(context);

        autoLoginProgressDialog = DialogUtil.getInstance().showProgressDialog(context, "自动登录中");
        try {
            params.put("userName", account.getUid());
            params.put("password", password);
            BingoHttpClient.postJson(ApiConfig.LOGIN_ACCOUNT, params, new ResponseCallBack<UserInfo>() {
                @Override
                public void onSuccess(final UserInfo data) {
                    LogUtil.e("自动登录成功 User: " + data);

                    if (autoLoginProgressDialog != null && autoLoginProgressDialog.isShowing()) {
                        autoLoginProgressDialog.dismiss();
                    }
                    callBackLoginSuccess(context, password, data, callback);

                }

                @Override
                public void onFailed(final int code, final String msg) {
                    LogUtil.e("自动登录失败: " + code + "\t" + msg);
                    if (autoLoginProgressDialog != null && autoLoginProgressDialog.isShowing()) {
                        autoLoginProgressDialog.dismiss();
                    }
                    GameReportHelper.onEventLogin("account", false);
                    GDTLogHelper.onLogin("account", false);
                    callbackFailed(msg, callback);

                }

                @Override
                public void onError(final int code, final String error) {
                    LogUtil.e("自动登录错误: " + code + "\t" + error);

                    if (autoLoginProgressDialog != null && autoLoginProgressDialog.isShowing()) {
                        autoLoginProgressDialog.dismiss();
                    }
                    GameReportHelper.onEventLogin("account", false);
                    GDTLogHelper.onLogin("account", false);
                    callbackFailed(error, callback);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void callbackFailed(String msg, LoginCallBack callback) {
        if (callback != null) {
            callback.onLogin(false, msg, null);
        }
    }

    private static void showRealNameDialog(final Activity context, BingoSDKCallBack coreCallback) {

        //未实名账号

        //根据游戏配置以及账号是否实名,确定是否弹出实名认证框
        GameConfig gameConfig = BingoSdkCore.getInstance().getGameConfig();

        if (gameConfig != null && gameConfig.getGame() != null) {
            Game game = gameConfig.getGame();
            int realNameType = game.getRealNameType();
            if (realNameType == 2) {
                //当天首次提示
                long lastShowTime;
                Object o = BGSPUtil.get(context, BGSPUtil.KEY_REALNAME_SHOW_TIME);
                if (o == null)
                    lastShowTime = 0;
                else
                    lastShowTime = Long.parseLong(o.toString());


                Date lastDate = new Date(lastShowTime);
                Date nowDate = new Date(System.currentTimeMillis());
                boolean sameDay = CommonUtil.isSameDay(lastDate, nowDate);
                if (!sameDay) {
                    //不同一天,显示弹框
                    //每次提示
                    DialogUtil.getInstance().showRealNameDialog(context, game.getForceRealName() == 1, new RealNameCallBack() {
                        @Override
                        public void onSuccess() {
                            //实名认证成功
                            realNameSuccess(context);
                        }

                        @Override
                        public void onFailed() {
                            //实名认证失败
                            realNameFailed(context);
                        }
                    });
                    //更新显示时间
                    BGSPUtil.save(context, BGSPUtil.KEY_REALNAME_SHOW_TIME, System.currentTimeMillis());
                }


            } else if (realNameType == 3) {
                //每次提示
                DialogUtil.getInstance().showRealNameDialog(context, game.getForceRealName() == 1, new RealNameCallBack() {
                    @Override
                    public void onSuccess() {
                        //实名认证成功
                        realNameSuccess(context);
                    }

                    @Override
                    public void onFailed() {
                        //实名认证失败
                        realNameFailed(context);
                    }
                });
            }

        }
    }

    private static void realNameFailed(Activity context) {

    }

    private static void realNameSuccess(Activity context) {

    }

    /**
     * @param context  context
     * @param password 密码:后台无法返回,需要从上面传过来
     * @param data     登录结果
     */
    private static void callBackLoginSuccess(Context context, String password, UserInfo data, LoginCallBack callback) {
//        ToastUtil.showShortToast(context, "登录成功");
        if (callback != null) {
            AccountUtil.saveAccount2db(context, data.getUserName(), password, data);
            callback.onLogin(true, null, data);
            GameReportHelper.onEventLogin("account", true);
            GDTLogHelper.onLogin("account", true);
        }
    }

    public static void bindPhone(Activity activity, String mobile, String code, final CommonCallback callback) {
        JSONObject json = ApiUtil.getBaseParams(activity);
        try {
            json.put("mobile", mobile);
            json.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BingoHttpClient.postJson(ApiConfig.BIND_PHONE, json, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(EmptyBean data) {
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_SUCCESS, null);
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_EXCEPTION, msg);
            }

            @Override
            public void onError(int code, String error) {
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_EXCEPTION, error);
            }
        });


    }

    public static void getCode(String mobile, final CommonCallback callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("mobile", mobile);
            json.put("phoneKey", "123");//todo 这里是临时模拟数据,后台也不知道有什么用

        } catch (JSONException e) {
            e.printStackTrace();
        }

        BingoHttpClient.postJson(ApiConfig.SEND_VERIFY_CODE, json, new ResponseCallBack<EmptyBean>() {
            @Override
            public void onSuccess(final EmptyBean content) {
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_SUCCESS, null);
            }

            @Override
            public void onFailed(int code, final String msg) {
                LogUtil.e("发送验证码失败: " + code + "\t" + msg);
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_EXCEPTION, msg);
            }

            @Override
            public void onError(int code, final String error) {
                LogUtil.e("请求异常: " + code + "\t" + error);
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CODE_EXCEPTION, error);
            }
        });
    }

    /**
     * 同步在线时间
     *
     * @param activity
     * @param period       间隔分钟数(多长时间发送一次,单位分钟)
     * @param coreCallback 实名回调
     */
    public static void syncPlayTime(final Activity activity, int period, final BingoSDKCallBack coreCallback) {
        //发送信息到后台
        LogUtil.e("同步信息,并且校验实名认证");

        JSONObject json = ApiUtil.getBaseParams(activity);
        try {
            json.put("timeInterval", period);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BingoHttpClient.postJson(ApiConfig.LINE_HEART, json, new ResponseCallBack<OnlineBean>() {
            @Override
            public void onSuccess(OnlineBean data) {
                LogUtil.e("同步在线时间成功: " + data);
                if (!BingoSdkCore.getInstance().showRealName()) {
                    showRealNameDialog(activity, coreCallback);
                }

                if (data.isShowBox()) {//todo  临时测试,正式需要去掉!
                    //超过限制时间 showBox变成了false?

                    DialogUtil.getInstance().showOnlineLimitDialog(activity, data, new RealNameCallBack() {
                        @Override
                        public void onSuccess() {
                            if (coreCallback != null) {
                                coreCallback.onRealNameFinished(ApiStatusCode.CODE_SUCCESS);
                            }
                        }

                        @Override
                        public void onFailed() {
                            if (coreCallback != null) {
                                coreCallback.onRealNameFinished(ApiStatusCode.CODE_FAILED);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(int code, String msg) {

                LogUtil.e("同步在线时间失败: " + msg);
            }

            @Override
            public void onError(int code, String error) {

                LogUtil.e("同步在线时间错误: " + error);
            }
        });
    }
}
