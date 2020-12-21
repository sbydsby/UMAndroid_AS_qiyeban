package com.sheca.umee.companyCert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umee.LaunchActivity;
import com.sheca.umee.MainActivity;
import com.sheca.umee.R;
import com.sheca.umee.ValidateGestureActivity;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.SealInfoDao;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.SealInfo;
import com.sheca.umee.presenter.SealController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommUtil;
import com.sheca.umplus.dao.OrgInfoDao;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author shaoboyang
 * @time 2019/8/6 10:41
 * @descript 申请印章--选择证书
 */
public class SealApplyStep3 extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_seal_cert_ok)
    TextView mTvSealCertOk;
    @BindView(R.id.down_choice)
    ImageView mDownChoice;
    @BindView(R.id.et_input_name)
    TextView mEtName;
    @BindView(R.id.guide_line1)
    View mGuideLine1;
    @BindView(R.id.et_input_number)
    EditText edtPassword;
    @BindView(R.id.iv_seal)
    ImageView mIvSeal;
    @BindView(R.id.tv_seal_apply_title3)
    TextView tvSealTitle;
//    @BindView(R.id.timeline)
//    TimeLineView timeLineView;

    private HandlerThread ht = null;

    protected Handler workHandler = null;
    List<String> mList = new ArrayList<>();
    SealController sealController = new SealController();

    private String strSign = "";        //签名数据
    private String strCert = "";        //base64证书
    private int mCertId = -1;        //当前选中证书ID
    private int mLocalCertId = -1;        //当前选中证书ID

//    private int rsaCertId = -1;

    private String strSealName = "";    //印章别名

    private String strPicDate = "";
    private String strPicType = "";
    private String strCertPsd;
    private int localCertid;
    private String orgName;

    private CertDao certDao = null;
    private AccountDao mAccountDao = null;
    private SealInfoDao mSealInfoDao = null;
    private OrgInfoDao orgInfoDao = null;
//    private LogDao mLogDao = null;

    //    private Cert localCert = null;
    private String orgid = "";
    private String userName;

    List<Cert> certs = new ArrayList<>();

    boolean isSingle;

//    boolean rsaFinish = false;


    private SharedPreferences sharedPrefs;
    private final int VALIDATE_GESTURE_CODE = 1;


    boolean isIFAAFingerOpend = LaunchActivity.isIFAAFingerOpend;//是否开启指纹
    boolean isNotificationGesture = false;//是否开启手势
    // 用户需要提供给IFAA的基本信息类
//    private IfaaBaseInfo ifaaBaseInfo = null;
//    // 认证类型
//    private IfaaBaseInfo.IFAAAuthTypeEnum ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;
    private javasafeengine jse = null;
    private final int MSG_AUTHENABLE = 1;
    private Handler ifaaHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTHENABLE:

                    edtPassword.setText(certDao.getCertByID(mCertId).getCerthash());
                    if (edtPassword.getText().toString().trim().length() == 0) {
                        Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_LONG).show();

                    } else {
//                        showDg();
//                        if (rsaFinish) {
                        applyAndSaveSeal();
//                        } else {
//                            applyAndSaveSeal(true);
//                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private File fiel;
    private Bitmap mImage;

    boolean isOnlySealPic = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seal_apply_step3);
        ButterKnife.bind(this);
        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());
        mAccountDao = new AccountDao(this);
        certDao = new CertDao(this);
        mSealInfoDao = new SealInfoDao(this);
//        mLogDao = new LogDao(ApplySealActivity.this);
        orgInfoDao = new OrgInfoDao(this);
//        localCert = certDao.getCertByID(localCertid);
        jse = new javasafeengine();

        isSingle = getIntent().getBooleanExtra("single", false);
        if (isSingle) {
            tvSealTitle.setText("当前证书");
            mDownChoice.setVisibility(View.GONE);
        }


        initView();
        initList();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mImage = null;
//            if (extras.getString("UserType") != null)
//                strUserType = extras.getString("UserType");
//            if (extras.getString("SealName") != null)
//                strSealName = extras.getString("SealName");
            if (extras.getString("PicData") != null) {
                strPicDate = extras.getString("PicData");
//                if (strPicDate.equals("1")) {//1为防止内存溢出存在sp里的图片信息
//                    strPicDate=AccountHelper.getPicData(this);
//                }
//                mIvSeal.setImageURI(Uri.fromFile(new File(strPicDate)));
                if (extras.getBoolean("base64", false)) {
                    isOnlySealPic = true;

                    File file = new File(strPicDate);
                    mImage = BitmapFactory.decodeFile(file.toString());
//                    mImage = CommUtil.stringtoBitmap(strPicDate);
                } else {
                    //isOnlySealPic = false;
                    mImage = BitmapUtils.getCircleBitmap(BitmapUtils.uriToBitmap(strPicDate));
                    File file = new File(strPicDate);
                    file.delete();
                }
                mIvSeal.setImageBitmap(mImage);
                fiel = BitmapUtils.bitmapToFile(SealApplyStep3.this, mImage);

            }
            if (extras.getString("PicType") != null)
                strPicType = extras.getString("PicType");
            if (extras.getString("Cert") != null)
                strCert = extras.getString("Cert");
            if (extras.getString("SignData") != null)
                strSign = extras.getString("SignData");
//            if (extras.containsKey("CertID"))
//                mCertId = extras.getInt("CertID");
            if (extras.containsKey("psd"))
                strCertPsd = extras.getString("psd");
            if (extras.containsKey("localCertid"))
                localCertid = extras.getInt("localCertid");

        }


//        intent.putExtra("name", edName.getText().toString().trim());


//        initData();
//        initGesture();
    }

//    /**
//     * 初始化数据
//     */
//    private void initData() {
//
//        sharedPrefs = getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
//        isNotificationGesture = sharedPrefs.getBoolean(CommonConst.SETTINGS_GESTURE_OPENED + LockPatternUtil.getActName(), false);
//
//        mAccountDao = new AccountDao(SealApplyStep3.this);
//        boolean bIfaaFace = sharedPrefs.getBoolean(CommonConst.SETTINGS_IFAA_FACE_ENABLED, false);
//        if (bIfaaFace)
//            ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE;
//
//        // app 需要提供给ifaa 一些基本信息
//        ifaaBaseInfo = new IfaaBaseInfo(this);
//        // 认证类型，默认为指纹 AUTHTYPE_FINGERPRINT
//        ifaaBaseInfo.setAuthType(ifaaAuthType);
//        // 业务 ID, 请保持唯一，记录在ifaa log 中，当出问题时候，查问题用。
//        ifaaBaseInfo.setTransactionID("transId");
//        // 业务的附加信息，记录在ifaa log 中，当出问题时，查问题用。
//        ifaaBaseInfo.setTransactionPayload("transPayload");
//        ifaaBaseInfo.setTransactionType("Login");
//        // TODO 用户id, 此值参与ifaa 业务以及 token 的生成，务必保证其唯一，可以传入用户名的hash值来脱敏。
//        ifaaBaseInfo.setUserID(/* "user1"*/mAccountDao.getLoginAccount().getName());
//        // 设置使用SDK提供的指纹弹框页面
//        ifaaBaseInfo.usingDefaultAuthUI("法人一证通认证口令", "取消");
//        // 设置 IFAA 服务器的url 地址，默认为一砂测试服务器。
//        final String ifaaURL = CommonConst.ESAND_DEV_SERVER_URL;
////				DaoActivity.this.getString(R.string.UMSP_Service_IFAA);
//        ifaaBaseInfo.setUrl(ifaaURL);
//
//        //设置指纹框认证次数，类方法
//        ETASManager.setAuthNumber(3);
//    }
//
//
//    private void initGesture() {
//        LaunchActivity.isIFAAFingerOK = false;
//        if (isIFAAFingerOpend) {
//            if (null == LaunchActivity.authenticator)
//                LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
//        }
//        ((ImageView) findViewById(R.id.pwdkeyboard)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                findViewById(R.id.cl_password).setVisibility(RelativeLayout.VISIBLE);
//                findViewById(R.id.relativelayoutFinger).setVisibility(RelativeLayout.GONE);
//                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
//
//            }
//        });
//        findViewById(R.id.finger_image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showFingerCheck();
//            }
//        });
//        findViewById(R.id.gesture_image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showGestureCheck();
//            }
//        });
//        findViewById(R.id.relativelayoutGesture).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
//                findViewById(R.id.gesture_image).setVisibility(RelativeLayout.VISIBLE);
//                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
//
////                        useGesture = true;
//            }
//        });
//        if (isIFAAFingerOpend) {
//
//            findViewById(R.id.relativelayoutFinger).setVisibility(View.VISIBLE);
//            findViewById(R.id.finger_image).setVisibility(View.VISIBLE);
//            findViewById(R.id.gesture_image).setVisibility(View.GONE);
//            findViewById(R.id.cl_password).setVisibility(View.GONE);
//
//            if (isNotificationGesture) {
//                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.VISIBLE);
//
//            } else {
//                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
//
//            }
//
//            showFingerCheck();
//        } else if (isNotificationGesture) {
//            findViewById(R.id.relativelayoutFinger).setVisibility(View.VISIBLE);
//            findViewById(R.id.finger_image).setVisibility(View.GONE);
//            findViewById(R.id.gesture_image).setVisibility(View.VISIBLE);
//            findViewById(R.id.cl_password).setVisibility(View.GONE);
//        }
//
//
//    }

    private void showGestureCheck() {
        //验证手势密码
        Intent intent = new Intent(getApplicationContext(), ValidateGestureActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, VALIDATE_GESTURE_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VALIDATE_GESTURE_CODE) {
            if (resultCode == RESULT_OK) {
                LaunchActivity.isIFAAFingerOK = true;
                ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);

            } else if (resultCode == RESULT_CANCELED) {

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void showFingerCheck() {
        /*
        curProcess = Process.AUTH_GETREQ;

        if(!getToken().isEmpty())
            IFAAFingerprintOpenAPI.getInstance().setToken(getToken());

        String info = AuthenticatorManager.getAuthData(DaoActivity.this,  mAccountDao.getLoginAccount().getName());
        IFAAFingerprintOpenAPI.getInstance().getIFAAAuthRequestAsyn(DaoActivity.this, mAccountDao.getLoginAccount().getName(),info, callback);
        secData = info;
        */

        // 执行 IFAA 认证操作
//        authIFAA(ifaaBaseInfo);
        //auth(ifaaBaseInfo);
    }

//    private void authIFAA(final IfaaBaseInfo ifaaBaseInfo) {
//        final EtasAuthentication etasAuthentication = new EtasAuthentication(ifaaBaseInfo);
//
//        // 认证初始化
//        final EtasResult etasResult = etasAuthentication.authInit();
//        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//
//            //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");
//
//            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {
//
//                // TODO 此时可引导用户录入指纹/人脸后在做认证操作
//                Toast.makeText(SealApplyStep3.this, "该手机未录入指纹", Toast.LENGTH_LONG).show();
//            }
//            return;
//        }
//        auth(ifaaBaseInfo);
////		new MyAsycnTaks(){
////			@Override
////			public void preTask() {
////				String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
////				strInfo = String.format("%s=%s&%s=%s",
////						URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
////						URLEncoder.encode(mTokenId),
////						URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
////						URLEncoder.encode(etasResult.getMsg()));
////			}
////
////			@Override
////			public void doinBack() {
////				UniTrust mUnitTrust = new UniTrust(DaoActivity.this, false);
////				responResult=mUnitTrust.IFAAAuth(strInfo);
////			}
////
////			@Override
////			public void postTask() {
////				final APPResponse response = new APPResponse(responResult);
////				int resultStr = response.getReturnCode();
////				final String retMsg = response.getReturnMsg();
////				if(0==resultStr){
////					showProgDlg("IFAA认证初始化中...");
////					auth(ifaaBaseInfo);
////				}else{
////					showProgDlg("IFAA认证初始化中...");
////					//Toast.makeText(DaoActivity.this, "认证初始化失败:"+resultStr+","+retMsg,Toast.LENGTH_LONG).show();
////					auth(ifaaBaseInfo);
////				}
////			}
////		}.execute();
//
//    }

//    /**
//     * ifaa 认证
//     */
//    private void auth(final IfaaBaseInfo ifaaBaseInfo) {
//
//        do {
//
//            final EtasAuthentication etasAuthentication = new EtasAuthentication(ifaaBaseInfo);
//
//            // 认证初始化
//            EtasResult etasResult = etasAuthentication.authInit();
//            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
////                closeProgDlg();
//                //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");
//
//                if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {
//
//                    // TODO 此时可引导用户录入指纹/人脸后在做认证操作
//
//                }
//                break;
//            }
//            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
//            String msg = etasResult.getMsg().replace(CommonConst.IFFA_OLD_APP_ID, CommonConst.IFFA_NEW_APP_ID);
//            etasExcecuteObservable.excecute(msg)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .flatMap(new Func1<String, Observable<String>>() {
//
//                        @Override
//                        public Observable<String> call(final String msg) { // 发起认证请求
//
//                            final Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
//
//                                @Override
//                                public void call(final Subscriber<? super String> subscriber) {
//
//                                    // 服务器数据已经返回，执行本地注册操作
//                                    etasAuthentication.auth(msg, new EtasAuthenticatorCallback() {
//                                        @Override
//                                        public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {
//
//                                            // 不是运行在ui 线程，所以不能在此更新界面
////                                            updateTextView("指纹认证返回状态 ： " + authStatusCode);
//                                        }
//
//                                        @Override
//                                        public void onResult(EtasResult etasResult) {
//                                            if (etasResult != null) {
//                                                //updateTextView("认证 onResult：" + etasResult.getCode() + "\n");
//                                                MyLog.error("认证 onResult：" + etasResult.getCode());
////                                                closeProgDlg();
//                                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//
//                                                    // 不支持多指位，请用注册手指进行操作
//                                                    if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.CLIENT_ERROR_MULTI_FP_NOT_SUPPORT) {
//
//                                                        String msg;
//                                                        // 判断注册的那个指位是否被删除了
//                                                        EtasStatus etasStatus = new EtasStatus(ifaaBaseInfo);
//                                                        // 这里返回的 etasResult.getMsg() 是注册 token
//                                                        EtasResult result = etasStatus.checkLocalStatus(etasResult.getMsg());
//                                                        if (result.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_DELETED) {
//
//                                                            // 引导用户注销了吧
//                                                            msg = "此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用";
//                                                        } else {
//
//                                                            msg = "此手机不支持多指位，请用注册的那根手指进行操作";
//                                                        }
//
//                                                        // 不是运行在ui 线程，所以不能在此更新界面;
//                                                        //updateTextView(msg);
//
//                                                    } else {
//
//                                                        // 不是运行在ui 线程，所以不能在此更新界面;
//                                                        //updateTextView("认证失败 ： " + etasResult.getMsg());
//                                                    }
//
//                                                } else {
//                                                    String msg = etasResult.getMsg().replace(CommonConst.IFFA_OLD_APP_ID, CommonConst.IFFA_NEW_APP_ID);
//
//                                                    subscriber.onNext(msg);
////                                                updateTextView("本地认证成功 ：)");
//                                                }
//                                            }
//
//                                        }
//                                    });
//                                }
//                            });
//
//                            return observable;
//                        }
//                    })
//                    .flatMap(new Func1<String, Observable<String>>() {
//
//                        @Override
//                        public Observable<String> call(String msg) {
//
//                            // 把认证信息同步到服务器
//                            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
//                            Observable observable = etasExcecuteObservable.excecute(msg);
//                            return observable;
//                        }
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Subscriber<String>() {
//
//                        @Override
//                        public void onCompleted() {
//
////                            updateTextView("认证请求流程结束\n");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
////                            closeProgDlg();
//                            etasAuthentication.sendAuthStatusCodeComplete();
//                            //updateTextView("认证请求失败 ： " + e.getMessage() + "\n");
//                        }
//
//                        @Override
//                        public void onNext(String msg) {
//
//                            // 告知 sdk, 注册流程已经结束
//                            EtasResult etasResult = etasAuthentication.authFinish(msg);
//                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                                ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
//                                //updateTextView("认证成功 ：）\n");
//                                //ifaaSwitch.setChecked(true);
//
//                            } else if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.WRONG_AUTHDATAINDEX) { // 指位不匹配，此处可以选择是否更新指位
//
//                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()) {
//                                    //updateTextView("即将更新人脸 ：)\n");
//                                    ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
//                                } else {
//                                    //updateTextView("即将更新指位 ：)\n");
//                                    ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
//                                }
//
//                                //onTemplateMismatch(ifaaBaseInfo, etasResult.getMsg());
//                            } else {
//
//                                //tvShowInfos.append("认证失败 :(\n" + etasResult.getMsg());
//                                //ifaaSwitch.setChecked(false);
//                            }
//                        }
//                    });
//        } while (false);
//    }

    private void initView() {
        mTvTitle.setText("申请印章");
//        timeLineView.setStep3Constraint();
    }


    public void initList() {
        CertDao certDao = new CertDao(this);

        if (isSingle) {
            Cert cert = certDao.getCertByCertsn(getIntent().getStringExtra("certSn"), AccountHelper.getUsername(this));

            mCertId = cert.getSdkID();
            mLocalCertId=cert.getId();
//            rsaCertId = certDao.getOtherCert(mCertId,this).getId();

            orgName = CommUtil.getCertDetail(cert, 17);
            mEtName.setText(orgName);


            String certificate = cert.getCertificate();
            byte[] bCert = Base64.decode(certificate);
            // byte[] bEncCert = Base64.decode(cert.getEnccertificate());
            Certificate oCert = jse.getCertFromBuffer(bCert);
            X509Certificate oX509Cert = (X509Certificate) oCert;

            orgid = jse.getCertExtInfo("1.2.156.112570.11.210", oX509Cert);
            if ("".equals(orgid) || null == orgid)
                orgid = "";

//            OrgInfo orgInfo = orgInfoDao.getOrgInfoByCommonName(orgName);
//            if (orgInfo != null) {
//                orgid = orgInfo.getPaperNo() + "";
//
//            }
        } else {

// TODO
//            certs = certDao.queryWithCanApplySeal();


            List<Cert> localCerts = certDao.getAllCerts(AccountHelper.getUsername(this));

            for (int i = 0; i < localCerts.size(); i++) {
                if (localCerts.get(i).getCerttype().contains("个人")) {
                } else {
                    if (localCerts.get(i).getEnvsn().indexOf("-e") != -1) {//过滤加密证书
                        continue;
                    }

                    if (localCerts.get(i).getSealstate() == 0) {
                        certs.add(localCerts.get(i));
                    } else {
                        SealInfo sealInfo = mSealInfoDao.getSealByCertsn(localCerts.get(i).getCertsn(), AccountHelper.getUsername(SealApplyStep3.this));

                        if (sealInfo != null) {
                            if (sealInfo.getState() == 5) {
                                certs.add(localCerts.get(i));
                            }

                        }


                    }
                }
            }


            if (certs != null) {

                for (int i = 0; i < certs.size(); i++) {
                    String name = CommUtil.getCertDetail(certs.get(i), 17);

                    mList.add(name);

                }
                if (mList.size() > 0) {

                    mCertId = certs.get(0).getSdkID();
                    mLocalCertId=certs.get(0).getId();
//                    rsaCertId = certDao.getOtherCert(mCertId,this).getId();
                    orgName = CommUtil.getCertDetail(certs.get(0), 17);
                    mEtName.setText(orgName);


                    String certificate = certs.get(0).getCertificate();
                    byte[] bCert = Base64.decode(certificate);
                    // byte[] bEncCert = Base64.decode(cert.getEnccertificate());
                    Certificate oCert = jse.getCertFromBuffer(bCert);
                    X509Certificate oX509Cert = (X509Certificate) oCert;

                    orgid = jse.getCertExtInfo("1.2.156.112570.11.210", oX509Cert);
                    if ("".equals(orgid) || null == orgid)
                        orgid = "";

//                    OrgInfo orgInfo = orgInfoDao.getOrgInfoByCommonName(orgName);
//                    if (orgInfo != null) {
//                        orgid = orgInfo.getPaperNo() + "";
//
//                    }
                }

            }
        }

    }

    @OnClick({R.id.iv_back, R.id.tv_seal_cert_ok, R.id.down_choice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_seal_cert_ok:
                if (edtPassword.getText().toString().trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_LONG).show();

                } else {
//                    showDg();
//                    if (rsaFinish) {
                    applyAndSaveSeal();
//                    } else {
//                        applyAndSaveSeal(true);
//                    }
                }
//                Intent intent = new Intent(SealApplyStep3.this, SealApplyStep4.class);
//                startActivity(intent);
                break;
            case R.id.down_choice:
                showPop();

                break;
        }

    }

    //下拉列表
    private void showPop() {
        final PopupWindowUtil popupWindow = new PopupWindowUtil(SealApplyStep3.this, mList);
        popupWindow.setItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mCertId = certs.get(position).getSdkID();
                mLocalCertId=certs.get(position).getId();
//                rsaCertId = certDao.getOtherCert(mCertId,SealApplyStep3.this).getId();
                orgName = CommUtil.getCertDetail(certs.get(position), 17);
                mEtName.setText(orgName);


                String certificate = certs.get(position).getCertificate();
                byte[] bCert = Base64.decode(certificate);
                // byte[] bEncCert = Base64.decode(cert.getEnccertificate());
                Certificate oCert = jse.getCertFromBuffer(bCert);
                X509Certificate oX509Cert = (X509Certificate) oCert;

                orgid = jse.getCertExtInfo("1.2.156.112570.11.210", oX509Cert);
                if ("".equals(orgid) || null == orgid)
                    orgid = "";
//                OrgInfo orgInfo = orgInfoDao.getOrgInfoByCommonName(orgName);
//                if (orgInfo != null) {
//                    orgid = orgInfo.getPaperNo() + "";
//                }
//                userName=certs.get(position).get
                popupWindow.dismiss();
            }
        });
        //根据后面的数字 手动调节窗口的宽度
        int guideWith = mGuideLine1.getWidth();
        popupWindow.showPopMiddle(guideWith, mGuideLine1);
    }


    private void applyAndSaveSeal() {
        //showProgDlg("申请印章中...");

        ICallback callback = new ICallback() {
            @Override
            public void onCallback(Object data) {
                try {

                    APPResponse response = new APPResponse((String) data);
//                    JSONObject jbRet = null;
                    if (response.getReturnCode() == 0) {

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {


                        JSONObject jb = response.getResult();

//                                {"result":"{\"sealSN\":\"04777181695000308615986380287764\"}","returnCode":0,"returnMsg":"成功"}

                        String sealSN = jb.getString("sealID");

                        String account = new AccountDao(getApplicationContext()).getLoginAccount().getName();

                        SealInfo sealInfo = sealController.getAccountSealInfoBySN(SealApplyStep3.this, sealSN, account);
                        sealInfo.setState(6);//审核中
                        sealInfo.setDownloadstatus(0);//未下载
                        Cert localCert = certDao.getCertByID(mLocalCertId);


                        //设置证书状态
                        localCert.setSealsn(sealInfo.getSealsn());
                        localCert.setSealstate(Cert.STATUS_IS_SEAL);

                        //印章需要与证书做关联
                        sealInfo.setCertsn(localCert.getCertsn());
                        sealInfo.setCert(localCert.getCertificate());
                        sealInfo.setSdkID(mCertId);

                        SealInfoDao sealInfoDao = new SealInfoDao(getApplicationContext());
                        sealInfoDao.addSeal(sealInfo, account);

                        localCert.setSealsn(sealInfo.getSealsn());
                        certDao.updateCert(localCert, account);


                        gotoNextActivity(1);


//                            }
//                        });

//                        JSONObject jb = response.getResult();
//                        String sealid = jb.getString("sealID");
//
//                        String account = new AccountDao(getApplicationContext()).getLoginAccount().getName();

//                        SealInfo sealInfo = sealController.getAccountSealInfoBySN(SealApplyStep3.this, sealid, account);


//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Toast.makeText(SealApplyStep3.this, "申请印章成功", Toast.LENGTH_LONG).show();
//                                //closeProgDlg();
////                                showLoadingView(SEAL_APPLY_ERR);
//
////                                findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
//                                return;
//                            }
//                        });

                    } else if (response.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || response.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                dismissDg();
//                                AccountHelper.reLogin(SealApplyStep3.this);
                            }
                        });

                    } else {
                        final String strReturnStr = response.getReturnMsg();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                dismissDg();
                                Toast.makeText(getApplicationContext(), strReturnStr, Toast.LENGTH_LONG).show();
                                //closeProgDlg();
//                                showLoadingView(SEAL_APPLY_ERR);

//                                findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
                                return;
                            }
                        });
                    }

                } catch (final Exception exc) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            dismissDg();
                            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
                            //closeProgDlg();
//                            showLoadingView(SEAL_APPLY_ERR);

                            return;
                        }
                    });
                }
            }
        };


        String pwd = CommUtil.getPWDHash(edtPassword.getText().toString().trim());

        if (isOnlySealPic) {
            SealController.getInstance().makeSeal(this, strPicDate,
                    orgid.length() >= 18 ? orgid.substring(8, 17) : orgid, "" + mCertId, pwd, mEtName.getText()
                            .toString() + "的印章", orgName, getIntent().getStringExtra("picType"), callback);
        } else {
            SealController.getInstance().applySeal(this, CommUtil.fileToBase64(fiel),
                    orgid.length() >= 18 ? orgid.substring(8, 17) : orgid, "" + mCertId, pwd, mEtName.getText()
                            .toString() + "的印章", orgName, callback);
        }


//
////
////
////
//        final Handler handler = new Handler(SealApplyStep3.this.getMainLooper());
//
//        workHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//
//                    String responseStr = applySeal(isRSA);
//                    APPResponse response = new APPResponse(responseStr);
////                    JSONObject jbRet = null;
//                    if (response.getReturnCode() == 0) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (isRSA) {
//                                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                                    AccountHelper.getCertList(SealApplyStep3.this, mAccountName, 0);
//                                } else {
//                                    String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
//                                    AccountHelper.getCertList(SealApplyStep3.this, mAccountName, 1);
//                                }
//
//
//                            }
//                        });
//
////                        JSONObject jb = response.getResult();
////                        String sealid = jb.getString("sealID");
////
////                        String account = new AccountDao(getApplicationContext()).getLoginAccount().getName();
//
////                        SealInfo sealInfo = sealController.getAccountSealInfoBySN(SealApplyStep3.this, sealid, account);
//
//
////                        handler.post(new Runnable() {
////                            @Override
////                            public void run() {
////
////                                Toast.makeText(SealApplyStep3.this, "申请印章成功", Toast.LENGTH_LONG).show();
////                                //closeProgDlg();
//////                                showLoadingView(SEAL_APPLY_ERR);
////
//////                                findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
////                                return;
////                            }
////                        });
//
//                    } else if (response.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR9 || response.getReturnCode() == com.sheca.umplus.util.CommonConst.RETURN_CODE_ERR12) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                AccountHelper.reLogin(SealApplyStep3.this);
//                            }
//                        });
//
//                    } else {
//                        final String strReturnStr = response.getReturnMsg();
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Toast.makeText(SealApplyStep3.this, strReturnStr, Toast.LENGTH_LONG).show();
//                                //closeProgDlg();
////                                showLoadingView(SEAL_APPLY_ERR);
//
////                                findViewById(R.id.relativelayout3).setVisibility(RelativeLayout.VISIBLE);
//                                return;
//                            }
//                        });
//                    }
//
//                } catch (final Exception exc) {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SealApplyStep3.this, exc.getMessage(), Toast.LENGTH_LONG).show();
//                            //closeProgDlg();
////                            showLoadingView(SEAL_APPLY_ERR);
//
//                            return;
//                        }
//                    });
//                }
//            }
//        });

    }


//    private String applySeal(boolean isRSA) throws Exception {
//        Log.e("strPicDate", strPicDate);
//        String responseStr = sealController.applySeal(this, CommUtil.fileToBase64(new File(strPicDate)), orgid.length() >= 18 ? orgid.substring(8, 17) : orgid, "" + (isRSA ? rsaCertId : mCertId), edtPassword.getText().toString().trim(), mEtName.getText().toString() + "的印章", orgName);
//        return responseStr;
//    }

    @Override
    public void gotoNextActivity(int type) {
        super.gotoNextActivity(type);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0) {
//                    applyAndSaveSeal(false);
                } else {

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "印章申请成功", Toast.LENGTH_SHORT).show();
//                            dismissDg();
//                            if (isOnlySealPic) {
//
//                                EventBus.getDefault().post(new SealRefreshEvent());
//                                Intent intent = new Intent(SealApplyStep3.this, MainActivityNew.class);
//                                startActivity(intent);
//                            } else {
//                                Intent intent = new Intent(SealApplyStep3.this, SealApplyStep4.class);
//                                startActivity(intent);
//                                finish();
//                            }


                            Intent intent = new Intent(SealApplyStep3.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });

                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapUtils.recycleBitmap(mImage);
    }
}
