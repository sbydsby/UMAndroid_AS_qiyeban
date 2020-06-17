package com.sheca.umandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.esandinfo.core.utils.MyLog;
import com.esandinfo.ifaa.AuthStatusCode;
import com.esandinfo.ifaa.EDISAuthManager;
import com.esandinfo.ifaa.IFAAAuthTypeEnum;
import com.esandinfo.ifaa.IFAABaseInfo;
import com.esandinfo.ifaa.IFAACommon;
import com.esandinfo.ifaa.bean.IFAAResult;
import com.esandinfo.ifaa.biz.IFAACallback;
import com.sheca.fingerui.FingerPrintToast;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.presenter.ExcecuteObservable;
import com.sheca.umandroid.util.CommonConst;

import org.ifaa.android.manager.face.IFAAFaceManager;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.esandinfo.ifaa.EDISAuthManager.getSupportBIOTypes;
import static com.sheca.umandroid.util.CommUtil.getPWDHash;

public class SettingFingerTypeActivity extends Activity {
    private RadioGroup group_temo;
    private RadioButton checkRadioButton;

    private ProgressDialog progDialog = null;
    private SharedPreferences sharedPrefs;
    private boolean m_log = true;
    private AccountDao mAccountDao = null;
    private boolean isInited = false;

    private String secData = "";

    public enum Process {
        REG_GETREQ, REG_SENDRESP, AUTH_GETREQ, AUTH_SENDRESP, DEREG_GETREQ
    }

    ;

    private Handler handler;
    private final int MSG_REGENABLE = 1;
    private final int MSG_REGDISABLE = 2;
    private String token = "";
    private static final int FINGER_CODE = 0;

    private Process curProcess = Process.REG_GETREQ;

    protected Handler workHandler = null;
    private HandlerThread ht = null;

    // 认证类型
    private IFAAAuthTypeEnum ifaaAuthType = IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;
    // 用户需要提供给IFAA的基本信息类
    private IFAABaseInfo ifaaBaseInfo = null;
    private List<IFAAAuthTypeEnum> supportBIOTypes = null;

    private String strInfo = "";
    private String responResult;

    private IFAAFaceManager.AuthenticatorCallback regCallback = new IFAAFaceManager.AuthenticatorCallback() {
//        @Override
//        public void onStatus(int status) {
//            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(SettingFingerTypeActivity.this, status);
//        }
//
//        @Override
//        public void onResult(final AuthenticatorResponse response) {
//            String data = response.getData();
//            curProcess = Process.REG_SENDRESP;
//            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
//                  IFAAFingerprintOpenAPI.getInstance().sendIFAARegResponeAsyn(SettingFingerTypeActivity.this,data, secData, callback);
//                //IFAAFingerprintOpenAPI.getInstance().sendRegResponeAsyn(data, secData, callback);
//            } else {
//            	SettingFingerTypeActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_REGFAIL).show("注册指纹失败");
//                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(SettingFingerTypeActivity.this, 0);
//                    }
//                });
//            }
//        }
    };

    private IFAAFaceManager.AuthenticatorCallback deregCallback = new IFAAFaceManager.AuthenticatorCallback() {
//        @Override
//        public void onStatus(int status) {
//            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(SettingFingerTypeActivity.this, status);
//
//        }
//
//        @Override
//        public void onResult(final AuthenticatorResponse response) {
//            String data = response.getData();
//            SettingFingerTypeActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
//                        new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_DEREGSUCCESS).show(null);
//                        saveToken("");
//                        isInited = false;
//                        handler.sendEmptyMessage(MSG_REGENABLE);
//                    } else {
//                        new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_DEREGFAIL).show("注销指纹失败");
//                    }
//                }
//            });
//        }
    };

//    private IFAAFingerprintOpenAPI.Callback callback = new IFAAFingerprintOpenAPI.Callback() {
//        @Override
//        public void onCompeleted(int status, final String info) {
//            switch (curProcess) {
//                case REG_GETREQ:
//                    SettingFingerTypeActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (info.equals("isReg")) {
//                                saveToken(IFAAFingerprintOpenAPI.getInstance().getToken());
//                                token = IFAAFingerprintOpenAPI.getInstance().getToken();
//                                handler.sendEmptyMessage(MSG_REGDISABLE);
//                            } else if (info.equals("init")) {
//                                saveToken("");
//                                isInited = true;
//                                handler.sendEmptyMessage(MSG_REGENABLE);
//                            } else {
//                                startFPActivity(false);
//                                AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_REGISTER_REQUEST, 2);
//                                requestMessage.setData(info);
//                                LaunchActivity.authenticator.process(requestMessage, regCallback);
//                            }
//
//                        }
//                    });
//                    break;
//
//                case REG_SENDRESP:
//                    SettingFingerTypeActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (info.equals("OK")) {//"Success"
//                                new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_REGSUCCESS).show("");
//                                saveToken(IFAAFingerprintOpenAPI.getInstance().getToken());
//                                token = IFAAFingerprintOpenAPI.getInstance().getToken();
//                                isInited = false;
//                                handler.sendEmptyMessage(MSG_REGDISABLE);
//                            } else {
//                                new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_REGFAIL).show("ifaa注册指纹失败");
//                            }
//                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(SettingFingerTypeActivity.this, 0);
//                        }
//                    });
//                    break;
//
//                case DEREG_GETREQ:
//                    SettingFingerTypeActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_DEREGISTER_REQUEST, 2);
//                            requestMessage.setData(info);
//                            LaunchActivity.authenticator.process(requestMessage, deregCallback);
//
//                        }
//                    });
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_setting_finger_type);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("指纹免密");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        //token = getToken();
//        if (null == LaunchActivity.authenticator)
//            LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);

        //if(!token.isEmpty()){
        //IFAAFingerprintOpenAPI.getInstance().setToken(token);
        // }

        supportBIOTypes = getSupportBIOTypes(getApplicationContext());

        mAccountDao = new AccountDao(SettingFingerTypeActivity.this);
        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SettingFingerTypeActivity.this.finish();
            }
        });

        Intent intent = getIntent();
        if (null != intent.getExtras())
            m_log = intent.getExtras().getBoolean("fingerType");

        group_temo = (RadioGroup) findViewById(R.id.radioGroup1);

        // 改变默认选项
        if (m_log)
            group_temo.check(R.id.radio0);
        else
            group_temo.check(R.id.radio1);

        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        // 获取默认被被选中值
        checkRadioButton = (RadioButton) group_temo.findViewById(group_temo.getCheckedRadioButtonId());
        // 注册事件
        group_temo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 点击事件获取的选择对象
                checkRadioButton = (RadioButton) group_temo.findViewById(checkedId);
                if (checkedId == R.id.radio0)
                    m_log = true;
                else
                    m_log = false;
            }
        });

        ImageView mRegActBtn = (ImageView) findViewById(R.id.edit);
        mRegActBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Editor editor = sharedPrefs.edit();
                if (m_log) {
                    sdkReg(ifaaBaseInfo);
                    //regIFAA(ifaaBaseInfo);
                    //reg(ifaaBaseInfo);
                    //regIFAARegRequest(true);
					/*Intent intent = new Intent();
					intent.setClass(SettingFingerTypeActivity.this,com.sheca.finger.MainActivity.class);
					startActivity(intent);
					*/
                } else {
                    //unregIFAA(ifaaBaseInfo);
                    showProgDlg(" IFAA注销中...");
                    sdkUnReg(ifaaBaseInfo);
                    //deRegIFAARequest();
                }
            }
        });

        isInited = true;
        //regIFAARegRequest(false);
        initData();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_REGDISABLE:
                        setIFAADisable(isInited);
                        break;
                    case MSG_REGENABLE:
                        isInited = false;
                        setIFAAEnable(isInited);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // app 需要提供给ifaa 一些基本信息
        ifaaBaseInfo = new IFAABaseInfo(this);
        // 认证类型，默认为指纹 AUTHTYPE_FINGERPRINT
        ifaaBaseInfo.setAuthType(ifaaAuthType);
        // 业务 ID, 请保持唯一，记录在ifaa log 中，当出问题时候，查问题用。
        ifaaBaseInfo.setTransactionID("transId");
        // 业务的附加信息，记录在ifaa log 中，当出问题时，查问题用。
        ifaaBaseInfo.setTransactionPayload("transPayload");
        ifaaBaseInfo.setTransactionType("Login");
        // TODO 用户id, 此值参与ifaa 业务以及 token 的生成，务必保证其唯一，可以传入用户名的hash值来脱敏。
        ifaaBaseInfo.setUserID(/* "user1"*/ getPWDHash(mAccountDao.getLoginAccount().getName()));
        // 设置使用SDK提供的指纹弹框页面
        ifaaBaseInfo.usingDefaultAuthUI("移证通认证口令", "");
        // 设置 IFAA 服务器的url 地址，默认为一砂测试服务器。
        final String ifaaURL = SettingFingerTypeActivity.this.getString(R.string.UMSP_Service_IFAA);
        //ifaaBaseInfo.setUrl(ifaaURL);

        //设置指纹框认证次数，类方法
        //ETASManager.setAuthNumber(3);
        sdkCheckIfaaStatus(ifaaBaseInfo);
        //checkIfaaStatusIFAA(ifaaBaseInfo);
        //checkIfaaStatus(ifaaBaseInfo);
    }

//    private void checkIfaaStatusIFAA(final IFAABaseInfo ifaaBaseInfo) {
//        final EtasStatus etasStatus = new EtasStatus(ifaaBaseInfo);
//        final EtasResult etasResult = etasStatus.checkStatusInit();
//        if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_REGISTERED) {
//            setIFAADisable(true);
//            //tvShowInfos.append("IFAA已经注册了\n");
//            //ifaaSwitch.setChecked(true);
//            return;
//        }
//
//        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//            //tvShowInfos.append("检查注册状态初始化出错了:" + etasResult.getMsg() + "\n");
//            Toast.makeText(SettingFingerTypeActivity.this, "检查注册状态初始化出错了:" + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        new MyAsycnTaks() {
//            @Override
//            public void preTask() {
//                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//                strInfo = String.format("%s=%s&%s=%s",
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
//                        URLEncoder.encode(mTokenId),
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
//                        URLEncoder.encode(etasResult.getMsg()));
//
//            }
//
//            @Override
//            public void doinBack() {
//                UniTrust mUnitTrust = new UniTrust(SettingFingerTypeActivity.this, false);
//                responResult = mUnitTrust.IFAACheckStatus(strInfo);
//            }
//
//            @Override
//            public void postTask() {
//                final APPResponse response = new APPResponse(responResult);
//                int resultStr = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//                if (0 == resultStr) {
//                    checkIfaaStatus(ifaaBaseInfo);
//                } else {
//                    //Toast.makeText(SettingFingerTypeActivity.this, "查询注册状态错误:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
//                }
//            }
//        }.execute();
//
//    }

    /**
     * SDK直连 检查ifaa 注册状态
     */
    private void sdkCheckIfaaStatus(final IFAABaseInfo ifaaBaseInfo) {

        final EDISAuthManager manager = new EDISAuthManager(ifaaBaseInfo);
        // 发送请求到APPServer获取初始化数据
        // POST 请求
        ExcecuteObservable excecuteObservable = new ExcecuteObservable(CommonConst.ESANDCLOUD_DEV_SERVER_URL + "/init");
        excecuteObservable.excecute("")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingFingerTypeActivity.this, "查询注册状态错误 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                        MyLog.error("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String msg) {

                        MyLog.debug("msg: " + msg);

                        manager.checkStatus(msg, new IFAACallback() {
                            @Override
                            public void onStatus(AuthStatusCode authStatusCode) {

                            }

                            @Override
                            public void onResult(IFAAResult IFAAResult) {

                                MyLog.debug("检查注册状态: " + IFAAResult.getCode());
                                MyLog.debug("检查注册状态: " + IFAAResult.getMsg());

                                if (IFAACommon.IFAA_STATUS_REGISTERED.equals(IFAAResult.getCode())) {


                                    if (IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT == ifaaBaseInfo.getAuthType()) {
                                        setIFAADisable(true);
                                    } else {
                                        setIFAADisable(true);
                                    }


                                } else if (IFAACommon.IFAA_STATUS_DELETED.equals(IFAAResult.getCode())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT == ifaaBaseInfo.getAuthType()) {
                                                Toast.makeText(SettingFingerTypeActivity.this, "此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用\n", Toast.LENGTH_SHORT).show();
                                                //updateTextView("此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用\n");
                                            } else {
                                                Toast.makeText(SettingFingerTypeActivity.this, "此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用\n", Toast.LENGTH_SHORT).show();
                                                //updateTextView("此手机不支持多指位，并且注册的那个指位已经被删除，需要注销后在注册方能使用\n");
                                            }
                                        }
                                    });


                                } else {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT == ifaaBaseInfo.getAuthType()) {
//                                                //setIFAADisable(false);
//                                                Toast.makeText(SettingFingerTypeActivity.this, "IFAA指纹未注册", Toast.LENGTH_SHORT).show();
//
//                                            } else {
//                                                //setIFAADisable(false);
//                                                Toast.makeText(SettingFingerTypeActivity.this, "IFAA人脸未注册", Toast.LENGTH_SHORT).show();
//
//                                            }
//                                        }
//                                    });


                                }
                            }
                        });

                    }
                });


    }


//    /**
//     * 检查ifaa 注册状态
//     */
//    private void checkIfaaStatus(IFAABaseInfo ifaaBaseInfo) {
//
//        do {
//
//            // 如果本地 token 为空，那么检查服务器端是否已经注册了
//            final EtasStatus etasStatus = new EtasStatus(ifaaBaseInfo);
//            EtasResult etasResult = etasStatus.checkStatusInit();
//            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_REGISTERED) {
//                setIFAADisable(true);
//                //tvShowInfos.append("IFAA已经注册了\n");
//                //ifaaSwitch.setChecked(true);
//                break;
//            }
//
//            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                //tvShowInfos.append("检查注册状态初始化出错了:" + etasResult.getMsg() + "\n");
//                Toast.makeText(SettingFingerTypeActivity.this, "检查注册状态初始化出错了:" + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                break;
//            }
//
//            // 发送报文到业务服务器，插叙服务器端的注册状态
//            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
//            etasExcecuteObservable.excecute(etasResult.getMsg())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Subscriber<String>() {
//
//                        @Override
//                        public void onCompleted() {
//
////                        tvShownfos.append("查询注册状态服务器请求流程结束\n");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            Toast.makeText(SettingFingerTypeActivity.this, "查询注册状态错误 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            //tvShowInfos.append("查询注册状态错误 ： " + e.getMessage() + "\n");
//                            //ifaaSwitch.setChecked(false);
//                        }
//
//                        @Override
//                        public void onNext(String msg) {
//
//                            // 服务器正确返回啦
//                            do {
//
//                                // 解析服务器返回的报文数据
//                                EtasResult etasResult = etasStatus.parseResult(msg);
//                                // 服务器尚未注册或者出错
//                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                                    Toast.makeText(SettingFingerTypeActivity.this, etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                                    //tvShowInfos.append(etasResult.getMsg() + "\n");
//                                    //ifaaSwitch.setChecked(false);
//                                    break;
//                                }
//
//                                // 到这一步说明服务器已经注册啦，现在检查本地是否注册了， etasResult.getMsg() 的数据为注册的 token 数据
//                                etasResult = etasStatus.checkLocalStatus(etasResult.getMsg());
//                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                                    Toast.makeText(SettingFingerTypeActivity.this, "本地尚未注册/出错 ： " + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                                    //tvShowInfos.append("本地尚未注册/出错 ： " + etasResult.getMsg() + "\n");
//                                    //ifaaSwitch.setChecked(false);
//                                    break;
//                                }
//
//                                Toast.makeText(SettingFingerTypeActivity.this, "IFAA 已经注册", Toast.LENGTH_SHORT).show();
//                                setIFAADisable(true);
//                                //tvShowInfos.append("IFAA 已经注册\n");
//                                //ifaaSwitch.setChecked(true);
//                            } while (false);
//                        }
//                    });
//        } while (false);
//    }

//    private void regIFAA(final IFAABaseInfo info) {
//        final IFAABaseInfo ifaaBaseInfo = info;
//        final EtasRegister etasRegister = new EtasRegister(ifaaBaseInfo);
//
//        // 注册初始化
//        final EtasResult etasResult = etasRegister.regInit();
//        // 如果操作失败，辣么结束流程
//        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//            Toast.makeText(SettingFingerTypeActivity.this, "注册初始化失败 :" + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//            //tvShowInfos.append("注册初始化失败 ： " + etasResult.getMsg() + '\n');
//            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {
//
//                //此时可引导用户录入指纹后在做注册操作
//                Toast.makeText(SettingFingerTypeActivity.this, "该手机未录入指纹或人脸", Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
//
//        new MyAsycnTaks() {
//            @Override
//            public void preTask() {
//                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//                strInfo = String.format("%s=%s&%s=%s",
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
//                        URLEncoder.encode(mTokenId),
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
//                        URLEncoder.encode(etasResult.getMsg()));
//
//            }
//
//            @Override
//            public void doinBack() {
//                UniTrust mUnitTrust = new UniTrust(SettingFingerTypeActivity.this, false);
//                responResult = mUnitTrust.IFAARegister(strInfo);
//            }
//
//            @Override
//            public void postTask() {
//                final APPResponse response = new APPResponse(responResult);
//                int resultStr = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//                if (0 == resultStr) {
//                    showProgDlg(" IFAA注册初始化中...");
//                    reg(ifaaBaseInfo);
//                } else {
//                    Toast.makeText(SettingFingerTypeActivity.this, "注册初始化失败:" + resultStr + "," + retMsg, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }.execute();
//
//    }


    /**
     * SDK 直连 ifaa 注册
     */
    private void sdkReg(final IFAABaseInfo ifaaBaseInfo) {
        final EDISAuthManager manager = new EDISAuthManager(ifaaBaseInfo);

        // 发送请求到APPServer获取初始化数据
        // POST 请求
        ExcecuteObservable excecuteObservable = new ExcecuteObservable(CommonConst.ESANDCLOUD_DEV_SERVER_URL + "/init");
        excecuteObservable.excecute("")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        MyLog.error("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String msg) {

                        MyLog.debug("msg: " + msg);

                        manager.reg(msg, new IFAACallback() {
                            @Override
                            public void onStatus(AuthStatusCode authStatusCode) {

                            }

                            @Override
                            public void onResult(IFAAResult IFAAResult) {


                                if (IFAACommon.IFAA_SUCCESS.equals(IFAAResult.getCode())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT == ifaaBaseInfo.getAuthType()) {
                                                Toast.makeText(SettingFingerTypeActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                                setIFAADisable(false);

                                            } else {
                                                Toast.makeText(SettingFingerTypeActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                                setIFAADisable(false);
                                            }
                                        }
                                    });

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT == ifaaBaseInfo.getAuthType()) {
                                                MyLog.debug("指纹注册失败");
                                                Toast.makeText(SettingFingerTypeActivity.this, "注册失败：" + IFAAResult.getMsg(), Toast.LENGTH_SHORT).show();
                                                setIFAAEnable(true);
//                                        updateSwitch(false);
//                                        updateTextView("指纹注册失败,code: " + IFAAResult.getCode() + ",msg: " + IFAAResult.getMsg() + "\n");
                                            } else {
                                                MyLog.debug("人脸注册失败");
                                                Toast.makeText(SettingFingerTypeActivity.this, "注册失败：" + IFAAResult.getMsg(), Toast.LENGTH_SHORT).show();
                                                setIFAAEnable(true);
//                                        updateTextView("人脸注册失败,code: " + IFAAResult.getCode() + ",msg: " + IFAAResult.getMsg() + "\n");
//                                        updateSwitch2(false);
                                            }
                                        }
                                    });


                                }

                            }
                        });

                    }
                });
    }


//    /**
//     * ifaa 注册
//     */
//    private void reg(IfaaBaseInfo info) {
//
//        final IfaaBaseInfo ifaaBaseInfo = info;
//        final EtasRegister etasRegister = new EtasRegister(ifaaBaseInfo);
//
//        do {
//
//            // 注册初始化
//            EtasResult etasResult = etasRegister.regInit();
//            // 如果操作失败，辣么结束流程
//            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                closeProgDlg();
//                Toast.makeText(SettingFingerTypeActivity.this, "注册初始化失败 :" + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                //tvShowInfos.append("注册初始化失败 ： " + etasResult.getMsg() + '\n');
//                if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {
//
//                    //此时可引导用户录入指纹后在做注册操作
//
//                }
//                break;
//            }
//
//            // 发起注册请求操作
//            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
//            etasExcecuteObservable.excecute(etasResult.getMsg())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .flatMap(new Func1<String, Observable<String>>() {
//
//                        @Override
//                        public Observable<String> call(final String msg) { // 执行注册请求操作
//
//                            final Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
//
//                                @Override
//                                public void call(Subscriber<? super String> subscriber) {
//
//                                    final Subscriber<? super String> mSubscriber = subscriber;
//                                    // 服务器数据已经返回，执行本地注册操作
//
//                                    etasRegister.register(msg, new EtasAuthenticatorCallback() {
//                                        @Override
//                                        public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {
//
//                                            // 不是运行在ui 线程，所以不能在此更新界面
////                                        updateTextView("指纹认证返回状态 ： " + authStatusCode + "\n");
//                                        }
//
//                                        @Override
//                                        public void onResult(EtasResult etasResult) {
//                                            MyLog.error("注册 onResult：" + etasResult.getCode());
//                                            //updateTextView("注册 onResult：" + etasResult.getCode() + "\n");
//
//                                            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//
//                                                // 不是运行在ui 线程，所以不能在此更新界面;
//                                                mSubscriber.onError(new Exception(etasResult.getMsg()));
//                                                //updateTextView("注册失败 ： " + etasResult.getMsg() + "\n");
//                                            } else {
//
//                                                mSubscriber.onNext(etasResult.getMsg());
//                                            }
//                                        }
//                                    });
//                                }
//                            });
//
//                            return observable;
//                        }
//
//                    })
//                    .flatMap(new Func1<String, Observable<String>>() {
//
//                        @Override
//                        public Observable<String> call(String msg) {
//
//                            // 把注册信息同步到服务器
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
////                        tvShowInfos.append("注册请求流程结束\n");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            closeProgDlg();
//                            etasRegister.sendAuthStatusCodeComplete();
//                            Toast.makeText(SettingFingerTypeActivity.this, "注册请求失败 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            //tvShowInfos.append("注册请求失败 ： " + e.getMessage() + "\n");
//                        }
//
//                        @Override
//                        public void onNext(String msg) {
//
//                            // 告知 sdk, 注册流程已经结束
//                            EtasResult etasResult = etasRegister.regFinish(msg);
//                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                                //closeProgDlg();
//                                Toast.makeText(SettingFingerTypeActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
//                                setIFAADisable(false);
//                                //tvShowInfos.append("注册成功 ：）\n");
//                                //ifaaSwitch.setChecked(true);
//                            } else {
//                                //closeProgDlg();
//                                Toast.makeText(SettingFingerTypeActivity.this, "注册失败 :" + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                                setIFAAEnable(true);
//                                //tvShowInfos.append("注册失败 :(\n" + etasResult.getMsg());
//                                //ifaaSwitch.setChecked(false);
//                            }
//                        }
//                    });
//        } while (false);
//    }

//    private void unregIFAA(final IfaaBaseInfo ifaaBaseInfo) {
//        final EtasDeregister ifaaDeregister = new EtasDeregister(ifaaBaseInfo);
//        // 注册初始化
//        final EtasResult etasResult = ifaaDeregister.deregInit();
//        // 如果操作失败，辣么结束流程
//        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//            Toast.makeText(SettingFingerTypeActivity.this, "注销初始化失败 ： " + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//            //tvShowInfos.append("注销初始化失败 ： " + etasResult.getMsg() + '\n');
//            return;
//        }
//
//        new MyAsycnTaks() {
//            @Override
//            public void preTask() {
//                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//                strInfo = String.format("%s=%s&%s=%s",
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
//                        URLEncoder.encode(mTokenId),
//                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
//                        URLEncoder.encode(etasResult.getMsg()));
//
//            }
//
//            @Override
//            public void doinBack() {
//                UniTrust mUnitTrust = new UniTrust(SettingFingerTypeActivity.this, false);
//                responResult = mUnitTrust.IFAADeRegister(strInfo);
//            }
//
//            @Override
//            public void postTask() {
//                final APPResponse response = new APPResponse(responResult);
//                int resultStr = response.getReturnCode();
//                final String retMsg = response.getReturnMsg();
//                if (0 == resultStr) {
//                    unreg(ifaaBaseInfo);
//                } else {
//                    Toast.makeText(SettingFingerTypeActivity.this, "注销初始化失败:" + resultStr + "," + retMsg, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }.execute();
//
//    }
//
//    /**
//     * ifaa 注销
//     */
//    private void unreg(final IfaaBaseInfo ifaaBaseInfo) {
//
//        do {
//
//            final EtasDeregister ifaaDeregister = new EtasDeregister(ifaaBaseInfo);
//            // 注册初始化
//            EtasResult etasResult = ifaaDeregister.deregInit();
//            // 如果操作失败，辣么结束流程
//            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                closeProgDlg();
//                Toast.makeText(SettingFingerTypeActivity.this, "注销初始化失败 ： " + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                //tvShowInfos.append("注销初始化失败 ： " + etasResult.getMsg() + '\n');
//                break;
//            }
//
//            // 发起注销请求操作
//            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
//            etasExcecuteObservable.excecute(etasResult.getMsg())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<String>() {
//                        @Override
//                        public void onCompleted() {
//
////                            tvShowInfos.append("注销流程结束\n");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                            //tvShowInfos.append("注销失败 ： " + e.getMessage() + "\n");
//                        }
//
//                        @Override
//                        public void onNext(String msg) {
//
//                            // 执行本地注销操作
//                            ifaaDeregister.dereg(msg, new EtasAuthenticatorCallback() {
//
//                                @Override
//                                public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {
//
//                                    // 此处回调函数没用
//                                }
//
//                                @Override
//                                public void onResult(EtasResult etasResult) {
//
//                                    // 不在 UI 线程中
//                                    if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
//                                        closeProgDlg();
//                                        Toast.makeText(SettingFingerTypeActivity.this, "本地注销失败 ： " + etasResult.getMsg(), Toast.LENGTH_SHORT).show();
//                                        // 此处不是运行在 UI 进程, 不能直接更新 UI
//                                        //updateSwitch(true);
//                                        //updateTextView("本地注销失败 ： " + etasResult.getMsg() + "\n");
//                                    } else {
//                                        handler.sendEmptyMessage(MSG_REGENABLE);
//
//                                        //setIFAAEnable(true);
//                                        // 此处不是运行在 UI 进程, 不能直接更新 UI
//                                        //updateSwitch(false);
//                                        //updateTextView("注销成功 ：)\n");
//                                    }
//                                }
//                            });
//                        }
//                    });
//        } while (false);
//    }

    /**
     * SDK直连模式下 ifaa注销
     */
    private void sdkUnReg(final IFAABaseInfo ifaaBaseInfo) {
        final EDISAuthManager manager = new EDISAuthManager(ifaaBaseInfo);

        // 发送请求到APPServer获取初始化数据
        // POST 请求
        ExcecuteObservable excecuteObservable = new ExcecuteObservable(CommonConst.ESANDCLOUD_DEV_SERVER_URL + "/init");
        excecuteObservable.excecute("")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        MyLog.error("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String msg) {


                        MyLog.debug("msg: " + msg);


                        manager.unReg(msg, new IFAACallback() {
                            @Override
                            public void onStatus(AuthStatusCode authStatusCode) {

                            }

                            @Override
                            public void onResult(IFAAResult IFAAResult) {

                                if (IFAACommon.IFAA_SUCCESS.equals(IFAAResult.getCode())) {


                                    if (IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()) {
                                        handler.sendEmptyMessage(MSG_REGENABLE);
//                                        MyLog.debug("人脸注销成功");
//                                        updateSwitch2(false);
//                                        updateTextView("人脸注销成功");
                                    } else {
                                        handler.sendEmptyMessage(MSG_REGENABLE);
//                                        MyLog.debug("指纹注销成功");
//                                        updateSwitch(false);
//                                        updateTextView("指纹注销成功");
                                    }

                                } else {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()) {
//                                        MyLog.debug("人脸注销失败");
//                                        updateSwitch2(true);
//                                        updateTextView("人脸注销失败,code: " + IFAAResult.getCode() + ",msg: " + IFAAResult.getMsg() + "\n");
                                                closeProgDlg();
                                                Toast.makeText(SettingFingerTypeActivity.this, "本地注销失败 ： " + IFAAResult.getMsg(), Toast.LENGTH_SHORT).show();

                                            } else {

//                                        MyLog.debug("指纹注销失败");
//                                        updateSwitch(true);
//                                        updateTextView("指纹注销失败,code: " + IFAAResult.getCode() + ",msg: " + IFAAResult.getMsg() + "\n");
                                                closeProgDlg();
                                                Toast.makeText(SettingFingerTypeActivity.this, "本地注销失败 ： " + IFAAResult.getMsg(), Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });


                                }
                            }
                        });

                    }
                });

    }


    private void setIFAADisable(boolean isInit) {
        closeProgDlg();
        Editor editor = sharedPrefs.edit();
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        editor.putBoolean(mUserName + CommonConst.SETTINGS_FINGER_ENABLED, true);      //使用指纹模块
        editor.commit();

        LaunchActivity.isIFAAFingerOpend = true;
        editor.putBoolean(CommonConst.SETTINGS_FINGER_OPENED, true);      //开启指纹模块
        editor.commit();

        group_temo = (RadioGroup) findViewById(R.id.radioGroup1);
        checkRadioButton = (RadioButton) group_temo.findViewById(R.id.radio0);
        checkRadioButton.setEnabled(false);

        m_log = false;
        group_temo.check(R.id.radio1);

        if (!isInit)
            SettingFingerTypeActivity.this.finish();
    }

    private void setIFAAEnable(boolean isInit) {
        closeProgDlg();
        Toast.makeText(SettingFingerTypeActivity.this, "注销成功", Toast.LENGTH_SHORT).show();
        Editor editor = sharedPrefs.edit();
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        editor.putBoolean(mUserName + CommonConst.SETTINGS_FINGER_ENABLED, false);    //不使用指纹模块
        editor.commit();

        LaunchActivity.isIFAAFingerOpend = false;
        editor.putBoolean(CommonConst.SETTINGS_FINGER_OPENED, false);      //关闭指纹模块
        editor.commit();

        group_temo = (RadioGroup) findViewById(R.id.radioGroup1);
        checkRadioButton = (RadioButton) group_temo.findViewById(R.id.radio0);
        checkRadioButton.setEnabled(true);

        if (!isInit)
            SettingFingerTypeActivity.this.finish();
    }


//    public void startFPActivity(boolean isAuthenticate) {
//        Intent intent = new Intent();
////	        if (isAuthenticate) {
////	            intent.putExtra(AuthenticatorMessage.KEY_OPERATIONT_TYPE,
////	                    AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST);
////	        }
//        intent.setClass(this, FingerPrintAuthSettingFingerTypeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);
//
//        // this.startActivityForResult(intent, FINGER_CODE);
//    }

//    private void regIFAARegRequest(boolean regFlag) {
//        curProcess = Process.REG_GETREQ;
//        String info = AuthenticatorManager.getAuthData(SettingFingerTypeActivity.this, mAccountDao.getLoginAccount().getName());
//        String deviceId = LaunchActivity.authenticator.getDeviceId();
//        IFAAFingerprintOpenAPI.getInstance().getIFAARegRequestAsyn(SettingFingerTypeActivity.this, mAccountDao.getLoginAccount().getName(), info, deviceId, regFlag, callback);
//        //IFAAFingerprintOpenAPI.getInstance().getRegRequestAsyn(info, callback);
//        secData = info;
//    }
//
//    private void deRegIFAARequest() {
//        curProcess = Process.DEREG_GETREQ;
//        String info = AuthenticatorManager.getAuthData(SettingFingerTypeActivity.this, mAccountDao.getLoginAccount().getName());
//        IFAAFingerprintOpenAPI.getInstance().getIFAADeregRequestAsyn(SettingFingerTypeActivity.this, mAccountDao.getLoginAccount().getName(), info, callback);
//        // IFAAFingerprintOpenAPI.getInstance().getDeregRequestAsyn(info, callback);
//        secData = info;
//
//    }

    private final String TOKENFILE = "user";
    private final String KEY_TOKEN = "token";

    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(TOKENFILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.commit();

    }

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(TOKENFILE, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, "");
    }


    /**
     * @author qiyi.wxc
     * @version $Id: FingerprintBroadcastUtil.java, v 0.1 2015年12月14日 下午7:44:55 qiyi.wxc Exp $
     */
    public static class FingerprintBroadcastUtil {

        //The is the broadcast for update UI status
        public final static String BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_ACTION";
        public final static String FINGERPRINTSENSOR_STATUS_VALUE = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_VALUE";

        //Send the UI Status of the FingerPrint Result and Change the UI
        public static void sendIdentifyStatusChangeMessage(Context context, int resultCode) {
            Intent broadcastIntent = new Intent(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            broadcastIntent.putExtra(FINGERPRINTSENSOR_STATUS_VALUE, resultCode);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        public static IntentFilter getIdentifyChangeBroadcastFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            return filter;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FINGER_CODE) {
            if (resultCode == MainActivity.RESULT_OK) {
                new FingerPrintToast(SettingFingerTypeActivity.this, FingerPrintToast.ST_REGSUCCESS).show("input pwd");
            }
            if (resultCode == MainActivity.RESULT_CANCELED) {
                //new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGSUCCESS).show("cancel");
                //MainActivity.this.finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                SettingFingerTypeActivity.this.finish();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }

    private void changeProgDlg(String strMsg) {
        if (progDialog.isShowing()) {
            progDialog.setMessage(strMsg);
        }
    }

    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }


}
