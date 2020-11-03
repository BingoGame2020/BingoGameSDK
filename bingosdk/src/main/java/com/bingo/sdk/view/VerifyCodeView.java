package com.bingo.sdk.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.utils.ResourceManager;

public class VerifyCodeView extends RelativeLayout {
    private final int MAX_LENGTH = 6;
    private EditText et_code;
    private String code;
    private Context context;
    private RelativeLayout layout_main;
    private AppCompatTextView[] textViews = new AppCompatTextView[MAX_LENGTH];


    public VerifyCodeView(Context context) {
        this(context, null);
    }

    public VerifyCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyCodeView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View.inflate(context, ResourceManager.getLayout(context, "layout_bingo_verify_code"), this);
        textViews[0] = findViewById(ResourceManager.getId(context, "tv_0"));
        textViews[1] = findViewById(ResourceManager.getId(context, "tv_1"));
        textViews[2] = findViewById(ResourceManager.getId(context, "tv_2"));
        textViews[3] = findViewById(ResourceManager.getId(context, "tv_3"));
        textViews[4] = findViewById(ResourceManager.getId(context, "tv_4"));
        textViews[5] = findViewById(ResourceManager.getId(context, "tv_5"));

        layout_main = findViewById(ResourceManager.getId(context, "layout_main"));
        et_code = findViewById(ResourceManager.getId(context, "et_code"));
        et_code.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300);
        setEditTextListener(context);

    }

    public String getCode() {
        return code;
    }

    private void setEditTextListener(final Context context) {
        final Drawable red = ResourcesCompat.getDrawable(context.getResources(), ResourceManager.getDrawable(context, ".bingo_shape_text_underline_red"), context.getTheme());
        final Drawable gray = ResourcesCompat.getDrawable(context.getResources(), ResourceManager.getDrawable(context, ".bingo_shape_text_underline_gray"), context.getTheme());
        et_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                code = s.toString();
                if (listener != null) {
                    if (code.length() >= MAX_LENGTH) {
                        listener.onInputComplete();
                    } else {
                        listener.onInputInvalid();
                    }
                }


                for (int i = 0; i < textViews.length; i++) {
                    if (i < code.length()) {
                        //setText()不能直接设置int,int会被认为是ResId
                        textViews[i].setText(String.valueOf(code.charAt(i)));

                        textViews[i].setCompoundDrawablesWithIntrinsicBounds(null, null, null, red);
                    } else {
                        //还没输入值的textview
                        textViews[i].setText("");
                        textViews[i].setCompoundDrawablesWithIntrinsicBounds(null, null, null, gray);
                    }
                }
            }
        });


        et_code.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("输入框长按");

                showCopyPopupMenu();
                return true;
            }
        });
    }


    public void showCopyPopupMenu() {
        PopupMenu popup = new PopupMenu(context, et_code);
        ((Activity) context).getMenuInflater().inflate(ResourceManager.getMenu(context, "bingo_menu_paste"), popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == ResourceManager.getId(context, "action_paste")) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip == null)
                        return true;
                    int itemCount = clip.getItemCount();
                    if (itemCount == 0) {
                        return true;
                    }

                    ClipData.Item clipItem = clip.getItemAt(0);
                    CharSequence text = clipItem.coerceToText(context);
                    LogUtil.e("剪贴板内容: " + text);
                    if (!TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)) {
                        et_code.setText(text);
                    }

                }
                return false;
            }
        });

        popup.show();
    }

    private CodeInputListener listener;

    public void setCodeInputListener(CodeInputListener listener) {
        this.listener = listener;
    }

    public interface CodeInputListener {
        void onInputComplete();

        void onInputInvalid();
    }
}
