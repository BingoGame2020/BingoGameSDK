package com.bingo.sdk;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bingo.sdk.bean.InitOptions;
import com.bingo.sdk.bean.RechargeOptions;
import com.bingo.sdk.bean.RoleEventOptions;
import com.bingo.sdk.bean.RoleEventType;
import com.bingo.sdk.callback.BingoExitCallBack;
import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.callback.PermissionCallback;
import com.bingo.sdk.impl.BaseImplHelper;
import com.bingo.sdk.impl.BaseInterface;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.GameConfig;
import com.bingo.sdk.inner.bean.UpdateBean;
import com.bingo.sdk.inner.channel.ChannelConfig;
import com.bingo.sdk.inner.log.GDTLogHelper;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.BGSPUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.DialogUtil;
import com.bingo.sdk.inner.util.IdentifierHelper;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.MetaUtil;
import com.bingo.sdk.inner.util.ParameterChecker;
import com.bingo.sdk.inner.util.PermissionUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.ApiStatusCode;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.HttpUtil;
import com.bingo.sdk.web.ResponseCallBack;
import com.bingo.sdk.worker.BingoScheduledWorkerHelper;
import com.bytedance.applog.AppLog;
import com.bytedance.applog.GameReportHelper;
import com.bytedance.applog.InitConfig;
import com.qq.gdt.action.GDTAction;

import org.json.JSONObject;

import java.util.Arrays;

public class BingoSdkCore {

    public static final int REQUEST_PERMISSION = 1202;
    private static volatile BingoSdkCore instance;
    private BaseInterface impl;
    private GameConfig gameConfig;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isInit = false;
    private BingoSDKCallBack coreCallback;
    private String[] permissions;
    private PermissionCallback permissionCallback;
    private boolean reChecking;
    private boolean showRealName = true;


    private BingoSdkCore() {
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public static BingoSdkCore getInstance() {
        if (instance == null) {
            synchronized (BingoSdkCore.class) {
                if (instance == null) {
                    instance = new BingoSdkCore();
                }
            }
        }
        return instance;
    }

    public void initApplication(final Application application) {
        int channelId = MetaUtil.getInteger(application.getApplicationContext(), ChannelConfig.CHANNEL_ID);
        //根据不同渠道ID获取不同渠道的实现类
        impl = BaseImplHelper.initImpl(channelId);
        impl.onApplicationCreate(application);

        getAndSaveOaid(application);
        initThirdSdkInApplication(application);

        //请求一个公共域名,通过ip获取大概地址
        HttpUtil.getDeviceAddrByIP(application);
    }


    private void initThirdSdkInApplication(Application application) {
        String appId = MetaUtil.getString(application, ChannelConfig.GDT_APP_ID);
        String appKey = MetaUtil.getString(application, ChannelConfig.GDT_APP_KEY);
        String appChannel = MetaUtil.getString(application, ChannelConfig.GDT_APP_CHANNEL);
        LogUtil.i("广点通: " + appId + "\tappKey: " + appKey + "\tchannel: " + appChannel);
        if (TextUtils.isEmpty(appChannel))
            GDTAction.init(application, appId, appKey);
        else
            GDTAction.init(application, appId, appKey, appChannel);

        GDTLogHelper.appStart();

    }

    private void getAndSaveOaid(final Application application) {


        final long start = System.currentTimeMillis();
        LogUtil.e("开始获取OAID:" + start);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new IdentifierHelper(new IdentifierHelper.AppIdsUpdater() {
                    @Override
                    public void onIdValid(String id) {
                        LogUtil.e("获取到OAID:" + (System.currentTimeMillis() - start) + "\t" + id);
                        BGSPUtil.save(application, BGSPUtil.KEY_OAID, id);
                    }
                }).getOAID(application);
            }
        }).start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //延迟上报,尽量等待oaid获取
                HttpUtil.reportStartData(application);

            }
        }, 2000);

    }


    public void initSdk(Activity activity, InitOptions opts, final BingoSDKCallBack callBack) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        if (callBack == null) throw new NullPointerException("callback can not be null");
        this.coreCallback = callBack;
        //获取清单文件配置的渠道ID,这里的渠道id和后台定义的保持一致,用来确认使用哪个渠道的资源
        impl.init(activity, opts, callBack);
        loadGameConfig(activity);
        initThirdSdk(activity);
    }

    private void checkUpdate(final Activity activity) {
        JSONObject json = ApiUtil.getUpdateParams(activity);
        LogUtil.e("检测更新: " + json);
        BingoHttpClient.postJson(ApiConfig.APP_UPDATE, json, new ResponseCallBack<UpdateBean>() {
            @Override
            public void onSuccess(UpdateBean data) {
                LogUtil.e("检查更新成功: " + data);
                if (!activity.isFinishing() && data.isValid()) {
                    DialogUtil.getInstance().showDownloadDialog(activity, data);
                }
            }

            @Override
            public void onFailed(int code, String msg) {

                LogUtil.e("检查更新失败: " + msg);
            }

            @Override
            public void onError(int code, String error) {

                LogUtil.e("检查更新错误: " + error);
            }
        });
    }

    /**
     * 初始化第三方SDK
     *
     * @param activity
     */
    private void initThirdSdk(Activity activity) {
        String appid = MetaUtil.getString(activity, ChannelConfig.RANGERS_APP_ID);
        String channel = MetaUtil.getString(activity, ChannelConfig.RANGERS_APP_CHANNEL);
        InitConfig config = new InitConfig(appid, channel);
        config.setEnablePlay(true);
        AppLog.init(activity, config);


    }


    private void loadGameConfig(final Activity activity) {
        JSONObject json = ApiUtil.getBaseParams(activity);
        LogUtil.e("基本参数: " + json);
        BingoHttpClient.postJson(ApiConfig.GET_GAME_CONFIG, json, new ResponseCallBack<GameConfig>() {
            @Override
            public void onSuccess(GameConfig data) {
                isInit = true;
                LogUtil.e("获取游戏配置成功: " + data);
                gameConfig = data;
                if (gameConfig.getGame().getCutLoginStatus() == 1) {
                    autoLogin(activity);
                }
                coreCallback.onInitFinished(ApiStatusCode.CODE_SUCCESS, "初始化成功");
            }

            @Override
            public void onFailed(int code, String msg) {
                isInit = false;
                coreCallback.onInitFinished(ApiStatusCode.CODE_FAILED, msg);
                gameConfig = null;
                LogUtil.e("获取游戏配置失败: " + msg);
            }

            @Override
            public void onError(int code, String error) {
                isInit = false;
                coreCallback.onInitFinished(ApiStatusCode.CODE_FAILED, error);
                gameConfig = null;
                LogUtil.e("获取游戏配置错误: " + error);
            }
        });

    }

    private void autoLogin(final Activity activity) {
        impl.autoLogin(activity);
    }

    public void pay(Activity activity, RechargeOptions opts) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        ParameterChecker.checkCallBack(coreCallback);
        if (!isInit) {
            coreCallback.onPayFinished(ApiStatusCode.CODE_EXCEPTION, "sdk has not been initialized");
            return;
        }
        if (opts == null) {
            coreCallback.onPayFinished(ApiStatusCode.CODE_EXCEPTION, "RechargeOptions can not be null");
            return;
        }
        if (!opts.isDataValid()) {
            coreCallback.onPayFinished(ApiStatusCode.CODE_EXCEPTION, "Recharge params invalid");
            return;
        }
        if (!isLogin()) {
            coreCallback.onPayFinished(ApiStatusCode.CODE_FAILED, "账号未登录");
            return;
        }
        impl.pay(activity, opts);
    }

    public void login(Activity activity) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        ParameterChecker.checkCallBack(coreCallback);
        if (!isInit) {
            coreCallback.onLoginFinished(ApiStatusCode.CODE_EXCEPTION, "sdk has not been initialized");
            return;
        }
        impl.login(activity);
    }

    public void logout(Activity activity) {
        ParameterChecker.checkActivity(activity);
        impl.logout(activity);
    }

    public void onCreate(Activity activity) {

        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        impl.onCreate(activity);
    }

    public void onResume(Activity activity) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        impl.onResume(activity);
    }

    public void onStart(Activity activity) {
        ParameterChecker.checkImpl(impl);
        impl.onStart(activity);
    }

    public void onRestart(Activity activity) {
        ParameterChecker.checkImpl(impl);
        impl.onRestart(activity);
    }

    public void onNewIntent(Activity activity) {
        ParameterChecker.checkImpl(impl);
        impl.onNewIntent(activity);
    }

    public void onPause(Activity activity) {
        ParameterChecker.checkActivity(activity);
        impl.onPause(activity);

    }

    public void onStop(Activity activity) {
        ParameterChecker.checkImpl(impl);
        impl.onStop(activity);
    }

    public void onDestroy(Activity activity) {
        ParameterChecker.checkImpl(impl);
        impl.onDestroy(activity);
    }

    public void onRoleEvent(Activity activity, @RoleEventType int type, RoleEventOptions options) {

        ParameterChecker.checkImpl(impl);
        impl.onRoleEvent(activity, type, options);
        thirdPartyLog(activity, type, options);
    }

    /**
     * 第三方渠道事件统计
     *
     * @param activity
     * @param type
     * @param options
     */
    private void thirdPartyLog(Activity activity, @RoleEventType int type, RoleEventOptions options) {
        switch (type) {

            case RoleEventType.ROLE_CREATE:
                GameReportHelper.onEventCreateGameRole(CommonUtil.filterNull(options.getRoleId()));
                GDTLogHelper.onCreateRole(CommonUtil.filterNull(options.getRoleName()));
                break;
            case RoleEventType.ROLE_LOGIN:

                break;
            case RoleEventType.ROLE_UPGRADE:
                GameReportHelper.onEventUpdateLevel(options.getRoleLevel());
                GDTLogHelper.onUpdateRole(options.getRoleLevel());
                break;
        }
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

        ParameterChecker.checkImpl(impl);
        impl.onActivityResult(activity, requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        ParameterChecker.checkImpl(impl);
        impl.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionResult(activity, requestCode, permissions, grantResults);
    }

    /**
     * 退出游戏
     * 建议在游戏主界面的backPressed()方法调用,这里除了弹出退出确认框,还会处理点击
     * 悬浮窗弹出的账号框返回事件
     *
     * @param activity activity,不能为空
     * @param callBack 退出回调
     */
    public void exit(Activity activity, BingoExitCallBack callBack) {
        ParameterChecker.checkActivity(activity);
        impl.onExit(activity, callBack);
    }


    /**
     * 对于Android版本6.0以上需要动态申请权限<p>
     * 调用了该方法必须在activity的onRequestPermissionsResult()中调用SDK的onRequestPermissionsResult()
     *
     * @param context     context
     * @param permissions 需要申请的权限列表,注意:这些权限必须在AndroidManifest.xml中有配置,否则获取不到权限
     * @param callback    回调
     */
    public void checkPermissions(Activity context, String[] permissions, PermissionCallback callback) {
        if (context == null) {
            throw new IllegalArgumentException("context不能为空");
        }
        checkPermissions(context, permissions, true, callback);
    }

    /**
     * 对于Android版本6.0及以上需要动态申请权限<p>
     * 6.0以下只要在AndroidManifest.xml中配置就可以获取
     *
     * @param context     context
     * @param permissions 需要申请的权限列表,注意:这些权限必须在AndroidManifest.xml中有配置,否则获取不到权限
     * @param reChecking  是否重新申请,直到用户给予权限为止.true:是;  false:否
     * @param callback    回调
     */
    public void checkPermissions(final Activity context, final String[] permissions, boolean reChecking, PermissionCallback callback) {
        this.permissions = permissions;
        this.permissionCallback = callback;
        this.reChecking = reChecking;
//        LogUtil.e(TAG, "checkPermissions: " + isChecking);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0以上才进行权限检测
            doCheckPermissions(context);
        } else {
            callback.onPermissionGranted();
        }
    }


    private void doCheckPermissions(Activity context) {

        LogUtil.i("doCheckPermissions: " + Arrays.toString(permissions));
        PermissionUtil.setPermissionCallback(permissionCallback);
        boolean hasPermission = PermissionUtil.checkPermissions(context, REQUEST_PERMISSION, true, reChecking, permissions);
        LogUtil.i("doCheckPermissions hasPermission: " + hasPermission);


    }

    public void changeAccount(Activity activity) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkCallBack(coreCallback);
        ParameterChecker.checkImpl(impl);
        if (!isInit) {
            ToastUtil.showShortToast(activity, "SDK尚未初始化");
            return;
        }
        impl.changeAccount(activity);
    }

    /**
     * 资源加载完成,进入游戏主界面
     *
     * @param activity
     */
    public void onGameLoadFinished(Activity activity) {
        ParameterChecker.checkActivity(activity);
        ParameterChecker.checkImpl(impl);
        ParameterChecker.checkCallBack(coreCallback);
        checkUpdate(activity);
        checkBind(activity);
        BingoScheduledWorkerHelper.getInstance().startRoleOnlineWorker(activity, coreCallback);
    }

    private void checkBind(Activity activity) {
        Account fromDb = AccountUtil.getCurrentLoginAccountFromDb(activity);
        if (fromDb != null && !fromDb.isBindPhone()) {
            DialogUtil.getInstance().showBindMobileDialog(activity);
        }
    }

    public boolean isLogin() {
        return impl.isLogin();
    }


    /**
     * 退出游戏,释放后台资源
     */
    public void release() {
        BingoScheduledWorkerHelper.getInstance().cancelOnlineWorker();
    }

    /**
     * 是否需要显示实名框,用来控制单次游戏是否显示,游戏重启后为true, 显示一次之后设置成false
     *
     * @return
     */
    public boolean showRealName() {
        return showRealName;
    }

    /**
     * 更改临时变量,只在显示实名弹框之后才能调用该方法
     *
     * @param showRealName
     */
    public void setShowRealName(boolean showRealName) {
        this.showRealName = showRealName;
    }
}
