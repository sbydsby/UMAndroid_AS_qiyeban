package com.sheca.zhongmei;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.LogDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.OperationLog;
import com.sheca.zhongmei.service.UploadLogService;
import com.sheca.zhongmei.util.CommonConst;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogActivity extends Activity {

	private javasafeengine jse = null;
	private CertDao certDao = null;
	private LogDao logDao = null;
	private AccountDao accountDao = null;
	
	private boolean isNotification = false;
	private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_log);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		((TextView) findViewById(R.id.header_text)).setText("记录明细");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		jse = new javasafeengine();
		certDao = new CertDao(LogActivity.this);
		Intent intent = getIntent();

		int logid = Integer.valueOf(intent.getStringExtra("logid"));

		logDao = new LogDao(LogActivity.this);
		OperationLog log = logDao.getLogByID(logid);
		
		accountDao = new AccountDao(LogActivity.this);
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);		
		isNotification = sharedPrefs.getBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, true); 
		
		TextView textType = (TextView) findViewById(R.id.tvtype);
		TextView textCreatetime = (TextView) findViewById(R.id.tvcreatetime);
		TextView textCertsn = (TextView) findViewById(R.id.tvcertsn);
		TextView textSign = (TextView) findViewById(R.id.tvsign);
		RelativeLayout rl_log_notes = (RelativeLayout) findViewById(R.id.rl_log_notes);
		TextView textDescription = (TextView) findViewById(R.id.tvdescription);
		RelativeLayout rl_log_signature = (RelativeLayout) findViewById(R.id.rl_log_signature);

		String typeStr = "";
		int type = log.getType();
		if (type == OperationLog.LOG_TYPE_APPLYCERT) {
			typeStr = "申请证书";
		}else if ((type == OperationLog.LOG_TYPE_LOGIN)) {
			typeStr = "扫码登录";
		}else if ((type == OperationLog.LOG_TYPE_SIGN)) {
			typeStr = "扫码签名";
		}else if ((type == OperationLog.LOG_TYPE_DAO_SIGN)) {
			typeStr = "签名";
		}else if ((type == OperationLog.LOG_TYPE_DAO_LOGIN)) {
			typeStr = "登录";
		}else if ((type == OperationLog.LOG_TYPE_DAO_LOGIN_INTERNET)) {
			typeStr = "登录上网";
		}else if(type == OperationLog.LOG_TYPE_INPUTCERT){
			typeStr = "导入证书";
		}else if(type == OperationLog.LOG_TYPE_RENEWCERT){
			typeStr = "更新证书";
		}else if ((type == OperationLog.LOG_TYPE_REVOKECERT)) {
			typeStr = "撤销证书";
		}else if ((type == OperationLog.LOG_TYPE_DAO_SIGNEX)) {
			typeStr = "扫码批量签名";
		}else if ((type == OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT)) {
			typeStr = "扫码解密";
		}else if ((type == OperationLog.LOG_TYPE_APPLYSEAL)) {
			typeStr = "申请印章";
		}

		final String certsn = log.getCertsn();
		// Cert cert = certDao.getCertByCertsn(certsn);
		// String certificate = cert.getCertificate();
		// byte[] bCert = Base64.decode(certificate);
		// Certificate oCert = jse.getCertFromBuffer(bCert);
		// X509Certificate oX509Cert = (X509Certificate) oCert;

		textType.setText(typeStr);
		textCreatetime.setText(log.getCreatetime());
		textCertsn.setText(certsn);

		if ("".equals(log.getMessage())) {
			rl_log_notes.setVisibility(TableRow.GONE);
		} else {
			rl_log_notes.setVisibility(TableRow.VISIBLE);
			if(type == OperationLog.LOG_TYPE_LOGIN){
				((TextView) findViewById(R.id.textView5)).setText("登录网站");
				textDescription.setText(log.getInvoker());
			}else if(type == OperationLog.LOG_TYPE_DAO_LOGIN){
				((TextView) findViewById(R.id.textView5)).setText("请求APP");
				textDescription.setText(log.getInvoker());
			}else if(type == OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT){
				((TextView) findViewById(R.id.textView5)).setText("加密密文");
				textDescription.setText(log.getInvoker());
			}else{
				((TextView) findViewById(R.id.textView5)).setText("签名原文");
				textDescription.setText(log.getMessage());
			}
		}

		if ("".equals(log.getSign())) {
			rl_log_signature.setVisibility(TableRow.GONE);
		} else {
			rl_log_signature.setVisibility(TableRow.VISIBLE);
			textSign.setText(log.getSign());
		}
		
		if(type == OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT){
			((TextView) findViewById(R.id.textView6)).setText("解密原文");
		}else{
			((TextView) findViewById(R.id.textView6)).setText("数字签名");
		}

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LogActivity.this.finish();
			}
		});
		
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		final Cert cert = certDao.getCertByCertsn(certsn,strActName);
		RelativeLayout rl_log_certdetail = (RelativeLayout) this.findViewById(R.id.rl_log_certdetail);
		if (cert == null) 
			rl_log_certdetail.setVisibility(RelativeLayout.GONE);
		else
			rl_log_certdetail.setVisibility(RelativeLayout.VISIBLE);
			
		rl_log_certdetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View certDetailView = inflater.inflate(
							R.layout.certdetail, null);

					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyyMMddHHmmss");
					SimpleDateFormat sdf2 = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
					sdf.setTimeZone(tzChina);
					// sdf2.setTimeZone(tzChina);
					if (cert != null) {
						String certificate = cert.getCertificate();
						byte[] bCert = Base64.decode(certificate);
						// byte[] bEncCert =
						// Base64.decode(cert.getEnccertificate());
						Certificate oCert = jse.getCertFromBuffer(bCert);
						X509Certificate oX509Cert = (X509Certificate) oCert;
						// X509Certificate oEncX509Cert = (X509Certificate) jse
						// .getCertFromBuffer(bEncCert);
						try {
							ASN1InputStream asn1Input = new ASN1InputStream(
									new ByteArrayInputStream(bCert));
							ASN1Object asn1X509 = asn1Input.readObject();
							X509CertificateStructure x509 = X509CertificateStructure
									.getInstance(asn1X509);
							((TextView) certDetailView
									.findViewById(R.id.tvversion)).setText(jse
									.getCertDetail(1, bCert));
							((TextView) certDetailView
									.findViewById(R.id.tvsignalg))
									.setText(oX509Cert.getSigAlgName());
							((TextView) certDetailView
									.findViewById(R.id.tvcertsn))
									.setText(new String(Hex.encode(oX509Cert
											.getSerialNumber().toByteArray())));
							((TextView) certDetailView
									.findViewById(R.id.tvsubject)).setText(x509
									.getSubject().toString());
							((TextView) certDetailView
									.findViewById(R.id.tvissue)).setText(x509
									.getIssuer().toString());

							String strNotBeforeTime = jse.getCertDetail(11,
									bCert);
							String strValidTime = jse.getCertDetail(12, bCert);
							Date fromDate = sdf.parse(strNotBeforeTime);
							Date toDate = sdf.parse(strValidTime);

							((TextView) certDetailView
									.findViewById(R.id.tvaftertime))
									.setText(sdf2.format(toDate));
							((TextView) certDetailView
									.findViewById(R.id.tvbeforetime))
									.setText(sdf2.format(fromDate));

							RelativeLayout rl_certchainURL = (RelativeLayout) certDetailView.findViewById(R.id.rl_certchainURL);
							String sCertChainPath = jse.getCertExtInfo(
									"1.2.156.1.8888.144", oX509Cert);
							if ("".equals(sCertChainPath) || null == sCertChainPath) {
								rl_certchainURL.setVisibility(RelativeLayout.GONE);
							} else {
								rl_certchainURL.setVisibility(RelativeLayout.VISIBLE);
								((TextView) certDetailView.findViewById(R.id.tvcertchainpath))
										.setText(sCertChainPath);
							}

							if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
								((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
								if(!"".equals(cert.getDevicesn())){
								    certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
								    ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
								}else
									certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
							}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
								((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);
								if(!"".equals(cert.getDevicesn())){
								    certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
								    ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
								}else
									certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
							}else{
								((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
								certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
							}
															
							RelativeLayout rl_subjectUID = (RelativeLayout) certDetailView.findViewById(R.id.rl_subjectUID);
		
							String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
							if("".equals(sCertUnicode) || null == sCertUnicode )
								 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
							
							if ("".equals(sCertUnicode) || null == sCertUnicode) {
								rl_subjectUID.setVisibility(RelativeLayout.GONE);
							} else {
								rl_subjectUID.setVisibility(RelativeLayout.VISIBLE);
								((TextView) certDetailView.findViewById(R.id.tvcertunicode))
										.setText(sCertUnicode);
							}
							
														
						} catch (Exception e) {
							Log.e(CommonConst.TAG, e.getMessage(), e);
							Toast.makeText(LogActivity.this, e.getMessage(),
									Toast.LENGTH_LONG).show();
							return;
						}

					} else {
						Toast.makeText(LogActivity.this, "证书不存在",
								Toast.LENGTH_LONG).show();
						return;
					}

					Intent intent = new Intent(LogActivity.this, CertDetailActivity.class);
				    intent.putExtra("CertId", cert.getId()+"");
				    startActivity(intent);
					
					/*AlertDialog.Builder builder = new Builder(LogActivity.this);
					builder.setIcon(R.drawable.view);
					builder.setTitle("证书明细");
					builder.setView(certDetailView);
					builder.setNegativeButton("关闭",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.show();*/
				}
		});
		
		if(isNotification)
			uploadLogRecord(logid);   //上传使用记录
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			LogActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private  void uploadLogRecord(int logId){
		UploadLogService.mActivity = LogActivity.this;
		
		Intent i = new Intent(this, UploadLogService.class);
		
		Bundle extras = new Bundle();	
		extras.putString("logId", logId+"");
        i.putExtras(extras);
        startService(i);
	}
	
}
