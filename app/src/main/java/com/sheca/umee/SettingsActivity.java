package com.sheca.umee;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

//参考http://www.cnblogs.com/zhangdongzi/archive/2012/01/05/2313519.html
public class SettingsActivity extends PreferenceActivity implements
OnPreferenceChangeListener, OnPreferenceClickListener {

// static final String TAG = "PreferenceActivityDemoActivity";
SharedPreferences preference = null;
ListPreference surfaceSizeListPreference = null;
ListPreference bestPhotoCountListPreference = null;

@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
// 设置显示Preferences
addPreferencesFromResource(R.layout.auth_settings);
// 获得SharedPreferences
preference = PreferenceManager.getDefaultSharedPreferences(this);
// 找到preference对应的Key标签并转化
surfaceSizeListPreference = (ListPreference) findPreference(getString(R.string.surface_size_key));
surfaceSizeListPreference.setSummary(surfaceSizeListPreference
		.getEntry());
// 为Preference注册监听
surfaceSizeListPreference.setOnPreferenceClickListener(this);
surfaceSizeListPreference.setOnPreferenceChangeListener(this);

bestPhotoCountListPreference = (ListPreference) findPreference(getString(R.string.bestphoto_count_key));
bestPhotoCountListPreference.setOnPreferenceClickListener(this);
bestPhotoCountListPreference.setOnPreferenceChangeListener(this);
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
}

@Override
public boolean onPreferenceChange(Preference preference, Object newValue) {
// 判断是哪个Preference改变了
if (preference.getKey().equals(getString(R.string.surface_size_key))) {
	// 动态改变summary的值
	// Toast.makeText(this,newValue+"s===="+((ListPreference)
	// preference).getEntry(), Toast.LENGTH_SHORT).show();
	preference
			.setSummary(((ListPreference) preference).getEntries()[((ListPreference) preference)
					.findIndexOfValue((String) newValue)]);
}
if (preference.getKey().equals(getString(R.string.bestphoto_count_key))) {
	preference
			.setSummary(((ListPreference) preference).getEntries()[((ListPreference) preference)
					.findIndexOfValue((String) newValue)]);
}
// 返回true表示允许改变
return true;
}

@Override
public boolean onPreferenceClick(Preference preference) {

return true;
}
}