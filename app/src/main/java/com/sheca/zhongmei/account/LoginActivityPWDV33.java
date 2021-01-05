package com.sheca.zhongmei.account;

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

import com.sheca.zhongmei.BaseActivity;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.CommUtil;
import com.sheca.zhongmei.util.CommonConst;

/**
 * 首次登录页面输入密码界面
 */
public class LoginActivityPWDV33 extends BaseActivity implements View.OnClickListener {

    private View mBack;
    private LinearLayout mForgetPwd;
    private EditText mPwd;
    private TextView mNext;
    private String mMobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_inputpwd);
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mMobile=getIntent().getStringExtra("phone");
    }

    private void initView() {
        mMobile=getIntent().getStringExtra("phone");
        mForgetPwd = (LinearLayout) findViewById(R.id.ll_forget_pwd);
        mPwd = (EditText) findViewById(R.id.et_v33_login_pwd);
        mNext = (TextView) findViewById(R.id.login_v33_pwd_next);
        mBack = findViewById(R.id.ic_back);

        mNext.setOnClickListener(this);
        mForgetPwd.setOnClickListener(this);
        mBack.setOnClickListener(this);


        mPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    mNext.setEnabled(true);
                } else {
                    mNext.setEnabled(false);
                }
            }
        });
//        mPwd.setText(AccountHelper.getAccountPWD(LoginActivityPWDV33.this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_forget_pwd:
                Intent intent = new Intent(this, ForgetPwdActivityV33.class);
                intent.putExtra("phone",mMobile);
                intent.putExtra("isFirst",true);
                startActivity(intent);
                break;
            case R.id.ic_back:
                finish();
                break;
            case R.id.login_v33_pwd_next:
                gotoNextFaceAuthActivity();//跳入实名信息界面
                break;
        }
    }


    /**
     * 跳入实名信息界面
     */
    private void gotoNextFaceAuthActivity() {
        String strPwd = mPwd.getText().toString();
        if (TextUtils.isEmpty(strPwd) || !CommUtil.isPasswordValid(strPwd)) {
            Toast.makeText(LoginActivityPWDV33.this, R.string.error_pwd, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent_pwd = new Intent(LoginActivityPWDV33.this, ValidateActivityV33.class);
            intent_pwd.putExtra(CommonConst.FACE_FROM_LOGIN, "fromLogin");
            intent_pwd.putExtra("phone",mMobile);
            intent_pwd.putExtra("pwd",strPwd);
            startActivity(intent_pwd);
            //保存密码信息
            AccountHelper.savaAcoountPWD(LoginActivityPWDV33.this, strPwd);
        }

    }
}
