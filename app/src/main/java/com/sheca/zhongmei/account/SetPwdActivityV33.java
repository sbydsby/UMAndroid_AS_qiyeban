package com.sheca.zhongmei.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.zhongmei.BaseActivity;
import com.sheca.zhongmei.MainActivity;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.interfaces.ResponseCallback;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.presenter.AccountController;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.zhongmei.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

public class SetPwdActivityV33 extends BaseActivity implements View.OnClickListener {
    View mBack;

    private TextView sign_in_button;
    private EditText edt_pwd;

    private ImageView switch_pwd;

    boolean isShowPwd = false;

    boolean isRegister = false;

    String phone = "";

    AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_setpwd);
        initData();
        initView();
        accountController = new AccountController();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void initData() {
        isRegister = getIntent().getBooleanExtra("register", false);
        phone = getIntent().getStringExtra("phone");
    }

    private void initView() {
        edt_pwd = (EditText) findViewById(R.id.edt_pwd);
//        edt_pwd.setFilters(new InputFilter[]{new SpaceFilter()});
        edt_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 7) {
                    sign_in_button.setEnabled(true);
                } else {
                    sign_in_button.setEnabled(false);
                }
            }
        });

        sign_in_button = (TextView) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(this);

        switch_pwd = (ImageView) findViewById(R.id.switch_pwd);
        switch_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowPwd) {
                    isShowPwd = false;
                    edt_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    switch_pwd.setImageResource(R.mipmap.pwd_visible);
                } else {
                    isShowPwd = true;
                    edt_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    switch_pwd.setImageResource(R.mipmap.pwd_invisible);
                }
                edt_pwd.setKeyListener(DigitsKeyListener.getInstance("0123456789qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM_"));

            }
        });
        mBack = findViewById(R.id.ic_back);
        mBack.setVisibility(View.GONE);
        mBack.setOnClickListener(this);
    }

    /**
     * 禁止输入空格
     *
     * @return
     */
    public class SpaceFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" "))
                return "";
            return null;
        }
    }

    private boolean check() {

        String pwd = edt_pwd.getText().toString().trim();

        if (pwd.length() < 8) {
            Toast.makeText(this,"密码至少为8位",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
                if (check()) {
                    String pwd = edt_pwd.getText().toString().trim();

                    resetPwd(pwd);


//                    Intent intent = new Intent(SetPwdActivityV33.this, ValidateActivityV33.class);
//                    intent.putExtra("register", isRegister);
//                    intent.putExtra("phone", phone);
//                    intent.putExtra("pwd", pwd);
//                    startActivity(intent);


                }
                break;
        }
    }


    //登录
    public void accountLogin(Activity context, String mobile, String pwd, String name, String idNo) {
        new MyAsycnTaks() {
            private String strMsg = "";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(mobile, pwd, idNo));
            }

            @Override
            public void postTask() {
                APPResponse response = new APPResponse(strMsg);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                final String tokenID;

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                    JSONObject jbRet = response.getResult();
                    tokenID = jbRet.optString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                    AccountHelper.clearAllUserData(context);
                    AccountHelper.savePersonInfoToLocal(context, tokenID, mobile);

                } else {
                    Toast.makeText(context, retMsg + retCode, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    private void resetPwd(final String mNewPassword) {

        accountController.resetPWD(SetPwdActivityV33.this, phone, mNewPassword, new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {
                    //非首次登录
                    AccountHelper.setChangePassword(SetPwdActivityV33.this, true); //已经设置过密码
                    SharePreferenceUtil.getInstance(SetPwdActivityV33.this).setBoolean(CommonConst.PARAM_HAS_LOGIN, true);
                    Toast.makeText(SetPwdActivityV33.this, "密码设置成功", Toast.LENGTH_SHORT).show();
                    Intent intent;
//                    if (isFirst) {
//                        intent=new Intent(SetPwdActivityV33.this, LoginActivityPWDV33.class);
//                        intent.putExtra("phone",phone);
//                        startActivity(intent);
//                    } else {
//                        intent=new Intent(SetPwdActivityV33.this, ReLoginActivityV33.class);
//                        startActivity(intent);
//                    }

                    intent = new Intent(SetPwdActivityV33.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SetPwdActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final UniTrust dao = new UniTrust(SetPwdActivityV33.this, false); //UM SDK+调用类，第二参数表示是否显示提示界面
//
//                try {
//                    String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//                    final String mActName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME);
//
//                    String strInfo = ParamGen.getResetUserPwdParams(mNewPassword, mTokenID, mActName);
//
//                    int resultStr = 1;
//
//                    String responseStr = dao.ResetAccountPassword(strInfo);
//                    final APPResponse response = new APPResponse(responseStr);
//                    resultStr = response.getReturnCode();
////                    retMsg = response.getReturnMsg();
//
//                    final int finalResultStr = resultStr;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (0 == finalResultStr) {
//                                SharePreferenceUtil.getInstance(getApplicationContext()).setBoolean((CommonConst.FIRST_SMS_LOGIN + mActName), true);
//                                AccountDao accountDao = new AccountDao(SetPwdActivityV33.this);
//                                Account curAct = accountDao.getLoginAccount();
//                                String mhashPass = CommUtil.getPWDHashNew(mNewPassword);
//                                curAct.setPassword(mhashPass);
//                                curAct.setActive(1);   //激活账户
//                                accountDao.update(curAct);
//                                Intent intent = new Intent(SetPwdActivityV33.this, MainActivity.class);
//                                startActivity(intent);
//                                SetPwdActivityV33.this.finish();
//                                Toast.makeText(SetPwdActivityV33.this, "设置账户口令成功", Toast.LENGTH_LONG).show();
//
//                            } else {
//                                Toast.makeText(SetPwdActivityV33.this, "修改失败" , Toast.LENGTH_SHORT).show();
//
//                            }
//                        }
//                    });
//
//                } catch (Exception e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SetPwdActivityV33.this, "修改失败" , Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    });
//
//
//                }
//
//
//            }
//        }).start();


    }
}
