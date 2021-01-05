package com.sheca.zhongmei.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


import com.sheca.zhongmei.R;
import com.sheca.zhongmei.listener.BannerViewHolder;
import com.sheca.zhongmei.model.SealInfo;
import com.sheca.zhongmei.util.CommUtil;



/**
 * Created by songwenchao
 * on 2018/5/17 0017.
 * <p>
 * 类名
 * 需要 --
 * 可以 --
 */
public class SealViewHolderNew implements BannerViewHolder<SealInfo> {
//    private final FromSealToCert fromSealToCert;
    private ImageView mDetailSeal;
    private ImageView mSeal;


//    private final List<SealInfo> mList;

    public SealViewHolderNew() {
//        this.fromSealToCert= fromSealToCert;
    }

    @Override
    public View createView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sealview_new, null);
//        mDetailSeal = (ImageView) view.findViewById(R.id.iv_seal_detail);
        mSeal = (ImageView) view.findViewById(R.id.iv_seal);

        return view;
    }

    @Override
    public void onBind(Context context, int position, SealInfo data) {
        // 数据绑定
//        if (data.getDownloadstatus() == 1) {//已下载
            mSeal.setImageBitmap(CommUtil.stringtoBitmap(data.getPicdata()));
//            mDetailSeal.setVisibility(View.GONE);
//        } else {
//            mSeal.setImageResource(R.mipmap.no_seal_icon);
//            mDetailSeal.setVisibility(View.GONE);
//        }
//        mDetailSeal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fromSealToCert.gotoCert(position,data.getCertsn());
//            }
//        });
    }

//    public interface FromSealToCert {
//        void gotoCert(int position, String certSn);
//    }
}
