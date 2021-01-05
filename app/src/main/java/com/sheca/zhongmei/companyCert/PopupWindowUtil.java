package com.sheca.zhongmei.companyCert;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.sheca.zhongmei.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuchangqing
 * @time 2019/8/8 17:18
 * @descript popupwindow工具类
 */
public class PopupWindowUtil {

    private ListView listView;
    private PopupWindow window;
    //窗口在x轴偏移量
    private int xOff = 0;
    //窗口在y轴的偏移量
    private int yOff = 0;

    public PopupWindowUtil(Context context, List<String> datas) {

        window = new PopupWindow(context);
        //ViewGroup.LayoutParams.WRAP_CONTENT，自动包裹所有的内容
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable(true);
        //点击 back 键的时候，窗口会自动消失
        window.setBackgroundDrawable(new BitmapDrawable());

        View localView = LayoutInflater.from(context).inflate(R.layout.pop_menue, null);
        listView = (ListView) localView.findViewById(R.id.pop_listview);

        listView.setAdapter(new MyAdapter(context, datas));
        listView.setTag(window);
        //设置显示的视图
        window.setContentView(localView);
    }

    public void setItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void dismiss() {
        window.dismiss();
    }

    /**
     * @param xOff x轴（左右）偏移
     * @param yOff y轴（上下）偏移
     */
    public void setOff(int xOff, int yOff) {
        this.xOff = xOff;
        this.yOff = yOff;
    }

    /**
     * @param paramView 点击的按钮
     */
    public void show(View paramView, int count) {
        //该count 是手动调整窗口的宽度
        window.setWidth(paramView.getWidth() * count);
        //设置窗口显示位置, 后面两个0 是表示偏移量，可以自由设置
        window.showAsDropDown(paramView, xOff, yOff);
        //更新窗口状态
        window.update();
    }

    public void showPopMiddle(int widgetwith,View view){
        window.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int windowWith=window.getContentView().getMeasuredWidth();
//        int xWith = widgetwith / 2 - windowWith / 2;
//        int xWith = -widgetwith;
        window.showAsDropDown(view,0,0);
    }

    class MyAdapter extends BaseAdapter {

        private Context context;
        private List<String> mDatas;

        public MyAdapter(Context context, List<String> datas) {
            this.context = context;
            if (datas == null) {
                datas = new ArrayList<>();
            }
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tvItem;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_popupwindow, null);
                tvItem = (TextView) convertView.findViewById(R.id.tv_popupwindow);
                convertView.setTag(tvItem);
            } else {
                tvItem = (TextView) convertView.getTag();
            }
            tvItem.setText(getItem(position) + "");
            return convertView;
        }
    }

}
