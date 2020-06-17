package com.sheca.umandroid.account;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umandroid.BaseActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.presenter.AccountController;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;

/**
 * 首次登录页面输入密码界面
 */
public class ReLoginActivityV33 extends BaseActivity implements View.OnClickListener {

    private View mBack;
    private TextView mAccount;
    private EditText mLoginPWD;
    private TextView mForgetPwd;
    private TextView mMore;
    private Button mRelogin;
    private TextView mRegister;
    private TextView mLoginAccount;
    private Dialog bottomDialog;
    private String mMobile="";
    private AccountController mAccountController = new AccountController();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_relogin);
        initView();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
    }

    private void initData() {
        mMobile=AccountHelper.getAccountMobile(ReLoginActivityV33.this);
       String fixMob=mMobile.substring(0, 3) + "****" + mMobile.substring(7, 11);
        mAccount.setText(fixMob);
    }

    private void initView() {
        mBack=findViewById(R.id.ic_back);
        mAccount=(TextView)findViewById(R.id.tv_mobile);
        mLoginPWD=(EditText)findViewById(R.id.et_v33_login_pwd);
        mForgetPwd=(TextView)findViewById(R.id.tv_forget_pwd);
        mMore=(TextView)findViewById(R.id.tv_more_relogin);
        mRelogin=(Button)findViewById(R.id.login_v33_relogin);

        mBack.setOnClickListener(this);
        mForgetPwd.setOnClickListener(this);
        mMore.setOnClickListener(this);
        mRelogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ic_back:
                finish();
                break;
            case R.id.tv_forget_pwd:
                Intent intent_pwd = new Intent(ReLoginActivityV33.this,ForgetPwdActivityV33.class);
                intent_pwd.putExtra("phone",mMobile);
                startActivity(intent_pwd);
                break;
            case R.id.tv_more_relogin:
                showBottomDialog(ReLoginActivityV33.this);
                break;
            case R.id.login_v33_relogin:
                accountLogin();
                break;
            //底部dialog点击事件
            case R.id.tv_goto_register:
                closeBottomDialog();
                Intent intent_register = new Intent(ReLoginActivityV33.this,RegisterActivityV33.class);
                startActivity(intent_register);
                break;
            case R.id.tv_goto_account:
                closeBottomDialog();
                Intent intent_login = new Intent(ReLoginActivityV33.this,LoginActivityV33.class);
                startActivity(intent_login);
                break;
        }
    }


    /**
     * 再次登录接口调用
     */
    private void accountLogin() {
        String strPwd= mLoginPWD.getText().toString();
        if (TextUtils.isEmpty(strPwd) || !CommUtil.isPasswordValid(strPwd)) {
            Toast.makeText(ReLoginActivityV33.this,R.string.error_pwd,Toast.LENGTH_SHORT).show();
        }else{
            String mAccountName = AccountHelper.getIDName(ReLoginActivityV33.this);
            String mAccountNO = AccountHelper.getIDNumber(ReLoginActivityV33.this);
            mAccountController.accountLogin(ReLoginActivityV33.this,mMobile,strPwd,mAccountName,mAccountNO);
        }

    }

    /**
     * 底部对话框
     * @param mActivity
     */
    public void showBottomDialog(Activity mActivity){
         bottomDialog = new Dialog(mActivity, R.style.BottomDialog);
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_content_normal, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = mActivity.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
        //前往注册
        mRegister=(TextView)contentView.findViewById(R.id.tv_goto_register);
        //更换账户
        mLoginAccount=(TextView)contentView.findViewById(R.id.tv_goto_account);
        mRegister.setOnClickListener(this);
        mLoginAccount.setOnClickListener(this);
    }

    public void closeBottomDialog(){
        if(bottomDialog!=null && bottomDialog.isShowing()){
            bottomDialog.dismiss();
        }
    }
}
