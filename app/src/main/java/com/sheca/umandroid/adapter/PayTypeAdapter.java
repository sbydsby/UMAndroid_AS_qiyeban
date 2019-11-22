package com.sheca.umandroid.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sheca.umandroid.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayTypeAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;
	
	private int selectItem=-1;

	public PayTypeAdapter(Activity a, List<Map<String, String>> d) {
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

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.paytypeitem, null);

		ImageView itempaytypepic = (ImageView) vi.findViewById(R.id.paytypepic);
		ImageView itempaytypeext = (ImageView) vi.findViewById(R.id.paytypeext);

		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);

			// Setting all values in listview
			if(selectItem == -1){
			   if(position == 0){
				   itempaytypepic.setImageResource(Integer.valueOf(certmap.get("paytypepic")));
				   itempaytypeext.setImageResource(Integer.valueOf(certmap.get("paytypeext")));
				   itempaytypeext.setVisibility(RelativeLayout.VISIBLE);
				
//				   vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#faefdb"));
				   vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.VISIBLE);	
			   }else{
				   itempaytypepic.setImageResource(Integer.valueOf(certmap.get("paytypepic")));
				   itempaytypeext.setVisibility(RelativeLayout.GONE);
		
//				   vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#ffffff"));
				   vi.findViewById(R.id.layout).setVisibility(RelativeLayout.GONE);	
				   vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.GONE);	
			   }
			}else{
				if(position == selectItem){
					itempaytypepic.setImageResource(Integer.valueOf(certmap.get("paytypepic")));
					if("".equals(certmap.get("paytypeext"))){
						itempaytypeext.setVisibility(RelativeLayout.GONE);
					}else{
					   itempaytypeext.setImageResource(Integer.valueOf(certmap.get("paytypeext")));
					   itempaytypeext.setVisibility(RelativeLayout.VISIBLE);
					}
					
				    vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#faefdb"));	
				    vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.VISIBLE);	
				}else{
					itempaytypepic.setImageResource(Integer.valueOf(certmap.get("paytypepic")));
					if("".equals(certmap.get("paytypeext"))){
						itempaytypeext.setVisibility(RelativeLayout.GONE);
					}else{
						itempaytypeext.setImageResource(Integer.valueOf(certmap.get("paytypeext")));
					    itempaytypeext.setVisibility(RelativeLayout.VISIBLE);
					}
			
				    vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#ffffff"));	
					vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.GONE);	
				}
			}
			
		}

		return vi;
	}
	
	
	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
	}
}