package com.sheca.umandroid.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;

/**
 * 首次登录输入手机号界面
 */
public class LoginActivityV33 extends BaseActivity implements View.OnClickListener {
    private LinearLayout mCreateAccount;
    private EditText mMobileNO;
    private TextView mLoginMobile;
    private View mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_login);
        initView();
    }

    private void initView() {
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_v33_mobile://跳转到输入密码界面
                gotoPwdActivity();
                break;
            case R.id.ic_back:
                finish();
                break;
            case R.id.ll_create_account://跳转到注册页面
                startActivity(new Intent(this, RegisterActivityV33.class));
                break;
        }
    }


    //跳转到输入密码界面
    private void gotoPwdActivity() {
        String mMobile = mMobileNO.getText().toString();
        if (TextUtils.isEmpty(mMobile)) {
            Toast.makeText(LoginActivityV33.this, R.string.error_account_required, Toast.LENGTH_SHORT).show();
        } else if (!CommUtil.isAccountValid(mMobile)) {
            Toast.makeText(LoginActivityV33.this, R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent_pwd = new Intent(LoginActivityV33.this, LoginActivityPWDV33.class);
            intent_pwd.putExtra("phone",mMobile);
            startActivity(intent_pwd);
            //保存输入的手机号信息
            AccountHelper.saveAccountMobile(LoginActivityV33.this, mMobile);
        }
    }


}
