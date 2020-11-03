package com.bingo.sdk.inner.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.content.PermissionChecker;

import com.bingo.sdk.BuildConfig;
import com.bingo.sdk.inner.encrypt.EncryptUtil;
import com.bingo.sdk.mediastore.DeviceIdImageUtils;
import com.bingo.sdk.web.ApiConfig;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DeviceUtil {
    /**
     * 运营商,1、移动；2、联通；3、电信；4、其他
     */
    public static String getOper(Context context) {
        try {
            TelephonyManager iPhoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String iNumeric = iPhoneManager.getSimOperator();
            if (iNumeric.length() > 0) {
                return iNumeric;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "4";
    }


    /**
     * @return 系统版本号(如10.0.1)
     */
    public static String getDeviceVersion() {
        try {
            return android.os.Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获得手机MAC
     *
     * @param context context
     * @return mac
     */
    public static String getMacAddress(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return getMacFromWifiInfo(context);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return getMacFromFile();
        } else {
            return getMachineHardwareAddress();
        }
    }

    /**
     * 6.0以上 7.0 以下获取mac
     *
     * @return mac
     */
    private static String getMacFromFile() {
        String str = "";
        String macSerial = "";
        try {
            java.lang.Process process = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            LogUtil.e("NetInfoManager getMacAddress:" + ex.toString());
        }
        if ("".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("NetInfoManager getMacAddress:" + e.toString());
            }

        }
        return macSerial;
    }

    /**
     * 6.0以下获取mac
     *
     * @param context context
     * @return mac
     */
    private static String getMacFromWifiInfo(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 7.0以上获取mac
     * 获取设备HardwareAddress地址
     *
     * @return mac
     */
    private static String getMachineHardwareAddress() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String hardWareAddress = null;
        NetworkInterface iF = null;
        if (interfaces == null) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                if (!iF.getName().equalsIgnoreCase("wlan0")) continue;
                hardWareAddress = bytesToString(iF.getHardwareAddress());
                if (hardWareAddress != null)
                    break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        LogUtil.e("mac: " + hardWareAddress);
        return hardWareAddress;

    }


    /***
     * byte转为String
     *
     * @param bytes byte[]
     * @return string
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }


    /**
     * @return 应用名称
     */
    public static String getAppName(Context context) {
        String name = "";
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            name = context.getResources().getString(labelRes);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }


    public static String getSdkVersion() {
        return BuildConfig.SDK_VERSION;
    }

    public static long getAppVersionCode(Context context) {
        int code = 0;
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return packageInfo.getLongVersionCode();
            } else {
                return packageInfo.versionCode;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;

    }

    public static String getSerialNumber() {
        String serial = "";
        try {
            serial = Build.getSerial();
        } catch (Throwable e) {
            LogUtil.e("获取serial失败," + e.getMessage());
        }
        return CommonUtil.filterNull(serial);

    }

    public static String getImei(Context context) {
        String imei = "";
        try {
            TelephonyManager iPhoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = iPhoneManager.getImei(0);
        } catch (Exception e) {
            LogUtil.e("获取Imei失败," + e.getMessage());
        }
        return CommonUtil.filterNull(imei);
    }

    public static String getImei2(Context context) {
        String imei = "";
        try {
            TelephonyManager iPhoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = iPhoneManager.getImei(1);
        } catch (Exception e) {
            LogUtil.e("获取Imei失败," + e.getMessage());
        }
        return CommonUtil.filterNull(imei);
    }

    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo info : processes) {
                if (info.pid == Process.myPid()) {
                    return info.processName;
                }
            }
        }
        return "";
    }

    /**
     * @return 设备厂商
     */
    public static String getDeviceCompany() {
        return Build.MANUFACTURER;
    }

    /**
     * @return 设备型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * @return 设备品牌
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * @param context context
     * @return 屏幕高度(像素)
     */
    public static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * @param context context
     * @return 屏幕宽度(像素)
     */
    public static int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDeviceSdkInt() {
        return Build.VERSION.SDK_INT;
    }

    public static String getAppVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getProcessId() {
        return Process.myPid();
    }

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }


    public static String getMainHost() {
        String host = "";
        if (ApiConfig.HOST.contains("http") || ApiConfig.HOST.contains("https")) {
            host = ApiConfig.HOST.substring(ApiConfig.HOST.indexOf("//") + 2);
            LogUtil.e("截取后的域名: " + host);
            return host;
        }
        return ApiConfig.HOST;
    }

    public static String getLocalIp(Context context) {
        return NetUtil.getLocalIp(context);
    }

    public static String getMobileNumber(Context context) {
        //todo 待补充
        return null;
    }

    public static String getNetType(Context context) {
        return NetUtil.getNetworkType(context);
    }

    public static String getWifiName(Context context) {
        String wifiName = NetUtil.getWifiName(context);
        if (wifiName.startsWith("\"")) {
            wifiName = wifiName.substring(1);
        }
        if (wifiName.endsWith("\"")) {
            wifiName = wifiName.substring(0, wifiName.length() - 1);
        }
        return wifiName;
    }

    public static String getWifiMac(Context context) {
        return getMacAddress(context);
    }

    public static String getTimeZone() {
        //这句获取到的是 China Standard Time
//        return Calendar.getInstance().getTimeZone().getDisplayName();
        String pattern = "ZZZZ";
        //这句获取到的是 GMT+08:00
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(System.currentTimeMillis());
    }


    public static String getOaid(Context context) {
        Object o = BGSPUtil.get(context, BGSPUtil.KEY_OAID);
        if (o == null)
            return "";
        return o.toString();
    }


    public static String getDeviceCode(Context context) {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_DENIED) {
            //如果没有读写权限,读取不到公共目录的文件,如果再继续往下执行,就会生成一个新的code
            //为了确保卸载后还能读取到文件,只有在获取到读写权限之后再生成
            return "";
        }
        Object obj = BGSPUtil.get(context, BGSPUtil.KEY_DEVICE_CODE);
        String spCode = obj != null ? obj.toString() : "";
        LogUtil.i("从sp获取code: " + spCode);
        if (TextUtils.isEmpty(spCode)) {
            //sp中没有读取到,从公共目录读取
            String externalCode = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                externalCode = DeviceIdImageUtils.parseDeviceCodeByLastModifyImage(context);
            } else {
                externalCode = FileUtil.getDeviceCodeFromFile();
            }
            LogUtil.i("从外部获取code: " + externalCode);
            if (TextUtils.isEmpty(externalCode)) {
                //公共目录也没有读取到,生成新的
                String newCode = generateNewCode();
                LogUtil.e("生成新code: " + newCode);
                BGSPUtil.save(context, BGSPUtil.KEY_DEVICE_CODE, newCode);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DeviceIdImageUtils.syncDeviceCode2Public(context, newCode);
                } else {
                    FileUtil.saveDeviceCodeToFile(newCode);
                }
                return newCode;
            } else {
                BGSPUtil.save(context, BGSPUtil.KEY_DEVICE_CODE, externalCode);
                return externalCode;
            }
        } else {
            //sp中读取到了
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                DeviceIdImageUtils.syncDeviceCode2Public(context, spCode);
            } else {
                FileUtil.saveDeviceCodeToFile(spCode);
            }
            return spCode;
        }
    }

    private static String generateNewCode() {
        UUID uuid = UUID.randomUUID();
        return EncryptUtil.encodeByMD5(uuid.toString());
    }


}
