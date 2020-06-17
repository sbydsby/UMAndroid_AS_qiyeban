package com.sheca.umandroid.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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

import com.sheca.umandroid.FAQsActivity;
import com.sheca.umandroid.MeChatActivity;
import com.sheca.umandroid.NetworkOnlineActivity;
import com.sheca.umandroid.R;

public class ServiceFragmentNew extends Fragment {

	private View view = null;

	private Context context = null;

	private Activity activity = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activity = getActivity();
		view = inflater.inflate(R.layout.context_service1, container, false);
		context = view.getContext();	
		
		ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
		ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
		TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
		tv_title.setText("服务");
		Typeface typeFace = Typeface.createFromAsset(activity.getAssets(),"fonts/font.ttf");
		tv_title.setTypeface(typeFace);
		
		iv_unitrust.setVisibility(ImageButton.GONE);
		ib_account.setVisibility(ImageView.GONE);
		tv_title.setVisibility(TextView.VISIBLE);
		
		TextView textTeleNum = (TextView) view.findViewById(R.id.tvtelenum);
		textTeleNum.setVisibility(RelativeLayout.GONE);
		final String phoneNum = textTeleNum.getText().toString();
		
		((Button)view.findViewById(R.id.button_hotline)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNum.replace("-", "")));
				startActivity(intent);
			}
		});
		
		((Button)view.findViewById(R.id.button_site)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//showIFAATest();
				
				Intent intent = new Intent(context, NetworkOnlineActivity.class);
				startActivity(intent);	
			}
		});
		
		((Button)view.findViewById(R.id.button_online_sevice)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, MeChatActivity.class);
				startActivity(intent);	
			}
		});
		
		((Button)view.findViewById(R.id.button_question)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, FAQsActivity.class);
				startActivity(intent);	
			}
		});
		/*
		(view.findViewById(R.id.Layout_my_service)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNum.replace("-", "")));
				startActivity(intent);
			}
		});
*/
		return view;
	}
	
//	private  void  showIFAATest(){
//		Intent intent = new Intent(context, MainActivity.class);
//		startActivity(intent);
//	}

	
}
