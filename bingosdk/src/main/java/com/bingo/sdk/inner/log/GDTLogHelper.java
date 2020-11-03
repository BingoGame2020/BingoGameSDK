package com.bingo.sdk.inner.log;

import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.ActionUtils;
import com.qq.gdt.action.GDTAction;

/**
 * 广点通
 */
public class GDTLogHelper {
    /**
     * 游戏启动
     */
    public static void appStart() {
        GDTAction.logAction(ActionType.START_APP);
    }

    /**
     * 注册
     *
     * @param method  注册方式
     * @param success 是否成功
     */
    public static void onRegister(String method, boolean success) {
        ActionUtils.onRegister(method, success);
    }

    public static void onCreateRole(String role) {
        ActionUtils.onCreateRole(role);
    }

    public static void onUpdateRole(int level) {
        ActionUtils.onUpdateLevel(level);
    }

    /**
     * @param type                商品类型如"装备"、"皮肤"
     * @param name                商品名称
     * @param id                  商品标识符
     * @param number              商品数量
     * @param isVirtualCurrency   是否使用虚拟货币
     * @param virtualCurrencyType 虚拟货币类型，如"钻石"、"金币"等
     * @param success             提交购买/下单是否成功
     */
    public static void onCheckout(String type, String name, String id, int number, boolean isVirtualCurrency, String virtualCurrencyType, String currency, boolean success) {
        ActionUtils.onCheckout(type, name, id, number, isVirtualCurrency, virtualCurrencyType, currency, success);
    }

    /**
     * @param type     商品类型如"装备"、"皮肤"
     * @param name     商品名称
     * @param id       商品标识符
     * @param number   商品数量
     * @param channel  支付渠道名，如支付宝、微信等
     * @param currency 真实货币类型，ISO 4217代码，如："CNY"
     * @param value    本次支付的真实货币的金额，单位分
     * @param success  支付是否成功
     */
    public static void onPurchase(String type, String name, String id, int number, String channel, String currency, int value, boolean success) {
        ActionUtils.onPurchase(type, name, id, number, channel, currency, value, success);

    }


    public static void onLogin(String method, boolean success) {
        ActionUtils.onLogin(method, success);
    }
}

