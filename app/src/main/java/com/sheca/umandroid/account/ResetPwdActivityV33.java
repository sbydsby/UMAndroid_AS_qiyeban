package com.sheca.umandroid.account;

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

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.interfaces.ResponseCallback;
import com.sheca.umandroid.presenter.AccountController;

public class ResetPwdActivityV33 extends BaseActivity implements View.OnClickListener {
    View mBack;

    private TextView sign_in_button;
    TextView txt_hint;
    private EditText edt_pwd;

    private ImageView switch_pwd;

    boolean isShowPwd = false;
    boolean isFirst = false;
    AccountController accountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_setpwd);
        accountController = new AccountController();
        initData();
        initView();
    }

    private void initData() {
        isFirst = getIntent().getBooleanExtra("isFirst", false);
    }


    private void initView() {
        txt_hint = (TextView) findViewById(R.id.txt_hint);
        txt_hint.setText("重置登录密码");
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
                    // 显示为普通文本
                    isShowPwd = true;
                    edt_pwd.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    switch_pwd.setImageResource(R.mipmap.pwd_invisible);
                }
                edt_pwd.setKeyListener(DigitsKeyListener.getInstance("0123456789qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM_"));

            }
        });
        mBack = findViewById(R.id.ic_back);
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
        return true;
    }

    private void resetPwd() {
        String pwd = edt_pwd.getText().toString().trim();
        String phone = getIntent().getStringExtra("phone");
        accountController.resetPWD(ResetPwdActivityV33.this, phone, pwd, new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {
                    Toast.makeText(ResetPwdActivityV33.this, "重置成功", Toast.LENGTH_SHORT).show();
                    Intent intent;
                    if (isFirst) {
                        intent=new Intent(ResetPwdActivityV33.this, LoginActivityPWDV33.class);
                        intent.putExtra("phone",phone);
                        startActivity(intent);
                    } else {
                        intent=new Intent(ResetPwdActivityV33.this, ReLoginActivityV33.class);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(ResetPwdActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
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
                    resetPwd();
                break;
        }
    }
}
