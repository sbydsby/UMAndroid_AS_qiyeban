package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.excelsecu.slotapi.EsIBankDevice;
import com.sheca.umandroid.AuthChoiceActivity;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.LoginActivity;
import com.sheca.umandroid.NetworkSignActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.ScanBlueToothSimActivity;
import com.sheca.umandroid.companyCert.SealChoiceActivity;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.PKIUtil;

import org.spongycastle.util.encoders.Base64;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SealPresenter extends BasePresenter {

    private Context context;
    private AccountDao accountDao;
    private CertDao certDao;
    private Handler workHandler = null;
    private SharedPreferences sharedPrefs;
    private Activity activity;

    public SealPresenter(Context context, AccountDao accountDao, CertDao certDao, Handler workHandler, Activity activity) {
        super(context);
        this.context = context;
        this.accountDao = accountDao;
        this.certDao = certDao;
        this.workHandler = workHandler;
        this.activity = activity;

        sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void applySeal(List<Map<String, String>> mData) {
        // TODO: 2019/4/18 查询是否需要登录
        if (!AccountHelper.hasLogin(context)) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {
            try {
                mData = getCertData("");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mData.size() == 0) {   //进行人脸识并下载证书
                if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                    Toast.makeText(context, "无证书,请先下载证书", Toast.LENGTH_SHORT).show();
                else
                    showFaceReg();   //进行人脸识别
            } else {
                try {

//                    Intent intent = new Intent(context, SealChoiceActivity.class);
//                    context.startActivity(intent);

                    if (!isContainSeal()) {
                        Toast.makeText(context, "所有证书已申请印章", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                                            Intent intent = new Intent(context, SealChoiceActivity.class);
                    context.startActivity(intent);


//                        Intent intent = new Intent(context, NetworkSignActivity.class);
//                        context.startActivity(intent);

                    }
                } catch (Exception ex) {
                    Toast.makeText(context, "所有证书已申请印章", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    private boolean isContainSeal() throws Exception {
        boolean isNoSeal = false;
        List<Cert> certList = new ArrayList<Cert>();

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
                        SealInfoDao mSealInfoDao = new SealInfoDao(context);
                        SealInfo sealInfo = mSealInfoDao.getSealByCertsn(cert.getCertsn(), strActName);
                        if (null == sealInfo||sealInfo.getState()==5) {
                            isNoSeal = true;
                            break;
                        }
                    }
                }
            }
        }

        return isNoSeal;
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
            }/*else if(i == 0){
				if(bShow)
					Toast.makeText(context, "证书过期", Toast.LENGTH_SHORT).show();
			}*/ else {
                if (bShow)
                    Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
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
                    if (bShow)
                        Toast.makeText(context, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
                    if (bShow)
                        Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
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
                if (bShow)
                    Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
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
                    if (bShow)
                        Toast.makeText(context, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
                    if (bShow)
                        Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
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
            if (i == CommonConst.RET_VERIFY_CERT_OK) {
                return true;
            } else {
                return false;
            }

//            return true;

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


//            return true;

        }


        return false;
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

    private List<Map<String, String>> getCertData(String certsn) throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<Cert> certList = new ArrayList<Cert>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);

        String strActName = accountDao.getLoginAccount().getName();
        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        if (certsn != null && !"".equals(certsn)) {
            certList.add(certDao.getCertByCertsn(certsn, strActName));
        } else {
            certList = certDao.getAllCerts(strActName);
        }

        for (Cert cert : certList) {
            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

            if (getCertType(cert) == false&&!com.sheca.umplus.util.PKIUtil.isAccountCert(cert.getCertificate(), AccountHelper.getIDNumber(context)))
                continue;
            if (getCertType(cert) == true&&! com.sheca.umplus.util.PKIUtil.isOrgCert(cert.getCertificate(), AccountHelper.getIDNumber(context)))
                continue;


            if (verifyCert(cert, false)) {
                if (verifyDevice(cert, false)) {
                    javasafeengine jse = new javasafeengine();
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
                        list.add(map);
                    }
                }
            }
        }

        return list;
    }

    public boolean checkBTDevice() {
        if (CommonConst.SAVE_CERT_TYPE_PHONE == accountDao.getLoginAccount().getSaveType())
            return true;

        SharedPreferences sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))) {
            String strBTDevSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType()) {
                JShcaEsStd gEsDev = JShcaEsStd.getIntence(context);

                shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
                if (null == devInfo) {
                    int nRet = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
                    if (nRet == 0)
                        return true;
                    else
                        return false;
                } else {
                    return true;
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType()) {
                if (ScanBlueToothSimActivity.gKsSdk.isConnected())
                    return true;

                try {
                    ScanBlueToothSimActivity.gKsSdk.connect(strBTDevSN, "778899", 500);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        }

        return true;
    }

    public void showFaceReg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("无证书,是否需要自助申请证书?");
        builder.setIcon(R.drawable.alert);
        builder.setTitle("提示");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final Handler handler = new Handler(context.getMainLooper());

                    if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
                        showProgDlgCert("正在连接蓝牙key设备...");
                    else if (CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
                        showProgDlgCert("正在连接蓝牙sim卡...");

                    workHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (checkBTDevice()) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlgCert();
                                    }
                                });

                                if (AccountHelper.hasAuth(context)) { //账户已实名认证
                                    applyByFace();
                                } else {
                                    Intent intent = new Intent(context, AuthChoiceActivity.class);
                                    Bundle bundle = new Bundle();
                                    intent.putExtra("isPayAndAuth", "isPayAndAuth");
                                    intent.putExtras(bundle);
                                    context.startActivity(intent);
                                }
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlgCert();
                                        Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    closeProgDlgCert();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        builder.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    public boolean checkShcaCciStdServiceState(int actCertType) {
        if (CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
            return true;

        return true;
    }

    public void applyByFace() {

        if (!checkShcaCciStdServiceState(accountDao.getLoginAccount().getCertType())) {
            Toast.makeText(context, "密码分割组件初始化失败,请退出重启应用", Toast.LENGTH_SHORT).show();

            Account act = accountDao.getLoginAccount();
            act.setCertType(CommonConst.SAVE_CERT_TYPE_RSA);
            accountDao.update(act);
            return;
        }

        Intent intent = null;
        if (accountDao.getLoginAccount().getStatus() == 5 || accountDao.getLoginAccount().getStatus() == 3 || accountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
            AuthController controller = new AuthController();
            controller.faceAuth(activity, true);
        } else {
            intent = new Intent(context, AuthChoiceActivity.class);
            intent.putExtra("needPay", "true");
            context.startActivity(intent);
        }
    }

    private boolean getCertType(Cert cert) {  //true 单位证书 false个人证书
        Log.e("类型", cert.getCerttype());
        return !cert.getCerttype().contains("个人");

    }
}
