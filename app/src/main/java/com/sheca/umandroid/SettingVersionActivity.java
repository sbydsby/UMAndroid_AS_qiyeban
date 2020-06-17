package com.sheca.umandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import com.sheca.umandroid.util.CommUtil;

public class SettingVersionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_setting_version);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("版本信息");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingVersionActivity.this.finish();
			}
		});

		((TextView) findViewById(R.id.txtView1)).setTypeface(typeFace);

		String ver = CommUtil.formatString(this,R.string.version,CommUtil.getVerName(this));
		((TextView) findViewById(R.id.txtView1)).setText(ver);

		ImageView ifaaTest = (ImageView) this
				.findViewById(R.id.logo);

		ifaaTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent i = new Intent(SettingVersionActivity.this, com.esandinfo.IfaaRxjavaDemoActivity.class);
//				startActivity(i);
//				SettingVersionActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SettingVersionActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

}
