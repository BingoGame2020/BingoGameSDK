package com.bingo.sdk.web;

import com.bingo.sdk.inner.encrypt.Base64Util;
import com.bingo.sdk.inner.encrypt.aes.AesUtil;
import com.bingo.sdk.inner.encrypt.rsa.RSAUtil;

import org.json.JSONObject;

public class ResponseDecrypt {
    public static String decrypt(JSONObject json) throws Exception {
        String mask = json.optString("k");//解密key
//        LogUtil.e("需要解密的aesKey: " + mask);
        byte[] aesKeyByte = Base64Util.decodeBase64(mask);
        String decryptKey = RSAUtil.decryptByPublicKey(aesKeyByte);
//        LogUtil.e("解密后的AesKey: " + decryptKey);
        String content = json.optString("v");
//        LogUtil.e("需要解密的内容: " + content);
//        byte[] base64 = Base64Util.decodeBase64(content);
        String decryptContent = AesUtil.decrypt(content, decryptKey);
//        LogUtil.e("解密后的内容: " + decryptContent);
        return decryptContent;
    }
}
