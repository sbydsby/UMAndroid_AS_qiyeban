package com.sheca.zhongmei;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.zhongmei.R;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.AppInfoDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.layout.VerificationCodeView;
import com.sheca.zhongmei.model.Account;
import com.sheca.zhongmei.model.AppInfo;
import com.sheca.zhongmei.model.AppInfoEx;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.WebClientUtil;


public class GetRegMsgCodeActivity extends Activity {
	private TextView textView;
	private String strMobile;
	private String mError = "";
	private ProgressDialog progDialog = null;
	
	//用户账户属性全局变量
	private  int     m_ActState;         //用户状态
	private  String  m_ActName;          //用户姓名
	private  String  m_ActIdentityCode;  //用户身份证
	private  String  m_ActCopyIDPhoto;   //用户头像
	private  int     m_ActType;          //用户账户类别（1：个人；2：单位）
	private  String  m_OrgName;          //企业单位名称
	private  String  m_PWDHash;          //账户口令哈希
	
	private AccountDao mAccountDao = null;
	private CertDao    mCertDao = null;
	private AppInfoDao mAppInfoDao = null;
	private SharedPreferences sharedPrefs;
	
	private final int COUNT_DOWN_NUM = 60;  //设置倒计时60秒
	int count = COUNT_DOWN_NUM;
	
	private Timer timer = new Timer( );
	private TimerTask task = null;

	final Handler handler = new Handler( ) {
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case 1:
	                update();
	                break; 
	        }
	       
	        super.handleMessage(msg);
	    }

	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_msg_code_reg);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("填写短信验证码");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制竖屏
		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GetRegMsgCodeActivity.this.finish();
			}
		});
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){ 
			if(extras.getString("mobile")!=null)
				strMobile = extras.getString("mobile");
			//Toast.makeText(GetRegMsgCodeActivity.this, strMobile,Toast.LENGTH_SHORT).show();
		}
		
		mAccountDao = new AccountDao(GetRegMsgCodeActivity.this);
		mCertDao = new CertDao(GetRegMsgCodeActivity.this);
		mAppInfoDao = new AppInfoDao(GetRegMsgCodeActivity.this);
		sharedPrefs = this.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		findViewById(R.id.get_msg_button).setVisibility(RelativeLayout.GONE);
		findViewById(R.id.msgcodetext).setVisibility(RelativeLayout.GONE);
		((TextView)findViewById(R.id.msgcodetext)).setText("");
		VerificationCodeView verificationcodeview = (VerificationCodeView)findViewById(R.id.verificationcodeview);
	    textView = (TextView)findViewById(R.id.text);
	    verificationcodeview.setOnCodeFinishListener(new VerificationCodeView.OnCodeFinishListener() {
	            @Override
	            public void onComplete(final String content) {
	                textView.setText(content);
	                //短信验证码登录
	                final Handler handler = new Handler(GetRegMsgCodeActivity.this.getMainLooper());

	        	    new Thread(new Runnable(){
	        	            @Override
	        	            public void run() {
	        	            	handler.post(new Runnable(){
	        						 @Override
	        							public void run(){
	        							 try{
	        								  showProgDlg("短信验证码登录中...");
	        							      userLoginByValidationCode(strMobile,content);
	        							 }catch(Exception ex){
	        								 Toast.makeText(GetRegMsgCodeActivity.this, mError,Toast.LENGTH_SHORT).show(); 
	        							 }
	        						 }
	        	            	});
	        	            }
	        	        }).start();
	            }
	    });
	    
	    Button mGetMsgCodeBtn = (Button) findViewById(R.id.get_msg_button);  
	    mGetMsgCodeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				findViewById(R.id.get_msg_button).setVisibility(RelativeLayout.GONE);
				findViewById(R.id.msgcodetext).setVisibility(RelativeLayout.GONE);
				getMsgCode(strMobile);
			}
		});
		
	    
	    getMsgCode(strMobile);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			GetRegMsgCodeActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	
	private void getMsgCode(final String account){
		final Handler handler = new Handler(GetRegMsgCodeActivity.this.getMainLooper());
		
		showProgDlg("获取短信验证码中...");
	    new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	handler.post(new Runnable(){
						 @Override
							public void run(){
			                       getValidationCode(account);
						 }
	            	});
	            }
	        }).start();
	}
	
	private  void getValidationCode(String phone){
		String returnStr = "";
		String resultStr = "";
		try {
			//异步调用UMSP服务：获取短信验证码
			String timeout = GetRegMsgCodeActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = GetRegMsgCodeActivity.this.getString(R.string.UMSP_Service_GetValidationCode);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("mobile", phone);
			postParams.put("codeType", "2");
			
			String responseStr = "";
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				
				String postParam = "mobile="+URLEncoder.encode(phone, "UTF-8")+
                                   "&codeType="+URLEncoder.encode("2", "UTF-8");
		        responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
				closeProgDlg();
				if(e.getMessage().indexOf("peer")!=-1)
					mError = "无效的服务器请求";
				else					    
				   mError = "网络连接异常或无法访问服务";
				throw new Exception(mError);	
			}

			JSONObject jb = JSONObject.fromObject(responseStr);
		    resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);
				
			closeProgDlg();
			if (resultStr.equals("0")) {	
				mError = "短信已发送，请等待";
				((TextView)findViewById(R.id.msgtext)).setText("验证码已发送至"+phone.substring(0,3)+"*****"+phone.substring(8));
				((TextView)findViewById(R.id.msgcodetext)).setText("60s后可重新获取");
				findViewById(R.id.msgcodetext).setVisibility(RelativeLayout.VISIBLE);
				Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
				//findViewById(R.id.textVoice).setVisibility(RelativeLayout.VISIBLE);
				showCountDown(COUNT_DOWN_NUM);   //显示倒计时
				return;
			}else if(resultStr.equals("1001"))
				mError = "验证服务请求错误";
			else if(resultStr.equals("10003"))
				mError = "内部处理错误";
			else
				mError = returnStr;
			
			((TextView)findViewById(R.id.msgtext)).setText("验证码未发送至"+phone.substring(0,3)+"*****"+phone.substring(8)+",请重试");
			((TextView)findViewById(R.id.msgcodetext)).setText("");
			findViewById(R.id.msgcodetext).setVisibility(RelativeLayout.GONE);
			findViewById(R.id.get_msg_button).setVisibility(RelativeLayout.VISIBLE);
			Toast.makeText(this, mError, Toast.LENGTH_SHORT).show();
			
		} catch (Exception exc) {
			closeProgDlg();
			mError = exc.getMessage();
		}

	}
	
	public boolean  userLoginByValidationCode(String mobile,String code)  throws Exception{
		    String returnStr = "";

			//异步调用UMSP服务：用户登录
			String timeout = GetRegMsgCodeActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = GetRegMsgCodeActivity.this.getString(R.string.UMSP_Service_LoginByDynamicCode);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put(CommonConst.PARAM_ACCOUNT_NAME, mobile);
			postParams.put(CommonConst.PARAM_APPID, CommonConst.UM_APPID);
			postParams.put(CommonConst.PARAM_CODE, code);
			postParams.put(CommonConst.PARAM_MOBILE, mobile);
			
			String responseStr = "";
			try {
				//清空本地缓存
				WebClientUtil.cookieStore = null;
				
				String postParam = "";
				postParam = "accountName="+URLEncoder.encode(mobile, "UTF-8")+
						    "&appID="+URLEncoder.encode(CommonConst.UM_APPID, "UTF-8")+
			                "&code="+URLEncoder.encode(code, "UTF-8")+
	                        "&mobile="+URLEncoder.encode(mobile, "UTF-8") ;
				
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			} catch (Exception e) {
				closeProgDlg();
				if(e.getMessage().indexOf("peer")!=-1){
					mError = "No peer certificate 无效的服务器请求";
					throw new Exception("No peer certificate 无效的服务器请求");
				}else{		
					mError = "用户登录失败：网络连接异常或无法访问服务";
				    throw new Exception("用户登录失败：" + "网络连接异常或无法访问服务");
				}
			}
			
			JSONObject jb = JSONObject.fromObject(responseStr);
			String resultStr = jb.getString(CommonConst.RETURN_CODE);
		    String retMsg = jb.getString(CommonConst.RETURN_MSG);

		    closeProgDlg();
		    if (resultStr.equals("0")) {
		    	m_ActState = getPersonalInfo();
		    	
			    mAccountDao.add( new Account(mobile, m_PWDHash,m_ActState,1,m_ActName,m_ActIdentityCode,m_ActCopyIDPhoto,m_ActType,CommonConst.UM_APPID,m_OrgName,CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_MSG));
	
			    getAllAppInfos(mobile);
			    
			    Intent intent = new Intent(GetRegMsgCodeActivity.this, AccountActivity.class);
			    intent.putExtra("Message", "用户登录成功");
			    startActivity(intent);	
			    GetRegMsgCodeActivity.this.finish();
		    }else if (resultStr.equals("10010")) {
		    	m_ActState = getPersonalInfo();
			    mAccountDao.add( new Account(mobile, m_PWDHash, m_ActState,0,m_ActName,m_ActIdentityCode,m_ActCopyIDPhoto,m_ActType,CommonConst.UM_APPID,m_OrgName,CommonConst.SAVE_CERT_TYPE_PHONE,CommonConst.SAVE_CERT_TYPE_SM2,CommonConst.LOGIN_BY_MSG));

			    getAllAppInfos(mobile);
			    
			    Intent intent = new Intent(GetRegMsgCodeActivity.this, PasswordActivity.class);
		        intent.putExtra("Account", mobile); 
		        startActivity(intent);
		        GetRegMsgCodeActivity.this.finish();
		    }else{
		    	if(resultStr.equals("10003"))
		    	   mError = "短信验证码错误";
		    	else	    		
		    	   mError = retMsg;
		    	throw new Exception(retMsg);
		    }
		    
		    return true;	
	}
	
	private  int  getPersonalInfo(){
		String responseStr = "";
		String resultStr = "";
		String returnStr = "";
		
		int retState = 1;
		
		try {
			String timeout = GetRegMsgCodeActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = GetRegMsgCodeActivity.this.getString(R.string.UMSP_Service_GetPersonalInfo);
			
			Map<String,String> postParams = new HashMap<String,String>();
			//postParams.put("AccountName", mAccount);
			try {
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				String postParam = "";
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
				//Thread.sleep(3000);    
			} catch (Exception e) {
				m_ActName = "";
				m_ActIdentityCode = "";
				m_ActCopyIDPhoto = "";
				m_ActType = 1;
				m_PWDHash = "";
				return 1;
			}
			
			if(null == responseStr || "null".equals(responseStr)){
				m_ActName = "";
				m_ActIdentityCode = "";
				m_ActCopyIDPhoto = "";
				m_ActType = 1;
				m_PWDHash = "";
				return 1;
			}
			
			JSONObject jb = JSONObject.fromObject(responseStr);
			resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);
			
			if (resultStr.equals("0")) {
				JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
				
				retState = Integer.parseInt(jbRet.getString(CommonConst.PARAM_STATUS));   //获取用户状态
				if(null != jbRet.getString(CommonConst.PARAM_NAME))
				    m_ActName = jbRet.getString(CommonConst.PARAM_NAME);    //获取用户姓名
				else
				    m_ActName = "";    
				if(null != jbRet.getString(CommonConst.PARAM_IDENTITY_CODE))
				    m_ActIdentityCode = jbRet.getString(CommonConst.PARAM_IDENTITY_CODE);  //获取用户身份证号	
				else
					m_ActIdentityCode = "";    
				if(null != jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO))
				    m_ActCopyIDPhoto = jbRet.getString(CommonConst.PARAM_COPY_IDPHOTO);    //获取用户头像数据
				else
					m_ActCopyIDPhoto = "";    
				if(null != jbRet.getString(CommonConst.PARAM_ORG_NAME))
					m_OrgName = jbRet.getString(CommonConst.PARAM_ORG_NAME);              //获取企业单位名称
			    else
				    m_OrgName = ""; 
				if(jbRet.containsKey(CommonConst.PARAM_PWD_HASH)){
				   if(null != jbRet.getString(CommonConst.PARAM_PWD_HASH))
					  m_PWDHash = jbRet.getString(CommonConst.PARAM_PWD_HASH);              //获取企业单位名称
			       else
			    	  m_PWDHash = ""; 
				}else
					m_PWDHash = "";
				
				m_ActType = Integer.parseInt(jbRet.getString(CommonConst.PARAM_TYPE));    //获取用户账户类别
			}else {
				m_ActName = "";
				m_ActIdentityCode = "";
				m_ActCopyIDPhoto = "";
				m_ActType = 1;
				m_PWDHash = "";
			}
		
		} catch (Exception exc) {
			m_ActName = "";
			m_ActIdentityCode = "";
			m_ActCopyIDPhoto = "";
			m_ActType = 1;
			m_PWDHash = "";
		    return 1;
		}

		return retState;
	}
	
	private  void   getAllAppInfos(String actNo)  throws Exception{
		List<AppInfo> applications = null;
		String responseStr = "";
		String strAllAppInfo = "";
		Editor editor = sharedPrefs.edit();				
		
		try {
			editor.putString(CommonConst.SETTINGS_LOGIN_ACT_NAME, actNo);
    	    editor.commit();
    	    
			String timeout = GetRegMsgCodeActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = GetRegMsgCodeActivity.this.getString(R.string.UMSP_Service_GetAllAppInfos);										
			Map<String,String> postParams = new HashMap<String,String>();	
			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
			
			String postParam = "";
			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			
			JSONObject jb = JSONObject.fromObject(responseStr);
			String resultStr = jb.getString(CommonConst.RETURN_CODE);
			String returnStr = jb.getString(CommonConst.RETURN_MSG);

			if("0".equals(resultStr)){    
				//JSONObject jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));				
				JSONArray transitListArray = JSONArray.fromObject(jb.getString(CommonConst.RETURN_RESULT));	
				applications = new ArrayList<AppInfo>();
				
				for(int i = 0;i<transitListArray.size();i++){
					AppInfo appInfo = new AppInfo();
					JSONObject jbRet =  transitListArray.getJSONObject(i) ;
					appInfo.setAppID(jbRet.getString(CommonConst.PARAM_APPID));
					appInfo.setName(jbRet.getString(CommonConst.PARAM_NAME));
					appInfo.setVisibility(Integer.parseInt(jbRet.getString(CommonConst.PARAM_VISIBILITY)));
					
					if(null != jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION))
					    appInfo.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
					else
						appInfo.setDescription("");
					if(null != jbRet.getString(CommonConst.PARAM_CONTACT_PERSON))	
					    appInfo.setContactPerson(jbRet.getString(CommonConst.PARAM_CONTACT_PERSON));
					else
						appInfo.setContactPerson("");
					if(null != jbRet.getString(CommonConst.PARAM_CONTACT_PHONE))
					    appInfo.setContactPhone(jbRet.getString(CommonConst.PARAM_CONTACT_PHONE));
					else
						appInfo.setContactPhone("");
					if(null != jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL))
					    appInfo.setContactEmail(jbRet.getString(CommonConst.PARAM_CONTACT_EMAIL));
					else
						appInfo.setContactEmail("");
					if(null != jbRet.getString(CommonConst.PARAM_ASSIGN_TIME))
					    appInfo.setAssignTime(jbRet.getString(CommonConst.PARAM_ASSIGN_TIME));
					else
						appInfo.setAssignTime("");
					
					applications.add(appInfo);
				}
			}
			else
				throw new Exception(returnStr);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

        if(null != applications){
        	for(AppInfo appInfo:applications ){       			
        		if(null == mAppInfoDao.getAppInfoByAppID(appInfo.getAppID())){         		
        			//Toast.makeText(AccountLoginActivity.this, appItems,Toast.LENGTH_SHORT).show();
        		    AppInfoEx appInfoEx = new AppInfoEx();
        		    appInfoEx.setAppidinfo(appInfo.getAppID());
        		    appInfoEx.setName(appInfo.getName());
        		    if(null != appInfo.getAssignTime())
        		    	appInfoEx.setAssigntime(appInfo.getAssignTime());
        		    else
        		        appInfoEx.setAssigntime("");
        		    if(null != appInfo.getContactEmail())
        		        appInfoEx.setContactemail(appInfo.getContactEmail());
        		    else
         		        appInfoEx.setContactemail("");
        		    if(null != appInfo.getContactPerson())
        		        appInfoEx.setContactperson(appInfo.getContactPerson());
        		    else
          		        appInfoEx.setContactperson("");
        		    if(null != appInfo.getContactPhone())
        		        appInfoEx.setContactphone(appInfo.getContactPhone());
        		    else
           		        appInfoEx.setContactphone("");
        		    if(null != appInfo.getDescription())
				        appInfoEx.setDescription(appInfo.getDescription());
        		    else
           		        appInfoEx.setDescription("");
        		
				    mAppInfoDao.addAPPInfo(appInfoEx);     
        		}
				
				strAllAppInfo += appInfo.getAppID().replace("-", "")+"-";
        	}
        	
        	if(!"".equals(strAllAppInfo)){
        		strAllAppInfo = strAllAppInfo.substring(0,strAllAppInfo.length()-1);
        	    editor.putString(CommonConst.SETTINGS_ALL_APP_INFO, strAllAppInfo);
        	    editor.commit();
        	}
        	
        }
		
	}

	
	private  void showCountDown(final int countDownNum){
		timer = new Timer();		
		task = new TimerTask( ) {
		    public void run ( ) {
		        Message message = new Message( );
		        message.what = 1;
		        handler.sendMessage(message);
		    }
		};
		
		timer.schedule(task,0,1000);
		/*final Handler handler = new Handler(RegAccountActivity.this.getMainLooper());
		final Button mMsgCodeBtn = (Button) findViewById(R.id.btnCode);  
		mMsgCodeBtn.setEnabled(false);
		
		new Thread(new Runnable(){
            @Override
            public void run() {
            	try {
            		handler.post(new Runnable(){
						 @Override
							public void run(){   
							    timer = new Timer();
							    timerTask = new TimerTask() {
							            @Override
							            public void run() {
							                if (count >0 ){
							                	mMsgCodeBtn.setText(count+"秒后重新获取");	
							                }
							                else{
							                	mMsgCodeBtn.setEnabled(true);
											    mMsgCodeBtn.setText("获取验证码");
											   // timer.cancel();
											    count = countDownNum;
							                }
							                count --;

							            }
							     };       
							     timer.schedule(timerTask, 0, 1000);   
							}
						}); 
					
            	}catch(Exception exc){
            		exc.printStackTrace();
            	}
            }
        }).start();
		*/
	}
	
	private  void update(){
		count--;
		if(count > 0){
			((TextView)findViewById(R.id.msgcodetext)).setText(count+"s后可重新获取");
		}
		else{
			findViewById(R.id.get_msg_button).setVisibility(RelativeLayout.VISIBLE);
			findViewById(R.id.msgcodetext).setVisibility(RelativeLayout.GONE);
			
			timer.cancel( );
			timer = null;
			task.cancel();
			task = null;
			count = COUNT_DOWN_NUM;
		}
	}
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

	

}
