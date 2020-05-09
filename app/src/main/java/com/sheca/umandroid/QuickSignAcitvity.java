package com.sheca.umandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.util.PKIUtil;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;

import pl.droidsonroids.gif.GifImageView;


public class QuickSignAcitvity extends BaseActivity {

    //    @BindView(R.id.view_guidline7)
//    View mViewGuidline7;
    private UniTrust uniTrust;
    String result;

    String certSn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_sign);

        initView();
        result = getIntent().getStringExtra("result");

        showDg();


        dosign();
//        autoLogin();


    }

    private void initView() {
        uniTrust = new UniTrust(this, false);
    }


    //            accountUID
//        signBizNO
//        signCertSN
    private void dosign() {
        try {
            final JSONObject jb = JSONObject.fromObject(result);
            certSn = jb.optString(CommonConst.PARAM_SIGN_CERT_SN);


            CertDao certDao = new CertDao(this);
            Cert cert = certDao.getCertByCertsn(certSn, AccountHelper.getUsername(getApplicationContext()));
            if (null == cert) {
                Toast.makeText(this, "无有效证书", Toast.LENGTH_LONG).show();
                finish();
                return;
            }


            GetSignBizData(jb.optString(CommonConst.PARAM_SIGN_BIZ_NO));


        } catch (Exception e) {
            GetSignBizData(result);
        }
    }

    private void autoLogin() {//自动登录
        showDg();
        final UniTrust uniTrust = new UniTrust(this, false);
        new MyAsycnTaks() {
            @Override
            public void preTask() {
                //

            }

            @Override
            public void doinBack() {


                String mStrVal = uniTrust.userAutoLogin(ParamGen.getAutoLoginParam(AccountHelper.getToken(QuickSignAcitvity.this)));
                Log.e("自动登录", mStrVal);
                try {
                    APPResponse response = new APPResponse(mStrVal);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                dosign();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDg();
                                finish();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDg();
                            finish();
                        }
                    });
                }


            }


            @Override
            public void postTask() {

            }
        }.execute();
    }


    //获取待签名数据
    private void GetSignBizData(final String signBizNO) {

        showDg();
        final String param = ParamGen.GetSignBizData(this, signBizNO);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String mStrVal = uniTrust.GetSignBizData(param);
                    final APPResponse response = new APPResponse(mStrVal);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                                String signBizData = response.getResult().optString("signBizData");
//                                String certSn = response.getResult().optString("certSn");//暂定

                                doSign(signBizNO, signBizData, certSn);
                            } else {
                                dismissDg();
                                Toast.makeText(QuickSignAcitvity.this, "获取待签名数据失败", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });


                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDg();
                            Toast.makeText(QuickSignAcitvity.this, "获取待签名数据失败", Toast.LENGTH_LONG).show();
                            finish();

                        }
                    });
                }
            }
        }).start();

    }


    private void doSign(String signBizNO, String signBizData, String certSn) {


        CertDao certDao = new CertDao(this);
        Cert cert = certDao.getCertByCertsn(certSn, AccountHelper.getUsername(getApplicationContext()));
        if (null != cert) {
            if (cert.getCerttype().toUpperCase().contains("SM2")) {//SM2
                JShcaUcmStdRes jres = new JShcaUcmStdRes();
                JShcaUcmStd gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
                if (null != gUcmSdk) {
                    try {
                        int reCode = initShcaUCMService(gUcmSdk);
                        if (reCode != 0) {
                            Toast.makeText(QuickSignAcitvity.this, "初始化失败，请重试", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(QuickSignAcitvity.this, "初始化失败，请重试", Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                        return;
                    }
                }

                try {
                    jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), cert.getCerthash(), signBizData.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                    String strSign = jres.response; //new String(Base64.encode(signDate));
                    UploadSignValue(signBizNO, strSign, true);
                } catch (UnsupportedEncodingException e) {
                    UploadSignValue(signBizNO, e.getMessage(), false);
                }


            } else {//RSA

                try {

                    String strSign = PKIUtil.sign(signBizData.getBytes(CommonConst.SIGN_STR_CODE), cert.getKeystore(), cert.getCerthash());
                    UploadSignValue(signBizNO, strSign, true);
                } catch (Exception e) {
                    UploadSignValue(signBizNO, e.getMessage(), false);

                }
            }
        } else {
            Toast.makeText(this, "证书不存在", Toast.LENGTH_SHORT).show();
        }
    }

    private int initShcaUCMService(JShcaUcmStd gUcmSdk) throws Exception {  //初始化CA手机盾中间件
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        int retcode = -1;
        byte[] bRan = null;

        String myHttpBaseUrl = QuickSignAcitvity.this.getString(R.string.UMSP_Base_Service);
// 		if(null == WebClientUtil.mCookieStore || "".equals(WebClientUtil.mCookieStore))
// 			loginUMSPService(mAccountDao.getLoginAccount().getName());
//
//		myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);


        //gUcmSdk.setRandomSeed(bRS);
        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);


        return retcode;
    }

    private void UploadSignValue(String signBizNO, String result, boolean isSuccess) {


        final String param = ParamGen.UploadSignValue(this, signBizNO, result, isSuccess);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String mStrVal = uniTrust.UploadSignValue(param);
                    APPResponse response = new APPResponse(mStrVal);
                    final int retCode = response.getReturnCode();
                    final String retMsg = response.getReturnMsg();

Log.e("签名结果",retMsg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDg();
                            if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                                String signBizData = response.getResult().optString("signBizData");


                            } else {

//                                Toast.makeText(MainActivityNew.this, "上传签名结果失败", Toast.LENGTH_LONG).show();
                            }
                            finish();
                        }
                    });


                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDg();
//                            Toast.makeText(MainActivityNew.this, "上传签名结果失败", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            }
        }).start();
    }


    public void showDg() {
//        showProgDlg("请稍候...");

//        if (dialog == null) {
//            dialog = new MyDialog(this);
//            DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            };
//            dialog.setOnKeyListener(keylistener);
//        }
//        LayoutInflater inflater = getLayoutInflater();
//        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_launch_activity, null);
//
//        ((GifImageView) layout.findViewById(R.id.launch_loading)).setBackgroundResource(R.drawable.launchloading);
//
//
//        dialog.show();
//        dialog.setCancelable(false);
//        dialog.setContentView(layout);// show方法要在前面

    }


    public void dismissDg() {
//        closeProgDlg();

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

    }

    MyDialog dialog = null;


    public class MyDialog extends AlertDialog {
        Context mContext;

        public MyDialog(Context context) {
            super(context, R.style.FullDialog); // 自定义全屏style
            this.mContext = context;
        }

//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//        }

        @Override
        public void show() {
            super.show();
            /**
             * 设置宽度全屏，要设置在show的后面
             */

            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
//解决切换黑屏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0f;

            getWindow().getDecorView().setPadding(0, 0, 0, 0);
            getWindow().setAttributes(layoutParams);
        }
    }

}
