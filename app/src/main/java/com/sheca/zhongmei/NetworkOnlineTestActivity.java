package com.sheca.zhongmei;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.PKIUtil;
import com.sheca.zhongmei.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class NetworkOnlineTestActivity extends Activity {
	private WebView mWebView = null;  
	private ProgressDialog progDialog = null;
	private WakeLock wakeLock = null;
	
	private final String NETWORK_ONLINE = "http://wx.sheca.com/admin/home/siteSearch";
	private final String NETWORK_ONLINE_TEST = "http://192.168.2.211:8080/UM/H5Entry.jsp";   //内网地址
	//"http://192.168.14.49/UM/H5Entry.jsp";
	//"http://202.96.220.165:8080/UM/H5Entry.jsp"   //外网地址
	//"http://weixin.sheca.com/Home~Consult~dotSearch?menuIn=1";
	
	private javasafeengine jse = null;
	private  AccountDao accountDao = null;
	private CertDao certDao = null;
	private List<Map<String, String>> mData = null;
	
	private String strCallbackURL = "";

	private static final int CERT_LOGIN_TEST_CODE = 1;
	private final static int LOGIN_SIGN = 0;
	private final static int LOGIN_SIGN_FAILURE = 1;
	private final static int SIGN_SUCCESS = 2;
	private final static int LOGIN_UPLOAD_FAILURE = 3;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_network_online_test);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("洛安测试");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);
		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NetworkOnlineTestActivity.this.finish();
			}
		});
		
		jse = new javasafeengine();
		certDao = new CertDao(NetworkOnlineTestActivity.this);
		accountDao = new AccountDao(NetworkOnlineTestActivity.this);
		
		mWebView = (WebView) findViewById(R.id.webView); 
		mWebView.getSettings().setJavaScriptEnabled(true);
		
	    if(mWebView != null) { 
	    	mWebView.setWebViewClient(new WebViewClient() { 
	                @Override 
	                public void onPageFinished(WebView view,String url) 
	                { 
	                	closeProgDlg(); 
	                } 
	         }); 
	             
	    	//loadNetworkOnline();
	    } 
	    
	    //mWebView.loadUrl("file:///android_asset/test.html");//加载本地asset下面的js_java_interaction.html文件
        //mWebView.loadUrl("https://www.baidu.com/");//加载本地assets下面的js_java_interaction.html文件
	    mWebView.loadUrl(NETWORK_ONLINE_TEST);  //测试demo地址

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);//打开js支持
        /**
         * 打开js接口給H5调用，参数1为本地类名，参数2为别名；h5用window.别名.类名里的方法名才能调用方法里面的内容，例如：window.android.back();
         * */
        mWebView.addJavascriptInterface(new JsInteration(), "android");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			NetworkOnlineTestActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private  void  loadNetworkOnline(){	
		 if(null != mWebView){
			showProgDlg("正在加载数据中...");
		    mWebView.loadUrl(NETWORK_ONLINE);
		    mWebView.reload();
		 }  	
	}
	
	
	private  void  testCertLogin(String callbackURL,String bizSN){
		if(accountDao.count() == 0){
			Intent intent = new Intent(NetworkOnlineTestActivity.this, LoginActivity.class);	
			startActivity(intent);	
			NetworkOnlineTestActivity.this.finish();
		}else{
			  if(accountDao.getLoginAccount().getActive() == 0){
					Intent intent = new Intent(NetworkOnlineTestActivity.this, PasswordActivity.class);
				    intent.putExtra("Account", accountDao.getLoginAccount().getName());
				    startActivity(intent);
				    NetworkOnlineTestActivity.this.finish();
			 }
			  else{
				    try {
						mData = getData("");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
			   		if(mData.size() == 0){   //进行人脸识并下载证书
			   			//if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			   			   Toast.makeText(NetworkOnlineTestActivity.this, "无证书,请先下载证书", Toast.LENGTH_SHORT).show();
			   			//else
			   			 //  showFaceReg();   //进行人脸识别
			   		}
			   		else{
			   			strCallbackURL = callbackURL;
			   			showLoginActivity(bizSN);
			   		}
			  }
		}
			
	}
	
	private void showLoginActivity(String bizSN){
		try {
			Intent intent = new Intent(NetworkOnlineTestActivity.this, DaoActivity.class);	
			
			Bundle extras = new Bundle();
			extras.putString("OriginInfo", bizSN);
			extras.putString("ServiecNo", bizSN);
			extras.putString("AppName", CommonConst.UM_APPID);
			extras.putString("OperateState", "1");
			
			intent.putExtras(extras);
			startActivityForResult(intent,CERT_LOGIN_TEST_CODE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(NetworkOnlineTestActivity.this,"请先安装UniTrust Mobile应用", Toast.LENGTH_SHORT).show();
		}
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CERT_LOGIN_TEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
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
			if (resultCode == Activity.RESULT_CANCELED) {
				
			}
			
		}
		
	}
	
	private  void   doScan(final String token,final String orgDate,final String signDate,final String cert,final String certSN,final String uniID,final String certType,final String saveType,final String appID){
		wakeLock = ((PowerManager) NetworkOnlineTestActivity.this
				.getSystemService(NetworkOnlineTestActivity.this.POWER_SERVICE))
				.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ON_AFTER_RELEASE,
						"Login");
		wakeLock.acquire();

		progDialog = new ProgressDialog(NetworkOnlineTestActivity.this);
		progDialog.setMessage("正在登录...");
		progDialog.setCancelable(false);
		progDialog.show();
		
		new Thread() {
			@Override
			public void run() {	
				if (signDate != null) {
				  String postHttpParams = "";
				  JSONObject jo = null;
					
				  try{
						postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
					                      "&appID="+URLEncoder.encode(appID, "UTF-8")+
					                      "&idNumber="+URLEncoder.encode(uniID, "UTF-8")+
					                      "&randomNumber="+URLEncoder.encode(orgDate, "UTF-8")+
					                      "&message="+URLEncoder.encode(orgDate, "UTF-8")+
					                      "&cert="+URLEncoder.encode(cert, "UTF-8")+
					                      "&result="+URLEncoder.encode(signDate, "UTF-8")+
					                      "&signatureValue="+URLEncoder.encode(signDate, "UTF-8");
						
								   
						if(CommonConst.CERT_TYPE_SM2.equals(certType)){
						         postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
		        		                           "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						}else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
						            postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
								    postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
						      
						         postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						}else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
							    
							     postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						}else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
							     postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
      		                                       "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						}						  
						
				  }catch(Exception e){
						Log.e(CommonConst.TAG, e.getMessage(),e);
						// Toast.makeText(UniTrustMobileActivity.this,
						
				  }

				  try {
						final String urlPath = strCallbackURL;
						String strPostUrlPath = urlPath;
						String sResp = null;
						//sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
						
						sResp = WebClientUtil.postHttpClientJson(strPostUrlPath,postHttpParams, 5000);
					
				  } catch (Exception e) {
						Log.e(CommonConst.TAG, e.getMessage(),
								e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						
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

	private List<Map<String, String>> getData(String certsn) throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		if (certsn != null && !"".equals(certsn)) {
			certList.add(certDao.getCertByCertsn(certsn,strActName));
		} else {
			certList = certDao.getAllCerts(strActName);
		}

		for (Cert cert : certList) {
			 if(null == cert.getCertificate() ||"".equals(cert.getCertificate()))
					continue;
			 
			if(verifyCert(cert,false)){
	    		 if(verifyDevice(cert,false)){
	    			 if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
	    				 Map<String, String> map = new HashMap<String, String>();
				         map.put("id", String.valueOf(cert.getId()));

				         byte[] bCert = Base64.decode(cert.getCertificate());
				         String commonName = jse.getCertDetail(17, bCert);
				         String organization = jse.getCertDetail(14, bCert);

				         String strNotBeforeTime = jse.getCertDetail(11, bCert);
				         String strValidTime = jse.getCertDetail(12, bCert);
				         Date fromDate = sdf.parse(strNotBeforeTime);
				         Date toDate = sdf.parse(strValidTime);

				         map.put("organization", organization);
				         map.put("commonname", commonName);
				         map.put("validtime",
						 sdf2.format(fromDate) + " ~ " + sdf2.format(toDate));
				         list.add(map);
	    			 }
	    		 }
			}
		}

		return list;
	}
	
	private boolean verifyCert(final Cert cert,boolean bShow){	
		if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
			int i = -1;
			if(CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))		
				 i = PKIUtil.verifyCertificate(cert.getCertificate(),CommonConst.RSA_CERT_CHAIN);
		    else
		    	 i = PKIUtil.verifyCertificate(cert.getCertificate(),
				                          cert.getCertchain());
			if(i == 1){
				return true;
			}else if(i == 0){
				if(bShow)
					Toast.makeText(NetworkOnlineTestActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			}else{
				if(bShow)
					Toast.makeText(NetworkOnlineTestActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
			}
		}else if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())){
			String strSignCert = "";
			int i = -1;		
			
			if(cert.getEnvsn().indexOf("-e")!=-1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
				return false;
			
			if(!"".equals(cert.getContainerid())){
			   try {
			        javasafeengine jse = new javasafeengine();
			        strSignCert = cert.getCertificate();		
			        if(CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
						 i = jse.verifySM2Cert(cert.getCertificate(),CommonConst.SM2_CERT_CHAIN);
					else
			             i = jse.verifySM2Cert(strSignCert,cert.getCertchain());
			   } catch (Exception e) {
					   e.printStackTrace();
			   }
			   
			   if(i == 0){
				     return true;
			   }else if(i == 1){
					if(bShow)
					   Toast.makeText(NetworkOnlineTestActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			   }else{
					if(bShow)
				       Toast.makeText(NetworkOnlineTestActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
			   } 
			}
		}else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(cert.getCerttype())){
			int i = -1;
			if(CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))		
				 i = PKIUtil.verifyCertificate(cert.getCertificate(),CommonConst.RSA_CERT_CHAIN);
		    else
		    	 i = PKIUtil.verifyCertificate(cert.getCertificate(),
				                          cert.getCertchain());
			if(i == 1){
				return true;
			}else if(i == 0){
				if(bShow)
					Toast.makeText(NetworkOnlineTestActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			}else{
				if(bShow)
					Toast.makeText(NetworkOnlineTestActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
			}
		}else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())){
			String strSignCert = "";
			int i = -1;		
			
			if(cert.getEnvsn().indexOf("-e")!=-1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
				return false;
			
			if(!"".equals(cert.getContainerid())){
			   try {
			        javasafeengine jse = new javasafeengine();
			        strSignCert = cert.getCertificate();		
			        if(CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
						 i = jse.verifySM2Cert(cert.getCertificate(),CommonConst.SM2_CERT_CHAIN);
					else
			             i = jse.verifySM2Cert(strSignCert,cert.getCertchain());
			   } catch (Exception e) {
					   e.printStackTrace();
			   }
			   
			   if(i == 0){
				     return true;
			   }else if(i == 1){
					if(bShow)
					   Toast.makeText(NetworkOnlineTestActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			   }else{
					if(bShow)
				       Toast.makeText(NetworkOnlineTestActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
			   } 
			}
		}
		
		return false;
	}
	
	
	private boolean verifyDevice(final Cert cert,boolean bShow){
		/*String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		String sDeciceID = jse.getCertExtInfo("1.2.156.112570.12.102",oX509Cert);
		
		//获取设备唯一标识符
		String deviceID = android.os.Build.SERIAL;
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
			deviceID = cert.getDevicesn();
		if(sDeciceID.equals(deviceID))
			return true;
		
		if(bShow)
		   Toast.makeText(NetworkOnlineTestActivity.this, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
		*/
		return true;
	}
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		//progDialog.setCancelable(false);
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
	
	
	/**
     * 自己写一个类，里面是提供给H5访问的方法
     * */
    public class JsInteration {

        @JavascriptInterface//一定要写，不然H5调不到这个方法
        public String back(String callbackURL,String bizSN) {
            return "我是java里的方法返回值\ncallbackURL:"+callbackURL+",bisSN:"+bizSN;
        }
    }
    
    

    //点击按钮，访问H5里带返回值的方法
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClick(View v) {
        Log.e("TAG", "onClick: ");

        mWebView.loadUrl("JavaScript:show()");//直接访问H5里不带返回值的方法，show()为H5里的方法


        //传固定字符串可以直接用单引号括起来
        mWebView.loadUrl("javascript:alertMessage('哈哈')");//访问H5里带参数的方法，alertMessage(message)为H5里的方法

        //当出入变量名时，需要用转义符隔开
        String content="9880";
        mWebView.loadUrl("javascript:alertMessage(\""   +content+   "\")"   );


        //Android调用有返回值js方法，安卓4.4以上才能用这个方法
        mWebView.evaluateJavascript("sum(1,2)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //Log.d(TAG, "js返回的结果为=" + value);
                Toast.makeText(NetworkOnlineTestActivity.this,"js返回的结果为=" + value,Toast.LENGTH_LONG).show();
            }
        });
    }


}
