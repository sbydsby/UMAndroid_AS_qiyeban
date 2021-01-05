package com.sheca.zhongmei;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sheca.zhongmei.account.LoginActivityV33;
import com.sheca.zhongmei.account.ReLoginActivityV33;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.presenter.LoginController;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.CommUtil;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;

public class MineActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_mine);
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("我的");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MineActivity.this.finish();
            }
        });

        LinearLayout ll_version = findViewById(R.id.ll_version);
        ll_version.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MineActivity.this, SettingVersionActivity.class);
                startActivity(i);
            }
        });

        TextView txt_logout = findViewById(R.id.txt_logout);
        txt_logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAccount();
            }
        });


        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_phone = findViewById(R.id.txt_phone);

        String realName = AccountHelper.getRealName(this);
        txt_name.setText(realName);

        String mMobile = AccountHelper.getUsername(this);
        String fixMob = mMobile.substring(0, 3) + "****" + mMobile.substring(7, 11);
        txt_phone.setText(fixMob);


        TextView txt_code = findViewById(R.id.txt_code);
//        String ver = CommUtil.formatString(this, R.string.version, CommUtil.getVerName(this));
        String ver=CommUtil.getVerName(this);
        txt_code.setText(ver);


    }

    private void logoutAccount() {
        final Handler handler = new Handler(this.getMainLooper());

        if (!AccountHelper.hasLogin(this)) {  //账户未登录
            Toast.makeText(this, "账户未登录", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.alert);
            builder.setTitle("提示");
            builder.setMessage("确定退出此账户？");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            doLogout();
                        }

                    });

            builder.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        }
    }


    private void doLogout() {

        UniTrust uniTrust = new UniTrust(this, false);
        new Thread(new Runnable() {
            @Override
            public void run() {


                String res = null;
                try {
                    res = uniTrust.Logout(ParamGen.getLogout(AccountHelper.getToken(MineActivity.this)));
                } catch (Exception e) {
                    AccountHelper.clearAllUserData(MineActivity.this);
//                    Intent intent = new Intent(getActivity(), LoginActivity.class);
//                    startActivity(intent);

//					if (AccountHelper.isFirstLogin(MineActivity.this)) {
//						Intent intentLoignV33 = new Intent(MineActivity.this, LoginActivityV33.class);
//						startActivity(intentLoignV33);
//					} else {
//						Intent intentLoignV33 = new Intent(MineActivity.this, ReLoginActivityV33.class);
//						startActivity(intentLoignV33);
//					}

                    finish();
                    return;
                }

                AccountHelper.clearAllUserData(MineActivity.this);

                Log.d("unitrust", res);

                APPResponse response = new APPResponse(res);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                    setAccountLogoutStatus();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MineActivity.this, "账户退出成功", Toast.LENGTH_SHORT).show();


                            if (!AccountHelper.hasLogin(MineActivity.this)) {
                                if (AccountHelper.isFirstLogin(MineActivity.this)) {
                                    Intent intentLoignV33 = new Intent(MineActivity.this, LoginActivityV33.class);
                                    intentLoignV33.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentLoignV33);

                                } else {
                                    Intent intentLoignV33 = new Intent(MineActivity.this, ReLoginActivityV33.class);
                                    intentLoignV33.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentLoignV33);

                                }

                                finish();
                            }


                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MineActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });

            }
        }).start();
    }


    private void setAccountLogoutStatus() {
        new LoginController().setLogout(this);
    }
}
