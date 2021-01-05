package com.sheca.zhongmei.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.sheca.zhongmei.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountDao {
	private DBHelper db;
	private  int LOGIN_STATE = 0;

	public AccountDao(Context context) {
		db = new DBHelper(context);
	}

	public int add(Account account) {
		int id = 0;
		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("password", account.getPassword());
		values.put("status", account.getStatus());
		values.put("active", account.getActive());
		values.put("identityname", account.getIdentityName());
		values.put("identitycode", account.getIdentityCode());
		values.put("copyidphoto", account.getCopyIDPhoto());
		values.put("accounttype", account.getType());
		values.put("appidinfo", account.getAppIDInfo());
		values.put("orgname", account.getOrgName());
		values.put("savetype", account.getSaveType());
		values.put("certtype", account.getCertType());
		values.put("logintype", account.getLoginType());
		id = (int) db.insert(DBHelper.TBL_ACCOUNT, values);
		db.close();
		return id;
	}
	
	
	public void delete(int id) {
		db.delete(DBHelper.TBL_ACCOUNT, "id=" + id);
		db.close();
	}

	
	public void deleteByName(String name) {
		db.delete(DBHelper.TBL_ACCOUNT, "name=" + name);
		db.close();
	}
	
	public void update(Account account) {
		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("password", account.getPassword());
		values.put("status", account.getStatus());
		values.put("active", account.getActive());
		values.put("identityname", account.getIdentityName());
		values.put("identitycode", account.getIdentityCode());
		values.put("copyidphoto", account.getCopyIDPhoto());
		values.put("accounttype", account.getType());
		values.put("appidinfo", account.getAppIDInfo());
		values.put("orgname", account.getOrgName());
		values.put("savetype", account.getSaveType());
		values.put("certtype", account.getCertType());
		values.put("logintype", account.getLoginType());
		db.update(DBHelper.TBL_ACCOUNT, values, "id=" + account.getId());
		db.close();
	}

	public Account query(int id) {
		Account account = new Account();
		Cursor result = db.query(DBHelper.TBL_ACCOUNT, "id=" + id);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			account.setId(result.getInt(0));
			account.setName(result.getString(1));
			account.setPassword(result.getString(2));
			account.setStatus(result.getInt(3));
			account.setActive(result.getInt(4));
			account.setIdentityName(result.getString(5));
			account.setIdentityCode(result.getString(6));
			account.setCopyIDPhoto(result.getString(7));
			account.setType(result.getInt(8));
			account.setAppIDInfo(result.getString(9));
			account.setOrgName(result.getString(10));
			account.setSaveType(result.getInt(11));
			account.setCertType(result.getInt(12));
			account.setLoginType(result.getInt(13));
		}
		result.close();
		db.close();
		return account;
	}
	
	public Account queryByName(String name) {
		Account account = new Account();
		Cursor result = db.query(DBHelper.TBL_ACCOUNT, "name='" + name + "'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			account.setId(result.getInt(0));
			account.setName(result.getString(1));
			account.setPassword(result.getString(2));
			account.setStatus(result.getInt(3));
			account.setActive(result.getInt(4));
			account.setIdentityName(result.getString(5));
			account.setIdentityCode(result.getString(6));
			account.setCopyIDPhoto(result.getString(7));
			account.setType(result.getInt(8));
			account.setAppIDInfo(result.getString(9));
			account.setOrgName(result.getString(10));
			account.setSaveType(result.getInt(11));
			account.setCertType(result.getInt(12));
			account.setLoginType(result.getInt(13));
		}
		result.close();
		db.close();
		return account;
	}
	
	public List<com.sheca.umplus.model.Account> queryAll() {
		List<com.sheca.umplus.model.Account> accounts = new ArrayList<com.sheca.umplus.model.Account>();
		com.sheca.umplus.model.Account mAccount =null;
		Cursor result = db.query(DBHelper.TBL_ACCOUNT, "");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			mAccount=new com.sheca.umplus.model.Account();

			mAccount.setId(result.getInt(0));
			mAccount.setName(result.getString(1));
			mAccount.setPassword(result.getString(2));
			mAccount.setStatus(result.getInt(3));
			mAccount.setActive(result.getInt(4));
			mAccount.setIdentityName(result.getString(5));
			mAccount.setIdentityCode(result.getString(6));
			mAccount.setCopyIDPhoto(result.getString(7));
			mAccount.setType(result.getInt(8));
			mAccount.setAppIDInfo(result.getString(9));
			mAccount.setOrgName(result.getString(10));
			mAccount.setSaveType(result.getInt(11));
			mAccount.setCertType(result.getInt(12));
			mAccount.setLoginType(result.getInt(13));
			accounts.add(mAccount);
			result.moveToNext();
		}
		result.close();
		db.close();
		return accounts;
	}

	public int  count() {
		int count = 0;
		Cursor result = db.rawQuery("select count(*) from account where status >= "+LOGIN_STATE, null);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			count = result.getInt(0);
		}
		result.close();
		db.close();
		return count;
	}
	
	public Account getLoginAccount() {
		Account account = new Account();
		Cursor result = db.query(DBHelper.TBL_ACCOUNT, "status >=" + LOGIN_STATE);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			account.setId(result.getInt(0));
			/*
			if(result.getInt(8) == CommonConst.ACCOUNT_TYPE_COMPANY)
			 
			   account.setName(result.getString(1).substring(0,result.getString(1).indexOf("&")));
			else
			   account.setName(result.getString(1));
			*/
			
			if(result.getString(1).indexOf("&") != -1)
				account.setName(result.getString(1).substring(0,result.getString(1).indexOf("&")));
		    else
				account.setName(result.getString(1));
			
			account.setPassword(result.getString(2));
			account.setStatus(result.getInt(3));
			account.setActive(result.getInt(4));
			account.setIdentityName(result.getString(5));
			account.setIdentityCode(result.getString(6));
			account.setCopyIDPhoto(result.getString(7));
			account.setType(result.getInt(8));
			account.setAppIDInfo(result.getString(9));
			account.setOrgName(result.getString(10));
			account.setSaveType(result.getInt(11));
			account.setCertType(result.getInt(12));
			account.setLoginType(result.getInt(13));
		}
		
		result.close();
		db.close();
		return account;
	}

	//删除本地数据库
	public void deleteAccount(){
		db.delete(DBHelper.TBL_ACCOUNT, "");
		db.close();
	}

}
