package com.bingo.sdk.inner.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.callback.PermissionCallback;
import com.bingo.sdk.utils.ResourceManager;

import java.util.Arrays;


/**
 * Created by OuYanglz on 2018/6/20.
 * 动态权限检查
 */

public class PermissionUtil {
    //    private static AlertDialog.Builder builder;
    private static AlertDialog permissionDialog;
    private static boolean isChecking = false;//判断当前是否有申请权限流程没走完(在上一个申请流程没走完时,不能开启下一个请求)
    private static boolean showRationale;
    private static boolean reChecking;

    /**
     * 判断权限集合是否有权限没有赋予
     *
     * @param permissions 权限
     * @return true->有权限未授予
     */
    private static boolean isAllPermissionGranted(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否缺少权限
     *
     * @param permission permission
     * @return true:缺少;false:已申请权限
     */
    private static boolean isPermissionGranted(Context context, String permission) {
        return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
    }

    /**
     * 请求权限兼容低版本
     */
    private static void requestPermissions(Activity context, int code, String... permissions) {
//        ActivityCompat.requestPermissions(context, permissions, code);
        ReflectUtils.callMethod(context, "requestPermissions", new Class<?>[]{String[].class, int.class},
                new Object[]{permissions, code});//使用反射机制调用(国内部分手机使用ActivityCompat无法弹出申请框)
        LogUtil.i("requestPermissions");
    }

    /**
     * 显示缺失权限提示
     */
    private static void showMissingPermissionDialog(final Activity context, final int code, final String[] permissions) {
        if (permissions != null)
            LogUtil.i("显示弹框提示缺少的权限: " + Arrays.toString(permissions));
        permissionDialog = new AlertDialog.Builder(context, ResourceManager.getAndroidResId(context, "style", "Theme_DeviceDefault_Light_Dialog")).create();
        permissionDialog.setTitle(ResourceManager.getString(context, "bingo_common_dialog_tips"));
        permissionDialog.setMessage(context.getString(ResourceManager.getString(context, "bingo_permission_request_tip")));

        permissionDialog.setButton(Dialog.BUTTON_NEGATIVE, context.getString(ResourceManager.getString(context, "bingo_common_dialog_cancel")), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (permissionDialog != null)
                    permissionDialog.dismiss();
                if (mCallback != null)
                    mCallback.onPermissionDenied();
            }
        });

        permissionDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(ResourceManager.getString(context, "bingo_common_dialog_setting")), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (permissionDialog != null) {
                    permissionDialog.cancel();
//                    permissionDialog.dismiss();
                    permissionDialog = null;
                }
                startAppSettings(context, code);
//                requestPermissions(context, code, permissions);
            }
        });

        permissionDialog.setCanceledOnTouchOutside(false);
        permissionDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                LogUtil.i("Dialog onKey: " + keyCode + "\tevent: " + event.getAction());
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (permissionDialog != null)
                        permissionDialog.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null)
                                mCallback.onPermissionDenied();
                        }
                    }, 200);
                    return true;
                }
                return false;
            }
        });


        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            display.getMetrics(metrics);
        }
        int width;
        int height;
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = (int) (metrics.widthPixels * 0.8);
        } else {
            height = (int) (metrics.heightPixels * 0.7);
            width = (int) (height * 1.3);
        }

        try {
            permissionDialog.getWindow().setLayout(width, -2);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (permissionDialog != null)
            permissionDialog.show();

    }

    /**
     * 启动应用的设置
     */
    private static void startAppSettings(Activity context, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//如果添加了这个,那么启动界面就会马上回调到onActivityResult
        context.startActivityForResult(intent, requestCode);
    }

//    /**
//     * 用户拒绝权限,但是没有勾选不再提示
//     *
//     * @param permissions
//     * @return
//     */
//    public static List<String> hasDelayAllPermissions(Activity context, String... permissions) {
//        List<String> list = new ArrayList<>();
//        for (String permission : permissions) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
//                    && PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
//                list.add(permission);
//            }
//        }
//        return list;
//    }

    /**
     * Android 6.0及以上权限检查<br/>
     *
     * @param context        context
     * @param code           请求码
     * @param mReChecking    用户拒绝权限时是否重新申请,直到赋予为止; true->是;     false->否
     * @param permissions    申请的权限集合(必须要在AndroidManifest.xml中有配置)
     * @param mShowRationale 用户拒绝权限且勾选不再提示时,是否显示弹框(主要用于说明为何需要权限)
     */
    public static boolean checkPermissions(Activity context, int code, boolean mShowRationale, boolean mReChecking, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        showRationale = mShowRationale;
        reChecking = mReChecking;
        if (!isAllPermissionGranted(context, permissions)) {
            if (!isChecking) {
                LogUtil.i("checkPermissions: " + code + "\tshowRationale: " + mShowRationale + "\tpermissions: " + Arrays.toString(permissions));
                requestPermissions(context, code, permissions);
                isChecking = true;
            }
            return false;
        } else {
            if (permissionDialog != null)
                permissionDialog.cancel();
            permissionDialog = null;
            if (mCallback != null)
                mCallback.onPermissionGranted();
            return true;
        }
    }

    private static PermissionCallback mCallback;

    public static void setPermissionCallback(PermissionCallback callback) {
        mCallback = callback;
    }

    public static void onRequestPermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        isChecking = false;
        LogUtil.i("onRequestPermissionsResult: " + requestCode + "\t->permissions: " + Arrays.toString(permissions) + "\t->result: " + Arrays.toString(grantResults));

        if (permissions == null || grantResults == null) {
            if (mCallback != null)
                mCallback.onPermissionDenied();
            return;
        }
        if (requestCode == BingoSdkCore.REQUEST_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                //第一次拒绝权限会返回true,如果拒绝并勾选了不再提醒,则返回false
                boolean shouldTip = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    LogUtil.i("onRequestPermissionResult:" + reChecking);
                    if (reChecking) {//重复请求
                        if (shouldTip) {//用户没有彻底拒绝权限
                            //拒绝权限,需要弹窗解释为何需要该权限
                            requestPermissions(activity, requestCode, permissions);
                        } else {
                            //拒绝权限,并勾选了不再提醒
                            if (showRationale)
                                showMissingPermissionDialog(activity, requestCode, permissions);
                        }
                    } else {
                        if (mCallback != null)
                            mCallback.onPermissionDenied();
                    }
                    return;
                }
            }
            if (mCallback != null)
                mCallback.onPermissionGranted();
        }
    }
}
