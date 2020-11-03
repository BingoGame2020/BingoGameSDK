package com.bingo.sdk.utils;

import android.content.Context;

/**
 * 通过反射的方式获取资源id, 直接用R.xxx 读取文件,在做打包工具的时候会找不到资源
 */
public class ResourceManager {

    /**
     * @param context context
     * @param type    类型,如 R.layout.xxx  那么这里就是 layout
     * @param name    资源名称,如R.layout.xxx  那么这里就是xxx
     * @return 资源id
     */
    public static int getResId(Context context, String type, String name) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    /**
     * 获取android sdk资源
     *
     * @param context context
     * @param type    类型,如 R.layout.xxx  那么这里就是 layout
     * @param name    资源名称,如R.layout.xxx  那么这里就是xxx
     * @return 资源id
     */
    public static int getAndroidResId(Context context, String type, String name) {
        return context.getResources().getIdentifier(name, type, "android");
    }

    public static int getId(Context context, String name) {

        return getResId(context, "id", name);
    }

    public static int getLayout(Context context, String name) {

        return getResId(context, "layout", name);
    }

    public static int getDrawable(Context context, String name) {

        return getResId(context, "drawable", name);
    }

    public static int getAnimation(Context context, String name) {
        return getResId(context, "anim", name);
    }

    public static int getString(Context context, String name) {
        return getResId(context, "string", name);
    }

    public static int getArray(Context context, String name) {
        return getResId(context, "array", name);
    }

    public static int getColor(Context context, String name) {
        return getResId(context, "color", name);
    }

    public static int getStyle(Context context, String name) {
        return getResId(context, "style", name);
    }

    public static int getMenu(Context context, String name) {
        return getResId(context, "menu", name);
    }
}
