package com.sheca.umandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;

import com.sheca.umandroid.util.CommonConst;
import com.suke.widget.SwitchButton;

public class GestureSettingActivity extends BaseActivity2 {

    private SwitchButton sbGuesture;
    private View itemChangeGuesture;
    
    private SharedPreferences sharedPrefs;
    private SharedPreferences sharedPrefsGesture;
    private String spGesture_Keyname = "";
    private boolean isNotificationGesture = false;
    
    private final int VALIDATE_GESTURE_CODE = 1;	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.mine_gesture, R.layout.activity_guesture_setting);
        
        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPrefsGesture = this.getSharedPreferences(CommonConst.SP_SETTINGS_GESTURE, Context.MODE_PRIVATE);
        spGesture_Keyname = sharedPrefsGesture.getString(CommonConst.SETTINGS_GESTURE_KEYNAME+LockPatternUtil.getActName(), "");
        isNotificationGesture = sharedPrefs.getBoolean(CommonConst.SETTINGS_GESTURE_OPENED+LockPatternUtil.getActName(), false);

        sbGuesture = (SwitchButton)findViewById(R.id.sb_gesture);   
        sbGuesture.setChecked(isNotificationGesture);
        
        if(isNotificationGesture)
        	findViewById(R.id.item_gesture).setVisibility(RelativeLayout.VISIBLE);
        else
        	findViewById(R.id.item_gesture).setVisibility(RelativeLayout.GONE);
        
        sbGuesture.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                  //TODO 
                  if (isChecked){
                	  //设置手势密码
                	  if("".equals(spGesture_Keyname)){
                         Intent intent = new Intent(getApplicationContext(), SetGuestureActivity.class);
                         startActivity(intent);
                	  }else{
                    	  Editor editor = sharedPrefs.edit();
                    	  editor.putBoolean(CommonConst.SETTINGS_GESTURE_OPENED+LockPatternUtil.getActName(), true);
                    	  editor.commit();
                    	  
                    	  findViewById(R.id.item_gesture).setVisibility(RelativeLayout.VISIBLE);
                      }
                  }else{
                	  Editor editor = sharedPrefs.edit();
                	  editor.putBoolean(CommonConst.SETTINGS_GESTURE_OPENED+LockPatternUtil.getActName(), false);
                	  editor.commit();
                	  
                	  findViewById(R.id.item_gesture).setVisibility(RelativeLayout.GONE);
                  }
            }
        });

        itemChangeGuesture = findViewById(R.id.item_gesture);
        itemChangeGuesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {           
                //验证手势密码
                Intent intent = new Intent(getApplicationContext(), ValidateGestureActivity.class);
                //startActivity(intent);     
                startActivityForResult(intent, VALIDATE_GESTURE_CODE);   	
            }
        });
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
		isNotificationGesture = sharedPrefs.getBoolean(CommonConst.SETTINGS_GESTURE_OPENED+LockPatternUtil.getActName(), false);
        sbGuesture.setChecked(isNotificationGesture);
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VALIDATE_GESTURE_CODE) {
			if (resultCode == GestureSettingActivity.this.RESULT_OK) {
				Intent intent = new Intent(getApplicationContext(), SetGuestureActivity.class);
                startActivity(intent);
			}
			if (resultCode == GestureSettingActivity.this.RESULT_CANCELED) {
				
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
		
    }
}
