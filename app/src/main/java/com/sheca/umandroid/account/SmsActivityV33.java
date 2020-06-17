package com.sheca.umandroid.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.interfaces.ResponseCallback;
import com.sheca.umandroid.presenter.AccountController;
import com.sheca.umandroid.widget.vcedittext.VerificationAction;
import com.sheca.umandroid.widget.vcedittext.VerificationCodeEditText;

public class SmsActivityV33 extends BaseActivity implements View.OnClickListener {
    View mBack;
    private TextView sign_in_button;
    private TextView txt_phone;
    private VerificationCodeEditText edt_code;
    String phone;
    private boolean isReset = false;
    private boolean isRegister = false;
    private boolean isFirst = false;
    AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_sms);
        accountController = new AccountController();

        initData();
        initView();
    }

    private void initData() {
        phone = getIntent().getStringExtra("phone");
        isReset = getIntent().getBooleanExtra("reset", false);//false登录，true忘记密码
        isRegister = getIntent().getBooleanExtra("register", false);//true注册
        isFirst = getIntent().getBooleanExtra("isFirst", false);//true首次登录 false 再次登录
    }

    private void initView() {

        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_phone.setText("已将验证码发送至 " + phone.substring(0, 3) + "****" + phone.substring(7, 11));

        edt_code = (VerificationCodeEditText) findViewById(R.id.edt_code);
        edt_code.setOnVerificationCodeChangedListener(new VerificationAction.OnVerificationCodeChangedListener() {
            @Override
            public void onVerCodeChanged(CharSequence s, int start, int before, int count) {
                sign_in_button.setEnabled(false);
            }

            @Override
            public void onInputCompleted(CharSequence s) {
                sign_in_button.setEnabled(true);
            }
        });

        sign_in_button = (TextView) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(this);
        mBack = findViewById(R.id.ic_back);
        mBack.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
                if (isReset) {
                    verifyMac("3");
                } else if (isRegister) {
                    verifyMac("1");
                } else {
                    verifyMac("2");
                }

                break;
        }
    }


    private void verifyMac(String type) {
        String mac = edt_code.getText().toString();

        accountController.verifyMac(this, phone, type, mac, new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {
                    if (isReset) {
                        Intent intent = new Intent(SmsActivityV33.this, ResetPwdActivityV33.class);
                        intent.putExtra("phone", phone);
                        intent.putExtra("isFirst", isFirst);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SmsActivityV33.this, SetPwdActivityV33.class);
                        intent.putExtra("register", isRegister);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                    }
                } else {

                    Toast.makeText(SmsActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
