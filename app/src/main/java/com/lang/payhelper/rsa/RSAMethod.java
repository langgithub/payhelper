package com.lang.payhelper.rsa;

import java.io.FileNotFoundException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by ss on 2018/2/8.
 */
public class RSAMethod {
    //公钥加密
    public static String publicEnData(String source){
        try {
            PublicKey publicKey = RSAUtils.loadPublicKey(RSAUtils.PUCLIC_KEY);
            byte[] b1 = RSAUtils.encryptData(source.getBytes(), publicKey);
            String bs = Base64Utils.encode(b1);
            return bs;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //公钥加密
    public static String publicEnData(String source,String key){
        try {
            PublicKey publicKey = RSAUtils.loadPublicKey(key);
            byte[] b1 = RSAUtils.encryptData(source.getBytes(), publicKey);
            String bs = Base64Utils.encode(b1);
            return bs;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //公钥解密私钥加密内容
    public static String publicDeData(String privateEnData){
        try {
//            PrivateKey privateKey = RSAUtils.loadPrivateKey(RSAUtils.PRIVATE_KEY);
            PublicKey publicKey = RSAUtils.loadPublicKey(RSAUtils.PUCLIC_KEY);
            return new String(RSAUtils.dePrivateData(Base64Utils.decode(privateEnData), publicKey));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    //公钥解密私钥加密内容
    public static String publicDeData(String privateEnData,String key){
        try {
//            PrivateKey privateKey = RSAUtils.loadPrivateKey(RSAUtils.PRIVATE_KEY);
            PublicKey publicKey = RSAUtils.loadPublicKey(key);
            return new String(RSAUtils.dePrivateData(Base64Utils.decode(privateEnData), publicKey));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //私钥加密
    public static String privateEnData(String source,String key){
        try {
            PrivateKey privateKey = RSAUtils.loadPrivateKey(key);
            byte[] b1 = RSAUtils.enPrivateData(source.getBytes(), privateKey);
            String bs = Base64Utils.encode(b1);
            return bs;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //私钥解密
    public static String privateDeData(String privateEnData,String key){
        try {
            PrivateKey privateKey = RSAUtils.loadPrivateKey(key);
            return new String(RSAUtils.decryptData(Base64Utils.decode(privateEnData), privateKey));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }







}
