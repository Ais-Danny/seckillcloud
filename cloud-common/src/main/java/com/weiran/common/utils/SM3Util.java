package com.weiran.common.utils;

import cn.hutool.crypto.SmUtil;

/**
 * 国密 sm3加密解密工具类
 */
public class SM3Util {

    public static String sm3(String str) {
        return SmUtil.sm3(str);
    }

    // 前端服务器登陆验证的加盐
    private static final String salt = "3a41dx1d";

    public static String inputPassToFormPass(String inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return sm3(str);
    }

    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return sm3(str);
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("" + 123));
    }

}
