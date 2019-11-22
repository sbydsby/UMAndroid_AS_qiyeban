package com.sheca.umandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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

import com.excelsecu.slotapi.EsIBankDevice;
import com.facefr.bean.CollectInfoInstance;
import com.junyufr.szt.activity.AuthMainActivity;
import com.junyufr.szt.instance.BodyCheckThread;
import com.junyufr.szt.instance.UploadPhotoThread;
import com.junyufr.szt.struct.EnumInstance;
import com.junyufr.szt.struct.PersonTask;
import com.junyufr.szt.util.App;
import com.junyufr.szt.util.CustomProgressDialog;
import com.sheca.PKCS10CertificationRequest;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.jshcaksstd.JShcaKsStd;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.DownloadCertResponse;
import com.sheca.umandroid.model.GetPersonalInfo;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.PKIUtil;
import com.sheca.umandroid.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ApplyCertRequestActivity extends com.facefr.activity.BaseActivity implements OnRequestPermissionsResultCallback{
	private CustomProgressDialog mProgressDialog = null;
	private long lastConTime = -1;
	private ProgressDialog progDialog = null;
	private boolean  bfailClicked = false;
	
	private SharedPreferences sharedPrefs;
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	
	private PersonTask person = null;
	private CollectInfoInstance mInfoInstance;
	private String mAccount = "";
	private Bitmap mHeadPhoto = null;
	
	public static String strSignature = "sheca";    //人脸识别的签名
	public static String strSignatureAlgorithm = "1";  //签名算法（1：SHA1withRSA）
	
	private String strPersonName = "";    //用户姓名
	private String strPaperNO = "";       //证件号码
	private String strPaperType = "1";    //证件类型（1：身份证）
	//private String strSignatureAlgorithm = "1";  //签名算法（1：RSAWithSHA1）
	private String strENVSN = "";         //用户姓名
	private String strPersonCardPhoto = "";    //用户身份证照片
	private boolean  bFinish = false;
	
	private boolean mIsDao = false;     //第三方接口调用标记
	private boolean mIsReset = false;   //是否重置密码标记
	private boolean mIsDownload = false; 
	private boolean isPayed = false;    
	private int     mPayType = CommonConst.PAY_TYPE_USE_WX;
	
	private AccountDao mAccountDao = null;
	private CertDao mCertDao = null;
	private LogDao mLogDao = null;
	
	private KeyPair mKeyPair = null;  
	private String  mContainerid = "";    
	private int     mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
	private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
	private String  mStrBTDevicePwd = "";    //蓝牙key密码
	
	private String mError = "";
	private String strReqNumber = "";
	private String strStatus = "";
	//private IWXAPI api;
	private String  out_trade_no;
	
	private  JShcaEsStd gEsDev = null;
	private  JShcaUcmStd gUcmSdk = null;
    
	public  static int  failCount = 0;   //人脸识别失败次数计数器
	
	//证书下载状态
	private final int  FACE_AUTH_LOADING = 1;
	private final int  CERT_APPLY_LOADING = 2;
	private final int  CERT_DOWNLOAD_LOADING = 3;
	private final int  CERT_SAVE_LOADING = 4;
	private final int  CERT_SAVE_OK = 5;
	private final int  FACE_AUTH_ERR = -1;
	
	static {
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(LaunchActivity.LOG_FLAG)
	           LaunchActivity.logUtil.recordLogServiceLog("ApplyCertRequestActivity.onCreate");     
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_auth_result);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		((TextView) findViewById(R.id.header_text)).setText("申请证书");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		gEsDev = JShcaEsStd.getIntence(this); 
		//if(null == ScanBlueToothSimActivity.gKsSdk)
		   //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(ApplyCertRequestActivity.this.getApplication(), ApplyCertRequestActivity.this);
		
		gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
		
		ht = new HandlerThread("es_device_working_thread");
      	ht.start();
        workHandler = new Handler(ht.getLooper()); 
       // api = WXAPIFactory.createWXAPI(com.sheca.umandroid.ApplyCertRequestActivity.this, Constants.APP_ID);   //微信支付测试appid
		out_trade_no = "";

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);
		cancelScanButton.setVisibility(RelativeLayout.GONE);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ApplyCertRequestActivity.this.finish();
			}
		});

		((TextView) findViewById(R.id.auth_result)).setText("");
		((TextView) findViewById(R.id.auth_result_description_label)).setVisibility(RelativeLayout.GONE);
		((TextView) findViewById(R.id.auth_result_description)).setVisibility(RelativeLayout.GONE);
		((Button) findViewById(R.id.facedesc)).setVisibility(RelativeLayout.GONE);
		((Button) findViewById(R.id.faillink)).setVisibility(RelativeLayout.GONE);
		
		if(!mIsReset){
			if(!mIsDownload){
		       if(failCount >= CommonConst.FACE_RECOGNITION_FAIL_COUNT)
			      ((Button) findViewById(R.id.faillink)).setVisibility(RelativeLayout.VISIBLE);
			}
		}
		
		mAccountDao= new AccountDao(ApplyCertRequestActivity.this);
		mCertDao = new CertDao(ApplyCertRequestActivity.this);
		mLogDao = new LogDao(ApplyCertRequestActivity.this);
		findViewById(R.id.indicater).setVisibility(RelativeLayout.VISIBLE);

		Button nextBtn = (Button) findViewById(R.id.login_btn_next);
		nextBtn.setText("");
		//if (person.isbBodySuccess()) 
		nextBtn.setVisibility(RelativeLayout.GONE);
		//else
			//nextBtn.setVisibility(RelativeLayout.VISIBLE);
		
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnNextBtnClick();
			}
		});
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mAccountDao.getLoginAccount().getSaveType()){
			mSaveType = CommonConst.SAVE_CERT_TYPE_BLUETOOTH;
			mBBTDeviceUsed = true;
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == mAccountDao.getLoginAccount().getSaveType()){
			mSaveType = CommonConst.SAVE_CERT_TYPE_SIM;
			mBBTDeviceUsed = true;
		}else{
			mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
			mBBTDeviceUsed = false;
		}
		
		((Button) findViewById(R.id.facedesc)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showFaceDesc();
			}
		});
			
		//strPhoto = "";
	   // strSignature = "";
	    
	    mInfoInstance = CollectInfoInstance.getInstance();
		if (mInfoInstance == null)
			return;
	    
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			/*
			person = (PersonTask) bundle.getSerializable(AuthMainActivity.PERSONTASK);
			if (person == null){
				showAuthResult(EnumInstance.RT_Body_Fail, null);
				showLoadingView(FACE_AUTH_ERR);
				
				Button nextBtn = (Button) findViewById(R.id.login_btn_next);
				nextBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ApplyCertRequestActivity.this.finish();
					}
				});
				return;
			}
			
			Log.i(BodyCheckThread.TAG2, person.getStrPersonName() + ":"
					+ person.getStrPersonId());
			*/
			if(bundle.getString("message")!=null){
				mIsDao = true;
				cancelScanButton.setVisibility(RelativeLayout.GONE);
				findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
			}
			
			if(bundle.getString("Reset")!=null){
				mIsReset = true;
				mAccount = bundle.getString("AccountName");
				cancelScanButton.setVisibility(RelativeLayout.GONE);
				findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
			}
			
			if(bundle.getString("download")!=null){
				mIsDownload = true;
				cancelScanButton.setVisibility(RelativeLayout.GONE);
				findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
			}
				
			if(bundle.getString("loginAccount") != null){
				strPersonName = bundle.getString("loginAccount");	  
			}
			
			if(bundle.getString("loginId") != null){
				strPaperNO = bundle.getString("loginId");
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
		
			mPayType = bundle.getInt("paytype");
			mAccount = mAccountDao.getLoginAccount().getName();
			//strPersonCardPhoto = person.getStrCopyIDPhoto();
			
			if(mAccountDao.getLoginAccount().getStatus() == 2 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4)  //账户已实名认证
				 ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_noface_guide_3)));
			else
				 ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_face_guide_4)));  
			
			// 活体检测成功把数据通知给服务器
			/*
			if (person.isbBodySuccess()) {
				//startDialog();
				//showProgDlg("身份审核中...");
				showLoadingView(FACE_AUTH_LOADING);
				startThread(person);
			} else {
				showAuthResult(EnumInstance.RT_Body_Fail, null);
				showLoadingView(FACE_AUTH_ERR);
			}*/
			
			btnResultControl(EnumInstance.RT_Success);
		}
		
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 应用运行时，保持屏幕高亮，不锁屏
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.result, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (App.onKeyDown(this, keyCode, event))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (App.dispatchKeyEvent(this, event))
			return true;
		return super.dispatchKeyEvent(event);
	}

	/*
	// 点击支付事件
	private void onPayBtnClick(Handler handler) {
		showProgDlg("开始支付中...");
		
		handler.post(new Runnable(){
            @Override
            public void run() {
            	try{
            		 boolean  isReg = api.registerApp(Constants.APP_ID); 
            		
		             String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		             String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_WeChatPayUnifiedorder);			
		
		             //String url = "http://wxpay.wxutil.com/pub_v2/app/app_pay.php";
		             //url = "http://192.168.2.133:8080/UMAPI/WeChatPayUnifiedorder";
		             //Toast.makeText(PayActivity.this, "获取订单中...", Toast.LENGTH_SHORT).show();
                     
		             try{
			            //byte[] buf = Util.httpGet(url);
		            	  String strCertType = "";
		            	  Account  act = mAccountDao.getLoginAccount();
		              	  if(act.getCertType() == CommonConst.SAVE_CERT_TYPE_SM2)
		            		 strCertType = CommonConst.CERT_TYPE_SM2;
		            	  else
		            		 strCertType = CommonConst.CERT_TYPE_RSA;

        	              //String postParam = "certType="+URLEncoder.encode(strCertType, "UTF-8")+
	                                        // "&validity="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"", "UTF-8");
        	              
        	              String postParam = "requestNumber="+URLEncoder.encode(strReqNumber, "UTF-8");
        	              String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			              //byte[] buf = Util.httpPost(url, entity);
			              if (responseStr != null && !"".equals(responseStr)) {
				                 String content = responseStr;
				                 Log.d("get server pay params:",content);
	        	                 JSONObject json = new JSONObject(content); 
				                 if(null != json && "0".equals(json.getString("returnCode")) ){
					                   JSONObject jbRet =  new JSONObject(json.getString("result"));
					                   if("SUCCESS".equals(jbRet.getString("result_code"))){
					                       PayReq req = new PayReq();
					                       //req.appId = "wxf8b4f85f3a794e77";  // ������appId
					                       req.appId			= Constants.APP_ID;//json.getString("appid");
					                       req.partnerId		= "1505170281";//jbRet.getString("partnerid");
					                       req.prepayId		    = jbRet.getString("prepay_id");
					                       req.nonceStr		    = jbRet.getString("noncestr");
					                       req.timeStamp		= jbRet.getString("timestamp");
					                       req.packageValue	    = "Sign=WXPay";//jbRet.getString("package");
					                       req.sign			    = jbRet.getString("sign");
					                       req.extData			= jbRet.getString("out_trade_no");// optional
					                       out_trade_no = jbRet.getString("out_trade_no");
					                       //Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
					                        //在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
					                       boolean  ispay = api.sendReq(req);		
					                       //Toast.makeText(PayActivity.this, "sendReq:"+String.valueOf(ispay), Toast.LENGTH_SHORT).show();
					                       
					                       closeProgDlg();
					                   }else{
						                   Toast.makeText(ApplyCertRequestActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();
						                   
						                   closeProgDlg();
						                   ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText(jbRet.getString("err_code_des"));
									       showLoadingView(FACE_AUTH_ERR);
									       ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next).setVisibility(RelativeLayout.VISIBLE);
									       ((Button)ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next)).setText("");
					                   }
				
				                 }else if(json.getString("returnCode").equals("10012")){
				                	 closeProgDlg();
				                	 showProgDlg("账户登录中...");
				                	 loginUMSPService(mAccount);
				                 }else{
		        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
		        	                 Toast.makeText(ApplyCertRequestActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();	
		        	                 
		        	                 closeProgDlg();
		        	                 ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText(json.getString("returnMsg"));
								     showLoadingView(FACE_AUTH_ERR);
								     ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next).setVisibility(RelativeLayout.VISIBLE);
								     ((Button)ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next)).setText("");
				                 }
			
			              }else{
			            	  Log.d("PAY_GET", "服务器请求错误");
	        	              Toast.makeText(ApplyCertRequestActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
	        	              
	        	              closeProgDlg();
	        	              ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText("服务器请求错误");
							  showLoadingView(FACE_AUTH_ERR);
							  ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next).setVisibility(RelativeLayout.VISIBLE);
							  ((Button)ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next)).setText("");
	                      }      
		             }catch(Exception e){
        	             Log.e("PAY_GET", "异常："+e.getMessage());
        	             Toast.makeText(ApplyCertRequestActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
        	             
        	             closeProgDlg();
        	             ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText(e.getMessage());
					     showLoadingView(FACE_AUTH_ERR);
					     ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next).setVisibility(RelativeLayout.VISIBLE);
					     ((Button)ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next)).setText("");
                     }
		             
		             isPayed = true;
            	}catch(final Exception exc){
            		Log.e("PAY_GET", "exc:"+exc.getMessage());
            		Toast.makeText(ApplyCertRequestActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
            		
            		closeProgDlg();
            		((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText(exc.getMessage());
				    showLoadingView(FACE_AUTH_ERR);
				    ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next).setVisibility(RelativeLayout.VISIBLE);
				    ((Button)ApplyCertRequestActivity.this.findViewById(R.id.login_btn_next)).setText("");
            	}
            }
        });
		
	}*/
	
	private  void  showWXPayActivity(String strActName,String strActIdentityCode,String strReqNumber,String strStatus){    //进行微信支付
		 Intent intent = new Intent();
		 intent.setClass(ApplyCertRequestActivity.this, WXPayActivity.class);
		 intent.putExtra("loginAccount", strActName); 
		 intent.putExtra("loginId", strActIdentityCode); 
		 intent.putExtra("requestNumber",strReqNumber); 
		 intent.putExtra("applyStatus",strStatus); 
		 if(mBBTDeviceUsed)
		    intent.putExtra("bluetoothpwd", mStrBTDevicePwd); 
		 
		 ApplyCertRequestActivity.this.startActivity(intent);
		 ApplyCertRequestActivity.this.finish();	 
			
	}
	
	private  void  showAliPayActivity(String strActName,String strActIdentityCode,String strReqNumber,String strStatus){    //进行支付宝支付
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		// 重新获取
		App.setLockScreenAndPattern(this, true);
		
		/*
		final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
		if(isPayed){
			showProgDlg("等待支付结果中...");
			setContentView(R.layout.activity_launch);
			
			handler.post(new Runnable(){
	            @Override
	            public void run() {
	            	try{
			            String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
                        String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_WeChatPayOrderquery);			

                        String postParam = "out_trade_no="+URLEncoder.encode(out_trade_no, "UTF-8");
                        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
 
                        Intent i = new Intent(ApplyCertRequestActivity.this, com.sheca.umandroid.WXPayResultActivity.class);
                        i.putExtra("loginAccount", strPersonName); 
               		    i.putExtra("loginId", strPaperNO); 
               		    i.putExtra("isPayed", "pay"); 
               		    i.putExtra("requestNumber",strReqNumber); 
             	        i.putExtra("applyStatus",strStatus); 
             	        if(mBBTDeviceUsed)
             	    	   i.putExtra("bluetoothpwd", mStrBTDevicePwd); 
                        
                        if (responseStr != null && !"".equals(responseStr)) {
                        	JSONObject json = new JSONObject(responseStr); 
			                 if(null != json && "0".equals(json.getString("returnCode")) ){
				                   JSONObject jbRet =  new JSONObject(json.getString("result"));
				                   if("SUCCESS".equals(jbRet.getString("result_code"))){
				                	   String trade_state = jbRet.getString("trade_state");
				                	   String retStr =  "transaction_id:"+jbRet.getString("transaction_id")+
				                			            ",out_trade_no:"+out_trade_no+
				                	                    ",total_fee:"+jbRet.getString("total_fee");
				                	   //Toast.makeText(PayActivity.this, "paystate:"+trade_state+"\nretStr:\n"+retStr, Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   if("SUCCESS".equals(trade_state))
				                	      i.putExtra("paystate", Boolean.TRUE); 
				                	   else
				                		  i.putExtra("paystate", Boolean.FALSE); 
				                	   
				        			   startActivity(i);
				        			   ApplyCertRequestActivity.this.finish();
				                   }else{
				                	   Toast.makeText(ApplyCertRequestActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   i.putExtra("paystate", Boolean.FALSE); 
				        			   startActivity(i);
				        			   ApplyCertRequestActivity.this.finish();
				                   }
			                 }else{
	        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
	        	                 Toast.makeText(ApplyCertRequestActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();
	        	                 closeProgDlg();
	        	                 
	        	                 i.putExtra("paystate", Boolean.FALSE); 
			        			 startActivity(i);
			        			 ApplyCertRequestActivity.this.finish();
			                 }		             
                        }else{
		            	    Log.d("PAY_GET", "服务器请求错误");
        	                Toast.makeText(ApplyCertRequestActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
        	                closeProgDlg();
        	                
        	                i.putExtra("paystate", Boolean.FALSE); 
		        			startActivity(i);
		        			ApplyCertRequestActivity.this.finish();
                        }      
      
	            	}catch(final Exception exc){
	            		Log.e("PAY_GET", "exc:"+exc.getMessage());
	            		Toast.makeText(ApplyCertRequestActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
	            		closeProgDlg();
	            	}
	            }
	        });
		}
		*/
		
	}

	
	@Override
	protected void onPause() {
		// 在Activity销毁的时候释放wakeLock
		super.onPause();
		App.setLockScreenAndPattern(this, false);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ExitThread();
		
		closeProgDlg();
		if (mProgressDialog != null) {
			mProgressDialog.dismissDialog();
		}
	}

	// ===================================================================
	// private int mCheckSleepTimeout = 1000 * 5;// 超时时间,以后要修改成3分钟,跟WEB端保证一致
	private UploadPhotoThread mUploadThread = null;

	private void startDialog() {
		mProgressDialog = new CustomProgressDialog(this, false);
		mProgressDialog.setMessage(R.string.label_loading_txt1);
		mProgressDialog.showDialog();
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			closeProgDlg();
			
			showAuthResult(msg.what, String.valueOf(msg.obj));
			ExitThread();
			Log.i(BodyCheckThread.TAG2, msg.what + "");
			Log.i(BodyCheckThread.TAG2,
					(System.currentTimeMillis() - lastConTime) + "毫秒");
			
			if (mProgressDialog != null) {
				mProgressDialog.dismissDialog();
				closeProgDlg();
			}
		}
	};

	private void startThread(PersonTask person) {
		lastConTime = System.currentTimeMillis();
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// // 如果time已经超时,则也终止Dialog,提示超时,同时中断其他线程
		// try {
		// Thread.sleep(mCheckSleepTimeout);
		// mHandler.sendEmptyMessage(EnumInstance.RT_Timeout);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// }).start();

		// 执行耗时操作
		// 其他情况为从后台获取到结果,则终止Dialog,拿到返回码与返回详情,通知主线程更新UI,同时中断超时的线程

		if (mUploadThread == null) {
			mUploadThread = new UploadPhotoThread(person, mHandler);
			mUploadThread.ThreadBegin();
		}

	}

	private void btnResultControl(int iReturnCode) {
		Button NextBtn = (Button) findViewById(R.id.login_btn_next);
		if (NextBtn == null)
			return;
		
		 if(iReturnCode == EnumInstance.RT_Success) {			
		    bFinish = true;
			NextBtn.setText(R.string.action_finish);	
			((Button) findViewById(R.id.facedesc)).setVisibility(RelativeLayout.GONE);
			((Button) findViewById(R.id.faillink)).setVisibility(RelativeLayout.GONE);
			OnNextBtnClick();
		 } else {
		    bFinish = false;
			NextBtn.setText("");
			((Button) findViewById(R.id.facedesc)).setVisibility(RelativeLayout.VISIBLE);
			NextBtn.setVisibility(RelativeLayout.VISIBLE);
		    ((TextView) findViewById(R.id.auth_result_description)).setVisibility(RelativeLayout.VISIBLE);
		    if(((TextView) findViewById(R.id.auth_result_description)).length() > 0){
				   ((TextView) findViewById(R.id.auth_result_description_label)).setVisibility(RelativeLayout.GONE);	   
		    }
			else{
				   ((TextView) findViewById(R.id.auth_result_description_label)).setVisibility(RelativeLayout.GONE);
			}
		    
		    showLoadingView(FACE_AUTH_ERR);
		    
		    strSignature = "";
		    
		    failCount++;  //人脸识别失败次数自增
		    
		    if(!mIsReset){
		      if(failCount >=   CommonConst.FACE_RECOGNITION_FAIL_COUNT ){
		          ((Button) findViewById(R.id.faillink)).setVisibility(RelativeLayout.VISIBLE);
		          if(failCount ==  CommonConst.FACE_RECOGNITION_FAIL_COUNT )
		              showManualCheck();
		      }
		    
		      ((Button) findViewById(R.id.faillink)).setOnClickListener(new OnClickListener() {
				  @Override
				  public void onClick(View v) {
					 // TODO Auto-generated method stub
					 showManualCheck();
				  }
			   });
		    }
		 }
		
		/*bFinish = true;
		NextBtn.setText(R.string.action_finish);	
		OnNextBtnClick();*/
	}

	private void ExitThread() {
		if (mUploadThread != null) {
			mUploadThread.ThreadEnd();
			// 这两个函数不安全，所以采用线程自己退出方式
			mUploadThread = null;
		}
	}

	private void showAuthResult(int iReturnCode, String infoMsg) {			
        String info = "";
        //if(null != infoMsg){
        	//info =	infoMsg;   //infoMsg.split("||")[0];
        //strPhoto = infoMsg.substring(infoMsg.indexOf("||")+2);
           // strPhoto = infoMsg.split("||")[0];
            //strPhoto = "photo";
            //if(null == strPhoto || "null".equals(strPhoto))
            	//strPhoto = "photo";
            
           // strSignature = infoMsg.split("||")[1];
           // if(null == strSignature || "null".equals(strSignature))
            	//strSignature = "sheca";
       // }

		TextView resultTv = (TextView) findViewById(R.id.auth_result);
		TextView resultDesTv = (TextView) findViewById(R.id.auth_result_description);

		if (resultTv == null || resultDesTv == null)
			return;
		if (iReturnCode == EnumInstance.RT_Success) {// 0通过
			resultTv.setText(R.string.label_result_success);
			resultTv.setTextColor(getResources()
					.getColor(R.color.alert_success));
		} else {
			if (iReturnCode == EnumInstance.RT_Unsure) {// -1无法判定
				resultTv.setText(R.string.label_result_notsure);
				resultTv.setTextColor(getResources().getColor(
						R.color.alert_warn));
			} else {// 未通过
				resultTv.setText(R.string.label_result_fail);
				resultTv.setTextColor(getResources().getColor(
						R.color.alert_danger));
			}
		}
		if (info == null || ("").equals(info) || ("null").equals(info)) {
			if (iReturnCode == EnumInstance.RT_Success)
				resultDesTv.setText(R.string.label_auth_success);
			else if (iReturnCode == EnumInstance.RT_Unsure)
				resultDesTv.setText(R.string.label_auth_unknown);
			else if (iReturnCode == EnumInstance.RT_Fail)
				resultDesTv.setText(R.string.label_auth_fail);
			else if (iReturnCode == EnumInstance.RT_Unqualified)
				resultDesTv.setText(R.string.label_auth_unqualified);
			else if (iReturnCode == EnumInstance.RT_Body_Fail)
				resultDesTv.setText(R.string.label_auth_body_fail);
			else if (iReturnCode == EnumInstance.RT_Timeout)
				resultDesTv.setText(R.string.label_auth_timeout);
			else if (iReturnCode == EnumInstance.RT_Compare_Error)
				resultDesTv.setText(R.string.label_auth_compare_error);
			else if (iReturnCode == EnumInstance.RT_IDPhoto_Fail)
				resultDesTv.setText(R.string.label_auth_idphoto_fail);
			else if (iReturnCode == EnumInstance.RT_Compare_Timeout)
				resultDesTv.setText("远程比对服务器"
						+ this.getResources().getString(
								R.string.label_auth_timeout));
			else if (iReturnCode == EnumInstance.RT_Session_Timeout)
				resultDesTv.setText("SESSION"
						+ this.getResources().getString(
								R.string.label_auth_timeout));
			else if (iReturnCode == EnumInstance.RT_Name_NotMatch)
				resultDesTv.setText(R.string.label_auth_name_notsame);
			else if (iReturnCode == EnumInstance.RT_Id_NotMatch)
				resultDesTv.setText(R.string.label_auth_id_service_result);
			else if (iReturnCode == EnumInstance.RT_NOIdPhoto)
				resultDesTv.setText(R.string.label_auth_noidphoto);
			else if (iReturnCode == EnumInstance.RT_BestPhoto_Fail)
				resultDesTv.setText(R.string.label_auth_nobestphoto);
			else if (iReturnCode == EnumInstance.RT_Start_Fail)
				resultDesTv.setText(R.string.label_auth_notaskguid);
			else if (iReturnCode == EnumInstance.RT_NO_INDENTY_CODE)
				resultDesTv.setText(R.string.label_auth_noidentycode);
			else if (iReturnCode == EnumInstance.RT_Format_INDENTY_CODE)
				resultDesTv.setText(R.string.label_auth_formatidentycode);
			else if (iReturnCode == EnumInstance.RT_NO_INDENTY_NAME)
				resultDesTv.setText(R.string.label_auth_noidentyname);
			else if (iReturnCode == EnumInstance.RT_Format_INDENTY_NAME)
				resultDesTv.setText(R.string.label_auth_formatidentyname);
			else if (iReturnCode == EnumInstance.RT_NO_PHOTO)
				resultDesTv.setText(R.string.label_auth_nophoto);
			else if (iReturnCode == EnumInstance.RT_Format_PHOTO)
				resultDesTv.setText(R.string.label_auth_formatphoto);
			else if (iReturnCode == EnumInstance.RT_CHECK_FACE_FAIL)
				resultDesTv.setText(R.string.label_auth_checkfacefail);
			else if (iReturnCode == EnumInstance.RT_CHECK_FACE_MORE)
				resultDesTv.setText(R.string.label_auth_checkfacemore);
			else if (iReturnCode == EnumInstance.RT_Format_CLIENT_TYPE)
				resultDesTv.setText(R.string.label_auth_checkfacemore);
			else if (iReturnCode == EnumInstance.RT_CLIENT_TYPE_NotMatch)
				resultDesTv.setText(R.string.label_auth_clienttypenotmatch);
			else
				resultDesTv.setText(R.string.label_auth_fail);
		} else {
			resultDesTv.setText(info);
		}
		
		/*resultTv.setText(R.string.label_result_success);
		resultTv.setTextColor(getResources()
				.getColor(R.color.alert_success));
		resultDesTv.setText(R.string.label_auth_success);*/
		
		btnResultControl(iReturnCode);
	}

	private void OnNextBtnClick() {
		final Button btn = (Button) findViewById(R.id.login_btn_next);
		btn.setVisibility(RelativeLayout.GONE);

		if (btn != null){
		  if(!mIsReset){
			  if(mIsDownload){
				  if(bFinish){	
					   /* Account curAct = mAccountDao.queryByName(person.getStrAccountName());
						curAct.setCopyIDPhoto(strPersonCardPhoto);
						curAct.setStatus(CommonConst.ACCOUNT_STATE_TYPE3);
						mAccountDao.update(curAct);
						*/
						Toast.makeText(ApplyCertRequestActivity.this, "实名认证通过，请下载证书",Toast.LENGTH_SHORT).show();
					 
					    Intent intent = new Intent(ApplyCertRequestActivity.this, ApplicationActivity.class);
						ApplyCertRequestActivity.this.startActivity(intent);
					    ApplyCertRequestActivity.this.finish();


				 }else{
					    // 跳回输入界面	
					    Intent intent = new Intent();
					    intent.setClass(ApplyCertRequestActivity.this, AuthMainActivity.class);
					    intent.putExtra("loginAccount", strPersonName); 
					    intent.putExtra("loginId", strPaperNO); 
					    //intent.putExtra("headphoto",mHeadPhoto);
					    if(mIsDao)
						    intent.putExtra("message", "dao"); 
					    if(mIsDownload)
						    intent.putExtra("download", "dao"); 
					    ApplyCertRequestActivity.this.startActivity(intent);
					    // 强制触发返回事件
					    ApplyCertRequestActivity.this.finish();
				 }
			  }else{
			    if(bFinish) {	
				    if(mBBTDeviceUsed){
				    	if("".equals(mStrBTDevicePwd))
				           setBlueToothPwd();
				    	else
				    	   doApplyCertByFaceRecognition(btn);	
				    }else{
				       doApplyCertByFaceRecognition(btn);	
				    }
			    }else{
				    // 跳回输入界面	
				    Intent intent = new Intent();
				    intent.setClass(ApplyCertRequestActivity.this, AuthMainActivity.class);
				    intent.putExtra("loginAccount", strPersonName); 
				    intent.putExtra("loginId", strPaperNO); 
				    //intent.putExtra("headphoto",mHeadPhoto);
				    if(mIsDao)
					    intent.putExtra("message", "dao"); 
				    if(mIsDownload)
					    intent.putExtra("download", "dao"); 
				    ApplyCertRequestActivity.this.startActivity(intent);
				    // 强制触发返回事件
				    ApplyCertRequestActivity.this.finish();
			    }
			  }
		 }else{
			 if(bFinish){	
				   /* 
				    Account curAct = mAccountDao.queryByName(person.getStrAccountName());
					curAct.setCopyIDPhoto(strPersonCardPhoto);
					curAct.setStatus(CommonConst.ACCOUNT_STATE_TYPE3);
					mAccountDao.update(curAct);
				 */
				    Intent intent = new Intent(ApplyCertRequestActivity.this, SetPasswordActivity.class);    
					intent.putExtra("ActName", mAccount);
					if(mIsDao)
						intent.putExtra("message", "dao"); 
					ApplyCertRequestActivity.this.startActivity(intent);
				    ApplyCertRequestActivity.this.finish();
			 }else{
				 Intent intent = new Intent();
				 intent.setClass(ApplyCertRequestActivity.this, AuthMainActivity.class);
				 intent.putExtra("loginAccount", strPersonName); 
				 intent.putExtra("loginId", strPaperNO); 
				 intent.putExtra("Account", mAccount); 
				 intent.putExtra("BizSN", person.getStrTaskGuid()); 
				 intent.putExtra("Reset", "reset"); 
				// intent.putExtra("headphoto",mHeadPhoto);
				 if(mIsDao)
				     intent.putExtra("message", "dao"); 
				 if(mIsDownload)
					  intent.putExtra("download", "dao"); 
				 ApplyCertRequestActivity.this.startActivity(intent);
				 // 强制触发返回事件
				 ApplyCertRequestActivity.this.finish();
			 }
		 }
	  }
	}
	
	
	private  void  doApplyCertByFaceRecognition(final Button btn){
		//showProgDlg("证书申请中...");
		showLoadingView(CERT_APPLY_LOADING);
		
		Account curAct = mAccountDao.getLoginAccount();
		//curAct.setCopyIDPhoto(strPersonCardPhoto);
		//mAccountDao.update(curAct);
		
		final int actCertType = curAct.getCertType();		
		final int actSaveType = curAct.getSaveType();
		final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
		//获取证书申请业务流水号
		//new Thread(new Runnable(){
		workHandler.post(new Runnable(){
	            @Override
	            public void run() {
	            	try {
	            		if(mBBTDeviceUsed){
	            			String strBTDevSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
	            		
	            			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == actSaveType){
	            			    shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
	        				    if(null == devInfo)	{
	        					    if(gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN) == 0){
	        						    devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
	        						    if(devInfo.isDefaultPin){
	        							    handler.post(new Runnable() {
	    									   @Override
	    										public void run() {
	    										    changeProgDlg("修改蓝牙key设备密码中...");
	    										}
	    								    });
	        							 
	        							    if(gEsDev.changePin(CommonConst.BT_DEVICE_DEFAULT_PWD, mStrBTDevicePwd) == 0){
	        								    handler.post(new Runnable() {
		    									   @Override
		    										public void run() {
		    										   //changeProgDlg("证书申请中...");
		    										   closeProgDlg();
		    										   showLoadingView(CERT_APPLY_LOADING);
		    										}
		    								    });
	        							    }
	        						    }
	        					    }
	        				    }else{
	        					    if(devInfo.isDefaultPin){
        							     handler.post(new Runnable() {
    									     @Override
    										 public void run() {
    										    changeProgDlg("修改蓝牙key设备密码中...");
    										 }
        							     });
        							 
        							     if(gEsDev.changePin(CommonConst.BT_DEVICE_DEFAULT_PWD, mStrBTDevicePwd) == 0){
        								     handler.post(new Runnable() {
	    									 @Override
	    										 public void run() {
	    										    //changeProgDlg("证书申请中...");
	    										    closeProgDlg();
	    										    showLoadingView(CERT_APPLY_LOADING);
	    										 }
	    								     });
        							     }
	        					    }
	        				    }
	            			}else if(CommonConst.SAVE_CERT_TYPE_SIM == actSaveType){
	            				if(ScanBlueToothSimActivity.gKsSdk.isConnected()){
	            					handler.post(new Runnable() {
 									   @Override
 										public void run() {
 										   //changeProgDlg("证书申请中...");
 										   showLoadingView(CERT_APPLY_LOADING);
 										}
 								    });
	            				}else{
	            					handler.post(new Runnable() {
									     @Override
										 public void run() {
										    changeProgDlg("蓝牙sim卡设备连接中...");
										 }
   							        });
	            					
	            					ScanBlueToothSimActivity.gKsSdk.connect(strBTDevSN, "778899", 500);
	            					handler.post(new Runnable() {
    									 @Override
    									 public void run() {
    										 closeProgDlg();
    										 showLoadingView(CERT_APPLY_LOADING);
    								     }
    								 });
	            				}
	            			}
	            		}
	            		
	            		loginUMSPService(mAccount);
	            		
	            		if(!"".equals(strReqNumber)){
	            			String responseStr = ""; 
	            			String resultStr = ""; 
	            			String returnStr = ""; 
	            			
	            			net.sf.json.JSONObject jb = null;
	            			
	            			if(strStatus.equals("0")) {  //待上送P10
	            				if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
	            					responseStr = UploadPkcs10(strReqNumber,strPersonName,CommonConst.CERT_TYPE_RSA,CommonConst.SAVE_CERT_TYPE_RSA+"",mSaveType);
								else
									responseStr = UploadSM2Pkcs10(strReqNumber,strPersonName,CommonConst.CERT_TYPE_SM2,CommonConst.SAVE_CERT_TYPE_SM2+"",mSaveType);
								 
	            				jb = net.sf.json.JSONObject.fromObject(responseStr);
								resultStr = jb.getString(CommonConst.RETURN_CODE);
								returnStr = jb.getString(CommonConst.RETURN_MSG);
								if (resultStr.equals("0")) {
									//设置时间间隔，等待后台签发证书
									String threadSleepTime = ApplyCertRequestActivity.this.getString(R.string.Thread_Sleep);
								    if(CommonConst.SAVE_CERT_TYPE_SM2 == actCertType)
									    Thread.sleep(Long.parseLong(threadSleepTime)*2);
									else
										Thread.sleep(Long.parseLong(threadSleepTime));
								    
								    strStatus = "2";
								    
								    if(mPayType == CommonConst.PAY_TYPE_USE_WX)
								    	showWXPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
								    else
								    	showAliPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
								    	
								    return;
								}else {					
									throw new Exception("调用UMSP服务之UploadPkcs10失败：" + resultStr + "，" + returnStr);
								}	
								    
									//调用UMSP服务：下载证书
	            			}else if (strStatus.equals("1")) {  //待签发
	            				throw new Exception("证书签发中，请等待一分钟，再尝试下载。");
	            			}else if (strStatus.equals("2")) {  //待下载
	            				if(mPayType == CommonConst.PAY_TYPE_USE_WX)
							    	showWXPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
							    else
							    	showAliPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
	            				
	            				return;
	            			}
	            		}else{		
	            			if("".equals(strENVSN)){
		                        String responseStr = ""; 
		                        isPayed = true;

		                        if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
	            		           //responseStr = ApplyCertByFaceRecognitionRealTime(strPersonName,strPaperType,strPaperNO,ResultActivity.strSignature,strSignatureAlgorithm,mSaveType);
		                           responseStr = applyRSACertByFaceAuth(strPersonName,strPaperType,strPaperNO);    
		                        else
		                           //responseStr = ApplySM2CertByFaceRecognitionRealTime(strPersonName,strPaperType,strPaperNO,ResultActivity.strSignature,strSignatureAlgorithm,mSaveType);
		                           responseStr = applySM2CertByFaceAuth(strPersonName,strPaperType,strPaperNO);
		                        
		                        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
	            		        String resultStr = jb.getString(CommonConst.RETURN_CODE);
	        				    String returnStr = jb.getString(CommonConst.RETURN_MSG);
						        final String strErr = returnStr;

						        if(resultStr.equals("0")) {									    	
								   //调用UMSP服务：下载证书
								   handler.post(new Runnable() {
									 @Override
										public void run() {
										   //changeProgDlg("证书下载中...");
										   showLoadingView(CERT_APPLY_LOADING);
										}    
								   });
								
								   net.sf.json.JSONObject jbRet =  new net.sf.json.JSONObject();
								   if(null != jb.getString(CommonConst.RETURN_RESULT) && !"null".equals(jb.getString(CommonConst.RETURN_RESULT)))  
									  jbRet =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
								
								   strENVSN = jbRet.getString(CommonConst.RESULT_PARAM_REQUEST_NUMBER);   //记录ENVSN
								   //strENVSN = returnStr;   //记录ENVSN
								   strReqNumber = strENVSN;
								   strStatus = "2";
								   
								   if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
		            					responseStr = UploadPkcs10(strReqNumber,strPersonName,CommonConst.CERT_TYPE_RSA,CommonConst.SAVE_CERT_TYPE_RSA+"",mSaveType);
									else
										responseStr = UploadSM2Pkcs10(strReqNumber,strPersonName,CommonConst.CERT_TYPE_SM2,CommonConst.SAVE_CERT_TYPE_SM2+"",mSaveType);
									
									jb = JSONObject.fromObject(responseStr);
									resultStr = jb.getString(CommonConst.RETURN_CODE);
									returnStr = jb.getString(CommonConst.RETURN_MSG);
									
									if(!"0".equals(resultStr)){    
										throw new Exception("调用UMSP服务之UploadPkcs10失败：" + resultStr + "，" + returnStr);
									}
								
								    //设置时间间隔，等待后台签发证书									
								    String threadSleepTime = ApplyCertRequestActivity.this.getString(R.string.Thread_Sleep);
								    if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType){
								      Thread.sleep(Long.parseLong(threadSleepTime));
								      updateCertByReqNumber(strReqNumber,strPersonName,CommonConst.CERT_TYPE_RSA,CommonConst.CERT_SIGN_ALG_TYPE_RSA+"",mSaveType);
								    }else{
						    	      Thread.sleep(Long.parseLong(threadSleepTime)*2);   //签发sm2证书等待时间需10秒
						    	      updateSM2CertByReqNumber(strReqNumber,strPersonName,CommonConst.CERT_TYPE_SM2,CommonConst.CERT_SIGN_ALG_TYPE_SM2+"",mSaveType);
								    }
								
								    if(mPayType == CommonConst.PAY_TYPE_USE_WX)
								    	showWXPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
								    else
								    	showAliPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
								   
								   return;
								   /*
								   //下载证书
								   if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
								      responseStr = DownloadCert(strENVSN,mSaveType,CommonConst.CERT_TYPE_RSA);
								   else
									  responseStr = DownloadSM2Cert(strENVSN,mSaveType,CommonConst.CERT_TYPE_SM2);
								
								   jb = net.sf.json.JSONObject.fromObject(responseStr);
								   resultStr = jb.getString(CommonConst.RETURN_CODE);
								   returnStr = jb.getString(CommonConst.RETURN_MSG);

								   handler.post(new Runnable() {
									 @Override
										public void run() {
										   closeProgDlg();
										}
								   }); 
								
								   if (resultStr.equals("0")) {
									jbRet =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
							    	
							    	DownloadCertResponse dcResponse = new DownloadCertResponse();
							    	dcResponse.setReturn(returnStr);
							    	dcResponse.setResult(resultStr);
							    	if(null!= jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT) && !"null".equals(jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT)))
							    	    dcResponse.setUserCert(jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT));
							    	if(null!= jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT) && !"null".equals(jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT)))
							    	    dcResponse.setEncCert(jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT));
							    	if(null!= jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT) && !"null".equals(jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT)))
							    	    dcResponse.setEncKey(jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT));
							    	if(null!= jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN) && !"null".equals(jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN)))
							    	    dcResponse.setCertChain(jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN));
							    	if(null!= jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG) && !"null".equals(jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG)))
							    	    dcResponse.setEncAlgorithm(jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG));
							    	
							    	final DownloadCertResponse  fDcResponse = dcResponse;
									//UI处理必须放在主线程
									 handler.post(new Runnable() {
										 @Override
											public void run() {
											   //保存证书到本地
											   saveCert(strENVSN, fDcResponse,mSaveType,actCertType);
											}
										}); 
								   } else {
									  throw new Exception("调用UMSP服务之DownloadCert失败：" + resultStr + "，" + returnStr);
								   }
								   */
						    }else{
							    handler.post(new Runnable() {
								    @Override
									public void run() {
								    	closeProgDlg();
								    	((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText(strErr);
								    	showLoadingView(FACE_AUTH_ERR);
									    Toast.makeText(ApplyCertRequestActivity.this, strErr,Toast.LENGTH_SHORT).show();
									    btn.setVisibility(RelativeLayout.VISIBLE);
									    btn.setText("");
									}
							    }); 				
						    }
	            		}else{   //重新下载证书
	            			//调用UMSP服务：下载证书
							handler.post(new Runnable() {
								 @Override
									public void run() {
									  //changeProgDlg("证书下载中...");
									  showLoadingView(CERT_APPLY_LOADING);
									}
							});		
							
							strReqNumber = strENVSN;
							strStatus = "2";
							
							if(mPayType == CommonConst.PAY_TYPE_USE_WX)
						    	showWXPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
						    else
						    	showAliPayActivity(strPersonName,strPaperNO,strReqNumber,strStatus);
							
							return;
							/*
							String responseStr = "";									
							if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
							    responseStr = DownloadCert(strENVSN,mSaveType,CommonConst.CERT_TYPE_RSA);
							else
								responseStr = DownloadSM2Cert(strENVSN,mSaveType,CommonConst.CERT_TYPE_SM2);
								
							net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
							String resultStr = jb.getString(CommonConst.RETURN_CODE);
							String returnStr = jb.getString(CommonConst.RETURN_MSG);																					
							final String strErr = returnStr;
							
							handler.post(new Runnable() {
								 @Override
									public void run() {
									   closeProgDlg();
									}
							}); 
							
							if (resultStr.equals("0")) {
								net.sf.json.JSONObject jbRet =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
						    	
						    	DownloadCertResponse dcResponse = new DownloadCertResponse();
						    	dcResponse.setReturn(returnStr);
						    	dcResponse.setResult(resultStr);
						    	dcResponse.setUserCert(jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT));
						    	dcResponse.setEncCert(jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT));
						    	dcResponse.setEncKey(jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT));
						    	dcResponse.setCertChain(jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN));
						    	dcResponse.setEncAlgorithm(jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG));
						    	
						    	final DownloadCertResponse  fDcResponse= dcResponse;
								
								//UI处理必须放在主线程
								 handler.post(new Runnable() {
									 @Override
										public void run() {
										//保存证书到本地
										 saveCert(strENVSN, fDcResponse,mSaveType,actCertType);
										}
									}); 
							} else {
								throw new Exception("调用UMSP服务之DownloadCert失败：" + resultStr + "，" + returnStr);
							}	*/ 
	            		  }
	            		}
	            	}catch(final Exception exc){
	            		handler.post(new Runnable() {
							 @Override
								public void run() {
								  closeProgDlg();
								  if(exc.getMessage().indexOf("peer")!=-1){
									 Toast.makeText(ApplyCertRequestActivity.this, "无效的服务器请求", Toast.LENGTH_SHORT).show();
									 ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText("无效的服务器请求");
								  }else{					  
								     Toast.makeText(ApplyCertRequestActivity.this, "网络连接或访问服务异常",Toast.LENGTH_SHORT).show();
								     ((TextView)ApplyCertRequestActivity.this.findViewById(R.id.auth_result_description)).setText("网络连接或访问服务异常");
								  }
								  
								  showLoadingView(FACE_AUTH_ERR);
								  btn.setVisibility(RelativeLayout.VISIBLE);
								  btn.setText("");
								}
						}); 
	            	}
	            }
	        });//.start();
		
	}

	
	private String ApplyCertByFaceRecognition(String PersonName,String PaperType,String PaperNO,String Photo,String Signature,String SignatureAlgorithm) throws Exception{
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_ApplyCertByFaceRecognition);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("PersonName", PersonName);
		postParams.put("PaperType", PaperType);  
		postParams.put("PaperNO", PaperNO);
		postParams.put("Signature", Signature);   
		postParams.put("SignatureAlgorithm", SignatureAlgorithm);  
		postParams.put("Photo", Photo);   
		
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
    	String postParam = "PersonName="+URLEncoder.encode(PersonName, "UTF-8")+
    			           "&PaperType="+URLEncoder.encode(PaperType, "UTF-8")+
    			           "&PaperNO="+URLEncoder.encode(PaperNO, "UTF-8")+
    			           "&Signature="+URLEncoder.encode(Signature, "UTF-8")+
    			           "&SignatureAlgorithm="+URLEncoder.encode(SignatureAlgorithm, "UTF-8")+
    			           "&Photo="+URLEncoder.encode(Photo, "UTF-8");
    	
    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	return responseStr;
	}
	
	private String ApplyCertByFaceRecognitionRealTime(String PersonName,String PaperType,String PaperNO,String Signature,String SignatureAlgorithm,int saveType) throws Exception{
		String p10 = "";
		String deviceSN = "";
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			p10 = genPkcs10ByBlueTooth(PersonName);
			deviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			p10 = genPkcs10ByBlueToothSim(PersonName);
			deviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		}else{
			p10 = genPkcs10(PersonName);
			deviceSN = android.os.Build.SERIAL;
		}
		
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_ApplyCertByFaceRecognitionRealTimeEx);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("personName", PersonName);
		postParams.put("paperType", PaperType);  
		postParams.put("paperNO", PaperNO);
	    postParams.put("p10",  p10);
		postParams.put("deviceID", deviceSN);  //获取设备唯一标识符
		postParams.put("signature", Signature);   
		postParams.put("signatureAlgorithm", SignatureAlgorithm);  
		postParams.put("certType", CommonConst.CERT_TYPE_RSA);  
		postParams.put("validity", CommonConst.CERT_TYPE_SM2_VALIDITY+"");  
		
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
		String certValid = CommonConst.CERT_TYPE_SM2_VALIDITY+"";
		if(isPayed)
			certValid = CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"";
		
    	String postParam = "personName="+URLEncoder.encode(PersonName, "UTF-8")+
		                   "&paperType="+URLEncoder.encode(PaperType, "UTF-8")+
		                   "&PaperNO="+URLEncoder.encode(PaperNO, "UTF-8")+
		                   "&p10="+URLEncoder.encode(p10, "UTF-8")+
		                   "&deviceID="+URLEncoder.encode(deviceSN, "UTF-8")+
		                   "&signature="+URLEncoder.encode(Signature, "UTF-8")+
		                   "&signatureAlgorithm="+URLEncoder.encode(SignatureAlgorithm, "UTF-8")+
		                   "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_RSA, "UTF-8")+
		                   "&validity="+URLEncoder.encode(certValid, "UTF-8");

    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	return responseStr;
	}
	
	private String ApplySM2CertByFaceRecognitionRealTime(String PersonName,String PaperType,String PaperNO,String Signature,String SignatureAlgorithm,int saveType) throws Exception{
		String p10 = "";
		String deviceSN = "";
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			p10 = genSM2Pkcs10ByBlueTooth(PersonName);
			deviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			p10 = genSM2Pkcs10ByBlueToothSim(PersonName);
			deviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
		}else{
			p10 = genSM2Pkcs10(PersonName);
			deviceSN = android.os.Build.SERIAL;
		}
					
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_ApplyCertByFaceRecognitionRealTimeEx);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("personName", PersonName);
		postParams.put("paperType", PaperType);  
		postParams.put("paperNO", PaperNO);
		postParams.put("p10",  p10);
		postParams.put("deviceID",deviceSN );  //获取设备唯一标识符
		postParams.put("signature", Signature);   
		postParams.put("signatureAlgorithm", SignatureAlgorithm);  
		postParams.put("certType", CommonConst.CERT_TYPE_SM2);  
		postParams.put("validity", CommonConst.CERT_TYPE_SM2_VALIDITY+"");  
		
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
		String certValid = CommonConst.CERT_TYPE_SM2_VALIDITY+"";
		if(isPayed)
			certValid = CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"";
		
    	String postParam = "personName="+URLEncoder.encode(PersonName, "UTF-8")+
		                   "&paperType="+URLEncoder.encode(PaperType, "UTF-8")+
		                   "&paperNO="+URLEncoder.encode(PaperNO, "UTF-8")+
		                   "&p10="+URLEncoder.encode(p10, "UTF-8")+
		                   "&deviceID="+URLEncoder.encode(deviceSN, "UTF-8")+
		                   "&signature="+URLEncoder.encode(Signature, "UTF-8")+
		                   "&signatureAlgorithm="+URLEncoder.encode(SignatureAlgorithm, "UTF-8")+
		                   "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2, "UTF-8")+
		                   "&validity="+URLEncoder.encode(certValid, "UTF-8");

    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	return responseStr;
	}
	
	
	private String applyRSACertByFaceAuth(String PersonName,String PaperType,String PaperNO) throws Exception{
		//异步调用UMSP服务：用户登录
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);			
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_ApplyCert);	
		
		String certValid = CommonConst.CERT_TYPE_SM2_VALIDITY+"";
		if(isPayed)
			certValid = CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"";
		
		String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
				                           "authKeyID",
				                           CommonConst.UM_APP_AUTH_KEY,
				                           "certType",
				                           CommonConst.CERT_TYPE_RSA,
                                           "commonName",
                                           PersonName,
                                           "paperNo",
                                           PaperNO,
                                           "paperType",
                                           PaperType,
                                           "sigAlg",
                                           CommonConst.UM_APP_SIGN_ALG,
                                           "validity",
                                           certValid);
		  

		  String strSign = "";		  
		  try {
			  strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"),CommonConst.UM_APP_PRIVATE_KEY);
		  }catch (Exception e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }

		
    	String postParam = "commonName="+URLEncoder.encode(PersonName, "UTF-8")+
		                   "&paperType="+URLEncoder.encode(PaperType, "UTF-8")+
		                   "&paperNO="+URLEncoder.encode(PaperNO, "UTF-8")+	
		                   "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_RSA, "UTF-8")+
		                   "&validity="+URLEncoder.encode(certValid, "UTF-8")+
		                   "&payMode="+URLEncoder.encode("1", "UTF-8")+
		                   "&authKeyID="+URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY, "UTF-8")+
		                   "&licence="+URLEncoder.encode(CommonConst.UM_APP_LICENSE, "UTF-8");
		                   //"&signature="+URLEncoder.encode(strSign, "UTF-8")+
		                   //"&sigAlg="+URLEncoder.encode(CommonConst.UM_APP_SIGN_ALG, "UTF-8");

    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	return responseStr;
	}
	
	private String applySM2CertByFaceAuth(String PersonName,String PaperType,String PaperNO) throws Exception{
		//异步调用UMSP服务：用户登录
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);			
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_ApplyCert);	
				
	    String certValid = CommonConst.CERT_TYPE_SM2_VALIDITY+"";
		if(isPayed)
		   certValid = CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"";
				
		String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                                          "authKeyID",
                                           CommonConst.UM_APP_AUTH_KEY,
                                           "certType",
                                           CommonConst.CERT_TYPE_SM2,
                                           "commonName",
                                           PersonName,
                                           "paperNo",
                                           PaperNO,
                                           "paperType",
                                           PaperType,
                                           "sigAlg",
                                           CommonConst.UM_APP_SIGN_ALG,
                                           "validity",
                                           certValid);

		String strSign = "";		  
        try {
           strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"),CommonConst.UM_APP_PRIVATE_KEY);
        }catch (Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }

        String postParam = "commonName="+URLEncoder.encode(PersonName, "UTF-8")+
                           "&paperType="+URLEncoder.encode(PaperType, "UTF-8")+
                           "&paperNO="+URLEncoder.encode(PaperNO, "UTF-8")+
                           "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2, "UTF-8")+
                           "&validity="+URLEncoder.encode(certValid, "UTF-8")+
                           "&payMode="+URLEncoder.encode("1", "UTF-8")+
                           "&authKeyID="+URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY, "UTF-8")+
		                   "&licence="+URLEncoder.encode(CommonConst.UM_APP_LICENSE, "UTF-8");
                           //"&signature="+URLEncoder.encode(strSign, "UTF-8")+
                           //"&sigAlg="+URLEncoder.encode(CommonConst.UM_APP_SIGN_ALG, "UTF-8");

		String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		    	
		return responseStr;
	}

	private String genPkcs10(String PersonName)  throws Exception {
      String p10 = "";
      
	  try{
		 KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		 keyGen.initialize(CommonConst.CERT_MOUDLE_SIZE);
		 mKeyPair = keyGen.genKeyPair();
		
		 String dn = "CN=" + PersonName;
		 X500Principal subjectName = new X500Principal(dn);
		 org.spongycastle.jce.PKCS10CertificationRequest kpGen = new org.spongycastle.jce.PKCS10CertificationRequest(
				 CommonConst.CERT_ALG_RSA, subjectName, mKeyPair.getPublic(), null, mKeyPair.getPrivate());
		
		 p10 = new String(Base64.encode(kpGen.getEncoded()));
		
		}catch(Exception ex){
			Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_LONG).show();
		}
	  
	    if("".equals(p10))
		  throw new Exception("生成P10失败");
		
	   return p10;
	}
	
	
	private String genPkcs10ByBlueTooth(String PersonName)  throws Exception  {
	      String p10 = "";
	      int  nRet = -1;
	      
		  try{
			 String dn = "CN=" + PersonName;
			 shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			 if(null == devInfo)
				 gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			 
			if(null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
				nRet = gEsDev.detroyRSASignCert(mStrBTDevicePwd,CommonConst.CERT_MOUDLE_SIZE);
			
			 p10 = gEsDev.genRSAPKCS10(dn, mStrBTDevicePwd,CommonConst.CERT_MOUDLE_SIZE);			
		  }catch(Exception ex){
				Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_LONG).show();
	      }
			
		  if(null == p10 || "".equals(p10))
			  throw new Exception("使用蓝牙key生成P10失败");
		  
		   return p10;
	}
	
	
	private String genPkcs10ByBlueToothSim(String PersonName)  throws Exception  {
	      String p10 = "";
	      int  nRet = -1;
	      
		  try{
			 String dn = "CN=" + PersonName;
			 if(!ScanBlueToothSimActivity.gKsSdk.isConnected())
				 ScanBlueToothSimActivity.gKsSdk.connect(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), "778899", 500);
			 			
			if(null != ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert()))
				nRet = ScanBlueToothSimActivity.gKsSdk.detroyRSAKeyPairAndCert(mStrBTDevicePwd);
			
			 p10 = ScanBlueToothSimActivity.gKsSdk.genRSAPKCS10(dn, mStrBTDevicePwd);			
		  }catch(Exception ex){
				Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_LONG).show();
	      }
			
		  if(null == p10 || "".equals(p10))
			  throw new Exception("使用蓝牙sim卡生成P10失败");
		  
		   return p10;
	}
		
	
	private String genSM2Pkcs10(String PersonName) throws Exception {
	      String p10 = "";
          /*
		  try{
			  if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode!=0)
					initShcaCciStdService();
			  
			  shcaCciStdGenKeyPairRes r = null;
							
			  if(ShcaCciStd.gSdk != null && ShcaCciStd.errorCode == 0){
					r = ShcaCciStd.gSdk.genSM2KeyPair(CommonConst.JSHECACCISTD_PWD);
					//Thread.sleep(Long.parseLong(ApplyCertRequestActivity.this.getString(R.string.Thread_Sleep)));
						
					if(r != null && r.retcode == 0){					
						//String dn = "CN=" + PersonName+",OU=Test,C=CN,ST=SH,O=Sheca";
						String dn = "CN=" + PersonName;
						
						byte[] bPubkey = android.util.Base64.decode(r.pubkey, android.util.Base64.NO_WRAP);
						p10 = ShcaCciStd.gSdk.getSM2PKCS10(dn, bPubkey, CommonConst.JSHECACCISTD_PWD, r.containerID);	
						//Thread.sleep(Long.parseLong(ApplyCertRequestActivity.this.getString(R.string.Thread_Sleep)));
						mContainerid = r.containerID;
					}
				}
			
			}catch(Exception ex){
				ShcaCciStd.gSdk = null;
				Toast.makeText(ApplyCertRequestActivity.this, "密码分割组件初始化失败",Toast.LENGTH_LONG).show();
	      }
	     */
	      
	      int retCode = -1;
		  try{
				if(null != gUcmSdk)
					retCode = initShcaUCMService();
		  }catch(Exception ex){
				
		  }
			
		  if(retCode == 0){
			   String myCid = "";		
			   JShcaUcmStdRes jres = null;		
			
			   jres = gUcmSdk.genSM2KeyPairWithPin(CommonConst.JSHECACCISTD_PWD);
		
			   if (jres.retCode == 0){
				 myCid = jres.containerid;
				 String dn = "CN=" + PersonName;
				 jres = gUcmSdk.genSM2PKCS10WithCID(myCid, CommonConst.JSHECACCISTD_PWD, dn);
				 
				 if (jres.retCode == 0){				
					 p10 = jres.response;
					 mContainerid = myCid;
				 }
			   }
		  }
		  
		  if("".equals(p10))
			  throw new Exception("密码分割组件初始化失败");
			  
		  return p10;
	}
	
	private String genSM2Pkcs10ByBlueTooth(String PersonName)  throws Exception {
	       String p10 = "";
	       int  nRet = -1;
	      
		   try{
			 String dn = "CN=" + PersonName;
			 if(null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
			    nRet = gEsDev.detroySM2SignCert(mStrBTDevicePwd);
			 if(null != gEsDev.readSM2EncryptCert() &&  !"".equals(gEsDev.readSM2EncryptCert()))
				nRet = gEsDev.detroySM2EncryptCert(mStrBTDevicePwd);
			 
			 p10 = gEsDev.genSM2PKCS10(dn, mStrBTDevicePwd);			
		   }catch(Exception ex){
				Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_LONG).show();
	       }
		   
		   if(null == p10 || "".equals(p10))
				  throw new Exception("使用蓝牙key生成P10失败");
			
		   return p10;
	}
	
	private String genSM2Pkcs10ByBlueToothSim(String PersonName)  throws Exception {
	       String p10 = "";
	       int  nRet = -1;
	      
		   try{
			 String dn = "CN=" + PersonName;
			 if(null == ScanBlueToothSimActivity.gKsSdk)
				 ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(ApplyCertRequestActivity.this.getApplication(), ApplyCertRequestActivity.this);
			 
			 if(null != ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert()))
			    nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(mStrBTDevicePwd);
			
			 p10 = ScanBlueToothSimActivity.gKsSdk.genSM2PKCS10(dn, mStrBTDevicePwd);			
		   }catch(Exception ex){
				Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_LONG).show();
	       }
		   
		   if(null == p10 || "".equals(p10))
				  throw new Exception("使用蓝牙sim卡生成P10失败");
			
		   return p10;
	}
	
	private String DownloadCert(String requestNumber,int saveType,String certType) throws Exception {
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_DownloadCert);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("requestNumber", requestNumber);
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
    	String postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8");
    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	boolean isSavedCert = false;
    	Cert cert = new Cert();
    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
    		cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), mAccountDao.getLoginAccount().getName(),certType);
    	    if(null == cert){
    	    	cert = new Cert();
    	    	isSavedCert =  false;
    	    }else{
    	    	isSavedCert = true;
    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", mAccountDao.getLoginAccount().getName());
    	    	if(null != encCert)
    	    	  mCertDao.deleteCert(encCert.getId());
    	    }
    	}
	
		cert.setEnvsn(requestNumber);
		cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
		cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
		cert.setCerttype(CommonConst.CERT_TYPE_RSA);
		cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
		cert.setSignalg(1);
		cert.setContainerid("");
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
		    cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
		    cert.setPrivatekey("");
		    cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		    
		    if(isSavedCert)
		    	mCertDao.updateCert(cert, mAccountDao.getLoginAccount().getName());		    	
		    else
		    	mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
		    cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
		    cert.setPrivatekey("");
		    cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		    
		    if(isSavedCert)
		    	mCertDao.updateCert(cert, mAccountDao.getLoginAccount().getName());		    	
		    else
		    	mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}else{
			cert.setPrivatekey(new String(Base64.encode(mKeyPair.getPrivate().getEncoded())));
			cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
			cert.setDevicesn(android.os.Build.SERIAL);
			
			mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}

		return responseStr;
	}
	
	private String DownloadSM2Cert(String requestNumber,int saveType,String certType) throws Exception {
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_DownloadCert);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("requestNumber", requestNumber);
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
    	String postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8");
    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	boolean isSavedCert = false;
    	Cert cert = new Cert();
    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
    		cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), mAccountDao.getLoginAccount().getName(),certType);
    	    if(null == cert){
    	    	cert = new Cert();
    	    	isSavedCert =  false;
    	    }else{
    	    	isSavedCert = true;
    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", mAccountDao.getLoginAccount().getName());
    	    	if(null != encCert)
    	    	  mCertDao.deleteCert(encCert.getId());
    	    }
    	}
    	
		cert.setEnvsn(requestNumber);
		cert.setPrivatekey("");
		cert.setCerttype(CommonConst.CERT_TYPE_SM2);
		cert.setSignalg(2);
		cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
		cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
		cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			cert.setContainerid("");
			cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
			cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			
			if(isSavedCert)
		    	mCertDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
		    else
		    	mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			cert.setContainerid("");
			cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
			cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			
			if(isSavedCert)
		    	mCertDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
		    else
		    	mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}else{
			cert.setContainerid(mContainerid);
			cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
			cert.setDevicesn(android.os.Build.SERIAL);
			
			mCertDao.addCert(cert,mAccountDao.getLoginAccount().getName());
		}

    	return responseStr;
	}
	

	private void saveCert(final String requestNumber, final DownloadCertResponse response,final int saveType,final int certType) {
		Builder builder = new Builder(ApplyCertRequestActivity.this);		
		builder.setIcon(R.drawable.alert);
		builder.setTitle("请设置证书密码");
		builder.setCancelable(false); 
	   // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(ApplyCertRequestActivity.this).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);	
		final EditText prikeyPasswordView = (EditText)view.findViewById(R.id.et_prikey_password);
		final EditText prikeyPassword2View = (EditText)view.findViewById(R.id.et_prikey_password2);
		prikeyPassword2View.setText("");
		prikeyPasswordView.setText("");
		
		prikeyPasswordView.requestFocus();
		prikeyPasswordView.setFocusable(true);   
		prikeyPasswordView.setFocusableInTouchMode(true);   
		
		if(!DaoActivity.bCreditAPP){
		  if(!mBBTDeviceUsed){
			builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							try {
						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						          field.setAccessible(true);
						          field.set(dialog, false);
						    } catch (Exception e) {
						            e.printStackTrace();
						    }
							
							String prikeyPassword = prikeyPasswordView.getText().toString().trim();
							String prikeyPassword2 = prikeyPassword2View.getText().toString().trim();						
							// 检查用户输入的私钥口令是否有效
							if (TextUtils.isEmpty(prikeyPassword) || !isPasswordValid(prikeyPassword)) {
								Toast.makeText(ApplyCertRequestActivity.this, "无效的证书密码,密码长度8-16位", Toast.LENGTH_SHORT).show();
								return;
							} 									
							// 检查用户输入的重复私钥口令是否有效
							if (TextUtils.isEmpty(prikeyPassword2) || !isPasswordValid(prikeyPassword2)) {
								Toast.makeText(ApplyCertRequestActivity.this, "无效的重复证书密码,密码长度8-16位", Toast.LENGTH_SHORT).show();
								return;
							}							
							// 检查用户两次输入的私钥口令是否一致
							if (!prikeyPassword.equals(prikeyPassword2)) {
								Toast.makeText(ApplyCertRequestActivity.this, "两次输入的证书密码不一致", Toast.LENGTH_SHORT).show();
								return;
							}	
							
							try {
						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						          field.setAccessible(true);
						          field.set(dialog, true);
						     } catch (Exception e) {
						           e.printStackTrace();
						     }
							//showMessage("1");
							if(CommonConst.SAVE_CERT_TYPE_RSA == certType)
							    uploadCertStatus(requestNumber,response,prikeyPassword,saveType);
							else
								uploadSM2CertStatus(requestNumber,response,prikeyPassword,saveType);
							
						} catch (Exception e) {
							Log.e(CommonConst.TAG, e.getMessage(), e);
							Toast.makeText(ApplyCertRequestActivity.this, "保存证书失败",Toast.LENGTH_SHORT).show();
						}
						
						dialog.dismiss();	
					}
				});
		
			builder.show();
		  }else{
			  try {
			     if(CommonConst.SAVE_CERT_TYPE_RSA == certType)
				    uploadCertStatus(requestNumber,response,mStrBTDevicePwd,saveType);
			     else
				    uploadSM2CertStatus(requestNumber,response,mStrBTDevicePwd,saveType);
			  } catch (Exception e) {
				  Log.e(CommonConst.TAG, e.getMessage(), e);
				  Toast.makeText(ApplyCertRequestActivity.this, "保存证书失败",Toast.LENGTH_SHORT).show();
			  }
		  }
		}else{
			String prikeyPassword = strPaperNO.substring(strPaperNO.length()-8,strPaperNO.length());   //第三方登录下载证书密码为身份证后8位
			try {
				if(CommonConst.SAVE_CERT_TYPE_RSA == certType)
			        uploadCertStatus(requestNumber,response,prikeyPassword,saveType);
				else
				    uploadSM2CertStatus(requestNumber,response,prikeyPassword,saveType);
			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(ApplyCertRequestActivity.this, "保存证书失败",Toast.LENGTH_SHORT).show();
			}
			
		}
	}
		
	private void setBlueToothPwd() {
		Builder builder = new Builder(ApplyCertRequestActivity.this);		
		builder.setIcon(R.drawable.alert);
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
		   builder.setTitle("请输入蓝牙key密码");
		else if(CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
		   builder.setTitle("请输入蓝牙sim卡密码");
		builder.setCancelable(false); 
	   // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(ApplyCertRequestActivity.this).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);	
		final EditText prikeyPasswordView = (EditText)view.findViewById(R.id.et_prikey_password);
		final EditText prikeyPassword2View = (EditText)view.findViewById(R.id.et_prikey_password2);
		prikeyPassword2View.setVisibility(RelativeLayout.GONE);
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
		   prikeyPasswordView.setHint("输入蓝牙key密码");
		else if(CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
	       prikeyPasswordView.setHint("输入蓝牙sim卡密码");
		
		prikeyPassword2View.setText("");
		prikeyPasswordView.setText("");
		
		prikeyPasswordView.requestFocus();
		prikeyPasswordView.setFocusable(true);   
		prikeyPasswordView.setFocusableInTouchMode(true);   
			
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							try {
						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						          field.setAccessible(true);
						          field.set(dialog, false);
						    } catch (Exception e) {
						            e.printStackTrace();
						    }
							
							final String prikeyPassword = prikeyPasswordView.getText().toString().trim();										
							// 检查用户输入的私钥口令是否有效
							if (TextUtils.isEmpty(prikeyPassword) || !isPasswordValid(prikeyPassword)) {
								if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
								   Toast.makeText(ApplyCertRequestActivity.this, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
								else if(CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
								   Toast.makeText(ApplyCertRequestActivity.this, "无效的蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
								
								return;
							} 									
														
							try {
						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						          field.setAccessible(true);
						          field.set(dialog, true);
						     } catch (Exception e) {
						           e.printStackTrace();
						     }
							
							mStrBTDevicePwd = prikeyPassword;
							final Button btn = (Button) findViewById(R.id.login_btn_next);		
							
							doApplyCertByFaceRecognition(btn);
							
							/*final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
							workHandler.post(new Runnable(){
					            @Override
					            public void run() {
					            	 int nRet = -1;
								     nRet = gEsDev.verifyUserPin(prikeyPassword);	
								     if(nRet != 0){
								    	 handler.post(new Runnable() {
				       						   @Override
				       						   public void run() {
				            			           Toast.makeText(ApplyCertRequestActivity.this, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
				            			           return;
				       						   }
				        					}); 
								     }else{
								    	 
								     }
					            }
							});
								*/
						} catch (Exception e) {
							Log.e(CommonConst.TAG, e.getMessage(), e);
							
						}
						
						dialog.dismiss();	
					}	
		});
		
		builder.show();
		
	}
	
	private  void  uploadCertStatus(final String requestNumber, final DownloadCertResponse response,final String prikeyPassword,final int saveType) throws Exception{
		//showProgDlg("证书保存中...");
		showLoadingView(CERT_SAVE_LOADING);
		DaoActivity.strPwd = prikeyPassword;   //保存证书密码
		
		String userCert = response.getUserCert();
		String certChain = response.getCertChain();
		Cert cert = mCertDao.getCertByEnvsn(requestNumber,mAccountDao.getLoginAccount().getName());
		cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
		cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
		byte[] bCert = Base64.decode(userCert);
		javasafeengine jse = new javasafeengine();
	    Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));
		cert.setCertchain(certChain);
		cert.setNotbeforetime(getCertNotbeforetime(userCert));
		cert.setValidtime(getCertValidtime(userCert));
		//cert.setPrivatekey("");
	    cert.setCertname(jse.getCertDetail(17, bCert)+CommonConst.CERT_RSA_NAME+"证书");
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
		   cert.setCertificate(userCert);
		   cert.setKeystore("");
		   cert.setPrivatekey("");
		   cert.setCerthash(mStrBTDevicePwd);
		   cert.setFingertype(CommonConst.USE_FINGER_TYPE);

		   int retcode = -1;
		   retcode = gEsDev.saveRSASignatureCert(mStrBTDevicePwd, userCert);
		}else{
		   cert.setCertificate(userCert);
		   String p12 = genP12(cert.getPrivatekey(), getPWDHash(prikeyPassword), userCert, certChain);
		   cert.setKeystore(p12);
		   cert.setPrivatekey("");
		   cert.setCerthash(getPWDHash(prikeyPassword));
		   cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
		}
		
		//showMessage("2");
		
		mCertDao.updateCert(cert,mAccountDao.getLoginAccount().getName());
		//Toast.makeText(ApplyCertRequestActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();
        
		//showMessage("3");
		saveLog(OperationLog.LOG_TYPE_APPLYCERT, cert.getCertsn(), "","", "");
		//showMessage("4");
		final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
		//网络访问必须放在子线程
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					//调用UMSP服务：设置证书保存成功状态
					String responseStr = SetSuccessStatus(requestNumber,saveType);	
					net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
					String resultStr = jb.getString(CommonConst.RETURN_CODE);
					String returnStr = jb.getString(CommonConst.RETURN_MSG);
					if (resultStr.equals("0")) {
						//Toast.makeText(ApplyCertRequestActivity.this, "下载证书成功", Toast.LENGTH_LONG).show();
					} else {
						throw new Exception("调用UMSP服务之SetSuccessStatus失败：" + resultStr + "，" + returnStr);
					}										
					
					responseStr = getPersonalInfo();					
					net.sf.json.JSONObject jbInfo = net.sf.json.JSONObject.fromObject(responseStr);
					resultStr = jbInfo.getString(CommonConst.RETURN_CODE);
				    String retMsg = jbInfo.getString(CommonConst.RETURN_MSG);
				    net.sf.json.JSONObject jbRet =  net.sf.json.JSONObject.fromObject(jbInfo.getString(CommonConst.RETURN_RESULT));
				    
				    GetPersonalInfo responseInfo = new GetPersonalInfo();
				    responseInfo.setResult(resultStr);
				    responseInfo.setReturn(retMsg);
				    responseInfo.setStatus(Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS)));
				    responseInfo.setIdentityCode(jbRet.getString(CommonConst.PARAM_IDENTITY_CODE));
				    responseInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
				    responseInfo.setCopyIDPhoto(jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO));
				    
					resultStr = responseInfo.getResult();
					//showMessage("3."+resultStr);
					returnStr = responseInfo.getReturn();
					if (resultStr.equals("0")) {
						handler.post(new Runnable() {
							 @Override
								public void run() {
								   closeProgDlg();
								}
						}); 
						
						showLoadingView(CERT_SAVE_OK);
						updatePersonalInfo(responseInfo);
						ApplyCertSuccess();
					}
					else{
						handler.post(new Runnable() {
							 @Override
								public void run() {
								   closeProgDlg();
								}
						}); 
						throw new Exception("调用UMSP服务之GetPersonalInfo失败：" + resultStr + "，" + returnStr);
					}
					
				} catch (Exception exc) {
					handler.post(new Runnable() {
						 @Override
							public void run() {
							   closeProgDlg();
							}
					}); 
					Toast.makeText(ApplyCertRequestActivity.this, exc.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
		
	}
	
	private  void  uploadSM2CertStatus(final String requestNumber, final DownloadCertResponse response,final String prikeyPassword,final int saveType) throws Exception{
		//showProgDlg("证书保存中...");
		showLoadingView(CERT_SAVE_LOADING);
		DaoActivity.strPwd = prikeyPassword;   //保存证书密码
		
		String userCert = response.getUserCert();
		String certChain = response.getCertChain();
		String encCert = response.getEncCert();
		String encKeystore = response.getEncKey();
		String strCertName = "";
		
		int retcode = -1;
		
		if(null == encCert)
			encCert = "";
		if(null == encKeystore)
			encKeystore = "";		
		/*	
		String userCert = "MIIDwzCCA2agAwIBAgIQd0gGJqSaAkIAFnG4Fb5GyjAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEBVrkqL1OLoNKol29i/Bvw2+rkyq0UelOP/FhEUVBkHuNO8h/YV8AYbHIRmoEBNVYzlHvmPiJ8mp4rEw1Bo1kKOCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBT5aslV8UJupOSIlYFBpq57mYDjBTAOBgNVHQ8BAf8EBAMCBsAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANJADBGAiEA81NRuSndplECK2+MPAh6IWYzQqwwWuNw9/YueSMlGfcCIQDiVn92cAwffhVBZ4vwTPQ01Gr30KvnkHL22ezyJKHenA==";
		String encCert = "MIIDwTCCA2agAwIBAgIQeYJqWnw8IHOKl55VjiyTUTAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEsyKg0zrs61met8qbSo591/Dp5olRV+22c4BjIdrF/k3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztg6OCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBQIvR69sN+jKaw0V8npb057mLrzQTAOBgNVHQ8BAf8EBAMCAzgwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANHADBEAiAZW2ykFLR4GmFO3eDzyV5IQb6Wbftib/dJUaAFthtCXwIgRndEpjqh4n1D7c21JLfyAfr8snB14LRSr7tS5tFzx/k=";
		String encKeystore = "AQAAAAEBAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABhlaYF5AoBV+JrqirNdss3OWWNofO91l6CDRLbAL8+nwABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABLMioNM67OtZnrfKm0qOfdfw6eaJUVfttnOAYyHaxf5AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztgwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAArf3K6UhMb2NFtG6XGNQRxZKeLSZbK0bFviC/XmfSEk4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAOWnbCvF0Qdq4RFVMf+roWtG9M4TKjE5Kt6hyatGdkU+yMWG1A0m4I83jAkLY3gxaQBWMS9FDggTRI8sV3llqBYQAAAAZuIhfMG87/3T/1OSe3w0ew==";
		String certChain = "MIIEiAYJKoZIhvcNAQcCoIIEeTCCBHUCAQExADALBgkqhkiG9w0BBwGgggRdMIIBpzCCAUugAwIBAgICAIEwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zODEyMzEwMDAwMDBaMDcxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDEVMBMGA1UEAwwMVUNBIFJvb3QgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEnLsOejX5v0nI1BsH6Glz/+ui/Uge27gmxsIemVDmOxKjs0Hp9ZPbqzXajUoYp9Rlcf6BmoVe02Y12ZvRHMBCU6NFMEMwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wIAYDVR0OAQEABBYEFO7osJzV3Oxz/e98+lAsxsFA5kyzMAwGCCqBHM9VAYN1BQADSAAwRQIhAJqydZmPsiPSBBWmD8bTLBXBnvDhUv4xp81GCNCBh+L+AiAoulB2Q7LIe0zFaRl1liJ9QH8NaZtI1I7eOGC8Z9gUvjCCAq4wggJRoAMCAQICEF17yetm9O3ri9K6qQyPciMwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zNzEyMzEwMDAwMDBaMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEfdEfkS0GSlQQ8ISEVSUdvKL7tcd3bsNssWlmmOhN5VCg1iLJgMDDqhO9TFt4EDsZuvECXz8uiU+BL4pddBcMgKOCAT4wggE6MEQGA1UdIAEBAAQ6MDgwNgYIKoEchu86gRUwKjAoBggrBgEFBQcCARYcaHR0cDovL3d3dy5zaGVjYS5jb20vcG9saWN5LzAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADA9BgNVHR8BAQAEMzAxMC+gLaArhilodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20yc3ViLmNybDAiBgNVHSMBAQAEGDAWgBTu6LCc1dzsc/3vfPpQLMbBQOZMszAgBgNVHQ4BAQAEFgQUiTEEkXtDqqqav4Qdm4bu8LhwmaAwSQYIKwYBBQUHAQEBAQAEOjA4MDYGCCsGAQUFBzAChipodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20ycm9vdC5kZXIwDAYIKoEcz1UBg3UFAANJADBGAiEAmFStsqFTAiEmqQUDR+0QXwTUgJYhNZicXfaGtuyKhF0CIQCDONwlcY/av+yWE+3+VVqzmiBnLKw6QnyHvLkNnEYH9DEA";								
        */
		
		Cert cert = mCertDao.getCertByEnvsn(requestNumber,mAccountDao.getLoginAccount().getName());		
		cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
		cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
		cert.setKeystore("");	
		cert.setPrivatekey("");
		cert.setCertsn(getCertSN(userCert));	
		cert.setNotbeforetime(getCertNotbeforetime(userCert));
		cert.setValidtime(getCertValidtime(userCert));
		cert.setCertchain(certChain);
		
		byte[] bCert = Base64.decode(userCert);
	    javasafeengine jse = new javasafeengine();
		strCertName = jse.getCertDetail(17, bCert)+CommonConst.CERT_SM2_NAME+"证书";
	    cert.setCertname(strCertName);
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			cert.setCertificate(userCert);
		    cert.setEnccertificate(encCert);
		    cert.setEnckeystore(encKeystore);
		    cert.setCerthash(mStrBTDevicePwd);
		    cert.setFingertype(CommonConst.USE_FINGER_TYPE);
		    
		    if(!"".equals(encCert))
			    retcode = gEsDev.saveSM2DoubleCert(mStrBTDevicePwd, userCert, encCert, encKeystore);
			else
			    retcode = gEsDev.saveSM2DoubleCert(mStrBTDevicePwd, userCert, "", encKeystore);

			if(retcode == 0){
				   if(!"".equals(encCert)){
					   if(null == mCertDao.getCertByEnvsn(requestNumber+"-e",mAccountDao.getLoginAccount().getName())){
						   Cert certEnc = new Cert();
				           certEnc.setEnvsn(requestNumber+"-e");
				           certEnc.setPrivatekey("");
				           certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
				           certEnc.setCerttype(cert.getCerttype());
				           certEnc.setSignalg(cert.getSignalg());
				           certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
				           certEnc.setContainerid("");
				           certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
				           certEnc.setCertificate(encCert);
				           certEnc.setCertchain(certChain);
				           certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
				           certEnc.setValidtime(getCertValidtime(encCert));
				           certEnc.setKeystore("");
				           certEnc.setEnccertificate(encCert);
				           certEnc.setEnckeystore(encKeystore);
				           certEnc.setCertsn(getCertSN(encCert));
				           certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
				           certEnc.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
				           certEnc.setCertname(strCertName);
				           certEnc.setCerthash(mStrBTDevicePwd);
				           certEnc.setFingertype(CommonConst.USE_FINGER_TYPE);
				
				           mCertDao.addCert(certEnc,mAccountDao.getLoginAccount().getName());
					   }
				   }  
			}		
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			cert.setCertificate(userCert);
		    cert.setEnccertificate(encCert);
		    cert.setEnckeystore(encKeystore);
		    cert.setCerthash(mStrBTDevicePwd);
		    cert.setFingertype(CommonConst.USE_FINGER_TYPE);
		    
		    if(!"".equals(encCert))
			    retcode = ScanBlueToothSimActivity.gKsSdk.saveSM2DoubleCert(mStrBTDevicePwd, userCert, encCert, encKeystore);
			else
			    retcode = ScanBlueToothSimActivity.gKsSdk.saveSM2DoubleCert(mStrBTDevicePwd, userCert, "", encKeystore);

			if(retcode == 0){
				   if(!"".equals(encCert)){
					   if(null == mCertDao.getCertByEnvsn(requestNumber+"-e",mAccountDao.getLoginAccount().getName())){
						   Cert certEnc = new Cert();
				           certEnc.setEnvsn(requestNumber+"-e");
				           certEnc.setPrivatekey("");
				           certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
				           certEnc.setCerttype(cert.getCerttype());
				           certEnc.setSignalg(cert.getSignalg());
				           certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
				           certEnc.setContainerid("");
				           certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
				           certEnc.setCertificate(encCert);
				           certEnc.setCertchain(certChain);
				           certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
				           certEnc.setValidtime(getCertValidtime(encCert));
				           certEnc.setKeystore("");
				           certEnc.setEnccertificate(encCert);
				           certEnc.setEnckeystore(encKeystore);
				           certEnc.setCertsn(getCertSN(encCert));
				           certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
				           certEnc.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
				           certEnc.setCertname(strCertName);
				           certEnc.setCerthash(mStrBTDevicePwd);
				           certEnc.setFingertype(CommonConst.USE_FINGER_TYPE);
				
				           mCertDao.addCert(certEnc,mAccountDao.getLoginAccount().getName());
					   }
				   }  
			}		
		}else{		
			cert.setCertificate(userCert);
		    cert.setEnccertificate(encCert);
		    cert.setEnckeystore(encKeystore);
		    cert.setCerthash(getPWDHash(prikeyPassword));
		    cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
/*
		    if(null == ShcaCciStd.gSdk  || ShcaCciStd.errorCode!=0)
		       initShcaCciStdService();

		    if(!"".equals(encCert))
			   retcode = ShcaCciStd.gSdk.saveSM2DoubleCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert, encCert, encKeystore);
		    else
			   retcode = ShcaCciStd.gSdk.saveSM2SignatureCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert);
*/
		    retcode = 0;
		    if(retcode == 0)
			   //retcode = ShcaCciStd.gSdk.changePin(cert.getContainerid(),CommonConst.JSHECACCISTD_PWD,getPWDHash(prikeyPassword));
		    	retcode = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),CommonConst.JSHECACCISTD_PWD,getPWDHash(prikeyPassword));
		    if(retcode == 0){
			   if(!"".equals(encCert)){
				   if(null == mCertDao.getCertByEnvsn(requestNumber+"-e",mAccountDao.getLoginAccount().getName())){
					   Cert certEnc = new Cert();
			           certEnc.setEnvsn(requestNumber+"-e");
			           certEnc.setPrivatekey("");
			           certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
			           certEnc.setCerttype(cert.getCerttype());
			           certEnc.setSignalg(cert.getSignalg());
			           certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
			           certEnc.setContainerid(cert.getContainerid());
			           certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
			           certEnc.setCertificate(encCert);
			           certEnc.setCertchain(certChain);
			           certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
			           certEnc.setValidtime(getCertValidtime(encCert));
			           certEnc.setKeystore("");
			           certEnc.setEnccertificate(encCert);
			           certEnc.setEnckeystore(encKeystore);
			           certEnc.setCertsn(getCertSN(encCert));
			           certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
			           certEnc.setDevicesn(android.os.Build.SERIAL);
			           certEnc.setCertname(strCertName);
			           certEnc.setCerthash(getPWDHash(prikeyPassword));
			           certEnc.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
			
			           mCertDao.addCert(certEnc,mAccountDao.getLoginAccount().getName());
				   }
			   }
		    }
		}
		/*byte[] bCert = Base64.decode(userCert);
		javasafeengine jse = new javasafeengine();
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));	
		String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
		cert.setKeystore(p12);
		cert.setPrivatekey("");
		*/
		//showMessage("2");
		
		try {
			if(null != cert)
			   mCertDao.updateCert(cert,mAccountDao.getLoginAccount().getName());

			saveLog(OperationLog.LOG_TYPE_APPLYCERT, cert.getCertsn(), "","", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(ApplyCertRequestActivity.this, "保存证书失败",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		//Toast.makeText(ApplyCertRequestActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();
        
		//showMessage("3");
		
		//showMessage("4");
		final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
		//网络访问必须放在子线程
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					//调用UMSP服务：设置证书保存成功状态
					String responseStr = SetSuccessStatus(requestNumber,saveType);	
					net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
					String resultStr = jb.getString(CommonConst.RETURN_CODE);
					String returnStr = jb.getString(CommonConst.RETURN_MSG);
					if (resultStr.equals("0")) {
						//Toast.makeText(ApplyCertRequestActivity.this, "下载证书成功", Toast.LENGTH_LONG).show();
						Cert cert = mCertDao.getCertByEnvsn(requestNumber,mAccountDao.getLoginAccount().getName());
						if(null != cert){
						   cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
						   mCertDao.updateCert(cert,mAccountDao.getLoginAccount().getName());
						}
						
						cert = mCertDao.getCertByEnvsn(requestNumber+"-e",mAccountDao.getLoginAccount().getName());
						if(null != cert){
						   cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
						   mCertDao.updateCert(cert,mAccountDao.getLoginAccount().getName());
						}	
					} else {
						throw new Exception("调用UMSP服务之SetSuccessStatus失败：" + resultStr + "，" + returnStr);
					}										
					
					responseStr = getPersonalInfo();				
					net.sf.json.JSONObject jbInfo = net.sf.json.JSONObject.fromObject(responseStr);
					resultStr = jbInfo.getString(CommonConst.RETURN_CODE);
				    String retMsg = jbInfo.getString(CommonConst.RETURN_MSG);
				    net.sf.json.JSONObject jbRet =  net.sf.json.JSONObject.fromObject(jbInfo.getString(CommonConst.RETURN_RESULT));
				    
				    GetPersonalInfo responseInfo = new GetPersonalInfo();
				    responseInfo.setResult(resultStr);
				    responseInfo.setReturn(retMsg);
				    responseInfo.setStatus(Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS)));
				    responseInfo.setIdentityCode(jbRet.getString(CommonConst.PARAM_IDENTITY_CODE));
				    responseInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
				    
					resultStr = responseInfo.getResult();
					//showMessage("3."+resultStr);
					returnStr = responseInfo.getReturn();
					//showMessage("4."+returnStr);
					if (resultStr.equals("0")) {
						handler.post(new Runnable() {
							 @Override
								public void run() {
								   closeProgDlg();
								}
						}); 
						
						showLoadingView(CERT_SAVE_OK);
						updatePersonalInfo(responseInfo);
						ApplyCertSuccess();
					}
					else{
						handler.post(new Runnable() {
							 @Override
								public void run() {
								   closeProgDlg();
								}
						}); 
						throw new Exception("调用UMSP服务之GetPersonalInfo失败：" + resultStr + "，" + returnStr);
					}
					
				} catch (Exception exc) {
					handler.post(new Runnable() {
						 @Override
							public void run() {
							   closeProgDlg();
							}
					}); 
					Toast.makeText(ApplyCertRequestActivity.this, exc.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
		
	}
	

	private String SetSuccessStatus(final String requestNumber,final int saveType) throws Exception {
		String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_SetSuccessStatus);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("requestNumber", requestNumber);
		postParams.put("clientOSType", "1");  //客户端操作系统类型（1：Android；2：IOS；3：WP）
		postParams.put("clientOSDesc", getOSInfo());  //客户端操作系统描述
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
		    postParams.put("media", "2");   //证书存储介质类型（1：文件；2：SD卡）
		else 
			postParams.put("media", "1");   //证书存储介质类型（1：文件；2：SD卡）
		
		if(DaoActivity.bCreditAPP)
		    postParams.put("messageType", "1");   //1：通知证书口令为身份证号后8位
		
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
		String postParam = "";
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			postParam =  "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
                         "&clientOSType="+URLEncoder.encode("1", "UTF-8")+
                         "&clientOSDesc="+URLEncoder.encode(getOSInfo(), "UTF-8")+
                         "&media="+URLEncoder.encode("2", "UTF-8");
		else
			postParam =  "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
                         "&clientOSType="+URLEncoder.encode("1", "UTF-8")+
                         "&clientOSDesc="+URLEncoder.encode(getOSInfo(), "UTF-8")+
                         "&media="+URLEncoder.encode("1", "UTF-8");
		
		if(DaoActivity.bCreditAPP)
			  postParam+="&messageType="+URLEncoder.encode("1", "UTF-8");

		String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		
    	return responseStr;
	}
	
	private  String  getPersonalInfo() throws Exception {
		String responseStr = "";
		
	    String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
	    String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);
			
		Map<String,String> postParams = new HashMap<String,String>();
		//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
		
		String postParam = "";
		responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));

		return responseStr;
	}
	
	private  boolean  updatePersonalInfo(GetPersonalInfo responseInfo) throws Exception {
		Account curAct = mAccountDao.getLoginAccount();
		curAct.setStatus(responseInfo.getStatus());  
		curAct.setIdentityCode(responseInfo.getIdentityCode());
		curAct.setIdentityName(responseInfo.getName());
		if(null != responseInfo.getCopyIDPhoto() && (!"".equals(responseInfo.getCopyIDPhoto())))
			curAct.setCopyIDPhoto(responseInfo.getCopyIDPhoto());
		
		mAccountDao.update(curAct);
		
		return true;
	}
	
	private String genP12(String privateKey, String pin, String cert, String chain) throws Exception {
		String p12 = "";
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream certBIn = new ByteArrayInputStream(
				Base64.decode(cert));
		Certificate certificate = cf.generateCertificate(certBIn);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ByteArrayInputStream bIn = new ByteArrayInputStream(
				Base64.decode(chain));
		CertPath oCertPath = cf.generateCertPath(bIn, "PKCS7");
		List certs = oCertPath.getCertificates();
		Certificate[] bChain = (Certificate[]) certs
				.toArray(new Certificate[certs.size() + 1]);
		bChain[certs.size()] = certificate;

		List certList = new ArrayList();
		for (Certificate c : bChain) {
			certList.add(c);
		}
		Collections.reverse(certList);
		bChain = (Certificate[]) certList.toArray(new Certificate[certList
				.size()]);

		KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(
				Base64.decode(privateKey));
		RSAPrivateKey privKey = (RSAPrivateKey) rsaKeyFac
				.generatePrivate(encodedKeySpec);
		ks.setKeyEntry("", privKey, pin.toCharArray(), bChain);

		ByteArrayOutputStream outp12 = new ByteArrayOutputStream();

		ks.store(outp12, pin.toCharArray());
		p12 = new String(Base64.encode(outp12.toByteArray()));
		outp12.close();
		return p12;
	}
	
	private void saveLog(int type, String certsn, String message,String invoker, String sign) {
		OperationLog log = new OperationLog();
		log.setType(type);
		log.setCertsn(certsn);
		log.setMessage(message);
		log.setSign(sign);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		log.setCreatetime(sdf.format(date));
		log.setInvoker(invoker);
		log.setSignalg(1);
		log.setIsupload(0);
		log.setInvokerid(CommonConst.UM_APPID);
		
		mLogDao.addLog(log,mAccountDao.getLoginAccount().getName());
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
	
	private void ApplyCertSuccess(){		
		if(!mIsDao){
			final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
			
			handler.post(new Runnable() {
				    @Override
					public void run() {
			            Toast.makeText(ApplyCertRequestActivity.this, "证书已下载成功", Toast.LENGTH_SHORT).show();
				    }
			}); 
			
			Intent intent = new Intent(ApplyCertRequestActivity.this, MainActivity.class);	
			startActivity(intent);	
			ApplyCertRequestActivity.this.finish();
			
			/*
			AlertDialog.Builder builder = new Builder(ApplyCertRequestActivity.this);
		    builder.setMessage("证书已下载成功!");
		    builder.setIcon(R.drawable.alert);
		    builder.setTitle("提示");
		    builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				  try {
					//Intent intent = new Intent();
					//intent.putExtra("Message", "证书申请成功，请下载证书");
					//intent.setClass(ApplyCertRequestActivity.this, MainActivity.class);		

					//startActivity(intent);
					  dialog.dismiss();
					  
				  } catch (Exception e) {
					Toast.makeText(ApplyCertRequestActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
			      }

			   }
		    });
		
		    builder.show();
		    */
		}else{
			   /*int nOperateState = DaoActivity.operateState;
			   String strOriginInfo =  DaoActivity.strResult;
			   String strServiecNo =  DaoActivity.strServiecNo;
			   String strAppName =  DaoActivity.strAppName;
			   String strCertSN =  DaoActivity.strCertSN;
			   
			   Intent inet = new Intent(ApplyCertRequestActivity.this, DaoActivity.class);
			   inet.putExtra("OperateState", nOperateState);
			   inet.putExtra("OriginInfo", strOriginInfo);
			   inet.putExtra("ServiecNo", strServiecNo);
			   inet.putExtra("AppName", strAppName);
			   inet.putExtra("CertSN", strCertSN);
			   inet.putExtra("Path", "face");
			   startActivity(inet);*/
			   DaoActivity.bCreated = false;
			   //Intent inet = new Intent(ApplyCertRequestActivity.this, DaoFaceActivity.class);
			   //startActivity(inet);
			   ApplyCertRequestActivity.this.finish();	   
		}
	}
	
	 private Boolean loginUMSPService(String act){    //重新登录UM Service
		   closeProgDlg();
		   
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_Login);

				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", act);
				postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
				postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());
				
				String postParam = "";
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					String actpwd = "";
					if(mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
						actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword());
					else
						actpwd = mAccountDao.getLoginAccount().getPassword();
					
					postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
		                        "&pwdHash="+URLEncoder.encode(actpwd, "UTF-8")+
	                            "&appID="+URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
					responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));	
				} catch (Exception e) {
					if(null== e.getMessage())
					   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
					else
					  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
				}

				net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
				String resultStr = jb.getString(CommonConst.RETURN_CODE);
				returnStr = jb.getString(CommonConst.RETURN_MSG);

				if (resultStr.equals("0")) {
					//若成功登录，注册已登录账号，并跳转到首页；
					/*
					Intent intent = new Intent(PayActivity.this, PayActivity.class);
					intent.putExtra("loginAccount", strLoginAccount);
					intent.putExtra("loginId", strLoginId);
					if(mIsDao)
						   intent.putExtra("message", "dao");
					if(mIsDownload)
						   intent.putExtra("download", "dao");
					startActivity(intent);	
					PayActivity.this.finish();
					*/
					//onPayBtnClick(workHandler);
					return true;
				} else if (resultStr.equals("10010")) {
					//若账号未激活，显示修改初始密码页面；
					if(!mIsDao){
					   Intent intent = new Intent(ApplyCertRequestActivity.this, PasswordActivity.class);
					   intent.putExtra("Account", mAccount); 
					   if(mIsDao)
					       intent.putExtra("message", "dao");
					   startActivity(intent);
					   ApplyCertRequestActivity.this.finish();
					}else{
						Intent intent = new Intent(ApplyCertRequestActivity.this, com.sheca.umandroid.PayActivity.class);
						intent.putExtra("loginAccount", strPersonName);
						intent.putExtra("loginId", strPaperNO);
					    intent.putExtra("message", "dao");
						startActivity(intent);	
						ApplyCertRequestActivity.this.finish();	
					}
				}else if(resultStr.equals("10009")){
					//若账号口令错误,显示账户登录页面；
					Account curAct = mAccountDao.getLoginAccount();
					curAct.setStatus(-1);   //重置登录状态为未登录状态
					mAccountDao.update(curAct);
					
					Intent intent = new Intent(ApplyCertRequestActivity.this, LoginActivity.class);
					intent.putExtra("AccName", curAct.getName());
					if(mIsDao)
						 intent.putExtra("message", "dao");
					startActivity(intent);
					ApplyCertRequestActivity.this.finish();
				}
				else {
					throw new Exception(returnStr);
				}
			} catch (Exception exc) {
				mError = exc.getMessage();
				Log.e(CommonConst.TAG, mError, exc);
				//Toast.makeText(AuthMainActivity.this, mError,Toast.LENGTH_LONG).show();
				return false;
			}
			
			return true;
	}
	 
	 
	private String UploadPkcs10(String requestNumber,String strCN,String certType,String signAlg,int saveType) throws Exception {
			String p10 = "";
			boolean isSavedCert = false;
			String strActName = mAccountDao.getLoginAccount().getName();
			if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
				strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
			
			Cert cert = new Cert();
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
				cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,certType);
	    	    if(null == cert){
	    	    	cert = new Cert();
	    	    	isSavedCert =  false;
	    	    }else{
	    	    	isSavedCert = true;
	    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
	    	    	if(null != encCert)
	    	    	  mCertDao.deleteCert(encCert.getId());
	    	    }
			}
			
			cert.setEnvsn(requestNumber);
			cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
			cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
			cert.setCerttype(certType);
		    cert.setSignalg(Integer.parseInt(signAlg));
			cert.setContainerid("");
			cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
			
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
				try{
					 int  nRet = -1;
					 String dn = "CN=" + strCN;
					 if(null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
					    nRet = gEsDev.detroyRSASignCert(mStrBTDevicePwd,CommonConst.CERT_MOUDLE_SIZE);
					 
					 p10 = gEsDev.genRSAPKCS10(dn, mStrBTDevicePwd,CommonConst.CERT_MOUDLE_SIZE);			
				 }catch(Exception ex){
				     Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
			     }
				
				 cert.setPrivatekey("");
				 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);	
				 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
				try{
					int  nRet = -1;
				    String dn = "CN=" + strCN;
				    if(null != ScanBlueToothSimActivity.gKsSdk){
				       if(null != ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert()))
					       nRet = ScanBlueToothSimActivity.gKsSdk.detroyRSAKeyPairAndCert(mStrBTDevicePwd);
				 
				       p10 = ScanBlueToothSimActivity.gKsSdk.genRSAPKCS10(dn, mStrBTDevicePwd);
				    }else{
				    	throw new Exception("蓝牙sim卡初始化失败");
				    }
				 }catch(Exception ex){
				     Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
			     }
				
				 cert.setPrivatekey("");
				 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);	
				 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			}else{
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			    keyGen.initialize(CommonConst.CERT_MOUDLE_SIZE);
			    KeyPair keypair = keyGen.genKeyPair();
			    String dn = "CN=" + strCN;
			    X500Principal subjectName = new X500Principal(dn);
			    PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
			    		CommonConst.CERT_ALG_RSA, subjectName, keypair.getPublic(), null, keypair.getPrivate());
			    p10 = new String(Base64.encode(kpGen.getEncoded()));
			    
			    cert.setPrivatekey(new String(Base64.encode(keypair.getPrivate().getEncoded())));
			    cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
			    cert.setDevicesn(android.os.Build.SERIAL);
			}
			
			//获取设备唯一标识符
			String deviceID = android.os.Build.SERIAL;
			
			String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_UploadPkcs10);										
			Map<String,String> postParams = new HashMap<String,String>();	
			postParams.put("requestNumber", requestNumber);
			postParams.put("p10", p10);
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   postParams.put("deviceID", sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));  //获取蓝牙设备序列号
			else
			   postParams.put("deviceID", deviceID);
			
	    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			
			String postParam = "";
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				 postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
						     "&p10="+URLEncoder.encode(p10, "UTF-8")+
						     "&deviceID="+URLEncoder.encode(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), "UTF-8");
			 else
				 postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
			                 "&p10="+URLEncoder.encode(p10, "UTF-8")+
			                 "&deviceID="+URLEncoder.encode(deviceID, "UTF-8");
			
	        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
	       
	    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
	    		if(isSavedCert)
			    	mCertDao.updateCert(cert, strActName);		    	
			    else
			    	mCertDao.addCert(cert,strActName);
	    	}else{   	
	    	   mCertDao.addCert(cert,strActName);
	    	}
	    	
	    	return responseStr;
	}

	private String UploadSM2Pkcs10(String requestNumber,String strCN,String certType,String signAlg,int saveType) throws Exception {
	    	String p10 = "";
	    	boolean isSavedCert = false;
			String strActName = mAccountDao.getLoginAccount().getName();
			if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
				strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
	    	
	    	Cert cert = new Cert();
	    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
				cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,certType);
	    	    if(null == cert){
	    	    	cert = new Cert();
	    	    	isSavedCert =  false;
	    	    }else{
	    	    	isSavedCert = true;
	    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
	    	    	if(null != encCert)
	    	    	  mCertDao.deleteCert(encCert.getId());
	    	    }
			}
	    	
			cert.setEnvsn(requestNumber);
			cert.setPrivatekey("");
			cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
			cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
			cert.setCerttype(certType);
			cert.setSignalg(Integer.parseInt(signAlg));
			cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
	    	
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
				 try{
					 int  nRet = -1;
					 String dn = "CN=" + strCN;
					 if(null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
						nRet = gEsDev.detroySM2SignCert(mStrBTDevicePwd);
					 if(null != gEsDev.readSM2EncryptCert() && !"".equals(gEsDev.readSM2EncryptCert()))
						nRet = gEsDev.detroySM2EncryptCert(mStrBTDevicePwd);
					 
					 p10 = gEsDev.genSM2PKCS10(dn,mStrBTDevicePwd);			
				 }catch(Exception ex){
						Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
			     }
					
				 cert.setContainerid("");
				 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
				 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
				try{
					 int  nRet = -1;
					 String dn = "CN=" + strCN;
					 if(null != ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert()))
						nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(mStrBTDevicePwd);
					
					 p10 = ScanBlueToothSimActivity.gKsSdk.genSM2PKCS10(dn,mStrBTDevicePwd);			
				 }catch(Exception ex){
						Toast.makeText(ApplyCertRequestActivity.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
			     }
					
				 cert.setContainerid("");
				 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
				 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			}else{	
				 /*
				try{
				    if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
					   initShcaCciStdService();
				}catch(Exception ex){
					ShcaCciStd.gSdk = null;
				}
			  
				shcaCciStdGenKeyPairRes r = null;			
			    if(ShcaCciStd.gSdk != null && ShcaCciStd.errorCode == 0){
			    	r = ShcaCciStd.gSdk.genSM2KeyPair(CommonConst.JSHECACCISTD_PWD);
			    	
			    	if(r != null && r.retcode == 0){					
			    		//String dn = "CN=" + requestNumber+",OU=Test,C=CN,ST=SH,O=Sheca";
			    		String dn = "CN=" + strCN;
					
			    		byte[] bPubkey = android.util.Base64.decode(r.pubkey, android.util.Base64.NO_WRAP);
					    p10 = ShcaCciStd.gSdk.getSM2PKCS10(dn, bPubkey, CommonConst.JSHECACCISTD_PWD, r.containerID);			    					
			    	}	
			    	
			    	if(r != null) 
			    	   cert.setContainerid(r.containerID);
				    else
					   cert.setContainerid("");
			    	
			    	cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
			    	cert.setDevicesn(android.os.Build.SERIAL);
			    }*/
				
				int retCode = -1;
				try{
					if(null != gUcmSdk)
						retCode = initShcaUCMService();
				}catch(Exception ex){
					
				}
				
				if(retCode == 0){
				   String myCid = "";		
				   JShcaUcmStdRes jres = null;		
				
				   jres = gUcmSdk.genSM2KeyPairWithPin(CommonConst.JSHECACCISTD_PWD);
			
				   if (jres.retCode == 0){
					 myCid = jres.containerid;
					 String dn = "CN=" + strCN;
					 jres = gUcmSdk.genSM2PKCS10WithCID(myCid, CommonConst.JSHECACCISTD_PWD, dn);
					 
					 if (jres.retCode == 0){				
						 p10 = jres.response;
						 if(!"".equals(myCid))
					        cert.setContainerid(myCid);
						 else
						    cert.setContainerid("");
					    	
					     cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
					     cert.setDevicesn(android.os.Build.SERIAL);
					 }
				   }
				}
			    
			    if("".equals(p10))
					  throw new Exception("密码分割组件初始化失败");
			}
				
			//获取设备唯一标识符
			String deviceID = android.os.Build.SERIAL;
			
			String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_UploadPkcs10);										
			Map<String,String> postParams = new HashMap<String,String>();	
			postParams.put("requestNumber", requestNumber);
			postParams.put("p10", p10);
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				postParams.put("deviceID", sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));  //获取蓝牙设备序列号
			else
			    postParams.put("deviceID", deviceID);
			
	    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));   
			String postParam = "";
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				 postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
						     "&p10="+URLEncoder.encode(p10, "UTF-8")+
						     "&deviceID="+URLEncoder.encode(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), "UTF-8");
			 else
				 postParam = "requestNumber="+URLEncoder.encode(requestNumber, "UTF-8")+
			                 "&p10="+URLEncoder.encode(p10, "UTF-8")+
			                 "&deviceID="+URLEncoder.encode(deviceID, "UTF-8");
			
	        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
	    	
	    	
	    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
	    		if(isSavedCert)
			    	mCertDao.updateCert(cert, strActName);		    	
			    else
			    	mCertDao.addCert(cert,strActName);
	    	}else{   	
	    	   mCertDao.addCert(cert,strActName);
	    	}
			
	    	return responseStr;
	}
	
	private void updateCertByReqNumber(String requestNumber,String strCN,String certType,String signAlg,int saveType) throws Exception {
		boolean isSavedCert = false;
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		Cert cert = new Cert();
		cert = mCertDao.getCertByEnvsn(requestNumber, strActName);
		if(null != cert)
			return;
		
		cert = new Cert();
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,certType);
    	    if(null == cert){
    	    	cert = new Cert();
    	    	isSavedCert =  false;
    	    }else{
    	    	isSavedCert = true;
    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
    	    	if(null != encCert)
    	    	  mCertDao.deleteCert(encCert.getId());
    	    }
		}
		
		cert.setEnvsn(requestNumber);
		cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
		cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
		cert.setCerttype(certType);
	    cert.setSignalg(Integer.parseInt(signAlg));
		cert.setContainerid("");
		cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			 cert.setPrivatekey("");
			 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);	
			 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			 cert.setPrivatekey("");
			 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);	
			 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		}else{
		    cert.setPrivatekey(new String(Base64.encode(mKeyPair.getPrivate().getEncoded())));
		    cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
		    cert.setDevicesn(android.os.Build.SERIAL);
		}
		
    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
    		if(isSavedCert)
		    	mCertDao.updateCert(cert, strActName);		    	
		    else
		    	mCertDao.addCert(cert,strActName);
    	}else{   	
    	   mCertDao.addCert(cert,strActName);
    	}

	}

    private void updateSM2CertByReqNumber(String requestNumber,String strCN,String certType,String signAlg,int saveType) throws Exception {
    	boolean isSavedCert = false;
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
    	
    	Cert cert = new Cert();
    	cert = mCertDao.getCertByEnvsn(requestNumber, strActName);
		if(null != cert)
			return;
		
		cert = new Cert();
    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			cert = mCertDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,certType);
    	    if(null == cert){
    	    	cert = new Cert();
    	    	isSavedCert =  false;
    	    }else{
    	    	isSavedCert = true;
    	    	Cert encCert = mCertDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
    	    	if(null != encCert)
    	    	  mCertDao.deleteCert(encCert.getId());
    	    }
		}
    	
		cert.setEnvsn(requestNumber);
		cert.setPrivatekey("");
		cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
		cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
		cert.setCerttype(certType);
		cert.setSignalg(Integer.parseInt(signAlg));
		cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
    	
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
			 cert.setContainerid("");
			 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
			 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
			 cert.setContainerid("");
			 cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
			 cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
		}else{	
		    if(null != mContainerid && !"".equals(mContainerid)) 
		       cert.setContainerid(mContainerid);
			else
			   cert.setContainerid("");
		    	
		    cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
		    cert.setDevicesn(android.os.Build.SERIAL);
		}

    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType){
    		if(isSavedCert)
		    	mCertDao.updateCert(cert, strActName);		    	
		    else
		    	mCertDao.addCert(cert,strActName);
    	}else{   	
    	   mCertDao.addCert(cert,strActName);
    	}
	}
	
    
	private String getOSInfo(){
		   String strOSInfo = "";
		   
		   strOSInfo = "硬件型号:" + android.os.Build.MODEL + "|操作系统版本号:" 
	                   + android.os.Build.VERSION.RELEASE;
		   return strOSInfo;
	}
	   
	 
	private  String  getCertSN(String strCert){
		   String strCertSN = "";
		   
		   try{
			   byte[] bCert = Base64.decode(strCert);
		       javasafeengine jse = new javasafeengine();
		       strCertSN = jse.getCertDetail(2, bCert);
		   }catch (Exception e) {
			   
		   }
		   
		   return strCertSN;
	 }
	    
	 private  String  getCertNotbeforetime(String strCert){
		   String strNotBeforeTime = "";
		   
		   try{
			   byte[] bCert = Base64.decode(strCert);
		       javasafeengine jse = new javasafeengine();
		       strNotBeforeTime = jse.getCertDetail(11, bCert);
		   }catch (Exception e) {
			   
		   }
		   
		   return strNotBeforeTime;
	 }
	   
     private  String  getCertValidtime(String strCert){
  	   String strValidTime = "";
  	   
  	   try{
  		   byte[] bCert = Base64.decode(strCert);
  	       javasafeengine jse = new javasafeengine();
  	       strValidTime = jse.getCertDetail(12, bCert);
  	   }catch (Exception e) {
  		   
  	   }
  	   
  	   return strValidTime;
	 }
   
     
     private void showManualCheck(){
    	 if(failCount == CommonConst.FACE_RECOGNITION_FAIL_COUNT){
    		if(!bfailClicked){
    	       Builder builder = new Builder(ApplyCertRequestActivity.this);		
 		       builder.setIcon(R.drawable.alert);
 	           builder.setTitle("提示");
 	           builder.setMessage("您已失败"+CommonConst.FACE_RECOGNITION_FAIL_COUNT+"次,是否提交人工审核？");							
 		       builder.setNegativeButton("确定",
 		          new DialogInterface.OnClickListener() {
 		               @Override
 			           public void onClick(DialogInterface dialog, int which) {	 
 		        	      bfailClicked = true;
			              submitManualCheckRequest(person.getStrTaskGuid());	
			              dialog.dismiss();
		               }
	              });
		
                  builder.setPositiveButton("取消",
	                  new DialogInterface.OnClickListener() {
		              @Override
		              public void onClick(DialogInterface dialog, int which) {
		        	     bfailClicked = true;
			             dialog.dismiss();
		              }
	              });
           
                  builder.show();
    		 }else{
    			 Intent intent = new Intent(ApplyCertRequestActivity.this, ManualCheckActivity.class);    
                 intent.putExtra("BizSN", person.getStrTaskGuid()); 
                 if(mIsDao)
                	 intent.putExtra("message", "dao"); 
    	         ApplyCertRequestActivity.this.startActivity(intent);
    	         ApplyCertRequestActivity.this.finish();   			 
    		 }
    	 }else{
    		 Intent intent = new Intent(ApplyCertRequestActivity.this, ManualCheckActivity.class);    
             intent.putExtra("BizSN", person.getStrTaskGuid()); 
             if(mIsDao)
            	 intent.putExtra("message", "dao"); 
	         ApplyCertRequestActivity.this.startActivity(intent);
	         ApplyCertRequestActivity.this.finish();
    	 }
     }

     private  void  submitManualCheckRequest(final String strCheckBizSN){  	
 	    final Handler handler = new Handler(ApplyCertRequestActivity.this.getMainLooper());
 	    showProgDlg("提交人工审核中...");
 		    	   
 		          new Thread(new Runnable(){
                   @Override
                      public void run() {
              	        try {
 			                //异步调用UMSP服务：获取短信验证码
 			                final String timeout = ApplyCertRequestActivity.this.getString(R.string.WebService_Timeout);				
 			                final String urlPath = ApplyCertRequestActivity.this.getString(R.string.UMSP_Service_SubmitManualCheckRequest);

 			                Map<String,String> postParams = new HashMap<String,String>();
 			                postParams.put("bizSN", strCheckBizSN);
 			                if(CommonConst.SAVE_CERT_TYPE_RSA == mAccountDao.getLoginAccount().getCertType())
							   postParams.put("certType", CommonConst.CERT_TYPE_RSA);
 			                else
 			                   postParams.put("certType", CommonConst.CERT_TYPE_SM2);
 			                postParams.put("validity", CommonConst.CERT_TYPE_SM2_VALIDITY+"");
 			                
 			               // final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
 			                
 			               String certValid = CommonConst.CERT_TYPE_SM2_VALIDITY+"";
 			      		   if(isPayed)
 			      			  certValid = CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"";
 			                
 			               String postParam = "";
 			               if(CommonConst.SAVE_CERT_TYPE_RSA == mAccountDao.getLoginAccount().getCertType())
 			      			   postParam =  "bizSN="+URLEncoder.encode(strCheckBizSN, "UTF-8")+
 			                                "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_RSA, "UTF-8")+
 			                                "&validity="+URLEncoder.encode(certValid, "UTF-8");
 			      		   else
 			      			   postParam =  "bizSN="+URLEncoder.encode(strCheckBizSN, "UTF-8")+
                                            "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2, "UTF-8")+
                                            "&validity="+URLEncoder.encode(certValid, "UTF-8");

 			      		    final String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
 			      		
 			
 			      		    net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
 			                final String resultStr = jb.getString(CommonConst.RETURN_CODE);
 			                final String returnStr = jb.getString(CommonConst.RETURN_MSG);
 
 			                if (resultStr.equals("0")){ 	
 			                	handler.post(new Runnable() {
 									 @Override
 										public void run() {
 										   closeProgDlg();
 										   Toast.makeText(ApplyCertRequestActivity.this, "人工审核已提交", Toast.LENGTH_SHORT).show();     
 										   if(mIsDao)
											   DaoActivity.bManualChecked = true;	
 										   
 										   Intent intent = new Intent(ApplyCertRequestActivity.this, ManualCheckActivity.class);    
 								           intent.putExtra("BizSN", strCheckBizSN);
 								           intent.putExtra("Manunal", "1");
 								           if(mIsDao)
 								        	 intent.putExtra("message", "dao"); 
 								           ApplyCertRequestActivity.this.startActivity(intent);
 								           ApplyCertRequestActivity.this.finish();										 
 										}
 								}); 		
 			                }else{
 			                	throw new Exception(returnStr);
 			                }
 			
 		                } catch (final Exception exc) {
 			                 handler.post(new Runnable() {
 								 @Override
 									public void run() {
 									   closeProgDlg();
 									   Toast.makeText(ApplyCertRequestActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
 									}
 							}); 
 		                }
 	                }
 	              }).start();
 	}

     
     
     private void showFaceDesc(){
    	 Intent intent = new Intent(ApplyCertRequestActivity.this, FaceGuideActivity.class);    
		 ApplyCertRequestActivity.this.startActivity(intent);	 
     }
      
	 private int initShcaCciStdService(){  //初始化创元中间件
 		int retcode = -1;
 		
 		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode!=0){
             ShcaCciStd.gSdk = ShcaCciStd.getInstance(ApplyCertRequestActivity.this);
     	     retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);    		
     	     //Toast.makeText(ApplyCertRequestActivity.this, "retcode="+retcode, Toast.LENGTH_SHORT).show();		
     	     ShcaCciStd.errorCode = retcode;
     	    
 		     if(retcode != 0)
 		    	ShcaCciStd.gSdk = null;
 		}
 		
     	return retcode;
 	}
	 
	private int initShcaUCMService(){  //初始化创元中间件
	 		int retcode = -1;
	 		byte[] bRan = null;
	 		javasafeengine jse = new javasafeengine();
	 		
	 		String myHttpBaseUrl = ApplyCertRequestActivity.this.getString(R.string.UMSP_Base_Service);		
			myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
			
			bRan = jse.random(256, "SHA1PRNG", "SUN");
			gUcmSdk.setRandomSeed(bRan);
			//gUcmSdk.setRandomSeed(bRS);
			retcode = gUcmSdk.doInitService(myHttpBaseUrl,CommonConst.UM_APPID);
	 		
	     	return retcode;
	}
	 
	private  void showLoadingView(int state){
		GifImageView gifImageView =(GifImageView) findViewById(R.id.face_loading);		
		GifImageView gifImageView2 = (GifImageView) findViewById(R.id.face_err);  
		
		try {
			GifDrawable gifDrawable = null;

			if(state == FACE_AUTH_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.faceloading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("人脸识别审核中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
			}else if(state == CERT_APPLY_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.cert_download_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("证书申请中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
			}else if(state == CERT_DOWNLOAD_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.cert_download_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("证书下载中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
			}else if(state == CERT_SAVE_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.cert_download_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("证书保存中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
			}else if(state == CERT_SAVE_OK){
				gifImageView.setVisibility(RelativeLayout.GONE);
				gifImageView2.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setImageResource(R.drawable.cert_download_ok);  
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.GONE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
			}else if(state == FACE_AUTH_ERR){
				gifImageView.setVisibility(RelativeLayout.GONE);
				gifImageView2.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setImageResource(R.drawable.face_error);  
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.GONE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.VISIBLE);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	 
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void changeProgDlg(String strMsg){
		if (null == progDialog ) { 
			showProgDlg(strMsg);
		}else{		 
		   if (progDialog.isShowing()) {
			  progDialog.setMessage(strMsg);
		   }
		}
	}

	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}
	
	
	private   void showMessage(String strCode){  
        LayoutInflater inflater = (LayoutInflater) ApplyCertRequestActivity.this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certDetailView = inflater.inflate(R.layout.certdetail, null);
		
		Builder builder = new Builder(ApplyCertRequestActivity.this);
		builder.setIcon(R.drawable.view);
		builder.setTitle(strCode+"");
		builder.setView(certDetailView);
		builder.setNegativeButton(strCode+"",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						 dialog.dismiss();
						
					}
				});
		builder.show();
    }
	 
	private Bitmap Bytes2Bimap(byte[] b) {   
			if (b.length != 0) {  
				return BitmapFactory.decodeByteArray(b, 0, b.length);
			}else {            
				return null;
			} 
	}

}