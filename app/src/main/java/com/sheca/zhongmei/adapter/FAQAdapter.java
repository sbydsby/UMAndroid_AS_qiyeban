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

public class FAQAdapter extends BaseAdapter {

	private Activity activity;
	private List<Map<String, String>> data;
	private static LayoutInflater inflater = null;

	public FAQAdapter(Activity a, List<Map<String, String>> d) {
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
			vi = inflater.inflate(R.layout.faqselect_item, null);

		TextView questionText = (TextView) vi.findViewById(R.id.question);

		Map<String, String> certmap = new HashMap<String, String>();

		if (data.size() > 0) {
			certmap = data.get(position);

			// Setting all values in listview
			questionText.setText(toDBC(certmap.get("question")));
		}

		return vi;
	}

	private String toDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}
}