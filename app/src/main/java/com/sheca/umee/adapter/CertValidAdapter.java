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

public class CertValidAdapter extends BaseAdapter {

    private Activity activity;
    private List<Map<String, String>> data;
    private static LayoutInflater inflater = null;

    private int selectItem = -1;

    public CertValidAdapter(Activity a, List<Map<String, String>> d) {
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
            vi = inflater.inflate(R.layout.certvaliditem, null);

        TextView validname = (TextView) vi.findViewById(R.id.validname);
        TextView validdesc = (TextView) vi.findViewById(R.id.validdesc);

        Map<String, String> certmap = new HashMap<String, String>();

        if (data.size() > 0) {
            certmap = data.get(position);

            // Setting all values in listview
            validname.setText(certmap.get("certvalidname"));
            validdesc.setText(certmap.get("certvaliddesc"));

            if (selectItem == -1) {
                if (position == 1) {
//				   vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#faefdb"));
                    vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.VISIBLE);
                } else {
//				   vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#ffffff"));
                    vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.GONE);
                }
            } else {
                if (position == selectItem) {
//					vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#faefdb"));
                    vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.VISIBLE);
                } else {
//					vi.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#ffffff"));
                    vi.findViewById(R.id.thumbnail2).setVisibility(RelativeLayout.GONE);
                }
            }
        }

        if (position == 0) {
            vi.setVisibility(View.GONE);
        } else {
            vi.setVisibility(View.VISIBLE);
        }

        return vi;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }
}