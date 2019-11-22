package com.esandinfo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.esandinfo.etas.ETASManager;
import com.esandinfo.etas.EtasResult;
import com.esandinfo.etas.IfaaBaseInfo;
import com.esandinfo.etas.IfaaCommon;
import com.esandinfo.etas.biz.EtasAuthentication;
import com.esandinfo.etas.biz.EtasAuthenticatorCallback;
import com.esandinfo.etas.biz.EtasDeregister;
import com.esandinfo.etas.biz.EtasRegister;
import com.esandinfo.etas.biz.EtasStatus;
import com.esandinfo.etas.biz.EtasTemplateUpdater;
import com.esandinfo.utils.EtasExcecuteObservable;
import com.esandinfo.utils.MyLog;
import com.esandinfo.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.sheca.umandroid.R;

public class IfaaRxjavaDemoActivity extends Activity implements View.OnClickListener, View.OnTouchListener, AdapterView.OnItemSelectedListener {

    // 检测机器是否支持IFAA 按钮
    private Button btIsSupportIFAA = null;
    // 检测IFAA注册状态 按钮
    private Button btIfaaCheckStatus = null;
    // 退出此Activity 按钮
    private Button btExit = null;
    // 发起指纹认证按钮
    protected Button btAuth = null;
    // 更新 IFAA 服务器地址按钮
    private Button btUpdateIfaaUrl = null;
    // 获取APPID 按钮，appid 是唯一标志apk的id, IFAA用于是被数据来源用。
    private Button btGetAppID = null;
    // 开通和注销IFAA
    protected Switch ifaaSwitch = null;
    // 显示执行结果
    protected TextView tvShowInfos = null;
    protected ScrollView scrollView = null;
    // IFAA 服务器 url
    private EditText etUrl = null;
    private TextView tvClean = null;
    // 认证类型
    private IfaaBaseInfo.IFAAAuthTypeEnum ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;
    // 用户需要提供给IFAA的基本信息类
    private IfaaBaseInfo ifaaBaseInfo = null;
    // 子线程更新界面用
    private Handler handler;
    // 用于避免 switch 控件在短时间内被被多次点击导致异常
    long lastClickTime = 0;
    private final int UPDATE_TEXT_VIEW = 0;
    private final int UPDATE_SWITCH = 1;
    private EditText mEtTransactionType;
    private EditText mEtUserId;
    private EditText mEtTransactionId;
    private EditText mEtTransactionPayload;
    private List<IfaaBaseInfo.IFAAAuthTypeEnum> supportBIOTypes = null;

    /**
     * 权限获取
     */
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
    };

    /**
     * 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
     */
    private List<String> mPermissionList = new ArrayList<>();
    private Spinner mSpinnerAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ifaa);
        // 显示 SDK 版本
       // this.setTitle(ETASManager.getVersion());

        supportBIOTypes = ETASManager.getSupportBIOTypes(getApplicationContext());
        // 获取控件
        getWidgets();
        // 设置控件
        setWidgets();

        if (supportBIOTypes.contains(IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE)) {
            getPermissions();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // app 需要提供给ifaa 一些基本信息
        ifaaBaseInfo = new IfaaBaseInfo(this);
        // 认证类型，默认为指纹 AUTHTYPE_FINGERPRINT
        ifaaBaseInfo.setAuthType(ifaaAuthType);
        // 业务 ID, 请保持唯一，记录在ifaa log 中，当出问题时候，查问题用。
        ifaaBaseInfo.setTransactionID(mEtTransactionId.getText().toString());
        // 业务的附加信息，记录在ifaa log 中，当出问题时，查问题用。
        ifaaBaseInfo.setTransactionPayload(mEtTransactionPayload.getText().toString());
        ifaaBaseInfo.setTransactionType(mEtTransactionType.getText().toString());
        // TODO 用户id, 此值参与ifaa 业务以及 token 的生成，务必保证其唯一，可以传入用户名的hash值来脱敏。
        ifaaBaseInfo.setUserID(mEtUserId.getText().toString());
        // 设置使用SDK提供的指纹弹框页面
        ifaaBaseInfo.usingDefaultAuthUI("移证通指纹认证", null);
        // 设置 IFAA 服务器的url 地址，默认为一砂测试服务器。
        ifaaBaseInfo.setUrl(Utils.getIfaaUrl(this.getApplicationContext()));

        //设置指纹框认证次数，类方法
        ETASManager.setAuthNumber(3);

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == UPDATE_TEXT_VIEW) {

                    tvShowInfos.append(msg.obj + "\n");
                } else if (msg.what == UPDATE_SWITCH) {

                    ifaaSwitch.setChecked((Boolean) msg.obj);
                }
            }
        };

        if (!ETASManager.isSupportIFAA(this, ifaaAuthType)) {
            // 如果手机不支持 IFAA , 那么不需要在做其他操作。
            ifaaSwitch.setEnabled(false);
            btAuth.setEnabled(false);
            tvShowInfos.append("此设备不支持该认证类型：" + ifaaAuthType + "\n" );
        } else {
            tvShowInfos.append("此设备支持该认证类型：" + ifaaAuthType + "\n" );
            ifaaSwitch.setEnabled(true);
            btAuth.setEnabled(true);
            // 检查注册状态，更新 UI
            checkIfaaStatus(ifaaBaseInfo);
        }

    }

    // 更新 UI 进程界面
    public void updateTextView(String messageStr) {

        Message message = new Message();
        message.obj = messageStr;
        message.what = UPDATE_TEXT_VIEW;
        handler.sendMessage(message);
    }

    // 更新 UI 进程界面
    public void updateSwitch(boolean isCheck) {

        Message message = new Message();
        message.what = UPDATE_SWITCH;
        message.obj = isCheck;
        handler.sendMessage(message);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == btIsSupportIFAA.getId()) { // 点击是否支持 IFAA 按钮

            // 检查本机是否支持IFAA
            List<IfaaBaseInfo.IFAAAuthTypeEnum> supportBIOTypesList = ETASManager.getSupportBIOTypes(getApplicationContext());

            if (supportBIOTypesList.size() == 0) {

                tvShowInfos.append("此设备不支持IFAA :(\n");
            } else {

                for (IfaaBaseInfo.IFAAAuthTypeEnum supportBIOType : supportBIOTypesList) {

                    if (supportBIOType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT) {

                        tvShowInfos.append("支持指纹\n");
                    }

                    if (supportBIOType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_IRIS) {

                        tvShowInfos.append("支持虹膜\n");
                    }

                    if (supportBIOType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {

                        tvShowInfos.append("支持人脸\n");
                    }

                    if (supportBIOType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_HARDWARE_IC) {

                        tvShowInfos.append("支持硬件IC\n");
                    }

                    if (supportBIOType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_IN_SCREEN_FINGERPRINT) {

                        tvShowInfos.append("支持屏下指纹\n");
                    }
                }
            }
        } else if (view.getId() == btIfaaCheckStatus.getId()) { // 点击查询注册状态按钮

            // 执行状态查询操作
            checkIfaaStatus(ifaaBaseInfo);
        } else if (view.getId() == btAuth.getId()) { // 点击认证按钮

            // 执行 IFAA 认证操作
            auth(ifaaBaseInfo);
        } else if (view.getId() == btExit.getId()) { // 点击退出按钮

            // 退出Activity
            this.finish();
        } else if (view.getId() == btUpdateIfaaUrl.getId()) { // 点击更新ifaa 服务器 url 按钮

            // 更新 url 地址
            Utils.saveIfaaUrl(this.getApplicationContext(), etUrl.getText().toString());
            ifaaBaseInfo.setUrl(Utils.getIfaaUrl(this.getApplicationContext()));
            // 显示更新  toast
            Toast.makeText(this.getApplicationContext(), "url 更新成功 !!  url = " + etUrl.getText(), Toast.LENGTH_SHORT).show();
            initData();
        } else if (view.getId() == btGetAppID.getId()) { // 点击获取 appID 按钮

            // 获取并显示 appid
            tvShowInfos.append(ETASManager.getApplicationID(this.getApplicationContext()) + "\n");
        } else if (view.getId() == tvClean.getId()) { // 点击清除 tvShowInfos 的内容

            tvShowInfos.setText("");
        }
    }

    /**
     * 获取控件
     */
    private void getWidgets() {

        btIfaaCheckStatus = (Button) findViewById(R.id.btIfaaCheckStatus);
        btIsSupportIFAA = (Button) findViewById(R.id.btIsSupportIFAA);
        btExit = (Button) findViewById(R.id.btExit);
        btAuth = (Button) findViewById(R.id.btAuth);
        btUpdateIfaaUrl = (Button) findViewById(R.id.btUpdateIfaaUrl);
        btGetAppID = (Button) findViewById(R.id.btGetAppID);
        ifaaSwitch = (Switch) findViewById(R.id.ifaaSwitch);
        tvShowInfos = (TextView) findViewById(R.id.tvShowInfos);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        etUrl = (EditText) findViewById(R.id.etUrl);
        tvClean = (TextView) findViewById(R.id.tvClean);

        mEtUserId = (EditText) findViewById(R.id.et_user_id);
        mEtTransactionId = (EditText) findViewById(R.id.et_transaction_id);
        mEtTransactionPayload = (EditText) findViewById(R.id.et_transaction_payload);

        mEtTransactionType = (EditText) findViewById(R.id.et_transaction_type);

        mSpinnerAuth = (Spinner) findViewById(R.id.spinner_auth);

        //数据
        List authTypeDataList = new ArrayList<String>();
        authTypeDataList.add("指纹认证");
        authTypeDataList.add("人脸认证");
        //适配器
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, authTypeDataList);
        //设置样式
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        mSpinnerAuth.setAdapter(arrayAdapter);

    }

    /**
     * 设置控件
     */
    private void setWidgets() {

        btIfaaCheckStatus.setOnClickListener(this);
        btIsSupportIFAA.setOnClickListener(this);
        btExit.setOnClickListener(this);
        btAuth.setOnClickListener(this);
        btUpdateIfaaUrl.setOnClickListener(this);
        btGetAppID.setOnClickListener(this);
        ifaaSwitch.setOnTouchListener(this);
        etUrl.setText(Utils.getIfaaUrl(this));
        tvClean.setOnClickListener(this);

        mSpinnerAuth.setOnItemSelectedListener(this);

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        etUrl.setMaxLines(1);
        etUrl.setMaxWidth(width - 200);
        etUrl.setSingleLine();
    }

    /**
     * switch 点击事件
     *
     * @param view
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        Switch sw = (Switch) view;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            return true;
        }

        // 避免switch 在短时间内被连续多次点击
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if ((currentTime - lastClickTime) < 1000) {

            return false;
        }
        lastClickTime = currentTime;

        if (sw.getId() == ifaaSwitch.getId()) {

            if (sw.isChecked()) {

                // 如果从打开转换成关闭状态，那么执行注销操作
                unreg(ifaaBaseInfo);
            } else {

                // 如果从关闭转换成打开状态，执行IFAA注册操作
                // TODO IFAA 注册是一个比较敏感的操作，建议新验证用户身份(比如验证校验码或者验证密码)后再做此操作。
                final EditText dialog_et = new EditText(this);
                dialog_et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                // 验证密码
                new AlertDialog.Builder(this)
                        .setTitle("请输入密码：(123)")
                        .setView(dialog_et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog_et.getText().toString().compareTo("123") == 0) {

                                    tvShowInfos.append("密码验证成功\n");
                                    reg(ifaaBaseInfo);
                                } else {

                                    ifaaSwitch.setChecked(false);
                                    tvShowInfos.append("密码输入错误\n");
                                }

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                tvShowInfos.append("已取消登录\n");
                            }
                        })
                        .show();

            }
        }

        return true;
    }

    /**
     * 检查ifaa 注册状态
     */
    private void checkIfaaStatus(IfaaBaseInfo ifaaBaseInfo) {

        do {

            // 如果本地 token 为空，那么检查服务器端是否已经注册了
            final EtasStatus etasStatus = new EtasStatus(ifaaBaseInfo);
            EtasResult etasResult = etasStatus.checkStatusInit();
            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_REGISTERED) {

                tvShowInfos.append("IFAA已经注册了\n");
                ifaaSwitch.setChecked(true);
                break;
            }

            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {
                tvShowInfos.append("检查注册状态初始化出错了:" + etasResult.getMsg() + "\n");
                break;
            }

            // 发送报文到业务服务器，插叙服务器端的注册状态
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            etasExcecuteObservable.excecute(etasResult.getMsg())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {

                        @Override
                        public void onCompleted() {

//                        tvShownfos.append("查询注册状态服务器请求流程结束\n");
                        }

                        @Override
                        public void onError(Throwable e) {

                            tvShowInfos.append("查询注册状态错误 ： " + e.getMessage() + "\n");
                            ifaaSwitch.setChecked(false);
                        }

                        @Override
                        public void onNext(String msg) {

                            // 服务器正确返回啦
                            do {

                                // 解析服务器返回的报文数据
                                EtasResult etasResult = etasStatus.parseResult(msg);
                                // 服务器尚未注册或者出错
                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                    tvShowInfos.append(etasResult.getMsg() + "\n");
                                    ifaaSwitch.setChecked(false);
                                    break;
                                }

                                // 到这一步说明服务器已经注册啦，现在检查本地是否注册了， etasResult.getMsg() 的数据为注册的 token 数据
                                etasResult = etasStatus.checkLocalStatus(etasResult.getMsg());
                                if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                    tvShowInfos.append("本地尚未注册/出错 ： " + etasResult.getMsg() + "\n");
                                    ifaaSwitch.setChecked(false);
                                    break;
                                }

                                tvShowInfos.append("IFAA 已经注册\n");
                                ifaaSwitch.setChecked(true);
                            } while (false);
                        }
                    });
        } while (false);
    }

    /**
     * ifaa 注册
     */
    private void reg(IfaaBaseInfo info) {

        final IfaaBaseInfo ifaaBaseInfo = info;
        final EtasRegister etasRegister = new EtasRegister(ifaaBaseInfo);

        do {

            // 注册初始化
            EtasResult etasResult = etasRegister.regInit();
            // 如果操作失败，辣么结束流程
            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                tvShowInfos.append("注册初始化失败 ： " + etasResult.getMsg() + '\n');
                if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {

                    // 此时可引导用户录入指纹后在做注册操作

                }
                break;
            }

            // 发起注册请求操作
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            etasExcecuteObservable.excecute(etasResult.getMsg())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<String, Observable<String>>() {

                        @Override
                        public Observable<String> call(final String msg) { // 执行注册请求操作

                            final Observable observable = Observable.create(new Observable.OnSubscribe<String>() {

                                @Override
                                public void call(Subscriber<? super String> subscriber) {

                                    final Subscriber<? super String> mSubscriber = subscriber;
                                    // 服务器数据已经返回，执行本地注册操作
                                    etasRegister.register(msg, new EtasAuthenticatorCallback() {
                                        @Override
                                        public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {

                                            // 不是运行在ui 线程，所以不能在此更新界面
//                                        updateTextView("指纹认证返回状态 ： " + authStatusCode + "\n");
                                        }

                                        @Override
                                        public void onResult(EtasResult etasResult) {
                                            MyLog.error("注册 onResult：" + etasResult.getCode());
                                            updateTextView("注册 onResult：" + etasResult.getCode() + "\n");

                                            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                                // 不是运行在ui 线程，所以不能在此更新界面;
                                                mSubscriber.onError(new Exception(etasResult.getMsg()));
                                                updateTextView("注册失败 ： " + etasResult.getMsg() + "\n");
                                            } else {

                                                mSubscriber.onNext(etasResult.getMsg());
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

                            // 把注册信息同步到服务器
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

//                        tvShowInfos.append("注册请求流程结束\n");
                        }

                        @Override
                        public void onError(Throwable e) {
                            etasRegister.sendAuthStatusCodeComplete();
                            tvShowInfos.append("注册请求失败 ： " + e.getMessage() + "\n");
                        }

                        @Override
                        public void onNext(String msg) {

                            // 告知 sdk, 注册流程已经结束
                            EtasResult etasResult = etasRegister.regFinish(msg);
                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                tvShowInfos.append("注册成功 ：）\n");
                                ifaaSwitch.setChecked(true);
                            } else {

                                tvShowInfos.append("注册失败 :(\n" + etasResult.getMsg());
                                ifaaSwitch.setChecked(false);
                            }
                        }
                    });
        } while (false);
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

                tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");

                if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {

                    // TODO 此时可引导用户录入指纹/人脸后在做认证操作

                }
                break;
            }
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            etasExcecuteObservable.excecute(etasResult.getMsg())
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
                                                updateTextView("认证 onResult：" + etasResult.getCode() + "\n");
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
                                                        updateTextView(msg);

                                                    } else {

                                                        // 不是运行在ui 线程，所以不能在此更新界面;
                                                        updateTextView("认证失败 ： " + etasResult.getMsg());
                                                    }

                                                } else {

                                                    subscriber.onNext(etasResult.getMsg());
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
                            updateTextView("认证请求失败 ： " + e.getMessage() + "\n");
                        }

                        @Override
                        public void onNext(String msg) {

                            // 告知 sdk, 注册流程已经结束
                            EtasResult etasResult = etasAuthentication.authFinish(msg);
                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                updateTextView("认证成功 ：）\n");
                                ifaaSwitch.setChecked(true);
                            } else if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.WRONG_AUTHDATAINDEX) { // 指位不匹配，此处可以选择是否更新指位

                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                                    updateTextView("即将更新人脸 ：)\n");
                                }else{
                                    updateTextView("即将更新指位 ：)\n");
                                }

                                onTemplateMismatch(ifaaBaseInfo, etasResult.getMsg());
                            } else {

                                tvShowInfos.append("认证失败 :(\n" + etasResult.getMsg());
                                ifaaSwitch.setChecked(false);
                            }
                        }
                    });
        } while (false);
    }

    public void onTemplateMismatch(final IfaaBaseInfo ifaaBaseInfo, final String ifaaMessage) {

        // 验证密码
        final Context context = ifaaBaseInfo.getContext();
        final EditText dialog_et = new EditText(context);
        dialog_et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                new AlertDialog.Builder(context)
                        .setTitle("请输入密码：(123)")
                        .setView(dialog_et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog_et.getText().toString().compareTo("123") == 0) {

                                    updateTextView("密码正确\n");
                                    // 执行指位更新操作。
                                    templateUpdate(ifaaBaseInfo, ifaaMessage);
                                } else {

                                    updateTextView("密码错误\n");
                                    updateTextView("密码认证失败\n");
                                }

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                                    updateTextView("人脸更新已取消\n");
                                }else{
                                    updateTextView("指位更新已取消\n");
                                }
                            }
                        })
                        .show();
            }
        });

    }

    private void templateUpdate(final IfaaBaseInfo ifaaBaseInfo, String ifaaMessage) {

        do {

            final EtasTemplateUpdater etasTemplateUpdater = new EtasTemplateUpdater(ifaaBaseInfo);
            // 认证初始化
            final EtasResult etasResult = etasTemplateUpdater.templateUpdaInit(ifaaMessage);
            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                    tvShowInfos.append("人脸更新初始化失败 ： " + etasResult.getMsg() + "\n");
                }else{
                    tvShowInfos.append("指位更新初始化失败 ： " + etasResult.getMsg() + "\n");
                }

                break;
            }

            // 把需要更新的指位信息同步到服务器
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            etasExcecuteObservable.excecute(etasResult.getMsg())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {

                        @Override
                        public void onCompleted() {

//                            tvShowInfos.append("指位更新初始化失败流程结束");
                        }

                        @Override
                        public void onError(Throwable e) {


                            if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                                tvShowInfos.append("人脸更新操作出错 ： " + e.getMessage());
                            }else{
                                tvShowInfos.append("指位更新操作出错 ： " + e.getMessage());
                            }
                        }

                        @Override
                        public void onNext(String message) {

                            // 服务器正常返回，完成指位更新操作
                            EtasResult etasResult = etasTemplateUpdater.templateUpdaFinish(message);
                            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                                    tvShowInfos.append("人脸更新操作成功 :)\n");
                                }else{
                                    tvShowInfos.append("指位更新操作成功 :)\n");
                                }

                            } else {

                                if (IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE == ifaaBaseInfo.getAuthType()){
                                    tvShowInfos.append("人脸更新操作出错 ： " + etasResult.getMsg() + "\n");
                                }else{
                                    tvShowInfos.append("指位更新操作出错 ： " + etasResult.getMsg() + "\n");
                                }
                            }
                        }
                    });

        } while (false);
    }

    /**
     * ifaa 注销
     */
    private void unreg(final IfaaBaseInfo ifaaBaseInfo) {

        do {

            final EtasDeregister ifaaDeregister = new EtasDeregister(ifaaBaseInfo);
            // 注册初始化
            EtasResult etasResult = ifaaDeregister.deregInit();
            // 如果操作失败，辣么结束流程
            if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                tvShowInfos.append("注销初始化失败 ： " + etasResult.getMsg() + '\n');
                break;
            }

            // 发起注销请求操作
            EtasExcecuteObservable etasExcecuteObservable = new EtasExcecuteObservable(ifaaBaseInfo.getUrl());
            etasExcecuteObservable.excecute(etasResult.getMsg())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {

//                            tvShowInfos.append("注销流程结束\n");
                        }

                        @Override
                        public void onError(Throwable e) {

                            tvShowInfos.append("注销失败 ： " + e.getMessage() + "\n");
                        }

                        @Override
                        public void onNext(String msg) {

                            // 执行本地注销操作
                            ifaaDeregister.dereg(msg, new EtasAuthenticatorCallback() {

                                @Override
                                public void onStatus(IfaaCommon.AuthStatusCode authStatusCode) {

                                    // 此处回调函数没用
                                }

                                @Override
                                public void onResult(EtasResult etasResult) {

                                    // 不在 UI 线程中
                                    if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

                                        // 此处不是运行在 UI 进程, 不能直接更新 UI
                                        updateSwitch(true);
                                        updateTextView("本地注销失败 ： " + etasResult.getMsg() + "\n");
                                    } else {

                                        // 此处不是运行在 UI 进程, 不能直接更新 UI
                                        updateSwitch(false);
                                        updateTextView("注销成功 ：)\n");
                                    }
                                }
                            });
                        }
                    });
        } while (false);
    }

    /**
     * 获取权限
     */
    private void getPermissions() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {
            //未授予的权限为空，表示都授予了

        } else {
            //请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                //这个是权限拒绝
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    String s = permissions[i];
                    Toast.makeText(this, s + "权限被拒绝了，请手动添加", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0) {
            // 指纹认证
            ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;
            initData();
        } else if (position == 1) {
            // 人脸认证
            ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE;
            initData();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
