package com.esandinfo.utils;

import android.util.Log;


public class MyLog {

    private static String LOGTAG = "etasSDK";

    public static void error(String message) {
        Log.e(LOGTAG, message);
    }

    public static void warn(String message) {
        Log.w(LOGTAG, message);
    }

    public static void info(String message) {
        Log.i(LOGTAG, message);
    }

    public static void debug(String message) {
        Log.i(LOGTAG, message);
    }

}
