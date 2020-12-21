package com.sheca.umee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sheca.umee.util.CommUtil;

public class CertPasswordActivity extends BaseActivity2 {

    private EditText etPwd,etPwdAgain,etCertName;
    private Button button;
    
    private int    mPayType = 0;
    private String strLoginAccount;
    private String strLoginId = "";
    
    private String strReqNumber = "";
	private String strStatus = "";
	private String strCertType = "";
	
	private boolean mIsDao = false;     //第三方接口调用标记
	private boolean mIsReset = false;   //是否重置密码标记
	private boolean mIsDownload = false; 
	private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
	private String  mStrBTDevicePwd = "";    //蓝牙key密码


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.title_setpwd,R.layout.activity_cert_setpwd);

        etPwd = (EditText)findViewById(R.id.et_pwd);
        etPwdAgain = (EditText)findViewById(R.id.et_pwd_again);
        etCertName = (EditText)findViewById(R.id.et_certname);
        button = (Button)findViewById(R.id.btn_ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPassword()){
                    Intent intent = new Intent();
                    intent.putExtra("psd", etPwd.getText().toString().trim());
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
            }
        });

        this.findViewById(R.id.btn_goback).setVisibility(View.GONE);

    }

    private boolean checkPassword(){
        String psd1 = etPwd.getText().toString().trim();
        String psd2 = etPwdAgain.getText().toString().trim();

        if (psd1.length()<8 || psd1.length()>16){
            Toast.makeText(this,R.string.error_psd,Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!CommUtil.isPasswordValid(psd1)){
            Toast.makeText(this,"密码强度过低，必须由8至16位英文、数字或符号组成",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (psd2.length()<8){
            Toast.makeText(this,R.string.error_psd,Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!psd1.equals(psd2)){
            Toast.makeText(this,R.string.error_psd2,Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
