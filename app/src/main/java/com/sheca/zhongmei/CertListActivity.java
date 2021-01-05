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
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.adapter.CertAdapter;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CommonConst;

import org.spongycastle.util.encoders.Base64;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CertListActivity extends Activity {
	private List<Map<String, String>> mData = null;
	private ListView list = null;
	private CertAdapter adapter = null;
	
	private javasafeengine jse = null;
    private CertDao certDao = null;
	private AccountDao accountDao = null;
	
	private int operatorType = 0;
	private static final int INSTALL_KEYCHAIN_CODE = 1;
	private static final String DEFAULT_ALIAS = "My KeyStore";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.certlist);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("证书列表");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CertListActivity.this.finish();
			}
		});

		jse = new javasafeengine();
		certDao = new CertDao(CertListActivity.this);
		accountDao = new AccountDao(CertListActivity.this);	
		list = (ListView)findViewById(R.id.certlist);
		
		Intent intent = getIntent();
		operatorType = intent.getExtras().getInt("type");
		
		showCertList();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			CertListActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private List<Map<String, String>> getData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
	
		certList = certDao.getAllCerts(strActName);

		for (Cert cert : certList) {
			if(cert.getEnvsn().indexOf("-e")!=-1)
				continue;
			
			if(operatorType == CommonConst.CERT_OPERATOR_TYPE_IMPORT){
				if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
					continue;	
			}

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
						
				if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
		        	map.put("certtype", CommonConst.CERT_SM2_NAME); 
		        else
		        	map.put("certtype", CommonConst.CERT_RSA_NAME);
				
				map.put("savetype", cert.getSavetype()+"");
			
				list.add(map);
			}
		}

		return list;
	}

	
	private void showCertList(){
		try {
			mData = getData();
		}catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(CertListActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
		}
		
		adapter = new CertAdapter(this, mData);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int certId = Integer.valueOf(mData.get(position).get("id"));
				viewCert(certId);
			}

		});
	}
	
	
	private void viewCert(final int certId) {
		if(operatorType == CommonConst.CERT_OPERATOR_TYPE_VIEW){
			Intent intent = new Intent(CertListActivity.this, CertDetailActivity.class);
		    intent.putExtra("CertId", certId+"");
		    startActivity(intent);
		    CertListActivity.this.finish();
		}
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_CHANGEPWD){
			Intent intent = new Intent(CertListActivity.this, CertChangePwdActivity.class);
		    intent.putExtra("CertId", certId+"");
		    startActivity(intent);
		    CertListActivity.this.finish();
		}			
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_IMPORT)
			installCert(certId);
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_DELETE){
			Intent intent = new Intent(CertListActivity.this, CertDeleteActivity.class);
		    intent.putExtra("CertId", certId+"");
		    startActivity(intent);
		    CertListActivity.this.finish();
		}
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
				Toast.makeText(CertListActivity.this, "SM2证书无法操作", Toast.LENGTH_SHORT).show();
			}


		} catch (Exception e) {
			Toast.makeText(CertListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
}
