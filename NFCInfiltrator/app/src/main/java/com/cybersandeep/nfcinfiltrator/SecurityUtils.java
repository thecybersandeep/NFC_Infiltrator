package com.cybersandeep.nfcinfiltrator;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {
    private static final String ALGO = "AES/CBC/PKCS5Padding", AES = "AES";
    private static final String CH1_KEY = "NfcInf1ltr4t0rK3y", CH2_KEY = "S3cur3dF4c1l1tyK3y";
    private static final String CH1_IV = "MTIzNDU2Nzg5MDEyMzQ1Ng==", CH2_IV = "QWJjZGVmZ2hpamtsbW5vcA==";
    private static final String ENC_CH1 = "U2FsdGVkX1/XVjNFp9ArcSBJT0JTQ0gzTTNfQllQQTU1RUR9", ENC_CH2 = "U2FsdGVkX1/KLMpIzXwTfkZMQUd7TTFNM19UWVBFX0VYUEwwMVRfVlVMTn0=";
    private static final String FB_CH1_1 = "RkxBR3tORkNfU0NIRU1FX0JZUEFTU19TVUNDRVNTRlVM", FB_CH1_2 = "fQ==";
    private static final String FB_CH2_1 = "RkxBR3tNSU1FX1RZUEVfRVhQTE9JVF9TVUNDRVNTRlVM", FB_CH2_2 = "fQ==";


    public static String getEncryptedChallenge1Flag() {
        return ENC_CH1;
    }

    public static String getEncryptedChallenge2Flag() {
        return ENC_CH2;
    }

    public static String decryptChallenge1Flag(String input) {
        if (!verifyCh1(input)) return "Challenge not completed";
        try {
            String d = dec(ENC_CH1, CH1_KEY, Base64.decode(CH1_IV, Base64.DEFAULT));
            return (d != null && d.contains("FLAG{")) ? d : fbCh1();
        } catch (Exception e) {
            return fbCh1();
        }
    }

    public static String decryptChallenge2Flag(String input) {
        if (!verifyCh2(input)) return "Challenge not completed";
        try {
            String d = dec(ENC_CH2, CH2_KEY, Base64.decode(CH2_IV, Base64.DEFAULT));
            return (d != null && d.contains("FLAG{")) ? d : fbCh2();
        } catch (Exception e) {
            return fbCh2();
        }
    }

    private static String dec(String data, String key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES), new IvParameterSpec(iv));
        byte[] out = c.doFinal(Base64.decode(data, Base64.DEFAULT));
        return new String(out, StandardCharsets.UTF_8);
    }

    public static boolean verifyCh1(String uri) {
        return uri != null && uri.startsWith("nfcinfiltrator://") && uri.contains("admin");
    }

    public static boolean verifyCh2(String payload) {
        return payload != null && payload.contains("AUTHORIZATION_USER:CYBERSANDEEP");
    }

    private static String fbCh1() {
        return new String(Base64.decode(FB_CH1_1 + FB_CH1_2, Base64.DEFAULT));
    }

    private static String fbCh2() {
        return new String(Base64.decode(FB_CH2_1 + FB_CH2_2, Base64.DEFAULT));
    }
}