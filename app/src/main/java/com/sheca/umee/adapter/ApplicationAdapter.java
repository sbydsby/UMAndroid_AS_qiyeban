package com.sheca.umee.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheca.umee.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationAdapter extends BaseAdapter {

    private Activity activity;
    private List<Map<String, String>> data;
    private static LayoutInflater inflater = null;

    public ApplicationAdapter(Activity a, List<Map<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View vi, ViewGroup parent) {

        ViewHolder holder;
        if (vi == null) {
            holder = new ViewHolder();
            vi = inflater.inflate(R.layout.applicationitem,null);
            holder.itemTitle = (TextView) vi.findViewById(R.id.tv_CommonName);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }


		ImageView itemListImage = (ImageView) vi.findViewById(R.id.list_image);
//        TextView itemTitle = (TextView) vi.findViewById(R.id.tv_CommonName);
//		TextView certType = (TextView) vi.findViewById(R.id.certtype);
//		TextView itemDesc = (TextView) vi.findViewById(R.id.tv_ApplyTime);
        //TextView orgName = (TextView) vi.findViewById(R.id.organization);
//		ImageView itemListCertType = (ImageView) vi.findViewById(R.id.list_image2);
//		TextView itemImage = (TextView) vi.findViewById(R.id.iv_download);
        Map<String, String> certmap = new HashMap<String, String>();

        if (getCount() > 0) {
            certmap = data.get(position);
            // Setting all values in listview
            holder.itemTitle.setText(certmap.get("CommonName"));
//			itemDesc.setText(certmap.get("ApplyTime"));
//			itemImage.setImageResource(Integer.valueOf(certmap.get("Download")));
			itemListImage.setImageResource(Integer.valueOf(certmap.get("ListImage")));

            //if("2".equals(certmap.get("ActType")))
            //orgName.setText("企业移动证书");
            //else
            //orgName.setText("个人移动证书");

//			if(CommonConst.CERT_TYPE_SM2.equals(certmap.get("CertType")) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(certmap.get("CertType"))||certmap.get("CertType").contains("SM2")){
//				certType.setText(CommonConst.CERT_SM2_NAME+"证书");
//				itemListCertType.setImageResource(R.drawable.cert_sm2);
//			}else{
//				certType.setText(CommonConst.CERT_RSA_NAME+"证书");
//				itemListCertType.setImageResource(R.drawable.cert_rsa);
//			}
        }

        //vi.setBackgroundResource(R.drawable.selector_listview);
        return vi;
    }


    class ViewHolder{
        TextView itemTitle;
    }
}