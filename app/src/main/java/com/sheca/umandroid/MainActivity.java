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
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.umandroid.fragment.FragmentFactory;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.SystemBarTintManager;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

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


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            Fragment fragment = FragmentFactory.getInstanceByIndex(R.id.rb_home);
            transaction.add(R.id.content, fragment, "HomeFragment");
            transaction.commit();

            rg = (RadioGroup) findViewById(R.id.rg_menu);
            RadioButton rb_home = (RadioButton) rg.getChildAt(0);
            rb_home.setChecked(true);

            mHome=(RadioButton)findViewById(R.id.rb_home);
            mCert=(RadioButton)findViewById(R.id.rb_cert);
            mScan =(RadioButton)findViewById(R.id.rb_service);
            mSeal=(RadioButton)findViewById(R.id.rb_seal);
            mSettings=(RadioButton)findViewById(R.id.rb_settings);

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
                    /*if (checkedId == R.id.rb_service) {
                        Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivityForResult(i, CAPTURE_CODE);
                    } else {*/
                        FragmentTransaction transaction = fm.beginTransaction();
                        Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId);
                        transaction.replace(R.id.content, fragment);
                        transaction.commit();
                    //}
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

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        //setIntent(intent);
        //Toast.makeText(getApplicationContext(), "onNewIntent", Toast.LENGTH_SHORT).show();

        fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment fragment = FragmentFactory.getInstanceByIndex(R.id.rb_home);
        transaction.add(R.id.content, fragment, "HomeFragment");
        transaction.commit();

        rg = (RadioGroup) findViewById(R.id.rg_menu);
        RadioButton rb_home = (RadioButton) rg.getChildAt(0);
        rb_home.setChecked(true);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup group, int checkedId) {
                FragmentTransaction transaction = fm.beginTransaction();
                Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId);
                transaction.replace(R.id.content, fragment);
                transaction.commit();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        XGPushClickedResult click = XGPushManager.onActivityStarted(this);

        if (!bUpdated) {
            if (null != LaunchActivity.updateuUtil) {
                //LaunchActivity.updateuUtil.setActicity(MainActivity.this);
               //LaunchActivity.updateuUtil.checkToUpdate();
            }

            bUpdated = true;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (10086 == requestCode){
            LaunchActivity.updateuUtil.installNewApk();
        }
        super.onActivityResult(requestCode, resultCode, data);
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


}
