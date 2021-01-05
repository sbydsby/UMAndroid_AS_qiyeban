package com.sheca.zhongmei;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.esandinfo.ifaa.EDISAuthManager;
import com.esandinfo.ifaa.IFAAAuthTypeEnum;
import com.sheca.zhongmei.util.CommonConst;
import com.suke.widget.SwitchButton;

import java.util.List;

public class LocalPasswordActivity extends BaseActivity2 {

    private View gestureItem;
    private SwitchButton sBFinger, sBFace,sBIFAAFace;
    
    private SharedPreferences sharedPrefs;
    private boolean isUserNotificationFinger = false;
    private boolean isNotificationFinger = false;
    
    private int nShowView  = 0;
    private boolean isFaceNoPassState=false;
    private List<IFAAAuthTypeEnum> supportBIOTypes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.mine_fp,R.layout.activity_local_password);

        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        //手势
        gestureItem = findViewById(R.id.item_gesture);
        gestureItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GestureSettingActivity.class);
                startActivity(intent);
            }
        });

        //指纹
        sBFinger = (SwitchButton)findViewById(R.id.sb_finger);
        sBIFAAFace  = (SwitchButton)findViewById(R.id.sb_ifaa_face);

//        sBFinger.setVisibility(RelativeLayout.GONE);
//        sBIFAAFace.setVisibility(RelativeLayout.GONE);
        findViewById(R.id.item_finger).setVisibility(RelativeLayout.GONE);
        findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.GONE);

        //ifaa指纹开关默认关闭
        isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
//        if (LaunchActivity.isIFAAFingerUsed) {
//        	sBFinger.setVisibility(RelativeLayout.VISIBLE);
//        	findViewById(R.id.item_finger).setVisibility(RelativeLayout.VISIBLE);
//        } else {

            List<IFAAAuthTypeEnum> supportBIOTypes = EDISAuthManager.getSupportBIOTypes(this);

            if (supportBIOTypes.isEmpty()) {
                // 如果手机不支持 IFAA , 那么不需要在做其他操作。
//                sBFinger.setVisibility(RelativeLayout.GONE);
//                sBIFAAFace.setVisibility(RelativeLayout.GONE);
                findViewById(R.id.item_finger).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.GONE);
            }else{
                if (supportBIOTypes.contains(IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT)) {
                    sBFinger.setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.item_finger).setVisibility(RelativeLayout.VISIBLE);
                }

                if (supportBIOTypes.contains(IFAAAuthTypeEnum.AUTHTYPE_FACE)) {
                    sBIFAAFace.setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.VISIBLE);
                }


            }
//        }

        sBFinger.setChecked(isUserNotificationFinger);
        sBFinger.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
            	isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
            	nShowView++;
            	
            	if(nShowView %2 != 0){
            	   if (isChecked){
                    //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, true);
                      Intent i = new Intent(getApplicationContext(), SettingFingerTypeActivity.class);
                      i.putExtra("fingerType", isChecked);
                      startActivity(i);
                   }else{
                    //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, false);
                       Intent i = new Intent(getApplicationContext(), SettingFingerTypeActivity.class);
                       i.putExtra("fingerType", isChecked);
                       startActivity(i);
                   }
            	}
            }
        });

        //人脸免密状态设定


        isFaceNoPassState = sharedPrefs.getBoolean(mUserName+CommonConst.FACE_NOPASS, false);


        //人脸免密
        sBFace = (SwitchButton)findViewById(R.id.sb_ifaa_face);
        sBFace.setChecked(isFaceNoPassState);
        sBFace.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                sharedPrefs.edit().putBoolean(mUserName+CommonConst.FACE_NOPASS,isChecked).apply();
            }
        });

        findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.GONE);



        if (supportBIOTypes.contains(IFAAAuthTypeEnum.AUTHTYPE_FACE)) {
            findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.VISIBLE);

            isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);

            sBIFAAFace.setChecked(isUserNotificationFinger);
            sBIFAAFace.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
                    isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
                    nShowView++;

                    if(nShowView %2 != 0){
                        if (isChecked){
                            //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, true);
                            Intent i = new Intent(getApplicationContext(), SettingIFAAFaceTypeActivity.class);
                            i.putExtra("fingerType", isChecked);
                            startActivity(i);
                        }else{
                            //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, false);
                            Intent i = new Intent(getApplicationContext(), SettingIFAAFaceTypeActivity.class);
                            i.putExtra("fingerType", isChecked);
                            startActivity(i);
                        }
                    }
                }
            });
        }else{
            findViewById(R.id.item_ifaa_face).setVisibility(RelativeLayout.GONE);
        }

    }
    
    
    @Override
   	protected void onResume() {
   		super.onResume();
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
   		isNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
   		if(!String.valueOf(isNotificationFinger).equals(String.valueOf(isUserNotificationFinger)))	
    		nShowView++;
    		
   		sBFinger.setChecked(isNotificationFinger);
        sBIFAAFace.setChecked(isNotificationFinger);
    }
}
