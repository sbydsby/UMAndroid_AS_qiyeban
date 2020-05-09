package com.sheca.umandroid;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
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
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;

public class CertChangePwdActivity extends Activity {
    private CertDao certDao = null;
    private AccountDao accountDao = null;
    private javasafeengine jse = null;
    private int certID = 0;
    private Cert mCert = null;

    // UI references.
    private ProgressDialog progDialog = null;
    private EditText mOriginalPasswordView;
    private EditText mNewPasswordView;
    private EditText mNewPassword2View;

    protected Handler workHandler = null;
    private HandlerThread ht = null;

    private JShcaUcmStd gUcmSdk = null;
    private String strInfo = "";
    private String responResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_password);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("修改证书密码");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CertChangePwdActivity.this.finish();
            }
        });

        certDao = new CertDao(this);
        accountDao = new AccountDao(this);
        jse = new javasafeengine();

        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        mNewPasswordView = (EditText) findViewById(R.id.et_new_password);
        mNewPasswordView.setText("");
        mNewPassword2View = (EditText) findViewById(R.id.et_new_password2);
        mNewPassword2View.setText("");
        mOriginalPasswordView = (EditText) findViewById(R.id.et_original_password);
        mOriginalPasswordView.setText("");
        mOriginalPasswordView.requestFocus();
        mOriginalPasswordView.setFocusable(true);
        mOriginalPasswordView.setFocusableInTouchMode(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("CertId") != null) {
                certID = Integer.parseInt(extras.getString("CertId"));
                mCert = certDao.getCertByID(certID);

                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
                    ((TextView) findViewById(R.id.header_text)).setText("修改蓝牙key密码");
                    mOriginalPasswordView.setHint("原蓝牙key密码");
                    mNewPasswordView.setHint("新蓝牙key密码");
                    mNewPassword2View.setHint("再次输入新蓝牙key密码");
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()) {
                    ((TextView) findViewById(R.id.header_text)).setText("修改蓝牙sim卡密码");
                    mOriginalPasswordView.setHint("原蓝牙sim卡密码");
                    mNewPasswordView.setHint("新蓝牙sim卡密码");
                    mNewPassword2View.setHint("再次输入新蓝牙sim卡密码");
                }

            }
        }

        findViewById(R.id.btn_change_password).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        if (CommUtil.isPasswordLocked(CertChangePwdActivity.this, certID)) {
            findViewById(R.id.btn_change_password).setVisibility(android.widget.RelativeLayout.GONE);
            return;
        }

        findViewById(R.id.tv_cert_forget).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CertChangePwdActivity.this, CertResetActivity.class);
                intent.putExtra("CertId", certID + "");
                startActivity(intent);
                finish();
            }
        });
    }

    boolean hasChangePws = false;


    private void changePassword() {
        try {
            // Reset errors.
            mNewPasswordView.setError(null);

            String originalPassword = mOriginalPasswordView.getText().toString();
            String newPassword = mNewPasswordView.getText().toString();
            String newPassword2 = mNewPassword2View.getText().toString();

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
            if (!isPasswordValid(originalPassword)) {
                mOriginalPasswordView.setError(getString(R.string.password_rule));
                focusView = mOriginalPasswordView;
                cancel = true;
            }

            // 检查用户输入的新密码是否有效
            if (null == newPassword) {
                mNewPasswordView.setError(getString(R.string.password_rule));
                focusView = mNewPasswordView;
                cancel = true;
            }
            if (!isPasswordValid(newPassword)) {
                mNewPasswordView.setError(getString(R.string.password_rule));
                focusView = mNewPasswordView;
                cancel = true;
            }
            if (TextUtils.isEmpty(newPassword)) {
                mNewPasswordView.setError(getString(R.string.password_rule));
                focusView = mNewPasswordView;
                cancel = true;
            }

            if (!CommUtil.isPasswordValid(newPassword)) {
                mNewPasswordView.setError("密码强度过低，必须由8至16位英文、数字或符号组成");
                focusView = mNewPasswordView;
                cancel = true;
            }

            // 检查用户两次输入的新密码是否一致
            if (!newPassword.equals(newPassword2)) {
                mNewPasswordView.setError(getString(R.string.error_inconformity_password));
                focusView = mNewPassword2View;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt continue and focus the first form field with an error.
                focusView.requestFocus();
            } else {
                if(mCert.getFingertype()==CommonConst.USE_FINGER_TYPE) {
                    changeOldCertPwd(mCert, mOriginalPasswordView.getText().toString(),mNewPasswordView.getText().toString().trim());
                }else{
                    certId=mCert.getSdkID();
                    if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                        doChangeSM2CertPwd();
                        //根据SDK改变证书密码
                        doSDKChangePwd(mNewPasswordView.getText().toString(), mNewPasswordView.getText().toString(),certId);
                    } else {
                        doChangeCertPwd();
                        doSDKChangePwd(mOriginalPasswordView.getText().toString(), mNewPasswordView.getText().toString(),certId);
                    }



                }



            }
        } catch (Exception exc) {
            Log.e(CommonConst.TAG, exc.getMessage(), exc);
        }

    }

    /**
     * 根据SDK更改证书密码
     *
     * @param oldPwd
     * @param newPwd
     */
    private void doSDKChangePwd(final String oldPwd, final String newPwd,final  int certId) {
        new MyAsycnTaks() {

            @Override
            public void preTask() {
//				String hasholdPwd = getPWDHash(oldPwd, null);
//				String hashnewPwd = getPWDHash(newPwd, null);
                int msdkCertID = certId;
                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                strInfo = ParamGen.fixCertPassWord(mTokenId, msdkCertID + "", CommUtil.getPWDHash(oldPwd), CommUtil.getPWDHash(newPwd));
            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(CertChangePwdActivity.this, false);
                responResult = mUnitTrust.ChangeCertPWD(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    closeProgDlg();
                    CommUtil.resetPasswordLocked(CertChangePwdActivity.this, mCert.getId());

                    //if (CertChangePwdActivity.this.isFinishing()) {
                    CertChangePwdActivity.this.finish();
                    //}

                    //Toast.makeText(CertChangePwdActivity.this, "证书修改密码成功",Toast.LENGTH_SHORT).show();

                } else {
                    closeProgDlg();
                    //Toast.makeText(CertChangePwdActivity.this, "证书修改密码失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }

    public void getCertIdByCertSn(Activity context, String userName, String certSn,final  String newPwd,final  String newPwd1) {
        final UniTrust uniTrustObi = new UniTrust(context, false);
        final String param = ParamGen.getAccountCertByCertSN(context, userName, certSn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.sheca.umplus.model.Cert cert = uniTrustObi.getAccountCertByCertSN(param);
                if (null != cert) {
                    certId = cert.getId();
                    doSDKResetPwd(certId,newPwd,newPwd1);
                }
            }
        }).start();


    }

    private void doSDKResetPwd(final int certId, final String newPwd, final String newPwd1) {
        new MyAsycnTaks() {

            @Override
            public void preTask() {
//				String hasholdPwd = getPWDHash(oldPwd, null);
//				String hashnewPwd = getPWDHash(newPwd, null);
                int msdkCertID = certId;
                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                strInfo = ParamGen.ResetCertPWD(mTokenId, msdkCertID + "", CommUtil.getPWDHash(newPwd));
            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(CertChangePwdActivity.this, false);
                responResult = mUnitTrust.ResetCertPWD(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    closeProgDlg();
                    hasChangePws = true;

                    if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                        doChangeSM2CertPwd();
                        //根据SDK改变证书密码
                        doSDKChangePwd(newPwd1, newPwd1,certId);
                    } else {
                        doChangeCertPwd();
                        doSDKChangePwd(newPwd, newPwd1,certId);
                    }
                    CommUtil.resetPasswordLocked(CertChangePwdActivity.this, mCert.getId());

                    //if (CertChangePwdActivity.this.isFinishing()) {
                    //CertChangePwdActivity.this.finish();
                    //}

                    //Toast.makeText(CertChangePwdActivity.this, "证书修改密码成功",Toast.LENGTH_SHORT).show();

                } else {
                    closeProgDlg();
                    //Toast.makeText(CertChangePwdActivity.this, "证书修改密码失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }


    private void doChangeCertPwd() {
        Cert cert = mCert;
        String sKeyStore = cert.getKeystore();
        byte[] bKeyStore = Base64.decode(sKeyStore);
        ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
        ByteArrayOutputStream kos = new ByteArrayOutputStream();

        KeyStore oStore = null;
        try {
            oStore = KeyStore.getInstance("PKCS12");
            oStore.load(kis, getPWDHash(mOriginalPasswordView.getText().toString(), cert).toCharArray());

        } catch (Exception e) {
            Toast.makeText(CertChangePwdActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
            //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
            return;
        }

        try {
            oStore.store(kos, getPWDHash(mNewPasswordView.getText().toString(), cert).toCharArray());

            String newKeyStore = new String(Base64.encode(kos
                    .toByteArray()));
            cert.setKeystore(newKeyStore);
            cert.setCerthash(getPWDHash(mNewPasswordView.getText().toString(), cert));
            cert.setSdkID(certId);
            String strActName = accountDao.getLoginAccount().getName();
            if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

            certDao.updateCert(cert, strActName);

            Toast.makeText(CertChangePwdActivity.this, "修改证书密码成功", Toast.LENGTH_SHORT)
                    .show();

            //CertChangePwdActivity.this.finish();
        } catch (Exception e) {
            Toast.makeText(CertChangePwdActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    private void doChangeSM2CertPwd() {
        Cert cert = mCert;
        int retCode = -1;

        try {
            //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
            //initShcaCciStdService();
            if (null != gUcmSdk)
                retCode = initShcaUCMService();

            if (retCode != 0) {
                Toast.makeText(CertChangePwdActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }

            //int ret1 = ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
            //int ret =  ShcaCciStd.gSdk.verifyUserPin(cert.getContainerid(), getPWDHash(mOriginalPasswordView.getText().toString(),cert));
            int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash(mOriginalPasswordView.getText().toString(), cert));
            if (ret != 0) {
                Toast.makeText(CertChangePwdActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
                return;
            }

            ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                    getPWDHash(mOriginalPasswordView.getText().toString(), cert),
                    getPWDHash(mNewPasswordView.getText().toString(), cert));

            if (ret == 0) {
                Toast.makeText(CertChangePwdActivity.this, "修改证书密码成功", Toast.LENGTH_SHORT).show();

                cert.setCerthash(getPWDHash(mNewPasswordView.getText().toString(), cert));
                cert.setSdkID(certId);
                String strActName = accountDao.getLoginAccount().getName();
                if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                    strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                certDao.updateCert(cert, strActName);

                //CertChangePwdActivity.this.finish();
            } else if (ret == -13) {
                Toast.makeText(CertChangePwdActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
                return;
            } else if (ret == -1) {
                Toast.makeText(CertChangePwdActivity.this, "修改证书密码失败", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -2) {
                Toast.makeText(CertChangePwdActivity.this, "修改证书密码异常", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -3) {
                Toast.makeText(CertChangePwdActivity.this, "参数错误", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            ShcaCciStd.gSdk = null;
            Toast.makeText(CertChangePwdActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }


    private void doChangeCertPwdByBlueTooth() {
        final Cert cert = mCert;
        final int saveType = accountDao.getLoginAccount().getSaveType();
        final SharedPreferences sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Handler handler = new Handler(CertChangePwdActivity.this.getMainLooper());

        final JShcaEsStd gEsDev = JShcaEsStd.getIntence(CertChangePwdActivity.this);
        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk =  JShcaKsStd.getIntence(CertChangePwdActivity.this.getApplication(), CertChangePwdActivity.this);
        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        try {
            showProgDlg("连接设备中...");

            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    int retcode = -1;
 					/*
 					if(!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))){	 		
 					    if(!sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "").equals(cert.getDevicesn())){
 						  handler.post(new Runnable() {
							 @Override
						     public void run() {
 						        closeProgDlg();
 						        Toast.makeText(CertChangePwdActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
							  }
						  }); 

 						  return;
 					    }
 					}else{
 						 handler.post(new Runnable() {
							 @Override
						     public void run() {
 						        closeProgDlg();
 						        Toast.makeText(CertChangePwdActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
							  }
						  }); 

 						  return;
 					}
			        */
                    if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                        shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
                        if (null == devInfo) {
                            retcode = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
                            if (retcode != 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlg();
                                        Toast.makeText(CertChangePwdActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeProgDlg("修改蓝牙key密码中..");
                                Toast.makeText(CertChangePwdActivity.this, "请在蓝牙key上进行确认", Toast.LENGTH_LONG).show();
                            }
                        });

                        retcode = gEsDev.verifyUserPin(mOriginalPasswordView.getText().toString());
                        if (retcode != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(CertChangePwdActivity.this, "蓝牙key密码错误", Toast.LENGTH_SHORT).show();
                                }
                            });

                            return;
                        }

                        retcode = gEsDev.changePin(mOriginalPasswordView.getText().toString(), mNewPasswordView.getText().toString());
                    } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
                        try {
                            if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
                                ScanBlueToothSimActivity.gKsSdk.connect(cert.getDevicesn(), "778899", 500);
                        } catch (Exception ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(CertChangePwdActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                }
                            });

                            return;
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeProgDlg("修改蓝牙sim卡密码中..");
                            }
                        });

                        if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype()))
                            retcode = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInRSA(mOriginalPasswordView.getText().toString());
                        else
                            retcode = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInSM2(mOriginalPasswordView.getText().toString());

                        if (retcode != 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(CertChangePwdActivity.this, "蓝牙sim卡密码错误", Toast.LENGTH_SHORT).show();
                                }
                            });

                            return;
                        }

                        if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype()))
                            retcode = ScanBlueToothSimActivity.gKsSdk.changeUserPinInRSA(mOriginalPasswordView.getText().toString(), mNewPasswordView.getText().toString());
                        else
                            retcode = ScanBlueToothSimActivity.gKsSdk.changeUserPinInSM2(mOriginalPasswordView.getText().toString(), mNewPasswordView.getText().toString());
                    }

                    if (retcode == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(CertChangePwdActivity.this, "修改蓝牙key密码成功", Toast.LENGTH_SHORT).show();
                            }
                        });

                        cert.setCerthash(mNewPasswordView.getText().toString());
                        String strActName = accountDao.getLoginAccount().getName();
                        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                        certDao.updateCert(cert, strActName);

                        CertChangePwdActivity.this.finish();
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(CertChangePwdActivity.this, "蓝牙key密码错误", Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }
                }
            });
        } catch (final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                    Toast.makeText(CertChangePwdActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            });

            return;
        }

    }

    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertChangePwdActivity.this);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;

            if (retcode != 0)
                ShcaCciStd.gSdk = null;
        }

        return retcode;
    }

    private int initShcaUCMService() {  //初始化CA手机盾中间件
        int retcode = -1;
        byte[] bRan = null;

        String myHttpBaseUrl = CertChangePwdActivity.this.getString(R.string.UMSP_Base_Service);
        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        bRan = jse.random(256, "SHA1PRNG", "SUN");
        gUcmSdk.setRandomSeed(bRan);
        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);

        return retcode;
    }

    /**
     * 密码由8-16位英文、数字或符号组成。
     */
    private boolean isPasswordValid(String password) {
        boolean isValid = false;
        if (password.length() > 7 && password.length() < 17) {
            isValid = true;
        }
        return isValid;
    }


    private String getPWDHash1(String strPWD) {
        String strPWDHash = "";



       /* if (!"".equals(strPWD) && strPWD.length() > 0)
            return strPWD;
		*/
        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;
    }

    private String getPWDHash(String strPWD, Cert cert) {
        String strPWDHash = "";

        if (null == cert)
            return strPWD;


       /* if (!"".equals(strPWD) && strPWD.length() > 0)
            return strPWD;
		*/
        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;
    }

    int certId=0;

    private boolean changeOldCertPwd(Cert cert, String oldPwd,String newPwd) {
        if (CommonConst.USE_FINGER_TYPE == cert.getFingertype()) {
            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                int retCode = -1;

                try {
                    if (null != gUcmSdk)
                        retCode = initShcaUCMService();

                    if (retCode != 0) {
                        Toast.makeText(CertChangePwdActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash1(oldPwd));
                    if (ret != 0) {
                        Toast.makeText(CertChangePwdActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                                getPWDHash1(oldPwd),
                                getPWDHash1(oldPwd));

                        if (ret == 0) {
                            Cert newCert = cert;
                            newCert.setCerthash(getPWDHash(oldPwd, cert));
                            newCert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
                            String strActName = accountDao.getLoginAccount().getName();
                            if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                            certDao.updateCert(newCert, strActName);


//                            doSDKResetPwd(oldPwd);
                            getCertIdByCertSn(CertChangePwdActivity.this,strActName,newCert.getCertsn(),oldPwd,newPwd);
                        }
                    }

                } catch (Exception e) {

                    return false;
                }
            } else {
                Cert newCert = cert;
                String sKeyStore = cert.getKeystore();
                byte[] bKeyStore = Base64.decode(sKeyStore);
                ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
                ByteArrayOutputStream kos = new ByteArrayOutputStream();

                KeyStore oStore = null;
                try {
                    oStore = KeyStore.getInstance("PKCS12");
                    oStore.load(kis, getPWDHash1(oldPwd).toCharArray());

                } catch (Exception e) {
                    Toast.makeText(CertChangePwdActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                    //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
                    return false;
                }

                try {
                    oStore.store(kos, getPWDHash(oldPwd, cert).toCharArray());
                    String newKeyStore = new String(Base64.encode(kos.toByteArray()));
                    newCert.setKeystore(newKeyStore);
                    newCert.setCerthash(getPWDHash(oldPwd, cert));
                    newCert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);

                    String strActName = accountDao.getLoginAccount().getName();
                    if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                        strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                    certDao.updateCert(newCert, strActName);

//                    doSDKResetPwd(oldPwd);

                    getCertIdByCertSn(CertChangePwdActivity.this,strActName,newCert.getCertsn(),oldPwd, newPwd);
                    //CertChangePwdActivity.this.finish();
                } catch (Exception e) {
                    return false;
                }

            }
        } else {
            return true;
        }

        return true;
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
