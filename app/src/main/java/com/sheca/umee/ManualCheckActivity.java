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

import com.sheca.umee.dao.AccountDao;
import com.sheca.umee.util.CommonConst;
import com.sheca.umee.util.WebClientUtil;

import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ManualCheckActivity extends Activity {
	private String  mStrBizSN = "";        //流水号
	private ProgressDialog progDialog = null;
	private boolean mIsDao = false;     //第三方接口调用标记
	private boolean mIsManualed = false;     //第三方接口调用标记
	
	private AccountDao mAccountDao = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_manual_check);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("人工审核");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		Intent intent = getIntent();
		if(null != intent.getExtras()){ 
		     if(null != intent.getExtras().getString("BizSN")){
		    	    mStrBizSN = intent.getExtras().getString("BizSN");
		     }
		     if(null != intent.getExtras().getString("message")){
					mIsDao = true;		
			 }
		     if(null != intent.getExtras().getString("Manunal")){
		    	    mIsManualed = true;		
			 }
		}
		
		
		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);
		cancelScanButton.setVisibility(RelativeLayout.GONE);
		
		mAccountDao = new AccountDao(ManualCheckActivity.this);
		
		if(mIsManualed){
			((ImageView) findViewById(R.id.manual_check_tip1)).setVisibility(RelativeLayout.GONE);
			((ImageView) findViewById(R.id.manual_check_tip_ok)).setVisibility(RelativeLayout.VISIBLE);
			((ImageView) findViewById(R.id.btn_manual_check)).setVisibility(RelativeLayout.GONE);
			((ImageView) findViewById(R.id.btn_manual_check_ok)).setVisibility(RelativeLayout.VISIBLE);
			
			((ImageView) this.findViewById(R.id.btn_manual_check_ok)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mIsDao)
						DaoActivity.bManualChecked = true;
					ManualCheckActivity.this.finish();
				}
			});
		}else{		
			((ImageView) findViewById(R.id.manual_check_tip1)).setVisibility(RelativeLayout.VISIBLE);
			((ImageView) findViewById(R.id.manual_check_tip_ok)).setVisibility(RelativeLayout.GONE);
			((ImageView) findViewById(R.id.btn_manual_check)).setVisibility(RelativeLayout.VISIBLE);
			((ImageView) findViewById(R.id.btn_manual_check_ok)).setVisibility(RelativeLayout.GONE);
			
		    ((ImageView) this.findViewById(R.id.btn_manual_check)).setOnClickListener(new OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   submitManualCheckRequest();
			   }
		    });
	    }
	}
	
	
	private  void  submitManualCheckRequest(){  
	    final Handler handler = new Handler(ManualCheckActivity.this.getMainLooper());
	    showProgDlg("提交人工审核中...");
		    	   
		          new Thread(new Runnable(){
                  @Override
                     public void run() {
             	        try {
			                //异步调用UMSP服务：获取短信验证码
			                final String timeout = ManualCheckActivity.this.getString(R.string.WebService_Timeout);				
			                final String urlPath = ManualCheckActivity.this.getString(R.string.UMSP_Service_SubmitManualCheckRequest);

			                Map<String,String> postParams = new HashMap<String,String>();
			                postParams.put("bizSN", mStrBizSN);
			                if(CommonConst.SAVE_CERT_TYPE_RSA == mAccountDao.getLoginAccount().getCertType())
			                    postParams.put("certType", CommonConst.CERT_TYPE_RSA);
			                else
			                	postParams.put("certType", CommonConst.CERT_TYPE_SM2);
 			                postParams.put("validity", CommonConst.CERT_TYPE_SM2_VALIDITY+"");
			                //final String responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			                
 			                String postParam = "";
 			                if(CommonConst.SAVE_CERT_TYPE_RSA == mAccountDao.getLoginAccount().getCertType())
 								postParam = "bizSN="+URLEncoder.encode(mStrBizSN, "UTF-8")+
 									        "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_RSA, "UTF-8")+
 									        "&Source="+URLEncoder.encode("1", "UTF-8")+
 							                "&validity="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2_VALIDITY+"", "UTF-8");
 							else
 								postParam = "bizSN="+URLEncoder.encode(mStrBizSN, "UTF-8")+
							                "&certType="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2, "UTF-8")+
							                "&Source="+URLEncoder.encode("1", "UTF-8")+
					                        "&validity="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2_VALIDITY+"", "UTF-8");
 							
 							final String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			
			                JSONObject jb = JSONObject.fromObject(responseStr);
 			                final String resultStr = jb.getString(CommonConst.RETURN_CODE);
 			                final String returnStr = jb.getString(CommonConst.RETURN_MSG);
				
			                if (resultStr.equals("0")){ 	
			                	handler.post(new Runnable() {
									 @Override
										public void run() {
										   closeProgDlg();
										   Toast.makeText(ManualCheckActivity.this, "人工审核已提交", Toast.LENGTH_SHORT).show();    
										   if(mIsDao)
											   DaoActivity.bManualChecked = true;
										   ManualCheckActivity.this.finish();
										}
								}); 		
			                }else{
			                	handler.post(new Runnable() {
									 @Override
										public void run() {
			                	           closeProgDlg();
									    }
								}); 	
			                	
			                	throw new Exception(returnStr);
			                }
			
		                } catch (final Exception exc) {
			                 handler.post(new Runnable() {
								 @Override
									public void run() {
									   closeProgDlg();
									   Toast.makeText(ManualCheckActivity.this, exc.getMessage()+"", Toast.LENGTH_SHORT).show();
									}
							}); 
		                }
	                }
	              }).start();

	}

	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(ManualCheckActivity.this);
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
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	
}
