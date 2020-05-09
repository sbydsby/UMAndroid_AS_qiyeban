package com.sheca.umandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ifaa.sdk.api.AuthenticatorManager;
import com.ifaa.sdk.auth.AuthenticatorCallback;
import com.ifaa.sdk.auth.message.AuthenticatorMessage;
import com.ifaa.sdk.auth.message.AuthenticatorResponse;
import com.igexin.sdk.PushManager;
import com.intsig.idcardscancaller.CardScanActivity;
import com.junyufr.szt.activity.AuthMainActivity;
import com.sheca.fingerui.FingerPrintAuthLoginActivity;
import com.sheca.fingerui.FingerPrintToast;
import com.sheca.fingerui.IFAAFingerprintOpenAPI;
import com.sheca.fingerui.MainActivity.Process;
import com.sheca.javasafeengine;
import com.sheca.umandroid.adapter.AccountAppInfoAdapter;
import com.sheca.umandroid.adapter.AppInfoAdapter;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.AppInfoDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.layout.InputMethodRelativeLayout;
import com.sheca.umandroid.layout.InputMethodRelativeLayout.OnSizeChangedListenner;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.model.AccountInfo;
import com.sheca.umandroid.model.AppInfo;
import com.sheca.umandroid.model.AppInfoEx;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.CertApplyInfoLite;
import com.sheca.umandroid.presenter.BasePresenter;
import com.sheca.umandroid.presenter.LoginController;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A login screen that offers login via account/password.
 */
public class LoginActivity extends Activity implements OnSizeChangedListenner {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
//	private static final String[] DUMMY_CREDENTIALS = new String[] {
//			"foo@example.com:hello", "bar@example.com:world" };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references
    private EditText mAccountView;
    private EditText mPasswordView;
    //private View mProgressView;
    private View mLoginFormView;
    private InputMethodRelativeLayout layout;
    private InputMethodManager manager;

    private String mStrAccountView = "";
    private String mStrPasswordView = "";

    //DB Access Object
    private AccountDao mAccountDao = null;
    private CertDao mCertDao = null;
    private AppInfoDao mAppInfoDao = null;

    private ProgressDialog progDialog = null;
    private AlertDialog certListDialog = null;
    private SharedPreferences sharedPrefs;
    //Error Message
    private String mError = "";

    private boolean mIsDao = false;       //第三方接口调用标记
    private boolean mIsScanDao = false;   //第三方扫码接口调用标记
    private boolean mIsReg = false;       //是否新注册账户

    private boolean mIsCompanyType = false;    //是否企业账户登录
    private boolean mIsGetAppList = false;     //是否已获取应用列表
    private boolean mIsOneApp = false;         //是否账户只有单个应用
    private boolean mIsRegAct = false;         //是否自动注册账户
    private String m_AppID = "";              //登录应用ID(企业账户)

    //用户账户属性全局变量
    private int m_ActState;         //用户状态
    private String m_ActName;          //用户姓名
    private String m_ActIdentityCode;  //用户身份证
    private String m_ActCopyIDPhoto;   //用户头像
    private int m_ActType;          //用户账户类别（1：个人；2：单位）
    private String m_OrgName;          //企业单位名称

    private List<Map<String, String>> mData = null;
    private List<Map<String, String>> mAppData = null;

    private Process curProcess = Process.REG_GETREQ;
    //	private String userid = "test";
    private String secData = "";

    private UniTrust uniTrust;


    private String mUID = "";


    LinearLayout ll_pwd, ll_sms;
    EditText et_sms;
    Button btn_sms;


    boolean isSMSLogin = false;//false表示密码登录 true表示验证码登录


    private IFAAFingerprintOpenAPI.Callback callback = new IFAAFingerprintOpenAPI.Callback() {
        @Override
        public void onCompeleted(int status, final String info) {
            switch (curProcess) {
                case AUTH_GETREQ:
                    LoginActivity.this.runOnUiThread(new Runnable() {
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
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.equals("Success")) {
                                new FingerPrintToast(LoginActivity.this, FingerPrintToast.ST_AUTHSUCCESS).show("");
                            } else {
                                new FingerPrintToast(LoginActivity.this, FingerPrintToast.ST_AUTHFAIL).show("验证指纹失败");
                            }
                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(LoginActivity.this, 0);
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
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(LoginActivity.this, status);
        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            curProcess = Process.AUTH_SENDRESP;

            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                IFAAFingerprintOpenAPI.getInstance().sendAuthResponeAsyn(data, secData, callback);
            } else {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //new FingerPrintToast(LoginActivity.this, FingerPrintToast.ST_AUTHTEEFAIL).show("验证指纹失败");
                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(LoginActivity.this, 0);

                        if (LaunchActivity.isIFAAFingerOK) {
                            doFingerLogin();
                        } else {
                            if (LaunchActivity.failCount >= 3) {
                                findViewById(R.id.password).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.passwordview).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.sign_in_button).setVisibility(RelativeLayout.VISIBLE);
                                findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                                findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
                            }
                        }
                    }
                });
            }

        }
    };
    private SealInfoDao mSealInfoDao;
    private com.sheca.umplus.model.Account accountPlus;

    private void loadLisence() {
        new Thread() {
            public void run() {
//                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
//                uniTrust.setStrPubKey(CommonConst.UM_APP_PUBLIC_KEY);
                uniTrust.setUMSPServerUrl(CommonConst.UM_APP_UMSP_SERVER, AccountHelper.getUMSPAddress(LoginActivity.this));
                uniTrust.setUCMServerUrl(CommonConst.UM_APP_UCM_SERVER, AccountHelper.getUCMAddress(LoginActivity.this));
                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(LoginActivity.this));
                Log.d("unitrust", mStrVal);
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                            Toast.makeText(LoginActivity.this, "应用初始化失败", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            SharedPreferences sharedPrefs = LoginActivity.this.getSharedPreferences("sheca_settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("authKeyID", CommonConst.UM_APP_AUTH_KEY);
                            editor.commit();
                            attemptAccountLogin(mAccountView.getText().toString(), mPasswordView.getText().toString());
                        }

                    }
                });


            }
        }.start();
    }


    private void loadLisence(final boolean isSendSms) {
        new Thread() {
            public void run() {
                UniTrust uniTrust = new UniTrust(LoginActivity.this, false);
//                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
//                uniTrust.setStrPubKey(CommonConst.UM_APP_PUBLIC_KEY);
                uniTrust.setUMSPServerUrl(CommonConst.UM_APP_UMSP_SERVER, AccountHelper.getUMSPAddress(LoginActivity.this));
                uniTrust.setUCMServerUrl(CommonConst.UM_APP_UCM_SERVER, AccountHelper.getUCMAddress(LoginActivity.this));
                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(LoginActivity.this));
                Log.d("unitrust", mStrVal);
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                            Toast.makeText(LoginActivity.this, "应用初始化失败", Toast.LENGTH_LONG).show();
                            return;
                        } else {

                            SharedPreferences sharedPrefs = LoginActivity.this.getSharedPreferences("sheca_settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("authKeyID", CommonConst.UM_APP_AUTH_KEY);
                            editor.commit();


                            if (isSendSms) {
                                getMsgCode();
                            } else {
                                if (mIsReg) {
                                    regNewAccount();
                                } else {
                                    loginAccountByCode();  //账户短信验证码登录
                                }
                            }
                        }

                    }
                });


            }
        }.start();
    }


    private void loginAccountByCode() {
        mAccountView.setError(null);
        et_sms.setError(null);

        String account = mAccountView.getText().toString();
        String msgcode = et_sms.getText().toString();

        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_account_required));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_account);
        }

        if (TextUtils.isEmpty(msgcode)) {
            et_sms.setError(getString(R.string.error_invalid_code));
            focusView = et_sms;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_code);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
            Toast.makeText(LoginActivity.this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {
            showProgDlg("用户短信验证码登录中...");

            if (!userLoginByValidationCode(account, msgcode)) {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }
            closeProgDlg();

        }
    }


    /**
     * 验证码动态登录的请求
     *
     * @param mobile
     * @param code
     * @return
     */

    public boolean userLoginByValidationCode(final String mobile, final String code) {
        String returnStr = "";

        //异步调用UMSP服务：用户登录
        String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_LoginByDynamicCode);

//		Map<String,String> postParams = new HashMap<String,String>();
//		postParams.put(CommonConst.PARAM_ACCOUNT_NAME, mobile);
//		postParams.put(CommonConst.PARAM_APPID, CommonConst.UM_APPID);
//		postParams.put(CommonConst.PARAM_CODE, code);
//		postParams.put(CommonConst.PARAM_MOBILE, mobile);
//


        //清空本地缓存
//			WebClientUtil.cookieStore = null;

//			String postParam = "";
//			postParam = "accountName="+URLEncoder.encode(mobile, "UTF-8")+
//					    "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8")+
//		                "&code="+URLEncoder.encode(code, "UTF-8")+
//                        "&mobile="+URLEncoder.encode(mobile, "UTF-8") ;
//			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strInfo = ParamGen.getUserLoginByValidationCodeParams(mobile, code);
                    UniTrust dao = new UniTrust(LoginActivity.this, false); //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.LoginByMAC(strInfo);

                    final APPResponse response = new APPResponse(responseStr);
                    int resultStr = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (resultStr == 0) {
                        String token = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                        final com.sheca.umplus.model.Account accountPlus = getPersonInfo(token, mobile);
//					final String mAccountInfo = getPersonActive(token, mobile);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, token);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, accountPlus.getIdentityName());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, accountPlus.getStatus());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_TYPE, accountPlus.getType());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, accountPlus.getActive());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, accountPlus.getIdentityCode());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_ACCOUNT_UID, accountPlus.getAccountuid());

                        int status = Integer.valueOf(accountPlus.getStatus());
                        int active = Integer.valueOf(accountPlus.getActive());
                        int type = Integer.valueOf(accountPlus.getType());
                        int saveType = Integer.valueOf(accountPlus.getSaveType());
                        int certType = Integer.valueOf(accountPlus.getCertType());
                        int loginType = Integer.valueOf(accountPlus.getLoginType());

                        com.sheca.umandroid.model.Account account = new com.sheca.umandroid.model.Account(
                                accountPlus.getName(),
                                accountPlus.getPassword(),
                                status,
                                active,
                                accountPlus.getIdentityName(),
                                accountPlus.getIdentityCode(),
                                accountPlus.getCopyIDPhoto(),
                                type,
                                accountPlus.getAppIDInfo(),
                                accountPlus.getOrgName(),
                                saveType,
                                certType,
                                loginType);

                        if (null == mAccountDao) {
                            mAccountDao = new AccountDao(getApplicationContext());
                        }
                        mAccountDao.add(account);

                        //com.sheca.umandroid.util.WebClientUtil.mCookieStore = com.sheca.umplus.util.WebClientUtil.mCookieStore;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //获取枚举证书及枚举印章信息,只有老版本才需要进行备份处理
                                int mLocalCode = SharePreferenceUtil.getInstance(LoginActivity.this).getInt(CommonConst.PARAM_V26_DBCHECK);
                                if (mLocalCode < 0) {
                                    getEnumInfo(accountPlus);
                                    //备份完成后，设置为1
                                    SharePreferenceUtil.getInstance(LoginActivity.this).setInt(CommonConst.PARAM_V26_DBCHECK, 1);
                                }


                                //
                                if (!mIsReg) {

                                    AccountHelper.clearAllUserData(LoginActivity.this);
                                    String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, mobile);
                                    AccountHelper.uid = mUID;

                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, accountPlus.getIdentityName());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, accountPlus.getStatus());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_TYPE, accountPlus.getType());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, accountPlus.getActive());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, accountPlus.getIdentityCode());


                                    boolean isFirstLogin = SharePreferenceUtil.getInstance(getApplicationContext()).getBoolean((CommonConst.FIRST_SMS_LOGIN + mobile));


                                    if (!isFirstLogin) {
                                        Intent intent = new Intent(LoginActivity.this, SetPasswordActivity.class);
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("Message", "用户登录成功");
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    }
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, SetPasswordActivity.class);
                                    startActivity(intent);
                                    AccountHelper.clearAllUserData(LoginActivity.this);
                                    String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, mobile);
                                    AccountHelper.uid = mUID;
                                    LoginActivity.this.finish();
                                }


                            }


                        });


                    } else if (resultStr == 10010) {
//	    	m_ActState = getPersonalInfo();
//		    mAccountDao.add( new Account(mobile, m_PWDHash, m_ActState,0,m_ActName,m_ActIdentityCode,m_ActCopyIDPhoto,m_ActType,CommonConst.UM_APPID,m_OrgName,CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_MSG));

//		    try {
//				getAllAppInfos(mobile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							Intent intent = new Intent(SMSLoginActivity.this, PasswordActivity.class);
//							intent.putExtra("Account", mobile);
//							startActivity(intent);
//							SMSLoginActivity.this.finish();
//						}
//					});

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

        return true;
    }


    private void regNewAccount() {
        mAccountView.setError(null);
        et_sms.setError(null);

        String account = mAccountView.getText().toString();
        String msgcode = et_sms.getText().toString();

        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_account_required));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_account);
        }

//		if(TextUtils.isEmpty(msgcode)){
//			etSMSCode.setError(getString(R.string.error_invalid_code));
//			focusView = etSMSCode;
//			cancel = true;
//
//			strErrTip = getString(R.string.error_invalid_code);
//		}

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
            Toast.makeText(LoginActivity.this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {
            showProgDlg("账户注册中...");
//            progDialog.showProgDlgCert("账户注册中...");
            regUMAccount(account, msgcode);
            if (!mIsReg) {
                Toast.makeText(LoginActivity.this, "账户注册失败", Toast.LENGTH_SHORT).show();
            }

            closeProgDlg();
//            progDialog.closeProgDlgCert();
        }
    }

    /**
     * 注册账户的网络请求，获取uid
     *
     * @param account
     * @param msgcode
     */
    private void regUMAccount(final String account, final String msgcode) {
        //javasafeengine jse = new javasafeengine();
        //byte[] bRan = jse.random(8, "SHA1PRNG", "SUN");
        //m_Password = new String(bRan);


        //异步调用UMSP服务：用户注册
//			String timeout = SMSLoginActivity.this.getString(R.string.WebService_Timeout);
//			String urlPath = SMSLoginActivity.this.getString(R.string.UMSP_Service_RegisterPersonalAccount);
//
//			Map<String,String> postParams = new HashMap<String,String>();
//			postParams.put("accountName", mAccount);
//			postParams.put("pwdHash", getPWDHash(mPassword));    //账户口令需要HASH并转为BASE64字符串
//			postParams.put("appID", CommonConst.UM_APPID);
//			postParams.put("validationCode", mMsgCode);


        //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

//				String postParam = "accountName="+URLEncoder.encode(mAccount, "UTF-8")+
//                                   "&pwdHash="+URLEncoder.encode( getPWDHash(mPassword), "UTF-8")+
//                                   "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8")+
//                                   "&validationCode="+URLEncoder.encode( mMsgCode, "UTF-8");
//				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strInfo = ParamGen.getRegisterAccountParams(account, msgcode);
                    UniTrust dao = new UniTrust(LoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
                    final String responseStr = dao.RegisterPersonalAccount(strInfo);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            APPResponse response = new APPResponse(responseStr);
                            int resultStr = response.getReturnCode();
                            String returnStr = response.getReturnMsg();

                            if (resultStr == 0) {
                                //						Message message = new Message();
                                //						message.what = 2;
                                //						handler.sendMessage(message);
                                mIsReg = true;
                                JSONObject jbRet = response.getResult();
                                mUID = jbRet.getString(CommonConst.PARAM_CCOUNT_UID);
//                                getValidationCode(account);
                                String ranPwdHash = account + "" + account;

                                attemptAccountLogin(account, ranPwdHash);
                            } else {

                                Toast.makeText(LoginActivity.this, returnStr, Toast.LENGTH_SHORT).show();
//                                mIsReg = false;
                            }

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 获取验证码的方法
     */
    private void getMsgCode() {
        final Handler handler = new Handler(LoginActivity.this.getMainLooper());
        final String account = mAccountView.getText().toString();
        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        mAccountView.setError(null);
        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_account_required));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_account);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
            btn_sms.setText("获取验证码");

            Toast.makeText(this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {

            //1：检测账户是否注册、未注册进行注册、已注册直接登录
            checkAccounts(account);

        }
    }


    /**
     * 检测账户是否注册
     *
     * @param phone
     */
    private void checkAccounts(final String phone) {


//			//异步调用UMSP服务：检测当前账户是否已注册
//			String timeout = SMSLoginActivity.this.getString(R.string.WebService_Timeout);
//			String urlPath = SMSLoginActivity.this.getString(R.string.UMSP_Service_IsAccountExisted);
//
//			Map<String,String> postParams = new HashMap<String,String>();
//			postParams.put("accountName", phone);
//			postParams.put("appID", CommonConst.UM_APPID);
        final String strInfo = ParamGen.getCheckIsAccountExistedParams(phone);


        //清空本地缓存
//				WebClientUtil.cookieStore = null;
//				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//				String postParam = "accountName="+URLEncoder.encode(phone, "UTF-8")+
//	                               "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
//				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UniTrust dao = new UniTrust(LoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.IsAccountExisted(strInfo);

                    JSONObject jb = JSONObject.fromObject(responseStr);
                    final String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    String returnStr = jb.getString(CommonConst.RETURN_MSG);
                    final String result = jb.getString(CommonConst.RETURN_RESULT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultStr.equals("0")) {      //账户已经存在

                                if (result.equals("true")) {
                                    mIsReg = false;
                                } else {

                                    mIsReg = true;
                                }


                                getValidationCode(phone);
                            } else {
                                Toast.makeText(getApplicationContext(), "获取账户信息失败", Toast.LENGTH_LONG).show();

//                                //注册账户
//                                regNewAccount();
                            }
                            btn_sms.setText("获取验证码");
                            //获取验证码方法

                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 获取验证码的网络请求
     *
     * @param phone
     */
    private void getValidationCode(String phone) {

        //异步调用UMSP服务：获取短信验证码
//			String timeout = SMSLoginActivity.this.getString(R.string.WebService_Timeout);
//			String urlPath = SMSLoginActivity.this.getString(R.string.UMSP_Service_GetValidationCode);
//			Map<String,String> postParams = new HashMap<String,String>();
//			postParams.put("mobile", phone);
//			postParams.put("codeType", "0");


        //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
        String codeType = "";
        if (mIsReg)
            codeType = "1";
        else
            codeType = "2";

//				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8")+
//                                   "&codeType="+URLEncoder.encode(codeType, "UTF-8");
//                                   //"&authKeyID="+URLEncoder.encode(CommonConst.YGT_APP_AUTH_KEY, "UTF-8");
//		        responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));

        final String strInfo = ParamGen.getValidationCodeParams(phone, codeType);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UniTrust dao = new UniTrust(LoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.GetMAC(strInfo);

                    JSONObject jb = JSONObject.fromObject(responseStr);
                    final String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    final String returnStr = jb.getString(CommonConst.RETURN_MSG);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultStr.equals("0")) {

                                //findViewById(R.id.textVoice).setVisibility(RelativeLayout.VISIBLE);
                                showCountDown(COUNT_DOWN_NUM);   //显示倒计时
                                Toast.makeText(LoginActivity.this, "短信验证码发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "短信验证码错误" + returnStr, Toast.LENGTH_SHORT).show();
                                btn_sms.setText("获取验证码");
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
    private int count = COUNT_DOWN_NUM;

    private Timer timer = new Timer();
    private TimerTask task = null;

    private void showCountDown(final int countDownNum) {
        btn_sms.setEnabled(false);
        btn_sms.setText("等待60秒");
        //mHandler.postDelayed(mRunnable, 1000*60);
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };

        timer.schedule(task, 0, 1000);
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    update();
                    break;
                case 2:
                    mIsReg = true;
//	            	RegAccountSuccess();
                    break;
            }

            super.handleMessage(msg);
        }

    };

    private void update() {
        count--;
        if (count > 0) {
            btn_sms.setText("等待" + count + "秒");
        } else {
            btn_sms.setText("获取验证码");
            btn_sms.setEnabled(true);
            timer.cancel();
            timer = null;
            task.cancel();
            task = null;
            count = COUNT_DOWN_NUM;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_login_v3);

        uniTrust = new UniTrust(this, false);


        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // //取得InputMethodRelativeLayout组件
        layout = (InputMethodRelativeLayout) this.findViewById(R.id.loginpage);
        // //设置监听事件
        layout.setOnSizeChangedListenner(this);


        ll_pwd = (LinearLayout) this.findViewById(R.id.ll_pwd);

        ll_sms = (LinearLayout) this.findViewById(R.id.ll_sms);


        et_sms = (EditText) this.findViewById(R.id.et_sms);
        btn_sms = (Button) this.findViewById(R.id.btn_sms);
        btn_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_sms.setText("正在发送...");

                loadLisence(true);

            }
        });

        ImageView cancelButton = (ImageView) this.findViewById(R.id.login_exit);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsDao || mIsScanDao) {
                    LoginActivity.this.finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            }
        });

        TextView switch_service = (TextView) this.findViewById(R.id.switch_service);
        switch_service.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SwitchServerActivity.class);
                startActivity(intent);
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("message") != null)
                mIsDao = true;
            if (extras.getString("scan") != null)
                mIsScanDao = true;
            if (extras.getString("AccName") != null)
                mStrAccountView = extras.getString("AccName");
            if (extras.getString("AccPwd") != null)
                mStrPasswordView = extras.getString("AccPwd");
            if (extras.getString("isReg") != null)
                mIsReg = true;
        }

        //判断账号是否已登录。

        mAccountDao = new AccountDao(LoginActivity.this);
        mCertDao = new CertDao(LoginActivity.this);
        mAppInfoDao = new AppInfoDao(LoginActivity.this);
        mSealInfoDao = new SealInfoDao(LoginActivity.this);
        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);

        mIsGetAppList = false;
        mIsCompanyType = false;
        mIsRegAct = false;
        m_AppID = CommonConst.UM_APPID;

        LaunchActivity.isIFAAFingerOpend = sharedPrefs.getBoolean(CommonConst.SETTINGS_FINGER_OPENED, false);


        mAccountView = (EditText) findViewById(R.id.account);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText("");
        mAccountView.clearFocus();

        if (!"".equals(DaoActivity.strAccountName)) {
            mAccountView.setText(DaoActivity.strAccountName);
            mAccountView.setEnabled(false);
            mAccountView.setFocusable(false);
        }
        if (!"".equals(mStrAccountView))
            mAccountView.setText(mStrAccountView);

        if (!"".equals(DaoActivity.strAccountPwd)) {
            mPasswordView.setText(DaoActivity.strAccountPwd);
            //mPasswordView.setFocusable(true);
        }
        if (!"".equals(mStrPasswordView))
            mPasswordView.setText(mStrPasswordView);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);  //账户登录
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSMSLogin) {
                    loadLisence(false);
                } else {
                    mIsReg=false;
                    attemptLogin();
                }
            }
        });


        TextView mRegAct = (TextView) findViewById(R.id.regaccount);   //账户短信验证码登录
        mRegAct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isSMSLogin) {//切换到密码登录
                    isSMSLogin = false;
                    mRegAct.setText("验证码登录");
                    ll_pwd.setVisibility(View.VISIBLE);
                    ll_sms.setVisibility(View.GONE);

                } else {//切换到验证码登录
                    isSMSLogin = true;
                    mRegAct.setText("账户密码登录");
                    ll_pwd.setVisibility(View.GONE);
                    ll_sms.setVisibility(View.VISIBLE);

                }


//                Intent intent = new Intent(LoginActivity.this, SMSLoginActivity.class);
//                Bundle bundle = new Bundle();
//                if (mIsDao)
//                    bundle.putString("message", "dao");
//
//                if (!TextUtils.isEmpty(mAccountView.getText().toString())) {
//                    bundle.putString("mobile", mAccountView.getText().toString());
//                }
//
//                intent.putExtras(bundle);
//                startActivity(intent);
//                LoginActivity.this.finish();

            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);

        if ((!"".equals(DaoActivity.strAccountName)) && (!"".equals(DaoActivity.strAccountPwd))) {  //实现自动登录
            LaunchActivity.isIFAAFingerOK = false;
            attemptLogin();
        }

        if ((!"".equals(mStrAccountView)) && (!"".equals(mStrPasswordView))) {  //实现自动登录
            LaunchActivity.isIFAAFingerOK = false;
            attemptLogin();
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAccountView.setText(sharedPrefs.getString(CommonConst.SETTINGS_LOGIN_ACT_NAME, ""));


        LaunchActivity.failCount = 0;

        if (LaunchActivity.isIFAAFingerOpend) {
            findViewById(R.id.password).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.passwordview).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

            LaunchActivity.isIFAAFingerOK = false;
            if (null == LaunchActivity.authenticator)
                LaunchActivity.authenticator = AuthenticatorManager.create(this, com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT);

            ((ImageView) this.findViewById(R.id.pwdkeyboard)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.password).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.passwordview).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.sign_in_button).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);

                    ((EditText) findViewById(R.id.password)).setText("");
                    LaunchActivity.isIFAAFingerOK = false;
                }
            });

            ((ImageView) this.findViewById(R.id.finger_image)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFingerCheck();
                }
            });

            ((TextView) this.findViewById(R.id.finger_txt)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFingerCheck();
                }
            });
        } else {
            findViewById(R.id.password).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.passwordview).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.finger_image).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.finger_txt).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.pwdkeyboard).setVisibility(RelativeLayout.GONE);
        }

        /*((EditText) findViewById(R.id.account))*/


        String username = SharePreferenceUtil.getInstance(this).getString(CommonConst.PARAM_USERNAME);
        if (!TextUtils.isEmpty(username))
            mAccountView.setText(username);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        String username = SharePreferenceUtil.getInstance(this).getString(CommonConst.PARAM_USERNAME);
//        if(!TextUtils.isEmpty(username))
//        mAccountView.setText(username);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mIsDao || mIsScanDao) {
                    LoginActivity.this.finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }

                break;
        }

        return true;
    }

    @Override
    public void onSizeChange(boolean flag, int w, int h) {
        if (flag) {// 键盘弹出时
            layout.setPadding(0, -600, 0, 0);
        } else { // 键盘隐藏时
            layout.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null
                    && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    private void showAppList() {
        final Handler handler = new Handler(LoginActivity.this.getMainLooper());

        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //showProgDlg("获取应用列表中...");
                    //loginUMSPService("13917755297","11111111");
                    getAllAppInfoCount();

                    if (null != mData)
                        changeAppList();
                    else
                        Toast.makeText(LoginActivity.this, "获取应用列表错误", Toast.LENGTH_SHORT).show();

                    closeProgDlg();
                }
            });

        } catch (final Exception exc) {
            exc.getLocalizedMessage();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                    Toast.makeText(LoginActivity.this, "获取应用列表错误:" + exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void showAccountAppList(final String account, final String password) {
        final Handler handler = new Handler(LoginActivity.this.getMainLooper());

        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!checkAccount(account)) {
                        if (mIsRegAct) {
                            showRegAccount(account);
                        }

                        return;
                    } else {
                        showProgDlg("获取应用中...");

                        try {
                            getAllAccountInfos(account, password);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (null != mAppData) {
                            if (mIsOneApp)
                                attemptAccountLogin(account, password);
                            else
                                changeAccountAppList(account, password);
                        } else {
                            if (!"".equals(mError))
                                Toast.makeText(LoginActivity.this, mError, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(LoginActivity.this, "获取应用列表错误", Toast.LENGTH_SHORT).show();
                        }

                        closeProgDlg();
                    }
                }
            });

        } catch (final Exception exc) {
            exc.getLocalizedMessage();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    closeProgDlg();
                    Toast.makeText(LoginActivity.this, "获取应用列表错误:" + exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void changeAppList() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certListView = inflater.inflate(R.layout.certlist, null);
        ListView list = (ListView) certListView.findViewById(R.id.certlist);
        AppInfoAdapter adapter = null;

        try {
            adapter = new AppInfoAdapter(LoginActivity.this, mData);
            list.setAdapter(adapter);

            Builder builder = new Builder(LoginActivity.this);
            builder.setIcon(R.drawable.view);
            builder.setTitle("请选择应用");
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
            Toast.makeText(LoginActivity.this, "获取应用列表错误", Toast.LENGTH_SHORT).show();
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strAppName = mData.get(position).get("commonname");
                //((TextView) findViewById(R.id.appname)).setText(strAppName);
                m_AppID = mData.get(position).get("id");

                mIsGetAppList = true;
                certListDialog.dismiss();
            }
        });
    }

    private void changeAccountAppList(final String account, final String password) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certListView = inflater.inflate(R.layout.certlist, null);
        ListView list = (ListView) certListView.findViewById(R.id.certlist);
        AccountAppInfoAdapter adapter = null;

        try {
            adapter = new AccountAppInfoAdapter(LoginActivity.this, mAppData);
            list.setAdapter(adapter);

            Builder builder = new Builder(LoginActivity.this);
            builder.setIcon(R.drawable.view);
            builder.setTitle("请选择应用");
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
            Toast.makeText(LoginActivity.this, "获取应用列表错误", Toast.LENGTH_SHORT).show();
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strAppName = mAppData.get(position).get("appname");
                //((TextView) findViewById(R.id.appname)).setText(strAppName);
                m_AppID = mAppData.get(position).get("appid");

                mIsGetAppList = true;
                if (Integer.parseInt(mAppData.get(position).get("acttype")) == CommonConst.ACCOUNT_TYPE_PERSONAL)
                    mIsCompanyType = false;
                else
                    mIsCompanyType = true;

                attemptAccountLogin(account, password);
                certListDialog.dismiss();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mAccountView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String account = mAccountView.getText().toString();
        final String password = mPasswordView.getText().toString();
        Handler handler = new Handler(LoginActivity.this.getMainLooper());

        boolean cancel = false;
        View focusView = null;

        if (mIsCompanyType) {   //企业账户登录判断
            if (TextUtils.isEmpty(account)) {
                mAccountView.setError(getString(R.string.error_account_required));
                focusView = mAccountView;
                cancel = true;
            }

            if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
        } else {
            // Check for a valid account name.
            if (TextUtils.isEmpty(account)) {
                mAccountView.setError(getString(R.string.error_account_required));
                focusView = mAccountView;
                cancel = true;
            } else if (!isAccountValid(account)) {
                mAccountView.setError(getString(R.string.error_invalid_account));
                focusView = mAccountView;
                cancel = true;
            }

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            loadLisence();


//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mIsDao || mIsScanDao) {
//                        attemptAccountLogin(account, password);
//                    } else {
//                        if (mIsGetAppList) {
//                            if (mIsOneApp)
//                                attemptAccountLogin(account, password);
//                            else
//                                changeAccountAppList(account, password);
//                        } else {
//                            showAccountAppList(account, password);
//                        }
//                    }
//                }
//            });
        }
    }

    public void attemptAccountLogin(final String account, final String password) {

        showProgDlg("账户登录中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(account, password));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });

                    APPResponse response = new APPResponse(strMsg);
                    int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();

                    String tokenID;

                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {


                        JSONObject jbRet = response.getResult();
                        String r = com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID +
                                ":" + jbRet.getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID) + "\n";
                        Log.d("unitrust", r);
                        tokenID = jbRet.getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                        AccountHelper.clearAllUserData(LoginActivity.this);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, tokenID);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, account);
                        PushManager.getInstance().bindAlias(LoginActivity.this, account);//注册个推别名
                        DaoActivity.strAccountName = account;

                        getPersoninfo(tokenID, account);
                         /*
                        try {
					       Thread.sleep(3000);
				         }catch (InterruptedException e) {

				         }*/
                        registerXGPush(account);

                        //com.sheca.umandroid.util.WebClientUtil.mCookieStore = com.sheca.umplus.util.WebClientUtil.mCookieStore;


                        if (mIsReg) {
                            Intent intent = new Intent(LoginActivity.this, SetPasswordActivity.class);
                            startActivity(intent);
//                        AccountHelper.clearAllUserData(LoginActivity.this);
//                        String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
//                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
//                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
//                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, mobile);
//                        AccountHelper.uid = mUID;
                            LoginActivity.this.finish();
                        } else {


                            SharePreferenceUtil.getInstance(getApplicationContext()).setBoolean((CommonConst.FIRST_SMS_LOGIN + account), true);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    /**
     * 获取枚举信息（证书及印章）
     */
    private void getEnumInfo(final com.sheca.umplus.model.Account accountPlus) {

        //获取证书
        new MyAsycnTaks() {
            private String strInfoGetAllCert = "";

            @Override
            public void preTask() {
                String mTokenID = SharePreferenceUtil.getInstance(LoginActivity.this).getString(CommonConst.PARAM_TOKEN);
//                strInfo = ParamGen.getEnumCertIDs(mTokenID);
                String mAccountName = accountPlus.getName();
                strInfoGetAllCert = ParamGen.getAcountAllCerts(mTokenID, mAccountName);
                Log.e("TEST_DATABASE", "登录界面同步开始执行2." + strInfoGetAllCert);

            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(LoginActivity.this, false);
                List<com.sheca.umplus.model.Cert> mCertList = mUnitTrust.getAcountAllCerts(strInfoGetAllCert);
                Log.e("TEST_DATABASE", "登录界面同步certmList" + mCertList + "");
                if (mCertList != null && mCertList.size() != 0) {
                    Log.e("TEST_DATABASE", "登录界面同步certmList" + mCertList.size() + "");
                    for (int i = 0; i < mCertList.size(); i++) {
                        com.sheca.umandroid.model.Cert mCert = new com.sheca.umandroid.model.Cert();
                        mCert.setId(mCertList.get(i).getId());
                        mCert.setCertsn(mCertList.get(i).getCertsn());
                        mCert.setEnvsn(mCertList.get(i).getEnvsn());
                        mCert.setPrivatekey(mCertList.get(i).getPrivatekey());
                        mCert.setCertificate(mCertList.get(i).getCertificate());
                        mCert.setKeystore(mCertList.get(i).getKeystore());
                        mCert.setEnccertificate(mCertList.get(i).getEnccertificate());
                        mCert.setEnckeystore(mCertList.get(i).getEnckeystore());
                        mCert.setCertchain(mCertList.get(i).getCertchain());
                        mCert.setStatus(mCertList.get(i).getStatus());
                        mCert.setAccountname(mCertList.get(i).getAccountname());
                        mCert.setNotbeforetime(mCertList.get(i).getNotbeforetime());
                        mCert.setValidtime(mCertList.get(i).getValidtime());
                        mCert.setUploadstatus(mCertList.get(i).getUploadstatus());
                        mCert.setCerttype(mCertList.get(i).getCerttype());
                        mCert.setSignalg(mCertList.get(i).getSignalg());
                        mCert.setContainerid(mCertList.get(i).getContainerid());
                        mCert.setAlgtype(mCertList.get(i).getAlgtype());
                        mCert.setSavetype(mCertList.get(i).getSavetype());
                        mCert.setDevicesn(mCertList.get(i).getDevicesn());
                        mCert.setCertname(mCertList.get(i).getCertname());
                        mCert.setCerthash(mCertList.get(i).getCerthash());
                        mCert.setFingertype(mCertList.get(i).getFingertype());
                        mCert.setSealsn(mCertList.get(i).getSealsn());
                        mCert.setSealstate(mCertList.get(i).getSealstate());
                        mCert.setCertlevel(mCertList.get(i).getCertlevel());

                        mCertDao.addCert(mCert, accountPlus.getName());
                    }

                }
            }

            @Override
            public void postTask() {

            }
        }.execute();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        //获取印章信息
        new MyAsycnTaks() {
            private String strInfoGetAllSeal = "";

            @Override
            public void preTask() {
                String mTokenID = SharePreferenceUtil.getInstance(LoginActivity.this).getString(CommonConst.PARAM_TOKEN);
//                strInfo = ParamGen.getEnumCertIDs(mTokenID);
                String mAccountName = accountPlus.getName();
                strInfoGetAllSeal = ParamGen.getAcountAllCerts(mTokenID, mAccountName);
                Log.e("TEST_DATABASE", "登录界面同步开始执行yin张." + strInfoGetAllSeal);
            }

            @Override
            public void doinBack() {
                UniTrust mUnitTrust = new UniTrust(LoginActivity.this, false);
                List<com.sheca.umplus.model.SealInfo> mSealList = mUnitTrust.getAcountAllSealInfos(strInfoGetAllSeal);
                Log.e("TEST_DATABASE", "登录界面同步开始执行yin张." + mSealList + "");
                if (mSealList != null && mSealList.size() != 0) {

                    for (int i = 0; i < mSealList.size(); i++) {
                        com.sheca.umandroid.model.SealInfo mSealInfo = new com.sheca.umandroid.model.SealInfo();
                        mSealInfo.setId(mSealList.get(i).getId());
                        mSealInfo.setVid(mSealList.get(i).getVid());
                        mSealInfo.setSealname(mSealList.get(i).getSealname());
                        mSealInfo.setSealsn(mSealList.get(i).getSealsn());
                        mSealInfo.setIssuercert(mSealList.get(i).getIssuercert());
                        mSealInfo.setCert(mSealList.get(i).getCert());
                        mSealInfo.setPicdata(mSealList.get(i).getPicdata());
                        mSealInfo.setPictype(mSealList.get(i).getPictype());
                        mSealInfo.setPicwidth(mSealList.get(i).getPicwidth());
                        mSealInfo.setPicheight(mSealList.get(i).getPicheight());
                        mSealInfo.setNotbefore(mSealList.get(i).getNotbefore());
                        mSealInfo.setNotafter(mSealList.get(i).getNotafter());
                        mSealInfo.setSignal(mSealList.get(i).getSignal());
                        mSealInfo.setExtensions(mSealList.get(i).getExtensions());
                        mSealInfo.setAccountname(mSealList.get(i).getAccountname());
                        mSealInfo.setCertsn(mSealList.get(i).getCertsn());
                        mSealInfo.setState(mSealList.get(i).getState());
                        mSealInfo.setDownloadstatus(mSealList.get(i).getDownloadstatus());

                        mSealInfoDao.addSeal(mSealInfo, accountPlus.getName());
                    }

                }
            }

            @Override
            public void postTask() {

            }
        }.execute();

    }

    public com.sheca.umplus.model.Account getPersonInfo(String token, String username) {
        LoginController controller = new LoginController();
        com.sheca.umplus.model.Account personalInfo = controller.getPersonInfo(this, token, username);

        return personalInfo;
    }

    public void getPersoninfo(String token, String usernmae) {
        LoginController controller = new LoginController();
        accountPlus = controller.getPersonInfo(this, token, usernmae);

        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, accountPlus.getIdentityName());
        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, accountPlus.getStatus());
        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_TYPE, accountPlus.getType());
        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, accountPlus.getActive());
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, accountPlus.getIdentityCode());
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_ACCOUNT_UID, accountPlus.getAccountuid());


        int status = Integer.valueOf(accountPlus.getStatus());
        int active = Integer.valueOf(accountPlus.getActive());
        int type = Integer.valueOf(accountPlus.getType());
        int saveType = Integer.valueOf(accountPlus.getSaveType());
        int certType = Integer.valueOf(accountPlus.getCertType());
        int loginType = Integer.valueOf(accountPlus.getLoginType());
        AccountHelper.setRealName(accountPlus.getIdentityName());
        AccountHelper.setIdcardno(accountPlus.getIdentityCode());

        Account account = new Account(
                accountPlus.getName(),
                accountPlus.getPassword(),
                status,
                active,
                accountPlus.getIdentityName(),
                accountPlus.getIdentityCode(),
                accountPlus.getCopyIDPhoto(),
                type,
                accountPlus.getAppIDInfo(),
                accountPlus.getOrgName(),
                saveType,
                certType,
                loginType);


        if (null == mAccountDao) {
            mAccountDao = new AccountDao(this);
        }

        mAccountDao.add(account);
        //获取枚举证书及枚举印章信息,只有老版本才需要进行备份处理
        int mLocalCode = SharePreferenceUtil.getInstance(LoginActivity.this).getInt(CommonConst.PARAM_V26_DBCHECK);
        Log.e("TEST_DATABASE", "登录界面同步mLocalCode" + mLocalCode);
        Log.e("TEST_DATABASE", "登录界面同步accountPlus" + accountPlus.getName());
        if (mLocalCode < 0) {
            Log.e("TEST_DATABASE", "登录界面同步开始执行1.");
            getEnumInfo(accountPlus);
            //备份完成后，设置为1
            SharePreferenceUtil.getInstance(LoginActivity.this).setInt(CommonConst.PARAM_V26_DBCHECK, 1);
        }
    }

    public boolean attemptLoginByMsgCode() {
        // Reset errors.
        mAccountView.setError(null);

        // Store values at the time of the login attempt.
        final String account = mAccountView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_account_required));
            focusView = mAccountView;
            cancel = true;
        } else if (!isAccountValid(account)) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;
        }

        return cancel;
    }


    /**
     * 账号必须为手机号和电子邮箱。
     */
    private boolean isAccountValid(String account) {
        boolean isValid = false;
        //手机号码
        //移动：134[0-8],135,136,137,138,139,150,151,157,158,159,182,187,188
        //联通：130,131,132,152,155,156,185,186
        //电信：133,1349,153,180,189
        //String MOBILE = "^1(3[0-9]|5[0-35-9]|8[025-9]|7[0-9])\\d{8}$";
        String MOBILE = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$";
        Pattern mobilepattern = Pattern.compile(MOBILE);
        Matcher mobileMatcher = mobilepattern.matcher(account);
        //邮箱
        //p{Alpha}：内容是必选的，和字母字符[\p{Lower}\p{Upper}]等价。
        //w{2,15}：2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。
        //[a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的。
        //[.]：'.'号时必选的。
        //p{Lower}{2,}：小写字母，两个以上。
        String EMAIL = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
        Pattern emailpattern = Pattern.compile(EMAIL);
        Matcher emailMatcher = emailpattern.matcher(account);
        //验证正则表达式
        if (mobileMatcher.matches() || emailMatcher.matches()) {
            isValid = true;
        }

        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (account.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(account);
            isValid = m.matches();
        }

        return isValid;
    }

    /**
     * 密码由8-16位英文、数字或符号组成。
     */
    private boolean isPasswordValid(String password) {
        boolean isValid = false;
        if (password.length() > 7 && password.length() < 17) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.VISIBLE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.VISIBLE : View.VISIBLE);
                        }
                    });

            if (show)
                showProgDlg("账户登录中...");
            else
                closeProgDlg();
			/*mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});*/
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            //mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show)
                showProgDlg("账户登录中...");
            else
                closeProgDlg();
            mLoginFormView.setVisibility(show ? View.VISIBLE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mAccount;
        private final String mPassword;

        UserLoginTask(String account, String password) {
            mAccount = account;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String returnStr = "";
            String resultStr = "";
            int certCount = 0;

            try {
                if (mIsCompanyType) {
                    if (!mIsGetAppList)
                        throw new Exception("no choose app");
                }

                //异步调用UMSP服务：用户登录
                String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
                String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_Login);

                Map<String, String> postParams = new HashMap<String, String>();
                postParams.put("accountName", mAccount);
                postParams.put("pwdHash", getPWDHash(mPassword));    //账户口令需要HASH并转为BASE64字符串
                if (mIsCompanyType) {
                    postParams.put("appID", m_AppID);
                } else {
                    postParams.put("appID", m_AppID);
                }

                String responseStr = "";
                try {
                    //清空本地缓存
                    WebClientUtil.cookieStore = null;
                    //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

                    String postParam = "";
                    if (mIsCompanyType)
                        postParam = "accountName=" + URLEncoder.encode(mAccount, "UTF-8") +
                                "&pwdHash=" + URLEncoder.encode(getPWDHash(mPassword), "UTF-8") +
                                "&appID=" + URLEncoder.encode(m_AppID, "UTF-8");
                    else
                        postParam = "accountName=" + URLEncoder.encode(mAccount, "UTF-8") +
                                "&pwdHash=" + URLEncoder.encode(getPWDHash(mPassword), "UTF-8") +
                                "&appID=" + URLEncoder.encode(m_AppID, "UTF-8");

                    //if(!mIsOneApp)
                    responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

                    //Thread.sleep(3000);
                } catch (Exception e) {
                    if (e.getMessage().indexOf("peer") != -1)
                        throw new Exception("No peer certificate 无效的服务器请求");
                    else
                        throw new Exception("用户登录失败：" + "网络连接异常或无法访问服务");
                }

                //if(!mIsOneApp){
                JSONObject jb = JSONObject.fromObject(responseStr);
                resultStr = jb.getString(CommonConst.RETURN_CODE);
                returnStr = jb.getString(CommonConst.RETURN_MSG);
                //}else{
                //resultStr = "0";
                //}

                if (resultStr.equals("0")) {
                    //若成功登录，注册已登录账号，并跳转到首页；
                    registerXGPush(mAccount);

                    if (mIsCompanyType) {
                        if (m_AppID.equals(CommonConst.UM_APPID)) {
                            m_ActState = getPersonalInfo();
                            mAccountDao.add(new Account(mAccount, mPassword, m_ActState, 1, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                        } else {
                            m_ActState = getOrgInfo();
                            mAccountDao.add(new Account(mAccount + "&" + m_AppID.replace("-", ""), mPassword, m_ActState, 1, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                        }
                    } else {
                        m_ActState = getPersonalInfo();
                        mAccountDao.add(new Account(mAccount, mPassword, m_ActState, 1, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                    }

                    certCount = getAccountCertCount();  //获取用户已下载证书数量
                    getAllAppInfos(mAccount);

                    if (!mIsDao) {
                        if (mIsScanDao) {
                            if (!mIsCompanyType) {
                                DaoScanActivity.mCertCount = getCertApplyLisExCount();
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            } else {
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        } else {
                            Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                            //intent.putExtra("Account", mAccount);
                            if (mIsReg)
                                intent.putExtra("isReg", "isReg");
                            intent.putExtra("Message", "用户登录成功");
                            startActivity(intent);
                            LoginActivity.this.finish();
                        }
                    } else {
                        if (mIsScanDao) {
                            if (!mIsCompanyType) {
                                DaoScanActivity.mCertCount = getCertApplyLisExCount();
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            } else {
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        } else {
                            if (certCount == 0) {
                                if (!mIsCompanyType) {
                                    Intent inet = null;

                                    if (mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
                                        if ((!"".equals(mAccountDao.getLoginAccount().getCopyIDPhoto())) && (null != mAccountDao.getLoginAccount().getCopyIDPhoto()))
                                            inet = new Intent(LoginActivity.this, AuthMainActivity.class);
                                        else
                                            inet = new Intent(LoginActivity.this, CardScanActivity.class);
                                    } else
                                        inet = new Intent(LoginActivity.this, CardScanActivity.class);

                                    inet.putExtra("loginAccount", mAccountDao.getLoginAccount().getIdentityName());
                                    inet.putExtra("loginId", mAccountDao.getLoginAccount().getIdentityCode());
                                    inet.putExtra("message", "dao");
                                    startActivity(inet);
                                    LoginActivity.this.finish();
                                } else {
                                    DaoActivity.bCreated = false;
                                    LoginActivity.this.finish();
                                }
                            } else {
						   /*Intent inet = new Intent(LoginActivity.this, DaoActivity.class);
						   inet.putExtra("OperateState", DaoActivity.operateState);
						   inet.putExtra("OriginInfo", DaoActivity.strResult);
						   inet.putExtra("ServiecNo", DaoActivity.strServiecNo);
						   inet.putExtra("AppName", DaoActivity.strAppName);
						   inet.putExtra("CertSN", DaoActivity.strCertSN);
						   startActivity(inet);*/
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        }
                    }
                } else if (resultStr.equals("10010")) {
                    registerXGPush(mAccount);
                    //若账号未激活，显示修改初始密码页面；

                    if (mIsCompanyType) {
                        if (m_AppID.equals(CommonConst.UM_APPID)) {
                            m_ActState = getPersonalInfo();
                            mAccountDao.add(new Account(mAccount, mPassword, m_ActState, 0, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                        } else {
                            m_ActState = getOrgInfo();
                            mAccountDao.add(new Account(mAccount + "&" + m_AppID.replace("-", ""), mPassword, m_ActState, 0, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                        }
                    } else {
                        m_ActState = getPersonalInfo();
                        mAccountDao.add(new Account(mAccount, mPassword, m_ActState, 0, m_ActName, m_ActIdentityCode, m_ActCopyIDPhoto, m_ActType, m_AppID, m_OrgName, CommonConst.SAVE_CERT_TYPE_PHONE, CommonConst.SAVE_CERT_TYPE_SM2, CommonConst.LOGIN_BY_PWD));
                    }

                    getAllAppInfos(mAccount);

                    if (!mIsDao) {
                        if (mIsScanDao) {
                            if (!mIsCompanyType) {
                                DaoScanActivity.mCertCount = getCertApplyLisExCount();
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            } else {
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        } else {
                            Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
                            intent.putExtra("Account", mAccount);
                            startActivity(intent);
                            LoginActivity.this.finish();
                        }
                    } else {
                        if (mIsScanDao) {
                            if (!mIsCompanyType) {
                                DaoScanActivity.mCertCount = getCertApplyLisExCount();
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            } else {
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        } else {
						   /*
						   Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
						   intent.putExtra("Account", mAccount);
						   intent.putExtra("message", "dao");
						   startActivity(intent);
						   LoginActivity.this.finish();
						   */
                            certCount = getAccountCertCount();  //获取用户已下载证书数量

                            if (certCount == 0) {
                                if (!mIsCompanyType) {
                                    Intent inet = null;
                                    if (mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
                                        if ((!"".equals(mAccountDao.getLoginAccount().getCopyIDPhoto())) && (null != mAccountDao.getLoginAccount().getCopyIDPhoto()))
                                            inet = new Intent(LoginActivity.this, AuthMainActivity.class);
                                        else
                                            inet = new Intent(LoginActivity.this, CardScanActivity.class);
                                    } else
                                        inet = new Intent(LoginActivity.this, CardScanActivity.class);

                                    inet.putExtra("loginAccount", mAccountDao.queryByName(mAccount).getIdentityName());
                                    inet.putExtra("loginId", mAccountDao.queryByName(mAccount).getIdentityCode());
                                    inet.putExtra("message", "dao");
                                    startActivity(inet);
                                    LoginActivity.this.finish();
                                } else {
                                    DaoActivity.bCreated = false;
                                    LoginActivity.this.finish();
                                }
                            } else {
							   /*Intent inet = new Intent(LoginActivity.this, DaoActivity.class);
							   inet.putExtra("OperateState", DaoActivity.operateState);
							   inet.putExtra("OriginInfo", DaoActivity.strResult);
							   inet.putExtra("ServiecNo", DaoActivity.strServiecNo);
							   inet.putExtra("AppName", DaoActivity.strAppName);
							   inet.putExtra("CertSN", DaoActivity.strCertSN);
							   startActivity(inet);*/
                                DaoActivity.bCreated = false;
                                LoginActivity.this.finish();
                            }
                        }
                    }
                } else {
                    if (mIsDao)
                        mPasswordView.setText("");
                    if (mIsScanDao)
                        mPasswordView.setText("");

                    throw new Exception(returnStr);
                }


//				try {
//					// Simulate network access.
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					return false;
//				}

//				for (String credential : DUMMY_CREDENTIALS) {
//					String[] pieces = credential.split(":");
//					if (pieces[0].equals(mAccount)) {
//						// Account exists, return true if the password matches.
//						return pieces[1].equals(mPassword);
//					}
//				}

            } catch (Exception exc) {
                if (resultStr.equals("10009")) {
                    mError = "账户口令错误";
                } else if (resultStr.equals("10007")) {
                    mError = "账户不存在";
                } else {
                    if (exc.getMessage().indexOf("peer") != -1)
                        mError = "无效的服务器请求";
                    else if (exc.getMessage().indexOf("choose") != -1)
                        mError = "请先选择应用";
                    else
                        mError = "网络连接异常或无法访问服务";
                }
                //Log.e(CommonConst.TAG, mError, exc);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                //Toast.makeText(LoginActivity.this, mError, Toast.LENGTH_LONG).show();
                Toast toast = Toast.makeText(getApplicationContext(), mError, Toast.LENGTH_LONG);  //显示时间较长
                toast.setGravity(Gravity.CENTER, 0, 0);  // 居中显示
                toast.show();
                mAccountView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    private String getPWDHash(String strPWD) {
        String strPWDHash = "";

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

	/*	try {
			strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        return strPWDHash;
    }

	/*
	private  void  regAccount(){
		   Intent intent = new Intent(LoginActivity.this, RegAccountActivity.class);
		   if(mIsDao)
		    	intent.putExtra("message", "dao");
		   startActivity(intent);
		   LoginActivity.this.finish();
	}*/

    private void forgetPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
        if (mAccountView.getText().toString().trim().length() > 0)
            intent.putExtra("ActName", mAccountView.getText().toString());
        if (mIsDao)
            intent.putExtra("message", "dao");
        startActivity(intent);
        LoginActivity.this.finish();
    }


    private int getPersonalInfo() {
        String responseStr = "";
        String resultStr = "";
        String returnStr = "";

        int retState = 1;

        try {
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);

            Map<String, String> postParams = new HashMap<String, String>();
            //postParams.put("AccountName", mAccount);
            try {
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String postParam = "";
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
                //Thread.sleep(3000);
            } catch (Exception e) {
                m_ActName = "";
                m_ActIdentityCode = "";
                m_ActCopyIDPhoto = "";
                m_ActType = 1;
                return 1;
            }

            if (null == responseStr || "null".equals(responseStr)) {
                m_ActName = "";
                m_ActIdentityCode = "";
                m_ActCopyIDPhoto = "";
                m_ActType = 1;
                return 1;
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (resultStr.equals("0")) {
                JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));

                retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
                if (null != jbRet.getString(CommonConst.PARAM_NAME))
                    m_ActName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
                else
                    m_ActName = "";
                if (null != jbRet.getString(CommonConst.PARAM_IDENTITY_CODE))
                    m_ActIdentityCode = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号
                else
                    m_ActIdentityCode = "";
                if (null != jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO))
                    m_ActCopyIDPhoto = jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO);    //获取用户头像数据
                else
                    m_ActCopyIDPhoto = "";
                if (null != jbRet.getString(CommonConst.PARAM_ORG_NAME))
                    m_OrgName = jbRet.getString(CommonConst.PARAM_ORG_NAME);              //获取企业单位名称
                else
                    m_OrgName = "";

                m_ActType = Integer.parseInt(jbRet.getString(CommonConst.PARAM_TYPE));    //获取用户账户类别
            } else {
                m_ActName = "";
                m_ActIdentityCode = "";
                m_ActCopyIDPhoto = "";
                m_ActType = 1;
            }

        } catch (Exception exc) {
            m_ActName = "";
            m_ActIdentityCode = "";
            m_ActCopyIDPhoto = "";
            m_ActType = 1;
            return 1;
        }

        return retState;
    }

    private int getOrgInfo() {
        String responseStr = "";
        String resultStr = "";
        String returnStr = "";

        int retState = 1;

        try {
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_GetOrgInfo);

//			Map<String,String> postParams = new HashMap<String,String>();
            new HashMap<String, String>();
            //postParams.put("AccountName", mAccount);
            try {
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String postParam = "";
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

                //Thread.sleep(3000);
            } catch (Exception e) {
                return 1;
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (resultStr.equals("0")) {
                JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));

                retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
                if (null != jbRet.getString(CommonConst.PARAM_NAME))
                    m_ActName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
                else
                    m_ActName = "";
                if (null != jbRet.getString(CommonConst.PARAM_IDENTITY_CODE))
                    m_ActIdentityCode = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号
                else
                    m_ActIdentityCode = "";

                m_ActCopyIDPhoto = "";
                if (null != jbRet.getString(CommonConst.PARAM_NAME))
                    m_OrgName = jbRet.getString(CommonConst.PARAM_NAME);          //获取企业单位名称
                else
                    m_OrgName = "";

                m_ActType = Integer.parseInt(jbRet.getString(CommonConst.PARAM_TYPE));    //获取用户账户类别
            } else {
                m_ActName = "";
                m_ActIdentityCode = "";
                m_ActCopyIDPhoto = "";
                m_ActType = 1;
            }

        } catch (Exception exc) {
            m_ActName = "";
            m_ActIdentityCode = "";
            m_ActCopyIDPhoto = "";
            m_ActType = 1;
            return 1;
        }

        return retState;
    }


    private int getCertApplyLisExCount() {
        List<CertApplyInfoLite> applications = null;
        String responseStr = "";

        try {
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_GetCertApplyList);
//			Map<String,String> postParams = new HashMap<String,String>();
            new HashMap<String, String>();
            //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
            String postParam = "";
            responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
//    		String returnStr = jb.getString(CommonConst.RETURN_MSG);
            jb.getString(CommonConst.RETURN_MSG);

            if ("0".equals(resultStr)) {
                JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));

                applications = new ArrayList<CertApplyInfoLite>();
                for (int i = 0; i < transitListArray.size(); i++) {
                    CertApplyInfoLite certApplyInfo = new CertApplyInfoLite();
                    JSONObject jbRet = transitListArray.getJSONObject(i);
                    certApplyInfo.setRequestNumber(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER));
                    certApplyInfo.setCommonName(jbRet.getString(CommonConst.PARAM_COMMON_NAME));
                    certApplyInfo.setApplyTime(jbRet.getString(CommonConst.PARAM_APPLY_NAME));
                    certApplyInfo.setApplyStatus(Integer.parseInt(jbRet.getString(CommonConst.PARAM_APPLY_STATUS)));
                    certApplyInfo.setBizSN(jbRet.getString(CommonConst.PARAM_BIZSN));
                    certApplyInfo.setCertType(jbRet.getString(CommonConst.PARAM_CERT_TYPE));
                    certApplyInfo.setSignAlg(Integer.parseInt(jbRet.getString(CommonConst.PARAM_SIGNALG_PLUS)));

                    applications.add(certApplyInfo);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //处理服务返回值


        if (null == applications)
            return 0;

        return applications.size();
    }


    private int getAccountCertCount() throws Exception {
        List<Cert> certList = new ArrayList<Cert>();
        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");


        certList = mCertDao.getAllCerts(strActName);

        return certList.size();
    }

    private void getAllAppInfos(String actNo) throws Exception {
        List<AppInfo> applications = null;
        String responseStr = "";
        String strAllAppInfo = "";
        Editor editor = sharedPrefs.edit();

        try {
            editor.putString(CommonConst.SETTINGS_LOGIN_ACT_NAME, actNo);
            editor.commit();

            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_GetAllAppInfos);
//			Map<String,String> postParams = new HashMap<String,String>();
            new HashMap<String, String>();
            //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

            String postParam = "";
            responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            String returnStr = jb.getString(CommonConst.RETURN_MSG);

            if ("0".equals(resultStr)) {
                //JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                applications = new ArrayList<AppInfo>();

                for (int i = 0; i < transitListArray.size(); i++) {
                    AppInfo appInfo = new AppInfo();
                    JSONObject jbRet = transitListArray.getJSONObject(i);
                    appInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
                    appInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
                    appInfo.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));

                    if (null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
                        appInfo.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
                    else
                        appInfo.setDescription("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))
                        appInfo.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
                    else
                        appInfo.setContactPerson("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
                        appInfo.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
                    else
                        appInfo.setContactPhone("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
                        appInfo.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
                    else
                        appInfo.setContactEmail("");
                    if (null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
                        appInfo.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
                    else
                        appInfo.setAssignTime("");

                    applications.add(appInfo);
                }
            } else
                throw new Exception(returnStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        if (null != applications) {
            for (AppInfo appInfo : applications) {
                if (null == mAppInfoDao.getAppInfoByAppID(appInfo.getAppID())) {
                    //Toast.makeText(LoginActivity.this, appItems,Toast.LENGTH_SHORT).show();
                    AppInfoEx appInfoEx = new AppInfoEx();
                    appInfoEx.setAppidinfo(appInfo.getAppID());
                    appInfoEx.setName(appInfo.getName());
                    if (null != appInfo.getAssignTime())
                        appInfoEx.setAssigntime(appInfo.getAssignTime());
                    else
                        appInfoEx.setAssigntime("");
                    if (null != appInfo.getContactEmail())
                        appInfoEx.setContactemail(appInfo.getContactEmail());
                    else
                        appInfoEx.setContactemail("");
                    if (null != appInfo.getContactPerson())
                        appInfoEx.setContactperson(appInfo.getContactPerson());
                    else
                        appInfoEx.setContactperson("");
                    if (null != appInfo.getContactPhone())
                        appInfoEx.setContactphone(appInfo.getContactPhone());
                    else
                        appInfoEx.setContactphone("");
                    if (null != appInfo.getDescription())
                        appInfoEx.setDescription(appInfo.getDescription());
                    else
                        appInfoEx.setDescription("");

                    mAppInfoDao.addAPPInfo(appInfoEx);
                }

                strAllAppInfo += appInfo.getAppID().replace("-", "") + "-";
            }

            if (!"".equals(strAllAppInfo)) {
                strAllAppInfo = strAllAppInfo.substring(0, strAllAppInfo.length() - 1);
                editor.putString(CommonConst.SETTINGS_ALL_APP_INFO, strAllAppInfo);
                editor.commit();
            }

        }

    }

    private int getAllAppInfoCount() {
        List<AppInfo> appInfos = null;
        String responseStr = "";
        String strAllAppInfo = "";
        Editor editor = sharedPrefs.edit();

        try {
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_GetAllAppInfos);
            //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

            String postParam = "";
            responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            String returnStr = jb.getString(CommonConst.RETURN_MSG);

            if ("0".equals(resultStr)) {
                JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                appInfos = new ArrayList<AppInfo>();

                for (int i = 0; i < transitListArray.size(); i++) {
                    AppInfo appInfo = new AppInfo();
                    JSONObject jbRet = transitListArray.getJSONObject(i);
                    appInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
                    appInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
                    appInfo.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));

                    if (null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
                        appInfo.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
                    else
                        appInfo.setDescription("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))
                        appInfo.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
                    else
                        appInfo.setContactPerson("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
                        appInfo.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
                    else
                        appInfo.setContactPhone("");
                    if (null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
                        appInfo.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
                    else
                        appInfo.setContactEmail("");
                    if (null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
                        appInfo.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
                    else
                        appInfo.setAssignTime("");

                    appInfos.add(appInfo);
                }
            } else
                throw new Exception(returnStr);

            if (null != appInfos) {
                mData = new ArrayList<Map<String, String>>();

                for (AppInfo appInfo : appInfos) {
                    if (null == mAppInfoDao.getAppInfoByAppID(appInfo.getAppID())) {
                        AppInfoEx appInfoEx = new AppInfoEx();
                        appInfoEx.setAppidinfo(appInfo.getAppID());
                        appInfoEx.setName(appInfo.getName());
                        if (null != appInfo.getDescription())
                            appInfoEx.setDescription(appInfo.getDescription());
                        if (null != appInfo.getContactPhone())
                            appInfoEx.setContactphone(appInfo.getContactPhone());
                        if (null != appInfo.getContactPerson())
                            appInfoEx.setContactperson(appInfo.getContactPerson());
                        if (null != appInfo.getContactEmail())
                            appInfoEx.setContactemail(appInfo.getContactEmail());
                        if (null != appInfo.getAssignTime())
                            appInfoEx.setAssigntime(appInfo.getAssignTime());

                        mAppInfoDao.addAPPInfo(appInfoEx);

                    }

                    strAllAppInfo += appInfo.getAppID().replace("-", "") + "-";

                    if (appInfo.getVisibility() != 1)  //1：仅单位账户可见
                        continue;

                    Map<String, String> map = new HashMap<String, String>();

                    map.put("id", appInfo.getAppID());
                    map.put("commonname", appInfo.getName());
                    if (null != appInfo.getDescription()) {
                        map.put("organization", appInfo.getDescription());
                        map.put("validtime", appInfo.getDescription());
                    } else {
                        map.put("commonname", "");
                        map.put("validtime", "");
                    }

                    mData.add(map);
                }

                mIsGetAppList = true;

                if (!"".equals(strAllAppInfo)) {
                    strAllAppInfo = strAllAppInfo.substring(0, strAllAppInfo.length() - 1);
                    editor.putString(CommonConst.SETTINGS_ALL_APP_INFO, strAllAppInfo);
                    editor.commit();
                }

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            mIsGetAppList = false;

            //Toast.makeText(LoginActivity.this, e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }

        //处理服务返回值
        if (null == appInfos)
            return 0;

        return appInfos.size();
    }

    private void getAllAccountInfos(String actName, String pwd) throws Exception {
        List<AccountInfo> applications = null;
        String responseStr = "";

        try {
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_PreLogin);

            String postParam = "";
            postParam = "accountName=" + URLEncoder.encode(actName, "UTF-8") +
                    "&pwdHash=" + URLEncoder.encode(getPWDHash(pwd), "UTF-8");

            responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            String returnStr = jb.getString(CommonConst.RETURN_MSG);

            if ("0".equals(resultStr)) {
                //JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                applications = new ArrayList<AccountInfo>();

                if (transitListArray.size() == 1) {
                    mIsOneApp = true;
                    JSONObject jbRet = transitListArray.getJSONObject(0);
                    m_AppID = jbRet.getString(CommonConst.PARAM_APPID);

                    if (Integer.parseInt(jbRet.getString(CommonConst.PARAM_CCOUNT_TYPE)) == CommonConst.ACCOUNT_TYPE_PERSONAL)
                        mIsCompanyType = false;
                    else
                        mIsCompanyType = true;
                }

                for (int i = 0; i < transitListArray.size(); i++) {
                    AccountInfo actInfo = new AccountInfo();
                    JSONObject jbRet = transitListArray.getJSONObject(i);
                    actInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
                    actInfo.setAppName(jbRet.getString(CommonConst.PARAM_APP_NAME));
                    actInfo.setAccountUID(jbRet.getString(CommonConst.PARAM_CCOUNT_UID));
                    actInfo.setAccountType(jbRet.getString(CommonConst.PARAM_CCOUNT_TYPE));

                    applications.add(actInfo);
                }
            } else {
                mError = returnStr;
                throw new Exception(mError);
            }

            if (null != applications) {
                mAppData = new ArrayList<Map<String, String>>();

                for (AccountInfo appInfo : applications) {
                    Map<String, String> map = new HashMap<String, String>();

                    map.put("appid", appInfo.getAppID());
                    map.put("appname", appInfo.getAppName());
                    map.put("acttype", appInfo.getAccountType());

                    mAppData.add(map);
                }

                mIsGetAppList = true;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mIsGetAppList = false;
            throw new Exception(e.getMessage());
        }

    }

    private void isShowMsgCode(final String account) {
        final Handler handler = new Handler(LoginActivity.this.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkAccount(account)) {
                            Intent intent = new Intent(LoginActivity.this, GetMsgCodeActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("mobile", account);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            LoginActivity.this.finish();
                        }
                    }
                });
            }
        }).start();

    }

    private boolean checkAccount(String phone) {
        String returnStr = "";
        String resultStr = "";
        try {
            //异步调用UMSP服务：检测当前账户是否已注册
            String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_IsAccountExisted);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", phone);
            postParams.put("appID", CommonConst.UM_APPID);

            String responseStr = "";
            try {
                //清空本地缓存
                WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

                String postParam = "accountName=" + URLEncoder.encode(phone, "UTF-8") +
                        "&appID=" + URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
                responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
            } catch (Exception e) {
                if (e.getMessage().indexOf("peer") != -1)
                    mError = "无效的服务器请求";
                else
                    mError = "网络连接异常或无法访问服务";
                throw new Exception(mError);
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

            if (resultStr.equals("10008")) {      //账户已存在
                mIsRegAct = false;
                return true;
            } else if (resultStr.equals("10007")) {
                mError = "账户不存在";
                mIsRegAct = true;
            } else if (resultStr.equals("1001")) {
                mError = "验证服务请求错误";
                mIsRegAct = false;
                throw new Exception(mError);
            } else if (resultStr.equals("10003")) {
                mError = "内部处理错误";
                mIsRegAct = false;
                throw new Exception(mError);
            } else {
                mError = returnStr;
                mIsRegAct = false;
                throw new Exception(mError);
            }

        } catch (Exception exc) {
            mError = exc.getMessage();
        }

        Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void showRegAccount(String account) {
        Intent intent = new Intent(LoginActivity.this, SMSLoginActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mobile", account);
        bundle.putString("isReg", "isReg");
        if (mIsDao)
            bundle.putString("message", "dao");
        intent.putExtras(bundle);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    private void showFingerCheck() {
        curProcess = Process.AUTH_GETREQ;

        String info = AuthenticatorManager.getAuthData(LoginActivity.this, mAccountView.getText().toString());
        IFAAFingerprintOpenAPI.getInstance().getAuthRequestAsyn(info, callback);
        secData = info;
    }

    private void startFPActivity(boolean isAuthenticate) {
        Intent intent = new Intent();
//	        if (isAuthenticate) {
//	            intent.putExtra(AuthenticatorMessage.KEY_OPERATIONT_TYPE,
//	                    AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST);
//	        }
        intent.setClass(this, FingerPrintAuthLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

        // this.startActivityForResult(intent, FINGER_CODE);

    }

    private void doFingerLogin() {
        String act = ((EditText) findViewById(R.id.account)).getText().toString().trim();

        if (null != act && (!act.isEmpty())) {
            if (mIsCompanyType) {
                act = act + "&" + m_AppID.replace("-", "");
            }

            Account loginAct = mAccountDao.queryByName(act);
            if (null != loginAct && null != loginAct.getPassword()) {
                ((EditText) findViewById(R.id.password)).setText(loginAct.getPassword());
                attemptLogin();
            } else {
                Toast.makeText(LoginActivity.this, "该账号不存在", Toast.LENGTH_SHORT).show();
                return;
                //attemptLogin();
            }
        } else {
            attemptLogin();
        }


    }

//	private Boolean loginUMSPService(String act,String pwd){    //重新登录UM Service
//		   String returnStr = "";
//			try {
//				//异步调用UMSP服务：用户登录
//				String timeout = LoginActivity.this.getString(R.string.WebService_Timeout);				
//				String urlPath = LoginActivity.this.getString(R.string.UMSP_Service_Login);
//	
//				Map<String,String> postParams = new HashMap<String,String>();
//				postParams.put("accountName", act);
//				postParams.put("pwdHash", getPWDHash(pwd));    //账户口令需要HASH并转为BASE64字符串
//				postParams.put("appID", CommonConst.UM_APPID);
//				
//				String responseStr = "";
//				try {
//					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout)/6);
//					String postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
//         		                       "&pwdHash="+URLEncoder.encode(getPWDHash(pwd), "UTF-8")+
//         		                       "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
//                    responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout)/6);
//				} catch (Exception e) {
//					if(null== e.getMessage())
//					   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
//					else
//					  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
//				}
//	
//				JSONObject jb = JSONObject.fromObject(responseStr);
////				String resultStr = jb.getString(CommonConst.RETURN_CODE);
//				jb.getString(CommonConst.RETURN_CODE);
//				returnStr = jb.getString(CommonConst.RETURN_MSG);
//			} catch (Exception exc) {
//				return false;
//			}
//			
//			return true;
//	}


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

    private void registerXGPush(String actName) {    //注册信鸽推送SDK
        XGPushManager.registerPush(getApplicationContext(), actName,
                new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int flag) {
                        //Toast.makeText(LoginActivity.this, "Login 成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        //Toast.makeText(LoginActivity.this, "Login 失败",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }

//	private void changeProgDlg(String strMsg){
//		if (progDialog.isShowing()) {
//			progDialog.setMessage(strMsg);
//		}
//	}

    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }


}
