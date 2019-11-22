package com.sheca.umandroid.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.sheca.umandroid.MainActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.util.CommonConst;

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
		Intent intent = new Intent(activity, MainActivity.class);
		activity.startActivity(intent);
		//activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		activity.finish();
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
