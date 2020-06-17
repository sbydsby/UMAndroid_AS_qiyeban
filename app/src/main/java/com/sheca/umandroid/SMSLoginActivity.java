package com.sheca.umandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.igexin.sdk.PushManager;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.AppInfoDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.interfaces.CertCallBack;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.CertOperateParams;
import com.sheca.umandroid.presenter.BasePresenter;
import com.sheca.umandroid.presenter.LoginController;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CertEnum;
import com.sheca.umandroid.util.CertUtils;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Account;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.SealInfo;

import net.sf.json.JSONObject;

import org.apache.http.cookie.SM;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSLoginActivity extends Activity {
    private EditText etAccount, etSMSCode;
    private Button btnCode, btnLogin;
    private TextView tvAccount;

    private BasePresenter progDialog;

    private boolean mIsReg = false;       //是否自动注册账户
    private boolean mIsDao = false;   //第三方接口调用标记
    private String mUID = "";

    private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
    //用户账户属性全局变量
//	private  int     m_ActState;         //用户状态
    private String m_ActName;          //用户姓名
    private String m_ActIdentityCode;  //用户身份证
    private String m_ActCopyIDPhoto;   //用户头像
    private int m_ActType;          //用户账户类别（1：个人；2：单位）
    private String m_OrgName;          //企业单位名称
    private String m_PWDHash;          //账户口令哈希
    private String m_Password;         //用户注册随机口令

    //DB Access Object
    private AccountDao mAccountDao = null;
    private AppInfoDao mAppInfoDao = null;
    private SharedPreferences sharedPrefs;
    private int count = COUNT_DOWN_NUM;
    private CertDao mCertDao;
    private SealInfoDao mSealInfoDao;

    private Timer timer = new Timer();
    private TimerTask task = null;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smslogin);

        TextView switch_service = (TextView) this.findViewById(R.id.switch_service);
        switch_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SMSLoginActivity.this, SwitchServerActivity.class);
                startActivity(intent);
            }
        });

        etAccount = (EditText) findViewById(R.id.et_account);   //账户名称
        etSMSCode = (EditText) findViewById(R.id.et_sms);       //短信验证码

        btnCode = (Button) findViewById(R.id.btn_sms);   //获取短信验证码
        btnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCode.setText("正在发送...");

                loadLisence(true);

            }
        });

        btnLogin = (Button) findViewById(R.id.login);    //账户登录
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadLisence(false);


            }
        });

        tvAccount = (TextView) findViewById(R.id.tv_login_acc);   //显示账户登录
        tvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SMSLoginActivity.this, LoginActivity.class);
                if (!TextUtils.isEmpty(etAccount.getText().toString())) {
                    Bundle bundle = new Bundle();
                    bundle.putString("AccName", etAccount.getText().toString());
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                SMSLoginActivity.this.finish();
            }
        });

        progDialog = new BasePresenter(SMSLoginActivity.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("mobile") != null) {
                etAccount.setText(extras.getString("mobile"));
            }
            if (extras.getString("isReg") != null) {
                Toast.makeText(this, "请获取短信验证码", Toast.LENGTH_SHORT).show();
                mIsReg = true;
            }
            if (extras.getString("message") != null) {
                mIsDao = true;
            }
        }

        mAccountDao = new AccountDao(SMSLoginActivity.this);
        mAppInfoDao = new AppInfoDao(SMSLoginActivity.this);
        mCertDao = new CertDao(SMSLoginActivity.this);
        mSealInfoDao = new SealInfoDao(SMSLoginActivity.this);
        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);

    }

    /**
     * 账号必须为手机号和电子邮箱。
     */
    private boolean isAccountValid(String account) {
        String regex = "(1[0-9][0-9]|15[0-9]|18[0-9])\\d{8}";
        Pattern p = Pattern.compile(regex);
        return p.matches(regex, account);//如果不是号码，则返回false，是号码则返回true

//        boolean isValid = false;
//        //手机号码
//        //移动：134[0-8],135,136,137,138,139,150,151,157,158,159,182,187,188
//        //联通：130,131,132,152,155,156,185,186
//        //电信：133,1349,153,180,189
//        //String MOBILE = "^1(3[0-9]|5[0-35-9]|8[025-9])\\d{8}$";
//        String MOBILE = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$";
//        Pattern mobilepattern = Pattern.compile(MOBILE);
//        Matcher mobileMatcher = mobilepattern.matcher(account);
//        //邮箱
//        //p{Alpha}：内容是必选的，和字母字符[\p{Lower}\p{Upper}]等价。
//        //w{2,15}：2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。
//        //[a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的。
//        //[.]：'.'号时必选的。
//        //p{Lower}{2,}：小写字母，两个以上。
//        String EMAIL = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
//        Pattern emailpattern = Pattern.compile(EMAIL);
//        Matcher emailMatcher = emailpattern.matcher(account);
//        //验证正则表达式
//        if (mobileMatcher.matches() || emailMatcher.matches()) {
//            isValid = true;
//        }
//
//        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
//        if (account.length() != 11) {
//            return false;
//        } else {
//            Pattern p = Pattern.compile(regex);
//            Matcher m = p.matcher(account);
//            isValid = m.matches();
//        }
//
//        return isValid;
    }

    /**
     * 获取验证码的方法
     */
    private void getMsgCode() {
        final Handler handler = new Handler(SMSLoginActivity.this.getMainLooper());
        final String account = etAccount.getText().toString();
        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        etAccount.setError(null);
        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            etAccount.setError(getString(R.string.error_account_required));
            focusView = etAccount;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            etAccount.setError(getString(R.string.error_invalid_account));
            focusView = etAccount;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_account);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
            btnCode.setText("获取验证码");

            Toast.makeText(this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {

            //1：检测账户是否注册、未注册进行注册、已注册直接登录
            checkAccount(account);

        }
    }

    /**
     * 检测账户是否注册
     *
     * @param phone
     */
    private void checkAccount(final String phone) {


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
                    UniTrust dao = new UniTrust(SMSLoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
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
                            btnCode.setText("获取验证码");
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
                    UniTrust dao = new UniTrust(SMSLoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
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
                                Toast.makeText(SMSLoginActivity.this, "短信验证码发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SMSLoginActivity.this, "短信验证码错误" + returnStr, Toast.LENGTH_SHORT).show();
                                btnCode.setText("获取验证码");
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }


    private void regNewAccount() {
        etAccount.setError(null);
        etSMSCode.setError(null);

        String account = etAccount.getText().toString();
        String msgcode = etSMSCode.getText().toString();

        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            etAccount.setError(getString(R.string.error_account_required));
            focusView = etAccount;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            etAccount.setError(getString(R.string.error_invalid_account));
            focusView = etAccount;
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
            Toast.makeText(SMSLoginActivity.this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {
            progDialog.showProgDlgCert("账户注册中...");
            regUMAccount(account, msgcode);
            if (!mIsReg) {
                Toast.makeText(SMSLoginActivity.this, "账户注册失败", Toast.LENGTH_SHORT).show();
            }
            progDialog.closeProgDlgCert();
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


        String mAccount = account;
        String mMsgCode = msgcode;

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
                    UniTrust dao = new UniTrust(SMSLoginActivity.this, false);   //UM SDK+调用类，第二参数表示是否显示提示界面
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
                                mIsReg = false;
                            }

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public void attemptAccountLogin(final String accountName, final String password) {

//        showProgDlg("账户登录中...");
        final UniTrust uniTrust = new UniTrust(SMSLoginActivity.this, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(accountName, password));

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            closeProgDlg();
//                        }
//                    });

                    final APPResponse response = new APPResponse(strMsg);
                    int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();

                    String tokenID;

                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                        String token = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                        final Account accountPlus = getPersoninfo(token, accountName);
//					final String mAccountInfo = getPersonActive(token, mobile);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, token);
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, accountPlus.getIdentityName());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, accountPlus.getStatus());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_TYPE, accountPlus.getType());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, accountPlus.getActive());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, accountPlus.getIdentityCode());
                        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_ACCOUNT_UID, accountPlus.getAccountuid());
                        PushManager.getInstance().bindAlias(SMSLoginActivity.this, accountName);//注册个推别名
                        int status = Integer.valueOf(accountPlus.getStatus());
                        int active = Integer.valueOf(accountPlus.getActive());
                        int type = Integer.valueOf(accountPlus.getType());
                        int saveType = Integer.valueOf(accountPlus.getSaveType());
                        int certType = Integer.valueOf(accountPlus.getCertType());
                        int loginType = Integer.valueOf(accountPlus.getLoginType());

                        final com.sheca.umandroid.model.Account account = new com.sheca.umandroid.model.Account(
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
                                int mLocalCode = SharePreferenceUtil.getInstance(SMSLoginActivity.this).getInt(CommonConst.PARAM_V26_DBCHECK);
                                if (mLocalCode < 0) {
                                    getEnumInfo(accountPlus);
                                    //备份完成后，设置为1
                                    SharePreferenceUtil.getInstance(SMSLoginActivity.this).setInt(CommonConst.PARAM_V26_DBCHECK, 1);
                                }

                                PushManager.getInstance().bindAlias(SMSLoginActivity.this, accountName);//注册个推别名
                                //
                                if (!mIsReg) {

                                    AccountHelper.clearAllUserData(SMSLoginActivity.this);
                                    String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, accountName);
                                    AccountHelper.uid = mUID;

                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, accountPlus.getIdentityName());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, accountPlus.getStatus());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_TYPE, accountPlus.getType());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, accountPlus.getActive());
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, accountPlus.getIdentityCode());


                                    boolean isFirstLogin = SharePreferenceUtil.getInstance(getApplicationContext()).getBoolean((CommonConst.FIRST_SMS_LOGIN + accountName));


                                    if (!isFirstLogin) {
                                        Intent intent = new Intent(SMSLoginActivity.this, SetPasswordActivity.class);
                                        startActivity(intent);
                                        SMSLoginActivity.this.finish();
                                    } else {
                                        Intent intent = new Intent(SMSLoginActivity.this, MainActivity.class);
                                        intent.putExtra("Message", "用户登录成功");
                                        startActivity(intent);
                                        SMSLoginActivity.this.finish();
                                    }
                                } else {
                                    Intent intent = new Intent(SMSLoginActivity.this, SetPasswordActivity.class);
                                    startActivity(intent);
                                    AccountHelper.clearAllUserData(SMSLoginActivity.this);
                                    String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, accountName);
                                    AccountHelper.uid = mUID;
                                    SMSLoginActivity.this.finish();
                                }


                            }


                        });


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SMSLoginActivity.this, retMsg, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }
//
//	private  String   getPWDHash(String strPWD){
//		String strPWDHash = "";
//
//		javasafeengine oSE = new javasafeengine();
//		byte[] bText = strPWD.getBytes();
//		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
//		strPWDHash = new String(Base64.encode(bDigest));
//
//		return strPWDHash;
//	}


//	private void RegAccountSuccess(){
//		Intent intent = new Intent(this, LoginActivity.class);
//	    intent.putExtra("AccName", etAccount.getText().toString());
//	    intent.putExtra("AccPwd", m_Password);
//	    if(mIsDao)
//	    	intent.putExtra("message", "dao");
//	    intent.putExtra("isReg", "isReg");
//	    startActivity(intent);
//	    SMSLoginActivity.this.finish();
//	}

    private void loginAccountByCode() {
        etAccount.setError(null);
        etSMSCode.setError(null);

        String account = etAccount.getText().toString();
        String msgcode = etSMSCode.getText().toString();

        String strErrTip = "";

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(account)) {
            etAccount.setError(getString(R.string.error_account_required));
            focusView = etAccount;
            cancel = true;

            strErrTip = getString(R.string.error_account_required);
        } else if (!isAccountValid(account)) {
            etAccount.setError(getString(R.string.error_invalid_account));
            focusView = etAccount;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_account);
        }

        if (TextUtils.isEmpty(msgcode)) {
            etSMSCode.setError(getString(R.string.error_invalid_code));
            focusView = etSMSCode;
            cancel = true;

            strErrTip = getString(R.string.error_invalid_code);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
            Toast.makeText(SMSLoginActivity.this, strErrTip, Toast.LENGTH_SHORT).show();
        } else {
            progDialog.showProgDlgCert("用户短信验证码登录中...");
            if (!userLoginByValidationCode(account, msgcode)) {
                Toast.makeText(SMSLoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }
            progDialog.closeProgDlgCert();
        }
    }

    public Account getPersoninfo(String token, String username) {
        LoginController controller = new LoginController();
        Account personalInfo = controller.getPersonInfo(this, token, username);

        return personalInfo;
    }


    /**
     * 验证码动态登录的请求
     *
     * @param mobile
     * @param code
     * @return
     */

    public boolean userLoginByValidationCode(final String mobile, final String code) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strInfo = ParamGen.getUserLoginByValidationCodeParams(mobile, code);
                    UniTrust dao = new UniTrust(SMSLoginActivity.this, false); //UM SDK+调用类，第二参数表示是否显示提示界面
                    String responseStr = dao.LoginByMAC(strInfo);

                    final APPResponse response = new APPResponse(responseStr);
                    int resultStr = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (resultStr == 0) {
                        String token = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                        final Account accountPlus = getPersoninfo(token, mobile);
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
                                int mLocalCode = SharePreferenceUtil.getInstance(SMSLoginActivity.this).getInt(CommonConst.PARAM_V26_DBCHECK);
                                if (mLocalCode < 0) {
                                    getEnumInfo(accountPlus);
                                    //备份完成后，设置为1
                                    SharePreferenceUtil.getInstance(SMSLoginActivity.this).setInt(CommonConst.PARAM_V26_DBCHECK, 1);
                                }


                                //
                                if (!mIsReg) {

                                    AccountHelper.clearAllUserData(SMSLoginActivity.this);
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
                                        Intent intent = new Intent(SMSLoginActivity.this, SetPasswordActivity.class);
                                        startActivity(intent);
                                        SMSLoginActivity.this.finish();
                                    } else {
                                        Intent intent = new Intent(SMSLoginActivity.this, MainActivity.class);
                                        intent.putExtra("Message", "用户登录成功");
                                        startActivity(intent);
                                        SMSLoginActivity.this.finish();
                                    }
                                } else {
                                    Intent intent = new Intent(SMSLoginActivity.this, SetPasswordActivity.class);
                                    startActivity(intent);
                                    AccountHelper.clearAllUserData(SMSLoginActivity.this);
                                    String mTokenID = response.getResult().getString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, mTokenID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_UID, mUID);
                                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, mobile);
                                    AccountHelper.uid = mUID;
                                    SMSLoginActivity.this.finish();
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
                                Toast.makeText(SMSLoginActivity.this, retMsg, Toast.LENGTH_SHORT).show();
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


    private void loadLisence(final boolean isSendSms) {
        new Thread() {
            public void run() {
                UniTrust uniTrust = new UniTrust(SMSLoginActivity.this, false);
//                uniTrust.setSignPri(CommonConst.UM_APP_PRIVATE_KEY);
                uniTrust.setStrPubKey(CommonConst.UM_APP_PUBLIC_KEY);
                uniTrust.setUMSPServerUrl(CommonConst.UM_APP_UMSP_SERVER, AccountHelper.getUMSPAddress(SMSLoginActivity.this));
                uniTrust.setUCMServerUrl(CommonConst.UM_APP_UCM_SERVER, AccountHelper.getUCMAddress(SMSLoginActivity.this));
                String mStrVal = uniTrust.LoadLicense(ParamGen.getLoadLisenceParams(SMSLoginActivity.this));
                Log.d("unitrust", mStrVal);
                String strRetMsg;

                APPResponse response = new APPResponse(mStrVal);
                final int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {

                            Toast.makeText(SMSLoginActivity.this, "应用初始化失败", Toast.LENGTH_LONG).show();
                            return;
                        } else {

                            SharedPreferences  sharedPrefs = SMSLoginActivity.this.getSharedPreferences("sheca_settings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("authKeyID",CommonConst.UM_APP_AUTH_KEY);
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

    private void getEnumInfo(final Account accountPlus) {
        //获取证书信息
        final CertOperateParams mCertParams = new CertOperateParams();

        mCertParams.setAccountName(accountPlus.getName());

        new CertUtils(SMSLoginActivity.this, mCertParams, CertEnum.GetCertList, new CertCallBack() {

            @Override
            public void certCallBackforList(final List<Cert> mList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (mList != null && mList.size() != 0) {

                            for (int i = 0; i < mList.size(); i++) {
                                com.sheca.umandroid.model.Cert mCert = new com.sheca.umandroid.model.Cert();
                                mCert.setId(mList.get(i).getId());
                                mCert.setCertsn(mList.get(i).getCertsn());
                                mCert.setEnvsn(mList.get(i).getEnvsn());
                                mCert.setPrivatekey(mList.get(i).getPrivatekey());
                                mCert.setCertificate(mList.get(i).getCertificate());
                                mCert.setKeystore(mList.get(i).getKeystore());
                                mCert.setEnccertificate(mList.get(i).getEnccertificate());
                                mCert.setEnckeystore(mList.get(i).getEnckeystore());
                                mCert.setCertchain(mList.get(i).getCertchain());
                                mCert.setStatus(mList.get(i).getStatus());
                                mCert.setAccountname(mList.get(i).getAccountname());
                                mCert.setNotbeforetime(mList.get(i).getNotbeforetime());
                                mCert.setValidtime(mList.get(i).getValidtime());
                                mCert.setUploadstatus(mList.get(i).getUploadstatus());
                                mCert.setCerttype(mList.get(i).getCerttype());
                                mCert.setSignalg(mList.get(i).getSignalg());
                                mCert.setContainerid(mList.get(i).getContainerid());
                                mCert.setAlgtype(mList.get(i).getAlgtype());
                                mCert.setSavetype(mList.get(i).getSavetype());
                                mCert.setDevicesn(mList.get(i).getDevicesn());
                                mCert.setCertname(mList.get(i).getCertname());
                                mCert.setCerthash(mList.get(i).getCerthash());
                                mCert.setFingertype(mList.get(i).getFingertype());
                                mCert.setSealsn(mList.get(i).getSealsn());
                                mCert.setSealstate(mList.get(i).getSealstate());
                                mCert.setCertlevel(mList.get(i).getCertlevel());

                                mCertDao.addCert(mCert, mCertParams.getAccountName());
                            }

                        }
                    }
                }).start();
            }

            @Override
            public void sealCallBackfoirList(List mList) {

            }

            @Override
            public void certCallBackForCert(com.sheca.umplus.model.Cert mCert) {

            }

        });

        //等待1秒,保持线程同步
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        //获取印章信息
        new CertUtils(SMSLoginActivity.this, mCertParams, CertEnum.GetSealList, new CertCallBack() {

            @Override
            public void certCallBackforList(final List<Cert> mList) {

            }

            @Override
            public void sealCallBackfoirList(final List<SealInfo> mList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (mList != null && mList.size() != 0) {

                            for (int i = 0; i < mList.size(); i++) {
                                com.sheca.umandroid.model.SealInfo mSealInfo = new com.sheca.umandroid.model.SealInfo();
                                mSealInfo.setId(mList.get(i).getId());
                                mSealInfo.setVid(mList.get(i).getVid());
                                mSealInfo.setSealname(mList.get(i).getSealname());
                                mSealInfo.setSealsn(mList.get(i).getSealsn());
                                mSealInfo.setIssuercert(mList.get(i).getIssuercert());
                                mSealInfo.setCert(mList.get(i).getCert());
                                mSealInfo.setPicdata(mList.get(i).getPicdata());
                                mSealInfo.setPictype(mList.get(i).getPictype());
                                mSealInfo.setPicwidth(mList.get(i).getPicwidth());
                                mSealInfo.setPicheight(mList.get(i).getPicheight());
                                mSealInfo.setNotbefore(mList.get(i).getNotbefore());
                                mSealInfo.setNotafter(mList.get(i).getNotafter());
                                mSealInfo.setSignal(mList.get(i).getSignal());
                                mSealInfo.setExtensions(mList.get(i).getExtensions());
                                mSealInfo.setAccountname(mList.get(i).getAccountname());
                                mSealInfo.setCertsn(mList.get(i).getCertsn());
                                mSealInfo.setState(mList.get(i).getState());
                                mSealInfo.setDownloadstatus(mList.get(i).getDownloadstatus());

                                mSealInfoDao.addSeal(mSealInfo, mCertParams.getAccountName());
                            }

                        }
                    }
                }).start();
            }

            @Override
            public void certCallBackForCert(com.sheca.umplus.model.Cert mCert) {

            }

        });

    }


//	private  int  getPersonalInfo(){
//	   String responseStr = "";
//	   String resultStr = "";
//	   String returnStr = "";
//
//	   int retState = 1;
//
//	   try {
//		String timeout = SMSLoginActivity.this.getString(R.string.WebService_Timeout);
//		String urlPath = SMSLoginActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);
//
//		Map<String,String> postParams = new HashMap<String,String>();
//		//postParams.put("AccountName", mAccount);
//		try {
//			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//			String postParam = "";
//			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//			//Thread.sleep(3000);
//		} catch (Exception e) {
//			m_ActName = "";
//			m_ActIdentityCode = "";
//			m_ActCopyIDPhoto = "";
//			m_ActType = 1;
//			m_PWDHash = "";
//			return 1;
//		}
//
//		if(null == responseStr || "null".equals(responseStr)){
//			m_ActName = "";
//			m_ActIdentityCode = " ";
//			m_ActCopyIDPhoto = "";
//			m_ActType = 1;
//			m_PWDHash = "";
//			return 1;
//		}
//
//		JSONObject jb = JSONObject.fromObject(responseStr);
//		resultStr = jb.getString(CommonConst.RETURN_CODE);
//		returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//		if (resultStr.equals("0")) {
//			JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
//
//			retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
//			if(null != jbRet.getString(CommonConst.PARAM_NAME))
//			    m_ActName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
//			else
//			    m_ActName = "";
//			if(null != jbRet.getString(CommonConst.PARAM_IDENTITY_CODE))
//			    m_ActIdentityCode = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号
//			else
//				m_ActIdentityCode = "";
//			if(null != jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO))
//			    m_ActCopyIDPhoto = jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO);    //获取用户头像数据
//			else
//				m_ActCopyIDPhoto = "";
//			if(null != jbRet.getString(CommonConst.PARAM_ORG_NAME))
//				m_OrgName = jbRet.getString(CommonConst.PARAM_ORG_NAME);              //获取企业单位名称
//		    else
//			    m_OrgName = "";
//			if(jbRet.containsKey(CommonConst.PARAM_PWD_HASH)){
//			   if(null != jbRet.getString(CommonConst.PARAM_PWD_HASH))
//				  m_PWDHash = jbRet.getString(CommonConst.PARAM_PWD_HASH);              //获取企业单位名称
//		       else
//		    	  m_PWDHash = "";
//			}else
//				m_PWDHash = "";
//
//			m_ActType = Integer.parseInt(jbRet.getString(CommonConst.PARAM_TYPE));    //获取用户账户类别
//		}else {
//			m_ActName = "";
//			m_ActIdentityCode = "";
//			m_ActCopyIDPhoto = "";
//			m_ActType = 1;
//			m_PWDHash = "";
//		}
//
//	} catch (Exception exc) {
//		m_ActName = "";
//		m_ActIdentityCode = "";
//		m_ActCopyIDPhoto = "";
//		m_ActType = 1;
//		m_PWDHash = "";
//	    return 1;
//
//	}
//
//
//	   return retState;
//
//	}


//	private  void   getAllAppInfos(String actNo)  throws Exception{
//	   List<AppInfo> applications = null;
//	   String responseStr = "";
//	   String strAllAppInfo = "";
//	   Editor editor = sharedPrefs.edit();
//
//	   try {
//		editor.putString(CommonConst.SETTINGS_LOGIN_ACT_NAME, actNo);
//	    editor.commit();
//
//		String timeout = SMSLoginActivity.this.getString(R.string.WebService_Timeout);
//		String urlPath = SMSLoginActivity.this.getString(R.string.UMSP_Service_GetAllAppInfos);
//		Map<String,String> postParams = new HashMap<String,String>();
//		//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//
//		String postParam = "";
//		responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//
//		JSONObject jb = JSONObject.fromObject(responseStr);
//		String resultStr = jb.getString(CommonConst.RETURN_CODE);
//		String returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//		if("0".equals(resultStr)){
//			//JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
//			JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));
//			applications = new ArrayList<AppInfo>();
//
//			for(int i = 0;i<transitListArray.size();i++){
//				AppInfo appInfo = new AppInfo();
//				JSONObject jbRet =  transitListArray.getJSONObject(i) ;
//				appInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
//				appInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
//				appInfo.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));
//
//				if(null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
//				    appInfo.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
//				else
//					appInfo.setDescription("");
//				if(null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))
//				    appInfo.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
//				else
//					appInfo.setContactPerson("");
//				if(null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
//				    appInfo.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
//				else
//					appInfo.setContactPhone("");
//				if(null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
//				    appInfo.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
//				else
//					appInfo.setContactEmail("");
//				if(null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
//				    appInfo.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
//				else
//					appInfo.setAssignTime("");
//
//				applications.add(appInfo);
//			}
//		}
//		else
//			throw new Exception(returnStr);
//	   } catch (Exception e) {
//		e.printStackTrace();
//		throw new Exception(e.getMessage());
//	   }
//
//	   if(null != applications){
//    	for(AppInfo appInfo:applications ){
//    		if(null == mAppInfoDao.getAppInfoByAppID(appInfo.getAppID())){
//    			//Toast.makeText(AccountLoginActivity.this, appItems,Toast.LENGTH_SHORT).show();
//    		    AppInfoEx appInfoEx = new AppInfoEx();
//    		    appInfoEx.setAppidinfo(appInfo.getAppID());
//    		    appInfoEx.setName(appInfo.getName());
//    		    if(null != appInfo.getAssignTime())
//    		    	appInfoEx.setAssigntime(appInfo.getAssignTime());
//    		    else
//    		        appInfoEx.setAssigntime("");
//    		    if(null != appInfo.getContactEmail())
//    		        appInfoEx.setContactemail(appInfo.getContactEmail());
//    		    else
//     		        appInfoEx.setContactemail("");
//    		    if(null != appInfo.getContactPerson())
//    		        appInfoEx.setContactperson(appInfo.getContactPerson());
//    		    else
//      		        appInfoEx.setContactperson("");
//    		    if(null != appInfo.getContactPhone())
//    		        appInfoEx.setContactphone(appInfo.getContactPhone());
//    		    else
//       		        appInfoEx.setContactphone("");
//    		    if(null != appInfo.getDescription())
//			        appInfoEx.setDescription(appInfo.getDescription());
//    		    else
//       		        appInfoEx.setDescription("");
//
//			    mAppInfoDao.addAPPInfo(appInfoEx);
//    		}
//
//    		strAllAppInfo += appInfo.getAppID().replace("-", "")+"-";
//
//    	}
//
//    	if(!"".equals(strAllAppInfo)){
//    		strAllAppInfo = strAllAppInfo.substring(0,strAllAppInfo.length()-1);
//    	    editor.putString(CommonConst.SETTINGS_ALL_APP_INFO, strAllAppInfo);
//    	    editor.commit();
//    	}
//
//	   }
//	}


    private void showCountDown(final int countDownNum) {
        btnCode.setEnabled(false);
        btnCode.setText("等待60秒");
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

    private void update() {
        count--;
        if (count > 0) {
            btnCode.setText("等待" + count + "秒");
        } else {
            btnCode.setText("获取验证码");
            btnCode.setEnabled(true);
            timer.cancel();
            timer = null;
            task.cancel();
            task = null;
            count = COUNT_DOWN_NUM;
        }
    }

}
