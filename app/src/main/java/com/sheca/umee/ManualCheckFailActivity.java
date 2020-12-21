package com.sheca.umee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ManualCheckFailActivity extends Activity {
	private String  mStrBizSN = "";        //流水号
	private ProgressDialog progDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_manual_check_fail);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("审核结果");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		Intent intent = getIntent();
		if(null != intent.getExtras()){ 
		     if(null != intent.getExtras().getString("BizSN")){
		    	   mStrBizSN = intent.getExtras().getString("BizSN");
		     }
		}
		
		((TextView) findViewById(R.id.auth_result_description_label)).setVisibility(RelativeLayout.GONE);
		((ImageView) findViewById(R.id.btn_ok)).setVisibility(RelativeLayout.GONE);
		
		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ManualCheckFailActivity.this.finish();
			}
		});
		
		((ImageView) findViewById(R.id.btn_ok)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ManualCheckFailActivity.this.finish();
			}
		});

		getManualCheckFailedReason();
	}
	
	private  void  getManualCheckFailedReason(){
		 showProgDlg("获取人工审核结果中...");
		 
		 final Handler handler = new Handler(ManualCheckFailActivity.this.getMainLooper());
		
		 new Thread(new Runnable(){
             @Override
                public void run() {
        	        try {
		                //异步调用UMSP服务：获取短信验证码
		                final String timeout = ManualCheckFailActivity.this.getString(R.string.WebService_Timeout);				
		                final String urlPath = ManualCheckFailActivity.this.getString(R.string.UMSP_Service_GetManualCheckFailedReason);

		                Map<String,String> postParams = new HashMap<String,String>();
		                postParams.put("bizSN", mStrBizSN);
		               // final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
		                
		                String postParam = "bizSN="+URLEncoder.encode(mStrBizSN, "UTF-8");
                        final  String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		
		                final JSONObject jb = JSONObject.fromObject(responseStr);
			            final String resultStr = jb.getString(CommonConst.RETURN_CODE);
			            final String returnStr = jb.getString(CommonConst.RETURN_MSG);
			
		                if (resultStr.equals("0")){ 	
		                	handler.post(new Runnable() {
								 @Override
									public void run() {
									   closeProgDlg();
									   JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
									   
									   ((TextView) findViewById(R.id.text_about)).setText(jbRet.getString(CommonConst.RESULT_PARAM_REASON));
									   ((TextView) findViewById(R.id.auth_result_description_label)).setVisibility(RelativeLayout.VISIBLE);
									   ((ImageView) findViewById(R.id.btn_ok)).setVisibility(RelativeLayout.VISIBLE);
									}
							}); 		
		                }else{
		                	throw new Exception(returnStr);
		                }
		
	                } catch (final Exception exc) {
		                 handler.post(new Runnable() {
							 @Override
								public void run() {
								   closeProgDlg();
								   Toast.makeText(ManualCheckFailActivity.this, exc.getMessage()+"", Toast.LENGTH_SHORT).show();
								}
						}); 
	                }
               }
             }).start();

	}

	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(ManualCheckFailActivity.this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			ManualCheckFailActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	
}
