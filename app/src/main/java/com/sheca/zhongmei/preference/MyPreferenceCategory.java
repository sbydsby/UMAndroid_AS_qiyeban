package com.sheca.zhongmei.preference;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyPreferenceCategory extends PreferenceCategory {

	public MyPreferenceCategory(Context context, AttributeSet attrs) {

		super(context, attrs);

	}

	@Override
	protected void onBindView(View view) {

		super.onBindView(view);

		view.setPadding(30, 30, 30, 30);
		view.setBackgroundColor(Color.parseColor("#E3E3E3"));

		if (view instanceof TextView) {

			TextView tv = (TextView) view;

			tv.setTextSize(23);

			tv.setTextColor(Color.parseColor("#828282"));

		}

	}
}