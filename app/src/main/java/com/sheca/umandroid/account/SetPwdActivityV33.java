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

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;

public class SetPwdActivityV33 extends BaseActivity implements View.OnClickListener {
    View mBack;

    private TextView sign_in_button;
    private EditText edt_pwd;

    private ImageView switch_pwd;

    boolean isShowPwd = false;

    boolean isRegister = false;

    String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_setpwd);
        initData();
        initView();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
                if (check()) {
                    String pwd=edt_pwd.getText().toString().trim();
                    Intent intent = new Intent(SetPwdActivityV33.this, ValidateActivityV33.class);
                    intent.putExtra("register", isRegister);
                    intent.putExtra("phone", phone);
                    intent.putExtra("pwd", pwd);
                    startActivity(intent);
                }
                break;
        }
    }
}
