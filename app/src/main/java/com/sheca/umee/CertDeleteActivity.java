package com.sheca.umee;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sheca.javasafeengine;

import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.myWebClientUtil;

import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.SealInfoDao;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.SealInfo;
import com.sheca.umee.model.ShcaCciStd;
import com.sheca.umee.util.CommUtil;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.MyAsycnTaks;
import com.sheca.umee.util.ParamGen;
import com.sheca.umee.util.SharePreferenceUtil;
import com.sheca.umee.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

public class CertDeleteActivity extends Activity {
	private CertDao certDao = null;
	private AccountDao mAccountDao = null;
	private SealInfoDao mSealInfoDao = null;
	
	private javasafeengine jse = null;
	private int certID = 0;
	private Cert mCert = null;
	
	private ProgressDialog progDialog = null;
	private EditText mOriginalPasswordView;
	private CheckBox delBTDevice;
	
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	private  JShcaUcmStd gUcmSdk = null;
	private String strInfo="";
	private String responResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_cert_delete);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("删除证书");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CertDeleteActivity.this.finish();
			}
		});

		certDao = new CertDao(this);
		mAccountDao= new AccountDao(this);
		mSealInfoDao = new SealInfoDao(CertDeleteActivity.this);
		jse = new javasafeengine();
		
		gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
		
		delBTDevice = (CheckBox)findViewById(R.id.checkBox1); 
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("CertId")!=null){
				certID = Integer.parseInt(extras.getString("CertId"));
				mCert = certDao.getCertByID(certID);
			}
		}
		
		findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkDeleteCert();
	        }		
		 });
		
	    mOriginalPasswordView = (EditText) findViewById(R.id.textCertPwd);
		mOriginalPasswordView.setText("");
		mOriginalPasswordView.requestFocus();
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
			delBTDevice.setText("同时删除蓝牙key内证书");
		else if(CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype())
			delBTDevice.setText("同时删除蓝牙sim卡内证书");
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()){
			findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.GONE);
			
			mOriginalPasswordView.setText("1");
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
			   mOriginalPasswordView.setHint("输入蓝牙key密码");
			else if(CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype())
			   mOriginalPasswordView.setHint("输入蓝牙sim卡密码");
			
			delBTDevice.setChecked(false);
			 //给CheckBox设置事件监听 
			delBTDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
	            @Override 
	            public void onCheckedChanged(CompoundButton buttonView, 
	                    boolean isChecked) { 
	                // TODO Auto-generated method stub 
	                if(isChecked){ 
	                	findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
	                	mOriginalPasswordView.setText("");
	                }else{ 
	                	findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.GONE);
	                	mOriginalPasswordView.setText("1");
	                } 
	            } 
	        }); 
			
		}else{
			mOriginalPasswordView.setHint("输入证书密码");
			findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
		    findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
		}

		if(CommUtil.isPasswordLocked(CertDeleteActivity.this,certID)){
			findViewById(R.id.btnDelete).setVisibility(android.widget.RelativeLayout.GONE);
			return;
		}
	}
	
	
	private  void checkDeleteCert(){
		mOriginalPasswordView.setError(null);
		
		String originalPassword = mOriginalPasswordView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// 检查用户输入的原密码是否有效
		if(null == originalPassword){
			mOriginalPasswordView.setError(getString(R.string.password_rule));
			focusView = mOriginalPasswordView;
			cancel = true;	
		}
		if (TextUtils.isEmpty(originalPassword)) {
			mOriginalPasswordView.setError(getString(R.string.password_rule));
			focusView = mOriginalPasswordView;
			cancel = true;
		} 
		
		if (cancel) {
			// There was an error; don't attempt continue and focus the first form field with an error.
			focusView.requestFocus();
		} else {
              showDeleteCert(originalPassword);
		}
		
	}
		
	private  void showDeleteCert(final String pwd){
		Builder builder = new Builder(CertDeleteActivity.this);
		builder.setMessage("是否确定删除证书？");
		builder.setIcon(R.drawable.alert);
		builder.setTitle("提示");
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {	
					if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()){

						   doDeleteCertByNoBlueTooth(certID);
					}else{
						if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
							doDeleteSM2Cert(certID);
						}else {
							doDeleteCert(certID);
						}

//						int mSdkCertId = mCert.getSdkID();
//						//SDK请求删除证书
//						doRequestDelteCert(mSdkCertId,pwd);

					}
				} catch (Exception e) {
					Toast.makeText(CertDeleteActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.show();	
	}

	/**
	 * 请求SDK删除证书
	 * @param certID
	 * @param certPwd
	 */
	private void doRequestDelteCert(final int certID, final String certPwd) {
		new MyAsycnTaks(){

			@Override
			public void preTask() {
//				String hashPwd = getPWDHash(certPwd, null);
				String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
				strInfo= ParamGen.getDelteCerParams(mTokenId,certID+"",certPwd);
			}

			@Override
			public void doinBack() {
				UniTrust mUnitTrust = new UniTrust(CertDeleteActivity.this, false);
				responResult=mUnitTrust.DeleteCert(strInfo);
			}

			@Override
			public void postTask() {
				final APPResponse response = new APPResponse(responResult);
				int resultStr = response.getReturnCode();
				final String retMsg = response.getReturnMsg();

				if(0==resultStr){
					closeProgDlg();
					Toast.makeText(CertDeleteActivity.this, "证书删除成功",Toast.LENGTH_SHORT).show();
				}else{
					closeProgDlg();
					Toast.makeText(CertDeleteActivity.this, "证书删除失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
				}

			}
		}.execute();
	}


	private  void  doDeleteCert(final int  certId){
		boolean bRet = true;
		final Cert  cert = mCert;
		
		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			final String sKeyStore = cert.getKeystore();
			byte[] bKeyStore = Base64.decode(sKeyStore);
			ByteArrayInputStream kis = new ByteArrayInputStream(
					bKeyStore);
			KeyStore oStore = null;
			try {
				oStore = KeyStore.getInstance("PKCS12");
				oStore.load(kis, getPWDHash(sPwd,cert).toCharArray());

			} catch (Exception e) {
				Toast.makeText(CertDeleteActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
				//CommUtil.showErrPasswordMsg(CertDeleteActivity.this,cert.getId());
				return;
			}

			certDao.deleteCert(certId);
			SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());		
			if(null != sealInfo)
			   mSealInfoDao.deleteSeal(sealInfo.getId());

			CommUtil.resetPasswordLocked(CertDeleteActivity.this,mCert.getId());
			Toast.makeText(CertDeleteActivity.this, "删除证书成功", Toast.LENGTH_SHORT)
				.show();
			
			Intent intent = new Intent(CertDeleteActivity.this, MainActivity.class);	
			startActivity(intent);	
			CertDeleteActivity.this.finish();

		} else {
			Toast.makeText(CertDeleteActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	private  void  doDeleteSM2Cert(final int  certId){
		boolean bRet = true;
		final Cert  cert = mCert;
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			try {
				//if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
					//initShcaCciStdService();
				int retCode = -1;
				if(null != gUcmSdk)
					retCode = initShcaUCMService();

				if(retCode != 0){
					Toast.makeText(CertDeleteActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
					return;
				}
				
				int ret =  gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash(sPwd,cert));
				if(ret != 0){
					Toast.makeText(CertDeleteActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
					//CommUtil.showErrPasswordMsg(CertDeleteActivity.this,cert.getId());
					return;
				}
				
				ret = gUcmSdk.delSM2ContainerWithCID(cert.getContainerid(), getPWDHash(sPwd,cert));	
				if(ret == 0){
					certDao.deleteCert(certId);
					SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());
					if(null != sealInfo)
					   mSealInfoDao.deleteSeal(sealInfo.getId());

					Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
					if(null != encCert)
						certDao.deleteCert(encCert.getId());

					CommUtil.resetPasswordLocked(CertDeleteActivity.this,mCert.getId());
					Toast.makeText(CertDeleteActivity.this, "删除证书成功", Toast.LENGTH_SHORT).show();

					Intent intent = new Intent(CertDeleteActivity.this, MainActivity.class);
					startActivity(intent);
					CertDeleteActivity.this.finish();
			    }else if(ret == -23){
			    	Toast.makeText(CertDeleteActivity.this, "设备未初始化", Toast.LENGTH_SHORT).show();
					return;
			    }else if(ret == -1){
			    	Toast.makeText(CertDeleteActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
					//CommUtil.showErrPasswordMsg(CertDeleteActivity.this,cert.getId());
					return;
			    }else if(ret == -2){
			    	Toast.makeText(CertDeleteActivity.this, "删除证书异常", Toast.LENGTH_SHORT).show();
					return;
			    }else if(ret == -3){
			    	Toast.makeText(CertDeleteActivity.this, "参数错误", Toast.LENGTH_SHORT).show();
					return;
			    }

			} catch (Exception e) {
				ShcaCciStd.gSdk = null;
				Toast.makeText(CertDeleteActivity.this, "密码分割组件初始化失败",Toast.LENGTH_SHORT).show();
				return;
			}
		} else {
			Toast.makeText(CertDeleteActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
	}
	

	
	private  void  doDeleteCertByNoBlueTooth(final int  certId){
		final Cert  cert = mCert;
	
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
			certDao.deleteCert(certId);
		}else{
			certDao.deleteCert(certId);
			Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActName);
	        if(null != encCert)
		       certDao.deleteCert(encCert.getId());
		}
		
		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());
		if(null != sealInfo)
		   mSealInfoDao.deleteSeal(sealInfo.getId());
		
		Toast.makeText(CertDeleteActivity.this, "删除证书成功", Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(CertDeleteActivity.this, MainActivity.class);	
		startActivity(intent);	
		CertDeleteActivity.this.finish();

	}
		
	private int initShcaCciStdService(){  //初始化创元中间件
		int retcode = -1;
		
		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertDeleteActivity.this);
    	    retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);    		
    	    ShcaCciStd.errorCode = retcode;
    	    
    	    if(retcode != 0)
    	    	ShcaCciStd.gSdk = null;
		}
		
    	return retcode;
	}
	
	private int initShcaUCMService(){  //初始化CA手机盾中间件
 		int retcode = -1;
 		byte[] bRan = null;
 		
 		String myHttpBaseUrl = CertDeleteActivity.this.getString(R.string.UMSP_Base_Service);		
		myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
		
		bRan = jse.random(256, "SHA1PRNG", "SUN");
		gUcmSdk.setRandomSeed(bRan);
		//gUcmSdk.setRandomSeed(bRS);
		retcode = gUcmSdk.doInitService(myHttpBaseUrl,CommonConst.UM_APPID);
 		
     	return retcode;
	}
   
	private  String   getPWDHash(String strPWD,Cert cert){
		String strPWDHash = "";
		
		if(null == cert)
			return strPWD;
//		if(CommonConst.USE_FINGER_TYPE == cert.getFingertype()){
//			if(!"".equals(cert.getCerthash())) {
//				//return cert.getCerthash();
//				if(!"".equals(strPWD) && strPWD.length() > 0)
//					return strPWD;
//			}else
//			    return strPWD;
//		}

//		if (!"".equals(strPWD) && strPWD.length() > 0)
//			return strPWD;


		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
		if(cert.getFingertype()   == CommonConst.USE_FINGER_TYPE)
			bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要

		strPWDHash = new String(Base64.encode(bDigest));

		return strPWDHash;

//		return strPWD;
	}
    
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void changeProgDlg(String strMsg){
		if (progDialog.isShowing()) {
			progDialog.setMessage(strMsg);
		}
	}

	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}
	
	

}
