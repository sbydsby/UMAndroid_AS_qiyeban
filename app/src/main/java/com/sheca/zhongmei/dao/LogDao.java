package com.sheca.zhongmei.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.sheca.zhongmei.model.OperationLog;

import java.util.ArrayList;
import java.util.List;

public class LogDao {
	private DBHelper db;

	public LogDao(Context context) {
		db = new DBHelper(context);
	}

	public int addLog(OperationLog log,String accountName) {
		int id = 0;
		ContentValues values = new ContentValues();
		values.put("certsn", log.getCertsn());
		values.put("type", log.getType());
		values.put("sign", log.getSign());
		values.put("message", log.getMessage());
		values.put("createtime", log.getCreatetime());
		values.put("accountname", accountName);
		values.put("invoker", log.getInvoker());
		values.put("signalg", log.getSignalg());
		values.put("isupload", log.getIsupload());
		values.put("invokerappid", log.getInvokerid());
		id = (int) db.insert(DBHelper.TBL_LOG, values);
		db.close();
		return id;
	}

	public OperationLog getLogByID(int id) {
		OperationLog log = null;
		Cursor result = db.query(DBHelper.TBL_LOG, "id=" + id);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			log = new OperationLog();
			log.setId(result.getInt(0));
			log.setCertsn(result.getString(1));
			log.setType(result.getInt(2));
			log.setSign(result.getString(3));
			log.setMessage(result.getString(4));
			log.setCreatetime(result.getString(5));
			log.setAccountname(result.getString(6));
			log.setInvoker(result.getString(7));
			log.setSignalg(result.getInt(8));
			log.setIsupload(result.getInt(9));
			log.setInvokerid(result.getString(10));
		}
		result.close();
		db.close();
		return log;
	}
	
	public List<OperationLog> getAllLogs(String accountName) {
		List<OperationLog> logList = new ArrayList<OperationLog>();
		OperationLog log = null;
		Cursor result = db.query(DBHelper.TBL_LOG, "accountname = '"+accountName+"'");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			log = new OperationLog();
			log.setId(result.getInt(0));
			log.setCertsn(result.getString(1));
			log.setType(result.getInt(2));
			log.setSign(result.getString(3));
			log.setMessage(result.getString(4));
			log.setCreatetime(result.getString(5));
			log.setAccountname(result.getString(6));
			log.setInvoker(result.getString(7));
			log.setSignalg(result.getInt(8));
			log.setIsupload(result.getInt(9));
			log.setInvokerid(result.getString(10));
			logList.add(log);
			result.moveToNext();
		}
		result.close();
		db.close();
		return logList;
	}
	
	public List<OperationLog> getLogsByType(int type,String accountName) {
		List<OperationLog> logList = new ArrayList<OperationLog>();
		OperationLog log = null;
		Cursor result = db.query(DBHelper.TBL_LOG, "type=" + type+ "' and accountname='"+accountName+"'");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			log = new OperationLog();
			log.setId(result.getInt(0));
			log.setCertsn(result.getString(1));
			log.setType(result.getInt(2));
			log.setSign(result.getString(3));
			log.setMessage(result.getString(4));
			log.setCreatetime(result.getString(5));
			log.setAccountname(result.getString(6));
			log.setInvoker(result.getString(7));
			log.setSignalg(result.getInt(8));
			log.setIsupload(result.getInt(9));
			log.setInvokerid(result.getString(10));
			logList.add(log);
			result.moveToNext();
		}
		result.close();
		db.close();
		return logList;
	}

	public void updateLog(OperationLog log,String accountName) {
		ContentValues values = new ContentValues();
		values.put("certsn", log.getCertsn());
		values.put("type", log.getType());
		values.put("sign", log.getSign());
		values.put("message", log.getMessage());
		values.put("createtime", log.getCreatetime());
		values.put("accountname", accountName);
		values.put("invoker", log.getInvoker());
		values.put("signalg", log.getSignalg());
		values.put("isupload", log.getIsupload());
		values.put("invokerappid", log.getInvokerid());
		db.update(DBHelper.TBL_LOG, values, "id=" + log.getId());
		db.close();
	}

	public void deleteLog(int id) {
		db.delete(DBHelper.TBL_LOG, "id=" + id);
		db.close();
	}
}
