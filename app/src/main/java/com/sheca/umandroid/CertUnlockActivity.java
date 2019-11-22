package com.sheca.umandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;

import org.spongycastle.util.encoders.Base64;

import java.security.KeyPair;

public class CertUnlockActivity extends Activity {
	private CertDao certDao = null;
	private AccountDao mAccountDao = null;
	private SealInfoDao mSealInfoDao = null;
	
	private LogDao logDao = null;
	private javasafeengine jse = null;
	private int certID = 0;
	private Cert mCert = null;
	private KeyPair mKeyPair = null;  
	
	private ProgressDialog progDialog = null;
	private EditText mOriginalPasswordView;
	
	private SharedPreferences sharedPrefs;
	private String  mContainerid = "";   
	private String strENVSN = "";       
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_cert_unlock);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("解锁证书密码");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CertUnlockActivity.this.finish();
			}
		});

		certDao = new CertDao(this);
		mAccountDao= new AccountDao(this);
		logDao = new LogDao(this);
		mSealInfoDao = new SealInfoDao(this);
		jse = new javasafeengine();
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("CertId")!=null){
				certID = Integer.parseInt(extras.getString("CertId"));
				mCert = certDao.getCertByID(certID);
			}
		}
		
		findViewById(R.id.btnRename).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkRevokeCert();
	        }		
		 });
		
	    mOriginalPasswordView = (EditText) findViewById(R.id.textCertPwd);
		mOriginalPasswordView.setText("");
		mOriginalPasswordView.requestFocus();
		
		mOriginalPasswordView.setHint("设置证书别名");
		findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
		findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.GONE);
		
		showCertName();
	}
	
	private  void  showCertName(){
		String certificate = mCert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		String strBlank = "证书";
		String strCertName = "";
		
		try {
			strCertName = jse.getCertDetail(17, bCert);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			strCertName = "";
		}
		
		if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype()))
			strCertName += CommonConst.CERT_SM2_NAME+strBlank;
		else
			strCertName += CommonConst.CERT_RSA_NAME+strBlank;
		
		if(CommonConst.SAVE_CERT_TYPE_PHONE == mCert.getSavetype()){
			//strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
		}else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()){
			//strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
		}
		
		if(null == mCert.getCertname()){
			((EditText) findViewById(R.id.textCertPwd)).setText(strCertName);
		}else{
		    if(mCert.getCertname().isEmpty())
			   ((EditText) findViewById(R.id.textCertPwd)).setText(strCertName);
		    else
		       ((EditText) findViewById(R.id.textCertPwd)).setText(mCert.getCertname());
		}
		
		((EditText) findViewById(R.id.textCertPwd)).setSelectAllOnFocus(true);
		((EditText) findViewById(R.id.textCertPwd)).setSelection(((EditText) findViewById(R.id.textCertPwd)).getText().toString().length());//set cursor to the end  
		
	}
	
	private  void checkRevokeCert(){
		mOriginalPasswordView.setError(null);
		
		String originalPassword = mOriginalPasswordView.getText().toString().trim();
		
		boolean cancel = false;
		View focusView = null;

		// 检查用户输入的原密码是否有效
		if(null == originalPassword){
			mOriginalPasswordView.setError(getString(R.string.certname_rule));
			focusView = mOriginalPasswordView;
			cancel = true;	
		}
		if (TextUtils.isEmpty(originalPassword)) {
			mOriginalPasswordView.setError(getString(R.string.certname_rule));
			focusView = mOriginalPasswordView;
			cancel = true;
		} 
		if (originalPassword.length() > 16) {
			mOriginalPasswordView.setError(getString(R.string.certname_rule));
			focusView = mOriginalPasswordView;
			cancel = true;
		} 
		
		
		if (cancel) {
			// There was an error; don't attempt continue and focus the first form field with an error.
			  focusView.requestFocus();
		} else {
			  showRenameCert();
		}
		
	}
		
	private  void showRenameCert(){
		doRenameCert(certID);
	}
	
	private  void  doRenameCert(final int  certId){
		final Cert  cert = mCert;
		
		final String sCertName = mOriginalPasswordView.getText().toString().trim();
		if (sCertName != null && !"".equals(sCertName)) {
			//setCertName(cert,sCertName);
			CommUtil.resetPasswordLocked(CertUnlockActivity.this,mCert.getId());
			Toast.makeText(CertUnlockActivity.this, "解锁证书口令成功",Toast.LENGTH_SHORT).show();
		    Intent intent = new Intent(CertUnlockActivity.this, MainActivity.class);	
			startActivity(intent);

			CertUnlockActivity.this.finish();

		} else {
			Toast.makeText(CertUnlockActivity.this,"证书别名不能为空", Toast.LENGTH_SHORT).show();
		}
		
	}


	private void setCertName(Cert cert,String name){
		cert.setCertname(name);
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		certDao.updateCert(cert,strActName);
		
		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), strActName);
		if(null != sealInfo){
		   sealInfo.setSealname(name+"的印章");
		   mSealInfoDao.updateSealInfo(sealInfo, strActName);
		}
		
	}

}
