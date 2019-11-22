package net.sourceforge.simcpux;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sheca.umandroid.R;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.WebClientUtil;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.net.URLEncoder;

public class PayActivity extends Activity implements IWXAPIEventHandler{
	
	private IWXAPI api;
	private boolean isPayed;
	private ProgressDialog progDialog = null;
	
	private String  out_trade_no;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay);
		//"wxb4ba3c02aa476ea1"
		isPayed = false;
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);   //微信支付测试appid
		api.handleIntent(getIntent(), this);
		out_trade_no = "";

		final Handler handler = new Handler(PayActivity.this.getMainLooper());
		
		Button appayBtn = (Button) findViewById(R.id.appay_btn);
		appayBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//
				showProgDlg("开始支付中...");
				
				handler.post(new Runnable(){
		            @Override
		            public void run() {
		            	try{
		            		 boolean  isReg = api.registerApp(Constants.APP_ID); 
		            		
				             String timeout = PayActivity.this.getString(R.string.WebService_Timeout);				
				             String urlPath = PayActivity.this.getString(R.string.UMSP_Service_WeChatPayUnifiedorder);			
				
				             //String url = "http://wxpay.wxutil.com/pub_v2/app/app_pay.php";
				             //url = "http://192.168.2.133:8080/UMAPI/WeChatPayUnifiedorder";
				             Button payBtn = (Button) findViewById(R.id.appay_btn);
				             payBtn.setEnabled(false);
				             Toast.makeText(PayActivity.this, "获取订单中...", Toast.LENGTH_SHORT).show();
		                     
				             try{
					            //byte[] buf = Util.httpGet(url);
		        	              String postParam = "certType="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2, "UTF-8")+
			                                         "&validity="+URLEncoder.encode(CommonConst.CERT_TYPE_SM2_VALIDITY_ONE_YEAR+"", "UTF-8");
		        	              String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
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
							                       Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
							                        //在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
							                       boolean  ispay = api.sendReq(req);		
							                       Toast.makeText(PayActivity.this, "sendReq:"+String.valueOf(ispay), Toast.LENGTH_SHORT).show();
							                       
							                       closeProgDlg();
							                   }else{
								                   Toast.makeText(PayActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();
								                   
								                   closeProgDlg();
							                   }
						
						                 }else{
				        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
				        	                 Toast.makeText(PayActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();	
				        	                 
				        	                 closeProgDlg();
						                 }
					
					              }else{
					            	  Log.d("PAY_GET", "服务器请求错误");
			        	              Toast.makeText(PayActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
			        	              
			        	              closeProgDlg();
			                      }      
				             }catch(Exception e){
		        	             Log.e("PAY_GET", "异常："+e.getMessage());
		        	             Toast.makeText(PayActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
		        	             
		        	             closeProgDlg();
		                     }
				             
				             payBtn.setEnabled(true);
				             isPayed = true;
		            	}catch(final Exception exc){
		            		Log.e("PAY_GET", "exc:"+exc.getMessage());
		            		Toast.makeText(PayActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
		            		
		            		closeProgDlg();
		            	}
		            }
		        });
		            	         	
			}
		});		
		
		Button checkPayBtn = (Button) findViewById(R.id.check_pay_btn);
		checkPayBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
				Toast.makeText(PayActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT).show();
			}
		});	
		
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final Handler handler = new Handler(PayActivity.this.getMainLooper());
		if(isPayed){
			showProgDlg("等待支付结果中...");
			setContentView(R.layout.activity_launch);
			
			handler.post(new Runnable(){
	            @Override
	            public void run() {
	            	try{
			            String timeout = PayActivity.this.getString(R.string.WebService_Timeout);				
                        String urlPath = PayActivity.this.getString(R.string.UMSP_Service_WeChatPayOrderquery);			

                        String postParam = "out_trade_no="+URLEncoder.encode(out_trade_no, "UTF-8");
                        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
 
                        Intent i = new Intent(net.sourceforge.simcpux.PayActivity.this, com.sheca.umandroid.WXPayResultActivity.class);
                        
                        if (responseStr != null && !"".equals(responseStr)) {
                        	JSONObject json = new JSONObject(responseStr); 
			                 if(null != json && "0".equals(json.getString("returnCode")) ){
				                   JSONObject jbRet =  new JSONObject(json.getString("result"));
				                   if("SUCCESS".equals(jbRet.getString("result_code"))){
				                	   String trade_state = jbRet.getString("trade_state");
				                	   String retStr =  "transaction_id:"+jbRet.getString("transaction_id")+
				                			            ",out_trade_no:"+out_trade_no+
				                	                    ",total_fee:"+jbRet.getString("total_fee");
				                	   Toast.makeText(PayActivity.this, "paystate:"+trade_state+"\nretStr:\n"+retStr, Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   if("SUCCESS".equals(trade_state))
				                	      i.putExtra("paystate", Boolean.TRUE); 
				                	   else
				                		  i.putExtra("paystate", Boolean.FALSE); 
				                	   
				        			   startActivity(i);
				        			   PayActivity.this.finish();
				                   }else{
				                	   Toast.makeText(PayActivity.this, "返回错误:"+jbRet.getString("err_code_des"), Toast.LENGTH_SHORT).show();
				                	   closeProgDlg();
				                	   
				                	   i.putExtra("paystate", Boolean.FALSE); 
				        			   startActivity(i);
				        			   PayActivity.this.finish();
				                   }
			                 }else{
	        	                 Log.d("PAY_GET", "返回错误:"+json.getString("returnMsg"));
	        	                 Toast.makeText(PayActivity.this, "返回错误:"+json.getString("returnMsg"), Toast.LENGTH_SHORT).show();
	        	                 closeProgDlg();
	        	                 
	        	                 i.putExtra("paystate", Boolean.FALSE); 
			        			 startActivity(i);
			        			 PayActivity.this.finish();
			                 }		             
                        }else{
		            	    Log.d("PAY_GET", "服务器请求错误");
        	                Toast.makeText(PayActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
        	                closeProgDlg();
        	                
        	                i.putExtra("paystate", Boolean.FALSE); 
		        			startActivity(i);
		        			PayActivity.this.finish();
                        }      
      
	            	}catch(final Exception exc){
	            		Log.e("PAY_GET", "exc:"+exc.getMessage());
	            		Toast.makeText(PayActivity.this, "exc:"+exc.getMessage(), Toast.LENGTH_SHORT).show();
	            		closeProgDlg();
	            	}
	            }
	        });
		}
		
	}

	@Override
	public void onReq(BaseReq req) {
        Toast.makeText(this, "openid = " + req.openId, Toast.LENGTH_SHORT).show();
		
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			Toast.makeText(this, "COMMAND_GETMESSAGE_FROM_WX", Toast.LENGTH_SHORT).show();	
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			Toast.makeText(this, "COMMAND_SHOWMESSAGE_FROM_WX" , Toast.LENGTH_SHORT).show();
			break;
		case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
			Toast.makeText(this, R.string.launch_from_wx, Toast.LENGTH_SHORT).show();
			break;
		case ConstantsAPI.COMMAND_PAY_BY_WX:
			Toast.makeText(this, "COMMAND_PAY_BY_WX" , Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.app_tip);
			builder.setMessage(getString(R.string.pay_result_callback_msg, String.valueOf(resp.errCode)));
			builder.show();
		}
	}
	
	private void showProgDlg(String strMsg){
		progDialog = new ProgressDialog(this);
		progDialog.setMessage(strMsg);
		progDialog.setCancelable(false);
		progDialog.show();	
	}
	
	private void changeProgDlg(String strMsg){
		if (null == progDialog ) { 
			showProgDlg(strMsg);
		}else{		 
		   if (progDialog.isShowing()) {
			  progDialog.setMessage(strMsg);
		   }
		}
	}

	private void closeProgDlg() {
		if (null != progDialog && progDialog.isShowing()) {
			progDialog.dismiss();
			progDialog = null;
		}
	}
	
}
