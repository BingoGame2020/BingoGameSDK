package com.bingo.sdk.inner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.bingo.sdk.inner.bean.NetInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtil {

    private static final String NET_TYPE_2G = "2G";
    private static final String NET_TYPE_3G = "3G";
    private static final String NET_TYPE_4G = "4G";
    private static final String NET_TYPE_WIFI = "Wifi";
    private static final String NET_TYPE_UNKNOWN = "Unknown";
    private static final String DEFAULT_GATE_WAY = "0.0.0.0";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String typeName = networkInfo.getTypeName();
            LogUtil.i("network is available: " + typeName);
            LogUtil.i("network is available subType: " + networkInfo.getSubtypeName());
            return true;
        } else {
            LogUtil.i("network  is not available");
            return false;
        }
    }


    public static String getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo.State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            NetworkInfo.State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            // 如果3G、wifi、2G等网络状态是连接的，则退出，否则显示提示信息进入网络设置界面
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                NetworkInfo info = manager.getActiveNetworkInfo();
                int type = info.getSubtype();
                switch (type) {
                    //2G类型
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                    case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                    case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NET_TYPE_2G;
                    //如果是3g类型
                    case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NET_TYPE_3G;
                    //如果是4g类型
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NET_TYPE_4G;
                    default:
                        //中国移动 联通 电信 三种3G制式
                        String strSubTypeName = info.getSubtypeName();
                        if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            return NET_TYPE_3G;
                        } else {
                            return NET_TYPE_UNKNOWN;
                        }
                }
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                return NET_TYPE_WIFI;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NET_TYPE_UNKNOWN;
    }


    public static NetInfo getNetworkInfo(Context context) {
        NetInfo netInfo = getSimOperator(context);
        String networkName = getNetworkType(context);
        netInfo.setConnect(isNetworkAvailable(context));
        netInfo.setNetworkType(networkName);
        netInfo.setIp(getLocalIp(context));
        netInfo.setDns(getDns(context));
        netInfo.setGateWay(getGateWay(context));
        return netInfo;
    }

    public static String getGateWay(Context context) {
        String type = getNetworkType(context);
        String gateWay = DEFAULT_GATE_WAY;
        if (type.equals(NET_TYPE_2G) || type.equals(NET_TYPE_3G) || type.equals(NET_TYPE_4G)) {
            gateWay = getMobileGateWay(context);
        } else if (type.equals(NET_TYPE_WIFI)) {
            gateWay = getWifiGateWay(context);
        }

        return gateWay;
    }

    public static String getWifiGateWay(Context context) {
        String gateWay = "";
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            DhcpInfo dhcpInfo = manager.getDhcpInfo();
            gateWay = intToIp(dhcpInfo.gateway);
        }
        return gateWay;
    }

    public static List<String> getDns(Context context) {
        String type = getNetworkType(context);
        List<String> dnsList = new ArrayList<>();

        if (type.equals(NET_TYPE_2G) || type.equals(NET_TYPE_3G) || type.equals(NET_TYPE_4G)) {
            List<String> mobileDNS = getMobileDNS(context);
            dnsList.addAll(mobileDNS);
        } else if (type.equals(NET_TYPE_WIFI)) {
            List<String> wifiDNS = getWifiDNS(context);
            dnsList.addAll(wifiDNS);
        }

        return dnsList;
    }

    public static List<String> getWifiDNS(Context context) {
        List<String> dnsList = new ArrayList<>();
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            DhcpInfo dhcpInfo = manager.getDhcpInfo();
            String dns1 = intToIp(dhcpInfo.dns1);
            String dns2 = intToIp(dhcpInfo.dns2);
            dnsList.add(CommonUtil.filterNull(dns1));
            dnsList.add(CommonUtil.filterNull(dns2));
        }
        return dnsList;
    }

    public static List<String> getMobileDNS(Context context) {

        List<String> dnsList = new ArrayList<>();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] allNetworks = manager.getAllNetworks();
            if (allNetworks == null) {
                return dnsList;
            }
            for (int i = 0; i < allNetworks.length; i++) {
                Network network = allNetworks[i];
                if (network == null) {
                    return dnsList;
                }
                LinkProperties properties = manager.getLinkProperties(network);
                if (properties == null)
                    return dnsList;
                List<InetAddress> dnsServers = properties.getDnsServers();
                if (dnsServers == null) {
                    return dnsList;
                }
                for (InetAddress address : dnsServers) {
                    if (!address.isLoopbackAddress()) {
                        LogUtil.i("dns服务器: " + address.getHostAddress());
                        dnsList.add(address.getHostAddress());
                    }
                }
            }
        }
        return dnsList;
    }

    public static String getMobileGateWay(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] allNetworks = manager.getAllNetworks();
            for (Network network : allNetworks) {
                if (network == null) {
                    return DEFAULT_GATE_WAY;
                }
                LinkProperties properties = manager.getLinkProperties(network);
                if (properties == null)
                    return DEFAULT_GATE_WAY;
                List<RouteInfo> routeInfos = properties.getRoutes();
                for (RouteInfo routeInfo : routeInfos) {
                    InetAddress inetAddress = routeInfo.getGateway();
                    if (inetAddress != null && !inetAddress.isLoopbackAddress()
                            && !inetAddress.isAnyLocalAddress() && !inetAddress.isLinkLocalAddress()) {
                        LogUtil.i("网关信息: " + inetAddress.getHostAddress());
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        return DEFAULT_GATE_WAY;
    }

    public static String getLocalIp(Context context) {
        String type = getNetworkType(context);
        String ip;
        if (type.equals(NET_TYPE_2G) || type.equals(NET_TYPE_3G) || type.equals(NET_TYPE_4G)) {
            ip = getMobileIP();
        } else if (type.equals(NET_TYPE_WIFI)) {
            ip = getWifiIP(context);
        } else {
            ip = "0.0.0.0";
        }
        return CommonUtil.filterNull(ip);
    }

    public static String getWifiIP(Context context) {
        String ip = "0.0.0.0";
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            DhcpInfo dhcpInfo = manager.getDhcpInfo();
            ip = intToIp(dhcpInfo.ipAddress);
        }

        return ip;
    }

    public static String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            return wifiInfo.getSSID();
        }

        return "";
    }

    public static String getMobileIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    if (inetAddresses != null) {
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress address = inetAddresses.nextElement();
                            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                                String hostAddress = address.getHostAddress();
                                LogUtil.i("本机IP: " + hostAddress);
                                return hostAddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return "0.0.0.0";
        }
        return "0.0.0.0";
    }

    private static NetInfo getSimOperator(Context context) {
        NetInfo info = new NetInfo();
        try {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telephonyManager.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                String operator = telephonyManager.getSimOperator();//获取MCC+MNC码  5-6位   mcc前3位
                info.setSimOperator(operator);
            }
            info.setSimState(simState);
            String networkCountryIso = telephonyManager.getNetworkCountryIso();
            info.setCountryISO(networkCountryIso);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (paramInt >> 8 & 0xFF) + "." + (paramInt >> 16 & 0xFF) + "." + (paramInt >> 24 & 0xFF);
    }

}