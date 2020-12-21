package com.sheca.umee.account;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umee.BaseActivity;
import com.sheca.umee.R;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.presenter.AccountController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.ParamGen;
import com.sheca.umee.widget.vcedittext.VerificationAction;
import com.sheca.umee.widget.vcedittext.VerificationCodeEditText;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

public class SmsActivityV33 extends BaseActivity implements View.OnClickListener {
    View mBack;
    private TextView sign_in_button;
    private TextView txt_phone;
    private VerificationCodeEditText edt_code;

    private boolean isReset = false;
    private boolean isRegister = false;
    private boolean isFirst = false;
    AccountController accountController;
    private AccountDao mAccountDao = null;


    String phone="";
//    String MAC = "";
    String name = "";
    String idNumber = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_sms);
        accountController = new AccountController();


        initData();
        initView();
    }

    private void initData() {

        name = getIntent().getStringExtra("name");
        idNumber = getIntent().getStringExtra("idNumber");
//        MAC = getIntent().getStringExtra("MAC");
        phone = getIntent().getStringExtra("phone");
        isReset = getIntent().getBooleanExtra("reset", false);//false登录，true忘记密码
        isRegister = getIntent().getBooleanExtra("register", false);//true注册
        isFirst = getIntent().getBooleanExtra("isFirst", false);//true首次登录 false 再次登录
    }

    private void initView() {

        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_phone.setText("已将验证码发送至 " + phone.substring(0, 3) + "****" + phone.substring(7, 11));

        edt_code = (VerificationCodeEditText) findViewById(R.id.edt_code);
        edt_code.setOnVerificationCodeChangedListener(new VerificationAction.OnVerificationCodeChangedListener() {
            @Override
            public void onVerCodeChanged(CharSequence s, int start, int before, int count) {
                sign_in_button.setEnabled(false);
            }

            @Override
            public void onInputCompleted(CharSequence s) {
                sign_in_button.setEnabled(true);
            }
        });

        sign_in_button = (TextView) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(this);
        mBack = findViewById(R.id.ic_back);
        mBack.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
//                if (isReset) {
//                    verifyMac("3");
//                } else if (isRegister) {
//                    verifyMac("1");
//                } else {
//                    verifyMac("2");
//                }

                verifyMac();

                break;
        }
    }


    private void verifyMac() {

        String mac = edt_code.getText().toString().trim();

        accountController.smsLogin(this,phone,name,idNumber,mac);

//        userLoginByValidationCode(name,idNumber,phone,mac);

//        if (mac.equals(MAC)) {


//        Intent intent = new Intent(SmsActivityV33.this, SetPwdActivityV33.class);
//        intent.putExtra("register", isRegister);
//        intent.putExtra("phone", phone);
//        startActivity(intent);

//        accountController.verifyMac(this, phone, type, mac, new ResponseCallback() {
//            @Override
//            public void responseCallback(String returnCode, String retMsg, String result) {
//                if (returnCode.equals("0")) {
//                    Intent intent = new Intent(SmsActivityV33.this, ValidateActivityV33.class);
//                        intent.putExtra("phone", phone);
////                    intent.putExtra("MAC", mac);
//                        startActivity(intent);
//
////                    if (isReset) {
////                        Intent intent = new Intent(SmsActivityV33.this, ResetPwdActivityV33.class);
////                        intent.putExtra("phone", phone);
////                        intent.putExtra("isFirst", isFirst);
////                        startActivity(intent);
////                    } else {
////                        Intent intent = new Intent(SmsActivityV33.this, SetPwdActivityV33.class);
////                        intent.putExtra("register", isRegister);
////                        intent.putExtra("phone", phone);
////                        startActivity(intent);
////                    }
////            userLoginByValidationCode(phone, mac);
//                } else {
//
//                    Toast.makeText(SmsActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        }else{
//            Toast.makeText(this,"验证码错误",Toast.LENGTH_SHORT).show();
//        }
    }


    public boolean userLoginByValidationCode(final String name,final String idNumber,final String mobile, final String code) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strInfo = ParamGen.getUserLoginByValidationCodeParams(mobile, code);
                    UniTrust dao = new UniTrust(SmsActivityV33.this, false); //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.LoginByMAC(strInfo);

                    final APPResponse response = new APPResponse(responseStr);
                    int resultStr = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (resultStr == 0) {

                        JSONObject jbRet = response.getResult();
                      String  tokenID = jbRet.optString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);

                        AccountHelper.setNeedChangePwd(SmsActivityV33.this,true);
                        AccountHelper.clearAllUserData(SmsActivityV33.this);
                        AccountHelper.savePersonInfoToLocal(SmsActivityV33.this,tokenID,mobile);
                    }else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SmsActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

        return true;
    }


}
