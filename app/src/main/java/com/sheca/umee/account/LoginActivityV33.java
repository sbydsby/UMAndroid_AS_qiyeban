package com.sheca.umee.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umee.BaseActivity;
import com.sheca.umee.R;
import com.sheca.umee.SwitchServerActivity;
import com.sheca.umee.interfaces.ResponseCallback;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.presenter.AccountController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommUtil;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

/**
 * 首次登录输入手机号界面
 */
public class LoginActivityV33 extends BaseActivity implements View.OnClickListener {
    private LinearLayout mCreateAccount;
    private EditText mMobileNO;
    private TextView mLoginMobile;
    private View mBack;
    UniTrust uniTrust;
    AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_login);
        accountController = new AccountController();
        uniTrust = new UniTrust(this, false);
        initView();


    }

    private void initView() {
        Button testLoginButton = findViewById(R.id.testLoginButton);
        testLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AccountHelper.setUsername(LoginActivityV33.this, mMobileNO.getText().toString().trim());
                AccountHelper.setIDName(LoginActivityV33.this, "邵博阳");
                AccountHelper.setIdcardno("310109199205263514");

                Intent intent = new Intent(LoginActivityV33.this, ReLoginActivityV33.class);
                startActivity(intent);
            }
        });


        mCreateAccount = (LinearLayout) findViewById(R.id.ll_create_account);
        mMobileNO = (EditText) findViewById(R.id.et_v33_mobile);
        mLoginMobile = (TextView) findViewById(R.id.login_v33_mobile);
        mBack = findViewById(R.id.ic_back);
        mLoginMobile.setOnClickListener(this);
        mCreateAccount.setOnClickListener(this);
        mBack.setOnClickListener(this);
        //获取输入的手机号信息

        mMobileNO.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    mLoginMobile.setEnabled(true);
                } else {
                    mLoginMobile.setEnabled(false);
                }
            }
        });

        LinearLayout switch_service = (LinearLayout) this.findViewById(R.id.switch_service);
        switch_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivityV33.this, SwitchServerActivity.class);
                startActivity(intent);
            }
        });





    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_v33_mobile://跳转到输入密码界面
                if (checkAddress()) {
                    loadLicense();
                } else {
                    Toast.makeText(LoginActivityV33.this, "请先设置服务器地址", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivityV33.this, SwitchServerActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ic_back:
                finish();
                break;
            case R.id.ll_create_account://跳转到注册页面
                startActivity(new Intent(this, RegisterActivityV33.class));
                break;
        }
    }


    private boolean checkAddress() {

        if (TextUtils.isEmpty(AccountHelper.getUMSPAddress(this))) {
            return false;
        } else {

            return true;
        }


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        System.exit(0);
    }

    //跳转到输入密码界面

    private void gotoPwdActivity() {
        String mMobile = mMobileNO.getText().toString();
        if (TextUtils.isEmpty(mMobile)) {
            Toast.makeText(LoginActivityV33.this, R.string.error_account_required, Toast.LENGTH_SHORT).show();
        } else if (!CommUtil.isAccountValid(mMobile)) {
            Toast.makeText(LoginActivityV33.this, R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
        } else {


            Intent intent_pwd = new Intent(LoginActivityV33.this, ValidateActivityV33.class);
            intent_pwd.putExtra("phone", mMobile);

            intent_pwd.putExtra("isFirst", true);
            startActivity(intent_pwd);

//            Intent intent_pwd = new Intent(LoginActivityV33.this, SmsActivityV33.class);
//            intent_pwd.putExtra("phone",mMobile);
//            intent_pwd.putExtra("isFirst",true);
//            startActivity(intent_pwd);
//            //保存输入的手机号信息
//            AccountHelper.saveAccountMobile(LoginActivityV33.this, mMobile);


//            checkAccountExist();


//            phone = getIntent().getStringExtra("phone");
//            isReset = getIntent().getBooleanExtra("reset", false);//false登录，true忘记密码
//            isRegister = getIntent().getBooleanExtra("register", false);//true注册
//            isFirst = getIntent().getBooleanExtra("isFirst", false);//true首次登录 false 再次登录


        }
    }

    private void getMAC() {

        String mMobile = mMobileNO.getText().toString();
        accountController.getMac(this, mMobile, "2", new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {

                    Intent intent_pwd = new Intent(LoginActivityV33.this, SmsActivityV33.class);
                    intent_pwd.putExtra("phone", mMobile);
                    intent_pwd.putExtra("MAC", result);

                    intent_pwd.putExtra("isFirst", true);
                    startActivity(intent_pwd);
                    //保存输入的手机号信息
                    AccountHelper.saveAccountMobile(LoginActivityV33.this, mMobile);

                } else {
                    Toast.makeText(LoginActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void checkAccountExist() {
        String mMobile = mMobileNO.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String strInfo = ParamGen.getCheckIsAccountExistedParams(mMobile);
                    UniTrust dao = new UniTrust(LoginActivityV33.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.IsAccountExisted(strInfo);

                    JSONObject jb = JSONObject.fromObject(responseStr);
                    final String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    String returnStr = jb.getString(CommonConst.RETURN_MSG);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultStr.equals("0")) {      //账户已经存在


                                final String result = jb.getString(CommonConst.RETURN_RESULT);
                                if (result.equals("true")) {
                                    getMAC();
                                } else {
                                    Toast.makeText(getApplicationContext(), "账户不存在", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "获取账户是否存在失败", Toast.LENGTH_LONG).show();

                            }


                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }


    private void loadLicense() {

        new Thread() {
            public void run() {
                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
                uniTrust.setUMSPServerUrl(CommonConst.UM_APP_UMSP_SERVER, AccountHelper.getUMSPAddress(LoginActivityV33.this));

                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(AccountHelper.getUMSPAddress(LoginActivityV33.this)));
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                            Toast.makeText(LoginActivityV33.this, retMsg, Toast.LENGTH_LONG).show();
//                            finish();
                        } else {
                            AccountHelper.setLoadLicence(LoginActivityV33.this, true);


                            gotoPwdActivity();

                        }
                    }
                });

            }
        }.start();
    }


}
