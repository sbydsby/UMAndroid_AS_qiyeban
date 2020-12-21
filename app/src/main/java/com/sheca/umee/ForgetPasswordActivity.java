package com.sheca.umee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.intsig.idcardscancaller.CardScanActivity;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.model.Cert;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetPasswordActivity extends Activity {
	private EditText mPhoneNo;         //用户手机号码
	private EditText mPhoneCode;       //账户验证码	
	
	private Button   mButton = null;
	private Button   mButtonNext = null;
	
	private CertDao certDao = null;
	private ProgressDialog progDialog = null;

	private String mTaskID ="";
	private  String  mError = "";
	private boolean mIsDao = false;   //第三方接口调用标记
	
	private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
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
		setContentView(R.layout.activity_forget_password);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("找回密码");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ForgetPasswordActivity.this.finish();
			}
		});

		certDao = new CertDao(ForgetPasswordActivity.this);		

		mPhoneNo = (EditText) findViewById(R.id.account);
		mPhoneNo.setText("");
		mPhoneCode = (EditText) findViewById(R.id.et_sms); 
		mPhoneCode.setText("");
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("ActName")!=null){
				mPhoneNo.setText(extras.getString("ActName"));
			}
			if(extras.getString("mesage")!=null){
				mIsDao = true;
			}
		}
		
		mPhoneNo.requestFocus();
		
		mButton = (Button) findViewById(R.id.btn_sms);  
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getNextView();	
			}
		});
		
		mButtonNext = (Button) findViewById(R.id.btnNext);    //找回登录密码
		mButtonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showSetPwdView();	
			}
		});
		 

	}
	
	private void getMsgCode(){
		final Handler handler = new Handler(ForgetPasswordActivity.this.getMainLooper());		

	    new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	handler.post(new Runnable(){
						 @Override
							public void run(){ 
							  getValidationCode(mPhoneNo.getText().toString());
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
			String timeout = ForgetPasswordActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ForgetPasswordActivity.this.getString(R.string.UMSP_Service_GetValidationCode);

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
	
	
	private void  getNextView(){
	    String account = mPhoneNo.getText().toString();
		boolean cancel = false;
		View focusView = null;

		mPhoneNo.setError(null);
		// Check for a valid account name.
		if (TextUtils.isEmpty(account)) {
			mPhoneNo.setError(getString(R.string.error_account_required));
			focusView = mPhoneNo;
			cancel = true;
		} else if (!isAccountValid(account)) {
			mPhoneNo.setError(getString(R.string.error_invalid_account));
			focusView = mPhoneNo;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first form field with an error.
			focusView.requestFocus();
		} else{		
			getMsgCode();
			
			/* 
           	               try {  	
				               if(getTaskGUIDWithoutLogin(account)){   //获取业务流水号
				            	  //发送短信验证码
						          showValidPasswordActicity(account);					
				            	 
					              if(getCertCount(account) > 0 ){     //判断本地是否有用户证书
					            	 Intent intent = new Intent(ForgetPasswordActivity.this, ForgetPasswordCertActivity.class);    //有证书进行签名登录
					            	 intent.putExtra("Account", account);
					            	 intent.putExtra("TaskID", mTaskID);
					            	 if(mIsDao)
					            		 intent.putExtra("message", "dao");
						             startActivity(intent);
					                 ForgetPasswordActivity.this.finish();
					              }else{   //无证书判断是否已实名认证
						             if(isAccountReal(account)){   //判断账户是否已实名认证
						                 //人脸识别							             
						            	 showFaceRecognitionActicity(account);							            
						             }else{
						                //发送短信验证码
							            showValidPasswordActicity(account);							          
						             }
					              }
				               }else{
					              Toast.makeText(ForgetPasswordActivity.this, "获取业务流水号失败,请重试!", Toast.LENGTH_SHORT).show();
				               }    	           
	           	            } catch (final Exception exc) {
								   Toast.makeText(ForgetPasswordActivity.this, exc.getMessage(), Toast.LENGTH_SHORT).show();		 
	                        }
	                  */

		}
		
	}
	
	private void  showSetPwdView(){
		final String msgPhoneCode = mPhoneCode.getText().toString();
		final String account = mPhoneNo.getText().toString();
		boolean cancel = false;
		View focusView = null;
		
		mPhoneCode.setError(null);
		mPhoneNo.setError(null);
		
		// Check for a valid account name.
		if (TextUtils.isEmpty(account)) {
			mPhoneNo.setError(getString(R.string.error_account_required));
			focusView = mPhoneNo;
			cancel = true;
		} else if (!isAccountValid(account)) {
			mPhoneNo.setError(getString(R.string.error_invalid_account));
			focusView = mPhoneNo;
			cancel = true;
		}

		if (TextUtils.isEmpty(msgPhoneCode)) {
			mPhoneCode.setError(getString(R.string.error_invalid_code));
			focusView = mPhoneCode;
			cancel = true;
		}
		if (cancel) {
			// There was an error; don't attempt login and focus the first form field with an error.
			focusView.requestFocus();
		} else{
			final Handler handler = new Handler(ForgetPasswordActivity.this.getMainLooper());		

		    new Thread(new Runnable(){
		            @Override
		            public void run() {
		            	handler.post(new Runnable(){
							 @Override
								public void run(){ 
								 try {  	
					                  if(verifyMobile(account,msgPhoneCode)){
						                 Intent intent = new Intent(ForgetPasswordActivity.this, SetPasswordActivity.class);    
						                 intent.putExtra("ActName", account);	
						                 if(mIsDao)
						                	 intent.putExtra("message", "dao");
					                     startActivity(intent);
					                     ForgetPasswordActivity.this.finish();				
					                  }
		           	                } catch (final Exception exc) {
									     Toast.makeText(ForgetPasswordActivity.this, exc.getMessage()+"", Toast.LENGTH_SHORT).show();		 
		                            }
							 }
		            	});
		            }
		    }).start();
      
		}
	}
	
	private  boolean  verifyMobile(String phone,String msgCode) throws Exception{
		showProgDlg("验证手机号中...");
		
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = ForgetPasswordActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ForgetPasswordActivity.this.getString(R.string.UMSP_Service_VerifyMobile);

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
		
	/**
	 * 账号必须为手机号和电子邮箱。
	 */
	private boolean isAccountValid(String account) {
		boolean isValid = false;
		//手机号码
		//移动：134[0-8],135,136,137,138,139,150,151,157,158,159,182,187,188 
		//联通：130,131,132,152,155,156,185,186 
		//电信：133,1349,153,180,189 
		//String MOBILE = "^1(3[0-9]|5[0-35-9]|8[025-9])\\d{8}$";
		String MOBILE = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$";
		Pattern mobilepattern = Pattern.compile(MOBILE); 
		Matcher mobileMatcher = mobilepattern.matcher(account);
		//邮箱
		//p{Alpha}：内容是必选的，和字母字符[\p{Lower}\p{Upper}]等价。
		//w{2,15}：2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。
		//[a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的。
		//[.]：'.'号时必选的。
		//p{Lower}{2,}：小写字母，两个以上。
		String EMAIL = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
		Pattern emailpattern = Pattern.compile(EMAIL); 
		Matcher emailMatcher = emailpattern.matcher(account);
		//验证正则表达式
		if(mobileMatcher.matches() || emailMatcher.matches()) {
		      isValid = true;
		}
		
		String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
	    if (account.length() != 11) {
	        return false;
	    } else {
	        Pattern p = Pattern.compile(regex);
	        Matcher m = p.matcher(account);
	        isValid = m.matches();
	    }
	    
		return isValid; 
	}
	
	
	private Boolean getTaskGUIDWithoutLogin(String strActName) throws Exception{
		//showProgDlg("获取流水号中...");
		
		String timeout = ForgetPasswordActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ForgetPasswordActivity.this.getString(R.string.UMSP_Service_GetTaskGUIDWithoutLogin);		
		
		Map<String,String> postParams = new HashMap<String,String>();	
		//postParams.put("accountName", strActName);
		//postParams.put("appID", CommonConst.UM_APPID);   

		String responseStr = "";
		try {
			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			String postParam = "";
	        responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		
		    JSONObject jb = JSONObject.fromObject(responseStr);
		    String resultStr = jb.getString(CommonConst.RETURN_CODE);
		    String returnStr = jb.getString(CommonConst.RETURN_MSG);

		    if (resultStr.equals("0")) {
			   JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
			   mTaskID = jbRet.getString(CommonConst.PARAM_TASK_GUID);
		    } else {
			   closeProgDlg();
			   if(resultStr.equals("10007"))
			      throw new Exception("账户不存在");
			   else
				  throw new Exception("该账户不存在或账户异常");
		   }
		 
		   closeProgDlg();		
		} catch (Exception e) {
			  closeProgDlg();
			  throw new Exception("获取业务流水号失败:网络连接或访问服务异常!");
		}

    	return true;
	}
	
	
	private Boolean isAccountReal(String strActName) throws Exception{
		//showProgDlg("获取账户信息中...");
		
		String timeout = ForgetPasswordActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ForgetPasswordActivity.this.getString(R.string.UMSP_Service_IsAccountReal);		
		
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("accountName", strActName);
		postParams.put("appID", CommonConst.UM_APPID);   

		String responseStr = "";
		try {
			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			
			String postParam = "accountName="+URLEncoder.encode(strActName, "UTF-8")+
			                   "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		} catch (final Exception e) {
			  closeProgDlg();
			  if(e.getMessage().indexOf("peer")!=-1)
			     throw new Exception("无效的服务器请求");
			  else				    
			     throw new Exception("网络连接或访问服务异常:"+e.getMessage());
		}

		JSONObject jb = JSONObject.fromObject(responseStr);
		String resultStr = jb.getString(CommonConst.RETURN_CODE);
		String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		if (resultStr.equals("0")) {
			closeProgDlg();
			JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
			returnStr = jbRet.getString(CommonConst.PARAM_ISREAL);
			if(returnStr.equals("1"))
				return true;
			
			return false;
		}else{
			closeProgDlg();
			throw new Exception( returnStr);
		}
	}
	
	private int getCertCount(String strActName) {
		int nCount = 0;
		for(Cert cert : certDao.getAllCerts(strActName)){
			if(null == cert.getCertificate() ||"".equals(cert.getCertificate()))
				continue;
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH ==  cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM ==  cert.getSavetype())
				continue;
			if(cert.getEnvsn().indexOf("-e")!=-1)
				continue;
			if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT)
				nCount++;
		}
		
	    return  nCount;
	}
	
	
	private  void  showFaceRecognitionActicity(String strActName){
		Intent inet = new Intent(ForgetPasswordActivity.this, CardScanActivity.class);    //有证书进行签名登录
		inet.putExtra("Account", strActName);
		inet.putExtra("BizSN", mTaskID);
		inet.putExtra("Reset", "reset");
		if(mIsDao)
			inet.putExtra("message", "dao");
	    startActivity(inet);
	    ForgetPasswordActivity.this.finish();		
	}
		
	private  void  showValidPasswordActicity(String strActName){
		Intent inet = new Intent(ForgetPasswordActivity.this, ForgetPasswordValidActivity.class);    //有证书进行签名登录
		inet.putExtra("Account", strActName);
		if(mIsDao)
			inet.putExtra("message", "dao");
	    startActivity(inet);
	    ForgetPasswordActivity.this.finish();	
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
	
}
