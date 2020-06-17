package com.sheca.umandroid;

//import android.app.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.umandroid.account.LoginActivityV33;
import com.sheca.umandroid.account.ReLoginActivityV33;
import com.sheca.umandroid.fragment.FragmentFactory;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.service.GeTuiIntentService;
import com.sheca.umandroid.service.GeTuiService;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.SystemBarTintManager;
import com.sheca.umandroid.util.UpdateUtil;
import com.sheca.umplus.dao.UniTrust;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import net.sf.json.JSONObject;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;

import java.security.Security;

public class MainActivity extends FragmentActivity {
    private FragmentManager fm;
    private RadioGroup rg;

    private boolean bUpdate = true;          //是否检查更新
    private boolean bShowUpdate = true;      //是否显示检查更新错误信息
    private ProgressDialog progDialog = null;
    private long timeDelay = 2000;          //启动更新任务时长(10秒)
    private boolean bUpdated = false;       //是否检测版本更新
    private boolean bLogined = false;       //是否检测自动登录
    private boolean bCheckPremissoned = false;       //是否检测应用权限

    private SharedPreferences sharedPrefs;

    public static String strErr = "";
    private long exitTime = 0;   //退出应用计时器
    private RadioButton mHome;
    private RadioButton mCert;
    private RadioButton mScan;
    private RadioButton mSeal;
    private RadioButton mSettings;

    int nowid = R.id.rb_home;

    private Class userPushService = GeTuiService.class;

     UniTrust uniTrust ;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.bg_red);//通知栏所需颜色
        }

        bUpdate = false;
        exitTime = System.currentTimeMillis();

        try {
            fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            homeFragment = FragmentFactory.getInstanceByIndex(R.id.rb_home);
            transaction.add(R.id.content, homeFragment, "HomeFragment");

            //transaction.addToBackStack(null);//防止扫码闪退
            transaction.commit();

            rg = (RadioGroup) findViewById(R.id.rg_menu);
            RadioButton rb_home = (RadioButton) rg.getChildAt(0);
            rb_home.setChecked(true);

            mHome = (RadioButton) findViewById(R.id.rb_home);
            mCert = (RadioButton) findViewById(R.id.rb_cert);
            mScan = (RadioButton) findViewById(R.id.rb_service);
            mSeal = (RadioButton) findViewById(R.id.rb_seal);
            mSettings = (RadioButton) findViewById(R.id.rb_settings);

//定义底部标签图片大小和位置
            Drawable drawable_home = getResources().getDrawable(R.drawable.selector_home);
//    //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
            drawable_home.setBounds(0, 0, 64, 64);
//    //设置图片在文字的哪个方向
            mHome.setCompoundDrawables(null, drawable_home, null, null);

            Drawable drawable_cert = getResources().getDrawable(R.drawable.selector_cert);
            Drawable drawable_scan = getResources().getDrawable(R.drawable.selector_scan);
            Drawable drawable_seal = getResources().getDrawable(R.drawable.selector_seal);
            Drawable drawable_mine = getResources().getDrawable(R.drawable.selector_settings);

            drawable_cert.setBounds(0, 0, 64, 64);
            drawable_scan.setBounds(0, 0, 64, 64);
            drawable_seal.setBounds(0, 0, 64, 64);
            drawable_mine.setBounds(0, 0, 64, 64);

            mCert.setCompoundDrawables(null, drawable_cert, null, null);
            mScan.setCompoundDrawables(null, drawable_scan, null, null);
            mSeal.setCompoundDrawables(null, drawable_seal, null, null);
            mSettings.setCompoundDrawables(null, drawable_mine, null, null);

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
                            findViewById(R.id.tv_right).setVisibility(View.VISIBLE);


                            break;
                        default:
                            findViewById(R.id.tv_right).setVisibility(View.GONE);

                            break;


                    }


//                    fragment.onResume();
                }
            });

            if (AccountHelper.hasLogin(this)) {
                registerXGPush(AccountHelper.getUsername(this));
            }

            //CommUtil.showByCheckAndroidVersion(MainActivity.this);

        } catch (Exception ex) {
            ex.printStackTrace();
            //Toast.makeText(MainActivity.this,ex.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }

        initGetui();


uniTrust= new UniTrust(this, false);


        if (AccountHelper.isLoadLicence(this)) {
            if (AccountHelper.hasLogin(MainActivity.this) && !AccountHelper.hasAuth(MainActivity.this)) {
                doLogout();//覆盖安装
                return;
            }
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
            loadLicense();
        }
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
                        startActivity(intentLoignV33);}
//                    getActivity().finish();
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


//                            Intent intent = new Intent(getActivity(), LoginActivity.class);
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

                            JShcaEsStd gEsDev = JShcaEsStd.getIntence(MainActivity.this);
                            gEsDev.disconnect();
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

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        getActivity().finish();
//                    }
//                });

            }
        }).start();
    }


    private void loadLicense() {
//        String aesKey = null;
//        try {
//            aesKey = PKIUtil.priKeyDecrypt("I7kyZ+B4RSznidHhAOM7K1iLGhDIvRKzh3kdFXb4PKkyCJM7oGuQN04r3eXLHbDP+g2gJ+mqJzQH+kigTdM1kvlXUeeDCss9ismQt4oEtrJTmibfFDgOPHAa08ipheNy5FjO1lmoRTVSKkYnDGrTjfVGG+itj2BbqIUPvQ1J/OY=", CommonConst.UM_APP_PRIVATE_KEY);
//            aesKey = new String(Base64.encode(aesKey.getBytes()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String data = null;
//        try {
//            data = PKIUtil.aesDecrypt("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", aesKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Log.e("dataaaa", aesKey + "  ,  " + data);
        new Thread() {
            public void run() {
                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams());
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

                            if (AccountHelper.hasLogin(MainActivity.this) && !AccountHelper.hasAuth(MainActivity.this)) {
                                doLogout();//覆盖安装
                                return;
                            }
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
        PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
//        }

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GeTuiIntentService.class);

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次将退出移证通", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                JShcaEsStd gEsDev = JShcaEsStd.getIntence(this);
                gEsDev.disconnect();

//	    		if(null == ScanBlueToothSimActivity.gKsSdk)
//	    			ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(MainActivity.this.getApplication(), this);
                //ScanBlueToothSimActivity.gKsSdk.disconnect();

                ShcaCciStd.gSdk = null;

                if (LaunchActivity.LOG_FLAG)
                    LaunchActivity.logUtil.destory();

                MainActivity.this.finish();
                System.exit(0);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    Fragment homeFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        //setIntent(intent);
        //Toast.makeText(getApplicationContext(), "onNewIntent", Toast.LENGTH_SHORT).show();

        RadioButton rb_home = (RadioButton) rg.getChildAt(0);
        rb_home.setChecked(true);
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

        findViewById(R.id.tv_right).setVisibility(View.GONE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (10086 == requestCode) {
            LaunchActivity.updateuUtil.installNewApk();
        }
        super.onActivityResult(requestCode, resultCode, data);
//        homeFragment.onActivityResult(requestCode, resultCode, data);
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


    private String getPWDHash(String strPWD) {
        String strPWDHash = "";

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;
    }

    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(MainActivity.this);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;

            if (retcode != 0)
                ShcaCciStd.gSdk = null;
        }
        //Toast.makeText(MainActivity.this,"errorCode:"+ShcaCciStd.errorCode ,Toast.LENGTH_SHORT).show();
        return retcode;
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
