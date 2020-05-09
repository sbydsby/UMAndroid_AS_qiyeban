package com.sheca.umandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class NetworkSignActivity extends Activity {
	private WebView mWebView = null;  
	private ProgressDialog progDialog = null;
	
	private Handler handler = null;	
	
	//private final String NETWORK_ONLINE = "http://202.96.220.166/UMAPI/jSignature/jSignature.html";
	private final String NETWORK_ONLINE = "https://umsp.sheca.com/UMSPService/v2/jSignature/jSignature.html";
	
	//"http://weixin.sheca.com/Home~Consult~dotSearch?menuIn=1";
	
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		setContentView(R.layout.activity_network_online);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制竖屏
		
		handler = new Handler(NetworkSignActivity.this.getMainLooper());
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

	    	//mWebView.loadUrl(NETWORK_ONLINE_TEST);  //测试demo地址

	        WebSettings webSettings = mWebView.getSettings();
	        webSettings.setJavaScriptEnabled(true);//打开js支持
	        /**
	           * 打开js接口給H5调用，参数1为本地类名，参数2为别名；h5用window.别名.类名里的方法名才能调用方法里面的内容，例如：window.android.back();
	        * */
	        mWebView.addJavascriptInterface(new JsInteration(), "android");
	        mWebView.setWebViewClient(new WebViewClient());
	        mWebView.setWebChromeClient(new WebChromeClient());
	  		
	    } 
		
	    /*
	    ViewGroup decorView = (ViewGroup) this.getWindow().getDecorView();
	    WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
	    WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) decorView.getLayoutParams();
	    //旋转
//	        layoutParams.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
	    //淡入淡出
//	        layoutParams.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
	    //无动画
//	        layoutParams.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT;
	    //无缝旋转模式
	    layoutParams.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT;
	    windowManager.updateViewLayout(decorView, layoutParams);
	    */
	}

	@Override
	protected void onResume() {
	 /**
	  * 设置为横屏
	  */
	    if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	 
	    super.onResume();
	}
	
	@Override 
    public void onConfigurationChanged(Configuration newConfig)
    { 
        super.onConfigurationChanged(newConfig); 
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //land
        }else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
           //port
        }
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			NetworkSignActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private  void  loadNetworkOnline(){	
		 if(null != mWebView){
			//showProgDlg("正在加载数据中...");
		    mWebView.loadUrl(NETWORK_ONLINE);
		   // mWebView.reload();
		 }  	
	}
	
	/**
     * 自己写一个类，里面是提供给H5访问的方法
     * */
    public class JsInteration {

        @JavascriptInterface//一定要写，不然H5调不到这个方法
        public String back(String base64PNG) {
        	base64PNG = base64PNG.split(",")[1];
        	base64PNG = base64PNG.substring(1);
        	if(base64PNG.endsWith("]"))
        		base64PNG = base64PNG.substring(0,base64PNG.length()-1);
        	if(base64PNG.endsWith("\""))
        		base64PNG = base64PNG.substring(0,base64PNG.length()-1);
        	
        	final String strPic = base64PNG;
        	
        	handler.post(new Runnable() {
               @Override
               public void run() {
        	        showProgDlg("界面加载中...");
            	
        	         try{
        	        	 //Toast.makeText(NetworkSignActivity.this, strPic,Toast.LENGTH_SHORT).show(); 
        	             //mWebView.loadUrl("file:///android_asset/test1.html?a="+strPic);//加载本地asset下面的js_java_interaction.html文件
        	             showSealPreview(strPic);
        	             closeProgDlg();
        	         }catch(Exception ex){
        		        String strErr = ex.getMessage();
        		        strErr += "\n"+ex.getLocalizedMessage();
        	         }
        	         
        	         
               }
			}); 
        	
            return "我是java里的方法返回值\npng:"+base64PNG;
        }
    }
    
    
    private  void showSealPreview(String strPic){
    	Intent intent = new Intent(NetworkSignActivity.this, SealPreviewActivity.class);
        intent.putExtra("SealPic", strPic);
        startActivity(intent);
        NetworkSignActivity.this.finish();
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
