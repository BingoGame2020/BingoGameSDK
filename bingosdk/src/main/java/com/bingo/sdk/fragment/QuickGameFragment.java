package com.bingo.sdk.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bingo.sdk.R;
import com.bingo.sdk.inner.annotation.FragmentEvent;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.log.GDTLogHelper;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.ApiUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.utils.ResourceManager;
import com.bingo.sdk.web.ApiConfig;
import com.bingo.sdk.web.BingoHttpClient;
import com.bingo.sdk.web.ResponseCallBack;
import com.bytedance.applog.GameReportHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QuickGameFragment extends BaseFragment implements View.OnClickListener {

    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView tv_userName, tv_pwd, tv_tip;
    private int viewWidth, viewHeight;
    private Button btn_enter;
    private ViewTreeObserver.OnGlobalLayoutListener viewObserver;
    private String IMAGES_FOLDER_NAME = "bingoGame";
    private CountDownTimer timer;
    private int COUNT_DOWN_SECONDS = 15;
    private UserInfo userInfo;
    private boolean isSuccess = false;//是否快速注册成功

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(ResourceManager.getLayout(getContext(), "fragment_bingo_quick_game"), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        quickRegister();

    }

    private void initView(final View view) {
//        CommonUtil.autoHideKeyboard(getActivity(), view.findViewById(ResourceManager.getId(getContext(), "layout_main));
        view.findViewById(ResourceManager.getId(getContext(), "iv_close")).setOnClickListener(this);
        view.findViewById(ResourceManager.getId(getContext(), "iv_back")).setOnClickListener(this);
        btn_enter = view.findViewById(ResourceManager.getId(getContext(), "btn_enter"));
        btn_enter = view.findViewById(ResourceManager.getId(getContext(), "btn_enter"));
        tv_tip = view.findViewById(ResourceManager.getId(getContext(), "tv_tip_account"));
        btn_enter.setEnabled(false);
        tv_userName = view.findViewById(ResourceManager.getId(getContext(), "tv_account"));
        tv_pwd = view.findViewById(ResourceManager.getId(getContext(), "tv_password"));


        btn_enter.setOnClickListener(this);

        viewObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewWidth = view.getWidth();
                viewHeight = view.getHeight();
                LogUtil.e("width: " + viewWidth + "\theight: " + viewHeight);
                view.getViewTreeObserver().removeOnGlobalLayoutListener(viewObserver);
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(viewObserver);
    }

    private void quickRegister() {
        Context context = getContext();
        if (context == null)
            return;
        JSONObject params = ApiUtil.getBaseParams(context);
        BingoHttpClient.postJson(ApiConfig.QUICK_REGISTER, params, new ResponseCallBack<UserInfo>() {
            @Override
            public void onSuccess(final UserInfo data) {
                LogUtil.e("快速注册成功: " + data);
                isSuccess = true;
                userInfo = data;
                Context ctx = getContext();
                tv_pwd.setText("密码: " + data.getPassword());
                tv_userName.setText("账号: " + data.getUserName());
                btn_enter.setEnabled(true);
                tv_tip.setVisibility(View.VISIBLE);
                GameReportHelper.onEventRegister("quick", true);
                AccountUtil.saveAccount2db(ctx, data.getUserName(), data.getPassword(), data);
                GDTLogHelper.onRegister("quick", true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //避免有内容显示出来,延迟一秒再截图
                        saveImage();
                        startCountDown();
                    }
                }, 1000);

            }

            @Override
            public void onFailed(int code, final String msg) {
                LogUtil.e("快速注册失败: " + code + "\t" + msg);
                isSuccess = false;
                userInfo = null;
                GameReportHelper.onEventRegister("quick", false);
                GDTLogHelper.onRegister("quick", false);
                Context ctx = getContext();
                if (ctx != null) {
                    ToastUtil.showShortToast(ctx, "快速注册失败:" + msg);
                }
            }

            @Override
            public void onError(int code, final String error) {
                isSuccess = false;
                userInfo = null;
                GameReportHelper.onEventRegister("quick", false);
                GDTLogHelper.onRegister("quick", false);
                LogUtil.e("快速注册异常: " + code + "\t" + error);
                Context ctx = getContext();
                if (ctx != null) {
                    ToastUtil.showShortToast(ctx, "快速注册异常:" + error);
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
                String secString = "进入游戏(" + sec + "秒)";
                btn_enter.setText(secString);
                Context context = getContext();


            }

            @Override
            public void onFinish() {
//                LogUtil.i("onFinish");
                btn_enter.setText("进入游戏");
                Context context = getContext();
                if (listener != null) {
                    listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, userInfo.toJsonString());
                }

            }
        };
        timer.start();
    }


    private void saveImage() {
        Context context = getContext();
        Activity activity;
        if (!(context instanceof Activity)) {
            return;
        }
        activity = (Activity) context;
        LogUtil.e("activity: " + activity);

        Bitmap bitmap = screenShot(activity);

        if (isEmptyBitmap(bitmap))
            return;

        try {
            String format = "YYYYMMddHHmmss";
            String time = new SimpleDateFormat(format, Locale.getDefault()).format(new Date(System.currentTimeMillis()));
            String name = "bingo_account" + time + ".jpg";
            saveImage(context, bitmap, name);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void saveImage(Context context, Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdirs();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        LogUtil.e("保存图片: " + saved);
        fos.flush();
        fos.close();
    }

    private boolean isEmptyBitmap(Bitmap bitmap) {
        return bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0;
    }


    /**
     * 获取当前屏幕截图，不包含状态栏（Status Bar）。
     *
     * @param activity activity
     * @return Bitmap
     */
    private Bitmap screenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();


        Bitmap ret = Bitmap.createBitmap(bmp, 0, 0, viewWidth, viewHeight);
        view.destroyDrawingCache();

        return ret;
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
                if (isSuccess) {
                    listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, userInfo.toJsonString());
                } else {
                    listener.onEvent(FragmentEvent.EVENT_BACK, false, "取消登录");
                }
            }
        } else if (id == ResourceManager.getId(getContext(), "iv_close")) {
            //关闭
            if (isSuccess) {
                if (listener != null)
                    listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, userInfo.toJsonString());
            } else {
                if (listener != null) {
                    listener.onEvent(FragmentEvent.EVENT_CLOSE, false, "取消登录");
                }
            }
        } else if (id == ResourceManager.getId(getContext(), "btn_enter")) {
            if (listener != null) {
                listener.onEvent(FragmentEvent.EVENT_LOGIN_SUCCESS, true, userInfo.toJsonString());
            }
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {

            LogUtil.i("quick register onDestroyView, cancel timer");
            timer.cancel();
            timer = null;
        }
    }
}