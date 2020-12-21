package com.sheca.umee;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

import com.junyufr.szt.util.Base64ImgUtil;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.model.Account;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;
import com.tencent.android.tpush.XGPushManager;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//import android.os.Handler;

public class AccountActivity extends Activity {
	private String  mStrAccount = "";
	
	// UI references.
	private TextView mAccountView;
	private ImageView mArrowGoView;
	private TextView mLogoutView;
	private TextView mAccountChgPwd;
	
	//DB Access Object
	private AccountDao accountDao = null;
	private ProgressDialog progDialog = null;
	private SharedPreferences sharedPrefs;
	
	private String mActIndenyityCode = "";
	
	private boolean mIsReg = false;       //是否新注册账户
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_account);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		((TextView) findViewById(R.id.header_text)).setText("账户信息");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AccountActivity.this.finish();
			}
		});
		
        accountDao = new AccountDao(this);
        LaunchActivity.isIFAAFingerOK = false;
		
		if(accountDao.count() == 0)
		{
			Intent intent = new Intent(AccountActivity.this, LoginActivityV33.class);
			startActivity(intent);	
			AccountActivity.this.finish();
			return;
		}
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("isReg")!=null)
		    	mIsReg = true;    
			
			if(mIsReg){
			   Intent intent = new Intent(AccountActivity.this, SetPasswordActivity.class);	
			   intent.putExtra("isReg", "isReg");
			   intent.putExtra("ActName", accountDao.getLoginAccount().getName());
			   startActivity(intent);		
			   AccountActivity.this.finish();
			   return;	
			}else{
			   if(extras.getString("Message")!=null){
				  LockPatternUtil.setActName(accountDao.getLoginAccount().getName());
				   
				  Toast.makeText(AccountActivity.this, extras.getString("Message"), Toast.LENGTH_LONG).show();
				
				  Intent intent = new Intent(AccountActivity.this, MainActivity.class);													
				  startActivity(intent);		
				  AccountActivity.this.finish();
				  return;
			   }
			}
		}
		
		
		mStrAccount = accountDao.getLoginAccount().getName();
		
		mAccountView = (TextView) findViewById(R.id.tv_account_value);
		mAccountView.setText(mStrAccount);
		mAccountView.setVisibility(TextView.VISIBLE);
		
		/*RelativeLayout rlabout = (RelativeLayout) findViewById(R.id.rl_change_password);
		rlabout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//显示修改密码页面
				Intent intent = new Intent(AccountActivity.this, PasswordActivity.class);
				intent.putExtra("Account", mStrAccount); 
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		mAccountChgPwd = (TextView) findViewById(R.id.tv_change_password);
		mAccountChgPwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//显示修改密码页面
				Intent intent = new Intent(AccountActivity.this, PasswordActivity.class);
				intent.putExtra("Account", mStrAccount); 
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});
		
		mArrowGoView = (ImageView) findViewById(R.id.iv_arrow_go);
		mArrowGoView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//显示修改密码页面
				Intent intent = new Intent(AccountActivity.this, PasswordActivity.class);
				intent.putExtra("Account", mStrAccount); 
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});
		*/
		
		mActIndenyityCode = accountDao.getLoginAccount().getIdentityCode();
		if(null != mActIndenyityCode){
			if(mActIndenyityCode.length() == 15){
				mActIndenyityCode = mActIndenyityCode.substring(0,3)+"********";
				mActIndenyityCode += accountDao.getLoginAccount().getIdentityCode().substring(11);
			}else if(mActIndenyityCode.length() == 18){
				mActIndenyityCode = mActIndenyityCode.substring(0,3)+"***********";
				mActIndenyityCode += accountDao.getLoginAccount().getIdentityCode().substring(14);
			}
		}else
			mActIndenyityCode = "";
		
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY){  //如果是企业账户
			   ((TextView) findViewById(R.id.accnamelabel)).setText("单位名称");
			   ((TextView) findViewById(R.id.accname)).setText(accountDao.getLoginAccount().getOrgName());
			   findViewById(R.id.layout3).setVisibility(RelativeLayout.GONE);
			   findViewById(R.id.layout4).setVisibility(RelativeLayout.GONE);
		}else{  //如果是个人账户
			   findViewById(R.id.layout3).setVisibility(RelativeLayout.VISIBLE);
			   findViewById(R.id.layout4).setVisibility(RelativeLayout.VISIBLE);
			   ((TextView) findViewById(R.id.accname)).setText(accountDao.getLoginAccount().getIdentityName());
			   ((TextView) findViewById(R.id.accno)).setText(mActIndenyityCode);
			   ((TextView) findViewById(R.id.accmobile)).setText(mStrAccount);
			
			   if((!"".equals(accountDao.getLoginAccount().getCopyIDPhoto())) && (null != accountDao.getLoginAccount().getCopyIDPhoto())){
				   Bitmap headMap = Bytes2Bimap(Base64ImgUtil.GenerateImageByte(accountDao.getLoginAccount().getCopyIDPhoto()));
				   ((ImageView)findViewById(R.id.list_image)).setImageBitmap(headMap);
				   ((ImageView)findViewById(R.id.list_image)).invalidate();
			   }
		}
		
		((ImageView) findViewById(R.id.logout)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				logoutAccount();
			}
		});
	
	}
	
	
	private  void  logoutAccount(){
		final Handler handler = new Handler(AccountActivity.this.getMainLooper());
		
		if(accountDao.count() == 0){  //账户未登录
			Toast.makeText(AccountActivity.this, "账户已退出", Toast.LENGTH_SHORT).show();
		}
		else{
		   Builder builder = new Builder(AccountActivity.this);		
		   builder.setIcon(R.drawable.alert);
		   builder.setTitle("提示");
		   builder.setMessage("确定退出此账户？");							
		   builder.setNegativeButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						    showProgDlg("账户退出中...");
							//不能在主线程中请求HTTP请求
				            new Thread(new Runnable(){
				                @Override
				                public void run() {
				                	try {
				                		handler.post(new Runnable() {
											 @Override
												public void run() {
												   unregisterXGPush(); 
												}
										}); 		
				                		
										//异步调用UMSP服务：用户注销
										final String timeout = AccountActivity.this.getString(R.string.WebService_Timeout);				
										final String urlPath = AccountActivity.this.getString(R.string.UMSP_Service_Logout);										
										final Map<String,String> postParams = new HashMap<String,String>();							
					                	//final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					                	
					                	String postParam = "";
					                	final String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
					                	
					                	//处理服务返回值
					                	JSONObject jb = JSONObject.fromObject(responseStr);
					        			String resultStr = jb.getString(CommonConst.RETURN_CODE);
					        			final String returnStr  = jb.getString(CommonConst.RETURN_MSG);	
										if (resultStr.equals("0")) {
											//删除本地存储的当前账户
											//accountDao.deleteByName(mStrAccount);
											//当前账户退出,更新登录状态
											Account curAct = accountDao.getLoginAccount();
											curAct.setStatus(-1);   //重置登录状态为未登录状态
											curAct.setCopyIDPhoto("");
											accountDao.update(curAct);
											
											handler.post(new Runnable() {
												 @Override
													public void run() {
													   closeProgDlg();
													   Toast.makeText(AccountActivity.this, "账户退出成功", Toast.LENGTH_SHORT).show();
			            
											           AccountActivity.this.finish();
													}
											}); 		
											//清空本地缓存
											//WebClientUtil.cookieStore = null;
											//跳转
											//Intent intent = new Intent(AccountActivity.this, MainActivity.class);
											//intent.putExtra("Message", "用户注销成功");
											//startActivity(intent);
										} else if (resultStr.equals("10012")) {
											//删除本地存储的当前账户
											//accountDao.deleteByName(mStrAccount);
											//当前账户退出,更新登录状态
											Account curAct = accountDao.getLoginAccount();
											curAct.setStatus(-1);   //重置登录状态为未登录状态
											curAct.setCopyIDPhoto("");
											accountDao.update(curAct);
											
											handler.post(new Runnable() {
												 @Override
													public void run() {
													   closeProgDlg();
													   Toast.makeText(AccountActivity.this, "账户退出成功", Toast.LENGTH_SHORT).show();
													   
													   AccountActivity.this.finish();
													}
											}); 		
											//清空本地缓存
											//WebClientUtil.cookieStore = null;
											//若账号未登录，跳转到登录页面
										} else {
											Account curAct = accountDao.getLoginAccount();
											curAct.setStatus(-1);   //重置登录状态为未登录状态
											curAct.setCopyIDPhoto("");
											accountDao.update(curAct);
											
											handler.post(new Runnable() {
												 @Override
													public void run() {
													   closeProgDlg();
													   Toast.makeText(AccountActivity.this, "账户退出成功", Toast.LENGTH_SHORT).show();
													   
													   AccountActivity.this.finish();
													}
											}); 		
											//清空本地缓存
											//WebClientUtil.cookieStore = null;
											//跳转
											//Intent intent = new Intent(AccountActivity.this, MainActivity.class);
											//intent.putExtra("Message", "用户注销成功");
											//startActivity(intent);
											//throw new Exception("调用UMSP服务之用户注销失败：" + returnStr);
										}
									} catch (Exception exc) {
										Log.e(CommonConst.TAG, exc.getMessage(), exc);
										
										Account curAct = accountDao.getLoginAccount();
										curAct.setStatus(-1);   //重置登录状态为未登录状态
										curAct.setCopyIDPhoto("");
										accountDao.update(curAct);
										
										handler.post(new Runnable() {
											 @Override
												public void run() {
												   closeProgDlg();
												   Toast.makeText(AccountActivity.this, "账户退出成功", Toast.LENGTH_SHORT).show();
												   
												   AccountActivity.this.finish();
												}
										}); 
										//清空本地缓存
										//WebClientUtil.cookieStore = null;
										//跳转
										//Intent intent = new Intent(AccountActivity.this, MainActivity.class);
										//intent.putExtra("Message", "用户注销成功");
									}
				                }
				            }).start();
				            
				            dialog.dismiss();
				            
				            DaoActivity.strAccountName = "";
				            DaoActivity.strAccountPwd = "";   
				            
				            Editor editor = sharedPrefs.edit();		
				    		editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
				      	    editor.commit();
				      	    

					}
				});
		      builder.setPositiveButton("取消",
				  new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  dialog.dismiss();
					  }
				  });
		     builder.show();
	     }	
	}
	
	private  void  unregisterXGPush(){    //反注册信鸽推送SDK	    	
    	 XGPushManager.unregisterPush(getApplicationContext());
    	 
    	 //Toast.makeText(AccountActivity.this, "Logout 成功", Toast.LENGTH_SHORT).show();
    }
		
	private Bitmap Bytes2Bimap(byte[] b) {   
		if (b.length != 0) {  
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}else {            
			return null;
		} 
	}
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(AccountActivity.this);
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
