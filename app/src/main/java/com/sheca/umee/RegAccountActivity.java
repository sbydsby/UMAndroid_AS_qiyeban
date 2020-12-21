package com.sheca.umee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.model.Account;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.os.Handler;

public class RegAccountActivity extends Activity {
	private EditText mPhoneNo;         //用户手机号码	
	private EditText mMsgCode;         //短信验证码
	private EditText mPasswordView;    //登录口令
	private EditText mPasswordView2;   //确认口令
	//private View     mProgressView;
	private View     mRegFormView;
	private Button   mButton;
	private int      mValidCount = 0;
	
	private UserRegTask mAuthTask = null;
	private String mError = "";
	private ProgressDialog progDialog = null;
	
	private boolean mIsDao = false;   //第三方接口调用标记
	
	private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
	//private final String APPID = "470BAEE9D98E07F855016CA4A683060E";
	//private final String APPID = "49beb9e1-0a98-4f8d-bba3-832095b656f1";
	//private final String APPID = "fb9cd5a6-95a3-4821-8916-c9048b5b245e";
	
	//用户账户属性全局变量
	private  int     m_ActState;         //用户状态
	private  String  m_ActName;          //用户姓名
	private  String  m_ActIdentityCode;  //用户身份证
	
	//DB Access Object
	private AccountDao mAccountDao = null;
	private CertDao    mCertDao = null;
	
	int count = COUNT_DOWN_NUM;
	Handler mHandler = new Handler();

	Runnable mRunnable = new Runnable() {
		public void run() {
			// 假设mButton就是你说的那个button
			mButton.setText("获取验证码");
			mButton.setEnabled(true);
		}
	};

	private Timer timer = new Timer( );
	private TimerTask task = null;

	final Handler handler = new Handler( ) {
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case 1:
	                update();
	                break;
	            case 2:
	            	RegAccountSuccess();
	            	break;
	        }
	       
	        super.handleMessage(msg);
	    }

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_register_account);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		((TextView) findViewById(R.id.header_text)).setText("注册账户");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RegAccountActivity.this.finish();
			}
		});
		
		if (Build.VERSION.SDK_INT > 9) {
    	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
    	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    	 }
				
		mPhoneNo = (EditText) findViewById(R.id.account);
		mMsgCode = (EditText) findViewById(R.id.phonecode);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView2 = (EditText) findViewById(R.id.password2);
		mRegFormView = findViewById(R.id.reg_form);
		//mProgressView = findViewById(R.id.reg_progress);	
		mButton = (Button) findViewById(R.id.btnCode);   //获取验证码 
		
		mPhoneNo.setText("");
		mPhoneNo.requestFocus();
		mPhoneNo.setFocusable(true);   
		mPhoneNo.setFocusableInTouchMode(true);   
		
		mMsgCode.setText("");
		mPasswordView.setText("");
		mPasswordView2.setText("");
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) 
			mIsDao = true;
		
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			    mButton.setText("正在发送...");
				getMsgCode();
			}
		});
		
		ImageView mRegActBtn = (ImageView) findViewById(R.id.btnReg);  //注册账户
		mRegActBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				regNewAccount();
			}
		});
		
		TextView mUserTxt = (TextView) findViewById(R.id.txtUserProl);  //查看用户协议
		mUserTxt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
		mUserTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				 showUserProtocol();
			}
		});
		
		findViewById(R.id.textVoice).setVisibility(RelativeLayout.GONE);
		findViewById(R.id.textVoice).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if("".equals(mPhoneNo.getText().toString())){
					Toast.makeText(RegAccountActivity.this,"请输入账号", Toast.LENGTH_SHORT).show();
					return;
				}

			    new Thread(new Runnable(){
		            @Override
		            public void run() {
		            	handler.post(new Runnable(){
						    @Override
							public void run(){ 
							    getVoiceCode(mPhoneNo.getText().toString());
							}
		            	});
		            }
			    }).start();
			}
		});

	}
	
	private boolean checkAccount(String phone){
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：检测当前账户是否已注册
			String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);
			String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_IsAccountExisted);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("accountName", phone);
			postParams.put("appID", CommonConst.UM_APPID);

			String responseStr = "";
			try {
				//清空本地缓存
				WebClientUtil.cookieStore = null;
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

				String postParam = "accountName="+URLEncoder.encode(phone, "UTF-8")+
	                               "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
				if(e.getMessage().indexOf("peer")!=-1)
					mError = "无效的服务器请求";
				else
				    mError = "网络连接异常或无法访问服务";
				throw new Exception(mError);
			}

			JSONObject jb = JSONObject.fromObject(responseStr);
		    resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);

			if (resultStr.equals("10007")) {	  //账户不存在
				return true;
			}
			else if(resultStr.equals("10008"))
				mError = "账户已存在";
			else if(resultStr.equals("1001"))
				mError = "验证服务请求错误";
			else if(resultStr.equals("10003"))
				mError = "内部处理错误";
			else
				mError = returnStr;

		} catch (Exception exc) {
			mError = exc.getMessage();
		}

        mButton.setText("获取验证码");
		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
		return false;
	}
	
	
	private void getMsgCode(){
		final Handler handler = new Handler(RegAccountActivity.this.getMainLooper());
		final String account = mPhoneNo.getText().toString();
		String strErrTip = "";
		
		boolean cancel = false;
		View focusView = null;
		
		mPhoneNo.setError(null);
		// Check for a valid account name.
		if (TextUtils.isEmpty(account)) {
			mPhoneNo.setError(getString(R.string.error_account_required));
			focusView = mPhoneNo;
			cancel = true;
			
			strErrTip = getString(R.string.error_account_required);
		} else if (!isAccountValid(account)) {
			mPhoneNo.setError(getString(R.string.error_invalid_account));
			focusView = mPhoneNo;
			cancel = true;
			
			strErrTip = getString(R.string.error_invalid_account);
		}
		
		if(cancel) {
			// There was an error; don't attempt login and focus the first form field with an error.
			focusView.requestFocus();
			mButton.setText("获取验证码");
			
			Toast.makeText(this, strErrTip, Toast.LENGTH_SHORT).show();
		} else{
			new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	handler.post(new Runnable(){
						 @Override
							public void run(){
							  if(checkAccount(account)){
			                       getValidationCode(account);
							  }
//
						 }
	            	});
	            }
	        }).start();
		}
	}
	
	
	private  void getValidationCode(String phone){
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);
			String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_GetValidationCode);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("mobile", phone);
			postParams.put("codeType", "0");

			String responseStr = "";
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8")+
                                   "&codeType="+URLEncoder.encode("0", "UTF-8");
                                   //"&authKeyID="+URLEncoder.encode(CommonConst.YGT_APP_AUTH_KEY, "UTF-8");
		        responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
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
				mError = "短信已发送，请等待";
				Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
				//findViewById(R.id.textVoice).setVisibility(RelativeLayout.VISIBLE);
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
			mError = exc.getMessage();
		}

		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
	}
	
	private  void getVoiceCode(String phone){	
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_GetVoiceCode);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("mobile", phone);
	
			String responseStr = "";
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				
				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8");
                responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (final Exception e) {
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
				mError = "提交已发送，请等待";
				Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
				return;
			}
			else if(resultStr.equals("1001"))
				mError = "验证服务请求错误";
			else if(resultStr.equals("10003"))
				mError = "内部处理错误";
			else
				mError = returnStr;
			
		} catch (Exception exc) {
			mError = exc.getMessage();
		}

		Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
	}

	private  void showCountDown(final int countDownNum){
		mButton.setEnabled(false);
		mButton.setText("等待60秒");
		//mHandler.postDelayed(mRunnable, 1000*60);
		timer = new Timer();		
		task = new TimerTask( ) {
		    public void run ( ) {
		        Message message = new Message( );
		        message.what = 1;
		        handler.sendMessage(message);
		    }
		};
		
		timer.schedule(task,0,1000);
		/*final Handler handler = new Handler(RegAccountActivity.this.getMainLooper());
		final Button mMsgCodeBtn = (Button) findViewById(R.id.btnCode);  
		mMsgCodeBtn.setEnabled(false);
		
		new Thread(new Runnable(){
            @Override
            public void run() {
            	try {
            		handler.post(new Runnable(){
						 @Override
							public void run(){   
							    timer = new Timer();
							    timerTask = new TimerTask() {
							            @Override
							            public void run() {
							                if (count >0 ){
							                	mMsgCodeBtn.setText(count+"秒后重新获取");	
							                }
							                else{
							                	mMsgCodeBtn.setEnabled(true);
											    mMsgCodeBtn.setText("获取验证码");
											   // timer.cancel();
											    count = countDownNum;
							                }
							                count --;

							            }
							     };       
							     timer.schedule(timerTask, 0, 1000);   
							}
						}); 
					
            	}catch(Exception exc){
            		exc.printStackTrace();
            	}
            }
        }).start();
		*/
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
	
	private  void regNewAccount(){
		if (mAuthTask != null) {
			return;
		}
		
		mPhoneNo.setError(null);
		mMsgCode.setError(null);
		mPasswordView.setError(null);
		mPasswordView2.setError(null);

		String account = mPhoneNo.getText().toString();
		String password = mPasswordView.getText().toString();
		String password2 = mPasswordView2.getText().toString();
		String msgcode = mMsgCode.getText().toString();
		
		String strErrTip = "";
		
		boolean cancel = false;
		View focusView = null;

		// Check for a valid account name.
		if (TextUtils.isEmpty(account)) {
			mPhoneNo.setError(getString(R.string.error_account_required));
			focusView = mPhoneNo;
			cancel = true;
			
			strErrTip = getString(R.string.error_account_required);
		} else if (!isAccountValid(account)) {
			mPhoneNo.setError(getString(R.string.error_invalid_account));
			focusView = mPhoneNo;
			cancel = true;
			
			strErrTip = getString(R.string.error_invalid_account);
		}
		
		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
			
			strErrTip = getString(R.string.error_invalid_password);
		}
		
		if(!password2.equals(password)){
			mPasswordView2.setError(getString(R.string.error_incorrect_password2));
			focusView = mPasswordView;
			cancel = true;
			
			strErrTip = getString(R.string.error_incorrect_password2);
		}
		
		if(TextUtils.isEmpty(msgcode)){
			mMsgCode.setError(getString(R.string.error_invalid_code));
			focusView = mMsgCode;
			cancel = true;	
			
			strErrTip = getString(R.string.error_invalid_code);
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first form field with an error.
			focusView.requestFocus();
			Toast.makeText(RegAccountActivity.this, strErrTip, Toast.LENGTH_SHORT).show();  
		} else {
			// Show a progress spinner, and kick off a background task to perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserRegTask(account, password,msgcode);
			mAuthTask.execute((Void) null);
		}
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

	/**
	 * 密码由8-16位英文、数字或符号组成。
	 */	
	private boolean isPasswordValid(String password) {
		boolean isValid = false;
		if (password.length() > 7 && password.length() < 17) {
			isValid = true;
		}
		return isValid;
	}

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mRegFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});

			if(show)
			   showProgDlg("账户注册中...");
			else
			   closeProgDlg();
			
			/*mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});*/
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			//mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			if(show)
			   showProgDlg("账户注册中...");
			else
			  closeProgDlg();
			mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRegTask extends AsyncTask<Void, Void, Boolean> {

		private final String mAccount;
		private final String mPassword;
		private final String mMsgCode;

		UserRegTask(String account, String password,String msgcode) {
			mAccount = account;
			mPassword = password;
			mMsgCode = msgcode;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String returnStr = "";
			String resultStr = "";
			try {
				//异步调用UMSP服务：用户注册
				String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_RegisterPersonalAccount);
	
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", mAccount);
				postParams.put("pwdHash", getPWDHash(mPassword));    //账户口令需要HASH并转为BASE64字符串
				postParams.put("appID", CommonConst.UM_APPID);
				postParams.put("validationCode", mMsgCode);
	
				String responseStr = "";
				try {
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					
					String postParam = "accountName="+URLEncoder.encode(mAccount, "UTF-8")+
                                       "&pwdHash="+URLEncoder.encode( getPWDHash(mPassword), "UTF-8")+
                                       "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8")+
                                       "&validationCode="+URLEncoder.encode( mMsgCode, "UTF-8");
					responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
				} catch (Exception e) {
					if(e.getMessage().indexOf("peer")!=-1)
						throw new Exception("无效的服务器请求");
					else					       
					    throw new Exception("用户注册失败：" + "网络连接异常或无法访问服务");
				}
	
				JSONObject jb = JSONObject.fromObject(responseStr);
				resultStr = jb.getString(CommonConst.RETURN_CODE);
				returnStr = jb.getString(CommonConst.RETURN_MSG);
	
				if (resultStr.equals("0")) {
					 Message message = new Message();
				     message.what = 2;
				     handler.sendMessage(message);
				} else{
					throw new Exception(returnStr);
				}
			} catch (Exception exc) {
				if(resultStr.equals("1001")){
					mError = "验证服务请求错误";
				}else if(resultStr.equals("10008")){
					mError = "账户已存在";
				}else if(resultStr.equals("10003")){
					mError = exc.getMessage();
				}else if(resultStr.equals("1212")){
					mError = exc.getMessage();
				}else if(resultStr.equals("1211")){
					mError = exc.getMessage();
				}else{
					if(exc.getMessage().indexOf("peer")!=-1)
						mError = "无效的服务器请求";
					else					    
				        mError = "网络连接异常或无法访问服务";
				}
				//Log.e(CommonConst.TAG, mError, exc);
				return false;
			}
			
				
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				Toast toast = Toast.makeText(getApplicationContext(), mError, Toast.LENGTH_LONG);  //显示时间较长 
				toast.setGravity(Gravity.CENTER, 0, 0);  // 居中显示 
				toast.show();
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	private  String   getPWDHash(String strPWD){
		String strPWDHash = "";
		
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		return strPWDHash;
	}
	
	
	private void RegAccountSuccess(){
		Toast.makeText(RegAccountActivity.this, "账号已注册成功", Toast.LENGTH_LONG).show();  //显示时间较长 
		
		//loginUMSPService();    //直接登录
		
		Intent intent = new Intent(this, LoginActivityV33.class);
	    intent.putExtra("AccName", mPhoneNo.getText().toString());
	    intent.putExtra("AccPwd", mPasswordView.getText().toString());
	    if(mIsDao)
	    	intent.putExtra("message", "dao");
	    startActivity(intent);
		RegAccountActivity.this.finish();
		
		
		/*AlertDialog.Builder builder = new Builder(RegAccountActivity.this);
		builder.setMessage("账号已注册，请登录!");
		builder.setIcon(R.drawable.alert);
		builder.setTitle("提示");
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					RegAccountActivity.this.finish();
				} catch (Exception e) {
					
				}

			}
		});
		*/
		//builder.show();	
	}
	
	
	private Boolean loginUMSPService(){    //重新登录UM Service
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_Login);
	
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", mPhoneNo.getText().toString());
				postParams.put("pwdHash", getPWDHash(mPasswordView.getText().toString()));    //账户口令需要HASH并转为BASE64字符串
	
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					
					String postParam = "accountName="+URLEncoder.encode(mPhoneNo.getText().toString(), "UTF-8")+
                                       "&pwdHash="+URLEncoder.encode( getPWDHash(mPasswordView.getText().toString()), "UTF-8");
					responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
				} catch (Exception e) {
					if(null== e.getMessage())
					   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
					else
					  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
				}
	
				JSONObject jb = JSONObject.fromObject(responseStr);
				String resultStr = jb.getString(CommonConst.RETURN_CODE);
				returnStr = jb.getString(CommonConst.RETURN_MSG);	
				
				if (resultStr.equals("0")) {
					//若成功登录，注册已登录账号，并跳转到首页；
					m_ActState = getPersonalInfo();
					mAccountDao.add( new Account(
							        mPhoneNo.getText().toString(), 
							        mPasswordView.getText().toString(),
							        m_ActState,1,m_ActName,m_ActIdentityCode,"",1,CommonConst.UM_APPID,"",CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_PWD));
					RegAccountActivity.this.finish();
				} else if (resultStr.equals("10010")) {
					//若账号未激活，显示修改初始密码页面；
					m_ActState = getPersonalInfo();
					mAccountDao.add( new Account(mPhoneNo.getText().toString(), 
							                     mPasswordView.getText().toString(), 
							                     m_ActState,0,m_ActName,m_ActIdentityCode,"",1,CommonConst.UM_APPID,"",CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_PWD));
					
					Intent intent = new Intent(RegAccountActivity.this, PasswordActivity.class);
					intent.putExtra("Account", mPhoneNo.getText().toString()); 
					startActivity(intent);
					RegAccountActivity.this.finish();

				} else{
					throw new Exception(returnStr);
				}
				
			} catch (Exception exc) {
				return false;
			}
			
			return true;
	  }
	
	
	private  int  getPersonalInfo(){
		String responseStr = "";
		String resultStr = "";
		String returnStr = "";
		
		int retState = 1;
		
		try {
			String timeout = RegAccountActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = RegAccountActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);
			
			Map<String,String> postParams = new HashMap<String,String>();
			//postParams.put("AccountName", mAccount);
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				
				String postParam = "";
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
				return 1;
			}

			JSONObject jb = JSONObject.fromObject(responseStr);
			resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);

			if (resultStr.equals("0")) {
				JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));				
				retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
				m_ActName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
				m_ActIdentityCode = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号	
			}else {
				m_ActName = "";
				m_ActIdentityCode = "";
			}
		
		} catch (Exception exc) {
			m_ActName = "";
			m_ActIdentityCode = "";
		    return 1;
		}

		return retState;
	}
	
	
	private  void  showUserProtocol(){
		Intent intent = new Intent(this, UserProtocolActivity.class);
	    startActivity(intent);	
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
	
	
}
