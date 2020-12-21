package com.sheca.umee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.sheca.javasafeengine;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.LogDao;
import com.sheca.umee.dao.SealInfoDao;
import com.sheca.umee.event.RefreshEvent;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.ShcaCciStd;
import com.sheca.umee.presenter.CertController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.PKIUtil;
import com.sheca.umee.util.ParamGen;
import com.sheca.umee.util.SharePreferenceUtil;

import net.sf.json.JSONObject;

import org.greenrobot.eventbus.EventBus;

import java.security.KeyPair;

public class CertRenewActivity extends Activity {
    private CertDao certDao = null;
    private AccountDao mAccountDao = null;
    private LogDao logDao = null;
    private SealInfoDao mSealInfoDao = null;
    private javasafeengine jse = null;
    private int certID = 0;
    private Cert mCert = null;
    private KeyPair mKeyPair = null;

    private ProgressDialog progDialog = null;
    private EditText mOriginalPasswordView;

    protected Handler workHandler = null;
    private HandlerThread ht = null;
    //    private JShcaEsStd gEsDev = null;
//    private JShcaUcmStd gUcmSdk = null;
    //private  JShcaKsStd gKsSdk;
    private SharedPreferences sharedPrefs;

    private String mContainerid = "";
    private String strENVSN = "";
    private String strCertName = "";
    private String strInfo = "";
    private String responseStr = "";

    int certId = 0;

    CertController certController = new CertController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_renew);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("更新证书");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CertRenewActivity.this.finish();
            }
        });

        certDao = new CertDao(this);
        mAccountDao = new AccountDao(this);
        mSealInfoDao = new SealInfoDao(CertRenewActivity.this);
        logDao = new LogDao(this);
        jse = new javasafeengine();


        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertRenewActivity.this.getApplication(), CertRenewActivity.this);



        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("CertId") != null) {
                certID = Integer.parseInt(extras.getString("CertId"));
                mCert = certDao.getCertByID(certID);
                if (mCert != null) {
                    strCertName = mCert.getCertname();
                } else {
                    Toast.makeText(CertRenewActivity.this, "证书不存在", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }

        findViewById(R.id.btnRenew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRenewCert();
            }
        });

        mOriginalPasswordView = (EditText) findViewById(R.id.textCertPwd);
        mOriginalPasswordView.setText("");
        mOriginalPasswordView.requestFocus();

//        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
//            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
//            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
//
//            //mOriginalPasswordView.setText("1");
//            mOriginalPasswordView.setHint("输入蓝牙key密码");
//        } else if (CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()) {
//            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
//            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
//
//            //mOriginalPasswordView.setText("1");
//            mOriginalPasswordView.setHint("输入蓝牙sim卡密码");
//        } else {
            mOriginalPasswordView.setHint("输入证书密码");
            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
//        }

//        if (CommUtil.isPasswordLocked(CertRenewActivity.this, certID)) {
//            findViewById(R.id.btnRenew).setVisibility(android.widget.RelativeLayout.GONE);
//            return;
//        }
    }


    private void checkRenewCert() {
        mOriginalPasswordView.setError(null);

        String originalPassword = mOriginalPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查用户输入的原密码是否有效
        if (null == originalPassword) {
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
            showRenewCert();
        }

    }

    private void showRenewCert() {
        try {
            certId = mCert.getSdkID();
            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype()) || mCert.getCerttype().contains("SM2")) {


                doRenewSM2Cert(certID);


            } else {

                doRenewRSACert(certID);


            }

        } catch (Exception e) {
            Toast.makeText(CertRenewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

    }




    private void doRenewRSACert(final int certId) {
        final Cert cert = mCert;

        final String sPwd = mOriginalPasswordView.getText().toString();
        if (sPwd != null && !"".equals(sPwd)) {
            if (!PKIUtil.verifyPin(cert.getPrivatekey(), sPwd,CertRenewActivity.this)) {

                Toast.makeText(CertRenewActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();

                return;
            }

            final Handler handler = new Handler(CertRenewActivity.this.getMainLooper());
            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        //doRenewCert(handler,sPwd);
                        renewCert(handler, cert.getCertsn(), "", sPwd);
                    } catch (Exception ex) {
                        closeProgDlg();
                        Toast.makeText(CertRenewActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(CertRenewActivity.this,
                    "请输入证书密码", Toast.LENGTH_SHORT).show();
        }

    }


    private void doRenewSM2Cert(final int certId) {
        boolean bRet = true;
        final Cert cert = mCert;

        final String sPwd = mOriginalPasswordView.getText().toString();
        if (sPwd != null && !"".equals(sPwd)) {
            try {
                if (!PKIUtil.verifyPin(cert.getPrivatekey(), sPwd,CertRenewActivity.this)) {

                    Toast.makeText(CertRenewActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();

                    return;
                }

                final Handler handler = new Handler(CertRenewActivity.this.getMainLooper());
                workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //doRenewCert(handler,sPwd);
                            renewCert(handler, cert.getCertsn(), "", sPwd);
                        } catch (Exception ex) {
                            closeProgDlg();
                            Toast.makeText(CertRenewActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                ShcaCciStd.gSdk = null;
                Toast.makeText(CertRenewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(CertRenewActivity.this,
                    "请输入证书密码", Toast.LENGTH_SHORT).show();
        }
    }






    private void renewCert(final Handler handler, String certSN, String p10, final String strPwd) throws Exception {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showProgDlg("证书更新中...");
            }
        });

        int mSDk_CertId = certId;
        String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
        strInfo = ParamGen.getUpDateCertParams(mTokenId, mSDk_CertId + "", strPwd);

        String res;

        responseStr = certController.renewCert(CertRenewActivity.this, strInfo);

        final APPResponse response = new APPResponse(responseStr);
        int resultStr = response.getReturnCode();
        final String retMsg = response.getReturnMsg();

        if (0 == resultStr) {

            JSONObject jbRet = response.getResult();
            String certId = jbRet.getString("certID");
            com.sheca.umplus.model.Cert certPlus = certController.getCertDetailandSave(CertRenewActivity.this, certId);

            Cert cert = certController.convertCert(certPlus);

            if (null == certDao) {
                certDao = new CertDao(getApplicationContext());
            }
            certDao.addCert(cert, AccountHelper.getUsername(getApplicationContext()));
            certDao.deleteCert(mCert.getId());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                }
            });

            //申请成功跳入到mainActivity,更新证书
            EventBus.getDefault().post(new RefreshEvent());
            Toast.makeText(CertRenewActivity.this, "证书更新成功", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(CertRenewActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();

            Intent intent = new Intent(CertRenewActivity.this, CertResultActivity.class);
            intent.putExtra("type", 2);
            startActivity(intent);
            finish();
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                }
            });

            Toast.makeText(CertRenewActivity.this, "证书更新失败:" + retMsg, Toast.LENGTH_SHORT).show();
        }

//		String timeout = CertRenewActivity.this.getString(R.string.WebService_Timeout);
//		String urlPath = CertRenewActivity.this.getString(R.string.UMSP_Service_RenewCert);
//		Map<String,String> postParams = new HashMap<String,String>();
//		postParams.put("certSN", certSN);
//		postParams.put("p10", p10);
//    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//    	String postParam = "certSN="+URLEncoder.encode(certSN, "UTF-8")+
//		                   "&p10="+URLEncoder.encode(p10, "UTF-8");
//        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//    	return responseStr;

        /**
         * 调用sdk更新
         */
/*
		new MyAsycnTaks(){

			@Override
			public void preTask() {
//				String hashPwd = getPWDHash(sPwd, null);
				int mSDk_CertId = mCert.getSdkID();
				String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
				strInfo= ParamGen.getUpDateCertParams(mTokenId,mSDk_CertId+"",strPwd);
			}

			@Override
			public void doinBack() {
				UniTrust mUnitTrust = new UniTrust(CertRenewActivity.this, false);
				responseStr=mUnitTrust.RenewCert(strInfo);

			}

			@Override
			public void postTask() {
				final APPResponse response = new APPResponse(responseStr);
				int resultStr = response.getReturnCode();
				final String retMsg = response.getReturnMsg();

				if(0==resultStr){
					//closeProgDlg();
					//Toast.makeText(CertRenewActivity.this, "证书更新成功",Toast.LENGTH_SHORT).show();

					JSONObject jbRet = response.getResult();
					String certId = jbRet.getString("certID");
					com.sheca.umplus.model.Cert certPlus = certController.getCertDetailandSave(CertRenewActivity.this,certId);

					Cert cert = certController.convertCert(certPlus);

					if (null == certDao){
						certDao = new CertDao(getApplicationContext());
					}
					certDao.addCert(cert, AccountHelper.getUsername(getApplicationContext()));

					//申请成功跳入到mainActivity,更新证书
					Intent intent = new Intent(CertRenewActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}else{
					//closeProgDlg();
					Toast.makeText(CertRenewActivity.this, "证书更新失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
				}
			}
		}.execute();
*/
    }



    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }

    private void changeProgDlg(String strMsg) {
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
