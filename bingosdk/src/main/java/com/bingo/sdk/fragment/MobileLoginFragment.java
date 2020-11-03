package com.bingo.sdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.inner.util.ViewUtil;
import com.bingo.sdk.utils.ResourceManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MobileLoginFragment extends BaseFragment implements View.OnClickListener {

    private AppCompatEditText et_mobile;
    private AppCompatCheckBox checkBox;
    private TextView tv_country_code;
    private ListPopupWindow window;
    private ConstraintLayout layout_popup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(ResourceManager.getLayout(getContext(), "fragment_bingo_mobile_login"), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }


    private void initView(View view) {
        CommonUtil.autoHideKeyboard(getActivity(), view.findViewById(ResourceManager.getId(getContext(), "layout_main")));

        TextView tv_privacy = view.findViewById(ResourceManager.getId(getContext(), "tv_privacy"));
//        CommonUtil.setTextViewUnderline(tv_privacy);
        String content = getString(ResourceManager.getString(getContext(), "bingo_privacy_tip"));
        ViewUtil.setPrivacySpan(getContext(), tv_privacy, content);

        et_mobile = view.findViewById(ResourceManager.getId(getContext(), "et_phone_number"));
        checkBox = view.findViewById(ResourceManager.getId(getContext(), "cb_privacy"));

        view.findViewById(ResourceManager.getId(getContext(), "btn_next")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_close")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_back")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "layout_quick_game")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "layout_account_login")).setOnClickListener(this);

//        tv_privacy.setOnClickListener(this);

        layout_popup = view.findViewById(ResourceManager.getId(getContext(), "layout_popup"));
        tv_country_code = view.findViewById(ResourceManager.getId(getContext(), "tv_country_code"));
        tv_country_code.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResourceManager.getId(getContext(), "btn_next")) {

            Editable editable = et_mobile.getText();
            if (editable == null) {
                return;
            }
            String mobile = editable.toString().trim();
            if (!CommonUtil.isMobileNumber(mobile)) {
                ToastUtil.showShortToast(getActivity(), "手机号格式错误");
                return;
            }
            //下一步
            boolean agreed = checkBox.isChecked();
            if (!agreed) {
                ToastUtil.showShortToast(getActivity(), "请先阅读并同意隐私政策");
                return;
            }
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_NEXT, true, mobile);
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_back")) {
            //返回按钮
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_BACK, true, "取消登录");
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_close")) {
            //关闭
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_CLOSE, true, "取消登录");
            }
        } else if (id == ResourceManager.getId(getContext(), "layout_account_login")) {
            //登录
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_ACCOUNT_LOGIN, true, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "layout_quick_game")) {
            //快速游戏
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_QUICK_GAME, false, null);
            }
        } else if (id == ResourceManager.getId(getContext(), "tv_country_code")) {
            showPopupWindow();
        }


    }

    private void showPopupWindow() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        if (window == null) {

            final String[] array = context.getResources().getStringArray(ResourceManager.getArray(getContext(), "itc"));


            window = new ListPopupWindow(context);
            window.setModal(true);//不需要手动控制弹框消失,设置这个即可
            window.setHeight(CommonUtil.dp2px(context, 144));
            window.setAdapter(new ArrayAdapter<>(context, ResourceManager.getAndroidResId(context, "layout", "simple_list_item_1"), array));
            window.setAnchorView(layout_popup);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                }
            });

            window.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selected = array[position];
                    LogUtil.e("选中: " + selected);
                    window.dismiss();
                    tv_country_code.setText(selected);
                }
            });
        }
        window.show();
    }


}