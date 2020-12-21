package com.sheca.umee;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sheca.umee.controller.SealViewHolderNew;
import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.dao.SealInfoDao;
import com.sheca.umee.model.Cert;
import com.sheca.umee.model.SealInfo;
import com.sheca.umee.util.CommUtil;
import com.sheca.umee.widget.Banner;
import com.sheca.umee.widget.BannerConfig;

import java.util.ArrayList;
import java.util.List;

public class SealManageActivity extends Activity {


    SealInfoDao sealInfoDao;
    AccountDao accountDao;
    CertDao certDao;

    int oldPosition = 0;

    Banner mCardBanner;
    List<SealInfo> mSealList;
    private LinearLayout llDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_seal_manage);

        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("印章管理");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        mCardBanner = findViewById(R.id.card_banner);
        llDots = findViewById(R.id.ll_dots);


        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SealManageActivity.this.finish();
            }
        });

        mSealList = new ArrayList<>();
        sealInfoDao = new SealInfoDao(this);
        accountDao = new AccountDao(this);
        certDao = new CertDao(this);
        String strActName = accountDao.getLoginAccount().getName();
        List<SealInfo> SealList = sealInfoDao.getAllSealInfos(strActName);

        for (int i = 0; i < SealList.size(); i++) {
            Cert cert = certDao.getCertByCertsn(SealList.get(i).getCertsn(), strActName);
            if (cert == null) {
            } else {

                if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT || cert.getStatus() == Cert.STATUS_RENEW_CERT) {

                    mSealList.add(SealList.get(i));
                }


            }

        }


        initView();

    }


    private void initView() {


        mCardBanner.setIsCert(false);
        mCardBanner.setAutoPlay(false)
                .setPages(mSealList, new SealViewHolderNew())
                .setBannerStyle(BannerConfig.NOT_INDICATOR)
                .start();

        mCardBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mSealList.size() > 1) {
                    ImageView img = (ImageView) findViewById(getResources().getIdentifier("" + position, "id", "com.tony.viewpager"));
                    img.setImageResource(R.drawable.dot_enable);

                    ImageView imgold = (ImageView) findViewById(getResources().getIdentifier("" + oldPosition, "id", "com.tony.viewpager"));
                    imgold.setImageResource(R.drawable.dot_disable);

                    oldPosition = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        for (int i = 0; i < mSealList.size(); i++) {
            if (mSealList.size() > 1) {
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
                param.width = CommUtil.dip2px(SealManageActivity.this, 10);
                param.height = CommUtil.dip2px(SealManageActivity.this, 10);

                img.setLayoutParams(param);

            }
        }
    }


}
