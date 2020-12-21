package com.sheca.umee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sheca.umee.util.CommonConst;
import com.suke.widget.SwitchButton;

public class SettingLogUploadTypeActivity extends Activity{

	private SharedPreferences sharedPrefs;
	private boolean  m_log = true;

	private SwitchButton switchButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_setting_log_upload_type);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("使用记录");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingLogUploadTypeActivity.this.finish();
			}
		});


		//查看记录
		findViewById(R.id.logs_see).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SettingLogUploadTypeActivity.this, LogsActivity.class);
				startActivity(i);
			}
		});

		switchButton = (SwitchButton)findViewById(R.id.switch_button);
		
		Intent intent = getIntent();	
		if(null != intent.getExtras())
			m_log = intent.getExtras().getBoolean("logType");

		// 改变默认选项  
		switchButton.setChecked(m_log);

		switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(SwitchButton view, boolean isChecked) {
				m_log = isChecked;
			}
		});

	   findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view){
				Editor editor = sharedPrefs.edit();
				if (m_log) {
					editor.putBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, true);	
					DaoActivity.bUploadRecord = true;
				} else {
					editor.putBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, false);
					DaoActivity.bUploadRecord = false;
				}
				
				editor.commit();
				SettingLogUploadTypeActivity.this.finish();
			}
		});	
	}

	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SettingLogUploadTypeActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	
}
