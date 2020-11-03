

package com.bingo.sdk.inner.encrypt.rsa;


import com.bingo.sdk.inner.encrypt.Base64Util;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtil {
    private static final String KEY_ALGORITHM = "RSA";
    private static final String RSA_PADDING = "RSA/ECB/PKCS1Padding";
    private static final String PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjo6TPIBt3KVqV9AkawzL97m36HuZAy2iwle6xtxtC6EqFozc1nu+mLRRMck6fqq4BdTSSYOnPOKfZwV4SU6ts7PlJq/ASXvdmUC8EXhXQuMYUJDIaVqGBu/M1IOJp4QMjUJ7aebEnLl/HSPmvxfTAAjnY6YZGX2hGXKs7/lH319hlJMC3Thb9vn07ksJlSWU0xnNuy/bjjvMvusSq2pV3+sdF47CqS7urC5WaoYZAhWCCbeaVdN/YA64cyGL2HhpVt6KraD9/JVB+xC5HMePPATk5xWQJ1xjEqITAJe2AJdl9LyoViqqzWpOyvrfZmElZ7NjGsISMRUM/nGdCFVpDwIDAQAB";

//    private static final String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCOjpM8gG3cpWpX0CRrDMv3ubfoe5kDLaLCV7rG3G0LoSoWjNzWe76YtFExyTp+qrgF1NJJg6c84p9nBXhJTq2zs+Umr8BJe92ZQLwReFdC4xhQkMhpWoYG78zUg4mnhAyNQntp5sScuX8dI+a/F9MACOdjphkZfaEZcqzv+UffX2GUkwLdOFv2+fTuSwmVJZTTGc27L9uOO8y+6xKralXf6x0XjsKpLu6sLlZqhhkCFYIJt5pV039gDrhzIYvYeGlW3oqtoP38lUH7ELkcx488BOTnFZAnXGMSohMAl7YAl2X0vKhWKqrNak7K+t9mYSVns2MawhIxFQz+cZ0IVWkPAgMBAAECggEAPUNAOwvmKqBbpwkn3WlOblM9mYckJp2CCIqzFaq8ofbMo4w9i176dZ1oF0EnBjBPjRH8nOMbB6tMXXWYT8ikFUoF/C4jMHEutiF/sPJKbdAyLs1Jkxz35BtBp0Iv8fFpg3tCliw56jA9VESNcSnH34f0CHrk5nN6u/jwIOC75l3uRRVJm7PVtPORyBw049iZOU0hxnGDnDl+hB2s+UYdiwNRCVNgyiCiXNDPm40Do0y6faqXVMTgJJB5m1YfpoE8kSfvfmmeqMQuQEVaeaM4AiReb280aMgbPth9gSUwUBTL0MJMfqRucES236Xd1MM+Lo8LxJ7d4XdXZkPl7eWPoQKBgQDP51ACc5Rrxo0zfc5hNWhfsPNZKrCpDNO/pThAUtV+Rd0XkJQM1dZAzFEP0hF8eNfDJ50+aEITCwubiIO+DYJMGannLxnCPE3Odv1LD9kaP6KEJbrGthpSEehpzUfbfiaMBUFIcOeGGC2Jfu4TSHC1r4kNDFLZPwM5gPneBnpm1QKBgQCviTwIXbzwp5im3UDgEx5GYutq5Z5WfA09OXAOvzrLAvDvMoPMf+QJaXPLxfh9s4XUBabW01C7sIVUSsl6x9zjzqvaJ/LYQKvh3Bp1XiQSHWT0Q24q2M1kT0MOcaRo/KGugmMmARq9QwG7ehwEoWbURULuAGBK8KP6tM/N+HrKUwKBgDp+g1VdPYohQAMXx2Q0SZx6xcz1pOS7VixaiPwYEjdH4s/Jfs4pr/FlSv1fhkn2Ggavc59maAjBl8931lE3rcxJgDMqXl3255FKizN+TwACnW/+yZE1FSy4GyMBfdE0qhXFOLvwAEQLM2y3CLbmL8f+jlnceMbk4rxgHG0qd2/NAoGAUdjtcwEyPgwHc5aqz1Bax3sMRhW21KCXE7sacuXbphlC+mVVax8e1vyij5tJmGMCva+88WIqvLk8EeelNmjGqDoPi3MhGpBdFL0qYRt6OjAWiIaAcd4RNvp2S9/VR1a0lMgbLvdI3oLF5gKz2bzgU43P/WWTgJ3f9+AjHOkFaB0CgYEAm9rcI3uIJTjbTB9aUPTiudOxcoGaL98tqd+OTXqTknbO9uxEeqDdvWleQH92WT+bWF5chAkhpioRwNwoSBugzzsMkGBfQFUYuGYOqevXzVCfsGw51TWVWuREzNFnHiRpHx6ZQJsm+8pWIUCO0mvtdaAmyafOi9L0t/NOvapqLsQ=";

    public static String encryptByPublicKey(String data) throws Exception {
        byte[] buf = data.getBytes();
        byte[] keyBytes = Base64Util.decodeBase64(PUB_KEY);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(RSA_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        return Base64Util.encodeBase64(cipher.doFinal(buf, 0, buf.length));
    }

    public static String decryptByPublicKey(byte[] buf) throws Exception {
        byte[] keyBytes = Base64Util.decodeBase64(PUB_KEY);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(RSA_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        return new String(cipher.doFinal(buf, 0, buf.length), StandardCharsets.UTF_8);
    }
//
//    public static String encryptByPrivateKey(byte[] buf) throws Exception {
//        byte[] keyBytes = Base64Util.decodeBase64(priKey);
//        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = null;
//        keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
//        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
//        Cipher cipher = Cipher.getInstance(RSA_PADDING);
//        cipher.init(Cipher.ENCRYPT_MODE, privateK);
//        return Base64Util.encodeBase64(cipher.doFinal(buf, 0, buf.length));
//    }
//
//    public static String decryptByPrivateKey(byte[] buf) throws Exception {
//        byte[] keyBytes = Base64Util.decodeBase64(priKey);
//        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
//        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
//        Cipher cipher = Cipher.getInstance(RSA_PADDING);
//        cipher.init(Cipher.DECRYPT_MODE, privateK);
//        return new String(cipher.doFinal(buf, 0, buf.length), StandardCharsets.UTF_8);
//    }
}
