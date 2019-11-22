package com.sheca.umandroid.util;

/**
 * @author xuchangqing
 * @time 2019/8/8 10:58
 * @descript 权限请求工具类
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>请求时调用
 * mPermissionUtil = new PermissionUtil(MainActivity.this);
 * mPermissionUtil.requertLocationPermission(0);
 * <p>回调结果处理
 * switch (requestCode) {
 * case 0:
 * boolean location = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
 * if (location) {
 * // 已获取权限-
 * } else {
 * mPermissionUtil.requertLocationPermission(0);
 * }
 * break;
 * }
 */
@SuppressWarnings("all")
public class PermissionUtil {

    private Context mContext;

    public PermissionUtil(Context context) {
        this.mContext = context;
    }

    /**
     * 请求获取拍照权限
     * permission:android.permission.CAMERA
     */
    public boolean requestCameraPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.CAMERA, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.CAMERA}, requestCode);
        }
        return false;
    }

    public boolean requestCallPhone(int requestCode){
       boolean b= checkPermission(mContext,Manifest.permission.CALL_PHONE,requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.CALL_PHONE}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取录音权限
     * permission:android.permission.RECORD_AUDIO
     */
    public boolean requestAudioPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.RECORD_AUDIO, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.RECORD_AUDIO}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取写入数据权限
     * permission:android.permission.READ_EXTERNAL_STORAGE
     * permission:android.permission.WRITE_EXTERNAL_STORAGE
     */
    public boolean requestStoragePermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取定位权限
     * permission:android.permission.ACCESS_FINE_LOCATION
     * permission:android.permission.ACCESS_COARSE_LOCATION
     */
    public boolean requestLocationPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取联系人权限
     * permission:android.permission.WRITE_CONTACTS
     * permission:android.permission.GET_ACCOUNTS
     * permission:android.permission.READ_CONTACTS
     */
    public boolean requestContactPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.WRITE_CONTACTS, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.WRITE_CONTACTS}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取短信权限
     * permission:android.permission.READ_SMS
     * permission:android.permission.RECEIVE_WAP_PUSH
     * permission:android.permission.RECEIVE_MMS
     * permission:android.permission.RECEIVE_SMS
     * permission:android.permission.SEND_SMS
     * permission:android.permission.READ_CELL_BROADCASTS
     */
    public boolean requestSmsPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.SEND_SMS, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.SEND_SMS}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取手机状态权限
     * permission:android.permission.READ_CALL_LOG
     * permission:android.permission.READ_PHONE_STATE
     * permission:android.permission.CALL_PHONE
     * permission:android.permission.WRITE_CALL_LOG
     * permission:android.permission.USE_SIP
     * permission:android.permission.PROCESS_OUTGOING_CALLS
     * permission:com.android.voicemail.permission.ADD_VOICEMAIL
     */
    public boolean requestPhoneStatePermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.READ_PHONE_STATE, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取日历权限
     * permission:android.permission.READ_CALENDAR
     * permission:android.permission.WRITE_CALENDAR
     */
    public boolean requestCalendarPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.WRITE_CALENDAR, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.WRITE_CALENDAR}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取传感器权限
     * permission:android.permission.BODY_SENSORS
     */
    public boolean requestSensorsPermission(int requestCode) {
        boolean b = checkPermission(mContext, Manifest.permission.BODY_SENSORS, requestCode);
        if (b) {
            return true;
        } else {
            requestPermission(mContext, new String[]{Manifest.permission.BODY_SENSORS}, requestCode);
        }
        return false;
    }

    /**
     * 请求获取多个权限 - 一般用于首次进入提示
     * List<String> mPermissionsList = new ArrayList<>();
     * mPermissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
     * mPermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
     * boolean b = requestPermissions(mPermissionsList);
     */
    public boolean requestPermissions(List<String> permissionsList) {
        boolean flag = true;
        List<String> mPermissionsList = new ArrayList<>();
        if (!mPermissionsList.isEmpty()) {
            mPermissionsList.clear();
        }
        for (int i = 0; i < permissionsList.size(); i++) {
            String permission = permissionsList.get(i);
            boolean b = checkPermission(mContext, permission, 10);
            if (!b) {
                flag = b;
                mPermissionsList.add(permission);
            }
        }
        if (!mPermissionsList.isEmpty()) {
            String[] permissions = (String[]) mPermissionsList.toArray(new String[mPermissionsList.size()]);
            requestPermission(mContext, permissions, 10);
        }
        return flag;
    }

    /**
     * 检测权限
     */
    private boolean checkPermission(Context context, String permission, int code) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // judgePermission(context, permission, code);
        } else {
            return true;
        }
        return false;
    }

    /**
     * 请求权限
     */
    private void requestPermission(Context context, String[] permissions, int code) {
        ActivityCompat.requestPermissions((Activity) context, permissions, code);
    }

    /**
     * 判断是否已拒绝过权限
     *
     * @describe :如果应用之前请求过此权限但用户拒绝，此方法将返回 true;
     * -----------如果应用第一次请求权限或 用户在过去拒绝了权限请求，
     * -----------并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。
     */
    private void judgePermission(Context context, String permission, int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            requestPermission(context, new String[]{permission}, code);
        } else {
            toPermissionSetting(context);
        }
    }

    /**
     * 跳转到权限设置界面
     */
    private void toPermissionSetting(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }
}

