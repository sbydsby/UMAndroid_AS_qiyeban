package com.sheca.umandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.SealInfo;

public class SaveSealActivity extends Activity {
	private int      mCertId = -1;        //当前选中证书ID
	private String   strSealSn = "";
	
	private CertDao certDao = null;
	private AccountDao mAccountDao = null;
	private SealInfoDao mSealInfoDao = null;
	
	private ProgressDialog progDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_save_seal);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("保存印章");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);
		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SaveSealActivity.this.finish();
			}
		});
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("SealSn")!=null)
				strSealSn = extras.getString("SealSn");
			if(extras.containsKey("CertID"))
				mCertId = extras.getInt("CertID");
		}
		
		mAccountDao = new AccountDao(SaveSealActivity.this);
		certDao = new CertDao(SaveSealActivity.this);
		mSealInfoDao = new SealInfoDao(SaveSealActivity.this);

		ImageView okBtn  = ((ImageView)findViewById(R.id.save_seal_ok));
 		okBtn.setOnClickListener(new OnClickListener() {
 			  @Override
 			  public void onClick(View v) {
 				 Intent intent = new Intent(SaveSealActivity.this, MainActivity.class);
 		         startActivity(intent);
 		         SaveSealActivity.this.finish();
 			  }		
 	    });
 		okBtn.setVisibility(RelativeLayout.GONE);
 		
 		saveSeal();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SaveSealActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void saveSeal(){
		 final Handler handler = new Handler(SaveSealActivity.this.getMainLooper());
		 showProgDlg("保存印章中...");
		 
		 handler.post(new Runnable(){
	        @Override
	        public void run() {
		        String actName = mAccountDao.getLoginAccount().getName();
		        Cert cert = certDao.getCertByID(mCertId);
		        SealInfo sealInfo =  mSealInfoDao.getSealBySealsn(strSealSn, actName);
		 
		        cert.setSealsn(strSealSn);
		        cert.setSealstate(Cert.STATUS_IS_SEAL);
		        certDao.updateCert(cert, actName);
		 
		        sealInfo.setCertsn(cert.getCertsn());
		        mSealInfoDao.updateSealInfo(sealInfo, actName);
		        
		        Toast.makeText(SaveSealActivity.this, "保存印章成功",Toast.LENGTH_SHORT).show();
    			closeProgDlg();
    			
    			findViewById(R.id.save_seal_ok).setVisibility(RelativeLayout.VISIBLE);
  	        }
		 });
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
	}
}
