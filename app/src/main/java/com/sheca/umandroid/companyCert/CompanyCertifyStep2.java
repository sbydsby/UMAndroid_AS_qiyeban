package com.sheca.umandroid.companyCert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.OrgInfoDao;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.OrgInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author xuchangqing
 * @time 2019/8/5 10:41
 * @descript 单位认证--单位认证
 */
public class CompanyCertifyStep2 extends BaseActivity {

    //    @BindView(R.id.timeline)
//    TimeLineView mTimeline;
//    @BindView(R.id.view_toolbar)
//    View mViewToolbar;
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    //    @BindView(R.id.guide_line)
//    View mGuideLine;
//    @BindView(R.id.tv_cert_certify_title1)
//    TextView mTvCertCertifyTitle1;
//    @BindView(R.id.tv_cert_certify_title2)
//    TextView mTvCertCertifyTitle2;
//
//    @BindView(R.id.tv_cert_certify_title3)
//    TextView mTvCertCertifyTitle3;
//    @BindView(R.id.et_input_name)
//    EditText mEtInputName;
    @BindView(R.id.et_input_number)
    EditText mEtInputNumber;
    //    @BindView(R.id.guide_line2)
//    View mGuideLine2;
//    @BindView(R.id.tv_cert_legal_title)
//    TextView mTvCertLegalTitle;
//    @BindView(R.id.tv_cert_legal_name)
//    TextView mTvCertLegalName;
//    @BindView(R.id.et_input_legal_name)
//    EditText mEtInputLegalName;
//    @BindView(R.id.tv_cert_legal_id)
//    TextView mTvCertLegalId;
//    @BindView(R.id.et_input_legal_id)
//    EditText mEtInputLegalId;
    @BindView(R.id.tv_cert_pwd_ok)
    TextView mTvCertPwdOk;
//    private boolean isPay;

    int kind;//0 法人  1 管理员 2 未授权

    String psdhash = "11111111";
    int time = 36;//默认三年


    String orgName;
    String orgNo;
    String lrName;
    String lrNo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cert_company_certify_step2);
        ButterKnife.bind(this);
        initView();

    }


    private void initView() {
        mTvTitle.setText("申请单位证书");
//        mTimeline.setStep3Constraint();
//        //isPay标志位判断是对公打款认证还是法人认证,true表示对公打款
//        isPay = getIntent().getBooleanExtra("isPay", false);
//        if (isPay) {
//            mTimeline.setStep2Name(getString(R.string.pay_public_title));
//        } else {
//            mTimeline.setStep2Name(getString(R.string.cert_manage_certify_step3));
//        }

    }

    @OnClick({R.id.iv_back, R.id.tv_cert_pwd_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_cert_pwd_ok:
                if (check()) {
//                    if (!DoubleUtils.isFastDoubleClick()) {
                       GetOrgInfo(CompanyCertifyStep2.this, mEtInputNumber.getText().toString().trim());

//                    }

                    //                    if (!isPay) {
//                        //法人认证界面
//                        Intent intent_ok = new Intent(CompanyCertifyStep2.this, CompanyCertifyStep3.class);
//                        intent_ok.putExtra("name", mEtInputName.getText().toString().trim());
//                        intent_ok.putExtra("number", mEtInputNumber.getText().toString().trim());
//                        intent_ok.putExtra("legalName", mEtInputLegalName.getText().toString().trim());
//                        intent_ok.putExtra("legalId", mEtInputLegalId.getText().toString().trim());
//                        startActivity(intent_ok);
//                    } else {
//                        //对公打款界面
//                        Intent intent_next = new Intent(CompanyCertifyStep2.this, CompanyCertifyPublicPayStep3.class);
//                        startActivity(intent_next);
//                    }
                }
                break;
        }
    }

    int orgType;//单位类型（91：企业；12：事业单位；51：社会团体；11：机关法人）

    private boolean check() {
        boolean success = true;
//        if (mEtInputName.getText().toString().trim().length() == 0) {
//            success = false;
//            Toast.makeText(getApplicationContext(), "请输入公司名称", Toast.LENGTH_LONG).show();
//        } else
        if (mEtInputNumber.getText().toString().trim().length() == 0) {
            success = false;
            Toast.makeText(getApplicationContext(), "请输入统一信用代码", Toast.LENGTH_LONG).show();
        } else {

            if (mEtInputNumber.getText().toString().trim().length() >= 2) {
                String type = mEtInputNumber.getText().toString().trim().substring(0, 2);

                if (type.equals("91")) {
                    orgType = 1;
                } else if (type.equals("12")) {
                    orgType = 2;
                } else if (type.equals("51")) {
                    orgType = 3;
                } else if (type.equals("11")) {
                    orgType = 4;
                } else {
                    orgType = 0;
                }

                if (orgType == 0) {

                    success = false;
                    Toast.makeText(getApplicationContext(), "暂不支持该类型的单位申请证书", Toast.LENGTH_LONG).show();
                }

            }


        }
//        else if (mEtInputLegalName.getText().toString().trim().length() == 0) {
//            success = false;
//            Toast.makeText(this, "请输入法人姓名", Toast.LENGTH_LONG).show();
//        } else if (mEtInputLegalId.getText().toString().trim().length() == 0) {
//            success = false;
//            Toast.makeText(this, "请输入身份证号", Toast.LENGTH_LONG).show();
//        }

        return success;
    }

    int otherId;
    String otherHash;
    boolean needHide = false;//是否需要隐藏


//    //获取单位信息
    public void GetOrgInfo(final Activity act, String oNo) {
//        OrgInfoDao orgInfoDao = new OrgInfoDao(act);
//        OrgInfo orgInfo = orgInfoDao.getOrgInfoByPaperNo(oNo);//根据统一信用代码查单位信息
//        if (null != orgInfo) {
//
//            String oName = orgInfo.getCommonName();//根据单位信息查单位名称
//
//            CertDao certDao = new CertDao(act);
//            List<Cert> list = certDao.getCerListByOrgName(oName);
//            if (list.size() == 2) {
//                if (list.get(0).getCerttype().equals(list.get(1).getCerttype())) {//有两张一样的，都删
////                otherId = list.get(1).getId();
////                otherHash = list.get(1).getCerthash();
////
////                deleteCert(list.get(0).getId(), list.get(0).getCerthash(), 3);
//
//                } else {
//                    Toast.makeText(getApplicationContext(), "该公司已经申请证书", Toast.LENGTH_LONG).show();
//                    return;
//                }
//
//
//            }
//        }


        final UniTrust uniTrust = new UniTrust(act, false);


//        orgName = oName;
        orgNo = oNo.toUpperCase();


        new MyAsycnTaks() {


            @Override
            public void preTask() {
                //

            }

            @Override
            public void doinBack() {

                String params = ParamGen.GetOrgInfo(act.getApplicationContext(), orgNo);


                try {
                    String responseStr = uniTrust.GetOrgInfo(params);

                    //
                    APPResponse response = new APPResponse(responseStr);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


//
//
                    needHide = false;
                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

//                        {"returnCode":0,"returnMsg":"单位信息认证成功","result":{"LRPaperType":1,"LRPaperNO":"360430197609251218","LRName":"汪结培","LRMobile":"18817625768"}}

                        String LRPaperNO = response.getResult().optString("LRPaperNO");
                        String LRName = response.getResult().optString("LRName");
                        String LRMobile = response.getResult().optString("LRMobile");
                        orgName = response.getResult().optString("orgName");


                        if (null != LRName && !"".equals(LRName) && !"null".equals(LRName)) {

                            lrName = LRName;
                        } else {
                            lrName = "暂无法人姓名";
                            needHide = true;
                        }

                        if (null != LRPaperNO && !"".equals(LRPaperNO) && !"null".equals(LRPaperNO)) {

                            lrNo = LRPaperNO;
                        } else {
                            lrNo = "暂无法人身份证";
                            needHide = true;
                        }

//
//                        String phoneNO = response.getResult().optString("phoneNO");


//
//                        if (AccountHelper.getIdcardno(CompanyCertifyStep2.this).toUpperCase().equals(LRPaperNO.toUpperCase()) && AccountHelper.getRealName(CompanyCertifyStep2.this).equals(LRName)) {//是法人，走保存单位，申请证书
//                            Log.e("身份证", AccountHelper.getIdcardno(CompanyCertifyStep2.this));

                            AddOrg(orgName, orgNo, LRName, AccountHelper.getUsername(CompanyCertifyStep2.this));

//                        } else {//判断是否授权人
//
//                            checkDownloadList();
//
//
//                        }


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                AccountHelper.reLogin(context);
//                                dismissDg();
                                Toast.makeText(getApplicationContext(), retMsg, Toast.LENGTH_LONG).show();
//                                AccountHelper.getCertList(CompanyCertifyStep2.this, "", false);

                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            dismissDg();
                            Toast.makeText(getApplicationContext(), "单位认证失败", Toast.LENGTH_LONG).show();
//                            AccountHelper.getCertList(CompanyCertifyStep2.this, "", false);

                        }
                    });
                }

//                Log.d("unitrust", responseStr);


            }

            @Override
            public void postTask() {

            }
        }.execute();

    }
//
//
//    //验证单位
//    public void verifyOrg(final Activity act, String oName, String oNo) {
//        CertDao certDao = new CertDao(act);
//        List<Cert> list = certDao.getCerListByOrgName(oName);
//        if (list.size() == 2) {
//            if (list.get(0).getCerttype().equals(list.get(1).getCerttype())) {//有两张一样的，都删
////                otherId = list.get(1).getId();
////                otherHash = list.get(1).getCerthash();
////
////                deleteCert(list.get(0).getId(), list.get(0).getCerthash(), 3);
//
//            } else {
//                Toast.makeText(getApplicationContext(), "该公司已经申请证书", Toast.LENGTH_LONG).show();
//                return;
//            }
//
//
//        }
//
//
//        showDg();
//        final UniTrust uniTrust = new UniTrust(act, false);
//
//
//        orgName = oName;
//        orgNo = oNo.toUpperCase();
//
//
//        new MyAsycnTaks() {
//
//
//            @Override
//            public void preTask() {
//                //
//
//            }
//
//            @Override
//            public void doinBack() {
//
//                String params = ParamGen.verifyOrg(act.getApplicationContext(), orgName, orgNo);
//
//
//                try {
//                    String responseStr = uniTrust.VerifyOrg(params);
//
//                    //
//                    APPResponse response = new APPResponse(responseStr);
//                    final int retCode = response.getReturnCode();
//                    final String retMsg = response.getReturnMsg();
//
//
////
////
//                    needHide = false;
//                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//
////                        {"returnCode":0,"returnMsg":"单位信息认证成功","result":{"LRPaperType":1,"LRPaperNO":"360430197609251218","LRName":"汪结培","LRMobile":"18817625768"}}
//
//                        String LRPaperNO = response.getResult().optString("LRPaperNO");
//                        String LRName = response.getResult().optString("LRName");
//                        String LRMobile = response.getResult().optString("LRMobile");
//
//
//                        if (null != LRName && !"".equals(LRName) && !"null".equals(LRName)) {
//
//                            lrName = LRName;
//                        } else {
//                            lrName = "暂无法人姓名";
//                            needHide = true;
//                        }
//
//                        if (null != LRPaperNO && !"".equals(LRPaperNO) && !"null".equals(LRPaperNO)) {
//
//                            lrNo = LRPaperNO;
//                        } else {
//                            lrNo = "暂无法人身份证";
//                            needHide = true;
//                        }
//
////
////                        String phoneNO = response.getResult().optString("phoneNO");
//
//
////
//                        if (AccountHelper.getIdcardno(CompanyCertifyStep2.this).toUpperCase().equals(LRPaperNO.toUpperCase()) && AccountHelper.getRealName(CompanyCertifyStep2.this).equals(LRName)) {//是法人，走保存单位，申请证书
//                            Log.e("身份证", AccountHelper.getIdcardno(CompanyCertifyStep2.this));
//
//                            AddOrg(orgName, orgNo, LRName, AccountHelper.getUsername(CompanyCertifyStep2.this));
//
//                        } else {//判断是否授权人
//
//                            checkDownloadList();
//
//
//                        }
//
//
//                    } else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
////                                AccountHelper.reLogin(context);
//                                dismissDg();
//                                Toast.makeText(getApplicationContext(), retMsg, Toast.LENGTH_LONG).show();
////                                AccountHelper.getCertList(CompanyCertifyStep2.this, "", false);
//
//                            }
//                        });
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//
//                    act.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDg();
//                            Toast.makeText(getApplicationContext(), "单位认证失败", Toast.LENGTH_LONG).show();
////                            AccountHelper.getCertList(CompanyCertifyStep2.this, "", false);
//
//                        }
//                    });
//                }
//
////                Log.d("unitrust", responseStr);
//
//
//            }
//
//            @Override
//            public void postTask() {
//
//            }
//        }.execute();
//
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
////                            Intent intent = new Intent(CompanyCertifyStep2.this, CertSetPwdActivity.class);
////                            intent.putExtra("name", mEtInputName.getText().toString().trim());
////                            intent.putExtra("no", mEtInputNumber.getText().toString().trim());
////                            startActivity(intent);
////
////                        } else if (response.size() == 1) {//有一张，删除该证书后申请
////                            Cert cert = ((List<Cert>) data).get(0);
////
////                            deleteCert(cert.getId(), cert.getCerthash(), 0);
////
//////                            Toast.makeText(CompanyCertifyStep2.this, "该单位已经申请证书", Toast.LENGTH_LONG).show();
////                        } else {//兩張類型相同則要全部刪除
////
////                            Toast.makeText(CompanyCertifyStep2.this, "该单位已经申请证书", Toast.LENGTH_LONG).show();
////                        }
////                    }
////                });
////
////
////            }
////        };
////        CertController.getInstance().getAcountCertsByCommonName(CompanyCertifyStep2.this, phone, org, callback);
////
////    }
//
    @Override
    public void gotoNextActivity(int type) {//刪除證書成功後申請證書
        super.gotoNextActivity(type);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0) {
//                    dismissDg();
                    Intent intent = new Intent(CompanyCertifyStep2.this, CertSetPwdActivity.class);
                    intent.putExtra("name", orgName);
                    intent.putExtra("no", mEtInputNumber.getText().toString().trim());
                    intent.putExtra("orgType", orgType);
                    startActivity(intent);
                }
//                else if (type == 1) {//列表里有一张不配套证书，删掉后的回调
////                    verifyOrg(CompanyCertifyStep2.this, orgName, mEtInputNumber.getText().toString().trim());
//                    GetOrgInfo(CompanyCertifyStep2.this, mEtInputNumber.getText().toString().trim());
//
//                } else if (type == 3) {//列表里有两张相同类型的证书，删掉其中第一张后的回调
//                    deleteCert(otherId, otherHash, 4);
//                } else if (type == 4) {//列表里有两张相同类型的证书，删掉其中第二张后的回调
////                    verifyOrg(CompanyCertifyStep2.this, orgName, mEtInputNumber.getText().toString().trim());
//                    GetOrgInfo(CompanyCertifyStep2.this, mEtInputNumber.getText().toString().trim());
//
//                }
            }
        });
    }
//
//    private void deleteCert(int certId, String pwd, int type) {
//
//        new Thread() {
//            public void run() {
//                CertController certController = new CertController();
//                String resStr = certController.deleteCert(CompanyCertifyStep2.this,
//                        certId + "", pwd);
//
//                try {
//                    APPResponse re = new APPResponse(resStr);
//                    if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                        //设置成功
//                        String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                        AccountHelper.getCertList(CompanyCertifyStep2.this, mAccountName, type);
//
//
//                    } else if (re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || re.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dismissDg();
//                                AccountHelper.reLogin(CompanyCertifyStep2.this);
//                            }
//                        });
//
//                    } else {
//                        //设置失败
////                        String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
////                        AccountHelper.getCertList(CertDeleteActivity.this, mAccountName);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dismissDg();
//                                Toast.makeText(getApplicationContext(), "删除多余证书失败", Toast.LENGTH_LONG).show();
//                                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                                AccountHelper.getCertList(CompanyCertifyStep2.this, mAccountName, false);
//
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDg();
//                            Toast.makeText(getApplicationContext(), "删除多余证书失败", Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                }
//
//
//            }
//        }.start();
//    }
//
//
////    private String applyRSACertByFaceAuth() throws Exception {
////        String res = certController.applyCert(this,
////                getIntent().getStringExtra("compName"),
////                getIntent().getStringExtra("paperNo"),
////                CommonConst.CERT_TYPE_RSA_COMPANY,
////                strPsdHash, time);
////        return res;
////    }
////
////    private String applySM2CertByFaceAuth() throws Exception {
////        String res = certController.applyCert(this,
////                getIntent().getStringExtra("compName"),
////                getIntent().getStringExtra("paperNo"),
////                CommonConst.CERT_TYPE_SM2_COMPANY,
////                strPsdHash, time);
////        return res;
////    }
//
//
//    private void checkDownloadList() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String responseStr = GetCertApplyListEx();
//                    JSONObject jb = JSONObject.fromObject(responseStr);
//                    String resultStr = jb.getString(CommonConst.RETURN_CODE);
//
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDg();
//                            if (resultStr.equals("0")) {//有数据去下载
//
//
//                                JSONArray transitListArray = null;
//                                if (jb.containsKey(CommonConst.RETURN_RESULT)) {
//                                    JSONObject jbRet = jb.getJSONObject(com.sheca.umplus.util.CommonConst.RETURN_RESULT);
//                                    if (jbRet.has(com.sheca.umplus.util.CommonConst.PARAM_CERT_INFOS)) {
//                                        transitListArray = JSONArray.fromObject(jbRet.getString(com.sheca.umplus.util.CommonConst.PARAM_CERT_INFOS));
//                                    }
//                                }
//
//                                ArrayList<CertApplyInfoLite> rsaList = new ArrayList<>();
//                                ArrayList<CertApplyInfoLite> applications = new ArrayList<>();
//
//                                if (null != transitListArray && transitListArray.size() > 0) {
//                                    for (int i = 0; i < transitListArray.size(); i++) {
//                                        JSONObject jbRet = transitListArray.getJSONObject(i);
//                                        if (null == jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER) || "null".equals(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER)) || "".equals(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER)))
//                                            continue;
//
//                                        CertApplyInfoLite certApplyInfo = new CertApplyInfoLite();
//                                        certApplyInfo.setRequestNumber(jbRet.optString(CommonConst.PARAM_REQUEST_NUMBER));
//                                        certApplyInfo.setCommonName(jbRet.optString(CommonConst.PARAM_COMMON_NAME));
//                                        certApplyInfo.setApplyTime(jbRet.optString(CommonConst.PARAM_APPLY_NAME));
//                                        certApplyInfo.setStatus(Integer.parseInt(jbRet.optString(CommonConst.PARAM_STATUS)));
//                                        certApplyInfo.setBizSN(jbRet.optString(CommonConst.PARAM_ENCRYPT_CERTSN));
//                                        certApplyInfo.setCertType(jbRet.optString(CommonConst.PARAM_CERT_TYPE));
//                                        certApplyInfo.setSignAlg(1);/*Integer.parseInt(jbRet.getString(CommonConst.PARAM_SIGNALG_PLUS))*/
//                                        certApplyInfo.setPayStatus(1);/*Integer.parseInt(jbRet.getString(CommonConst.PARAM_PAY_STATUS))*/
//                                        certApplyInfo.setOrderUID(jbRet.optString(CommonConst.PARAM_ORDER_UID));
//                                        certApplyInfo.setPartnerReqNO(jbRet.optString(CommonConst.PARAM_PATNER_REQUEST_NUMBER));
//
//                                        if (jbRet.optString(CommonConst.PARAM_CERT_VALIDITY) != null && !jbRet.optString(CommonConst.PARAM_CERT_VALIDITY).equals("null")) {
//                                            certApplyInfo.setValidity(Integer.parseInt(jbRet.optString(CommonConst.PARAM_CERT_VALIDITY)));
//                                        }
//                                        if (jbRet.optString(CommonConst.PARAM_CERT_LEVEL) != null && !jbRet.optString(CommonConst.PARAM_CERT_LEVEL).equals("null")) {
//                                            certApplyInfo.setCertLevel(Integer.parseInt(jbRet.optString(CommonConst.PARAM_CERT_LEVEL)));
//                                        }
//
//                                        if (1 == certApplyInfo.getStatus() || 2 == certApplyInfo.getStatus()) { // 只有0的时候可以下载
//
//                                            if (certApplyInfo.getCommonName().equals(orgName)) {//只顯示當前公司證書
//                                                if (!certApplyInfo.getCertType().toUpperCase().contains("SM2")) {//RSA证书不显示，单独拿出来
////                                           Log.e("req+RSA",certApplyInfo.getRequestNumber()+" "+certApplyInfo.getPartnerReqNO());
////sm2证书的patnernumber=rsa的reqnumber
//                                                    rsaList.add(certApplyInfo);
//                                                } else {
////                                            Log.e("req+SM2",certApplyInfo.getRequestNumber()+" "+certApplyInfo.getPartnerReqNO());
//
//                                                    applications.add(certApplyInfo);
//                                                }
//                                            }
//                                        }
//
//                                    }
////去掉不匹配的sm2下载列表
//                                    for (int i = applications.size() - 1; i >= 0; i--) {
//                                        boolean hasDouble = false;
//                                        for (int j = rsaList.size() - 1; j >= 0; j--) {
//                                            if (applications.get(i).getPartnerReqNO().equals(rsaList.get(j).getRequestNumber()) || (applications.get(i).getRequestNumber().equals(rsaList.get(j).getPartnerReqNO()))) {
//                                                hasDouble = true;
//                                            }
//                                        }
//                                        if (hasDouble) {
//                                        } else {
//                                            applications.remove(i);
//                                        }
//                                    }
//
//
//                                    if (applications.size() == 0 || rsaList.size() == 0) {//无数据走多元
//                                        OrgInfoDao orgInfoDao = new OrgInfoDao(CompanyCertifyStep2.this);
//
////                                        if (orgInfoDao.getOrgInfoByCommonName(orgName) != null) {//公司已认证，直接申请证书
////
////                                            Intent intent = new Intent(CompanyCertifyStep2.this, CertSetPwdActivity.class);
////                                            intent.putExtra("name", mEtInputName.getText().toString().trim());
////                                            intent.putExtra("no", mEtInputNumber.getText().toString().trim());
////                                            intent.putExtra("typeNo",true);
////                                            intent.putExtra("orgType", orgType);
////                                            startActivity(intent);
////                                        } else {
//                                        Intent intent = new Intent(CompanyCertifyStep2.this, MultisourceAuthAcitivty.class);
//                                        intent.putExtra("orgName", orgName);
//                                        intent.putExtra("orgNo", orgNo);
//                                        intent.putExtra("lrName", lrName);
//                                        intent.putExtra("lrNo", lrNo);
//                                        intent.putExtra("orgType", orgType);
//                                        intent.putExtra("needHide", needHide);
//                                        startActivity(intent);
//
////                                        }
//
//
//                                    } else {//有数据去下载
//                                        Intent intent = new Intent(CompanyCertifyStep2.this, CertDownloadActivity.class);
//                                        intent.putExtra("orgName", orgName);
//                                        intent.putExtra("orgNo", orgNo);
//                                        intent.putExtra("lrName", lrName);
//                                        intent.putExtra("lrNo", lrNo);
//                                        intent.putExtra("orgType", orgType);
//                                        startActivity(intent);
//
//                                    }
//
//
//                                }
//
//
//                            } else {//无数据走多元
//
//                                OrgInfoDao orgInfoDao = new OrgInfoDao(CompanyCertifyStep2.this);
//
////                                if (orgInfoDao.getOrgInfoByCommonName(orgName) != null) {//公司已认证，直接申请证书
////                                    Intent intent = new Intent(CompanyCertifyStep2.this, CertSetPwdActivity.class);
////                                    intent.putExtra("name", mEtInputName.getText().toString().trim());
////                                    intent.putExtra("no", mEtInputNumber.getText().toString().trim());
////                                    intent.putExtra("typeNo", true);
////                                    intent.putExtra("orgType", orgType);
////                                    startActivity(intent);
////
////                                } else {
//                                Intent intent = new Intent(CompanyCertifyStep2.this, MultisourceAuthAcitivty.class);
//                                intent.putExtra("orgName", orgName);
//                                intent.putExtra("orgNo", orgNo);
//                                intent.putExtra("lrName", lrName);
//                                intent.putExtra("lrNo", lrNo);
//                                intent.putExtra("orgType", orgType);
//                                intent.putExtra("needHide", needHide);
//                                startActivity(intent);
//
////                                }
//
//
//                            }
//                        }
//                    });
//
//
//                } catch (Exception e) {
////                    mError = e.getMessage();
////                    Log.e(CommonConst.TAG, mError, exc);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissDg();
//                            if (e.getMessage().indexOf("peer") != -1)
//                                Toast.makeText(getApplicationContext(), "无效的服务器请求", Toast.LENGTH_LONG).show();
//                            else
//                                Toast.makeText(getApplicationContext(), "网络连接或访问服务异常", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            }
//
//
//        }).start();
//    }
//
//
//    private String GetCertApplyListEx() throws Exception {
////		String timeout = ApplicationActivity.this.getString(R.string.WebService_Timeout);
////		String urlPath = ApplicationActivity.this.getString(R.string.UMSP_Service_GetCertApplyList);
////		Map<String,String> postParams = new HashMap<String,String>();
////    	//String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
////
////    	String postParam = "";
////        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//        CertController certController = new CertController();
//        String responseStr = certController.getCertInfoList(this, AccountHelper.getToken(getApplicationContext()));
//        return responseStr;
//    }
//
//
    //法人添加单位
    private void AddOrg(String orgName, String orgNo, String legalName, String legalId) {
        final UniTrust uniTrust = new UniTrust(this, false);
        new Thread() {
            public void run() {
                String mStrVal = uniTrust.AddOrgInfo(ParamGen.getVerifyOrg(CompanyCertifyStep2.this, orgName, orgNo, legalName, legalId));
                Log.d("unitrust", mStrVal);
                String strRetMsg;


                try {

                    APPResponse response = new APPResponse(mStrVal);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//
                                AccountHelper.getOrgList(CompanyCertifyStep2.this, "", 0);

//                                Intent intent = new Intent(CompanyCertifyStep2.this, CertSetPwdActivity.class);
//                                intent.putExtra("name", orgName);
//                                intent.putExtra("no", mEtInputNumber.getText().toString().trim());
//                                intent.putExtra("orgType", orgType);
//                                startActivity(intent);
                            } else if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });

                            } else {

                                Toast.makeText(getApplicationContext(), retCode + retMsg, Toast.LENGTH_LONG).show();

//                            SealInfoDao sealInfoDao=new SealInfoDao(CompanyCertifyStep3.this);
//                            sealInfoDao.add();
//                            AccountHelper.getOrgList(CompanyCertifyStep2.this);

                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(), "添加单位信息失败", Toast.LENGTH_LONG).show();

                        }
                    });
                }

            }
        }.start();
    }
}
