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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
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
						if(delBTDevice.isChecked())				
						   doDeleteCertByBlueTooth(certID);
						else
						   doDeleteCertByNoBlueTooth(certID);
					}else{
						if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())) {
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
	
	private  void  doDeleteCertByBlueTooth(final int  certId){
		boolean bRet = true;
		final Cert  cert = mCert;
		final int saveType = mAccountDao.getLoginAccount().getSaveType();
		final Handler handler = new Handler(CertDeleteActivity.this.getMainLooper());
		
		String strActName = mAccountDao.getLoginAccount().getName();
		if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		final String strActNameBT = strActName;
		final SharedPreferences sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		final JShcaEsStd gEsDev = JShcaEsStd.getIntence(CertDeleteActivity.this);
		//if(null == ScanBlueToothSimActivity.gKsSdk)
		  //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertDeleteActivity.this.getApplication(), CertDeleteActivity.this);
		ht = new HandlerThread("es_device_working_thread");
	    ht.start();
	    workHandler = new Handler(ht.getLooper()); 
				
		final String sPwd = mOriginalPasswordView.getText().toString();
		if (sPwd != null && !"".equals(sPwd)) {
			try {
				 showProgDlg("连接设备中..."); 			
				
				 workHandler.post(new Runnable() {
		 			@Override
		 			public void run() {  
		 		     if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType){
		 				shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
	                    if(null == devInfo){			
		 				     int ret = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
				             if(ret != 0){
				            	 handler.post(new Runnable() {
									 @Override
								     public void run() {
				        	            closeProgDlg();
					                    Toast.makeText(CertDeleteActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
									 }
								 }); 
				            	 
					             return;
				             }
	                    }

	                    handler.post(new Runnable() {
						   @Override
						   public void run() {
	                           changeProgDlg("删除证书中..");
						   }
						});
	                    
				        int nRet = -1;
				        nRet = gEsDev.verifyUserPin(sPwd);
				        if(nRet != 0){
				        	handler.post(new Runnable() {
							      @Override
							      public void run() {
							    	 closeProgDlg();
			        		         Toast.makeText(CertDeleteActivity.this, "蓝牙key密码错误",Toast.LENGTH_SHORT).show();
							      }
							});
			        		
						    return;
				        }
				                
				        if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
				        	if(null == gEsDev.readRSASignatureCert() || "".equals(gEsDev.readRSASignatureCert())){
				        		certDao.deleteCert(certId);
				        		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());	
				        		if(null != sealInfo)
								   mSealInfoDao.deleteSeal(sealInfo.getId());
				        		//nRet = gEsDev.detroyRSASignCert(sPwd,CommonConst.CERT_MOUDLE_SIZE);
				        	}else{	
				        	   byte[] bCert = Base64.decode(gEsDev.readRSASignatureCert());
				        	   String certSN = "";
				        	   try {
								   certSN = jse.getCertDetail(2, bCert);
							   } catch (Exception e) {
								 // TODO Auto-generated catch block
								 e.printStackTrace();
							   }
				        	   
				        	   if(!certSN.equals(cert.getCertsn()))	 {
				        		   handler.post(new Runnable() {
									      @Override
									      public void run() {
						        	          closeProgDlg();
						        	          Toast.makeText(CertDeleteActivity.this, "该证书与蓝牙key内证书不匹配",Toast.LENGTH_SHORT).show();
									      } 
				        		   });
				        		   
				        		   return;
				        	   }
				        			   
				        	   nRet = gEsDev.detroyRSASignCert(sPwd,CommonConst.CERT_MOUDLE_SIZE);
				        	   handler.post(new Runnable() {
							      @Override
							      public void run() {
				        	          closeProgDlg();
							      }
							   });
				        	
				        	   if(nRet == 0){
				        		   certDao.deleteCert(certId);
				        		   SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());		
				        		   if(null != sealInfo)
								      mSealInfoDao.deleteSeal(sealInfo.getId());
				        	   }else{
				        		   handler.post(new Runnable() {
								      @Override
								      public void run() {
				        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
								      }
								   });
				        		
								   return;
				        	   }
				        	}
				        }else{
				        	if(null == gEsDev.readSM2SignatureCert() || "".equals(gEsDev.readSM2SignatureCert()) ){
				        		certDao.deleteCert(certId);
				        		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());			
				        		if(null != sealInfo)
				        		   mSealInfoDao.deleteSeal(sealInfo.getId());
				        		//nRet = gEsDev.detroySM2SignCert(sPwd);
				        		if(null != gEsDev.readSM2EncryptCert() && !"".equals(gEsDev.readSM2EncryptCert())){
				        			nRet = gEsDev.detroySM2EncryptCert(sPwd);	
				        			
				        		    if(nRet == 0){
				        		        Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
				        		        if(null != encCert)
				        			       certDao.deleteCert(encCert.getId());
				        		    }
				        		}else{
				        			Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
			        		        if(null != encCert){
			        			       certDao.deleteCert(encCert.getId());
			        			       //nRet = gEsDev.detroySM2EncryptCert(sPwd);	
			        		        }
				        		}   		
				        	}else{	
				        	   nRet = gEsDev.detroySM2SignCert(sPwd);
				        	   if(nRet == 0){
				        		   certDao.deleteCert(certId);
				        		   SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());	
				        		   if(null != sealInfo)
								      mSealInfoDao.deleteSeal(sealInfo.getId());
				               }else{
				        		   handler.post(new Runnable() {
								      @Override
								      public void run() {
				        		         closeProgDlg();
				        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
								      }
								   });
				        		
								   return;
				        	   }
				        	
				        	   Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
							   if(null != encCert){
								  nRet = gEsDev.detroySM2EncryptCert(sPwd);
								  handler.post(new Runnable() {
								     @Override
								     public void run() {
								        closeProgDlg();
								     }
								  });
								
					        	  if(nRet == 0){
					        		  certDao.deleteCert(encCert.getId());
					        	  }else{
					        		  certDao.deleteCert(encCert.getId());
					        		  handler.post(new Runnable() {
									      @Override
									      public void run() {
					        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
									      }
									  });
					        		
									  return;
					        	  }
							   }
				        	}
				        }
				        
				        handler.post(new Runnable() {
						    @Override
						    public void run() {
				              closeProgDlg();
					          Toast.makeText(CertDeleteActivity.this, "删除证书成功", Toast.LENGTH_SHORT).show();
						    }
						});
				        
				        Intent intent = new Intent(CertDeleteActivity.this, MainActivity.class);	
						startActivity(intent);	
					    CertDeleteActivity.this.finish();
		 		     }else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType){
		 		    	try{
		 		    	    if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
		 		    	    	ScanBlueToothSimActivity.gKsSdk.connect(cert.getDevicesn(), "778899", 500);
		 		    	}catch(Exception ex){
		 		    		 handler.post(new Runnable() {
								 @Override
							     public void run() {
			        	            closeProgDlg();
				                    Toast.makeText(CertDeleteActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
								 }
							 }); 
			            	 
				             return;
						}
		 		    	
		 		        handler.post(new Runnable() {
							   @Override
							   public void run() {
		                           changeProgDlg("删除证书中..");
							   }
					    });
		                    
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
				        		         Toast.makeText(CertDeleteActivity.this, "蓝牙sim卡密码错误",Toast.LENGTH_SHORT).show();
								      }
								});
				        		
							    return;
					     }
					                
					     if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
					        	if(null == ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert())){
					        		certDao.deleteCert(certId);
					        		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());
					        		if(null != sealInfo)
									   mSealInfoDao.deleteSeal(sealInfo.getId());
					        		//nRet = gEsDev.detroyRSASignCert(sPwd,CommonConst.CERT_MOUDLE_SIZE);
					        	}else{	
					        	   byte[] bCert = Base64.decode(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert());
					        	   String certSN = "";
					        	   try {
									   certSN = jse.getCertDetail(2, bCert);
								   } catch (Exception e) {
									 // TODO Auto-generated catch block
									 e.printStackTrace();
								   }
					        	   
					        	   if(!certSN.equals(cert.getCertsn()))	 {
					        		   handler.post(new Runnable() {
										      @Override
										      public void run() {
							        	          closeProgDlg();
							        	          Toast.makeText(CertDeleteActivity.this, "该证书与蓝牙sim卡内证书不匹配",Toast.LENGTH_SHORT).show();
										      } 
					        		   });
					        		   
					        		   return;
					        	   }
					        			   
					        	   nRet = ScanBlueToothSimActivity.gKsSdk.detroyRSAKeyPairAndCert(sPwd);
					        	   handler.post(new Runnable() {
								      @Override
								      public void run() {
					        	          closeProgDlg();
								      }
								   });
					        	
					        	   if(nRet == 0){
					        		   certDao.deleteCert(certId);
					        		   SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());	
					        		   if(null != sealInfo)
									      mSealInfoDao.deleteSeal(sealInfo.getId());
					        	   }else{
					        		   handler.post(new Runnable() {
									      @Override
									      public void run() {
					        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
									      }
									   });
					        		
									   return;
					        	   }
					        	}
					     }else{
					        	if(null == ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert()) ){
					        		certDao.deleteCert(certId);
					        		SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());	
					        		if(null != sealInfo)
									   mSealInfoDao.deleteSeal(sealInfo.getId());
					        		//nRet = gEsDev.detroySM2SignCert(sPwd);
					        		if(null != ScanBlueToothSimActivity.gKsSdk.readSM2EncryptCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readSM2EncryptCert())){
					        			nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(sPwd);	
					        			
					        		    if(nRet == 0){
					        		        Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
					        		        if(null != encCert)
					        			       certDao.deleteCert(encCert.getId());
					        		    }
					        		}else{
					        			Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
				        		        if(null != encCert){
				        			       certDao.deleteCert(encCert.getId());
				        			       //nRet = gEsDev.detroySM2EncryptCert(sPwd);	
				        		        }
					        		}   		
					        	}else{	
					        	   nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(sPwd);	
					        	   if(nRet == 0){
					        		   certDao.deleteCert(certId);
					        		   SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), mAccountDao.getLoginAccount().getName());		
					        		   if(null != sealInfo)
									      mSealInfoDao.deleteSeal(sealInfo.getId());
					               }else{
					        		   handler.post(new Runnable() {
									      @Override
									      public void run() {
					        		         closeProgDlg();
					        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
									      }
									   });
					        		
									   return;
					        	   }
					        	
					        	   Cert  encCert = certDao.getCertByEnvsn(cert.getEnvsn()+"-e", strActNameBT);
								   if(null != encCert){
									  nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(sPwd);	
									  handler.post(new Runnable() {
									     @Override
									     public void run() {
									        closeProgDlg();
									     }
									  });
									
						        	  if(nRet == 0){
						        		  certDao.deleteCert(encCert.getId());
						        	  }else{
						        		  certDao.deleteCert(encCert.getId());
						        		  handler.post(new Runnable() {
										      @Override
										      public void run() {
						        		         Toast.makeText(CertDeleteActivity.this, "删除证书失败",Toast.LENGTH_SHORT).show();
										      }
										  });
						        		
										  return;
						        	  }
								   }
					        	}
					     }
					        
					     handler.post(new Runnable() {
							    @Override
							    public void run() {
					              closeProgDlg();
						          Toast.makeText(CertDeleteActivity.this, "删除证书成功", Toast.LENGTH_SHORT).show();
							    }
					     });
					        
					     Intent intent = new Intent(CertDeleteActivity.this, MainActivity.class);	
				         startActivity(intent);	
						 CertDeleteActivity.this.finish();
		 		    		
		 		     }
		 			}
				 });
			} catch (Exception e) {
				 handler.post(new Runnable() {
					  @Override
					  public void run() {
			               closeProgDlg();
				           Toast.makeText(CertDeleteActivity.this, "蓝牙key密码错误",Toast.LENGTH_SHORT).show();
					   }
			     });
				 
				 return;
			}
		}else {
			Toast.makeText(CertDeleteActivity.this,"请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
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
		if(CommonConst.USE_FINGER_TYPE == cert.getFingertype()){
			if(!"".equals(cert.getCerthash())) {
				//return cert.getCerthash();
				if(!"".equals(strPWD) && strPWD.length() > 0)
					return strPWD;
			}else
			    return strPWD;
		}

		if (!"".equals(strPWD) && strPWD.length() > 0)
			return strPWD;

/*
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));

		return strPWDHash;
		*/
		return strPWD;
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
