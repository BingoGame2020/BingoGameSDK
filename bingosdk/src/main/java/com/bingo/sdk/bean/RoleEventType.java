package com.bingo.sdk.bean;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({RoleEventType.ROLE_CREATE, RoleEventType.ROLE_UPGRADE, RoleEventType.ROLE_LOGIN})
@Retention(RetentionPolicy.SOURCE)
public @interface RoleEventType {
    /**
     * 角色创建
     */
    int ROLE_CREATE = 1;
    /**
     * 角色升级
     */
    int ROLE_UPGRADE = 2;
    /**
     * 角色登录
     */
    int ROLE_LOGIN = 3;
}
