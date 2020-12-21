package com.sheca.umee;

//import android.app.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

//import com.igexin.sdk.PushManager;

import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.account.ReLoginActivityV33;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.event.RefreshEvent;
import com.sheca.umee.fragment.FragmentFactory;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.ShcaCciStd;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.MyAsycnTaks;
import com.sheca.umee.util.PKIUtil;
import com.sheca.umee.util.ParamGen;
import com.sheca.umee.util.SealSignUtil;
import com.sheca.umee.util.SharePreferenceUtil;
import com.sheca.umee.util.UpdateUtil;
import com.sheca.umee.util.WebClientUtil;
import com.sheca.umee.util.WebUtil;

import com.sheca.umplus.activity.CaptureActivity;
import com.sheca.umplus.dao.UniTrust;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.net.URLEncoder;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.sheca.umplus.util.WebClientUtil.mScanToken;

public class MainActivity extends FragmentActivity {

    private static final int CAPTURE_CODE = 0;
    private FragmentManager fm;
    private RadioGroup rg;

    private boolean bUpdate = true;          //是否检查更新
    private boolean bShowUpdate = true;      //是否显示检查更新错误信息
    private ProgressDialog progDialog = null;
    private long timeDelay = 2000;          //启动更新任务时长(10秒)
    private boolean bUpdated = false;       //是否检测版本更新
    private boolean bLogined = false;       //是否检测自动登录
    private boolean bCheckPremissoned = false;       //是否检测应用权限
    private PowerManager.WakeLock wakeLock = null;
    private SharedPreferences sharedPrefs;
    private int operatorType = 0;
    int rsaType = 0;
    private int scanSignCount = 0;
    private String strPrint = "";
    private int scanSealNum = 0;
    private String strScanResult = "";
    private String strScanSealResult = "";
    private static final int SCAN_SEAL_CODE = 3;
    private final static int UPLOAD_P10 = 4;

    private final static int DOWNLOAD_CERT = 5;

    private final static int SAVE_CERT = 6;

    private final static int LOGIN_SUCCESS = 7;

    private final static int SIGN_SUCCESS = 8;

    private final static int SIGNEX_SUCCESS = 9;

    private final static int ENVELOP_DECRYPT_SUCCESS = 10;
    private final static int SEALINFO_SCAN_FAILURE = 11;
    private final static int SEAL_SCAN_FAILURE = 12;

    private final static int SEALINFO_SCAN_SUCCESS = 13;

    private final static int SEAL_SCAN_SUCCESS = 14;

    private final static int SEAL_SIGN_SCAN_SUCCESS = 15;
    private final static int SEAL_SIGN = 16;
    private final static int ENVELOPE_ENCODE_SUCCESS = 16;

    private static final int SCAN_CODE = 1;

    private static final int REQUEST_SEARCH_BT = 2;

    private final static int LOGIN_SIGN = 0;

    private final static int LOGIN_SIGN_FAILURE = 1;

    private final static int LOGIN_UPLOAD = 2;

    private final static int LOGIN_UPLOAD_FAILURE = 3;

    private final static int FAILURE = -1;

    private String qrCodeSN = "";
    public static String strErr = "";
    private String strScanErr = "";
    private boolean isJSONDate = false;
    private String strScanAppName = "";
    private long exitTime = 0;   //退出应用计时器

    private RadioButton mCert;
    private RadioButton mScan;
    private RadioButton mSeal;
    private RadioButton mSettings;

    private Button rb_scan;
    private boolean isSealSign = false;
    int nowid = R.id.rb_cert;

//    private Class userPushService = GeTuiService.class;


    boolean isEncode;//是否为扫码加密

    UniTrust uniTrust;
    String scanMsg = "";
    private boolean isSignEx = false;    //批量签名标志

    AccountDao accountDao;
    CertDao certDao;

    private boolean mState = false;

    private List<Cert> mCertList = new ArrayList<Cert>();

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.mainActivityNew = this;

//        initLicense();
        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        try {
            Security.addProvider(new BouncyCastleProvider());
            bCheckPremissoned = sharedPrefs.getBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, false);

            if (!bCheckPremissoned) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
                bCheckPremissoned = true;

                Editor editor = sharedPrefs.edit();
                editor.putBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, bCheckPremissoned);
                editor.commit();
            }

            //if(null != ShcaCciStd.gSdk && ShcaCciStd.errorCode == 0)
            //ShcaCciStd.gSdk = null;

            //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode!=0)
            // initShcaCciStdService();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            //ShcaCciStd.gSdk = null;
            //Toast.makeText(MainActivity.this,e1.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }

        if (LaunchActivity.LOG_FLAG)
            LaunchActivity.logUtil.recordLogServiceLog("MainActivity.onCreate");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintResource(R.color.bg_red);//通知栏所需颜色
//        }

        bUpdate = false;
        exitTime = System.currentTimeMillis();


        accountDao = new AccountDao(MainActivity.this);
        certDao = new CertDao(MainActivity.this);

        if (AccountHelper.hasLogin(this)) {
            String strActName = accountDao.getLoginAccount().getName();
            mCertList = certDao.getAllCerts(strActName);
        }


        try {
            fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            certFragment = FragmentFactory.getInstanceByIndex(R.id.rb_cert);
            transaction.add(R.id.content, certFragment, "CertFragment");

            //transaction.addToBackStack(null);//防止扫码闪退
            transaction.commit();

            rg = (RadioGroup) findViewById(R.id.rg_menu);
            RadioButton rb_home = (RadioButton) rg.getChildAt(0);
            rb_home.setChecked(true);

//            mHome = (RadioButton) findViewById(R.id.rb_home);
            mCert = (RadioButton) findViewById(R.id.rb_cert);
            mScan = (RadioButton) findViewById(R.id.rb_service);
            mSeal = (RadioButton) findViewById(R.id.rb_seal);
//            mSettings = (RadioButton) findViewById(R.id.rb_settings);

//定义底部标签图片大小和位置
            Drawable drawable_cert = getResources().getDrawable(R.drawable.selector_cert);
//    //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
            drawable_cert.setBounds(0, 0, 64, 64);
//    //设置图片在文字的哪个方向
            mCert.setCompoundDrawables(null, drawable_cert, null, null);


            Drawable drawable_scan = getResources().getDrawable(R.drawable.selector_scan);
            Drawable drawable_seal = getResources().getDrawable(R.drawable.selector_seal);


            drawable_cert.setBounds(0, 0, 64, 64);
            drawable_scan.setBounds(0, 0, 64, 64);
            drawable_seal.setBounds(0, 0, 64, 64);


            mCert.setCompoundDrawables(null, drawable_cert, null, null);
            mScan.setCompoundDrawables(null, drawable_scan, null, null);
            mSeal.setCompoundDrawables(null, drawable_seal, null, null);
//            mSettings.setCompoundDrawables(null, drawable_mine, null, null);

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final RadioGroup group, int checkedId) {
                    nowid = checkedId;
                    /*if (checkedId == R.id.rb_service) {
                        Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivityForResult(i, CAPTURE_CODE);
                    } else {*/
                    FragmentTransaction transaction = fm.beginTransaction();
                    Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId);
                    transaction.replace(R.id.content, fragment);
                    //transaction.addToBackStack(null);//防止扫码闪退
                    transaction.commit();
                    //}


                    switch (checkedId) {
                        case R.id.rb_cert:
                        case R.id.rb_seal:
//                            findViewById(R.id.tv_right).setVisibility(View.VISIBLE);


                            break;
                        default:
//                            findViewById(R.id.tv_right).setVisibility(View.GONE);

                            break;


                    }


//                    fragment.onResume();
                }
            });

            if (AccountHelper.hasLogin(this)) {
                registerXGPush(AccountHelper.getUsername(this));
            }


            rb_scan = findViewById(R.id.rb_scan);
            rb_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    QRScan();


                }
            });

            TextView txt_mine = findViewById(R.id.txt_mine);
            txt_mine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!AccountHelper.hasLogin(MainActivity.this)) {
                        if (AccountHelper.isFirstLogin(MainActivity.this)) {
                            Intent intentLoignV33 = new Intent(MainActivity.this, LoginActivityV33.class);
                            startActivity(intentLoignV33);
                        } else {
                            Intent intentLoignV33 = new Intent(MainActivity.this, ReLoginActivityV33.class);
                            startActivity(intentLoignV33);
                        }
                    } else {
                        Intent mineAc = new Intent(MainActivity.this, MineActivity.class);
                        startActivity(mineAc);

                    }

                }
            });

            //CommUtil.showByCheckAndroidVersion(MainActivity.this);

        } catch (Exception ex) {
            ex.printStackTrace();
            //Toast.makeText(MainActivity.this,ex.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }

//        initGetui();


        uniTrust = new UniTrust(this, false);


        if (AccountHelper.isLoadLicence(this)) {

//            EventBus.getDefault().post(new RefreshEvent());
//            if (AccountHelper.hasLogin(MainActivity.this) && !AccountHelper.hasAuth(MainActivity.this)) {
//                doLogout();//覆盖安装
//                return;
//            }
//            //license有效，检测是否登录，如果已经登录则进行重新登录，未登录跳入登录页面
//            if (!AccountHelper.hasLogin(MainActivity.this)) {
//                if (AccountHelper.isFirstLogin(MainActivity.this)) {
//                    Intent intentLoignV33 = new Intent(MainActivity.this, LoginActivityV33.class);
//                    startActivity(intentLoignV33);
//                } else {
//                    Intent intentLoignV33 = new Intent(MainActivity.this, ReLoginActivityV33.class);
//                    startActivity(intentLoignV33);
//                }
//            } else {
//                reLogin();
//            }

        } else {
//            loadLicense();
        }
    }

    private void QRScan() {

        if (AccountHelper.hasLogin(this)) {
            List<Map<String, String>> mData = null;

            try {
                mData = getData("");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mData.size() == 0) {

                AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                mAlertDialog.setTitle("提示");
                mAlertDialog.setMessage("无证书,是否下载证书?");
                mAlertDialog.setPositiveButton("去下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, CertDownloadActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mAlertDialog.show();
            } else {
                Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(i, CAPTURE_CODE);

            }


        } else {

            if (AccountHelper.isFirstLogin(MainActivity.this)) {
                Intent intentLoignV33 = new Intent(MainActivity.this, LoginActivityV33.class);
                startActivity(intentLoignV33);
            } else {
                Intent intentLoignV33 = new Intent(MainActivity.this, ReLoginActivityV33.class);
                startActivity(intentLoignV33);
            }
        }


    }

    private List<Map<String, String>> getData(String certsn) throws Exception {
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
//            if (getCertType(cert) == false && !com.sheca.umplus.util.PKIUtil.isAccountCert(cert.getCertificate(), AccountHelper.getIDNumber(MainActivity.this)))
//                continue;
//            if (getCertType(cert) == true&&! com.sheca.umplus.util.PKIUtil.isOrgCert(cert.getCertificate(), AccountHelper.getIDNumber(MainActivity.this)))
//                continue;
            if (PKIUtil.verifyCert(cert, false, MainActivity.this)) {

                if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("id", String.valueOf(cert.getId()));
//
//                        byte[] bCert = Base64.decode(cert.getCertificate());
//                        String commonName = jse.getCertDetail(17, bCert);
//                        String organization = jse.getCertDetail(14, bCert);
//
//                        String strNotBeforeTime = jse.getCertDetail(11, bCert);
//                        String strValidTime = jse.getCertDetail(12, bCert);
//                        Date fromDate = sdf.parse(strNotBeforeTime);
//                        Date toDate = sdf.parse(strValidTime);
//
//                        map.put("organization", organization);
//                        map.put("commonname", commonName);
//                        map.put("validtime",
//                                sdf2.format(fromDate) + " ~ " + sdf2.format(toDate));
                    list.add(map);
                }

            }
        }

        return list;
    }


    private void doLogout() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String res = null;
                try {
                    res = uniTrust.Logout(ParamGen.getLogout(AccountHelper.getToken(MainActivity.this)));
                } catch (Exception e) {
                    AccountHelper.clearAllUserData(MainActivity.this);
                    if (AccountHelper.isFirstLogin(MainActivity.this)) {
                        Intent intent = new Intent(MainActivity.this, LoginActivityV33.class);
                        startActivity(intent);
                    } else {
                        Intent intentLoignV33 = new Intent(MainActivity.this, ReLoginActivityV33.class);
                        startActivity(intentLoignV33);
                    }
//                    MainActivity.this.finish();
                }

                AccountHelper.clearAllUserData(MainActivity.this);


                APPResponse response = new APPResponse(res);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

//                    setAccountLogoutStatus();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            showSettingInfo();


//                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                            startActivity(intent);
                            if (AccountHelper.isFirstLogin(MainActivity.this)) {
                                Intent intent = new Intent(MainActivity.this, LoginActivityV33.class);
                                startActivity(intent);
                            } else {

                                Intent intent = new Intent(MainActivity.this, ReLoginActivityV33.class);
                                startActivity(intent);
                            }
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
                            editor.apply();


                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        MainActivity.this.finish();
//                    }
//                });

            }
        }).start();
    }


    private void loadLicense() {

        new Thread() {
            public void run() {
                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(AccountHelper.getUMSPAddress(MainActivity.this)));
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                            Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            AccountHelper.setLoadLicence(MainActivity.this, true);

//                            if (AccountHelper.hasLogin(MainActivity.this) && !AccountHelper.hasAuth(MainActivity.this)) {
//                                doLogout();//覆盖安装
//                                return;
//                            }
                            //license有效，检测是否登录，如果已经登录则进行重新登录，未登录跳入登录页面
                            if (!AccountHelper.hasLogin(MainActivity.this)) {
                                if (AccountHelper.isFirstLogin(MainActivity.this)) {
                                    Intent intentLoignV33 = new Intent(MainActivity.this, LoginActivityV33.class);
                                    startActivity(intentLoignV33);
                                } else {
                                    Intent intentLoignV33 = new Intent(MainActivity.this, ReLoginActivityV33.class);
                                    startActivity(intentLoignV33);
                                }
                            } else {
                                reLogin();
                            }

                        }
                    }
                });

            }
        }.start();
    }

    private void reLogin() {
        new MyAsycnTaks() {
            private String mStrVal = "";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                mStrVal = uniTrust.userAutoLogin(ParamGen.getAutoLoginParam(AccountHelper.getToken(MainActivity.this)));
            }

            @Override
            public void postTask() {
                closeProgDlg();
                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                Log.e("autologin", retCode + retMsg);
                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                    doLogout();
                }
            }

        }.execute();
    }


    private void initGetui() {

//        if (Build.VERSION.SDK_INT >= 23 && (!sdCardWritePermission || !phoneSatePermission)) {
//            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
//                    REQUEST_PERMISSION);
//        } else {
//        PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
//        }

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
//        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GeTuiIntentService.class);

    }

    public void showMsg(String result) {
        Log.e("收到透传消息", result);

        if (result.contains(CommonConst.PARAM_ACCOUNT_SIGN_UID)) {//测试用

            try {
                final JSONObject jb = JSONObject.fromObject(result);
                String accountName = jb.optString(CommonConst.PARAM_ACCOUNT_SIGN_UID);
                String accountUid = SharePreferenceUtil.getInstance(this).getString(CommonConst.PARAM_ACCOUNT_UID);
                if (AccountHelper.hasLogin(this) && accountName.equals(accountUid)) {
                    Intent intent = new Intent(this, QuickSignAcitvity.class);
                    intent.putExtra("result", result);
                    startActivity(intent);
                }
            } catch (Exception e) {
            }
        }


    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次将退出中煤易投", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {


//	    		if(null == ScanBlueToothSimActivity.gKsSdk)
//	    			ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(MainActivity.this.getApplication(), this);
            //ScanBlueToothSimActivity.gKsSdk.disconnect();

            ShcaCciStd.gSdk = null;

            if (LaunchActivity.LOG_FLAG)
                LaunchActivity.logUtil.destory();

            System.exit(0);
        }

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            if (System.currentTimeMillis() - exitTime > 2000) {
//                Toast.makeText(getApplicationContext(), "再按一次将退出中煤易投", Toast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            } else {
//
//
////	    		if(null == ScanBlueToothSimActivity.gKsSdk)
////	    			ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(MainActivity.this.getApplication(), this);
//                //ScanBlueToothSimActivity.gKsSdk.disconnect();
//
//                ShcaCciStd.gSdk = null;
//
//                if (LaunchActivity.LOG_FLAG)
//                    LaunchActivity.logUtil.destory();
//
//                System.exit(0);
//            }
//
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    Fragment certFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        //setIntent(intent);
        //Toast.makeText(getApplicationContext(), "onNewIntent", Toast.LENGTH_SHORT).show();


        int type = intent.getIntExtra("type", 0);
        if (type == 1) {
            RadioButton rb_seal = (RadioButton) rg.getChildAt(3);
            rb_seal.setChecked(true);
        } else {

            RadioButton rb_cert = (RadioButton) rg.getChildAt(0);
            rb_cert.setChecked(true);
        }
        EventBus.getDefault().post(new RefreshEvent());

//        homeFragment.onResume();

//        fm = getSupportFragmentManager();
//        FragmentTransaction transaction = fm.beginTransaction();
//        homeFragment = FragmentFactory.getInstanceByIndex(R.id.rb_home);
//        transaction.add(R.id.content, homeFragment, "HomeFragment");
//        //transaction.addToBackStack(null);//防止扫码闪退
//        transaction.commit();
//
//        rg = (RadioGroup) findViewById(R.id.rg_menu);
//        RadioButton rb_home = (RadioButton) rg.getChildAt(0);
//        rb_home.setChecked(true);
//        homeFragment.onResume();
//        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(final RadioGroup group, int checkedId) {
//                nowid=R.id.rb_home;
//                FragmentTransaction transaction = fm.beginTransaction();
//                Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId);
//                transaction.replace(R.id.content, fragment);
//                //transaction.addToBackStack(null);//防止扫码闪退
//                transaction.commit();
//
//
//
//                switch (checkedId) {
//                    case R.id.rb_cert:
//                    case R.id.rb_seal:
//                        findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
//
//
//                        break;
//                    default:
//                        findViewById(R.id.tv_right).setVisibility(View.GONE);
//
//                        break;
//
//
//                }
//
//
////                fragment.onResume();
//            }
//        });

//        findViewById(R.id.tv_right).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        XGPushClickedResult click = XGPushManager.onActivityStarted(this);
        FragmentFactory.getInstanceByIndex(nowid).onResume();
//        if (!bUpdated) {
//
//            checkVersion();
//            bUpdated = true;
//        }

    }

    boolean isNewQrCode = false;//兼容新旧二维码标识

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (10086 == requestCode) {
            LaunchActivity.updateuUtil.installNewApk();
            return;
        }

        if (requestCode == CAPTURE_CODE) {
            // 处理扫描结果（在界面上显示）
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();

                final String str = bundle.getString("result");
                if (str.length() == 0) {
                    Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONObject test = JSONObject.fromObject(str);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_SHORT).show();

                    return;
                }
                final JSONObject jb = JSONObject.fromObject(str);
                if (jb.containsKey("appID") && jb.optString("appID").length() > 0) {


                    final UniTrust uniTrust = new UniTrust(MainActivity.this, false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String param = ParamGen.IsValidApplication(MainActivity.this, jb.optString("appID"));
                            final String result = uniTrust.IsValidApplication(param);
                            APPResponse response = new APPResponse(result);
                            final int retCode = response.getReturnCode();
                            final String retMsg = response.getReturnMsg();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                                        if (jb.containsKey(CommonConst.PARAM_QR_CODE)) {
                                            qrCodeSN = jb.optString(CommonConst.PARAM_QR_CODE);
                                            GetQRCodeInfo(jb.optString(CommonConst.PARAM_QR_CODE));//暂定
                                        } else {

                                            try {


//                                                final JSONObject jb = response.getResult();

                                                if (jb.containsKey("randomNumber")) {
                                                    scanMsg = jb.optString("randomNumber");
                                                } else if (jb.containsKey("message")) {
                                                    scanMsg = jb.optString("message");
                                                } else if (jb.containsKey("messages")) {
                                                    scanMsg = jb.optString("messages");
                                                }


                                                if (jb.containsKey("appID") && jb.optString("appID").length() > 0) {

                                                    final UniTrust uniTrust = new UniTrust(MainActivity.this, false);
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            String param = ParamGen.IsValidApplication(MainActivity.this, jb.optString("appID"));
                                                            String result = uniTrust.IsValidApplication(param);
                                                            APPResponse response = new APPResponse(result);
                                                            final int retCode = response.getReturnCode();
                                                            final String retMsg = response.getReturnMsg();
                                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                                                                        String strReslut = str;

                                                                        if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
                                                                            sealScan(strReslut);

                                                                            return;
                                                                        }

                                                                        try {

                                                                            if (jb.containsKey("appAlg")) {
                                                                                if (jb.optString("appAlg").equals("SM2")) {
                                                                                    rsaType = 1;

                                                                                } else if (jb.optString("appAlg").equals("RSA")) {
                                                                                    rsaType = 2;
                                                                                }
                                                                            } else {
                                                                                rsaType = 0;
                                                                            }
                                                                        } catch (Exception e) {
                                                                            rsaType = 0;
                                                                        }


                                                                        if (strReslut.indexOf("?") == -1) {
                                                                            strReslut = parseJSONScanResult(strReslut);
                                                                        } else {
                                                                            isJSONDate = false;
                                                                        }


                                                                        final String scanResult = strReslut;
                                                                        strScanResult = scanResult;


                                                                        try {
//
//					certController.scan(MainActivity.this,strScanResult);
                                                                            if ("-1".equals(scanResult)) {
                                                                                throw new Exception("二维码格式解析异常");
                                                                            }

                                                                            final String urlPath = WebUtil.getUrlPath(scanResult);

                                                                            if ("".equals(urlPath)) {
                                                                                throw new Exception("二维码格式错误");
                                                                            }

                                                                            Map<String, String> params = WebUtil
                                                                                    .getURLRequest(scanResult);

                                                                            String type = params.get(CommonConst.PARAM_TYPE);

                                                                            if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
                                                                                showScanCert(scanResult);
                                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
                                                                                showScanCert(scanResult);
                                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
                                                                                showScanCert(scanResult);
                                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
                                                                                showScanCert(scanResult);
                                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {

                                                                                showScanCert(scanResult);
                                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
                                                                                showScanSeal(scanResult);
                                                                            } else {
                                                                                throw new Exception("二维码内容错误");
                                                                            }

                                                                        } catch (Exception e) {
                                                                            Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_LONG).show();
                                                                            return;
                                                                        }


                                                                    } else {


                                                                        Toast.makeText(MainActivity.this, "获取应用状态失败", Toast.LENGTH_LONG).show();
                                                                        return;
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }).start();

                                                } else if (jb.containsKey("appID") && jb.optString("appID").length() == 0) {
                                                    Toast.makeText(MainActivity.this, "应用不合法", Toast.LENGTH_LONG).show();
                                                    return;
                                                } else {
                                                    String strReslut = str;

                                                    if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
                                                        sealScan(strReslut);

                                                        return;
                                                    }


                                                    if (strReslut.indexOf("?") == -1) {
                                                        strReslut = parseJSONScanResult(strReslut);
                                                    } else {
                                                        isJSONDate = false;
                                                    }


                                                    final String scanResult = strReslut;
                                                    strScanResult = scanResult;


                                                    try {
//
//					certController.scan(MainActivity.this,strScanResult);
                                                        if ("-1".equals(scanResult)) {
                                                            throw new Exception("二维码格式解析异常");
                                                        }

                                                        final String urlPath = WebUtil.getUrlPath(scanResult);

                                                        if ("".equals(urlPath)) {
                                                            throw new Exception("二维码格式错误");
                                                        }

                                                        Map<String, String> params = WebUtil
                                                                .getURLRequest(scanResult);

                                                        String type = params.get(CommonConst.PARAM_TYPE);

                                                        if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {

                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
                                                            showScanSeal(scanResult);
                                                        } else {
                                                            throw new Exception("二维码内容错误");
                                                        }

                                                    } catch (Exception e) {
                                                        Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
                                                }


                                            } catch (Exception e) {
                                                Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_LONG).show();
                                                return;

                                            }

                                        }
                                    } else {


                                        Toast.makeText(MainActivity.this, "获取应用状态失败", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            });

                        }
                    }).

                            start();


                } else if (jb.containsKey("appID") && jb.optString("appID").length() == 0) {
                    Toast.makeText(MainActivity.this, "应用不合法", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (jb.containsKey(CommonConst.PARAM_QR_CODE)) {
                        qrCodeSN = jb.optString(CommonConst.PARAM_QR_CODE);
                        GetQRCodeInfo(jb.optString(CommonConst.PARAM_QR_CODE));//暂定
                    } else {

                        try {


                            if (jb.containsKey("randomNumber")) {
                                scanMsg = jb.optString("randomNumber");
                            } else if (jb.containsKey("message")) {
                                scanMsg = jb.optString("message");
                            } else if (jb.containsKey("messages")) {
                                scanMsg = jb.optString("messages");
                            }


                            if (jb.containsKey("appID") && jb.optString("appID").length() > 0) {

                                final UniTrust uniTrust = new UniTrust(MainActivity.this, false);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String param = ParamGen.IsValidApplication(MainActivity.this, jb.optString("appID"));
                                        String result = uniTrust.IsValidApplication(param);
                                        APPResponse response = new APPResponse(result);
                                        final int retCode = response.getReturnCode();
                                        final String retMsg = response.getReturnMsg();
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                                                    String strReslut = str;

                                                    if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
                                                        sealScan(strReslut);

                                                        return;
                                                    }


                                                    try {

                                                        if (jb.containsKey("appAlg")) {
                                                            if (jb.optString("appAlg").equals("SM2")) {
                                                                rsaType = 1;

                                                            } else if (jb.optString("appAlg").equals("RSA")) {
                                                                rsaType = 2;
                                                            }
                                                        } else {
                                                            rsaType = 0;
                                                        }
                                                    } catch (Exception e) {
                                                        rsaType = 0;
                                                    }


                                                    if (strReslut.indexOf("?") == -1) {
                                                        strReslut = parseJSONScanResult(strReslut);
                                                    } else {
                                                        isJSONDate = false;
                                                    }


                                                    final String scanResult = strReslut;
                                                    strScanResult = scanResult;


                                                    try {
//
//					certController.scan(MainActivity.this,strScanResult);
                                                        if ("-1".equals(scanResult)) {
                                                            throw new Exception("二维码格式解析异常");
                                                        }

                                                        final String urlPath = WebUtil.getUrlPath(scanResult);

                                                        if ("".equals(urlPath)) {
                                                            throw new Exception("二维码格式错误");
                                                        }

                                                        Map<String, String> params = WebUtil
                                                                .getURLRequest(scanResult);

                                                        String type = params.get(CommonConst.PARAM_TYPE);

                                                        if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {

                                                            showScanCert(scanResult);
                                                        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
                                                            showScanSeal(scanResult);
                                                        } else {
                                                            throw new Exception("二维码内容错误");
                                                        }

                                                    } catch (Exception e) {
                                                        Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_LONG).show();
                                                        return;
                                                    }


                                                } else {


                                                    Toast.makeText(MainActivity.this, "获取应用状态失败", Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                            }
                                        });

                                    }
                                }).start();

                            } else if (jb.containsKey("appID") && jb.optString("appID").length() == 0) {
                                Toast.makeText(MainActivity.this, "应用不合法", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                String strReslut = str;

                                if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
                                    sealScan(strReslut);

                                    return;
                                }
                                try {

                                    if (jb.containsKey("appAlg")) {
                                        if (jb.optString("appAlg").equals("SM2")) {
                                            rsaType = 1;

                                        } else if (jb.optString("appAlg").equals("RSA")) {
                                            rsaType = 2;
                                        }
                                    } else {
                                        rsaType = 0;
                                    }
                                } catch (Exception e) {
                                    rsaType = 0;
                                }


                                if (strReslut.indexOf("?") == -1) {
                                    strReslut = parseJSONScanResult(strReslut);
                                } else {
                                    isJSONDate = false;
                                }


                                final String scanResult = strReslut;
                                strScanResult = scanResult;


                                try {
//
//					certController.scan(MainActivity.this,strScanResult);
                                    if ("-1".equals(scanResult)) {
                                        throw new Exception("二维码格式解析异常");
                                    }

                                    final String urlPath = WebUtil.getUrlPath(scanResult);

                                    if ("".equals(urlPath)) {
                                        throw new Exception("二维码格式错误");
                                    }

                                    Map<String, String> params = WebUtil
                                            .getURLRequest(scanResult);

                                    String type = params.get(CommonConst.PARAM_TYPE);

                                    if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
                                        showScanCert(scanResult);
                                    } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
                                        showScanCert(scanResult);
                                    } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
                                        showScanCert(scanResult);
                                    } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {

                                        showScanCert(scanResult);
                                    } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
                                        showScanCert(scanResult);
                                    } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
                                        showScanSeal(scanResult);
                                    } else {
                                        throw new Exception("二维码内容错误");
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }


                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "扫码失败，请重启后再试", Toast.LENGTH_LONG).show();
                            return;

                        }

                    }

                }


            }
        } else if (requestCode == SCAN_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                doScan(bundle.getString("ServiecNo"),
                        bundle.getString("OriginInfo"),
                        bundle.getString("Sign"),
                        bundle.getString("Cert"),
                        bundle.getString("CertSN"),
                        bundle.getString("UniqueID"),
                        bundle.getString("CertType"),
                        bundle.getString("SaveType"),
                        bundle.getString("AppID"),
                        bundle.getString("MsgWrapper"),
                        bundle.getString("encCert")
                );
            }
            if (resultCode == RESULT_CANCELED) {

            }
        } else if (requestCode == SCAN_SEAL_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                doSealScan(bundle.getString("ServiecNo"),
                        bundle.getString("OriginInfo"),
                        bundle.getString("CertSN"),
                        bundle.getString("SealSN"),
                        bundle.getString("AppID"),
                        bundle.getString("MsgWrapper"));
            }
            if (resultCode == RESULT_CANCELED) {
                Bundle bundle = data.getExtras();
                if (null != bundle.getString("SealSN") && ("".equals(bundle.getString("SealSN"))))
                    Toast.makeText(MainActivity.this, "签章证书不匹配或不存在", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MainActivity.this, "操作取消", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == SEAL_SIGN) {
            if (resultCode == RESULT_OK) {
//                doInputCert();

//                String msg=AccountHelper.getRealName(MainActivity.this)+"在"+CommUtil.getNowTime()+"做了印章签署";
//                uploadLogRecord("6", "印章签署");
//                uploadLogRecord("6", data.getStringExtra("result"));

            } else {

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showScanSeal(final String scanResult) {
//        try {
//            mSealData = getSealData();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Toast.makeText(context, "无印章,请先申请印章", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (mSealData.size() == 0) {
//            Toast.makeText(MainActivity.this, "无印章,请先申请印章", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Map<String, String> params = WebUtil.getURLRequest(scanResult);
        final String urlPath = WebUtil.getUrlPath(scanResult);
        String certsn = "";

        if (null != params.get(CommonConst.PARAM_CERTSN))
            certsn = params.get(CommonConst.PARAM_CERTSN);
        if (null != params.get(CommonConst.PARAM_ENCRYPT_CERTSN))
            certsn = params.get(CommonConst.PARAM_ENCRYPT_CERTSN);

        Intent intent = new Intent(MainActivity.this, SealListActivity.class);
        Bundle extras = new Bundle();
        extras.putString("ScanDao", "scan");
        extras.putString("ServiecNo", params.get(CommonConst.PARAM_BIZSN));
        if (isJSONDate)
            extras.putString("IsJson", "isJson");

        if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_RANDOM_NUMBER));
            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码登录");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "1");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
            if (isSignEx) {
                try {
                    extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
                }

                if ("".equals(strScanAppName))
                    extras.putString("AppName", "批量签名");
                else
                    extras.putString("AppName", strScanAppName);
                extras.putString("OperateState", "3");
                if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                    extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
                else
                    extras.putString("MsgWrapper", "0");
            }

            if (isSignEx)
                extras.putString("IsSignEx", "isSignEx");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGNEX;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
            try {
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
            }

            extras.putString("CertSN", certsn);

            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码解密");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "4");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
            try {
                //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
                //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
                //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
                //extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            }
            extras.putString("CertSN", certsn);

            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码签名");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "2");
            if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
            else
                extras.putString("MsgWrapper", "0");

            operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码签章");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "5");
            if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
            else
                extras.putString("MsgWrapper", "0");

            extras.putString("CertSN", certsn);

            operatorType = CommonConst.CERT_OPERATOR_TYPE_SEAL;
        }

        intent.putExtras(extras);
        startActivityForResult(intent, SCAN_SEAL_CODE);
    }


    private void doScan(final String token, final String orgDate, final String signDate, final String cert, final String certSN, final String uniID, final String certType, final String saveType, final String appID, final String msgWrapper, final String encCert) {
        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE,
                        "Login:TAG");
        wakeLock.acquire();

        progDialog = new ProgressDialog(MainActivity.this);
        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN)
            progDialog.setMessage("正在签名...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN)
            progDialog.setMessage("正在登录...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT)
            progDialog.setMessage("正在解密...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX)
            progDialog.setMessage("正在批量签名...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
            progDialog.setMessage("正在签章...");

        progDialog.setCancelable(false);
        progDialog.show();

        new Thread() {
            @Override
            public void run() {
                if (signDate != null) {
                    Map<String, String> postParams = new HashMap<String, String>();
                    String postHttpParams = "";
                    JSONObject jo = null;

                    try {
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN || operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_ENCODE) {

                            postParams.put("qrCodeSN", qrCodeSN);
                            postParams.put("encCert", encCert);


                            postParams.put("bizSN", token);
                            postParams.put("appID", appID);
//                            postParams.put("idNumber", uniID);
                            try {
                                String enceUniID = com.sheca.umplus.util.PKIUtil.envelopeByPublicKey(uniID, CommonConst.UM_APP_PUBLIC_KEY);


                                postParams.put("idNumber", enceUniID.length() == 0 ? uniID : enceUniID);

                            } catch (Exception e) {
                                postParams.put("idNumber", uniID);
                            }
                            postParams.put("cert", cert);
                            postParams.put("message", orgDate);
                            postParams.put("signature", signDate);
                            postParams.put("msgWrapper", msgWrapper);
                            postParams.put("certSN", certSN);
                            postParams.put("signatureValue", signDate);

                            postHttpParams += "bizSN=" + URLEncoder.encode(token, "UTF-8") +
                                    "&appID=" + URLEncoder.encode(appID, "UTF-8") +
                                    "&idNumber=" + URLEncoder.encode(uniID, "UTF-8") +
                                    "&cert=" + URLEncoder.encode(cert, "UTF-8") +
                                    "&message=" + URLEncoder.encode(orgDate, "UTF-8") +
                                    "&signature=" + URLEncoder.encode(signDate, "UTF-8") +
                                    "&signatureValue=" + URLEncoder.encode(signDate, "UTF-8") +
                                    "&msgWrapper=" + URLEncoder.encode(msgWrapper, "UTF-8") +

                                    "&qrCodeSN=" + URLEncoder.encode(qrCodeSN, "UTF-8") +
                                    "&encCert=" + URLEncoder.encode(encCert, "UTF-8") +

                                    "&certSN=" + URLEncoder.encode(certSN, "UTF-8");


                            if (isJSONDate) {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);


                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);

                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8")
                                                + "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8")
                                        ;
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                } else if (!certType.contains("SM2")) {

                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");

                                    postParams.put("certType", "" + certType);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + certType, "UTF-8");
                                }
                            } else {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.CERT_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.CERT_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);

                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8");
                                } else if (!certType.contains("SM2")) {

                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");

                                    postParams.put("certType", "" + certType);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + certType, "UTF-8");
                                }
                            }
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN) {
                            postParams.put("bizSN", token);
                            postParams.put("appID", appID);
                            postParams.put("qrCodeSN", qrCodeSN);

//                            postParams.put("idNumber", uniID);

                            try {
                                String enceUniID = com.sheca.umplus.util.PKIUtil.envelopeByPublicKey(uniID, CommonConst.UM_APP_PUBLIC_KEY);


                                postParams.put("idNumber", enceUniID.length() == 0 ? uniID : enceUniID);
                            } catch (Exception e) {
                                postParams.put("idNumber", uniID);
                            }

                            String accountUid = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_ACCOUNT_UID);

                            postParams.put("randomNumber", orgDate);
                            postParams.put("message", orgDate);
                            postParams.put("cert", cert);
                            postParams.put("signature", signDate);
                            postParams.put("signatureValue", signDate);
                            postParams.put("certSN", certSN);
                            postParams.put("accountUID", accountUid);


                            postHttpParams += "bizSN=" + URLEncoder.encode(token, "UTF-8") +
                                    "&appID=" + URLEncoder.encode(appID, "UTF-8") +
                                    "&qrCodeSN=" + URLEncoder.encode(qrCodeSN, "UTF-8") +
                                    "&idNumber=" + URLEncoder.encode(uniID, "UTF-8") +
                                    "&randomNumber=" + URLEncoder.encode(orgDate, "UTF-8") +
                                    "&message=" + URLEncoder.encode(orgDate, "UTF-8") +
                                    "&cert=" + URLEncoder.encode(cert, "UTF-8") +
                                    "&signature=" + URLEncoder.encode(signDate, "UTF-8") +
                                    "&certSN=" + URLEncoder.encode(certSN, "UTF-8") +
                                    "&accountUID=" + URLEncoder.encode(accountUid, "UTF-8") +
                                    "&signatureValue=" + URLEncoder.encode(signDate, "UTF-8")
                            ;

                            if (isJSONDate) {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8")
                                        ;
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                } else if (!certType.contains("SM2")) {

                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");

                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                }
                            } else {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.CERT_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_PERSONAL);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        postParams.put("sigAlg", CommonConst.CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
                                    } else {
                                        postParams.put("sigAlg", CommonConst.USE_CERT_ALG_RSA);
                                        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
                                        postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8") +
                                                "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
                                    }
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    postParams.put("sigAlg", CommonConst.CERT_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8");
                                } else if (!certType.contains("SM2")) {

                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");

                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postHttpParams += "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
                                } else {
                                    postParams.put("sigAlg", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postParams.put("certType", "" + CommonConst.ACCOUNT_TYPE_COMPANY);
                                    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
                                    postHttpParams += "&sigAlg=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8") +
                                            "&certType=" + URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8") +
                                            "&signatureAlgorithm=" + URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8");
                                }
                            }
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX) {
                            jo = new JSONObject();

                            jo.put("qrCodeSN", URLEncoder.encode(qrCodeSN, "UTF-8"));
                            jo.put("encCert", URLEncoder.encode(encCert, "UTF-8"));

                            jo.put("bizSN", URLEncoder.encode(token, "UTF-8"));
                            jo.put("appID", URLEncoder.encode(appID, "UTF-8"));
//                            jo.put("idNumber", URLEncoder.encode(uniID, "UTF-8"));

                            try {
                                String enceUniID = com.sheca.umplus.util.PKIUtil.envelopeByPublicKey(uniID, CommonConst.UM_APP_PUBLIC_KEY);


                                jo.put("idNumber", enceUniID.length() == 0 ? URLEncoder.encode(uniID, "UTF-8") : URLEncoder.encode(enceUniID, "UTF-8"));
//

                            } catch (Exception e) {
                                jo.put("idNumber", URLEncoder.encode(uniID, "UTF-8"));
                            }

                            jo.put("cert", URLEncoder.encode(cert, "UTF-8"));
                            jo.put("msgWrapper", URLEncoder.encode(msgWrapper, "UTF-8"));
                            jo.put("certSN", URLEncoder.encode(certSN, "UTF-8"));


                            String orgMsg = orgDate;
                            if (orgMsg.endsWith(CommonConst.UM_SPLIT_STR))
                                orgMsg = orgMsg.substring(0, orgMsg.lastIndexOf(CommonConst.UM_SPLIT_STR));
                            ArrayList<String> arrayList = new ArrayList<String>();
                            for (int i = 0; i < orgMsg.split(CommonConst.UM_SPLIT_STR).length; i++) {
                                arrayList.add(i, URLEncoder.encode(orgMsg.split(CommonConst.UM_SPLIT_STR)[i], "UTF-8"));
                            }
                            jo.element("messages", arrayList);

                            orgMsg = signDate;
                            arrayList = new ArrayList<String>();
                            for (int i = 0; i < orgMsg.split(CommonConst.UM_SPLIT_STR).length; i++) {
                                arrayList.add(i, URLEncoder.encode(orgMsg.split(CommonConst.UM_SPLIT_STR)[i], "UTF-8"));
                            }
                            jo.element("signature", arrayList);
                            jo.element("signatureValues", arrayList);

                            if (isJSONDate) {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8"));
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8"));
                                    } else {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                    }
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8"));
                                    } else {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                    }
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8"));
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8"));
                                } else if (!certType.contains("SM2")) {//RSA证书

                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));

                                    jo.put("certType", URLEncoder.encode("" + certType, "UTF-8"));
                                }


                            } else {
                                if (CommonConst.CERT_TYPE_SM2.equals(certType)) {
                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8"));
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_RSA.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8"));
                                    } else {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8"));
                                    }
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
                                    if (("" + CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || ("" + CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)) {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8"));
                                    } else {
                                        jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8"));
                                        jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8"));
                                    }
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8"));
                                } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)) {
                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8"));
                                    jo.put("certType", URLEncoder.encode("" + CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8"));
                                } else if (!certType.contains("SM2")) {//RSA证书

                                    jo.put("sigAlg", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));
                                    jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8"));

                                    jo.put("certType", URLEncoder.encode("" + certType, "UTF-8"));
                                }
                            }
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT) {

                            postParams.put("qrCodeSN", qrCodeSN);

                            postParams.put("bizSN", token);
                            postParams.put("appID", appID);
//                            postParams.put("encryptData", signDate);
//                            postParams.put("message", orgDate);
                            postParams.put("message", signDate);

                            try {
                                String encData = com.sheca.umplus.util.PKIUtil.envelopeByPublicKey(orgDate, CommonConst.UM_APP_PUBLIC_KEY);

                                encData = encData.length() == 0 ? URLEncoder.encode(orgDate, "UTF-8") : URLEncoder.encode(encData, "UTF-8");


                                postHttpParams += "bizSN=" + URLEncoder.encode(token, "UTF-8") +
                                        "&appID=" + URLEncoder.encode(appID, "UTF-8") +
//                                        "&encryptData=" + URLEncoder.encode(signDate, "UTF-8") +

                                        "&qrCodeSN=" + URLEncoder.encode(qrCodeSN, "UTF-8") +

                                        "&message=" + URLEncoder.encode(signDate, "UTF-8");


                            } catch (Exception e) {
                                postHttpParams += "bizSN=" + URLEncoder.encode(token, "UTF-8") +
                                        "&appID=" + URLEncoder.encode(appID, "UTF-8") +
//                                        "&encryptData=" + URLEncoder.encode(signDate, "UTF-8") +

                                        "&qrCodeSN=" + URLEncoder.encode(qrCodeSN, "UTF-8") +

                                        "&message=" + URLEncoder.encode(signDate, "UTF-8");

                            }
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL) {
                            String sealSignDate = "";
                            sealSignDate = String.format("%s&%s&%s&%s&%s",
                                    CommonConst.SEAL_SCAN_APP_CODE,
                                    token,
                                    signDate,
                                    certSN,
                                    CommonConst.SEAL_SCAN_APP_PWD);
                            sealSignDate = PKIUtil.getSHADigest(sealSignDate, "SHA-256", "SUN");
                            postHttpParams += "AppCode=" + URLEncoder.encode(CommonConst.SEAL_SCAN_APP_CODE, "UTF-8") +
                                    "&BizSN=" + URLEncoder.encode(token, "UTF-8") +
                                    "&HashSign=" + URLEncoder.encode(signDate, "UTF-8") +
                                    "&CertSN=" + URLEncoder.encode(certSN, "UTF-8") +
                                    "&SignatureValue=" + URLEncoder.encode(sealSignDate, "UTF-8");
                        }
                    } catch (Exception e) {
                        Log.e(CommonConst.TAG, e.getMessage(), e);
                        // Toast.makeText(UniTrustMobileActivity.this,
                        // "上传签名错误", Toast.LENGTH_LONG).show();
                        handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }

                    try {
                        final String urlPath = WebUtil.getUrlPath(strScanResult);
                        String strPostUrlPath = urlPath.substring(0, urlPath.lastIndexOf("/"));
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
                            strPostUrlPath = strScanSealResult;

                        String sResp = null;
                        //sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX) {
                            WebClientUtil.mBScanPost = true;
                            sResp = WebClientUtil.postJsonArray(strPostUrlPath, jo.toString(), 5000);
                        } else {
                            WebClientUtil.mBScanPost = true;
                            sResp = WebClientUtil.postHttpClientJson(strPostUrlPath, postHttpParams, 5000);
                        }

                        strPrint = sResp;
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL) {
                            if ("".equals(strPrint) || null == strPrint || "null".equals(strPrint)) {
                                handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
                            } else {
                                JSONObject jb = JSONObject.fromObject(strPrint);
                                String resultStr = "";
                                String returnStr = "";
                                String returnStrBizSN = "";
                                String returnStrPdfHash = "";
                                String returnStrCertSN = "";
                                String returnStrSignUrl = "";
                                String strReturn = "";

                                if (jb.containsKey("RetCode"))
                                    resultStr = jb.getString("RetCode");
                                if (jb.containsKey("RetMsg"))
                                    returnStr = jb.getString("RetMsg");

                                if ("1".equals(resultStr)) {
                                    if (jb.containsKey("BizSN"))
                                        returnStrBizSN = jb.getString("BizSN");
								   /*
								   if(!returnStrBizSN.equals(token))
									   handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
								   */
                                    if (jb.containsKey("PdfHash"))
                                        returnStrPdfHash = jb.getString("PdfHash");
                                    if (jb.containsKey("CertSN"))
                                        returnStrCertSN = jb.getString("CertSN");
                                    if (jb.containsKey("SignUrl"))
                                        returnStrSignUrl = jb.getString("SignUrl");

                                    if (null == returnStrPdfHash || "null".equals(returnStrPdfHash)) {
                                        handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
                                    } else {
                                        if (!"".equals(returnStrPdfHash)) {
                                            if (scanSignCount < 0) {
                                                handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
                                                return;
                                            }

                                            scanSealNum++;
                                            if (scanSealNum <= scanSignCount) {
                                                isSealSign = true;
                                                SealSignUtil.context = MainActivity.this;
                                                SealSignUtil.activity = MainActivity.this;
                                                strScanSealResult = returnStrSignUrl;
                                                SealSignUtil.strBizSN = returnStrBizSN;
                                                SealSignUtil.strOrgDate = returnStrPdfHash;
                                                SealSignUtil.strCertSN = certSN;
                                                SealSignUtil.strUniID = uniID;
                                                SealSignUtil.strAccountName = accountDao.getLoginAccount().getName();
                                                SealSignUtil.strAppID = appID;
                                                //SealSignUtil.strMsgWrapper ="1";
                                                handler.sendEmptyMessage(SEAL_SIGN_SCAN_SUCCESS);
                                                return;
                                            } else
                                                isSealSign = false;
                                        } else {
                                            isSealSign = false;
                                        }
                                    }
                                } else {
                                    strScanErr = returnStr;
                                    handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
                                }
                            }
                        }

                        int signALg = 1;
                        if (CommonConst.CERT_TYPE_SM2.equals(certType) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType))
                            signALg = 2;
						/*if (!"ok".equals(sResp)) {
							Message msg = new Message();
							msg.what = FAILURE;
							Bundle data = new Bundle();
							data.putString("result", sResp);
							msg.setData(data);
							handler.sendMessage(msg);
						} else {*/
                        //showMessage();
                        WebClientUtil.mBScanPost = false;

                        if ("".equals(strPrint) || null == strPrint || "null".equals(strPrint)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                Toast.makeText(MainActivity.this,"失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                            if (progDialog.isShowing()) {
                                progDialog.dismiss();
                            }

                            if (wakeLock != null) {
                                wakeLock.release();
                            }

                            return;

                        }

                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN) {
                            if (isJSONDate) {
//                                saveLog(OperationLog.LOG_TYPE_SIGN,
//                                        certSN,
//                                        orgDate,
//                                        orgDate,
//                                        signDate, signALg);
                            } else {
//                                saveLog(OperationLog.LOG_TYPE_SIGN,
//                                        certSN,
//                                        new String(Base64.decode(URLDecoder.decode(orgDate, "UTF-8"))),
//                                        new String(Base64.decode(URLDecoder.decode(orgDate, "UTF-8"))),
//                                        signDate, signALg);
                            }


                            handler.sendEmptyMessage(SIGN_SUCCESS);
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_ENCODE) {
                            if (isJSONDate) {
//                                saveLog(OperationLog.LOG_TYPE_SIGN,
//                                        certSN,
//                                        orgDate,
//                                        orgDate,
//                                        signDate, signALg);
                            } else {
//                                saveLog(OperationLog.LOG_TYPE_SIGN,
//                                        certSN,
//                                        new String(Base64.decode(URLDecoder.decode(orgDate, "UTF-8"))),
//                                        new String(Base64.decode(URLDecoder.decode(orgDate, "UTF-8"))),
//                                        signDate, signALg);
                            }

                            handler.sendEmptyMessage(ENVELOPE_ENCODE_SUCCESS);
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN) {
//                            saveLog(OperationLog.LOG_TYPE_LOGIN,
//                                    certSN,
//                                    token,
//                                    urlPath,
//                                    signDate, signALg);

                            handler.sendEmptyMessage(LOGIN_SUCCESS);
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX) {
                            if (isJSONDate) {
//                                saveLog(OperationLog.LOG_TYPE_DAO_SIGNEX,
//                                        certSN,
//                                        orgDate.substring(0, orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),
//                                        orgDate.substring(0, orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),
//                                        signDate, signALg);
                            } else {
//                                saveLog(OperationLog.LOG_TYPE_DAO_SIGNEX,
//                                        certSN,
//                                        new String(Base64.decode(URLDecoder.decode(orgDate.substring(0, orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)), "UTF-8"))),
//                                        new String(Base64.decode(URLDecoder.decode(orgDate.substring(0, orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)), "UTF-8"))),
//                                        signDate, signALg);

                            }

                            handler.sendEmptyMessage(SIGNEX_SUCCESS);
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT) {
//                            saveLog(OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT,
//                                    certSN,
//                                    orgDate,
//                                    orgDate,
//                                    signDate, signALg);

                            handler.sendEmptyMessage(ENVELOP_DECRYPT_SUCCESS);
                        } else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL) {
                            handler.sendEmptyMessage(SEAL_SCAN_SUCCESS);
                        }

                        //}
                    } catch (Exception e) {
//                        Log.e(CommonConst.TAG, e.getMessage(),
//                                e);
                        // Toast.makeText(UniTrustMobileActivity.this,
                        // "上传签名错误", Toast.LENGTH_LONG).show();
                        handler.sendEmptyMessage(LOGIN_UPLOAD_FAILURE);
                    }
                }

                if (progDialog.isShowing()) {
                    progDialog.dismiss();
                }

                if (wakeLock != null) {
                    wakeLock.release();
                }
            }
        }.start();
    }

    private void doSealScan(final String serviceNo, final String orgDate, final String certSN, final String sealSN, final String appID, final String msgWrapper) {
        wakeLock = ((PowerManager)
                getSystemService(POWER_SERVICE))
                .newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE,
                        "Login:TAG");
        wakeLock.acquire();

        progDialog = new ProgressDialog(MainActivity.this);
        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN)
            progDialog.setMessage("正在签名...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN)
            progDialog.setMessage("正在登录...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT)
            progDialog.setMessage("正在解密...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX)
            progDialog.setMessage("正在批量签名...");
        else if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
            progDialog.setMessage("正在提交签章数据...");

        progDialog.setCancelable(false);
        progDialog.show();

        new Thread() {
            @Override
            public void run() {
                if (sealSN != null) {
                    String postHttpParams = "";
                    JSONObject jo = null;

                    try {
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL) {
                            String signDate = "";
                            signDate = String.format("%s&%s&%s&%s",
                                    CommonConst.SEAL_SCAN_APP_CODE,
                                    serviceNo,
                                    sealSN,
                                    CommonConst.SEAL_SCAN_APP_PWD);
                            signDate = PKIUtil.getSHADigest(signDate, "SHA-256", "SUN");
                            postHttpParams += "AppCode=" + URLEncoder.encode(CommonConst.SEAL_SCAN_APP_CODE, "UTF-8") +
                                    "&BizSN=" + URLEncoder.encode(serviceNo, "UTF-8") +
                                    "&SealSN=" + URLEncoder.encode(sealSN, "UTF-8") +
                                    "&SignatureValue=" + URLEncoder.encode(signDate, "UTF-8");
                        }
                    } catch (Exception e) {
                        Log.e(CommonConst.TAG, e.getMessage(), e);
                        // Toast.makeText(UniTrustMobileActivity.this,
                        // "上传签名错误", Toast.LENGTH_LONG).show();
                        handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
                    }

                    try {
                        final String urlPath = WebUtil.getUrlPath(strScanResult);
                        String strPostUrlPath = strScanSealResult;
                        String sResp = null;
                        //sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
                        if (operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX) {
                            WebClientUtil.mBScanPost = true;
                            sResp = WebClientUtil.postJsonArray(strPostUrlPath, jo.toString(), 5000);
                        } else {
                            WebClientUtil.mBScanPost = true;
                            sResp = WebClientUtil.postHttpClientJson(strPostUrlPath, postHttpParams, 5000);
                        }

                        strPrint = sResp;
                        WebClientUtil.mBScanPost = false;

                        if ("".equals(strPrint) || null == strPrint || "null".equals(strPrint)) {
                            handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
                        } else {
                            JSONObject jb = JSONObject.fromObject(strPrint);
                            String resultStr = "";
                            String returnStr = "";
                            String returnStrBizSN = "";
                            String returnStrPdfHash = "";
                            String returnStrCertSN = "";
                            String returnStrSignUrl = "";
                            String strReturn = "";

                            if (jb.containsKey("RetCode"))
                                resultStr = jb.getString("RetCode");
                            if (jb.containsKey("RetMsg"))
                                returnStr = jb.getString("RetMsg");

                            if ("1".equals(resultStr)) {
                                if (jb.containsKey("BizSN"))
                                    returnStrBizSN = jb.getString("BizSN");
                                if (jb.containsKey("PdfHash"))
                                    returnStrPdfHash = jb.getString("PdfHash");
                                if (jb.containsKey("CertSN"))
                                    returnStrCertSN = jb.getString("CertSN");
                                if (jb.containsKey("SignUrl"))
                                    returnStrSignUrl = jb.getString("SignUrl");
                                if (jb.containsKey("SignCount"))
                                    scanSignCount = jb.getInt("SignCount");

                                if (!"".equals(returnStrSignUrl))
                                    strScanSealResult = returnStrSignUrl;

                                if ("1".equals(msgWrapper))
                                    strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_MSGWRAPPER + "=%s&" + CommonConst.PARAM_CERTSN + "=%s",
                                            returnStrSignUrl, CommonConst.QR_SEAL, returnStrBizSN, returnStrPdfHash, msgWrapper, certSN);
                                else
                                    strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_CERTSN + "=%s",
                                            returnStrSignUrl, CommonConst.QR_SEAL, returnStrBizSN, returnStrPdfHash, certSN);
                            } else {
                                strScanErr = returnStr;
                                handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
                            }

                            strScanErr = strReturn;
                            handler.sendEmptyMessage(SEALINFO_SCAN_SUCCESS);
                        }

                        //}
                    } catch (Exception e) {
                        Log.e(CommonConst.TAG, e.getMessage(),
                                e);
                        // Toast.makeText(UniTrustMobileActivity.this,
                        // "上传签名错误", Toast.LENGTH_LONG).show();
                        handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
                    }
                }

                if (progDialog.isShowing()) {
                    progDialog.dismiss();
                }

                if (wakeLock != null) {
                    wakeLock.release();
                }
            }
        }.start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = null;
            switch (msg.what) {
                case LOGIN_SIGN:
                    break;
                case LOGIN_SIGN_FAILURE:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, "数字签名错误", Toast.LENGTH_LONG).show();
                    break;
                case SEALINFO_SCAN_FAILURE:
                    progDialog.dismiss();
                    if (!"".equals(strScanErr))
                        Toast.makeText(MainActivity.this, "提交印章数据错误," + strScanErr, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this, "提交印章数据错误", Toast.LENGTH_LONG).show();
                    break;
                case SEAL_SCAN_FAILURE:
                    progDialog.dismiss();
                    if (!"".equals(strScanErr))
                        Toast.makeText(MainActivity.this, "扫码签章错误," + strScanErr, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this, "扫码签章错误", Toast.LENGTH_LONG).show();
                    break;
                case LOGIN_UPLOAD:
                    break;
                case LOGIN_UPLOAD_FAILURE:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, "上传签名日志错误,网络连接异常或无法访问更新服务", Toast.LENGTH_LONG).show();
                    break;
                case FAILURE:
                    progDialog.dismiss();
                    data = msg.getData();
                    Toast.makeText(MainActivity.this, data.getString("result"),
                            Toast.LENGTH_LONG).show();
                    break;
                case UPLOAD_P10:
                    progDialog.setMessage("正在上传P10...");
                    break;
                case DOWNLOAD_CERT:
                    progDialog.setMessage("正在下载证书...");
                    break;
                case LOGIN_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, "扫码登录成功", Toast.LENGTH_LONG).show();
//                    scanMsg=AccountHelper.getRealName(getActivity())+"在"+CommUtil.getNowTime()+"做了扫码登录";
//                    uploadLogRecord("1", scanMsg);
				/*new Thread() {
					@Override
					public void run() {
						try {
							Map<String, String> postParams = new HashMap<String, String>();
					        WebClientUtil.postJson(strPrint,postParams, 5000);
						} catch (Exception e1) {
								e1.printStackTrace();
						}
					}
				}.start();*/

                    //Toast.makeText(context, "返回数据:\n"+strPrint, Toast.LENGTH_LONG).show();

                    break;
                case SIGN_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.scan_success_sign, Toast.LENGTH_LONG).show();
//                    scanMsg=AccountHelper.getRealName(getActivity())+"在"+CommUtil.getNowTime()+"做了扫码签名";
//                    uploadLogRecord("2", scanMsg);
                    break;

                case ENVELOPE_ENCODE_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.scan_success_encode, Toast.LENGTH_LONG).show();
//                    scanMsg=AccountHelper.getRealName(getActivity())+"在"+CommUtil.getNowTime()+"做了扫码签名";
//                    uploadLogRecord("2", scanMsg);
                    break;

                case SIGNEX_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.scan_success_sign_batch, Toast.LENGTH_LONG).show();
//                    scanMsg=AccountHelper.getRealName(getActivity())+"在"+CommUtil.getNowTime()+"做了批量签名";
//                    uploadLogRecord("2", scanMsg);
                    break;
                case ENVELOP_DECRYPT_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.scan_success_decypt, Toast.LENGTH_LONG).show();
//                    scanMsg=AccountHelper.getRealName(getActivity())+"在"+CommUtil.getNowTime()+"做了扫码解密";
//                    uploadLogRecord("5", scanMsg);
                    break;
                case SEALINFO_SCAN_SUCCESS:
                    progDialog.dismiss();
                    showScanCert(strScanErr);
                    break;
                case SEAL_SCAN_SUCCESS:
                    progDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.scan_success_seal, Toast.LENGTH_LONG).show();
                    break;
                case SEAL_SIGN_SCAN_SUCCESS:
                    progDialog.dismiss();
                    SealSignUtil.sealPdfSign();
                    doScan(SealSignUtil.strBizSN,
                            SealSignUtil.strOrgDate,
                            SealSignUtil.strSignDate,
                            SealSignUtil.strCert,
                            SealSignUtil.strCertSN,
                            SealSignUtil.strUniID,
                            SealSignUtil.strCertType,
                            SealSignUtil.strSaveType,
                            SealSignUtil.strAppID,
                            SealSignUtil.strMsgWrapper,
                            SealSignUtil.encCert
                    );
                    break;
                case SAVE_CERT:
//                    data = msg.getData();
//                    String responseStr = data.getString("responseStr");
//                    final String envsn = data.getString("envsn");
//                    try {
//                        final DownloadCertResponse responseObj = null;
//                        final ChangePasswordDialog.Builder builder = new ChangePasswordDialog.Builder(
//                                MainActivity.this);
//                        builder.setMessage1("请输入证书口令");
//                        builder.setMessage2("请输入重复证书口令");
//                        builder.setTitle("提示");
//                        builder.setIcon(R.drawable.alert);
//                        builder.setNegativeButton("确定",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int which) {
//                                        String sOldPwd = builder
//                                                .getEditText1Value();
//                                        String sNewPwd = builder
//                                                .getEditText2Value();
//
//                                        if (sOldPwd == null || "".equals(sOldPwd)) {
//                                            Toast.makeText(MainActivity.this, "请输入口令",
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//
//                                        if (sNewPwd == null || "".equals(sNewPwd)) {
//                                            Toast.makeText(MainActivity.this, "请输入重复口令",
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//
//                                        if (sOldPwd.length() < 8) {
//                                            Toast.makeText(MainActivity.this, "口令长度不能小于8位",
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//
//                                        if (!sOldPwd.equals(sNewPwd)) {
//                                            Toast.makeText(MainActivity.this, "口令与重复口令不一致",
//                                                    Toast.LENGTH_LONG).show();
//                                            return;
//                                        }
//
//                                        try {
//                                            Cert cert = null;
//
//                                            cert = certDao.getCertByEnvsn(envsn, accountDao.getLoginAccount().getName());
//
//                                            String p12 = genP12(
//                                                    cert.getPrivatekey(), sOldPwd,
//                                                    responseObj.getUserCert(),
//                                                    responseObj.getCertChain());
//
//                                            cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
//                                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
//
//                                            cert.setCertificate(responseObj
//                                                    .getUserCert());
//
//                                            byte[] bCert = Base64
//                                                    .decode(responseObj
//                                                            .getUserCert());
//
//                                            String sn=PKIUtil.getCertDetail(bCert, EnumCertDetailNo.EnumCertDetailNoSN);
//
//                                            cert.setCertsn(sn);
//
//                                            cert.setCertchain(responseObj
//                                                    .getCertChain());
//                                            cert.setKeystore(p12);
//                                            cert.setPrivatekey("");
//                                            cert.setNotbeforetime(PKIUtil.getCertDetail(bCert,EnumCertDetailNo.EnumCertDetailNoNotBefore));
//                                            cert.setValidtime(PKIUtil.getCertDetail(bCert,EnumCertDetailNo.EnumCertDetailNoNotAfter));
//                                            certDao.updateCert(cert, accountDao.getLoginAccount().getName());
//
//                                            Toast.makeText(MainActivity.this, "保存证书成功",
//                                                    Toast.LENGTH_LONG).show();
//
////                                            saveLog(OperationLog.LOG_TYPE_APPLYCERT,
////                                                    cert.getCertsn(), "", "", "", 1);
//                                        } catch (Exception e) {
//                                            Log.e(CommonConst.TAG, e.getMessage(),
//                                                    e);
//                                            Toast.makeText(MainActivity.this, "保存证书失败",
//                                                    Toast.LENGTH_LONG).show();
//                                        }
//                                        dialog.dismiss();
//
//                                        closeProgDlg();
//                                    }
//                                });
//
//                        builder.setPositiveButton(
//                                "取消",
//                                new android.content.DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int which) {
//                                        dialog.dismiss();
//                                        closeProgDlg();
//                                    }
//                                });
//
//                        builder.show();
//                    } catch (Exception e) {
//                        Log.e(CommonConst.TAG, e.getMessage(), e);
//                        closeProgDlg();
//                        Toast.makeText(MainActivity.this, "保存证书失败", Toast.LENGTH_LONG).show();
//                    }

                    break;
            }
        }
    };

    //扫码签章
    private void sealScan(String result) {


        JSONObject jb = JSONObject.fromObject(result);


        String certSn = jb.optString("certSn");
        Cert cert = certDao.getCertByCertsn(certSn, AccountHelper.getUsername(MainActivity.this));

        if (cert == null) {

            Toast.makeText(MainActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
        } else {
            if (certDao.verifyCert(cert) == 0) {

                mScanToken = "";
                Intent intent = new Intent(MainActivity.this, SealSignActivity.class);
                intent.putExtra("certSn", certSn);
                intent.putExtra("result", result);
                startActivityForResult(intent, SEAL_SIGN);
            } else {
                Toast.makeText(MainActivity.this, "证书已过期,无法使用", Toast.LENGTH_LONG).show();
            }

        }
    }

    private String parseJSONScanResult(String scanResult) {
        isSignEx = false;
        String strReturn = "";
        if (!scanResult.startsWith("{"))
            scanResult = "{" + scanResult;
        if (!scanResult.endsWith("}"))
            scanResult += "}";

        try {
            JSONObject jb = JSONObject.fromObject(scanResult);
            String serviceURL = jb.getString(CommonConst.QR_SERVICEURL);
            String actionName = jb.getString(CommonConst.QR_ACTIONNAME).replace("_", "");
            String bizSN = jb.getString(CommonConst.PARAM_BIZSN);
            String message = "";
            String certSN = "";
            boolean isWrapper = false;

            if (jb.containsKey(CommonConst.PARAM_APPID)) {
                if (null != jb.getString(CommonConst.PARAM_APPID) && !"".equals(jb.getString(CommonConst.PARAM_APPID)))
                    strScanAppName = jb.getString(CommonConst.PARAM_APPID);
                else
                    strScanAppName = "";
            } else
                strScanAppName = CommonConst.UM_APPID;

            if (actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase())) {
                message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);
            } else if (actionName.toLowerCase().equals(CommonConst.QR_SignEx.toLowerCase())) {
                JSONArray transitListArray = jb.getJSONArray(CommonConst.PARAM_MESSAGES);
                for (int i = 0; i < transitListArray.size(); i++) {
                    message += transitListArray.getString(i) + CommonConst.UM_SPLIT_STR;
                }

                if (jb.containsKey(CommonConst.PARAM_MSGWRAPPER)) {
                    String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
                    if (Integer.parseInt(mWrapper) == 1)
                        isWrapper = true;
                    else
                        isWrapper = false;
                }

                isSignEx = true;
            } else if (actionName.toLowerCase().equals(CommonConst.QR_EnvelopeDecrypt.toLowerCase())) {
                message = jb.getString(CommonConst.PARAM_ENCRYPT_DATE);
                certSN = jb.getString(CommonConst.PARAM_ENCRYPT_CERTSN);
            } else if (actionName.toLowerCase().equals(CommonConst.QR_Sign.toLowerCase())) {
                if (!isSignEx)
                    message = jb.getString(CommonConst.PARAM_MESSAGE);

                if (jb.containsKey(CommonConst.PARAM_MSGWRAPPER)) {
                    String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
                    if (Integer.parseInt(mWrapper) == 1)
                        isWrapper = true;
                    else
                        isWrapper = false;
                }
            } else if (actionName.toLowerCase().equals(CommonConst.QR_EnvelopeEncode.toLowerCase())) {

                message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);

                if (jb.containsKey(CommonConst.PARAM_MSGWRAPPER)) {
                    String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
                    if (Integer.parseInt(mWrapper) == 1)
                        isWrapper = true;
                    else
                        isWrapper = false;
                }
            } else if (actionName.toLowerCase().equals(CommonConst.QR_SEAL.toLowerCase())) {
                if (jb.containsKey(CommonConst.PARAM_RANDOM_NUMBER))
                    message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);

                if (jb.containsKey(CommonConst.PARAM_MSGWRAPPER)) {
                    String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
                    if (Integer.parseInt(mWrapper) == 1)
                        isWrapper = true;
                    else
                        isWrapper = false;
                }

                if (jb.containsKey(CommonConst.PARAM_ENCRYPT_CERTSN))
                    certSN = jb.getString(CommonConst.PARAM_ENCRYPT_CERTSN);
            }

            if (actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase())) {
                strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_RANDOM_NUMBER + "=%s",
                        serviceURL, CommonConst.QR_Login, bizSN, message);
            } else if (actionName.toLowerCase().equals(CommonConst.QR_SignEx.toLowerCase())) {
                if (isWrapper)
                    strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGES + "=%s&" + CommonConst.PARAM_MSGWRAPPER + "=%s",
                            serviceURL, CommonConst.QR_SignEx, bizSN, message, "1");
                else
                    strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGES + "=%s",
                            serviceURL, CommonConst.QR_SignEx, bizSN, message);
            } else if (actionName.toLowerCase().equals(CommonConst.QR_EnvelopeDecrypt.toLowerCase())) {
                strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_ENCRYPT_DATE + "=%s&" + CommonConst.PARAM_ENCRYPT_CERTSN + "=%s",
                        serviceURL, CommonConst.QR_EnvelopeDecrypt, bizSN, message, certSN);
            } else if (actionName.toLowerCase().equals(CommonConst.QR_Sign.toLowerCase())) {
                if (!isSignEx) {
                    if (isWrapper)
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_MSGWRAPPER + "=%s",
                                serviceURL, CommonConst.QR_Sign, bizSN, message, "1");
                    else
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s",
                                serviceURL, CommonConst.QR_Sign, bizSN, message);
                }
            } else if (actionName.toLowerCase().equals(CommonConst.QR_EnvelopeEncode.toLowerCase())) {
                if (!isSignEx) {
                    if (isWrapper)
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_MSGWRAPPER + "=%s",
                                serviceURL, CommonConst.QR_EnvelopeEncode, bizSN, message, "1");
                    else
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s",
                                serviceURL, CommonConst.QR_EnvelopeEncode, bizSN, message);
                }
            } else if (actionName.toLowerCase().equals(CommonConst.QR_SEAL.toLowerCase())) {
                if (!isSignEx) {
                    if (isWrapper)
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_MSGWRAPPER + "=%s&" + CommonConst.PARAM_ENCRYPT_CERTSN + "=%s",
                                serviceURL, CommonConst.QR_SEAL, bizSN, message, "1", certSN);
                    else
                        strReturn = String.format("%s/%s?" + CommonConst.PARAM_BIZSN + "=%s&" + CommonConst.PARAM_MESSAGE + "=%s&" + CommonConst.PARAM_ENCRYPT_CERTSN + "=%s",
                                serviceURL, CommonConst.QR_SEAL, bizSN, message, certSN);
                }

                strScanSealResult = serviceURL;
            }

            isJSONDate = true;
        } catch (Exception ex) {
            strReturn = scanResult;
        }

        return strReturn;

    }

    //获取二维码序列号对应业务详细信息
    private void GetQRCodeInfo(final String result) {

        final UniTrust uniTrust = new UniTrust(MainActivity.this, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String param = ParamGen.GetQRCodeInfo(MainActivity.this, result);
                final String result = uniTrust.GetQRCodeInfo(param);
                final APPResponse response = new APPResponse(result);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (retCode != 0) {
                            Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {

                            String resOB = response.getResultStr();
                            final JSONObject jsonObject = JSONObject.fromObject(resOB);
                            JSONObject jb = JSONObject.fromObject(jsonObject.get(CommonConst.ACTION_INFO));
                            isNewQrCode = true;

                            if (jb.containsKey("randomNumber")) {
                                scanMsg = jb.optString("randomNumber");
                            } else if (jb.containsKey("message")) {
                                scanMsg = jb.optString("message");
                            } else if (jb.containsKey("messages")) {
                                scanMsg = jb.optString("messages");
                            }


//                            if (jb.containsKey("appID") && jb.optString("appID").length() > 0) {
//
//                                final UniTrust uniTrust = new UniTrust(MainActivity.this, false);
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        String param = ParamGen.IsValidApplication(MainActivity.this, jb.optString("appID"));
//                                        final String result = uniTrust.IsValidApplication(param);
//                                        final APPResponse response = new APPResponse(result);
//                                        final int retCode = response.getReturnCode();
//                                        final String retMsg = response.getReturnMsg();
//                                        MainActivity.this.runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//
//                                                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
////                                            String authRestult="true";
//                                                    String authRestult = response.getResultStr();
//
//
//                                                    if (authRestult.equals("true")) {
//                                                        String strReslut = result;
//                                                        if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
//                                                            sealScan(strReslut);
//
//                                                            return;
//                                                        }
//
//                                                        try {
//                                                            JSONObject jb = JSONObject.fromObject(strReslut);
//                                                            if (jb.containsKey("appAlg")) {
//                                                                if (jb.optString("appAlg").equals("SM2")) {
//                                                                    rsaType = 1;
//
//                                                                } else if (jb.optString("appAlg").equals("RSA")) {
//                                                                    rsaType = 2;
//                                                                }
//                                                            } else {
//                                                                rsaType = 0;
//                                                            }
//                                                        } catch (Exception e) {
//                                                            rsaType = 0;
//                                                        }
//
//                                                        if (strReslut.indexOf("?") == -1) {
//                                                            strReslut = parseJSONScanResult(strReslut);
//                                                        } else {
//                                                            isJSONDate = false;
//                                                        }
//
//
//                                                        final String scanResult = strReslut;
//                                                        strScanResult = scanResult;
//
//
//                                                        try {
//
//                                                            if ("-1".equals(scanResult)) {
//                                                                throw new Exception("二维码格式解析异常");
//                                                            }
//
//                                                            final String urlPath = WebUtil.getUrlPath(scanResult);
//
//                                                            if ("".equals(urlPath)) {
//                                                                throw new Exception("二维码格式错误");
//                                                            }
//
//                                                            Map<String, String> params = WebUtil
//                                                                    .getURLRequest(scanResult);
//
//                                                            String type = params.get(CommonConst.PARAM_TYPE);
//
//                                                            if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
//                                                                showScanCert(scanResult);
//                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
//                                                                showScanCert(scanResult);
//                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
//                                                                showScanCert(scanResult);
//                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
//                                                                showScanCert(scanResult);
//                                                            } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
//                                                                showScanSeal(scanResult);
//                                                            } else {
//                                                                throw new Exception("二维码内容错误");
//                                                            }
//
//                                                        } catch (Exception e) {
//                                                            Toast.makeText(MainActivity.this, "扫码失败", Toast.LENGTH_LONG).show();
//
//                                                        }
//                                                    } else {
//
//                                                        Toast.makeText(MainActivity.this, "应用不合法", Toast.LENGTH_LONG).show();
//
//                                                    }
//
//                                                } else {
//
//
//                                                    Toast.makeText(MainActivity.this, "获取应用状态失败", Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//                                        });
//
//                                    }
//                                }).start();
//
//                            } else if (jb.containsKey("appID") && jb.optString("appID").length() == 0) {
//                                Toast.makeText(MainActivity.this, "应用不合法", Toast.LENGTH_LONG).show();
//                            } else {
                            String strReslut = jb.toString();

                            if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
                                sealScan(strReslut);

                                return;
                            }
                            try {

                                if (jb.containsKey("appAlg")) {
                                    if (jb.optString("appAlg").equals("SM2")) {
                                        rsaType = 1;

                                    } else if (jb.optString("appAlg").equals("RSA")) {
                                        rsaType = 2;
                                    }
                                } else {
                                    rsaType = 0;
                                }
                            } catch (Exception e) {
                                rsaType = 0;
                            }

                            if (jb.containsKey(CommonConst.QR_ACTIONNAME)) {
                                String actionName = jb.getString(CommonConst.QR_ACTIONNAME).replace("_", "");
                                if (actionName.toLowerCase().equals(CommonConst.QR_EnvelopeEncode.toLowerCase())) {
                                    isEncode = true;
                                } else {
                                    isEncode = false;
                                }

                            } else {
                                isEncode = false;
                            }


                            if (strReslut.indexOf("?") == -1) {
                                strReslut = parseJSONScanResult(strReslut);
                            } else {
                                isJSONDate = false;
                            }


                            final String scanResult = strReslut;
                            strScanResult = scanResult;


                            try {
//
//					certController.scan(MainActivity.this,strScanResult);
                                if ("-1".equals(scanResult)) {
                                    throw new Exception("二维码格式解析异常");
                                }

                                final String urlPath = WebUtil.getUrlPath(scanResult);

                                if ("".equals(urlPath)) {
                                    throw new Exception("二维码格式错误");
                                }

                                Map<String, String> params = WebUtil
                                        .getURLRequest(scanResult);

                                String type = params.get(CommonConst.PARAM_TYPE);


                                if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {

                                    showScanCert(scanResult);
                                } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {

                                    showScanCert(scanResult);
                                } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {

                                    showScanCert(scanResult);
                                } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
                                    showScanCert(scanResult);
                                } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {

                                    showScanCert(scanResult);
                                } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
                                    showScanSeal(scanResult);
                                } else {
                                    throw new Exception("二维码内容错误");
                                }

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_LONG).show();
                                return;
                            }


//                            }


                        } catch (Exception e) {
                            e.getMessage();
                            Toast.makeText(MainActivity.this, retMsg, Toast.LENGTH_LONG).show();
                            return;

                        }

                    }
                });

            }
        }).start();
    }

    private void showScanCert(final String scanResult) {
        Map<String, String> params = WebUtil.getURLRequest(scanResult);
        final String urlPath = WebUtil.getUrlPath(scanResult);
        String certsn = "";

        if (null != params.get(CommonConst.PARAM_CERTSN))
            certsn = params.get(CommonConst.PARAM_CERTSN);
        if (null != params.get(CommonConst.PARAM_ENCRYPT_CERTSN))
            certsn = params.get(CommonConst.PARAM_ENCRYPT_CERTSN);

        Intent intent = new Intent(MainActivity.this, DaoActivity.class);

        Bundle extras = new Bundle();
        if (mState) {
            String strCertHash = mCertList.get(0).getCerthash();
            if (!"".equals(strCertHash)) {
                DaoActivity.strPwd = strCertHash;
                extras.putString("certhash", strCertHash);
            }
        }

        extras.putInt("isSM2", rsaType);
        extras.putBoolean("isNewQrCode", isNewQrCode);

        extras.putString("ScanDao", "scan");
        extras.putString("ServiecNo", params.get(CommonConst.PARAM_BIZSN));
        if (isJSONDate)
            extras.putString("IsJson", "isJson");

        if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_RANDOM_NUMBER));
            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码登录");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "1");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
            if (isSignEx) {
                try {
                    extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
                } catch (Exception e) {
                    extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
                }

                if ("".equals(strScanAppName))
                    extras.putString("AppName", "批量签名");
                else
                    extras.putString("AppName", strScanAppName);
                extras.putString("OperateState", "3");
                if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                    extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
                else
                    extras.putString("MsgWrapper", "0");
            }

            if (isSignEx)
                extras.putString("IsSignEx", "isSignEx");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGNEX;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
            try {
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
            } catch (Exception e) {
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
            }

            extras.putString("CertSN", certsn);

            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码解密");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "4");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
            try {
                //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
                //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
                //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
                //extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
            } catch (Exception e) {
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            }
            extras.putString("CertSN", certsn);

            if (isEncode) {
                extras.putString("OperateState", "6");
            } else {
                extras.putString("OperateState", "2");
            }


            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码签名");
            else
                extras.putString("AppName", strScanAppName);


            if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
            else
                extras.putString("MsgWrapper", "0");


            if (isEncode) {
                operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_ENCODE;
            } else {
                operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
            }

        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeEncode.toLowerCase()) != -1) {//扫码加密
            try {
                //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
                //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
                //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
                //extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
            } catch (Exception e) {
                extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            }
            extras.putString("CertSN", certsn);

            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码加密");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "6");
            if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
            else
                extras.putString("MsgWrapper", "0");

            operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_ENCODE;
        } else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            if ("".equals(strScanAppName))
                extras.putString("AppName", "扫码签章");
            else
                extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "5");
            extras.putString("CertSN", certsn);
            if (null != params.get(CommonConst.PARAM_MSGWRAPPER))
                extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
            else
                extras.putString("MsgWrapper", "0");

            operatorType = CommonConst.CERT_OPERATOR_TYPE_SEAL;
        }


        intent.putExtras(extras);
        startActivityForResult(intent, SCAN_CODE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        XGPushManager.onActivityStoped(this);
    }

    //检测网络是否连接
    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();

            return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void registerXGPush(String actName) {    //注册信鸽推送SDK
        XGPushManager.registerPush(getApplicationContext(), actName,
                new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int flag) {

                        //Toast.makeText(MainActivity.this, "Token:"+XGPushConfig.getToken(getApplicationContext()),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        //Toast.makeText(MainActivity.this, "Main 失败",Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void showProgDlg(String strMsg) {
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


    private void checkVersion() {


        if (!bUpdated) {

            final UpdateUtil updateUtil = new UpdateUtil(MainActivity.this, false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String res = uniTrust.GetClientLatestInfo(ParamGen.GetClientLatestInfo(UpdateUtil.UPDATE_APP_NAME, updateUtil.getVerCode(getApplicationContext()) + ""));
                        Log.e("更新", res);

                        final APPResponse response = new APPResponse(res);
                        final int retCode = response.getReturnCode();
                        final String retMsg = response.getReturnMsg();
//
//

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                dismissDg();

                                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                                    JSONObject jbRet = response.getResult();


                                    String version = jbRet.getString(CommonConst.RESULT_PARAM_VERSION);
                                    String downloadUrl = jbRet.getString(CommonConst.RESULT_PARAM_DOWNLOADURL);
                                    String des = jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION);
                                    boolean isCompulsion = jbRet.getBoolean(CommonConst.RESULT_PARAM_ISUPDATE);


                                    updateUtil.newVerName = version;
                                    updateUtil.newVerCode = Integer.parseInt(version.replace(".", ""));
                                    updateUtil.strDownPath = downloadUrl;
                                    updateUtil.Description = des;
                                    updateUtil.isCompulsion = isCompulsion ? 1 : 0;


                                    updateUtil.checkToUpdate();

                                } else {


                                }

                            }
                        });


                    } catch (Exception e) {

                    }
                }
            }).start();
            bUpdated = true;
        }
    }

}
