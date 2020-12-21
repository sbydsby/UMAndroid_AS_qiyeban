package com.sheca.umee.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;

public class ViewCertPagerAdapter extends PagerAdapter {

	public ArrayList<View> listViews;// content

	public int size;

	public ViewCertPagerAdapter(ArrayList<View> listViews) {

		this.listViews = listViews;
		size = listViews == null ? 0 : listViews.size();
	}


	public void setListViews(ArrayList<View> listViews) {
		this.listViews = listViews;
		size = listViews == null ? 0 : listViews.size();
	}

	@Override
	public int getCount() {// ��������
		return size;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {

		((ViewPager) arg0).removeView(listViews.get(arg1 % (listViews == null ? 0 : listViews.size())));


	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		try {
			((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);
		} catch (Exception e) {
	
		}
		return listViews.get(arg1 % size);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}


}
