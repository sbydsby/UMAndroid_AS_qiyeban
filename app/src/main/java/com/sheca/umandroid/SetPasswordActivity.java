package com.sheca.umandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

public class SetPasswordActivity extends Activity {
//	private String   mStrAccountName = "";  //账户名称
	private EditText mNewPasswordView;	
	private EditText mNewPassword2View;
	
	private ResetPasswordTask mTask = null;
	private String strErr = "";
	private ProgressDialog progDialog = null;
	private boolean mIsDao = false;   //第三方接口调用标记
	private String retMsg="";
//	private boolean mIsReg = false;       //是否新注册账户
	
	//DB Access Object
	private AccountDao accountDao = null;

//	private final Handler handler = new Handler( ) {
//	    public void handleMessage(Message msg) {
//	        switch (msg.what) {
//	            case 2:
//	            	resetAccountSuccess();
//	            	break;
//	        }
//
//	        super.handleMessage(msg);
//	    }
//
//	};


	@Override
	public void onBackPressed() {
//		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_set_password);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		((TextView) findViewById(R.id.header_text)).setText("重置账户口令");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);
		cancelScanButton.setVisibility(RelativeLayout.GONE);
		
//		Bundle extras = getIntent().getExtras();
//		if (extras != null){
//			if(extras.getString("ActName") != null){
//				mStrAccountName = extras.getString("ActName");
//			}
//			if(extras.getString("mesage")!=null){
//				mIsDao = true;
//			}
//			if(extras.getString("isReg")!=null){
//				mIsReg = true;
//			}
//		}
		
		accountDao = new AccountDao(SetPasswordActivity.this);

		mNewPasswordView = (EditText) findViewById(R.id.et_new_password);
		mNewPasswordView.setText("");
		mNewPasswordView.requestFocus();
		mNewPassword2View = (EditText) findViewById(R.id.et_new_password2);
		mNewPassword2View.setText("");
		
		mNewPasswordView.requestFocus();
		mNewPasswordView.setFocusable(true);   
		mNewPasswordView.setFocusableInTouchMode(true);   

		ImageView mChangePasswordButton = (ImageView) findViewById(R.id.btn_change_password);
		mChangePasswordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				resetPassword();
			}
		});
		
//		if(mIsReg)
			((TextView) findViewById(R.id.header_text)).setText("设置账户口令");
	}
	
	public void resetPassword() {
		if (mTask != null) {
			return;
		}
		
		try {
			// Reset errors.
			mNewPasswordView.setError(null);
						
			String newPassword = mNewPasswordView.getText().toString();
			String newPassword2 = mNewPassword2View.getText().toString();
			
			boolean cancel = false;
			View focusView = null;
			
			// 检查用户输入的新密码是否有效
			if(null == newPassword){
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;		
			}
			if (!isPasswordValid(newPassword)) {
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;
			}
			if (TextUtils.isEmpty(newPassword)) {
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;
			}
			
			// 检查用户两次输入的新密码是否一致
			if (!newPassword.equals(newPassword2)) {
				mNewPasswordView.setError(getString(R.string.error_inconformity_password));
				focusView = mNewPassword2View;
				cancel = true;
			}
			
			if (cancel) {
				// There was an error; don't attempt continue and focus the first form field with an error.
				focusView.requestFocus();
			} else {
				//异步调用UMSP服务：修改口令
				showProgress(true);
				mTask = new ResetPasswordTask(newPassword);
				mTask.execute((Void) null);
			}
		} catch (Exception exc) {
			Log.e(CommonConst.TAG, exc.getMessage(), exc);
		}
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
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			if(show)
			  showProgDlg("账户口令重置中...");
			else
			  closeProgDlg();
			
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			//mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			if(show)
		       showProgDlg("账户登录中...");
			else
			   closeProgDlg();	
		}
	}

	
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {
		private final String mNewPassword;

		ResetPasswordTask(String newPassword) {
			mNewPassword = newPassword;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
			String mActName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME);

			String strInfo = ParamGen.getResetUserPwdParams(mNewPassword, mTokenID,mActName);

			UniTrust dao = new UniTrust(SetPasswordActivity.this,false); //UM SDK+调用类，第二参数表示是否显示提示界面
			String responseStr = dao.ResetAccountPassword(strInfo);
			final APPResponse response = new APPResponse(responseStr);
			int resultStr = response.getReturnCode();
			  retMsg = response.getReturnMsg();
			if(0==resultStr){
				SharePreferenceUtil.getInstance(getApplicationContext()).setBoolean((CommonConst.FIRST_SMS_LOGIN + mActName),true);
				AccountDao accountDao = new AccountDao(SetPasswordActivity.this);
				Account curAct = accountDao.getLoginAccount();
				String mhashPass = CommUtil.getPWDHash(mNewPassword);
				curAct.setPassword(mhashPass);
				curAct.setActive(1);   //激活账户
				accountDao.update(curAct);
				return true;
			}else{
				return false;
			}


//			String responseStr = "";
//			String resultStr = "";
//			String returnStr = "";
//
//			try {
//				String timeout = SetPasswordActivity.this.getString(R.string.WebService_Timeout);
//				String urlPath = SetPasswordActivity.this.getString(R.string.UMSP_Service_SetAccountPassword);
//
//				Map<String,String> postParams = new HashMap<String,String>();
//				postParams.put("pwdHash", getPWDHash(mNewPassword));
//				postParams.put("appID", CommonConst.UM_APPID);
//
//				try {
//					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//					String postParam = "accountName="+URLEncoder.encode(mAccount, "UTF-8")+
//                                       "&pwdHash="+URLEncoder.encode(getPWDHash(mNewPassword), "UTF-8")+
//                                       "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
//					responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//				} catch (Exception e) {
//					//closeProgDlg();
//					if(e.getMessage().indexOf("peer")!=-1)
//						strErr = "无效的服务器请求";
//					else
//					    strErr = "修改用户登录密码失败："+ "网络连接或访问服务异常";
//					throw new Exception(strErr);
//				}
//
//				JSONObject jb = JSONObject.fromObject(responseStr);
//				resultStr = jb.getString(CommonConst.RETURN_CODE);
//				returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//				if (resultStr.equals("0")) {
//					 //closeProgDlg();
//					 Message message = new Message();
//				     message.what = 2;
//				     handler.sendMessage(message);
//				} else {
//					//closeProgDlg();
//					strErr = returnStr;
//					//Toast.makeText(PasswordActivity.this, strErr, Toast.LENGTH_LONG).show();
//					throw new Exception(strErr);
//				}
//
//
//			} catch (Exception exc) {
//				strErr = exc.getMessage();
//				Log.e(CommonConst.TAG, exc.getMessage(), exc);
//				//Toast.makeText(PasswordActivity.this, strErr, Toast.LENGTH_LONG).show();
//				return false;
//			}


		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			mTask = null;
			showProgress(false);

			if (success) {
				Intent intent = new Intent(SetPasswordActivity.this, MainActivity.class);
				startActivity(intent);
				SetPasswordActivity.this.finish();
				Toast.makeText(SetPasswordActivity.this,"密码重置成功", Toast.LENGTH_LONG).show();
			} else {
//				Toast toast = Toast.makeText(SetPasswordActivity.this, strErr, Toast.LENGTH_SHORT);  //显示时间较长
//				toast.setGravity(Gravity.CENTER, 0, 0);  // 居中显示
//				toast.show();
				Toast.makeText(SetPasswordActivity.this,"修改失败"+strErr,Toast.LENGTH_SHORT).show();
				mNewPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mTask = null;
			showProgress(false);
		}
	}
	
//	private  String   getPWDHash(String strPWD){
//		String strPWDHash = "";
//
//		javasafeengine oSE = new javasafeengine();
//		byte[] bText = strPWD.getBytes();
//		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
//		strPWDHash = new String(Base64.encode(bDigest));
//
//		/*try {
//			strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}*/
//		return strPWDHash;
//	}
	
	private void resetAccountSuccess(){


//		if(!mIsReg){
//		   Toast.makeText(SetPasswordActivity.this, "账号口令已重置成功", Toast.LENGTH_SHORT).show();  //显示时间较长
//
//		   Intent intent = new Intent(SetPasswordActivity.this, LoginActivity.class);
//	       intent.putExtra("AccName", mStrAccountName);
//	       intent.putExtra("AccPwd", mNewPasswordView.getText().toString());
//	       if(mIsDao)
//	       	intent.putExtra("message", "dao");
//	       startActivity(intent);
//	       SetPasswordActivity.this.finish();
//		}else{
//			Account  act = accountDao.getLoginAccount();
//			act.setPassword(mNewPasswordView.getText().toString());
//			accountDao.update(act);
//
//			Toast.makeText(SetPasswordActivity.this, "账号口令已设置成功", Toast.LENGTH_SHORT).show();  //显示时间较长
//
//			Intent intent = new Intent(SetPasswordActivity.this, MainActivity.class);
//		    if(mIsDao)
//		       intent.putExtra("message", "dao");
//		    startActivity(intent);
//		    SetPasswordActivity.this.finish();
//		}
			
		/*
		AlertDialog.Builder builder = new Builder(RegAccountActivity.this);
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
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			break;
		}

		return super.onKeyDown(keyCode, event);
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
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			//do something.
			return true;
		}else {
			return super.dispatchKeyEvent(event);
		}
	}

}
