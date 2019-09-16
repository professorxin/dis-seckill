package com.lzx.common.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }


    //第一次MD5加密的固定盐值
    public static final String salt = "1a2b3c4d";

    /**
     * 用户输入密码MD5加密为表单密码
     * @param inputPassword
     * @return
     */
    public static String inputPassToFormPass(String inputPassword) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPassword + salt.charAt(5)
                + salt.charAt(4);
        return md5(str);
    }

    /**
     * 表单密码MD5加密为数据库存储密码
     * @param formPassword
     * @param saltDb
     * @return
     */
    public static String formPassToDbPass(String formPassword, String saltDb) {
        String str = "" + saltDb.charAt(0) + saltDb.charAt(2) + formPassword + saltDb.charAt(5)
                + saltDb.charAt(4);
        return md5(str);
    }

    /**
     * 两次MD5加密
     * @param inputPassword
     * @param saltDb
     * @return
     */
    public static String inputPassToDbPass(String inputPassword, String saltDb) {
        String formPassword = inputPassToFormPass(inputPassword);
        String dbPassword = formPassToDbPass(formPassword, saltDb);
        return dbPassword;
    }

}
