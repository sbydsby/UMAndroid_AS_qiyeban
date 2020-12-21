package com.sheca.umee;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.model.Account;
import com.sheca.umee.util.CommonConst;

public class SettingCertTypeActivity extends Activity {
	private RadioGroup group_temo;  
    private RadioButton checkRadioButton;  
    
    private int  m_certtype = CommonConst.SAVE_CERT_TYPE_SM2;
    private  AccountDao accountDao = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_setting_cert_type);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("证书类别 ");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		accountDao = new AccountDao(SettingCertTypeActivity.this);

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingCertTypeActivity.this.finish();
			}
		});
		
		Intent intent = getIntent();	
		if(null != intent.getExtras())
		   m_certtype = intent.getExtras().getInt("certType");
		
		group_temo = (RadioGroup) findViewById(R.id.radioGroup1);   
		// 改变默认选项  
		
		if(CommonConst.SAVE_CERT_TYPE_RSA == accountDao.getLoginAccount().getCertType())
	        group_temo.check(R.id.radio0);  
		else
			group_temo.check(R.id.radio1);  
	   
	    // 获取默认被被选中值  
	    checkRadioButton = (RadioButton) group_temo.findViewById(group_temo.getCheckedRadioButtonId());  

	   // 注册事件  
	   group_temo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
	          @Override  
	          public void onCheckedChanged(RadioGroup group, int checkedId) {  	  
	             // 点击事件获取的选择对象  
	             checkRadioButton = (RadioButton) group_temo.findViewById(checkedId);  
	             if(checkedId == R.id.radio0)
	            	 m_certtype = CommonConst.SAVE_CERT_TYPE_RSA;
	             else
	            	 m_certtype = CommonConst.SAVE_CERT_TYPE_SM2;
	          }  
	   });  

	   ImageView mRegActBtn = (ImageView) findViewById(R.id.edit);  
		mRegActBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Account  act = accountDao.getLoginAccount();
				act.setCertType(m_certtype);
				accountDao.update(act);
				
				SettingCertTypeActivity.this.finish();
			}
		});
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SettingCertTypeActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

}
