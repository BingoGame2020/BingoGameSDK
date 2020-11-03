package com.bingo.sdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bingo.sdk.adapter.AccountListAdapter;
import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.encrypt.EncryptUtil;
import com.bingo.sdk.inner.encrypt.aes.AesUtil;
import com.bingo.sdk.inner.log.GDTLogHelper;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.ResponseCallBack;
import com.bytedance.applog.GameReportHelper;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AccountLoginFragment extends BaseFragment implements View.OnClickListener {

    private ConstraintLayout layout_popup, layout_tip;
    private ListPopupWindow window;
    private ImageView iv_account_list, iv_show_pwd;
    private EditText et_account, et_password;
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isChangeMode = false;//是否为切换账号模式


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(ResourceManager.getLayout(getContext(), "fragment_bingo_account_login"), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);

    }

    private void initData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            isChangeMode = arguments.getBoolean("change", false);
        }
    }


    private void initView(View view) {
        //点击输入框右边的时候触发屏蔽键盘,但是马上又弹出来
        //因为这个图标是在输入框区域内的,要么这里禁用,要买把这个图标放在输入框外面
//        CommonUtil.autoHideKeyboard(getActivity(), view.findViewById(ResourceManager.getId(getContext(), "layout_main));

        view.findViewById(ResourceManager.getId(getContext(), "iv_close")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_back")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "tv_register")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "tv_forget")).setOnClickListener(this);
        iv_account_list = view.findViewById(ResourceManager.getId(getContext(), "iv_account_list"));
        iv_show_pwd = view.findViewById(ResourceManager.getId(getContext(), "iv_show_pwd"));
        view.findViewById(ResourceManager.getId(getContext(), "btn_login")).setOnClickListener(this);

        et_account = view.findViewById(ResourceManager.getId(getContext(), "et_bingo_account"));
        et_password = view.findViewById(ResourceManager.getId(getContext(), "et_bingo_password"));
        iv_account_list.setOnClickListener(this);
        iv_show_pwd.setOnClickListener(this);
        layout_popup = view.findViewById(ResourceManager.getId(getContext(), "layout_popup"));
        layout_tip = view.findViewById(ResourceManager.getId(getContext(), "layout_tip"));
        if (isChangeMode) {
            //切换账号模式不显示注册和忘记密码
            layout_tip.setVisibility(View.GONE);
        } else {
            layout_tip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResourceManager.getId(getContext(), "iv_back")) {
            //返回按钮
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_BACK, true, "取消登录");
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_close")) {
            //关闭
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_CLOSE, true, "取消登录");
            }
        } else if (id == ResourceManager.getId(getContext(), "tv_forget")) {
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_TO_FORGET_PWD, false, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "tv_register")) {
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_REGISTER, false, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_account_list")) {
            showPopupWindow();
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
        } else if (id == ResourceManager.getId(getContext(), "btn_login")) {
            String account = et_account.getText().toString();
            String password = et_password.getText().toString();
            boolean accountValid = isAccountValid(account);
            boolean passwordValid = isPasswordValid(password);
            if (!accountValid) {
                ToastUtil.showShortToast(getContext(), "游戏账号不能为空");
                et_account.requestFocus();
                return;
            } else if (!passwordValid) {
                ToastUtil.showShortToast(getContext(), "密码不能为空");
                et_password.requestFocus();
                return;
            }

            doLogin(account, password);


        }

    }

    private void doLogin(final String account, final String password) {
        final Context context = getContext();
        if (context == null)
            return;

        JSONObject params = ApiUtil.getBaseParams(context);
        try {
            params.put("userName", account);
            params.put("password", password);
            BingoHttpClient.postJson(ApiConfig.LOGIN_ACCOUNT, params, new ResponseCallBack<UserInfo>() {
                @Override
                public void onSuccess(final UserInfo data) {
                    LogUtil.e("登录成功 User: " + data);
//                    showTopTips(data.getUserName());

                    callBackLoginSuccess(context, password, data);

                }

                @Override
                public void onFailed(final int code, final String msg) {
                    LogUtil.e("登录失败: " + code + "\t" + msg);
                    GameReportHelper.onEventLogin("account", false);
                    GDTLogHelper.onLogin("account", false);
                    ToastUtil.showShortToast(context, "登录失败: " + msg);
                    callbackLoginFailed(context, msg);
                }

                @Override
                public void onError(final int code, final String error) {
                    GameReportHelper.onEventLogin("account", false);
                    GDTLogHelper.onLogin("account", false);
                    LogUtil.e("登录错误: " + code + "\t" + error);
                    ToastUtil.showShortToast(context, "登录异常:" + error);
                    callbackLoginFailed(context, error);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callbackLoginFailed(Context context, String msg) {
        listener.onEvent(FragmentEvent.EVENT_LOGIN_FAILED, true, msg);
        GameReportHelper.onEventLogin("account", false);
        GDTLogHelper.onLogin("account", false);
    }


    /**
     * @param context  context
     * @param password 密码:后台无法返回,需要从上面传过来
     * @param data     登录结果
     */
    private void callBackLoginSuccess(Context context, String password, UserInfo data) {
//        ToastUtil.showShortToast(context, "登录成功");
        if (listener != null) {
            AccountUtil.saveAccount2db(context, data.getUserName(), password, data);
            listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, data.toJsonString());
            GameReportHelper.onEventLogin("account", true);
            GDTLogHelper.onLogin("account", true);
        }
    }


    private boolean isPasswordValid(String account) {
        //目前只是做了非空判断,如果后续需要添加规则,方法里面更方便加
        return !TextUtils.isEmpty(account);
    }

    private boolean isAccountValid(String pwd) {
        return !TextUtils.isEmpty(pwd);
    }


    private void showPopupWindow() {


        if (window == null) {
            final List<Account> accounts = AccountUtil.getAllAccount(getContext());

            final Context context = getContext();
            if (context == null)
                return;
            window = new ListPopupWindow(context);
            window.setModal(true);//不需要手动控制弹框消失,设置这个即可
            window.setHeight(CommonUtil.dp2px(getContext(), 144));
            final AccountListAdapter adapter = new AccountListAdapter(context, accounts);
            window.setAdapter(adapter);
            window.setAnchorView(layout_popup);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    iv_account_list.setImageResource(ResourceManager.getDrawable(getContext(), "bingo_ic_arrow_down"));
                }
            });

            adapter.setOnDeleteListener(new AccountListAdapter.OnItemDeleteListener() {
                @Override
                public void onDelete(Account account) {
                    LogUtil.e("delete: " + account);
                    String showingAccount = et_account.getText().toString();
                    if (!TextUtils.isEmpty(showingAccount) && showingAccount.equals(account.getUid())) {
                        //如果删除的是当前选中的账号,则清空界面显示的数据
                        et_account.setText("");
                        et_password.setText("");
                    }
                    long delete = AccountUtil.deleteByUid(context, account.getUid());
                    if (delete > 0) {
                        ToastUtil.showShortToast(context, "账号已删除");
                        try {
                            accounts.remove(account);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

            window.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Account selected = accounts.get(position);
                    LogUtil.e("选中: " + selected);
                    et_account.setText(selected.getUid());
                    String pKey = selected.getP_key();
                    String key = EncryptUtil.filterKey(pKey);
                    String decryptPwd = AesUtil.decrypt(selected.getPassword(), key);
                    et_account.setText(selected.getUid());
                    et_password.setText(decryptPwd);
                    window.dismiss();
                    if (position != 0)
                        //把当前选择的放到第一位
                        Collections.swap(accounts, 0, position);
                }
            });
        }
        window.show();
        iv_account_list.setImageResource(ResourceManager.getDrawable(getContext(), "bingo_ic_arrow_up"));


    }


}