package com.bingo.sdk.inner.util;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 字节转换工具类
 */
public class ByteUtils {


    private static final String TAG = "ByteUtils";

    /**
     * 文件转成十六进制
     *
     * @param filePath 文件路径
     */
    public static String fileToHex(String filePath) {
        Log.d(TAG, filePath + "文件转Hex开始");
        String hex = null;
        FileInputStream fis = null;
        java.io.ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(filePath);
            bos = new java.io.ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int read = 1024;
            int readSize = 1024;
            while (read == readSize) {
                read = fis.read(buffer, 0, readSize);
                bos.write(buffer, 0, read);
            }

            byte[] result = bos.toByteArray();
            Log.d(TAG, "转为Hex字节长度为：" + result.length);
            // 字节数组转成十六进制
            hex = byte2HexStr(result);
            Log.d(TAG, "hex：" + hex);
            Log.d(TAG, "hex长度为：" + hex.length());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hex;
    }

    /**
     * 字节数组 -> 十六进制的转换，此方法高效
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {
        Log.d(TAG, "byte2HexStr...start" + b.length);
        StringBuilder stringBuilder = new StringBuilder(b.length);
        for (byte byteChar : b) {
            stringBuilder.append(String.format("%02X ", byteChar).trim());
        }
        Log.d(TAG, "byte2HexStr...end");
        return stringBuilder.toString();

    }


    /**
     * 将十六进制串转化为byte数组
     */
    public static final byte[] hex2byte(String hex) throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 将十六进制串转换为二进制
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将字符串转成ASCII值
     */
    public static String strToASCII(String data) {
        String requestStr = "";
        for (int i = 0; i < data.length(); i++) {
            char a = data.charAt(i);
            int aInt = (int) a;
            requestStr = requestStr + integerToHexString(aInt);
        }
        return requestStr;
    }

    /**
     * 将十进制整数转为十六进制数，并补位
     */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }

    /**
     * 将二进制转换成十六进制串
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将byte数组转换为16进制字符串
     * 实现思路：
     * 先将byte转换成int,再使用Integer.toHexString(int)
     *
     * @param data byte数组
     * @return
     */
    public static String byte2Hex(byte[] data, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            int tmp = data[i] & 0xff;
            String hv = Integer.toHexString(tmp);
            if (hv.length() < 2) {
                builder.append("0");
            }
            builder.append(hv);
            /*builder.append(" ");*/
            if (i % 16 == 15) {
                /*builder.append("\n");*/
            }
        }
        return builder.toString();
    }

    /**
     * 计算指定字符串（这里要求是字符）的16进制所表示的数字
     *
     * @param str
     * @return
     */
    public static int indexOf(String str) {
        return "0123456789ABCDEF".indexOf(str);
    }

    /**
     * 计算byte数组所表示的值，字节数组的值以大端表示，低位在高索引上，高位在低索引
     * <br/>
     * 例：data = {1,2},那么结果为: 1 << 8 + 2 = 258
     *
     * @param data byte数组
     * @return 计算出的值
     */
    public static long highByteToLong(byte[] data) {
        long sum = 0;
        for (int i = 0; i < data.length; i++) {
            long value = ((data[i] & 0xff) << (8 * (data.length - i - 1)));
            sum += value;
        }
        return sum;
    }

    /**
     * 计算byte数组所表示的值，字节数组的值以小端表示，低位在低索引上，高位在高索引
     * 例：data = {1,2},那么结果为: 2 << 8 + 1 = 513
     *
     * @param data byte数组
     * @return 计算出的值
     */
    public static int lowByteToInt(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            long value = ((data[i] & 0xff) << (8 * i));
            sum += value;
        }
        return sum;
    }

    /**
     * 计算byte数组所表示的值，字节数组的值以大端表示，低位在高索引上，高位在低索引
     * 例：data = {1,2},那么结果为: 1 << 8 + 2 = 258
     *
     * @param data byte数组
     * @return 计算出的值
     */
    public static int highByteToInt(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            long value = ((data[i] & 0xff) << (8 * (data.length - i - 1)));
            sum += value;
        }
        return sum;
    }

    /**
     * long值转换为指定长度的小端字节数组
     *
     * @param data long值
     * @param len  长度
     * @return 字节数组, 小端形式展示
     */
    public static byte[] longToLowByte(long data, int len) {
        byte[] value = new byte[len];
        for (int i = 0; i < len; i++) {
            value[i] = (byte) ((data >> (8 * i)) & 0xff);
        }
        return value;
    }

    /**
     * long值转换为指定长度的大端字节数组
     *
     * @param data long值
     * @param len  长度
     * @return 字节数组, 大端形式展示
     */
    public static byte[] longToHighByte(long data, int len) {
        byte[] value = new byte[len];
        for (int i = 0; i < len; i++) {
            value[i] = (byte) ((data >> (8 * (len - 1 - i))) & 0xff);
        }
        return value;
    }

    /**
     * int值转换为指定长度的小端字节数组
     *
     * @param data int值
     * @param len  长度
     * @return 字节数组, 小端形式展示
     */
    public static byte[] intToLowByte(int data, int len) {
        byte[] value = new byte[len];
        for (int i = 0; i < len; i++) {
            value[i] = (byte) ((data >> (8 * i)) & 0xff);
        }
        return value;
    }

    /**
     * int值转换为指定长度的大端字节数组
     *
     * @param data int值
     * @param len  长度
     * @return 字节数组, 大端形式展示
     */
    public static byte[] intToHighByte(int data, int len) {
        byte[] value = new byte[len];
        for (int i = 0; i < len; i++) {
            value[i] = (byte) ((data >> (8 * (len - 1 - i))) & 0xff);
        }
        return value;
    }

    /**
     * 计算base的exponent次方
     *
     * @param base     基数
     * @param exponent 指数
     * @return
     */
    public static long power(int base, int exponent) {
        long sum = 1;
        for (int i = 0; i < exponent; i++) {
            sum *= base;
        }
        return sum;
    }

    /**
     * 裁剪字节数据，获取指定开始位置（0开始）后的第个len字节
     *
     * @param data  原来的字节数组
     * @param start 开始位置
     * @param len   长度
     * @return 裁剪后的字节数组
     */
    public static byte[] cutByte(byte[] data, int start, int len) {
        byte[] value = null;
        do {
            if (len + start > data.length || start < 0 || len <= 0) {
                break;
            }
            value = new byte[len];
            for (int i = 0; i < len; i++) {
                value[i] = data[start + i];
            }
        } while (false);

        return value;
    }

}
