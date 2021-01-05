package com.sheca.zhongmei;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.WebClientUtil;

import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ForgetPasswordValidActivity extends Activity {
	private EditText mPhoneCode;         //账户验证码	
	private CertDao  certDao = null;
	private Intent   intent = null;  
	
	private  String  mActName = "";      //账户名称
	private  Button  mButton = null;
	private  String  mError = "";
	private  int     mValidCount = 0;

	private ProgressDialog progDialog = null;
	private boolean mIsDao = false;   //第三方接口调用标记
	
	private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
	private final int GET_VALID_NUM = 3;    //设置获取验证码次数
	
	private Timer timer = new Timer();
	private TimerTask task = null;

	private int count = COUNT_DOWN_NUM;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_forget_password_valid);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("输入验证码");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		intent = new Intent(this, ForgetPasswordActivity.class);  
		certDao = new CertDao(ForgetPasswordValidActivity.this);
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("Account")!=null){
				mActName = extras.getString("Account");
			}
			if(extras.getString("mesage")!=null){
				mIsDao = true;
			}
		}

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.putExtra("ActName", mActName);	
			    startActivity(intent);
			    ForgetPasswordValidActivity.this.finish();
			}
		});
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
    	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
    	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    	 }
		
		mPhoneCode = (EditText) findViewById(R.id.phonecode);
		mPhoneCode.setText("");
		mPhoneCode.requestFocus();	
		mButton = (Button) findViewById(R.id.btnCode);  
		
		((TextView) findViewById(R.id.textActNo)).setText("请输入"+mActName.substring(0,3)+"****"+mActName.substring(mActName.length()-4)+"收到的短信验证码。");
		
		ImageView mNextBtn = (ImageView) findViewById(R.id.btnNext);  //下一步
		mNextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getNextView();
			}
		});

		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getMsgCode();
			}
		});
		
		getMsgCode();
	}
	
	private void  getNextView(){
		final String msgPhoneCode = mPhoneCode.getText().toString();
		boolean cancel = false;
		View focusView = null;
		
		mPhoneCode.setError(null);
		// Check for a valid account name.
		if (TextUtils.isEmpty(msgPhoneCode)) {
			mPhoneCode.setError(getString(R.string.error_invalid_code));
			focusView = mPhoneCode;
			cancel = true;
		}
		if (cancel) {
			// There was an error; don't attempt login and focus the first form field with an error.
			focusView.requestFocus();
		} else{
			
           	               try {  	
			                  if(verifyMobile(mActName,msgPhoneCode)){
				                 Intent intent = new Intent(ForgetPasswordValidActivity.this, SetPasswordActivity.class);    
				                 intent.putExtra("ActName", mActName);	
				                 if(mIsDao)
				                	 intent.putExtra("message", "dao");
			                     startActivity(intent);
			                     ForgetPasswordValidActivity.this.finish();				
			                  }
           	                } catch (final Exception exc) {
							     Toast.makeText(ForgetPasswordValidActivity.this, exc.getMessage()+"", Toast.LENGTH_SHORT).show();		 
                            }
                     
		}
	}
	
	
	private void getMsgCode(){
		final Handler handler = new Handler(ForgetPasswordValidActivity.this.getMainLooper());		
		mValidCount++;
		
	    new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	handler.post(new Runnable(){
						 @Override
							public void run(){ 
							     //if(mValidCount >= GET_VALID_NUM)
							    	// getVoiceCode(mActName);
							    // else
							    	 getValidationCode(mActName);
						 }
	            	});
	            }
	    }).start();

	}
	
	
	private  void getValidationCode(String phone){
		showProgDlg("获取验证码中...");
		
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = ForgetPasswordValidActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ForgetPasswordValidActivity.this.getString(R.string.UMSP_Service_GetValidationCode);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("mobile", phone);
			postParams.put("codeType", "1");
			
			String responseStr = "";
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				
				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8")+
                                   "&codeType="+URLEncoder.encode("1", "UTF-8");
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (final Exception e) {
				closeProgDlg();
				if(e.getMessage().indexOf("peer")!=-1)
					mError = "无效的服务器请求";
				else				 
				    mError = "网络连接异常或无法访问服务";
				
				throw new Exception(mError);	
			}

			JSONObject jb = JSONObject.fromObject(responseStr);
		    resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);
				
			if (resultStr.equals("0")) {	
				closeProgDlg();
				mError = "短信已发送，请等待";
				Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
				showCountDown(COUNT_DOWN_NUM);   //显示倒计时
				return;
			}
			else if(resultStr.equals("1001"))
				mError = "验证服务请求错误";
			else if(resultStr.equals("10003"))
				mError = "内部处理错误";
			else
				mError = returnStr;
			
		} catch (Exception exc) {
			closeProgDlg();
			mError = exc.getMessage();
		}

		closeProgDlg();
		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
	}
	
	
//	private  void getVoiceCode(String phone){
//		showProgDlg("获取语音验证码中...");
//		
//		String returnStr = "";
//		String resultStr = "";
//		try {
//			//异步调用UMSP服务：获取短信验证码
//			String timeout = ForgetPasswordValidActivity.this.getString(R.string.WebService_Timeout);				
//			String urlPath = ForgetPasswordValidActivity.this.getString(R.string.UMSP_Service_GetVoiceCode);
//
//			Map<String,String> postParams = new HashMap<String,String>();
//			postParams.put("mobile", phone);
//	
//			String responseStr = "";
//			try {
//				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//				
//				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8");
//				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//			} catch (final Exception e) {
//				closeProgDlg();
//				if(e.getMessage().indexOf("peer")!=-1)
//					mError = "无效的服务器请求";
//				else				 
//				    mError = "网络连接异常或无法访问服务";
//				
//				throw new Exception(mError);	
//			}
//
//			JSONObject jb = JSONObject.fromObject(responseStr);
//		    resultStr = jb.getString(CommonConst.RETURN_CODE);
//			returnStr = jb.getString(CommonConst.RETURN_MSG);
//				
//			if (resultStr.equals("0")) {	
//				closeProgDlg();
//				mError = "提交已发送，请等待";
//				Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
//				showCountDown(COUNT_DOWN_NUM);   //显示倒计时
//				return;
//			}
//			else if(resultStr.equals("1001"))
//				mError = "验证服务请求错误";
//			else if(resultStr.equals("10003"))
//				mError = "内部处理错误";
//			else
//				mError = returnStr;
//			
//		} catch (Exception exc) {
//			closeProgDlg();
//			mError = exc.getMessage();
//		}
//
//		closeProgDlg();
//		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
//	}
	
	
	
	
	private  boolean  verifyMobile(String phone,String msgCode) throws Exception{
		showProgDlg("验证手机号中...");
		
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = ForgetPasswordValidActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ForgetPasswordValidActivity.this.getString(R.string.UMSP_Service_VerifyMobile);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("mobile", phone);
			postParams.put("validationCode", msgCode);
			
			String responseStr = "";
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				
				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8")+
                                   "&validationCode="+URLEncoder.encode(msgCode, "UTF-8");
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
				closeProgDlg();
				if(e.getMessage().indexOf("peer")!=-1)
					mError = "无效的服务器请求";
				else				 
				    mError = "网络连接异常或无法访问服务";
				
				throw new Exception(mError);	
			}

			JSONObject jb = JSONObject.fromObject(responseStr);
		    resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);
				
			if (resultStr.equals("0")) 	
				return true;
			else
				mError = returnStr;
			
		} catch (Exception exc) {
			closeProgDlg();
			mError = exc.getMessage();
		}

		closeProgDlg();
		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
		return false;
		
	}
		
	private  void showCountDown(final int countDownNum){
		mButton.setEnabled(false);
		mButton.setText("等待60秒");
		
		timer = new Timer();	
		task =  new TimerTask( ) {
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
			mButton.setText("等待"+count+"秒");
		}
		else{
			mButton.setText("获取验证码");
			mButton.setEnabled(true);	
			timer.cancel( );
			timer = null;
			task.cancel();
			task = null;
			count = COUNT_DOWN_NUM;
		}
	}
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
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
			intent.putExtra("ActName", mActName);	
		    startActivity(intent);
		    ForgetPasswordValidActivity.this.finish();
			break;
		}

		return true;
	}
	
}
