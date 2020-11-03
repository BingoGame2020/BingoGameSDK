package com.bingo.sdk.callback;

/**
 * Created by OuYanglz on 2018/7/2.
 */

public interface PermissionCallback {
    /**
     * 用户赋予了申请的所有权限
     * <p>
     * 注:在6.0以下是不需要申请的,因此会直接调用该回调
     * </p>
     */
    void onPermissionGranted();

    /**
     * 申请的权限一个或多个被拒绝
     */
    void onPermissionDenied();
}
