package com.sheca.umandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

public class AuthChoiceActivity extends BaseActivity2 {
    private AccountDao mAccountDao = null;
    private Button btnTakePhoto, btnInput;

    private UniTrust uniTrust;

    private enum AUTH_TYPE {AUTH_TYPE_TAKE_PHOTO, AUTH_TYPE_INPUT}

    ;
    private boolean mIsFaceAuth = false;       //是否实名认证
    private boolean mIsDao = false;
    private boolean mIsDownload = false;

    private boolean needPay = false;

//    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.title_activity_auth2, R.layout.activity_auth_choice);

        uniTrust = new UniTrust(this, false);
        uniTrust.setFaceAuth(true);

        mAccountDao = new AccountDao(AuthChoiceActivity.this);
        btnTakePhoto = (Button) findViewById(R.id.btn_takephoto);  //拍摄照片
        btnInput = (Button) findViewById(R.id.btn_input);          //手动输入

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("needPay") != null) {
                needPay = true;
                faceAuth(AUTH_TYPE.AUTH_TYPE_INPUT);
            }
            if (extras.getString("isFaceAuth") != null) {
                mIsFaceAuth = true;
            }
            if (extras.getString("message") != null) {
                mIsDao = true;
            }
            if (extras.getString("download") != null) {
                mIsDownload = true;
            }

            if(extras.getString("isPayAndAuth") != null){
                needPay = true;
            }
        }

        btnTakePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AUTH_TYPE auth_photo = AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO;
                faceAuth(auth_photo);
            }
        });

        btnInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final AUTH_TYPE auth_input = AUTH_TYPE.AUTH_TYPE_INPUT;
                faceAuth(auth_input);

//                authSuccess();

            }
        });
    }

    private void setAccountVal(String name, String id) {
        String setRes = uniTrust.SetAccountCertification(ParamGen.getAccountVerification(
                AccountHelper.getToken(getApplicationContext()),
                name, id, "4"));

        Log.d("unitrust", setRes);

        final APPResponse setResp = new APPResponse(setRes);
        final int retCode = setResp.getReturnCode();
        final String retMsg = setResp.getReturnMsg();

        if(retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AuthChoiceActivity.this, retMsg, Toast.LENGTH_SHORT).show();finish();
                }
            });

            finish();
        }else{
            finish();
        }

    }

        private void faceAuth (final AUTH_TYPE authType){

//            Intent intent = new Intent();
//            intent.setClass(AuthChoiceActivity.this, PayActivity.class);
//            intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
//            intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
//            startActivity(intent);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    uniTrust.setFaceAuthBGColor("#F7D05B");   //设置界面背景色
                    uniTrust.setFaceAuthTextColor("#000000");  //设置界面标题色

                    String info;
                    if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO){
                        uniTrust.setFaceAuthActionNumber(3);
                        uniTrust.setFaceAuth(true);
                        info = ParamGen.getFaceAuthOCR();
                    }else{
                        info = ParamGen.getFaceAuth(getApplicationContext());
                    }


                    String result = uniTrust.FaceAuth(info);

                    Log.d("unitrust", result);
                    APPResponse response = new APPResponse(result);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AuthChoiceActivity.this, getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
                            }
                        });
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AuthChoiceActivity.this, retMsg, Toast.LENGTH_LONG).show();
                            }
                        });

                        //人脸验证成功
                        JSONObject jbRet = response.getResult();
                        String personName = jbRet.getString("personName");
                        String personId = jbRet.getString("personID");
                        AccountHelper.setRealName(personName);
                        AccountHelper.setIdcardno(personId);
                        String authMsg = AccountHelper.getUsername(getApplicationContext());
                        SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_HAS_AUTH, authMsg);

                        SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
                        SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));

                        Account acount = mAccountDao.getLoginAccount();
                        acount.setStatus(2);
                        acount.setIdentityName(personName);
                        acount.setIdentityCode(personId);

                        mAccountDao.update(acount);
                        SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setInt(CommonConst.PARAM_STATUS,2);

                        if (needPay) {
                            Intent intent = new Intent();
                            intent.setClass(AuthChoiceActivity.this, com.sheca.umandroid.PayActivity.class);
                            intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
                            intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
                            startActivity(intent);
                            finish();
                        }else{
                            setAccountVal(personName, personId);
                        }


                    }
                }
            }).start();

        }
    }
