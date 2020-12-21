package com.sheca.umee.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.security.KeyChain;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umee.CertDownloadActivity;
import com.sheca.umee.CertManageActivity;
import com.sheca.umee.R;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.account.ReLoginActivityV33;
import com.sheca.umee.adapter.ViewCertPagerAdapter;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.LogDao;
import com.sheca.umee.event.RefreshEvent;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.ShcaCciStd;
import com.sheca.umee.presenter.CertController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;
import com.sheca.umplus.util.PKIUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QiyeCertFragment extends Fragment {

    private List<Map<String, String>> mData = null;

    private javasafeengine jse = null;

    private CertDao certDao = null;

    private AccountDao accountDao = null;

    private LogDao logDao = null;

    private int certID = 0;

    private Cert mCert = null;

    private View view = null;

    private Context context = null;

    private Activity activity = null;

    private ProgressDialog progDialogCert = null;

    private int cunIndex = -1;

    private SharedPreferences sharedPrefs;

    //private  JShcaKsStd gKsSdk = null;

    protected Handler workHandler = null;

    private HandlerThread ht = null;

    private static final int INSTALL_KEYCHAIN_CODE = 1;

    private static final String DEFAULT_ALIAS = "My KeyStore";

    private ArrayList<View> views = null;

    private ViewPager viewPager;

    private int count = 1;

    private ViewCertPagerAdapter adapter;

    private static int pageNum = 1;

    private static int positionNow = 0;

    private LinearLayout relativeLayout;

    private final int ITEM_COUNT = 1;

    private KeyPair mKeyPair = null;

    private String mContainerid = "";

    private String strENVSN = "";

//    AuthController controller = new AuthController();
	/*
	 private GestureDetector mGestureDetector;

	 private OnTouchListener myTouch = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return mGestureDetector.onTouchEvent(event);
		}

    };*/

    boolean certType = false;//0个人 1单位

    TextView txt_cert_num;

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();

        try {
            view = inflater.inflate(R.layout.fragment_cert, container, false);
            context = view.getContext();


            EventBus.getDefault().register(this);

            txt_cert_num = view.findViewById(R.id.txt_cert_num);
            LinearLayout ll_download = (LinearLayout) view.findViewById(R.id.ll_download);
            ll_download.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (AccountHelper.hasLogin(getActivity())) {

                        startActivity(new Intent(activity, CertDownloadActivity.class));

                    } else {

                        if (AccountHelper.isFirstLogin(getActivity())) {
                            Intent intentLoignV33 = new Intent(getActivity(), LoginActivityV33.class);
                            startActivity(intentLoignV33);
                        } else {
                            Intent intentLoignV33 = new Intent(getActivity(), ReLoginActivityV33.class);
                            startActivity(intentLoignV33);
                        }

                    }

                }
            });
            ImageView img_add = (ImageView) view.findViewById(R.id.img_add);
            img_add.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ll_download.callOnClick();
                }
            });

            RelativeLayout rl_my_cert = view.findViewById(R.id.rl_my_cert);
            rl_my_cert.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, CertManageActivity.class);
                    startActivity(intent);
                }
            });
            RelativeLayout rl_org_cert = view.findViewById(R.id.rl_org_cert);
            rl_org_cert.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (AccountHelper.hasLogin(getActivity())) {

                        if (mData != null && mData.size() > 0) {
                            startActivity(new Intent(activity, CertManageActivity.class));
                        } else {

                            startActivity(new Intent(activity, CertDownloadActivity.class));
                        }

                    } else {

                        if (AccountHelper.isFirstLogin(getActivity())) {
                            Intent intentLoignV33 = new Intent(getActivity(), LoginActivityV33.class);
                            startActivity(intentLoignV33);
                        } else {
                            Intent intentLoignV33 = new Intent(getActivity(), ReLoginActivityV33.class);
                            startActivity(intentLoignV33);
                        }

                    }

                }
            });

            Button btn_apply_org_cert = view.findViewById(R.id.btn_apply_org_cert);
            btn_apply_org_cert.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {


                    applyCertLite();


                }
            });


//
//
//            jse = new javasafeengine();
            certDao = new CertDao(context);
            accountDao = new AccountDao(context);
//            logDao = new LogDao(context);
//
//            //view = inflater.inflate(R.layout.context_cert1, container, false);
//
//
//            ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
//            ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
//            TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
//            ImageView tv_right = (ImageView) activity.findViewById(R.id.tv_right);
//            tv_right.setImageResource(R.drawable.switch_org);
//            tv_right.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (certType == false) {
//                        certType = true;
//                        tv_right.setImageResource(R.drawable.switch_person);
//
//                    } else {
//                        certType = false;
//                        tv_right.setImageResource(R.drawable.switch_org);
//                    }
//                    if (relativeLayout != null) {
//                        relativeLayout.removeAllViews();
//                    }
//                    if (views != null) {
//                        views.clear();
//                    }
//                    count = 1;
//                    showCertList();
////                    Log.e("尺寸", mData.size() + " " + adapter.getCount() + "");
//
//                }
//            });
//
//
//            tv_title.setText("证书");
//            Typeface typeFace = Typeface.createFromAsset(activity.getAssets(), "fonts/font.ttf");
//            tv_title.setTypeface(typeFace);
//
//            iv_unitrust.setVisibility(ImageButton.GONE);
//            ib_account.setVisibility(ImageView.GONE);
//            tv_right.setVisibility(View.GONE);
//            tv_title.setVisibility(TextView.VISIBLE);
//
//            CommUtil.setTitleColor(getActivity(), R.color.bg_yellow,
//                    R.color.black);
//
//            if (accountDao.count() == 0) {
//                clearCertList();
////                Intent intent = new Intent(context, LoginActivity.class);
////                startActivity(intent);
//                if (!AccountHelper.hasLogin(getActivity())) {
//                    if (AccountHelper.isFirstLogin(getActivity())) {
//                        Intent intentLoignV33 = new Intent(getActivity(), LoginActivityV33.class);
//                        startActivity(intentLoignV33);
//                    } else {
//                        Intent intentLoignV33 = new Intent(getActivity(), ReLoginActivityV33.class);
//                        startActivity(intentLoignV33);
//                    }
//                }
//
//
//                //activity.finish();
//                return view;
//            }
//
//            sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
//            gEsDev = JShcaEsStd.getIntence(context);
////			   if(null == ScanBlueToothSimActivity.gKsSdk)
////			      ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(activity.getApplication(), context);
//            ht = new HandlerThread("es_device_working_thread");
//            ht.start();
//            workHandler = new Handler(ht.getLooper());
//
//            showCertList();

            initCert();

        } catch (Exception ex) {
            String strr = ex.getMessage();
            strr += "";
        }

        return view;
    }


    private void applyCertLite() {
        String compName = "上海小荧星教育培训有限公司";
        String paperNo = "91310000053001912A";
        String picData = "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";
        CertController certController = new CertController();
        certController.applyCertLite(getActivity(),
                compName,
                paperNo, CommonConst.CERT_TYPE_RSA_QY,
                "11111111", 12, picData);


    }


    @Override
    public void onDestroy() {

        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

//        if (activity != null) {
//            ImageView tv_right = (ImageView) activity.findViewById(R.id.tv_right);
//
////        if (isVisibleToUser) {
//            tv_right.setVisibility(View.VISIBLE);
//            if (certType == false) {
//                tv_right.setImageResource(R.drawable.switch_org);
//            } else {
//                tv_right.setImageResource(R.drawable.switch_person);
//            }
//
////        } else {
////            tv_right.setVisibility(View.GONE);
////        }
//        }
/*
		if(accountDao.count() == 0){
			clearCertList();
		    Toast.makeText(context, "CertFragment onResume", Toast.LENGTH_SHORT).show();

		    Intent intent = new Intent(context, MainActivity.class);
			startActivity(intent);
			return;
		}else{
			showCertList();
		}
	*/


    }

    private List<Map<String, String>> getData() throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<Cert> certList = new ArrayList<Cert>();


        String strActName = accountDao.getLoginAccount().getName();

        certList = certDao.getAllCerts(strActName);

        for (Cert cert : certList) {
            if (cert.getEnvsn().indexOf("-e") != -1)
                continue;
            if (CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
                continue;

            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

//            if (certType == false && getCertType(cert) == true) {//true表示单位证书 false表示个人证书
//                continue;
//            }
//            if (certType == true && getCertType(cert) == false) {//true表示单位证书 false表示个人证书
//
//                continue;
//            }

            if (cert.getEnvsn().indexOf("-e") != -1) {//过滤加密证书
                continue;
            }
            if (getCertType(cert) == false && !PKIUtil.isAccountCert(cert.getCertificate(), AccountHelper.getIDNumber(getActivity())))
                continue;
//            if (getCertType(cert) == true&&! PKIUtil.isOrgCert(cert.getCertificate(), AccountHelper.getIDNumber(getActivity())))
//                continue;


            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {
                Map<String, String> map = new HashMap<String, String>();

                list.add(map);
            }
        }

        return list;
    }


    public void installCert(final int certId) {
        try {
            Cert cert = certDao.getCertByID(certId);
            String sKeyStore = cert.getKeystore();
            byte[] bKeyStore = Base64.decode(sKeyStore);

            Intent installIntent = KeyChain.createInstallIntent();
            installIntent.putExtra(KeyChain.EXTRA_PKCS12, bKeyStore);
            installIntent.putExtra(KeyChain.EXTRA_NAME, DEFAULT_ALIAS);
            startActivityForResult(installIntent, INSTALL_KEYCHAIN_CODE);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    /*
    private  void  showLeftCert(){
        if(cunIndex == 0)
            cunIndex = mData.size()-1;
        else
            cunIndex -= 1;

        certID = Integer.valueOf(mData.get(cunIndex).get("id"));
        ((TextView)view.findViewById(R.id.certname)).setText("持有者: "+mData.get(cunIndex).get("commonname"));
        ((TextView)view.findViewById(R.id.certtype)).setText("证书算法: "+mData.get(cunIndex).get("certtype"));
        ((TextView)view.findViewById(R.id.savetype)).setText("存储介质: "+mData.get(cunIndex).get("savetype"));
        ((TextView)view.findViewById(R.id.certbefore)).setText(mData.get(cunIndex).get("notbeforetime"));
        ((TextView)view.findViewById(R.id.certafter)).setText(mData.get(cunIndex).get("validtime"));

        ((TextView)view.findViewById(R.id.indexpage)).setText((cunIndex+1)+"/"+mData.size());
    }

    private  void  showRightCert(){
        if(cunIndex == mData.size()-1)
            cunIndex = 0;
        else
            cunIndex += 1;

        certID = Integer.valueOf(mData.get(cunIndex).get("id"));
        ((TextView)view.findViewById(R.id.certname)).setText("持有者: "+mData.get(cunIndex).get("commonname"));
        ((TextView)view.findViewById(R.id.certtype)).setText("证书算法: "+mData.get(cunIndex).get("certtype"));
        ((TextView)view.findViewById(R.id.savetype)).setText("存储介质: "+mData.get(cunIndex).get("savetype"));
        ((TextView)view.findViewById(R.id.certbefore)).setText(mData.get(cunIndex).get("notbeforetime"));
        ((TextView)view.findViewById(R.id.certafter)).setText(mData.get(cunIndex).get("validtime"));

        ((TextView)view.findViewById(R.id.indexpage)).setText((cunIndex+1)+"/"+mData.size());

    }
    */


//    private void applyByFace() {
//        if (!checkShcaCciStdServiceState(accountDao.getLoginAccount().getCertType())) {
//            Toast.makeText(activity, "密码分割组件初始化失败,请退出重启应用", Toast.LENGTH_SHORT).show();
//
//            Account act = accountDao.getLoginAccount();
//            act.setCertType(CommonConst.SAVE_CERT_TYPE_RSA);
//            accountDao.update(act);
//            return;
//        }
//
//        Intent intent = null;
////		if(accountDao.getLoginAccount().getStatus() == 2 || accountDao.getLoginAccount().getStatus() == 3 || accountDao.getLoginAccount().getStatus() == 4){  //账户已实名认证
//////			if("".equals(sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, ""))){
////////			  intent = new Intent(activity, com.junyufr.szt.activity.AuthMainActivity.class);
////////			 // intent = new Intent(activity, com.sheca.umandroid.PayActivity.class);
////////			  intent.putExtra("loginAccount", accountDao.getLoginAccount().getIdentityName());
////////			  intent.putExtra("loginId", accountDao.getLoginAccount().getIdentityCode());
////////			}else{
////////			       ResultActivity.strSignature = sharedPrefs.getString(CommonConst.SETTINGS_FACE_AUTH_SIGN, "");
////////
////////			       intent = new Intent(context, com.junyufr.szt.activity.AuthMainActivity.class);
////////				   //intent = new Intent(context, com.sheca.umandroid.PayActivity.class);
////////				   intent.putExtra("loginAccount", accountDao.getLoginAccount().getIdentityName());
////////				   intent.putExtra("loginId", accountDao.getLoginAccount().getIdentityCode());
////////			}
////			intent = new Intent();
////			intent.setClass(activity, com.sheca.umandroid.PayActivity.class);
////			intent.putExtra("loginAccount", AccountHelper.getRealName(activity));
////			intent.putExtra("loginId", AccountHelper.getIdcardno(activity));
////			activity.startActivity(intent);
////
////			//controller.faceAuth(getActivity(),true);
////		}else{
//        intent = new Intent(activity, AuthChoiceActivity.class);
//        intent.putExtra("needPay", "true");
//        startActivity(intent);
////		}
//
//    }














    private boolean checkCertPwd(final Cert cert, final String certPwd) {
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
            return true;
        } else {
            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()) || mCert.getCerttype().contains("SM2")) {
                if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                    initShcaCciStdService();

                if (null == ShcaCciStd.gSdk) {
                    Toast.makeText(context, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    return false;
                }

                try {
                    int ret = ShcaCciStd.gSdk.verifyUserPin(cert.getContainerid(), certPwd);
                    if (ret != 0) {
                        Toast.makeText(context, "证书密码错误", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "密码分割组件初始化失败", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                final String sKeyStore = cert.getKeystore();
                byte[] bKeyStore = Base64.decode(sKeyStore);
                ByteArrayInputStream kis = new ByteArrayInputStream(
                        bKeyStore);
                KeyStore oStore = null;
                try {
                    oStore = KeyStore.getInstance("PKCS12");
                    oStore.load(kis, certPwd.toCharArray());

                } catch (Exception e) {
                    Toast.makeText(context, "证书密码错误", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            return true;
        }

    }


    private String renewCert(final Handler handler, String certSN, String p10) throws Exception {
        handler.post(new Runnable() {
            @Override
            public void run() {
                changeProgDlgCert("提交更新中...");
            }
        });

        String timeout = activity.getString(R.string.WebService_Timeout);
        String urlPath = activity.getString(R.string.UMSP_Service_RenewCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("certSN", certSN);
        postParams.put("p10", p10);
        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
        return responseStr;
    }














    private int initShcaCciStdService() {  //初始化创元中间件
        int retcode = -1;

        if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0) {
            ShcaCciStd.gSdk = ShcaCciStd.getInstance(context);
            retcode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);
            ShcaCciStd.errorCode = retcode;

            if (retcode != 0)
                ShcaCciStd.gSdk = null;
        }

        return retcode;
    }

    private boolean getCertType(Cert cert) {  //true 单位证书 false个人证书
//        Log.e("类型", cert.getCerttype());
        return !cert.getCerttype().contains("个人");

    }


    private String getCertName(Cert cert) {
        String certificate = cert.getCertificate();
        byte[] bCert = Base64.decode(certificate);
        String strBlank = "证书";
        String strCertName = "";

        try {
            strCertName = jse.getCertDetail(17, bCert);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strCertName = "";
        }

        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()) || cert.getCerttype().contains("SM2"))
            strCertName += CommonConst.CERT_SM2_NAME + strBlank;
        else
            strCertName += CommonConst.CERT_RSA_NAME + strBlank;

        if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype()) {
            //strCertName += CommonConst.SAVE_CERT_TYPE_PHONE_NAME;
        } else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()) {
            //strCertName += CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME;
        }

        if (null == cert.getCertname())
            return strCertName;

        if (cert.getCertname().isEmpty())
            return strCertName;

        return cert.getCertname();
    }

    private String getCertSN(Cert cert) {
        return cert.getCertsn().toLowerCase();
    }

    private boolean checkShcaCciStdServiceState(int actCertType) {
        if (CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
            return true;
	    /*
	    	try{	
	    		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
	    			 ShcaCciStd.gSdk = ShcaCciStd.getInstance(activity);
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


    private void showProgDlgCert(String strMsg) {
        progDialogCert = new ProgressDialog(context);
        progDialogCert.setMessage(strMsg);
        progDialogCert.setCancelable(false);
        progDialogCert.show();
    }

    private void changeProgDlgCert(String strMsg) {
        if (progDialogCert.isShowing()) {
            progDialogCert.setMessage(strMsg);
        }
    }


    private void closeProgDlgCert() {
        if (null != progDialogCert && progDialogCert.isShowing()) {
            progDialogCert.dismiss();
            progDialogCert = null;
        }
    }


    private void initCert() {
        if (AccountHelper.hasLogin(getContext())) {

            try {
                if (mData != null) {
                    mData.clear();
                }
                mData = getData();
                txt_cert_num.setText(mData.size()+"");
            } catch (Exception e) {
                txt_cert_num.setText("0");
            }


        } else {
            txt_cert_num.setText("0");
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent event) {

        initCert();
    }


}
