package com.sheca.zhongmei;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.security.KeyChain;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.adapter.SealInfoAdapter;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.SealInfoDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.SealInfo;
import com.sheca.zhongmei.util.CommonConst;

import org.spongycastle.util.encoders.Base64;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SealListActivity extends Activity {
	public static  String   strResult = "";      //返回结果
	public static  String   strServiecNo = "";   //业务流水号
	public static  String   strAppName = "";     //第三方APP的名称
	public static  String   strMsgWrapper = "";  //待签名数据的包装器
	public static  String   strCertSN = "";      //证书序列号
	public static  String   strAPPID = "";       //APP应用唯一标识
	
	private List<Map<String, String>> mData = null;
	private ListView list = null;
	private SealInfoAdapter adapter = null;
	
	private javasafeengine jse = null;
    private CertDao certDao = null;
	private AccountDao accountDao = null;
	private SealInfoDao sealDao = null;
	
	private  int operatorType = 0;
	private  final int LOG_TYPE_LOGIN = 1;
	private  final int LOG_TYPE_SIGN = 2;
	private  final int LOG_TYPE_SIGNEX = 3;
	private  final int LOG_TYPE_ENVELOP_DECRYPT = 4;
	private  final int LOG_TYPE_SEAL = 5;
	
	public   static  int  operateState = 0;   //操作状态 
	private  static final int INSTALL_KEYCHAIN_CODE = 1;
	private  static final String DEFAULT_ALIAS = "My KeyStore";
	
	private  boolean  bScanDao = false;       //是否通过扫码登录进入
	private  boolean  isJSONDate = false;     //是否第三方扫码签名数据json格式
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.seallist);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("印章列表");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);
		cancelScanButton.setVisibility(RelativeLayout.GONE);
		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SealListActivity.this.finish();
			}
		});

		jse = new javasafeengine();
		certDao = new CertDao(SealListActivity.this);
		accountDao = new AccountDao(SealListActivity.this);	
		sealDao = new SealInfoDao(SealListActivity.this);
		list = (ListView)findViewById(R.id.seallist);
		
		Intent intent = getIntent();
		if(null != intent.getExtras().getString("type"))
		    operatorType = intent.getExtras().getInt("type");
		if(null != intent.getExtras().getString("ScanDao"))
			bScanDao = true;  
		if(null != intent.getExtras().getString("IsJson"))
		    isJSONDate = true; 

	    operateState = Integer.parseInt(intent.getExtras().getString("OperateState"));
		strResult = intent.getExtras().getString("OriginInfo");		
		strServiecNo = intent.getExtras().getString("ServiecNo");	
		strAppName = intent.getExtras().getString("AppName");	
		strAPPID = CommonConst.UM_APPID;
		
		if(strAppName.equals(CommonConst.CREDIT_APP_NAME)){   //判断是否信用APP调用
			strAPPID = CommonConst.CREDIT_APP_ID;
		}else{
			if(strAppName.equals(CommonConst.UTEST_APP_NAME) )     //判断是否UTest调用,兼容分旧版本UTest
			   strAPPID = CommonConst.UTEST_APP_ID;
			else if(strAppName.equals(CommonConst.NETHELPER_APP_NAME))   //判断是否上网助手调用,兼容分旧版本上网助手
			   strAPPID = CommonConst.NETHELPER_APP_ID;
			else if(strAppName.equals(CommonConst.SCAN_LOGIN_NAME))  //判断是否扫码登录
			   strAPPID = CommonConst.UM_APPID;
			else if(strAppName.equals(CommonConst.SCAN_SIGN_NAME))  //判断是否扫码签名
			   strAPPID = CommonConst.UM_APPID;
			else if(strAppName.equals(CommonConst.SCAN_SIGNEX_NAME))   //判断是否扫码批量签名
			   strAPPID = CommonConst.UM_APPID;
		    else if(strAppName.equals(CommonConst.SCAN_ENVELOP_DECRYPT_NAME))   //判断是否扫码解密
			   strAPPID = CommonConst.UM_APPID;
		    else if(strAppName.equals(CommonConst.SCAN_SEAL_NAME))   //判断是否扫码签章
			   strAPPID = CommonConst.UM_APPID;
			else
			   strAPPID = strAppName; 
		}
		
		if(operateState == LOG_TYPE_SIGN){
		    if(null != intent.getExtras().getString("CertSN"))
		        strCertSN = intent.getExtras().getString("CertSN");	
			   
		    strMsgWrapper = intent.getExtras().getString("MsgWrapper");	
		    if(null == strMsgWrapper)
		    	strMsgWrapper = "0";
		    if("".equals(strMsgWrapper))
		        strMsgWrapper = "0";
		}else if(operateState == LOG_TYPE_SIGNEX){
			strMsgWrapper = intent.getExtras().getString("MsgWrapper");	
			if(null == strMsgWrapper)
			    strMsgWrapper = "0";
			if("".equals(strMsgWrapper))
			    strMsgWrapper = "0";
		}else if(operateState == LOG_TYPE_ENVELOP_DECRYPT){
			if(null != intent.getExtras().getString("CertSN"))
			    strCertSN = intent.getExtras().getString("CertSN");	
		}else if(operateState == LOG_TYPE_SEAL){
			strMsgWrapper = intent.getExtras().getString("MsgWrapper");	
		    if(null == strMsgWrapper)
		    	strMsgWrapper = "0";
		    if("".equals(strMsgWrapper))
		        strMsgWrapper = "0";
		    if(null != intent.getExtras().getString("CertSN"))
			    strCertSN = intent.getExtras().getString("CertSN");	
		}

		if(!"".equals(strCertSN))
		   showSealInfo();
		else
		   showSealList();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			   Intent resultIntent = new Intent();
			   SealListActivity.this.setResult(RESULT_CANCELED, resultIntent);
			   SealListActivity.this.finish();			
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private List<Map<String, String>> getData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<SealInfo> sealList = new ArrayList<SealInfo>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
	
		sealList = sealDao.getAllSealInfos(strActName);

		for (SealInfo sealInfo : sealList) {
			Cert cert = certDao.getCertByCertsn(sealInfo.getCertsn(), strActName);
			if(null == cert)
				continue;
			if(cert.getEnvsn().indexOf("-e")!=-1)
				continue;

			if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", String.valueOf(sealInfo.getId()));

				String strNotBeforeTime = sealInfo.getNotbefore();
				String strValidTime = sealInfo.getNotafter();
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				map.put("sealname", sealInfo.getSealname());
				map.put("validtime",sdf2.format(fromDate) + " ~ " + sdf2.format(toDate));
				map.put("picdata", sealInfo.getPicdata());
			
				list.add(map);
			}
		}

		return list;
	}

	private void showSealInfo(){
		SealInfo sealInfo = sealDao.getSealByCertsn(strCertSN.toLowerCase(), accountDao.getLoginAccount().getName());
        if(null != sealInfo)
	       doSealSign(sealInfo,strCertSN);
        else
           noSealSign(strCertSN);
	}
	
	private void showSealList(){
		try {
			mData = getData();
		}catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(SealListActivity.this, "获取印章列表错误！", Toast.LENGTH_SHORT).show();
		}
		
		adapter = new SealInfoAdapter(this, mData);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int sealId = Integer.valueOf(mData.get(position).get("id"));
				viewSeal(sealId);
			}

		});
	}

	private void viewSeal(final int sealId) {
		SealInfo sealInfo = sealDao.getSealByID(sealId);
		String   certSN = sealInfo.getCertsn();
		
	    doSealSign(sealInfo,certSN);
	}
	
	private  void  doSealSign(SealInfo sealInfo,String   certSN){
		   Intent resultIntent = new Intent();
		   Bundle bundle = new Bundle();
		   bundle.putString("ServiecNo", strServiecNo);
		   bundle.putString("OriginInfo", strResult);
		   bundle.putString("CertSN", certSN);
		   bundle.putString("AppID", strAPPID);
		   bundle.putString("SealSN", sealInfo.getSealsn());
		   bundle.putString("MsgWrapper", strMsgWrapper);
		   resultIntent.putExtras(bundle);
		
		   SealListActivity.this.setResult(RESULT_OK, resultIntent);
		   SealListActivity.this.finish();			
	}
	
	private  void  noSealSign(String   certSN){
		   Intent resultIntent = new Intent();
		   Bundle bundle = new Bundle();
		   bundle.putString("ServiecNo", strServiecNo);
		   bundle.putString("OriginInfo", strResult);
		   bundle.putString("CertSN", certSN);
		   bundle.putString("AppID", strAPPID);
		   bundle.putString("SealSN", "");
		   bundle.putString("MsgWrapper", strMsgWrapper);
		   resultIntent.putExtras(bundle);
		
		   SealListActivity.this.setResult(RESULT_CANCELED, resultIntent);
		   SealListActivity.this.finish();			
	}

	public void installCert(final int certId) {
		try {
			Cert cert = certDao.getCertByID(certId);
			
			if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){		
			    String sKeyStore = cert.getKeystore();
			    byte[] bKeyStore = Base64.decode(sKeyStore);

			    Intent installIntent = KeyChain.createInstallIntent();
			    installIntent.putExtra(KeyChain.EXTRA_PKCS12, bKeyStore);
		 	    installIntent.putExtra(KeyChain.EXTRA_NAME, DEFAULT_ALIAS);
			    startActivityForResult(installIntent, INSTALL_KEYCHAIN_CODE);
			}else{
				Toast.makeText(SealListActivity.this, "SM2证书无法操作", Toast.LENGTH_SHORT).show();
			}


		} catch (Exception e) {
			Toast.makeText(SealListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
}
