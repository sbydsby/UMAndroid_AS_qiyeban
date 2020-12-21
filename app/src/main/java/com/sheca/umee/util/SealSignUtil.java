package com.sheca.umee.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.sheca.javasafeengine;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.umee.R;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.model.Cert;

import org.spongycastle.util.encoders.Base64;

import java.security.cert.Certificate;

public class SealSignUtil {
	public static  String  strBizSN = "";
	public static  String  strOrgDate = "";
	public static  String  strSignDate = "";
	public static  String  strCert = "";
	public static  String  strCertSN = "";
	public static  String  strUniID = "";
	public static  String  strCertType = "";
	public static  String  strSaveType= "";
	public static  String  strAppID= "";
	public static  String  strAccountName= "";
	public static  String  strCertPwd= "";
	public static  String  strMsgWrapper= "";
	public static  String  encCert= "";
	
	public  static  Context  context = null;
	public  static  Activity activity = null;
	private static  CertDao  certDao = null;
	
	private static JShcaUcmStd gUcmSdk = null;
	
	private static SharedPreferences sharedPrefs;

	public static void sealPdfSign() {
		certDao = new CertDao(context);
		sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);		
		String strPdfSign = "";
		
		final Cert cert = certDao.getCertByCertsn(strCertSN, strAccountName);
		strCert = cert.getCertificate();
		strCertType = cert.getCerttype();
		strSaveType = cert.getSavetype()+"";
		strCertPwd = sharedPrefs.getString(CommonConst.SETTINGS_CERT_PWD, "");		
		strMsgWrapper = sharedPrefs.getString(CommonConst.SETTINGS_MSG_WRAPPER, "");	
		
		if(null != activity)
		   gUcmSdk = JShcaUcmStd.getIntence(activity.getApplication(), context);
		
		if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())){
			//new Thread() {
			   //@Override
			  // public void run() {
				  String pdfsign = "";
				  pdfsign = getSM2Sign(cert);
			   //}
			//}.start();
		}else{
			//new Thread() {
			//  @Override
			 // public void run() {
				  String pdfsign = "";
				  pdfsign = getRSASign(cert);
			  //}
			//}.start();
		}

	}

	public static   String getRSASign(final Cert cert) {
		String message = strOrgDate;
		try {	
		   if("1".equals(strMsgWrapper)){
			  strSignDate = PKIUtil.sign(Base64.decode(message),cert.getPrivatekey(), strCertPwd,context);
		   }else if("0".equals(strMsgWrapper)){				
			  strSignDate = PKIUtil.sign(Base64.decode(message),cert.getPrivatekey(), strCertPwd,context);
           }else{
        	 strSignDate = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE),cert.getPrivatekey(), strCertPwd,context);
           }	
		   
		   boolean flag = false;		   
		   flag = verifyRSASign(strOrgDate,strSignDate,cert.getCertificate(),CommonConst.USE_CERT_ALG_RSA,"1");
		} catch (Exception e) {
			   
			   strSignDate = "";
		}	
		
		return strSignDate;
	}

	public static  String getSM2Sign(final Cert cert)  {
		String message = strOrgDate;
		byte[] signDate = null;
				
		try {		
			int retCode = -1;
			if(null != gUcmSdk)
			   retCode = initShcaUCMService();

			if(retCode != 0){
				strSignDate = "";
				return strSignDate;
			}
			
			int ret =  gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), strCertPwd);
			if(ret != 0){
				strSignDate = "";
				return strSignDate;
			}
			
			JShcaUcmStdRes jres = new JShcaUcmStdRes();

			if("1".equals(strMsgWrapper)){		
			   jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), strCertPwd, Base64.decode(message),CommonConst.SERT_TYPE  );
			}else if("0".equals(strMsgWrapper)){	
			   jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), strCertPwd, Base64.decode(message),CommonConst.SERT_TYPE   );
			}else{
			   jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), strCertPwd, message.getBytes(CommonConst.SIGN_STR_CODE),CommonConst.SERT_TYPE   );
			}
								
			strSignDate = jres.response; 
									
			boolean flag = false;		   
			flag = verifySM2Sign(strOrgDate,strSignDate,cert.getCertificate(),"1");
		} catch (Exception e) {
		   strSignDate = "";
		}	
	
		return strSignDate;
	}
	
	private static int initShcaUCMService(){  //初始化创元中间件
 		int retcode = -1;
 		byte[] bRan = null;
 		javasafeengine jse = null;
 		jse = new javasafeengine();		
 		
 		String myHttpBaseUrl = context.getString(R.string.UMSP_Base_Service);		
		myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
		
		bRan = jse.random(256, "SHA1PRNG", "SUN");
		gUcmSdk.setRandomSeed(bRan);
		//gUcmSdk.setRandomSeed(bRS);
		retcode = gUcmSdk.doInitService(myHttpBaseUrl,CommonConst.UM_APPID);
 		
     	return retcode;
    } 

	public static  boolean verifyRSASign(String data, String sign,String certificate,String strSignatureAlgorithm,String msgWrapper) throws Exception {
		javasafeengine jse = new javasafeengine();

		Certificate cert = null;
		cert = jse.getCertFromBuffer(certificate.getBytes());
		if (cert == null) {
			cert = jse.getCertFromBuffer(Base64.decode(certificate));
		}
		
		boolean result = false;
		if("0".equals(msgWrapper))
		    result = jse.verifySign(data.getBytes(CommonConst.SIGN_STR_CODE), Base64.decode(sign),
				                    strSignatureAlgorithm, cert, "SunRsaSign");
		else
			result = jse.verifySign(Base64.decode(data), Base64.decode(sign),
                                    strSignatureAlgorithm, cert, "SunRsaSign");
		
		return result;
    }
  
	public static  boolean verifySM2Sign(String data, String sign,String certificate,String msgWrapper) throws Exception {
		javasafeengine jse = new javasafeengine();
	
		int result = -1;
		if("0".equals(msgWrapper))
		    result = jse.verifySM2Signature(certificate, data.getBytes(CommonConst.SIGN_STR_CODE),Base64.decode(sign));
		else
			result = jse.verifySM2Signature(certificate, Base64.decode(data),Base64.decode(sign));
		
		if(0 == result)
			return true;
			
		return false;
    }
}
