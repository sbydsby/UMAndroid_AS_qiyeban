package com.sheca.umandroid.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {

    private static Context context;
    private static SharePreferenceUtil instance;
    private SharedPreferences sharedPrefs;

    public static SharePreferenceUtil getInstance(Context ctx){
        context = ctx;
        if (null == instance){
            instance = new SharePreferenceUtil();
        }
        return instance;
    }

    private void load(){
        if (null == sharedPrefs){
            sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
    }

    public void setBoolean(String key,boolean value){
        load();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key){
        load();
        return sharedPrefs.getBoolean(key, false);
    }

    public boolean getBoolean_true(String key){
        load();
        return sharedPrefs.getBoolean(key, true);
    }

    public void setString(String key,String value){
        load();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key){
        load();
        return sharedPrefs.getString(key, null);
    }

    public String getString(String key,String defaultValue){
        load();
        return sharedPrefs.getString(key, defaultValue);
    }

    public void setInt(String key,int value){
        load();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key){
        load();
        return sharedPrefs.getInt(key, -1);
    }
}
