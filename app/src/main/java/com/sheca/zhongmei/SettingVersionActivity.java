package com.sheca.zhongmei;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import com.sheca.zhongmei.util.CommUtil;

public class SettingVersionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_setting_version);

		//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//设置状态栏颜色
		getWindow().setStatusBarColor(getResources().getColor(R.color.bg_theme_deep));
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
