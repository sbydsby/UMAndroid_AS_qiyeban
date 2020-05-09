package com.sheca.umandroid;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.excelsecu.slotapi.EsIBankDevice;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
	private JShcaEsStd gEsDev = null; 
	private  JShcaUcmStd gUcmSdk = null;
	//private  JShcaKsStd gKsSdk = null;
	private SharedPreferences sharedPrefs;
	
	private String  mContainerid = "";   
	private String strENVSN = "";
	private String strInfo="";
	private String responResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_cert_revoke);
		
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
		
		gEsDev = JShcaEsStd.getIntence(CertRevokeActivity.this);
		//if(null == ScanBlueToothSimActivity.gKsSdk)
		  // ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertRevokeActivity.this.getApplication(), CertRevokeActivity.this);
		
		gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		ht = new HandlerThread("es_device_working_thread");
	    ht.start();
	    workHandler = new Handler(ht.getLooper()); 
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("CertId")!=null){
				certID = Integer.parseInt(extras.getString("CertId"));
				mCert = certDao.getCertByID(certID);
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
			        if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()){	
				        doRenvokeCertByBlueTooth(certID);
			        }else{
				        if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
					        doRenvokeSM2Cert(certID);
		                else
		        	        doRenvokeRSACert(certID);
			       }		       
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
			final String sKeyStore = cert.getKeystore();
			byte[] bKeyStore = Base64.decode(sKeyStore);
			ByteArrayInputStream kis = new ByteArrayInputStream(
					bKeyStore);
			KeyStore oStore = null;
			try {
				oStore = KeyStore.getInstance("PKCS12");
				oStore.load(kis, getPWDHash(sPwd,cert).toCharArray());

			} catch (Exception e) {
				Toast.makeText(CertRevokeActivity.this, "证书密码错误",
						Toast.LENGTH_SHORT).show();
				//CommUtil.showErrPasswordMsg(CertRevokeActivity.this,cert.getId());
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

						getCertIdByCertSn(CertRevokeActivity.this, strActName, cert.getCertsn(), getPWDHash(sPwd,cert), handler,certId);
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
					Toast.makeText(CertRevokeActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
					return;
				}
				
				int ret =  gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash(sPwd,cert));
				if(ret != 0){
					Toast.makeText(CertRevokeActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
					//CommUtil.showErrPasswordMsg(CertRevokeActivity.this,cert.getId());
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

							getCertIdByCertSn(CertRevokeActivity.this, strActName, cert.getCertsn(), getPWDHash(sPwd,cert), handler,certId);

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
	
	private  void  doRenvokeCertByBlueTooth(final int  certId){
		boolean bRet = true;
		final Cert  cert = mCert;
		final int saveType = mAccountDao.getLoginAccount().getSaveType();
		final Handler handler = new Handler(CertRevokeActivity.this.getMainLooper());
		
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			try {
				 showProgDlg("连接设备中..."); 			
				
				 workHandler.post(new Runnable() {
		 			@Override
		 			public void run() {  
		 			/*	if(!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))){	 				
		 				  if(!sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "").equals(cert.getDevicesn())){
		 					 handler.post(new Runnable() {
								 @Override
							     public void run() {
		 					        closeProgDlg();
	 						        Toast.makeText(CertDeleteActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
								 }
							  }); 
		 					 
					          return;
	 					  }
		 				}else{
		 					 handler.post(new Runnable() {
								 @Override
							     public void run() {
		 					        closeProgDlg();
	 						        Toast.makeText(CertDeleteActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
								 }
							  }); 
		 					 
					          return;
		 				}
		 				*/
		 				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
		 				   shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
	                       if(null == devInfo){			
		 				       int ret = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
				               if(ret != 0){
				            	   handler.post(new Runnable() {
									 @Override
								     public void run() {
				        	            closeProgDlg();
					                    Toast.makeText(CertRevokeActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
									 }
								   }); 
				            	 
					               return;
				               }
	                       }

				           int nRet = -1;
				           nRet = gEsDev.verifyUserPin(sPwd);
				           if(nRet != 0){
				        	  handler.post(new Runnable() {
							      @Override
							      public void run() {
							    	 closeProgDlg();
			        		         Toast.makeText(CertRevokeActivity.this, "蓝牙key密码错误",Toast.LENGTH_SHORT).show();
							      }
							  });
			        		
						      return;
				           }
		 				}else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
		 					try{
		 					  if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
		 						 ScanBlueToothSimActivity.gKsSdk.connect(cert.getDevicesn(), "778899", 500);
		 					}catch(Exception ex){
		 						 handler.post(new Runnable() {
									 @Override
								     public void run() {
				        	            closeProgDlg();
					                    Toast.makeText(CertRevokeActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
									 }
								   }); 
				            	 
					               return;
							}
		 					
		 					int nRet = -1;
		 					if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype()))
					           nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInRSA(sPwd);
		 					else
		 					   nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInSM2(sPwd);
		 					
					        if(nRet != 0){
					        	handler.post(new Runnable() {
								      @Override
								      public void run() {
								    	 closeProgDlg();
				        		         Toast.makeText(CertRevokeActivity.this, "蓝牙sim卡密码错误",Toast.LENGTH_SHORT).show();
								      }
								});
				        		
							    return;
					        }
		 				}
		 				
				        handler.post(new Runnable() {
							   @Override
							   public void run() {
								   closeProgDlg();
							   }
				        });
				        
				        try{			
					        //doRevokeCert(handler,certId,sPwd);
						}catch(Exception ex){
							closeProgDlg();
							Toast.makeText(CertRevokeActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
						}    
		 			}
				 });
			} catch (Exception e) {
				 handler.post(new Runnable() {
					  @Override
					  public void run() {
			               closeProgDlg();
				           Toast.makeText(CertRevokeActivity.this, "蓝牙key密码错误",Toast.LENGTH_SHORT).show();
					   }
			     });
				 
				 return;
			}
		}else {
			Toast.makeText(CertRevokeActivity.this,"请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
		}	
	}

	int certId=0;

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
							Toast.makeText(CertRevokeActivity.this, "证书撤销成功",Toast.LENGTH_SHORT).show();

							Intent intent = new Intent(CertRevokeActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
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
//
//		String timeout = CertRevokeActivity.this.getString(R.string.WebService_Timeout);
//		String urlPath = CertRevokeActivity.this.getString(R.string.UMSP_Service_RevokeCert);
//		Map<String,String> postParams = new HashMap<String,String>();
//		postParams.put("certSN", mCert.getCertsn());
//		postParams.put("reason", CommonConst.REVOKE_CERT_REASON);
//    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//    	String postParam = "certSN="+URLEncoder.encode(mCert.getCertsn(), "UTF-8")+
//		                   "&reason="+URLEncoder.encode(CommonConst.REVOKE_CERT_REASON, "UTF-8");
//        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//
//    	JSONObject jb = JSONObject.fromObject(responseStr);
//		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
//		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//		if (resultStr.equals("0")) {
//			    setCertRevokeStatus(mCert);
//			    saveLog(OperationLog.LOG_TYPE_REVOKECERT, mCert.getCertsn(), "","", "");
//
//			   /* if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()){
//			        shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
//				    if(null == devInfo)
//					   gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
//
//				    if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())){
//				    	 if(null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
//							 nRet = gEsDev.detroySM2SignCert(certPwd);
//						 if(null != gEsDev.readSM2EncryptCert() &&  !"".equals(gEsDev.readSM2EncryptCert()))
//						     nRet = gEsDev.detroySM2EncryptCert(certPwd);
//				    }else{
//				    	 if(null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
//							 nRet = gEsDev.detroyRSASignCert(certPwd,CommonConst.CERT_MOUDLE_SIZE);
//				    }
//			    } */
//
//			    handler.post(new Runnable() {
//				     @Override
//				      public void run() {
//				    	 closeProgDlg();
//				         Toast.makeText(CertRevokeActivity.this, "证书撤销成功",Toast.LENGTH_SHORT).show();
//
//				         Intent intent = new Intent(CertRevokeActivity.this, MainActivity.class);
//				         startActivity(intent);
//				     }
//				});
//		}else if(resultStr.equals("10012")){
//			handler.post(new Runnable() {
//				 @Override
//					public void run() {
//					  changeProgDlg("登录中...");
//			          loginUMSPService(mAccountDao.getLoginAccount().getName());
//					  closeProgDlg();
//
//					  showRenvokeCert(true);
//					}
//			});
//		}else {
//			 handler.post(new Runnable() {
//			     @Override
//			      public void run() {
//			          closeProgDlg();
//			          Toast.makeText(CertRevokeActivity.this, "证书撤销失败:"+resultStr+","+returnStr,Toast.LENGTH_SHORT).show();
//			     }
//			 });
//		}


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
						responResult=mUnitTrust.RevokeCert(strInfo);
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

							Toast.makeText(CertRevokeActivity.this, "证书撤销成功",Toast.LENGTH_SHORT).show();

							Intent intent = new Intent(CertRevokeActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
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
	
	private Boolean loginUMSPService(String act){    //重新登录UM Service
		   String returnStr = "";
			try {
				//异步调用UMSP服务：用户登录
				String timeout = CertRevokeActivity.this.getString(R.string.WebService_Timeout);				
				String urlPath = CertRevokeActivity.this.getString(R.string.UMSP_Service_Login);
	
				Map<String,String> postParams = new HashMap<String,String>();
				postParams.put("accountName", act);
				postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword(),null));    //账户口令需要HASH并转为BASE64字符串	
				if(mAccountDao.getLoginAccount().getType() == 1)
				    postParams.put("appID", CommonConst.UM_APPID);
				else
				    postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());
				
				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					String actpwd = "";
					if(mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
						actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword(),null);
					else
						actpwd = mAccountDao.getLoginAccount().getPassword();
					
					String postParam = "";
					if(mAccountDao.getLoginAccount().getType() == 1)
			    		 postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
			                         "&pwdHash="+URLEncoder.encode(actpwd, "UTF-8")+
			                         "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
			    	else
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

			} catch (Exception exc) {
				return false;
			}
			
			return true;
	}
	   
	private  String   getPWDHash(String strPWD,Cert cert){
		String strPWDHash = "";

//		if(null != cert) {
//			if (!"".equals(strPWD) && strPWD.length() > 0)
//				return strPWD;
//		}

		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
		if(cert.getFingertype()   == CommonConst.USE_FINGER_TYPE)
			bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要

		strPWDHash = new String(Base64.encode(bDigest));
		
		return strPWDHash;
	}
	
	
		
	private int initShcaCciStdService(){  //初始化创元中间件
		int retcode = -1;
		
		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertRevokeActivity.this);
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
 		
 		String myHttpBaseUrl = CertRevokeActivity.this.getString(R.string.UMSP_Base_Service);		
		myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
		
		bRan = jse.random(256, "SHA1PRNG", "SUN");
		gUcmSdk.setRandomSeed(bRan);
		//gUcmSdk.setRandomSeed(bRS);
		retcode = gUcmSdk.doInitService(myHttpBaseUrl,CommonConst.UM_APPID);
 		
     	return retcode;
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
