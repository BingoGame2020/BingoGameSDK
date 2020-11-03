package com.bingo.sdk.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bingo.sdk.fragment.AccountLoginFragment;
import com.bingo.sdk.fragment.ForgetPasswordFragment;
import com.bingo.sdk.fragment.MobileLoginFragment;
import com.bingo.sdk.fragment.QuickGameFragment;
import com.bingo.sdk.fragment.VerifyCodeFragment;
import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.interf.LoginCallBack;
import com.bingo.sdk.inner.interf.OnFragmentEventListener;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BingoLoginActivity extends AppCompatActivity implements OnFragmentEventListener {
    private static LoginCallBack loginCallBack;
    private static final Gson gson = new Gson();

    private String TAG_LOGIN = "loginFragment", TAG_VERIFY = "verifyFragment",
            TAG_FORGET = "forgetFragment", TAG_QUICK = "quickFragment", TAG_ACCOUNT_LOGIN = "accountFragment";


    private HashMap<String, Fragment> fragmentHashMap = new HashMap<>(0);
    private HashSet<Fragment> addedFragment = new HashSet<>(0);

    public static void setLoginCallBack(LoginCallBack callBack) {
        loginCallBack = callBack;
        LogUtil.e("set login callback: " + loginCallBack);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceManager.getLayout(this, "activity_bingo_login"));
        reLayout();
        initView();
    }

    private void reLayout() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int height;
        int width;
        if (CommonUtil.isLandScape(this)) {
            width = (int) (metrics.widthPixels * 0.5);
        } else {
            width = (int) (metrics.widthPixels * 0.9);
        }
        height = WindowManager.LayoutParams.WRAP_CONTENT;

        lp.height = height;
        lp.width = width;
        getWindow().setAttributes(lp);
        setFinishOnTouchOutside(false);
    }

    private void initView() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                LogUtil.e("onBackStackChanged: " + backStackEntryCount);
                initFragmentMap();
            }
        });

        initFragmentMap();
        List<Account> accounts = AccountUtil.getAllAccount(this);
        if (accounts != null && !accounts.isEmpty()) {
            //如果本地有登录过账号,显示账号登录界面
            showFragment(TAG_ACCOUNT_LOGIN, null);
        } else {
            showFragment(TAG_LOGIN, null);
        }


    }

    /**
     * 将fragment和tag对应
     */
    private void initFragmentMap() {
        FragmentManager manager = getSupportFragmentManager();
        //可以抽基类
        MobileLoginFragment loginFragment = (MobileLoginFragment) manager.findFragmentByTag(TAG_LOGIN);
        VerifyCodeFragment codeFragment = (VerifyCodeFragment) manager.findFragmentByTag(TAG_VERIFY);
        AccountLoginFragment accountLoginFragment = (AccountLoginFragment) manager.findFragmentByTag(TAG_ACCOUNT_LOGIN);
        ForgetPasswordFragment forgetPasswordFragment = (ForgetPasswordFragment) manager.findFragmentByTag(TAG_FORGET);
        QuickGameFragment quickGameFragment = (QuickGameFragment) manager.findFragmentByTag(TAG_QUICK);


        if (loginFragment == null) {
            loginFragment = new MobileLoginFragment();
            loginFragment.setOnFragmentEventListener(this);
        }
        if (codeFragment == null) {
            codeFragment = new VerifyCodeFragment();
            codeFragment.setOnFragmentEventListener(this);
        }
        if (accountLoginFragment == null) {
            accountLoginFragment = new AccountLoginFragment();
            accountLoginFragment.setOnFragmentEventListener(this);
        }
        if (forgetPasswordFragment == null) {
            forgetPasswordFragment = new ForgetPasswordFragment();
            forgetPasswordFragment.setOnFragmentEventListener(this);
        }
        if (quickGameFragment == null) {
            quickGameFragment = new QuickGameFragment();
            quickGameFragment.setOnFragmentEventListener(this);
        }

        fragmentHashMap.put(TAG_LOGIN, loginFragment);
        fragmentHashMap.put(TAG_VERIFY, codeFragment);
        fragmentHashMap.put(TAG_ACCOUNT_LOGIN, accountLoginFragment);
        fragmentHashMap.put(TAG_QUICK, quickGameFragment);
        fragmentHashMap.put(TAG_FORGET, forgetPasswordFragment);

    }


    @Override
    public void onEvent(int event, boolean isLogin, String content) {
        //mobileLoginFragment 点击事件回调
        if (event == FragmentEvent.EVENT_NEXT) {
            JSONObject json = new JSONObject();
            showFragment(TAG_VERIFY, content);
        } else if (event == FragmentEvent.EVENT_QUICK_GAME) {
            showFragment(TAG_QUICK, content);
        } else if (event == FragmentEvent.EVENT_TO_REGISTER) {
            showFragment(TAG_QUICK, content);
        } else if (event == FragmentEvent.EVENT_TO_FORGET_PWD) {
            showFragment(TAG_FORGET, content);
        } else if (event == FragmentEvent.EVENT_ACCOUNT_LOGIN) {
            showFragment(TAG_ACCOUNT_LOGIN, content);
        } else if (event == FragmentEvent.EVENT_REGISTER) {
            showFragment(TAG_LOGIN, content);
        } else if (event == FragmentEvent.EVENT_BACK) {
            onBackPressed();
        } else if (event == FragmentEvent.EVENT_CLOSE) {
            closeAllFragments();
            finishWithResult(false, isLogin, content);
        } else if (event == FragmentEvent.EVENT_LOGIN_SUCCESS) {
            //登录成功
            finishWithResult(true, isLogin, content);
        } else if (event == FragmentEvent.EVENT_LOGIN_FAILED) {
            finishWithResult(false, isLogin, content);
        } else if (event == FragmentEvent.EVENT_RESET_PWD_SUCCESS) {
            closeAllFragments();
            finishWithResult(false, false, null);
        }
    }

    private void finishWithResult(boolean success, boolean isLogin, String content) {

        if (isLogin) {
            if (loginCallBack != null) {
                if (!success || TextUtils.isEmpty(content)) {
                    loginCallBack.onLogin(false, content, null);
                } else {
                    UserInfo info = gson.fromJson(content, UserInfo.class);
                    loginCallBack.onLogin(true, null, info);
                }
            }
        }
        finish();
    }


    private void closeAllFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Map.Entry<String, Fragment> entry : fragmentHashMap.entrySet()) {
            transaction.hide(entry.getValue());
            transaction.remove(entry.getValue());
        }
        transaction.commit();
        addedFragment.clear();
    }


    private void showFragment(String tag, String json) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = fragmentHashMap.get(tag);
        if (fragment == null) {
            return;
        }
        if (!addedFragment.contains(fragment)) {
            //如果没有添加过
            transaction.add(ResourceManager.getId(this, "frameLayout"), fragment, tag);
            addedFragment.add(fragment);
        }
        if (addedFragment.size() > 1) {
            //只有1个fragment显示时不需要添加到栈内,否则在处理返回时出出现一个空白弹框
            transaction.addToBackStack(tag);
        }

        hideFragment(transaction, fragment);

        if (!TextUtils.isEmpty(json)) {
            //如果有带参数
            Bundle intent = new Bundle();
            intent.putString("data", json);
            fragment.setArguments(intent);
        }

        transaction.show(fragment);


        transaction.commit();
        //fragment显示后会绑定tag和id,需要更新map中的数据
        initFragmentMap();
    }


    private void hideFragment(FragmentTransaction transaction, Fragment currentShowFragment) {
        for (Fragment f : addedFragment) {
            //遍历已经添加的,除了当前要显示的,其余的都隐藏
            if (f != currentShowFragment) {
                LogUtil.e("Hide fragment: " + f);
                transaction.hide(f);
            }
        }
    }


    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        int count = manager.getBackStackEntryCount();
        LogUtil.e("back stack count: " + count);
        if (count == 0) {
            finishWithResult(false, true, "取消登录");
        } else {
            manager.popBackStack();
        }
    }

    @Override
    public void finish() {
        super.finish();
        LogUtil.e("bingo login activity finish ");
        loginCallBack = null;
    }
}