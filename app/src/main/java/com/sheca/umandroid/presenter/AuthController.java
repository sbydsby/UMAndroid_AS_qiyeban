package com.sheca.umandroid.presenter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.custle.dyrz.DYRZResult;
import com.custle.dyrz.DYRZResultBean;
import com.custle.dyrz.DYRZSDK;
import com.custle.dyrz.config.DYErrMacro;
import com.sheca.umandroid.AuthChoiceActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.custle.dyrz.config.Constants.auth_type_alipay;
import static com.custle.dyrz.config.Constants.auth_type_bank;
import static com.custle.dyrz.config.Constants.auth_type_dyrz;
import static com.custle.dyrz.config.Constants.auth_type_face;
import static com.custle.dyrz.config.Constants.auth_type_face_alipay;
import static com.custle.dyrz.config.Constants.auth_type_mobile;
import static com.excelsecu.util.LibUtil.getApplicationContext;

public class AuthController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private String transaction_id = "app" + UUID.randomUUID().toString();


    private void authByFace(final Activity act, final boolean needPay) {

        DYRZSDK.getInstance(CommonConst.appID, CommonConst.priKey, DYRZSDK.BUILD_RELEASE).faceAuth(act, AccountHelper.getRealName(act), AccountHelper.getIdcardno(act), transaction_id, new DYRZResult() {
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
                    if ((Build.VERSION.SDK_INT > 22) && (ContextCompat.checkSelfPermission(act,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.CAMERA}, 0);
                        return;
                    }
                }


                if (bean.getCode().equals("0")) {

                    //人脸验证成功

//                    String authMsg = AccountHelper.getUsername(getApplicationContext());
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));

                    AccountDao mAccountDao = new AccountDao(act.getApplicationContext());
                    Account acount = mAccountDao.getLoginAccount();
                    acount.setStatus(4);
                    acount.setIdentityName(AccountHelper.getRealName(act));
                    acount.setIdentityCode(AccountHelper.getIdcardno(act));

                    mAccountDao.update(acount);

                    if (needPay) {
                        Intent intent = new Intent();
                        intent.setClass(act, com.sheca.umandroid.PayActivity.class);
                        intent.putExtra("loginAccount", AccountHelper.getRealName(act));
                        intent.putExtra("loginId", AccountHelper.getIdcardno(act));
                        act.startActivity(intent);

                    } else {
//                            setAccountVal(personName, personId);
                    }
                } else {

                    Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
                }
//                closeProgDlg();
            }
        });


//        final UniTrust uniTrust = new UniTrust(act, false);
//        uniTrust.setFaceAuth(true);
//        uniTrust.setFaceAuthActionNumber(3);
//
//        String result = null;
//        Future<String> future =threadPool.submit(
//                        new Callable<String>() {
//                            public String call() throws Exception {
//
//                                String info = ParamGen.getFaceAuth(getApplicationContext());
//
//                                String responseStr = null;
//                                try {
//                                    responseStr = uniTrust.FaceAuth(info);
//                                } catch (Exception e) {
//                                    act.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//
//                                        }
//                                    });
//                                    e.printStackTrace();
//                                }
//
//                                return responseStr;
//                            }
//                        }
//                );
//
//        try {
//            result = future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            act.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//
//                }
//            });
//        }
//        final String finalResult = result;
//        act.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("unitrust", finalResult);
//                APPResponse response = new APPResponse(finalResult);
//                final int retCode = response.getReturnCode();
//
//                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {//不成功
//                    Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();
//
//                } else {
//
//                    //人脸验证成功
//                    JSONObject jbRet = response.getResult();
//                    String personName = jbRet.getString("personName");
//                    String personId = jbRet.getString("personID");
//                    AccountHelper.setRealName(personName);
//                    AccountHelper.setIdcardno(personId);
//                    String authMsg = AccountHelper.getUsername(getApplicationContext());
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
//                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));
//
//                    AccountDao mAccountDao = new AccountDao(act.getApplicationContext());
//                    Account acount = mAccountDao.getLoginAccount();
//                    acount.setStatus(4);
//                    acount.setIdentityName(personName);
//                    acount.setIdentityCode(personId);
//
//                    mAccountDao.update(acount);
//
//                    if (needPay) {
//                        Intent intent = new Intent();
//                        intent.setClass(act, com.sheca.umandroid.PayActivity.class);
//                        intent.putExtra("loginAccount", AccountHelper.getRealName(act));
//                        intent.putExtra("loginId", AccountHelper.getIdcardno(act));
//                        act.startActivity(intent);
//
//                    } else {
////                            setAccountVal(personName, personId);
//                    }
//                }
//            }
//        });

    }

    public void faceAuth(final Activity act, final boolean needPay) {

        if (AccountHelper.hasAuth(act)) {
            authByFace(act, needPay);
        } else {
            LayoutInflater inflater = LayoutInflater.from(act);
            View fourView = inflater.inflate(R.layout.activity_auth_main, null);


            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            final AlertDialog dialog = builder.create();

            dialog.setView(fourView);
            dialog.show();  //注意：必须在window.setContentView之前show
            Window window = dialog.getWindow();


            window.setContentView(R.layout.activity_auth_main);
            EditText edtName = (EditText) window.findViewById(R.id.person_name);

            EditText edtId = (EditText) window.findViewById(R.id.person_id);

            ImageView login_btn_next = (ImageView) window.findViewById(R.id.login_btn_next);

            login_btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    authByFace(act, needPay);
                }
            });


        }
    }


}
