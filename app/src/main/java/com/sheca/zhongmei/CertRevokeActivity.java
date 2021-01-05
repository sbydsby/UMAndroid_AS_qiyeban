package com.sheca.zhongmei;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sheca.javasafeengine;

import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.LogDao;
import com.sheca.zhongmei.event.RefreshEvent;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.OperationLog;
import com.sheca.zhongmei.model.ShcaCciStd;
import com.sheca.zhongmei.util.CommUtil;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.PKIUtil;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.zhongmei.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import org.greenrobot.eventbus.EventBus;

import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertRevokeActivity extends Activity {
	private CertDao certDao = null;
	private AccountDao mAccountDao = null;
	private LogDao logDao = null;
	private javasafeengine jse = null;
	private int certID = 0;
	private Cert mCert = null;
	private KeyPair mKeyPair = null;  
	
	private ProgressDialog progDialog = null;
	private EditText mOriginalPasswordView;
	
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	//private  JShcaKsStd gKsSdk = null;
	private SharedPreferences sharedPrefs;
	
	private String  mContainerid = "";   
	private String strENVSN = "";
	private String strInfo="";
	private String responResult;

	int certId=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_cert_revoke);

		//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//设置状态栏颜色
		getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("撤销证书");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CertRevokeActivity.this.finish();
			}
		});

		certDao = new CertDao(this);
		mAccountDao= new AccountDao(this);
		logDao = new LogDao(this);
		jse = new javasafeengine();

		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		ht = new HandlerThread("es_device_working_thread");
	    ht.start();
	    workHandler = new Handler(ht.getLooper()); 
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("CertId")!=null){
				certID = Integer.parseInt(extras.getString("CertId"));
				mCert = certDao.getCertByID(certID);
				if (mCert != null) {

				} else {

					Toast.makeText(CertRevokeActivity.this,"证书不存在",Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
			}
		}
		
		findViewById(R.id.btnRevoke).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkRevokeCert();
	        }		
		 });
		
	    mOriginalPasswordView = (EditText) findViewById(R.id.textCertPwd);
		mOriginalPasswordView.setText("");
		mOriginalPasswordView.requestFocus();
		
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()){
			findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
		    findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
			
			//mOriginalPasswordView.setText("1");
			mOriginalPasswordView.setHint("输入蓝牙key密码");
		}else if(CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()){
			findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
		    findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
			
			//mOriginalPasswordView.setText("1");
			mOriginalPasswordView.setHint("输入蓝牙sim卡密码");
		}else{
			mOriginalPasswordView.setHint("输入证书密码");
			findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
		    findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
		}

		if(CommUtil.isPasswordLocked(CertRevokeActivity.this,certID)){
			findViewById(R.id.btnRevoke).setVisibility(android.widget.RelativeLayout.GONE);
			return;
		}
	}
	
	
	private  void checkRevokeCert(){
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
              showRenvokeCert(false);
		}
		
	}
		
	private  void showRenvokeCert(boolean isLogin){
	  if(isLogin){
		        if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
			        doRenvokeSM2Cert(certID);
              else
      	        doRenvokeRSACert(certID);
	  }else{
		Builder builder = new Builder(CertRevokeActivity.this);
		builder.setMessage("确认是否撤销证书？");
		builder.setIcon(R.drawable.alert);
		builder.setTitle("提示");
		builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				try {	

				        if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
					        doRenvokeSM2Cert(certID);
		                else
		        	        doRenvokeRSACert(certID);

				} catch (Exception e) {			
					Toast.makeText(CertRevokeActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
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

	}
	
	private  void  doRenvokeRSACert(final int  certId){
		boolean bRet = true;
		final Cert  cert = mCert;
		
		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			if (!PKIUtil.verifyPin(cert.getPrivatekey(), sPwd,CertRevokeActivity.this)) {

				Toast.makeText(CertRevokeActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();

				return;
			}
			
			final Handler handler = new Handler(CertRevokeActivity.this.getMainLooper());		
			workHandler.post(new Runnable() {
				@Override
				public void run() {		
					try {			
//			           doRevokeCert(handler,certId,getPWDHash(sPwd,cert));
//			           doRevokeCert(handler,certId,sPwd);

						String strActName = mAccountDao.getLoginAccount().getName();
						if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
							strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

						getCertIdByCertSn(CertRevokeActivity.this, strActName, cert.getCertsn(),sPwd, handler,certId);
					}catch(Exception ex){
						closeProgDlg();
						Toast.makeText(CertRevokeActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
					}
				}
			}); 

		} else {
			Toast.makeText(CertRevokeActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	private  void  doRenvokeSM2Cert(final int  certId){

		final Cert  cert = mCert;

		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			try {
				if (!PKIUtil.verifyPin(cert.getPrivatekey(), sPwd,CertRevokeActivity.this)) {

					Toast.makeText(CertRevokeActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();

					return;
				}
				
				final Handler handler = new Handler(CertRevokeActivity.this.getMainLooper());					
				workHandler.post(new Runnable() {
					@Override
					public void run() {		
						try {			
//				           doRevokeCert(handler,certId,getPWDHash(sPwd,cert));
							String strActName = mAccountDao.getLoginAccount().getName();
							if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
								strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

							getCertIdByCertSn(CertRevokeActivity.this, strActName, cert.getCertsn(), sPwd, handler,certId);

						}catch(Exception ex){
							closeProgDlg();
							Toast.makeText(CertRevokeActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
						}
					}
				}); 

				
			} catch (Exception e) {
				ShcaCciStd.gSdk = null;
				Toast.makeText(CertRevokeActivity.this, "密码分割组件初始化失败",Toast.LENGTH_SHORT).show();
				return;
			}
		} else {
			Toast.makeText(CertRevokeActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
	}
	



	public void getCertIdByCertSn(Activity context, String userName, String certSn,final  String newPwd,final Handler handler,final int oldcertId) {
		final UniTrust uniTrustObi = new UniTrust(context, false);
		final String param = ParamGen.getAccountCertByCertSN(context, userName, certSn);
		new Thread(new Runnable() {
			@Override
			public void run() {
				com.sheca.umplus.model.Cert cert = uniTrustObi.getAccountCertByCertSN(param);
				if (null != cert) {
					certId = cert.getId();
					try {
						doRevokeCert(handler,oldcertId,certId,newPwd);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{

					certDao.deleteCert(certID);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EventBus.getDefault().post(new RefreshEvent());
							Toast.makeText(CertRevokeActivity.this, "证书撤销成功",Toast.LENGTH_SHORT).show();

							Intent intent = new Intent(CertRevokeActivity.this, CertResultActivity.class);
							intent.putExtra("type",3);
							startActivity(intent);
							finish();
//							Intent intent = new Intent(CertRevokeActivity.this, MainActivity.class);
//							startActivity(intent);
//							finish();
						}
					});

				}
			}
		}).start();


	}
	

	private  void  doRevokeCert(final Handler handler, final int  oldcertId,final int certID, final String certPwd) throws Exception{
	   handler.post(new Runnable() {
		     @Override
		      public void run() {
		         showProgDlg("证书撤销中...");
		      }
		});
		
		final Cert mCert = certDao.getCertByID(oldcertId);
		final int mSDKCertId = certID;
		int nRet = -1;

		//SDK撤销证书
		// 证书撤销操作
				new MyAsycnTaks(){
					@Override
					public void preTask() {
						String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//						String hashPwd = getPWDHash(certPwd, null);
						strInfo=ParamGen.doRevokeCertParams(mTokenId,mSDKCertId+"",certPwd);
					}

					@Override
					public void doinBack() {
						UniTrust mUnitTrust = new UniTrust(CertRevokeActivity.this, false);
						responResult=mUnitTrust.RevokeCertNew(strInfo);
					}

					@Override
					public void postTask() {
						final APPResponse response = new APPResponse(responResult);
						int resultStr = response.getReturnCode();
						final String retMsg = response.getReturnMsg();

						if(0==resultStr){


							setCertRevokeStatus(mCert);
							saveLog(OperationLog.LOG_TYPE_REVOKECERT, mCert.getCertsn(), "","", "");
							closeProgDlg();
							CommUtil.resetPasswordLocked(CertRevokeActivity.this,mCert.getId());


							EventBus.getDefault().post(new RefreshEvent());
							Toast.makeText(CertRevokeActivity.this, "证书撤销成功",Toast.LENGTH_SHORT).show();

							Intent intent = new Intent(CertRevokeActivity.this, CertResultActivity.class);
							intent.putExtra("type",3);
							startActivity(intent);
							finish();
//							Intent intent = new Intent(CertRevokeActivity.this, MainActivity.class);
//							startActivity(intent);
//							finish();
						}else{
							closeProgDlg();
							Toast.makeText(CertRevokeActivity.this, "证书撤销失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
						}


					}
				}.execute();


	}

	private void setCertRevokeStatus(Cert cert){
		cert.setStatus(Cert.STATUS_REVOKE_CERT);
		certDao.updateCert(cert,mAccountDao.getLoginAccount().getName());
	}
	
	private void saveLog(int type, String certsn, String message,String invoker, String sign) {
		OperationLog log = new OperationLog();
		log.setType(type);
		log.setCertsn(certsn);
		log.setMessage(message);
		log.setSign(sign);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		log.setCreatetime(sdf.format(date));
		log.setInvoker(invoker);
		log.setSignalg(1);
		log.setIsupload(0);
		log.setInvokerid(CommonConst.UM_APPID);
		
		logDao.addLog(log,mAccountDao.getLoginAccount().getName());
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
