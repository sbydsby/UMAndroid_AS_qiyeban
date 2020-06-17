package com.sheca.umandroid;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.UpdateUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.tencent.android.tpush.XGPushManager;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySettingsActivityNew extends Activity{

	private ProgressDialog progDialog = null;
	private SharedPreferences sharedPrefs;
	private AccountDao accountDao = null;
	
	private List<String> m_Devlst = null;
	private Handler handler = null;
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	private List<Map<String, String>> mData = null;
	private AlertDialog certListDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_settings1);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		((TextView) findViewById(R.id.header_text)).setText("系统设置");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		accountDao = new AccountDao(this);
		
		ht = new HandlerThread("es_device_working_thread");
      	ht.start();
        workHandler = new Handler(ht.getLooper()); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MySettingsActivityNew.this.finish();
			}
		});

		showSettingInfo();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		showSettingInfo();
	}
	
	
    private  void  showSettingInfo(){
    	final Account curAct = accountDao.getLoginAccount();
    	
    	RelativeLayout rl1 = (RelativeLayout) this.findViewById(R.id.relativeLayout2);  //设置证书类别
		rl1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, SettingCertTypeActivity.class);
				i.putExtra("certType", curAct.getCertType());
				startActivity(i);
			}
		});
		
		if(CommonConst.SAVE_CERT_TYPE_RSA == curAct.getCertType())			
			((TextView) findViewById(R.id.textCertType)).setText(CommonConst.CERT_RSA_NAME);
		else
			((TextView) findViewById(R.id.textCertType)).setText(CommonConst.CERT_SM2_NAME);
		
		RelativeLayout rl2 = (RelativeLayout) this.findViewById(R.id.relativeLayout3);  //设置证书介质
		rl2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, SettingSaveTypeActivity.class);
				i.putExtra("saveType", curAct.getSaveType());
				startActivity(i);
			}
		});
		
		if(CommonConst.SAVE_CERT_TYPE_PHONE == curAct.getSaveType())			
			((TextView) findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
		else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == curAct.getSaveType())
			((TextView) findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
		else if(CommonConst.SAVE_CERT_TYPE_AUDIO == curAct.getSaveType())
			((TextView) findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_AUDIO_NAME);
		else if(CommonConst.SAVE_CERT_TYPE_SIM == curAct.getSaveType())
			((TextView) findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);

		RelativeLayout rl3 = (RelativeLayout) this.findViewById(R.id.relativeLayout5);  //版本信息
		rl3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, SettingVersionActivity.class);
				startActivity(i);
			}
		});

		
		RelativeLayout rlabout = (RelativeLayout) this.findViewById(R.id.relativeLayout6);  //关于
		rlabout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, AboutActivity.class);
				//Intent i = new Intent(MySettingsActivityNew.this, net.sourceforge.simcpux.wxapi.WXEntryActivity.class);
				//Intent i = new Intent(MySettingsActivityNew.this, com.sheca.umandroid.PayActivity.class);
				startActivity(i);
			}
		});

		RelativeLayout rl5 = (RelativeLayout) this.findViewById(R.id.relativeLayout1);  //用户协议
		rl5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, UserProtocolActivity.class);
				startActivity(i);
			}
		});
		
		
		RelativeLayout rl6 = (RelativeLayout) this.findViewById(R.id.rlabout);  //检查更新
		//rl6.setVisibility(RelativeLayout.GONE);
		rl6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(context,"正在获取更新版本", Toast.LENGTH_SHORT).show();
				showProgDlg("正在获取更新版本...");
				try{
					if (android.os.Build.VERSION.SDK_INT > 9) {
						StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
						StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
					}
					
					 new Thread(new Runnable(){
				            public void run() {
							try {
								if(isNetworkAvailable(MySettingsActivityNew.this) == false){
									workHandler.post(new Runnable() {
									  public void run(){
									    closeProgDlg();
						    		    Toast.makeText(MySettingsActivityNew.this,"网络连接异常或无法访问更新服务", Toast.LENGTH_SHORT).show();
								      }
							         }); 
						    		return;
						    	}else{
						    		Thread.sleep(1000);
						    		workHandler.post(new Runnable(){
									   public void run(){
										   LaunchActivity.updateuUtil = new UpdateUtil(MySettingsActivityNew.this,true);
										   LaunchActivity.updateuUtil.getServerVersion();
									       if(!LaunchActivity.updateuUtil.checkToUpdate())
										      Toast.makeText(MySettingsActivityNew.this,"已是最新版本", Toast.LENGTH_SHORT).show();
									      closeProgDlg();
									    }
								    }); 
						    	}		
							} catch (Exception e) {
								e.printStackTrace();
								workHandler.post(new Runnable() {
								   public void run(){
								     closeProgDlg();
								   }
							    }); 
							}
						}
					}).start();
				}catch(Exception ex){
					ex.printStackTrace();
					workHandler.post(new Runnable() {
						public void run(){
						   closeProgDlg();
					    }
					}); 
				}
			}
		});
		
		final boolean isNotification = sharedPrefs.getBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, true);   //上传使用记录开关默认开启
		if (!isNotification) 
			((TextView) findViewById(R.id.textLogType)).setText("不上传");
		else
			((TextView) findViewById(R.id.textLogType)).setText("上传");
		
		RelativeLayout rl7 = (RelativeLayout) this.findViewById(R.id.relativeLayout7);  //设置使用记录开关
		rl7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, SettingLogUploadTypeActivity.class);
				i.putExtra("logType", isNotification);
				startActivity(i);
			}
		});
		final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
		final boolean isNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);   //ifaa指纹开关默认关闭
		if (!isNotificationFinger) 
			((TextView) findViewById(R.id.textFingerType)).setText("关闭");
		else
			((TextView) findViewById(R.id.textFingerType)).setText("开启");
		
		RelativeLayout rlFinger = (RelativeLayout) this.findViewById(R.id.relativeLayoutFinger);  //设置ifaa指纹开关
		rlFinger.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MySettingsActivityNew.this, SettingFingerTypeActivity.class);
				i.putExtra("fingerType", isNotificationFinger);
				startActivity(i);
			}
		});
		
		if(LaunchActivity.isIFAAFingerUsed){
		    rlFinger.setVisibility(RelativeLayout.VISIBLE);
		}else{
//			if(AuthenticatorManager.isSupportIFAA(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT)){
//				rlFinger.setVisibility(RelativeLayout.VISIBLE);
//			}else{
//			    rlFinger.setVisibility(RelativeLayout.GONE);
//			}
		}
		
		findViewById(R.id.relativeLayoutLogout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				logoutAccount();
			}
		});

	}
    
    private  void  logoutAccount(){
		final Handler handler = new Handler(MySettingsActivityNew.this.getMainLooper());
		
		if(accountDao.count() == 0){  //账户未登录
			Toast.makeText(MySettingsActivityNew.this, "账户已退出", Toast.LENGTH_SHORT).show();
		}
		else{
		   Builder builder = new Builder(MySettingsActivityNew.this);		
		   builder.setIcon(R.drawable.alert);
		   builder.setTitle("提示");
		   builder.setMessage("确定退出此账户？");							
		   builder.setNegativeButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	 
							//不能在主线程中请求HTTP请求   
				               try {
				                  handler.post(new Runnable() {
									 @Override
									 public void run() {
										showProgDlg("账户退出中...");
										unregisterXGPush(); 
	
										try{
											//loginUMSPService(accountDao.getLoginAccount().getName()); 
											
										   //异步调用UMSP服务：用户注销
										   final String timeout = MySettingsActivityNew.this.getString(R.string.WebService_Timeout);				
										   final String urlPath = MySettingsActivityNew.this.getString(R.string.UMSP_Service_Logout);										
										   final Map<String,String> postParams = new HashMap<String,String>();							
					                	   //final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					                	   
					                	   String postParam = "";
					                	   final  String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
					                	
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
											
											  closeProgDlg();
											  Toast.makeText(MySettingsActivityNew.this, "账户退出成功", Toast.LENGTH_SHORT).show();	
											  Intent intent = new Intent(MySettingsActivityNew.this, MainActivity.class);
											  startActivity(intent);
											  MySettingsActivityNew.this.finish();
												
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

											  closeProgDlg();
											  Toast.makeText(MySettingsActivityNew.this, "账户退出成功", Toast.LENGTH_SHORT).show();
											  Intent intent = new Intent(MySettingsActivityNew.this, MainActivity.class);
											  startActivity(intent);
											  MySettingsActivityNew.this.finish();
												
											  //清空本地缓存
											  //WebClientUtil.cookieStore = null;
											  //若账号未登录，跳转到登录页面
										  } else {
											  Account curAct = accountDao.getLoginAccount();
											  curAct.setStatus(-1);   //重置登录状态为未登录状态
											  curAct.setCopyIDPhoto("");
											  accountDao.update(curAct);
										
											  closeProgDlg();
											  Toast.makeText(MySettingsActivityNew.this, "账户退出成功", Toast.LENGTH_SHORT).show();
											  Intent intent = new Intent(MySettingsActivityNew.this, MainActivity.class);
											  startActivity(intent);
											  MySettingsActivityNew.this.finish();
												
											  //清空本地缓存
											  //WebClientUtil.cookieStore = null;
											  //跳转
											  //Intent intent = new Intent(AccountActivity.this, MainActivity.class);
											  //intent.putExtra("Message", "用户注销成功");
											  //startActivity(intent);
											  //throw new Exception("调用UMSP服务之用户注销失败：" + returnStr);
										  }
										}catch(Exception ex){
											Account curAct = accountDao.getLoginAccount();
											curAct.setStatus(-1);   //重置登录状态为未登录状态
											curAct.setCopyIDPhoto("");
											accountDao.update(curAct);										
										
											closeProgDlg();
											Toast.makeText(MySettingsActivityNew.this, "账户退出成功", Toast.LENGTH_SHORT).show();
											Intent intent = new Intent(MySettingsActivityNew.this, MainActivity.class);
											startActivity(intent);
											MySettingsActivityNew.this.finish();												
										}
									 }	
				                  }); 							
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
												   Toast.makeText(MySettingsActivityNew.this, "账户退出成功", Toast.LENGTH_SHORT).show();
												   Intent intent = new Intent(MySettingsActivityNew.this, MainActivity.class);
												   startActivity(intent);
												   MySettingsActivityNew.this.finish();
												}
										}); 
										//清空本地缓存
										//WebClientUtil.cookieStore = null;
										//跳转
										//Intent intent = new Intent(AccountActivity.this, MainActivity.class);
										//intent.putExtra("Message", "用户注销成功");	
				               }
     
				               dialog.dismiss();
				            
				               DaoActivity.strAccountName = "";
				               DaoActivity.strAccountPwd = "";   
				            
				               Editor editor = sharedPrefs.edit();		
				    		   editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
				      	       editor.commit();
				      	    
				      	       JShcaEsStd gEsDev = JShcaEsStd.getIntence(MySettingsActivityNew.this); 
							   gEsDev.disconnect();
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
	
    private Boolean loginUMSPService(String act){    //重新登录UM Service
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = MySettingsActivityNew.this.getString(R.string.WebService_Timeout);				
				String urlPath = MySettingsActivityNew.this.getString(R.string.UMSP_Service_Login);
	
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", act);
				postParams.put("pwdHash", getPWDHash(accountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
				if(accountDao.getLoginAccount().getType() == 1)
				    postParams.put("appID", CommonConst.UM_APPID);
				else
					postParams.put("appID", accountDao.getLoginAccount().getAppIDInfo());
	
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout)/6);
					
					String postParam = "";
					if(accountDao.getLoginAccount().getType() == 1)
						postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
							        "&pwdHash="+URLEncoder.encode(getPWDHash(accountDao.getLoginAccount().getPassword()), "UTF-8")+
					                "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
					else
						postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
				                    "&pwdHash="+URLEncoder.encode(getPWDHash(accountDao.getLoginAccount().getPassword()), "UTF-8")+
		                            "&appID="+URLEncoder.encode(accountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
					
					responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout)/6);
				} catch (Exception e) {
					if(null== e.getMessage())
					   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
					else
					  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
				}
	
				JSONObject jb = JSONObject.fromObject(responseStr);
				String resultStr = jb.getString(CommonConst.RETURN_CODE);
				returnStr = jb.getString(CommonConst.RETURN_MSG);

				if(!"0".equals(resultStr))
				    throw new Exception(resultStr+":" + returnStr);
				//Toast.makeText(LaunchActivity.this,resultStr+":"+returnStr ,Toast.LENGTH_LONG).show();
			} catch (Exception exc) {
				return false;
			}
			
			return true;
	}
 
	private  String   getPWDHash(String strPWD){
		String strPWDHash = "";
		
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		return strPWDHash;
	}
    
	private  void  unregisterXGPush(){    //反注册信鸽推送SDK	    	
    	 XGPushManager.unregisterPush(getApplicationContext());
    	 
    	 //Toast.makeText(AccountActivity.this, "Logout 成功", Toast.LENGTH_SHORT).show();
    }
    
    //检测网络是否连接
    private  boolean isNetworkAvailable(Context context) {
    	try{
    		ConnectivityManager cm = (ConnectivityManager)context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
	
    		return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
	}
    
    private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(MySettingsActivityNew.this);
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
	

}
