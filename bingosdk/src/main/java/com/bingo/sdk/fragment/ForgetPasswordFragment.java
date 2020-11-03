package com.bingo.sdk.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.bean.CheckMobileExist;
import com.bingo.sdk.inner.bean.EmptyBean;
import com.bingo.sdk.inner.util.BGSPUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.DialogUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.ResponseCallBack;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ForgetPasswordFragment extends BaseFragment implements View.OnClickListener {


    private CountDownTimer timer;
    //倒计时时间,单位(秒)
    private int COUNT_DOWN_SECONDS = 60;
    private int leftTime = COUNT_DOWN_SECONDS;
    /**
     * 记录验证码是否通过验证,如果是验证通过了,那么fragment销毁的时候则不应该记录
     * 倒计时
     */
    private boolean isCodeValid = false;
    private Button btn_code, btn_reset;
    private AppCompatEditText et_mobile, et_password, et_code;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView iv_show_pwd;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.e("ForgetPasswordFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LogUtil.e("ForgetPasswordFragment onCreateView");
        return inflater.inflate(ResourceManager.getLayout(getContext(), "fragment_bingo_forget_password"), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogUtil.e("ForgetPasswordFragment onViewCreated");
        initView(view);
        countDown();

    }


    private void initView(View view) {
//        CommonUtil.autoHideKeyboard(getActivity(), view.findViewById(ResourceManager.getId(getContext(), "layout_main));

        view.findViewById(ResourceManager.getId(getContext(), "iv_close")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_back")).setOnClickListener(this);
        btn_code = view.findViewById(ResourceManager.getId(getContext(), "btn_get_code"));
        btn_reset = view.findViewById(ResourceManager.getId(getContext(), "btn_set"));
        et_mobile = view.findViewById(ResourceManager.getId(getContext(), "et_bingo_phone_number"));
        et_password = view.findViewById(ResourceManager.getId(getContext(), "et_bingo_password"));
        et_code = view.findViewById(ResourceManager.getId(getContext(), "et_bingo_code"));
        iv_show_pwd = view.findViewById(ResourceManager.getId(getContext(), "iv_show_pwd"));

        btn_code.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

    }

    private void countDown() {
        //因为下面是加了1秒的,所以有可能出现最开始的出多1的情况
        //比如正常从10000 也就是10秒,后面显示的时候加1,成了11秒; 减去50毫秒则可避免这个问题

        Object o = BGSPUtil.get(getActivity(), BGSPUtil.KEY_VERIFY_CODE_EXIT_TIME);
        long exitTime = 0;
        if (o != null) {
            exitTime = Long.parseLong(o.toString());
        }

        Object leftTimeObj = BGSPUtil.get(getActivity(), BGSPUtil.KEY_VERIFY_CODE_LEFT_TIME);
        if (leftTimeObj != null) {
            leftTime = Integer.parseInt(leftTimeObj.toString());
        }
        long current = System.currentTimeMillis();
        int exitInterval = (int) ((current - exitTime) / 1000);//退出的时间
        if (exitInterval < leftTime) {
            //退出的时间小于倒计时剩余时间
            //计算还需要继续倒计时的秒数,例如 上次倒计时还有45秒退出界面,出去了30秒,那么需要从15开始倒计时
            COUNT_DOWN_SECONDS = leftTime - exitInterval;
            LogUtil.i("剩余倒计时: " + COUNT_DOWN_SECONDS);
            startCountDown();
        }
    }

    private void startCountDown() {
        timer = new CountDownTimer(COUNT_DOWN_SECONDS * 1000 - 50, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //这里加1 是为了避免出现 0秒 的问题
                int sec = (int) ((millisUntilFinished / 1000) + 1);
                leftTime = sec;
                String secString = sec + "秒";
//                LogUtil.i("onTick: " + millisUntilFinished + "\t" + sec);
                btn_code.setText(secString);
                btn_code.setEnabled(false);

            }

            @Override
            public void onFinish() {
//                LogUtil.i("onFinish");
                btn_code.setEnabled(true);
                btn_code.setText("重新发送");
            }
        };
        timer.start();
    }

    private void requestSendCode(String mobile) {
        btn_code.setEnabled(false);

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
                LogUtil.e("发送验证码成功: " + content);
                dismissProgressDialog();
                btn_code.setText("重新发送");
                startCountDown();

            }

            @Override
            public void onFailed(int code, final String msg) {
                LogUtil.e("发送验证码失败: " + code + "\t" + msg);
                dismissProgressDialog();
                btn_code.setEnabled(true);
                btn_code.setText("重新发送");

            }

            @Override
            public void onError(int code, final String error) {
                LogUtil.e("请求异常: " + code + "\t" + error);
                dismissProgressDialog();
                Context context = getContext();
                if (context != null) {
                    ToastUtil.showShortToast(context, error);
                }
                btn_code.setEnabled(true);
                btn_code.setText("重新发送");

            }
        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            if (!isCodeValid) {
                LogUtil.i("save count down time");
                BGSPUtil.save(getActivity(), BGSPUtil.KEY_VERIFY_CODE_EXIT_TIME, System.currentTimeMillis());
                BGSPUtil.save(getActivity(), BGSPUtil.KEY_VERIFY_CODE_LEFT_TIME, leftTime);
            }

            LogUtil.i("VerifyCodeFragment onDestroyView, cancel timer");
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == ResourceManager.getId(getContext(), "iv_back")) {
            //返回按钮
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_BACK, false, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_close")) {
            //关闭
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_CLOSE, false, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "btn_get_code")) {
            String mobile = et_mobile.getText().toString();

            if (TextUtils.isEmpty(mobile)) {
                ToastUtil.showShortToast(getContext(), "手机号不能为空");
                return;
            } else if (!CommonUtil.isMobileNumber(mobile)) {
                ToastUtil.showShortToast(getContext(), "手机号格式不正确,请重新输入");
                return;
            }
            verifyMobileExists(mobile);
//            requestSendCode(mobile);
        } else if (id == ResourceManager.getId(getContext(), "iv_show_pwd")) {
            //使用input type判断 不可用
            boolean showing = et_password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance());
            if (showing) {
                et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                iv_show_pwd.setImageResource(ResourceManager.getDrawable(getContext(), "bingo_ic_pwd_invisible"));
            } else {
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                iv_show_pwd.setImageResource(ResourceManager.getDrawable(getContext(), "bingo_ic_pwd_visible"));
            }

            et_password.setSelection(et_password.getText().length());
        } else if (id == ResourceManager.getId(getContext(), "btn_set")) {
            String code = et_code.getText().toString();
            String password = et_password.getText().toString();
            String mobile = et_mobile.getText().toString();

            if (TextUtils.isEmpty(mobile)) {
                ToastUtil.showShortToast(getContext(), "手机号不能为空");
            } else if (TextUtils.isEmpty(code)) {
                ToastUtil.showShortToast(getContext(), "验证码不能为空");
            } else if (TextUtils.isEmpty(password)) {
                ToastUtil.showShortToast(getContext(), "密码不能为空");
            } else {
                resetPwd(mobile, code, password);
            }
        }
    }

    private void verifyMobileExists(final String mobile) {
        JSONObject json = new JSONObject();
        try {
            json.put("mobile", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialog = DialogUtil.getInstance().showProgressDialog(getActivity(), "处理中");
        BingoHttpClient.postJson(ApiConfig.CHECK_MOBILE_EXISTS, json, new ResponseCallBack<CheckMobileExist>() {
            @Override
            public void onSuccess(CheckMobileExist data) {
                if (!data.isExistMobile()) {
                    dismissProgressDialog();
                    Context context = getContext();
                    if (context != null) {
                        ToastUtil.showShortToast(context, "该手机号尚未注册");
                    }
                } else {
                    requestSendCode(mobile);
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                dismissProgressDialog();
                Context context = getContext();
                if (context != null) {
                    ToastUtil.showShortToast(context, "获取验证码失败:" + msg);
                }
            }

            @Override
            public void onError(int code, String error) {
                dismissProgressDialog();
                Context context = getContext();
                if (context != null) {
                    ToastUtil.showShortToast(context, "获取验证码失败:" + error);
                }
            }
        });
    }

    private void dismissProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void resetPwd(String mobile, String code, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("mobile", mobile);
            json.put("code", code);
            json.put("newPassword", password);

            BingoHttpClient.postJson(ApiConfig.FIND_PASSWORD_BY_PHONE, json, new ResponseCallBack<EmptyBean>() {
                @Override
                public void onSuccess(EmptyBean data) {
                    LogUtil.i("密码重置成功");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Context context = getContext();
                            if (context == null)
                                return;
                            ToastUtil.showShortToast(context, "密码重置成功");
                            if (listener != null) {
                                listener.onEvent(FragmentEvent.EVENT_RESET_PWD_SUCCESS, false, null);
                            }
                        }
                    });
                }

                @Override
                public void onFailed(int code, final String msg) {
                    LogUtil.e("密码重置失败: " + msg);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Context context = getContext();
                            if (context == null)
                                return;
                            ToastUtil.showShortToast(context, "找回密码失败," + msg);
                        }
                    });
                }

                @Override
                public void onError(int code, final String error) {
                    LogUtil.e("密码重置失败: " + error);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Context context = getContext();
                            if (context == null)
                                return;
                            ToastUtil.showShortToast(context, "找回密码错误," + error);
                        }
                    });
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}