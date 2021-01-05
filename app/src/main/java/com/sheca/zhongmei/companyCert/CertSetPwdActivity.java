package com.sheca.zhongmei.companyCert;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.sheca.zhongmei.MainActivity;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.presenter.CertController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author xuchangqing
 * @time 2019/8/5 17:48
 * @descript 申请证书--支付
 */
public class CertSetPwdActivity extends BaseActivity {


    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.et_input_pwd)
    EditText edPwd;
    @BindView(R.id.et_input_pwd_again)
    EditText edAgain;
    @BindView(R.id.tv_cert_pwd_ok)
    TextView mTvOk;

    int time = 36;

    boolean typeNo;//true:授权人申请 false 法人申请

    String requestNumber = "";

    int certId;

    int orgType;

    String RSA_TYPE;

    String SM2_TYPE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cert_set_pwd);
        ButterKnife.bind(this);
        initView();
//        typeNo = getIntent().getBooleanExtra("typeNo", false);
//
//
//        orgType = getIntent().getIntExtra("orgType", 1);
//
//
//        RSA_TYPE = getOrgStr(orgType, true);
//        SM2_TYPE = getOrgStr(orgType, false);

        mTvTitle.setText("设置保护口令");

    }

    //    int otherId;
//    String otherHash;
//
//    private void checkFalse() {
////        CertDao certDao = new CertDao(this);
////        List<Cert> list = certDao.getCerListByOrgName(getIntent().getStringExtra("name"));
////        if (list.size() == 2) {
////            if (list.get(0).getCerttype().equals(list.get(1).getCerttype())) {//有两张一样的，都删
////                otherId = list.get(1).getId();
////                otherHash = list.get(1).getCerthash();
////                showDg();
////                deleteCert(list.get(0).getId(), list.get(0).getCerthash(), 3);
////
////            } else {
////                Toast.makeText(getApplicationContext(), "该公司已经申请证书", Toast.LENGTH_LONG).show();
////                return;
////            }
////
////        } else if (list.size() == 1) {//只有一张删除
////            showDg();
////            deleteCert(list.get(0).getId(), list.get(0).getCerthash(), 2);
////
////        } else {
////            showDg();
//            applyCert(false, typeNo ? 3 : 1);//1为法人申请 3为未授权人申请
////        }
//    }
//
//    private void deleteCert(int certId, String pwd, int type) {
//
//        new Thread() {
//            public void run() {
//                CertController certController = new CertController();
//                String resStr = certController.deleteCert(CertSetPwdActivity.this,
//                        certId + "", pwd);
//
//                try {
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            APPResponse re = new APPResponse(resStr);
//                            if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                                //设置成功
//                                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                                AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, type);
//
//
//                            } else if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {
//
//
//                                dismissDg();
//
//                                AccountHelper.reLogin(CertSetPwdActivity.this);
//
//
//                            } else {
//                                //设置失败
////                        String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
////                        AccountHelper.getCertList(CertDeleteActivity.this, mAccountName);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        dismissDg();
//                                        Toast.makeText(CertSetPwdActivity.this, "申请失败：删除多余证书失败", Toast.LENGTH_LONG).show();
//
//                                    }
//                                });
//                            }
//                        }
//                    });
//
//                } catch (Exception e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDg();
//                            Toast.makeText(CertSetPwdActivity.this, "申请失败：删除多余证书失败", Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                }
//
//            }
//        }.start();
//    }
//
//
////    private void getAcountCertsByCommonName(String phone, String org) {
////
////        ICallback callback = new ICallback() {
////            @Override
////            public void onCallback(Object data) {
////
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        List<Cert> response = (List<Cert>) data;
////                        if (response == null || response.size() == 0) {//没记录，可以申请证书
////
////                            applyCert(false, typeNo ? 3 : 1);//1为法人申请 3为未授权人申请
////                        } else if (response.size() == 1) {//有一张，删除该证书后申请
////                            Cert cert = ((List<Cert>) data).get(0);
////
////                            deleteCert(cert.getId(), cert.getCerthash());
////
//////                            Toast.makeText(CertSetPwdActivity.this, "该单位已经申请证书", Toast.LENGTH_LONG).show();
////                        } else {//兩張類型相同則要全部刪除
////
////                            Toast.makeText(CertSetPwdActivity.this, "该单位已经申请证书", Toast.LENGTH_LONG).show();
////                        }
////                    }
////                });
////
////
////            }
////        };
////        CertController certController = new CertController();
////        certController.getAcountCertsByCommonName(CertSetPwdActivity.this, phone, org, callback);
////
////    }
////
////    private void deleteCert(int certId, String pwd) {
////
////        new Thread() {
////            public void run() {
////                CertController certController = new CertController();
////                String resStr = certController.deleteCert(CertSetPwdActivity.this,
////                        certId + "", pwd);
////
////                APPResponse re = new APPResponse(resStr);
////                if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
////                    //设置成功
////                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
////                    AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, 0);
////
////
////                } else if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {
////
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            AccountHelper.reLogin(CertSetPwdActivity.this);
////                        }
////                    });
////
////                } else {
////                    //设置失败
//////                        String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//////                        AccountHelper.getCertList(CertDeleteActivity.this, mAccountName);
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            Toast.makeText(CertSetPwdActivity.this, "删除多余证书失败", Toast.LENGTH_LONG).show();
////                            String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
////                            AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, false);
////
////                        }
////                    });
////                }
////            }
////        }.start();
////    }
//
//
    private void initView() {
        mTvTitle.setText("设置保护口令");

    }

    //
    @OnClick({R.id.iv_back, R.id.tv_cert_pwd_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_cert_pwd_ok:
                if (check()) {
//                    changeCertPassword();
//                    applyCert(false, typeNo ? 3 : 1);//1为法人申请 3为未授权人申请


                    if (check()) {
                        Intent intent = getIntent().putExtra("pwd", edPwd.getText().toString().trim());
                        setResult(RESULT_OK, intent);
                        finish();

                    }

//                    getAcountCertsByCommonName(AccountHelper.getUsername(CertSetPwdActivity.this), getIntent().getStringExtra("name"));
                }
                break;

        }
    }

    //
    public boolean check() {
        if (edPwd.getText().toString().trim().length() < 8 || edPwd.getText().toString().trim().length() > 16) {
            Toast.makeText(getApplicationContext(), "密码必须为8—16位数字或字母", Toast.LENGTH_LONG).show();

            return false;
        } else if (!edPwd.getText().toString().trim().equals(edAgain.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "两次密码输入不一致", Toast.LENGTH_LONG).show();

            return false;
        }

        return true;
    }

    //
//
    @Override
    public void gotoNextActivity(int type) {
        super.gotoNextActivity(type);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0) {//删除多余证书后回调
                    applyCert(false, typeNo ? 3 : 1);
                } else if (type == 1) {//申请rsa证书后回调
                    CertDao certDao = new CertDao(CertSetPwdActivity.this);
                    com.sheca.zhongmei.model.Cert cert = certDao.getCertByID(certId);
                    requestNumber = cert.getEnvsn();

                    applyCert(true, typeNo ? 3 : 1);
                } else if (type == 2) {
                    applyCert(false, typeNo ? 3 : 1);//1为法人申请 3为未授权人申请
                } else if (type == 3) {//列表里有两张相同类型的证书，删掉其中第一张后的回调
//                    deleteCert(otherId, otherHash, 4);
                } else if (type == 4) {//列表里有两张相同类型的证书，删掉其中第二张后的回调
                    applyCert(false, typeNo ? 3 : 1);//1为法人申请 3为未授权人申请
                } else if (type == 5) {//证书申请成功
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(CertSetPwdActivity.this, MainActivity.class);
                            startActivity(intent);

//                            Intent intent = new Intent(CertSetPwdActivity.this, CertApplySuccess.class);
//                            startActivity(intent);
//                            finish();
//                            dismissDg();
                        }
                    }, 200);

                }
            }
        });
    }

    //
    //申请证书
    private void applyCert(boolean isSM2, int certLevel) {

        ICallback callback = new ICallback() {
            @Override
            public void onCallback(Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        try {
                            APPResponse response = new APPResponse((String) data);

                            if (response.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                                if (isSM2 == false) {

                                    certId = response.getResult().optInt("certID");


//                                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//
//                                    AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, 1);

                                    gotoNextActivity(1);

                                } else {

//                                        CommUtil.disMissFloat(CertSetPwdActivity.this);

//                                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                                    AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, 5);

                                    gotoNextActivity(5);
                                    Toast.makeText(CertSetPwdActivity.this, "证书申请成功", Toast.LENGTH_LONG).show();
                                }

//                    JSONObject jbRet = response.getResult();
//                    String certId = jbRet.getString("certID");
//
//                    Intent intent = new Intent(CertSetPwdActivity.this, CertSetPwdActivity.class);
//                    intent.putExtra("certID", certId);
//                    startActivity(intent);

                            } else {

//                                dismissDg();
                                Toast.makeText(CertSetPwdActivity.this, response.getReturnMsg(), Toast.LENGTH_LONG).show();

                                //
//                            String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                            AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, false);

                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    dismissDg();
                                    Toast.makeText(CertSetPwdActivity.this, "申请证书失败", Toast.LENGTH_LONG).show();

                                }
                            });
                        }


                    }
                });

            }
        };
        CertController certController = new CertController();


        certController.applyNewCert(CertSetPwdActivity.this, getIntent().getStringExtra("name"),
                getIntent().getStringExtra("no"), isSM2 ? SM2_TYPE : RSA_TYPE, edPwd.getText().toString().trim(), time, certLevel, isSM2 ? requestNumber : "", callback);

    }
//
////    private void changeCertPassword() {
////        //设置别名
////        int certId = getIntent().getIntExtra("certID", 0);
////
////        Log.e("id", getIntent().getIntExtra("certID", 0) + "");
////
////
////        String newPwd = edPwd.getText().toString().trim();
////
////
////        new Thread() {
////            public void run() {
////                CertController certController = new CertController();
////                String resStr = certController.changeCertPwd(CertSetPwdActivity.this,
////                        certId + "", oldPwd, newPwd);
////
////                APPResponse re = new APPResponse(resStr);
////                if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
////                    //设置成功
////                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
////                    AccountHelper.getCertList(CertSetPwdActivity.this, mAccountName, false);
//////                    runOnUiThread(new Runnable() {
//////                        @Override
//////                        public void run() {
//////                            Toast.makeText(CertSetPwdActivity.this, "修改成功", Toast.LENGTH_LONG).show();
//////                        }
//////                    });
////
////                } else if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {
////
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            AccountHelper.reLogin(CertSetPwdActivity.this);
////                        }
////                    });
////
////                } else {
////                    //设置失败
//////                        String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//////                        AccountHelper.getCertList(CertChangePwd.this, mAccountName);
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            Toast.makeText(CertSetPwdActivity.this, "设置证书密码失败失败", Toast.LENGTH_LONG).show();
////                        }
////                    });
////                }
////            }
////        }.start();
////    }


}
