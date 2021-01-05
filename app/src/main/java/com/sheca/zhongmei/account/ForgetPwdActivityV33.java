package com.sheca.zhongmei.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.zhongmei.BaseActivity;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.interfaces.ResponseCallback;
import com.sheca.zhongmei.presenter.AccountController;

/**
 * 首次登录页面输入密码界面
 */
public class ForgetPwdActivityV33 extends BaseActivity implements View.OnClickListener {

    private View mBack;
    private TextView txt_phone;

    private Button sign_in_button;
    AccountController accountController;
    String phone;
    boolean isFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_forget_pwd);
        accountController = new AccountController();
        initData();
        initView();
    }

    private void initData() {
        isFirst=getIntent().getBooleanExtra("isFirst",false);
    }

    private void initView() {
        mBack = findViewById(R.id.ic_back);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        sign_in_button = (Button) findViewById(R.id.sign_in_button);

        mBack.setOnClickListener(this);
        sign_in_button.setOnClickListener(this);
        phone = getIntent().getStringExtra("phone");
        txt_phone.setText("将向 " + phone.substring(0, 3) + "****" + phone.substring(7, 11) + "发送验证码");
    }

    private void sendSms() {
//        startActivity(new Intent(ForgetPwdActivityV33.this, ResetPwdActivityV33.class).putExtra("phone",phone));
        accountController.getMac(this, phone, "3", new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {
                    Intent intent = new Intent(ForgetPwdActivityV33.this, SmsActivityV33.class);
                    intent.putExtra("phone", getIntent().getStringExtra("phone"));
                    intent.putExtra("isFirst",isFirst);
                    intent.putExtra("reset", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgetPwdActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
                sendSms();

                break;


        }
    }
}
