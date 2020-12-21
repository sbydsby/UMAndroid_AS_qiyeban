package com.sheca.umee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.scsk.EnumCertDetailNo;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.account.ReLoginActivityV33;
import com.sheca.umee.adapter.ApplicationAdapter;
import com.sheca.umee.companyCert.ICallback;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.LogDao;
import com.sheca.umee.dao.SealInfoDao;
import com.sheca.umee.event.RefreshEvent;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Account;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.OperationLog;
import com.sheca.umee.model.SealInfo;
import com.sheca.umee.presenter.CertController;
import com.sheca.umee.presenter.SealController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.PKIUtil;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SealDownloadActivity extends Activity {
    // UI references.
    private ListView mApplicationsView;
    private List<Map<String, String>> mData = null;

    //DB Access Object
    private AccountDao mAccountDao = null;
    private CertDao mCertDao = null;
    private LogDao mLogDao = null;

    CertController controller = new CertController();


    private String mAccount = "";
    private String mError = "";

    private boolean mIsScanDao = false;   //第三方扫码接口调用标记
    private boolean mIsDao = false;       //第三方接口调用标记

    //private View mProgressView;
    private ProgressDialog progDialog = null;
    private SharedPreferences sharedPrefs;

    //    private JShcaEsStd gEsDev = null;
//    private JShcaUcmStd gUcmSdk = null;
    //private  JShcaKsStd gKsSdk = null;
    protected Handler workHandler = null;
    private HandlerThread ht = null;

    private int mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
    private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
    private String mStrBTDevicePwd = "";    //蓝牙key密码

    private String strPersonName = "";
    private String strPaperNO = "";

    private final int DOWNLAOD_CERT_STATE = 1;
    private final int APPLY_CERT_STATE = 2;

    private Button btnApply;
    private ImageView applyCert;
    SealController sealController;

    CertDao certDao;
    AccountDao accountDao;
    SealInfoDao sealInfoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_seal_download);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));

        ((TextView) findViewById(R.id.header_text)).setText("印章下载列表");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);

        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
//        gEsDev = JShcaEsStd.getIntence(this);
        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertDownloadActivity.this.getApplication(), this);

//        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        certDao = new CertDao(this);
        accountDao = new AccountDao(this);
        sealInfoDao = new SealInfoDao(this);

//        ht = new HandlerThread("es_device_working_thread");
//        ht.start();
//        workHandler = new Handler(ht.getLooper());

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SealDownloadActivity.this.finish();
            }
        });

        mAccountDao = new AccountDao(this);
        mLogDao = new LogDao(SealDownloadActivity.this);
        mCertDao = new CertDao(SealDownloadActivity.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("scan") != null) {
                mIsScanDao = true;
                cancelScanButton.setVisibility(RelativeLayout.GONE);
            }
            if (extras.getString("message") != null) {
                mIsDao = true;
            }
        }

        //判断账号是否已登录。
        Boolean isLoggedIn = false;

        if (!AccountHelper.hasLogin(this)) {
            isLoggedIn = false;
        } else {
            isLoggedIn = true;
        }

        if (!isLoggedIn) {
            //若账号未登录，跳转到登录页面
//			Intent intent = new Intent(CertDownloadActivity.this, LoginActivity.class);
//			startActivity(intent);
            if (!AccountHelper.hasLogin(SealDownloadActivity.this)) {
                if (AccountHelper.isFirstLogin(SealDownloadActivity.this)) {
                    Intent intentLoignV33 = new Intent(SealDownloadActivity.this, LoginActivityV33.class);
                    startActivity(intentLoignV33);
                } else {
                    Intent intentLoignV33 = new Intent(SealDownloadActivity.this, ReLoginActivityV33.class);
                    startActivity(intentLoignV33);
                }
            }


            SealDownloadActivity.this.finish();
            return;
        } else {
            if (mAccountDao.getLoginAccount().getActive() == 0) {
//				Intent intent = new Intent(this, PasswordActivity.class);
//			    intent.putExtra("Account", mAccountDao.getLoginAccount().getName());
//			    startActivity(intent);
//			    CertDownloadActivity.this.finish();
            }


            //mProgressView = findViewById(R.id.cert_progress);
            mApplicationsView = (ListView) findViewById(R.id.lv_applications);
            Account currentAccount = mAccountDao.getLoginAccount();//accounts.get(0);
            mAccount = currentAccount.getName();

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == currentAccount.getSaveType()) {
                mSaveType = CommonConst.SAVE_CERT_TYPE_BLUETOOTH;
                mBBTDeviceUsed = true;
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == currentAccount.getSaveType()) {
                mSaveType = CommonConst.SAVE_CERT_TYPE_SIM;
                mBBTDeviceUsed = true;
            } else {
                mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
                mBBTDeviceUsed = false;
            }

//			findViewById(R.id.Layout3).setVisibility(RelativeLayout.GONE);
//
//			findViewById(R.id.Layout3).setVisibility(RelativeLayout.VISIBLE);

//			if(currentAccount.getType() == CommonConst.ACCOUNT_TYPE_COMPANY){
////				findViewById(R.id.Layout3).setVisibility(RelativeLayout.GONE);
//				LayoutParams layoutPam =   mApplicationsView.getLayoutParams();
//				layoutPam.height = LayoutParams.WRAP_CONTENT;
//				mApplicationsView.setLayoutParams(layoutPam);
//			}else{
////				findViewById(R.id.Layout3).setVisibility(RelativeLayout.VISIBLE);
//			}

            this.registerForContextMenu(mApplicationsView);
        }

        findViewById(R.id.btn_apply).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 申请证书
                finish();
            }
        });


        sealController = new SealController();
        showCertList();
    }

    private void getCertPic(String certId, String certSn) {
//		1为个人印章，2为单位印章

        sealController.getSealPic(SealDownloadActivity.this, certId, "2", new ICallback() {
            @Override
            public void onCallback(Object data) {
                APPResponse appResponse = new APPResponse((String) data);
                String picData = appResponse.getResult().optString("picData");


                String strActName = accountDao.getLoginAccount().getName();

                Cert localCert = certDao.getCertByCertsn(certSn, strActName);
                if (localCert != null) {

//                    localCert.setSealsn(sealInfo.getSealsn());
                    localCert.setSealstate(Cert.STATUS_IS_SEAL);

//                    SealInfo sealInfo=new SealInfo();

                    //印章需要与证书做关联

//                    sealInfo.setSdkID(mCertId);
//                    sealInfo.setSealname(strSealName);

                    SealInfo sealInfo = sealInfoDao.getSealByCertsn(localCert.getCertsn(), strActName);
                    if (sealInfo == null) {
                        sealInfo = new SealInfo();
                        sealInfo.setPicdata(picData);
                        sealInfo.setCertsn(localCert.getCertsn());
                        sealInfo.setCert(localCert.getCertificate());

                        sealInfoDao.addSeal(sealInfo, strActName);
//                        certDao.updateCert(localCert, strActName);
                    } else {
                        sealInfo.setPicdata(picData);
                        sealInfo.setCertsn(localCert.getCertsn());
                        sealInfo.setCert(localCert.getCertificate());
                        sealInfoDao.updateSealInfo(sealInfo, strActName);
                    }


                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        EventBus.getDefault().post(new RefreshEvent());
                        Intent intent = new Intent(SealDownloadActivity.this, CertResultActivity.class);
                        intent.putExtra("type", 1);
                        startActivity(intent);
                        finish();
                        Toast.makeText(SealDownloadActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    private void showCertList() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mData != null) {
                        mData.clear();
                    }
                    mData = getData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mData.size() == 0) {

                                findViewById(R.id.Layout_cert1).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.Layout_cert2).setVisibility(RelativeLayout.GONE);


                            } else {
                                findViewById(R.id.Layout_cert1).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.Layout_cert2).setVisibility(RelativeLayout.VISIBLE);


                                final ApplicationAdapter adapter = new ApplicationAdapter(SealDownloadActivity.this, mData);

                                mApplicationsView.setAdapter(adapter);

                                //调用多个UMSP服务进行印章下载
                                mApplicationsView.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        String certId = mData.get(position).get("CertId");

                                        String certSn = mData.get(position).get("CertSn");
                                        getCertPic(certId, certSn);
                                    }
                                });


                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(CommonConst.TAG, e.getMessage(), e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SealDownloadActivity.this, "获取印章错误！", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }
            }
        }).start();


    }


    private void applyByFace() {
        if (!checkShcaCciStdServiceState(mAccountDao.getLoginAccount().getCertType())) {
            Toast.makeText(SealDownloadActivity.this, "密码分割组件初始化失败,请退出重启应用", Toast.LENGTH_SHORT).show();

            Account act = mAccountDao.getLoginAccount();
            act.setCertType(CommonConst.SAVE_CERT_TYPE_RSA);
            mAccountDao.update(act);
            return;
        }

        Intent intent = null;
//        if (mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
//            AuthController controller = new AuthController();
//            controller.faceAuth(SealDownloadActivity.this, true);
//        } else {
//            intent = new Intent(SealDownloadActivity.this, AuthChoiceActivity.class);
//            Bundle bundle = new Bundle();
//            intent.putExtra("isPayAndAuth", "isPayAndAuth");
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }
//        if (mIsScanDao) {
//            intent = new Intent(SealDownloadActivity.this, AuthChoiceActivity.class);
//            intent.putExtra("message", "dao");
//            startActivity(intent);
//        }
//
//        SealDownloadActivity.this.finish();
    }


    private List<Map<String, String>> getData() {
        String strActName = accountDao.getLoginAccount().getName();
        List<Cert> certList = certDao.getAllCerts(strActName);


        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        for (Cert application : certList) {
            byte[] bCert = Base64.decode(application.getCertificate());
            map = new HashMap<String, String>();

            map.put("CertSn", application.getCertsn());

//			map.put("RequestNumber", application.getRequestNumber());
            map.put("CommonName", PKIUtil.getCertDetail(bCert, EnumCertDetailNo.EnumCertDetailNoSubjectCN));
//            map.put("CommonName", "测试");
//			map.put("ApplyTime", application.getApplyTime());
//			map.put("ApplyStatus", application.getApplyStatus()+"");
//			map.put("BizSN", application.getBizSN());
//			map.put("CertType", application.getCertType());
//			map.put("SignAlg", application.getSignAlg()+"");
//			map.put("PayStatus", application.getPayStatus()+"");

//			if(application.getApplyStatus() == 4)
//				map.put("Download", R.drawable.manucheckfailwait+"");
//			else if(application.getApplyStatus() == 6)
//				map.put("Download", R.drawable.manucheckfail+"");
//			else
//				map.put("Download", R.drawable.download+"");

//			if(application.getApplyStatus() == 6)
            map.put("ListImage", R.drawable.seal_icon + "");
            map.put("CertId", application.getSdkID() + "");
//			else
//			   map.put("ListImage", R.drawable.seal_icon+"");

//			map.put("ActType",mAccountDao.getLoginAccount().getType()+"");

            data.add(map);
        }

        return data;
    }

    private String GetCertApplyList() throws Exception {
        String timeout = SealDownloadActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = SealDownloadActivity.this.getString(R.string.UMSP_Service_GetCertApplyList);
        Map<String, String> postParams = new HashMap<String, String>();
        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "";
        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

        return responseStr;
    }


    private String SetSuccessStatus(String requestNumber, int saveType) throws Exception {
        String timeout = SealDownloadActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = SealDownloadActivity.this.getString(R.string.UMSP_Service_SetSuccessStatus);
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

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        mLogDao.addLog(log, strActName);
    }

    private void checkCertStatus() throws Exception {
        List<Cert> certList = new ArrayList<Cert>();
        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        certList = mCertDao.getAllCerts(strActName);

        for (Cert cert : certList) {
            if (cert.getEnvsn().isEmpty())
                continue;
            if (cert.getEnvsn().indexOf("-e") != -1)
                continue;

            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT && cert.getUploadstatus() == Cert.STATUS_UNUPLOAD_CERT) {
                String responseStr = SetSuccessStatus(cert.getEnvsn(), cert.getSavetype());
                JSONObject jb = JSONObject.fromObject(responseStr);
                String resultStr = jb.getString(CommonConst.RETURN_CODE);
                String returnStr = jb.getString(CommonConst.RETURN_MSG);

                if (resultStr.equals("0")) {
                    cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                    mCertDao.updateCert(cert, strActName);
                } else {
                    throw new Exception("调用UMSP服务之SetSuccessStatus失败：" + resultStr + "，" + returnStr);
                }
            }
        }

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

    //@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
		/*public void showProgress(final boolean show) {
			// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
			// for very easy animations. If available, use these APIs to fade-in
			// the progress spinner.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

				mApplicationsView.setVisibility(show ? View.GONE : View.VISIBLE);
				mApplicationsView.animate().setDuration(shortAnimTime)
						.alpha(show ? 0 : 1)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mApplicationsView.setVisibility(show ? View.GONE : View.VISIBLE);
							}
						});


				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				mProgressView.animate().setDuration(shortAnimTime)
						.alpha(show ? 1 : 0)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mProgressView.setVisibility(show ? View.VISIBLE
										: View.GONE);
							}
						});
			} else {
				// The ViewPropertyAnimator APIs are not available, so simply show
				// and hide the relevant UI components.
				mApplicationsView.setVisibility(show ? View.VISIBLE : View.GONE);
				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		}
		*/
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

    private Boolean loginUMSPService(String act, int state) {    //重新登录UM Service
        String returnStr = "";
        try {
            //异步调用UMSP服务：用户登录
            String timeout = SealDownloadActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = SealDownloadActivity.this.getString(R.string.UMSP_Service_Login);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", act);
            postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
            if (mAccountDao.getLoginAccount().getType() == 1)
                postParams.put("appID", CommonConst.UM_APPID);
            else
                postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());

            String responseStr = "";
            try {
                //清空本地缓存
                //WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String actpwd = "";
                if (mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
                    actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword());
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

            if (resultStr.equals("0")) {
                if (state == DOWNLAOD_CERT_STATE) {
                    //若成功登录，注册已登录账号，并跳转到首页；
                    Intent intent = new Intent(SealDownloadActivity.this, SealDownloadActivity.class);
                    //intent.putExtra("Account", mAccount);
                    //intent.putExtra("Message", "用户登录成功");
                    if (mIsScanDao)
                        intent.putExtra("scan", "dao");
                    if (mIsDao)
                        intent.putExtra("message", "dao");

                    startActivity(intent);
                    SealDownloadActivity.this.finish();
                } else if (state == APPLY_CERT_STATE) {
                    return true;
                }
                //showCertApplyList();
            } else if (resultStr.equals("10010")) {
                //若账号未激活，显示修改初始密码页面；
                Intent intent = new Intent(SealDownloadActivity.this, PasswordActivity.class);
                intent.putExtra("Account", mAccount);
                if (mIsScanDao)
                    intent.putExtra("scan", "dao");
                startActivity(intent);
                SealDownloadActivity.this.finish();
            } else if (resultStr.equals("10009")) {
                //若账号口令错误,显示账户登录页面；
                Account curAct = mAccountDao.getLoginAccount();
                curAct.setStatus(-1);   //重置登录状态为未登录状态
                mAccountDao.update(curAct);

                Intent intent = new Intent(SealDownloadActivity.this, LoginActivityV33.class);
                intent.putExtra("AccName", curAct.getName());
                if (mIsScanDao)
                    intent.putExtra("scan", "dao");
                startActivity(intent);
                SealDownloadActivity.this.finish();
            } else {
                throw new Exception(resultStr + ":" + returnStr);
            }

//				try {
//					// Simulate network access.
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					return false;
//				}

//				for (String credential : DUMMY_CREDENTIALS) {
//					String[] pieces = credential.split(":");
//					if (pieces[0].equals(mAccount)) {
//						// Account exists, return true if the password matches.
//						return pieces[1].equals(mPassword);
//					}
//				}

        } catch (Exception exc) {
            mError = exc.getMessage();
            Log.e(CommonConst.TAG, mError, exc);
            return false;
        }

        return true;
    }


    private String getPWDHash(String strPWD) {
        String strPWDHash = "";

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

			/*try {
				strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        return strPWDHash;
    }


    private String getOSInfo() {
        String strOSInfo = "";

        strOSInfo = "硬件型号:" + Build.MODEL + "|操作系统版本号:"
                + Build.VERSION.RELEASE;
        return strOSInfo;
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





    private boolean checkShcaCciStdServiceState(int actCertType) {
        if (CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
            return true;

        if (mIsDao)
            return true;
    	/*
    	try{
    		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
   			   ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertDownloadActivity.this);
   			   ShcaCciStd.errorCode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);    		
   			   if(ShcaCciStd.errorCode != 0 )
   			    	ShcaCciStd.gSdk = null;
   		    }
    		
    		if(null == ShcaCciStd.gSdk)
    			return false;
    			
    		if(ShcaCciStd.errorCode != 0)
    			return false;		
	
    	}catch(Exception ex){
    		return false;
    	}
    	*/
        return true;

    }


}
