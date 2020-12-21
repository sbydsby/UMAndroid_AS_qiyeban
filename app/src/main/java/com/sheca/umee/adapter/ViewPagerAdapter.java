package com.sheca.umee.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.umee.R;
import com.sheca.umee.UserProtocolActivity;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommonConst;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

	// 界面列表
	private List<View> views = null;
	private Activity   activity = null;
	private boolean    isLaunched = false;

	public ViewPagerAdapter(List<View> views, Activity activity,boolean isLaunched) {
		this.views = views;
		this.activity = activity;
		this.isLaunched = isLaunched;
	}

	// 销毁arg1位置的界面
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(views.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	// 获得当前界面数
	@Override
	public int getCount() {
		if (views != null) {
			return views.size();
		}
		return 0;
	}

	// 初始化arg1位置的界面
	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(views.get(arg1), 0);
		if (arg1 == views.size() - 1) {
			if(isLaunched){
				View mStartWeiboImageButton = arg0.findViewById(R.id.button);
				mStartWeiboImageButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 设置已经引导
						setGuided();
						goHome();
					}
				});
			}

		}

		return views.get(arg1);
	}

	private void goHome() {
		// 跳转
//		Intent intent = new Intent(activity, MainActivity.class);
//		activity.startActivity(intent);
//		//activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//		activity.finish();


            showUserRules();


	}



	public void showUserRules() {//用户协议
		AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_Transparent);
//
		builder.setCancelable(false);
//
		View view = LayoutInflater.from(activity).inflate(R.layout.item_notice_start, null, false);
		builder.setView(view);
		TextView txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
		TextView txt_ok = (TextView) view.findViewById(R.id.txt_ok);
//        WebView webView = (WebView) view.findViewById(R.id.web);
//        webView.loadUrl(CommonConst.RULES_SERVER);
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setUseWideViewPort(true);  //将图片调整到适合webView的大小
//        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setDomStorageEnabled(true);

		TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
		txt_content.setText(activity.getString(R.string.fryzt_rule1) + activity.getString(R.string.fryzt_rule2) + activity.getString(R.string.fryzt_rule3));

		String rule1 = activity.getString(R.string.fryzt_rule1);
		String rule2 = activity.getString(R.string.fryzt_rule2);
		String rule3 = activity.getString(R.string.fryzt_rule3);

		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(activity.getResources()
				.getColor(R.color.bg_blue));
		String mTitleAgreement = rule1 + rule2 + rule3;
		spannableStringBuilder.append(mTitleAgreement);
		spannableStringBuilder.setSpan(foregroundColorSpan, rule1.length(), (rule1 + rule2).length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		//设置协议点击事件
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				//这里的判断是为了去掉在点击后字体出现的背景色
				if (widget instanceof TextView) {
					((TextView) widget).setHighlightColor(Color.TRANSPARENT);
				}
				Intent intent = new Intent(activity, UserProtocolActivity.class);
				activity.startActivity(intent);

			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				//去除下划线
				ds.setColor(activity.getResources().getColor(R.color.bg_blue));
				ds.setUnderlineText(false);
			}
		};
		spannableStringBuilder.setSpan(clickableSpan, rule1.length(), (rule1 + rule2).length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		txt_content.setText(spannableStringBuilder);
		txt_content.setMovementMethod(LinkMovementMethod.getInstance());


		AlertDialog dia = builder.show();
//        dia.setView(dia.getWindow().getDecorView(),0,0,0,0);

		WindowManager m = activity.getWindowManager();
		Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
		android.view.WindowManager.LayoutParams p = dia.getWindow().getAttributes();  //获取对话框当前的参数值
		p.height = (int) (d.getHeight() * 0.5);   //高度设置为屏幕的0.3
		p.width = (int) (d.getWidth() * 0.8);    //宽度设置为屏幕的0.5
		dia.getWindow().setAttributes(p);     //设置生效


//        dia.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rules);

		dia.setCanceledOnTouchOutside(false);//禁止点外部
//        dia.setOnKeyListener(new DialogInterface.OnKeyListener() {//不可點返回鍵取消
//
//            @Override
//
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//
//                    return true;
//
//                } else {
//
//                    return false; // 默认返回 false
//
//                }
//
//            }
//
//        });
		txt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
				activity.finish();
				System.exit(0);
			}
		});
		txt_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
				AccountHelper.setAgreeUserRules(activity, true);


				Intent intent = new Intent(activity, LoginActivityV33.class);
				activity.startActivity(intent);
				//activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
				activity.finish();
//				checkVersion();
//                Intent i = new Intent(LaunchActivity.this, GuideActivity.class);
//                startActivity(i);
//                //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//              finish();
			}
		});

	}

	/**
	 * 
	 * method desc：设置已经引导过了，下次启动不用再次引导
	 */
	private void setGuided() {
		SharedPreferences preferences = activity.getSharedPreferences(
				CommonConst.PREFERENCES_NAME, Activity.MODE_PRIVATE);
		try {
			int presentVersionCode = getVersionCode();
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(CommonConst.VERSION_CODE, presentVersionCode);
			editor.commit();
		} catch (NameNotFoundException e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(activity, "获取应用程序版本号失败!", Toast.LENGTH_LONG).show();
		}
	}

	// 判断是否由对象生成界面
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

	private int getVersionCode() throws NameNotFoundException {
		int versionCode = 1;
		PackageManager manager = activity.getPackageManager();
		PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
		versionCode = info.versionCode;
		return versionCode;
	}

}
