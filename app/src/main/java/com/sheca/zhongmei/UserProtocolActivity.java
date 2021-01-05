package com.sheca.zhongmei;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sheca.zhongmei.util.CommonConst;

public class UserProtocolActivity extends Activity {

    private WebView mWebViewAgreement;
    private String mURL;
    private ImageButton btGoback;

    int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.about);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        initView();
    }


    private void initView() {
        btGoback = (ImageButton) findViewById(R.id.btn_goback);
        mWebViewAgreement = (WebView) findViewById(R.id.webView_agreement);

        type = getIntent().getIntExtra("type", 0);
        mURL = (type == 0) ?  CommonConst.PROTOCOL_SERVER:CommonConst.PRIVACY_POLICY_SERVER ;
        ((TextView) findViewById(R.id.header_text)).setText((type == 0) ?"用户协议":"隐私政策");

        WebSettings webSettings = mWebViewAgreement.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);  //将图片调整到适合webView的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setBuiltInZoomControls(true);//设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setDomStorageEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebViewAgreement.loadUrl(mURL);
        mWebViewAgreement.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebViewAgreement.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                mWebViewAgreement.loadUrl(mURL);
                return true;
            }
        });
        btGoback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebViewAgreement != null) {
            mWebViewAgreement.stopLoading();
            mWebViewAgreement.clearHistory();
            mWebViewAgreement.removeAllViews();
            mWebViewAgreement.destroy();
        }
    }


}
