package com.sheca.umandroid;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;


import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.facefr.util.CheckPermServer;
import com.ifaa.sdk.api.AuthenticatorManager;
import com.ifaa.sdk.auth.Constants;
import com.ifaa.sdk.auth.IAuthenticator;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.AppInfoDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.dialog.AlertDialogUtil;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.AppInfo;
import com.sheca.umandroid.model.AppInfoEx;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.presenter.LoginController;
import com.sheca.umandroid.test.MainActivityNew;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.LogUtil;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.UpdateUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Account;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.SealInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LaunchActivity extends Activity implements OnRequestPermissionsResultCallback {
    public static UpdateUtil updateuUtil = null;
    private ProgressDialog progDialog = null;

    private static final int RQF_UPDATE = 1;
    public static final boolean LOG_FLAG = false;       //记录日志开关
    public static boolean isBlueToothUsed = false;      //使用蓝牙模块标志
    public static boolean isIFAAFingerUsed = false;     //使用ifaa指纹模块
    public static boolean isIFAAFingerOpend = false;    //ifaa指纹模块开关
    public static IAuthenticator authenticator;
    public static boolean isIFAAFingerOK = false;       //ifaa指纹识别是否通过标志
    public static int failCount = 0;                //ifaa指纹识别验证失败计数器

    public static LogUtil logUtil = null;
    private boolean isNotification = false;
    private SharedPreferences sharedPrefs = null;

    private PowerManager pm = null;
    private PowerManager.WakeLock mWakeLock = null;

    private String resPonSeal = "";
    private String resPonCert = "";
    private String resPonAccount = "";//

    private AccountDao accountDao = null;
    private AppInfoDao mAppInfoDao = null;
    private boolean bLogined = false;       //是否检测自动登录
    private boolean bCheckPremissoned = false;       //是否检测应用权限

    private CheckPermServer mCheckPermServer;  // android 6.0动态权限

    private String mErr = "";
    public static boolean isGMCheck = false;  //国密安审标记
    private HandlerThread ht = null;
    protected Handler workHandler = null;

    private int mPresentVersionCode = 26;//数据库备份，当前版本号
    private CertDao mCerDao = null;
    private SealInfoDao mSealInfoDao = null;

    public static final String[] PERMISSION_LAUNCH_TEST = new String[]{"android.permission.CAMERA", "android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_VIDEO", "android.permission.RECORD_AUDIO"};

    public String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public void showAlertDialog() {
        String ver = getSystemVersion();
        String str = String.format(getResources().getString(R.string.version_alert), ver);
        AlertDialogUtil.getInstance(this).showAlertDialog(
                R.string.app_tip,
                str,
                R.string.app_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
    }

    public boolean checkSystemLessThanLOLLIPOP() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
/*
        if(CommUtil.isRoot()){
            CommUtil.exitByIsRoot(LaunchActivity.this);
            return;
        }

        if(!CommUtil.isSupportAndroidVersion()){
            CommUtil.exitByIsSupportAndroidVersion(LaunchActivity.this);
            return;
        }

        if (checkSystemLessThanLOLLIPOP()) {
            showAlertDialog();
            return;
        }
*/
        //低于26的，需要退出重新登陆，进行数据同步
        Log.e("TEST_DATABASE", "Lauch判断");
        Log.e("TEST_DATABASE", SharePreferenceUtil.getInstance(getApplicationContext()).getInt(CommonConst.PARAM_V26_DBCHECK) + "");
        if (SharePreferenceUtil.getInstance(getApplicationContext()).getInt(CommonConst.PARAM_V26_DBCHECK) < 0) {
            new LoginController().setLogout(this);
            updateDataBase();
        }

        accountDao = new AccountDao(LaunchActivity.this);
        mAppInfoDao = new AppInfoDao(LaunchActivity.this);
        mCerDao = new CertDao(LaunchActivity.this);
        mSealInfoDao = new SealInfoDao(LaunchActivity.this);

        authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
        //if(null == authenticator)
        //authenticator = AuthenticatorManager.create(this, Constants.TYPE_FACE);
        bLogined = false;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }


        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        //isNotification = sharedPrefs.getBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, false);

        if (LOG_FLAG) {
            logUtil = new LogUtil(LaunchActivity.this, LOG_FLAG);   //是否记录日志
            logUtil.init();
            logUtil.recordLogServiceLog("LaunchActivity.onCreate");
        }

//        getCertChain();
//        checkBlueToothUsed();
        checkIFAAFingerUsed();


        mCheckPermServer = new CheckPermServer(this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 权限不足
                        setResult(CheckPermServer.PERMISSION_DENIEG);
                        finish();
                    }
                });

        ht = new HandlerThread("ccit_working_thread1");
        ht.start();
        workHandler = new Handler(ht.getLooper());
        if (!mCheckPermServer.permissionSet(LaunchActivity.this, CheckPermServer.PERMISSION_LAUNCH))
//        if (!mCheckPermServer.permissionSet(LaunchActivity.this, PERMISSION_LAUNCH_TEST))
            showPermissionLaunch();
        //getHttpsCert();
        //pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");


    }


    private void updateDataBase() {
        Log.e("TEST_DATABASE", "更新数据库开始.");
        int mLocalCode = SharePreferenceUtil.getInstance(LaunchActivity.this).getInt(CommonConst.PARAM_V26_DBCHECK);
        if (mLocalCode < 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<Account> mAccountList = accountDao.queryAll();
                    final List<Cert> mCertList = mCerDao.getAllCert();
                    final List<SealInfo> mSealInfoList = mSealInfoDao.getAllSealInfo();
                    Log.e("TEST_DATABASE", "更新数据库Account." + mAccountList.size());
                    Log.e("TEST_DATABASE", "更新数据库Cert." + mCertList.size());
                    Log.e("TEST_DATABASE", "更新数据库SealInfo." + mSealInfoList.size());
                    //更新用户表
                    if (mAccountList != null && mAccountList.size() != 0) {

                        UniTrust mUnitTrust = new UniTrust(LaunchActivity.this, false);
                        resPonAccount = mUnitTrust.setAllAcounts(mAccountList);

                    }

                    //更新证书表
                    if (mCertList != null && mCertList.size() != 0) {

                        UniTrust mUnitTrust = new UniTrust(LaunchActivity.this, false);
                        resPonCert = mUnitTrust.setAllCerts(mCertList);

                    }

                    //更新印章表
                    if (mSealInfoList != null && mSealInfoList.size() != 0) {

                        UniTrust mUnitTrust = new UniTrust(LaunchActivity.this, false);
                        resPonSeal = mUnitTrust.setAllSealInfos(mSealInfoList);
                    }

                    accountDao.deleteAccount();
                    mCerDao.deleteAllCert();
                    mSealInfoDao.deleteAllSeal();

                    Log.e("UPDATE_BASE", "resPonAccount" + resPonAccount);
                    Log.e("UPDATE_BASE", "resPonCert" + resPonCert);
                    Log.e("UPDATE_BASE", "resPonSeal" + resPonSeal);

                }
            }).start();

        }


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @SuppressLint("NewApi")
    private void checkBlueToothUsed() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            isBlueToothUsed = false;
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            isBlueToothUsed = false;
            return;
        }

        isBlueToothUsed = true;
    }

    private void showPermissionLaunch() {
        final Handler handler = new Handler(LaunchActivity.this.getMainLooper());

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                        bCheckPremissoned = sharedPrefs.getBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, false);
                        if (!bCheckPremissoned) {
                            //ActivityCompat.requestPermissions(LaunchActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
                            bCheckPremissoned = true;

                            Editor editor = sharedPrefs.edit();
                            editor.putBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, bCheckPremissoned);
                            editor.commit();
                        }

                        if (isNetworkAvailable(LaunchActivity.this)) {
                            if (isGMCheck) {
                                //if(null == ShcaCciStd.gEsDev)
                                //ShcaCciStd.gEsDev = JShcaEsStd.getIntence(LaunchActivity.this);     //文鼎创初始化

                                if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                                    initShcaCciStdService();

                                getHttpsEncodeCert();
                            }

                            updateuUtil = new UpdateUtil(LaunchActivity.this, false);
                            if (null != updateuUtil)
                                //bLogined = updateuUtil.getServerVersion();
                                //checkVersion();
                                bLogined = true;

                            if (bLogined) {
                                if (accountDao.count() > 0) {
                                    // loginUMSPService(accountDao.getLoginAccount().getName());
//                                    startActivity(new Intent(LaunchActivity.this, MainActivityNew.class));
                                    autoLogin();
                                } else {
                                    //if (!mCheckPermServer.permissionSet(LaunchActivity.this,CheckPermServer.PERMISSION_LAUNCH)){
                                    gotoNextActivity();
                                }
                                //}
                            } else {
                                //if (!mCheckPermServer.permissionSet(LaunchActivity.this,CheckPermServer.PERMISSION_LAUNCH)){
                                gotoNextActivity();
                                //}
                            }

                            //  if(null == ShcaCciStd.gSdk)
                            //   initShcaCciStdService();
                        } else {
                            //if (!mCheckPermServer.permissionSet(LaunchActivity.this,CheckPermServer.PERMISSION_LAUNCH)){
                            gotoNextActivity();
                            // }
                        }
                    }
                });
            }
        });
    }

    boolean bUpdated = false;

    private void checkVersion() {
        final UniTrust uniTrust = new UniTrust(this, false);

        if (!bUpdated) {

            final UpdateUtil updateUtil = new UpdateUtil(LaunchActivity.this, false);
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

    private void autoLogin() {//自动登录
        final UniTrust uniTrust = new UniTrust(this, false);
        new MyAsycnTaks() {
            @Override
            public void preTask() {
                //

            }

            @Override
            public void doinBack() {


                String mStrVal = uniTrust.userAutoLogin(ParamGen.getAutoLoginParam(AccountHelper.getToken(LaunchActivity.this)));
                Log.e("自动登录", mStrVal);
                try {
                    APPResponse response = new APPResponse(mStrVal);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                    String name = AccountHelper.getRealName(LaunchActivity.this);
//                                    txtName.setText(name.length() >= 2 ? name.substring(name.length() - 2, name.length()) : name);
//                                    txtName.setVisibility(View.VISIBLE);
//                                    mIvMine.setVisibility(View.GONE);
//                                    dismissDg();
//                                    checkVersion();


                                gotoNextActivity();

//                                            Toast.makeText(MainActivity.this, retCode + retMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    dismissDg();
//                                    AccountHelper.reLogin(MainActivityNew.this);
                        doLogout();
//                                            Toast.makeText(MainActivity.this, retCode + retMsg, Toast.LENGTH_LONG).show();
//                                }
//                            });
                    }
                } catch (Exception e) {
                    doLogout();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                AccountHelper.clearAllUserData(getApplicationContext());
//                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                                startActivity(intent);
//                                finish();
////                                dismissDg();
////                                checkVersion();
////                                            Toast.makeText(MainActivity.this, retCode + retMsg, Toast.LENGTH_LONG).show();
//                            }
//                        });
                }


            }


            @Override
            public void postTask() {

            }
        }.execute();
    }

    private void doLogout() {
        final UniTrust uniTrust = new UniTrust(this, false);
        new Thread(new Runnable() {
            @Override
            public void run() {

                String res = null;
                try {
                    res = uniTrust.Logout(ParamGen.getLogout(AccountHelper.getToken(getApplicationContext())));
                } catch (Exception e) {
                    setAccountLogoutStatus();
                    AccountHelper.clearAllUserData(getApplicationContext());
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                AccountHelper.clearAllUserData(getApplicationContext());

                Log.d("unitrust", res);

                APPResponse response = new APPResponse(res);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

//                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                setAccountLogoutStatus();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                            Toast.makeText(getContext(), "账户退出成功", Toast.LENGTH_SHORT).show();
//                            if (isUserNotificationFinger){
//                                Toast.makeText(getContext(), "使用指纹登录需重启应用", Toast.LENGTH_SHORT).show();
//                            }


                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
                        editor.commit();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();


//                            JShcaEsStd gEsDev = JShcaEsStd.getIntence(v());
//                            gEsDev.disconnect();
                    }
                });
//                } else {
////                    getActivity().runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            Toast.makeText(getContext(),retMsg,Toast.LENGTH_SHORT).show();
////                        }
////                    });
//

            }
        }).start();
    }

    private void setAccountLogoutStatus() {
        new LoginController().setLogout(getApplicationContext());
    }

    private void testUCMMoudle() {
        final String K_TEST_PWD = CommonConst.JSHECACCISTD_PWD;
        final String K_TEST_MSG = CommonConst.RSA_CERT_CHAIN;

        JShcaUcmStd gUcmSdk = null;
        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        String myCid = "";
        int retCode = -1;
        JShcaUcmStdRes jres = null;

        String myHttpBaseUrl = LaunchActivity.this.getString(R.string.UMSP_Base_Service);
        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        //gUcmSdk.setRandomSeed(bRS);
        retCode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);
        //System.out.println("gUcmSdk.doInitService ret:"+retCode);
        //
        if (retCode != 0)
            return;

        jres = gUcmSdk.genSM2KeyPairWithPin(K_TEST_PWD);
        //
        if (jres.retCode == 0) {
            myCid = jres.containerid;
            jres = gUcmSdk.genSM2PKCS10WithCID(myCid, K_TEST_PWD, "C=CN" + ",CN=testP10");
            if (jres.retCode == 0) {
//				System.out.println("p10:"+jres.response);
//				//
                String strP10 = jres.response;
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, "@" + K_TEST_PWD + "@");
                //System.out.println("verifyUserPinWithCID_ret1:"+retCode);
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, K_TEST_PWD);
                //System.out.println("verifyUserPinWithCID__ret2:"+retCode);
//				//
////				gUcmSdk.delSM2ContainerWithCID(myCid,K_TEST_PWD);
//				//
                retCode = gUcmSdk.changeUserPinWithCID(myCid, K_TEST_PWD, "@" + K_TEST_PWD + "@");
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, K_TEST_PWD);
//				System.out.println("verifyUserPinWithCID__ret3:"+retCode);
//				//
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, "@" + K_TEST_PWD + "@");

                try {
                    jres = gUcmSdk.doSM2SignatureWithCID(myCid, "@" + K_TEST_PWD + "@", K_TEST_MSG.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (jres.retCode == 0) {
                    String strSign = jres.response;
                    strSign += "";
                }

                retCode = gUcmSdk.delSM2ContainerWithCID(myCid, "@" + K_TEST_PWD + "@");
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, "@" + K_TEST_PWD + "@");
                retCode = gUcmSdk.verifyUserPinWithCID(myCid, K_TEST_PWD);
//				System.out.println("verifyUserPinWithCID__ret4:"+retCode);
//				//
            }
        }
    }

    private void checkIFAAFingerUsed() {
        if (AuthenticatorManager.isSupportIFAA(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT)) {
            isIFAAFingerUsed = true;

            isIFAAFingerOpend = sharedPrefs.getBoolean(CommonConst.SETTINGS_FINGER_OPENED, false);

            if (null != authenticator) {
                String deviceId = authenticator.getDeviceId();
                int userStatus = authenticator.checkUserStatus("");
                //Toast.makeText(LaunchActivity.this,"deviceId="+deviceId+"\n"+"userStatus="+userStatus,Toast.LENGTH_LONG).show();
            }
        }
    }


    private void gotoNextActivity() {
//        showUserRules();//用户协议


        //SharedPreferences preferences = getSharedPreferences(CommonConst.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        int savedVersionCode = sharedPrefs.getInt(CommonConst.VERSION_CODE, -1);
        if (savedVersionCode == -1) {
//            // 首次运行程序，跳转引导页
//
            if (!AccountHelper.isAgreeUserRules(LaunchActivity.this)) {

                        showUserRules();//用户协议


            } else {
                Intent i = new Intent(LaunchActivity.this, GuideActivity.class);
                startActivity(i);
                //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                this.finish();
            }
        } else {
            // 已经运行过程序
            try {
                int presentVersionCode = getVersionCode();
                if (savedVersionCode == presentVersionCode) {
                    // 版本号一致，跳转首页


                    Intent i = new Intent(LaunchActivity.this, MainActivity.class);
                    startActivity(i);
                    //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                    this.finish();
                } else {
                    // 版本号不一致，跳转引导页
                    Intent i = new Intent(LaunchActivity.this, GuideActivity.class);
                    startActivity(i);
                    //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                    this.finish();
                }

            } catch (Exception e) {
                Log.e(CommonConst.TAG, e.getMessage(), e);
            }
        }
    }


    public void showUserRules() {//用户协议
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this,R.style.dialogstyle);

        builder.setCancelable(false);

        View view = LayoutInflater.from(this).inflate(R.layout.item_notice_start, null, false);
        builder.setView(view);
        TextView txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
        TextView txt_ok = (TextView) view.findViewById(R.id.txt_ok);
        WebView webView=(WebView) view.findViewById(R.id.web);
        webView.loadUrl(CommonConst.RULES_SERVER);
//        webView.loadUrl(" https://test-ssc.mohrss.gov.cn/indep/veriPassWord?_api_signature=Uz2IT09B5SdC3RaFU5SUWcDbOoQ%3D&_api_name=get_token&_api_version=1.0.0&_api_access_key=aa340822ef4745d294cdb13ec757393a&security=MDIckNwy2jMaf%2FsE1CxdnP%2FLlTDx%2FdE2qhQye8SqL3z%2BncaUZeDa16Mv8x7%2BGQeYGDHONcHzhyj4MUHiMwgWM%2BJUNPTwTW%2Fxy8SdZlxrYBw3zDDKBktFe54s3nzyQYbJhyoHiGhEHLamMkhmgoIqHBAiQ37H2uQXCulGhw9XqtfQSlZLIc7hNtigrvvsLWXcxc1zRj4J1EtV%2BNgZTNQptGjoNirEevcWL2kYJjLzmUo%3D&api_access_key=aa340822ef4745d294cdb13ec757393a&return_url=&_api_timestamp=1584692313503&isWebView=1");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);  //将图片调整到适合webView的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);

//        TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
//        txt_content.setText(getString(R.string.fryzt_rule1) + getString(R.string.fryzt_rule2) + getString(R.string.fryzt_rule3));
//
//        String rule1 = getString(R.string.fryzt_rule1);
//        String rule2 = getString(R.string.fryzt_rule2);
//        String rule3 = getString(R.string.fryzt_rule3);
//
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getResources()
//                .getColor(R.color.bg_blue));
//        String mTitleAgreement = rule1 + rule2 + rule3;
//        spannableStringBuilder.append(mTitleAgreement);
//        spannableStringBuilder.setSpan(foregroundColorSpan, rule1.length(), (rule1 + rule2).length(),
//                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//
//        //设置协议点击事件
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                //这里的判断是为了去掉在点击后字体出现的背景色
//                if (widget instanceof TextView) {
//                    ((TextView) widget).setHighlightColor(Color.TRANSPARENT);
//                }
//                Intent intent = new Intent(LaunchActivity.this, UserProtocolActivity.class);
//                startActivity(intent);
//
//            }
//
//            @Override
//            public void updateDrawState(TextPaint ds) {
//                super.updateDrawState(ds);
//                //去除下划线
//                ds.setColor(getResources().getColor(R.color.bg_blue));
//                ds.setUnderlineText(false);
//            }
//        };
//        spannableStringBuilder.setSpan(clickableSpan, rule1.length(), (rule1 + rule2).length(),
//                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//        txt_content.setText(spannableStringBuilder);
//        txt_content.setMovementMethod(LinkMovementMethod.getInstance());


        AlertDialog dia = builder.show();
//        dia.setView(dia.getWindow().getDecorView(),0,0,0,0);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        android.view.WindowManager.LayoutParams p = dia.getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.5);   //高度设置为屏幕的0.3
        p.width = (int) (d.getWidth() * 0.8);    //宽度设置为屏幕的0.5
        dia.getWindow().setAttributes(p);     //设置生效


//        dia.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rules);

        dia.setCanceledOnTouchOutside(false);//禁止点外部
//        dia.setOnKeyListener(new DialogInterface.OnKeyListener() {//不可點返回鍵取消
//
//            @Override
//
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//
//                    return true;
//
//                } else {
//
//                    return false; // 默认返回 false
//
//                }
//
//            }
//
//        });
        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dia.dismiss();
                finish();
                System.exit(0);
            }
        });
        txt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LaunchActivity.this, GuideActivity.class);
                startActivity(i);
                //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
              finish();
            }
        });

    }

    private int getVersionCode() throws Exception {
        int versionCode = 1;
        PackageManager manager = this.getPackageManager();
        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        versionCode = info.versionCode;
        return versionCode;
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

    private void getCertChain() {
        InputStream inCfg = null;
        Properties prop = new Properties();
        int certCount = 0;

        try {
            inCfg = this.getAssets().open("CertChain.properties");
            ;
            prop.load(inCfg);
            certCount = Integer.parseInt(prop.getProperty("CertChainNum"));

            for (int i = 0; i < certCount; i++) {
                String chainIndex = String.format("CertChain%s", i + "");
                String chainCont = prop.getProperty(chainIndex);
                WebClientUtil.mCertChainList.add(chainCont);
            }

        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (inCfg != null)
                    inCfg.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private void getHttpsCert() {
        InputStream ins = null;

        try {
            ins = getAssets().open("cert.cer"); //下载的证书放到项目中的assets目录中
            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            WebClientUtil.mCert = cerFactory.generateCertificate(ins);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private void getHttpsEncodeCert() {
        InputStream ins = null;

        try {
            ins = getAssets().open("CAServer.cer"); //下载的证书放到项目中的assets目录中
            WebClientUtil.mEncodeCert = new String(convertStreamToString(ins));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            WebClientUtil.mEncodeCert = "";
        } finally {
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;

        try {

            while ((line = reader.readLine()) != null) {

                sb.append(line);

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                is.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }


        return sb.toString();

    }


    private Boolean loginUMSPService(String act) {    //重新登录UM Service
        String returnStr = "";
        try {
            //异步调用UMSP服务：用户登录
            String timeout = LaunchActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LaunchActivity.this.getString(R.string.UMSP_Service_Login1);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", act);
            postParams.put("pwdHash", getPWDHash(accountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
            if (accountDao.getLoginAccount().getType() == 1)
                postParams.put("appID", CommonConst.UM_APPID);
            else
                postParams.put("appID", accountDao.getLoginAccount().getAppIDInfo());

            String actpwd = "";
//            if (accountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
//                actpwd = getPWDHash(accountDao.getLoginAccount().getPassword());
//            else
            actpwd = accountDao.getLoginAccount().getPassword();

            String postParam = "";
            if (accountDao.getLoginAccount().getType() == 1)
                postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                        "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
                        "&appID=" + URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
            else
                postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                        "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
                        "&appID=" + URLEncoder.encode(accountDao.getLoginAccount().getAppIDInfo(), "UTF-8");

            String responseStr = "";
            try {
                //清空本地缓存
                WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout)/6);
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout) / 6);

            } catch (Exception e) {
                if (null == e.getMessage())
                    throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
                else
                    throw new Exception("用户登录失败：" + e.getMessage() + " 请重新点击登录");
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (!"0".equals(resultStr)) {
                throw new Exception(resultStr + ":" + returnStr);
            } else {
                getAllAppInfos();
                //testUCMMoudle();

//                if (!mCheckPermServer.permissionSet(LaunchActivity.this, CheckPermServer.PERMISSION_LAUNCH)) {
                if (!mCheckPermServer.permissionSet(LaunchActivity.this, CheckPermServer.PERMISSION_LAUNCH)) {
                    gotoNextActivity();
                }
            }

        } catch (Exception exc) {
            mErr = exc.getMessage();
            gotoNextActivity();
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

        return strPWDHash;
    }

    private void getAllAppInfos() throws Exception {
        List<AppInfo> applications = null;
        String responseStr = "";
        String strAllAppInfo = "";
        Editor editor = sharedPrefs.edit();

        try {
            String timeout = LaunchActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LaunchActivity.this.getString(R.string.UMSP_Service_GetAllAppInfos);
            Map<String, String> postParams = new HashMap<String, String>();
            //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

            String postParam = "";
            responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            String returnStr = jb.getString(CommonConst.RETURN_MSG);

            if ("0".equals(resultStr)) {
                //JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                applications = new ArrayList<AppInfo>();

                for (int i = 0; i < transitListArray.size(); i++) {
                    AppInfo appInfo = new AppInfo();
                    JSONObject jbRet = transitListArray.getJSONObject(i);
                    appInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
                    appInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
                    appInfo.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));

                    if (null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
                        appInfo.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
                    else
                        appInfo.setDescription("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))
                        appInfo.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
                    else
                        appInfo.setContactPerson("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
                        appInfo.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
                    else
                        appInfo.setContactPhone("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
                        appInfo.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
                    else
                        appInfo.setContactEmail("");
                    if (null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
                        appInfo.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
                    else
                        appInfo.setAssignTime("");

                    applications.add(appInfo);
                }
            } else
                throw new Exception(returnStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        if (null != applications) {
            for (AppInfo appInfo : applications) {
                if (null == mAppInfoDao.getAppInfoByAppID(appInfo.getAppID())) {
                    //Toast.makeText(LoginActivity.this, appItems,Toast.LENGTH_SHORT).show();
                    AppInfoEx appInfoEx = new AppInfoEx();
                    appInfoEx.setAppidinfo(appInfo.getAppID());
                    appInfoEx.setName(appInfo.getName());
                    if (null != appInfo.getAssignTime())
                        appInfoEx.setAssigntime(appInfo.getAssignTime());
                    else
                        appInfoEx.setAssigntime("");
                    if (null != appInfo.getContactEmail())
                        appInfoEx.setContactemail(appInfo.getContactEmail());
                    else
                        appInfoEx.setContactemail("");
                    if (null != appInfo.getContactPerson())
                        appInfoEx.setContactperson(appInfo.getContactPerson());
                    else
                        appInfoEx.setContactperson("");
                    if (null != appInfo.getContactPhone())
                        appInfoEx.setContactphone(appInfo.getContactPhone());
                    else
                        appInfoEx.setContactphone("");
                    if (null != appInfo.getDescription())
                        appInfoEx.setDescription(appInfo.getDescription());
                    else
                        appInfoEx.setDescription("");

                    mAppInfoDao.addAPPInfo(appInfoEx);
                }

                strAllAppInfo += appInfo.getAppID().replace("-", "") + "-";
            }

            if (!"".equals(strAllAppInfo)) {
                strAllAppInfo = strAllAppInfo.substring(0, strAllAppInfo.length() - 1);
                editor.putString(CommonConst.SETTINGS_ALL_APP_INFO, strAllAppInfo);
                editor.commit();
            }

        }

    }


    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(LaunchActivity.this);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;

            if (retcode != 0)
                ShcaCciStd.gSdk = null;
        }
        //Toast.makeText(MainActivity.this,"errorCode:"+ShcaCciStd.errorCode ,Toast.LENGTH_SHORT).show();
        return retcode;
    }


    @SuppressLint("Override")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (CheckPermServer.PERMISSION_REQUEST_CODE == requestCode
                && mCheckPermServer.hasAllPermissionGranted(grantResults)) {
            // 回调中加载下一个Activity
            //gotoNextActivity();
            showPermissionLaunch();
        } else {
            mCheckPermServer.showMissingPermissionDialog();
            LaunchActivity.this.finish();
        }
    }

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(LaunchActivity.this);
        progDialog.setMessage(strMsg);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(false);
        progDialog.show();
    }

    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }

    private void testLogin() {
        Intent i = new Intent(LaunchActivity.this, LoginActivity.class);
        startActivity(i);
        //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        this.finish();
    }


    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RQF_UPDATE: {
                    updateuUtil = new UpdateUtil(LaunchActivity.this, false);
                    updateuUtil.getServerVersion();
                }
                break;
                default:
                    break;
            }
        }

        ;
    };

}
