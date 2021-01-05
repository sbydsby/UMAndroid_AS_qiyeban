package com.sheca.zhongmei;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.junyufr.szt.util.Base64ImgUtil;
import com.sheca.javasafeengine;
import com.sheca.jshcaucmstd.JShcaUcmStd;
import com.sheca.jshcaucmstd.JShcaUcmStdRes;
import com.sheca.jshcaucmstd.myWebClientUtil;
import com.sheca.zhongmei.adapter.CertAdapter;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.SealInfoDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.ShcaCciStd;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.PKIUtil;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.zhongmei.util.WebClientUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
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
import java.util.TimeZone;

@SuppressLint("SetJavaScriptEnabled")
public class SealPreviewActivity extends Activity {
    private WebView mWebView = null;
    private ProgressDialog progDialog = null;

    private Handler handler = null;
    private final String NETWORK_ONLINE = "file:///android_asset/sealpreview.html";
    private String strSealPic = "";

    private String strSign = "";        //签名数据
    private String strCert = "";        //base64证书
    private int mCertId = -1;        //当前选中证书ID
    private boolean mIsViewCert = false; //是否可查看证书详情
    private String mStrCertPwd = "";
    private String strSealName = "";    //印章别名
    private int localCertid;

    private javasafeengine jse = null;
    private List<Map<String, String>> mData = null;
    private AlertDialog certListDialog = null;
    private CertDao certDao = null;
    private AccountDao mAccountDao = null;
    private SealInfoDao mSealInfoDao = null;

    private JShcaUcmStd gUcmSdk = null;

    private int sdkid;

    private final static int LOGIN_SIGN_FAILURE = 1;
    private final static int LOGIN_SIGN_SUCCESS = 2;

    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_apply_seal_preview);

        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("申请印章");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制竖屏
        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SealPreviewActivity.this.finish();
            }
        });

        Security.addProvider(new BouncyCastleProvider());     //导入bc包操作

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("SealPic") != null)
                strSealPic = extras.getString("SealPic");
        }

        if (null == strSealPic || "".equals(strSealPic)) {
            Toast.makeText(SealPreviewActivity.this, "手写签名有误,请重新输入", Toast.LENGTH_SHORT).show();
            SealPreviewActivity.this.finish();
        }

        gUcmSdk = JShcaUcmStd.getIntence(this.getApplication(), this);
        handler = new Handler(SealPreviewActivity.this.getMainLooper());
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (mWebView != null) {
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    closeProgDlg();
                }
            });

            //mWebView.loadUrl(NETWORK_ONLINE_TEST);  //测试demo地址

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);//打开js支持
            /**
             * 打开js接口給H5调用，参数1为本地类名，参数2为别名；h5用window.别名.类名里的方法名才能调用方法里面的内容，例如：window.android.back();
             * */
            mWebView.addJavascriptInterface(new JsInteration(), "android");
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.setWebChromeClient(new WebChromeClient());
        }


        jse = new javasafeengine();
        mAccountDao = new AccountDao(SealPreviewActivity.this);
        certDao = new CertDao(SealPreviewActivity.this);
        mSealInfoDao = new SealInfoDao(SealPreviewActivity.this);

        showPreviewWnd();

        TextView okBtn = ((TextView) findViewById(R.id.apply_seal_button));
        okBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDoSign();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                SealPreviewActivity.this.finish();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void loadNetworkOnline() {
        if (null != mWebView) {
            //showProgDlg("正在加载数据中...");
            mWebView.loadUrl(NETWORK_ONLINE + "?a=" + strSealPic);
            //mWebView.loadUrl("file:///android_asset/test.html?a="+strPic);//加载本地asset下面的js_java_interaction.html文件
            // mWebView.reload();
        }
    }

    private void showPreviewWnd() {
        Bitmap bitMap = bitMapScale(stringtoBitmap(strSealPic), 0.3f);
        if (null == bitMap) {
            findViewById(R.id.webView).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.list_image).setVisibility(RelativeLayout.GONE);
            loadNetworkOnline();
        } else {
            findViewById(R.id.webView).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.list_image).setVisibility(RelativeLayout.VISIBLE);
            ((ImageView) findViewById(R.id.list_image)).setImageBitmap(bitMap);
            ((ImageView) findViewById(R.id.list_image)).invalidate();
        }


//        findViewById(R.id.textCertView).getBackground().setAlpha(100);  //0~255透明度值
        LaunchActivity.isIFAAFingerOK = false;
        showCert();
    }


    private void showCert() {
        try {
            ImageView viewCertBtn = ((ImageView) findViewById(R.id.btnCertView));
            viewCertBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    selectCert();
                }
            });

            //findViewById(R.id.btnCertView).getBackground().setAlpha(100);  //0~255透明度值

            mData = getData();
            if (mData.size() == 0) {
                ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                mIsViewCert = false;
                ((EditText) findViewById(R.id.sealname)).setText("");
                return;
            }

            viewCertBtn.setVisibility(RelativeLayout.VISIBLE);

            mCertId = Integer.valueOf(mData.get(0).get("id"));
            final Cert cert = certDao.getCertByID(mCertId);

            sdkid = cert.getSdkID();

            if (cert != null) {
                mIsViewCert = true;
                String certificate = cert.getCertificate();
                byte[] bCert = Base64.decode(certificate);
                String strBlank = "证书";
                String strCertName = jse.getCertDetail(17, bCert);
                if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()))
                    strCertName += CommonConst.CERT_SM2_NAME + strBlank;
                else
                    strCertName += CommonConst.CERT_RSA_NAME + strBlank;

                String strSealBlank = "的印章";
                strSealBlank = strCertName + strSealBlank;
                if (null == cert.getCertname()) {
                    ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                } else {
                    if (cert.getCertname().isEmpty())
                        ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                    else {
                        ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
                        strSealBlank = cert.getCertname() + "的印章";
                    }
                }


                ((EditText) findViewById(R.id.sealname)).setText(strSealBlank);
            } else {
                ((TextView) findViewById(R.id.textCertView)).setText("无证书");
                mIsViewCert = false;
                ((EditText) findViewById(R.id.sealname)).setText("");
            }

            ((TextView) findViewById(R.id.textCertView)).setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View view) {
                            viewCertDetail();
                        }
                    });

        } catch (Exception e) {
            Log.e(CommonConst.TAG, e.getMessage(), e);
            Toast.makeText(SealPreviewActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.textCertView)).setText("无证书");
            mIsViewCert = false;
            ((EditText) findViewById(R.id.sealname)).setText("");
        }

    }


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

        certList = certDao.getAllCertsNoSeal(strActName);

        for (Cert cert : certList) {
            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

            if (verifyCert(cert, false)) {
                if (verifyDevice(cert, false)) {
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


                        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()))
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
                Toast.makeText(SealPreviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

        } else {
            Toast.makeText(SealPreviewActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
            return;
        }

        Builder builder = new Builder(SealPreviewActivity.this);
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
                Toast.makeText(SealPreviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

        } else {
            Toast.makeText(SealPreviewActivity.this, "证书不存在", Toast.LENGTH_LONG).show();
            return;
        }

        Builder builder = new Builder(SealPreviewActivity.this);
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
			  Toast.makeText(SealPreviewActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
		  }*/ else {
                if (bShow)
                    Toast.makeText(SealPreviewActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
            }
        } else if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())) {
            String strSignCert = "";

            if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn())) {
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
                        Toast.makeText(SealPreviewActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
                    if (bShow)
                        Toast.makeText(SealPreviewActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SealPreviewActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
            }
        } else if (CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())) {
            String strSignCert = "";

            if (cert.getEnvsn().indexOf("-e") != -1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn())) {
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
                        Toast.makeText(SealPreviewActivity.this, "证书过期", Toast.LENGTH_SHORT).show();
                } else {
                    if (bShow)
                        Toast.makeText(SealPreviewActivity.this, "验证证书失败", Toast.LENGTH_SHORT).show();
                }

            }
        }

        return false;
    }

    private boolean verifyDevice(final Cert cert, boolean bShow) {
       /* if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
            return true;
        if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
            return true;

        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);
        Certificate oCert = jse.getCertFromBuffer(bCert);
        X509Certificate oX509Cert = (X509Certificate) oCert;
        String sDeciceID = jse.getCertExtInfo("1.2.156.112570.12.102", oX509Cert);

        //	if(mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
        //	return true;
        if (null == sDeciceID)
            return true;

        //获取设备唯一标识符
        String deviceID = android.os.Build.SERIAL;
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
            deviceID = cert.getDevicesn();
        if (sDeciceID.equals(deviceID))
            return true;

        if (bShow)
            Toast.makeText(SealPreviewActivity.this, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
*/
        return true;
    }

    private void selectCert() {
        if (!mIsViewCert) {
            Toast.makeText(SealPreviewActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
            return;
        }

        changeCert();
    }


    private void changeCert() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certListView = inflater.inflate(R.layout.certlist, null);
        ListView list = (ListView) certListView.findViewById(R.id.certlist);
        CertAdapter adapter = null;

        try {
            adapter = new CertAdapter(SealPreviewActivity.this, mData);
            list.setAdapter(adapter);

            Builder builder = new Builder(SealPreviewActivity.this);
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
            Toast.makeText(SealPreviewActivity.this, "获取证书错误！", Toast.LENGTH_SHORT).show();
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCertId = Integer.valueOf(mData.get(position).get("id"));
                final Cert cert = certDao.getCertByID(mCertId);
                sdkid = cert.getSdkID();
                try {
                    String certificate = cert.getCertificate();
                    byte[] bCert = Base64.decode(certificate);
                    String strBlank = "证书";
                    String strCertName = jse.getCertDetail(17, bCert);
                    if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()))
                        strCertName += CommonConst.CERT_SM2_NAME + strBlank;
                    else
                        strCertName += CommonConst.CERT_RSA_NAME + strBlank;

                    if (null == cert.getCertname()) {
                        ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                    } else {
                        if (cert.getCertname().isEmpty())
                            ((TextView) findViewById(R.id.textCertView)).setText(strCertName);
                        else
                            ((TextView) findViewById(R.id.textCertView)).setText(cert.getCertname());
                    }

                    String strSealBlank = "的印章";
                    ((EditText) findViewById(R.id.sealname)).setText(strCertName + strSealBlank);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                certListDialog.dismiss();
            }
        });

    }

    private void viewCertDetail() {
        if (!mIsViewCert) {
            Toast.makeText(SealPreviewActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
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

        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()))
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

    private void setCertPwd() {
        Builder builder = new Builder(SealPreviewActivity.this);
        builder.setIcon(R.drawable.alert);
        builder.setTitle("请输入证书密码");
        builder.setCancelable(false);
        // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(SealPreviewActivity.this).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);
        final EditText prikeyPasswordView = (EditText) view.findViewById(R.id.et_prikey_password);
        final EditText prikeyPassword2View = (EditText) view.findViewById(R.id.et_prikey_password2);
        prikeyPassword2View.setVisibility(RelativeLayout.GONE);
        prikeyPasswordView.setHint("输入证书密码");

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
                                Toast.makeText(SealPreviewActivity.this, "无效的证书密码", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            mStrCertPwd = prikeyPassword;

                            doSign();
                        } catch (Exception e) {
                            Log.e(CommonConst.TAG, e.getMessage(), e);

                        }

                        dialog.dismiss();
                    }
                });

        builder.show();
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

    private void showDoSign() {
        if (!mIsViewCert) {
            Toast.makeText(SealPreviewActivity.this, "不存在证书", Toast.LENGTH_SHORT).show();
            return;
        }

        strSealName = ((EditText) findViewById(R.id.sealname)).getText().toString().trim();
        if ("".equals(strSealName)) {
            Toast.makeText(SealPreviewActivity.this, "输入印章别名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strSealName.length() > 16) {
            Toast.makeText(SealPreviewActivity.this, "印章别名长度不超过16位字符", Toast.LENGTH_SHORT).show();
            return;
        }

        setCertPwd();
    }


    private void doSign() {
        final Cert cert = certDao.getCertByID(mCertId);

        if (verifyCert(cert, true)) {
            if (verifyDevice(cert, true)) {
//                if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())) {
//                    signSM2(cert);
//                } else {
//                    sign(cert);
//                }
                if (!cert.getCerthash().equals(getPWDHash(mStrCertPwd,cert)) ){
                    Toast.makeText(this,"证书密码错误",Toast.LENGTH_SHORT).show();
                    return;
                }

                String strActName = mAccountDao.getLoginAccount().getName();
                if (mAccountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
                    strActName = mAccountDao.getLoginAccount().getName() + "&" + mAccountDao.getLoginAccount().getAppIDInfo().replace("-", "");
                localCertid = cert.getId();
                getCertIdByCertSn(SealPreviewActivity.this,strActName,cert.getCertsn());
//


//                handlerSign.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
            }
        }
    }




    public void getCertIdByCertSn(Activity context, String userName, String certSn) {
        final UniTrust uniTrustObi = new UniTrust(context, false);
        final String param = ParamGen.getAccountCertByCertSN(context, userName, certSn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.sheca.umplus.model.Cert cert = uniTrustObi.getAccountCertByCertSN(param);
                if (null != cert) {
                    sdkid = cert.getId();
                    handlerSign.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
                }
            }
        }).start();


    }




    private void sign(final Cert cert) {
        String strAccountPwd = mStrCertPwd;
        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        localCertid = cert.getId();
        final String sPwd = strAccountPwd;
        if (sPwd != null && !"".equals(sPwd)) {
            final String sKeyStore = cert.getKeystore();
            byte[] bKeyStore = Base64.decode(sKeyStore);
            ByteArrayInputStream kis = new ByteArrayInputStream(bKeyStore);
            KeyStore oStore = null;

            try {
                oStore = KeyStore.getInstance("PKCS12");
                oStore.load(kis, sPwd.toCharArray());
            } catch (Exception e) {
                handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                //Toast.makeText(SealPreviewActivity.this, "证书密码错误",Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread() {
                @Override
                public void run() {
                    String message = strSealPic;

                    try {
                        strSign = PKIUtil.sign(message.getBytes(CommonConst.SIGN_STR_CODE), cert.getPrivatekey(), sPwd,SealPreviewActivity.this);
                    } catch (Exception e) {
                        Log.e("sheca", e.getMessage(), e);
                        handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }

                    if (strSign != null && !"".equals(strSign)) {
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
//								dialog.dismiss();
                            strCert = cert.getCertificate();
                            handlerSign.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
                        } else {
                            Toast.makeText(
                                    SealPreviewActivity.this,
                                    "验证签名失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                        //Toast.makeText(SealPreviewActivity.this,"签名失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }.start();
        } else {
            Toast.makeText(SealPreviewActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();
        }
    }

    private void signSM2(final Cert cert) {
        String strAccountPwd = mStrCertPwd;
        if (!LaunchActivity.isIFAAFingerOK) {
            if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
                strAccountPwd = getPWDHash(strAccountPwd, cert);
        }

        final String sPwd = strAccountPwd;
        String strSignCert = "";

        if (sPwd != null && !"".equals(sPwd)) {
            try {
                //if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                //initShcaCciStdService();
                int retCode = -1;
                if (null != gUcmSdk)
                    retCode = initShcaUCMService();

                if (retCode != 0) {
                    Toast.makeText(SealPreviewActivity.this, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                //int ret =  ShcaCciStd.gSdk.verifyUserPin(cert.getContainerid(), sPwd);
                int ret = gUcmSdk.verifyUserPinWithCID(cert.getContainerid(), sPwd);
                if (ret != 0) {
                    //ret =  ShcaCciStd.gSdk.unLockPin(cert.getContainerid());
                    handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    //Toast.makeText(SealPreviewActivity.this, "证书密码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                //strSignCert = ShcaCciStd.gSdk.readSM2SignatureCert(cert.getContainerid());
					    /* if(null == strSignCert || "".equals(strSignCert)){
					    	 Toast.makeText(DaoActivity.this, "证书签名失败",Toast.LENGTH_SHORT).show();
							 resState = 1;
							 return;
					    }*/
            } catch (Exception e) {
                ShcaCciStd.gSdk = null;
                handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                //Toast.makeText(SealPreviewActivity.this, "密码分割组件初始化失败",Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread() {
                @Override
                public void run() {

                    String message = strSealPic;
                    byte[] signDate = null;

                    try {
                        // signDate =  ShcaCciStd.gSdk.doSM2Signature(message.getBytes(CommonConst.SIGN_STR_CODE), sPwd, cert.getContainerid());
                        JShcaUcmStdRes jres = gUcmSdk.doSM2SignatureWithCID(cert.getContainerid(), sPwd, message.getBytes(CommonConst.SIGN_STR_CODE), CommonConst.SERT_TYPE);
                        //strSign = new String(Base64.encode(signDate));
                        if (null == jres.response || "".equals(jres.response))
                            handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);

                        strSign = jres.response;
                    } catch (Exception e) {
                        Log.e("sheca", e.getMessage(), e);
                        handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                    }

                    if (strSign != null && !"".equals(strSign)) {
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
//								dialog.dismiss();
                            strCert = cert.getCertificate();
                            handlerSign.sendEmptyMessage(LOGIN_SIGN_SUCCESS);
                        } else {
                            Toast.makeText(
                                    SealPreviewActivity.this,
                                    "验证签名失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handlerSign.sendEmptyMessage(LOGIN_SIGN_FAILURE);
                        //Toast.makeText(SealPreviewActivity.this,"签名失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }.start();
        } else {
            Toast.makeText(SealPreviewActivity.this, "请输入证书密码", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean loginUMSPService(String act) throws Exception {    //重新登录UM Service
        String returnStr = "";
        try {
            //showProgDlg("获取更新数据中...");
            //异步调用UMSP服务：用户登录
            String timeout = SealPreviewActivity.this.getString(R.string.WebService_Timeout);
            String urlPath = SealPreviewActivity.this.getString(R.string.UMSP_Service_Login);
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


        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        if(cert.getFingertype()   == CommonConst.USE_FINGER_TYPE)
            bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

        return strPWDHash;
    }

    private Handler handlerSign = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SIGN_FAILURE: {
                    Toast.makeText(SealPreviewActivity.this, "证书密码错误或数字签名错误", Toast.LENGTH_SHORT).show();
                }
                break;
                case LOGIN_SIGN_SUCCESS: {
                    final Cert cert = certDao.getCertByID(localCertid);

                    Intent intent = new Intent(SealPreviewActivity.this, ApplySealActivity.class);
                    intent.putExtra("UserType", CommonConst.ACCOUNT_TYPE_PERSONAL + "");
                    intent.putExtra("SealName", strSealName);
                    intent.putExtra("PicData", strSealPic);
                    intent.putExtra("PicType", CommonConst.SEAL_PIC_TYPE);
                    intent.putExtra("Cert", strCert);
                    intent.putExtra("SignData", strSign);
                    intent.putExtra("CertID", sdkid);
                    intent.putExtra("localCertid", localCertid);
//                    intent.putExtra("psd", certPsdHash);
                    intent.putExtra("psd", getPWDHash(mStrCertPwd,cert));
                    startActivity(intent);
                    SealPreviewActivity.this.finish();
                }
                break;
            }
        }
    };


    private int initShcaCciStdService() {  //初始化创元中间件
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        int retcode = -100;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(SealPreviewActivity.this);
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

        String myHttpBaseUrl = SealPreviewActivity.this.getString(R.string.UMSP_Base_Service);
        if (null == WebClientUtil.mCookieStore || "".equals(WebClientUtil.mCookieStore))
            loginUMSPService(mAccountDao.getLoginAccount().getName());

        myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);

        bRan = jse.random(256, "SHA1PRNG", "SUN");
        gUcmSdk.setRandomSeed(bRan);

        retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);
        if (retcode == 10012) {
            loginUMSPService(mAccountDao.getLoginAccount().getName());
            myWebClientUtil.setCookieStore(WebClientUtil.mCookieStore);
            retcode = gUcmSdk.doInitService(myHttpBaseUrl, CommonConst.UM_APPID);
        }

        return retcode;
    }

    public Bitmap stringtoBitmap(String picString) {
        //将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapdata = Base64ImgUtil.GenerateImageByte(picString);
            bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public Bitmap bitMapScale(Bitmap bitmap, float scale) {
        if (null == bitmap)
            return bitmap;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return resizeBmp;
    }

    /**
     * 自己写一个类，里面是提供给H5访问的方法
     */
    public class JsInteration {

        @JavascriptInterface//一定要写，不然H5调不到这个方法
        public String back(String base64PNG) {
            base64PNG = base64PNG.split(",")[1];
            base64PNG = base64PNG.substring(1);
            if (base64PNG.endsWith("]"))
                base64PNG = base64PNG.substring(0, base64PNG.length() - 1);
            if (base64PNG.endsWith("\""))
                base64PNG = base64PNG.substring(0, base64PNG.length() - 1);

            final String strPic = base64PNG;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    showProgDlg("界面加载中...");

                    try {
                        //Toast.makeText(NetworkSignActivity.this, strPic,Toast.LENGTH_SHORT).show();
                        mWebView.loadUrl("file:///android_asset/test.html?a=" + strPic);//加载本地asset下面的js_java_interaction.html文件
                        closeProgDlg();
                    } catch (Exception ex) {
                        String strErr = ex.getMessage();
                        strErr += "\n" + ex.getLocalizedMessage();
                    }


                }
            });

            return "我是java里的方法返回值\npng:" + base64PNG;
        }
    }


    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        //progDialog.setCancelable(false);
        progDialog.show();
    }


    private void closeProgDlg() {
        if (null != progDialog && progDialog.isShowing()) {
            progDialog.dismiss();
            progDialog = null;
        }
    }

}
