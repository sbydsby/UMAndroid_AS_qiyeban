package com.sheca.zhongmei;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

import com.sheca.javasafeengine;

import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.myWebClientUtil;

import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.ShcaCciStd;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.CommUtil;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.zhongmei.util.SharePreferenceUtil;
import com.sheca.zhongmei.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;

public class CertResetActivity extends Activity {
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


    TextView txt_phone;

    EditText et_sms_code;

    TextView tv_sms;

    String MAC = "";

    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_reset_password);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("重置证书口令");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CertResetActivity.this.finish();
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
                    ((TextView) findViewById(R.id.header_text)).setText("重置蓝牙key口令");
                    mOriginalPasswordView.setHint("原蓝牙key口令");
                    mNewPasswordView.setHint("新蓝牙key口令");
                    mNewPassword2View.setHint("再次输入新蓝牙key口令");
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()) {
                    ((TextView) findViewById(R.id.header_text)).setText("重置蓝牙sim卡口令");
                    mOriginalPasswordView.setHint("原蓝牙sim卡口令");
                    mNewPasswordView.setHint("新蓝牙sim卡口令");
                    mNewPassword2View.setHint("再次输入新蓝牙sim卡口令");
                }

            }
        }

        findViewById(R.id.btn_change_password).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                changePassword();
            }
        });

        if (CommUtil.isPasswordLocked(CertResetActivity.this, certID)) {
            findViewById(R.id.btn_change_password).setVisibility(android.widget.RelativeLayout.GONE);
            return;
        }
        userName = AccountHelper.getUsername(this);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_phone.setText("手机号  " + userName.substring(0, 3) + "****" + userName.substring(7, 11));
        et_sms_code = (EditText) findViewById(R.id.et_sms_code);

        tv_sms = (TextView) findViewById(R.id.tv_sms);
        tv_sms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getValidationCode(userName);
            }
        });
    }

    /**
     * 获取验证码的网络请求
     *
     * @param phone
     */
    private void getValidationCode(final String phone) {
        final String strInfo = ParamGen.getValidationCodeParams(phone, "5");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UniTrust dao = new UniTrust(CertResetActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
                    final String responseStr = dao.GetMAC(strInfo);
                    Log.e("验证码", responseStr);

                    JSONObject jb = JSONObject.fromObject(responseStr);
                    final String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    final String returnStr = jb.getString(CommonConst.RETURN_MSG);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                            if (resultStr.equals("0")) {
//                                MAC=jb.getString(CommonConst.RETURN_RESULT).


                                APPResponse response = new APPResponse(responseStr);

                                MAC = response.getResult().optString("MAC");


                                showCountDown(COUNT_DOWN_NUM);   //显示倒计时
                                Toast.makeText(getApplicationContext(), "短信验证码发送成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "短信验证码错误" + returnStr, Toast.LENGTH_LONG).show();
                                tv_sms.setText("发送验证码");
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });
                }
            }

        }).start();

    }

    private Timer timer = new Timer();
    private TimerTask task = null;

    private void showCountDown(final int countDownNum) {
        tv_sms.setEnabled(false);
        tv_sms.setText("等待60秒");
        //mHandler.postDelayed(mRunnable, 1000*60);
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };

        timer.schedule(task, 0, 1000);
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    update();
                    break;
            }

            super.handleMessage(msg);
        }

    };
    private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒

    private int count = COUNT_DOWN_NUM;

    private void update() {
        count--;
        if (count > 0) {
            tv_sms.setText("等待" + count + "秒");
        } else {
            tv_sms.setText("发送验证码");
            tv_sms.setEnabled(true);
            timer.cancel();
            timer = null;
            task.cancel();
            task = null;
            count = COUNT_DOWN_NUM;
        }
    }

    private void changePassword() {
        try {
            // Reset errors.
            mNewPasswordView.setError(null);

            String originalPassword = mOriginalPasswordView.getText().toString();
            String newPassword = mNewPasswordView.getText().toString();
            String newPassword2 = mNewPassword2View.getText().toString();

            boolean cancel = false;
            View focusView = null;


            // 检查用户输入的新口令是否有效

            if (MAC.length() == 0) {
                mNewPasswordView.setError("请先获取验证码");
                focusView = mNewPasswordView;
                cancel = true;
            }

            if (!MAC.equals(et_sms_code.getText().toString().trim())) {
                mNewPasswordView.setError("验证码错误");
                focusView = mNewPasswordView;
                cancel = true;
            }

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

            // 检查用户两次输入的新口令是否一致
            if (!newPassword.equals(newPassword2)) {
                mNewPasswordView.setError(getString(R.string.error_inconformity_password));
                focusView = mNewPassword2View;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt continue and focus the first form field with an error.
                focusView.requestFocus();
            } else {
          {
                    if (mCert.getFingertype() == CommonConst.USE_FINGER_TYPE) {
                        String strActName = accountDao.getLoginAccount().getName();
                        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                        getCertIdByCertSn(CertResetActivity.this,strActName,mCert.getCertsn(),newPassword, newPassword);
                    } else {
                        certId=mCert.getSdkID();
                        if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                            doChangeSM2CertPwd();
                            //根据SDK改变证书口令
                            doSDKChangePwd(newPassword, newPassword,mCert.getId());
                        } else {
                            doChangeCertPwd();
                            doSDKChangePwd(originalPassword, newPassword,mCert.getId());
                        }
                    }

                }
            }
        } catch (Exception exc) {
            Log.e(CommonConst.TAG, exc.getMessage(), exc);
        }

    }

    /**
     * 根据SDK更改证书口令
     *
     * @param oldPwd
     * @param newPwd
     */
    private void doSDKChangePwd(final String oldPwd, final String newPwd,final int certid) {
        new MyAsycnTaks() {

            @Override
            public void preTask() {
//				String hasholdPwd = getPWDHash(oldPwd, null);
//				String hashnewPwd = getPWDHash(newPwd, null);
                int msdkCertID = certid;
                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                strInfo = ParamGen.ResetCertPWD(mTokenId, msdkCertID + "", CommUtil.getPWDHash(newPwd));
            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(CertResetActivity.this, false);
                responResult = mUnitTrust.ResetCertPWD(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    closeProgDlg();


                    //if (CertResetActivity.this.isFinishing()) {
                    CertResetActivity.this.finish();
                    //}

                    //Toast.makeText(CertResetActivity.this, "证书重置口令成功",Toast.LENGTH_SHORT).show();

                } else {
                    closeProgDlg();
                    //Toast.makeText(CertResetActivity.this, "证书重置口令失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
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
            oStore.load(kis, cert.getCerthash().toCharArray());

        } catch (Exception e) {
            Toast.makeText(CertResetActivity.this, "原证书口令错误", Toast.LENGTH_SHORT).show();
            //CommUtil.showErrPasswordMsg(CertResetActivity.this,cert.getId());
            return;
        }

        try {
            oStore.store(kos, getPWDHash(mNewPasswordView.getText().toString(), cert).toCharArray());

            String newKeyStore = new String(Base64.encode(kos
                    .toByteArray()));
            cert.setKeystore(newKeyStore);
            cert.setCerthash(getPWDHash(mNewPasswordView.getText().toString(), cert));
            cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
            cert.setSdkID(certId);
            String strActName = accountDao.getLoginAccount().getName();
            if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

            certDao.updateCert(cert, strActName);

            Toast.makeText(CertResetActivity.this, "重置证书口令成功", Toast.LENGTH_SHORT)
                    .show();

            CertResetActivity.this.finish();
        } catch (Exception e) {
            Toast.makeText(CertResetActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
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
                Toast.makeText(CertResetActivity.this, "口令分割组件初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }

            //int ret1 = ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
            //int ret =  ShcaCciStd.gSdk.verifyUserPin(cert.getContainerid(), getPWDHash(mOriginalPasswordView.getText().toString(),cert));
            int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), cert.getCerthash());
            if (ret != 0) {
                Toast.makeText(CertResetActivity.this, "原证书口令错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertResetActivity.this,cert.getId());
                return;
            }

            if(cert.getFingertype() == CommonConst.USE_FINGER_TYPE)
               ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                        cert.getCerthash(),
                       cert.getCerthash());
            else
               ret = gUcmSdk.changeUserPinWithCID(cert.getContainerid(),
                       cert.getCerthash(),
                       getPWDHash(mNewPasswordView.getText().toString(), cert));

            if (ret == 0) {
                Toast.makeText(CertResetActivity.this, "重置证书口令成功", Toast.LENGTH_SHORT).show();

                cert.setCerthash(getPWDHash(mNewPasswordView.getText().toString(), cert));
                cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
                cert.setSdkID(certId);

                String strActName = accountDao.getLoginAccount().getName();
                if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                    strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                certDao.updateCert(cert, strActName);

                CertResetActivity.this.finish();
            } else if (ret == -13) {
                Toast.makeText(CertResetActivity.this, "原证书口令错误", Toast.LENGTH_SHORT).show();
                //CommUtil.showErrPasswordMsg(CertResetActivity.this,cert.getId());
                return;
            } else if (ret == -1) {
                Toast.makeText(CertResetActivity.this, "重置证书口令失败", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -2) {
                Toast.makeText(CertResetActivity.this, "重置证书口令异常", Toast.LENGTH_SHORT).show();
                return;
            } else if (ret == -3) {
                Toast.makeText(CertResetActivity.this, "参数错误", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            ShcaCciStd.gSdk = null;
            Toast.makeText(CertResetActivity.this, "口令分割组件初始化失败", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    int certId = 0;

    public void getCertIdByCertSn(Activity context, String userName, String certSn, final String oldpwd, final String newpwd) {
        final UniTrust uniTrustObi = new UniTrust(context, false);
        final String param = ParamGen.getAccountCertByCertSN(context, userName, certSn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.sheca.umplus.model.Cert cert = uniTrustObi.getAccountCertByCertSN(param);
                if (null != cert) {
                    certId = cert.getId();
                    if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")) {
                        doChangeSM2CertPwd();
                        //根据SDK改变证书口令
                        doSDKChangePwd(newpwd, newpwd,certId);
                    } else {
                        doChangeCertPwd();
                        doSDKChangePwd(oldpwd, newpwd,certId);
                    }
                }
            }
        }).start();


    }




    private int initShcaUCMService() {  //初始化CA手机盾中间件
        int retcode = -1;
        byte[] bRan = null;

        String myHttpBaseUrl = CertResetActivity.this.getString(R.string.UMSP_Base_Service);
        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        bRan = jse.random(256, "SHA1PRNG", "SUN");
        gUcmSdk.setRandomSeed(bRan);
        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);

        return retcode;
    }

    /**
     * 口令由8-16位英文、数字或符号组成。
     */
    private boolean isPasswordValid(String password) {
        boolean isValid = false;
        if (password.length() > 7 && password.length() < 17) {
            isValid = true;
        }
        return isValid;
    }


    private String getPWDHash(String strPWD, Cert cert) {
        String strPWDHash = "";

        if (null == cert)
            return strPWD;

        if (CommonConst.USE_FINGER_TYPE == cert.getFingertype()) {
            if (!"".equals(cert.getCerthash())) {
                //return cert.getCerthash();
                if (!"".equals(strPWD) && strPWD.length() > 0)
                    return strPWD;
            } else
                return strPWD;
        }

      /*  if (!"".equals(strPWD) && strPWD.length() > 0)
            return strPWD;
		*/
        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;


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
