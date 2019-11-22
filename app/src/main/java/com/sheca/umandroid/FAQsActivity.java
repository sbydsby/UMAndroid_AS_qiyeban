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

import com.sheca.umandroid.adapter.FAQAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FAQsActivity extends Activity {

	private List<Map<String, String>> mData = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.faqs);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		((TextView) findViewById(R.id.header_text)).setText("帮助中心");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FAQsActivity.this.finish();
			}
		});

		FAQAdapter adapter = null;
		mData = getData();
		ListView list = (ListView) findViewById(R.id.list);
		adapter = new FAQAdapter(this, mData);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.setClass(FAQsActivity.this,
						FAQActivity.class);
				intent.putExtra("title",
						(String) mData.get(position).get("title"));
				intent.putExtra("question",
						(String) mData.get(position).get("question"));
				intent.putExtra("answer",
						(String) mData.get(position).get("answer"));
				intent.putExtra("resourceid",
						(String) mData.get(position).get("resourceid"));
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			FAQsActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private List<Map<String, String>> getData() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();

		Map<String, String> map = new HashMap<String, String>();
		map.put("title", "问题1");
		map.put("question", "移证通是什么？");
		map.put("answer", "移证通（UniTrust）是上海市数字证书认证中心有限公司（简称上海CA）于2014年推出的一款为移动用户和移动应用提供移动电子认证服务的免费软件。");
		map.put("resourceid", "");
		data.add(map);

		map = new HashMap<String, String>();
		map.put("title", "问题2");
		map.put("question", "移证通支持哪些智能终端？");
		map.put("answer", "目前移证通支持Android和iOS平台下的主流手机和平板电脑");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题3");
		map.put("question", "如何获取移证通？");
		map.put("answer", "获取移证通有以下几个途径：\n\n1）通过手机浏览器访问“http://umsp.sheca.com/d/”进行下载安装。\n\n2）通过移动应用市场（苹果App Store、91市场或安卓市场）搜索“UniTrust”进行下载安装。\n\n3）通过移证通账户注册短信里的链接进行下载安装。\n\n");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题4");
		map.put("question", "移证通账户是什么？");
		map.put("answer", "移证通通过移证通账户向智能终端用户交付上海CA提供的电子认证服务。通常情况下，用户必须先注册移证通账户并登录，才能完整地使用移证通。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题5");
		map.put("question", "如何拥有移证通账户？");
		map.put("answer", "移证通向用户提供注册账户功能，用户只需要使用自己的手机号就可以注册移证通账户。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题6");
		map.put("question", "移证通如何管理移动证书？");
		map.put("answer", "用户可以通过移证通在移动设备上实现数字证书的全生命周期管理，包括申请证书、上传公钥、下载证书、保存证书等。用户还可以使用查看证书、修改证书口令、导入系统、删除证书等功能。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题7");
		map.put("question", "如何通过移证通获取移动证书？");
		map.put("answer", "获取移动证书有以下几个途径：\n\n1）用户注册移证通账户并登录移证通，通过人脸识别进行身份认证，自助申请移动证书。\n\n2）用户前往上海CA受理点申请移动证书。受理点现场审核通过后，用户会收到移证通账户注册短信，登录移证通后，直接进行证书下载即可。\n\n3）移证通支持可信身份数据源统一授信（一对多）。机构后台统一授信后，机构用户会收到移证通账户注册短信，登录移证通后，直接进行证书下载即可。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题8");
		map.put("question", "扫一扫是什么？");
		map.put("answer", "用户可以通过移证通的“扫一扫”功能来扫描二维码进行电子认证，适用于结合第三方WEB应用进行“扫码登录”和“扫码签名”等应用场景。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题9");
		map.put("question", "移证通SDK是什么？");
		map.put("answer", "移证通SDK是移证通提供给第三方移动应用的电子认证服务接口。目前，移证通SDK主要用来向第三方移动应用提供便捷、安全以及可靠的数字证书登录、数字签名、验证签名等电子认证服务。");
		map.put("resourceid","");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题10");
		map.put("question", "账户密码是什么？");
		map.put("answer", "账户密码是用户登录移证通时需要输入的密码。一个移证通账户对应一个账户密码。");
		map.put("resourceid", "");
		data.add(map);
		
		map = new HashMap<String, String>();
		map.put("title", "问题11");
		map.put("question", "证书密码是什么？");
		map.put("answer", "证书密码是用户使用移动证书时需要输入的证书私钥保护口令。一个移证通账户下可以拥有多张移动证书，每个移动证书可以设置自己的证书密码。");
		map.put("resourceid","");
		data.add(map);
		
		return data;
	}
}
