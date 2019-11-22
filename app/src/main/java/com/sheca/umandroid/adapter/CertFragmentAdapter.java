package com.sheca.umandroid.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheca.umandroid.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertFragmentAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public CertFragmentAdapter(Activity a, List<Map<String, String>> d) {
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
		if (convertView == null){
			vi = inflater.inflate(R.layout.certfragment_item, null);}

		TextView itemTitle = (TextView) vi.findViewById(R.id.certfragment_item_title);
		TextView itemDesc = (TextView) vi.findViewById(R.id.certfragment_item_description);
		ImageView itemImage = (ImageView) vi.findViewById(R.id.certfragment_item_image);
		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			// Setting all values in listview
			itemTitle.setText(certmap.get("title"));
			itemDesc.setText(certmap.get("description"));
			itemImage.setImageResource(Integer.valueOf(certmap.get("resourceid")));
		}

		return vi;
	}
}