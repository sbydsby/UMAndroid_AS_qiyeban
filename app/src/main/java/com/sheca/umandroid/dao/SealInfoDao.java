package com.sheca.umandroid.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.sheca.umandroid.model.SealInfo;

import java.util.ArrayList;
import java.util.List;

public class SealInfoDao {
	private DBHelper db;

	public SealInfoDao(Context context) {
		db = new DBHelper(context);
	}

	public int addSeal(SealInfo sealInfo,String accountName) {
		int id = 0;
		ContentValues values = new ContentValues();
		values.put("vid", sealInfo.getVid());
		values.put("sealname", sealInfo.getSealname());
		values.put("sealsn", sealInfo.getSealsn());
		values.put("issuercert", sealInfo.getIssuercert());
		values.put("cert", sealInfo.getCert());
		values.put("picdata", sealInfo.getPicdata());
		values.put("pictype", sealInfo.getPictype());
		values.put("picwidth", sealInfo.getPicwidth());
		values.put("picheight", sealInfo.getPicheight());
		values.put("notbefore",sealInfo.getNotbefore());
		values.put("notafter",sealInfo.getNotafter());
		values.put("signal",sealInfo.getSignal());
	    values.put("extensions",sealInfo.getExtensions());
	    values.put("accountname", accountName);
	    values.put("certsn",sealInfo.getCertsn());
		values.put("sdkcertid",sealInfo.getSdkID());
        values.put("status",sealInfo.getState());
        values.put("downloadstatus",sealInfo.getDownloadstatus());
			
		id = (int) db.insert(DBHelper.TBL_SEALINFO, values);
		db.close();
		return id;
	}

	public SealInfo getSealByID(int id) {
		SealInfo sealinfo = null;
		Cursor result = db.query(DBHelper.TBL_SEALINFO, "id=" + id);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			sealinfo = new SealInfo();
			sealinfo.setId(result.getInt(0));
			sealinfo.setVid(result.getString(1));
			sealinfo.setSealname(result.getString(2));
			sealinfo.setSealsn(result.getString(3));
			sealinfo.setIssuercert(result.getString(4));
			sealinfo.setCert(result.getString(5));
			sealinfo.setPicdata(result.getString(6));
			sealinfo.setPictype(result.getString(7));
			sealinfo.setPicwidth(result.getString(8));
			sealinfo.setPicheight(result.getString(9));
			sealinfo.setNotbefore(result.getString(10));
			sealinfo.setNotafter(result.getString(11));
			sealinfo.setSignal(result.getString(12));
			sealinfo.setExtensions(result.getString(13));
			sealinfo.setAccountname(result.getString(14));
			sealinfo.setCertsn(result.getString(15));
			sealinfo.setSdkID(result.getInt(16));
            sealinfo.setState(result.getInt(17));
            sealinfo.setDownloadstatus(result.getInt(18));

		}
		result.close();
		db.close();
		return sealinfo;
	}

	
	public SealInfo getSealBySealsn(String sealsn,String accountName) {
		SealInfo sealinfo = null;
		Cursor result = db.query(DBHelper.TBL_SEALINFO, "sealsn='" + sealsn + "' and accountname='"+accountName+"'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			sealinfo = new SealInfo();
			sealinfo.setId(result.getInt(0));
			sealinfo.setVid(result.getString(1));
			sealinfo.setSealname(result.getString(2));
			sealinfo.setSealsn(result.getString(3));
			sealinfo.setIssuercert(result.getString(4));
			sealinfo.setCert(result.getString(5));
			sealinfo.setPicdata(result.getString(6));
			sealinfo.setPictype(result.getString(7));
			sealinfo.setPicwidth(result.getString(8));
			sealinfo.setPicheight(result.getString(9));
			sealinfo.setNotbefore(result.getString(10));
			sealinfo.setNotafter(result.getString(11));
			sealinfo.setSignal(result.getString(12));
			sealinfo.setExtensions(result.getString(13));
			sealinfo.setAccountname(result.getString(14));
			sealinfo.setCertsn(result.getString(15));
			sealinfo.setSdkID(result.getInt(16));
            sealinfo.setState(result.getInt(17));
            sealinfo.setDownloadstatus(result.getInt(18));
		}
		result.close();
		db.close();
		return sealinfo;
	}
	
	public SealInfo getSealByCertsn(String certsn,String accountName) {
		SealInfo sealinfo = null;
		Cursor result = db.query(DBHelper.TBL_SEALINFO, "certsn='" + certsn + "' and accountname='"+accountName+"'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			sealinfo = new SealInfo();
			sealinfo.setId(result.getInt(0));
			sealinfo.setVid(result.getString(1));
			sealinfo.setSealname(result.getString(2));
			sealinfo.setSealsn(result.getString(3));
			sealinfo.setIssuercert(result.getString(4));
			sealinfo.setCert(result.getString(5));
			sealinfo.setPicdata(result.getString(6));
			sealinfo.setPictype(result.getString(7));
			sealinfo.setPicwidth(result.getString(8));
			sealinfo.setPicheight(result.getString(9));
			sealinfo.setNotbefore(result.getString(10));
			sealinfo.setNotafter(result.getString(11));
			sealinfo.setSignal(result.getString(12));
			sealinfo.setExtensions(result.getString(13));
			sealinfo.setAccountname(result.getString(14));
			sealinfo.setCertsn(result.getString(15));
			sealinfo.setSdkID(result.getInt(16));
            sealinfo.setState(result.getInt(17));
            sealinfo.setDownloadstatus(result.getInt(18));
		}
		result.close();
		db.close();
		return sealinfo;
	}

	public List<SealInfo> getAllSealInfos(String accountName) {
		List<SealInfo> sealList = new ArrayList<SealInfo>();
		SealInfo sealinfo = null;
		Cursor result = db.query(DBHelper.TBL_SEALINFO, "accountname = '"+accountName+"'");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			sealinfo = new SealInfo();
			sealinfo.setId(result.getInt(0));
			sealinfo.setVid(result.getString(1));
			sealinfo.setSealname(result.getString(2));
			sealinfo.setSealsn(result.getString(3));
			sealinfo.setIssuercert(result.getString(4));
			sealinfo.setCert(result.getString(5));
			sealinfo.setPicdata(result.getString(6));
			sealinfo.setPictype(result.getString(7));
			sealinfo.setPicwidth(result.getString(8));
			sealinfo.setPicheight(result.getString(9));
			sealinfo.setNotbefore(result.getString(10));
			sealinfo.setNotafter(result.getString(11));
			sealinfo.setSignal(result.getString(12));
			sealinfo.setExtensions(result.getString(13));
			sealinfo.setAccountname(result.getString(14));
			sealinfo.setCertsn(result.getString(15));
			sealinfo.setSdkID(result.getInt(16));
            sealinfo.setState(result.getInt(17));
            sealinfo.setDownloadstatus(result.getInt(18));
			sealList.add(sealinfo);
			result.moveToNext();
		}
		result.close();
		db.close();
		return sealList;
	}

	/**
	 * 查询所有印章
	 * @return
	 */
	public List<com.sheca.umplus.model.SealInfo> getAllSealInfo(){
		List<com.sheca.umplus.model.SealInfo> sealList = new ArrayList<com.sheca.umplus.model.SealInfo>();
		com.sheca.umplus.model.SealInfo sealinfo=null;

		Cursor result = db.query(DBHelper.TBL_SEALINFO, "");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			sealinfo = new com.sheca.umplus.model.SealInfo();
//			sealinfo.setId(result.getInt(0));
			sealinfo.setVid(result.getString(1));
			sealinfo.setSealname(result.getString(2));
			sealinfo.setSealsn(result.getString(3));
			sealinfo.setIssuercert(result.getString(4));
			sealinfo.setCert(result.getString(5));
			sealinfo.setPicdata(result.getString(6));
			sealinfo.setPictype(result.getString(7));
			sealinfo.setPicwidth(result.getString(8));
			sealinfo.setPicheight(result.getString(9));
			sealinfo.setNotbefore(result.getString(10));
			sealinfo.setNotafter(result.getString(11));
			sealinfo.setSignal(result.getString(12));
			sealinfo.setExtensions(result.getString(13));
			sealinfo.setAccountname(result.getString(14));
			sealinfo.setCertsn(result.getString(15));
			sealinfo.setSdkid(result.getInt(16));
            sealinfo.setState(result.getInt(17));
            sealinfo.setDownloadstatus(result.getInt(18));
			sealList.add(sealinfo);
			result.moveToNext();
		}
		result.close();
		db.close();
		return sealList;

	}

	public void updateSealInfo(SealInfo sealinfo,String accountName) {
		ContentValues values = new ContentValues();
		values.put("vid", sealinfo.getVid());
		values.put("sealname", sealinfo.getSealname());
		values.put("sealsn", sealinfo.getSealsn());
		values.put("issuercert", sealinfo.getIssuercert());
		values.put("cert", sealinfo.getCert());
		values.put("picdata", sealinfo.getPicdata());
		values.put("pictype", sealinfo.getPictype());
		values.put("picwidth", sealinfo.getPicwidth());
		values.put("picheight", sealinfo.getPicheight());
		values.put("notbefore",sealinfo.getNotbefore());
		values.put("notafter",sealinfo.getNotafter());
		values.put("signal",sealinfo.getSignal());
	    values.put("extensions",sealinfo.getExtensions());
	    values.put("accountname", accountName);
	    values.put("certsn",sealinfo.getCertsn());
		values.put("sdkcertid",sealinfo.getSdkID());
        values.put("status",sealinfo.getState());
        values.put("downloadstatus",sealinfo.getDownloadstatus());
		
		db.update(DBHelper.TBL_SEALINFO, values, "id=" + sealinfo.getId());
		db.close();
	}

	public void deleteSeal(int id) {
		db.delete(DBHelper.TBL_SEALINFO, "id=" + id);
		db.close();
	}
	public void deleteAllSeal(){
		db.delete(DBHelper.TBL_SEALINFO, "");
		db.close();
	}
}
