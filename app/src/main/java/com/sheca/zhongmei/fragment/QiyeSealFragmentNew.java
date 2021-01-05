package com.sheca.zhongmei.fragment;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.SealDownloadActivity;
import com.sheca.zhongmei.SealManageActivity;
import com.sheca.zhongmei.account.LoginActivityV33;
import com.sheca.zhongmei.account.ReLoginActivityV33;
import com.sheca.zhongmei.adapter.ViewCertPagerAdapter;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.LogDao;
import com.sheca.zhongmei.dao.SealInfoDao;
import com.sheca.zhongmei.event.RefreshEvent;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.model.SealInfo;
import com.sheca.zhongmei.presenter.SealPresenter;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.umplus.dao.UniTrust;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.spongycastle.util.encoders.Base64;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QiyeSealFragmentNew extends Fragment {

    private List<Map<String, String>> mData = null;

    private javasafeengine jse = null;

    private CertDao certDao = null;

    private AccountDao accountDao = null;

    private LogDao logDao = null;

    private SealInfoDao mSealInfoDao = null;

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

    private SealPresenter sealPresenter;
    TextView txt_seal_num;

    /*
     private GestureDetector mGestureDetector;

     private OnTouchListener myTouch = new OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

    };*/
    boolean certType = false;//false个人 true单位

    TextView txt_seal_todownload;

    UniTrust uniTrust;

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();

        try {
            view = inflater.inflate(R.layout.fragment_seal_new, container, false);
            context = view.getContext();

            EventBus.getDefault().register(this);

            txt_seal_num = view.findViewById(R.id.txt_seal_num);
            txt_seal_todownload = view.findViewById(R.id.txt_seal_todownload);


            LinearLayout ll_download = (LinearLayout) view.findViewById(R.id.ll_download);
            ll_download.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AccountHelper.hasLogin(getActivity())) {


                        if (certNum() > 0) {
                            startActivity(new Intent(activity, SealDownloadActivity.class));
                        } else {
                            Toast.makeText(getActivity(), "请先下载证书", Toast.LENGTH_SHORT).show();
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

            ImageView img_add = (ImageView) view.findViewById(R.id.img_add);
            img_add.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ll_download.callOnClick();
                }
            });


            RelativeLayout rl_my_seal = view.findViewById(R.id.rl_my_seal);
            rl_my_seal.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (AccountHelper.hasLogin(getContext())) {
                        Intent intent = new Intent(activity, SealManageActivity.class);
                        startActivity(intent);

                    } else {
                    }

                }
            });
            RelativeLayout rl_org_seal = view.findViewById(R.id.rl_org_seal);
            rl_org_seal.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (AccountHelper.hasLogin(getActivity())) {

                        if (mData != null && mData.size() > 0) {


                            Intent intent = new Intent(activity, SealManageActivity.class);
                            startActivity(intent);

                        } else {


                            if (certNum() > 0) {
                                startActivity(new Intent(activity, SealDownloadActivity.class));
                            } else {
                                Toast.makeText(getActivity(), "请先下载证书", Toast.LENGTH_SHORT).show();
                            }


//                            startActivity(new Intent(activity, SealDownloadActivity.class));
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

//            uniTrust = new UniTrust(activity, false);
//
//            jse = new javasafeengine();
            certDao = new CertDao(context);
            accountDao = new AccountDao(context);
//            logDao = new LogDao(context);
            mSealInfoDao = new SealInfoDao(context);
//
//            //view = inflater.inflate(R.layout.context_cert1, container, false);
//
//
//            ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
//            ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
//            TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
//            tv_title.setText("印章");
//            Typeface typeFace = Typeface.createFromAsset(activity.getAssets(), "fonts/font.ttf");
//            tv_title.setTypeface(typeFace);
//
//            ImageView tv_right = (ImageView) activity.findViewById(R.id.tv_right);
//            tv_right.setImageResource(R.drawable.switch_org);
//            tv_right.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (certType == false) {
//                        certType = true;
//                        tv_right.setImageResource(R.drawable.switch_person);
//                    } else {
//                        certType = false;
//                        tv_right.setImageResource(R.drawable.switch_org);
//                        view.findViewById(R.id.button_download_seal).setVisibility(RelativeLayout.GONE);
//                    }
//
//                    if (relativeLayout != null) {
//                        relativeLayout.removeAllViews();
//                    }
//                    if (views != null) {
//                        views.clear();
//                    }
//                    successTime = 0;
//                    positionNow = 0;
//                    count = 1;
//                    showSealList();
//                }
//            });
//
//            iv_unitrust.setVisibility(ImageButton.GONE);
//            ib_account.setVisibility(ImageView.GONE);
//            tv_title.setVisibility(TextView.VISIBLE);
//
//            CommUtil.setTitleColor(getActivity(), R.color.bg_yellow,
//                    R.color.black);
//
//            if (accountDao.count() == 0) {
//                clearSealList();
//                Intent intent = new Intent(context, LoginActivityV33.class);
//                startActivity(intent);
//                //activity.finish();
//                return view;
//            }
//
//            if (!AccountHelper.hasLogin(getContext())) {
//                Intent intent = new Intent(context, LoginActivityV33.class);
//                startActivity(intent);
//            }
//
//
//            sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
//            gEsDev = JShcaEsStd.getIntence(context);
//            //if(null == ScanBlueToothSimActivity.gKsSdk)
//            //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(activity.getApplication(), context);
//            ht = new HandlerThread("es_device_working_thread");
//            ht.start();
//            workHandler = new Handler(ht.getLooper());
//            sealPresenter = new SealPresenter(getContext(), accountDao, certDao, workHandler, getActivity());
//
//            showSealList();
        } catch (Exception ex) {
            String strr = ex.getMessage();
            strr += "";
        }

//        Button button_download_seal = (Button) view.findViewById(R.id.button_download_seal);
//        button_download_seal.setOnClickListener(new OnClickListener() {//下载印章
//            @Override
//            public void onClick(View v) {
//
//
//                SealInfo sealInfo = mSealInfoDao.getSealByID(certID);
//                sealInfo.setDownloadstatus(1);
//                mSealInfoDao.updateSealInfo(sealInfo, AccountHelper.getUsername(getActivity()));
//
//                if (relativeLayout != null) {
//                    relativeLayout.removeAllViews();
//                }
//                if (views != null) {
//                    views.clear();
//                }
//                count = 1;
//                showSealList();
//
////                if (!AccountHelper.hasLogin(getContext())) {
////                    Intent intent = new Intent(context, LoginActivity.class);
////                    startActivity(intent);
////                }
//            }
//        });
        initCert();
        initSeal();

        return view;
    }

    @Override
    public void onDestroyView() {
//        view.findViewById(R.id.button_apply_seal).setVisibility(ImageButton.GONE);
//        view.findViewById(R.id.button_download_seal).setVisibility(ImageButton.GONE);
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }


    private int certNum() {
        int certNum = 0;
        String strActName = accountDao.getLoginAccount().getName();
        List<Cert> certList = certDao.getAllCerts(strActName);
        if (certList != null && certList.size() > 0) {

            for (int i = 0; i < certList.size(); i++) {
                Cert cert = certList.get(i);
                if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {
                    certNum++;
                }

            }
        } else {


        }

        return certNum;
    }

    private List<Map<String, String>> getData() throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<SealInfo> sealList = new ArrayList<SealInfo>();
        List<String> certSn = new ArrayList<String>();


        String strActName = accountDao.getLoginAccount().getName();

        sealList = mSealInfoDao.getAllSealInfos(strActName);


        for (SealInfo sealInfo : sealList) {
            Cert cert = certDao.getCertByCertsn(sealInfo.getCertsn(), strActName);
            if (null == cert)
                continue;
            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {
                Map<String, String> map = new HashMap<String, String>();


                list.add(map);
            }


        }

        return list;
    }

    private boolean getCertType(Cert cert) {  //true 单位证书 false个人证书
        Log.e("类型", cert.getCerttype());
        return !cert.getCerttype().contains("个人");

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


    private void initSeal() {

        if (AccountHelper.hasLogin(getContext())) {

            try {
                if (mData != null) {
                    mData.clear();
                }
                mData = getData();
                txt_seal_num.setText(mData.size() + "");
            } catch (Exception e) {
                txt_seal_num.setText("0");
            }


        } else {
            txt_seal_num.setText("0");
        }
    }

    private void initCert() {

        if (AccountHelper.hasLogin(getContext())) {
            txt_seal_todownload.setText(certNum() + "");
        } else {
            txt_seal_todownload.setText("0");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent event) {
        initSeal();
        initCert();
    }


}
