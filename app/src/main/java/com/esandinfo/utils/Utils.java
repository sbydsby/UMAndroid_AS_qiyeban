package com.esandinfo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    private final static String IFAA_SERVER_URL = "IFAA_URL";
    private final static String IFAA_PERFERENCE_FILE = "IFAA_PERFERENCE_FILE";
    // 默认为一下测试服务器的 url
    private final static String ESAND_DEV_SERVER_URL = "http://bizserver.dev.esandinfo.com:80/gateway";
    //private final static String ESAND_DEV_SERVER_URL = "http://192.168.21.101:8080/ifaaws/gateway";
    //private final static String ESAND_DEV_SERVER_URL = "http://192.168.31.250:8080/ifaaws/gateway";

    // 保存 ifaa 服务器 url
    public static boolean saveIfaaUrl(Context context, String url) {

        boolean result = false;

        SharedPreferences sharedPreferences = context.getSharedPreferences(IFAA_PERFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IFAA_SERVER_URL, url);
        result = editor.commit();

        return result;

    }

    // 获取IFAA 服务器 url
    public static String getIfaaUrl(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(IFAA_PERFERENCE_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(IFAA_SERVER_URL, ESAND_DEV_SERVER_URL);

    }
}
