package com.bingo.sdk.inner.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.callback.BingoExitCallBack;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.OnlineBean;
import com.bingo.sdk.inner.bean.UpdateBean;
import com.bingo.sdk.inner.consts.CommonCallBackCode;
import com.bingo.sdk.inner.interf.CommonCallback;
import com.bingo.sdk.inner.interf.RealNameCallBack;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.web.BingoDownloadManager;
import com.bingo.sdk.web.HttpUtil;
import com.bingo.sdk.worker.BingoScheduledWorkerHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DialogUtil {
    public static final long WELCOME_DELAY_TIME = 3000;
    private static DialogUtil instance;
    private static boolean isSwitchAccount = false;//欢迎登录界面是否执行的切换账号
    private Handler handler = new Handler(Looper.getMainLooper());
    private AlertDialog limitDialog;

    public static DialogUtil getInstance() {
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }


    public void showTopEnterDialog(Activity activity, String message, final CommonCallback callback) {
        isSwitchAccount = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, ResourceManager.getStyle(activity, "BingoTheme_Dialog_Transparent"));
        View view = LayoutInflater.from(activity).inflate(ResourceManager.getLayout(activity, "layout_bingo_top_enter"), null);
        TextView tv_content = view.findViewById(ResourceManager.getId(activity, "tv_account"));
        Button btn_change = view.findViewById(ResourceManager.getId(activity, "btn_change"));
        tv_content.setText(message);

        builder.setView(view);
        final AlertDialog dialog = builder.show();

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isSwitchAccount = true;
                if (callback != null)
                    callback.onCallBack(CommonCallBackCode.CHANGE_ACCOUNT, null);
            }
        });


//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                LogUtil.e("dismiss");
//                if (callback != null) {
////                    if (!isSwitchAccount)//如果是切换账号就不执行后续???
//                    callback.onCallBack(CommonCallBackCode.WELCOME_DIALOG_DISMISS, null);
//                }
//            }
//        });

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.CENTER_VERTICAL;
        window.setAttributes(attributes);
        window.setGravity(Gravity.TOP);
        window.setWindowAnimations(ResourceManager.getStyle(activity, "dialog_top_enter_anim"));
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, WELCOME_DELAY_TIME);
    }

    public void showNoticeDialog(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", null);
        builder.setMessage(message);
        final AlertDialog dialog = builder.show();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);

    }

    public void showRealNameDialog(final Activity activity, final boolean forceRealName, final RealNameCallBack callBack) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, ResourceManager.getLayout(activity, "layout_bingo_real_name"), null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();

        BingoSdkCore.getInstance().setShowRealName(false);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(!forceRealName);

        relayoutDialog(activity, dialog);

        final AppCompatEditText et_name = view.findViewById(ResourceManager.getId(activity, "et_real_name"));
        final AppCompatEditText et_cardNumber = view.findViewById(ResourceManager.getId(activity, "et_real_name_id"));
        ImageView iv_close = view.findViewById(ResourceManager.getId(activity, "iv_close"));
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!forceRealName) {
                    dialog.dismiss();
                    if (callBack != null)
                        callBack.onFailed();
                }
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!forceRealName) {
                        dialog.dismiss();
                        callBack.onFailed();
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });

        view.findViewById(ResourceManager.getId(activity, "btn_real_name")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String cardNumber = et_cardNumber.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    ToastUtil.showShortToast(activity, "请输入真实姓名");
                    return;
                } else if (TextUtils.isEmpty(cardNumber)) {
                    ToastUtil.showShortToast(activity, "请输入身份证号码");
                    return;
                } else if (!CommonUtil.isCardNumberValid(cardNumber)) {
                    ToastUtil.showShortToast(activity, "身份证号码不合规");
                    return;
                }

                HttpUtil.realName(activity, cardNumber, name, callBack);
            }
        });

    }

    private void relayoutDialog(Context context, AlertDialog dialog) {
        int height;
        int width;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (CommonUtil.isLandScape(context)) {
            height = WindowManager.LayoutParams.WRAP_CONTENT;
            width = (int) (metrics.widthPixels * 0.5);
        } else {
            width = (int) (metrics.widthPixels * 0.9);
            height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = width;
            attributes.height = height;
            window.setAttributes(attributes);
        }
    }

    public void showBindMobileDialog(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, ResourceManager.getLayout(activity, "layout_bingo_bind_phone"), null);
        final Button btn_bind = view.findViewById(ResourceManager.getId(activity, "btn_bind_phone"));
        final Button btn_getCode = view.findViewById(ResourceManager.getId(activity, "btn_get_code"));
        ImageView iv_close = view.findViewById(ResourceManager.getId(activity, "iv_close"));
        final AppCompatEditText et_phone = view.findViewById(ResourceManager.getId(activity, "et_bind_phone_number"));
        final AppCompatEditText et_code = view.findViewById(ResourceManager.getId(activity, "et_bingo_code"));

        builder.setView(view);
        final AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);

        relayoutDialog(activity, dialog);

        view.findViewById(ResourceManager.getId(activity, "iv_close")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = et_phone.getText().toString();
                if (!CommonUtil.isMobileNumber(mobile)) {
                    ToastUtil.showShortToast(activity, "手机号码不正确");
                    return;
                }
                btn_getCode.setEnabled(false);
                btn_getCode.setText("发送中");
                HttpUtil.getCode(mobile, new CommonCallback() {
                    @Override
                    public void onCallBack(int code, String msg) {
                        if (!activity.isFinishing()) {
                            if (code == CommonCallBackCode.CODE_SUCCESS) {
                                ToastUtil.showShortToast(activity, "短信发送成功");
                                btn_getCode.setText("发送成功");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        btn_getCode.setText("重新发送");
                                        btn_getCode.setEnabled(true);
                                    }
                                }, 30 * 1000);
                            } else {
                                ToastUtil.showShortToast(activity, "短信发送失败: " + msg);
                                btn_getCode.setEnabled(true);
                                btn_getCode.setText("重新发送");
                            }
                        }
                    }
                });
            }
        });

        btn_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mobile = et_phone.getText().toString().trim();
                String code = et_code.getText().toString().trim();
                if (!CommonUtil.isMobileNumber(mobile)) {
                    ToastUtil.showShortToast(activity, "手机号码不正确");
                    return;
                }
                if (TextUtils.isEmpty(code)) {
                    ToastUtil.showShortToast(activity, "请输入验证码");
                    return;
                }
                HttpUtil.bindPhone(activity, mobile, code, new CommonCallback() {
                    @Override
                    public void onCallBack(int code, String msg) {
                        if (code == CommonCallBackCode.CODE_SUCCESS) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        } else {
                            if (!activity.isFinishing()) {
                                ToastUtil.showShortToast(activity, "绑定手机失败:" + msg);
                            }
                        }

                    }
                });

            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

    }

    public void showExitDialog(Activity activity, final BingoExitCallBack callBack) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, ResourceManager.getLayout(activity, "layout_bingo_exit_game"), null);


        builder.setView(view);
        final AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);
        view.findViewById(ResourceManager.getId(activity, "btn_exit_confirm")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (callBack != null) {
                    callBack.onExit(true);
                }
            }
        });
        view.findViewById(ResourceManager.getId(activity, "btn_exit_cancel")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (callBack != null) {
                    callBack.onExit(false);
                }
            }
        });

    }

    public ProgressDialog showProgressDialog(Activity activity, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
        return progressDialog;
    }

    public void showDownloadDialog(final Activity activity, final UpdateBean data) {
        boolean forceUpdate = data.isForceUpdate();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, ResourceManager.getLayout(activity, "layout_bingo_dialog_update"), null);

        ImageView iv_close = view.findViewById(ResourceManager.getId(activity, "iv_close"));
        Button btn_ignore = view.findViewById(ResourceManager.getId(activity, "btn_ignore_update"));
        Button btn_update = view.findViewById(ResourceManager.getId(activity, "btn_update"));
        TextView tv_version = view.findViewById(ResourceManager.getId(activity, "tv_tip_update_version"));
        TextView tv_desc = view.findViewById(ResourceManager.getId(activity, "tv_tip_update_info"));

        if (data.isForceUpdate()) {
            //强更
            iv_close.setVisibility(View.INVISIBLE);
            btn_ignore.setVisibility(View.GONE);
        } else {
            iv_close.setVisibility(View.VISIBLE);
            btn_ignore.setVisibility(View.VISIBLE);
        }
        String version = String.format("最新版本: %s", data.getVersionName());
        String desc = String.format("更新内容: %s", data.getVersionDesc());
        tv_version.setText(version);
        tv_desc.setText(desc);

        builder.setView(view);
        final AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(!forceUpdate);

        int height;
        int width;
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        if (CommonUtil.isLandScape(activity)) {
            height = WindowManager.LayoutParams.WRAP_CONTENT;
            width = (int) (metrics.widthPixels * 0.5);
        } else {
            width = (int) (metrics.widthPixels * 0.9);
            height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = width;
            attributes.height = height;
            window.setAttributes(attributes);
        }


        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return !data.isForceUpdate();
                }
                return false;
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        btn_ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BingoDownloadManager.getInstance().download(activity, data.getDownloadUrl());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

    }

    public void showOnlineLimitDialog(final Activity activity, final OnlineBean data, final RealNameCallBack callBack) {
        Account account = AccountUtil.getCurrentLoginAccountFromDb(activity);
        if (account == null) {
            LogUtil.e("数据库账号为空");
            return;
        }
        if (account.isRealName()) {
            LogUtil.e("账号已实名");
            return;
        }
        if (limitDialog != null && limitDialog.isShowing())
            limitDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(ResourceManager.getLayout(activity, "layout_bingo_online_limit"), null);
        builder.setView(view);
        limitDialog = builder.show();
        limitDialog.setCanceledOnTouchOutside(false);

        TextView tv_tip = view.findViewById(ResourceManager.getId(activity, "tv_tip"));
        String onlineMins = new BigDecimal(data.getOnlineTime()).divide(new BigDecimal(60000), RoundingMode.HALF_UP).toPlainString();
        String leftMins = new BigDecimal(data.getOverTime()).divide(new BigDecimal(60000), RoundingMode.HALF_UP).toPlainString();
        String limitHours = new BigDecimal(data.getLimitTime()).divide(new BigDecimal("60.0"), 1, RoundingMode.HALF_UP).toPlainString();
        tv_tip.setText(String.format(activity.getResources().getString(ResourceManager.getString(activity, "bingo_tip_unrealname_limit")), limitHours, onlineMins, leftMins));
        ConstraintLayout layout_realName = view.findViewById(ResourceManager.getId(activity, "layout_realName"));
        Button btn_exit = view.findViewById(ResourceManager.getId(activity, "btn_exit"));
        Button btn_cancel = view.findViewById(ResourceManager.getId(activity, "btn_cancel"));
        Button btn_realName = view.findViewById(ResourceManager.getId(activity, "btn_realName"));
        if (!data.isContinueGame()) {
            //不能继续游戏
//            layout_realName.setVisibility(View.GONE);
//            btn_exit.setVisibility(View.VISIBLE);
            BingoScheduledWorkerHelper.getInstance().cancelOnlineWorker();
            btn_cancel.setVisibility(View.GONE);
            //停止在线累积
            if (limitDialog != null) {
                limitDialog.setCancelable(false);
            }

        } else {
//            layout_realName.setVisibility(View.VISIBLE);
//            btn_exit.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.VISIBLE);
            if (limitDialog != null) {
                limitDialog.setCancelable(true);
            }
        }

        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == ResourceManager.getId(activity, "btn_exit")) {
//                    BingoSdkCore.getInstance().release();
//                    Process.killProcess(Process.myPid());
                } else if (id == ResourceManager.getId(activity, "btn_cancel")) {
                    if (limitDialog != null && limitDialog.isShowing())
                        limitDialog.dismiss();
                } else if (id == ResourceManager.getId(activity, "btn_realName")) {
                    if (data.isContinueGame()) {
                        if (limitDialog != null && limitDialog.isShowing())
                            limitDialog.dismiss();
                        showRealNameDialog(activity, false, callBack);
                    } else {
                        showRealNameDialog(activity, true, callBack);
                    }
                }
            }
        };
        btn_exit.setOnClickListener(l);
        btn_cancel.setOnClickListener(l);
        btn_realName.setOnClickListener(l);

    }
}
