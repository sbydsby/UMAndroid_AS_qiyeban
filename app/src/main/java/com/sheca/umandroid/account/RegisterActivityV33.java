package com.sheca.umandroid.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.interfaces.ResponseCallback;
import com.sheca.umandroid.presenter.AccountController;
import com.sheca.umandroid.util.CommUtil;

public class RegisterActivityV33 extends BaseActivity implements View.OnClickListener {

    private View mBack;
    private TextView sign_in_button;
    private EditText edt_phone;

    private AccountController mAccountController;
    private String mAccount="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_register);
        mAccountController = new AccountController();
        initView();
        initData();
    }

    private void initData() {
        mAccount=getIntent().getStringExtra("isnotexit_account");
        if(mAccount!=null && !mAccount.equals("")){
            edt_phone.setText(mAccount);
        }
    }

    private void initView() {

        edt_phone = (EditText) findViewById(R.id.edt_phone);
        edt_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    sign_in_button.setEnabled(true);
                } else {
                    sign_in_button.setEnabled(false);
                }

            }
        });

        sign_in_button = (TextView) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(this);
        mBack = findViewById(R.id.ic_back);
        mBack.setOnClickListener(this);
    }

    private boolean check() {
        if (!CommUtil.isPhoneNumber(edt_phone.getText().toString().trim())) {
            Toast.makeText(this, "手机号格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void register() {
        String phone=edt_phone.getText().toString().trim();
//        startActivity(new Intent(RegisterActivityV33.this, SetPwdActivityV33.class).putExtra("register", true).putExtra("phone",phone));

        mAccountController.getMac(this, phone, "1", new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {

                    Intent intent = new Intent(RegisterActivityV33.this, SmsActivityV33.class);
                    intent.putExtra("phone", phone);
                    intent.putExtra("register", true);//注册
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivityV33.this,retMsg,Toast.LENGTH_SHORT).show();
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
                if (check())
                    register();
                break;
        }
    }

}
