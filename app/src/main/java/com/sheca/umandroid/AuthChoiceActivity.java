package com.sheca.umandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.custle.dyrz.DYRZResult;
import com.custle.dyrz.DYRZResultBean;
import com.custle.dyrz.DYRZSDK;
import com.custle.dyrz.config.DYErrMacro;
import com.custle.dyrz.utils.T;
import com.junyufr.szt.util.CustomProgressDialog;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import java.util.UUID;

import static com.custle.dyrz.config.Constants.auth_type_alipay;
import static com.custle.dyrz.config.Constants.auth_type_bank;
import static com.custle.dyrz.config.Constants.auth_type_dyrz;
import static com.custle.dyrz.config.Constants.auth_type_face;
import static com.custle.dyrz.config.Constants.auth_type_face_alipay;
import static com.custle.dyrz.config.Constants.auth_type_mobile;

public class AuthChoiceActivity extends BaseActivity2 implements DYRZResult, ActivityCompat.OnRequestPermissionsResultCallback {
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


    boolean isFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.title_activity_auth2, R.layout.activity_auth_choice);

        uniTrust = new UniTrust(this, false);
//        uniTrust.setFaceAuth(true);
        uniTrust.setDYFaceAuth(true);

        mAccountDao = new AccountDao(AuthChoiceActivity.this);
        btnTakePhoto = (Button) findViewById(R.id.btn_takephoto);  //拍摄照片
        btnInput = (Button) findViewById(R.id.btn_input);          //手动输入

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("needPay") != null) {
                needPay = true;

//                faceAuth(AUTH_TYPE.AUTH_TYPE_INPUT);
                faceAuthNew(AUTH_TYPE.AUTH_TYPE_INPUT, "", "");
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

            if (extras.getString("isPayAndAuth") != null) {
                needPay = true;
            }
        }

        btnTakePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AUTH_TYPE auth_photo = AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO;
//                faceAuthNew(auth_photo);
                faceAuth(auth_photo);
            }
        });

        btnInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final AUTH_TYPE auth_input = AUTH_TYPE.AUTH_TYPE_INPUT;
//                faceAuth(auth_input);
                faceAuthNew(auth_input, "", "");
//                authSuccess();

            }
        });
    }

    private void setAccountVal(final String name, final String id) {
        String setRes = uniTrust.SetAccountCertification(ParamGen.getAccountVerification(
                AccountHelper.getToken(getApplicationContext()),
                name, id, "4"));

        Log.d("unitrust", setRes);

        final APPResponse setResp = new APPResponse(setRes);
        final int retCode = setResp.getReturnCode();
        final String retMsg = setResp.getReturnMsg();

        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AuthChoiceActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AuthChoiceActivity.this, "设置实名认证状态成功", Toast.LENGTH_SHORT).show();
                    Account acount = mAccountDao.getLoginAccount();
                    acount.setStatus(4);
                    acount.setIdentityName(name);
                    acount.setIdentityCode(id);
                    mAccountDao.update(acount);

                    finish();
                }
            });

        }

    }

    private void faceAuth(final AUTH_TYPE authType) {

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
                if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO) {
                    uniTrust.setFaceAuthActionNumber(3);
                    uniTrust.setFaceAuth(true);
                    uniTrust.setOCRFlag(true);
                    info = ParamGen.getFaceAuthOCR();
                } else {
                    info = ParamGen.getFaceAuth(getApplicationContext());
                }

//                String result = uniTrust.IDAuth(info);
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

                    if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                faceAuthNew(authType, personName, personId);
                            }
                        });


                        return;
                    }


                    AccountHelper.setRealName(personName);
                    AccountHelper.setIdcardno(personId);
                    String authMsg = AccountHelper.getUsername(getApplicationContext());
                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_HAS_AUTH, authMsg);

                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));

                    Account acount = mAccountDao.getLoginAccount();
                    //acount.setStatus(4);
                    acount.setIdentityName(personName);
                    acount.setIdentityCode(personId);

                    mAccountDao.update(acount);
                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setInt(CommonConst.PARAM_STATUS, 2);
                    if (acount.getStatus() != 4) {
                        setAccountVal(personName, personId);
                    } else {
                        if (needPay) {
                            Intent intent = new Intent();
                            intent.setClass(AuthChoiceActivity.this, PayActivity.class);
                            intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
                            intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
                            startActivity(intent);
                            finish();
                        } else {
                            setAccountVal(personName, personId);
                        }
                    }


//


                }
            }
        }).start();

    }

    private String transaction_id = "app" + UUID.randomUUID().toString();


    String personName = "";
    String personId = "";

    private void faceAuthNew(final AUTH_TYPE authType, String name, String id) {

        if (AccountHelper.hasAuth(this) && !TextUtils.isEmpty(AccountHelper.getRealName(this)) && !TextUtils.isEmpty(AccountHelper.getIdcardno(this))) {
            showProgDlg(this);
            DYRZSDK.getInstance(CommonConst.appID, CommonConst.priKey, DYRZSDK.BUILD_RELEASE).faceAuth(AuthChoiceActivity.this, AccountHelper.getRealName(this), AccountHelper.getIdcardno(this), transaction_id, this);
        } else {

            LayoutInflater inflater = LayoutInflater.from(AuthChoiceActivity.this);
            View fourView = inflater.inflate(R.layout.activity_auth_main, null);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder.create();

            dialog.setView(fourView);
            dialog.show();  //注意：必须在window.setContentView之前show
            Window window = dialog.getWindow();
            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
            android.view.WindowManager.LayoutParams p = window.getAttributes();  //获取对话框当前的参数值
            p.height = (int) (d.getHeight() * 1);   //高度设置为屏幕的0.3
            p.width = (int) (d.getWidth() * 1);    //宽度设置为屏幕的0.5
            window.setAttributes(p);     //设置生效

            window.setContentView(R.layout.activity_auth_main);
            EditText edtName = (EditText) window.findViewById(R.id.person_name);
            if (!TextUtils.isEmpty(name)) {
                edtName.setText(name);
            }
            EditText edtId = (EditText) window.findViewById(R.id.person_id);
            if (!TextUtils.isEmpty(id)) {
                edtId.setText(id);
            }
            TextView login_btn_next = (TextView) window.findViewById(R.id.login_btn_next);

            login_btn_next.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (check(edtName.getText().toString().trim(), edtId.getText().toString().trim())) {
                        personName = edtName.getText().toString().trim();
                        personId = edtId.getText().toString().trim();
                        showProgDlg(AuthChoiceActivity.this);
                        DYRZSDK.getInstance(CommonConst.appID, CommonConst.priKey, DYRZSDK.BUILD_RELEASE).faceAuth(AuthChoiceActivity.this, edtName.getText().toString().trim(), edtId.getText().toString().trim(), transaction_id, AuthChoiceActivity.this);
                    }
                }
            });


        }


        //
//
//
//
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                uniTrust.setFaceAuthBGColor("#F7D05B");   //设置界面背景色
//                uniTrust.setFaceAuthTextColor("#000000");  //设置界面标题色
//
//                String info;
//                if (authType == AUTH_TYPE.AUTH_TYPE_TAKE_PHOTO) {
//                    uniTrust.setFaceAuthActionNumber(3);
//                    uniTrust.setFaceAuth(true);
//                    info = ParamGen.getFaceAuthOCR();
//                } else {
//                    info = ParamGen.getFaceAuth(getApplicationContext());
//                }
//
//
//                String result = uniTrust.FaceAuth(info);
//
//                Log.d("unitrust", result);
//                APPResponse response = new APPResponse(result);
//                final int retCode = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//
//
//                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(AuthChoiceActivity.this, getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    finish();
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(AuthChoiceActivity.this, retMsg, Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//                    //人脸验证成功
//                    JSONObject jbRet = response.getResult();
//                    String personName = jbRet.getString("personName");
//                    String personId = jbRet.getString("personID");
//                    AccountHelper.setRealName(personName);
//                    AccountHelper.setIdcardno(personId);
//                    String authMsg = AccountHelper.getUsername(getApplicationContext());
//                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
//
//                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
//                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));
//
//                    Account acount = mAccountDao.getLoginAccount();
//                    //acount.setStatus(4);
//                    acount.setIdentityName(personName);
//                    acount.setIdentityCode(personId);
//
//                    mAccountDao.update(acount);
//                    SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setInt(CommonConst.PARAM_STATUS, 2);
//                    if (acount.getStatus() != 4) {
//                        setAccountVal(personName, personId);
//                    } else {
//                        if (needPay) {
//                            Intent intent = new Intent();
//                            intent.setClass(AuthChoiceActivity.this, com.sheca.umandroid.PayActivity.class);
//                            intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
//                            intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            setAccountVal(personName, personId);
//                        }
//                    }
//
//
////
//
//
//                }
//            }
//        }).start();

    }

    public boolean check(String name, String id) {

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "身份证号不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!CommUtil.isPersonNO(id)) {
            Toast.makeText(this, "身份证号格式有误", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    private void showProgDlg(final Context context) {
        progDialog = new ProgressDialog(context);
//        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }


    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }

    private ProgressDialog progDialog = null;


    @Override
    public void dyrzResultCallBack(DYRZResultBean bean) {
        String result = "code: " + bean.getCode() + "\r\n";
        result += "msg: " + bean.getMsg() + "\r\n";
        String authType;
        switch (bean.getAuthType()) {
            case auth_type_dyrz:
                authType = "多源认证";
                break;
            case auth_type_mobile:
                authType = "手机号认证";
                break;
            case auth_type_bank:
                authType = "银行卡认证";
                break;
            case auth_type_face:
                authType = "人脸识别认证";
                break;
            case auth_type_alipay:
                authType = "支付宝认证";
                break;
            case auth_type_face_alipay:
                authType = "人脸支付宝认证";
                break;
            default:
                authType = "其它";
                break;
        }
        result += "type: " + authType + "\r\n";
        result += "token: " + bean.getToken() + "\r\n";
        result += "data: " + bean.getData();
//        mTV.setText(result);
//
//        if (bean.getData() != null && bean.getData().length() > 0) {
//            byte[] bytes= Base64.decode(bean.getData(), Base64.NO_WRAP);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            mFaceIV.setImageBitmap(bitmap);
//            mFaceIV.setVisibility(View.VISIBLE);
//        }

        if (bean.getAuthType() == auth_type_face && bean.getCode().equals(DYErrMacro.camera_permission_err)) {
            if ((Build.VERSION.SDK_INT > 22) && (ContextCompat.checkSelfPermission(AuthChoiceActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(AuthChoiceActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }
        }


        if (bean.getCode().equals("0")) {

            Account acount = mAccountDao.getLoginAccount();
            //acount.setStatus(4);

            if (AccountHelper.hasAuth(this)) {
                personName=AccountHelper.getRealName(this);
                personId=AccountHelper.getIdcardno(this);
            }


            acount.setIdentityName(personName);
            acount.setIdentityCode(personId);

            mAccountDao.update(acount);
            AccountHelper.setRealName(personName);
            AccountHelper.setIdcardno(personId);

            SharePreferenceUtil.getInstance(AuthChoiceActivity.this).setInt(CommonConst.PARAM_STATUS, 2);
            if (acount.getStatus() != 4) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setAccountVal(personName, personId);
                    }
                }).start();

            } else {
                if (needPay) {
                    Intent intent = new Intent();
                    intent.setClass(AuthChoiceActivity.this, com.sheca.umandroid.PayActivity.class);
                    intent.putExtra("loginAccount", AccountHelper.getRealName(AuthChoiceActivity.this));
                    intent.putExtra("loginId", AccountHelper.getIdcardno(AuthChoiceActivity.this));
                    startActivity(intent);
                    finish();
                } else {
                    setAccountVal(personName,personId);
                }
            }

//            if (mProgressDialog != null) {
//                mProgressDialog.dismissDialog();
//            }
            closeProgDlg();
        } else {
            closeProgDlg();

            Toast.makeText(getApplicationContext(), "人脸验证失败", Toast.LENGTH_LONG).show();
            finish();


        }


    }


    @SuppressLint("Override")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // 回调中加载下一个Activity
            T.showShort(AuthChoiceActivity.this, "权限关闭");
        } else {
            T.showShort(AuthChoiceActivity.this, "权限开启，请再次认证");
        }
    }
}
