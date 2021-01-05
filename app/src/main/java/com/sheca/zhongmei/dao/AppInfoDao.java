package com.sheca.zhongmei.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.sheca.zhongmei.model.AppInfoEx;

import java.util.ArrayList;
import java.util.List;

public class AppInfoDao {
	private DBHelper db;

	public AppInfoDao(Context context) {
		db = new DBHelper(context);
	}

	public int addAPPInfo(AppInfoEx appInfo) {
		int id = 0;
		ContentValues values = new ContentValues();
		values.put("appidinfo", appInfo.getAppidinfo());
		values.put("name", appInfo.getName());
		values.put("description", appInfo.getDescription());
		values.put("contactperson", appInfo.getContactperson());
		values.put("contactphone", appInfo.getContactphone());
		values.put("contactemail", appInfo.getContactemail());
		values.put("assigntime", appInfo.getAssigntime());
		id = (int) db.insert(DBHelper.TBL_APPINFO, values);
		db.close();
		return id;
	}

	public AppInfoEx getAppInfoByID(int id) {
		AppInfoEx appInfo = null;
		Cursor result = db.query(DBHelper.TBL_APPINFO, "id=" + id);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			appInfo = new AppInfoEx();
			appInfo.setId(result.getInt(0));
			appInfo.setAppidinfo(result.getString(1));
			appInfo.setName(result.getString(2));
			appInfo.setDescription(result.getString(3));
			appInfo.setContactperson(result.getString(4));
			appInfo.setContactphone(result.getString(5));
			appInfo.setContactemail(result.getString(6));			
			appInfo.setAssigntime(result.getString(7));
		}
		
		result.close();
		db.close();
		return appInfo;
	}

	public AppInfoEx getAppInfoByAppID(String appID) {
		AppInfoEx appInfo = null;
		Cursor result = db.query(DBHelper.TBL_APPINFO, "appidinfo='" + appID + "'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			appInfo = new AppInfoEx();
			appInfo.setId(result.getInt(0));
			appInfo.setAppidinfo(result.getString(1));
			appInfo.setName(result.getString(2));
			appInfo.setDescription(result.getString(3));
			appInfo.setContactperson(result.getString(4));
			appInfo.setContactphone(result.getString(5));
			appInfo.setContactemail(result.getString(6));			
			appInfo.setAssigntime(result.getString(7));
		}
		
		result.close();
		db.close();
		return appInfo;
	}
	
	public List<AppInfoEx> getAllAppInfos() {
		List<AppInfoEx> appInfoList = new ArrayList<AppInfoEx>();
		AppInfoEx appInfo = null;
		Cursor result = db.query(DBHelper.TBL_APPINFO, "");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			appInfo = new AppInfoEx();
			appInfo.setId(result.getInt(0));
			appInfo.setAppidinfo(result.getString(1));
			appInfo.setName(result.getString(2));
			appInfo.setDescription(result.getString(3));
			appInfo.setContactperson(result.getString(4));
			appInfo.setContactphone(result.getString(5));
			appInfo.setContactemail(result.getString(6));			
			appInfo.setAssigntime(result.getString(7));
			appInfoList.add(appInfo);
			result.moveToNext();
		}
		
		result.close();
		db.close();
		return appInfoList;
	}

	
	public void deleteAppInfoByAppID(String appID) {
		db.delete(DBHelper.TBL_APPINFO, "appid='" + appID + "'");
		db.close();
	}
}
