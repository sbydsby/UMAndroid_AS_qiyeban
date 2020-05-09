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
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.DownloadCertResponse;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.presenter.CertController;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

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
    private JShcaEsStd gEsDev = null;
    private JShcaUcmStd gUcmSdk = null;
    //private  JShcaKsStd gKsSdk;
    private SharedPreferences sharedPrefs;

    private String mContainerid = "";
    private String strENVSN = "";
    private String strCertName = "";
    private String strInfo = "";
    private String responseStr = "";

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

        gEsDev = JShcaEsStd.getIntence(CertRenewActivity.this);
        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertRenewActivity.this.getApplication(), CertRenewActivity.this);

        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("CertId") != null) {
                certID = Integer.parseInt(extras.getString("CertId"));
                mCert = certDao.getCertByID(certID);
                strCertName = mCert.getCertname();
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

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);

            //mOriginalPasswordView.setText("1");
            mOriginalPasswordView.setHint("输入蓝牙key密码");
        } else if (CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()) {
            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);

            //mOriginalPasswordView.setText("1");
            mOriginalPasswordView.setHint("输入蓝牙sim卡密码");
        } else {
            mOriginalPasswordView.setHint("输入证书密码");
            findViewById(R.id.relativeLayout0).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.relativeLayout1).setVisibility(RelativeLayout.VISIBLE);
        }

        if (CommUtil.isPasswordLocked(CertRenewActivity.this, certID)) {
            findViewById(R.id.btnRenew).setVisibility(android.widget.RelativeLayout.GONE);
            return;
        }
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
            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {

                if (mCert.getFingertype() == CommonConst.USE_FINGER_TYPE) {
                    changeOldCertPwd(mCert, mOriginalPasswordView.getText().toString().trim(), mOriginalPasswordView.getText().toString().trim());
                } else {
                    doRenewSM2Cert(certID);
                }

            } else {
                if (mCert.getFingertype() == CommonConst.USE_FINGER_TYPE) {
                    changeOldCertPwd(mCert, mOriginalPasswordView.getText().toString().trim(), mOriginalPasswordView.getText().toString().trim());
                } else {
                    doRenewRSACert(certID);
                }

            }

        } catch (Exception e) {
            Toast.makeText(CertRenewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private boolean changeOldCertPwd(Cert cert, String oldPwd, String newPwd) {
        AccountDao accountDao = new AccountDao(this);
        if (CommonConst.USE_FINGER_TYPE == cert.getFingertype()) {
            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                int retCode = -1;

                try {
                    if (null != gUcmSdk)
                        retCode = initShcaUCMService();

                    if (retCode != 0) {
                        Toast.makeText(CertRenewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash1(oldPwd));
                    if (ret != 0) {
                        Toast.makeText(CertRenewActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                                getPWDHash1(oldPwd),
                                getPWDHash(oldPwd, cert));

                        if (ret == 0) {
                            Cert newCert = cert;
                            newCert.setCerthash(getPWDHash(oldPwd, cert));
                            newCert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
                            String strActName = accountDao.getLoginAccount().getName();
                            if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                            certDao.updateCert(newCert, strActName);


//                            doSDKResetPwd(oldPwd);
                            getCertIdByCertSn(CertRenewActivity.this, strActName, newCert.getCertsn(), oldPwd, newPwd);
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
                    Toast.makeText(CertRenewActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
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

                    getCertIdByCertSn(CertRenewActivity.this, strActName, newCert.getCertsn(), oldPwd, newPwd);
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

    int certId = 0;

    public void getCertIdByCertSn(Activity context, final String userName, String certSn, final String newPwd, final String newPwd1) {
        final UniTrust uniTrustObi = new UniTrust(context, false);
        final String param = ParamGen.getAccountCertByCertSN(context, userName, certSn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.sheca.umplus.model.Cert cert = uniTrustObi.getAccountCertByCertSN(param);
                if (null != cert) {
                    certId = cert.getId();
                    String strActName = userName;
                    mCert.setSdkID(certId);
                    certDao.updateCert(mCert, strActName);

                    doSDKResetPwd(certId, newPwd, newPwd1);
                }
            }
        }).start();


    }

    String responResult;

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
                UniTrust mUnitTrust = new UniTrust(CertRenewActivity.this, false);
                responResult = mUnitTrust.ResetCertPWD(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    closeProgDlg();
//                    hasChangePws = true;


                    if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                        doRenewSM2Cert(certID);
                    } else {
                        doRenewRSACert(certID);
                    }


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

    /**
     * 根据SDK更改证书密码
     *
     * @param oldPwd
     * @param newPwd
     */
    private void doSDKChangePwd(final String oldPwd, final String newPwd, final int certId) {
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
                UniTrust mUnitTrust = new UniTrust(CertRenewActivity.this, false);
                responResult = mUnitTrust.ChangeCertPWD(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    closeProgDlg();
                    CommUtil.resetPasswordLocked(CertRenewActivity.this, certID);

                    if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {

                        doRenewSM2Cert(certID);


                    } else {

                        doRenewRSACert(certID);


                    }

                    //if (CertChangePwdActivity.this.isFinishing()) {
//                    CertRenewActivity.this.finish();
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
        AccountDao accountDao = new AccountDao(this);
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
            Toast.makeText(CertRenewActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
            //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
            return;
        }

        try {
            oStore.store(kos, getPWDHash(mOriginalPasswordView.getText().toString(), cert).toCharArray());

            String newKeyStore = new String(Base64.encode(kos
                    .toByteArray()));
            cert.setKeystore(newKeyStore);
            cert.setCerthash(getPWDHash(mOriginalPasswordView.getText().toString(), cert));
            cert.setSdkID(certId);
            String strActName = accountDao.getLoginAccount().getName();
            if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

            certDao.updateCert(cert, strActName);

//            Toast.makeText(CertRenewActivity.this, "修改证书密码成功", Toast.LENGTH_SHORT)
//                    .show();

            //CertChangePwdActivity.this.finish();
        } catch (Exception e) {
//            Toast.makeText(CertRenewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
//                    .show();
            return;
        }
    }


    private void doChangeSM2CertPwd() {

        AccountDao accountDao = new AccountDao(this);
        Cert cert = mCert;
        int retCode = -1;

        try {
            //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
            //initShcaCciStdService();
            if (null != gUcmSdk)
                retCode = initShcaUCMService();

            if (retCode != 0) {
                Toast.makeText(CertRenewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }

            //int ret1 = ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
            //int ret =  ShcaCciStd.gSdk.verifyUserPin(cert.getContainerid(), getPWDHash(mOriginalPasswordView.getText().toString(),cert));
            int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash(mOriginalPasswordView.getText().toString(), cert));
            if (ret != 0) {
                Toast.makeText(CertRenewActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
                return;
            }

            ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                    getPWDHash(mOriginalPasswordView.getText().toString(), cert),
                    getPWDHash(mOriginalPasswordView.getText().toString(), cert));

            if (ret == 0) {
//                Toast.makeText(CertRenewActivity.this, "修改证书密码成功", Toast.LENGTH_SHORT).show();

                cert.setCerthash(getPWDHash(mOriginalPasswordView.getText().toString(), cert));
                cert.setSdkID(certId);
                String strActName = accountDao.getLoginAccount().getName();
                if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                    strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                certDao.updateCert(cert, strActName);

                //CertChangePwdActivity.this.finish();
            } else if (ret == -13) {
                Toast.makeText(CertRenewActivity.this, "原证书密码错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertChangePwdActivity.this,cert.getId());
                return;
            } else if (ret == -1) {
                Toast.makeText(CertRenewActivity.this, "修改证书密码失败", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -2) {
                Toast.makeText(CertRenewActivity.this, "修改证书密码异常", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -3) {
                Toast.makeText(CertRenewActivity.this, "参数错误", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            ShcaCciStd.gSdk = null;
            Toast.makeText(CertRenewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
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


    private void doRenewRSACert(final int certId) {
        boolean bRet = true;
        final Cert cert = mCert;

        final String sPwd = mOriginalPasswordView.getText().toString();
        if (sPwd != null && !"".equals(sPwd)) {
            final String sKeyStore = cert.getKeystore();
            byte[] bKeyStore = Base64.decode(sKeyStore);
            ByteArrayInputStream kis = new ByteArrayInputStream(
                    bKeyStore);
            KeyStore oStore = null;
            try {
                oStore = KeyStore.getInstance("PKCS12");
                oStore.load(kis, getPWDHash(sPwd, cert).toCharArray());

            } catch (Exception e) {
                Toast.makeText(CertRenewActivity.this, "证书密码错误",
                        Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertRenewActivity.this,cert.getId());
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
        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        final String sPwd = mOriginalPasswordView.getText().toString();
        if (sPwd != null && !"".equals(sPwd)) {
            try {
                //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                //initShcaCciStdService();
                int retCode = -1;
                if (null != gUcmSdk)
                    retCode = initShcaUCMService();

                if (retCode != 0) {
                    Toast.makeText(CertRenewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), getPWDHash(sPwd, cert));
                if (ret != 0) {
                    Toast.makeText(CertRenewActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                    //CommUtil.showErrPasswordMsg(CertRenewActivity.this,cert.getId());
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

    private void doRenewCertByBlueTooth(final int certId) {
        boolean bRet = true;
        final Cert cert = mCert;
        final Handler handler = new Handler(CertRenewActivity.this.getMainLooper());

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        final String sPwd = mOriginalPasswordView.getText().toString();
        final int saveType = mAccountDao.getLoginAccount().getSaveType();

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
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                            shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
                            if (null == devInfo) {
                                int ret = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
                                if (ret != 0) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgDlg();
                                            Toast.makeText(CertRenewActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            }

                            int nRet = -1;
                            nRet = gEsDev.verifyUserPin(sPwd);
                            if (nRet != 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlg();
                                        Toast.makeText(CertRenewActivity.this, "蓝牙key密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                }
                            });
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
                            if (!ScanBlueToothSimActivity.gKsSdk.isConnected()) {
                                try {
                                    ScanBlueToothSimActivity.gKsSdk.connect(cert.getDevicesn(), "778899", 500);
                                } catch (Exception ex) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgDlg();
                                            Toast.makeText(CertRenewActivity.this, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            }

                            int nRet = -1;
                            if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype()))
                                nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInRSA(sPwd);
                            else
                                nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInSM2(sPwd);

                            if (nRet != 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlg();
                                        Toast.makeText(CertRenewActivity.this, "蓝牙sim卡密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                }
                            });
                        }

                        try {
                            doRenewCert(handler, sPwd);
                        } catch (Exception ex) {
                            closeProgDlg();
                            Toast.makeText(CertRenewActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeProgDlg();
                        Toast.makeText(CertRenewActivity.this, "蓝牙key密码错误", Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }
        } else {
            Toast.makeText(CertRenewActivity.this, "请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
        }
    }


    private void doRenewCert(final Handler handler, final String sPwd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showProgDlg("证书更新中...");
            }
        });

        try {

            String responseStr = "";

            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
                responseStr = UploadSM2Pkcs10(handler, mCert.getCertsn(), mCert.getCerttype(), mCert.getSavetype(), mCert.getStatus(), sPwd);
            else
                responseStr = UploadPkcs10(handler, mCert.getCertsn(), mCert.getCerttype(), mCert.getSavetype(), mCert.getStatus(), sPwd);

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            String returnStr = jb.getString(CommonConst.RETURN_MSG);
            final String strErr = returnStr;
            int certSaveType = CommonConst.SAVE_CERT_TYPE_RSA;

            if (resultStr.equals("0")) {
                setCertRenewStatus(mCert);

                //调用UMSP服务：下载证书
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        changeProgDlg("证书下载中...");
                    }
                });

                JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                strENVSN = jbRet.getString(CommonConst.RESULT_PARAM_REQUEST_NUMBER);   //记录ENVSN

                //设置时间间隔，等待后台签发证书
                String threadSleepTime = CertRenewActivity.this.getString(R.string.Thread_Sleep);
                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                    certSaveType = CommonConst.SAVE_CERT_TYPE_SM2;
                    Thread.sleep(Long.parseLong(threadSleepTime) * 2);   //签发sm2证书等待时间需10秒
                } else {
                    certSaveType = CommonConst.SAVE_CERT_TYPE_RSA;
                    Thread.sleep(Long.parseLong(threadSleepTime));
                }

                //下载证书
                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
                    responseStr = DownloadSM2Cert(strENVSN, mCert.getSavetype(), mCert.getCerttype());
                else
                    responseStr = DownloadCert(strENVSN, mCert.getSavetype(), mCert.getCerttype());

                jb = JSONObject.fromObject(responseStr);
                resultStr = jb.getString(CommonConst.RETURN_CODE);
                returnStr = jb.getString(CommonConst.RETURN_MSG);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeProgDlg();
                    }
                });

                if (resultStr.equals("0")) {
                    jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));

                    DownloadCertResponse dcResponse = new DownloadCertResponse();
                    dcResponse.setReturn(returnStr);
                    dcResponse.setResult(resultStr);
                    dcResponse.setUserCert(jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT));
                    dcResponse.setEncCert(jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT));
                    dcResponse.setEncKey(jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT));
                    dcResponse.setCertChain(jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN));
                    dcResponse.setEncAlgorithm(jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG));

                    final DownloadCertResponse fDcResponse = dcResponse;
                    final int actCertType = certSaveType;
                    //UI处理必须放在主线程
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //保存证书到本地
                            saveCert(strENVSN, fDcResponse, mCert.getSavetype(), actCertType, mCert, sPwd);
                        }
                    });
                } else {
                    throw new Exception("调用UMSP服务之DownloadCert失败：" + resultStr + "，" + returnStr);
                }
            } else if (resultStr.equals("10012")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        changeProgDlg("登录中...");
                        loginUMSPService(mAccountDao.getLoginAccount().getName());
                        closeProgDlg();
                        showRenewCert();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeProgDlg();
                        Toast.makeText(CertRenewActivity.this, strErr, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                }
            });

            Toast.makeText(CertRenewActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
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
        strInfo = ParamGen.getUpDateCertParams(mTokenId, mSDk_CertId + "", CommUtil.getPWDHash(strPwd));

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
            Toast.makeText(CertRenewActivity.this, "证书更新成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CertRenewActivity.this, MainActivity.class);
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


    private String UploadPkcs10(final Handler handler, String certSN, String certType, int saveType, int certStatus, final String certPwd) throws Exception {
        String p10 = "";
        String responseStr = "";

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        Cert cert = certDao.getCertByCertsn(certSN, strActName);

        if (Cert.STATUS_RENEW_CERT == certStatus) {
            p10 = "";
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    changeProgDlg("生成P10中...");
                }
            });

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                p10 = genPkcs10ByBlueTooth(getCertCN(cert), certPwd);
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
                p10 = genPkcs10ByBlueToothSim(getCertCN(cert), certPwd);
            } else {
                p10 = genPkcs10(getCertCN(cert));
            }
        }

        //responseStr = renewCert(handler,cert.getCertsn(),p10,certPwd);

        return responseStr;
    }

    private String UploadSM2Pkcs10(final Handler handler, String certSN, String certType, int saveType, int certStatus, final String certPwd) throws Exception {
        String p10 = "";
        String responseStr = "";

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        Cert cert = certDao.getCertByCertsn(certSN, strActName);

        if (Cert.STATUS_RENEW_CERT == certStatus) {
            p10 = "";
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    changeProgDlg("生成P10中...");
                }
            });

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                p10 = genSM2Pkcs10ByBlueTooth(getCertCN(cert), certPwd);
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
                p10 = genSM2Pkcs10ByBlueToothSim(getCertCN(cert), certPwd);
            } else {
                p10 = genSM2Pkcs10(getCertCN(cert));
            }
        }

        //responseStr = renewCert(handler,cert.getCertsn(),p10,certPwd);

        return responseStr;
    }


    private String genPkcs10(String PersonName) throws Exception {
        String p10 = "";

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(CommonConst.CERT_MOUDLE_SIZE);
            mKeyPair = keyGen.genKeyPair();

            String dn = "CN=" + PersonName;
            X500Principal subjectName = new X500Principal(dn);
            org.spongycastle.jce.PKCS10CertificationRequest kpGen = new org.spongycastle.jce.PKCS10CertificationRequest(
                    CommonConst.CERT_ALG_RSA, subjectName, mKeyPair.getPublic(), null, mKeyPair.getPrivate());

            p10 = new String(Base64.encode(kpGen.getEncoded()));

        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if ("".equals(p10))
            throw new Exception("生成P10失败");

        return p10;
    }


    private String genPkcs10ByBlueTooth(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
            if (null == devInfo)
                gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
                nRet = gEsDev.detroyRSASignCert(certPwd, CommonConst.CERT_MOUDLE_SIZE);

            p10 = gEsDev.genRSAPKCS10(dn, certPwd, CommonConst.CERT_MOUDLE_SIZE);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙key生成P10失败");

        return p10;
    }

    private String genPkcs10ByBlueToothSim(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
                ScanBlueToothSimActivity.gKsSdk.connect(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), "778899", 500);

            if (null != ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert()))
                nRet = ScanBlueToothSimActivity.gKsSdk.detroyRSAKeyPairAndCert(certPwd);

            p10 = ScanBlueToothSimActivity.gKsSdk.genRSAPKCS10(dn, certPwd);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙sim卡生成P10失败");

        return p10;
    }


    private String genSM2Pkcs10(String PersonName) throws Exception {
        String p10 = "";
          
	      /*
		  try{
			  if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
					initShcaCciStdService();
			  
			  shcaCciStdGenKeyPairRes r = null;
							
			  if(ShcaCciStd.gSdk != null && ShcaCciStd.errorCode == 0){
					r = ShcaCciStd.gSdk.genSM2KeyPair(CommonConst.JSHECACCISTD_PWD);
					//Thread.sleep(Long.parseLong(ResultActivity.this.getString(R.string.Thread_Sleep)));
						
					if(r != null && r.retcode == 0){					
						//String dn = "CN=" + PersonName+",OU=Test,C=CN,ST=SH,O=Sheca";
						String dn = "CN=" + PersonName;
						
						byte[] bPubkey = android.util.Base64.decode(r.pubkey, android.util.Base64.NO_WRAP);
						p10 = ShcaCciStd.gSdk.getSM2PKCS10(dn, bPubkey, CommonConst.JSHECACCISTD_PWD, r.containerID);	
						//Thread.sleep(Long.parseLong(ResultActivity.this.getString(R.string.Thread_Sleep)));
						mContainerid = r.containerID;
					}
				}
			}catch(Exception ex){
				ShcaCciStd.gSdk = null;
				throw new Exception(ex.getMessage());
			}
	       */

        int retCode = 0;

        if (retCode == 0) {
            String myCid = "";
            JShcaUcmStdRes jres = null;

            jres = gUcmSdk.genSM2KeyPairWithPin(CommonConst.JSHECACCISTD_PWD);

            if (jres.retCode == 0) {
                myCid = jres.containerid;
                String dn = "CN=" + PersonName;
                jres = gUcmSdk.genSM2PKCS10WithCID(myCid, CommonConst.JSHECACCISTD_PWD, dn);

                if (jres.retCode == 0) {
                    p10 = jres.response;
                    mContainerid = myCid;
                }
            }
        }

        if ("".equals(p10))
            throw new Exception("密码分割组件初始化失败");

        return p10;
    }

    private String genSM2Pkcs10ByBlueTooth(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            if (null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
                nRet = gEsDev.detroySM2SignCert(certPwd);
            if (null != gEsDev.readSM2EncryptCert() && !"".equals(gEsDev.readSM2EncryptCert()))
                nRet = gEsDev.detroySM2EncryptCert(certPwd);

            p10 = gEsDev.genSM2PKCS10(dn, certPwd);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙key生成P10失败");

        return p10;
    }

    private String genSM2Pkcs10ByBlueToothSim(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            if (null != ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert()))
                nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(certPwd);

            p10 = ScanBlueToothSimActivity.gKsSdk.genSM2PKCS10(dn, certPwd);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙sim卡生成P10失败");

        return p10;
    }


    private String DownloadCert(String requestNumber, int saveType, String certType) throws Exception {
        String timeout = CertRenewActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = CertRenewActivity.this.getString(R.string.UMSP_Service_DownloadCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8");
        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

        boolean isSavedCert = false;
        Cert cert = new Cert();
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), mAccountDao.getLoginAccount().getName(), certType);
            if (null == cert) {
                cert = new Cert();
                isSavedCert = false;
            } else {
                isSavedCert = true;
                Cert encCert = certDao.getCertByEnvsn(cert.getEnvsn() + "-e", mAccountDao.getLoginAccount().getName());
                if (null != encCert)
                    certDao.deleteCert(encCert.getId());
            }
        }

        cert.setEnvsn(requestNumber);
        cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
        cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
        cert.setCerttype(CommonConst.CERT_TYPE_RSA);
        cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
        cert.setSignalg(1);
        cert.setContainerid("");

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
            cert.setPrivatekey("");
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
            cert.setPrivatekey("");
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        } else {
            cert.setPrivatekey(new String(Base64.encode(mKeyPair.getPrivate().getEncoded())));
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
            cert.setDevicesn(android.os.Build.SERIAL);

            certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        }

        return responseStr;
    }

    private String DownloadSM2Cert(String requestNumber, int saveType, String certType) throws Exception {
        String timeout = CertRenewActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = CertRenewActivity.this.getString(R.string.UMSP_Service_DownloadCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8");
        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

        boolean isSavedCert = false;
        Cert cert = new Cert();
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), mAccountDao.getLoginAccount().getName(), certType);
            if (null == cert) {
                cert = new Cert();
                isSavedCert = false;
            } else {
                isSavedCert = true;
                Cert encCert = certDao.getCertByEnvsn(cert.getEnvsn() + "-e", mAccountDao.getLoginAccount().getName());
                if (null != encCert)
                    certDao.deleteCert(encCert.getId());
            }
        }

        cert.setEnvsn(requestNumber);
        cert.setPrivatekey("");
        cert.setCerttype(CommonConst.CERT_TYPE_SM2);
        cert.setSignalg(2);
        cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
        cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
        cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setContainerid("");
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert.setContainerid("");
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        } else {
            cert.setContainerid(mContainerid);
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
            cert.setDevicesn(android.os.Build.SERIAL);

            certDao.addCert(cert, mAccountDao.getLoginAccount().getName());
        }

        return responseStr;
    }


    private void saveCert(final String requestNumber, final DownloadCertResponse response, final int saveType, final int certType, final Cert mCert, final String prikeyPassword) {
        try {
            if (CommonConst.SAVE_CERT_TYPE_RSA == certType)
                uploadCertStatus(requestNumber, response, prikeyPassword, saveType, mCert);
            else
                uploadSM2CertStatus(requestNumber, response, prikeyPassword, saveType, mCert);
        } catch (Exception e) {
            closeProgDlg();

            e.printStackTrace();
        }
    }


    private void uploadCertStatus(final String requestNumber, final DownloadCertResponse response, final String prikeyPassword, final int saveType, final Cert mCert) throws Exception {
        showProgDlg("证书保存中...");

        String userCert = response.getUserCert();
        String certChain = response.getCertChain();
        Cert cert = certDao.getCertByEnvsn(requestNumber, mAccountDao.getLoginAccount().getName());
        cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
        cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
        byte[] bCert = Base64.decode(userCert);
        javasafeengine jse = new javasafeengine();
        Certificate oCert = jse.getCertFromBuffer(bCert);
        X509Certificate oX509Cert = (X509Certificate) oCert;
        cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));
        cert.setCertchain(certChain);
        cert.setNotbeforetime(getCertNotbeforetime(userCert));
        cert.setValidtime(getCertValidtime(userCert));
        cert.setCertname(strCertName);
        cert.setSealsn("");
        cert.setSealstate(Cert.STATUS_NO_SEAL);
        //cert.setPrivatekey("");

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setCertificate(userCert);
            cert.setKeystore("");
            cert.setPrivatekey("");
            cert.setCerthash(prikeyPassword);
            cert.setFingertype(CommonConst.USE_FINGER_TYPE);

            int retcode = -1;
            retcode = gEsDev.saveRSASignatureCert(prikeyPassword, userCert);
        } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert.setCertificate(userCert);
            cert.setKeystore("");
            cert.setPrivatekey("");
            cert.setCerthash(prikeyPassword);
            cert.setFingertype(CommonConst.USE_FINGER_TYPE);

            int retcode = -1;
            retcode = ScanBlueToothSimActivity.gKsSdk.saveRSASignatureCert(prikeyPassword, userCert);
        } else {
            cert.setCertificate(userCert);
            String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
            cert.setKeystore(p12);
            cert.setPrivatekey("");
            cert.setCerthash(prikeyPassword);
            cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
        }

        //showMessage("2");
        certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
        certDao.deleteCert(mCert.getId());
        SealInfo sealInfo = mSealInfoDao.getSealByCertsn(mCert.getCertsn(), mAccountDao.getLoginAccount().getName());
        if (null != sealInfo)
            mSealInfoDao.deleteSeal(sealInfo.getId());
        //Toast.makeText(ResultActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();

        //showMessage("3");
        saveLog(OperationLog.LOG_TYPE_RENEWCERT, cert.getCertsn(), "", "", "");
        //showMessage("4");
        final Handler handler = new Handler(CertRenewActivity.this.getMainLooper());
        //网络访问必须放在子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //调用UMSP服务：设置证书保存成功状态
                    String responseStr = SetSuccessStatus(requestNumber, saveType);
                    JSONObject jb = JSONObject.fromObject(responseStr);
                    String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    String returnStr = jb.getString(CommonConst.RETURN_MSG);
                    if (resultStr.equals("0")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(CertRenewActivity.this, "证书更新成功", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CertRenewActivity.this, MainActivity.class);
                                startActivity(intent);
                                // activity.finish();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                            }
                        });

                        throw new Exception("证书更新失败："  + returnStr);
                    }
                } catch (Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });
                    //Toast.makeText(context, exc.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

    }

    private void uploadSM2CertStatus(final String requestNumber, final DownloadCertResponse response, final String prikeyPassword, final int saveType, final Cert mCert) throws Exception {
        showProgDlg("证书保存中...");

        String userCert = response.getUserCert();
        String certChain = response.getCertChain();
        String encCert = response.getEncCert();
        String encKeystore = response.getEncKey();

        int retcode = -1;

        if (null == encCert)
            encCert = "";
        if (null == encKeystore)
            encKeystore = "";
		/*	
		String userCert = "MIIDwzCCA2agAwIBAgIQd0gGJqSaAkIAFnG4Fb5GyjAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEBVrkqL1OLoNKol29i/Bvw2+rkyq0UelOP/FhEUVBkHuNO8h/YV8AYbHIRmoEBNVYzlHvmPiJ8mp4rEw1Bo1kKOCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBT5aslV8UJupOSIlYFBpq57mYDjBTAOBgNVHQ8BAf8EBAMCBsAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANJADBGAiEA81NRuSndplECK2+MPAh6IWYzQqwwWuNw9/YueSMlGfcCIQDiVn92cAwffhVBZ4vwTPQ01Gr30KvnkHL22ezyJKHenA==";
		String encCert = "MIIDwTCCA2agAwIBAgIQeYJqWnw8IHOKl55VjiyTUTAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEsyKg0zrs61met8qbSo591/Dp5olRV+22c4BjIdrF/k3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztg6OCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBQIvR69sN+jKaw0V8npb057mLrzQTAOBgNVHQ8BAf8EBAMCAzgwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANHADBEAiAZW2ykFLR4GmFO3eDzyV5IQb6Wbftib/dJUaAFthtCXwIgRndEpjqh4n1D7c21JLfyAfr8snB14LRSr7tS5tFzx/k=";
		String encKeystore = "AQAAAAEBAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABhlaYF5AoBV+JrqirNdss3OWWNofO91l6CDRLbAL8+nwABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABLMioNM67OtZnrfKm0qOfdfw6eaJUVfttnOAYyHaxf5AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztgwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAArf3K6UhMb2NFtG6XGNQRxZKeLSZbK0bFviC/XmfSEk4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAOWnbCvF0Qdq4RFVMf+roWtG9M4TKjE5Kt6hyatGdkU+yMWG1A0m4I83jAkLY3gxaQBWMS9FDggTRI8sV3llqBYQAAAAZuIhfMG87/3T/1OSe3w0ew==";
		String certChain = "MIIEiAYJKoZIhvcNAQcCoIIEeTCCBHUCAQExADALBgkqhkiG9w0BBwGgggRdMIIBpzCCAUugAwIBAgICAIEwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zODEyMzEwMDAwMDBaMDcxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDEVMBMGA1UEAwwMVUNBIFJvb3QgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEnLsOejX5v0nI1BsH6Glz/+ui/Uge27gmxsIemVDmOxKjs0Hp9ZPbqzXajUoYp9Rlcf6BmoVe02Y12ZvRHMBCU6NFMEMwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wIAYDVR0OAQEABBYEFO7osJzV3Oxz/e98+lAsxsFA5kyzMAwGCCqBHM9VAYN1BQADSAAwRQIhAJqydZmPsiPSBBWmD8bTLBXBnvDhUv4xp81GCNCBh+L+AiAoulB2Q7LIe0zFaRl1liJ9QH8NaZtI1I7eOGC8Z9gUvjCCAq4wggJRoAMCAQICEF17yetm9O3ri9K6qQyPciMwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zNzEyMzEwMDAwMDBaMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEfdEfkS0GSlQQ8ISEVSUdvKL7tcd3bsNssWlmmOhN5VCg1iLJgMDDqhO9TFt4EDsZuvECXz8uiU+BL4pddBcMgKOCAT4wggE6MEQGA1UdIAEBAAQ6MDgwNgYIKoEchu86gRUwKjAoBggrBgEFBQcCARYcaHR0cDovL3d3dy5zaGVjYS5jb20vcG9saWN5LzAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADA9BgNVHR8BAQAEMzAxMC+gLaArhilodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20yc3ViLmNybDAiBgNVHSMBAQAEGDAWgBTu6LCc1dzsc/3vfPpQLMbBQOZMszAgBgNVHQ4BAQAEFgQUiTEEkXtDqqqav4Qdm4bu8LhwmaAwSQYIKwYBBQUHAQEBAQAEOjA4MDYGCCsGAQUFBzAChipodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20ycm9vdC5kZXIwDAYIKoEcz1UBg3UFAANJADBGAiEAmFStsqFTAiEmqQUDR+0QXwTUgJYhNZicXfaGtuyKhF0CIQCDONwlcY/av+yWE+3+VVqzmiBnLKw6QnyHvLkNnEYH9DEA";								
        */

        Cert cert = certDao.getCertByEnvsn(requestNumber, mAccountDao.getLoginAccount().getName());
        cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
        cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
        cert.setKeystore("");
        cert.setPrivatekey("");
        cert.setCertsn(getCertSN(userCert));
        cert.setNotbeforetime(getCertNotbeforetime(userCert));
        cert.setValidtime(getCertValidtime(userCert));
        cert.setCertchain(certChain);
        cert.setCertname(strCertName);
        cert.setCerthash(prikeyPassword);
        cert.setSealsn("");
        cert.setSealstate(Cert.STATUS_NO_SEAL);

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setCertificate(userCert);
            cert.setEnccertificate(encCert);
            cert.setEnckeystore(encKeystore);
            cert.setFingertype(CommonConst.USE_FINGER_TYPE);

            if (!"".equals(encCert))
                retcode = gEsDev.saveSM2DoubleCert(prikeyPassword, userCert, encCert, encKeystore);
            else
                retcode = gEsDev.saveSM2DoubleCert(prikeyPassword, userCert, "", encKeystore);

            if (retcode == 0) {
                if (!"".equals(encCert)) {
                    if (null == certDao.getCertByEnvsn(requestNumber + "-e", mAccountDao.getLoginAccount().getName())) {
                        Cert certEnc = new Cert();
                        certEnc.setEnvsn(requestNumber + "-e");
                        certEnc.setPrivatekey("");
                        certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                        certEnc.setCerttype(cert.getCerttype());
                        certEnc.setSignalg(cert.getSignalg());
                        certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
                        certEnc.setContainerid("");
                        certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
                        certEnc.setCertificate(encCert);
                        certEnc.setCertchain(certChain);
                        certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
                        certEnc.setValidtime(getCertValidtime(encCert));
                        certEnc.setKeystore("");
                        certEnc.setEnccertificate(encCert);
                        certEnc.setEnckeystore(encKeystore);
                        certEnc.setCertsn(getCertSN(encCert));
                        certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
                        certEnc.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
                        certEnc.setCertname(strCertName);
                        certEnc.setCerthash(prikeyPassword);
                        certEnc.setFingertype(CommonConst.USE_FINGER_TYPE);
                        certEnc.setSealsn("");
                        certEnc.setSealstate(Cert.STATUS_NO_SEAL);


                        certDao.addCert(certEnc, mAccountDao.getLoginAccount().getName());
                    }
                }
            }
        } else if (CommonConst.SAVE_CERT_TYPE_SIM == saveType) {
            cert.setCertificate(userCert);
            cert.setEnccertificate(encCert);
            cert.setEnckeystore(encKeystore);
            cert.setFingertype(CommonConst.USE_FINGER_TYPE);

            if (!"".equals(encCert))
                retcode = ScanBlueToothSimActivity.gKsSdk.saveSM2DoubleCert(prikeyPassword, userCert, encCert, encKeystore);
            else
                retcode = ScanBlueToothSimActivity.gKsSdk.saveSM2DoubleCert(prikeyPassword, userCert, "", encKeystore);

            if (retcode == 0) {
                if (!"".equals(encCert)) {
                    if (null == certDao.getCertByEnvsn(requestNumber + "-e", mAccountDao.getLoginAccount().getName())) {
                        Cert certEnc = new Cert();
                        certEnc.setEnvsn(requestNumber + "-e");
                        certEnc.setPrivatekey("");
                        certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                        certEnc.setCerttype(cert.getCerttype());
                        certEnc.setSignalg(cert.getSignalg());
                        certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
                        certEnc.setContainerid("");
                        certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
                        certEnc.setCertificate(encCert);
                        certEnc.setCertchain(certChain);
                        certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
                        certEnc.setValidtime(getCertValidtime(encCert));
                        certEnc.setKeystore("");
                        certEnc.setEnccertificate(encCert);
                        certEnc.setEnckeystore(encKeystore);
                        certEnc.setCertsn(getCertSN(encCert));
                        certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
                        certEnc.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
                        certEnc.setCertname(strCertName);
                        certEnc.setCerthash(prikeyPassword);
                        certEnc.setFingertype(CommonConst.USE_FINGER_TYPE);
                        certEnc.setSealsn("");
                        certEnc.setSealstate(Cert.STATUS_NO_SEAL);

                        certDao.addCert(certEnc, mAccountDao.getLoginAccount().getName());
                    }
                }
            }
        } else {
            cert.setCertificate(userCert);
            cert.setEnccertificate(encCert);
            cert.setEnckeystore(encKeystore);
            cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);

            //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
            //initShcaCciStdService();

            // if(!"".equals(encCert))
            //retcode = ShcaCciStd.gSdk.saveSM2DoubleCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert, encCert, encKeystore);
            // else
            //retcode = ShcaCciStd.gSdk.saveSM2SignatureCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert);
            retcode = 0;
            if (retcode == 0)
                retcode = gUcmSdk.changeUserPinWithCID(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, prikeyPassword);
            if (retcode == 0) {
                if (!"".equals(encCert)) {
                    if (null == certDao.getCertByEnvsn(requestNumber + "-e", mAccountDao.getLoginAccount().getName())) {
                        Cert certEnc = new Cert();
                        certEnc.setEnvsn(requestNumber + "-e");
                        certEnc.setPrivatekey("");
                        certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                        certEnc.setCerttype(cert.getCerttype());
                        certEnc.setSignalg(cert.getSignalg());
                        certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
                        certEnc.setContainerid(cert.getContainerid());
                        certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
                        certEnc.setCertificate(encCert);
                        certEnc.setCertchain(certChain);
                        certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
                        certEnc.setValidtime(getCertValidtime(encCert));
                        certEnc.setKeystore("");
                        certEnc.setEnccertificate(encCert);
                        certEnc.setEnckeystore(encKeystore);
                        certEnc.setCertsn(getCertSN(encCert));
                        certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
                        certEnc.setDevicesn(android.os.Build.SERIAL);
                        certEnc.setCertname(strCertName);
                        certEnc.setCerthash(prikeyPassword);
                        certEnc.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
                        certEnc.setSealsn("");
                        certEnc.setSealstate(Cert.STATUS_NO_SEAL);

                        certDao.addCert(certEnc, mAccountDao.getLoginAccount().getName());
                    }
                }
            }
        }
		/*byte[] bCert = Base64.decode(userCert);
		javasafeengine jse = new javasafeengine();
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));	
		String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
		cert.setKeystore(p12);
		cert.setPrivatekey("");
		*/
        //showMessage("2");

        certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
        certDao.deleteCert(mCert.getId());
        SealInfo sealInfo = mSealInfoDao.getSealByCertsn(mCert.getCertsn(), mAccountDao.getLoginAccount().getName());
        if (null != sealInfo)
            mSealInfoDao.deleteSeal(sealInfo.getId());

        Cert oldEncCert = certDao.getCertByEnvsn(mCert.getEnvsn() + "-e", mAccountDao.getLoginAccount().getName());
        if (null != oldEncCert)
            certDao.deleteCert(oldEncCert.getId());

        //Toast.makeText(ResultActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();

        //showMessage("3");
        saveLog(OperationLog.LOG_TYPE_RENEWCERT, cert.getCertsn(), "", "", "");
        //showMessage("4");
        final Handler handler = new Handler(CertRenewActivity.this.getMainLooper());
        //网络访问必须放在子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //调用UMSP服务：设置证书保存成功状态
                    String responseStr = SetSuccessStatus(requestNumber, saveType);
                    JSONObject jb = JSONObject.fromObject(responseStr);
                    String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    String returnStr = jb.getString(CommonConst.RETURN_MSG);
                    if (resultStr.equals("0")) {
                        //Toast.makeText(ResultActivity.this, "下载证书成功", Toast.LENGTH_LONG).show();
                        Cert cert = certDao.getCertByEnvsn(requestNumber, mAccountDao.getLoginAccount().getName());
                        if (null != cert) {
                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                            certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
                        }

                        cert = certDao.getCertByEnvsn(requestNumber + "-e", mAccountDao.getLoginAccount().getName());
                        if (null != cert) {
                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                            certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(CertRenewActivity.this, "证书更新成功", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CertRenewActivity.this, MainActivity.class);
                                startActivity(intent);
                                //activity.finish();
                            }
                        });

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                            }
                        });

                        throw new Exception("证书更新失败：" + returnStr);
                    }
                } catch (Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });
                    Toast.makeText(CertRenewActivity.this, exc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

    }


    private String SetSuccessStatus(final String requestNumber, final int saveType) throws Exception {
        String timeout = CertRenewActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = CertRenewActivity.this.getString(R.string.UMSP_Service_SetSuccessStatus);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        postParams.put("clientOSType", "1");  //客户端操作系统类型（1：Android；2：IOS；3：WP）
        postParams.put("clientOSDesc", getOSInfo());  //客户端操作系统描述
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
            postParams.put("media", "2");   //证书存储介质类型（1：文件；2：SD卡）
        else
            postParams.put("media", "1");   //证书存储介质类型（1：文件；2：SD卡）

        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "";
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
            postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8") +
                    "&clientOSType=" + URLEncoder.encode("1", "UTF-8") +
                    "&clientOSDesc=" + URLEncoder.encode(getOSInfo(), "UTF-8") +
                    "&media=" + URLEncoder.encode("2", "UTF-8");
        else
            postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8") +
                    "&clientOSType=" + URLEncoder.encode("1", "UTF-8") +
                    "&clientOSDesc=" + URLEncoder.encode(getOSInfo(), "UTF-8") +
                    "&media=" + URLEncoder.encode("1", "UTF-8");

        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
        CommUtil.resetPasswordLocked(CertRenewActivity.this, mCert.getId());

        return responseStr;
    }


    private String genP12(String privateKey, String pin, String cert, String chain) throws Exception {
        String p12 = "";
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream certBIn = new ByteArrayInputStream(
                Base64.decode(cert));
        Certificate certificate = cf.generateCertificate(certBIn);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ByteArrayInputStream bIn = new ByteArrayInputStream(
                Base64.decode(chain));
        CertPath oCertPath = cf.generateCertPath(bIn, "PKCS7");
        List certs = oCertPath.getCertificates();
        Certificate[] bChain = (Certificate[]) certs
                .toArray(new Certificate[certs.size() + 1]);
        bChain[certs.size()] = certificate;

        List certList = new ArrayList();
        for (Certificate c : bChain) {
            certList.add(c);
        }
        Collections.reverse(certList);
        bChain = (Certificate[]) certList.toArray(new Certificate[certList
                .size()]);

        KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(
                Base64.decode(privateKey));
        RSAPrivateKey privKey = (RSAPrivateKey) rsaKeyFac
                .generatePrivate(encodedKeySpec);
        ks.setKeyEntry("", privKey, pin.toCharArray(), bChain);

        ByteArrayOutputStream outp12 = new ByteArrayOutputStream();

        ks.store(outp12, pin.toCharArray());
        p12 = new String(Base64.encode(outp12.toByteArray()));
        outp12.close();
        return p12;
    }

    private void setCertRenewStatus(Cert cert) {
        cert.setStatus(Cert.STATUS_RENEW_CERT);
        certDao.updateCert(cert, mAccountDao.getLoginAccount().getName());
    }

    private void saveLog(int type, String certsn, String message, String invoker, String sign) {
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

        logDao.addLog(log, mAccountDao.getLoginAccount().getName());
    }

    private String getOSInfo() {
        String strOSInfo = "";

        strOSInfo = "硬件型号:" + android.os.Build.MODEL + "|操作系统版本号:"
                + android.os.Build.VERSION.RELEASE;
        return strOSInfo;
    }


    private String getCertCN(Cert cert) {
        String commonName = "";

        byte[] bCert = Base64.decode(cert.getCertificate());
        try {
            commonName = jse.getCertDetail(17, bCert);
        } catch (Exception ex) {
            commonName = "";
        }

        return commonName;
    }

    private String getCertSN(String strCert) {
        String strCertSN = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strCertSN = jse.getCertDetail(2, bCert);
        } catch (Exception e) {

        }

        return strCertSN;
    }

    private String getCertNotbeforetime(String strCert) {
        String strNotBeforeTime = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strNotBeforeTime = jse.getCertDetail(11, bCert);
        } catch (Exception e) {

        }

        return strNotBeforeTime;
    }

    private String getCertValidtime(String strCert) {
        String strValidTime = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strValidTime = jse.getCertDetail(12, bCert);
        } catch (Exception e) {

        }

        return strValidTime;
    }

    private Boolean loginUMSPService(String act) {    //重新登录UM Service
        String returnStr = "";
        try {
            //异步调用UMSP服务：用户登录
            String timeout = CertRenewActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = CertRenewActivity.this.getString(R.string.UMSP_Service_Login);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", act);
            postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword(), null));    //账户口令需要HASH并转为BASE64字符串
            if (mAccountDao.getLoginAccount().getType() == 1)
                postParams.put("appID", CommonConst.UM_APPID);
            else
                postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());

            String responseStr = "";
            try {
                //清空本地缓存
                WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String actpwd = "";
                if (mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
                    actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword(), null);
                else
                    actpwd = mAccountDao.getLoginAccount().getPassword();

                String postParam = "";
                if (mAccountDao.getLoginAccount().getType() == 1)
                    postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                            "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
                            "&appID=" + URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
                else
                    postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                            "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
                            "&appID=" + URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");

                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
            } catch (Exception e) {
                if (null == e.getMessage())
                    throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
                else
                    throw new Exception("用户登录失败：" + e.getMessage() + " 请重新点击登录");
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

        } catch (Exception exc) {
            return false;
        }

        return true;
    }

    private String getPWDHash(String strPWD, Cert cert) {
        String strPWDHash = "";

        if (null == cert)
            return strPWD;

//        if (null != cert && (CommonConst.USE_FINGER_TYPE == cert.getFingertype())) {
//            if (!"".equals(cert.getCerthash())) {
//                //return cert.getCerthash();
//                if (!"".equals(strPWD) && strPWD.length() > 0)
//                    return strPWD;
//            } else
//                return strPWD;
//        }

//		if(null != cert) {
//			if (!"".equals(strPWD) && strPWD.length() > 0)
//				return strPWD;
//		}

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;
    }

    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertRenewActivity.this);
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

        String myHttpBaseUrl = CertRenewActivity.this.getString(R.string.UMSP_Base_Service);
        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        bRan = jse.random(256, "SHA1PRNG", "SUN");
        gUcmSdk.setRandomSeed(bRan);
        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);

        return retcode;
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
