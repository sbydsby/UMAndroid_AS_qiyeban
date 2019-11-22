package com.sheca.umandroid.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.junyufr.szt.util.Base64ImgUtil;
import com.sheca.umandroid.LoginActivity;
import com.sheca.umandroid.MySettingsActivityNew;
import com.sheca.umandroid.PasswordActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.util.CommonConst;

public class SettingsFragmentNew extends Fragment {
	private View view = null;
	private Context context = null;
	private Activity activity = null;
	
	private SharedPreferences sharedPrefs;	
	private ProgressDialog progDialog = null;	
	private View temp;  
	
	private AccountDao accountDao = null;
	
	private TextView mAccountView;
	private String   mStrAccount = "";
	private String   mActIndenyityCode = "";
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activity = getActivity();
		view = inflater.inflate(R.layout.context_settings1, container, false);
		context = view.getContext();
		
		accountDao = new AccountDao(activity);
		
		//view = inflater.inflate(R.layout.context_settings1, container, false);
		sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
		ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
		TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
		tv_title.setText("我");
		
		iv_unitrust.setVisibility(ImageButton.GONE);
		ib_account.setVisibility(ImageView.GONE);
		tv_title.setVisibility(TextView.VISIBLE);
		
		if(accountDao.count() == 0){
			clearAccountInfo();
			Intent intent = new Intent(context, LoginActivity.class);													
			startActivity(intent);	
			//activity.finish();
			return view;
		}

		showAccountInfo();

		return view;
	}
	
	@Override  
	public void onResume() {  
	    // TODO Auto-generated method stub  
		super.onResume();  
		/*
		if(accountDao.count() == 0){
			clearAccountInfo();
			Toast.makeText(context, "SettingsFragment onResume", Toast.LENGTH_SHORT).show();
			//Intent intent = new Intent(context, MainActivity.class);													
			//startActivity(intent);	
			
			return;
		}else{		
		    showAccountInfo();
		}
*/
	}  
	
	private  void  showAccountInfo(){
		view.findViewById(R.id.Layout_no_login).setVisibility(RelativeLayout.GONE);
		view.findViewById(R.id.Layout_login).setVisibility(RelativeLayout.VISIBLE);
		
		view.findViewById(R.id.button_setting).setEnabled(true);
		view.findViewById(R.id.button_password).setEnabled(true);
		
		view.findViewById(R.id.button_setting).setVisibility(RelativeLayout.VISIBLE);
		view.findViewById(R.id.button_password).setVisibility(RelativeLayout.VISIBLE);
		view.findViewById(R.id.list_image).setVisibility(RelativeLayout.GONE);
		
        mStrAccount = accountDao.getLoginAccount().getName();
		
		mAccountView = (TextView) view.findViewById(R.id.organization);
		mAccountView.setText(mStrAccount);
		mAccountView.setVisibility(TextView.VISIBLE);
		
		mActIndenyityCode = accountDao.getLoginAccount().getIdentityCode();
		if(null != mActIndenyityCode){
			if(mActIndenyityCode.length() == 15){
				mActIndenyityCode = mActIndenyityCode.substring(0,3)+"********";
				mActIndenyityCode += accountDao.getLoginAccount().getIdentityCode().substring(11);
			}else if(mActIndenyityCode.length() == 18){
				mActIndenyityCode = mActIndenyityCode.substring(0,3)+"***********";
				mActIndenyityCode += accountDao.getLoginAccount().getIdentityCode().substring(14);
			}
		}else
			mActIndenyityCode = "";
		
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY){  //如果是企业账户
			   ((TextView) view.findViewById(R.id.accnamelabel)).setText("单位名称:");
			   ((TextView) view.findViewById(R.id.accname)).setText(accountDao.getLoginAccount().getOrgName());
			   view.findViewById(R.id.layout3).setVisibility(RelativeLayout.GONE);
			   view.findViewById(R.id.layout4).setVisibility(RelativeLayout.GONE);
			   
			   ((ImageView)view.findViewById(R.id.list_image)).setImageDrawable(view.getResources().getDrawable(R.drawable.my_photo));
			   ((ImageView)view.findViewById(R.id.list_image)).invalidate();
		}else{  //如果是个人账户
			   view.findViewById(R.id.layout3).setVisibility(RelativeLayout.VISIBLE);
			   view.findViewById(R.id.layout4).setVisibility(RelativeLayout.GONE);
			   ((TextView) view.findViewById(R.id.accname)).setText(accountDao.getLoginAccount().getIdentityName());
			   ((TextView) view.findViewById(R.id.accno)).setText(mActIndenyityCode);
			   ((TextView) view.findViewById(R.id.accmobile)).setText(mStrAccount);
			
			   if((!"".equals(accountDao.getLoginAccount().getCopyIDPhoto())) && (null != accountDao.getLoginAccount().getCopyIDPhoto())){
				   Bitmap headMap = Bytes2Bimap(Base64ImgUtil.GenerateImageByte(accountDao.getLoginAccount().getCopyIDPhoto()));
				   ((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(headMap);
				   ((ImageView)view.findViewById(R.id.list_image)).invalidate();
			   }
		}
		
		view.findViewById(R.id.list_image).setVisibility(RelativeLayout.VISIBLE);
		((ImageView)view.findViewById(R.id.list_image)).invalidate();
		
		((Button)view.findViewById(R.id.button_setting)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);													
					startActivity(intent);	
				}else{
				  Intent intent = new Intent(activity, MySettingsActivityNew.class);	
				  startActivity(intent);	
				}
			}
		});
		
		((Button)view.findViewById(R.id.button_password)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);													
					startActivity(intent);	
				}else{
				   Intent intent = new Intent(activity, PasswordActivity.class);	
				   intent.putExtra("Account", accountDao.getLoginAccount().getName());
				   startActivity(intent);	
				}
			}
		});
	}
	
	private void clearAccountInfo(){
		//view.findViewById(R.id.button_setting).setVisibility(RelativeLayout.GONE);
		//view.findViewById(R.id.button_password).setVisibility(RelativeLayout.GONE);
		//view.findViewById(R.id.button_setting).setEnabled(false);
		//view.findViewById(R.id.button_password).setEnabled(false);
		view.findViewById(R.id.Layout_no_login).setVisibility(RelativeLayout.VISIBLE);
		view.findViewById(R.id.Layout_login).setVisibility(RelativeLayout.GONE);
		
		((ImageView)view.findViewById(R.id.list_image)).setImageDrawable(view.getResources().getDrawable(R.drawable.my_photo));
		 ((ImageView)view.findViewById(R.id.list_image)).invalidate();
		view.findViewById(R.id.list_image).setVisibility(RelativeLayout.GONE);
		
		((TextView)view.findViewById(R.id.organization)).setText("");
		((TextView) view.findViewById(R.id.accname)).setText("");
		((TextView) view.findViewById(R.id.accno)).setText("");
		((TextView) view.findViewById(R.id.accmobile)).setText("");
		 
		//((ImageView)view.findViewById(R.id.list_image)).setBackground(view.getResources().getDrawable(R.drawable.my_photo));
		//((ImageView)view.findViewById(R.id.list_image)).invalidate();
		//view.findViewById(R.id.list_image).setVisibility(RelativeLayout.VISIBLE);
		view.findViewById(R.id.Layout_no_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);													
					startActivity(intent);	
				}
			}
		});

		Button button_setting = (Button) view.findViewById(R.id.button_setting);
		button_setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);													
					startActivity(intent);	
				}
			}
		});
		
		Button button_password = (Button) view.findViewById(R.id.button_password);
		button_password.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);													
					startActivity(intent);	
				}
			}
		});
		
	}
	
	private Bitmap Bytes2Bimap(byte[] b) {   
		if (b.length != 0) {  
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}else {            
			return null;
		} 
	}
	
	

}
