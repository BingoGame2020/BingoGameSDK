

package com.bingo.sdk.inner.encrypt;

public class Base64Util {
    public Base64Util() {
    }

    public static byte[] decodeBase64(String str) {
        return Base64.decode(str, 0);
    }

    public static String encodeBase64(byte[] buf) {
        //去掉末尾的换行符
        return Base64.encodeToString(buf, 0).replaceAll("[\\s*\t\n\r]", "");
    }
}
