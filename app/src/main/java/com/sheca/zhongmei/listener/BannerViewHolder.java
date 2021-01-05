package com.sheca.zhongmei.listener;

import android.content.Context;
import android.view.View;

public interface BannerViewHolder<T> {

    View createView(Context context);

    void onBind(Context context, int position, T data);
}
