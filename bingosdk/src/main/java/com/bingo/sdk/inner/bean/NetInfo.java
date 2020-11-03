package com.bingo.sdk.inner.bean;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetInfo {


    private String[] array = new String[]{"SIM_STATE_UNKNOWN", "SIM_STATE_ABSENT", "SIM_STATE_PIN_REQUIRED", "SIM_STATE_PUK_REQUIRED"
            , "SIM_STATE_NETWORK_LOCKED", "SIM_STATE_READY", "SIM_STATE_NOT_READY", "SIM_STATE_PERM_DISABLED", "SIM_STATE_CARD_IO_ERROR", "SIM_STATE_CARD_RESTRICTED"};


    /**
     * sim卡运营商号(MCC + MNC)
     */
    private String simOperator;
    private String countryISO;
    private String networkType;
    private boolean isConnect;
    private int simState;
    private String simStateString;
    private String ip;
    private List<String> dns = new ArrayList<>();
    private String gateWay;

    public String getSimOperator() {
        return simOperator;
    }

    public NetInfo setSimOperator(String simOperator) {
        this.simOperator = simOperator;
        return this;
    }

    public String getCountryISO() {
        return countryISO;
    }

    public NetInfo setCountryISO(String countryISO) {
        this.countryISO = countryISO;
        return this;
    }

    public String getNetworkType() {
        return networkType;
    }

    public NetInfo setNetworkType(String networkType) {
        this.networkType = networkType;
        return this;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public NetInfo setConnect(boolean connect) {
        isConnect = connect;
        return this;
    }

    public int getSimState() {
        return simState;
    }

    public NetInfo setSimState(int simState) {
        this.simState = simState;
        try {
            setSimStateString(array[simState]);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String getSimStateString() {
        return simStateString;
    }

    private void setSimStateString(String simStateString) {
        this.simStateString = simStateString;
    }

    public String getIp() {
        return ip;
    }

    public NetInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public List<String> getDns() {
        return dns;
    }

    public NetInfo setDns(List<String> dns) {
        this.dns.clear();
        if (dns != null)
            this.dns.addAll(dns);
        return this;
    }

    public String getGateWay() {
        return gateWay;
    }

    public NetInfo setGateWay(String gateWay) {
        this.gateWay = gateWay;
        return this;
    }

    @Override
    public String toString() {
        return "NetInfo{" +
                ", simOperator='" + simOperator + '\'' +
                ", countryISO='" + countryISO + '\'' +
                ", networkType='" + networkType + '\'' +
                ", isConnect=" + isConnect +
                ", simState=" + simState +
                ", simStateString='" + simStateString + '\'' +
                ", ip='" + ip + '\'' +
                ", dns='" + Arrays.toString(dns.toArray()) + '\'' +
                ", gateWay='" + gateWay + '\'' +
                '}';
    }
}
