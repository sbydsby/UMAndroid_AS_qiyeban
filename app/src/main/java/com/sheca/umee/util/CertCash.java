package com.sheca.umee.util;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CertCash {

//    private final static String BASE_URL = "https://dzfp.sheca.com/isap-extends/print/middle.html?certSn=";//正式
//    private final static String BASE_URL = "http://192.168.2.33:8000/isap-extends/print/middle.html?certSn=";//测试



    private final static String PRIVITE_KEY = "sheca123";

    private final static String AES_ALGORITHMS="AES/CBC/PKCS5Padding";

    private static final String IVPARA = "1234567890ABCDEF";

    private static int offSet = 16;


    private static Cipher cipher;

    private static SecretKeySpec secretKeySpec;

    private static IvParameterSpec ivParameterSpec;


    public static String getQrCodeResult(String certSn,String url)  {
        Cipher cipher = null;
        try {
            cipher = getCipher();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] byteContent = certSn.getBytes(StandardCharsets.UTF_8);
        try {
//            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec());
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(),getIvParameterSpec());// 初始化
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] result = new byte[0];
        try {
            result = cipher.doFinal(byteContent);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String enContent=Base64.encodeToString(result, Base64.NO_WRAP);
        Log.e("密文",enContent);
        return url+enContent; // 加密
    }


    public String decrypt(String encContent) throws Exception {
        Cipher cipher = this.getCipher();
        cipher.init(Cipher.DECRYPT_MODE, this.getSecretKeySpec(), this.getIvParameterSpec());// 初始化
        byte[] result = cipher.doFinal(Base64.decode(encContent, Base64.NO_WRAP));
        return new String(result, StandardCharsets.UTF_8); // 解密
    }

    private static String paddingKeySpec(String key) {
        return String.format("%16s", key);
    }

    private static Cipher getCipher() throws Exception {

        cipher = Cipher.getInstance(AES_ALGORITHMS);

        return cipher;
    }

    private static SecretKeySpec getSecretKeySpec() {

        secretKeySpec = new SecretKeySpec(paddingKeySpec(PRIVITE_KEY).getBytes(), "AES");

        return secretKeySpec;
    }

    private static IvParameterSpec getIvParameterSpec() {

        ivParameterSpec = new IvParameterSpec(IVPARA.getBytes(), 0, offSet);

        return ivParameterSpec;
    }


}
