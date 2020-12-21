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

public class SettingFragmentAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public SettingFragmentAdapter(Activity a, List<Map<String, String>> d) {
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
			vi = inflater.inflate(R.layout.settingfragment_item, null);

		ImageView itemImage = (ImageView) vi.findViewById(R.id.iv_service_itemimage);
		TextView itemTitle = (TextView) vi.findViewById(R.id.tv_service_itemtitle);
		TextView itemDesc = (TextView) vi.findViewById(R.id.servicefragment_item_description);

		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			// Setting all values in listview
			itemImage.setImageResource(Integer.valueOf(certmap.get("resourceid")));
			itemTitle.setText(certmap.get("title"));
			itemDesc.setText(certmap.get("description"));
		}

		return vi;
	}
}