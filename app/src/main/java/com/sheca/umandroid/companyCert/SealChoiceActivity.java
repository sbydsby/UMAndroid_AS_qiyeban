package com.sheca.umandroid.companyCert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.custle.dyrz.DYRZResult;
import com.custle.dyrz.DYRZResultBean;
import com.custle.dyrz.config.DYErrMacro;
import com.custle.dyrz.utils.T;
import com.sheca.javasafeengine;
import com.sheca.umandroid.BaseActivity2;
import com.sheca.umandroid.NetworkSignActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.PKIUtil;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.custle.dyrz.config.Constants.auth_type_alipay;
import static com.custle.dyrz.config.Constants.auth_type_bank;
import static com.custle.dyrz.config.Constants.auth_type_dyrz;
import static com.custle.dyrz.config.Constants.auth_type_face;
import static com.custle.dyrz.config.Constants.auth_type_face_alipay;
import static com.custle.dyrz.config.Constants.auth_type_mobile;

public class SealChoiceActivity extends BaseActivity2 {
    private AccountDao mAccountDao = null;
    private RelativeLayout btnPerson, btnOrg;

    private UniTrust uniTrust;


    private enum AUTH_TYPE {AUTH_TYPE_TAKE_PHOTO, AUTH_TYPE_INPUT}

    ;
    private boolean mIsFaceAuth = false;       //是否实名认证
    private boolean mIsDao = false;
    private boolean mIsDownload = false;

    private boolean needPay = false;

//    private Handler handler = new Handler(Looper.getMainLooper());

    boolean hasPersonCert = false;
    boolean hasOrgCert = false;

    private boolean isContainSeal() throws Exception {
        boolean isNoSeal = false;
        List<Cert> certList = new ArrayList<Cert>();
        AccountDao accountDao = new AccountDao(this);
        CertDao certDao = new CertDao(this);
        String strActName = accountDao.getLoginAccount().getName();
        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        certList = certDao.getAllCerts(strActName);

        for (Cert cert : certList) {
            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

            if (verifyCert(cert, false)) {
                if (verifyDevice(cert, false)) {
                    if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
                        SealInfoDao mSealInfoDao = new SealInfoDao(this);
                        SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), strActName);
                        if (null == sealInfo||sealInfo.getState()==5) {
                            if (cert.getCerttype().contains("个人")) {
                                hasPersonCert = true;
                            } else {
                                hasOrgCert = true;
                            }


                            isNoSeal = true;
//                            break;
                        }
                    }
                }
            }
        }

        return isNoSeal;
    }


    private boolean verifyDevice(final Cert cert, boolean bShow) {
        /*
        javasafeengine jse = new javasafeengine();

        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);
        Certificate oCert = jse.getCertFromBuffer(bCert);
        X509Certificate oX509Cert = (X509Certificate) oCert;
        String sDeciceID = jse.getCertExtInfo("1.2.156.112570.12.102", oX509Cert);

        //获取设备唯一标识符
        String deviceID = android.os.Build.SERIAL;
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
            deviceID = cert.getDevicesn();
        if (sDeciceID.equals(deviceID))
            return true;

        if (bShow)
            Toast.makeText(context, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
 */
        return true;
    }

    private boolean verifyCert(final Cert cert, boolean bShow) {
        if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())) {
            int i = -1;
            if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
            else
                i = PKIUtil.verifyCertificate(cert.getCertificate(),
                        cert.getCertchain());
            if (i == CommonConst.RET_VERIFY_CERT_OK) {
                return true;
            } else {
//                if (bShow)
//                    Toast.makeText(this, "验证证书失败", Toast.LENGTH_SHORT).show();
            }
        } else if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())) {
            String strSignCert = "";
            int i = -1;

            if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
                return false;

            if (!"".equals(cert.getContainerid())) {
                try {
                    javasafeengine jse = new javasafeengine();
                    strSignCert = cert.getCertificate();
                    if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                        i = jse.verifySM2Cert(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                    else
                        i = jse.verifySM2Cert(strSignCert, cert.getCertchain());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (i == 0) {
                    return true;
                } else if (i == 1) {
//                    if (bShow)
//                        Toast.makeText(this, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
//                    if (bShow)
//                        Toast.makeText(this, "验证证书失败", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(cert.getCerttype())) {
            int i = -1;
            if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
            else
                i = PKIUtil.verifyCertificate(cert.getCertificate(),
                        cert.getCertchain());
            if (i == CommonConst.RET_VERIFY_CERT_OK) {
                return true;
            } else {
//                if (bShow)
//                    Toast.makeText(this, "验证证书失败", Toast.LENGTH_SHORT).show();
            }
        } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())) {
            String strSignCert = "";
            int i = -1;

            if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
                return false;

            if (!"".equals(cert.getContainerid())) {
                try {
                    javasafeengine jse = new javasafeengine();
                    strSignCert = cert.getCertificate();
                    if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                        i = jse.verifySM2Cert(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                    else
                        i = jse.verifySM2Cert(strSignCert, cert.getCertchain());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (i == 0) {
                    return true;
                } else if (i == 1) {
//                    if (bShow)
//                        Toast.makeText(this, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
//                    if (bShow)
//                        Toast.makeText(this, "验证证书失败", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (!cert.getCerttype().contains("SM2")) {
            int i = -1;
            try {
                if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
                else
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            // Toast.makeText(DaoActivity.this, "verifyCert:"+i+"\nCert:"+cert.getCertificate()+"\nCertchain:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
//            Log.d("DaoActivity", "verifyCert-Cert:" + cert.getCertificate() + " Certchain:" + cert.getCertchain());
            if (i == CommonConst.RET_VERIFY_CERT_OK) {
                return true;
            } else {
                return false;
            }
        } else if (cert.getCerttype().contains("SM2")) {
            String strSignCert = "";

            int i = -1;
            try {
                strSignCert = cert.getCertificate();
                if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                    i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                else
                    i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            if (i == 0) {
                return true;
            } else if (i == 1) {
                return false;
            } else {
                return false;
            }

        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.module_apply_seal, R.layout.activity_seal_choice);

        uniTrust = new UniTrust(this, false);
        uniTrust.setFaceAuth(true);

        mAccountDao = new AccountDao(SealChoiceActivity.this);
        btnPerson = (RelativeLayout) findViewById(R.id.btn_person);  //拍摄照片
        btnOrg = (RelativeLayout) findViewById(R.id.btn_org);          //手动输入

        btnPerson.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPersonCert) {
                    Intent intent = new Intent(SealChoiceActivity.this, NetworkSignActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SealChoiceActivity.this,"无可申请印章的个人证书",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnOrg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasOrgCert) {
                    Intent intent = new Intent(SealChoiceActivity.this, SealApplyStep1.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(SealChoiceActivity.this,"无可申请印章的单位证书",Toast.LENGTH_SHORT).show();

                }

            }
        });
        try {
            isContainSeal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void setAccountVal(final String name, final String id) {
//        String setRes = uniTrust.SetAccountCertification(ParamGen.getAccountVerification(
//                AccountHelper.getToken(getApplicationContext()),
//                name, id, "4"));
//
//        Log.d("unitrust", setRes);
//
//        final APPResponse setResp = new APPResponse(setRes);
//        final int retCode = setResp.getReturnCode();
//        final String retMsg = setResp.getReturnMsg();
//
//        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(SealChoiceActivity.this, retMsg, Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//            });
//
//        } else {
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(SealChoiceActivity.this, "设置实名认证状态成功", Toast.LENGTH_SHORT).show();
//                    Account acount = mAccountDao.getLoginAccount();
//                    acount.setStatus(4);
//                    acount.setIdentityName(name);
//                    acount.setIdentityCode(id);
//                    mAccountDao.update(acount);
//
//                    finish();
//                }
//            });
//
//        }
//
//    }
//
//    private void faceAuth(final AUTH_TYPE authType) {
//
////            Intent intent = new Intent();
////            intent.setClass(AuthChoiceActivity.this, PayActivity.class);
////            intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
////            intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
////            startActivity(intent);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                uniTrust.setFaceAuthBGColor("#F7D05B");   //设置界面背景色
//                uniTrust.setFaceAuthTextColor("#000000");  //设置界面标题色
//
//                String info;
//                if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO) {
//                    uniTrust.setFaceAuthActionNumber(3);
//                    uniTrust.setFaceAuth(true);
//                    info = ParamGen.getFaceAuthOCR();
//                } else {
//                    info = ParamGen.getFaceAuth(getApplicationContext());
//                }
//
//
//                String result = uniTrust.FaceAuth(info);
//
//                Log.d("unitrust", result);
//                APPResponse response = new APPResponse(result);
//                final int retCode = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//
//
//                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SealChoiceActivity.this, getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    finish();
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SealChoiceActivity.this, retMsg, Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//                    //人脸验证成功
//                    JSONObject jbRet = response.getResult();
//                    String personName = jbRet.getString("personName");
//                    String personId = jbRet.getString("personID");
//                    AccountHelper.setRealName(personName);
//                    AccountHelper.setIdcardno(personId);
//                    String authMsg = AccountHelper.getUsername(getApplicationContext());
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
//
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));
//
//                    Account acount = mAccountDao.getLoginAccount();
//                    //acount.setStatus(4);
//                    acount.setIdentityName(personName);
//                    acount.setIdentityCode(personId);
//
//                    mAccountDao.update(acount);
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setInt(CommonConst.PARAM_STATUS, 2);
//                    if (acount.getStatus() != 4) {
//                        setAccountVal(personName, personId);
//                    } else {
//                        if (needPay) {
//                        Intent intent = new Intent();
//                        intent.setClass(SealChoiceActivity.this, com.sheca.umandroid.PayActivity.class);
//                        intent.putExtra("loginAccount", AccountHelper.getRealName(SealChoiceActivity.this));
//                        intent.putExtra("loginId", AccountHelper.getIdcardno(SealChoiceActivity.this));
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        setAccountVal(personName, personId);
//                    }
//                    }
//
//
////
//
//
//                }
//            }
//        }).start();
//
//    }
//    private String transaction_id = "app" + UUID.randomUUID().toString();
//    private void faceAuthNew(final AUTH_TYPE authType) {
//
////        DYRZSDK.getInstance(CommonConst.appID, CommonConst.priKey, DYRZSDK.BUILD_RELEASE).faceAuth(AuthChoiceActivity.this, AccountHelper.getRealName(this), AccountHelper.getIdcardno(this), transaction_id, this);
////
////
////
////
////
////
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                uniTrust.setFaceAuthBGColor("#F7D05B");   //设置界面背景色
//                uniTrust.setFaceAuthTextColor("#000000");  //设置界面标题色
//
//                String info;
//                if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO) {
//                    uniTrust.setFaceAuthActionNumber(3);
//                    uniTrust.setFaceAuth(true);
//                    info = ParamGen.getFaceAuthOCR();
//                } else {
//                    info = ParamGen.getFaceAuth(getApplicationContext());
//                }
//
//
//                String result = uniTrust.FaceAuth(info);
//
//                Log.d("unitrust", result);
//                APPResponse response = new APPResponse(result);
//                final int retCode = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//
//
//                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SealChoiceActivity.this, getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    finish();
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SealChoiceActivity.this, retMsg, Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//                    //人脸验证成功
//                    JSONObject jbRet = response.getResult();
//                    String personName = jbRet.getString("personName");
//                    String personId = jbRet.getString("personID");
//                    AccountHelper.setRealName(personName);
//                    AccountHelper.setIdcardno(personId);
//                    String authMsg = AccountHelper.getUsername(getApplicationContext());
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
//
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));
//
//                    Account acount = mAccountDao.getLoginAccount();
//                    //acount.setStatus(4);
//                    acount.setIdentityName(personName);
//                    acount.setIdentityCode(personId);
//
//                    mAccountDao.update(acount);
//                    SharePreferenceUtil.getInstance(SealChoiceActivity.this).setInt(CommonConst.PARAM_STATUS, 2);
//                    if (acount.getStatus() != 4) {
//                        setAccountVal(personName, personId);
//                    } else {
//                        if (needPay) {
//                            Intent intent = new Intent();
//                            intent.setClass(SealChoiceActivity.this, com.sheca.umandroid.PayActivity.class);
//                            intent.putExtra("loginAccount", AccountHelper.getRealName(SealChoiceActivity.this));
//                            intent.putExtra("loginId", AccountHelper.getIdcardno(SealChoiceActivity.this));
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            setAccountVal(personName, personId);
//                        }
//                    }
//
//
////
//
//
//                }
//            }
//        }).start();
//
//    }
//
//
//
//    @Override
//    public void dyrzResultCallBack(DYRZResultBean bean) {
//        String result = "code: " + bean.getCode() + "\r\n";
//        result += "msg: " + bean.getMsg() + "\r\n";
//        String authType;
//        switch (bean.getAuthType()) {
//            case auth_type_dyrz:
//                authType = "多源认证";
//                break;
//            case auth_type_mobile:
//                authType = "手机号认证";
//                break;
//            case auth_type_bank:
//                authType = "银行卡认证";
//                break;
//            case auth_type_face:
//                authType = "人脸识别认证";
//                break;
//            case auth_type_alipay:
//                authType = "支付宝认证";
//                break;
//            case auth_type_face_alipay:
//                authType = "人脸支付宝认证";
//                break;
//            default:
//                authType = "其它";
//                break;
//        }
//        result += "type: " + authType + "\r\n";
//        result += "token: " + bean.getToken() + "\r\n";
//        result += "data: " + bean.getData();
////        mTV.setText(result);
////
////        if (bean.getData() != null && bean.getData().length() > 0) {
////            byte[] bytes= Base64.decode(bean.getData(), Base64.NO_WRAP);
////            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
////            mFaceIV.setImageBitmap(bitmap);
////            mFaceIV.setVisibility(View.VISIBLE);
////        }
//
//        if (bean.getAuthType() == auth_type_face && bean.getCode().equals(DYErrMacro.camera_permission_err)) {
//            if ((Build.VERSION.SDK_INT>22) && (ContextCompat.checkSelfPermission(SealChoiceActivity.this,
//                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)){
//                ActivityCompat.requestPermissions(SealChoiceActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
//                return;
//            }
//        }
//
//    }
//
//
//    @SuppressLint("Override")
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//            // 回调中加载下一个Activity
//            T.showShort(SealChoiceActivity.this, "权限关闭");
//        } else {
//            T.showShort(SealChoiceActivity.this, "权限开启，请再次认证");
//        }
//    }
}
