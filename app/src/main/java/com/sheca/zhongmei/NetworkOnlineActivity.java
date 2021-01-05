package com.sheca.zhongmei;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

public class NetworkOnlineActivity extends Activity {
	private WebView mWebView = null;  
	private ProgressDialog progDialog = null;
	
	private final String NETWORK_ONLINE = "http://wx.sheca.com/admin/home/siteSearch";
	//"http://weixin.sheca.com/Home~Consult~dotSearch?menuIn=1";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_network_online);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("服务网点");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);
		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NetworkOnlineActivity.this.finish();
			}
		});
		
		mWebView = (WebView) findViewById(R.id.webView); 
		mWebView.getSettings().setJavaScriptEnabled(true);
		
	    if(mWebView != null) { 
	    	mWebView.setWebViewClient(new WebViewClient() { 
	                @Override 
	                public void onPageFinished(WebView view,String url) 
	                { 
	                	closeProgDlg(); 
	                } 
	         }); 
	             
	    	loadNetworkOnline();
	    } 
		
			
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			NetworkOnlineActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private  void  loadNetworkOnline(){	
		 if(null != mWebView){
			showProgDlg("正在加载数据中...");
		    mWebView.loadUrl(NETWORK_ONLINE);
		    mWebView.reload();
		 }  	
	}
	
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		//progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	
	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

}
