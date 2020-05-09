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
import com.sheca.umandroid.fragment.FragmentFactory;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.service.GeTuiIntentService;
import com.sheca.umandroid.service.GeTuiService;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
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
        if (!bUpdated) {
//            if (null != LaunchActivity.updateuUtil) {
//                LaunchActivity.updateuUtil.setActicity(MainActivity.this);
//               LaunchActivity.updateuUtil.checkToUpdate();
//            }
            checkVersion();
            bUpdated = true;
        }

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
        final UniTrust uniTrust = new UniTrust(this, false);

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
