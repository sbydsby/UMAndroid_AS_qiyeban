package com.sheca.umee;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;

import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.myWebClientUtil;

import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.account.ReLoginActivityV33;
import com.sheca.umee.adapter.ApplicationAdapter;
import com.sheca.umee.companyCert.CertSetPwdActivity;
import com.sheca.umee.companyCert.ICallback;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.LogDao;
import com.sheca.umee.event.RefreshEvent;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.model.Account;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.CertApplyInfoLite;
import com.sheca.umee.model.DownloadCertResponse;
import com.sheca.umee.model.OperationLog;
import com.sheca.umee.model.ShcaCciStd;
import com.sheca.umee.presenter.CertController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertDownloadActivity extends Activity {
    // UI references.
    private ListView mApplicationsView;

    //DB Access Object
    private AccountDao mAccountDao = null;
    private CertDao mCertDao = null;
    private LogDao mLogDao = null;

    CertController controller = new CertController();

    private List<Map<String, String>> mApplications;
    private String mAccount = "";
    private String mError = "";

    private boolean mIsScanDao = false;   //第三方扫码接口调用标记
    private boolean mIsDao = false;       //第三方接口调用标记

    //private View mProgressView;
    private ProgressDialog progDialog = null;
    private SharedPreferences sharedPrefs;


    private JShcaUcmStd gUcmSdk = null;
    //private  JShcaKsStd gKsSdk = null;
    protected Handler workHandler = null;
    private HandlerThread ht = null;

    private int mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
    private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
    private String mStrBTDevicePwd = "";    //蓝牙key密码

    private String strPersonName = "";
    private String strPaperNO = "";

    private final int DOWNLAOD_CERT_STATE = 1;
    private final int APPLY_CERT_STATE = 2;
    private final int SET_CERT_PWD = 3;

    private Button btnApply;
    private ImageView applyCert;


    Handler localHandler;
    int localPosition;
    String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_application);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));

        ((TextView) findViewById(R.id.header_text)).setText("证书下载列表");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);

        sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);

        //if(null == ScanBlueToothSimActivity.gKsSdk)
        //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(CertDownloadActivity.this.getApplication(), this);

        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);

        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CertDownloadActivity.this.finish();
            }
        });

        mAccountDao = new AccountDao(this);
        mLogDao = new LogDao(CertDownloadActivity.this);
        mCertDao = new CertDao(CertDownloadActivity.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("scan") != null) {
                mIsScanDao = true;
                cancelScanButton.setVisibility(RelativeLayout.GONE);
            }
            if (extras.getString("message") != null) {
                mIsDao = true;
            }
        }

        //判断账号是否已登录。
        Boolean isLoggedIn = false;

        if (!AccountHelper.hasLogin(this)) {
            isLoggedIn = false;
        } else {
            isLoggedIn = true;
        }

        if (!isLoggedIn) {
            //若账号未登录，跳转到登录页面
//			Intent intent = new Intent(CertDownloadActivity.this, LoginActivity.class);
//			startActivity(intent);
            if (!AccountHelper.hasLogin(CertDownloadActivity.this)) {
                if (AccountHelper.isFirstLogin(CertDownloadActivity.this)) {
                    Intent intentLoignV33 = new Intent(CertDownloadActivity.this, LoginActivityV33.class);
                    startActivity(intentLoignV33);
                } else {
                    Intent intentLoignV33 = new Intent(CertDownloadActivity.this, ReLoginActivityV33.class);
                    startActivity(intentLoignV33);
                }
            }


            CertDownloadActivity.this.finish();
        } else {


            applyCert = (ImageView) this.findViewById(R.id.applycertbtn);
//            applyCert.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        final Handler handler = new Handler(CertDownloadActivity.this.getMainLooper());
//
//                        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mAccountDao.getLoginAccount().getSaveType())
//                            showProgDlg("正在连接蓝牙key设备...");
//                        else if (CommonConst.SAVE_CERT_TYPE_SIM == mAccountDao.getLoginAccount().getSaveType())
//                            showProgDlg("正在连接蓝牙sim卡...");
//
//                        workHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (checkBTDevice()) {
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            closeProgDlg();
//                                        }
//                                    });
//
//                                    applyByFace();
//                                } else {
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            closeProgDlg();
//                                            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mAccountDao.getLoginAccount().getSaveType())
//                                                Toast.makeText(CertDownloadActivity.this, "请确认蓝牙key设备是否正确连接", Toast.LENGTH_SHORT).show();
//                                            else if (CommonConst.SAVE_CERT_TYPE_SIM == mAccountDao.getLoginAccount().getSaveType())
//                                                Toast.makeText(CertDownloadActivity.this, "请确认蓝牙sim卡设备是否正确连接", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } catch (Exception e) {
//                        Toast.makeText(CertDownloadActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//            });

            //mProgressView = findViewById(R.id.cert_progress);
            mApplicationsView = (ListView) findViewById(R.id.lv_applications);
            Account currentAccount = mAccountDao.getLoginAccount();//accounts.get(0);
            mAccount = currentAccount.getName();

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == currentAccount.getSaveType()) {
                mSaveType = CommonConst.SAVE_CERT_TYPE_BLUETOOTH;
                mBBTDeviceUsed = true;
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == currentAccount.getSaveType()) {
                mSaveType = CommonConst.SAVE_CERT_TYPE_SIM;
                mBBTDeviceUsed = true;
            } else {
                mSaveType = CommonConst.SAVE_CERT_TYPE_PHONE;
                mBBTDeviceUsed = false;
            }

//			findViewById(R.id.Layout3).setVisibility(RelativeLayout.GONE);
            showCertApplyList();
//			findViewById(R.id.Layout3).setVisibility(RelativeLayout.VISIBLE);

            if (currentAccount.getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
//				findViewById(R.id.Layout3).setVisibility(RelativeLayout.GONE);
                LayoutParams layoutPam = mApplicationsView.getLayoutParams();
                layoutPam.height = LayoutParams.WRAP_CONTENT;
                mApplicationsView.setLayoutParams(layoutPam);
            } else {
//				findViewById(R.id.Layout3).setVisibility(RelativeLayout.VISIBLE);
            }

            this.registerForContextMenu(mApplicationsView);
        }

        findViewById(R.id.btn_apply).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 申请证书
                finish();
            }
        });
    }

//    private void applyByFace() {
//        if (!checkShcaCciStdServiceState(mAccountDao.getLoginAccount().getCertType())) {
//            Toast.makeText(CertDownloadActivity.this, "密码分割组件初始化失败,请退出重启应用", Toast.LENGTH_SHORT).show();
//
//            Account act = mAccountDao.getLoginAccount();
//            act.setCertType(CommonConst.SAVE_CERT_TYPE_RSA);
//            mAccountDao.update(act);
//            return;
//        }
//
//        Intent intent = null;
//        if (mAccountDao.getLoginAccount().getStatus() == 5 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
//            AuthController controller = new AuthController();
//            controller.faceAuth(CertDownloadActivity.this, true);
//        } else {
//            intent = new Intent(CertDownloadActivity.this, AuthChoiceActivity.class);
//            Bundle bundle = new Bundle();
//            intent.putExtra("isPayAndAuth", "isPayAndAuth");
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }
//        if (mIsScanDao) {
//            intent = new Intent(CertDownloadActivity.this, AuthChoiceActivity.class);
//            intent.putExtra("message", "dao");
//            startActivity(intent);
//        }
//
//        CertDownloadActivity.this.finish();
//    }


    private void showCertApplyList() {
        showProgDlg("获取待下载证书列表中...");
        final Handler handler = new Handler(CertDownloadActivity.this.getMainLooper());
        final ImageView noCertView = (ImageView) this.findViewById(R.id.nocertview);


                try {
                    //异步调用UMSP服务：获取证书申请列表
                    //showProgress(true);
//            		checkCertStatus();

                    //String responseStr = GetCertApplyList();
                    controller.getCertInfoList(CertDownloadActivity.this, AccountHelper.getToken(getApplicationContext()), new ICallback() {
                        @Override
                        public void onCallback(Object data) {
                         String   responseStr =(String)data;
                            JSONObject jb = JSONObject.fromObject(responseStr);
                            String resultStr = jb.getString(CommonConst.RETURN_CODE);
                            String returnStr = jb.getString(CommonConst.RETURN_MSG);

                            Log.d("unitrust", responseStr);

                            if (resultStr.equals("0")) {
                                JSONArray transitListArray = null;
                                if (jb.containsKey(CommonConst.RETURN_RESULT)) {
                                    JSONObject jbRet = jb.getJSONObject(com.sheca.umplus.util.CommonConst.RETURN_RESULT);
                                    if (jbRet.has(com.sheca.umplus.util.CommonConst.PARAM_CERT_INFOS)) {
                                        transitListArray = JSONArray.fromObject(jbRet.getString(com.sheca.umplus.util.CommonConst.PARAM_CERT_INFOS));
                                    }
                                }


                                //处理服务返回值
                                final List<CertApplyInfoLite> applications = new ArrayList<CertApplyInfoLite>();
                                if (null != transitListArray && transitListArray.size() > 0) {
                                    for (int i = 0; i < transitListArray.size(); i++) {
                                        JSONObject jbRet = transitListArray.getJSONObject(i);
                                        if (null == jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER) || "null".equals(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER)) || "".equals(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER)))
                                            continue;

                                        CertApplyInfoLite certApplyInfo = new CertApplyInfoLite();
                                        certApplyInfo.setRequestNumber(jbRet.getString(CommonConst.PARAM_REQUEST_NUMBER));
                                        certApplyInfo.setCommonName(jbRet.getString(CommonConst.PARAM_COMMON_NAME));
                                        certApplyInfo.setApplyTime(jbRet.getString(CommonConst.PARAM_APPLY_NAME));
                                        certApplyInfo.setApplyStatus(Integer.parseInt(jbRet.getString(CommonConst.PARAM_APPLY_STATUS_EX)));
                                        certApplyInfo.setBizSN(jbRet.getString(CommonConst.PARAM_ENCRYPT_CERTSN));
                                        certApplyInfo.setCertType(jbRet.getString(CommonConst.PARAM_CERT_TYPE));
                                        certApplyInfo.setSignAlg(1);/*Integer.parseInt(jbRet.getString(CommonConst.PARAM_SIGNALG_PLUS))*/
                                        certApplyInfo.setPayStatus(1);/*Integer.parseInt(jbRet.getString(CommonConst.PARAM_PAY_STATUS))*/

                                        if (1 == certApplyInfo.getApplyStatus() || 2 == certApplyInfo.getApplyStatus()) { // 只有0的时候可以下载
                                            applications.add(certApplyInfo);
                                        }

                                    }
                                }

                                //绑定applications到mApplicationsView
                                String[] from = {"CommonName", "ApplyTime"};
                                int[] to = {R.id.tv_CommonName, R.id.tv_ApplyTime};
                                mApplications = getData(applications);

                                final ApplicationAdapter adapter = new ApplicationAdapter(CertDownloadActivity.this, mApplications);
                                //final SimpleAdapter adapter = new SimpleAdapter(CertDownloadActivity.this, mApplications, R.layout.applicationitem, from, to);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mApplicationsView.setAdapter(adapter);
                                        if (mApplications.size() == 0) {
                                            findViewById(R.id.Layout_cert1).setVisibility(RelativeLayout.VISIBLE);
                                            findViewById(R.id.Layout_cert2).setVisibility(RelativeLayout.GONE);

                                            if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                                noCertView.setImageDrawable(getResources().getDrawable(R.drawable.no_cert_company));
                                            else
                                                noCertView.setImageDrawable(getResources().getDrawable(R.drawable.no_cert));
                                        } else {
                                            findViewById(R.id.Layout_cert1).setVisibility(RelativeLayout.GONE);
                                            findViewById(R.id.Layout_cert2).setVisibility(RelativeLayout.VISIBLE);

										/*
										if(mIsScanDao)
											findViewById(R.id.Layout3).setVisibility(RelativeLayout.GONE);
										else
										    findViewById(R.id.Layout3).setVisibility(RelativeLayout.VISIBLE);
										*/

                                        }
                                        closeProgDlg();
                                    }
                                });

                                //调用多个UMSP服务进行证书下载
                                mApplicationsView.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        if (!getCertApplyStatus(position))
                                            return;
//                                        if (!isAccountRealName())
//                                            return;

//                                        if (mBBTDeviceUsed) {
//                                            setBlueToothPwd(handler, position);
//                                        } else {
                                            localHandler = handler;
                                            localPosition = position;

                                            SetPassword();

//                                        }
                                    }
                                });

//                                if (mIsDao) {
//                                    if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_PERSONAL)
//                                        applyByFace();
//                                }

                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeProgDlg();
                                        findViewById(R.id.Layout_cert1).setVisibility(RelativeLayout.VISIBLE);
                                        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                                            noCertView.setImageDrawable(getResources().getDrawable(R.drawable.no_cert_company));
                                        else
                                            noCertView.setImageDrawable(getResources().getDrawable(R.drawable.no_cert));
                                    }
                                });


                            }
                        }
                    });


                    //showProgress(false);
                } catch (final Exception exc) {
                    mError = exc.getMessage();
                    Log.e(CommonConst.TAG, mError, exc);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                            if (exc.getMessage().indexOf("peer") != -1)
                                Toast.makeText(CertDownloadActivity.this, "无效的服务器请求", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(CertDownloadActivity.this, "网络连接或访问服务异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


    }
    //长按删除
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//	    // set context menu title
//		menu.setHeaderTitle("删除该证书");
//		menu.add(0, 0, 0, "删除");
//		menu.add(0, 1, 0, "取消");
//	}

    //长按删除
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//	    // 得到当前被选中的item信息
//	    AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
//	   // Log.v("1", "context item seleted ID="+ menuInfo.id);
//
//	    switch(item.getItemId()){
//	       case 0:
//
//	    	      showDeleteCertApply(mApplications.get(Integer.parseInt(menuInfo.id+"")).get("RequestNumber"),
//	    	    		              mApplications.get(Integer.parseInt(menuInfo.id+"")).get("BizSN"));
//	          break;
//	       case 1:
//	          // do something
//	          break;
//	    default:
//	        return super.onContextItemSelected(item);
//	    }
//
//	    return true;
//	}

    private void setBlueToothPwd(final Handler handler, final int position) {
        Builder builder = new Builder(CertDownloadActivity.this);
        builder.setIcon(R.drawable.alert);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
            builder.setTitle("请输入蓝牙key密码");
        else if (CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
            builder.setTitle("请输入蓝牙sim卡密码");

        builder.setCancelable(false);
        // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(CertDownloadActivity.this).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);
        final EditText prikeyPasswordView = (EditText) view.findViewById(R.id.et_prikey_password);
        final EditText prikeyPassword2View = (EditText) view.findViewById(R.id.et_prikey_password2);
        prikeyPassword2View.setVisibility(RelativeLayout.GONE);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
            prikeyPasswordView.setHint("输入蓝牙key密码");
        else if (CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
            prikeyPasswordView.setHint("输入蓝sim卡密码");

        prikeyPassword2View.setText("");
        prikeyPasswordView.setText("");

        prikeyPasswordView.requestFocus();
        prikeyPasswordView.setFocusable(true);
        prikeyPasswordView.setFocusableInTouchMode(true);

        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            try {
                                java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            final String prikeyPassword = prikeyPasswordView.getText().toString().trim();
                            // 检查用户输入的私钥口令是否有效
                            if (TextUtils.isEmpty(prikeyPassword) || !isPasswordValid(prikeyPassword)) {
                                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType)
                                    Toast.makeText(CertDownloadActivity.this, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
                                else if (CommonConst.SAVE_CERT_TYPE_SIM == mSaveType)
                                    Toast.makeText(CertDownloadActivity.this, "无效的蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            mStrBTDevicePwd = prikeyPassword;
                            localHandler = handler;
                            localPosition = position;


                            doApplyCertByCertList(handler, position);
							
							/*workHandler.post(new Runnable(){
					            @Override
					            public void run() {
					            	 int nRet = -1;
								     nRet = gEsDev.verifyUserPin(prikeyPassword);	
								     if(nRet != 0){
								    	 handler.post(new Runnable() {
				       						   @Override
				       						   public void run() {
				            			           Toast.makeText(CertDownloadActivity.this, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
				            			           return;
				       						   }
				        					}); 
								     }else{
								    	 
								     }
					            }
							});
							*/
                        } catch (Exception e) {
                            Log.e(CommonConst.TAG, e.getMessage(), e);

                        }

                        dialog.dismiss();
                    }
                });

        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case SET_CERT_PWD:
                  pwd=data.getStringExtra("pwd");

                    doApplyCertByCertList(localHandler,
                            localPosition);
                    break;

            }
        }
    }

    private void SetPassword() {

        Intent intent = new Intent(CertDownloadActivity.this, CertSetPwdActivity.class);
        startActivityForResult(intent, SET_CERT_PWD);
    }

    private void doApplyCertByCertList(final Handler handler, final int position) {
        //不能在主线程中请求HTTP请求
        //new Thread(new Runnable(){


        workHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showProgDlg("查询证书申请状态中...");
                        }
                    });

                    if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mSaveType) {
                        if (!checkBTDevice()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(CertDownloadActivity.this, "请确认蓝牙key设备是否正确连接", Toast.LENGTH_SHORT).show();
                                }
                            });

                            return;
                        }
                    } else if (CommonConst.SAVE_CERT_TYPE_SIM == mSaveType) {
                        if (!checkBTDevice()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgDlg();
                                    Toast.makeText(CertDownloadActivity.this, "请确认sim卡设备是否正确连接", Toast.LENGTH_SHORT).show();
                                }
                            });

                            return;
                        }
                    }

                    //调用UMSP服务：查询证书申请状态
//					final String requestNumber = mApplications.get(position).get("RequestNumber");
//					String responseStr = QueryCertApplyStatus(requestNumber);
//
//					JSONObject jb = JSONObject.fromObject(responseStr);
//					String resultStr = jb.getString(CommonConst.RETURN_CODE);
//				    String returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//				    JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
//				    returnStr = jbRet.getString(CommonConst.PARAM_STATUS);

                    String resultStr = mApplications.get(position).get("ApplyStatus");
                    String requestNumber = mApplications.get(position).get("RequestNumber");
                    if ("0".equals(resultStr) || "2".equals(resultStr) || "1".equals(resultStr)) {
                        final String payStatus = mApplications.get(position).get("PayStatus");
                        if (Integer.parseInt(payStatus) == 0) {
                            if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CertDownloadActivity.this, "请确认证书费用是否已支付", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            } else {
                                if (!mIsScanDao && !mIsDao) {
                                    getPersonalInfo();
                                    showPayActivity(requestNumber, resultStr, mApplications.get(position).get("CertType"));
                                    return;
                                }
                            }
                        }

                        String res;

                        res = DownloadCert(requestNumber, mApplications.get(position).get("CertType"), mApplications.get(position).get("CommonName"));


//							if (CommonConst.CERT_TYPE_SM2.equals(mApplications.get(position).get("CertType")) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mApplications.get(position).get("CertType"))) {
////								UploadSM2Pkcs10(requestNumber,mApplications.get(position).get("CommonName"),mApplications.get(position).get("CertType"),mApplications.get(position).get("SignAlg"),mSaveType);
//								res = DownloadCert(requestNumber, CommonConst.CERT_TYPE_SM2);
//							} else {
////								UploadPkcs10(requestNumber,mApplications.get(position).get("CommonName"),mApplications.get(position).get("CertType"),mApplications.get(position).get("SignAlg"),mSaveType);
//								res = DownloadCert(requestNumber, CommonConst.CERT_TYPE_RSA);
//							}

                        final APPResponse response = new APPResponse(res);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(getApplicationContext(), response.getReturnMsg(), Toast.LENGTH_LONG).show();
                            }
                        });
                        if (response.getReturnCode() == 0) {
                            JSONObject jbRet = response.getResult();
                            String certId = jbRet.getString("certID");
                            com.sheca.umplus.model.Cert certPlus = controller.getCertDetailandSave(CertDownloadActivity.this, certId);

                            Cert cert = controller.convertCert(certPlus);

                            if (null == mCertDao) {
                                mCertDao = new CertDao(getApplicationContext());
                            }
                            mCertDao.addCert(cert, AccountHelper.getUsername(getApplicationContext()));
//								Toast.makeText(CertDownloadActivity.this," 下载证书成功",Toast.LENGTH_LONG).show();
//								Log.d("unitrust", "mCertDao.addCert 成功");
//                            Intent intent = new Intent(CertDownloadActivity.this, MainActivity.class);
//                            startActivity(intent);
//                            CertDownloadActivity.this.finish();

                            EventBus.getDefault().post(new RefreshEvent());

                            Intent intent = new Intent(CertDownloadActivity.this, CertResultActivity.class);
                            intent.putExtra("type", 0);
                            startActivity(intent);
                            finish();
                        } else {
								Toast.makeText(CertDownloadActivity.this,"失败："+response.getReturnMsg(),Toast.LENGTH_SHORT).show();
                        }

//						} else if (returnStr.equals("1")) {  //待签发
//							handler.post(new Runnable() {
//								 @Override
//									public void run() {
//									 closeProgDlg();
//									 Toast.makeText(CertDownloadActivity.this, "证书签发中，请等待一分钟，再尝试下载。", Toast.LENGTH_SHORT).show();
//									}
//							});
//
//						} else if (returnStr.equals("2")) {  //待下载
//							//调用UMSP服务：下载证书
//							handler.post(new Runnable() {
//								 @Override
//									public void run() {
//									 changeProgDlg("下载证书中...");
//									}
//							});
//
//							String strActName = mAccountDao.getLoginAccount().getName();
//							if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
//								strActName = mAccountDao.getLoginAccount().getName()+"&"+mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
//
//							//判断私钥是否存放在本设备上
//							if(null == mCertDao.getCertByEnvsn(requestNumber,strActName)){
//								handler.post(new Runnable() {
//									 @Override
//										public void run() {
//										 closeProgDlg();
//										 Toast.makeText(CertDownloadActivity.this, "私钥未存放在本设备上", Toast.LENGTH_SHORT).show();
//
//										}
//								});
//
//								throw new Exception("私钥未存放在本设备上");
//						    }
//
//							String res;
//							if(CommonConst.CERT_TYPE_SM2.equals(mApplications.get(position).get("CertType")) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mApplications.get(position).get("CertType"))){
//								res=DownloadCert(requestNumber,CommonConst.CERT_TYPE_SM2);
//							}else{
//								res=DownloadCert(requestNumber,CommonConst.CERT_TYPE_RSA);
//							}
//
//							final APPResponse response = new APPResponse(res);
//							runOnUiThread(new Runnable() {
//								@Override
//								public void run() {
//									closeProgDlg();
//									Toast.makeText(getApplicationContext(),response.getReturnMsg(),Toast.LENGTH_SHORT).show();
//								}
//							});
//							if (response.getReturnCode() == 0){
//								JSONObject jbRet = response.getResult();
//								String certId = jbRet.getString("certID");
//								com.sheca.umplus.model.Cert certPlus = controller.getCertDetailandSave(CertDownloadActivity.this,certId);
//
//								Cert cert = controller.convertCert(certPlus);
//
//								if (null == mCertDao){
//									mCertDao = new CertDao(getApplicationContext());
//								}
//								mCertDao.addCert(cert,AccountHelper.getUsername(getApplicationContext()));
//								Log.d("unitrust","mCertDao.addCert 成功");
//							}
//
//						}else{
//							handler.post(new Runnable() {
//								 @Override
//									public void run() {
//									 closeProgDlg();
//									 Toast.makeText(CertDownloadActivity.this, "证书签发异常，请重新申请下载。", Toast.LENGTH_SHORT).show();
//									}
//							});
//
//						}
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                            }
                        });

                        throw new Exception("调用UMSP服务之QueryCertApplyStatus失败：" + resultStr);
                    }
                } catch (Exception exc) {
                    mError = exc.getMessage();
                    Log.e(CommonConst.TAG, mError, exc);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                            Toast.makeText(CertDownloadActivity.this, mError, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });//.start();

    }

    private List<Map<String, String>> getData(List<CertApplyInfoLite> applications) {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        for (CertApplyInfoLite application : applications) {
            map = new HashMap<String, String>();

            map.put("RequestNumber", application.getRequestNumber());
            map.put("CommonName", application.getCommonName());
            map.put("ApplyTime", application.getApplyTime());
            map.put("ApplyStatus", application.getApplyStatus() + "");
            map.put("BizSN", application.getBizSN());
            map.put("CertType", application.getCertType());
            map.put("SignAlg", application.getSignAlg() + "");
            map.put("PayStatus", application.getPayStatus() + "");

            if (application.getApplyStatus() == 4)
                map.put("Download", R.drawable.manucheckfailwait + "");
            else if (application.getApplyStatus() == 6)
                map.put("Download", R.drawable.manucheckfail + "");
            else
                map.put("Download", R.drawable.download + "");

            if (application.getApplyStatus() == 6)
                map.put("ListImage", R.drawable.cert_icon + "");
            else
                map.put("ListImage", R.drawable.cert_icon + "");

            map.put("ActType", mAccountDao.getLoginAccount().getType() + "");

            data.add(map);
        }

        return data;
    }

    private String GetCertApplyList() throws Exception {
        String timeout = CertDownloadActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = CertDownloadActivity.this.getString(R.string.UMSP_Service_GetCertApplyList);
        Map<String, String> postParams = new HashMap<String, String>();
        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "";
        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

        return responseStr;
    }






    private String DownloadCert(String requestNumber, String certType, String certName) throws Exception {
        String responseStr = controller.downloadCert(this, requestNumber, certType, certName,pwd);
        return responseStr;
    }

    private void saveCert(final String requestNumber, final DownloadCertResponse response, final String certType, final int saveType) {
        Builder builder = new Builder(CertDownloadActivity.this);
        builder.setIcon(R.drawable.alert);
        builder.setTitle("请设置证书密码");
        builder.setCancelable(false);
        // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(CertDownloadActivity.this).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);
        final EditText prikeyPasswordView = (EditText) view.findViewById(R.id.et_prikey_password);
        final EditText prikeyPassword2View = (EditText) view.findViewById(R.id.et_prikey_password2);
        prikeyPassword2View.setText("");
        prikeyPasswordView.setText("");

        prikeyPasswordView.requestFocus();
        prikeyPasswordView.setFocusable(true);
        prikeyPasswordView.setFocusableInTouchMode(true);

        if (!mBBTDeviceUsed) {
            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                try {
                                    java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String prikeyPassword = prikeyPasswordView.getText().toString().trim();
                                String prikeyPassword2 = prikeyPassword2View.getText().toString().trim();
                                // 检查用户输入的私钥口令是否有效
                                if (TextUtils.isEmpty(prikeyPassword) || !isPasswordValid(prikeyPassword)) {
                                    Toast.makeText(CertDownloadActivity.this, "无效的证书密码,密码长度8-16位", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 检查用户输入的重复私钥口令是否有效
                                if (TextUtils.isEmpty(prikeyPassword2) || !isPasswordValid(prikeyPassword2)) {
                                    Toast.makeText(CertDownloadActivity.this, "无效的重复证书密码,密码长度8-16位", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 检查用户两次输入的私钥口令是否一致
                                if (!prikeyPassword.equals(prikeyPassword2)) {
                                    Toast.makeText(CertDownloadActivity.this, "两次输入的证书密码不一致", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                uploadCertStatus(requestNumber, response, prikeyPassword, certType, saveType);

                            } catch (Exception e) {
                                Log.e(CommonConst.TAG, e.getMessage(), e);
                                Toast.makeText(CertDownloadActivity.this, "保存证书失败", Toast.LENGTH_SHORT).show();
                            }

                            if (mIsScanDao) {
                                DaoActivity.bCreated = false;
                                dialog.dismiss();
                                CertDownloadActivity.this.finish();
                            } else {
                                dialog.dismiss();

                                Intent intent = new Intent(CertDownloadActivity.this, MainActivity.class);
                                startActivity(intent);
                                CertDownloadActivity.this.finish();
                            }
                        }
                    });

            builder.show();
        } else {
            try {
                uploadCertStatus(requestNumber, response, mStrBTDevicePwd, certType, saveType);
            } catch (Exception e) {
                Log.e(CommonConst.TAG, e.getMessage(), e);
                Toast.makeText(CertDownloadActivity.this, "保存证书失败", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(CertDownloadActivity.this, MainActivity.class);
            startActivity(intent);
            CertDownloadActivity.this.finish();
        }
    }

    private void uploadCertStatus(final String requestNumber, final DownloadCertResponse response, String prikeyPassword, final String certType, final int saveType) throws Exception {
        showProgDlg("证书保存中...");

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        String userCert = response.getUserCert();
        String certChain = response.getCertChain();
        String encCert = response.getEncCert();
        String encKeystore = response.getEncKey();
        Cert cert = mCertDao.getCertByEnvsn(requestNumber, strActName);
        int retcode = -1;
        String strCertName = "";
        //String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
        cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
        cert.setCertchain(certChain);
        cert.setNotbeforetime(getCertNotbeforetime(userCert));
        cert.setValidtime(getCertValidtime(userCert));
        cert.setSealsn("");
        cert.setSealstate(Cert.STATUS_NO_SEAL);

        if (CommonConst.CERT_TYPE_RSA.equals(certType) || CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)) {
            byte[] bCert = Base64.decode(userCert);
            javasafeengine jse = new javasafeengine();
            Certificate oCert = jse.getCertFromBuffer(bCert);
            X509Certificate oX509Cert = (X509Certificate) oCert;
            cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));
            strCertName = jse.getCertDetail(17, bCert) + CommonConst.CERT_RSA_NAME + "证书";
            cert.setCertname(strCertName);

      {
                String p12 = genP12(cert.getPrivatekey(), getPWDHash(prikeyPassword), userCert, certChain);
                cert.setKeystore(p12);
                cert.setPrivatekey("");
                cert.setCertificate(userCert);
                cert.setCerthash(getPWDHash(prikeyPassword));
                cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
            }
        } else {
            //else if(CommonConst.CERT_TYPE_SM2.equals(certType)){
            if (null == encCert)
                encCert = "";
            if (null == encKeystore)
                encKeystore = "";

            cert.setKeystore("");
            cert.setCertsn(getCertSN(userCert));

            byte[] bCert = Base64.decode(userCert);
            javasafeengine jse = new javasafeengine();
            strCertName = jse.getCertDetail(17, bCert) + CommonConst.CERT_SM2_NAME + "证书";
            cert.setCertname(strCertName);

 {
                cert.setCertificate(userCert);
                cert.setEnccertificate(encCert);
                cert.setEnckeystore(encKeystore);
                cert.setCerthash(getPWDHash(prikeyPassword));
                cert.setFingertype(CommonConst.USE_NO_FINGER_TYPE);

                //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                // initShcaCciStdService();

                boolean isSM2SaveOK = false;
                // if(null != ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid()) && !"".equals(ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid())))
                //	isSM2SaveOK = true;
                //if(null != ShcaCciStd.gSdk.readSM2EncryptCert(cert.getContainerid()) && !"".equals(ShcaCciStd.gSdk.readSM2EncryptCert(cert.getContainerid())))
                isSM2SaveOK = true;

                if (isSM2SaveOK) {
			     /*  if(!"".equals(encCert))
			          retcode = ShcaCciStd.gSdk.saveSM2DoubleCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert, encCert, encKeystore);
			       else
				      retcode = ShcaCciStd.gSdk.saveSM2SignatureCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert);
			*/
                    retcode = 0;
                    if (retcode == 0)
                        //retcode = ShcaCciStd.gSdk.changePin(cert.getContainerid(),CommonConst.JSHECACCISTD_PWD,getPWDHash(prikeyPassword));
                        retcode = gUcmSdk.changeUserPinWithCID(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, getPWDHash(prikeyPassword));

                    if (retcode == 0) {
                        if (!"".equals(encCert)) {
                            if (null == mCertDao.getCertByEnvsn(requestNumber + "-e", strActName)) {
                                Cert certEnc = new Cert();
                                certEnc.setEnvsn(requestNumber + "-e");
                                certEnc.setPrivatekey("");
                                certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                                certEnc.setCerttype(certType);
                                certEnc.setSignalg(cert.getSignalg());
                                certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
                                certEnc.setContainerid(cert.getContainerid());
                                certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
                                certEnc.setCertificate(encCert);
                                certEnc.setCertchain(certChain);
                                certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
                                certEnc.setValidtime(getCertValidtime(encCert));
                                certEnc.setKeystore("");
                                certEnc.setEnccertificate(encCert);
                                certEnc.setEnckeystore(encKeystore);
                                certEnc.setCertsn(getCertSN(encCert));
                                certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
                                certEnc.setDevicesn(Build.SERIAL);
                                certEnc.setCertname(strCertName);
                                certEnc.setCerthash(getPWDHash(prikeyPassword));
                                certEnc.setFingertype(CommonConst.USE_NO_FINGER_TYPE);
                                certEnc.setSealsn("");
                                certEnc.setSealstate(Cert.STATUS_NO_SEAL);

                                mCertDao.addCert(certEnc, strActName);
                            }
                        }
                    }
                }
            }
        }

        mCertDao.updateCert(cert, strActName);
        Toast.makeText(CertDownloadActivity.this, "保存证书成功", Toast.LENGTH_SHORT).show();

        saveLog(OperationLog.LOG_TYPE_APPLYCERT, cert.getCertsn(), "", "", "");
        final Handler handler = new Handler(CertDownloadActivity.this.getMainLooper());

        //网络访问必须放在子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //调用UMSP服务：设置证书保存成功状态
                    String responseStr = SetSuccessStatus(requestNumber, saveType);
                    JSONObject jb = JSONObject.fromObject(responseStr);
                    String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    String returnStr = jb.getString(CommonConst.RETURN_MSG);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });

                    if (resultStr.equals("0")) {
                        String strActName = mAccountDao.getLoginAccount().getName();
                        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

                        //Toast.makeText(CertDownloadActivity.this, "下载证书成功", Toast.LENGTH_SHORT).show();
                        Cert cert = mCertDao.getCertByEnvsn(requestNumber, strActName);
                        if (null != cert) {
                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                            mCertDao.updateCert(cert, strActName);
                        }

                        if (CommonConst.CERT_TYPE_SM2.equals(certType) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType) || certType.contains("SM2")) {
                            cert = mCertDao.getCertByEnvsn(requestNumber + "-e", strActName);
                            if (null != cert) {
                                cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                                mCertDao.updateCert(cert, strActName);
                            }
                        }
                    } else {
                        throw new Exception("调用UMSP服务之SetSuccessStatus失败：" + resultStr + "，" + returnStr);
                    }
                } catch (Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                        }
                    });
                    mError = exc.getMessage();
                    Log.e(CommonConst.TAG, mError, exc);
                }
            }
        }).start();

    }

    private String SetSuccessStatus(String requestNumber, int saveType) throws Exception {
        String timeout = CertDownloadActivity.this.getString(R.string.WebService_Timeout);
        String urlPath = CertDownloadActivity.this.getString(R.string.UMSP_Service_SetSuccessStatus);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        postParams.put("clientOSType", "1");  //客户端操作系统类型（1：Android；2：IOS；3：WP）
        postParams.put("clientOSDesc", getOSInfo());  //客户端操作系统描述
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
            postParams.put("media", "2");   //证书存储介质类型（1：文件；2：SD卡）
        else
            postParams.put("media", "1");   //证书存储介质类型（1：文件；2：SD卡）

        //String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        String postParam = "";
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType || CommonConst.SAVE_CERT_TYPE_SIM == saveType)
            postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8") +
                    "&clientOSType=" + URLEncoder.encode("1", "UTF-8") +
                    "&clientOSDesc=" + URLEncoder.encode(getOSInfo(), "UTF-8") +
                    "&media=" + URLEncoder.encode("2", "UTF-8");
        else
            postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8") +
                    "&clientOSType=" + URLEncoder.encode("1", "UTF-8") +
                    "&clientOSDesc=" + URLEncoder.encode(getOSInfo(), "UTF-8") +
                    "&media=" + URLEncoder.encode("1", "UTF-8");

        String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));
        return responseStr;
    }

    private String genP12(String privateKey, String pin, String cert, String chain) throws Exception {
        String p12 = "";
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream certBIn = new ByteArrayInputStream(
                Base64.decode(cert));
        Certificate certificate = cf.generateCertificate(certBIn);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ByteArrayInputStream bIn = new ByteArrayInputStream(
                Base64.decode(chain));
        CertPath oCertPath = cf.generateCertPath(bIn, "PKCS7");
        List certs = oCertPath.getCertificates();
        Certificate[] bChain = (Certificate[]) certs
                .toArray(new Certificate[certs.size() + 1]);
        bChain[certs.size()] = certificate;

        List certList = new ArrayList();
        for (Certificate c : bChain) {
            certList.add(c);
        }
        Collections.reverse(certList);
        bChain = (Certificate[]) certList.toArray(new Certificate[certList
                .size()]);

        KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(
                Base64.decode(privateKey));
        RSAPrivateKey privKey = (RSAPrivateKey) rsaKeyFac
                .generatePrivate(encodedKeySpec);
        ks.setKeyEntry("", privKey, pin.toCharArray(), bChain);

        ByteArrayOutputStream outp12 = new ByteArrayOutputStream();

        ks.store(outp12, pin.toCharArray());
        p12 = new String(Base64.encode(outp12.toByteArray()));
        outp12.close();
        return p12;
    }

    private void saveLog(int type, String certsn, String message, String invoker, String sign) {
        OperationLog log = new OperationLog();
        log.setType(type);
        log.setCertsn(certsn);
        log.setMessage(message);
        log.setSign(sign);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        log.setCreatetime(sdf.format(date));
        log.setInvoker(invoker);
        log.setSignalg(1);
        log.setIsupload(0);
        log.setInvokerid(CommonConst.UM_APPID);

        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        mLogDao.addLog(log, strActName);
    }

    private void checkCertStatus() throws Exception {
        List<Cert> certList = new ArrayList<Cert>();
        String strActName = mAccountDao.getLoginAccount().getName();
        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        certList = mCertDao.getAllCerts(strActName);

        for (Cert cert : certList) {
            if (cert.getEnvsn().isEmpty())
                continue;
            if (cert.getEnvsn().indexOf("-e") != -1)
                continue;

            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT && cert.getUploadstatus() == Cert.STATUS_UNUPLOAD_CERT) {
                String responseStr = SetSuccessStatus(cert.getEnvsn(), cert.getSavetype());
                JSONObject jb = JSONObject.fromObject(responseStr);
                String resultStr = jb.getString(CommonConst.RETURN_CODE);
                String returnStr = jb.getString(CommonConst.RETURN_MSG);

                if (resultStr.equals("0")) {
                    cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                    mCertDao.updateCert(cert, strActName);
                } else {
                    throw new Exception("调用UMSP服务之SetSuccessStatus失败：" + resultStr + "，" + returnStr);
                }
            }
        }

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

    //@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
		/*public void showProgress(final boolean show) {
			// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
			// for very easy animations. If available, use these APIs to fade-in
			// the progress spinner.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		
				mApplicationsView.setVisibility(show ? View.GONE : View.VISIBLE);
				mApplicationsView.animate().setDuration(shortAnimTime)
						.alpha(show ? 0 : 1)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mApplicationsView.setVisibility(show ? View.GONE : View.VISIBLE);
							}
						});

				
				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				mProgressView.animate().setDuration(shortAnimTime)
						.alpha(show ? 1 : 0)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mProgressView.setVisibility(show ? View.VISIBLE
										: View.GONE);
							}
						});
			} else {
				// The ViewPropertyAnimator APIs are not available, so simply show
				// and hide the relevant UI components.
				mApplicationsView.setVisibility(show ? View.VISIBLE : View.GONE);
				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		}
		*/
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

    private Boolean loginUMSPService(String act, int state) {    //重新登录UM Service
        String returnStr = "";
        try {
            //异步调用UMSP服务：用户登录
            String timeout = CertDownloadActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = CertDownloadActivity.this.getString(R.string.UMSP_Service_Login);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", act);
            postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
            if (mAccountDao.getLoginAccount().getType() == 1)
                postParams.put("appID", CommonConst.UM_APPID);
            else
                postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());

            String responseStr = "";
            try {
                //清空本地缓存
                //WebClientUtil.cookieStore = null;
                //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                String actpwd = "";
                if (mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
                    actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword());
                else
                    actpwd = mAccountDao.getLoginAccount().getPassword();

                String postParam = "";
                if (mAccountDao.getLoginAccount().getType() == 1)
                    postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                            "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
                            "&appID=" + URLEncoder.encode(CommonConst.UM_APPID, "UTF-8");
                else
                    postParam = "accountName=" + URLEncoder.encode(act, "UTF-8") +
                            "&pwdHash=" + URLEncoder.encode(actpwd, "UTF-8") +
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

            if (resultStr.equals("0")) {
                if (state == DOWNLAOD_CERT_STATE) {
                    //若成功登录，注册已登录账号，并跳转到首页；
                    Intent intent = new Intent(CertDownloadActivity.this, CertDownloadActivity.class);
                    //intent.putExtra("Account", mAccount);
                    //intent.putExtra("Message", "用户登录成功");
                    if (mIsScanDao)
                        intent.putExtra("scan", "dao");
                    if (mIsDao)
                        intent.putExtra("message", "dao");

                    startActivity(intent);
                    CertDownloadActivity.this.finish();
                    } else if (state == APPLY_CERT_STATE) {
                        return true;
                    }
                //showCertApplyList();
            } else if (resultStr.equals("10010")) {
                //若账号未激活，显示修改初始密码页面；
                Intent intent = new Intent(CertDownloadActivity.this, PasswordActivity.class);
                intent.putExtra("Account", mAccount);
                if (mIsScanDao)
                    intent.putExtra("scan", "dao");
                startActivity(intent);
                CertDownloadActivity.this.finish();
            } else if (resultStr.equals("10009")) {
                //若账号口令错误,显示账户登录页面；
                Account curAct = mAccountDao.getLoginAccount();
                curAct.setStatus(-1);   //重置登录状态为未登录状态
                mAccountDao.update(curAct);

                Intent intent = new Intent(CertDownloadActivity.this, LoginActivityV33.class);
                intent.putExtra("AccName", curAct.getName());
                if (mIsScanDao)
                    intent.putExtra("scan", "dao");
                startActivity(intent);
                CertDownloadActivity.this.finish();
            } else {
                throw new Exception(resultStr + ":" + returnStr);
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
            mError = exc.getMessage();
            Log.e(CommonConst.TAG, mError, exc);
            return false;
        }

        return true;
    }


    private String getPWDHash(String strPWD) {
        String strPWDHash = "";

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));
			
			/*try {
				strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        return strPWDHash;
    }

    private boolean checkBTDevice() {
        int nRet = -1;
        String p10 = "";

        if (CommonConst.SAVE_CERT_TYPE_PHONE == mAccountDao.getLoginAccount().getSaveType())
            return true;


        return true;
    }

    private String getOSInfo() {
        String strOSInfo = "";

        strOSInfo = "硬件型号:" + Build.MODEL + "|操作系统版本号:"
                + Build.VERSION.RELEASE;
        return strOSInfo;
    }

    private String getCertSN(String strCert) {
        String strCertSN = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strCertSN = jse.getCertDetail(2, bCert);
        } catch (Exception e) {

        }

        return strCertSN;
    }


    private String getCertNotbeforetime(String strCert) {
        String strNotBeforeTime = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strNotBeforeTime = jse.getCertDetail(11, bCert);
        } catch (Exception e) {

        }

        return strNotBeforeTime;
    }

    private String getCertValidtime(String strCert) {
        String strValidTime = "";

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strValidTime = jse.getCertDetail(12, bCert);
        } catch (Exception e) {

        }

        return strValidTime;
    }


    private boolean getCertApplyStatus(int position) {
    	  /* if("1".equals(mApplications.get(position).get("ApplyStatus"))){
    		   Toast.makeText(CertDownloadActivity.this, "该证书待签发,无法下载", Toast.LENGTH_SHORT).show();
			   return false;
    	   } */
        if ("4".equals(mApplications.get(position).get("ApplyStatus"))) {
            Toast.makeText(CertDownloadActivity.this, "该证书待审核,无法下载", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ("6".equals(mApplications.get(position).get("ApplyStatus"))) {
            //Toast.makeText(CertDownloadActivity.this, "该证书审核失败,无法下载", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CertDownloadActivity.this, ManualCheckFailActivity.class);
            intent.putExtra("BizSN", mApplications.get(position).get("BizSN"));
            startActivity(intent);
            return false;
        }
        if ("100".equals(mApplications.get(position).get("ApplyStatus"))) {
            Toast.makeText(CertDownloadActivity.this, "该证书待上送CA,无法下载", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isAccountRealName() {
//        if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
//            return true;
//
//        int actStatus = mAccountDao.getLoginAccount().getStatus();
//
//        Intent intent = null;
//        if (actStatus == 5 || actStatus == 3 || actStatus == 4) {  //账户已实名认证
//            return true;
//        } else {
//            intent = new Intent(CertDownloadActivity.this, AuthChoiceActivity.class);
//            intent.putExtra("download", "dao");
//            if (mIsScanDao)
//                intent.putExtra("message", "dao");
//            startActivity(intent);
//            CertDownloadActivity.this.finish();
//        }

        return false;
    }

    private void showDeleteCertApply(final String requestNumber, final String bizSN) {
        Builder builder = new Builder(CertDownloadActivity.this);
        builder.setIcon(R.drawable.alert);
        builder.setTitle("提示");
        builder.setMessage("是否删除该条证书申请？");
        builder.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCertApply(requestNumber, bizSN);
                        dialog.dismiss();
                    }
                });

        builder.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }


    private void deleteCertApply(final String requestNumber, final String bizSN) {
        final Handler handler = new Handler(CertDownloadActivity.this.getMainLooper());
        showProgDlg("删除证书申请中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String timeout = CertDownloadActivity.this.getString(R.string.WebService_Timeout);
                    final String urlPath = CertDownloadActivity.this.getString(R.string.UMSP_Service_DeleteCertApply);

                    Map<String, String> postParams = new HashMap<String, String>();
                    postParams.put("requestNumber", requestNumber);
                    postParams.put("bizSN", bizSN);

                    //final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
                    String postParam = "requestNumber=" + URLEncoder.encode(requestNumber, "UTF-8") +
                            "&bizSN=" + URLEncoder.encode(bizSN, "UTF-8");
                    final String responseStr = WebClientUtil.getHttpClientPost(urlPath, postParam, Integer.parseInt(timeout));

                    JSONObject jb = JSONObject.fromObject(responseStr);
                    final String resultStr = jb.getString(CommonConst.RETURN_CODE);
                    final String returnStr = jb.getString(CommonConst.RETURN_MSG);

                    if (resultStr.equals("0")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                                Toast.makeText(CertDownloadActivity.this, "该证书申请已删除", Toast.LENGTH_SHORT).show();
                                showCertApplyList();
                            }
                        });

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeProgDlg();
                            }
                        });

                        throw new Exception(returnStr);
                    }

                } catch (final Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeProgDlg();
                            Toast.makeText(CertDownloadActivity.this, exc.getMessage() + "", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    private int getPersonalInfo() {

        strPersonName = AccountHelper.getRealName(this);
        strPaperNO = AccountHelper.getIdcardno(this);

        return 1;
// 		String responseStr = "";
// 		String resultStr = "";
// 		String returnStr = "";
//
// 		int retState = 1;
//
// 		try {
// 			String timeout = CertDownloadActivity.this.getString(R.string.WebService_Timeout);
// 			String urlPath = CertDownloadActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);
//
// 			Map<String,String> postParams = new HashMap<String,String>();
//
// 			try {
// 				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
// 				String postParam = "";
// 				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
// 				//Thread.sleep(3000);
// 			} catch (Exception e) {
// 				strPersonName = "";
// 				strPaperNO = "";
// 				return 1;
// 			}
//
// 			if(null == responseStr || "null".equals(responseStr)){
// 				strPersonName = "";
// 				strPaperNO = "";
// 				return 1;
// 			}
//
// 			JSONObject jb = JSONObject.fromObject(responseStr);
// 			resultStr = jb.getString(CommonConst.RETURN_CODE);
// 			returnStr = jb.getString(CommonConst.RETURN_MSG);
//
// 			if (resultStr.equals("0")) {
// 				JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
//
// 				retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
// 				if(null != jbRet.getString(CommonConst.PARAM_NAME))
// 					strPersonName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
// 				else
// 					strPersonName = "";
// 				if(null != jbRet.getString(CommonConst.PARAM_IDENTITY_CODE))
// 					strPaperNO = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号
// 				else
// 					strPaperNO = "";
// 			}else {
// 				strPersonName = "";
// 				strPaperNO = "";
// 			}
//
// 		} catch (Exception exc) {
// 			strPersonName = "";
// 			strPaperNO = "";
// 		    return 1;
// 		}
//
// 		return retState;
    }

    private void showPayActivity(String strReqNumber, String strStatus, String strCertType) {
        Intent intent = new Intent();
        intent.setClass(CertDownloadActivity.this, com.sheca.umee.PayActivity.class);
        intent.putExtra("loginAccount", strPersonName);
        intent.putExtra("loginId", strPaperNO);
        intent.putExtra("requestNumber", strReqNumber);
        intent.putExtra("applyStatus", strStatus);
        if (mBBTDeviceUsed)
            intent.putExtra("bluetoothpwd", mStrBTDevicePwd);
        if (!"".equals(strCertType))
            intent.putExtra("certtype", strCertType);

        CertDownloadActivity.this.startActivity(intent);
        CertDownloadActivity.this.finish();
    }

    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertDownloadActivity.this);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;

            if (retcode != 0)
                ShcaCciStd.gSdk = null;
        }

        return retcode;
    }

    private int initShcaUCMService() {  //初始化创元中间件
        int retcode = -1;

        String myHttpBaseUrl = CertDownloadActivity.this.getString(R.string.UMSP_Base_Service);
        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);

        return retcode;
    }

    private boolean checkShcaCciStdServiceState(int actCertType) {
        if (CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
            return true;

        if (mIsDao)
            return true;
    	/*
    	try{
    		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
   			   ShcaCciStd.gSdk = ShcaCciStd.getInstance(CertDownloadActivity.this);
   			   ShcaCciStd.errorCode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);    		
   			   if(ShcaCciStd.errorCode != 0 )
   			    	ShcaCciStd.gSdk = null;
   		    }
    		
    		if(null == ShcaCciStd.gSdk)
    			return false;
    			
    		if(ShcaCciStd.errorCode != 0)
    			return false;		
	
    	}catch(Exception ex){
    		return false;
    	}
    	*/
        return true;

    }


}
