package com.sheca.umandroid;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.junyufr.szt.activity.ResultActivity;
import com.sheca.javasafeengine;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.CertApplyInfoLite;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.LogUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umandroid.util.WebUtil;
import com.sheca.umplus.activity.CaptureActivity;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

public class DaoScanActivity extends Activity {
	public static  String   strServiecNo = "";   //业务流水号
	public static  String   strAppName = "";     //第三方APP的名称
	public static  String   strAccountName = ""; //账户注册名称
	public static  String   strAccountPwd = "";  //账户注册口令
	public static  String   strAPPID = "";       //APP应用唯一标识
	public static  String   strMsgWrapper = "";  //待签名数据的包装器
	public static  String   strIsJason = "0";     //是否jason格式标志
	public static  int      operateState = 0;  //操作状态 
	public static  int      mCertCount = 0;
	
	public static  boolean  bUploadRecord = true;  //是否已申请人工审核
	
	private int      scanMode = 0;          //扫码模式	
	private String   strCallbackURL = "";   //回调地址
	private String   strMessage = "";       //待签名数据
	private String   strScanResult = "";    //二维码原文
	private boolean  isJSONDate = false;    
	
	private javasafeengine jse = null;
	private List<Cert> mData = null;
	private CertDao certDao = null;
	private LogDao mLogDao = null;
	private AccountDao mAccountDao = null;
	private SharedPreferences sharedPrefs;
	
	private  static final int LOGIN_SIGN_FAILURE = 1;
	private  static final int LOGIN_SIGN_SUCCESS = 2;
	
	private  static final int CAPTURE_CODE = 0;	
	private  static final int SCAN_CODE = 1;
	private  final  int   LOG_TYPE_SCAN = 3;
	
	private  int      operatorType = 0;	      //扫码类型
	private  int      resState = 1;           //处理结果状态 (0:成功;1:失败)
	private  boolean  bChecked = false;       //是否已人脸识别
	private  boolean  bScanDao = false;       //是否通过扫码登录进入
	
	private  WakeLock       wakeLock = null;
	private  ProgressDialog progDialog = null;
	
	static{
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		DaoActivity.bCreated = true;
		DaoActivity.bManualChecked = false;
		Intent intent = getIntent();
		
		if(LaunchActivity.LOG_FLAG){
		   if(null == LaunchActivity.logUtil){
		     LaunchActivity.logUtil = new LogUtil(DaoScanActivity.this,LaunchActivity.LOG_FLAG);   //是否记录日志
		     LaunchActivity.logUtil.init();
		   }
	    }
		
	    operateState = Integer.parseInt(intent.getExtras().getString("OperateState"));
		strAppName = intent.getExtras().getString("AppName");	
		strServiecNo = intent.getExtras().getString("ServiecNo");
		scanMode = Integer.parseInt(intent.getExtras().getString("ScanMode"));
		
		sharedPrefs = DaoScanActivity.this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
				
		strAPPID = strAppName; 
		if(scanMode == 0){   //当调用模式为implicit时需要对参数赋值
			strCallbackURL = intent.getExtras().getString("CallbackURL");
			strMessage = intent.getExtras().getString("OriginInfo");			
			
			if(CommonConst.TYPE_LOGIN.toLowerCase().equals(intent.getExtras().getString("ScanType").toLowerCase()))
			    operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
			if(CommonConst.TYPE_SIGN.toLowerCase().equals(intent.getExtras().getString("ScanType").toLowerCase()))
				operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;	
			
			strMsgWrapper = intent.getExtras().getString("MsgWrapper");	
		    if(null == strMsgWrapper) 	   
		    	strMsgWrapper = "0";
		    
		    strIsJason = intent.getExtras().getString("isjason");	
		    if(null == strIsJason) 	   
		    	strIsJason = "0";
		    
		}
		
		if(!getAppInfo(strAPPID)){
			Toast.makeText(DaoScanActivity.this,"非法应用", Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}  
		
		if(!isNetworkAvailable(DaoScanActivity.this))
		   Toast.makeText(DaoScanActivity.this,"网络连接或访问服务异常", Toast.LENGTH_SHORT).show();	
			
		if(null != intent.getExtras().getString("AccountName"))
			strAccountName = intent.getExtras().getString("AccountName");
		if(null != intent.getExtras().getString("AccountPwd"))
			strAccountPwd = intent.getExtras().getString("AccountPwd");
		
		jse = new javasafeengine();
		certDao = new CertDao(DaoScanActivity.this);
		mLogDao = new LogDao(DaoScanActivity.this);
		
		if(WebClientUtil.mCertChainList.size() == 0)
         	getCertChain();
		
		mAccountDao = new AccountDao(DaoScanActivity.this);
		if(mAccountDao.count() == 0){   //用户未登录	
			ShowLogin();
			return;
		}
	
		if(ckeckLogin()){//自动登录检测
			ShowLogin();
			return;
		}else{
			try {
				if(!getAppInfo(strAPPID)){
						Toast.makeText(DaoScanActivity.this,"非法应用", Toast.LENGTH_SHORT).show();
						onBackPressed();
						return;
				}  
				
				loginUMSPService(mAccountDao.getLoginAccount().getName());
				mCertCount = getCertListCount();
				
		    }catch (Exception e) {
				 // TODO Auto-generated catch block
				 //e.printStackTrace(); 
				 Toast.makeText(DaoScanActivity.this,"非法应用", Toast.LENGTH_SHORT).show();
				 onBackPressed();
				 return;
			}
										  						
			if(operateState == LOG_TYPE_SCAN){	   
				setContentView(R.layout.activity_launch);
				checkCert();    
			}else{			  
				onBackPressed();
				return;
			}	   
												
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if(DaoActivity.bManualChecked){
			resState = 2;    //已提交人工审核,未下载证书
			
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("ServiecNo", strServiecNo);
			bundle.putInt("Code", resState);
			resultIntent.putExtras(bundle);
			
			DaoScanActivity.this.setResult(RESULT_OK, resultIntent);
			DaoScanActivity.this.finish();			
			return;
		}
		
		if(!DaoActivity.bCreated){
		   if(mAccountDao.count() == 0){   //用户未登录
			  ShowLogin();
			  return;
		   }
		   
		  /* 
		   if(scanMode == 0)
			   showScanCert(operatorType,strCallbackURL,strServiecNo,strMessage);
		   else
		       showScanDlg();
		 */
		   
		   try{ 
			   mData = getData();
		   } catch (Exception e) {
				// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
		   
		   if(operateState == LOG_TYPE_SCAN){	   
				//setContentView(R.layout.activity_launch);
				checkCert();    
			}
	   }
		
	}

	
	public void onBackPressed(){
	     showExitFrame();
    }
    
	private void showExitFrame(){
		Intent resultIntent = new Intent();
		Bundle bundle = new Bundle();

		bundle.putInt("Code", 1);
		resultIntent.putExtras(bundle);
		
		DaoScanActivity.this.setResult(RESULT_CANCELED, resultIntent);
		DaoScanActivity.this.finish();
	}
	

    private void ShowLogin(){
		Intent i = new Intent(DaoScanActivity.this, LoginActivity.class);
		i.putExtra("scan", "dao");
		if(!"".equals(strAccountName)){
			i.putExtra("AccName", strAccountName);
			i.putExtra("AccPwd", strAccountPwd);
		}
		
		startActivity(i);
		//DaoActivity.this.finish();
    }
    
    private void showScanDlg(){
    	Intent i = new Intent(DaoScanActivity.this, CaptureActivity.class);
    	i.putExtra("scan", "dao");
	    startActivityForResult(i, CAPTURE_CODE);   	
    }
    
	private List<Cert> getData() throws Exception {
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

		List<Cert> allCertList =  certDao.getAllCerts(strActName);
		
		for (Cert cert : allCertList) {
			if(cert.getEnvsn().indexOf("-e")!=-1)
				continue;
			if(CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
				continue;
			
			if(null == cert.getCertificate() ||"".equals(cert.getCertificate()))
				continue;
			
			if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
				certList.add(cert);
			}
		}
		
		return certList;
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		DaoActivity.bCreated = true;
		
		if (requestCode == CAPTURE_CODE) {
			// 处理扫描结果（在界面上显示）
			if (resultCode == DaoScanActivity.RESULT_OK) {
				Bundle bundle = data.getExtras();
				String strReslut = bundle.getString("result");
				if(strReslut.indexOf("?") == -1){
					strReslut = parseJSONScanResult(strReslut);
				}
				
				final String scanResult = strReslut;
				strScanResult = scanResult;
				try {
					final String urlPath = WebUtil.getUrlPath(scanResult);

					if ("".equals(urlPath)) {
						throw new Exception("二维码格式错误");
					}

					Map<String, String> params = WebUtil
							.getURLRequest(scanResult);
					
					String type = params.get(CommonConst.PARAM_TYPE);
						
					if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase())!=-1){
						showScanCert(scanResult);
					} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase())!=-1) {
						showScanCert(scanResult);
					} else{
						throw new Exception("二维码内容错误");
					}
					
				} catch (Exception e) {
					Log.e(CommonConst.TAG, e.getMessage(), e);
					Builder builder = new Builder(DaoScanActivity.this);
					builder.setMessage(e.getMessage());
					builder.setIcon(R.drawable.warning);
					builder.setTitle("异常");
					builder.setNegativeButton("二维码内容",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									Builder builder = new Builder(
											DaoScanActivity.this);
									builder.setMessage(scanResult);
									builder.setIcon(R.drawable.alert);
									builder.setTitle("二维码内容");
									builder.setNegativeButton(
											"复制",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													ClipboardManager cmb = (ClipboardManager) DaoScanActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);					    
													cmb.setPrimaryClip( ClipData.newPlainText("scanResult", scanResult));
												}
											});
									builder.setPositiveButton(
											"关闭",
											new DialogInterface.OnClickListener() {

												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
												}
											});
									builder.show();
								}

							});
					builder.setPositiveButton("关闭",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
		}else if (requestCode == SCAN_CODE) {
			if (resultCode == DaoScanActivity.this.RESULT_OK) {
				Bundle bundle = data.getExtras();
				doScan(bundle.getString("ServiecNo"),
					   bundle.getString("OriginInfo"),
					   bundle.getString("Sign"),
					   bundle.getString("Cert"),
					   bundle.getString("CertSN"),
					   bundle.getString("UniqueID"),
					   bundle.getString("CertType"),
					   bundle.getString("SaveType"),
					   bundle.getString("AppID"));
			}
			if (resultCode == DaoScanActivity.this.RESULT_CANCELED) {
				
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private  String  parseJSONScanResult(String scanResult){
		String  strReturn = "";
		
		try{
		   JSONObject jb = JSONObject.fromObject(scanResult);
		   String serviceURL = jb.getString(CommonConst.QR_SERVICEURL);
		   String actionName =  jb.getString(CommonConst.QR_ACTIONNAME).replace("_", "");
		   String bizSN =  jb.getString(CommonConst.PARAM_BIZSN);
		   String message  =  "";
		   if(actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase()))
		        message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);
		   else
		        message = jb.getString(CommonConst.PARAM_MESSAGE);
		
		   if(actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase()))
		        strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_RANDOM_NUMBER+"=%s", 
		    		                      serviceURL,CommonConst.QR_Login,bizSN,message);
		   else
			    strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s", 
			    		                   serviceURL,CommonConst.QR_Sign,bizSN,message);
					 
		   isJSONDate = true;							
		}catch(Exception ex){							
			strReturn = scanResult;							
		}                       
		
		return  strReturn;
		
	}
	
	private  void  showScanCert(final String scanResult){
		Map<String, String> params = WebUtil.getURLRequest(scanResult);
		final String urlPath = WebUtil.getUrlPath(scanResult);
	    String certsn = "";

	    if(null != params.get(CommonConst.PARAM_CERTSN))
			  certsn = params.get(CommonConst.PARAM_CERTSN);
	    
        Intent intent = new Intent(DaoScanActivity.this, DaoActivity.class);	
		Bundle extras = new Bundle();
		extras.putString("ScanDao", "scan");
		extras.putString("ServiecNo", params.get(CommonConst.PARAM_BIZSN));
		if(isJSONDate)
			extras.putString("IsJson", "isJson");
	    
		if(urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase())!=-1){			
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_RANDOM_NUMBER));
            extras.putString("AppName", "扫码登录");
            extras.putString("OperateState", "1");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
		} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase())!=-1) {
	        try {
	        	   //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
	        	   //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
	        	 //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");    
	        	extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
	        	//extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
			}
	        extras.putString("CertSN", certsn);
	        extras.putString("AppName", "扫码签名");
	        extras.putString("OperateState", "2");	
	        if(null != params.get(CommonConst.PARAM_MSGWRAPPER))	        
	        	extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
	        else
	        	extras.putString("MsgWrapper", "0");
	        operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
		}
		
		intent.putExtras(extras);
		startActivityForResult(intent, SCAN_CODE);
	}
	
	
	private  void  showScanCert(int scanType,String scanCallbackURL,String bizSN,String message){ 
        Intent intent = new Intent(DaoScanActivity.this, DaoActivity.class);	
		Bundle extras = new Bundle();
		extras.putString("ScanSDKDao", "scan");
		extras.putString("ServiecNo", bizSN);
		extras.putString("OriginInfo", message);
		if("1".equals(strIsJason))
		   extras.putString("IsJson", "isJson");
	    
		if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN){			
            extras.putString("AppName", strAppName);
            extras.putString("OperateState", "1");
		} else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN) {
	        extras.putString("AppName", strAppName);
	        extras.putString("OperateState", "2");	 
	        extras.putString("MsgWrapper", strMsgWrapper);	 
		}
		
		intent.putExtras(extras);
		startActivityForResult(intent, SCAN_CODE);
	}
	
	
	private  void   doScan(final String token,final String orgDate,final String signDate,final String cert,final String certSN,final String uniID,final String certType,final String saveType,final String appID){
		wakeLock = ((PowerManager) DaoScanActivity.this
				.getSystemService(DaoScanActivity.this.POWER_SERVICE))
				.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ON_AFTER_RELEASE,
						"Login");
		wakeLock.acquire();

		progDialog = new ProgressDialog(DaoScanActivity.this);
		if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN)
		   progDialog.setMessage("正在签名...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN)
		   progDialog.setMessage("正在登录...");
		progDialog.setCancelable(false);
		progDialog.show();
		
		 new Thread() {
			@Override
			public void run() {	
				if (signDate != null) {
					Map<String, String> postParams = new HashMap<String, String>();
					String postHttpParams = "";
					
					try {
					 if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN){
					   postParams.put("bizSN", token);
					   postParams.put("appID", appID);
					   postParams.put("idNumber", uniID);
					   postParams.put("cert", cert);
					   postParams.put("message", orgDate);
					   postParams.put("signatureValue", signDate);
					   
					   postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
							             "&appID="+URLEncoder.encode(appID, "UTF-8")+
							             "&idNumber="+URLEncoder.encode(uniID, "UTF-8")+
							             "&cert="+URLEncoder.encode(cert, "UTF-8")+
							             "&message="+URLEncoder.encode(orgDate, "UTF-8")+
							             "&signatureValue="+URLEncoder.encode(signDate, "UTF-8");
					   
					   if(isJSONDate){					   
						      if(CommonConst.CERT_TYPE_SM2.equals(certType)){
						         postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         
						         postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
						        		           "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
						            postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
						            postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }
							     else{
								    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
								    postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
							     
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     } else{
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
							     
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
							     postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     
							     postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
				        		                   "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }
					   }else{
							   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
		        		                             "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
							           postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
							           postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
							       postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
							       postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
								       postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
      		                                         "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   }					   
					   }
					 }else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN){
						postParams.put("bizSN", token);
						postParams.put("appID", appID);
						postParams.put("idNumber", uniID);
						postParams.put("randomNumber", orgDate);
						postParams.put("message", orgDate);
						postParams.put("cert", cert);
						postParams.put("signatureValue", signDate);
						
						postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
					             "&appID="+URLEncoder.encode(appID, "UTF-8")+
					             "&idNumber="+URLEncoder.encode(uniID, "UTF-8")+
					             "&randomNumber="+URLEncoder.encode(orgDate, "UTF-8")+
					             "&message="+URLEncoder.encode(orgDate, "UTF-8")+
					             "&cert="+URLEncoder.encode(cert, "UTF-8")+
					             "&signatureValue="+URLEncoder.encode(signDate, "UTF-8");
						
						if(isJSONDate){					   
						      if(CommonConst.CERT_TYPE_SM2.equals(certType)){
						         postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         
						         postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
				        		                   "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
						            postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
						            postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
								    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
								    postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
							     postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     
							     postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
		        		                           "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }						  
						}else{
							   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
        		                                     "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
							           postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
							           postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
							       postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
							       postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
								       postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
		                                             "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   } 
						}					
					 }
					}catch(Exception e){
						Log.e(CommonConst.TAG, e.getMessage(),e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
					}

					try {
						String strPostUrlPath = "";
						String urlPath = "";
						
						if(scanMode == 0){
							strPostUrlPath = strCallbackURL.substring(0,strCallbackURL.lastIndexOf("/"));	
						}else{
						    urlPath = WebUtil.getUrlPath(strScanResult);
						    strPostUrlPath = urlPath.substring(0,urlPath.lastIndexOf("/"));						
						}
						
						String sResp = null;
						//sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
						
						sResp = WebClientUtil.postHttpClientJson(strPostUrlPath,postHttpParams, 5000);
						/*if (!"ok".equals(sResp)) {
							Message msg = new Message();
							msg.what = FAILURE;
							Bundle data = new Bundle();
							data.putString("result", sResp);
							msg.setData(data);
							handler.sendMessage(msg);
						} else {*/
						//showMessage();
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN){
							if(scanMode == 0)
							   saveLog(OperationLog.LOG_TYPE_SIGN,
									certSN,
									new String(Base64.decode(URLDecoder.decode(strMessage,"UTF-8"))),
									new String(Base64.decode(URLDecoder.decode(strMessage,"UTF-8"))),
									signDate);
							else
								  saveLog(OperationLog.LOG_TYPE_SIGN,
											certSN,
											new String(Base64.decode(URLDecoder.decode(orgDate,"UTF-8"))),
											new String(Base64.decode(URLDecoder.decode(orgDate,"UTF-8"))),
											signDate);
							
							handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
						}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN){
							if(scanMode == 0)
							   saveLog(OperationLog.LOG_TYPE_LOGIN,
									certSN,
									token, 
									strCallbackURL, 
									signDate);
							else
								saveLog(OperationLog.LOG_TYPE_LOGIN,
									certSN,
									token, 
								    urlPath, 
									signDate);
							
							handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
						}									
						//}
					} catch (Exception e) {
						Log.e(CommonConst.TAG, e.getMessage(),
								e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
					}
				}
				if (progDialog.isShowing()) {
					progDialog.dismiss();
				}

				if (wakeLock != null) {
					wakeLock.release();
				}
			}
		}.start();

	}
	
	private void saveLog(int type, String certsn, String message,String invoker,String sign) {
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
		
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

		mLogDao.addLog(log,strActName);
	}
		
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOGIN_SIGN_FAILURE:{
				resState = 1;
				Toast.makeText(DaoScanActivity.this, "数字签名错误",Toast.LENGTH_SHORT).show();
				Intent resultIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("ServiecNo", strServiecNo);
				bundle.putInt("Code", resState);
				resultIntent.putExtras(bundle);
				
				DaoScanActivity.this.setResult(RESULT_OK, resultIntent);
			    DaoScanActivity.this.finish();			
			}
				break;
			case LOGIN_SIGN_SUCCESS:{
				   resState = 0;
				   Intent resultIntent = new Intent();
				   Bundle bundle = new Bundle();
				   bundle.putString("ServiecNo", strServiecNo);
				   bundle.putInt("Code", resState);
				   resultIntent.putExtras(bundle);
				
				   DaoScanActivity.this.setResult(RESULT_OK, resultIntent);
				   DaoScanActivity.this.finish();			
			}
				break;
			}
		}
	};
	
	private  void  checkCert(){	
	    try {	
	 	   mData = getData();
	 	 	    	
		   if(mData.size() == 0){   //判断本地是否有证书		 
			 if(mCertCount > 0){
				Intent intent = new Intent(DaoScanActivity.this, ApplicationActivity.class);
				intent.putExtra("scan", "dao");
				startActivity(intent);					
			 }else{
                Intent intent =  null;	
				
                if(mAccountDao.getLoginAccount().getStatus() == 2 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4){  //账户已实名认证
                	if("".equals(sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, ""))){
                		intent = new Intent(DaoScanActivity.this, com.junyufr.szt.activity.AuthMainActivity.class);
                	    //intent = new Intent(DaoScanActivity.this, com.sheca.umandroid.PayActivity.class);
					    intent.putExtra("loginAccount", mAccountDao.getLoginAccount().getIdentityName());
						intent.putExtra("loginId", mAccountDao.getLoginAccount().getIdentityCode());      				
                	}else{
    	  		        ResultActivity.strSignature = sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, "");
				        intent = new Intent(DaoScanActivity.this, com.junyufr.szt.activity.AuthMainActivity.class);
				        
    	  			    //intent = new Intent(DaoScanActivity.this, com.sheca.umandroid.PayActivity.class);
    	  			    intent.putExtra("loginAccount", mAccountDao.getLoginAccount().getIdentityName()); 
    	  			    intent.putExtra("loginId", mAccountDao.getLoginAccount().getIdentityCode()); 
    			}	
    			
                }else{								
                	intent = new Intent(DaoScanActivity.this, AuthChoiceActivity.class);								 
                }
				
                intent.putExtra("message", "dao");							 
                startActivity(intent);							
			}
		}else{
			if(scanMode == 0)
			   showScanCert(operatorType,strCallbackURL,strServiecNo,strMessage);	   
		    else
			   showScanDlg();
		}
				
	  }catch (Exception e) {
    		e.printStackTrace();
      }
	}
		
	private   void  getCertChain(){       
		InputStream inCfg = null;
		Properties prop = new Properties();
		int certCount = 0;
		WebClientUtil.mCertChainList.clear();
		
	    try { 
	    	inCfg = this.getAssets().open("CertChain.properties");; 
	        prop.load(inCfg);
	        certCount = Integer.parseInt(prop.getProperty("CertChainNum"));

	        for(int i = 0;i < certCount;i++){
	        	String  chainIndex = String.format("CertChain%s", i+""); 
	       	    String chainCont = prop.getProperty(chainIndex);
	       	    WebClientUtil.mCertChainList.add(chainCont);
	        }
	        
	    } catch (Exception e) {
	    	e.getMessage();
	    }finally {
	    	try{
	    		if (inCfg != null)
	    			inCfg.close();
	    	} catch (IOException e) {     
	    		e.printStackTrace(); 	
	    	}	    
	    } 
	}
	
	
	private  boolean  ckeckLogin(){
		if(!"".equals(strAccountName)){
			if(!strAccountName.equals(mAccountDao.getLoginAccount().getName())){
				logoutAccount();
				return true;
			}
			else
				return false;
		}
		
		return false;
	}
	
	private int getCertListCount() {
		List<CertApplyInfoLite> applications = null;
		
		try {
			String responseStr = GetCertApplyListEx();
			//处理服务返回值
			JSONObject jb = JSONObject.fromObject(responseStr);
    		String resultStr  = jb.getString(CommonConst.RETURN_CODE);
    		String returnStr = jb.getString(CommonConst.RETURN_MSG);
    		JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));	
				
    		if("0".equals(resultStr)){
    		   applications = new ArrayList<CertApplyInfoLite>();	
			   for(int i = 0;i<transitListArray.size();i++){
				  CertApplyInfoLite certApplyInfo = new CertApplyInfoLite();
				  JSONObject jbRet =  transitListArray.getJSONObject(i) ;
				  certApplyInfo.setRequestNumber(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER));
				  certApplyInfo.setCommonName(jbRet.getString(CommonConst.PARAM_COMMON_NAME));
				  certApplyInfo.setApplyTime(jbRet.getString(CommonConst.PARAM_APPLY_NAME));						
				  certApplyInfo.setApplyStatus(Integer.parseInt(jbRet.getString(CommonConst.PARAM_APPLY_STATUS)));	
			      certApplyInfo.setBizSN(jbRet.getString(CommonConst.PARAM_BIZSN));
			      certApplyInfo.setCertType(jbRet.getString(CommonConst.PARAM_CERT_TYPE));
			      certApplyInfo.setSignAlg(Integer.parseInt(jbRet.getString(CommonConst.PARAM_SIGNALG_PLUS)));
			
				  applications.add(certApplyInfo);
			   }
    		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(null == applications )
			return 0;
		
		return applications.size();
	}
	
	private  Boolean loginUMSPService(String act) throws Exception{    //重新登录UM Service
		   String returnStr = "";
			try {
				//showProgDlg("获取更新数据中...");
				//异步调用UMSP服务：用户登录
				String timeout = DaoScanActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = DaoScanActivity.this.getString(R.string.UMSP_Service_Login);
				String strPass = "";
				if(mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
				   strPass =  getPWDHash(mAccountDao.getLoginAccount().getPassword());
				else
				   strPass =  mAccountDao.getLoginAccount().getPassword();
				
				
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", act);
				postParams.put("pwdHash", strPass);    //账户口令需要HASH并转为BASE64字符串
				postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());
				
				String responseStr = "";
				try {
					//清空本地缓存
					//WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					
					String postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
							           "&pwdHash="+URLEncoder.encode(strPass, "UTF-8")+
							           "&appID="+URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
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

			} catch (Exception exc) {
				//closeProgDlg();
				return false;
			}
			
			//closeProgDlg();
			return true;
	}
	
	private void logoutAccount(){
		//用户注销	
		final String timeout = DaoScanActivity.this.getString(R.string.WebService_Timeout);				
		final String urlPath = DaoScanActivity.this.getString(R.string.UMSP_Service_Logout);										
	    final Map<String,String> postParams = new HashMap<String,String>();	
			
	    try {  
	    	//final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
	    	
	    	String postParam = "";
	    	final String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			//处理服务返回值
	    	JSONObject jb = JSONObject.fromObject(responseStr);
			String resultStr = jb.getString(CommonConst.RETURN_CODE);
			final String returnStr  = jb.getString(CommonConst.RETURN_MSG);	
			if (resultStr.equals("0")) {
				//当前账户退出,更新登录状态
				Account curAct = mAccountDao.getLoginAccount();
				curAct.setStatus(-1);   //重置登录状态为未登录状态
				mAccountDao.update(curAct);
			} else if (resultStr.equals("10012")) {
				//当前账户退出,更新登录状态
				Account curAct = mAccountDao.getLoginAccount();
				curAct.setStatus(-1);   //重置登录状态为未登录状态
				mAccountDao.update(curAct);
			} else {
				Account curAct = mAccountDao.getLoginAccount();
				curAct.setStatus(-1);   //重置登录状态为未登录状态
				mAccountDao.update(curAct);
			}
		} catch (Exception exc) {
			Log.e(CommonConst.TAG, exc.getMessage(), exc);
			
			Account curAct = mAccountDao.getLoginAccount();
			curAct.setStatus(-1);   //重置登录状态为未登录状态
			mAccountDao.update(curAct);
		}	
	}
	
	private String GetCertApplyListEx() throws Exception {
		String timeout = DaoScanActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = DaoScanActivity.this.getString(R.string.UMSP_Service_GetCertApplyList);										
		Map<String,String> postParams = new HashMap<String,String>();	
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
    	String postParam = "";
    	String  responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	return responseStr;
	}
	
	private  Boolean getAppInfo(String appID){  
		appID = appID.replace("-", "");
		String[] appConfig = CommonConst.UM_APPID_CONFIG.split("-");
		
		for(int i = 0;i<appConfig.length;i++){
			if(appID.equals(appConfig[i]))
				return true;
		}
		
		return false;
	}
	
	
	private  String   getPWDHash(String strPWD){
		String strPWDHash = "";
		
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		return strPWDHash;
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
		
		if (wakeLock != null) {
			wakeLock.release();
		}
	}
	
	
}