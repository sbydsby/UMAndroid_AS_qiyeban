package com.sheca.umee;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.model.Account;
import com.sheca.umee.util.CommonConst;

public class SettingSaveTypeActivity extends Activity{
	private RadioGroup group_temo;  
    private RadioButton checkRadioButton;  
    private SharedPreferences sharedPrefs;
    
    private int  m_savetype = CommonConst.SAVE_CERT_TYPE_PHONE;
	private AccountDao accountDao = null;
	
	private final int REQUEST_ENABLE_BT = 1;
	private final int REQUEST_SEARCH_BT = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_setting_save_type);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("证书介质");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		accountDao = new AccountDao(SettingSaveTypeActivity.this);
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingSaveTypeActivity.this.finish();
			}
		});
				
		Intent intent = getIntent();	
		if(null != intent.getExtras())
			m_savetype = intent.getExtras().getInt("saveType");

		group_temo = (RadioGroup) findViewById(R.id.radioGroup1);  
		if(LaunchActivity.isBlueToothUsed){
			findViewById(R.id.radio1).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.radioView2).setVisibility(RelativeLayout.VISIBLE);
			
			findViewById(R.id.radio2).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.radioView3).setVisibility(RelativeLayout.VISIBLE);
		}else{
			if(checkBlueToothUsed()){
				findViewById(R.id.radio1).setVisibility(RelativeLayout.VISIBLE);
				findViewById(R.id.radioView2).setVisibility(RelativeLayout.VISIBLE);
				
				findViewById(R.id.radio2).setVisibility(RelativeLayout.VISIBLE);
				findViewById(R.id.radioView3).setVisibility(RelativeLayout.VISIBLE);
			}else{
			    findViewById(R.id.radio1).setVisibility(RelativeLayout.GONE);
			    findViewById(R.id.radioView2).setVisibility(RelativeLayout.GONE);
			    
			    findViewById(R.id.radio2).setVisibility(RelativeLayout.GONE);
			    findViewById(R.id.radioView3).setVisibility(RelativeLayout.GONE);
			}
		}
		
		findViewById(R.id.radio2).setVisibility(RelativeLayout.GONE);
	    findViewById(R.id.radioView3).setVisibility(RelativeLayout.GONE);

		// 改变默认选项  
		if(CommonConst.SAVE_CERT_TYPE_PHONE == accountDao.getLoginAccount().getSaveType())
	        group_temo.check(R.id.radio0);  
		else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
			group_temo.check(R.id.radio1);  
		else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
			group_temo.check(R.id.radio2);  

	   // 获取默认被被选中值  
	   checkRadioButton = (RadioButton) group_temo.findViewById(group_temo.getCheckedRadioButtonId());  
	   // 注册事件  
	   group_temo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
	          @Override  
	          public void onCheckedChanged(RadioGroup group, int checkedId) {  	  
	             // 点击事件获取的选择对象  
	             checkRadioButton = (RadioButton) group_temo.findViewById(checkedId);  
	             if(checkedId == R.id.radio0)
	            	 m_savetype = CommonConst.SAVE_CERT_TYPE_PHONE;
	             else if(checkedId == R.id.radio1)
	            	 m_savetype = CommonConst.SAVE_CERT_TYPE_BLUETOOTH;
	             else if(checkedId == R.id.radio2)
	            	 m_savetype = CommonConst.SAVE_CERT_TYPE_SIM;
	          }  
	   });  

	   findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View view){	
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == m_savetype){ 
					if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {  
						Toast.makeText(SettingSaveTypeActivity.this, "无蓝牙模块", Toast.LENGTH_SHORT).show();  
						return;
					}  
					
					final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);  
					BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();          
					// Checks if Bluetooth is supported on the device.  
					if (mBluetoothAdapter == null) {  
						Toast.makeText(SettingSaveTypeActivity.this, "不支持蓝牙", Toast.LENGTH_SHORT).show();  
						return;
					}  
					if(mBluetoothAdapter.isEnabled()){
						//Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();  
						showBTDevice();
					}else{
						Toast.makeText(SettingSaveTypeActivity.this, "蓝牙未开启", Toast.LENGTH_SHORT).show();  
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 					
					}
				}else if(CommonConst.SAVE_CERT_TYPE_SIM == m_savetype){ 
					if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {  
						Toast.makeText(SettingSaveTypeActivity.this, "无蓝牙模块", Toast.LENGTH_SHORT).show();  
						return;
					}  
					
					final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);  
					BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();          
					// Checks if Bluetooth is supported on the device.  
					if (mBluetoothAdapter == null) {  
						Toast.makeText(SettingSaveTypeActivity.this, "不支持蓝牙", Toast.LENGTH_SHORT).show();  
						return;
					}  
					if(mBluetoothAdapter.isEnabled()){
						//Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();  
						showBTDevice();
					}else{
						Toast.makeText(SettingSaveTypeActivity.this, "蓝牙未开启", Toast.LENGTH_SHORT).show();  
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 					
					}
				}else{
					Account  act = accountDao.getLoginAccount();
					act.setSaveType(m_savetype);
					accountDao.update(act);
					
					Editor editor = sharedPrefs.edit();		
		 			editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		        	editor.commit();
					
					SettingSaveTypeActivity.this.finish();
				}
			}
		});	
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,Intent data){
	      super.onActivityResult(requestCode, resultCode, data);

	      switch(requestCode){
	        case (REQUEST_ENABLE_BT):
	        	if (resultCode == Activity.RESULT_OK) {
	        		showBTDevice();
	        	}
	            if (resultCode == Activity.RESULT_CANCELED) {
	            	Toast.makeText(this, "蓝牙设置取消", Toast.LENGTH_SHORT).show();  
	            } 
	            break;
	        case (REQUEST_SEARCH_BT):
	        	if (resultCode == Activity.RESULT_OK) {
	        		SettingSaveTypeActivity.this.finish();
	        	}
	            if (resultCode == Activity.RESULT_CANCELED) {
	            	
	            } 
	            break;    
	           
	      }
    }
	
	private void showBTDevice(){
//		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == m_savetype){
//		    Intent intent = new Intent();
//		    intent.setClass(SettingSaveTypeActivity.this, ScanBlueToothActivity.class);
//		    startActivityForResult(intent, REQUEST_SEARCH_BT);
//		}else if(CommonConst.SAVE_CERT_TYPE_SIM == m_savetype){
//			Intent intent = new Intent();
//			intent.setClass(SettingSaveTypeActivity.this, ScanBlueToothSimActivity.class);
//			startActivityForResult(intent, REQUEST_SEARCH_BT);
//		}
	}
	
	@SuppressLint("NewApi")
	private boolean checkBlueToothUsed(){
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {  
			return false;
		}  
		
		final BluetoothManager bluetoothManager =  (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);  
		BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();          
		// Checks if Bluetooth is supported on the device.  
		if (mBluetoothAdapter == null) {  
			return false;
		}  
		
		return true;
	}
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SettingSaveTypeActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	
}
