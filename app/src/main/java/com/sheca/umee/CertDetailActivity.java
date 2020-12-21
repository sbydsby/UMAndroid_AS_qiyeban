package com.sheca.umee;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sheca.javasafeengine;
import com.sheca.umee.dao.CertDao;
import com.sheca.umee.fragment.CertEncFragment;
import com.sheca.umee.fragment.CertSignFragment;
import com.sheca.umee.model.Cert;

public class CertDetailActivity extends FragmentActivity {
	private CertDao certDao = null;
	private javasafeengine jse = null;
	private int certID = 0;
	private Cert mCert = null;	
	private int index = 1;  
    // fragment管理类  
    private FragmentManager fm;  
    // 三个fragment  
    private Fragment signFragment, encFragment;  
    private LinearLayout signLayout,encLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_cert_tab_view);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("证书详情");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CertDetailActivity.this.finish();
			}
		});

		certDao = new CertDao(this);
		jse = new javasafeengine();
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("CertId")!=null){
				certID = Integer.parseInt(extras.getString("CertId"));
				mCert = certDao.getCertByID(certID);	
			}
		}
		
        fm = getSupportFragmentManager();
		initView();
		//viewCert();
	}
	
	
	 /** 
     * 初始化控件 
     */  
    private void initView() {  
    	signLayout = (LinearLayout) findViewById(R.id.cert_view_sign);  
    	encLayout = (LinearLayout) findViewById(R.id.cert_view_enc);  
    	
    	signLayout.setOnClickListener(new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
				  showSignCert();	
			  }		
		});
  	
    	encLayout.setOnClickListener(new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
				  showEncCert();	
			  }		
		});
    	
//    	if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype())){
//    		if( null != mCert.getEnccertificate() && !"".equals(mCert.getEnccertificate()))
//    		    findViewById(R.id.cert_tab).setVisibility(RelativeLayout.VISIBLE);
//    		else
//    			findViewById(R.id.cert_tab).setVisibility(RelativeLayout.GONE);
//    	}else{
//    		findViewById(R.id.cert_tab).setVisibility(RelativeLayout.GONE);
//    	}


		    	if(mCert.getCerttype().contains("SM2")){
    		if( null != mCert.getEnccertificate() && !"".equals(mCert.getEnccertificate()))
    		    findViewById(R.id.cert_tab).setVisibility(RelativeLayout.VISIBLE);
    		else
    			findViewById(R.id.cert_tab).setVisibility(RelativeLayout.GONE);
    	}else{
    		findViewById(R.id.cert_tab).setVisibility(RelativeLayout.GONE);
    	}
    	
        setDefaultFragment();  
    }
    
    /** 
     * 设置默认显示的fragment 
     */  
	private void setDefaultFragment() {  
        FragmentTransaction transaction = fm.beginTransaction();  
        signFragment = new CertSignFragment(certID);  
        transaction.replace(R.id.certcontent, signFragment);  
        transaction.commit(); 
        
        findViewById(R.id.cert_view_sign).setBackground(getBaseContext().getResources().getDrawable(R.drawable.tab_sign_cert_over));
    }  
  
    /** 
     *切换fragment 
     * @param newFragment 
     */  
    private void replaceFragment(Fragment newFragment) {  
        FragmentTransaction transaction = fm.beginTransaction();  
        if (!newFragment.isAdded()) {  
            transaction.replace(R.id.certcontent, newFragment);  
            transaction.commit();  
        } else {  
            transaction.show(newFragment);  
        }  
    }  
    
    
	private void  showSignCert(){
    	 if (signFragment == null) {  
        	 signFragment = new CertSignFragment(certID);  
         }  
         
         replaceFragment(signFragment);  
         index = 1;  
         
         findViewById(R.id.cert_view_sign).setBackground(getBaseContext().getResources().getDrawable(R.drawable.tab_sign_cert_over));
         findViewById(R.id.cert_view_enc).setBackground(getBaseContext().getResources().getDrawable(R.drawable.tab_enc_cert));
	}
    
   
	private void  showEncCert(){
    	 if (encFragment == null) {  
        	 encFragment = new CertEncFragment(certID);  
         }  
         
         replaceFragment(encFragment);  
         index = 2;  
         
         findViewById(R.id.cert_view_sign).setBackground(getBaseContext().getResources().getDrawable(R.drawable.tab_sign_cert));
         findViewById(R.id.cert_view_enc).setBackground(getBaseContext().getResources().getDrawable(R.drawable.tab_enc_cert_over));
   }


}
