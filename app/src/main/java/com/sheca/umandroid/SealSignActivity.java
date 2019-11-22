package com.sheca.umandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esandinfo.etas.ETASManager;
import com.esandinfo.etas.EtasResult;
import com.esandinfo.etas.IfaaBaseInfo;
import com.esandinfo.etas.IfaaCommon;
import com.esandinfo.etas.biz.EtasAuthentication;
import com.esandinfo.etas.biz.EtasAuthenticatorCallback;
import com.esandinfo.etas.biz.EtasStatus;
import com.esandinfo.etas.utils.MyLog;
import com.esandinfo.utils.EtasExcecuteObservable;
import com.ifaa.sdk.api.AuthenticatorManager;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.CertDao;
import com.sheca.umplus.dao.SealInfoDao;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.SealInfo;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class SealSignActivity extends BaseActivity {

    ImageView imgSeal;

    EditText textPwd;

    Button btn_loign_ok;

    ImageView btn_loign_back;


    String certSn;
    SealInfo seal;
    Cert cert;


    private SharedPreferences sharedPrefs;
    private final int VALIDATE_GESTURE_CODE = 1;

    AccountDao mAccountDao;

    boolean isNotificationGesture = false;//是否开启手势
    // 用户需要提供给IFAA的基本信息类
    private IfaaBaseInfo ifaaBaseInfo = null;
    // 认证类型
    private IfaaBaseInfo.IFAAAuthTypeEnum ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;

    private final int MSG_AUTHENABLE = 1;
    private Handler ifaaHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTHENABLE:
                    textPwd.setText(cert.getCerthash());

                    scan();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dao_seal_sign);

        imgSeal=findViewById(R.id.img_seal);
        textPwd=findViewById(R.id.textPwd);
        btn_loign_ok=findViewById(R.id.btn_loign_ok);
        btn_loign_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textPwd.getText().toString().trim().length() == 0) {
                    Toast.makeText(SealSignActivity.this, "请输入证书保护口令", Toast.LENGTH_LONG).show();
                    return;
                }
                scan();
            }
        });

        btn_loign_back=findViewById(R.id.btn_loign_back);
        btn_loign_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        initView();

        certSn = getIntent().getStringExtra("certSn");
        CertDao certDao = new CertDao(this);
        cert = certDao.getCertByCertsn(certSn, "");


        SealInfoDao sealInfoDao = new SealInfoDao(this);
        seal = sealInfoDao.getSealByCertsn(certSn, "");
        if (seal != null) {
//            Log.e("地址",seal.getPicdata());
            imgSeal.setImageBitmap(CommUtil.stringtoBitmap(seal.getPicdata()));
        }
        initData();
        initGesture();
    }


    private void scan() {
        if (!cert.getCerthash().equals(textPwd.getText().toString().trim())) {
            Toast.makeText(this, "证书保护口令错误", Toast.LENGTH_LONG).show();
            return;
        }

       final String params = ParamGen.Scan(this, getIntent().getStringExtra("result"),
                cert.getId() + "", textPwd.getText().toString().trim());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UniTrust uniTrust = new UniTrust(SealSignActivity.this, false);
                    String resStr = uniTrust.Scan(params);

                  final   APPResponse response = new APPResponse(resStr);
                    final int retCode = response.getReturnCode();
//                    final String retMsg = response.getReturnMsg();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                                Intent intent = getIntent();
                                intent.putExtra("result", response.getResult().optString("message"));
                                Toast.makeText(SealSignActivity.this, "印章签署成功", Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(SealSignActivity.this, "印章签署失败", Toast.LENGTH_LONG).show();
//
                                setResult(RESULT_CANCELED, getIntent());
                                finish();
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(SealSignActivity.this, "印章签署失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    /**
     * 初始化数据
     */
    private void initData() {


        sharedPrefs = getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);

        LaunchActivity.isIFAAFingerOpend = sharedPrefs.getBoolean(CommonConst.SETTINGS_FINGER_OPENED +  AccountHelper.getUsername(getApplicationContext()), false);
        LaunchActivity.isIFAAFingerOK = false;
        if (LaunchActivity.isIFAAFingerOpend) {
            if (null == LaunchActivity.authenticator)
                LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
        }
        isNotificationGesture = sharedPrefs.getBoolean(CommonConst.SETTINGS_GESTURE_OPENED +  AccountHelper.getUsername(getApplicationContext()), false);

        mAccountDao = new AccountDao(SealSignActivity.this);
        boolean bIfaaFace = sharedPrefs.getBoolean(CommonConst.SETTINGS_IFAA_FACE_ENABLED, false);
        if (bIfaaFace)
            ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE;

        // app 需要提供给ifaa 一些基本信息
        ifaaBaseInfo = new IfaaBaseInfo(this);
        // 认证类型，默认为指纹 AUTHTYPE_FINGERPRINT
        ifaaBaseInfo.setAuthType(ifaaAuthType);
        // 业务 ID, 请保持唯一，记录在ifaa log 中，当出问题时候，查问题用。
        ifaaBaseInfo.setTransactionID("transId");
        // 业务的附加信息，记录在ifaa log 中，当出问题时，查问题用。
        ifaaBaseInfo.setTransactionPayload("transPayload");
        ifaaBaseInfo.setTransactionType("Login");
        // TODO 用户id, 此值参与ifaa 业务以及 token 的生成，务必保证其唯一，可以传入用户名的hash值来脱敏。
        ifaaBaseInfo.setUserID(/* "user1"*/mAccountDao.getLoginAccount().getName());
        // 设置使用SDK提供的指纹弹框页面
        ifaaBaseInfo.usingDefaultAuthUI("法人一证通认证口令", "取消");
        // 设置 IFAA 服务器的url 地址，默认为一砂测试服务器。
        final String ifaaURL = CommonConst.ESAND_DEV_SERVER_URL;
//				DaoActivity.this.getString(R.string.UMSP_Service_IFAA);
        ifaaBaseInfo.setUrl(ifaaURL);

        //设置指纹框认证次数，类方法
        ETASManager.setAuthNumber(3);
    }


    private void initGesture() {
        if (LaunchActivity.isIFAAFingerOpend) {
            if (null == LaunchActivity.authenticator)
                LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
        }
        ((ImageView) findViewById(R.id.pwdkeyboard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.cl_password).setVisibility(RelativeLayout.VISIBLE);
                findViewById(R.id.relativelayoutFinger).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);

            }
        });
        findViewById(R.id.finger_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFingerCheck();
            }
        });
        findViewById(R.id.gesture_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGestureCheck();
            }
        });
        findViewById(R.id.relativelayoutGesture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.gesture_image).setVisibility(RelativeLayout.VISIBLE);
                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);

//                        useGesture = true;
            }
        });
        if (LaunchActivity.isIFAAFingerOpend) {

            findViewById(R.id.relativelayoutFinger).setVisibility(View.VISIBLE);
            findViewById(R.id.finger_image).setVisibility(View.VISIBLE);
            findViewById(R.id.gesture_image).setVisibility(View.GONE);
            findViewById(R.id.cl_password).setVisibility(View.GONE);

            if (isNotificationGesture) {
                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.VISIBLE);

            } else {
                findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);

            }
            if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");

            }
            showFingerCheck();
        } else if (isNotificationGesture) {
            findViewById(R.id.relativelayoutFinger).setVisibility(View.VISIBLE);
            findViewById(R.id.finger_image).setVisibility(View.GONE);
            findViewById(R.id.gesture_image).setVisibility(View.VISIBLE);
            findViewById(R.id.cl_password).setVisibility(View.GONE);
        }


    }

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
        authIFAA(ifaaBaseInfo);
        //auth(ifaaBaseInfo);
    }

    private void authIFAA(final IfaaBaseInfo ifaaBaseInfo) {
        final EtasAuthentication etasAuthentication = new EtasAuthentication(ifaaBaseInfo);

        // 认证初始化
        final EtasResult etasResult = etasAuthentication.authInit();
        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

            //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");

            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {

                // TODO 此时可引导用户录入指纹/人脸后在做认证操作
                Toast.makeText(SealSignActivity.this, "该手机未录入指纹", Toast.LENGTH_LONG).show();
            }
            return;
        }
        auth(ifaaBaseInfo);
//		new MyAsycnTaks(){
//			@Override
//			public void preTask() {
//				String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//				strInfo = String.format("%s=%s&%s=%s",
//						URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
//						URLEncoder.encode(mTokenId),
//						URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
//						URLEncoder.encode(etasResult.getMsg()));
//			}
//
//			@Override
//			public void doinBack() {
//				UniTrust mUnitTrust = new UniTrust(DaoActivity.this, false);
//				responResult=mUnitTrust.IFAAAuth(strInfo);
//			}
//
//			@Override
//			public void postTask() {
//				final APPResponse response = new APPResponse(responResult);
//				int resultStr = response.getReturnCode();
//				final String retMsg = response.getReturnMsg();
//				if(0==resultStr){
//					showProgDlg("IFAA认证初始化中...");
//					auth(ifaaBaseInfo);
//				}else{
//					showProgDlg("IFAA认证初始化中...");
//					//Toast.makeText(DaoActivity.this, "认证初始化失败:"+resultStr+","+retMsg,Toast.LENGTH_LONG).show();
//					auth(ifaaBaseInfo);
//				}
//			}
//		}.execute();

    }

    /**
     * ifaa 认证
     */
    private void auth(final IfaaBaseInfo ifaaBaseInfo) {

        do {

            final EtasAuthentication etasAuthentication = new EtasAuthentication(ifaaBaseInfo);

            // 认证初始化
            EtasResult etasResult = etasAuthentication.authInit();
            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");

                if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {

                    // TODO 此时可引导用户录入指纹/人脸后在做认证操作

                }
                break;
            }
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            String msg = etasResult.getMsg().replace(CommonConst.IFFA_OLD_APP_ID, CommonConst.IFFA_NEW_APP_ID);
            etasExcecuteObservable.excecute(msg)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<String, Observable<String>>() {

                        @Override
                        public Observable<String> call(final String msg) { // 发起认证请求

                            final Observable observable = Observable.create(new Observable.OnSubscribe<String>() {

                                @Override
                                public void call(final Subscriber<? super String> subscriber) {

                                    // 服务器数据已经返回，执行本地注册操作
                                    etasAuthentication.auth(msg, new EtasAuthenticatorCallback() {
                                        @Override
                                        public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {

                                            // 不是运行在ui 线程，所以不能在此更新界面
//                                            updateTextView("指纹认证返回状态 ： " + authStatusCode);
                                        }

                                        @Override
                                        public void onResult(EtasResult etasResult) {
                                            if (etasResult != null) {
                                                //updateTextView("认证 onResult：" + etasResult.getCode() + "\n");
                                                MyLog.error("认证 onResult：" + etasResult.getCode());

                                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                                    // 不支持多指位，请用注册手指进行操作
                                                    if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.CLIENT_ERROR_MULTI_FP_NOT_SUPPORT) {

                                                        String msg;
                                                        // 判断注册的那个指位是否被删除了
                                                        EtasStatus etasStatus = new EtasStatus(ifaaBaseInfo);
                                                        // 这里返回的 etasResult.getMsg() 是注册 token
                                                        EtasResult result = etasStatus.checkLocalStatus(etasResult.getMsg());
                                                        if (result.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_DELETED) {

                                                            // 引导用户注销了吧
                                                            msg = "此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用";
                                                        } else {

                                                            msg = "此手机不支持多指位，请用注册的那根手指进行操作";
                                                        }

                                                        // 不是运行在ui 线程，所以不能在此更新界面;
                                                        //updateTextView(msg);

                                                    } else {

                                                        // 不是运行在ui 线程，所以不能在此更新界面;
                                                        //updateTextView("认证失败 ： " + etasResult.getMsg());
                                                    }

                                                } else {
                                                    String msg = etasResult.getMsg().replace(CommonConst.IFFA_OLD_APP_ID, CommonConst.IFFA_NEW_APP_ID);

                                                    subscriber.onNext(msg);
//                                                updateTextView("本地认证成功 ：)");
                                                }
                                            }

                                        }
                                    });
                                }
                            });

                            return observable;
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {

                        @Override
                        public Observable<String> call(String msg) {

                            // 把认证信息同步到服务器
                            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
                            Observable observable = etasExcecuteObservable.excecute(msg);
                            return observable;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {

                        @Override
                        public void onCompleted() {

//                            updateTextView("认证请求流程结束\n");
                        }

                        @Override
                        public void onError(Throwable e) {

                            etasAuthentication.sendAuthStatusCodeComplete();
                            //updateTextView("认证请求失败 ： " + e.getMessage() + "\n");
                        }

                        @Override
                        public void onNext(String msg) {

                            // 告知 sdk, 注册流程已经结束
                            EtasResult etasResult = etasAuthentication.authFinish(msg);
                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
                                ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
                                //updateTextView("认证成功 ：）\n");
                                //ifaaSwitch.setChecked(true);

                            } else if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.WRONG_AUTHDATAINDEX) { // 指位不匹配，此处可以选择是否更新指位

                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()) {
                                    //updateTextView("即将更新人脸 ：)\n");
                                    ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
                                } else {
                                    //updateTextView("即将更新指位 ：)\n");
                                    ifaaHandler.sendEmptyMessage(MSG_AUTHENABLE);
                                }

                                //onTemplateMismatch(ifaaBaseInfo, etasResult.getMsg());
                            } else {

                                //tvShowInfos.append("认证失败 :(\n" + etasResult.getMsg());
                                //ifaaSwitch.setChecked(false);
                            }
                        }
                    });
        } while (false);
    }
}
