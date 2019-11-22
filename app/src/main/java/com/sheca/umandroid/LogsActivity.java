package com.sheca.umandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sheca.umandroid.adapter.LogAdapter;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.util.CommonConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogsActivity extends Activity {

	private List<Map<String, String>> mData;
	private ListView list;
	private LogDao logDao = null;
	private AccountDao accountDao = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_logs);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		((TextView) findViewById(R.id.header_text)).setText("使用记录");	
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);
		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogsActivity.this.finish();
			}
		});

		logDao = new LogDao(LogsActivity.this);
		accountDao  = new AccountDao(LogsActivity.this);
		
		//判断账号是否已登录。
		Boolean isLoggedIn = false;
				
		if (accountDao.count() == 0) {
			isLoggedIn = false;
		} else {
			isLoggedIn = true;
		}
				
		if (!isLoggedIn) {
			//若账号未登录，跳转到登录页面
		    Intent intent = new Intent(LogsActivity.this, LoginActivity.class);													
		    startActivity(intent);	
		    LogsActivity.this.finish();
		} else {	
			if(accountDao.getLoginAccount().getActive() == 0){
				Intent intent = new Intent(this, PasswordActivity.class);
			    intent.putExtra("Account", accountDao.getLoginAccount().getName());
			    startActivity(intent);
			    LogsActivity.this.finish();  
			    return;
			}
			
		    list = (ListView) findViewById(R.id.lv_logs);
		    LogAdapter adapter;
		    mData = getData();
		    adapter = new LogAdapter(this, mData);
		    list.setAdapter(adapter);

		    list.setOnItemClickListener(new OnItemClickListener() {
			   @Override
			   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				   Intent intent = new Intent();
				   intent.setClass(LogsActivity.this, LogActivity.class);
				   intent.putExtra("logid", (String) mData.get(position).get("logid"));
				   startActivity(intent);
			   }
		   });
		
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			LogsActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private List<Map<String, String>> getData(){
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<OperationLog> logList = new ArrayList<OperationLog>();
		Map<String, String> map = null;
		
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

		logList = logDao.getAllLogs(strActName);
		for (OperationLog log : logList) {
			map = new HashMap<String, String>();
			map.put("logid", String.valueOf(log.getId()));

			String typeStr = "";
			int type = log.getType();
			if (type == OperationLog.LOG_TYPE_APPLYCERT) {
				typeStr = "申请证书";
			}else if ((type == OperationLog.LOG_TYPE_LOGIN)) {
				typeStr = "扫码登录";
			}else if ((type == OperationLog.LOG_TYPE_SIGN)) {
				typeStr = "扫码签名";
			}else if ((type == OperationLog.LOG_TYPE_DAO_SIGN)) {
				typeStr = "签名";
			}else if ((type == OperationLog.LOG_TYPE_DAO_LOGIN)) {
				typeStr = "登录";
			}else if ((type == OperationLog.LOG_TYPE_DAO_LOGIN_INTERNET)) {
				typeStr = "登录上网";
			}else if ((type == OperationLog.LOG_TYPE_INPUTCERT)) {
				typeStr = "导入证书";
			}else if ((type == OperationLog.LOG_TYPE_RENEWCERT)) {
				typeStr = "更新证书";
			}else if ((type == OperationLog.LOG_TYPE_REVOKECERT)) {
				typeStr = "撤销证书";
			}else if ((type == OperationLog.LOG_TYPE_DAO_SIGNEX)) {
				typeStr = "扫码批量签名";
			}else if ((type == OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT)) {
				typeStr = "扫码解密";
			}else if ((type == OperationLog.LOG_TYPE_APPLYSEAL)) {
				typeStr = "申请印章";
			}

			map.put("type", typeStr);
			map.put("createtime", log.getCreatetime());
			list.add(map);
		}
		return list;
	}
}
