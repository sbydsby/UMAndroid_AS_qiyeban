/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.sheca.fingerui;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.ifaa.sdk.auth.message.AuthenticatorResponse;
import com.sheca.umandroid.LaunchActivity;
import com.sheca.umandroid.R;

/**
 * 
 * @author piwei.pw
 * @version $Id: FingerPrintToast.java, v 0.1 2014-6-8 下午4:16:38 piwei.pw Exp $
 */
public class FingerPrintToast {

    private int     mResultCode;
    private Context context;
    static private Toast   mToast;
    public static final int ST_REGSUCCESS = 1;
    public static final int ST_REGFAIL = 2;
    public static final int ST_AUTHSUCCESS = 3;
    public static final int ST_AUTHFAIL = 4;

    public static final int ST_REGTEEFAIL = 5;
    public static final int ST_AUTHTEEFAIL = 6;
    public static final int ST_DEREGSUCCESS = 7;
    public static final int ST_DEREGFAIL = 8;
    /**
     * @param context
     */
    public FingerPrintToast(Context context, int resultCode) {
        this.context = context;
        this.mResultCode = resultCode;
    }
    
    static public void cancelToast(){
        if (mToast != null) {
           mToast.cancel(); 
        }
    }

    /**
     * show the Toast
     */
    public void show(String text) {
        if (mToast != null) {
            //return;
            mToast.cancel();
        }

        if (text != null && !text.isEmpty()) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.show();
        } else {
            switch (mResultCode) {
                //根据UE需求 去掉
                case AuthenticatorResponse.RESULT_SUCCESS:
                	LaunchActivity.isIFAAFingerOK = true;
                	LaunchActivity.failCount = 0;
                    mToast = Toast.makeText(context, R.string.verify_success, Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case AuthenticatorResponse.RESULT_NO_MATCH:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, R.string.verify_not_match, Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case AuthenticatorResponse.RESULT_TIMEOUT:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, R.string.verify_timeout, Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case AuthenticatorResponse.RESULT_CANCELED:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, R.string.verify_cancel, Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_AUTHSUCCESS:
                	LaunchActivity.isIFAAFingerOK = true;
                	LaunchActivity.failCount = 0;
                    mToast = Toast.makeText(context, "指纹验证成功", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_AUTHFAIL:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, "指纹验证失败", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_REGSUCCESS:
                	LaunchActivity.isIFAAFingerOK = true;
                	LaunchActivity.failCount = 0;
                    mToast = Toast.makeText(context, "指纹注册成功", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_REGFAIL:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, "指纹注册失败", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_REGTEEFAIL:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, "指纹注册失败", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_AUTHTEEFAIL:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, "指纹验证失败", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_DEREGSUCCESS:
                	LaunchActivity.isIFAAFingerOK = true;
                	LaunchActivity.failCount = 0;
                    mToast = Toast.makeText(context, "指纹注销成功", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                case ST_DEREGFAIL:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, "指纹注销失败", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
                default:
                	LaunchActivity.isIFAAFingerOK = false;
                	LaunchActivity.failCount++;
                    mToast = Toast.makeText(context, R.string.verify_faile, Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    break;
            }

        }

    }

}
