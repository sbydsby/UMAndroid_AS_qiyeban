package com.sheca.zhongmei;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.scsk.EnumCertDetailNo;
import com.sheca.scsk.ScskResultException;
import com.sheca.scsk.ShecaSecKit;
import com.sheca.zhongmei.adapter.Entity;
import com.sheca.zhongmei.adapter.ViewCertPagerAdapter;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CertCash;
import com.sheca.zhongmei.util.CommUtil;
import com.sheca.zhongmei.util.CommonConst;

import org.json.JSONException;
import org.spongycastle.util.encoders.Base64;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CertManageActivity extends Activity {
    private List<Map<String, String>> mData = null;
    private AccountDao accountDao = null;
    CertDao certDao = null;
    private ViewPager viewPager;

    private static int pageNum = 1;

    private final int ITEM_COUNT = 1;

    private int count = 1;

    private ArrayList<View> views = null;

    private LinearLayout llDots;

    private static int positionNow = 0;

    private int certID = 0;

    ViewCertPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_manage);

        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("证书管理");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CertManageActivity.this.finish();
            }
        });


        LinearLayout ll_update = findViewById(R.id.ll_update);
        ll_update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CertManageActivity.this, CertRenewActivity.class);
                intent.putExtra("CertId", certID + "");
                startActivity(intent);

            }
        });
        LinearLayout ll_revoke = findViewById(R.id.ll_revoke);
        ll_revoke.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CertManageActivity.this, CertRevokeActivity.class);
                intent.putExtra("CertId", certID + "");
                startActivity(intent);
            }
        });

        LinearLayout ll_cash = findViewById(R.id.ll_cash);
        ll_cash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(CertManageActivity.this, CertCashActivity.class);
//                intent.putExtra("CertId", certID+"");
//                startActivity(intent);
                getCash();


            }
        });


        LinearLayout  ll_detail= findViewById(R.id.ll_detail);
        ll_detail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CertManageActivity.this, CertDetailActivity.class);
                intent.putExtra("CertId", certID+"");
                startActivity(intent);



            }
        });

        certDao = new CertDao(this);
        accountDao = new AccountDao(this);

        showCertList();
    }


    private void getCash() {//开票

        CertDao certDao = new CertDao(this);
        Cert mCert = certDao.getCertByID(certID);
        if (mCert != null) {
            String CertSn = mCert.getCertsn().toUpperCase();

            String UMSP_Cert_Cash=getResources().getString(R.string.UMSP_Cert_Cash);


            String result=  CertCash.getQrCodeResult(CertSn,UMSP_Cert_Cash);
            Uri uri = Uri.parse(result);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        } else {
            Toast.makeText(CertManageActivity.this, "证书不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


    }


    private void showCertList() {
        try {
            if (mData != null) {
                mData.clear();
            }
            mData = getData();
        } catch (Exception e) {
            clearCertList();
            Log.e(CommonConst.TAG, e.getMessage(), e);
            Toast.makeText(this, "获取证书错误！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mData.size() == 0) {
        } else {

            initView();
        }


    }


    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager_room);// 找到ViewPager
        viewPager.setPageMargin(30);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setOnPageChangeListener(pageChangeListener);// 设置页面滑动监听
        Entity entity = new Entity();
        pageNum = entity.getPageNum(mData.size(), ITEM_COUNT);//view 的页数


        //根据页数动态生成指示器
        llDots = (LinearLayout) findViewById(R.id.ll_dots);
        for (int i = 0; i < pageNum; i++) {
            if (pageNum > 1) {
                ImageView img = new ImageView(this);
                img.setPadding(0, 0, 0, 0);


                if (i == 0) {
                    img.setImageResource(R.drawable.dot_enable);
                } else {
                    img.setImageResource(R.drawable.dot_disable);
                }
                img.setId(i);   //注意这点 设置id
                llDots.addView(img);

                ViewGroup.LayoutParams param = img.getLayoutParams();
                param.width = CommUtil.dip2px(CertManageActivity.this, 10);
                param.height = CommUtil.dip2px(CertManageActivity.this, 10);

                img.setLayoutParams(param);

            }
        }
        //动态生成viewpager的多个view
        for (int i = 0; i < pageNum; i++) {
            initListViews(count++);
        }

        instantiated(mData.size(), ITEM_COUNT);//实例化ViewPager
        adapter = new ViewCertPagerAdapter(views);// 构造adapter
        viewPager.setAdapter(adapter);// 设置适配器
    }

    private void initListViews(int count) {
        if (views == null) {
            views = new ArrayList<View>();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
//	        views.add(inflater.inflate(R.layout.view_cert_item, null));
        views.add(inflater.inflate(R.layout.item_cardview_new, null));
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
                ImageView img = (ImageView) findViewById(getResources().getIdentifier("" + position, "id", "com.tony.viewpager"));
                img.setImageResource(R.drawable.dot_enable);

                ImageView imgold = (ImageView) findViewById(getResources().getIdentifier("" + oldPosition, "id", "com.tony.viewpager"));
                imgold.setImageResource(R.drawable.dot_disable);

                oldPosition = position;
            }
//
            certID = Integer.valueOf(mData.get(positionNow).get("id"));
//
//            if ("false".equals(mData.get(positionNow).get("isUpdate"))) {
//                Drawable drawable = context.getResources().getDrawable(R.drawable.module_renew_cert_tip);
//                ((Button) view.findViewById(R.id.button_renew_cert)).setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
//            } else {
//                Drawable drawable = context.getResources().getDrawable(R.drawable.module_renew_cert);
//                ((Button) view.findViewById(R.id.button_renew_cert)).setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
//            }
//
//            if (Cert.STATUS_RENEW_CERT == Integer.parseInt(mData.get(positionNow).get("status")))
//                view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//            else
//                view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//
//            if ("true".equals(mData.get(positionNow).get("isTested"))) {
//                view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//                view.findViewById(R.id.button_renew_cert).setVisibility(RelativeLayout.VISIBLE);
////                views.get(positionNow).findViewById(R.id.Layout_cert_item).setBackgroundResource(R.drawable.certcardview_test);
//            } else {
//                view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//                view.findViewById(R.id.button_renew_cert).setVisibility(RelativeLayout.VISIBLE);
////                views.get(positionNow).findViewById(R.id.Layout_cert_item).setBackgroundResource(R.drawable.certcardview_normal);
//            }

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {// 滑动中。。。

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变

        }
    };

    private void clearCertList() {


    }


    private List<Map<String, String>> getData() throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<Cert> certList = new ArrayList<Cert>();
//
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
        sdf.setTimeZone(tzChina);
//
        String strActName = accountDao.getLoginAccount().getName();
//        if (accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
//            strActName = accountDao.getLoginAccount().getName() + "&" + accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
//
        certList = certDao.getAllCerts(strActName);


        for (Cert cert : certList) {
            if (cert.getEnvsn().indexOf("-e") != -1)
                continue;
            if (CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
                continue;

            if (null == cert.getCertificate() || "".equals(cert.getCertificate()))
                continue;

//			if (certType == false && getCertType(cert) == true) {//true表示单位证书 false表示个人证书
//				continue;
//			}
//			if (certType == true && getCertType(cert) == false) {//true表示单位证书 false表示个人证书
//
//				continue;
//			}

            if (cert.getEnvsn().indexOf("-e") != -1) {//过滤加密证书
                continue;
            }
//			if (getCertType(cert) == false&&!PKIUtil.isAccountCert(cert.getCertificate(), AccountHelper.getIDNumber(getActivity())))
//				continue;
//			if (getCertType(cert) == true&&! PKIUtil.isOrgCert(cert.getCertificate(), AccountHelper.getIDNumber(getActivity())))
//				continue;


            if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", String.valueOf(cert.getId()));

                byte[] bCert = Base64.decode(cert.getCertificate());


//              byte[]  data = ShecaSecKit.certDetailWithCert(bCert, EnumCertDetailNo.EnumCertDetailNoNotAfter);
//                String commonName = new String(Base64.decode(data));
//
//
//                String commonName = jse.getCertDetail(17, bCert);
//                String organization = jse.getCertDetail(14, bCert);
//  String certSn = new String(ShecaSecKit.certDetailWithCert(bCert, EnumCertDetailNo.EnumCertDetailNoSN));
//
                String strNotBeforeTime = new String(ShecaSecKit.certDetailWithCert(bCert, EnumCertDetailNo.EnumCertDetailNoNotBefore));
                String strValidTime = new String(ShecaSecKit.certDetailWithCert(bCert, EnumCertDetailNo.EnumCertDetailNoNotAfter));
                Date fromDate = sdf.parse(strNotBeforeTime);
                Date toDate = sdf.parse(strValidTime);


//                String    sCertUnicode = new String(ShecaSecKit.certExtensionByOidWithCert(bCert, "1.2.156.112570.11.210"));  //获取统一社会信用代码
//
//                map.put("organization", organization);
//                map.put("commonname", commonName);

                if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype()) || cert.getCerttype().contains("SM2"))
                    map.put("certtype", CommonConst.CERT_SM2_NAME);
                else
                    map.put("certtype", CommonConst.CERT_RSA_NAME);

                if (CommonConst.SAVE_CERT_TYPE_PHONE == cert.getSavetype())
                    map.put("savetype", CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
                else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype())
                    map.put("savetype", CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
                else if (CommonConst.SAVE_CERT_TYPE_AUDIO == cert.getSavetype())
                    map.put("savetype", CommonConst.SAVE_CERT_TYPE_AUDIO_NAME);
                else if (CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
                    map.put("savetype", CommonConst.SAVE_CERT_TYPE_SIM_NAME);
                else
                    map.put("savetype", CommonConst.SAVE_CERT_TYPE_PHONE_NAME);

                map.put("notbeforetime", sdf2.format(fromDate));
                map.put("validtime", sdf2.format(toDate));
                map.put("status", cert.getStatus() + "");


//				if (getCertType(cert)) {
                map.put("certtypemore", "单位证书");
//				} else {
//					map.put("certtypemore", "个人证书");
//				}


//				if (isCertUpdateValid(cert.getCertificate()))
//					map.put("isUpdate", "true");
//				else
//					map.put("isUpdate", "false");

//                if (isCertTested(cert.getCertificate()))
//				if (!isCertUpdateValidOld(cert.getCertificate()))//小于90天的是试用证书
//					map.put("isTested", "true");
//				else
//					map.put("isTested", "false");

                map.put("validState", "" + getCertValidState(cert));
                map.put("certname", getCertName(cert));
                map.put("certsn", getCertSN(cert));

                list.add(map);
            }
        }
//
        return list;
    }


    @SuppressLint("NewApi")
    private void instantiated(int listSize, int numberOfEveryPage) {
        for (int i = 0; i < pageNum; i++) {
            for (int j = 0; j < numberOfEveryPage; j++) {
                TextView textview = null;
                textview = (TextView) views.get(i).findViewById(R.id.certnickname);
                textview.setText(mData.get(i).get("certname"));

                certID = Integer.valueOf(mData.get(0).get("id"));
//                textview = (TextView) views.get(i).findViewById(R.id.certsn);
//                textview.setText(mData.get(i).get("certsn"));

                // textview = (TextView)views.get(i).findViewById(R.id.certname);
                // textview.setText(mData.get(i).get("commonname"));

//                textview = (TextView) views.get(i).findViewById(R.id.certtype);
//                textview.setText(mData.get(i).get("certtype"));


                textview = (TextView) views.get(i).findViewById(R.id.certtypemore);
                textview.setText(mData.get(i).get("certtypemore"));


//	                textview = (TextView)views.get(i).findViewById(R.id.savetype);
//	                textview.setText("存储介质: "+mData.get(i).get("savetype"));

//	                textview = (TextView)views.get(i).findViewById(R.id.certbefore);
//	                textview.setText(mData.get(i).get("notbeforetime"));

                TextView tvStatus = (TextView) views.get(i).findViewById(R.id.tv_cert_status);


                textview = (TextView) views.get(i).findViewById(R.id.certafter);
//	                String bDate = String.valueOf(mData.get(i).get("notbeforetime"));
                String exDate = String.valueOf(mData.get(i).get("validtime"));

                long leftday = CommUtil.getLeftDay(exDate);

                Log.d("validtime", "" + leftday);

                if (0 > Integer.parseInt(mData.get(i).get("validState"))) {//过期
                    views.get(i).findViewById(R.id.layout_leftday).setVisibility(View.INVISIBLE);
                    tvStatus.setText(R.string.cert_status_expire);
                } else {
                    tvStatus.setText(R.string.certdetail_leftdays);
                    views.get(i).findViewById(R.id.layout_leftday).setVisibility(View.VISIBLE);
                    textview.setText(String.valueOf(leftday));
                }


//                if ("false".equals(mData.get(0).get("isUpdate"))) {
//                    Drawable drawable = context.getResources().getDrawable(R.drawable.module_renew_cert_tip);
//                    ((Button) view.findViewById(R.id.button_renew_cert)).setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
//                } else {
//                    Drawable drawable = context.getResources().getDrawable(R.drawable.module_renew_cert);
//                    ((Button) view.findViewById(R.id.button_renew_cert)).setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
//                }

//	                if("false".equals(mData.get(i).get("isTested"))){
//	                    views.get(i).findViewById(R.id.certusetype).setVisibility(RelativeLayout.GONE);
//	                    if(0 == Integer.parseInt(mData.get(i).get("validState"))){
//		                	((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_normal));
//	                	}else if(1 == Integer.parseInt(mData.get(i).get("validState"))){
//	                		((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_renew));
//	                	}else{
//	                		((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_invalid));	                	}
//	                }else{
//	                	views.get(i).findViewById(R.id.certusetype).setVisibility(RelativeLayout.GONE);
//	                	if(0 == Integer.parseInt(mData.get(i).get("validState"))){
//	                		((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_test));
//	                	}else if(1 == Integer.parseInt(mData.get(i).get("validState"))){
//	                		((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_test));
//	                	}else{
//	                		((Button) views.get(i).findViewById(R.id.cert_type)).setBackground(activity.getBaseContext().getResources().getDrawable(R.drawable.cert_invalid));
//	                	}
//	                }

//                if (Cert.STATUS_RENEW_CERT == Integer.parseInt(mData.get(0).get("status")))
//                    view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//                else
//                    view.findViewById(R.id.button_revoke_cert).setVisibility(RelativeLayout.VISIBLE);
//                // TODO: 2019/4/26 测试时注释掉
//                if ("true".equals(mData.get(0).get("isTested"))) {
////                    views.get(i).findViewById(R.id.Layout_cert_item).setBackgroundResource(R.drawable.certcardview_test);
//                    view.findViewById(R.id.button_renew_cert).setVisibility(RelativeLayout.VISIBLE);
//                } else {
////                    views.get(i).findViewById(R.id.Layout_cert_item).setBackgroundResource(R.drawable.certcardview_normal);
//                    view.findViewById(R.id.button_renew_cert).setVisibility(RelativeLayout.VISIBLE);
//                }
//					views.get(i).findViewById(R.id.Layout_cert_item).setBackgroundResource(R.drawable.certcardview_normal);
//					view.findViewById(R.id.button_renew_cert).setVisibility(RelativeLayout.VISIBLE);

            }

//            views.get(i).setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mData.size() == 0) {
//                        Toast.makeText(context, "无证书", Toast.LENGTH_SHORT).show();
//                    } else {
//                        // Intent intent = new Intent(activity, CertDetailActivity.class);
//                        // intent.putExtra("CertId", certID+"");
//                        //startActivity(intent);
//                    }
//                }
//            });
        }

    }

    private String getCertName(Cert mCert) {
        String commonName = "";
        byte[] data;
        byte[] cert = Base64.decode(mCert.getCertificate());
        try {
            data = ShecaSecKit.certDetailWithCert(cert, EnumCertDetailNo.EnumCertDetailNoSubjectCN);
            commonName = new String(data);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ScskResultException e) {
            e.printStackTrace();
        }

        return commonName;
    }


    private int getCertValidState(Cert mCert) {
        String strValidTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = new Date();//获取时间
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");//转换格式


        byte[] data;
        byte[] cert = Base64.decode(mCert.getCertificate());

        try {
            try {
                data = ShecaSecKit.certDetailWithCert(cert, EnumCertDetailNo.EnumCertDetailNoNotAfter);
                strValidTime = new String(data);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ScskResultException e) {
                e.printStackTrace();
            }


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


    private boolean getCertType(Cert cert) {  //true 单位证书 false个人证书
//        Log.e("类型", cert.getCerttype());
        return !cert.getCerttype().contains("个人");

    }

    private String getCertSN(Cert cert) {
        return cert.getCertsn().toLowerCase();
    }

}
