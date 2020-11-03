//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bingo.sdk.inner.encrypt.aes;

import com.bingo.sdk.inner.encrypt.Base64Util;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
    private static final int AES_IV_LENGTH = 16;
    private static String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";


    public static String encrypt(String data, String key) {
        try {
            if (key != null && key.length() != 0) {
                byte[] keyByte = key.getBytes();
                if (keyByte.length != 16 && keyByte.length != 32) {
                    throw new RuntimeException("aes key should be 16 bytes(128bits) or 32 bytes(256bits)");
                } else {
                    byte[] iv = generateIV(keyByte);
                    Cipher aesCipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
                    aesCipher.init(
                            Cipher.ENCRYPT_MODE, new SecretKeySpec(keyByte, "AES"), new IvParameterSpec(iv, 0, iv.length));
                    byte[] dataByte = data.getBytes();
                    byte[] encryptByte = aesCipher.doFinal(dataByte, 0, dataByte.length);
                    return Base64Util.encodeBase64(encryptByte);
                }
            } else {
                throw new RuntimeException("aes key should not be empty");
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param data
     * @param key
     * @return
     */
    public static String decrypt(String  data, String key) {
        try {
            byte[] buf = Base64Util.decodeBase64(data);
            if (key != null && key.length() != 0) {
                byte[] keyByte = key.getBytes();
                if (keyByte.length != 16 && keyByte.length != 32) {
                    throw new RuntimeException("aes key should be 16 bytes(128bits) or 32 bytes(256bits)");
                } else {
                    byte[] iv = generateIV(keyByte);
                    Cipher aesCipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
                    aesCipher.init(
                            Cipher.DECRYPT_MODE, new SecretKeySpec(keyByte, "AES"), new IvParameterSpec(iv, 0, iv.length));
                    byte[] bytes = aesCipher.doFinal(buf, 0, buf.length);
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            } else {
                throw new RuntimeException("aes key should not be empty");
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            return "";
        }
    }

    private static byte[] generateIV(byte[] aesKey) {
        byte[] ivBytes = new byte[AES_IV_LENGTH];
        int j = 0;

        for(int i = aesKey.length - 1; i >= 0 && j < AES_IV_LENGTH; ++j) {
            ivBytes[j] = aesKey[i];
            --i;
        }

        while(j < AES_IV_LENGTH) {
            ivBytes[j] = 1;
            ++j;
        }

        return ivBytes;
    }
}
