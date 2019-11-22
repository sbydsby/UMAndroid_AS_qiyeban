/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.sheca.fingerui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ifaa.sdk.authenticatorservice.message.Result;
import com.sheca.umandroid.DaoActivity;
import com.sheca.umandroid.R;

/**
 * 
 * @author piwei.pw
 * @version $Id: FingerPrintAuthActivity.java, v 0.1 2014-5-27 上午10:44:33 piwei.pw Exp $
 */
public class FingerPrintAuthDaoActivity extends Activity {

    public static int         CLOSE                  = 0;
    public static int         RESUME                 = 1;

    public static boolean     isAuthencicateActivity = false;
    private TextView          textView;
    private boolean           isExiting              = false;
    private BroadcastReceiver statusReciver;
    {
        statusReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //If there is no data, just exit the Activity
                int status = intent.getIntExtra(DaoActivity.FingerprintBroadcastUtil.FINGERPRINTSENSOR_STATUS_VALUE, 0);
                Log.i("FingerPrintAuthActivity", String.valueOf(status));
                switch (status) {
                    case 0:
                        finish();
                        break;
                    case -1:
                        FingerPrintToast.cancelToast();
                        break;
                    case Result.StatusCode.STATUS_INPUTTING:
                        textView.setText(R.string.waiting);
                        break;
                    case Result.StatusCode.STATUS_WAITING_FOR_INPUT:
                        textView.setText(R.string.input);
                        break;
                    case Result.StatusCode.RESULT_NO_MATCH:
                        textView.setText("请再试一次");
                    case Result.StatusCode.RESULT_SUCCESS:
                    case Result.StatusCode.RESULT_CANCELED:
                    case Result.StatusCode.RESULT_TIMEOUT:
                        new FingerPrintToast(FingerPrintAuthDaoActivity.this, status)
                            .show("");
                        break;
                    case Result.StatusCode.STATUS_INPUT_COMPLETED:
                        //textView.setText(R.string.waiting);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    /** 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化广播系统用于接收系统指纹接收器状态变化
        Debug.stopMethodTracing();
        registerReceiver(statusReciver, DaoActivity.FingerprintBroadcastUtil.getIdentifyChangeBroadcastFilter());
        isExiting = false;
        isAuthencicateActivity = false;
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.ifaafingerprintauth);
        textView = (TextView) findViewById(R.id.fptext); 
        
       // findViewById(R.id.imageButton1).setVisibility(RelativeLayout.GONE); 
    }

    /** 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        textView.setText(R.string.input);
    }

    /** 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(statusReciver);
        com.sheca.umandroid.LaunchActivity.authenticator.cancel();

    }

    /**
     * 点击其他地方对话框退出
     * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        if (!isExiting) {
            isExiting = true;
        }
        super.finish();
    }

    /** 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    /**
     * 点击退出箭头 对话框退出
     * 
     * @param v
     */
    public void fpExit(View v) {
        finish();
    }
    
 
}
