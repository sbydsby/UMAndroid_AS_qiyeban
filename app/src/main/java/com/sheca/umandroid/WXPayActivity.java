package com.sheca.umandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.presenter.PayController;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.WebClientUtil;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Constants;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class WXPayActivity extends com.facefr.activity.BaseActivity implements OnRequestPermissionsResultCallback {
	public static final String PERSONTASK = "person";

	private AccountDao mAccountDao = null;
	
	private String mAccount = "";
	private ProgressDialog progDialog = null;
	
	private String mError = "";
	private String strLoginAccount = "";
	private String strLoginId = "";
	private String strTaskGuid = "";
	private String strHeadPhoto = "";
	private Bitmap bHeadPhoto = null;
	
	private String strReqNumber = "";
	private String strStatus = "";
	private String strCertType = "";
	
	private boolean mIsDao = false;     //第三方接口调用标记
	private boolean mIsReset = false;   //是否重置密码标记
	private boolean mIsDownload = false; 
	private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
	private String  mStrBTDevicePwd = "";    //蓝牙key密码
	
	private String mCertType = CommonConst.CERT_TYPE_SM2;
	private String mCertValid = CommonConst.CERT_VALID_NAME_ONE_YEAR;
	
	private List<Map<String, String>> mDataCertAlg;
	private List<Map<String, String>> mDataCertValid;
	private List<Map<String, String>> mDataPayType;
	private ListView listCertAlg;
	private ListView listCertValid;
	private ListView listPayType;
	
	private IWXAPI api;
	private boolean isPayed;	
	private String  out_trade_no;
	private Handler handler = null;

	private PayController payController = new PayController();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		setContentView(R.layout.activity_wx_pay);
		showLoadingView("微信支付中...");
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
		mAccountDao = new AccountDao(WXPayActivity.this);
		findViewById(R.id.btn_pay).setVisibility(RelativeLayout.GONE);
		
		isPayed = false;
		api = WXAPIFactory.createWXAPI(WXPayActivity.this, Constants.APP_ID);   //微信支付测试appid
		out_trade_no = "";
		handler = new Handler(WXPayActivity.this.getMainLooper());
	
		Bundle extras = getIntent().getExtras(); 
		if (extras != null){
			if(extras.getString("message") != null){
				mIsDao = true;  
			}if(extras.getString("Reset") != null){
				mIsReset = true;
				mAccount = extras.getString("Account");
				strTaskGuid = extras.getString("BizSN");
			}if(extras.getString("download") != null){
				mIsDownload = true;	
			}
 
		}
		
		if(mIsReset){
			if(extras.getString("loginAccount") != null){
				strLoginAccount = extras.getString("loginAccount");	  
			}
			if(extras.getString("loginId") != null){
				strLoginId = extras.getString("loginId");
			}
		}else{		
		  if(!mIsDao){
		    if(mAccountDao.count() == 0){
		    //Toast.makeText(context, "不存在证书", Toast.LENGTH_LONG).show();
			  Intent intent = new Intent(WXPayActivity.this, LoginActivity.class);	
			  if(mIsDao)
			     intent.putExtra("message", "dao");
			  startActivity(intent);	
			  WXPayActivity.this.finish();
			  return;
		    }else{
			   if(mAccountDao.getLoginAccount().getActive() == 0){
				   Intent intent = new Intent(WXPayActivity.this, PasswordActivity.class);
			       intent.putExtra("Account", mAccountDao.getLoginAccount().getName());
			       if(mIsDao)
				       intent.putExtra("message", "dao");
			       startActivity(intent);
			       WXPayActivity.this.finish();
			       return;   
			   }
		    }
		  }
		
		  Account currentAccount = mAccountDao.getLoginAccount();
		  mAccount = currentAccount.getName();
		
		  if (extras != null) {
			if(extras.getString("loginAccount") != null)
				strLoginAccount = extras.getString("loginAccount");	
			if(extras.getString("loginId") != null)
				strLoginId = extras.getString("loginId");
			
			if(extras.getString("message")!=null)
				mIsDao = true;
			
			if(extras.getString("download")!=null)
				mIsDownload = true;
			
			if(extras.getString("bluetoothpwd")!=null){
				mBBTDeviceUsed = true;
				mStrBTDevicePwd = extras.getString("bluetoothpwd");
			}
			
			if(extras.getString("requestNumber") != null)
				strReqNumber = extras.getString("requestNumber");	
			
			if(extras.getString("applyStatus") != null)
				strStatus = extras.getString("applyStatus");
			
			if(extras.getString("certtype") != null)
				strCertType = extras.getString("certtype");	
		  }
		}
		
		findViewById(R.id.btn_pay).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPayBtnClick(handler);		
			}
		});
		
		onPayBtnClick(handler);		
	}

	// 点击支付事件
	private void onPayBtnClick(final Handler handler) {
		if(!api.isWXAppInstalled()){
			Toast.makeText(WXPayActivity.this, "未安装微信客户端", Toast.LENGTH_SHORT).show();
			return;
		}

		showProgDlg("开始支付中...");

		handler.post(new Runnable(){
            @Override
            public void run() {
            	try{
            		 boolean  isReg = api.registerApp(Constants.APP_ID); 
            		
		             String timeout = WXPayActivity.this.getString(R.string.WebService_Timeout);
		             String urlPath = WXPayActivity.this.getString(R.string.UMSP_Service_WeChatPayUnifiedorder);
		
		             //String url = "http://wxpay.wxutil.com/pub_v2/app/app_pay.php";
		             //url = "http://192.168.2.133:8080/UMAPI/WeChatPayUnifiedorder";
		             //Toast.makeText(WXPayActivity.this, "获取订单中...", Toast.LENGTH_SHORT).show();


		             try{
			            //byte[] buf = Util.httpGet(url);
//        	              String postParam = "requestNumber="+URLEncoder.encode(strReqNumber, "UTF-8");
//        	              String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));

						 String responseStr = payController.getWeChatPayUnifiedorder(WXPayActivity.this,strReqNumber);
			              //byte[] buf = Util.httpPost(url, entity);
			              if (responseStr != null && !"".equals(responseStr)) {
				                 String content = responseStr;
				                 Log.d("get server pay params:",content);
	        	                 JSONObject json = new JSONObject(content); 
				                 if(null != json && "0".equals(json.getString("returnCode")) ){
					                   JSONObject jbRet =  new JSONObject(json.getString("result"));
					                   if("SUCCESS".equals(jbRet.getString("result_code"))){
					                       PayReq req = new PayReq();
					                       //req.appId = "wxf8b4f85f3a794e77";  // ������appId
					                       req.appId			= Constants.APP_ID;//json.getString("appid");
					                       req.partnerId		= "1505170281";//jbRet.getString("partnerid");
					                       req.prepayId		    = jbRet.getString("prepay_id");
					                       req.nonceStr		    = jbRet.getString("noncestr");
					                       req.timeStamp		= jbRet.getString("timestamp");
					                       req.packageValue	    = "Sign=WXPay";//jbRet.getString("package");
					                       req.sign			    = jbRet.getString("sign");
					                       req.extData			= jbRet.getString("out_trade_no");// optional
					                       out_trade_no = jbRet.getString("out_trade_no");
					                       //Toast.makeText(WXPayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
					                        //在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
					                       boolean  ispay = api.sendReq(req);		
					                       //Toast.makeText(WXPayActivity.this, "sendReq:"+String.valueOf(ispay), Toast.LENGTH_SHORT).show();
					                       
					                       closeProgDlg();
					                   }else{
						                   Toast.makeText(WXPayActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();     
						                   closeProgDlg();
						                   
						                   findViewById(R.id.btn_pay).setVisibility(RelativeLayout.VISIBLE);
					                   }
				
				                 }else if(json.getString("returnCode").equals("10012")){
				                	 closeProgDlg();
				                	 showProgDlg("账户登录中...");
				                	 loginUMSPService(mAccount);
				                 }else{
		        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
		        	                 Toast.makeText(WXPayActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();	
		        	                 
		        	                 closeProgDlg();
		        	                 findViewById(R.id.btn_pay).setVisibility(RelativeLayout.VISIBLE);
				                 }
			
			              }else{
			            	  Log.d("PAY_GET", "服务器请求错误,"+responseStr);
	        	              Toast.makeText(WXPayActivity.this, "服务器请求错误,请退出并重启应用"+responseStr, Toast.LENGTH_SHORT).show();
	        	              
	        	              closeProgDlg();
	        	              findViewById(R.id.btn_pay).setVisibility(RelativeLayout.VISIBLE);
	        	             //onPayBtnClick(handler);		
	        	              
	                      }      
		             }catch(Exception e){
        	             Log.e("PAY_GET", "异常："+e.getMessage());
        	             Toast.makeText(WXPayActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
        	             
        	             closeProgDlg();
        	             findViewById(R.id.btn_pay).setVisibility(RelativeLayout.VISIBLE);
                     }

		             isPayed = true;
            	}catch(final Exception exc){
            		Log.e("PAY_GET", "exc:"+exc.getMessage());
            		Toast.makeText(WXPayActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
            		
            		closeProgDlg();
            		findViewById(R.id.btn_pay).setVisibility(RelativeLayout.VISIBLE);
            	}
            }
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
		final Handler handler = new Handler(WXPayActivity.this.getMainLooper());
		if(isPayed){
			showProgDlg("等待支付结果中...");
			setContentView(R.layout.activity_wx_pay);
			showLoadingView("微信支付中...");
			findViewById(R.id.btn_pay).setVisibility(RelativeLayout.GONE);
			
			handler.post(new Runnable(){
	            @Override
	            public void run() {
	            	try{
//			            String timeout = WXPayActivity.this.getString(R.string.WebService_Timeout);
//                        String urlPath = WXPayActivity.this.getString(R.string.UMSP_Service_WeChatPayOrderquery);
//
//                        String postParam = "out_trade_no="+URLEncoder.encode(out_trade_no, "UTF-8");
//                        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));

						String responseStr = payController.getWeChatPayQueryOrder(WXPayActivity.this,out_trade_no);
                        Intent i = new Intent(WXPayActivity.this, WXPayResultActivity.class);
                        i.putExtra("loginAccount", strLoginAccount); 
               		    i.putExtra("loginId", strLoginId); 
               		    i.putExtra("requestNumber",strReqNumber); 
          	            i.putExtra("applyStatus",strStatus);
						i.putExtra("strCertType",strCertType);

						if(mBBTDeviceUsed)
          	    	      i.putExtra("bluetoothpwd", mStrBTDevicePwd); 
               		    
               		    if(CommonConst.CERT_VALID_NAME_ONE_YEAR.equals(mCertValid))
               		    	i.putExtra("isPayed", "pay"); 
                        
                        if (responseStr != null && !"".equals(responseStr)) {
                        	JSONObject json = new JSONObject(responseStr); 
			                 if(null != json && "0".equals(json.getString("returnCode")) ){
				                   JSONObject jbRet =  new JSONObject(json.getString("result"));
				                   if("SUCCESS".equals(jbRet.getString("result_code"))){
				                	   String trade_state = jbRet.getString("trade_state");
				                	   String retStr =  "transaction_id:"+jbRet.getString("transaction_id")+
				                			            ",out_trade_no:"+out_trade_no+
				                	                    ",total_fee:"+jbRet.getString("total_fee");
				                	   //Toast.makeText(WXPayActivity.this, "paystate:"+trade_state+"\nretStr:\n"+retStr, Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   if("SUCCESS".equals(trade_state))
				                	      i.putExtra("paystate", Boolean.TRUE); 
				                	   else
				                		  i.putExtra("paystate", Boolean.FALSE); 
				                	   
				        			   startActivity(i);
				        			   WXPayActivity.this.finish();
				                   }else{
				                	   Toast.makeText(WXPayActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   i.putExtra("paystate", Boolean.FALSE); 
				        			   startActivity(i);
				        			   WXPayActivity.this.finish();
				                   }
			                 }else{
	        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
	        	                 Toast.makeText(WXPayActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();
	        	                 closeProgDlg();
	        	                 
	        	                 i.putExtra("paystate", Boolean.FALSE); 
			        			 startActivity(i);
			        			 WXPayActivity.this.finish();
			                 }		             
                        }else{
		            	    Log.d("PAY_GET", "服务器请求错误");
        	                Toast.makeText(WXPayActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
        	                closeProgDlg();
        	                
        	                i.putExtra("paystate", Boolean.FALSE); 
		        			startActivity(i);
		        			WXPayActivity.this.finish();
                        }      
      
	            	}catch(final Exception exc){
	            		Log.e("PAY_GET", "exc:"+exc.getMessage());
	            		Toast.makeText(WXPayActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
	            		closeProgDlg();
	            	}
	            }
	        });
		}
		
		
	}

   
   private Boolean loginUMSPService(String act){    //重新登录UM Service
	   closeProgDlg();
	   
	   String returnStr = "";
		try {
			//异步调用UMSP服务：用户登录
			String timeout = WXPayActivity.this.getString(R.string.WebService_Timeout);				
			String urlPath = WXPayActivity.this.getString(R.string.UMSP_Service_Login);

			Map<String,String> postParams = new HashMap<String,String>();
			postParams.put("accountName", act);
			postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
			postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());
			
			String postParam = "";
			String responseStr = "";
			try {
				//清空本地缓存
				WebClientUtil.cookieStore = null;
				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
				String actpwd = "";
				if(mAccountDao.getLoginAccount().getLoginType() == CommonConst.LOGIN_BY_PWD)
					actpwd = getPWDHash(mAccountDao.getLoginAccount().getPassword());
				else
					actpwd = mAccountDao.getLoginAccount().getPassword();
								
				postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
	                        "&pwdHash="+URLEncoder.encode(actpwd, "UTF-8")+
                            "&appID="+URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));	
			} catch (Exception e) {
				if(null== e.getMessage())
				   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
				else
				  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
			}

			net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
			String resultStr = jb.getString(CommonConst.RETURN_CODE);
			returnStr = jb.getString(CommonConst.RETURN_MSG);

			if (resultStr.equals("0")) {
				//若成功登录，注册已登录账号，并跳转到首页；
				/*
				Intent intent = new Intent(WXPayActivity.this, WXPayActivity.class);
				intent.putExtra("loginAccount", strLoginAccount);
				intent.putExtra("loginId", strLoginId);
				if(mIsDao)
					   intent.putExtra("message", "dao");
				if(mIsDownload)
					   intent.putExtra("download", "dao");
				startActivity(intent);	
				WXPayActivity.this.finish();
				*/
				onPayBtnClick(handler);
			} else if (resultStr.equals("10010")) {
				//若账号未激活，显示修改初始密码页面；
				if(!mIsDao){
				   Intent intent = new Intent(WXPayActivity.this, PasswordActivity.class);
				   intent.putExtra("Account", mAccount); 
				   if(mIsDao)
				       intent.putExtra("message", "dao");
				   startActivity(intent);
				   WXPayActivity.this.finish();
				}else{
					Intent intent = new Intent(WXPayActivity.this, WXPayActivity.class);
					intent.putExtra("loginAccount", strLoginAccount);
					intent.putExtra("loginId", strLoginId);
				    intent.putExtra("message", "dao");
					startActivity(intent);	
					WXPayActivity.this.finish();	
				}
			}else if(resultStr.equals("10009")){
				//若账号口令错误,显示账户登录页面；
				Account curAct = mAccountDao.getLoginAccount();
				curAct.setStatus(-1);   //重置登录状态为未登录状态
				mAccountDao.update(curAct);
				
				Intent intent = new Intent(WXPayActivity.this, LoginActivity.class);
				intent.putExtra("AccName", curAct.getName());
				if(mIsDao)
					 intent.putExtra("message", "dao");
				startActivity(intent);
				WXPayActivity.this.finish();
			}
			else {
				throw new Exception(returnStr);
			}
		} catch (Exception exc) {
			mError = exc.getMessage();
			Log.e(CommonConst.TAG, mError, exc);
			//Toast.makeText(AuthMainActivity.this, mError,Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
   }
   
   private  void showLoadingView(String strLoading){
		  GifImageView gifImageView = (GifImageView) findViewById(R.id.launch_loading);		
		  
		  try{   
		     GifDrawable gifDrawable = null;
		     gifDrawable = new GifDrawable(getResources(), R.drawable.launchloading);
		     gifImageView.setImageDrawable(gifDrawable);
		     gifImageView.setVisibility(RelativeLayout.VISIBLE);
		  }catch(Exception ex){
			  gifImageView.setVisibility(RelativeLayout.GONE);
		  }
			
		  ((TextView)this.findViewById(R.id.loading_txt)).setText(strLoading);
		  
	}
   
   private  String   getPWDHash(String strPWD){
		String strPWDHash = "";
		
		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		/*try {
			strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return strPWDHash;
   }

   private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
   }
	
   private void changeProgDlg(String strMsg){
		if (progDialog.isShowing()) {
			progDialog.setMessage(strMsg);
		}
   }
	
	
   private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
   }

}
