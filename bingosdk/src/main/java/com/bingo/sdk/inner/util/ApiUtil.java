package com.bingo.sdk.inner.util;

import android.content.Context;

import com.bingo.sdk.bean.RechargeOptions;
import com.bingo.sdk.bean.RoleEventOptions;
import com.bingo.sdk.bean.RoleEventType;
import com.bingo.sdk.inner.channel.ChannelConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 主要用于封装一些和接口相关的获取参数的方法
 */
public class ApiUtil {
    public static JSONObject getStartReportData(Context context) {

        String serialNumber = DeviceUtil.getSerialNumber();
        String mac = DeviceUtil.getMacAddress(context);
        String imei = DeviceUtil.getImei(context);
        String imei2 = DeviceUtil.getImei2(context);
        String processName = DeviceUtil.getProcessName(context);
        String deviceCompany = DeviceUtil.getDeviceCompany();
        String model = DeviceUtil.getDeviceModel();
        String brand = DeviceUtil.getDeviceBrand();
        String appName = DeviceUtil.getAppName(context);
        String appVersion = DeviceUtil.getAppVersion(context);
        int height = DeviceUtil.getDisplayHeight(context);
        int width = DeviceUtil.getDisplayWidth(context);
        String localIp = DeviceUtil.getLocalIp(context);
        String wifiMac = DeviceUtil.getWifiMac(context);
        String wifiName = DeviceUtil.getWifiName(context);
        String oaid = DeviceUtil.getOaid(context);
        LogUtil.e("wifiName: " + wifiName);
        String timeZone = DeviceUtil.getTimeZone();
        String mainHost = DeviceUtil.getMainHost();
        int processId = DeviceUtil.getProcessId();
        String netType = DeviceUtil.getNetType(context);
        int deviceSdkInt = DeviceUtil.getDeviceSdkInt();
        String deviceVersion = DeviceUtil.getDeviceVersion();
        String sdkVersion = DeviceUtil.getSdkVersion();
        String packageName = DeviceUtil.getPackageName(context);

        Object pobj = BGSPUtil.get(context, BGSPUtil.KEY_PROVINCE);
        Object cobj = BGSPUtil.get(context, BGSPUtil.KEY_CITY);
        String province = pobj == null ? "" : pobj.toString();
        String city = cobj == null ? "" : cobj.toString();

        JSONObject json = new JSONObject();
        try {
            json.put("appNumber", MetaUtil.getInteger(context, ChannelConfig.GAME_ID));
            json.put("teamCompanyNumber", MetaUtil.getInteger(context, ChannelConfig.PARTNER_ID));
            json.put("channelNumber", MetaUtil.getInteger(context, ChannelConfig.CHANNEL_ID));
            json.put("number", MetaUtil.getInteger(context, ChannelConfig.DISPATCH_ID));
            json.put("deviceCode", serialNumber);
            json.put("mac", mac);
            json.put("imei", imei);
            json.put("imei2", imei2);
            json.put("oaid", oaid);
            json.put("deviceType", "Android");
            json.put("deviceCompany", deviceCompany);
            json.put("deviceModel", model);
            json.put("deviceWidth", width);
            json.put("deviceHeight", height);
            json.put("systemVersion", deviceVersion);
            json.put("appVersion", appVersion);
            json.put("sdkVersion", sdkVersion);
            json.put("processNumber", processId);
            json.put("processName", processName);
            json.put("packageName", packageName);
            json.put("mainHost", mainHost);
            json.put("gameName", appName);
            json.put("localNetIp", localIp);
            json.put("localMobile", "");//手机号码,不建议获取,直接填空
            json.put("netType", netType);
            json.put("wifiName", wifiName);
            json.put("wifiMac", wifiMac);
            json.put("timeZone", timeZone);
            json.put("provinces", province);
            json.put("city", city);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }


    public static JSONObject getBaseParams(Context context) {

        String timeZone = DeviceUtil.getTimeZone();
        String imei = DeviceUtil.getImei(context);
        Object pobj = BGSPUtil.get(context, BGSPUtil.KEY_PROVINCE);
        Object cobj = BGSPUtil.get(context, BGSPUtil.KEY_CITY);
        String deviceCode = DeviceUtil.getDeviceCode(context);
        String province = pobj == null ? "" : pobj.toString();
        String city = cobj == null ? "" : cobj.toString();
        JSONObject json = new JSONObject();
        try {
            json.put("appNumber", MetaUtil.getInteger(context, ChannelConfig.GAME_ID));
            json.put("teamCompanyNumber", MetaUtil.getInteger(context, ChannelConfig.PARTNER_ID));
            json.put("channelNumber", MetaUtil.getInteger(context, ChannelConfig.CHANNEL_ID));
            json.put("number", MetaUtil.getInteger(context, ChannelConfig.DISPATCH_ID));
            json.put("simulator", EmulatorUtil.isEmulator(context, null));
            json.put("deviceType", "Android");//注意首字母大写
            json.put("deviceCode", CommonUtil.filterNull(deviceCode));
            json.put("timeZone", timeZone);
            json.put("provinces", province);
            json.put("city", city);
            json.put("imei", imei);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.e("基本参数: " + json);
        return json;

    }

    public static JSONObject getPayOrderParams(Context context, RechargeOptions options) {
        JSONObject json = getBaseParams(context);
        try {
            json.put("doId", CommonUtil.filterNull(options.getOrderId()));
            json.put("dsId", CommonUtil.filterNull(options.getServerId()));
            json.put("dsName", CommonUtil.filterNull(options.getServerName()));
            json.put("drId", CommonUtil.filterNull(options.getRoleId()));
            json.put("drName", CommonUtil.filterNull(options.getRoleName()));
            json.put("drLevel", String.valueOf(options.getRoleLevel()));
            json.put("dext", CommonUtil.filterNull(options.getExt()));
            json.put("dradio", String.valueOf(options.getRatio()));
            json.put("dunit", CommonUtil.filterNull(options.getUnitName()));
//            json.put("rechargeType", payType);
            json.put("dmoney", options.getAmount());
            json.put("token", AccountUtil.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject getLogEventParams(Context context, RoleEventOptions options, int type) {
        JSONObject json = getBaseParams(context);
        try {
            json.put("dsId", CommonUtil.filterNull(options.getServerId()));
            json.put("dsName", CommonUtil.filterNull(options.getServerName()));
            json.put("drId", CommonUtil.filterNull(options.getRoleId()));
            json.put("drName", CommonUtil.filterNull(options.getRoleName()));
            json.put("drLevel", String.valueOf(options.getRoleLevel()));
            json.put("drBalance", String.valueOf(options.getBalance()));
            json.put("drVip", String.valueOf(options.getVip()));
            json.put("dCountry", String.valueOf(options.getCountry()));
            json.put("dParty", String.valueOf(options.getParty()));
            json.put("roleCtime", String.valueOf(options.getRoleCreateTime()));
            json.put("roleLevelTime", String.valueOf(options.getRoleLevelUpTime()));
            if (type == RoleEventType.ROLE_CREATE)
                json.put("eId", 31);
            else if (type == RoleEventType.ROLE_LOGIN)
                json.put("eId", 32);
            else if (type == RoleEventType.ROLE_UPGRADE) {
                json.put("eId", 35);
            }
            json.put("dext", CommonUtil.filterNull(options.getExt()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject getUpdateParams(Context context) {
        int gameId = MetaUtil.getInteger(context, ChannelConfig.GAME_ID);
        int partnerId = MetaUtil.getInteger(context, ChannelConfig.PARTNER_ID);
        int sdkInt = DeviceUtil.getDeviceSdkInt();
        long gameCode = DeviceUtil.getAppVersionCode(context);

        JSONObject json = new JSONObject();
        try {
            json.put("appNumber", gameId);
            json.put("teamCompanyNumber", partnerId);
            json.put("sdkVersionName", sdkInt);
            json.put("gameVersionName", gameCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
