package com.sheca.zhongmei.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sheca.zhongmei.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BTDeviceAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public BTDeviceAdapter(Activity a, List<Map<String, String>> d) {
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
			vi = inflater.inflate(R.layout.btdeviceitem, null);

		TextView commonname = (TextView) vi.findViewById(R.id.commonname);	
		commonname.setTextSize(20);
		
		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			
			// Setting all values in listview
			commonname.setText(certmap.get("commonname"));
		
		}

		return vi;
	}
}