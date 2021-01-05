package com.sheca.zhongmei.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.custle.dyrz.DYRZResult;
//import com.custle.dyrz.DYRZResultBean;
//import com.custle.dyrz.DYRZSDK;
import com.sheca.zhongmei.BaseActivity;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.interfaces.ResponseCallback;
import com.sheca.zhongmei.presenter.AccountController;
import com.sheca.zhongmei.util.CommUtil;

import java.util.UUID;

public class ValidateActivityV33 extends BaseActivity implements  View.OnClickListener {

    private TextView sign_in_button;
    private EditText edt_name;
    private EditText edt_code;
    private String transaction_id = "app" + UUID.randomUUID().toString();
    private String mAccountName = "";
    private String mAccountNO = "";
    private View mBack;

    private AccountController mAccountController = new AccountController();


    private boolean isRegister = false; //标记 是否来自注册页面，默认来自登录页面
    private String phone, pwd;

    int failTime = 0;//失败次数，三次就弹窗


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v33_validate);
        initData();
        initView();
    }


    private void initData() {
        isRegister = getIntent().getBooleanExtra("register", false);
        phone = getIntent().getStringExtra("phone");

    }

    private void initView() {
        edt_name = (EditText) findViewById(R.id.edt_name);
        edt_code = (EditText) findViewById(R.id.edt_code);
        sign_in_button = (TextView) findViewById(R.id.sign_in_button);

        sign_in_button.setOnClickListener(this);

        edt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                check();

            }
        });
        edt_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                check();
            }
        });
//        edt_name.setText(AccountHelper.getIDName(ValidateActivityV33.this));
//        edt_code.setText(AccountHelper.getIDNumber(ValidateActivityV33.this));
        mBack = findViewById(R.id.ic_back);
        mBack.setOnClickListener(this);
    }

    private void check() {
        if (edt_name.getText().toString().trim().length() > 0 && edt_code.getText().toString().trim().length() > 0) {
            sign_in_button.setEnabled(true);

        } else {
            sign_in_button.setEnabled(false);
        }
    }

    //进行人脸识别

    /**
     * 多源人脸识别
     *
     * @param mName 姓名
     * @param mID   身份证号
     */
    private void facialRecognition(String mName, String mID) {
//        DYRZConfigure mConfigure = DYRZConfigure.getInstance();
//        String mAppId = mConfigure.getAppID();
//        String mAppKey = mConfigure.getPriKey();
//        DYRZSDK.getInstance(mAppId, mAppKey, DYRZSDK.BUILD_RELEASE).faceWithAlipayAuth(ValidateActivityV33.this,
//                mName, mID, transaction_id, this);
    }

//    @Override
//    public void dyrzResultCallBack(DYRZResultBean dyrzResultBean) {
//        //人脸识别成功
//        closeProgDialog();
//        if ("0".equals(dyrzResultBean.getCode())) {
//            //人脸识别成功调用登录接口，保存用户信息
//            if (isRegister) {
//                registerAccount(phone, pwd);
//            } else {
//                mAccountController.accountLogin(ValidateActivityV33.this, phone, pwd, mAccountName, mAccountNO);
//            }
//        } else {
//            failTime++;
//            Toast.makeText(ValidateActivityV33.this, dyrzResultBean.getMsg() + dyrzResultBean.getCode(), Toast.LENGTH_SHORT).show();
//
//            if (failTime >= 3) {
//
//                showFailDialog();
//            }
//
//        }
//    }

    private void showFailDialog() {

        Dialog dialog = new Dialog(ValidateActivityV33.this, R.style.MyDialog);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(ValidateActivityV33.this).inflate(R.layout.item_fail, null, false);
        dialog.setContentView(view);
        TextView mUpdateContent = (TextView) view.findViewById(R.id.tv_update_content);
        TextView mNotUpdate = (TextView) view.findViewById(R.id.tv_not_update);
        TextView mBeginUpdate = (TextView) view.findViewById(R.id.tv_begin_update);
        //更新内容
        mUpdateContent.setText("请联系客服:" + com.sheca.zhongmei.util.CommonConst.SERVER_PHONE);

        //暂不更新
        mNotUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initData();
            }
        });
        //开始更新
        mBeginUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + com.sheca.zhongmei.util.CommonConst.SERVER_PHONE);
                intent.setData(data);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                finish();
                break;
            case R.id.sign_in_button:
                testAccount();
                break;
        }
    }


    private void testAccount() {
        mAccountName = edt_name.getText().toString().trim();
        mAccountNO = edt_code.getText().toString().trim();

        if (TextUtils.isEmpty(mAccountName)) {
            Toast.makeText(ValidateActivityV33.this, R.string.v33_login_tip5, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mAccountNO)) {
            Toast.makeText(ValidateActivityV33.this, R.string.v33_login_tip6, Toast.LENGTH_SHORT).show();
        } else if (mAccountName.contains(" ")) {
            Toast.makeText(ValidateActivityV33.this, R.string.v33_login_tip5_name, Toast.LENGTH_SHORT).show();
        } else if (mAccountNO.contains(" ") || !CommUtil.isPersonNO(mAccountNO)) {
            Toast.makeText(ValidateActivityV33.this, R.string.label_auth_formatidentycode, Toast.LENGTH_SHORT).show();
        } else {

//            mAccountController.accountLogin(ValidateActivityV33.this, phone, pwd, mAccountName, mAccountNO);


//            mAccountController.smsLogin(ValidateActivityV33.this, phone, MAC, mAccountName, mAccountNO);


            //调用账户是否存在接口，存在再调用人脸识别
            //判断账户是否存在
            mAccountController.accountIsExicted(ValidateActivityV33.this, phone, mAccountName, mAccountNO, new ResponseCallback() {
                @Override
                public void responseCallback(String returnCode, String retMsg, String result) {
//                    JSONObject jb = JSONObject.fromObject(result);
//                    String isExist = jb.getString(CommonConst.RETURN_RESULT);
                    if (result.equals("true")) {

                        getMAC();


//                        if (isRegister) {
//                            isRegisterBuissiness(true);
//                        } else {
//                            isLoginBuissiness(true, 0, returnCode, phone, pwd);
//                        }
                    } else {
                        Toast.makeText(ValidateActivityV33.this, "账户不存在", Toast.LENGTH_SHORT).show();
                    }

//                    int hasAccount = jb.getInt("hasOldAccount");
//                    if (returnCode.equals("0")) {
//                        if (isRegister) {
//                            isRegisterBuissiness(isExist);
//                        } else {
//                            isLoginBuissiness(isExist, hasAccount, returnCode, phone, pwd);
//                        }
//                    } else {
//                        Toast.makeText(ValidateActivityV33.this, retMsg + returnCode, Toast.LENGTH_SHORT).show();
//                    }
                }
            });

        }

    }

    private void getMAC() {

        mAccountController.getMac(this, phone, "2", new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {

                    Intent intent_pwd = new Intent(ValidateActivityV33.this, SmsActivityV33.class);
                    intent_pwd.putExtra("name", mAccountName);
                    intent_pwd.putExtra("idNumber", mAccountNO);
                    intent_pwd.putExtra("phone", phone);
//                    intent_pwd.putExtra("MAC", result);

                    intent_pwd.putExtra("isFirst", true);
                    startActivity(intent_pwd);
                    //保存输入的手机号信息


                } else {
                    Toast.makeText(ValidateActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    /**
     * 登录页面逻辑
     *
     * @param isExist    账户是否存在 true 表示存在
     * @param hasAccount isExist为false此字段才有意义，此字段1表示老版本已注册未认证
     * @param returnCode 返回码
     * @param mMobile    账号
     * @param mPWD       密码
     */
    private void isLoginBuissiness(boolean isExist, int hasAccount, String returnCode, String mMobile, String mPWD) {
        if (isExist) {
            showProgDialog("加载中...");
            facialRecognition(mAccountName, mAccountNO);
        } else {
            if (hasAccount == 1) {
//                isRegister=true;
//                showProgDialog("加载中...");
//                facialRecognition(mAccountName, mAccountNO);
                showAlertTitleDialog("账户不存在,暂时无法登录", mMobile);

            } else {
                showAlertTitleDialog("账户无法登录,请进行可信身份注册", mMobile);
            }

        }
    }

    /**
     * 注册页面逻辑
     *
     * @param isExist 账户是否存在 true 表示存在
     */
    private void isRegisterBuissiness(boolean isExist) {
        if (isExist) {
            Toast.makeText(ValidateActivityV33.this, "账户已存在，请登录", Toast.LENGTH_SHORT).show();
        } else {
            showProgDialog("加载中...");
            facialRecognition(mAccountName, mAccountNO);
        }
    }


    /**
     * 注册接口逻辑
     *
     * @param mMobile 手机号
     * @param mPWD    密码
     */
    public void registerAccount(String mMobile, String mPWD) {
        mAccountController.registerAccount(ValidateActivityV33.this, mMobile, mAccountName, mAccountNO, mPWD, new ResponseCallback() {
            @Override
            public void responseCallback(String returnCode, String retMsg, String result) {
                if (returnCode.equals("0")) {
                    Toast.makeText(ValidateActivityV33.this, "注册成功", Toast.LENGTH_SHORT).show();
                    mAccountController.accountLogin(ValidateActivityV33.this, mMobile, mPWD, mAccountName, mAccountNO, new ResponseCallback() {
                        @Override
                        public void responseCallback(String returnCode, String retMsg, String result) {//注册后登陆失败直接回到登陆界面
//                                    Toast.makeText(ValidateActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ValidateActivityV33.this, LoginActivityV33.class));
                        }
                    });
                } else {
                    Toast.makeText(ValidateActivityV33.this, retMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showAlertTitleDialog(String message, String mMobile) {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(ValidateActivityV33.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        mAlertDialog.setTitle("提示");
        mAlertDialog.setMessage(message);
        mAlertDialog.setPositiveButton("去注册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ValidateActivityV33.this, RegisterActivityV33.class);
                intent.putExtra("isnotexit_account", mMobile);
                startActivity(intent);
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog.show();
    }


}
