package com.sheca.umandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.fingerui.FingerPrintAuthForgetPasswordActivity;
import com.sheca.fingerui.FingerPrintToast;
import com.sheca.fingerui.IFAAFingerprintOpenAPI;
import com.sheca.fingerui.MainActivity.Process;
import com.ifaa.sdk.api.AuthenticatorManager;
import com.ifaa.sdk.auth.AuthenticatorCallback;
import com.ifaa.sdk.auth.message.AuthenticatorMessage;
import com.ifaa.sdk.auth.message.AuthenticatorResponse;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.umandroid.adapter.CertAdapter;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.PKIUtil;
import com.sheca.umandroid.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ForgetPasswordCertActivity extends Activity {
	private CertDao  certDao = null;
	private List<Map<String, String>> mData = null;
	private boolean  mIsViewCert = false; //是否可查看证书详情
	private int      mCertId = -1;        //当前选中证书ID
	private AlertDialog  certListDialog = null;
	private boolean mIsDao = false;   //第三方接口调用标记
	
	private String         strSign = "";        //签名数据	
	private String         strCert = "";        //base64证书
	private int            signAlg = 1;         //签名算法
	
	private javasafeengine jse = null;
	private ProgressDialog progDialog = null;
	private SharedPreferences sharedPrefs;
	
	private  final static int LOGIN_SIGN_FAILURE = 1;
	private  final static int LOGIN_SIGN_SUCCESS = 2;
	private  final int LOG_TYPE_LOGIN = 1;
	private  final int LOG_TYPE_SIGN = 2;
	
	private String  mStrBizSN = "";        //流水号
	private String  mStrAccountName = "";  //账户名称
	private Intent inet = null;  
	
	private  JShcaEsStd gEsDev = null;
	private  JShcaUcmStd gUcmSdk = null;
	//private  JShcaKsStd gKsSdk;
	protected Handler workHandler = null;
	private HandlerThread ht = null;
	
	private Process curProcess = Process.REG_GETREQ;
	private String userid = "test";
	private String secData = "";
	
	private IFAAFingerprintOpenAPI.Callback callback = new IFAAFingerprintOpenAPI.Callback() {
        @Override
        public void onCompeleted(int status, final String info) {
            switch (curProcess) {
                case AUTH_GETREQ:
                	ForgetPasswordCertActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startFPActivity(false);
                            AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST, 2);
                            requestMessage.setData(info);
                            LaunchActivity.authenticator.process(requestMessage, authCallback);

                        }
                    });
                    break;
                case AUTH_SENDRESP:
                	ForgetPasswordCertActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.equals("Success")) {
                                new FingerPrintToast(ForgetPasswordCertActivity.this, FingerPrintToast.ST_AUTHSUCCESS).show("");

                            } else {
                                new FingerPrintToast(ForgetPasswordCertActivity.this, FingerPrintToast.ST_AUTHFAIL).show("验证指纹失败");
                            }
                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(ForgetPasswordCertActivity.this, 0);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };
    
    private AuthenticatorCallback authCallback = new AuthenticatorCallback() {
        @Override
        public void onStatus(int status) {
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(ForgetPasswordCertActivity.this, status);
        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            curProcess = Process.AUTH_SENDRESP;

            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                IFAAFingerprintOpenAPI.getInstance().sendAuthResponeAsyn(data, secData, callback);
            } else {
            	ForgetPasswordCertActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // new FingerPrintToast(ForgetPasswordCertActivity.this, FingerPrintToast.ST_AUTHTEEFAIL).show("验证指纹失败");
                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(ForgetPasswordCertActivity.this, 0);
                        
                        if(LaunchActivity.isIFAAFingerOK){
                        	doFingerLogin();  
                        }else{
                           if(LaunchActivity.failCount >=3){
                        	   findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
       	    				   findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
       	    				   findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
       	    				   findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
       	    				   findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                           }
                        }
                    }
                });
            }

        }
    };
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dao_login_local);
		
		gEsDev = JShcaEsStd.getIntence(ForgetPasswordCertActivity.this); 
		//if(null == ScanBlueToothSimActivity.gKsSdk)
			//ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(ForgetPasswordCertActivity.this.getApplication(), ForgetPasswordCertActivity.this);
		
		gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		certDao = new CertDao(ForgetPasswordCertActivity.this);		
		inet = new Intent(this, ForgetPasswordActivity.class);  
		jse = new javasafeengine();
        
		Intent intent = getIntent();
		if(null != intent.getExtras()){
		     if(null != intent.getExtras().getString("Account")){
		    	   mStrAccountName = intent.getExtras().getString("Account");
		     }
		     if(null != intent.getExtras().getString("TaskID")){
		    	   mStrBizSN = intent.getExtras().getString("TaskID");
		     }
		     if(null != intent.getExtras().getString("mesage")){
					mIsDao = true;
				}
		}
		
		LaunchActivity.isIFAAFingerOpend = sharedPrefs.getBoolean(CommonConst.SETTINGS_FINGER_OPENED, false);
		LaunchActivity.isIFAAFingerOK = false;
		if(LaunchActivity.isIFAAFingerOpend){
		   if(null == LaunchActivity.authenticator )
			   LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
		}
		
		findViewById(R.id.textAppLabel).setVisibility(RelativeLayout.GONE);
		findViewById(R.id.textAppView).setVisibility(RelativeLayout.GONE);
		
		showLoginDlg();
		LaunchActivity.failCount = 0;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			inet.putExtra("ActName", mStrAccountName);	
			if(mIsDao)
				inet.putExtra("message", "dao");
		    startActivity(inet);
		    ForgetPasswordCertActivity.this.finish();
			break;
		}

		return true;
	}
	
	
	private void showLoginDlg(){  	  
		showCert();
		 
		ImageView okBtn  = ((ImageView)findViewById(R.id.btn_loign_ok));
		okBtn.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 doSign();	
	         }		
	    });
		
	    ImageView backBtn  = ((ImageView)findViewById(R.id.btn_loign_back));
		backBtn.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v) {
					inet.putExtra("ActName", mStrAccountName);
					if(mIsDao)
						inet.putExtra("message", "dao");
				    startActivity(inet);
				    ForgetPasswordCertActivity.this.finish();
	         }		
		});	
		  
		findViewById(R.id.relativelayoutPwdLabel2).setVisibility(RelativeLayout.GONE);
		
		if(LaunchActivity.isIFAAFingerOpend){
    	    final Cert cert = certDao.getCertByID(mCertId);
    	    if(null != cert){
    	    	if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
    	    		findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
    				findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
    				findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
    				findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
    				findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
    				
    				LaunchActivity.isIFAAFingerOK = false;
					((EditText) findViewById(R.id.textPwd)).setText("");
    	    	}else{
    	    		if("".equals(cert.getCerthash())){
    	    			findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
	    				findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
	    				findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
	    				findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
	    				findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
	    				
	    				LaunchActivity.isIFAAFingerOK = false;
   						((EditText) findViewById(R.id.textPwd)).setText("");
    	    		}else{
			            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
			            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
			            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);			
			            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
			            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
			        
			            showFingerCheck();
    	    		}
    	    	}
			
    	    	((ImageView)findViewById(R.id.pwdkeyboard)).setOnClickListener(new OnClickListener() {
				   @Override
				   public void onClick(View v) {
					   findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					   findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
					   findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					   findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					   findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
					
					   LaunchActivity.isIFAAFingerOK = false;
					   ((EditText) findViewById(R.id.textPwd)).setText("");
				   }
    	    	});
			
			    ((ImageView)findViewById(R.id.finger_image)).setOnClickListener(new OnClickListener() {
				   @Override
				   public void onClick(View v) {
				      showFingerCheck();
				   }
			    });
			
			    ((TextView)findViewById(R.id.finger_txt)).setOnClickListener(new OnClickListener() {
				  @Override
				  public void onClick(View v) {
					  showFingerCheck();
				  }
			    });
    	    }else{
    	    	findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
    			findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
    			findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
    			findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
    			findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
    	    }
		}else{
			findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
			findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
			findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
		}
	 }  
	

	 private void showCert(){
    	try {
    		ImageView viewCertBtn  = ((ImageView)findViewById(R.id.btnCertView));
			viewCertBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					selectCert();
		        }		
			 });
    		
			mData = getData();
			viewCertBtn.setVisibility(RelativeLayout.VISIBLE);
			
			mCertId = Integer.valueOf(mData.get(0).get("id"));
			final Cert cert = certDao.getCertByID(mCertId);
			if (cert != null) {
				mIsViewCert = true;
				String certificate = cert.getCertificate();
				byte[] bCert = Base64.decode(certificate);
				String strBlank = "证书";
				String strCertName = jse.getCertDetail(17, bCert);
				if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
					strCertName += CommonConst.CERT_SM2_NAME+strBlank;
				else
					strCertName += CommonConst.CERT_RSA_NAME+strBlank;
				
				//if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
					//strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
				//else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
					//strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
				
				if(LaunchActivity.isIFAAFingerOpend){
					if(!"".equals(cert.getCerthash())){
					    EditText edit=(EditText)findViewById(R.id.textPwd);
				        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edit.getWindowToken(),0);
				        edit.clearFocus();
				    
					    findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
					    findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
					    findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
					    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
					    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);		
					}else{
						findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
						findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
						findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
						findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
						findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);	
						
						((EditText) findViewById(R.id.textPwd)).requestFocus();
						((EditText) findViewById(R.id.textPwd)).setFocusable(true);   
						((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);   
					}
				}else{
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);	
					
					((EditText) findViewById(R.id.textPwd)).requestFocus();
					((EditText) findViewById(R.id.textPwd)).setFocusable(true);   
					((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);   
				}
				
				if(null == cert.getCertname()){
					((TextView) findViewById(R.id.textCertView)).setText(strCertName);
				}else{
				    if(cert.getCertname().isEmpty())
				       ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
				    else
				       ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
				}
			}else{
				((TextView) findViewById(R.id.textCertView)).setText("无证书");
				mIsViewCert = false;
			}	
			
			((TextView)findViewById(R.id.textCertView)).setOnClickListener(
					new OnClickListener() {
				       public void onClick(View view) {
				    	   viewCertDetail();
				       }
			});

//			findViewById(R.id.textCertView).getBackground().setAlpha(100);  //0~255透明度值
    	}catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(ForgetPasswordCertActivity.this, "获取证书错误！", Toast.LENGTH_LONG).show();
			((TextView) findViewById(R.id.textCertView)).setText("无证书");
			mIsViewCert = false;
		}	
    }
	 
	private void selectCert(){
	    	if(!mIsViewCert){
	    		Toast.makeText(ForgetPasswordCertActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
	    		return;
	    	}

	    	changeCert();
	}
	    
	    
	private void changeCert(){
		    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View certListView = inflater.inflate(R.layout.certlist, null);
			ListView list = (ListView) certListView.findViewById(R.id.certlist);
			CertAdapter adapter = null;
			
			try {
			   adapter = new CertAdapter(ForgetPasswordCertActivity.this, mData);
			   list.setAdapter(adapter);
			   
			   Builder builder = new Builder(ForgetPasswordCertActivity.this);
			   builder.setIcon(R.drawable.view);
			   builder.setTitle("请选择证书");
			   builder.setView(certListView);
			   builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			   
			   certListDialog = builder.create();
			   certListDialog.show();
			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(ForgetPasswordCertActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
			}
			
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mCertId = Integer.valueOf(mData.get(position).get("id"));
					final Cert cert = certDao.getCertByID(mCertId);
					try {
						String certificate = cert.getCertificate();
						byte[] bCert = Base64.decode(certificate);
						String strBlank = "证书";
						String strCertName = jse.getCertDetail(17, bCert);
						if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
							strCertName += CommonConst.CERT_SM2_NAME+strBlank;
						else
							strCertName += CommonConst.CERT_RSA_NAME+strBlank;
						
						//if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
							//strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
						//else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
							//strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
						if(null == cert.getCertname()){
							((TextView) findViewById(R.id.textCertView)).setText(strCertName);
						}else{
						    if(cert.getCertname().isEmpty())
						       ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
						    else
						       ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(LaunchActivity.isIFAAFingerOpend){
						if(!"".equals(cert.getCerthash())){
						    findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
							findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
						  
						    findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
						    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
						    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);		
						}else{
							findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
							findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);

							findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
							findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
							findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);	
							
							((EditText)findViewById(R.id.textPwd)).setText("");
							LaunchActivity.isIFAAFingerOK = false;
						}
					}else{
						findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
						findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
						
						findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
						findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
						findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);	
						
						((EditText)findViewById(R.id.textPwd)).setText("");
						LaunchActivity.isIFAAFingerOK = false;
					}				
					
					certListDialog.dismiss();
				}
			});

	  }
	   
	  private void viewCertDetail(){
	    	if(!mIsViewCert){
	    		Toast.makeText(ForgetPasswordCertActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	Cert certView = certDao.getCertByID(mCertId);   	
	    	if(CommonConst.CERT_TYPE_RSA.equals(certView.getCerttype()))
	    	    viewCert(mCertId);
	    	else if(CommonConst.CERT_TYPE_SM2.equals(certView.getCerttype()))
	      	    viewSM2Cert(mCertId);
	  }
	    
	 
	 
      private  void  doSign(){
    	 final SharedPreferences sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
    	 
    	 ht = new HandlerThread("es_device_working_thread");
         ht.start();
         workHandler = new Handler(ht.getLooper()); 
      	 
	     if(!mIsViewCert){
		     Toast.makeText(ForgetPasswordCertActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
		     return;
		 }
	    	
	     final Cert cert = certDao.getCertByID(mCertId);
	     if(verifyCert(cert,true)){
	    	 if(verifyDevice(cert,true)){
	    		 workHandler.post(new Runnable(){
		             @Override
		             public void run() {
		            	 if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
		            		 loginSignSM2(cert); 
		            	 else
	    			         loginSign(cert);  
		             } 
	    		 }); 
	    	 }
	     }else{
			return;
	     }
	 }
	    
    
    private List<Map<String, String>> getData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		certList = certDao.getAllCerts(mStrAccountName);

		for (Cert cert : certList) {
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
				continue;
			
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
						
						if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()))
				        	 map.put("certtype", CommonConst.CERT_SM2_NAME); 
				         else
				        	 map.put("certtype", CommonConst.CERT_RSA_NAME);
						
						map.put("savetype", cert.getSavetype()+"");
						map.put("certname",getCertName(cert));
				        list.add(map);
	    			 }
	    		 }
			}
		}

		return list;
	}
    
    
    private void viewCert(final int certId) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certDetailView = inflater.inflate(R.layout.certdetail, null);

		final Cert cert = certDao.getCertByID(certId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);
		// sdf2.setTimeZone(tzChina);
		if (cert != null) {
			String certificate = cert.getCertificate();
			byte[] bCert = Base64.decode(certificate);
			// byte[] bEncCert = Base64.decode(cert.getEnccertificate());
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
				((TextView) certDetailView.findViewById(R.id.tvversion))
						.setText(jse.getCertDetail(1, bCert));
				((TextView) certDetailView.findViewById(R.id.tvsignalg))
						.setText(oX509Cert.getSigAlgName());
				((TextView) certDetailView.findViewById(R.id.tvcertsn))
						.setText(new String(Hex.encode(oX509Cert
								.getSerialNumber().toByteArray())));
				((TextView) certDetailView.findViewById(R.id.tvsubject))
						.setText(x509.getSubject().toString());
				((TextView) certDetailView.findViewById(R.id.tvissue))
						.setText(x509.getIssuer().toString());

				String strNotBeforeTime = jse.getCertDetail(11, bCert);
				String strValidTime = jse.getCertDetail(12, bCert);
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				((TextView) certDetailView.findViewById(R.id.tvaftertime))
						.setText(sdf2.format(toDate));
				((TextView) certDetailView.findViewById(R.id.tvbeforetime))
						.setText(sdf2.format(fromDate));

				RelativeLayout relativeLayout1 = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_certchainURL);
				String sCertChainPath = jse.getCertExtInfo(
						"1.2.156.1.8888.144", oX509Cert);
				if ("".equals(sCertChainPath) || null == sCertChainPath) {
					relativeLayout1.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView
							.findViewById(R.id.tvcertchainpath))
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
					
				BigInteger prime = null;
				int keySize =  0;
			
				String  algorithm  = oX509Cert.getPublicKey().getAlgorithm();  // 获取算法
				KeyFactory keyFact = KeyFactory.getInstance(algorithm);
				
				if ("RSA".equals(algorithm)) { // 如果是RSA加密
					RSAPublicKeySpec keySpec = (RSAPublicKeySpec)keyFact.getKeySpec(oX509Cert.getPublicKey(), RSAPublicKeySpec.class);
					prime = keySpec.getModulus();
				} else if ("DSA".equals(algorithm)) { // 如果是DSA加密
					DSAPublicKeySpec keySpec = (DSAPublicKeySpec)keyFact.getKeySpec(oX509Cert.getPublicKey(), DSAPublicKeySpec.class);
					prime = keySpec.getP();
				}
				
				keySize = prime.toString(2).length(); // 转换为二进制，获取公钥长度
				
				if (keySize == 0) {
					certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertKeySize))
							.setText(keySize+"位");
				}
					
				RelativeLayout relativeLayout2 = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_subjectUID);
				
				String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
				if("".equals(sCertUnicode) || null== sCertUnicode )
					 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
				
				//strUniqueID = sCertUnicode;    //从证书获取身份证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					relativeLayout2.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertunicode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3",oX509Cert);  //获取工商注册号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcerticnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5",oX509Cert);  //获取税号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcerttaxnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4",oX509Cert);  //获取组织机构代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertorgcode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2",oX509Cert);  //获取社会保险号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertinsnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201",oX509Cert);  //获取住房公积金账号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertaccfundnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202",oX509Cert);  //获取事业单位证书号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertinstinumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203",oX509Cert);  //获取社会组织法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertassnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204",oX509Cert);  //获取政府机关法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertgovnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207",oX509Cert);  //获取律师事务所执业许可证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertlawlicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208",oX509Cert);  //获取个体工商户营业执照
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertindilicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209",oX509Cert);  //外国企业常驻代表机构登记证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertforeigncode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210",oX509Cert);  //获取统一社会信用代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertcrednumber))
							.setText(sCertUnicode);
				}

			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(ForgetPasswordCertActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}

		} else {
			Toast.makeText(ForgetPasswordCertActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
			return;
		}

		Builder builder = new Builder(ForgetPasswordCertActivity.this);
		builder.setIcon(R.drawable.view);
		builder.setTitle("证书明细");
		builder.setView(certDetailView);
		builder.setNegativeButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		
		/*builder.setNegativeButton("签名登录",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
						if(VerifyCert(cert))
							   sign(cert);
							else
							   resState = 1;
					}
				});*/
		builder.show();
	}

    private void viewSM2Cert(final int certId) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certDetailView = inflater.inflate(R.layout.certdetail, null);

		final Cert cert = certDao.getCertByID(certId);
		if (cert != null) {
			String certificate = cert.getCertificate();
			byte[] bCert = Base64.decode(certificate);
			Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
		
			try {
				((TextView) certDetailView.findViewById(R.id.tvversion))
						.setText(jse.getCertDetail(1, bCert));
				((TextView) certDetailView.findViewById(R.id.tvsignalg))
						.setText(CommonConst.CERT_ALG_SM2);
				/* ((TextView) certDetailView.findViewById(R.id.tvcertsn))
						.setText(new String(Hex.encode(oX509Cert
								.getSerialNumber().toByteArray()))); */
				((TextView) certDetailView.findViewById(R.id.tvcertsn))
				.setText(jse.getCertDetail(2, bCert));
							
				((TextView) certDetailView.findViewById(R.id.tvsubject))
						.setText(getSM2CertIssueInfo(cert));
				((TextView) certDetailView.findViewById(R.id.tvissue))
						.setText(getSM2CertSubjectInfo(cert));

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
				sdf.setTimeZone(tzChina);
				
				String strNotBeforeTime = jse.getCertDetail(11, bCert);
				String strValidTime = jse.getCertDetail(12, bCert);
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				((TextView) certDetailView.findViewById(R.id.tvaftertime))
						.setText(sdf2.format(toDate));
				((TextView) certDetailView.findViewById(R.id.tvbeforetime))
						.setText(sdf2.format(fromDate));

				RelativeLayout relativeLayout1 = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_certchainURL);
				String sCertChainPath = jse.getCertExtInfo(
						"1.2.156.1.8888.144", oX509Cert);
				if ("".equals(sCertChainPath) || null == sCertChainPath) {
					relativeLayout1.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView
							.findViewById(R.id.tvcertchainpath))
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
					

				RelativeLayout relativeLayout2 = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_subjectUID);

				String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
				if("".equals(sCertUnicode) || null== sCertUnicode )
					 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
				
				//strUniqueID = sCertUnicode;    //从证书获取身份证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					relativeLayout2.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertunicode))
							.setText(sCertUnicode);
				}
				
				certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3",oX509Cert);  //获取工商注册号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcerticnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5",oX509Cert);  //获取税号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcerttaxnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4",oX509Cert);  //获取组织机构代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertorgcode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2",oX509Cert);  //获取社会保险号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertinsnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201",oX509Cert);  //获取住房公积金账号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertaccfundnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202",oX509Cert);  //获取事业单位证书号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertinstinumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203",oX509Cert);  //获取社会组织法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertassnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204",oX509Cert);  //获取政府机关法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertgovnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207",oX509Cert);  //获取律师事务所执业许可证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertlawlicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208",oX509Cert);  //获取个体工商户营业执照
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertindilicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209",oX509Cert);  //外国企业常驻代表机构登记证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertforeigncode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210",oX509Cert);  //获取统一社会信用代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
				} else {
					certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertcrednumber))
							.setText(sCertUnicode);
				}


			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(ForgetPasswordCertActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}

		} else {
			Toast.makeText(ForgetPasswordCertActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
			return;
		}

		Builder builder = new Builder(ForgetPasswordCertActivity.this);
		builder.setIcon(R.drawable.view);
		builder.setTitle("证书明细");
		builder.setView(certDetailView);
		builder.setNegativeButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		
		builder.show();
	}

	private boolean verifyCert(final Cert cert,boolean bShow){	
		if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
			int i = PKIUtil.verifyCertificate(cert.getCertificate(),
				                          cert.getCertchain());
			if(i == CommonConst.RET_VERIFY_CERT_OK){
				return true;
			}/*else if(i == 0){
				if(bShow)
					Toast.makeText(ForgetPasswordCertActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			}*/else{
				if(bShow)
					Toast.makeText(ForgetPasswordCertActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
			}
		}else if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())){
			String strSignCert = "";
			int i = -1;		
			
			if(cert.getEnvsn().indexOf("-e")!=-1)
				return false;
			
			if(!"".equals(cert.getContainerid())){
			   try {
			        javasafeengine jse = new javasafeengine();
			        strSignCert = cert.getCertificate();			      
			        i =	jse.verifySM2Cert(strSignCert,cert.getCertchain());
			   } catch (Exception e) {
					   e.printStackTrace();
			   }
			   
			   if(i == 0){
				     return true;
			   }else if(i == 1){
					if(bShow)
					   Toast.makeText(ForgetPasswordCertActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
			   }else{
					if(bShow)
				       Toast.makeText(ForgetPasswordCertActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
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
		if(null == sDeciceID)
			return true;
		
		//获取设备唯一标识符
		String deviceID = android.os.Build.SERIAL;
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
			deviceID = cert.getDevicesn();
		if(sDeciceID.equals(deviceID))
			return true;
		
		if(bShow)
		   Toast.makeText(ForgetPasswordCertActivity.this, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
		*/
		return true;
	}

	
	private void loginSign(final Cert cert){
		EditText accountPwd = (EditText) findViewById(R.id.textPwd);
		String strAccountPwd = accountPwd.getText().toString().trim();
		
		if(!LaunchActivity.isIFAAFingerOK){
			   if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
				  strAccountPwd = getPWDHash(strAccountPwd,cert);
		}

		final String sPwd = strAccountPwd;
		if (sPwd != null && !"".equals(sPwd)) {
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
				if(null == gEsDev.readRSASignatureCert() || "".equals(gEsDev.readRSASignatureCert())){
					Toast.makeText(ForgetPasswordCertActivity.this, "蓝牙key签名失败",Toast.LENGTH_SHORT).show();
				    return;
				}				
			}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
				if(null == ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert())){
					Toast.makeText(ForgetPasswordCertActivity.this, "蓝牙sim卡签名失败",Toast.LENGTH_SHORT).show();
				    return;
				}				
			}else{
				final String sKeyStore = cert.getKeystore();
			    byte[] bKeyStore = Base64.decode(sKeyStore);
			    ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
			    KeyStore oStore = null;
			    try {
				    oStore = KeyStore.getInstance("PKCS12");
				    oStore.load(kis, sPwd.toCharArray());
			    } catch (Exception e) {
				    Toast.makeText(ForgetPasswordCertActivity.this, "证书密码错误",Toast.LENGTH_SHORT).show();
				    return;
			    }
			}

			new Thread() {
				@Override
				public void run() {							
					String message = String.format("%s=%s&%s=%s&%s=%s",
							                        CommonConst.PARAM_ACCOUNT_NAME_PWD,mStrAccountName,
							                        CommonConst.PARAM_APPID,CommonConst.UM_APPID,
							                        CommonConst.PARAM_BIZSN,mStrBizSN);
							
					try {
						if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
							strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
							signAlg = 1;
						}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
							strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG,sPwd)));
							signAlg = 1;
						}else{
							strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE),cert.getKeystore(), sPwd);
							signAlg = 3;
						}
					} catch (Exception e) {
						Log.e("sheca", e.getMessage(), e);
						handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
					}
					if (strSign != null) {
						boolean flag = true;
						// try {
						// flag = PKIUtil.verifySign(message, sSign,
						// cert.getCertificate());
						// } catch (Exception e) {
						// Log.e("sheca", e.getMessage(), e);
						// Toast.makeText(
						// ProviderClientTestActivity.this,
						// "验证签名错误", Toast.LENGTH_SHORT)
						// .show();
						// }

						if (flag) {
//							dialog.dismiss();
							strCert = cert.getCertificate();							
							if("".equals(cert.getCerthash())){
								cert.setCerthash(sPwd);
								
								String strActName = mStrAccountName;
								certDao.updateCert(cert, strActName);
							}
							
							handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);					
						} else {
							Toast.makeText(
									ForgetPasswordCertActivity.this,
									"验证签名失败", Toast.LENGTH_LONG).show();
						}
					}
				}
			}.start();
		} else {
			Toast.makeText(ForgetPasswordCertActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private void loginSignSM2(final Cert cert){
		EditText accountPwd = (EditText) findViewById(R.id.textPwd);
		String   strAccountPwd = accountPwd.getText().toString().trim();
		
		if(!LaunchActivity.isIFAAFingerOK){
			   if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
				  strAccountPwd = getPWDHash(strAccountPwd,cert);
		}		
		final String sPwd = strAccountPwd;
		String strSignCert ="";
		
		if (sPwd != null && !"".equals(sPwd)) {
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
				if(null == gEsDev.readSM2SignatureCert() || "".equals(gEsDev.readSM2SignatureCert())){
					Toast.makeText(ForgetPasswordCertActivity.this, "蓝牙key签名失败",Toast.LENGTH_SHORT).show();
					return;
				}			
			}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
				if(null == ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert())){
					Toast.makeText(ForgetPasswordCertActivity.this, "蓝牙sim卡签名失败",Toast.LENGTH_SHORT).show();
					return;
				}			
			}else{	
				try {
					//if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
					    //initShcaCciStdService();
					
					int retCode = -1;
					if(null != gUcmSdk)
						retCode = initShcaUCMService();

					if(retCode != 0){
						 Toast.makeText(ForgetPasswordCertActivity.this, "密码分割组件初始化失败",Toast.LENGTH_SHORT).show();
						 return;
					}
				
				    //strSignCert = ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid());
			    } catch (Exception e) {
			    	ShcaCciStd.gSdk = null;
				    Toast.makeText(ForgetPasswordCertActivity.this, "证书密码错误",Toast.LENGTH_SHORT).show();
				    return;
			    }
			}
	
			new Thread() {
				@Override
				public void run() {							
					String message = String.format("%s=%s&%s=%s&%s=%s",
	                        CommonConst.PARAM_ACCOUNT_NAME_PWD,mStrAccountName,
	                        CommonConst.PARAM_APPID,CommonConst.UM_APPID,
	                        CommonConst.PARAM_BIZSN,mStrBizSN);
					byte[] signDate = null;
					
					try {	
						signAlg = 2;
						
						if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
							signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
						    if(null == signDate)						  	   	 
							   handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
						    strSign = new String(Base64.encode(signDate));						
					    }else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
							signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
						    if(null == signDate)						  	   	 
							   handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
						    strSign = new String(Base64.encode(signDate));						
					    }else{	
						    //signDate = ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());	
						    JShcaUcmStdRes jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE),CommonConst.SERT_TYPE  );
						    //signDate = ;
						    if(null == jres.response || "".equals(jres.response)){
							    handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
						    }
						
						    strSign = jres.response; //new String(Base64.encode(signDate));					
					     }
					} catch (Exception e) {
					    handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
				    }
					
					if (strSign != null) {
						boolean flag = true;
						// try {
						// flag = PKIUtil.verifySign(message, sSign,
						// cert.getCertificate());
						// } catch (Exception e) {
						// Log.e("sheca", e.getMessage(), e);
						// Toast.makeText(
						// ProviderClientTestActivity.this,
						// "验证签名错误", Toast.LENGTH_SHORT)
						// .show();
						// }

						if (flag) {
//							dialog.dismiss();
							strCert = cert.getCertificate();
							if("".equals(cert.getCerthash())){
								cert.setCerthash(sPwd);
								
								String strActName = mStrAccountName;
								certDao.updateCert(cert, strActName);
							}
		
							handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);		
									
						} else {
							Toast.makeText(
									ForgetPasswordCertActivity.this,
									"验证签名失败", Toast.LENGTH_LONG).show();
						}
					}
				}
			}.start();
		} else {
			Toast.makeText(ForgetPasswordCertActivity.this,
					"请输入证书密码", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			  case LOGIN_SIGN_FAILURE:{
				  Toast.makeText(ForgetPasswordCertActivity.this, "数字签名错误",Toast.LENGTH_SHORT).show();
			  }
			  break;
			  
			  case LOGIN_SIGN_SUCCESS:{				 
				                    try {
					                   if(verifyCertSignature()){		
					                	    Intent intent = new Intent(ForgetPasswordCertActivity.this, SetPasswordActivity.class);
						                    intent.putExtra("ActName", mStrAccountName);	
						                    if(mIsDao)
						                    	intent.putExtra("message", "dao");
					                        startActivity(intent);
					                        ForgetPasswordCertActivity.this.finish();
					                    }
				                    } catch (Exception e) {
					                     // TODO Auto-generated catch block
					                     Toast.makeText(ForgetPasswordCertActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				                     }						 
			  }
			  break;
			}
		}
	};
	
	
	private Boolean verifyCertSignature() throws Exception{
		showProgDlg("验证签名中...");
		
		String timeout = ForgetPasswordCertActivity.this.getString(R.string.WebService_Timeout);				
		String urlPath = ForgetPasswordCertActivity.this.getString(R.string.UMSP_Service_VerifyCertSignature);		
		
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("accountName", mStrAccountName);
		postParams.put("appID", CommonConst.UM_APPID);   
		postParams.put("bizSN", mStrBizSN);   
		postParams.put("signature", strSign);   
		postParams.put("certificate", strCert);   
		postParams.put("sigAlg", signAlg+"");   

		String responseStr = "";
		try {
			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			
			String postParam = "accountName="+URLEncoder.encode(mStrAccountName, "UTF-8")+
	                           "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8")+
	                           "&bizSN="+URLEncoder.encode(mStrBizSN, "UTF-8")+
	                           "&signature="+URLEncoder.encode(strSign, "UTF-8")+
	                           "&certificate="+URLEncoder.encode(strCert, "UTF-8")+
	                           "&sigAlg="+URLEncoder.encode(signAlg+"", "UTF-8");
			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		} catch (final Exception e) {
			  closeProgDlg();
			  if(e.getMessage().indexOf("peer")!=-1)
				  throw new Exception("无效的服务器请求");	
			  else				
			      throw new Exception("网络连接或访问服务异常:"+e.getMessage());
		}

		JSONObject jb = JSONObject.fromObject(responseStr);
		String resultStr = jb.getString(CommonConst.RETURN_CODE);
		String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		if (resultStr.equals("0")) {
			closeProgDlg();
			return true;
		}else{
			closeProgDlg();
			//return true;
			throw new Exception( returnStr);
		}
	}
	
	private String getSM2CertSubjectInfo(Cert cert){
		String certInfo = "";
		String certItem = "";
		
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		
		try {
			certItem = jse.getCertDetail(4, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"C="+certItem+",";
			
			certItem = jse.getCertDetail(5, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"O="+certItem+",";
			
			certItem = jse.getCertDetail(8, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"CN="+certItem+",";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(certInfo.length() > 0)
			certInfo = certInfo.substring(0,certInfo.length()-1);
		
		return certInfo;
	}
	
	private String getSM2CertIssueInfo(Cert cert){
		String certInfo = "";
		String certItem = "";
		
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		
		try {
			certItem = jse.getCertDetail(13, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"C="+certItem+",";
			
			certItem = jse.getCertDetail(18, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"ST="+certItem+",";
			
			certItem = jse.getCertDetail(16, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"L="+certItem+",";
			
			certItem = jse.getCertDetail(19, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"E="+certItem+",";
			
			certItem = jse.getCertDetail(17, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"CN="+certItem+",";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(certInfo.length() > 0)
			certInfo = certInfo.substring(0,certInfo.length()-1);
		
		return certInfo;
	}
	
	private  String   getCertName(Cert cert){
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		String strBlank = "证书";
		String strCertName = "";
		
		String commonName = "";
		try {
			commonName = jse.getCertDetail(17, bCert);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			strCertName = jse.getCertDetail(17, bCert);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			strCertName = "";
		}
		
		if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
			strCertName += CommonConst.CERT_SM2_NAME+strBlank;
		else
			strCertName += CommonConst.CERT_RSA_NAME+strBlank;
		
		/*if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()){
			strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
		}else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
			strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
		}*/
		
		if(null == cert.getCertname())
			return strCertName;
		
		if(cert.getCertname().isEmpty())
			return strCertName;
		
		if(strCertName.equals(cert.getCertname()))
			return cert.getCertname();
		
		return cert.getCertname();
	}

	private int initShcaCciStdService(){  //初始化创元中间件
		int retcode = -1;
		
		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(ForgetPasswordCertActivity.this);
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
 		
 		String myHttpBaseUrl = ForgetPasswordCertActivity.this.getString(R.string.UMSP_Base_Service);		
		myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
		
		bRan = jse.random(256, "SHA1PRNG", "SUN");
		gUcmSdk.setRandomSeed(bRan);
		//gUcmSdk.setRandomSeed(bRS);
		retcode = gUcmSdk.doInitService(myHttpBaseUrl,CommonConst.UM_APPID);
 		
     	return retcode;
	}
   
	private  String   getPWDHash(String strPWD,Cert cert){
		String strPWDHash = "";
		
		if(null != cert && (CommonConst.USE_FINGER_TYPE == cert.getFingertype())){
			if(!"".equals(cert.getCerthash())) {
				//return cert.getCerthash();
				if(!"".equals(strPWD) && strPWD.length() > 0)
					return strPWD;
			}else
			    return strPWD;
		}

		if(!"".equals(strPWD) && strPWD.length() > 0)
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
	
	private  void  showFingerCheck(){	 
        curProcess = Process.AUTH_GETREQ;
 
        String info = AuthenticatorManager.getAuthData(ForgetPasswordCertActivity.this, mStrAccountName);
        IFAAFingerprintOpenAPI.getInstance().getAuthRequestAsyn(info, callback);
        secData = info; 
	}
	
	
	private void startFPActivity(boolean isAuthenticate) {
        Intent intent = new Intent();
//        if (isAuthenticate) {
//            intent.putExtra(AuthenticatorMessage.KEY_OPERATIONT_TYPE,
//                    AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST);
//        }
        intent.setClass(this, FingerPrintAuthForgetPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        
       // this.startActivityForResult(intent, FINGER_CODE);   	
	}
	
	private  void  doFingerLogin(){
		final Cert cert = certDao.getCertByID(mCertId);
		((EditText) findViewById(R.id.textPwd)).setText(cert.getCerthash());
		
		doSign();
	}
	
	public static class FingerprintBroadcastUtil {

        //The is the broadcast for update UI status
        public final static String BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_ACTION";
        public final static String FINGERPRINTSENSOR_STATUS_VALUE            = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_VALUE";

        //Send the UI Status of the FingerPrint Result and Change the UI
        public static void sendIdentifyStatusChangeMessage(Context context, int resultCode) {
            Intent broadcastIntent = new Intent(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            broadcastIntent.putExtra(FINGERPRINTSENSOR_STATUS_VALUE, resultCode);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        public static IntentFilter getIdentifyChangeBroadcastFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            return filter;
        }

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
