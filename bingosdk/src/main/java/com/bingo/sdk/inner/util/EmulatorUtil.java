package com.bingo.sdk.inner.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.bingo.sdk.inner.interf.CommonCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EmulatorUtil {

    public static boolean isEmulator(Context context, CommonCallback callback) {
        StringBuilder builder = new StringBuilder();
        boolean hasLightSensor = hasLightSensor(context, builder);
        boolean isKnownEmulator = isKnownEmulator(context, builder);
        boolean cpu = isPcCpu(builder);
        boolean hasBluetooth = hasBluetoothFeature(context);
        builder.append("hasLightSensor: ").append(hasLightSensor).append("\n");
        builder.append("isKnownEmulator: ").append(isKnownEmulator).append("\n");
        builder.append("cpu: ").append(cpu).append("\n");
        builder.append("hasBluetooth: ").append(hasBluetooth).append("\n");
        if (callback != null)
            callback.onCallBack(-1, builder.toString());
        return !hasLightSensor || isKnownEmulator || cpu || !hasBluetooth;
    }

    /**
     * 是否有光线传感器
     * <br/>
     * 如果有光线传感器,一般都是真机
     *
     * @param context context
     * @param builder
     * @return true->有; false->没有
     */
    private static boolean hasLightSensor(Context context, StringBuilder builder) {
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        LogUtil.e("光线传感器: " + sensor);
        builder.append("light sensor: ").append(sensor);
        return sensor != null;
    }

    private static boolean isKnownEmulator(Context context, StringBuilder builder) {

        boolean generic = Build.FINGERPRINT.startsWith("generic");
        builder.append("Build.FINGERPRINT: ").append(Build.FINGERPRINT).append("\n");
        boolean vbox = Build.FINGERPRINT.toLowerCase().contains("vbox");
        boolean testKeys = Build.FINGERPRINT.toLowerCase().contains("test-keys");
        builder.append("Build.MODEL: ").append(Build.MODEL).append("\n");
        boolean googleSdk = Build.MODEL.contains("google_sdk");
        boolean emulator = Build.MODEL.contains("Emulator");
        boolean muMu = Build.MODEL.toLowerCase().contains("mumu");
        boolean virtual = Build.MODEL.toLowerCase().contains("virtual");
        boolean android = Build.SERIAL.equalsIgnoreCase("android");
        builder.append("Build.SERIAL: ").append(Build.SERIAL).append("\n");
        boolean genymotion = Build.MANUFACTURER.contains("Genymotion");
        builder.append("Build.MANUFACTURER: ").append(Build.MANUFACTURER).append("\n");
        builder.append("Build.BRAND: ").append(Build.BRAND).append("\n");
        builder.append("Build.PRODUCT: ").append(Build.PRODUCT).append("\n");
        boolean isGeneric = Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic");
        boolean googleProduct = "google_sdk".equals(Build.PRODUCT);

        TelephonyManager manager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        boolean isAndroidOper = false;
        if (manager != null) {
            String operatorName = manager.getNetworkOperatorName().toLowerCase();
            isAndroidOper = operatorName.equals("android");
            builder.append("NetworkOperatorName: ").append(operatorName).append("\n");

        }


        return generic || vbox || testKeys || googleSdk || emulator || muMu || virtual || android || genymotion || isGeneric || googleProduct || isAndroidOper;
    }


    /**
     * 如果cpu信息包含intel或者amd则为模拟器;
     * intel cpu的手机很少,仅有几款
     * 雷电模拟器的cpu信息会包含placeholder
     *
     * @param builder
     * @return
     */
    private static boolean isPcCpu(StringBuilder builder) {
        String cpuInfo = readCpuInfo();
        builder.append("cpuInfo: ").append(cpuInfo).append("\n");
        return cpuInfo.contains("intel") || cpuInfo.contains("amd") || cpuInfo.contains("placeholder");
    }


    /*
     *根据CPU是否为电脑来判断是否为模拟器(子方法)
     *返回:String
     */
    private static String readCpuInfo() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine).append("\n");
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (IOException ex) {
        }
        return result;
    }

    private static boolean hasBluetoothFeature(Context context) {
        boolean bluetooth = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        LogUtil.e("是否包含蓝牙: " + bluetooth);
        return bluetooth;
    }
}
