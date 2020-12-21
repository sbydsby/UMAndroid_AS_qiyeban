package com.sheca.umee.account;

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

import com.sheca.umee.BaseActivity;
import com.sheca.umee.R;
import com.sheca.umee.interfaces.ResponseCallback;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.presenter.AccountController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommUtil;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;

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
//                Intent intent_pwd = new Intent(ReLoginActivityV33.this,ForgetPwdActivityV33.class);
//                intent_pwd.putExtra("phone",mMobile);
//                startActivity(intent_pwd);

                getMAC();


                break;
            case R.id.tv_more_relogin:
                showBottomDialog(ReLoginActivityV33.this);
                break;
            case R.id.login_v33_relogin:
//                accountLogin();
                loadLicense();
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


    private void getMAC() {

        mAccountController.getMac(this, mMobile, "2", new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {


                    Intent intent_pwd = new Intent(ReLoginActivityV33.this,SmsActivityV33.class);
                    intent_pwd.putExtra("name",AccountHelper.getIDName(ReLoginActivityV33.this));
                    intent_pwd.putExtra("idNumber",AccountHelper.getIdcardno(ReLoginActivityV33.this));
                    intent_pwd.putExtra("phone",mMobile);
                    startActivity(intent_pwd);


                } else {
                    Toast.makeText(ReLoginActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    private void loadLicense() {
        UniTrust uniTrust=new UniTrust(this,false);

        new Thread() {
            public void run() {
                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
                uniTrust.setUMSPServerUrl(CommonConst.UM_APP_UMSP_SERVER, AccountHelper.getUMSPAddress(ReLoginActivityV33.this));

                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(AccountHelper.getUMSPAddress(ReLoginActivityV33.this)));
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                            Toast.makeText(ReLoginActivityV33.this, retMsg, Toast.LENGTH_LONG).show();
//                            finish();
                        } else {
                            AccountHelper.setLoadLicence(ReLoginActivityV33.this, true);


                            accountLogin();

                        }
                    }
                });

            }
        }.start();
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


    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
