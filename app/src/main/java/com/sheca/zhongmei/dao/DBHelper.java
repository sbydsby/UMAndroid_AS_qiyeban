package com.sheca.zhongmei.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sheca.zhongmei.util.CommonConst;

public class DBHelper extends SQLiteOpenHelper {
	public  static final String DB_NAME = "umdb";
	private static final int DB_VERSION = 11;
	public  static final String TBL_CERT = "cert";
	public  static final String TBL_LOG = "log";
	public  static final String TBL_ACCOUNT = "account";
	public  static final String TBL_APPINFO = "appinfo";
	public  static final String TBL_SEALINFO = "sealinfo";
	
	private static final String CREATE_TBL_CERT = " create table if not exists"
			+ " cert(id integer primary key autoincrement,certsn text,envsn text,privatekey text,certificate text,keystore text,enccertificate text,enckeystore text,certchain text,status integer,accountname text,notbeforetime text,validtime text,uploadstatus integer,certtype text default ('"+CommonConst.CERT_TYPE_RSA+"')"+",signalg integer default 1,containerid text default (''),algtype integer default 1,savetype integer default 1,devicesn text default (''),certname text default (''),certhash text default (''),fingertype integer default 0,sealsn text default (''),sealstate integer default 0,sdkcertid integer default 0,certlevel integer default 3) ";
	private static final String CREATE_TBL_LOG = " create table if not exists"
			+ " log(id integer primary key autoincrement,certsn text,type integer,sign text,message text,createtime text,accountname text,invoker text,signalg integer,isupload integer,invokerappid text default ('')) ";	
	private static final String CREATE_TBL_ACCOUNT = " create table if not exists"
			+ " account(id integer primary key autoincrement, name text, password text, status integer,active integer,identityname text,identitycode text,copyidphoto text default (''),accounttype integer default 1,appidinfo text default ('"+CommonConst.UM_APPID+"')"+",orgname text default (''),savetype integer default 1,certtype integer default 2,logintype integer default 1) ";	
	private static final String CREATE_TBL_APPINFO = " create table if not exists"
			+ " appinfo(id integer primary key autoincrement, appidinfo text,name text, description text, contactperson text,contactphone text,contactemail text,assigntime text) ";
	private static final String CREATE_TBL_SEALINFO = " create table if not exists"
			+ " sealinfo(id integer primary key autoincrement, vid text,sealname text, sealsn text, issuercert text,cert text,picdata text,pictype text,picwidth text,picheight text,notbefore text,notafter text,signal text,extensions text,accountname text,certsn text default (''),sdkcertid integer default 0,status integer default 6,downloadstatus integer default 0) ";
	
	
	private SQLiteDatabase db;

	public DBHelper(Context c) {
		super(c, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		db.execSQL(CREATE_TBL_CERT);
		db.execSQL(CREATE_TBL_LOG);
		db.execSQL(CREATE_TBL_ACCOUNT);
		db.execSQL(CREATE_TBL_APPINFO);
		db.execSQL(CREATE_TBL_SEALINFO);
	}

	public long insert(String table, ContentValues values) {
		db = getWritableDatabase();
		long ret = db.insert(table, null, values);
		return ret;
	}
	
	public void delete(String table, String whereclause) {
		db = getWritableDatabase();
		db.delete(table, whereclause, null);
	}

	public void update(String table, ContentValues values, String whereclause) {
		db = getWritableDatabase();
		db.update(table, values, whereclause, null);
	}
	
	public Cursor query(String table, String whereclause) {
		db = getReadableDatabase();
		Cursor c = db.query(table, null, whereclause, null, null, null, "id desc");
		return c;
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		db = getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);
		return c;
	}
		
	public void close() {
		if (db != null)
			db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    this.db = db;
	    final String ALTER_TBL_ACCOUNT = "alter table account add copyidphoto text default ('')";
	    final String ALTER_TBL_LOG = "alter table log add invokerappid text default ('')";
	    final String ALTER_TBL_ACCOUNT_ACCOUNTTYPE = "alter table account add accounttype integer default 1";
	    final String ALTER_TBL_ACCOUNT_APPIDINFO = "alter table account add appidinfo text default ('"+CommonConst.UM_APPID+"')";
	    final String ALTER_TBL_ACCOUNT_ORGNAME = "alter table account add orgname text default ('')";
	    final String ALTER_TBL_ACCOUNT_SAVETYPE = "alter table account add savetype integer default 1";
	    final String ALTER_TBL_ACCOUNT_CERTTYPE = "alter table account add certtype integer default 2";
	    final String ALTER_TBL_ACCOUNT_LOGINTYPE = "alter table account add logintype integer default 1";
			
		final String ALTER_TBL_CERT_CERTTYPE = "alter table cert add certtype text default ('"+CommonConst.CERT_TYPE_RSA+"')";
	    final String ALTER_TBL_CERT_SIGNALG = "alter table cert add signalg integer default 1";
	    final String ALTER_TBL_CERT_CONTAINERID = "alter table cert add containerid text default ('')";
	    final String ALTER_TBL_CERT_ALGTYPE = "alter table cert add algtype integer default 1";
	    final String ALTER_TBL_CERT_SAVETYPE = "alter table cert add savetype integer default 1";
	    final String ALTER_TBL_CERT_DEVICESN = "alter table cert add devicesn text default ('')";
	    final String ALTER_TBL_CERT_CERTNAME = "alter table cert add certname text default ('')";
	    final String ALTER_TBL_CERT_CERTHASH = "alter table cert add certhash text default ('')";
	    final String ALTER_TBL_CERT_FINGERTYPE = "alter table cert add fingertype integer default 0";
	    final String ALTER_TBL_CERT_SEALSN = "alter table cert add sealsn text default ('')";
	    final String ALTER_TBL_CERT_SEALSTATE = "alter table cert add sealstate integer default 0";

		final String ALTER_TBL_CERT_SDKCERTID = "alter table cert add sdkcertid integer default 0";
		final String ALTER_TBL_SEALINFO_SDKCERTID = "alter table sealinfo add sdkcertid integer default 0";

		final String ALTER_TBL_CERT_CERTLEVEL = "alter table cert add certlevel integer default 3";
		final String ALTER_TBL_SEALINFO_STATUS = "alter table sealinfo add status integer default 6";
		final String ALTER_TBL_SEALINFO_DOWNLOADSTATUS = "alter table sealinfo add downloadstatus integer default 0";
									
	    if(oldVersion <=2){
	    	db.execSQL(ALTER_TBL_ACCOUNT);
			db.execSQL(ALTER_TBL_LOG);
			db.execSQL(CREATE_TBL_APPINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_ACCOUNTTYPE);
			db.execSQL(ALTER_TBL_ACCOUNT_APPIDINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_ORGNAME);
			db.execSQL(ALTER_TBL_ACCOUNT_SAVETYPE);
			db.execSQL(ALTER_TBL_ACCOUNT_CERTTYPE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);
			
			db.execSQL(ALTER_TBL_CERT_CERTTYPE);
			db.execSQL(ALTER_TBL_CERT_SIGNALG);
			db.execSQL(ALTER_TBL_CERT_CONTAINERID);
			db.execSQL(ALTER_TBL_CERT_ALGTYPE);
			db.execSQL(ALTER_TBL_CERT_SAVETYPE);
			db.execSQL(ALTER_TBL_CERT_DEVICESN);
			db.execSQL(ALTER_TBL_CERT_CERTNAME);
			db.execSQL(ALTER_TBL_CERT_CERTHASH);
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
	    }
	    
		if(oldVersion ==3 ){
		    db.execSQL(CREATE_TBL_APPINFO);
		    db.execSQL(ALTER_TBL_ACCOUNT_ACCOUNTTYPE);
		    db.execSQL(ALTER_TBL_ACCOUNT_APPIDINFO);
		    db.execSQL(ALTER_TBL_ACCOUNT_ORGNAME);
		    db.execSQL(ALTER_TBL_ACCOUNT_SAVETYPE);
		    db.execSQL(ALTER_TBL_ACCOUNT_CERTTYPE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);
			
			db.execSQL(ALTER_TBL_CERT_CERTTYPE);
			db.execSQL(ALTER_TBL_CERT_SIGNALG);
			db.execSQL(ALTER_TBL_CERT_CONTAINERID);
			db.execSQL(ALTER_TBL_CERT_ALGTYPE);
			db.execSQL(ALTER_TBL_CERT_SAVETYPE);
			db.execSQL(ALTER_TBL_CERT_DEVICESN);
			db.execSQL(ALTER_TBL_CERT_CERTNAME);
			db.execSQL(ALTER_TBL_CERT_CERTHASH);
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
		
		if(oldVersion ==4 ){
			db.execSQL(ALTER_TBL_ACCOUNT_ACCOUNTTYPE);
			db.execSQL(ALTER_TBL_ACCOUNT_APPIDINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_ORGNAME);
			db.execSQL(ALTER_TBL_ACCOUNT_SAVETYPE);
			db.execSQL(ALTER_TBL_ACCOUNT_CERTTYPE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);
			
			db.execSQL(ALTER_TBL_CERT_CERTTYPE);
			db.execSQL(ALTER_TBL_CERT_SIGNALG);
			db.execSQL(ALTER_TBL_CERT_CONTAINERID);
			db.execSQL(ALTER_TBL_CERT_ALGTYPE);
			db.execSQL(ALTER_TBL_CERT_SAVETYPE);
			db.execSQL(ALTER_TBL_CERT_DEVICESN);
			db.execSQL(ALTER_TBL_CERT_CERTNAME);
			db.execSQL(ALTER_TBL_CERT_CERTHASH);
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
		
		if(oldVersion ==5 ){
			db.execSQL(ALTER_TBL_ACCOUNT_SAVETYPE);
			db.execSQL(ALTER_TBL_ACCOUNT_CERTTYPE);
			
		    db.execSQL(ALTER_TBL_CERT_CERTTYPE);
			db.execSQL(ALTER_TBL_CERT_SIGNALG);
			db.execSQL(ALTER_TBL_CERT_CONTAINERID);
			db.execSQL(ALTER_TBL_CERT_ALGTYPE);
			db.execSQL(ALTER_TBL_CERT_SAVETYPE);
			db.execSQL(ALTER_TBL_CERT_DEVICESN);
			db.execSQL(ALTER_TBL_CERT_CERTNAME);
			db.execSQL(ALTER_TBL_CERT_CERTHASH);
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
		
		if(oldVersion ==6 ){
			db.execSQL(ALTER_TBL_CERT_CERTNAME);
			db.execSQL(ALTER_TBL_CERT_CERTHASH);
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
		
		if(oldVersion ==7 ){
			db.execSQL(ALTER_TBL_CERT_FINGERTYPE);
			
			db.execSQL(CREATE_TBL_SEALINFO);
			db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
			db.execSQL(ALTER_TBL_CERT_SEALSN);
			db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
		
		if(oldVersion ==8 ){
		    db.execSQL(CREATE_TBL_SEALINFO);
		    db.execSQL(ALTER_TBL_ACCOUNT_LOGINTYPE);
		    db.execSQL(ALTER_TBL_CERT_SEALSN);
		    db.execSQL(ALTER_TBL_CERT_SEALSTATE);

			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}

		if (oldVersion ==9){
			db.execSQL(ALTER_TBL_CERT_SDKCERTID);
			db.execSQL(ALTER_TBL_SEALINFO_SDKCERTID);

			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}

		if (oldVersion == 10){
			db.execSQL(ALTER_TBL_CERT_CERTLEVEL);
			db.execSQL(ALTER_TBL_SEALINFO_STATUS );
			db.execSQL(ALTER_TBL_SEALINFO_DOWNLOADSTATUS );
		}
	}

	
}