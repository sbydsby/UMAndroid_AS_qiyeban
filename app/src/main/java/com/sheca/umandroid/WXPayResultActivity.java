package com.sheca.umandroid;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.presenter.CertController;
import com.sheca.umandroid.util.AccountHelper;

import net.sf.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class WXPayResultActivity extends Activity{
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	private boolean mIsPayed = false;     //是否支付成功
	private AccountDao mAccountDao = null;
	
	private final int COUNT_DOWN_NUM = 3;  //设置倒计时3秒
	
	private String strLoginAccount = "";
	private String strLoginId = "";
	private boolean isPayed = false;    
	private String strReqNumber = "";
	private String strStatus = "";
	private String strCertType;
	private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
	private String  mStrBTDevicePwd = "";    //蓝牙key密码
	
	private int count = COUNT_DOWN_NUM;
	private Timer timer = new Timer( );
	private TimerTask task = null;

	final Handler handler = new Handler( ) {
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case 1:
	                update();
	                break;
	        }
	       
	        super.handleMessage(msg);
	    }

	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_pay_result);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("支付");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);
		cancelScanButton.setVisibility(RelativeLayout.GONE);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		if(bundle.containsKey("paystate"))
			mIsPayed = bundle.getBoolean("paystate");
		
		if(bundle.getString("loginAccount") != null){
			strLoginAccount = bundle.getString("loginAccount");	  
		}
		
		if(bundle.getString("loginId") != null){
			strLoginId = bundle.getString("loginId");
		}
		
		if(bundle.getString("isPayed") != null){
			isPayed = true;
		}
		
		if(bundle.getString("bluetoothpwd")!=null){
			mBBTDeviceUsed = true;
			mStrBTDevicePwd = bundle.getString("bluetoothpwd");
		}
		
		if(bundle.getString("requestNumber") != null)
			strReqNumber = bundle.getString("requestNumber");	
		
		if(bundle.getString("applyStatus") != null)
			strStatus = bundle.getString("applyStatus");

		if(bundle.getString("strCertType") != null)
			strCertType = bundle.getString("strCertType");

		
		mAccountDao = new AccountDao(WXPayResultActivity.this);
		findViewById(R.id.indicater).setVisibility(RelativeLayout.VISIBLE);
		
		if(mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4)  //账户已实名认证
			  ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_noface_guide_2)));  
		else
			  ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_face_guide_3)));  

		
		if(mIsPayed){
			((ImageView)findViewById(R.id.payresult)).setImageDrawable(getResources().getDrawable((R.drawable.payok)));  
			((ImageView)findViewById(R.id.paybutton)).setImageDrawable(getResources().getDrawable((R.drawable.button_ok)));  
			((TextView)findViewById(R.id.paydesc)).setText("恭喜您,已支付成功");
			((TextView)findViewById(R.id.paystate)).setVisibility(RelativeLayout.VISIBLE);
			((TextView)findViewById(R.id.paystate)).setText("正在准备证书申请... "+COUNT_DOWN_NUM+"秒");
			
			showCountDown(COUNT_DOWN_NUM); 
		}else{
			((ImageView)findViewById(R.id.payresult)).setImageDrawable(getResources().getDrawable((R.drawable.payerr)));  
			((ImageView)findViewById(R.id.paybutton)).setImageDrawable(getResources().getDrawable((R.drawable.again)));  
			((TextView)findViewById(R.id.paydesc)).setText("很遗憾,您支付失败");
			((TextView)findViewById(R.id.paystate)).setVisibility(RelativeLayout.GONE);
			((TextView)findViewById(R.id.paystate)).setText("");	
		}
		
		ImageView payButton = (ImageView) this
				.findViewById(R.id.paybutton);
		payButton.setVisibility(RelativeLayout.VISIBLE);

		payButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			   if(mIsPayed){
				    timer.cancel( );
					timer = null;
					task.cancel();
					task = null;
					count = COUNT_DOWN_NUM;
				   
//				    Intent intent = new Intent();
//////					intent.setClass(WXPayResultActivity.this, DownlaodCertActivity.class);
//////				    intent.putExtra("loginAccount", strLoginAccount);
//////					intent.putExtra("loginId", strLoginId);

				   CertController certController = new CertController();
				   String responseStr = certController.downloadCert(WXPayResultActivity.this,strReqNumber,strCertType,AccountHelper.getRealName(getApplicationContext()));
				   final APPResponse response = new APPResponse(responseStr);

				   if (response.getReturnCode() == 0 ) {
					   JSONObject jbRet = response.getResult();
					   String certId = jbRet.getString("certID");
					   com.sheca.umplus.model.Cert certPlus = certController.getCertDetailandSave(WXPayResultActivity.this, certId);

					   Cert cert = certController.convertCert(certPlus);

					   CertDao mCertDao = new CertDao(getApplicationContext());
					   mCertDao.addCert(cert, AccountHelper.getUsername(getApplicationContext()));
					   Toast.makeText(WXPayResultActivity.this,response.getReturnMsg(),Toast.LENGTH_SHORT).show();
					   Log.d("unitrust", "mCertDao.addCert 成功");
				   }else{
					   Toast.makeText(WXPayResultActivity.this,"失败："+response.getReturnMsg(),Toast.LENGTH_SHORT).show();
				   }

//					if(isPayed)
//				   intent.putExtra("isPayed", "pay");
//				   intent.putExtra("requestNumber",strReqNumber);
//				   intent.putExtra("applyStatus",strStatus);
//				   if(mBBTDeviceUsed)
//					   intent.putExtra("bluetoothpwd", mStrBTDevicePwd);
//				   startActivity(intent);
				   finish();
			   }else{
				    Intent intent = new Intent();
					intent.setClass(WXPayResultActivity.this, com.sheca.umandroid.WXPayActivity.class);
				    intent.putExtra("loginAccount", strLoginAccount); 
					intent.putExtra("loginId", strLoginId); 
					intent.putExtra("requestNumber",strReqNumber); 
				    intent.putExtra("applyStatus",strStatus); 
				    if(mBBTDeviceUsed)
				    	intent.putExtra("bluetoothpwd", mStrBTDevicePwd); 
					startActivity(intent);
					finish();
			   }
			}
		});
	
    }

    private  void showCountDown(final int countDownNum){
		timer = new Timer();		
		task = new TimerTask( ) {
		    public void run ( ) {
		        Message message = new Message( );
		        message.what = 1;
		        handler.sendMessage(message);
		    }
		};
		
		timer.schedule(task,0,1000);
	}
    
    private  void update(){
		count--;
		if(count > 0){
			((TextView)findViewById(R.id.paystate)).setText("正在准备证书申请... "+count+"秒");
		}else{
			timer.cancel( );
			timer = null;
			task.cancel();
			task = null;
			count = COUNT_DOWN_NUM;
			
			Intent intent = new Intent();
			intent.setClass(WXPayResultActivity.this, DownlaodCertActivity.class);
			intent.putExtra("loginAccount", strLoginAccount); 
		    intent.putExtra("loginId", strLoginId); 
		    if(isPayed)
				intent.putExtra("isPayed", "pay");
			intent.putExtra("requestNumber",strReqNumber); 
		    intent.putExtra("applyStatus",strStatus); 
		    if(mBBTDeviceUsed)
		    	intent.putExtra("bluetoothpwd", mStrBTDevicePwd); 
			startActivity(intent);
		    finish();
		}
	}
	
    

}