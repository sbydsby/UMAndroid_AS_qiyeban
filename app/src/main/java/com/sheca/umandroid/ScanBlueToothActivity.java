package com.sheca.umandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.excelsecu.slotapi.EsIBankDevice;
import com.facefr.util.CheckPermServer;
import com.sheca.bluetoothscan.BluetoothScanCallback;
import com.sheca.bluetoothscan.JShcaBluetoothScanner;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.CommonConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScanBlueToothActivity extends Activity implements BluetoothScanCallback{
	private RadioGroup group_temo;  
    private RadioButton checkRadioButton;  

    private ProgressDialog progDialog = null;
	private SharedPreferences sharedPrefs;
	
	private  JShcaEsStd gEsDev;
	private  JShcaBluetoothScanner gEsDevScanner;
	
	private CheckPermServer mCheckPermServer;  // android 6.0动态权限
	
	private List<String> m_Devlst = null;
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	private List<Map<String, String>> mData = null;
	private AlertDialog certListDialog = null;
	
	private String strDeviceSN = "";
	private AccountDao accountDao = null;
	private boolean    mIsDao     = false;   //导入证书标志
	
	private final int REQUEST_ENABLE_BT = 1;
	
	public void onDevStateNotificate(int stateCode){
		if (stateCode == BluetoothScanCallback.K_STATECODE_FOUNDDEVICE){			
			List<String> lst = gEsDevScanner.getDeviceSnList();
			for (int i=0;i<lst.size();i++){
				m_Devlst.add(lst.get(i));

			}			
		}else if (stateCode == BluetoothScanCallback.K_STATECODE_UNKNOW){
			
		}else if (stateCode == BluetoothScanCallback.K_STATECODE_DIDENDLISTEN){
			m_Devlst.clear();
			
			List<String> lst = gEsDevScanner.getDeviceSnList();
			for (int i=0;i<lst.size();i++){
				m_Devlst.add(lst.get(i));

			}			
		}else if(stateCode == BluetoothScanCallback.K_STATECODE_CONNECTED){
			strDeviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		}else{
			
		}
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_scan_bluetooth_device);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("蓝牙key配对");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		accountDao = new AccountDao(ScanBlueToothActivity.this);

		gEsDev = JShcaEsStd.getIntence(this);        
		gEsDevScanner = JShcaBluetoothScanner.getIntence(this, this);
        m_Devlst = new ArrayList<String>();
        
        strDeviceSN = "";
    
        ht = new HandlerThread("es_device_working_thread1");
      	ht.start();
        workHandler = new Handler(ht.getLooper()); 
        
        Bundle extras = getIntent().getExtras(); 
        if(null != extras)
		   if(extras.getString("input")!=null)
			  mIsDao = true;

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScanBlueToothActivity.this.finish();
			}
		});
		
		mCheckPermServer = new CheckPermServer(this,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 权限不足
						setResult(CheckPermServer.PERMISSION_DENIEG);
						finish();
					}
		});
			
		if (!mCheckPermServer.permissionSet(ScanBlueToothActivity.this,CheckPermServer.PERMISSION_SEARCH_BLUETOOTH)){ 
		     checkBTConnect();
		}
		
		group_temo = (RadioGroup) findViewById(R.id.radioGroup1);  
	    // 注册事件  
	    group_temo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
	          @Override  
	          public void onCheckedChanged(RadioGroup group, int checkedId) {  	  
	             // 点击事件获取的选择对象  
	             checkRadioButton = (RadioButton) group_temo.findViewById(checkedId);  
	             strDeviceSN = checkRadioButton.getText().toString();
	          }  
	    });  

	   ImageView mRegActBtn = (ImageView) findViewById(R.id.edit);  
	   mRegActBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view){	
				if(m_Devlst.size() >0){
				    checkRadioButton = (RadioButton)findViewById(group_temo.getCheckedRadioButtonId());
				    strDeviceSN = checkRadioButton.getText().toString();
				    strDeviceSN = strDeviceSN.substring(strDeviceSN.indexOf(":")+1);				    

		        	connectBTDevice();
				}else{
					checkBTConnect();
				}
			}
		});	
	}
	
	@SuppressLint("Override")
	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		if (CheckPermServer.PERMISSION_REQUEST_CODE == requestCode
				&& mCheckPermServer.hasAllPermissionGranted(grantResults)) {
			// 回调中加载下一个Activity
			checkBTConnect();
		} else {
			mCheckPermServer.showMissingPermissionDialog();
			//ScanBlueToothSimActivity.this.finish();
		}
	}
	
	@SuppressLint("NewApi")
	private  void  checkBTConnect(){
		final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);  
		BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();      
		
		if (mBluetoothAdapter == null) {  
			Toast.makeText(ScanBlueToothActivity.this, "不支持蓝牙", Toast.LENGTH_SHORT).show();  
			return;
		}  
		if(mBluetoothAdapter.isEnabled()){
			//Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();  
			searchBTDevice();
		}else{
			Toast.makeText(ScanBlueToothActivity.this, "蓝牙未开启", Toast.LENGTH_SHORT).show();  
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 					
		}
		
	}

	private  void  searchBTDevice(){   //搜索蓝牙设备
		final Handler handler = new Handler(ScanBlueToothActivity.this.getMainLooper());		
		strDeviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		
		showProgDlg("正在搜索蓝牙设备...");
		try{								
			workHandler.post(new Runnable(){					   
				public void run(){  
					  /*  if(!"".equals(strDeviceSN)){
					    	shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strDeviceSN);
	        				if(null != devInfo)	
	        					gEsDev.disconnect();	
					    }
					
					    try {	    						   
							Thread.sleep(2000);  
						} catch (Exception e) {   
							// TODO Auto-generated catch block
							e.printStackTrace(); 
						}
					    */
					    
					    gEsDevScanner.stopBluetoothScanner();	
					
						gEsDevScanner.startBluetoothScanner(); 	      
						try {	    						   
							Thread.sleep(3000);  
						} catch (Exception e) {   
							// TODO Auto-generated catch block
							e.printStackTrace(); 
						}
			    					   	  
						if (gEsDevScanner.isBluetoothScaning() ){
							try {	    						   
								Thread.sleep(1000);  
							} catch (Exception e) {   
								// TODO Auto-generated catch block
								e.printStackTrace(); 
							}
							
							gEsDevScanner.stopBluetoothScanner();		
						}
			    			//Toast.makeText(MySettingsActivity.this,"停止蓝牙设备搜索", Toast.LENGTH_SHORT).show();
						
						
						//if(m_Devlst.size() == 0){		      
						//	Toast.makeText(ScanBlueToothActivity.this,"未搜索到蓝牙设备",Toast.LENGTH_SHORT).show(); 	
						//	Intent resultIntent = new Intent();
						//	ScanBlueToothActivity.this.setResult(RESULT_CANCELED, resultIntent);
						//	ScanBlueToothActivity.this.finish();			
						//}else {
							// Toast.makeText(MySettingsActivity.this,"搜索到蓝牙设备:"+m_Devlst.size()+"个",Toast.LENGTH_SHORT).show();
							//findViewById(R.id.textLabel).setVisibility(RelativeLayout.VISIBLE);
							 handler.post(new Runnable() {
								 @Override
									public void run() {
			    			            showBTDeviceList();	 
			    			            closeProgDlg();    
								    }								     	
						     }); 	
						//}

					    
				}								     	
			}); 				    		
		} catch (Exception e) {		
			e.printStackTrace();		
			closeProgDlg();	
			
			Toast.makeText(ScanBlueToothActivity.this,"搜索蓝牙设备失败",Toast.LENGTH_SHORT).show();
			Intent resultIntent = new Intent();
			ScanBlueToothActivity.this.setResult(RESULT_CANCELED, resultIntent);
			ScanBlueToothActivity.this.finish();		
		}		
	}

	private  void connectBTDevice(){  
		final Handler handler = new Handler(ScanBlueToothActivity.this.getMainLooper());
		showProgDlg("连接设备中...");

		try{
			workHandler.post(new Runnable(){					   
				public void run(){   
					if(!"".equals(strDeviceSN)){	
						shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strDeviceSN);
						if(null != devInfo){	
							gEsDev.disconnect();
							int nCode = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, strDeviceSN);
							
							handler.post(new Runnable() {
	   							   @Override
	   						       public void run() {
							          closeProgDlg();
	   							   }
  						    }); 
							
							if(nCode == 0){
								handler.post(new Runnable() {
		   							   @Override
		   						       public void run() {
								          Toast.makeText(ScanBlueToothActivity.this, "连接设备成功", Toast.LENGTH_SHORT).show();  
								          
								          if(!mIsDao){
								        	   Account  act = accountDao.getLoginAccount();
											   act.setSaveType(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
											   accountDao.update(act);
								          }
		   							}
	  						    }); 
								
								Intent resultIntent = new Intent();
								ScanBlueToothActivity.this.setResult(RESULT_OK, resultIntent);
								ScanBlueToothActivity.this.finish();								
							}else{
								handler.post(new Runnable() {
		   							   @Override
		   						       public void run() {
								          Toast.makeText(ScanBlueToothActivity.this, "连接设备失败", Toast.LENGTH_SHORT).show();  
		   							}
	  						    }); 
								return;
							}
						}else{
							int nCode = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, strDeviceSN);
							handler.post(new Runnable() {
	   							 @Override
	   						     public void run() {
							           closeProgDlg();
	   							}
  						    });
							
							if(nCode == 0){
								handler.post(new Runnable() {
		   							 @Override
		   						     public void run() {
								        Toast.makeText(ScanBlueToothActivity.this, "连接设备成功", Toast.LENGTH_SHORT).show(); 
								        
								        if(!mIsDao){
								        	   Account  act = accountDao.getLoginAccount();
											   act.setSaveType(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
											   accountDao.update(act);
								        }
		   							}
	  						    });
								
								Editor editor = sharedPrefs.edit();		
					 			editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, strDeviceSN);
					        	editor.commit();
					        		
								Intent resultIntent = new Intent();
								ScanBlueToothActivity.this.setResult(RESULT_OK, resultIntent);
								ScanBlueToothActivity.this.finish();								
							}else{
								handler.post(new Runnable() {
		   							 @Override
		   						     public void run() {
								        Toast.makeText(ScanBlueToothActivity.this, "连接设备失败", Toast.LENGTH_SHORT).show();  
		   							}
	  						    });
								return;
							}		
						}
					}else{
						handler.post(new Runnable() {
  							 @Override
  						     public void run() {
						       Toast.makeText(ScanBlueToothActivity.this, "连接设备失败", Toast.LENGTH_SHORT).show();  
  							 }
						});
						
						Intent resultIntent = new Intent();
						ScanBlueToothActivity.this.setResult(RESULT_CANCELED, resultIntent);
						ScanBlueToothActivity.this.finish();		
					}
	
				}								     	
			}); 	
		}catch(Exception ex){
			closeProgDlg();
			Toast.makeText(ScanBlueToothActivity.this, "连接设备失败", Toast.LENGTH_SHORT).show();  
			return;
		}
	}
	
	private  void  showBTDeviceList(){
		int[] idRadioButtons = new int[]{R.id.radio0,R.id.radio1,R.id.radio2,R.id.radio3,R.id.radio4};
		int[] idViews = new int[]{R.id.view0,R.id.view1,R.id.view2,R.id.view3,R.id.view4};
		
		if(m_Devlst.size() == 0){
			Toast.makeText(ScanBlueToothActivity.this, "未搜索到蓝牙key设备", Toast.LENGTH_SHORT).show();  
			return;
		}
		
		int listSize = m_Devlst.size();
		if(listSize > idRadioButtons.length)
			listSize = idRadioButtons.length;
			
	    for(int i = 0;i<listSize;i++){
	       ((RadioButton)findViewById(idRadioButtons[i])).setVisibility(RelativeLayout.VISIBLE);
		   ((View)this.findViewById(idViews[i])).setVisibility(RelativeLayout.VISIBLE);
		   
		   ((RadioButton)this.findViewById(idRadioButtons[i])).setText("序列号:"+m_Devlst.get(i));
		}
        
	    ((View)this.findViewById(idViews[listSize-1])).setVisibility(RelativeLayout.GONE);
	    ((RadioButton)findViewById(idRadioButtons[0])).setChecked(true);
	    
	}
		

	@Override
	public void onActivityResult(int requestCode, int resultCode,Intent data){
	      super.onActivityResult(requestCode, resultCode, data);

	      switch(requestCode){
	        case (REQUEST_ENABLE_BT):
	        	if (resultCode == Activity.RESULT_OK) {
	        		searchBTDevice();
	        	}
	            if (resultCode == Activity.RESULT_CANCELED) {
	            	Toast.makeText(this, "蓝牙设置取消", Toast.LENGTH_SHORT).show();  
	            } 
	            break;

	      }
    }
	
	
    private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(ScanBlueToothActivity.this);
		progDialog.setMessage(strMsg);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent resultIntent = new Intent();
			ScanBlueToothActivity.this.setResult(RESULT_CANCELED, resultIntent);
			ScanBlueToothActivity.this.finish();		
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	
}
