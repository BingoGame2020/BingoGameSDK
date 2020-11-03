package com.bingo.sdk.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.adapter.BingoAccountPanelAdapter;
import com.bingo.sdk.inner.bean.FloatWindow;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.BGSPUtil;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.utils.ResourceManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


public class FloatingActionView extends FrameLayout implements OnTouchListener {
    private WindowManager.LayoutParams wlp;
    private WindowManager wm;
    private int screenWidth;
    private int screenHeight;
    private boolean isRightSide = false;
    private boolean isMoved = false;
    private View floatingView;
    private int downX, downY;
    private int statusBarHeight = 0;
    private int navBarHeight = 0;
    private boolean hasNavigationBar;
    private int showingMode;
    private final int MODE_NORMAL = 0, MODE_SMALL = 1;
    private long downTime;
    private PopupWindow window;
    private boolean tempClose = false;//标记是否用户主动关闭浮标,重启应用失效
    private boolean isShowingCloseView;
    private boolean shouldClose;
    private View popupView;
    private TextView tv_close_text;
    private int closeViewHeight, closeViewWidth;
    private boolean isAdded;

    private static FloatingActionView instance;
    private Activity mActivity;
    private PopupWindow popupWindow;
    private View panel_view;

    public static FloatingActionView getInstance(Activity activity) {
        if (instance == null) {
            instance = new FloatingActionView(activity);
        }
        return instance;
    }


    private FloatingActionView(Activity activity) {
        super(activity);
        createWM(activity);

    }

    private void createWM(Activity activity) {

        wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }

        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
//            statusBarHeight = getResources().getDimensionPixelSize(resourceId);//todo 需要判断异形屏
        }
        int navId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (navId > 0) {
            //根据资源ID获取响应的尺寸值
//            navBarHeight = getResources().getDimensionPixelSize(navId);
//            LogUtil.i(TAG, "navigation bar height " + navBarHeight + "\t" + screenHeight);
        }
        hasNavigationBar = checkDeviceHasNavigationBar(activity);
//        LogUtil.i(TAG, "device has navigationBar: " + hasNavigationBar);

        this.wlp = new WindowManager.LayoutParams();

//        wlp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        wlp.format = PixelFormat.RGBA_8888;
//        wlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        wlp.type = 99;//需要这样写,如果用上面的 部分手机会因为没有权限而无法显示
        wlp.format = 1;
        wlp.flags = 8;

        wlp.gravity = Gravity.START | Gravity.TOP;

        Object ox = BGSPUtil.get(activity, BGSPUtil.KEY_FLOATING_VIEW_X);//缓存的坐标
        Object oy = BGSPUtil.get(activity, BGSPUtil.KEY_FLOATING_VIEW_Y);
        int x = ox == null ? 0 : (int) ox;
        int y = oy == null ? 0 : (int) oy;
        x = Math.max(0, x);//避免某些特殊情况,读取到的缓存数据为负数
        y = Math.max(0, y);
        LogUtil.e("缓存坐标:" + x + "\t" + y);


        //判断缓存的坐标是处于屏幕中
        //1: x坐标大于0 并且小于屏幕宽度
        //2: y坐标大于0 并且小于屏幕高度
        //3: 不在这个区域表示出界
        if ((x > 0 && x < screenWidth) || (y > 0 && y < screenHeight)) {
            wlp.x = x;
            wlp.y = y;
            if (x > screenWidth / 2) {
                isRightSide = true;
            }
        } else {
            wlp.x = screenWidth - 100;//右边
            wlp.y = screenHeight / 2;//垂直居中
            isRightSide = true;
        }

        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏模式,用屏幕高度的1/10
            wlp.width = (int) (screenHeight * 0.1);
            wlp.height = (int) (screenHeight * 0.1);
        } else {
            wlp.width = (int) (screenWidth * 0.1);
            wlp.height = (int) (screenWidth * 0.1);
        }

        if (floatingView == null) {
            floatingView = LayoutInflater.from(activity).inflate(ResourceManager.getLayout(activity, "layout_bingo_floatview"), null);
            floatingView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            floatingView.setFocusable(true);
            floatingView.setClickable(true);
            floatingView.setOnTouchListener(this);

        }

        try {
            if (!activity.isFinishing()) {
                if (!isAdded) {
                    wm.addView(floatingView, wlp);
                    isAdded = true;
                }
            }
        } catch (Exception e) {
            LogUtil.e("浮标显示异常: " + e.toString());
        }

    }


    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            //do something
        }
        return hasNavigationBar;
    }

    public void display(Activity activity) {
        this.mActivity = activity;//一定要在这里刷新,否则activity关闭后,这里面的引用就失效了
        if (!tempClose) {//如果用户手动关闭了,则单次运行不再显示
            if (!isAdded)
                createWM(activity);
            if (floatingView != null)
                floatingView.setVisibility(VISIBLE);
            halfDisplay();
        }
    }


    public void disappear() {
        floatingView.setVisibility(GONE);
    }


    public void destroy() {
        try {
            LogUtil.e("销毁浮标");
            if (wlp != null && !tempClose) {
                //如果用户手动关闭了浮标,在退出时不再缓存上次显示的位置,因为关闭时已经保存了下次初始化的位置
                LogUtil.e("销毁时保存坐标: " + mActivity);
                if (mActivity == null)
                    return;
                BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_X, wlp.x);
                BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_Y, wlp.y);
            }
            removeRootView();
            removePopupWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void removePopupWindow() {
        if (popupWindow != null) {
            LogUtil.e("移除popup window");
            popupWindow.dismiss();
            popupWindow = null;
        }
    }


    private void removeRootView() {
        try {
            if (wm != null && floatingView != null) {
                wm.removeViewImmediate(floatingView);//不要使用removeView(), 没效果
                LogUtil.e("移除浮标完毕");
                isAdded = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("移除浮标出错:" + e.getMessage());
        }
    }

    int[] out = new int[2];


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = event.getDownTime();
                restoreDisplay();


                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                isMoved = false;

                break;
            case MotionEvent.ACTION_MOVE:
                int mMoveStartX = (int) event.getRawX();
                int mMoveStartY = (int) event.getRawY();

//                LogUtil.i(TAG, "移动 X: " + mMoveStartX + "\tY: " + mMoveStartY);
                int spaceX = Math.abs(downX - mMoveStartX);
                int spaceY = Math.abs(downY - mMoveStartY);

//                LogUtil.e(TAG, "移动距离: X " + spaceX + "\tY: " + spaceY);

                if (spaceX >= 10 || spaceY >= 10) {//移动10个像素点才算是移动
                    isMoved = true;
                    downTime = event.getEventTime();
//                    dismissCloseView();
                    if (window != null) {
                        popupView = window.getContentView();
                        if (popupView != null) {
                            out = new int[2];
                            popupView.getLocationOnScreen(out);//获取popupwindow(关闭区域)的位置
                            if (closeViewHeight == 0 && closeViewWidth == 0) {
                                closeViewHeight = popupView.getHeight();
                                closeViewWidth = popupView.getWidth();
                            }
//                            LogUtil.e(TAG, "关闭区域坐标: " + Arrays.toString(out) + "\t布局宽高:" + closeViewWidth + ":" + closeViewHeight);
                        }
                    }
                    dismissOptionView();
                    if (!isShowingCloseView)
                        showCloseView();
                    int maxHeight = screenHeight - navBarHeight;
                    wlp.x = mMoveStartX - floatingView.getWidth() / 2;
                    int y = mMoveStartY - floatingView.getHeight();
                    if (y < statusBarHeight) {
                        wlp.y = statusBarHeight;
                    } else if (hasNavigationBar && (y + floatingView.getHeight()) > maxHeight) {
                        //这里要用y加上高度是因为y获取的是按钮上面的y坐标,但是实际需要用按钮下面的y坐标来判断
                        wlp.y = maxHeight - floatingView.getHeight();//同理,设置按钮的坐标都是以左上角为原点的,所以要让下边正好在对应位置,就需要减去按钮高度
                    } else {
                        wlp.y = y;
                    }
                    wm.updateViewLayout(floatingView, wlp);
                    if (wlp.x > screenWidth / 2) {
                        //大于屏幕一般,设置为右侧
                        isRightSide = true;
                    } else {
                        isRightSide = false;
                    }
//                    LogUtil.e(TAG, "wlp: " + wlp.x + "\t" + wlp.y);
                    if (mMoveStartX > out[0] && mMoveStartY > out[1] && mMoveStartX < (out[0] + closeViewWidth) && mMoveStartY < (out[1] + closeViewHeight)) {
//                        LogUtil.i(TAG, "到达关闭区域 ");
                        if (popupView != null) {
                            popupView.setAlpha(1);
                        }
                        if (tv_close_text != null) {
                            tv_close_text.setText("松开手指关闭悬浮窗");//todo 放到xml中

                        }
                        shouldClose = true;
                    } else {
                        shouldClose = false;
                        if (popupView != null) {
                            popupView.setAlpha(0.7f);
                        }

                        if (tv_close_text != null) {
                            tv_close_text.setText("拖动到此区域关闭悬浮窗");//todo 放到xml中
                        }
                    }

                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                halfDisplay();
                if (!isMoved) {
//                    v.performClick();
//                    LogUtil.e(TAG, "放开:" + event.getEventTime() + "\t按下时间: " + downTime);
                    if (event.getEventTime() - downTime > 1000) {
                    } else {
//                        if (listener != null) {
//                            listener.onClick(this);
//                        }

                        showPopupWindow();

//                        CommonOverSdkManger.getInstance().clearUnRead();
                    }

                    return false;
                }

                wm.updateViewLayout(floatingView, wlp);
                if (shouldClose) {
                    tempClose = true;
                    disappear();
//                    destroy();
                    dismissCloseView();
                    //为了避免下次打开浮标时显示在上次关闭浮标的位置,将缓存位置保存在左上角
                    if (mActivity != null) {
                        LogUtil.e("临时关闭,保存坐标");
                        BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_X, 0);
                        BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_Y, 200);
                    }
                } else {
                    dismissCloseView();
                }
                // 重置

                break;
        }
        return false;
    }


    private void restoreDisplay() {
        showFAB(floatingView, 0);
        changeHandler.removeCallbacks(delayRunnable);

    }

    private void halfDisplay() {
        changeHandler.removeCallbacks(delayRunnable);
        changeHandler.postDelayed(delayRunnable, 2000);
    }


    private Handler changeHandler = new Handler();
    //    private Handler longClickHandler = new Handler();
    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            if (floatingView != null) {
                hideFAB(floatingView, 500);
                dismissCloseView();
            }
        }
    };


    /**
     * 动画显示布局
     *
     * @param view     需要动画的视图
     * @param duration 动画时间
     */
    private void showFAB(View view, int duration) {
        showingMode = MODE_NORMAL;
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.5f, 1f);
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhAlpha, pvhScaleX, pvhScaleY);
        animator.setDuration(duration).start();
    }


    /**
     * 动画消失布局
     *
     * @param view     需要动画的视图
     * @param duration 动画时间
     */
    private void hideFAB(View view, int duration) {
        if (showingMode == MODE_SMALL)
            return;
        dismissCloseView();
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f);
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.5f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.5f);


        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhAlpha, pvhScaleX, pvhScaleY);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showingMode = MODE_SMALL;
                if (floatingView != null) {
                    if (wlp.x >= screenWidth / 2) {
                        wlp.x = screenWidth + 10;
                        isRightSide = true;
                    } else if (wlp.x < screenWidth / 2) {
                        wlp.x = -20;
                        isRightSide = false;
                    }

                    try {
                        if (isAdded)
                            wm.updateViewLayout(floatingView, wlp);
                    } catch (Exception e) {
                        LogUtil.e("缩小浮标,更新错误" + e.getMessage());
                    }
                    LogUtil.e("缩小浮标,保存位置: " + mActivity);
                    if (mActivity == null)
                        return;
                    BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_X, wlp.x);
                    BGSPUtil.save(mActivity, BGSPUtil.KEY_FLOATING_VIEW_Y, wlp.y);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(duration).start();
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.i("onConfigurationChanged");
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        int oldX = wlp.x;
        int oldY = wlp.y;
        // 横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            if (isRightSide) {
                wlp.x = screenWidth;
            } else {
                wlp.x = oldX;
            }
            wlp.y = oldY;
        }
        wm.updateViewLayout(floatingView, wlp);
    }


    public void showCloseView() {
        if (mActivity == null)
            return;
        isShowingCloseView = true;
        View contentView = LayoutInflater.from(mActivity).inflate(ResourceManager.getLayout(mActivity, "layout_bingo_floatview_close"), null, false);
        tv_close_text = contentView.findViewById(ResourceManager.getId(mActivity, "tv_close_flow"));
        window = new PopupWindow(contentView, (int) (screenWidth * 0.8), 200, true);//这里的宽高不指定的话, 在4.4可能会无法显示
        window.setTouchable(true);
        window.setBackgroundDrawable(new ColorDrawable());
        window.showAtLocation(getRootView(), Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, CommonUtil.dp2px(mActivity, 48));
//        LogUtil.e(TAG, "Y坐标: " + wlp.y + "\t高度:" + screenHeight + "\t视图高度: " + contentView.getHeight());
    }

    private void showPopupWindow() {
        LogUtil.e("showPopupWindow:" + mActivity + "\t" + popupWindow);
        if (mActivity == null) {
            LogUtil.e("showPopupWindow ignored, activity is null");
            return;
        }
        if (mActivity.isFinishing()) {
            LogUtil.e("showPopupWindow ignored, activity is finishing");
            return;
        }

        if (popupWindow == null) {
            panel_view = View.inflate(mActivity, ResourceManager.getLayout(mActivity, "layout_bingo_floatview_account_panel"), null);
//            popupWindow = new PopupWindow(panel_view, (int) (screenWidth * 0.8), LayoutParams.WRAP_CONTENT, true);
            popupWindow = new PopupWindow(mActivity);
            boolean landScape = CommonUtil.isLandScape(mActivity);
            popupWindow.setContentView(panel_view);
            if (landScape) {
                popupWindow.setWidth((int) (screenWidth * 0.4));
            } else {
                popupWindow.setWidth((int) (screenWidth * 0.8));
            }
            popupWindow.setTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable());

            OnClickListener l = new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        if (onClickListener != null)
                            onClickListener.onClick(v);
                    }
                }
            };


            RecyclerView recycler = panel_view.findViewById(ResourceManager.getId(mActivity, "recycler_account_panel"));
            setAdapter(recycler);

            panel_view.findViewById(ResourceManager.getId(mActivity, "tv_change_account")).setOnClickListener(l);
            panel_view.findViewById(ResourceManager.getId(mActivity, "iv_close")).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
            });


        } else if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }

        int[] screen_location = new int[2];
        floatingView.getLocationOnScreen(screen_location);

        LogUtil.e("坐标:" + Arrays.toString(screen_location));

        int xOffset;
        if (isRightSide) {
            int width = popupWindow.getWidth();
            LogUtil.e("宽:" + width);
            xOffset = screen_location[0] - width - 20;//X坐标为 浮标当前的横坐标 - 弹框宽度 -20(作为默认间隔)
            popupWindow.setAnimationStyle(ResourceManager.getStyle(mActivity, "popupwindow_anim_right_style"));

        } else {
            //浮标位于屏幕左侧
            int width = floatingView.getWidth();
            LogUtil.e("浮标宽度:" + width);
            xOffset = screen_location[0] + width + 20;//X坐标为 浮标当前的横坐标 - 悬浮窗宽度 -20(作为默认间隔)
            popupWindow.setAnimationStyle(ResourceManager.getStyle(mActivity, "popupwindow_anim_left_style"));

        }
        LogUtil.e("显示悬浮窗弹框:" + xOffset + "\t");

        TextView tv_userName = panel_view.findViewById(ResourceManager.getId(mActivity, "tv_user_name"));
        if (BingoSdkCore.getInstance().isLogin()) {
            tv_userName.setText(AccountUtil.getCurrentLoginAccount(mActivity).getUid());
        } else {
            tv_userName.setText("未登录");
        }
        popupWindow.showAtLocation(getRootView(), Gravity.TOP | Gravity.START, xOffset, screen_location[1]);
    }

    private void setAdapter(RecyclerView recycler) {
        GridLayoutManager manager = new GridLayoutManager(mActivity, 4);
        recycler.setLayoutManager(manager);

        List<FloatWindow> list = BingoSdkCore.getInstance().getGameConfig().getGame().getFloatWindowsVoList();
        BingoAccountPanelAdapter adapter = new BingoAccountPanelAdapter(mActivity, list);
        adapter.setOnItemClickListener(new BingoAccountPanelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FloatWindow window) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                itemClickListener.onItemClick(window);
            }
        });
        recycler.setAdapter(adapter);
    }


    private void dismissOptionView() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void dismissCloseView() {
        if (window != null) {
            window.dismiss();
            isShowingCloseView = false;
        }
    }

    public void showRedPoint(boolean showRed) {
        if (floatingView != null) {
//            ImageView redDot = floatingView.findViewById(ResourceManager.getId(context,"iv_red_dot);
//            redDot.setVisibility(showRed ? VISIBLE : GONE);
//            invalidate();
        }
    }

    public boolean isAdded() {
        return isAdded;
    }

    private OnClickListener onClickListener;


    public void setOnClickListener(OnClickListener clickListener) {
        this.onClickListener = clickListener;
    }

    public boolean isAccountPanelShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    public void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private BingoAccountPanelAdapter.OnItemClickListener itemClickListener;

    public void setOnItemClickListener(BingoAccountPanelAdapter.OnItemClickListener l) {
        itemClickListener = l;
    }
}
