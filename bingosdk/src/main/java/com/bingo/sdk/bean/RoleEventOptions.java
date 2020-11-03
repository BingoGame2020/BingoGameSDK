package com.bingo.sdk.bean;

public class RoleEventOptions {
    private String serverId;
    private String serverName;
    private String roleName;
    private String roleId;
    private int roleLevel;
    private long balance;
    private String vip;//会员
    private String country;//帮派
    private String party;//工会
    private String roleCreateTime;//角色创建时间
    private String roleLevelUpTime;//角色升级时间
    private String ext;


    public String getServerId() {
        return serverId;
    }

    public RoleEventOptions setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public RoleEventOptions setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public RoleEventOptions setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public RoleEventOptions setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public RoleEventOptions setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
        return this;
    }

    public long getBalance() {
        return balance;
    }

    public RoleEventOptions setBalance(long balance) {
        this.balance = balance;
        return this;
    }

    public String getVip() {
        return vip;
    }

    public RoleEventOptions setVip(String vip) {
        this.vip = vip;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public RoleEventOptions setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getParty() {
        return party;
    }

    public RoleEventOptions setParty(String party) {
        this.party = party;
        return this;
    }

    public String getRoleCreateTime() {
        return roleCreateTime;
    }

    public RoleEventOptions setRoleCreateTime(String roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
        return this;
    }

    public String getRoleLevelUpTime() {
        return roleLevelUpTime;
    }

    public RoleEventOptions setRoleLevelUpTime(String roleLevelUpTime) {
        this.roleLevelUpTime = roleLevelUpTime;
        return this;
    }

    public String getExt() {
        return ext;
    }

    public RoleEventOptions setExt(String ext) {
        this.ext = ext;
        return this;
    }

    @Override
    public String toString() {
        return "RoleEventOptions{" +
                "serverId='" + serverId + '\'' +
                ", serverName='" + serverName + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleId='" + roleId + '\'' +
                ", roleLevel=" + roleLevel +
                ", balance=" + balance +
                ", vip='" + vip + '\'' +
                ", country='" + country + '\'' +
                ", party='" + party + '\'' +
                ", roleCreateTime='" + roleCreateTime + '\'' +
                ", roleLevelUpTime='" + roleLevelUpTime + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }
}
