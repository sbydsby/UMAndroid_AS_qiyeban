package com.sheca.umandroid.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.security.KeyChain;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.excelsecu.slotapi.EsIBankDevice;
import com.junyufr.szt.util.Base64ImgUtil;
import com.sheca.JShcaCciStd.shcaCciStdGenKeyPairRes;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.LoginActivity;
import com.sheca.umandroid.MainActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.ScanBlueToothSimActivity;
import com.sheca.umandroid.adapter.Entity;
import com.sheca.umandroid.adapter.ViewCertPagerAdapter;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.DownloadCertResponse;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.model.ShcaCciStd;
import com.sheca.umandroid.presenter.SealPresenter;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.WebClientUtil;

import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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
import java.util.TimeZone;

import javax.security.auth.x500.X500Principal;

public class SealFragmentNew extends Fragment {

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

    private JShcaEsStd gEsDev = null;
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
    
	/* 
	 private GestureDetector mGestureDetector;

	 private OnTouchListener myTouch = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return mGestureDetector.onTouchEvent(event);
		}
    	
    };*/

    @SuppressLint("ResourceAsColor")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();

        try {
            view = inflater.inflate(R.layout.fragment_seal, container, false);
            context = view.getContext();

            jse = new javasafeengine();
            certDao = new CertDao(context);
            accountDao = new AccountDao(context);
            logDao = new LogDao(context);
            mSealInfoDao = new SealInfoDao(context);

            //view = inflater.inflate(R.layout.context_cert1, container, false);


            ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
            ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
            TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
            tv_title.setText("印章");
            Typeface typeFace = Typeface.createFromAsset(activity.getAssets(), "fonts/font.ttf");
            tv_title.setTypeface(typeFace);

            iv_unitrust.setVisibility(ImageButton.GONE);
            ib_account.setVisibility(ImageView.GONE);
            tv_title.setVisibility(TextView.VISIBLE);

            CommUtil.setTitleColor(getActivity(),R.color.bg_yellow,
                    R.color.black);

            if (accountDao.count() == 0) {
                clearSealList();
                Intent intent = new Intent(context, LoginActivity.class);
               startActivity(intent);
               //activity.finish();
               return view;
            }

            if (!AccountHelper.hasLogin(getContext())) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }


            sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
            gEsDev = JShcaEsStd.getIntence(context);
            //if(null == ScanBlueToothSimActivity.gKsSdk)
            //ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(activity.getApplication(), context);
            ht = new HandlerThread("es_device_working_thread");
            ht.start();
            workHandler = new Handler(ht.getLooper());
            sealPresenter = new SealPresenter(getContext(), accountDao, certDao, workHandler,getActivity());

            showSealList();
        } catch (Exception ex) {
            String strr = ex.getMessage();
            strr += "";
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        view.findViewById(R.id.button_apply_seal).setVisibility(ImageButton.GONE);

        super.onDestroyView();
    }

    private List<Map<String, String>> getData() throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<SealInfo> sealList = new ArrayList<SealInfo>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);

        String strActName = accountDao.getLoginAccount().getName();
        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        sealList = mSealInfoDao.getAllSealInfos(strActName);
        for (SealInfo sealInfo : sealList) {
            Cert cert = certDao.getCertByCertsn(sealInfo.getCertsn(), strActName);
            if (null == cert)
                continue;
            if (cert.getStatus() != Cert.STATUS_DOWNLOAD_CERT)
                continue;

            if (CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
                continue;

            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", String.valueOf(sealInfo.getId()));

                String strNotBeforeTime = sealInfo.getNotbefore();
                String strValidTime = sealInfo.getNotafter();
                Date fromDate = sdf.parse(strNotBeforeTime);
                Date toDate = sdf.parse(strValidTime);

                map.put("commonname", sealInfo.getSealname());
                map.put("notbeforetime", sdf2.format(fromDate));
                map.put("validtime", sdf2.format(toDate));
                map.put("status", cert.getStatus() + "");
                map.put("validState", "" + getCertValidState(cert.getCertificate()));
                map.put("picdata", sealInfo.getPicdata());
                map.put("sealsn", sealInfo.getSealsn());
                
                if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()) )
				    map.put("certtype", CommonConst.CERT_SM2_NAME);	
				else
					map.put("certtype", CommonConst.CERT_RSA_NAME);	

                if(isCertTested(cert.getCertificate()))
					map.put("isTested","true");
				else
					map.put("isTested","false");
                
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


    private void showSealList() {
    	if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
           view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.GONE);
    	else
    	   view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.VISIBLE);

        try {
            //清空之前的list
            if(mData!=null){
                mData.clear();
            }
            mData = getData();

        } catch (Exception e) {
            clearSealList();
            Log.e(CommonConst.TAG, e.getMessage(), e);
            Toast.makeText(context, "获取证书错误！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mData.size() == 0) {
            cunIndex = -1;
            view.findViewById(R.id.Layout_cert_info).setVisibility(RelativeLayout.GONE);
            view.findViewById(R.id.Layout_no_cert_info).setVisibility(RelativeLayout.VISIBLE);
            //view.findViewById(R.id.mask).setVisibility(View.GONE);
            view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.VISIBLE);

            (view.findViewById(R.id.Layout_no_cert_info)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mData.size() == 0) {
                        Toast.makeText(context, "无印章", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            view.findViewById(R.id.Layout_cert_info).setVisibility(RelativeLayout.VISIBLE);
            view.findViewById(R.id.Layout_no_cert_info).setVisibility(RelativeLayout.GONE);
            //view.findViewById(R.id.mask).setVisibility(View.VISIBLE);
            cunIndex = 0;
            //certID = Integer.valueOf(mData.get(cunIndex).get("id"));

            view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.VISIBLE);

            initView();

            if (mData.size() == 1) {
                //view.findViewById(R.id.indexpage).setVisibility(RelativeLayout.GONE);
                //view.findViewById(R.id.select_left).setVisibility(RelativeLayout.GONE);
                // view.findViewById(R.id.select_right).setVisibility(RelativeLayout.GONE);

                view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.VISIBLE);

                //((TextView)view.findViewById(R.id.indexpage)).setText("1/1");
            } else {
                //view.findViewById(R.id.indexpage).setVisibility(RelativeLayout.VISIBLE);
                //view.findViewById(R.id.select_left).setVisibility(RelativeLayout.VISIBLE);
                //view.findViewById(R.id.select_right).setVisibility(RelativeLayout.VISIBLE);

                view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.VISIBLE);
            }
			
			/*((TextView)view.findViewById(R.id.certname)).setText("持有者: "+mData.get(cunIndex).get("commonname"));
			((TextView)view.findViewById(R.id.certtype)).setText("证书算法: "+mData.get(cunIndex).get("certtype"));
			((TextView)view.findViewById(R.id.savetype)).setText("存储介质: "+mData.get(cunIndex).get("savetype"));
			((TextView)view.findViewById(R.id.certbefore)).setText(mData.get(cunIndex).get("notbeforetime"));
			((TextView)view.findViewById(R.id.certafter)).setText(mData.get(cunIndex).get("validtime")); */

            //((TextView)view.findViewById(R.id.indexpage)).setText((cunIndex+1)+"/"+mData.size());
			/*
			(view.findViewById(R.id.Layout_cert_info)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(mData.size() == 0){
						Toast.makeText(context, "无证书", Toast.LENGTH_SHORT).show();
					}else{
					  Intent intent = new Intent(activity, CertDetailActivity.class);
				      intent.putExtra("CertId", certID+"");
				      startActivity(intent);
					}
				}
			}); */
        }

        Button button_apply_seal = (Button) view.findViewById(R.id.button_apply_seal);
        button_apply_seal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sealPresenter.applySeal(mData);
            }
        });
		
		/*
		view.findViewById(R.id.select_left).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view1) {
				showLeftCert();
			}
		});
		
		view.findViewById(R.id.select_right).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view1) {
				showRightCert();
			}
		});	*/
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
    private void clearSealList() {
        view.findViewById(R.id.button_apply_seal).setVisibility(RelativeLayout.GONE);

        view.findViewById(R.id.Layout_cert_info).setVisibility(RelativeLayout.GONE);
        view.findViewById(R.id.Layout_no_cert_info).setVisibility(RelativeLayout.GONE);

        cunIndex = -1;

        Button button_apply_seal = (Button) view.findViewById(R.id.button_apply_seal);
        button_apply_seal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AccountHelper.hasLogin(getContext())) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        view.findViewById(R.id.Layout_my_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AccountHelper.hasLogin(getContext())) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    private void initView() {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager_room);// 找到ViewPager
        viewPager.setPageMargin(30);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setOnPageChangeListener(pageChangeListener);// 设置页面滑动监听
        Entity entity = new Entity();
        pageNum = entity.getPageNum(mData.size(), ITEM_COUNT);//view 的页数
        //根据页数动态生成指示器
        relativeLayout = (LinearLayout) view.findViewById(R.id.ll_dots);
        for (int i = 0; i < pageNum; i++) {
            if (pageNum > 1) {
                ImageView img = new ImageView(context);
                img.setPadding(0, 0, 10, 0);
                if (i == 0) {
                    img.setImageResource(R.drawable.dot_selected);
                } else {
                    img.setImageResource(R.drawable.dot_normal);
                }
                img.setId(i);   //注意这点 设置id
                relativeLayout.addView(img);
            }
        }
        //动态生成viewpager的多个view
        for (int i = 0; i < pageNum; i++) {
            initListViews(count++);
        }

        hideBackground(pageNum);
        instantiated(mData.size(), ITEM_COUNT);//实例化ViewPager
        adapter = new ViewCertPagerAdapter(views);// 构造adapter
        viewPager.setAdapter(adapter);// 设置适配器
    }
    
    @SuppressLint("ResourceAsColor")
	private void hideBackground(int pageNum){
		if (pageNum>0){
			view.findViewById(R.id.Layout_my_info).setBackgroundColor(android.R.color.transparent);
			view.findViewById(R.id.Layout_no_cert_info).setBackgroundColor(android.R.color.transparent);
		}
	}

    //实例化pagerview中ImageView、TextView 方法,输入数组长度、每页item数
    @SuppressLint("NewApi")
    private void instantiated(int listSize, int numberOfEveryPage) {
        for (int i = 0; i < pageNum; i++) {
            for (int j = 0; j < numberOfEveryPage; j++) {
                TextView textview = (TextView) views.get(i).findViewById(R.id.sealname);
                textview.setText("印章别名:" + mData.get(i).get("commonname"));

                textview = (TextView) views.get(i).findViewById(R.id.certbefore);
                textview.setText(mData.get(i).get("notbeforetime"));

                textview = (TextView) views.get(i).findViewById(R.id.certafter);
                textview.setText(mData.get(i).get("validtime"));

                Bitmap bitMap = bitMapScale(stringtoBitmap(mData.get(i).get("picdata")), 0.3f);
                views.get(i).findViewById(R.id.list_image).setVisibility(RelativeLayout.VISIBLE);
                ((ImageView) views.get(i).findViewById(R.id.list_image)).setImageBitmap(bitMap);
                ((ImageView) views.get(i).findViewById(R.id.list_image)).invalidate();
                
                if("true".equals(mData.get(0).get("isTested"))){
                	views.get(i).findViewById(R.id.Layout_seal_item).setBackgroundResource(R.drawable.certcardview_test);
                }else{
					views.get(i).findViewById(R.id.Layout_seal_item).setBackgroundResource(R.drawable.certcardview_normal);
				}
            }

            certID = Integer.valueOf(mData.get(0).get("id"));
            views.get(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mData.size() == 0) {
                        Toast.makeText(context, "无印章", Toast.LENGTH_SHORT).show();
                    } else {
                        // Intent intent = new Intent(activity, CertDetailActivity.class);
                        // intent.putExtra("CertId", certID+"");
                        //startActivity(intent);
                    }
                }
            });
        }

    }

    /*
     * listViews添加view对象
     *
     */
    private void initListViews(int count) {
        if (views == null) {
            views = new ArrayList<View>();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        views.add(inflater.inflate(R.layout.view_seal_item_v3, null));
    }

    /*
     * 页面监听事件
     */
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        private int oldPosition = 0;

        @SuppressLint("NewApi")
        public void onPageSelected(int position) {// 页面选择响应函数
            positionNow = position;
            if (pageNum > 1) {
                ImageView img = (ImageView) view.findViewById(getResources().getIdentifier("" + position, "id", "com.tony.viewpager"));
                img.setImageResource(R.drawable.dot_selected);

                ImageView imgold = (ImageView) view.findViewById(getResources().getIdentifier("" + oldPosition, "id", "com.tony.viewpager"));
                imgold.setImageResource(R.drawable.dot_normal);

                oldPosition = position;
            }

            certID = Integer.valueOf(mData.get(positionNow).get("id"));
            
            if("true".equals(mData.get(positionNow).get("isTested"))){
            	views.get(positionNow).findViewById(R.id.Layout_seal_item).setBackgroundResource(R.drawable.certcardview_test);
            }else{
            	views.get(positionNow).findViewById(R.id.Layout_seal_item).setBackgroundResource(R.drawable.certcardview_normal);
            }
            
            
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {// 滑动中。。。

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变

        }
    };

    private void showRenewCertPwd(final Handler handler, final int certID) {
        final Cert mCert = certDao.getCertByID(certID);

        Builder builder = new Builder(context);
        builder.setIcon(R.drawable.alert);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
            builder.setTitle("请输入蓝牙key密码");
        else
            builder.setTitle("请输入证书密码");
        //builder.setCancelable(false);
        // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(context).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);
        final EditText prikeyPasswordView = (EditText) view.findViewById(R.id.et_prikey_password);
        final EditText prikeyPassword2View = (EditText) view.findViewById(R.id.et_prikey_password2);
        prikeyPassword2View.setVisibility(RelativeLayout.GONE);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
            prikeyPasswordView.setHint("输入蓝牙key密码");
        else
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
                                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
                                    Toast.makeText(context, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, "无效的证书密码", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
                                sealPresenter.showProgDlgCert("正在连接设备...");

                                workHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (sealPresenter.checkBTDevice()) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                }
                                            });
                                        } else {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                    Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            if (checkCertPwd(mCert, prikeyPassword)) {
                                workHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.showProgDlgCert("证书更新中...");
                                                }
                                            });

                                            String responseStr = "";

                                            if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype()))
                                                responseStr = UploadSM2Pkcs10(handler, mCert.getCertsn(), mCert.getCerttype(), mCert.getSavetype(), mCert.getStatus(), prikeyPassword);
                                            else
                                                responseStr = UploadPkcs10(handler, mCert.getCertsn(), mCert.getCerttype(), mCert.getSavetype(), mCert.getStatus(), prikeyPassword);

                                            JSONObject jb = JSONObject.fromObject(responseStr);
                                            String resultStr = jb.getString(CommonConst.RETURN_CODE);
                                            String returnStr = jb.getString(CommonConst.RETURN_MSG);
                                            final String strErr = returnStr;
                                            int certSaveType = CommonConst.SAVE_CERT_TYPE_RSA;

                                            if (resultStr.equals("0")) {
                                                setCertRenewStatus(mCert);

                                                //调用UMSP服务：下载证书
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        sealPresenter.changeProgDlgCert("证书下载中...");
                                                    }
                                                });

                                                JSONObject jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
                                                strENVSN = jbRet.getString(CommonConst.RESULT_PARAM_REQUEST_NUMBER);   //记录ENVSN

                                                //设置时间间隔，等待后台签发证书
                                                String threadSleepTime = activity.getString(R.string.Thread_Sleep);
                                                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())) {
                                                    certSaveType = CommonConst.SAVE_CERT_TYPE_SM2;
                                                    Thread.sleep(Long.parseLong(threadSleepTime) * 2);   //签发sm2证书等待时间需10秒
                                                } else {
                                                    certSaveType = CommonConst.SAVE_CERT_TYPE_RSA;
                                                    Thread.sleep(Long.parseLong(threadSleepTime));
                                                }

                                                //下载证书
                                                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype()))
                                                    responseStr = DownloadSM2Cert(strENVSN, mCert.getSavetype(), mCert.getCerttype());
                                                else
                                                    responseStr = DownloadCert(strENVSN, mCert.getSavetype(), mCert.getCerttype());

                                                jb = JSONObject.fromObject(responseStr);
                                                resultStr = jb.getString(CommonConst.RETURN_CODE);
                                                returnStr = jb.getString(CommonConst.RETURN_MSG);

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        sealPresenter.closeProgDlgCert();
                                                    }
                                                });

                                                if (resultStr.equals("0")) {
                                                    jbRet = JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));

                                                    DownloadCertResponse dcResponse = new DownloadCertResponse();
                                                    dcResponse.setReturn(returnStr);
                                                    dcResponse.setResult(resultStr);
                                                    dcResponse.setUserCert(jbRet.getString(CommonConst.RESULT_PARAM_USER_CERT));
                                                    dcResponse.setEncCert(jbRet.getString(CommonConst.RESULT_PARAM_ENC_CERT));
                                                    dcResponse.setEncKey(jbRet.getString(CommonConst.RESULT_PARAM_ENC_KEYT));
                                                    dcResponse.setCertChain(jbRet.getString(CommonConst.RESULT_PARAM_CERT_CHAIN));
                                                    dcResponse.setEncAlgorithm(jbRet.getString(CommonConst.RESULT_PARAM_ENC_ALG));

                                                    final DownloadCertResponse fDcResponse = dcResponse;
                                                    final int actCertType = certSaveType;
                                                    //UI处理必须放在主线程
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //保存证书到本地
                                                            saveCert(strENVSN, fDcResponse, mCert.getSavetype(), actCertType, mCert, prikeyPassword);
                                                        }
                                                    });
                                                } else {
                                                    throw new Exception("调用UMSP服务之DownloadCert失败：" + resultStr + "，" + returnStr);
                                                }
                                            } else {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        sealPresenter.closeProgDlgCert();
                                                        Toast.makeText(context, strErr, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                        } catch (Exception ex) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                }
                                            });
                                            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                return;
                            }


                        } catch (Exception e) {
                            Log.e(CommonConst.TAG, e.getMessage(), e);
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                });


        builder.show();
    }


    private void showRevokeCertPwd(final Handler handler, final int certID) {
        final Cert mCert = certDao.getCertByID(certID);

        Builder builder = new Builder(context);
        builder.setIcon(R.drawable.alert);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
            builder.setTitle("请输入蓝牙key密码");
        else
            builder.setTitle("请输入证书密码");
        //builder.setCancelable(false);
        // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(context).inflate(R.layout.set_prikey_pwd, null);
        //设置自定义的布局文件作为弹出框的内容
        builder.setView(view);
        final EditText prikeyPasswordView = (EditText) view.findViewById(R.id.et_prikey_password);
        final EditText prikeyPassword2View = (EditText) view.findViewById(R.id.et_prikey_password2);
        prikeyPassword2View.setVisibility(RelativeLayout.GONE);
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
            prikeyPasswordView.setHint("输入蓝牙key密码");
        else
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
                                if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype())
                                    Toast.makeText(context, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, "无效的证书密码", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
                                sealPresenter.showProgDlgCert("正在连接设备...");

                                workHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (sealPresenter.checkBTDevice()) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                }
                                            });
                                        } else {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                    Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            if (checkCertPwd(mCert, prikeyPassword)) {
                                workHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.showProgDlgCert("撤销证书中...");
                                                }
                                            });

                                            revokeCert(certID, prikeyPassword);
                                        } catch (Exception ex) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sealPresenter.closeProgDlgCert();
                                                }
                                            });

                                            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                return;
                            }


                        } catch (Exception e) {
                            Log.e(CommonConst.TAG, e.getMessage(), e);
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                });

        builder.show();
    }


    private boolean checkCertPwd(final Cert cert, final String certPwd) {
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()) {
            return true;
        } else {
            if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())) {
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
                sealPresenter.changeProgDlgCert("提交更新中...");
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


    private void revokeCert(int certID, final String certPwd) throws Exception {
        Cert mCert = certDao.getCertByID(certID);
        int nRet = -1;

        String timeout = activity.getString(R.string.WebService_Timeout);
        String urlPath = activity.getString(R.string.UMSP_Service_RevokeCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("certSN", mCert.getCertsn());
        postParams.put("reason", CommonConst.REVOKE_CERT_REASON);
        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        JSONObject jb = JSONObject.fromObject(responseStr);
        String resultStr = jb.getString(CommonConst.RETURN_CODE);
        String returnStr = jb.getString(CommonConst.RETURN_MSG);
        if (resultStr.equals("0")) {
            certDao.deleteCert(certID);
            saveLog(OperationLog.LOG_TYPE_REVOKECERT, mCert.getCertsn(), "", "", "");

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == mCert.getSavetype()) {
                shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
                if (null == devInfo)
                    gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())) {
                    if (null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
                        nRet = gEsDev.detroySM2SignCert(certPwd);
                    if (null != gEsDev.readSM2EncryptCert() && !"".equals(gEsDev.readSM2EncryptCert()))
                        nRet = gEsDev.detroySM2EncryptCert(certPwd);
                } else {
                    if (null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
                        nRet = gEsDev.detroyRSASignCert(certPwd, CommonConst.CERT_MOUDLE_SIZE);
                }
            } else if (CommonConst.SAVE_CERT_TYPE_SIM == mCert.getSavetype()) {
                if (!ScanBlueToothSimActivity.gKsSdk.isConnected())
                    ScanBlueToothSimActivity.gKsSdk.connect(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), "778899", 500);

                if (CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())) {
                    if (null != ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert()))
                        nRet = ScanBlueToothSimActivity.gKsSdk.detroySM2KeyPairAndCert(certPwd);
                } else {
                    if (null != ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert() && !"".equals(ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert()))
                        nRet = ScanBlueToothSimActivity.gKsSdk.detroyRSAKeyPairAndCert(certPwd);
                }
            }

            sealPresenter.closeProgDlgCert();
            Toast.makeText(context, "证书撤销成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            //activity.finish();
        } else {
            sealPresenter.closeProgDlgCert();
            Toast.makeText(context, "证书撤销失败:" + resultStr + "," + returnStr, Toast.LENGTH_SHORT).show();
        }
    }

    private String UploadPkcs10(final Handler handler, String certSN, String certType, int saveType, int certStatus, final String certPwd) throws Exception {
        String p10 = "";
        String responseStr = "";

        String strActName = accountDao.getLoginAccount().getName();
        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        Cert cert = certDao.getCertByCertsn(certSN, strActName);

        if (Cert.STATUS_RENEW_CERT == certStatus) {
            p10 = "";
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sealPresenter.changeProgDlgCert("生成P10中...");
                }
            });

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                p10 = genPkcs10ByBlueTooth(getCertCN(cert), certPwd);
            } else {
                p10 = genPkcs10(getCertCN(cert));
            }
        }

        responseStr = renewCert(handler, cert.getCertsn(), p10);

        return responseStr;
    }

    private String UploadSM2Pkcs10(final Handler handler, String certSN, String certType, int saveType, int certStatus, final String certPwd) throws Exception {
        String p10 = "";
        String responseStr = "";

        String strActName = accountDao.getLoginAccount().getName();
        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

        Cert cert = certDao.getCertByCertsn(certSN, strActName);

        if (Cert.STATUS_RENEW_CERT == certStatus) {
            p10 = "";
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sealPresenter.changeProgDlgCert("生成P10中...");
                }
            });

            if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
                p10 = genSM2Pkcs10ByBlueTooth(getCertCN(cert), certPwd);
            } else {
                p10 = genSM2Pkcs10(getCertCN(cert));
            }
        }

        responseStr = renewCert(handler, cert.getCertsn(), p10);

        return responseStr;
    }


    private String genPkcs10(String PersonName) throws Exception {
        String p10 = "";

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(CommonConst.CERT_MOUDLE_SIZE);
            mKeyPair = keyGen.genKeyPair();

            String dn = "CN=" + PersonName;
            X500Principal subjectName = new X500Principal(dn);
            org.spongycastle.jce.PKCS10CertificationRequest kpGen = new org.spongycastle.jce.PKCS10CertificationRequest(
                    CommonConst.CERT_ALG_RSA, subjectName, mKeyPair.getPublic(), null, mKeyPair.getPrivate());

            p10 = new String(Base64.encode(kpGen.getEncoded()));

        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if ("".equals(p10))
            throw new Exception("生成P10失败");

        return p10;
    }


    private String genPkcs10ByBlueTooth(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            shcaEsDeviceInfo devInfo = gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
            if (null == devInfo)
                gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (null != gEsDev.readRSASignatureCert() && !"".equals(gEsDev.readRSASignatureCert()))
                nRet = gEsDev.detroyRSASignCert(certPwd, CommonConst.CERT_MOUDLE_SIZE);

            p10 = gEsDev.genRSAPKCS10(dn, certPwd, CommonConst.CERT_MOUDLE_SIZE);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙key生成P10失败");

        return p10;
    }


    private String genSM2Pkcs10(String PersonName) throws Exception {
        String p10 = "";

        try {
            if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                initShcaCciStdService();

            shcaCciStdGenKeyPairRes r = null;

            if (ShcaCciStd.gSdk != null) {
                r = ShcaCciStd.gSdk.genSM2KeyPair(CommonConst.JSHECACCISTD_PWD);
                //Thread.sleep(Long.parseLong(ResultActivity.this.getString(R.string.Thread_Sleep)));

                if (r != null && r.retcode == 0) {
                    //String dn = "CN=" + PersonName+",OU=Test,C=CN,ST=SH,O=Sheca";
                    String dn = "CN=" + PersonName;

                    byte[] bPubkey = android.util.Base64.decode(r.pubkey, android.util.Base64.NO_WRAP);
                    p10 = ShcaCciStd.gSdk.getSM2PKCS10(dn, bPubkey, CommonConst.JSHECACCISTD_PWD, r.containerID);
                    //Thread.sleep(Long.parseLong(ResultActivity.this.getString(R.string.Thread_Sleep)));
                    mContainerid = r.containerID;
                }
            }

        } catch (Exception ex) {
            ShcaCciStd.gSdk = null;
            throw new Exception(ex.getMessage());
        }

        if ("".equals(p10))
            throw new Exception("密码分割组件初始化失败");

        return p10;
    }

    private String genSM2Pkcs10ByBlueTooth(String PersonName, final String certPwd) throws Exception {
        String p10 = "";
        int nRet = -1;

        try {
            String dn = "CN=" + PersonName;
            if (null != gEsDev.readSM2SignatureCert() && !"".equals(gEsDev.readSM2SignatureCert()))
                nRet = gEsDev.detroySM2SignCert(certPwd);
            if (null != gEsDev.readSM2EncryptCert() && !"".equals(gEsDev.readSM2EncryptCert()))
                nRet = gEsDev.detroySM2EncryptCert(certPwd);

            p10 = gEsDev.genSM2PKCS10(dn, certPwd);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        if (null == p10 || "".equals(p10))
            throw new Exception("使用蓝牙key生成P10失败");

        return p10;
    }

    private String DownloadCert(String requestNumber, int saveType, String certType) throws Exception {
        String timeout = activity.getString(R.string.WebService_Timeout);
        String urlPath = activity.getString(R.string.UMSP_Service_DownloadCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        boolean isSavedCert = false;
        Cert cert = new Cert();
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), accountDao.getLoginAccount().getName(), certType);
            if (null == cert) {
                cert = new Cert();
                isSavedCert = false;
            } else {
                isSavedCert = true;
                Cert encCert = certDao.getCertByEnvsn(cert.getEnvsn() + "-e", accountDao.getLoginAccount().getName());
                if (null != encCert)
                    certDao.deleteCert(encCert.getId());
            }
        }

        cert.setEnvsn(requestNumber);
        cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
        cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
        cert.setCerttype(CommonConst.CERT_TYPE_RSA);
        cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
        cert.setSignalg(1);
        cert.setContainerid("");

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
            cert.setPrivatekey("");
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, accountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, accountDao.getLoginAccount().getName());
        } else {
            cert.setPrivatekey(new String(Base64.encode(mKeyPair.getPrivate().getEncoded())));
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
            cert.setDevicesn(android.os.Build.SERIAL);

            certDao.addCert(cert, accountDao.getLoginAccount().getName());
        }

        return responseStr;
    }

    private String DownloadSM2Cert(String requestNumber, int saveType, String certType) throws Exception {
        String timeout = activity.getString(R.string.WebService_Timeout);
        String urlPath = activity.getString(R.string.UMSP_Service_DownloadCert);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));

        boolean isSavedCert = false;
        Cert cert = new Cert();
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""), accountDao.getLoginAccount().getName(), certType);
            if (null == cert) {
                cert = new Cert();
                isSavedCert = false;
            } else {
                isSavedCert = true;
                Cert encCert = certDao.getCertByEnvsn(cert.getEnvsn() + "-e", accountDao.getLoginAccount().getName());
                if (null != encCert)
                    certDao.deleteCert(encCert.getId());
            }
        }

        cert.setEnvsn(requestNumber);
        cert.setPrivatekey("");
        cert.setCerttype(CommonConst.CERT_TYPE_SM2);
        cert.setSignalg(2);
        cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
        cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
        cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setContainerid("");
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
            cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

            if (isSavedCert)
                certDao.updateCert(cert, accountDao.getLoginAccount().getName());
            else
                certDao.addCert(cert, accountDao.getLoginAccount().getName());
        } else {
            cert.setContainerid(mContainerid);
            cert.setSavetype(CommonConst.SAVE_CERT_TYPE_PHONE);
            cert.setDevicesn(android.os.Build.SERIAL);

            certDao.addCert(cert, accountDao.getLoginAccount().getName());
        }

        return responseStr;
    }


    private void saveCert(final String requestNumber, final DownloadCertResponse response, final int saveType, final int certType, final Cert mCert, final String prikeyPassword) {
        try {
            if (CommonConst.SAVE_CERT_TYPE_RSA == certType)
                uploadCertStatus(requestNumber, response, prikeyPassword, saveType, mCert);
            else
                uploadSM2CertStatus(requestNumber, response, prikeyPassword, saveType, mCert);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void uploadCertStatus(final String requestNumber, final DownloadCertResponse response, final String prikeyPassword, final int saveType, final Cert mCert) throws Exception {
        sealPresenter.showProgDlgCert("证书保存中...");

        String userCert = response.getUserCert();
        String certChain = response.getCertChain();
        Cert cert = certDao.getCertByEnvsn(requestNumber, accountDao.getLoginAccount().getName());
        cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
        cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
        byte[] bCert = Base64.decode(userCert);
        javasafeengine jse = new javasafeengine();
        Certificate oCert = jse.getCertFromBuffer(bCert);
        X509Certificate oX509Cert = (X509Certificate) oCert;
        cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));
        cert.setCertchain(certChain);
        cert.setNotbeforetime(getCertNotbeforetime(userCert));
        cert.setValidtime(getCertValidtime(userCert));
        //cert.setPrivatekey("");

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setCertificate(userCert);
            cert.setKeystore("");
            cert.setPrivatekey("");

            int retcode = -1;
            retcode = gEsDev.saveRSASignatureCert(prikeyPassword, userCert);
        } else {
            cert.setCertificate(userCert);
            String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
            cert.setKeystore(p12);
            cert.setPrivatekey("");
        }

        //showMessage("2");
        certDao.updateCert(cert, accountDao.getLoginAccount().getName());
        certDao.deleteCert(mCert.getId());
        //Toast.makeText(ResultActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();

        //showMessage("3");
        saveLog(OperationLog.LOG_TYPE_RENEWCERT, cert.getCertsn(), "", "", "");
        //showMessage("4");
        final Handler handler = new Handler(activity.getMainLooper());
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
                    if (resultStr.equals("0")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sealPresenter.closeProgDlgCert();
                                Toast.makeText(context, "证书更新成功", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                // activity.finish();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sealPresenter.closeProgDlgCert();
                            }
                        });

                        throw new Exception("证书更新失败：" + resultStr + "，" + returnStr);
                    }
                } catch (Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sealPresenter.closeProgDlgCert();
                        }
                    });
                    //Toast.makeText(context, exc.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

    }

    private void uploadSM2CertStatus(final String requestNumber, final DownloadCertResponse response, final String prikeyPassword, final int saveType, final Cert mCert) throws Exception {
        sealPresenter.showProgDlgCert("证书保存中...");

        String userCert = response.getUserCert();
        String certChain = response.getCertChain();
        String encCert = response.getEncCert();
        String encKeystore = response.getEncKey();

        int retcode = -1;

        if (null == encCert)
            encCert = "";
        if (null == encKeystore)
            encKeystore = "";
		/*	
		String userCert = "MIIDwzCCA2agAwIBAgIQd0gGJqSaAkIAFnG4Fb5GyjAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEBVrkqL1OLoNKol29i/Bvw2+rkyq0UelOP/FhEUVBkHuNO8h/YV8AYbHIRmoEBNVYzlHvmPiJ8mp4rEw1Bo1kKOCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBT5aslV8UJupOSIlYFBpq57mYDjBTAOBgNVHQ8BAf8EBAMCBsAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANJADBGAiEA81NRuSndplECK2+MPAh6IWYzQqwwWuNw9/YueSMlGfcCIQDiVn92cAwffhVBZ4vwTPQ01Gr30KvnkHL22ezyJKHenA==";
		String encCert = "MIIDwTCCA2agAwIBAgIQeYJqWnw8IHOKl55VjiyTUTAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDMyODE2MDAwMFoXDTE4MDMyODE2MDAwMFowRDEOMAwGA1UEChMFc2hlY2ExETAPBgNVBAgTCHNoYW5naGFpMQswCQYDVQQGEwJDTjESMBAGA1UEAxMJc2hlY2FUZXN0MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEEsyKg0zrs61met8qbSo591/Dp5olRV+22c4BjIdrF/k3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztg6OCAkYwggJCMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBQIvR69sN+jKaw0V8npb057mLrzQTAOBgNVHQ8BAf8EBAMCAzgwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADCB4AYDVR0fBIHYMIHVMIGZoIGWoIGThoGQbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMNzIxLmNybCxvdT1SQTIwMTMwMjE2LG91PUNBOTEsb3U9Y3JsLG89VW5pVHJ1c3Q/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENsYXNzPWNSTERpc3RyaWJ1dGlvblBvaW50MDegNaAzhjFodHRwOi8vbGRhcDIuc2hlY2EuY29tL0NBOTEvUkEyMDEzMDIxNi9DUkw3MjEuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB4GCSqBHIbvOguBTQQREw8yODNASkoxMjM0NTY3ODkwDAYIKoEcz1UBg3UFAANHADBEAiAZW2ykFLR4GmFO3eDzyV5IQb6Wbftib/dJUaAFthtCXwIgRndEpjqh4n1D7c21JLfyAfr8snB14LRSr7tS5tFzx/k=";
		String encKeystore = "AQAAAAEBAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABhlaYF5AoBV+JrqirNdss3OWWNofO91l6CDRLbAL8+nwABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABLMioNM67OtZnrfKm0qOfdfw6eaJUVfttnOAYyHaxf5AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3Qhiiw+gmyF8/5mjobX61PEErgrjc6Ryko+5062ztgwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAArf3K6UhMb2NFtG6XGNQRxZKeLSZbK0bFviC/XmfSEk4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAOWnbCvF0Qdq4RFVMf+roWtG9M4TKjE5Kt6hyatGdkU+yMWG1A0m4I83jAkLY3gxaQBWMS9FDggTRI8sV3llqBYQAAAAZuIhfMG87/3T/1OSe3w0ew==";
		String certChain = "MIIEiAYJKoZIhvcNAQcCoIIEeTCCBHUCAQExADALBgkqhkiG9w0BBwGgggRdMIIBpzCCAUugAwIBAgICAIEwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zODEyMzEwMDAwMDBaMDcxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDEVMBMGA1UEAwwMVUNBIFJvb3QgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEnLsOejX5v0nI1BsH6Glz/+ui/Uge27gmxsIemVDmOxKjs0Hp9ZPbqzXajUoYp9Rlcf6BmoVe02Y12ZvRHMBCU6NFMEMwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wIAYDVR0OAQEABBYEFO7osJzV3Oxz/e98+lAsxsFA5kyzMAwGCCqBHM9VAYN1BQADSAAwRQIhAJqydZmPsiPSBBWmD8bTLBXBnvDhUv4xp81GCNCBh+L+AiAoulB2Q7LIe0zFaRl1liJ9QH8NaZtI1I7eOGC8Z9gUvjCCAq4wggJRoAMCAQICEF17yetm9O3ri9K6qQyPciMwDAYIKoEcz1UBg3UFADA3MQswCQYDVQQGEwJDTjERMA8GA1UECgwIVW5pVHJ1c3QxFTATBgNVBAMMDFVDQSBSb290IFNNMjAeFw0xMzAyMDUwMDAwMDBaFw0zNzEyMzEwMDAwMDBaMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEfdEfkS0GSlQQ8ISEVSUdvKL7tcd3bsNssWlmmOhN5VCg1iLJgMDDqhO9TFt4EDsZuvECXz8uiU+BL4pddBcMgKOCAT4wggE6MEQGA1UdIAEBAAQ6MDgwNgYIKoEchu86gRUwKjAoBggrBgEFBQcCARYcaHR0cDovL3d3dy5zaGVjYS5jb20vcG9saWN5LzAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADA9BgNVHR8BAQAEMzAxMC+gLaArhilodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20yc3ViLmNybDAiBgNVHSMBAQAEGDAWgBTu6LCc1dzsc/3vfPpQLMbBQOZMszAgBgNVHQ4BAQAEFgQUiTEEkXtDqqqav4Qdm4bu8LhwmaAwSQYIKwYBBQUHAQEBAQAEOjA4MDYGCCsGAQUFBzAChipodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3QvdWNhc20ycm9vdC5kZXIwDAYIKoEcz1UBg3UFAANJADBGAiEAmFStsqFTAiEmqQUDR+0QXwTUgJYhNZicXfaGtuyKhF0CIQCDONwlcY/av+yWE+3+VVqzmiBnLKw6QnyHvLkNnEYH9DEA";								
        */

        Cert cert = certDao.getCertByEnvsn(requestNumber, accountDao.getLoginAccount().getName());
        cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
        cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
        cert.setKeystore("");
        cert.setPrivatekey("");
        cert.setCertsn(getCertSN(userCert));
        cert.setNotbeforetime(getCertNotbeforetime(userCert));
        cert.setValidtime(getCertValidtime(userCert));
        cert.setCertchain(certChain);

        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType) {
            cert.setCertificate(userCert);
            cert.setEnccertificate(encCert);
            cert.setEnckeystore(encKeystore);

            if (!"".equals(encCert))
                retcode = gEsDev.saveSM2DoubleCert(prikeyPassword, userCert, encCert, encKeystore);
            else
                retcode = gEsDev.saveSM2DoubleCert(prikeyPassword, userCert, "", encKeystore);

            if (retcode == 0) {
                if (!"".equals(encCert)) {
                    if (null == certDao.getCertByEnvsn(requestNumber + "-e", accountDao.getLoginAccount().getName())) {
                        Cert certEnc = new Cert();
                        certEnc.setEnvsn(requestNumber + "-e");
                        certEnc.setPrivatekey("");
                        certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                        certEnc.setCerttype(cert.getCerttype());
                        certEnc.setSignalg(cert.getSignalg());
                        certEnc.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
                        certEnc.setContainerid("");
                        certEnc.setStatus(Cert.STATUS_DOWNLOAD_CERT);
                        certEnc.setCertificate(encCert);
                        certEnc.setCertchain(certChain);
                        certEnc.setNotbeforetime(getCertNotbeforetime(encCert));
                        certEnc.setValidtime(getCertValidtime(encCert));
                        certEnc.setKeystore("");
                        certEnc.setEnccertificate(encCert);
                        certEnc.setEnckeystore(encKeystore);
                        certEnc.setCertsn(getCertSN(encCert));
                        certEnc.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
                        certEnc.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));

                        certDao.addCert(certEnc, accountDao.getLoginAccount().getName());
                    }
                }
            }
        } else {
            cert.setCertificate(userCert);
            cert.setEnccertificate(encCert);
            cert.setEnckeystore(encKeystore);

            if (null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0)
                initShcaCciStdService();

            if (!"".equals(encCert))
                retcode = ShcaCciStd.gSdk.saveSM2DoubleCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert, encCert, encKeystore);
            else
                retcode = ShcaCciStd.gSdk.saveSM2SignatureCert(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, userCert);

            if (retcode == 0)
                retcode = ShcaCciStd.gSdk.changePin(cert.getContainerid(), CommonConst.JSHECACCISTD_PWD, prikeyPassword);
            if (retcode == 0) {
                if (!"".equals(encCert)) {
                    if (null == certDao.getCertByEnvsn(requestNumber + "-e", accountDao.getLoginAccount().getName())) {
                        Cert certEnc = new Cert();
                        certEnc.setEnvsn(requestNumber + "-e");
                        certEnc.setPrivatekey("");
                        certEnc.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
                        certEnc.setCerttype(cert.getCerttype());
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
                        certEnc.setDevicesn(android.os.Build.SERIAL);

                        certDao.addCert(certEnc, accountDao.getLoginAccount().getName());
                    }
                }
            }
        }
		/*byte[] bCert = Base64.decode(userCert);
		javasafeengine jse = new javasafeengine();
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		cert.setCertsn(new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray())));	
		String p12 = genP12(cert.getPrivatekey(), prikeyPassword, userCert, certChain);
		cert.setKeystore(p12);
		cert.setPrivatekey("");
		*/
        //showMessage("2");

        certDao.updateCert(cert, accountDao.getLoginAccount().getName());
        certDao.deleteCert(mCert.getId());
        Cert oldEncCert = certDao.getCertByEnvsn(mCert.getEnvsn() + "-e", accountDao.getLoginAccount().getName());
        if (null != oldEncCert)
            certDao.deleteCert(oldEncCert.getId());

        //Toast.makeText(ResultActivity.this, "保存证书成功",Toast.LENGTH_LONG).show();

        //showMessage("3");
        saveLog(OperationLog.LOG_TYPE_RENEWCERT, cert.getCertsn(), "", "", "");
        //showMessage("4");
        final Handler handler = new Handler(activity.getMainLooper());
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
                    if (resultStr.equals("0")) {
                        //Toast.makeText(ResultActivity.this, "下载证书成功", Toast.LENGTH_LONG).show();
                        Cert cert = certDao.getCertByEnvsn(requestNumber, accountDao.getLoginAccount().getName());
                        if (null != cert) {
                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                            certDao.updateCert(cert, accountDao.getLoginAccount().getName());
                        }

                        cert = certDao.getCertByEnvsn(requestNumber + "-e", accountDao.getLoginAccount().getName());
                        if (null != cert) {
                            cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
                            certDao.updateCert(cert, accountDao.getLoginAccount().getName());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sealPresenter.closeProgDlgCert();
                                Toast.makeText(context, "证书更新成功", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                //activity.finish();
                            }
                        });

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sealPresenter.closeProgDlgCert();
                            }
                        });

                        throw new Exception("证书更新失败：" + resultStr + "，" + returnStr);
                    }
                } catch (Exception exc) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sealPresenter.closeProgDlgCert();
                        }
                    });
                    Toast.makeText(context, exc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

    }


    private String SetSuccessStatus(final String requestNumber, final int saveType) throws Exception {
        String timeout = activity.getString(R.string.WebService_Timeout);
        String urlPath = activity.getString(R.string.UMSP_Service_SetSuccessStatus);
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("requestNumber", requestNumber);
        postParams.put("clientOSType", "1");  //客户端操作系统类型（1：Android；2：IOS；3：WP）
        postParams.put("clientOSDesc", getOSInfo());  //客户端操作系统描述
        if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
            postParams.put("media", "2");   //证书存储介质类型（1：文件；2：SD卡）
        else
            postParams.put("media", "1");   //证书存储介质类型（1：文件；2：SD卡）

        String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
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

    private void setCertRenewStatus(Cert cert) {
        cert.setStatus(Cert.STATUS_RENEW_CERT);
        certDao.updateCert(cert, accountDao.getLoginAccount().getName());
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

        logDao.addLog(log, accountDao.getLoginAccount().getName());
    }

    private String getOSInfo() {
        String strOSInfo = "";

        strOSInfo = "硬件型号:" + android.os.Build.MODEL + "|操作系统版本号:"
                + android.os.Build.VERSION.RELEASE;
        return strOSInfo;
    }


    private String getCertCN(Cert cert) {
        String commonName = "";

        byte[] bCert = Base64.decode(cert.getCertificate());
        try {
            commonName = jse.getCertDetail(17, bCert);
        } catch (Exception ex) {
            commonName = "";
        }

        return commonName;
    }

    private boolean isCertUpdateValid(String strCert) {  //判断证书是否需要更新
        String strValidTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = new Date();//获取时间
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");//转换格式

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strValidTime = jse.getCertDetail(12, bCert);

            Date toDate = sdf.parse(strValidTime);
            Date curDate = sdf.parse(sdf1.format(date));

            if (curDate.getTime() >= toDate.getTime())   //证书已过期
                return false;

            long intervalMilli = toDate.getTime() - curDate.getTime();
            int day = (int) (intervalMilli / (24 * 60 * 60 * 1000));
            if (day <= 15)  //证书过期不到15天
                return false;

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private int getCertValidState(String strCert) {  //获取证书过期状态
        String strValidTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = new Date();//获取时间
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");//转换格式

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strValidTime = jse.getCertDetail(12, bCert);

            Date toDate = sdf.parse(strValidTime);
            Date curDate = sdf.parse(sdf1.format(date));

            if (curDate.getTime() >= toDate.getTime())   //证书已过期
                return -1;

            long intervalMilli = toDate.getTime() - curDate.getTime();
            int day = (int) (intervalMilli / (24 * 60 * 60 * 1000));
            if (day <= 15)   //证书过期不到15天
                return 1;   //证书将过期

        } catch (Exception e) {
            return -1;
        }

        return 0;  //证书有效期内
    }


    private boolean isCertTested(String strCert) {    //判断证书是否测试证书
        String strValidTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            byte[] bCert = Base64.decode(strCert);
            javasafeengine jse = new javasafeengine();
            strValidTime = jse.getCertDetail(12, bCert);
            Date toDate = sdf.parse(strValidTime);

            strValidTime = jse.getCertDetail(11, bCert);
            Date fromDate = sdf.parse(strValidTime);

            if (fromDate.getTime() >= toDate.getTime())
                return true;

            long intervalMilli = toDate.getTime() - fromDate.getTime();
            int day = (int) (intervalMilli / (24 * 60 * 60 * 1000));
            if (day <= 92)
                return true;

        } catch (Exception e) {
            return true;
        }

        return false;
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

    private Boolean loginUMSPService(String act) {    //重新登录UM Service
        String returnStr = "";
        try {
            //异步调用UMSP服务：用户登录
            String timeout = activity.getString(R.string.WebService_Timeout);
            String urlPath = activity.getString(R.string.UMSP_Service_Login);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("accountName", act);
            postParams.put("pwdHash", getPWDHash(accountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
            if (accountDao.getLoginAccount().getType() == 1)
                postParams.put("appID", CommonConst.UM_APPID);
            else
                postParams.put("appID", accountDao.getLoginAccount().getAppIDInfo());

            String responseStr = "";
            try {
                //清空本地缓存
                WebClientUtil.cookieStore = null;
                responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
            } catch (Exception e) {
                if (null == e.getMessage())
                    throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
                else
                    throw new Exception("用户登录失败：" + e.getMessage() + " 请重新点击登录");
            }

            JSONObject jb = JSONObject.fromObject(responseStr);
            String resultStr = jb.getString(CommonConst.RETURN_CODE);
            returnStr = jb.getString(CommonConst.RETURN_MSG);

        } catch (Exception exc) {
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

        return strPWDHash;
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

        if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()))
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


}
