package com.sheca.zhongmei.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.sheca.zhongmei.R;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.listener.BannerViewHolder;
import com.sheca.zhongmei.model.Cert;


import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by songwenchao
 * on 2018/5/17 0017.
 * <p>
 * 类名
 * 需要 --
 * 可以 --
 */
public class CertViewHolderNew implements BannerViewHolder<Cert> {


    private final FromCertToSeal fromCertToSeal;
    @BindView(R.id.tv_cert_roles)
    TextView mTvCertRoles;
    @BindView(R.id.iv_cert_logo)
    ImageView mIvCertLogo;
//    @BindView(R.id.tv_cert_type)
//    TextView mTvCertType;
//    @BindView(R.id.tv_cert_company_name)
//    TextView mTvCertCompanyName;
//    @BindView(R.id.tv_cert_time)
//    TextView mTvCertTime;


    CertDao certDao;

    public CertViewHolderNew(FromCertToSeal fromCertToSeal) {
        this.fromCertToSeal = fromCertToSeal;
    }

//    SealInfoDao sealInfoDao;

    @Override
    public View createView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cardview_new, null);
        certDao = new CertDao(context);
//        sealInfoDao = new SealInfoDao(context);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onBind(Context context, int position, Cert data) {
//        String type = data.getCerttype().contains("SM2") ? "SM2" : "RSA";
//        mTvCertType.setText(type);
//        mTvCertCompanyName.setText(TextUtils.isEmpty(data.getCertname()) ? CommUtil.getCertDetail(data, 17) : data.getCertname());
//
//        if (certDao.verifyCert(data) == 0) {
//            mTvCertTime.setText("证书有效期剩余" + CommUtil.getLeftDay(data.getValidtime()) + "天");
//        } else {
//            mTvCertTime.setText("证书已过期");
//        }

//        SealInfo seal = sealInfoDao.getSealByCertsn(data.getCertsn(), "");

//        if (data.getSealstate() == 1) {
//            mCetForSeal.setVisibility(View.GONE);
//        } else {
//            mCetForSeal.setVisibility(View.GONE);
//        }

        switch (data.getCertlevel()) {

            case 1:
                mTvCertRoles.setText("法定代表人");
                break;
            case 2:
                mTvCertRoles.setText("证书管理员");
                break;
            case 3:
                mTvCertRoles.setText("办事人");
                break;
           default:
                mTvCertRoles.setText("办事人");
                break;

        }


//        mCetForSeal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //position代表改证书的位置，跳转到对应的印章页
//                fromCertToSeal.gotoSeal(position);
//
//            }
//        });

    }


    public interface FromCertToSeal {
        void gotoSeal(int position);
    }


}
