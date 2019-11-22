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
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class FAQActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.faq);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		Intent intent = getIntent();

		String sTitle = intent.getStringExtra("title");
		String sQuestion = intent.getStringExtra("question");
		String sAnswer = intent.getStringExtra("answer");
		String sResourceid = intent.getStringExtra("resourceid");
		
		((TextView) findViewById(R.id.header_text)).setText("问题解答");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		TextView textQuestion = (TextView) findViewById(R.id.tvquestion);
		TextView textAnswer = (TextView) findViewById(R.id.tvanswer);
//		TableRow trImage = (TableRow) findViewById(R.id.trimage);
		ImageView ivImage = (ImageView) findViewById(R.id.tvimage);

		textQuestion.setText(toDBC(sQuestion));
		textAnswer.setText(toDBC(sAnswer));
		
		if ("".equals(sResourceid)) {
			ivImage.setVisibility(TableRow.GONE);
		}else {
			ivImage.setVisibility(TableRow.VISIBLE);
			ivImage.setImageResource(Integer.valueOf(sResourceid));
		}

		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FAQActivity.this.finish();
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			FAQActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
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
