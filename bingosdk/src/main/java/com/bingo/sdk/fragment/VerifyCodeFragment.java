package com.bingo.sdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.bean.EmptyBean;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.log.GDTLogHelper;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.BGSPUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.view.VerifyCodeView;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.ResponseCallBack;
import com.bytedance.applog.GameReportHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VerifyCodeFragment extends BaseFragment implements View.OnClickListener {

    private String mobile = "";
    private TextView tv_count_down;
    private CountDownTimer timer;
    //倒计时时间,单位(秒)
    private int COUNT_DOWN_SECONDS = 60;
    private int leftTime = COUNT_DOWN_SECONDS;
    private UserInfo userInfo;
    private VerifyCodeView codeView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 记录验证码是否通过验证,如果是验证通过了,那么fragment销毁的时候则不应该记录
     * 倒计时
     */
    private boolean isCodeValid = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.i("VerifyCodeFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtil.i("VerifyCodeFragment onCreateView");
        return inflater.inflate(ResourceManager.getLayout(getContext(), "fragment_bingo_verify_code"), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogUtil.i("VerifyCodeFragment onViewCreated");
        initData();
        initView(view);
        setListener();

        countDown();

    }

    private void setListener() {
        codeView.setCodeInputListener(new VerifyCodeView.CodeInputListener() {
            @Override
            public void onInputComplete() {
                //todo 输入验证码完成,执行后面的请求,要做弹框等待,屏蔽重复发起的操作
                //验证码输入完成,自动执行注册
                doRegister();
//                //模拟登录成功
//                if (listener != null) {
//                    isCodeValid = true;
//                    listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, mobile);
//                }
            }

            @Override
            public void onInputInvalid() {

            }
        });


    }

    /**
     * 执行注册请求
     */
    private void doRegister() {
        String code = codeView.getCode();
        final Context context = getContext();
        if (context == null)
            return;

        JSONObject loginParams = ApiUtil.getBaseParams(context);
        try {
            loginParams.put("mobile", mobile);
            loginParams.put("code", code);
            BingoHttpClient.postJson(ApiConfig.MOBILE_REGISTER, loginParams, new ResponseCallBack<UserInfo>() {
                @Override
                public void onSuccess(final UserInfo data) {
                    LogUtil.e("注册成功: " + data);
                    userInfo = data;
                    isCodeValid = true;
                    Context ctx = getContext();
                    GameReportHelper.onEventRegister("mobile", true);
                    GDTLogHelper.onRegister("mobile", true);
                    if (ctx == null)
                        return;
                    ToastUtil.showShortToast(ctx, "注册成功");
                    AccountUtil.saveAccount2db(ctx, data.getUserName(), data.getPassword(), data);
                    if (listener != null) {
                        listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, userInfo.toJsonString());
                    }
                }

                @Override
                public void onFailed(int code, final String msg) {
                    LogUtil.e("注册失败: " + code + "\t" + msg);
                    GameReportHelper.onEventRegister("mobile", false);
                    GDTLogHelper.onRegister("mobile", false);
                    userInfo = null;
                    Context ctx = getContext();
                    if (ctx == null)
                        return;
                    ToastUtil.showShortToast(ctx, msg);

                }

                @Override
                public void onError(int code, String error) {
                    userInfo = null;
                    GameReportHelper.onEventRegister("mobile", false);
                    GDTLogHelper.onRegister("mobile", false);
                    LogUtil.e("注册异常: " + code + "\t" + error);
                    Context ctx = getContext();
                    if (ctx == null)
                        return;

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        } else {
            requestSendCode();
        }

    }

    private void requestSendCode() {
        startCountDown();

        tv_count_down.setEnabled(false);

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
                tv_count_down.setEnabled(true);
                tv_count_down.setText("重新发送");
                Context context = getContext();

                if (context != null) {
                    tv_count_down.setTextColor(getContext().getResources().getColor(ResourceManager.getColor(getContext(), "color_bingo_common_text_red")));
                    CommonUtil.setSelectableBackground(context, tv_count_down);

                    ToastUtil.showShortToast(context, "验证码发送成功");
                }

            }

            @Override
            public void onFailed(int code, final String msg) {
                LogUtil.e("发送验证码失败: " + code + "\t" + msg);

                tv_count_down.setEnabled(true);
                tv_count_down.setText("重新发送");
                Context context = getContext();
                if (context != null) {
                    tv_count_down.setTextColor(getContext().getResources().getColor(ResourceManager.getColor(getContext(), "color_bingo_common_text_red")));
                    CommonUtil.setSelectableBackground(context, tv_count_down);
                    ToastUtil.showShortToast(context, msg);
                }
            }

            @Override
            public void onError(int code, final String error) {
                LogUtil.e("请求异常: " + code + "\t" + error);
                Context context = getContext();
                if (context != null) {
                    ToastUtil.showShortToast(context, error);
                }
            }
        });

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
                tv_count_down.setText(secString);
                tv_count_down.setEnabled(false);
                Context context = getContext();
                if (context != null) {
                    tv_count_down.setTextColor(context.getResources().getColor(ResourceManager.getColor(getContext(), "color_bingo_gray_text")));
                    CommonUtil.setSelectableBackground(context, tv_count_down);
                }

            }

            @Override
            public void onFinish() {
//                LogUtil.i("onFinish");
                tv_count_down.setEnabled(true);
                tv_count_down.setText("重新发送");
                Context context = getContext();
                if (context != null) {
                    tv_count_down.setTextColor(getContext().getResources().getColor(ResourceManager.getColor(getContext(), "color_bingo_common_text_red")));
                    CommonUtil.setSelectableBackground(context, tv_count_down);
                }
            }
        };
        timer.start();
    }

    private void initData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mobile = arguments.getString("data", "");
        }
    }


    private void initView(View view) {

        view.findViewById(ResourceManager.getId(getContext(), "iv_close")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_back")).setOnClickListener(this);
        TextView tv_send = view.findViewById(ResourceManager.getId(getContext(), "tv_send_code"));
        tv_count_down = view.findViewById(ResourceManager.getId(getContext(), "tv_count_down"));
        tv_count_down.setOnClickListener(this);
        tv_send.setText("发送至:" + mobile);

        codeView = view.findViewById(ResourceManager.getId(getContext(), "code_view"));


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtil.i("VerifyCodeFragment onDestroy");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResourceManager.getId(getContext(), "iv_back")) {
            //返回按钮
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_BACK, false, "取消注册");
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_close")) {
            //关闭
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_CLOSE, false, "取消注册");
            }
        } else if (id == ResourceManager.getId(getContext(), "tv_count_down")) {
            countDown();
        }

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

}