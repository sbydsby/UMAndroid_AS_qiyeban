package com.sheca.umee.service;


import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sheca.javasafeengine;
import com.sheca.umee.R;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.LogDao;
import com.sheca.umee.model.OperationLog;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
  
public class UploadLogService extends Service {  
	public  static Activity mActivity = null;
	
	private LogDao logDao = null;
	private AccountDao accountDao = null;	
	private OperationLog log = null;
	
	private  final int LOG_TYPE_LOGIN = 1;
	private  final int LOG_TYPE_SIGN = 2;
	private  final int LOG_TYPE_INPUTCERT = 8;
	
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
  
    @Override  
    public void onCreate() {  
    	super.onCreate();
    	//mActivity = (Activity)getApplicationContext();
    
    }  
  
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	//strSignature = intent.getStringExtra(CommonConst.PARAM_SIGNATURE);
    	
    	//flags = START_STICKY;  
    	logDao = new LogDao(mActivity);		
		accountDao = new AccountDao(mActivity);
		
		log = logDao.getLogByID(Integer.parseInt(intent.getExtras().getString("logId")));
    	//mPeriodicEventHandler = new Handler();
    	
    	
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override  
    public void onDestroy() {  
    	//mPeriodicEventHandler.removeCallbacks(doPeriodicTask);
    	mActivity = null;
	    super.onDestroy();
    }  
  
    @Override  
    public void onStart(Intent intent, int startid) {  
    	//mPeriodicEventHandler.postDelayed(doPeriodicTask,PERIODIC_EVENT_TIMEOUT);
    	
    	try {
    		uploadLogRecord();
			//uniTrustFunc.getAppLicence(strAppName,strAuthKeyID,strPackageName,strSignAlg,strSignature);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
    
    private  void  uploadLogRecord() throws Exception{
    	boolean bUploadRecord = false;    	
    	String strActName = accountDao.getLoginAccount().getName();	
	
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

		try {
			if(0 == log.getIsupload()){
				try{
					bUploadRecord = loginUMSPService(strActName);
				}catch(Exception ex){
					bUploadRecord = false;
				}
				
				if(bUploadRecord){				
					String strBizType = "";

				    if(log.getType() == OperationLog.LOG_TYPE_DAO_LOGIN)
					    strBizType = LOG_TYPE_LOGIN+"";
				    else if(log.getType() == OperationLog.LOG_TYPE_DAO_SIGN)
					    strBizType = LOG_TYPE_SIGN+"";
				    else if(log.getType() == OperationLog.LOG_TYPE_LOGIN)
					    strBizType = LOG_TYPE_LOGIN+"";
				    else if(log.getType() == OperationLog.LOG_TYPE_SIGN)
					    strBizType = LOG_TYPE_SIGN+"";
				    else if(log.getType() == OperationLog.LOG_TYPE_INPUTCERT)
					    strBizType = LOG_TYPE_INPUTCERT+"";
				    else
					    return;
					
				    String strInvokerId = "";
				    strInvokerId = log.getInvokerid();
				    if(strInvokerId == null)
					    return;
					
				    if("".equals(strInvokerId)){
				    	if(CommonConst.CREDIT_APP_NAME.equals(log.getInvoker()))
				    		strInvokerId = CommonConst.CREDIT_APP_ID;
					    else if(CommonConst.UTEST_APP_NAME.equals(log.getInvoker()))
						    strInvokerId = CommonConst.UTEST_APP_ID;
					    else if(CommonConst.NETHELPER_APP_NAME.equals(log.getInvoker()))
						    strInvokerId = CommonConst.NETHELPER_APP_ID;
					    else if(CommonConst.SCAN_SIGN_NAME.equals(log.getInvoker()))
						    strInvokerId = CommonConst.UM_APPID;
					    else if(CommonConst.SCAN_LOGIN_NAME.equals(log.getInvoker()))
						    strInvokerId = CommonConst.UM_APPID;
					    else
						    strInvokerId = CommonConst.UM_APPID;
				     }
											
				    String responseStr = uploadCertRecord(strInvokerId,log.getCertsn(),strBizType,log.getCreatetime(),log.getMessage(),log.getSign(),log.getSignalg());
				    JSONObject jb = JSONObject.fromObject(responseStr);
					String resultStr = jb.getString(CommonConst.RETURN_CODE);
					String returnStr = jb.getString(CommonConst.RETURN_MSG);
		            
		           if("0".equals(resultStr)){
		               log.setIsupload(1);
		               logDao.updateLog(log,strActName);
		            	
		               //Toast.makeText(mActivity,"appid:"+strInvokerId+",bizType:"+strBizType+",time:"+log.getCreatetime(), Toast.LENGTH_LONG).show();		       
		           }
				}
			}
		} catch (Exception e) {
				// TODO Auto-generated catch block		
				e.printStackTrace();
				//Toast.makeText(DaoActivity.this,"网络连接异常,无法上传记录", Toast.LENGTH_LONG).show();
				return;
		}									


    }
       
    private String uploadCertRecord(String appID,String certSN,String bizType,String bizTime,String message,String msgSignature,int signAlg) throws Exception {
		String timeout = mActivity.getString(R.string.WebService_Timeout);				
		String urlPath = mActivity.getString(R.string.UMSP_Service_UploadCertRecord);
		
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("reqAppID", appID);
		postParams.put("certSN", certSN);
		postParams.put("bizType", bizType);
		postParams.put("bizTime", bizTime);
		postParams.put("message", message);
		postParams.put("msgSignature", msgSignature);
		if(signAlg == 1)
		    postParams.put("msgSignatureAlgorithm", CommonConst.UPLOAD_LOG_SIGNALG_TYPE+"");
		else
			postParams.put("msgSignatureAlgorithm", CommonConst.UPLOAD_LOG_SIGNALG_TYPE_SM2+"");
    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
    	
    	String postParam = "";
    	if(signAlg == 1)
    		postParam = "reqAppID="+URLEncoder.encode(appID, "UTF-8")+
                        "&certSN="+URLEncoder.encode(certSN, "UTF-8")+
                        "&bizType="+URLEncoder.encode(bizType, "UTF-8")+
                        "&bizTime="+URLEncoder.encode(bizTime, "UTF-8")+
                        "&message="+URLEncoder.encode(message, "UTF-8")+
                        "&msgSignature="+URLEncoder.encode(msgSignature, "UTF-8")+
                        "&msgSignatureAlgorithm="+URLEncoder.encode(CommonConst.UPLOAD_LOG_SIGNALG_TYPE+"", "UTF-8");
    	else
    		postParam = "reqAppID="+URLEncoder.encode(appID, "UTF-8")+
                        "&certSN="+URLEncoder.encode(certSN, "UTF-8")+
                        "&bizType="+URLEncoder.encode(bizType, "UTF-8")+
                        "&bizTime="+URLEncoder.encode(bizTime, "UTF-8")+
                        "&message="+URLEncoder.encode(message, "UTF-8")+
                        "&msgSignature="+URLEncoder.encode(msgSignature, "UTF-8")+
                        "&msgSignatureAlgorithm="+URLEncoder.encode(CommonConst.UPLOAD_LOG_SIGNALG_TYPE_SM2+"", "UTF-8");

    	String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
    	return responseStr;
	}
	
	
	private  Boolean loginUMSPService(String act) throws Exception{    //重新登录UM Service
		   String returnStr = "";
			try {
				//showProgDlg("获取更新数据中...");
				//异步调用UMSP服务：用户登录
				String timeout = mActivity.getString(R.string.WebService_Timeout);				
				String urlPath = mActivity.getString(R.string.UMSP_Service_Login);
				String strPass = getPWDHash(accountDao.getLoginAccount().getPassword());
				
				Map<String,String> postParams = new HashMap<String,String>();
				if(act.indexOf("&") != -1)
					act = act.substring(0,act.indexOf("&"));
				
				postParams.put("accountName", act);
				postParams.put("pwdHash", strPass);    //账户口令需要HASH并转为BASE64字符串
				postParams.put("appID", accountDao.getLoginAccount().getAppIDInfo());
				
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					String postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
	                                   "&pwdHash="+URLEncoder.encode(strPass, "UTF-8")+
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
	
				if (!resultStr.equals("0")) 
					return false;
					
			} catch (Exception exc) {
				//closeProgDlg();
				return false;
			}
			
			//closeProgDlg();
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
	

   
}  