package com.sheca.umee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HotlineActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.hotphone);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		((TextView) findViewById(R.id.header_text)).setText("热线电话");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HotlineActivity.this.finish();
			}
		});
		//Toast.makeText(HotlineActivity.this, phoneNum.replace("-", ""), Toast.LENGTH_LONG).show();
		TextView textTeleNum = (TextView) this.findViewById(R.id.tvtelenum);
		textTeleNum.setVisibility(RelativeLayout.GONE);
		final String phoneNum = textTeleNum.getText().toString();
		
		ImageView teleImageView = (ImageView) this
				.findViewById(R.id.image_phonecall);
		teleImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ phoneNum.replace("-", "")));
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			HotlineActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
}
