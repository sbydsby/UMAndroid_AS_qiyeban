package com.sheca.umandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordActivity extends Activity {
	private ChangePasswordTask mTask = null;
	
	// UI references.
	private EditText mOriginalPasswordView;
	private EditText mNewPasswordView;	
	private EditText mNewPassword2View;
	
	//DB Access Object
	private AccountDao accountDao = null;
	private CertDao    mCertDao = null;
	
	private String strAccount = "";
	private String strActName = "";
	private String strActIdentityCode = "";
	private String strActCopyIDPhoto = "";
	private String strAppInfo = "";
	private String strOrgName = "";
	private int    actType = 1;
	
	private String strErr = "";
	
	private boolean mIsDao     = false;   //第三方接口调用标记
	private boolean mIsScanDao = false;   //第三方扫码接口调用标记

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			setContentView(R.layout.activity_password);
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
			
			((TextView) findViewById(R.id.header_text)).setText("设置账户密码");
			Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
			((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
			TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
			tp.setFakeBoldText(true); 

			ImageButton cancelScanButton = (ImageButton) this
					.findViewById(R.id.btn_goback);

			cancelScanButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PasswordActivity.this.finish();
				}
			});
			
			Bundle extras = getIntent().getExtras(); 
			strAccount = extras.getString("Account");

			if(extras.getString("message")!=null)
				mIsDao = true;
			if(extras.getString("scan")!=null){
			    mIsScanDao = true;
			    cancelScanButton.setVisibility(RelativeLayout.GONE);
			}
			
			accountDao = new AccountDao(PasswordActivity.this);
			mCertDao = new CertDao(PasswordActivity.this);
//
//			if(accountDao.getLoginAccount().getActive() == 0)  //账户未激活
//				((TextView) findViewById(R.id.header_text)).setText("修改登录密码(账户未激活)");
			
			strActName = accountDao.getLoginAccount().getIdentityName();
			strActIdentityCode = accountDao.getLoginAccount().getIdentityCode();
			strActCopyIDPhoto = accountDao.getLoginAccount().getCopyIDPhoto();
			strAppInfo = accountDao.getLoginAccount().getAppIDInfo();
			actType = accountDao.getLoginAccount().getType();
			strOrgName = accountDao.getLoginAccount().getOrgName();

			mOriginalPasswordView = (EditText) findViewById(R.id.et_original_password);
			mOriginalPasswordView.setText("");
			mOriginalPasswordView.requestFocus();
			mOriginalPasswordView.setFocusable(true);   
			mOriginalPasswordView.setFocusableInTouchMode(true);   
			
			mNewPasswordView = (EditText) findViewById(R.id.et_new_password);
			mNewPasswordView.setText("");
			mNewPassword2View = (EditText) findViewById(R.id.et_new_password2);
			mNewPassword2View.setText("");
			
			findViewById(R.id.btn_change_password).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					changePassword();
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void changePassword() {
		if (mTask != null) {
			return;
		}
		
		try {
			String  strErr = "";
			
			// Reset errors.
			mNewPasswordView.setError(null);
						
			String originalPassword = mOriginalPasswordView.getText().toString();
			String newPassword = mNewPasswordView.getText().toString();
			String newPassword2 = mNewPassword2View.getText().toString();
			
			boolean cancel = false;
			View focusView = null;

			// 检查用户输入的原密码是否有效
			if(null == originalPassword){
//				strErr = getString(R.string.password_rule);
//				mOriginalPasswordView.setError(getString(R.string.password_rule));
//				focusView = mOriginalPasswordView;
//				cancel = true;
			}
			if (TextUtils.isEmpty(originalPassword)) {
//				strErr = getString(R.string.password_rule);
//				mOriginalPasswordView.setError(getString(R.string.password_rule));
//				focusView = mOriginalPasswordView;
//				cancel = true;
			} 		
			if (!isPasswordValid(originalPassword)) {
//				strErr = getString(R.string.password_rule);
//				mOriginalPasswordView.setError(getString(R.string.password_rule));
//				focusView = mOriginalPasswordView;
//				cancel = true;
			} 		
			
			// 检查用户输入的新密码是否有效
			if(null == newPassword){
				strErr = getString(R.string.password_rule);
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;		
			}
			if (!isPasswordValid(newPassword)) {
				strErr = getString(R.string.password_rule);
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;
			}
			if (TextUtils.isEmpty(newPassword)) {
				strErr = getString(R.string.password_rule);
				mNewPasswordView.setError(getString(R.string.password_rule));
				focusView = mNewPasswordView;
				cancel = true;
			}
			
			// 检查用户两次输入的新密码是否一致
			if (!newPassword.equals(newPassword2)) {
				strErr = getString(R.string.error_inconformity_password);
				mNewPasswordView.setError(getString(R.string.error_inconformity_password));
				focusView = mNewPassword2View;
				cancel = true;
			}
			
			if (cancel) {
				// There was an error; don't attempt continue and focus the first form field with an error.
				focusView.requestFocus();
				Toast.makeText(PasswordActivity.this, strErr, Toast.LENGTH_SHORT).show();
			} else {
				//异步调用UMSP服务：修改口令
				mTask = new ChangePasswordTask(strAccount, originalPassword, newPassword);
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
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {
		private final String mAccount;
		private final String mOriginalPassword;
		private final String mNewPassword;

		ChangePasswordTask(String account, String originalPassword, String newPassword) {
			mAccount = account;
			mOriginalPassword = originalPassword;
			mNewPassword = newPassword;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
//			String responseStr = "";
//			String resultStr = "";
//			String returnStr = "";
//			int certCount = 0;
//
//			try {
//				String timeout = PasswordActivity.this.getString(R.string.WebService_Timeout);
//				String urlPath = PasswordActivity.this.getString(R.string.UMSP_Service_ChangePassword);
//
//				Map<String,String> postParams = new HashMap<String,String>();
//				postParams.put("accountName", mAccount);
//				postParams.put("oldPwdHash", getPWDHash(mOriginalPassword));
//				postParams.put("newPwdHash", getPWDHash(mNewPassword));
//				postParams.put("appID", strAppInfo);
//
//				try {
//					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//					String postParam = "accountName="+URLEncoder.encode(mAccount, "UTF-8")+
//         		                       "&oldPwdHash="+URLEncoder.encode(getPWDHash(mOriginalPassword), "UTF-8")+
//         		                       "&newPwdHash="+URLEncoder.encode(getPWDHash(mNewPassword), "UTF-8")+
//         		                       "&appID="+URLEncoder.encode(strAppInfo, "UTF-8");
//                    responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//				} catch (Exception e) {
//					if(e.getMessage().indexOf("peer")!=-1)
//						strErr = "无效的服务器请求";
//				    else
//					    strErr = "修改用户登录密码失败："+ "网络连接或访问服务异常";
//					throw new Exception(strErr);
//				}
//
//				JSONObject jb = JSONObject.fromObject(responseStr);
//				resultStr = jb.getString(CommonConst.RETURN_CODE);
//				returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//				if (resultStr.equals("0")) {
//					if (accountDao.count() == 0) {
//						//若账号未登录，记录已登录账号，并跳转；
//						accountDao.add( new Account(mAccount, mNewPassword, 1,1,strActName,strActIdentityCode,strActCopyIDPhoto,actType,strAppInfo,strOrgName,CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_PWD));
//					} else {
//						//若远程修改用户登录密码成功，记录新口令到本地，并跳转；
//						Account curAct = accountDao.getLoginAccount();
//						curAct.setPassword(mNewPassword);
//						curAct.setActive(1);   //激活账户
//						accountDao.update(curAct);
//					}
//
//					certCount = getAccountCertCount();  //获取用户已下载证书数量
//					if(!mIsDao){
//						if(mIsScanDao){
//							DaoActivity.bCreated = false;
//							PasswordActivity.this.finish();
//						}else{
//					        Intent intent = new Intent(PasswordActivity.this, AccountActivity.class);
//					        //intent.putExtra("Account", mAccount);
//					        intent.putExtra("Message", "修改登录密码成功");
//					        startActivity(intent);
//					        //Toast.makeText(PasswordActivity.this, "修改登录密码成功", Toast.LENGTH_SHORT).show();
//					        PasswordActivity.this.finish();
//						}
//					}else{
//					  if(mIsScanDao){
//						  DaoActivity.bCreated = false;
//						  PasswordActivity.this.finish();
//					  }else{
//						if(certCount == 0){
//							 Intent inet = new Intent(PasswordActivity.this, CardScanActivity.class);
//							 inet.putExtra("loginAccount", accountDao.getLoginAccount().getIdentityName());
//						     inet.putExtra("loginId", accountDao.getLoginAccount().getIdentityCode());
//							 inet.putExtra("message", "dao");
//							 startActivity(inet);
//							 PasswordActivity.this.finish();
//						}else{
//							/*Intent inet = new Intent(PasswordActivity.this, DaoActivity.class);
//						    inet.putExtra("OperateState", DaoActivity.operateState);
//						    inet.putExtra("OriginInfo", DaoActivity.strResult);
//						    inet.putExtra("ServiecNo", DaoActivity.strServiecNo);
//					        inet.putExtra("AppName", DaoActivity.strAppName);
//						    inet.putExtra("CertSN", DaoActivity.strCertSN);
//						    startActivity(inet);*/
//							DaoActivity.bCreated = false;
//						    PasswordActivity.this.finish();
//						}
//					  }
//					}
//
//				} else if (resultStr.equals("10012")) {
//					//若账号未登录，跳转到登录页面；
//					loginUMSPService(mAccount);
//					/*if(!mIsDao){
//					   Intent i = new Intent(PasswordActivity.this, LoginActivity.class);
//					   startActivity(i);
//					   PasswordActivity.this.finish();
//					}
//					else{
//						 Intent i = new Intent(PasswordActivity.this, LoginActivity.class);
//						 i.putExtra("message", "dao");
//						 startActivity(i);
//						 PasswordActivity.this.finish();
//					}*/
//				} else {
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
//
//			return true;

			String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
			String mActName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME);

			String strInfo = ParamGen.getResetUserPwdParams(mNewPassword, mTokenID,mActName);

			//String strInfo =ParamGen.getChangeUserPwdParams(getApplicationContext(),mOriginalPassword,mNewPassword);
			UniTrust dao = new UniTrust(PasswordActivity.this,false); //UM SDK+调用类，第二参数表示是否显示提示界面
			String responseStr =dao.ResetAccountPassword(strInfo);
			final APPResponse response = new APPResponse(responseStr);
			int resultStr = response.getReturnCode();
			strErr = response.getReturnMsg();
			if(0==resultStr){
				Account curAct = accountDao.getLoginAccount();
				String mhashPass = getPWDHash(mNewPassword);
				curAct.setPassword(mhashPass);
				curAct.setActive(1);   //激活账户
				accountDao.update(curAct);
				return true;
			}else{
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			mTask = null;

			if (success) {
				Toast toast = Toast.makeText(getApplicationContext(),"修改成功", Toast.LENGTH_SHORT);  //显示时间较长
				toast.setGravity(Gravity.CENTER, 0, 0);  // 居中显示
				toast.show();
				finish();
			} else {
				//Toast.makeText(LoginActivity.this, mError, Toast.LENGTH_LONG).show();
				Toast toast = Toast.makeText(getApplicationContext(),"修改失败"+strErr, Toast.LENGTH_LONG);  //显示时间较长
				toast.setGravity(Gravity.CENTER, 0, 0);  // 居中显示 
				toast.show();
				mNewPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mTask = null;
		}
	}
	
	
	private  String   getPWDHash(String strPWD){
		String strPWDHash = "";
		
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		/*try {
			strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return strPWDHash;
	}
	
	private int getAccountCertCount() throws Exception {
		List<Cert> certList = new ArrayList<Cert>();
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
	
		certList = mCertDao.getAllCerts(strActName);

		return certList.size();
	}
	
	private Boolean loginUMSPService(String act){    //重新登录UM Service
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = PasswordActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = PasswordActivity.this.getString(R.string.UMSP_Service_Login);
	
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", act);
				postParams.put("pwdHash", getPWDHash(accountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
				postParams.put("appID", accountDao.getLoginAccount().getAppIDInfo());
				
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					String actpwd = "";
					if(accountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
						actpwd = getPWDHash(accountDao.getLoginAccount().getPassword());
					else
						actpwd = accountDao.getLoginAccount().getPassword();
					
					String postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
		                               "&pwdHash="+URLEncoder.encode(actpwd, "UTF-8")+
		                               "&appID="+URLEncoder.encode(accountDao.getLoginAccount().getAppIDInfo(), "UTF-8");  
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
				
				if (resultStr.equals("0") || resultStr.equals("10010") ){
					//若成功登录，注册已登录账号，并跳转到首页；
					Intent intent = new Intent(PasswordActivity.this, PasswordActivity.class);
					intent.putExtra("Account", act); 
					if(mIsDao)
						intent.putExtra("message", "dao");
					if(mIsScanDao)
						intent.putExtra("scan", "dao");
					startActivity(intent);	
					PasswordActivity.this.finish();
				}else if(resultStr.equals("10009")){
					//若账号口令错误,显示账户登录页面；
					Account curAct = accountDao.getLoginAccount();
					curAct.setStatus(-1);   //重置登录状态为未登录状态
					accountDao.update(curAct);
					
					Intent intent = new Intent(PasswordActivity.this, LoginActivity.class);
					intent.putExtra("AccName", strAccount);
					if(mIsDao)
						intent.putExtra("message", "dao");
					if(mIsScanDao)
						intent.putExtra("scan", "dao");
					startActivity(intent);
					PasswordActivity.this.finish();
				}else {
					throw new Exception(returnStr);
				}

//				try {
//					// Simulate network access.
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					return false;
//				}
	
//				for (String credential : DUMMY_CREDENTIALS) {
//					String[] pieces = credential.split(":");
//					if (pieces[0].equals(mAccount)) {
//						// Account exists, return true if the password matches.
//						return pieces[1].equals(mPassword);
//					}
//				}
				
			} catch (Exception exc) {
				return false;
			}
			
			return true;
	   }
	   
}

