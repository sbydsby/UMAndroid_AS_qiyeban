package com.sheca.umee.fragment;

import android.support.v4.app.Fragment;

import com.sheca.umee.R;

/*
   五个TAB Fragment的工厂类
 */
public class FragmentFactory {
	public static Fragment getInstanceByIndex(int index) {
		Fragment fragment = null;
		switch (index) {
//		case R.id.rb_home:
//			fragment = new HomeFragment();
//			break;
		case R.id.rb_cert:
			//fragment = new CertFragment();
//			fragment = new CertFragmentNew();
			fragment = new QiyeCertFragment();

			break;
		case R.id.rb_service:
			//fragment = new ServiceFragment();
			fragment = new ScanFragment();
			break;
//		case R.id.rb_settings:
//			//fragment = new SettingsFragment();
//			fragment = new MineFragmentV3();
//			break;
		case R.id.rb_seal:
			fragment = new QiyeSealFragmentNew();
			break;
		default:
			fragment =new QiyeCertFragment();
//			fragment = new HomeFragment();
			break;
		}
		return fragment;
	}
}
