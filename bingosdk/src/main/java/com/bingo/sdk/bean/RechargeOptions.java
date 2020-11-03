package com.bingo.sdk.bean;

import android.text.TextUtils;

public class RechargeOptions {
    /**
     * cp订单号
     */
    private String orderId;
    /**
     * cp服务器id
     */
    private String serverId;
    /**
     * cp服务器名称
     */
    private String serverName;
    /**
     * cp角色id
     */
    private String roleId;
    /**
     * cp角色名称
     */
    private String roleName;
    /**
     * cp角色等级
     */
    private int roleLevel;
    /**
     * 透传参数,原样返回给cp
     */
    private String ext;
    /**
     * 兑换比例,如1元兑换10元宝,则填10
     * //todo 这里填的值 有什么用? 写什么值
     */
    private int ratio;

    /**
     * 商品单位名称,如:元宝
     */
    private String unitName;
    /**
     * 商品购买数量,如1000元宝,则填写1000
     */
    private int unitNumber;
    /**
     * 充值金额,精确到分
     */
    private int amount;


    public String getOrderId() {
        return orderId;
    }

    public RechargeOptions setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public RechargeOptions setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public RechargeOptions setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public RechargeOptions setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public RechargeOptions setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public RechargeOptions setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
        return this;
    }

    public String getExt() {
        return ext;
    }

    public RechargeOptions setExt(String ext) {
        this.ext = ext;
        return this;
    }

    public int getRatio() {
        return ratio;
    }

    public RechargeOptions setRatio(int ratio) {
        this.ratio = ratio;
        return this;
    }

    public String getUnitName() {
        return unitName;
    }

    public RechargeOptions setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public RechargeOptions setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public RechargeOptions setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
        return this;
    }

    /**
     * 校验数据
     * <br/>
     * 做初步校验,订单号不能为空,金额大于0
     *
     * @return
     */
    public boolean isDataValid() {
        return !TextUtils.isEmpty(orderId) && amount > 0 && !TextUtils.isEmpty(unitName) && unitNumber > 0;
    }
}
