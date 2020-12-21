package com.sheca.umee.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sheca.umee.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountAppInfoAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public AccountAppInfoAdapter(Activity a, List<Map<String, String>> d) {
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
			vi = inflater.inflate(R.layout.certitem, null);

		TextView organization = (TextView) vi.findViewById(R.id.organization);
		TextView commonname = (TextView) vi.findViewById(R.id.commonname);
		TextView validtime = (TextView) vi.findViewById(R.id.validtime);
		TextView certname = (TextView) vi.findViewById(R.id.certname);
		
		vi.findViewById(R.id.list_image).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.list_image2).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.list_image3).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.validtime).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.list_image4).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.certtype).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.list_image5).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.savetype).setVisibility(RelativeLayout.GONE);
		vi.findViewById(R.id.list_imageCertName).setVisibility(RelativeLayout.GONE);
		certname.setVisibility(RelativeLayout.GONE);
		organization.setVisibility(RelativeLayout.GONE);
		validtime.setVisibility(RelativeLayout.GONE);
		
		commonname.setTextSize(20);
		
		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			
			// Setting all values in listview
			organization.setText(certmap.get("appid"));
			commonname.setText(certmap.get("appname"));
		}

		return vi;
	}
}