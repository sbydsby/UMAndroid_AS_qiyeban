package com.sheca.umee.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sheca.umee.R;
import com.sheca.umee.util.CommonConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public CertAdapter(Activity a, List<Map<String, String>> d) {
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
		TextView certtype = (TextView) vi.findViewById(R.id.certtype);
		TextView savetype = (TextView) vi.findViewById(R.id.savetype);
		TextView certname = (TextView) vi.findViewById(R.id.certname);

		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);
			
			// Setting all values in listview
			organization.setText(certmap.get("organization"));
			//commonname.setText(certmap.get("certname"));
			commonname.setText(certmap.get("commonname"));
			validtime.setText(certmap.get("validtime"));
			certtype.setText(certmap.get("certtype"));
			certname.setText(certmap.get("certname"));
			
			if(CommonConst.SAVE_CERT_TYPE_PHONE == Integer.parseInt(certmap.get("savetype")))
				savetype.setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
			else if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == Integer.parseInt(certmap.get("savetype")))
				savetype.setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);	
			else if(CommonConst.SAVE_CERT_TYPE_AUDIO == Integer.parseInt(certmap.get("savetype")))
				savetype.setText(CommonConst.SAVE_CERT_TYPE_AUDIO_NAME);	
			else if(CommonConst.SAVE_CERT_TYPE_SIM == Integer.parseInt(certmap.get("savetype")))
				savetype.setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);	
		}

		return vi;
	}
}