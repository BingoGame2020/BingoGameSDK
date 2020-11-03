//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bingo.sdk.inner.encrypt;

import java.security.MessageDigest;

public class EncryptUtil {
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encodeByMD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);

        for (int j = 0; j < len; ++j) {
            buf.append(HEX_DIGITS[bytes[j] >> 4 & 15]);
            buf.append(HEX_DIGITS[bytes[j] & 15]);
        }

        return buf.toString();
    }

    /**
     * 生成最终需要的key
     *
     * @param key
     * @return
     */
    public static String filterKey(String key) {
        StringBuilder tempKey = new StringBuilder(key);

        while (tempKey.length() < 32) {
            tempKey.append(key);
        }

        StringBuilder newKey = new StringBuilder();

        int half = tempKey.length() / 2;

        //前一半取奇数位
        //后一半取偶数位
        for (int i = 0; i < half; i++) {
            if (newKey.length() < 8) {
                if (i % 2 != 0) {
                    newKey.append(tempKey.charAt(i));
                }
            }
        }
        for (int i = half; i < tempKey.length(); i++) {
            if (newKey.length() < 16) {
                if (i % 2 == 0) {
                    newKey.append(tempKey.charAt(i));
                }
            }
        }

        return newKey.toString();
    }
}
