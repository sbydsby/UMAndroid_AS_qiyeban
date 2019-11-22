package com.sheca.umandroid;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.util.CommonConst;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;


public class MeChatActivity extends Activity {

	protected static final String TAG = "TestMainActivity";
	
	//DB Access Object
	private AccountDao mAccountDao = null;
	private String     mActName = "";   
	private String     mActPhone = "";   

	//private Button conversationBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAccountDao = new AccountDao(this);
		
		if (mAccountDao.count() > 0) {
			mActName = mAccountDao.getLoginAccount().getIdentityName();
			mActPhone = mAccountDao.getLoginAccount().getName();
		}
		
		showOnlineChat();
		
		/*
		 setContentView(R.layout.test_main_activity);
		
		
		conversationBtn = (Button) findViewById(R.id.conversationBtn);
		conversationBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub		
			}
		});
		
		 */

	}	
	
	private  void  showOnlineChat(){
		 UdeskSDKManager.getInstance().initApiKey(getApplicationContext(), CommonConst.UDESK_DOMAIN, CommonConst.UDESK_SECRETKEY);

         String sdkToken = PreferenceHelper.readString(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
         if (TextUtils.isEmpty(sdkToken)) {
             sdkToken = UUID.randomUUID().toString();
         }

         Map<String, String> info = new HashMap<String, String>();
         info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdkToken);

         if("".equals(mActName))
            info.put(UdeskConst.UdeskUserInfo.NICK_NAME,sdkToken );
         else
        	info.put(UdeskConst.UdeskUserInfo.NICK_NAME, "用户"+mActName.substring(0,1));

         if("".equals(mActPhone))
            info.put(UdeskConst.UdeskUserInfo.CELLPHONE, "");
         else
        	info.put(UdeskConst.UdeskUserInfo.CELLPHONE, mActPhone);

         UdeskSDKManager.getInstance().setUserInfo(
                 getApplicationContext(), sdkToken, info);

         UdeskSDKManager.getInstance().showRobotOrConversation(MeChatActivity.this);
         MeChatActivity.this.finish();
       
	}

}
