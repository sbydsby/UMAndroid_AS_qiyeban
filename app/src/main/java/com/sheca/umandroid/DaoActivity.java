package com.sheca.umandroid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.esandinfo.utils.EtasExcecuteObservable;
import com.esandinfo.utils.MyLog;
import com.esandinfo.utils.Utils;
import com.excelsecu.slotapi.EsIBankDevice;
import com.ifaa.sdk.api.AuthenticatorManager;
import com.ifaa.sdk.auth.AuthenticatorCallback;
import com.ifaa.sdk.auth.message.AuthenticatorMessage;
import com.ifaa.sdk.auth.message.AuthenticatorResponse;
import com.junyufr.szt.activity.ResultActivity;
import com.sheca.fingerui.FingerPrintAuthDaoActivity;
import com.sheca.fingerui.FingerPrintToast;
import com.sheca.fingerui.IFAAFingerprintOpenAPI;
import com.sheca.fingerui.MainActivity.Process;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;
import com.sheca.umandroid.adapter.CertAdapter;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.AppInfoDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.AppInfo;
import com.sheca.umandroid.model.AppInfoEx;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.LogUtil;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.PKIUtil;
import com.sheca.umandroid.util.SealSignUtil;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class DaoActivity extends Activity {
    public static String strResult = "";      //返回结果
    public static String strServiecNo = "";   //业务流水号
    public static String strAppName = "";     //第三方APP的名称
    public static String strCertSN = "";      //证书序列号
    public static String strCertType = "";    //证书类型
    public static String strSaveType = "";    //证书保存类型
    public static String strContainerID = ""; //证书容器ID
    public static String strPwd = "";         //下载证书密码
    public static String strAccountName = ""; //账户注册名称
    public static String strAccountPwd = "";  //账户注册口令
    public static String strMsgWrapper = "";  //待签名数据的包装器
    public static String strAPPID = "";       //APP应用唯一标识
    public static boolean bCreated = false;    //是否已创建界面
    public static boolean bCreditAPP = false;  //是否由信用APP调用
    public static boolean bManualChecked = false;  //是否已申请人工审核

    public static boolean bUploadRecord = true;  //是否徐上传使用记录

    private String strSign = "";        //签名数据
    private String strCert = "";        //base64证书
    private int mCertId = -1;        //当前选中证书ID
    private String strUniCodeID = "";   //用户身份证号码
    private boolean mIsViewCert = false; //是否可查看证书详情

    private javasafeengine jse = null;
    private List<Map<String, String>> mData = null;
    private AlertDialog certListDialog = null;
    private CertDao certDao = null;
    private LogDao mLogDao = null;
    private AccountDao mAccountDao = null;
    private AppInfoDao mAppInfoDao = null;

    private JShcaEsStd gEsDev = null;
    private JShcaUcmStd gUcmSdk = null;
    //private  JShcaKsStd gKsSdk = null;
    protected Handler workHandler = null;
    private HandlerThread ht = null;

    private final static int LOGIN_SIGN_FAILURE = 1;
    private final static int LOGIN_SIGN_SUCCESS = 2;

    private final int LOG_TYPE_LOGIN = 1;
    private final int LOG_TYPE_SIGN = 2;
    private final int LOG_TYPE_SIGNEX = 3;
    private final int LOG_TYPE_ENVELOP_DECRYPT = 4;
    private final int LOG_TYPE_SEAL = 5;

    public static int operateState = 0;   //操作状态
    private int resState = 1;           //处理结果状态 (0:成功;1:失败)
    private boolean bChecked = false;       //是否已人脸识别
    private boolean bScanDao = false;       //是否通过扫码登录进入
    private boolean bScanSDKDao = false;    //是否通过第三方扫码登录进入
    private boolean isJSONDate = false;     //是否第三方扫码签名数据json格式
    private boolean isSignEx = false;       //是否扫码批量签名

    private ProgressDialog progDialog = null;
    private SharedPreferences sharedPrefs;
    private boolean bCheckPremissoned = false;       //是否检测应用权限
    private boolean isNotificationGesture = false;

    private Process curProcess = Process.REG_GETREQ;
    private String userid = "test";
    private String secData = "";
    private boolean useGesture = false;
    private final int VALIDATE_GESTURE_CODE = 1;

    // 认证类型
    private IfaaBaseInfo.IFAAAuthTypeEnum ifaaAuthType = IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FINGERPRINT;
    // 用户需要提供给IFAA的基本信息类
    private IfaaBaseInfo ifaaBaseInfo = null;
    private List<IfaaBaseInfo.IFAAAuthTypeEnum> supportBIOTypes = null;
    private Handler ifaaHandler;
    private final int MSG_AUTHENABLE = 1;

    public static Boolean ifaaFaceAuth = false;
    private String strInfo = "";
    private String responResult;
    private int isSM2 = 0;

    private boolean isNewQrCode = false;

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private IFAAFingerprintOpenAPI.Callback callback = new IFAAFingerprintOpenAPI.Callback() {
        @Override
        public void onCompeleted(int status, final String info) {
            switch (curProcess) {
                case AUTH_GETREQ:
                    DaoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startFPActivity(false);
                            AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST, 2);
                            requestMessage.setData(info);
                            LaunchActivity.authenticator.process(requestMessage, authCallback);

                        }
                    });
                    break;
                case AUTH_SENDRESP:
                    DaoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.equals("OK")) {
                                new FingerPrintToast(DaoActivity.this, FingerPrintToast.ST_AUTHSUCCESS).show("");

                                if (LaunchActivity.isIFAAFingerOK) {
                                    if (operateState == LOG_TYPE_LOGIN)
                                        doFingerLogin();
                                    else if (operateState == LOG_TYPE_SIGN)
                                        doFingerSign();
                                    else if (operateState == LOG_TYPE_SIGNEX)
                                        doFingerSign();
                                    else if (operateState == LOG_TYPE_ENVELOP_DECRYPT)
                                        doFingerSign();
                                    else if (operateState == LOG_TYPE_SEAL)
                                        doFingerSign();
                                } else {
                                    if (operateState == LOG_TYPE_LOGIN) {
                                        if (LaunchActivity.failCount >= 3) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                        }
                                    } else if (operateState == LOG_TYPE_SIGN) {
                                        if (LaunchActivity.failCount >= 3) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                        }
                                    } else if (operateState == LOG_TYPE_SIGNEX) {
                                        if (LaunchActivity.failCount >= 3) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                        }
                                    } else if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                                        if (LaunchActivity.failCount >= 3) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                        }
                                    } else if (operateState == LOG_TYPE_SEAL) {
                                        if (LaunchActivity.failCount >= 3) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                        }
                                    }

                                }
                            } else {
                                new FingerPrintToast(DaoActivity.this, FingerPrintToast.ST_AUTHFAIL).show("验证指纹失败");
                            }
                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(DaoActivity.this, 0);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private AuthenticatorCallback authCallback = new AuthenticatorCallback() {
        @Override
        public void onStatus(int status) {
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(DaoActivity.this, status);
        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            curProcess = Process.AUTH_SENDRESP;

            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                IFAAFingerprintOpenAPI.getInstance().sendIFAAAuthResponeAsyn(DaoActivity.this, data, secData, callback);
            } else {
                DaoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new FingerPrintToast(DaoActivity.this, FingerPrintToast.ST_AUTHTEEFAIL).show("验证指纹失败");
                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(DaoActivity.this, 0);
                    }
                });
            }

        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Security.addProvider(new BouncyCastleProvider());     //导入bc包操作

        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        bCheckPremissoned = sharedPrefs.getBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, false);
        if (!bCheckPremissoned) {
            ActivityCompat.requestPermissions(DaoActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            bCheckPremissoned = true;

            Editor editor = sharedPrefs.edit();
            editor.putBoolean(CommonConst.SETTINGS_PREMISSION_ENABLED, bCheckPremissoned);
            editor.commit();
        }

        isNotificationGesture = sharedPrefs.getBoolean(CommonConst.SETTINGS_GESTURE_OPENED + LockPatternUtil.getActName(), false);
        LaunchActivity.isIFAAFingerOpend = sharedPrefs.getBoolean(CommonConst.SETTINGS_FINGER_OPENED, false);
        LaunchActivity.isIFAAFingerOK = false;
        if (LaunchActivity.isIFAAFingerOpend) {
            if (null == LaunchActivity.authenticator)
                LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);
        }
        supportBIOTypes = ETASManager.getSupportBIOTypes(getApplicationContext());

		/*
		try {
	        	if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
	    		    initShcaCciStdService();
	    } catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				ShcaCciStd.gSdk = null;
				//Toast.makeText(MainActivity.this,e1.getLocalizedMessage(),Toast.LENGTH_LONG).show();
		}*/

        gEsDev = JShcaEsStd.getIntence(DaoActivity.this);
        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(DaoActivity.this.getApplication(), DaoActivity.this);

        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        strPwd = "";
        bCreated = true;
        bCreditAPP = false;
        bManualChecked = false;
        Intent intent = getIntent();

        jse = new javasafeengine();

        mAccountDao = new AccountDao(DaoActivity.this);
        certDao = new CertDao(DaoActivity.this);
        mLogDao = new LogDao(DaoActivity.this);
        mAppInfoDao = new AppInfoDao(DaoActivity.this);


        if (LaunchActivity.LOG_FLAG) {
            if (null == LaunchActivity.logUtil) {
                LaunchActivity.logUtil = new LogUtil(DaoActivity.this, LaunchActivity.LOG_FLAG);   //是否记录日志
                LaunchActivity.logUtil.init();
            }
        }

        if (null != intent.getExtras().getString("ScanDao"))
            bScanDao = true;
        if (null != intent.getExtras().getString("ScanSDKDao"))
            bScanSDKDao = true;
        if (null != intent.getExtras().getString("IsJson"))
            isJSONDate = true;
        if (null != intent.getExtras().getString("IsSignEx"))
            isSignEx = true;
        if (null != intent.getExtras().getString("certhash") && !"".equals(intent.getExtras().getString("certhash"))) {
            strPwd = intent.getExtras().getString("certhash");
            LaunchActivity.isIFAAFingerOK = true;
        }


        isSM2 = intent.getExtras().getInt("isSM2", 0);//默认0 ，1过滤rsa 2过滤sm2

        isNewQrCode = intent.getExtras().getBoolean("isNewQrCode", false);

        operateState = Integer.parseInt(intent.getExtras().getString("OperateState"));
        strResult = intent.getExtras().getString("OriginInfo");
        strServiecNo = intent.getExtras().getString("ServiecNo");
        strAppName = intent.getExtras().getString("AppName");
        strAPPID = CommonConst.UM_APPID;

        if (strAppName.equals(CommonConst.CREDIT_APP_NAME)) {   //判断是否信用APP调用
            bCreditAPP = true;
            strAPPID = CommonConst.CREDIT_APP_ID;
        } else {
            bCreditAPP = false;
            if (strAppName.equals(CommonConst.UTEST_APP_NAME))     //判断是否UTest调用,兼容分旧版本UTest
                strAPPID = CommonConst.UTEST_APP_ID;
            else if (strAppName.equals(CommonConst.NETHELPER_APP_NAME))   //判断是否上网助手调用,兼容分旧版本上网助手
                strAPPID = CommonConst.NETHELPER_APP_ID;
            else if (strAppName.equals(CommonConst.SCAN_LOGIN_NAME))  //判断是否扫码登录
                strAPPID = "";
            else if (strAppName.equals(CommonConst.SCAN_SIGN_NAME))  //判断是否扫码签名
                strAPPID = "";
            else if (strAppName.equals(CommonConst.SCAN_SIGNEX_NAME))   //判断是否扫码批量签名
                strAPPID = "";
            else if (strAppName.equals(CommonConst.SCAN_ENVELOP_DECRYPT_NAME))   //判断是否扫码解密
                strAPPID = "";
            else if (strAppName.equals(CommonConst.SCAN_SEAL_NAME))   //判断是否扫码签章
                strAPPID = CommonConst.UM_APPID;
            else
                strAPPID = strAppName;
        }

//		if(!getAppInfo(strAPPID)){
//			Toast.makeText(DaoActivity.this,"应用未授权", Toast.LENGTH_SHORT).show();
//			onBackPressed();
//			return;
//		}

        if (operateState == LOG_TYPE_SIGN) {
            if (null != intent.getExtras().getString("CertSN"))
                strCertSN = intent.getExtras().getString("CertSN");

            strMsgWrapper = intent.getExtras().getString("MsgWrapper");
            if (null == strMsgWrapper)
                strMsgWrapper = "0";
            if ("".equals(strMsgWrapper))
                strMsgWrapper = "0";
        } else if (operateState == LOG_TYPE_SIGNEX) {
            if (null != intent.getExtras().getString("IsSignEx"))
                isSignEx = true;

            strMsgWrapper = intent.getExtras().getString("MsgWrapper");
            if (null == strMsgWrapper)
                strMsgWrapper = "0";
            if ("".equals(strMsgWrapper))
                strMsgWrapper = "0";
        } else if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
            if (null != intent.getExtras().getString("CertSN"))
                strCertSN = intent.getExtras().getString("CertSN");
        } else if (operateState == LOG_TYPE_SEAL) {
            if (null != intent.getExtras().getString("CertSN"))
                strCertSN = intent.getExtras().getString("CertSN");

            strMsgWrapper = intent.getExtras().getString("MsgWrapper");
            if (null == strMsgWrapper)
                strMsgWrapper = "0";
            if ("".equals(strMsgWrapper))
                strMsgWrapper = "0";
        }

        if (null != intent.getExtras().getString("AccountName"))
            strAccountName = intent.getExtras().getString("AccountName");
        if (null != intent.getExtras().getString("AccountPwd"))
            strAccountPwd = intent.getExtras().getString("AccountPwd");
		/*if(null != intent.getExtras().getString("AppID")){
			strAPPID = intent.getExtras().getString("AppID");
		}else{
			if(!bCreditAPP)
			   strAPPID =  CommonConst.UM_APPID ;
		}*/

        if (WebClientUtil.mCertChainList.size() == 0)
            getCertChain();

        if (mAccountDao.count() == 0) {   //用户未登录
            ShowLogin();
            return;
        }
		/*else{
			if(mAccountDao.getLoginAccount().getActive() == 0){
				Intent inet = new Intent(this, PasswordActivity.class);
				inet.putExtra("Account", mAccountDao.getLoginAccount().getName());
				inet.putExtra("message", "dao");
			    startActivity(inet);
			    //DaoActivity.this.finish();
			    return;
			}
		}
		*/

        checkNetConnected();

        if (ckeckLogin()) {//自动登录检测
            ShowLogin();
            return;
        } else {
//			try {
//				if(!getAppInfo(strAPPID)){
//						Toast.makeText(DaoActivity.this,"应用未授权", Toast.LENGTH_SHORT).show();
//						onBackPressed();
//						return;
//				}
//		    }catch (Exception e) {
//				 // TODO Auto-generated catch block
//				 //e.printStackTrace();
//				 Toast.makeText(DaoActivity.this,"应用未授权", Toast.LENGTH_SHORT).show();
//				 onBackPressed();
//				 return;
//			}

            initData();
            ifaaHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_AUTHENABLE:
                            ifaaAuth();
                            break;
                        default:
                            break;
                    }
                }
            };

            if (operateState == LOG_TYPE_SIGN) {
                setContentView(R.layout.activity_dao_sign_local);
                ShowSignDlg();
                if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                    findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                    ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");
                }
            } else if (operateState == LOG_TYPE_LOGIN) {
                setContentView(R.layout.activity_dao_login_local);
                ShowInitDlg();
                if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                    findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                    ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");
                }
            } else if (operateState == LOG_TYPE_SIGNEX) {
                setContentView(R.layout.activity_dao_sign_local);
                ShowSignDlg();
                if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                    findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                    ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");
                }
            } else if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                setContentView(R.layout.activity_dao_decrypt_local);
                ShowSignDlg();
                if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                    findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                    ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");
                }
            } else if (operateState == LOG_TYPE_SEAL) {
                setContentView(R.layout.activity_dao_sign_local);
                ShowSignDlg();
                if (ifaaAuthType == IfaaBaseInfo.IFAAAuthTypeEnum.AUTHTYPE_FACE) {
                    findViewById(R.id.finger_image).setBackgroundResource(R.drawable.face_auth_img);
                    ((TextView) findViewById(R.id.finger_txt)).setText("点击图片使用人脸识别或手势密码登录");
                }
            } else {
                setContentView(R.layout.activity_dao_login_internet);
                ShowLoginInternetDlg();
            }

            LaunchActivity.failCount = 0;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bManualChecked) {
            resState = 2;    //已提交人工审核,未下载证书

            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("ServiecNo", strServiecNo);
            bundle.putString("OriginInfo", strResult);
            bundle.putString("Sign", strSign);
            bundle.putString("Cert", strCert);
            bundle.putString("CertSN", strCertSN);
            bundle.putString("UniqueID", mAccountDao.getLoginAccount().getIdentityCode());
            bundle.putString("CertType", strCertType);
            bundle.putString("SaveType", strSaveType);
            bundle.putString("ContainerID", strContainerID);
            bundle.putString("AppID", mAccountDao.getLoginAccount().getAppIDInfo());
            bundle.putString("MsgWrapper", strMsgWrapper);
            bundle.putInt("Code", resState);
            resultIntent.putExtras(bundle);

            DaoActivity.this.setResult(RESULT_OK, resultIntent);
            DaoActivity.this.finish();
            return;
        }

        if (!bCreated) {
            if (mAccountDao.count() == 0) {   //用户未登录
                ShowLogin();
                return;
            }

            try {
                mData = getData();

                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                    if (mData.size() == 0) {
                        resState = 3;    //企业账号登录未下载证书

                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("ServiecNo", strServiecNo);
                        bundle.putString("OriginInfo", strResult);
                        bundle.putString("Sign", strSign);
                        bundle.putString("Cert", strCert);
                        bundle.putString("CertSN", strCertSN);
                        bundle.putString("UniqueID", "");
                        bundle.putString("CertType", "");
                        bundle.putString("SaveType", "");
                        bundle.putString("ContainerID", "");
                        bundle.putString("AppID", mAccountDao.getLoginAccount().getAppIDInfo());
                        bundle.putString("MsgWrapper", strMsgWrapper);
                        bundle.putInt("Code", resState);
                        resultIntent.putExtras(bundle);

                        DaoActivity.this.setResult(RESULT_OK, resultIntent);
                        DaoActivity.this.finish();
                        return;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                LaunchActivity.isIFAAFingerOK = false;

                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                    resState = 3;    //企业账号登录未下载证书

                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("ServiecNo", strServiecNo);
                    bundle.putString("OriginInfo", strResult);
                    bundle.putString("Sign", strSign);
                    bundle.putString("Cert", strCert);
                    bundle.putString("CertSN", strCertSN);
                    bundle.putString("UniqueID", "");
                    bundle.putString("CertType", "");
                    bundle.putString("SaveType", "");
                    bundle.putString("ContainerID", "");
                    bundle.putString("AppID", mAccountDao.getLoginAccount().getAppIDInfo());
                    bundle.putString("MsgWrapper", strMsgWrapper);
                    bundle.putInt("Code", resState);
                    resultIntent.putExtras(bundle);

                    DaoActivity.this.setResult(RESULT_OK, resultIntent);
                    DaoActivity.this.finish();
                    return;
                }
            }

            if (operateState == LOG_TYPE_SIGN) {
                setContentView(R.layout.activity_dao_sign_local);
                ShowSignDlg();
            } else if (operateState == LOG_TYPE_LOGIN) {
                setContentView(R.layout.activity_dao_login_local);
                ShowInitDlg();
            } else if (operateState == LOG_TYPE_SIGNEX) {
                setContentView(R.layout.activity_dao_sign_local);
                ShowSignDlg();
            } else if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                setContentView(R.layout.activity_dao_decrypt_local);
                ShowSignDlg();
            }
        }

    }


    public void onBackPressed() {
        showExitFrame();
    }

    private void showExitFrame() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();

        bundle.putInt("Code", 1);
        resultIntent.putExtras(bundle);

        DaoActivity.this.setResult(RESULT_CANCELED, resultIntent);
        DaoActivity.this.finish();
    }

    /**
     * 初始化数据
     */
    private void initData() {
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
        ifaaBaseInfo.usingDefaultAuthUI("移证通认证口令", "取消");
        // 设置 IFAA 服务器的url 地址，默认为一砂测试服务器。
        final String ifaaURL = DaoActivity.this.getString(R.string.UMSP_Service_IFAA);
        ifaaBaseInfo.setUrl(ifaaURL);

        //设置指纹框认证次数，类方法
        ETASManager.setAuthNumber(3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VALIDATE_GESTURE_CODE) {
            if (resultCode == DaoActivity.this.RESULT_OK) {
                LaunchActivity.isIFAAFingerOK = true;

                if (operateState == LOG_TYPE_LOGIN)
                    doFingerLogin();
                else if (operateState == LOG_TYPE_SIGN)
                    doFingerSign();
                else if (operateState == LOG_TYPE_SIGNEX)
                    doFingerSign();
                else if (operateState == LOG_TYPE_ENVELOP_DECRYPT)
                    doFingerSign();
                else if (operateState == LOG_TYPE_SEAL)
                    doFingerSign();
            }
            if (resultCode == DaoActivity.this.RESULT_CANCELED) {

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void ShowInitDlg() {
        findViewById(R.id.textAppLabel).setVisibility(RelativeLayout.VISIBLE);
        findViewById(R.id.textAppView).setVisibility(RelativeLayout.VISIBLE);

        final Handler handler = new Handler(DaoActivity.this.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //showProgDlg("获取数据中...");
                            if (!getAppInfoName(strAPPID)) {
                                if (!strAppName.equals(CommonConst.NETHELPER_APP_NAMEEX)) {
                                    if (null == mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", ""))) {
                                        getAppValid(strAPPID);
                                    } else {
                                        //closeProgDlg();
                                        strAppName = mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", "")).getName();
                                    }
                                } else {
                                    if (null == mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", ""))) {
                                        AppInfoEx appInfo = new AppInfoEx();
                                        appInfo.setAppidinfo(strAPPID.replace("-", ""));
                                        appInfo.setName(CommonConst.NETHELPER_APP_NAMEEX);
                                        mAppInfoDao.addAPPInfo(appInfo);

                                    }

                                    //closeProgDlg();
                                }
                            }

                            // closeProgDlg();
//                            findViewById(R.id.textCertView).getBackground().setAlpha(100);  //0~255透明度值
                            showCert();


                            Button okBtn = findViewById(R.id.btn_loign_ok);
                            okBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signByfinger=false;
                                    doSign();
                                }
                            });

                            ImageView backBtn = ((ImageView) findViewById(R.id.btn_loign_back));
                            backBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showExitFrame();
                                }
                            });

                            if (LaunchActivity.isIFAAFingerOpend) {
                                findViewById(R.id.relativelayoutPwdLabel2).setVisibility(RelativeLayout.GONE);
                            } else {
                                if (bCreditAPP)
                                    findViewById(R.id.relativelayoutPwdLabel2).setVisibility(RelativeLayout.VISIBLE);
                                else
                                    findViewById(R.id.relativelayoutPwdLabel2).setVisibility(RelativeLayout.GONE);
                            }

                            //if(bScanDao)
                            //strAppName = CommonConst.SCAN_APP_NAME;
                            //Toast.makeText(DaoActivity.this,strAppName, Toast.LENGTH_SHORT).show();
                            //((TextView)findViewById(R.id.textAppView)).setText(strAppName);
                            ((TextView) findViewById(R.id.textAppView)).setText(CommonConst.UM_APP_NAME_EX);  //名称暂为一证通
                            //closeProgDlg();
                            checkCert();
                            if (!("".equals(strPwd)))
                                doSign();

                            if (LaunchActivity.isIFAAFingerOpend) {
                                final Cert cert = certDao.getCertByID(mCertId);
                                if (null != cert) {
                                    if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                        LaunchActivity.isIFAAFingerOK = false;
                                        ((EditText) findViewById(R.id.textPwd)).setText("");
                                    } else {
                                        if ("".equals(cert.getCerthash())) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                            LaunchActivity.isIFAAFingerOK = false;
                                            ((EditText) findViewById(R.id.textPwd)).setText("");
                                        } else {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);

                                            showFingerCheck();
                                        }
                                    }

                                    ((ImageView) findViewById(R.id.pwdkeyboard)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                            LaunchActivity.isIFAAFingerOK = false;
                                            ((EditText) findViewById(R.id.textPwd)).setText("");
                                        }
                                    });

                                    ((ImageView) findViewById(R.id.finger_image)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showFingerCheck();
                                        }
                                    });

                                    ((TextView) findViewById(R.id.finger_txt)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showFingerCheck();
                                        }
                                    });
                                } else {
                                    findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                    findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                }
                            } else {
                                findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                            }

                            showGestureView();

                        }
                    });

                } catch (final Exception exc) {
                    exc.getLocalizedMessage();
                }
            }
        }).start();

    }

    private void ShowSignDlg() {
        findViewById(R.id.textAppLabel).setVisibility(RelativeLayout.VISIBLE);
        findViewById(R.id.textAppView).setVisibility(RelativeLayout.VISIBLE);

        final Handler handler = new Handler(DaoActivity.this.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // showProgDlg("获取数据中...");

                            if (!getAppInfoName(strAPPID)) {
                                if (!strAppName.equals(CommonConst.NETHELPER_APP_NAMEEX)) {
                                    if (null == mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", ""))) {
                                        getAppValid(strAPPID);
                                    } else {
                                        //closeProgDlg();
                                        strAppName = mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", "")).getName();
                                    }
                                } else {
                                    if (null == mAppInfoDao.getAppInfoByAppID(strAPPID.replace("-", ""))) {
                                        AppInfoEx appInfo = new AppInfoEx();
                                        appInfo.setAppidinfo(strAPPID.replace("-", ""));
                                        appInfo.setName(CommonConst.NETHELPER_APP_NAMEEX);
                                        mAppInfoDao.addAPPInfo(appInfo);

                                    }

                                    // closeProgDlg();
                                }
                            }

                            //closeProgDlg();

                            TextView list = ((TextView) findViewById(R.id.textOrgView));
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                list.setText(strResult);
                            } else {
                                // if(bScanSDKDao){
                                try {
                                    if (isSignEx) {
                                        String strViewMsg = "";
                                        strResult = strResult.substring(0, strResult.lastIndexOf(CommonConst.UM_SPLIT_STR));

                                        for (int i = 0; i < strResult.split(CommonConst.UM_SPLIT_STR).length; i++) {
                                            if (isJSONDate)
                                                strViewMsg += new String(strResult.split(CommonConst.UM_SPLIT_STR)[i]) + "\n";
                                            else
                                                strViewMsg += new String(Base64.decode(URLDecoder.decode(strResult.split(CommonConst.UM_SPLIT_STR)[i], "UTF-8"))) + "\n";
                                        }

                                        strViewMsg = strViewMsg.substring(0, strViewMsg.lastIndexOf("\n"));
                                        list.setText(strViewMsg);
                                    } else {
                                        if (isJSONDate)
                                            list.setText(new String(strResult));
                                        else
                                            list.setText(new String(Base64.decode(URLDecoder.decode(strResult, "UTF-8"))));
                                    }
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                // }else{
                                //list.setText(strResult);
                                //}
                            }

//                            findViewById(R.id.textOrgView).getBackground().setAlpha(100);  //0~255透明度值
//                            findViewById(R.id.textCertView).getBackground().setAlpha(100);  //0~255透明度值
                            showSignCert();

                            Button okBtn = findViewById(R.id.btn_ok);
                            okBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    doSign();
                                }
                            });

                            ImageView backBtn = ((ImageView) findViewById(R.id.btn_back));
                            backBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showExitFrame();
                                }
                            });

                            //if(bScanDao)
                            //strAppName = CommonConst.SCAN_APP_NAME;

                            ((TextView) findViewById(R.id.textAppView)).setText(CommonConst.UM_APP_NAME_EX);
                            checkCert();
                            if (!("".equals(strPwd)))
                                doSign();

                            if (LaunchActivity.isIFAAFingerOpend) {
                                final Cert cert = certDao.getCertByID(mCertId);
                                if (null != cert) {
                                    if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                        LaunchActivity.isIFAAFingerOK = false;
                                        ((EditText) findViewById(R.id.textPwd)).setText("");
                                    } else {
                                        if ("".equals(cert.getCerthash())) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                            LaunchActivity.isIFAAFingerOK = false;
                                            ((EditText) findViewById(R.id.textPwd)).setText("");
                                        } else {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);

                                            showFingerCheck();
                                        }
                                    }

                                    ((ImageView) findViewById(R.id.pwdkeyboard)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                                            LaunchActivity.isIFAAFingerOK = false;
                                            ((EditText) findViewById(R.id.textPwd)).setText("");
                                        }
                                    });

                                    ((ImageView) findViewById(R.id.finger_image)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showFingerCheck();
                                        }
                                    });

                                    ((TextView) findViewById(R.id.finger_txt)).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showFingerCheck();
                                        }
                                    });
                                } else {
                                    findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                    findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                                }
                            } else {
                                findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                            }

                            showGestureView();

                        }
                    });

                } catch (final Exception exc) {
                    exc.getLocalizedMessage();
                }

            }

        }).start();


    }

    private void ShowLoginInternetDlg() {
        Button okBtn = ((Button) findViewById(R.id.btn_loign_internet_ok));
        okBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCert();
            }
        });

    }

    private void ShowLogin() {
        Intent i = new Intent(DaoActivity.this, LoginActivity.class);
        i.putExtra("message", "dao");
        startActivity(i);
        //DaoActivity.this.finish();
    }

    private void showCert() {
        try {
            ImageView viewCertBtn = ((ImageView) findViewById(R.id.btnCertView));
            viewCertBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    selectCert(LOG_TYPE_LOGIN);
                }
            });

            //findViewById(R.id.btnCertView).getBackground().setAlpha(100);  //0~255透明度值

            mData = getData();
            if (mData.size() == 0) {
                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                    resState = 3;    //企业账号登录未下载证书

                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("ServiecNo", strServiecNo);
                    bundle.putString("OriginInfo", strResult);
                    bundle.putString("Sign", strSign);
                    bundle.putString("Cert", strCert);
                    bundle.putString("CertSN", strCertSN);
                    bundle.putString("UniqueID", "");
                    bundle.putString("CertType", "");
                    bundle.putString("SaveType", "");
                    bundle.putString("ContainerID", "");
                    bundle.putString("AppID", mAccountDao.getLoginAccount().getAppIDInfo());
                    bundle.putString("MsgWrapper", strMsgWrapper);
                    bundle.putInt("Code", resState);
                    resultIntent.putExtras(bundle);

                    DaoActivity.this.setResult(RESULT_OK, resultIntent);
                    DaoActivity.this.finish();
                    return;
                } else {
                    ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                    mIsViewCert = false;
                    return;
                }
            }

            viewCertBtn.setVisibility(RelativeLayout.VISIBLE);

            mCertId = Integer.valueOf(mData.get(0).get("id"));
            final Cert cert = certDao.getCertByID(mCertId);
            if (cert != null) {
                mIsViewCert = true;
                String certificate = cert.getCertificate();
                byte[] bCert = Base64.decode(certificate);
                String strBlank = "证书";
                String strCertName = jse.getCertDetail(17, bCert);
                if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
                    strCertName += CommonConst.CERT_SM2_NAME + strBlank;
                else
                    strCertName += CommonConst.CERT_RSA_NAME + strBlank;

                if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入证书密码");

                    if (LaunchActivity.isIFAAFingerOpend) {
                        if (!"".equals(cert.getCerthash())) {
                            EditText edit = (EditText) findViewById(R.id.textPwd);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                            edit.clearFocus();

                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
                        } else {
                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                            ((EditText) findViewById(R.id.textPwd)).requestFocus();
                            ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                            ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                        }
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                        ((EditText) findViewById(R.id.textPwd)).requestFocus();
                        ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                        ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                    }
                } else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙key密码");

                    if (LaunchActivity.isIFAAFingerOpend) {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    }

                    ((EditText) findViewById(R.id.textPwd)).requestFocus();
                    ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                    ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙sim卡密码");

                    if (LaunchActivity.isIFAAFingerOpend) {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    }

                    ((EditText) findViewById(R.id.textPwd)).requestFocus();
                    ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                    ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                }

                if (null == cert.getCertname()) {
                    ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                } else {
                    if (cert.getCertname().isEmpty())
                        ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                    else
                        ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
                }
            } else {
                ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                mIsViewCert = false;
            }

            ((TextView) findViewById(R.id.textCertView)).setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View view) {
                            viewCertDetail();
                        }
                    });

        } catch (Exception e) {
            Log.e(CommonConst.TAG, e.getMessage(), e);
            Toast.makeText(DaoActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.textCertView)).setText("无证书");
            mIsViewCert = false;
        }

    }

    private void showSignCert() {
        try {
            ImageView viewCertBtn = ((ImageView) findViewById(R.id.btnCertView));
            viewCertBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    selectCert(LOG_TYPE_SIGN);
                }
            });

            //findViewById(R.id.btnCertView).getBackground().setAlpha(100);  //0~255透明度值

            mData = getData();
            if (mData.size() == 0) {
                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                    resState = 3;    //企业账号登录未下载证书

                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("ServiecNo", strServiecNo);
                    bundle.putString("OriginInfo", strResult);
                    bundle.putString("Sign", strSign);
                    bundle.putString("Cert", strCert);
                    bundle.putString("CertSN", strCertSN);
                    bundle.putString("UniqueID", "");
                    bundle.putString("CertType", "");
                    bundle.putString("SaveType", "");
                    bundle.putString("ContainerID", "");
                    bundle.putString("AppID", mAccountDao.getLoginAccount().getAppIDInfo());
                    bundle.putString("MsgWrapper", strMsgWrapper);
                    bundle.putInt("Code", resState);
                    resultIntent.putExtras(bundle);

                    DaoActivity.this.setResult(RESULT_OK, resultIntent);
                    DaoActivity.this.finish();
                    return;
                } else {
                    ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                    mIsViewCert = false;
                    return;
                }
            }

            viewCertBtn.setVisibility(RelativeLayout.VISIBLE);

            if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {   //扫码解密根据certsn获取证书id
                if (!"".equals(strCertSN)) {
                    String certSNDecrypt = strCertSN.toLowerCase();
                    Cert certBySN = certDao.getCertByCertsn(certSNDecrypt, mAccountDao.getLoginAccount().getName());
                    if (null != certBySN)
                        mCertId = certBySN.getId();
                } else {
                    ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                    mIsViewCert = false;
                    return;
                }
            } else if (operateState == LOG_TYPE_SEAL) {
                viewCertBtn.setVisibility(View.GONE);
                if (!"".equals(strCertSN)) {
                    String certSNSeal = strCertSN.toLowerCase();
                    Cert certBySN = certDao.getCertByCertsn(certSNSeal, mAccountDao.getLoginAccount().getName());
                    if (null != certBySN)
                        mCertId = certBySN.getId();
                } else {
                    ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                    mIsViewCert = false;
                    return;
                }
            } else {
                mCertId = Integer.valueOf(mData.get(0).get("id"));
            }

            if (mCertId < 0) {
                ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                mIsViewCert = false;
                return;
            }

            final Cert cert = certDao.getCertByID(mCertId);
            if (cert != null) {
                mIsViewCert = true;
                String certificate = cert.getCertificate();
                byte[] bCert = Base64.decode(certificate);
                String strBlank = "证书";
                String strCertName = jse.getCertDetail(17, bCert);
                if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
                    strCertName += CommonConst.CERT_SM2_NAME + strBlank;
                else
                    strCertName += CommonConst.CERT_RSA_NAME + strBlank;

                if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入证书密码");

                    if (LaunchActivity.isIFAAFingerOpend) {
                        if (!"".equals(cert.getCerthash())) {
                            EditText edit = (EditText) findViewById(R.id.textPwd);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                            edit.clearFocus();

                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
                        } else {
                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                            ((EditText) findViewById(R.id.textPwd)).requestFocus();
                            ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                            ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                        }
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                        ((EditText) findViewById(R.id.textPwd)).requestFocus();
                        ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                        ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);
                    }
                } else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙key密码");

                    ((EditText) findViewById(R.id.textPwd)).requestFocus();
                    ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                    ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);

                    if (LaunchActivity.isIFAAFingerOpend) {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    }
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                    //strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
                    ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙蓝牙sim卡密码");

                    ((EditText) findViewById(R.id.textPwd)).requestFocus();
                    ((EditText) findViewById(R.id.textPwd)).setFocusable(true);
                    ((EditText) findViewById(R.id.textPwd)).setFocusableInTouchMode(true);

                    if (LaunchActivity.isIFAAFingerOpend) {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    }
                }

                if (null == cert.getCertname()) {
                    ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                } else {
                    if (cert.getCertname().isEmpty())
                        ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                    else
                        ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
                }
            } else {
                ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                mIsViewCert = false;
            }

            ((TextView) findViewById(R.id.textCertView)).setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View view) {
                            viewCertDetail();
                        }
                    });


        } catch (Exception e) {
            Log.e(CommonConst.TAG, e.getMessage(), e);
            //Toast.makeText(DaoActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.textCertView)).setText("无证书");
            mIsViewCert = false;
        }

    }

    private void showGestureView() {
        final Cert cert = certDao.getCertByID(mCertId);
        if (null != cert) {
            if (LaunchActivity.isIFAAFingerOpend) {
                if (isNotificationGesture) {
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                } else {
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                }

                findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                if (operateState == LOG_TYPE_LOGIN)
                    findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
                else
                    findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
            } else {
                if (isNotificationGesture) {
                    findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                    if (operateState == LOG_TYPE_LOGIN)
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
                    else
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.VISIBLE);

                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
                } else {
                    findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                    if (operateState == LOG_TYPE_LOGIN)
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                    else
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                }
            }

            ((ImageView) findViewById(R.id.pwdkeyboard)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                    if (operateState == LOG_TYPE_LOGIN)
                        findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                    else
                        findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);

                    LaunchActivity.isIFAAFingerOK = false;
                    ((EditText) findViewById(R.id.textPwd)).setText("");
                }
            });

            ((ImageView) findViewById(R.id.finger_image)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFingerCheck();
                }
            });

            ((ImageView) findViewById(R.id.gesture_image)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGestureCheck();
                }
            });

            ((TextView) findViewById(R.id.finger_txt)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LaunchActivity.isIFAAFingerOpend) {
                        if (!useGesture)
                            showFingerCheck();
                        else
                            showGestureCheck();
                    } else {
                        if (isNotificationGesture)
                            showGestureCheck();
                    }

                }
            });

            findViewById(R.id.relativelayoutGesture).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.gesture_image).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);

                    useGesture = true;
                }
            });

        } else {
            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
            if (operateState == LOG_TYPE_LOGIN)
                findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
            else
                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.relativelayoutGesture).setVisibility(RelativeLayout.GONE);
        }

    }

    private void selectCert(int opestate) {
        if (!mIsViewCert) {
            Toast.makeText(DaoActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
            return;
        }

        changeCert(opestate);
    }


    private void changeCert(final int opestate) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certListView = inflater.inflate(R.layout.certlist, null);
        ListView list = (ListView) certListView.findViewById(R.id.certlist);
        CertAdapter adapter = null;

        try {
            adapter = new CertAdapter(DaoActivity.this, mData);
            list.setAdapter(adapter);

            Builder builder = new Builder(DaoActivity.this);
            builder.setIcon(R.drawable.view);
            builder.setTitle("请选择证书");
            builder.setView(certListView);
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            certListDialog = builder.create();
            certListDialog.show();
        } catch (Exception e) {
            Log.e(CommonConst.TAG, e.getMessage(), e);
            Toast.makeText(DaoActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCertId = Integer.valueOf(mData.get(position).get("id"));
                final Cert cert = certDao.getCertByID(mCertId);
                try {
                    String certificate = cert.getCertificate();
                    byte[] bCert = Base64.decode(certificate);
                    String strBlank = "证书";
                    String strCertName = jse.getCertDetail(17, bCert);
                    if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
                        strCertName += CommonConst.CERT_SM2_NAME + strBlank;
                    else
                        strCertName += CommonConst.CERT_RSA_NAME + strBlank;

                    if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()) {
                        //strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
                        ((EditText) findViewById(R.id.textPwd)).setHint("请输入证书密码");
                    } else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                        ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙key密码");
                    } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                        ((EditText) findViewById(R.id.textPwd)).setHint("请输入蓝牙sim卡密码");
                    }

                    if (null == cert.getCertname()) {
                        ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                    } else {
                        if (cert.getCertname().isEmpty())
                            ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                        else
                            ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                    Toast.makeText(DaoActivity.this, "请确认蓝牙是否开启及设备是否正确连接", Toast.LENGTH_SHORT).show();

                    if (LaunchActivity.isIFAAFingerOpend) {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        if (opestate == LOG_TYPE_LOGIN)
                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        else if (opestate == LOG_TYPE_SIGN)
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        else
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);

                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        if (opestate == LOG_TYPE_LOGIN)
                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        else if (opestate == LOG_TYPE_SIGN)
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        else
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);

                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                    }

                    ((EditText) findViewById(R.id.textPwd)).setText("");
                    LaunchActivity.isIFAAFingerOK = false;
                } else {
                    if (LaunchActivity.isIFAAFingerOpend) {
                        if (!"".equals(cert.getCerthash())) {
                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.GONE);
                            if (opestate == LOG_TYPE_LOGIN)
                                findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.GONE);
                            else if (opestate == LOG_TYPE_SIGN)
                                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);
                            else
                                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.GONE);

                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.VISIBLE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.VISIBLE);
                        } else {
                            findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                            if (opestate == LOG_TYPE_LOGIN)
                                findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                            else if (opestate == LOG_TYPE_SIGN)
                                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                            else
                                findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);

                            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                            ((EditText) findViewById(R.id.textPwd)).setText("");
                            LaunchActivity.isIFAAFingerOK = false;
                        }
                    } else {
                        findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
                        if (opestate == LOG_TYPE_LOGIN)
                            findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
                        else if (opestate == LOG_TYPE_SIGN)
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
                        else
                            findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);

                        findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                        ((EditText) findViewById(R.id.textPwd)).setText("");
                        LaunchActivity.isIFAAFingerOK = false;
                    }
                }

                showGestureView();

                certListDialog.dismiss();
            }
        });

    }

    private void viewCertDetail() {
        if (!mIsViewCert) {
            Toast.makeText(DaoActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
            return;
        }
        Cert certView = certDao.getCertByID(mCertId);
        if (CommonConst.CERT_TYPE_RSA.equals(certView.getCerttype()))
            viewCert(mCertId);
        else if (CommonConst.CERT_TYPE_SM2.equals(certView.getCerttype()))
            viewSM2Cert(mCertId);
        else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(certView.getCerttype()))
            viewCert(mCertId);
        else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(certView.getCerttype()))
            viewSM2Cert(mCertId);
        else if (!certView.getCerttype().contains("SM2"))
            viewCert(mCertId);
        else if (certView.getCerttype().contains("SM2"))
            viewSM2Cert(mCertId);

    }


    private void doSign() {
        final SharedPreferences sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Handler handler = new Handler(DaoActivity.this.getMainLooper());

        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

//    	try {
//			if(!getAppInfo(strAPPID)){
//					Toast.makeText(DaoActivity.this,"应用未授权", Toast.LENGTH_SHORT).show();
//					onBackPressed();
//					return;
//			}
//	    } catch (Exception e) {
//			 // TODO Auto-generated catch block
//			 //e.printStackTrace();
//			 Toast.makeText(DaoActivity.this,"应用未授权", Toast.LENGTH_SHORT).show();
//			 onBackPressed();
//			 return;
//		}

        if (!mIsViewCert) {
            Toast.makeText(DaoActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CommUtil.isPasswordLocked(DaoActivity.this, mCertId)) {
            return;
        }

        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
        else
            strCertType = CommonConst.CERT_TYPE_RSA;

        final Cert cert = certDao.getCertByID(mCertId);
        byte[] bCert = Base64.decode(cert.getCertificate());
        String strCertName = "";
        try {
            strCertName = jse.getCertDetail(17, bCert);
        } catch (Exception ex) {
            strCertName = "";
        }

        String strIdentityName = mAccountDao.getLoginAccount().getIdentityName();

//        if (!"".equals(strIdentityName)) {
//            if (!strCertName.equals(strIdentityName)) {
//                Toast.makeText(DaoActivity.this, "证书信息与账户不匹配", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }

        if (verifyCert(cert, true)) {
            if (verifyDevice(cert, true)) {
                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                    showProgDlg("连接设备中...");

                workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                            if (!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))) {
                                if (!sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "").equals(cert.getDevicesn())) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgDlg();
                                            Toast.makeText(DaoActivity.this, "请确认蓝牙是否开启及设备是否正确连接", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            }

                            shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn());
                            if (null == devInfo) {
                                if (gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, cert.getDevicesn()) != 0) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgDlg();
                                            Toast.makeText(DaoActivity.this, "请确认蓝牙是否开启及设备是否正确连接", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                }
                            });
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                            if (!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))) {
                                if (!sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "").equals(cert.getDevicesn())) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgDlg();
                                            Toast.makeText(DaoActivity.this, "请确认蓝牙是否开启及设备是否正确连接", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            }

                            try {
                                if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
                                    ScanBlueToothSimActivity.gKsSdk.connect(cert.getDevicesn(), "778899", 500);
                            } catch (Exception ex) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlg();
                                        Toast.makeText(DaoActivity.this, "请确认蓝牙是否开启及设备是否正确连接", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                }
                            });
                        }

                        strSaveType = cert.getSavetype() + "";

                        if (operateState == LOG_TYPE_SIGN) {
                            //showProgDlg("数字签名中...");
                            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()) ||cert.getCerttype().contains("SM2")) {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_SM2_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_SM2;
                                signSM2(cert);
                            } else {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_RSA;
                                sign(cert);
                            }
                            // closeProgDlg();
                        } else if (operateState == LOG_TYPE_LOGIN) {
                            //showProgDlg("证书登录中...");
                            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2")) {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_SM2_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_SM2;
                                loginSignSM2(cert);
                            } else {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_RSA;
                                loginSign(cert);
                            }
                            //closeProgDlg();
                        } else if (operateState == LOG_TYPE_SIGNEX) {
                            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2")) {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_SM2_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_SM2;
                                signSM2(cert);
                            } else {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_RSA;
                                sign(cert);
                            }
                        } else if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2")) {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_SM2_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_SM2;
                                signSM2(cert);
                            } else {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_RSA;
                                sign(cert);
                            }
                        } else if (operateState == LOG_TYPE_SEAL) {
                            //showProgDlg("数字签名中...");
                            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2")) {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_SM2_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_SM2;
                                signSM2(cert);
                            } else {
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strCertType = CommonConst.CERT_TYPE_RSA_COMPANY;
                                else
                                    strCertType = CommonConst.CERT_TYPE_RSA;
                                sign(cert);
                            }
                            // closeProgDlg();
                        }
                    }
                });
            }
        } else
            resState = 1;
    }


    /*private void selectCert() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certListView = inflater.inflate(R.layout.certlist, null);
		ListView list = (ListView) certListView.findViewById(R.id.certlist);
		CertAdapter adapter = null;
		try {
			mData = getData();
			if (mData.size() == 0) {
				Toast.makeText(DaoActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
				return;
			}

			if (mData.size() == 1) {
				int certId = Integer.valueOf(mData.get(0).get("id"));
				viewCert(certId);
				return;
			}
			adapter = new CertAdapter(DaoActivity.this, mData);
			list.setAdapter(adapter);
			AlertDialog.Builder builder = new Builder(DaoActivity.this);
			builder.setIcon(R.drawable.view);
			builder.setTitle("请选择证书");
			builder.setView(certListView);
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			// builder.show();
			certListDialog = builder.create();
			certListDialog.show();
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(DaoActivity.this, "获取证书错误！", Toast.LENGTH_LONG).show();
		}

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int certId = Integer.valueOf(mData.get(position).get("id"));
				viewCert(certId);

				certListDialog.dismiss();
			}

		});

	}
*/
    private List<Map<String, String>> getData() throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<Cert> certList = new ArrayList<Cert>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        certList = certDao.getAllCerts(strActName);

        for (Cert cert : certList) {
            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

            if (verifyCert(cert, false)) {
                if (verifyDevice(cert, false)) {

                    if (isSM2 == 1) {
                        if (!cert.getCerttype().toUpperCase().contains("SM2"))
                            continue;
                    } else if (isSM2 == 2) {
                        if (cert.getCerttype().toUpperCase().contains("SM2"))
                            continue;
                    }


                    if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("id", String.valueOf(cert.getId()));

                        byte[] bCert = Base64.decode(cert.getCertificate());
                        String commonName = jse.getCertDetail(17, bCert);
                        String organization = jse.getCertDetail(14, bCert);

                        String strNotBeforeTime = jse.getCertDetail(11, bCert);
                        String strValidTime = jse.getCertDetail(12, bCert);
                        Date fromDate = sdf.parse(strNotBeforeTime);
                        Date toDate = sdf.parse(strValidTime);

                        map.put("organization", organization);
                        map.put("commonname", commonName);
                        map.put("validtime",
                                sdf2.format(fromDate) + " ~ " + sdf2.format(toDate));


                        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
                            map.put("certtype", CommonConst.CERT_SM2_NAME);
                        else
                            map.put("certtype", CommonConst.CERT_RSA_NAME);

                        map.put("savetype", cert.getSavetype() + "");
                        map.put("certname", getCertName(cert));

                        list.add(map);
                    }
                }
            }
        }

        return list;
    }

    private void viewCert(final int certId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certDetailView = inflater.inflate(R.layout.certdetail, null);

        final Cert cert = certDao.getCertByID(certId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);
        // sdf2.setTimeZone(tzChina);
        if (cert != null) {
            String certificate = cert.getCertificate();
            byte[] bCert = Base64.decode(certificate);
            // byte[] bEncCert = Base64.decode(cert.getEnccertificate());
            Certificate oCert = jse.getCertFromBuffer(bCert);
            X509Certificate oX509Cert = (X509Certificate) oCert;
            // X509Certificate oEncX509Cert = (X509Certificate) jse
            // .getCertFromBuffer(bEncCert);
            try {
                ((TextView) certDetailView.findViewById(R.id.usernamevalue)).setText(cert.getCertname());

                ASN1InputStream asn1Input = new ASN1InputStream(
                        new ByteArrayInputStream(bCert));
                ASN1Object asn1X509 = asn1Input.readObject();
                X509CertificateStructure x509 = X509CertificateStructure
                        .getInstance(asn1X509);
                ((TextView) certDetailView.findViewById(R.id.tvversion))
                        .setText(jse.getCertDetail(1, bCert));
                ((TextView) certDetailView.findViewById(R.id.tvsignalg))
                        .setText(oX509Cert.getSigAlgName());
                ((TextView) certDetailView.findViewById(R.id.tvcertsn))
                        .setText(new String(Hex.encode(oX509Cert
                                .getSerialNumber().toByteArray())));
                ((TextView) certDetailView.findViewById(R.id.tvsubject))
                        .setText(x509.getSubject().toString());
                ((TextView) certDetailView.findViewById(R.id.tvissue))
                        .setText(x509.getIssuer().toString());

                String strNotBeforeTime = jse.getCertDetail(11, bCert);
                String strValidTime = jse.getCertDetail(12, bCert);
                Date fromDate = sdf.parse(strNotBeforeTime);
                Date toDate = sdf.parse(strValidTime);

                ((TextView) certDetailView.findViewById(R.id.tvaftertime))
                        .setText(sdf2.format(toDate));
                ((TextView) certDetailView.findViewById(R.id.tvbeforetime))
                        .setText(sdf2.format(fromDate));

                RelativeLayout relativeLayout1 = (RelativeLayout) certDetailView
                        .findViewById(R.id.rl_certchainURL);
                String sCertChainPath = jse.getCertExtInfo(
                        "1.2.156.1.8888.144", oX509Cert);
                if ("".equals(sCertChainPath) || null == sCertChainPath) {
                    relativeLayout1.setVisibility(RelativeLayout.GONE);
                } else {
                    relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView
                            .findViewById(R.id.tvcertchainpath))
                            .setText(sCertChainPath);
                }

                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
                    if (!"".equals(cert.getDevicesn())) {
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
                        ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
                    } else
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);
                    if (!"".equals(cert.getDevicesn())) {
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
                        ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
                    } else
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                } else {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
                    certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                }

                BigInteger prime = null;
                int keySize = 0;

                String algorithm = oX509Cert.getPublicKey().getAlgorithm();  // 获取算法
                KeyFactory keyFact = KeyFactory.getInstance(algorithm);

                if ("RSA".equals(algorithm)) { // 如果是RSA加密
                    RSAPublicKeySpec keySpec = (RSAPublicKeySpec) keyFact.getKeySpec(oX509Cert.getPublicKey(), RSAPublicKeySpec.class);
                    prime = keySpec.getModulus();
                } else if ("DSA".equals(algorithm)) { // 如果是DSA加密
                    DSAPublicKeySpec keySpec = (DSAPublicKeySpec) keyFact.getKeySpec(oX509Cert.getPublicKey(), DSAPublicKeySpec.class);
                    prime = keySpec.getP();
                }

                keySize = prime.toString(2).length(); // 转换为二进制，获取公钥长度

                if (keySize == 0) {
                    certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertKeySize))
                            .setText(keySize + "位");
                }

                String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1", oX509Cert);
                if ("".equals(sCertUnicode) || null == sCertUnicode)
                    sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148", oX509Cert);

                RelativeLayout relativeLayout2 = (RelativeLayout) certDetailView
                        .findViewById(R.id.rl_subjectUID);
                //strUniqueID = sCertUnicode;    //从证书获取身份证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    relativeLayout2.setVisibility(RelativeLayout.GONE);
                } else {
                    relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertunicode))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3", oX509Cert);  //获取工商注册号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcerticnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5", oX509Cert);  //获取税号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcerttaxnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4", oX509Cert);  //获取组织机构代码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertorgcode))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2", oX509Cert);  //获取社会保险号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertinsnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201", oX509Cert);  //获取住房公积金账号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertaccfundnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202", oX509Cert);  //获取事业单位证书号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertinstinumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203", oX509Cert);  //获取社会组织法人编码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertassnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204", oX509Cert);  //获取政府机关法人编码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertgovnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207", oX509Cert);  //获取律师事务所执业许可证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertlawlicence))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208", oX509Cert);  //获取个体工商户营业执照
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertindilicence))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209", oX509Cert);  //外国企业常驻代表机构登记证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertforeigncode))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210", oX509Cert);  //获取统一社会信用代码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertcrednumber))
                            .setText(sCertUnicode);
                }

            } catch (Exception e) {
                Log.e(CommonConst.TAG, e.getMessage(), e);
                Toast.makeText(DaoActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

        } else {
            Toast.makeText(DaoActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
            return;
        }

        Builder builder = new Builder(DaoActivity.this);
        builder.setIcon(R.drawable.view);
        builder.setTitle("证书明细");
        builder.setView(certDetailView);
        builder.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

		/*builder.setNegativeButton("签名登录",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
						if(VerifyCert(cert))
							   sign(cert);
							else
							   resState = 1;
					}
				});*/
        builder.show();
    }

    private void viewSM2Cert(final int certId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certDetailView = inflater.inflate(R.layout.certdetail, null);

        final Cert cert = certDao.getCertByID(certId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);
        // sdf2.setTimeZone(tzChina);
        if (cert != null) {
            String certificate = cert.getCertificate();
            byte[] bCert = Base64.decode(certificate);
            Certificate oCert = jse.getCertFromBuffer(bCert);
            X509Certificate oX509Cert = (X509Certificate) oCert;

            try {
                ((TextView) certDetailView.findViewById(R.id.usernamevalue)).setText(cert.getCertname());

                ((TextView) certDetailView.findViewById(R.id.tvversion))
                        .setText(jse.getCertDetail(1, bCert));
                ((TextView) certDetailView.findViewById(R.id.tvsignalg))
                        .setText(CommonConst.CERT_ALG_SM2);
				/* ((TextView) certDetailView.findViewById(R.id.tvcertsn))
						.setText(new String(Hex.encode(oX509Cert
								.getSerialNumber().toByteArray()))); */
                ((TextView) certDetailView.findViewById(R.id.tvcertsn))
                        .setText(jse.getCertDetail(2, bCert));

                ((TextView) certDetailView.findViewById(R.id.tvsubject))
                        .setText(getSM2CertIssueInfo(cert));
                ((TextView) certDetailView.findViewById(R.id.tvissue))
                        .setText(getSM2CertSubjectInfo(cert));

                String strNotBeforeTime = jse.getCertDetail(11, bCert);
                String strValidTime = jse.getCertDetail(12, bCert);
                Date fromDate = sdf.parse(strNotBeforeTime);
                Date toDate = sdf.parse(strValidTime);

                ((TextView) certDetailView.findViewById(R.id.tvaftertime))
                        .setText(sdf2.format(toDate));
                ((TextView) certDetailView.findViewById(R.id.tvbeforetime))
                        .setText(sdf2.format(fromDate));

                RelativeLayout relativeLayout1 = (RelativeLayout) certDetailView
                        .findViewById(R.id.rl_certchainURL);
                String sCertChainPath = jse.getCertExtInfo(
                        "1.2.156.1.8888.144", oX509Cert);
                if ("".equals(sCertChainPath) || null == sCertChainPath) {
                    relativeLayout1.setVisibility(RelativeLayout.GONE);
                } else {
                    relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView
                            .findViewById(R.id.tvcertchainpath))
                            .setText(sCertChainPath);
                }

                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
                    if (!"".equals(cert.getDevicesn())) {
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
                        ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
                    } else
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);
                    if (!"".equals(cert.getDevicesn())) {
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
                        ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
                    } else
                        certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                } else {
                    ((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
                    certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
                }

                RelativeLayout relativeLayout2 = (RelativeLayout) certDetailView
                        .findViewById(R.id.rl_subjectUID);

                String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1", oX509Cert);
                if ("".equals(sCertUnicode) || null == sCertUnicode)
                    sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148", oX509Cert);

                //strUniqueID = sCertUnicode;    //从证书获取身份证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    relativeLayout2.setVisibility(RelativeLayout.GONE);
                } else {
                    relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertunicode))
                            .setText(sCertUnicode);
                }

                certDetailView.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3", oX509Cert);  //获取工商注册号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcerticnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5", oX509Cert);  //获取税号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcerttaxnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4", oX509Cert);  //获取组织机构代码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertorgcode))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2", oX509Cert);  //获取社会保险号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertinsnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201", oX509Cert);  //获取住房公积金账号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertaccfundnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202", oX509Cert);  //获取事业单位证书号
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertinstinumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203", oX509Cert);  //获取社会组织法人编码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertassnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204", oX509Cert);  //获取政府机关法人编码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertgovnumber))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207", oX509Cert);  //获取律师事务所执业许可证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertlawlicence))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208", oX509Cert);  //获取个体工商户营业执照
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertindilicence))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209", oX509Cert);  //外国企业常驻代表机构登记证
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertforeigncode))
                            .setText(sCertUnicode);
                }

                sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210", oX509Cert);  //获取统一社会信用代码
                if ("".equals(sCertUnicode) || null == sCertUnicode) {
                    certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
                } else {
                    certDetailView.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
                    ((TextView) certDetailView.findViewById(R.id.tvcertcrednumber))
                            .setText(sCertUnicode);
                }

            } catch (Exception e) {
                Log.e(CommonConst.TAG, e.getMessage(), e);
                Toast.makeText(DaoActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

        } else {
            Toast.makeText(DaoActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
            return;
        }

        Builder builder = new Builder(DaoActivity.this);
        builder.setIcon(R.drawable.view);
        builder.setTitle("证书明细");
        builder.setView(certDetailView);
        builder.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }


    private boolean verifyCert(final Cert cert, boolean bShow) {
        if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
            return true;
        } else {
            if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())) {
                int i = -1;
                if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
                else
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
                // Toast.makeText(DaoActivity.this, "verifyCert:"+i+"\nCert:"+cert.getCertificate()+"\nCertchain:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
                Log.d("DaoActivity", "verifyCert-Cert:" + cert.getCertificate() + " Certchain:" + cert.getCertchain());
                if (i == CommonConst.RET_VERIFY_CERT_OK) {
                    return true;
                }/*else if(i == 0){
			if(bShow)
			  Toast.makeText(DaoActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
		  }*/ else {
                    if (bShow)
                        Toast.makeText(DaoActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
                }
            } else if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())) {
                String strSignCert = "";

                if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn())) {
                    if (operateState != LOG_TYPE_ENVELOP_DECRYPT)
                        return false;
                } else {
                    int i = -1;
                    try {
                        strSignCert = cert.getCertificate();
                        if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                            i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                        else
                            i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());
                        //Toast.makeText(DaoActivity.this, "verifySM2Cert:"+i, Toast.LENGTH_LONG).show();
                        // LaunchActivity.logUtil.recordLogServiceLog("Cert="+strSignCert+"\nCertchain="+cert.getCertchain()+"\nresult="+i);
                        //Toast.makeText(DaoActivity.this,"证书链:"+cert.getCertchain(), Toast.LENGTH_SHORT).show();
                        // Toast.makeText(DaoActivity.this,"verifySM2Cert:"+i, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (i == 0) {
                        //Toast.makeText(DaoActivity.this, "验证证书通过", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (i == 1) {
                        if (bShow)
                            Toast.makeText(DaoActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
                    } else {
                        if (bShow)
                            Toast.makeText(DaoActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (CommonConst.CERT_TYPE_RSA_COMPANY.equals(cert.getCerttype())) {
                int i = -1;
                if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
                else
                    i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
                // Toast.makeText(DaoActivity.this, "verifyCert:"+i+"\nCert:"+cert.getCertificate()+"\nCertchain:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
                Log.d("DaoActivity", "verifyCert-Cert:" + cert.getCertificate() + " Certchain:" + cert.getCertchain());
                if (i == CommonConst.RET_VERIFY_CERT_OK) {
                    return true;
                } else {
                    if (bShow)
                        Toast.makeText(DaoActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
                }
            } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())) {
                String strSignCert = "";

                if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn())) {
                    if (operateState != LOG_TYPE_ENVELOP_DECRYPT)
                        return false;
                } else {
                    int i = -1;
                    try {
                        strSignCert = cert.getCertificate();
                        if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                            i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                        else
                            i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());
                        //Toast.makeText(DaoActivity.this, "verifySM2Cert:"+i, Toast.LENGTH_LONG).show();
                        // LaunchActivity.logUtil.recordLogServiceLog("Cert="+strSignCert+"\nCertchain="+cert.getCertchain()+"\nresult="+i);
                        //Toast.makeText(DaoActivity.this,"证书链:"+cert.getCertchain(), Toast.LENGTH_SHORT).show();
                        // Toast.makeText(DaoActivity.this,"verifySM2Cert:"+i, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (i == 0) {
                        return true;
                    } else if (i == 1) {
                        if (bShow)
                            Toast.makeText(DaoActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
                    } else {
                        if (bShow)
                            Toast.makeText(DaoActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
                    }

                }
            }else if (!cert.getCerttype().contains("SM2")) {
                int i = -1;
                try {
                    if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                        i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
                    else
                        i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                // Toast.makeText(DaoActivity.this, "verifyCert:"+i+"\nCert:"+cert.getCertificate()+"\nCertchain:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
//            Log.d("DaoActivity", "verifyCert-Cert:" + cert.getCertificate() + " Certchain:" + cert.getCertchain());
                if (i == CommonConst.RET_VERIFY_CERT_OK) {
                    return true;
                } else {
                    return false;
                }
            } else if (cert.getCerttype().contains("SM2")) {
                String strSignCert = "";

                int i = -1;
                try {
                    strSignCert = cert.getCertificate();
                    if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                        i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                    else
                        i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                if (i == 0) {
                    return true;
                } else if (i == 1) {
                    return false;
                } else {
                    return false;
                }

            }

            return false;
        }
    }

    private boolean verifyDevice(final Cert cert, boolean bShow) {
		/*if(CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
			return true;
		if(CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
			return true;

		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		String sDeciceID = jse.getCertExtInfo("1.2.156.112570.12.102",oX509Cert);

	//	if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
		//	return true;
		if(null == sDeciceID)
			return true;

		//获取设备唯一标识符
		String deviceID = android.os.Build.SERIAL;
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
			deviceID = cert.getDevicesn();
		if(sDeciceID.equals(deviceID))
			return true;

		if(bShow)
		   Toast.makeText(DaoActivity.this, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
		*/
        return true;
    }


    private void loginSign(final Cert cert) {
        EditText accountPwd = (EditText) findViewById(R.id.textPwd);
        String strAccountPwd = accountPwd.getText().toString().trim();
        if (!("".equals(strPwd)))
            strAccountPwd = strPwd;

        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_NONE == cert.getSavetype())

                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        final String sPwd = strAccountPwd;
        if (sPwd != null && !"".equals(sPwd)) {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                if (null == gEsDev.readRSASignatureCert() || "".equals(gEsDev.readRSASignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙key签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                if (null == ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙sim卡签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else {
                final String sKeyStore = cert.getKeystore();
                byte[] bKeyStore = Base64.decode(sKeyStore);
                ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
                KeyStore oStore = null;
                try {
                    oStore = KeyStore.getInstance("PKCS12");
                    oStore.load(kis, sPwd.toCharArray());
                } catch (Exception e) {
                    Toast.makeText(DaoActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                    //CommUtil.showErrPasswordMsg(DaoActivity.this,cert.getId());
                    resState = 1;
                    return;
                }
            }

            new Thread() {
                @Override
                public void run() {
                    String message = String.format("%s=%s&%s=%s", CommonConst.PARAM_BIZSN, strServiecNo, CommonConst.PARAM_RANDOM_NUMBER, strResult);
                    if (isNewQrCode) {
//                         message = String.format("%s=%s", CommonConst.PARAM_RANDOM_NUMBER, strResult);
                        message = strResult;
                    }
                    try {
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                            strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                            strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                        } else {
                            strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd);
                        }
                    } catch (Exception e) {
                        Log.e("sheca", e.getMessage(), e);
                        handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }
                    if (strSign != null) {
                        boolean flag = true;
                        // try {
                        // flag = PKIUtil.verifySign(message, sSign,
                        // cert.getCertificate());
                        // } catch (Exception e) {
                        // Log.e("sheca", e.getMessage(), e);
                        // Toast.makeText(
                        // ProviderClientTestActivity.this,
                        // "验证签名错误", Toast.LENGTH_SHORT)
                        // .show();
                        // }

                        if (flag) {
//							dialog.dismiss();
                            strCert = cert.getCertificate();
                            strCertSN = cert.getCertsn();
                            //strUniCodeID = getPersonID(strCert);
                            if ((!bScanDao) && (!bScanSDKDao))
                                saveLog(operateState,
                                        strCertSN,
                                        message,
                                        strAppName,
                                        strSign,
                                        1);    //保存操作日志

                            if ("".equals(cert.getCerthash())) {
                                cert.setCerthash(sPwd);

                                String strActName = mAccountDao.getLoginAccount().getName();
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                                certDao.updateCert(cert, strActName);
                            }

                            CommUtil.resetPasswordLocked(DaoActivity.this, cert.getId());
                            handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);


                        } else {
                            Toast.makeText(
                                    DaoActivity.this,
                                    "验证签名失败", Toast.LENGTH_LONG).show();
                            resState = 1;
                        }
                    }
                }
            }.start();
        } else {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
            else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(DaoActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();

            resState = 1;
        }
    }

    private void loginSignSM2(final Cert cert) {
        EditText accountPwd = (EditText) findViewById(R.id.textPwd);
        String strAccountPwd = accountPwd.getText().toString().trim();
        if (!("".equals(strPwd)))
            strAccountPwd = strPwd;

        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_NONE == cert.getSavetype())

                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        final String sPwd = strAccountPwd;
        String strSignCert = "";

        if (sPwd != null && !"".equals(sPwd)) {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                if (null == gEsDev.readSM2SignatureCert() || "".equals(gEsDev.readSM2SignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙key签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                if (null == ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙sim卡签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else {
                try {
                    //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                    //initShcaCciStdService();
                    int retCode = -1;
                    if (null != gUcmSdk)
                        retCode = initShcaUCMService();

                    if (retCode != 0) {
                        Toast.makeText(DaoActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                        resState = 1;
                        return;
                    }

                    int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), sPwd);
                    //if(ret == -50){
                    //ret = ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
                    //}

                    if (ret != 0) {
                        //ret =  ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
                        Toast.makeText(DaoActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                        //CommUtil.showErrPasswordMsg(DaoActivity.this,cert.getId());
                        resState = 1;
                        return;
                    }

                    //strSignCert = ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid());
				    /* if(null == strSignCert || "".equals(strSignCert)){
				    	 Toast.makeText(DaoActivity.this, "证书签名失败",Toast.LENGTH_SHORT).show();
						 resState = 1;
						 return;
				    } */
                } catch (Exception e) {
                    ShcaCciStd.gSdk = null;
                    Toast.makeText(DaoActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            }

            new Thread() {
                @Override
                public void run() {

                    String message = String.format("%s=%s&%s=%s", CommonConst.PARAM_BIZSN, strServiecNo, CommonConst.PARAM_RANDOM_NUMBER, strResult);

                    if (isNewQrCode) {
//                        message = String.format("%s=%s", CommonConst.PARAM_RANDOM_NUMBER, strResult);
                        message =  strResult;

                    }
                    byte[] signDate = null;

                    try {
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                            signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                            if (null == signDate) {
                                resState = 1;
                                return;
                            }

                            strSign = new String(Base64.encode(signDate));
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                            signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                            if (null == signDate) {
                                resState = 1;
                                return;
                            }

                            strSign = new String(Base64.encode(signDate));
                        } else {
                            //signDate = ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                            JShcaUcmStdRes jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                            //int retcode = ShcaCciStd.gSdk.getLastErrCode();

                            if (null == jres.response || "".equals(jres.response)) {
                                resState = 1;
                                return;
                            }
                            //strSign = new String(Base64.encode(signDate));
                            strSign = jres.response; //new String(Base64.encode(signDate));
                        }
                    } catch (Exception e) {
                        handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }
                    if (strSign != null) {
                        boolean flag = true;
                        // try {
                        // flag = PKIUtil.verifySign(message, sSign,
                        // cert.getCertificate());
                        // } catch (Exception e) {
                        // Log.e("sheca", e.getMessage(), e);
                        // Toast.makeText(
                        // ProviderClientTestActivity.this,
                        // "验证签名错误", Toast.LENGTH_SHORT)
                        // .show();
                        // }

                        if (flag) {
//							dialog.dismiss();
                            strCert = cert.getCertificate();
                            strCertSN = cert.getCertsn();
                            strContainerID = cert.getContainerid();
                            //strUniCodeID = getPersonID(strCert);
                            if ((!bScanDao) && (!bScanSDKDao))
                                saveLog(operateState,
                                        strCertSN,
                                        message,
                                        strAppName,
                                        strSign,
                                        2);   //保存操作日志

                            if ("".equals(cert.getCerthash())) {
                                cert.setCerthash(sPwd);

                                String strActName = mAccountDao.getLoginAccount().getName();
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                                certDao.updateCert(cert, strActName);
                            }

                            CommUtil.resetPasswordLocked(DaoActivity.this, cert.getId());
                            handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);

                        } else {

                            resState = 1;
                        }
                    }
                }
            }.start();
        } else {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
            else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(DaoActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();

            resState = 1;
        }
    }


    private void sign(final Cert cert) {
        EditText accountPwd = (EditText) findViewById(R.id.textPwd);
        String strAccountPwd = accountPwd.getText().toString().trim();
        if (!("".equals(strPwd)))
            strAccountPwd = strPwd;

        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_NONE == cert.getSavetype())

                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        final String sPwd = strAccountPwd;
        if (sPwd != null && !"".equals(sPwd)) {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                if (null == gEsDev.readRSASignatureCert() || "".equals(gEsDev.readRSASignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙key签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                if (null == ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙sim卡签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else {
                final String sKeyStore = cert.getKeystore();
                byte[] bKeyStore = Base64.decode(sKeyStore);
                ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
                KeyStore oStore = null;
                try {
                    oStore = KeyStore.getInstance("PKCS12");
                    oStore.load(kis, sPwd.toCharArray());
                } catch (Exception e) {
                    Toast.makeText(DaoActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                    //CommUtil.showErrPasswordMsg(DaoActivity.this,cert.getId());
                    resState = 1;
                    return;
                }
            }

            new Thread() {
                @Override
                public void run() {
                    String message = strResult;

                    try {
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                if ("1".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(gEsDev.doRSASignature(Base64.decode(message), sPwd)));
                                } else if ("0".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
                                } else {
                                    strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
                                }
                            } else {
                                if ("1".equals(strMsgWrapper)) {
                                    if (isJSONDate)
                                        strSign = new String(Base64.encode(gEsDev.doRSASignature(Base64.decode(message), sPwd)));
                                    else
                                        strSign = new String(Base64.encode(gEsDev.doRSASignature(Base64.decode(URLDecoder.decode(message, "UTF-8")), sPwd)));

                                } else if ("0".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
                                } else {
                                    strSign = new String(Base64.encode(gEsDev.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd)));
                                }
                            }
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                if ("1".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(Base64.decode(message), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                } else if ("0".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                } else {
                                    strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                }
                            } else {
                                if ("1".equals(strMsgWrapper)) {
                                    if (isJSONDate)
                                        strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(Base64.decode(message), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                    else
                                        strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(Base64.decode(URLDecoder.decode(message, "UTF-8")), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                } else if ("0".equals(strMsgWrapper)) {
                                    strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                } else {
                                    strSign = new String(Base64.encode(ScanBlueToothSimActivity.gKsSdk.doRSASignature(message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.KS_RSA_SIGN_ALG, sPwd)));
                                }
                            }
                        } else {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                if ("1".equals(strMsgWrapper)) {
                                    strSign = PKIUtil.sign(Base64.decode(message), cert.getKeystore(), sPwd);
                                } else if ("0".equals(strMsgWrapper)) {
                                    strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd);
                                } else {
                                    strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd);
                                }
                            } else {
                                if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                                    try {
                                        strSign = PKIUtil.envelopeDecrypt(message, cert.getKeystore(), sPwd);
                                    } catch (Exception ex) {
                                        strSign = "";
                                    }
                                } else {
                                    if (isSignEx) {
                                        String strSignMsg = "";
                                        if (message.endsWith(CommonConst.UM_SPLIT_STR))
                                            message = message.substring(0, message.lastIndexOf(CommonConst.UM_SPLIT_STR));

                                        for (int i = 0; i < message.split(CommonConst.UM_SPLIT_STR).length; i++) {
                                            if ("1".equals(strMsgWrapper)) {
                                                if (isJSONDate)
                                                    strSignMsg += PKIUtil.sign(Base64.decode(message.split(CommonConst.UM_SPLIT_STR)[i]), cert.getKeystore(), sPwd) + CommonConst.UM_SPLIT_STR;
                                                else
                                                    strSignMsg += PKIUtil.sign(Base64.decode(URLDecoder.decode(message.split(CommonConst.UM_SPLIT_STR)[i], "UTF-8")), cert.getKeystore(), sPwd) + CommonConst.UM_SPLIT_STR;
                                            } else if ("0".equals(strMsgWrapper)) {
                                                strSignMsg += PKIUtil.sign(message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd) + CommonConst.UM_SPLIT_STR;
                                            } else {
                                                strSignMsg += PKIUtil.sign(message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd) + CommonConst.UM_SPLIT_STR;
                                            }
                                        }

                                        strSignMsg = strSignMsg.substring(0, strSignMsg.lastIndexOf(CommonConst.UM_SPLIT_STR));
                                        strSign = strSignMsg;
                                    } else {
                                        SealSignUtil.strMsgWrapper = strMsgWrapper;
                                        Editor editor = sharedPrefs.edit();
                                        editor.putString(CommonConst.SETTINGS_CERT_PWD, sPwd);
                                        editor.putString(CommonConst.SETTINGS_MSG_WRAPPER, strMsgWrapper);
                                        editor.commit();

                                        if ("1".equals(strMsgWrapper)) {
                                            if (operateState == LOG_TYPE_SEAL) {
                                                strSign = PKIUtil.sign(Base64.decode(message), cert.getKeystore(), sPwd);
                                                SealSignUtil.strCertPwd = sPwd;
                                            } else {
                                                if (isJSONDate)
                                                    strSign = PKIUtil.sign(Base64.decode(message), cert.getKeystore(), sPwd);
                                                else
                                                    strSign = PKIUtil.sign(Base64.decode(URLDecoder.decode(message, "UTF-8")), cert.getKeystore(), sPwd);
                                            }
                                        } else if ("0".equals(strMsgWrapper)) {
                                            if (operateState == LOG_TYPE_SEAL)
                                                strSign = PKIUtil.sign(Base64.decode(message), cert.getKeystore(), sPwd);
                                            else
                                                strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd);
                                        } else {
                                            if (operateState == LOG_TYPE_SEAL)
                                                strSign = PKIUtil.sign(Base64.decode(message), cert.getKeystore(), sPwd);
                                            else
                                                strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), sPwd);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("sheca", e.getMessage(), e);
                        handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }

                    if (strSign != null) {
                        boolean flag = true;
                        // try {
                        // flag = PKIUtil.verifySign(message, sSign,
                        // cert.getCertificate());
                        // } catch (Exception e) {
                        // Log.e("sheca", e.getMessage(), e);
                        // Toast.makeText(
                        // ProviderClientTestActivity.this,
                        // "验证签名错误", Toast.LENGTH_SHORT)
                        // .show();
                        // }

                        if (flag) {
//							dialog.dismiss();
                            strCert = cert.getCertificate();
                            strCertSN = cert.getCertsn();
                            //strUniCodeID = getPersonID(strCert);
                            if ((!bScanDao) && (!bScanSDKDao))
                                saveLog(operateState,
                                        strCertSN,
                                        message,
                                        strAppName,
                                        strSign,
                                        1);   //保存操作日志

                            if ("".equals(cert.getCerthash())) {
                                cert.setCerthash(sPwd);

                                String strActName = mAccountDao.getLoginAccount().getName();
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                                certDao.updateCert(cert, strActName);
                            }

                            CommUtil.resetPasswordLocked(DaoActivity.this, cert.getId());
                            handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);

                        } else {
                            Toast.makeText(
                                    DaoActivity.this,
                                    "验证签名失败", Toast.LENGTH_SHORT).show();
                            resState = 1;
                        }
                    }
                }
            }.start();
        } else {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
            else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(DaoActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();

            resState = 1;
        }
    }

    private void signSM2(final Cert cert) {
        EditText accountPwd = (EditText) findViewById(R.id.textPwd);
        String strAccountPwd = accountPwd.getText().toString().trim();
        if (!("".equals(strPwd)))
            strAccountPwd = strPwd;

        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_NONE == cert.getSavetype())
                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        final String sPwd = strAccountPwd;
        String strSignCert = "";

        if (sPwd != null && !"".equals(sPwd)) {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                if (null == gEsDev.readSM2SignatureCert() || "".equals(gEsDev.readSM2SignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙key签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                if (null == ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() || "".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert())) {
                    Toast.makeText(DaoActivity.this, "蓝牙sim卡签名失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            } else {
                try {
                    //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                    //initShcaCciStdService();

                    int retCode = -1;
                    if (null != gUcmSdk)
                        retCode = initShcaUCMService();

                    if (retCode != 0) {
                        Toast.makeText(DaoActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                        resState = 1;
                        return;
                    }

                    int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), sPwd);
                    if (ret != 0) {
                        //ret =  ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
                        Toast.makeText(DaoActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                        //CommUtil.showErrPasswordMsg(DaoActivity.this,cert.getId());
                        resState = 1;
                        return;
                    }

                    // strSignCert = ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid());
				    /* if(null == strSignCert || "".equals(strSignCert)){
				    	 Toast.makeText(DaoActivity.this, "证书签名失败",Toast.LENGTH_SHORT).show();
						 resState = 1;
						 return;
				    }*/

                } catch (Exception e) {
                    ShcaCciStd.gSdk = null;
                    Toast.makeText(DaoActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    resState = 1;
                    return;
                }
            }

            new Thread() {
                @Override
                public void run() {

                    String message = strResult;
                    byte[] signDate = null;

                    try {
                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                if ("1".equals(strMsgWrapper)) {
                                    signDate = gEsDev.doSM2Signature(Base64.decode(message), sPwd);
                                } else if ("0".equals(strMsgWrapper)) {
                                    signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                } else {
                                    signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                }
                            } else {
                                if ("1".equals(strMsgWrapper)) {
                                    if (isJSONDate)
                                        signDate = gEsDev.doSM2Signature(Base64.decode(message), sPwd);
                                    else
                                        signDate = gEsDev.doSM2Signature(Base64.decode(URLDecoder.decode(message, "UTF-8")), sPwd);
                                } else if ("0".equals(strMsgWrapper)) {
                                    signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                } else {
                                    signDate = gEsDev.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                }
                            }

                            if (null == signDate) {
                                resState = 1;
                                return;
                            }

                            strSign = new String(Base64.encode(signDate));
                        } else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                if ("1".equals(strMsgWrapper)) {
                                    signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(Base64.decode(message), sPwd);
                                } else if ("0".equals(strMsgWrapper)) {
                                    signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                } else {
                                    signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                }
                            } else {
                                if ("1".equals(strMsgWrapper)) {
                                    if (isJSONDate)
                                        signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(Base64.decode(message), sPwd);
                                    else
                                        signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(Base64.decode(URLDecoder.decode(message, "UTF-8")), sPwd);
                                } else if ("0".equals(strMsgWrapper)) {
                                    signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                } else {
                                    signDate = ScanBlueToothSimActivity.gKsSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd);
                                }
                            }

                            if (null == signDate) {
                                resState = 1;
                                return;
                            }

                            strSign = new String(Base64.encode(signDate));
                        } else {
                            if ((!bScanDao) && (!bScanSDKDao)) {
                                JShcaUcmStdRes jres = new JShcaUcmStdRes();
                                if ("1".equals(strMsgWrapper)) {
                                    //signDate =  ShcaCciStd.gSdk.doSM2Signature(Base64.decode(message), sPwd, cert.getContainerid());
                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message), CommonConst.SERT_TYPE);
                                } else if ("0".equals(strMsgWrapper)) {
                                    //signDate =  ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                } else {
                                    //signDate =  ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                }

                                strSign = jres.response; //new String(Base64.encode(signDate));
                            } else {
                                if (operateState == LOG_TYPE_ENVELOP_DECRYPT) {
                                    try {
                                        byte[] envelope = ShcaCciStd.gSdk.doDecSM2Enveloper(cert.getContainerid(), sPwd, Base64.decode(message));
                                        strSign = new String(envelope, CommonConst.SIGN_STR_CODE);
                                    } catch (Exception ex) {
                                        strSign = "";
                                    }
                                } else {
                                    if (isSignEx) {
                                        String strSignMsg = "";
                                        JShcaUcmStdRes jres = new JShcaUcmStdRes();
                                        if (message.endsWith(CommonConst.UM_SPLIT_STR))
                                            message = message.substring(0, message.lastIndexOf(CommonConst.UM_SPLIT_STR));

                                        for (int i = 0; i < message.split(CommonConst.UM_SPLIT_STR).length; i++) {
                                            byte[] signDateArr = null;

                                            if ("1".equals(strMsgWrapper)) {
                                                if (isJSONDate)
                                                    //signDateArr =  ShcaCciStd.gSdk.doSM2Signature(Base64.decode(message.split(CommonConst.UM_SPLIT_STR)[i]), sPwd, cert.getContainerid());
                                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message.split(CommonConst.UM_SPLIT_STR)[i]), CommonConst.SERT_TYPE);
                                                else
                                                    //signDateArr =  ShcaCciStd.gSdk.doSM2Signature(Base64.decode(URLDecoder.decode(message.split(CommonConst.UM_SPLIT_STR)[i],"UTF-8")), sPwd, cert.getContainerid());
                                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(URLDecoder.decode(message.split(CommonConst.UM_SPLIT_STR)[i], "UTF-8")), CommonConst.SERT_TYPE);
                                            } else if ("0".equals(strMsgWrapper)) {
                                                //signDateArr =  ShcaCciStd.gSdk.doSM2Signature(message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                            } else {
                                                //signDateArr =  ShcaCciStd.gSdk.doSM2Signature(message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.split(CommonConst.UM_SPLIT_STR)[i].getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                            }

                                            if (null == jres.response || "".equals(jres.response)) {
                                                resState = 1;
                                                return;
                                            }

                                            strSignMsg += jres.response + CommonConst.UM_SPLIT_STR;
                                        }

                                        strSignMsg = strSignMsg.substring(0, strSignMsg.lastIndexOf(CommonConst.UM_SPLIT_STR));
                                        strSign = strSignMsg;
                                    } else {
                                        SealSignUtil.strMsgWrapper = strMsgWrapper;
                                        Editor editor = sharedPrefs.edit();
                                        editor.putString(CommonConst.SETTINGS_CERT_PWD, sPwd);
                                        editor.putString(CommonConst.SETTINGS_MSG_WRAPPER, strMsgWrapper);
                                        editor.commit();

                                        JShcaUcmStdRes jres = new JShcaUcmStdRes();

                                        if ("1".equals(strMsgWrapper)) {
                                            if (operateState == LOG_TYPE_SEAL) {
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message), CommonConst.SERT_TYPE);
                                                SealSignUtil.strCertPwd = sPwd;
                                            } else {
                                                if (isJSONDate)
                                                    //signDate =  ShcaCciStd.gSdk.doSM2Signature(Base64.decode(message), sPwd, cert.getContainerid());
                                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message), CommonConst.SERT_TYPE);
                                                else
                                                    //signDate =  ShcaCciStd.gSdk.doSM2Signature(Base64.decode(URLDecoder.decode(message,"UTF-8")), sPwd, cert.getContainerid());
                                                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(URLDecoder.decode(message, "UTF-8")), CommonConst.SERT_TYPE);
                                            }
                                        } else if ("0".equals(strMsgWrapper)) {
                                            if (operateState == LOG_TYPE_SEAL)
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message), CommonConst.SERT_TYPE);
                                            else
                                                //signDate =  ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                        } else {
                                            if (operateState == LOG_TYPE_SEAL)
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, Base64.decode(message), CommonConst.SERT_TYPE);
                                            else
                                                //signDate =  ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                                                jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                                        }

                                        if (null == jres.response || "".equals(jres.response)) {
                                            resState = 1;
                                            return;
                                        }

                                        strSign = jres.response; //new String(Base64.encode(signDate));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("sheca", e.getMessage(), e);
                        handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }

                    if (strSign != null) {
                        boolean flag = true;
                        // try {
                        // flag = PKIUtil.verifySign(message, sSign,
                        // cert.getCertificate());
                        // } catch (Exception e) {
                        // Log.e("sheca", e.getMessage(), e);
                        // Toast.makeText(
                        // ProviderClientTestActivity.this,
                        // "验证签名错误", Toast.LENGTH_SHORT)
                        // .show();
                        // }

                        if (flag) {
//							dialog.dismiss();
                            strCert = cert.getCertificate();
                            strCertSN = cert.getCertsn();
                            strContainerID = cert.getContainerid();
                            //strUniCodeID = getPersonID(strCert);
                            if ((!bScanDao) && (!bScanSDKDao))
                                saveLog(operateState,
                                        strCertSN,
                                        message,
                                        strAppName,
                                        strSign,
                                        2);   //保存操作日志

                            if ("".equals(cert.getCerthash())) {
                                cert.setCerthash(sPwd);

                                String strActName = mAccountDao.getLoginAccount().getName();
                                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                    strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                                certDao.updateCert(cert, strActName);
                            }

                            CommUtil.resetPasswordLocked(DaoActivity.this, cert.getId());
                            handler.sendEmptyMessage(LOGIN_SIGN_SUCCESS);

                        } else {

                            resState = 1;
                        }
                    }
                }
            }.start();
        } else {
            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙key密码", Toast.LENGTH_SHORT).show();
            else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                Toast.makeText(DaoActivity.this, "请输入蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(DaoActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();

            resState = 1;
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SIGN_FAILURE: {
                    resState = 1;
                    Toast.makeText(DaoActivity.this, "数字签名错误或证书密码错误", Toast.LENGTH_SHORT).show();
                }
                break;
                case LOGIN_SIGN_SUCCESS: {
                    resState = 0;
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("ServiecNo", strServiecNo);
                    bundle.putString("OriginInfo", strResult);
                    if (isSignEx)
                        bundle.putString("IsSignEx", "isSignEx");
                    bundle.putString("Sign", strSign);
                    bundle.putString("Cert", strCert);
                    bundle.putString("CertSN", strCertSN);
                    //if("".equals(strUniCodeID)|| null == strUniCodeID)
                    bundle.putString("UniqueID", mAccountDao.getLoginAccount().getIdentityCode());
                    //else
                    //bundle.putString("UniqueID", strUniCodeID);
                    bundle.putString("CertType", strCertType);
                    bundle.putString("SaveType", strSaveType);
                    bundle.putString("ContainerID", strContainerID);
                    bundle.putString("AppID", strAPPID);
                    bundle.putString("MsgWrapper", strMsgWrapper);
                    if (operateState == LOG_TYPE_ENVELOP_DECRYPT)
                        bundle.putString("IsDecrypt", "isDecrypt");
                    bundle.putInt("Code", resState);
                    resultIntent.putExtras(bundle);

                    DaoActivity.this.setResult(RESULT_OK, resultIntent);
                    DaoActivity.this.finish();
                }
                break;
            }
        }
    };

    private void saveLog(int type, String certsn, String message, String invoker, String sign, int signAlg) {
        OperationLog log = new OperationLog();
        if (type == LOG_TYPE_LOGIN) {
            log.setType(OperationLog.LOG_TYPE_DAO_LOGIN);
        } else if (type == LOG_TYPE_SIGN) {
            log.setType(OperationLog.LOG_TYPE_DAO_SIGN);
        } else if (type == LOG_TYPE_SIGNEX) {
            log.setType(OperationLog.LOG_TYPE_DAO_SIGNEX);
        } else if (type == LOG_TYPE_ENVELOP_DECRYPT) {
            log.setType(OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT);
        } else
            log.setType(OperationLog.LOG_TYPE_DAO_LOGIN_INTERNET);

        log.setCertsn(certsn);
        log.setMessage(message);
        log.setSign(sign);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        log.setCreatetime(sdf.format(date));
        log.setInvoker(invoker);
        log.setSignalg(signAlg);
        log.setIsupload(0);
        log.setInvokerid(strAPPID);

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        mLogDao.addLog(log, strActName);
    }

    private void checkCert() {
        if (mData.size() == 0) {
            if (isSM2 == 1) {
                Toast.makeText(this, "不存在SM2证书", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            } else if (isSM2 == 2) {
                Toast.makeText(this, "不存在RSA证书", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            } else {
                Toast.makeText(this, "证书不存在", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }


        }


//        return;
//        if (bChecked)
//            return;
//
//        if (mData.size() == 0) {
//            //进行人脸识并下载证书
//            if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
//                resState = 3;    //企业账号登录未下载证书
//
//                Intent resultIntent = new Intent();
//                Bundle bundle = new Bundle();
//                bundle.putString("ServiecNo", strServiecNo);
//                bundle.putString("OriginInfo", strResult);
//                bundle.putString("Sign", strSign);
//                bundle.putString("Cert", strCert);
//                bundle.putString("CertSN", strCertSN);
//                bundle.putString("UniqueID", "");
//                bundle.putString("CertType", "");
//                bundle.putString("SaveType", "");
//                bundle.putString("ContainerID", "");
//                bundle.putString("AppID", strAPPID);
//                bundle.putString("MsgWrapper", strMsgWrapper);
//                bundle.putInt("Code", resState);
//                resultIntent.putExtras(bundle);
//
//                DaoActivity.this.setResult(RESULT_OK, resultIntent);
//                DaoActivity.this.finish();
//                return;
//            } else {
//                bChecked = true;
//
//                Intent intent = null;
//                if (mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
//                    if ("".equals(sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, ""))) {
//                        intent = new Intent(DaoActivity.this, com.junyufr.szt.activity.AuthMainActivity.class);
//                        //intent = new Intent(DaoActivity.this, com.sheca.umandroid.PayActivity.class);
//                        intent.putExtra("loginAccount", mAccountDao.getLoginAccount().getIdentityName());
//                        intent.putExtra("loginId", mAccountDao.getLoginAccount().getIdentityCode());
//                    } else {
//                        ResultActivity.strSignature = sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, "");
//
//                        intent = new Intent(DaoActivity.this, com.junyufr.szt.activity.AuthMainActivity.class);
//                        //intent = new Intent(DaoActivity.this, com.sheca.umandroid.PayActivity.class);
//                        intent.putExtra("loginAccount", mAccountDao.getLoginAccount().getIdentityName());
//                        intent.putExtra("loginId", mAccountDao.getLoginAccount().getIdentityCode());
//                    }
//                } else {
//                    intent = new Intent(this, AuthChoiceActivity.class);
//                }
//
//                intent.putExtra("message", "dao");
//                startActivity(intent);
//                //DaoActivity.this.finish();
//            }
//        } else {
//            return;
//        }
    }


    private String getPersonID(String strCert) {
        String strRet = "";
        javasafeengine jse = new javasafeengine();

        byte[] bCert = Base64.decode(strCert);
        Certificate oCert = jse.getCertFromBuffer(bCert);
        X509Certificate oX509Cert = (X509Certificate) oCert;

        String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1", oX509Cert);
        if ("".equals(sCertUnicode) || null == sCertUnicode)
            sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148", oX509Cert);

        if ("".equals(sCertUnicode) || null == sCertUnicode) {
            strRet = "";
        } else {
            if (sCertUnicode.indexOf("SF") != -1)
                strRet = sCertUnicode.substring(2);
        }

        return strRet;
    }

    private boolean ckeckLogin() {
        if (!"".equals(strAccountName)) {
            if (!strAccountName.equals(mAccountDao.getLoginAccount().getName())) {
                logoutAccount();
                return true;
            } else
                return false;
        }

        return false;
    }

    private void logoutAccount() {
        //用户注销
        final String timeout = DaoActivity.this.getString(R.string.WebService_Timeout);
        final String urlPath = DaoActivity.this.getString(R.string.UMSP_Service_Logout);
        final Map<String, String> postParams = new HashMap<String, String>();

        try {
            //final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
            String postParam = "";
            final String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
            //处理服务返回值
            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            final String returnStr = jb.getString(CommonConst.RETURN_MSG);
            if (resultStr.equals("0")) {
                //当前账户退出,更新登录状态
                Account curAct = mAccountDao.getLoginAccount();
                curAct.setStatus(-1);   //重置登录状态为未登录状态
                mAccountDao.update(curAct);
            } else if (resultStr.equals("10012")) {
                //当前账户退出,更新登录状态
                Account curAct = mAccountDao.getLoginAccount();
                curAct.setStatus(-1);   //重置登录状态为未登录状态
                mAccountDao.update(curAct);
            } else {
                Account curAct = mAccountDao.getLoginAccount();
                curAct.setStatus(-1);   //重置登录状态为未登录状态
                mAccountDao.update(curAct);
            }
        } catch (Exception exc) {
            Log.e(CommonConst.TAG, exc.getMessage(), exc);

            Account curAct = mAccountDao.getLoginAccount();
            curAct.setStatus(-1);   //重置登录状态为未登录状态
            mAccountDao.update(curAct);
        }

    }


    private void getCertChain() {
        InputStream inCfg = null;
        Properties prop = new Properties();
        int certCount = 0;
        WebClientUtil.mCertChainList.clear();

        try {
            inCfg = this.getAssets().open("CertChain.properties");
            ;
            prop.load(inCfg);
            certCount = Integer.parseInt(prop.getProperty("CertChainNum"));

            for (int i = 0; i < certCount; i++) {
                String chainIndex = String.format("CertChain%s", i + "");
                String chainCont = prop.getProperty(chainIndex);
                WebClientUtil.mCertChainList.add(chainCont);
            }

        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (inCfg != null)
                    inCfg.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String uploadCertRecord(String appID, String certSN, String bizType, String bizTime, String message, String msgSignature) throws Exception {
        String timeout = DaoActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = DaoActivity.this.getString(R.string.UMSP_Service_UploadCertRecord);

        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("reqAppID", appID);
        postParams.put("certSN", certSN);
        postParams.put("bizType", bizType);
        postParams.put("bizTime", bizTime);
        postParams.put("message", message);
        postParams.put("msgSignature", msgSignature);
        postParams.put("msgSignatureAlgorithm", "1");
        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
        return responseStr;
    }


    private Boolean loginUMSPService(String act) throws Exception {    //重新登录UM Service
        String returnStr = "";
        try {
            //showProgDlg("获取更新数据中...");
            //异步调用UMSP服务：用户登录
            String timeout = DaoActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = DaoActivity.this.getString(R.string.UMSP_Service_Login);
            String strPass = "";
            if (mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
                strPass = getPWDHash(mAccountDao.getLoginAccount().getPassword(), null);
            else
                strPass = mAccountDao.getLoginAccount().getPassword();

            Map<String, String> postParams = new HashMap<String, String>();
            if (act.indexOf("&") != -1)
                act = act.substring(0, act.indexOf("&"));

            postParams.put("accountName", act);
            postParams.put("pwdHash", strPass);    //账户口令需要HASH并转为BASE64字符串
            postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());

            String responseStr = "";
            try {
                //清空本地缓存
                WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                        "&pwdHash=" + URLEncoder.encode(strPass, "UTF-8") +
                        "&appID=" + URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
            } catch (Exception e) {
                if (null == e.getMessage())
                    throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
                else
                    throw new Exception("用户登录失败：" + e.getMessage() + " 请重新点击登录");
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (!resultStr.equals("0"))
                return false;

        } catch (Exception exc) {
            //closeProgDlg();
            return false;
        }

        //closeProgDlg();
        return true;
    }


    private String getPWDHash(String strPWD, Cert cert) {
        String strPWDHash = "";
        boolean isFaceNoPassState = sharedPrefs.getBoolean(mAccountDao.getLoginAccount().getName() + CommonConst.FACE_NOPASS, false);

//        boolean fingerOpened = sharedPrefs.getBoolean(mAccountDao.getLoginAccount().getName() + CommonConst.SETTINGS_FINGER_ENABLED, false);


//        if (null != cert && (CommonConst.USE_FINGER_TYPE == cert.getFingertype())) {
        if (null != cert && (signByfinger|| isFaceNoPassState)) {

            if (!"".equals(cert.getCerthash())) {
                return cert.getCerthash();
//                if (!"".equals(strPWD) && strPWD.length() > 0)
//                    return strPWD;
            } else
                return strPWD;
        }

//        if (null != cert) {
//            if (!"".equals(strPWD) && strPWD.length() > 0)
//                return strPWD;
//        }

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        if (cert.getFingertype() == CommonConst.USE_FINGER_TYPE)
            bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要

        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;

    }


    private Boolean getAppValid(String appID) {
        try {
            //showProgDlg("获取更新数据中...");
            //异步调用UMSP服务：用户登录
            String timeout = DaoActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = DaoActivity.this.getString(R.string.UMSP_Service_GetAppInfo);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("appID", appID);

            String responseStr = "";
            String returnStr = "";
            try {
                //Toast.makeText(DaoActivity.this,"getAppInfo1", Toast.LENGTH_SHORT).show();
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String postParam = "appID=" + URLEncoder.encode(appID, "UTF-8");
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

                //Toast.makeText(DaoActivity.this,"responseStr:"+responseStr, Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                //Toast.makeText(DaoActivity.this,"getAppInfo Err:"+ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (!"0".equals(resultStr))
                return false;

            JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            AppInfo responseObj = new AppInfo();
            responseObj.setResult(resultStr);
            responseObj.setReturn(returnStr);
            responseObj.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
            responseObj.setName(jbRet.getString(CommonConst.PARAM_NAME));
            responseObj.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));
            if (null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
                responseObj.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
            else
                responseObj.setDescription("");
            if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))
                responseObj.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
            else
                responseObj.setContactPerson("");
            if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
                responseObj.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
            else
                responseObj.setContactPhone("");
            if (null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
                responseObj.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
            else
                responseObj.setContactEmail("");
            if (null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
                responseObj.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
            else
                responseObj.setAssignTime("");

            resultStr = responseObj.getResult();
            returnStr = responseObj.getReturn();

            strAppName = responseObj.getName();
            ((TextView) findViewById(R.id.textAppView)).setText(CommonConst.UM_APP_NAME_EX);

            AppInfoEx appInfo = new AppInfoEx();
            appInfo.setAppidinfo(strAPPID.replace("-", ""));
            appInfo.setName(strAppName);
            appInfo.setAssigntime("");
            appInfo.setContactemail("");
            appInfo.setContactperson("");
            appInfo.setContactphone("");
            appInfo.setDescription("");
            mAppInfoDao.addAPPInfo(appInfo);


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                // closeProgDlg();
                e.printStackTrace();
            }

        } catch (Exception exc) {
            //closeProgDlg();
            exc.getLocalizedMessage();
            return false;
        }

        //closeProgDlg();
        return true;
    }

    private Boolean getAppInfo(String appID) {
        if (null == appID || "".equals(appID) || "null".equals(appID))
            return false;

        appID = appID.replace("-", "");
        String[] appConfig = null;

        if ("".equals(sharedPrefs.getString(CommonConst.SETTINGS_ALL_APP_INFO, "")))
            appConfig = CommonConst.UM_APPID_CONFIG.split("-");
        else
            appConfig = sharedPrefs.getString(CommonConst.SETTINGS_ALL_APP_INFO, "").split("-");

        for (int i = 0; i < appConfig.length; i++) {
            if (appID.equals(appConfig[i]))
                return true;
        }

        return false;
    }

    private Boolean getAppInfoName(String appID) {
        if (appID.equals(CommonConst.SCAN_SIGN_NAME) || appID.equals(CommonConst.SCAN_LOGIN_NAME)) {
            strAppName = CommonConst.UM_APP_NAME;
            return true;
        }

        String curAppID = appID;
        appID = appID.replace("-", "");
        String[] appConfig = null;

        if ("".equals(sharedPrefs.getString(CommonConst.SETTINGS_ALL_APP_INFO, ""))) {
            appConfig = CommonConst.UM_APPID_CONFIG.split("-");

            for (int i = 0; i < appConfig.length; i++) {
                if (appID.equals(appConfig[0])) {
                    strAppName = CommonConst.UM_APP_NAME;
                    return true;
                } else if (appID.equals(appConfig[1])) {
                    strAppName = CommonConst.CREDIT_APP_NAMEEX;
                    return true;
                } else if (appID.equals(appConfig[2])) {
                    strAppName = CommonConst.UTEST_APP_NAME;
                    return true;
                } else if (appID.equals(appConfig[3])) {
                    strAppName = CommonConst.NETHELPER_APP_NAMEEX;
                    return true;
                } else if (appID.equals(appConfig[4])) {
                    strAppName = CommonConst.UM_SCAN_NAME;
                    return true;
                }
            }
        } else {
            if (null != mAppInfoDao.getAppInfoByAppID(curAppID)) {
                strAppName = mAppInfoDao.getAppInfoByAppID(curAppID).getName();
                return true;
            }
        }

        return false;
    }

    private String getSM2CertSubjectInfo(Cert cert) {
        String certInfo = "";
        String certItem = "";

        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);

        try {
            certItem = jse.getCertDetail(4, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "C=" + certItem + ",";

            certItem = jse.getCertDetail(5, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "O=" + certItem + ",";

            certItem = jse.getCertDetail(8, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "CN=" + certItem + ",";

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (certInfo.length() > 0)
            certInfo = certInfo.substring(0, certInfo.length() - 1);

        return certInfo;
    }

    private String getSM2CertIssueInfo(Cert cert) {
        String certInfo = "";
        String certItem = "";

        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);

        try {
            certItem = jse.getCertDetail(13, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "C=" + certItem + ",";

            certItem = jse.getCertDetail(18, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "ST=" + certItem + ",";

            certItem = jse.getCertDetail(16, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "L=" + certItem + ",";

            certItem = jse.getCertDetail(19, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "E=" + certItem + ",";

            certItem = jse.getCertDetail(17, bCert);
            if (!"".equals(certItem))
                certInfo = certInfo + "CN=" + certItem + ",";

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (certInfo.length() > 0)
            certInfo = certInfo.substring(0, certInfo.length() - 1);

        return certInfo;
    }

    private String getCertName(Cert cert) {
        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);
        String strBlank = "证书";
        String strCertName = "";

        String commonName = "";
        try {
            commonName = jse.getCertDetail(17, bCert);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            strCertName = jse.getCertDetail(17, bCert);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strCertName = "";
        }

        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())||cert.getCerttype().contains("SM2"))
            strCertName += CommonConst.CERT_SM2_NAME + strBlank;
        else
            strCertName += CommonConst.CERT_RSA_NAME + strBlank;

		/*if(CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()){
			strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
		}else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
			strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
		}*/

        if (null == cert.getCertname())
            return strCertName;

        if (cert.getCertname().isEmpty())
            return strCertName;

        if (strCertName.equals(cert.getCertname()))
            return cert.getCertname();

        return cert.getCertname();
    }

    //检测网络是否连接
    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
    		/*
    		 if(netWorkInfo != null && netWorkInfo.isAvailable()){

    			if(WebClientUtil.isPing()){
    			 //closeProgDlg();
    			  return  true;
    			}
    		}
    		 */
            //closeProgDlg();
            return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
        } catch (Exception e) {
            //closeProgDlg();

            e.printStackTrace();
            return false;
        }
    }

    private void checkNetConnected() {
        final Handler handler = new Handler(DaoActivity.this.getMainLooper());
        showProgDlg("获取数据中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if ((!strAppName.equals(CommonConst.NETHELPER_APP_NAME)) && (!strAppName.equals(CommonConst.NETHELPER_APP_ID))) {
                        if (!isNetworkAvailable(DaoActivity.this)) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(DaoActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
                                }
                            });

                            bUploadRecord = false;
                        } else {
                            if (android.os.Build.VERSION.SDK_INT > 9) {
                                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
                            }

                            boolean isNotification = sharedPrefs.getBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, true);
                            if (!isNotification) {
                                bUploadRecord = false;
                            } else {
                                bUploadRecord = true;
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                }
                            });
                        }
                    } else {
                        strAppName = CommonConst.NETHELPER_APP_NAMEEX;
                        bUploadRecord = false;

                        if (android.os.Build.VERSION.SDK_INT > 9) {
                            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                            }
                        });
                    }

                } catch (Exception ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });
                }

            }
        }).start();

    }

    private int initShcaCciStdService() {  //初始化创元中间件
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        int retcode = -100;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(DaoActivity.this);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;
            //Toast.makeText(DaoActivity.this,"retcode:"+retcode, Toast.LENGTH_LONG).show();
            if (retcode != 0)
                ShcaCciStd.gSdk = null;

        }

		/*
		try {
			Thread.sleep(3000);   //签发sm2证书等待时间需10秒
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/


        //Toast.makeText(DaoActivity.this,"initShcaCciStdService:"+retcode, Toast.LENGTH_LONG).show();
        return retcode;
    }

    private int initShcaUCMService() throws Exception {  //初始化CA手机盾中间件
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        int retcode = -1;
        byte[] bRan = null;

        String myHttpBaseUrl = DaoActivity.this.getString(R.string.UMSP_Base_Service);
        if (null == WebClientUtil.mCookieStore || "".equals(WebClientUtil.mCookieStore))
            loginUMSPService(mAccountDao.getLoginAccount().getName());

        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        bRan = jse.random(256, "SHA1PRNG", "SUN");
        gUcmSdk.setRandomSeed(bRan);
        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);
        if (retcode == 10012) {
            loginUMSPService(mAccountDao.getLoginAccount().getName());
            myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
            retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);
        }

        return retcode;
    }

    private void showGestureCheck() {
        //验证手势密码
        Intent intent = new Intent(getApplicationContext(), ValidateGestureActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, VALIDATE_GESTURE_CODE);

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

    private void startFPActivity(boolean isAuthenticate) {
        Intent intent = new Intent();
//	        if (isAuthenticate) {
//	            intent.putExtra(AuthenticatorMessage.KEY_OPERATIONT_TYPE,
//	                    AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST);
//	        }
        intent.setClass(this, FingerPrintAuthDaoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

        // this.startActivityForResult(intent, FINGER_CODE);

    }

    boolean signByfinger=false;

    private void doFingerLogin() {
        final Cert cert = certDao.getCertByID(mCertId);
        ((EditText) findViewById(R.id.textPwd)).setText(cert.getCerthash());
        signByfinger=true;
        doSign();
    }

    private void doFingerSign() {
        final Cert cert = certDao.getCertByID(mCertId);
        ((EditText) findViewById(R.id.textPwd)).setText(cert.getCerthash());
        signByfinger=true;
        doSign();
    }

    private void authIFAA(final IfaaBaseInfo ifaaBaseInfo) {
        final EtasAuthentication etasAuthentication = new EtasAuthentication(ifaaBaseInfo);

        // 认证初始化
        final EtasResult etasResult = etasAuthentication.authInit();
        if (etasResult.getCode() != IfaaCommon.IFAAErrorCodeEnum.SUCCESS) {

            //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");

            if (etasResult.getCode() == IfaaCommon.IFAAErrorCodeEnum.STATUS_NOT_ENROLLED) {

                // TODO 此时可引导用户录入指纹/人脸后在做认证操作
                Toast.makeText(DaoActivity.this, "该手机未录入指纹", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        new MyAsycnTaks() {
            @Override
            public void preTask() {
                String mTokenId = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                strInfo = String.format("%s=%s&%s=%s",
                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                        URLEncoder.encode(mTokenId),
                        URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IFAA_REQUEST),
                        URLEncoder.encode(etasResult.getMsg()));
            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(DaoActivity.this, false);
                responResult = mUnitTrust.IFAAAuth(strInfo);
            }

            @Override
            public void postTask() {
                final APPResponse response = new APPResponse(responResult);
                int resultStr = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                if (0 == resultStr) {
                    showProgDlg("IFAA认证初始化中...");
                    auth(ifaaBaseInfo);
                } else {
                    showProgDlg("IFAA认证初始化中...");
                    //Toast.makeText(DaoActivity.this, "认证初始化失败:"+resultStr+","+retMsg,Toast.LENGTH_SHORT).show();
                    auth(ifaaBaseInfo);
                }
            }
        }.execute();

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
                closeProgDlg();
                //tvShowInfos.append("认证失败 ： " + etasResult.getMsg() + "\n");

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
                                                //updateTextView("认证 onResult：" + etasResult.getCode() + "\n");
                                                MyLog.error("认证 onResult：" + etasResult.getCode());
                                                closeProgDlg();
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
                            closeProgDlg();
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

    private void ifaaAuth() {
        closeProgDlg();
        //if(LaunchActivity.isIFAAFingerOK){
        if (operateState == LOG_TYPE_LOGIN)
            doFingerLogin();
        else if (operateState == LOG_TYPE_SIGN)
            doFingerSign();
        else if (operateState == LOG_TYPE_SIGNEX)
            doFingerSign();
        else if (operateState == LOG_TYPE_ENVELOP_DECRYPT)
            doFingerSign();
        else if (operateState == LOG_TYPE_SEAL)
            doFingerSign();
		/*}else{
			if(operateState == LOG_TYPE_LOGIN){
				if(LaunchActivity.failCount >=3){
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_loign_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
				}
			}else if(operateState == LOG_TYPE_SIGN){
				if(LaunchActivity.failCount >=3){
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
				}
			}else if(operateState == LOG_TYPE_SIGNEX){
				if(LaunchActivity.failCount >=3){
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
				}
			}else if(operateState == LOG_TYPE_ENVELOP_DECRYPT){
				if(LaunchActivity.failCount >=3){
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
				}
			}else if(operateState == LOG_TYPE_SEAL){
				if(LaunchActivity.failCount >=3){
					findViewById(R.id.textPwd).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.btn_ok).setVisibility(RelativeLayout.VISIBLE);
					findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.gesture_image).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
					findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
				}
			}
		}
		*/
    }

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

    private final String TOKENFILE = "user";
    private final String KEY_TOKEN = "token";

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(TOKENFILE, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, "");
    }

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }


    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }

}