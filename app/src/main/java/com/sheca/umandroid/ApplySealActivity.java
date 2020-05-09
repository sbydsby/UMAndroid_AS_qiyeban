package com.sheca.umandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.presenter.SealController;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.WebClientUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ApplySealActivity extends Activity {
	private String   strSign = "";        //签名数据	
    private String   strCert = "";        //base64证书
	private int      mCertId = -1;        //当前选中证书ID
	private String   strUserType = "";  
	private String   strSealName = "";    //印章别名
	private String   strUserName = ""; 
	private String   strUserID = ""; 
	private String   strPicDate = ""; 
	private String   strPicType = "";
	private String   strCertPsd;
	private int localCertid;
	
	private String   strSealData = "";
	private String   strSealSn = "";
	private String   strCertSN = "";
		
	private CertDao certDao = null;
	private AccountDao mAccountDao = null;
	private SealInfoDao mSealInfoDao = null;
	private LogDao mLogDao = null;
	
	private ProgressDialog progDialog = null;
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	
	//证书下载状态
	private final int  SEAL_APPLY_LOADING = 1;
	private final int  SEAL_QUERY_LOADING = 2;
	private final int  SEAL_VERIFY_LOADING = 3;
	private final int  SEAL_APPLY_ERR = -1;

	private Cert localCert = null;

	SealController sealController = new SealController();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apply_seal);
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("UserType")!=null)
				strUserType = extras.getString("UserType");
			if(extras.getString("SealName")!=null)
				strSealName = extras.getString("SealName");
			if(extras.getString("PicData")!=null)
				strPicDate = extras.getString("PicData");
			if(extras.getString("PicType")!=null)
				strPicType = extras.getString("PicType");
			if(extras.getString("Cert")!=null)
				strCert = extras.getString("Cert");
			if(extras.getString("SignData")!=null)
				strSign = extras.getString("SignData");
			if(extras.containsKey("CertID"))
				mCertId = extras.getInt("CertID");
			if(extras.containsKey("psd"))
				strCertPsd = extras.getString("psd");
			if(extras.containsKey("localCertid"))
				localCertid = extras.getInt("localCertid");

		}
		
		mAccountDao = new AccountDao(ApplySealActivity.this);
		certDao = new CertDao(ApplySealActivity.this);
		mSealInfoDao = new SealInfoDao(ApplySealActivity.this);
		mLogDao  = new LogDao(ApplySealActivity.this);

		localCert = certDao.getCertByID(localCertid);
		
		strUserName = mAccountDao.getLoginAccount().getIdentityName();
		strUserID = mAccountDao.getLoginAccount().getIdentityCode();
	/*	
		StringBuffer sb = new StringBuffer();
		sb.append("appID:"+CommonConst.UM_APPID);
		sb.append("\n");
		sb.append("userType:"+strUserType);
		sb.append("\n");
		sb.append("userName:"+strUserName);
		sb.append("\n");
		sb.append("id:"+strUserID);
		sb.append("\n");
		sb.append("sealName:"+strSealName);
		sb.append("\n");
		sb.append("picData:"+strPicDate);
		sb.append("\n");
		sb.append("picType:"+strPicType);
		sb.append("\n");
		sb.append("cert:"+strCert);
		sb.append("\n");
		sb.append("signData:"+strSign);
		
		TextView aboutTextView = (TextView) findViewById(R.id.text_about);
		aboutTextView.setText(sb.toString());
		*/
		
		findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.GONE);
		
		Button nextBtn = (Button) findViewById(R.id.login_btn_next);
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				applyAndSaveSeal();
			}
		});
		
		ht = new HandlerThread("es_device_working_thread");
      	ht.start();
        workHandler = new Handler(ht.getLooper()); 
		applyAndSaveSeal();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private  void applyAndSaveSeal(){
		//showProgDlg("申请印章中...");
		final Handler handler = new Handler(ApplySealActivity.this.getMainLooper());

		workHandler.post(new Runnable(){
            @Override
            public void run() {
            	try {
            		handler.post(new Runnable() {
						@Override
						public void run() {
            		         showLoadingView(SEAL_APPLY_LOADING);
						}
					});
            		
            		String responseStr = applySeal();
					APPResponse response = new APPResponse(responseStr);
            		JSONObject jbRet = null;
            		if (response.getReturnCode()==0) {
						JSONObject jb =  response.getResult();
						String sealid = jb.getString("sealID");

						String account = new AccountDao(getApplicationContext()).getLoginAccount().getName();

						SealInfo sealInfo = sealController.getAccountSealInfoBySN(ApplySealActivity.this,sealid,account);

						//设置证书状态
						localCert.setSealsn(sealInfo.getSealsn());
						localCert.setSealstate(Cert.STATUS_IS_SEAL);

						//印章需要与证书做关联
						sealInfo.setCertsn(localCert.getCertsn());
						sealInfo.setCert(localCert.getCertificate());
						sealInfo.setSdkID(mCertId);
						sealInfo.setSealname(strSealName);

						SealInfoDao sealInfoDao = new SealInfoDao(getApplicationContext());
						sealInfoDao.addSeal(sealInfo,account);

						localCert.setSealsn(sealInfo.getSealsn());
						certDao.updateCert(localCert,account);

						Log.d("unitrust","sealInfoDao.addSeal 成功");

						Toast.makeText(getApplicationContext(),"印章申请成功",Toast.LENGTH_SHORT).show();

						//设置跳入到主页，更新印章
						Intent intent = new Intent(ApplySealActivity.this,MainActivity.class);
						startActivity(intent);
						finish();

					}else if(response.getReturnCode()==10012){
            			handler.post(new Runnable() {
						   @Override
					       public void run() {
            			       if(loginUMSPService(mAccountDao.getLoginAccount().getName()))
            				      applyAndSaveSeal();
						   }
					    });
            		
            		}else{
            			 final String strReturnStr = response.getReturnMsg() ;
            			 handler.post(new Runnable() {
							   @Override
								public void run() {
								    
								    Toast.makeText(ApplySealActivity.this, strReturnStr,Toast.LENGTH_SHORT).show();
			            			//closeProgDlg();
			            			showLoadingView(SEAL_APPLY_ERR);
			            			
			            			findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
			            			return;
								}
						    });
            		}
	
            	}catch(final Exception exc){
            		handler.post(new Runnable() {
						@Override
					 	public void run() {
            		        Toast.makeText(ApplySealActivity.this, exc.getMessage(),Toast.LENGTH_SHORT).show();
            		        //closeProgDlg();
            		        showLoadingView(SEAL_APPLY_ERR);
            		
            		        findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
        			        return;
						}
					});
            	}
            }
        });
		
	}
	

	private  String  applySeal() throws Exception{
		String responseStr = sealController.applySeal(this,strPicDate,""+mCertId, strCertPsd);
		return responseStr;
	}
	
	
	private  String  querySeal(String sealType,String sealSn) throws Exception{
		String timeout = ApplySealActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplySealActivity.this.getString(R.string.UMSP_Service_QuerySeal);		
		
		String postParam = "";
		if(mCertId != -1){
			final Cert cert = certDao.getCertByID(mCertId);
			postParam = "sealType="+URLEncoder.encode(sealType, "UTF-8")+
                        "&sealSn="+URLEncoder.encode(sealSn, "UTF-8")+
                        "&certSn="+URLEncoder.encode(cert.getCertsn(), "UTF-8");
			strCertSN = cert.getCertsn();
		}else{
			postParam = "sealType="+URLEncoder.encode(sealType, "UTF-8")+
                        "&sealSn="+URLEncoder.encode(sealSn, "UTF-8");
		}

		String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		return responseStr;
	}
	
	private  String  verifySeal(String sealData) throws Exception{
		String timeout = ApplySealActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ApplySealActivity.this.getString(R.string.UMSP_Service_VerifySeal);		
		
		String postParam = "sealData="+URLEncoder.encode(sealData, "UTF-8");
		String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		
		JSONObject jb = JSONObject.fromObject(responseStr);
		String resultStr = jb.getString(CommonConst.RETURN_CODE);
		String returnStr = jb.getString(CommonConst.RETURN_MSG);		
		String retVerify = "";
		
		if (resultStr.equals("0")) {
			JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
			
			retVerify = jbRet.getString(CommonConst.RESULT_PARAM_VERIFY);
			String retVid = jbRet.getString(CommonConst.RESULT_PARAM_VID);
			String sealName = jbRet.getString(CommonConst.RESULT_PARAM_SEALNAME);
			String sealSn = jbRet.getString(CommonConst.RESULT_PARAM_SEALSN);
			String issuerCert = jbRet.getString(CommonConst.RESULT_PARAM_ISSUERCERT);
			String userCert = jbRet.getString(CommonConst.RESULT_PARAM_CERT);
			String picData = jbRet.getString(CommonConst.RESULT_PARAM_PICDATE);
			String picType = jbRet.getString(CommonConst.RESULT_PARAM_PICTYPE);
			String picWidth = jbRet.getString(CommonConst.RESULT_PARAM_PICWIDTH);
			String picHeight = jbRet.getString(CommonConst.RESULT_PARAM_PICHEIGHT);
			String notBefore = jbRet.getString(CommonConst.RESULT_PARAM_NOTBEFORE);
			String notAfter = jbRet.getString(CommonConst.RESULT_PARAM_NOTAFTER);
			String signal = jbRet.getString(CommonConst.RESULT_PARAM_SIGNAL);
			
			String extensions = "";
			if(jbRet.containsKey(CommonConst.RESULT_PARAM_EXTENSIONS)){
				JSONArray transitListArray = JSONArray.fromObject(jbRet.getString(CommonConst.RESULT_PARAM_EXTENSIONS));
				List<Map<String, String>> extlist =  new ArrayList<Map<String, String>>();
				for(int i = 0;i<transitListArray.size();i++){
					Map<String, String> map = new HashMap<String, String>();
					JSONObject jbArrayRet =  transitListArray.getJSONObject(i) ;
					map.put("oid", jbArrayRet.getString(CommonConst.RESULT_PARAM_OID));
					map.put("value", jbArrayRet.getString(CommonConst.RESULT_PARAM_VALUE));
					extlist.add(map);
				}
				extensions = extlist.toString();
			}
						
		   //extensions = JSONObject.fromObject(jbRet.getString(CommonConst.RESULT_PARAM_EXTENSIONS)).toString();
			
			//changeProgDlg("保存印章中...");
			
			SealInfo sealInfo = new SealInfo();
			sealInfo.setAccountname(mAccountDao.getLoginAccount().getName());
			sealInfo.setCert(userCert);
			sealInfo.setCertsn(strCertSN);
			sealInfo.setExtensions(extensions);
			sealInfo.setIssuercert(issuerCert);
			sealInfo.setNotafter(notAfter);
			sealInfo.setNotbefore(notBefore);
			sealInfo.setPicdata(picData);
			sealInfo.setPicheight(picHeight);
			sealInfo.setPictype(picType);
			sealInfo.setPicwidth(picWidth);
			sealInfo.setSealname(sealName);
			sealInfo.setSealsn(sealSn);
			sealInfo.setSignal(signal);
			sealInfo.setVid(retVid);
			
			mSealInfoDao.addSeal(sealInfo, mAccountDao.getLoginAccount().getName());
			
			OperationLog log = new OperationLog();
			log.setType(OperationLog.LOG_TYPE_APPLYSEAL);
			log.setCertsn(strCertSN);
			log.setMessage("");
			log.setSign(strSign);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			log.setCreatetime(sdf.format(date));
			log.setInvoker(sealSn);
			log.setSignalg(1);
			log.setIsupload(0);
			log.setInvokerid(CommonConst.UM_APPID);
			mLogDao.addLog(log, mAccountDao.getLoginAccount().getName());
		}else {
			throw new Exception("调用UMSP服务之VerifySeal失败："+ returnStr);
		}	  
		
		return retVerify;
	}
	
	private Boolean loginUMSPService(String act){    //重新登录UM Service
		   closeProgDlg();
		   
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = ApplySealActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = ApplySealActivity.this.getString(R.string.UMSP_Service_Login);

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

				JSONObject jb = JSONObject.fromObject(responseStr);
				String resultStr = jb.getString(CommonConst.RETURN_CODE);
				returnStr = jb.getString(CommonConst.RETURN_MSG);

				if (!resultStr.equals("0")) {
					throw new Exception(returnStr);
				}
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

	
	private  void showLoadingView(int state){
		GifImageView gifImageView =(GifImageView) findViewById(R.id.face_loading);		
		GifImageView gifImageView2 = (GifImageView) findViewById(R.id.face_err);  
		
		try {
			GifDrawable gifDrawable = null;

			if(state == SEAL_APPLY_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.apply_seal_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("印章制作中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
				this.findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.GONE);
			}else if(state == SEAL_QUERY_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.apply_seal_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("印章查询中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
				this.findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.GONE);
			}else if(state == SEAL_VERIFY_LOADING){
				gifDrawable = new GifDrawable(getResources(), R.drawable.apply_seal_loading);
				gifImageView.setImageDrawable(gifDrawable);
				gifImageView.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result)).setText("印章验证中 ...");
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.GONE);
				this.findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.GONE);
			}else if(state == SEAL_APPLY_ERR){
				gifImageView.setVisibility(RelativeLayout.GONE);
				gifImageView2.setVisibility(RelativeLayout.VISIBLE);
				gifImageView2.setImageResource(R.drawable.face_error);  
				this.findViewById(R.id.auth_result).setVisibility(RelativeLayout.GONE);
				((TextView)this.findViewById(R.id.auth_result_description)).setText("印章制作失败");
				this.findViewById(R.id.auth_result_description).setVisibility(RelativeLayout.VISIBLE);
				this.findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
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
	

}
