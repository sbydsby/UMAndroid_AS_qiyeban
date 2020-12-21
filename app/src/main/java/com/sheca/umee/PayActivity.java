package com.sheca.umee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umee.account.LoginActivityV33;
import com.sheca.umee.adapter.CertAlgAdapter;
import com.sheca.umee.adapter.CertValidAdapter;
import com.sheca.umee.adapter.PayTypeAdapter;
import com.sheca.umee.interfaces.ThreadInterface;
import com.sheca.umee.model.APPResponse;
import com.sheca.umee.presenter.PayController;
import com.sheca.umee.util.AccountHelper;
import com.sheca.umee.util.CommUtil;
import com.sheca.umee.util.CommonConst;


import net.sf.json.JSONObject;


import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayActivity extends com.facefr.activity.BaseActivity implements OnRequestPermissionsResultCallback {
    public static final String PERSONTASK = "person";

    private static final int PASSWORD_PAGE = 1;

//	private AccountDao mAccountDao = null;

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

    private String strPsdHash;

    private boolean mIsDao = false;     //第三方接口调用标记
    private boolean mIsReset = false;   //是否重置密码标记
    private boolean mIsDownload = false;
    private boolean mBBTDeviceUsed = false;  //是否使用蓝牙key
    private String mStrBTDevicePwd = "";    //蓝牙key密码

    private String mCertType = CommonConst.CERT_TYPE_RSA;
    private String mCertValid = CommonConst.CERT_VALID_NAME_THREE_MONTH;
    private int mPayType = CommonConst.PAY_TYPE_USE_WX;

    private List<Map<String, String>> mDataCertAlg;
    private List<Map<String, String>> mDataCertValid;
    private List<Map<String, String>> mDataPayType;
    private ListView listCertAlg;
    private ListView listCertValid;
    private ListView listPayType;

    private boolean isPayed;
    private String out_trade_no;
    private Handler handler = null;

    private PayController payController = new PayController();

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_pay);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏

        ((TextView) findViewById(R.id.header_text)).setText("选择支付方式");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);
        cancelScanButton.setVisibility(View.GONE);
        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PayActivity.this.finish();
            }
        });

//		mAccountDao = new AccountDao(PayActivity.this);
//		findViewById(R.id.indicater).setVisibility(RelativeLayout.VISIBLE);

        isPayed = false;
//        api = WXAPIFactory.createWXAPI(PayActivity.this, Constants.APP_ID);   //微信支付测试appid
        out_trade_no = "";
        handler = new Handler(PayActivity.this.getMainLooper());

        findViewById(R.id.paybutton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PayActivity.this, CertPasswordActivity.class);
                startActivityForResult(intent, PASSWORD_PAGE);

//				if(CommonConst.CERT_VALID_NAME_THREE_MONTH.equals(mCertValid))
//				   showDownloadCertActivity();
//				else
//				   onPayBtnClick(handler);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("message") != null) {
                mIsDao = true;
                cancelScanButton.setVisibility(RelativeLayout.GONE);
//			    findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
            }
            if (extras.getString("Reset") != null) {
                mIsReset = true;
                mAccount = extras.getString("Account");
                strTaskGuid = extras.getString("BizSN");
                cancelScanButton.setVisibility(RelativeLayout.GONE);
//			    findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
            }
            if (extras.getString("download") != null) {
                mIsDownload = true;
                cancelScanButton.setVisibility(RelativeLayout.GONE);
//				findViewById(R.id.indicater).setVisibility(RelativeLayout.GONE);
            }

        }

        if (mIsReset) {
            if (extras.getString("loginAccount") != null) {
                strLoginAccount = extras.getString("loginAccount");
            }
            if (extras.getString("loginId") != null) {
                strLoginId = extras.getString("loginId");
            }
        } else {
            if (!mIsDao) {
                if (!AccountHelper.hasLogin(this)) {
                    //Toast.makeText(context, "不存在证书", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PayActivity.this, LoginActivityV33.class);
                    if (mIsDao)
                        intent.putExtra("message", "dao");
                    startActivity(intent);
                    PayActivity.this.finish();
                    return;
                } else {
//			   if(mAccountDao.getLoginAccount().getActive() == 0){
//				   Intent intent = new Intent(PayActivity.this, PasswordActivity.class);
//			       intent.putExtra("Account", mAccountDao.getLoginAccount().getName());
//			       if(mIsDao)
//				       intent.putExtra("message", "dao");
//			       startActivity(intent);
//			       PayActivity.this.finish();
//			       return;
//			   }
                }
            }

//		  Account currentAccount = mAccountDao.getLoginAccount();
//		  mAccount = currentAccount.getName();
            mAccount = AccountHelper.getUsername(getApplicationContext());

            if (extras != null) {
                if (extras.getString("loginAccount") != null)
                    strLoginAccount = extras.getString("loginAccount");
                if (extras.getString("loginId") != null)
                    strLoginId = extras.getString("loginId");

                if (extras.getString("message") != null)
                    mIsDao = true;

                if (extras.getString("download") != null)
                    mIsDownload = true;

                if (extras.getString("bluetoothpwd") != null) {
                    mBBTDeviceUsed = true;
                    mStrBTDevicePwd = extras.getString("bluetoothpwd");
                }

                if (extras.getString("requestNumber") != null)
                    strReqNumber = extras.getString("requestNumber");

                if (extras.getString("applyStatus") != null)
                    strStatus = extras.getString("applyStatus");

                if (extras.getString("certtype") != null)
                    strCertType = extras.getString("certtype");

            }

//		  if(mAccountDao.getLoginAccount().getStatus() == 2 || mAccountDao.getLoginAccount().getStatus() == 3 || mAccountDao.getLoginAccount().getStatus() == 4)  //账户已实名认证
//			  ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_noface_guide_2)));
//		  else
//			  ((ImageView)findViewById(R.id.indicater)).setImageDrawable(getResources().getDrawable((R.drawable.icon_face_guide_3)));

        }

        showPayView();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (PASSWORD_PAGE == requestCode && resultCode == Activity.RESULT_OK) {
            String psd = data.getExtras().getString("psd");
            strPsdHash = CommUtil.getPWDHash(psd);
            if (CommonConst.CERT_VALID_NAME_THREE_MONTH.equals(mCertValid))
                showDownloadCertActivity();
            else
                onPayBtnClick(handler);
        }

    }

    // 点击支付事件
    private void onPayBtnClick(Handler handler) {
//        if (mPayType == CommonConst.PAY_TYPE_USE_WX) {
//            if (!api.isWXAppInstalled()) {
//                Toast.makeText(PayActivity.this, "未安装微信客户端", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }

        if (!"".equals(strCertType)) {
            if (!mCertType.equals(strCertType)) {
                Toast.makeText(PayActivity.this, "证书类型不一致,重新选择证书类型", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showApplyCertReqActivity(strLoginAccount, strLoginId, strReqNumber, strStatus);

    }

    @Override
    protected void onResume() {
        super.onResume();
		
		/*
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
 
                        Intent i = new Intent(PayActivity.this, com.sheca.umandroid.WXPayResultActivity.class);
                        i.putExtra("loginAccount", strLoginAccount); 
               		    i.putExtra("loginId", strLoginId); 
               		    
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
				                	   //Toast.makeText(PayActivity.this, "paystate:"+trade_state+"\nretStr:\n"+retStr, Toast.LENGTH_SHORT).show();
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
		*/

    }


    private void showDownloadCertActivity() {
        if (!"".equals(strCertType)) {
            if (!mCertType.equals(strCertType)) {
                Toast.makeText(PayActivity.this, "证书类型不一致,重新选择证书类型", Toast.LENGTH_SHORT).show();
                return;
            }
        }

//		Account  act = mAccountDao.getLoginAccount();
//		if(CommonConst.CERT_TYPE_SM2.equals(mCertType))
//		   act.setCertType(CommonConst.SAVE_CERT_TYPE_SM2);
//		else
//		   act.setCertType(CommonConst.SAVE_CERT_TYPE_RSA);
//
//		mAccountDao.update(act);

        Intent intent = new Intent();
        intent.setClass(PayActivity.this, DownlaodCertActivity.class);
        intent.putExtra("loginAccount", strLoginAccount);
        intent.putExtra("loginId", strLoginId);
        intent.putExtra("requestNumber", strReqNumber);
        intent.putExtra("applyStatus", strStatus);
        intent.putExtra("certType", mCertType);
        intent.putExtra("psdHash",strPsdHash);
        if (mBBTDeviceUsed)
            intent.putExtra("bluetoothpwd", mStrBTDevicePwd);
        startActivity(intent);
        finish();
    }


    private void showPayView() {
        listCertAlg = (ListView) findViewById(R.id.lv_certalg);
        mDataCertAlg = getCertAlgData();
        final CertAlgAdapter certAlgAdapter = new CertAlgAdapter(this, mDataCertAlg);
        listCertAlg.setAdapter(certAlgAdapter);

        if (!"".equals(strCertType)) {
            if (CommonConst.CERT_TYPE_SM2.equals(strCertType)) {
                certAlgAdapter.setSelectItem(0);
                certAlgAdapter.notifyDataSetInvalidated();

                mCertType = CommonConst.CERT_TYPE_SM2;
            }
            if (CommonConst.CERT_TYPE_RSA.equals(strCertType)) {
                certAlgAdapter.setSelectItem(1);
                certAlgAdapter.notifyDataSetInvalidated();

                mCertType = CommonConst.CERT_TYPE_RSA;
            }
        }

        listCertAlg.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                certAlgAdapter.setSelectItem(position); //自定义的变量，以便让adapter知道要选中哪一项
                certAlgAdapter.notifyDataSetInvalidated();//提醒数据已经变动

                if (position == 0)
                    mCertType = CommonConst.CERT_TYPE_SM2;
                else if (position == 1)
                    mCertType = CommonConst.CERT_TYPE_RSA;
            }
        });


        listCertValid = (ListView) findViewById(R.id.lv_certvalid);
        mDataCertValid = getCertValidData();
        final CertValidAdapter certValidAdapter = new CertValidAdapter(this, mDataCertValid);
        listCertValid.setAdapter(certValidAdapter);

        listCertValid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                certValidAdapter.setSelectItem(position); //自定义的变量，以便让adapter知道要选中哪一项
                certValidAdapter.notifyDataSetInvalidated();//提醒数据已经变动

                if (position == 0) {
                    mCertValid = CommonConst.CERT_VALID_NAME_ONE_YEAR;
                    findViewById(R.id.lv_paytype).setVisibility(RelativeLayout.VISIBLE);
                    findViewById(R.id.paytype).setVisibility(RelativeLayout.VISIBLE);
                } else if (position == 1) {
                    mCertValid = CommonConst.CERT_VALID_NAME_THREE_MONTH;
                    findViewById(R.id.paybutton).setEnabled(true);
                    findViewById(R.id.lv_paytype).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.paytype).setVisibility(RelativeLayout.GONE);
                }
            }
        });

        listPayType = (ListView) findViewById(R.id.lv_paytype);
        mDataPayType = getPayTypeData();
        final PayTypeAdapter payTypeAdapter = new PayTypeAdapter(this, mDataPayType);
        listPayType.setAdapter(payTypeAdapter);

        listPayType.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                payTypeAdapter.setSelectItem(position); //自定义的变量，以便让adapter知道要选中哪一项
                payTypeAdapter.notifyDataSetInvalidated();//提醒数据已经变动

                if (position == 0) {
                    findViewById(R.id.paybutton).setEnabled(true);
                    mPayType = CommonConst.PAY_TYPE_USE_WX;
                } else if (position == 1) {
                    if (CommonConst.CERT_VALID_NAME_THREE_MONTH.equals(mCertValid))
                        findViewById(R.id.paybutton).setEnabled(true);
                    else
                        findViewById(R.id.paybutton).setEnabled(false);

                    mPayType = CommonConst.PAY_TYPE_USE_ALIPAY;
                }
            }
        });

        listPayType.setVisibility(RelativeLayout.GONE);
        findViewById(R.id.paytype).setVisibility(RelativeLayout.GONE);
    }

    private List<Map<String, String>> getCertAlgData() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;

        map = new HashMap<String, String>();
        map.put("certalgid", "0");
        map.put("certalgname", CommonConst.CERT_SM2_NAME);
        list.add(map);

        map = new HashMap<String, String>();
        map.put("certalgid", "1");
        map.put("certalgname", CommonConst.CERT_RSA_NAME);
        list.add(map);

        return list;
    }


    private List<Map<String, String>> getCertValidData() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;

        map = new HashMap<String, String>();
        map.put("certvalidid", "0");
        map.put("certvalidname", CommonConst.CERT_VALID_NAME_ONE_YEAR);
        map.put("certvaliddesc", CommonConst.CERT_VALID_DESC_ONE_YEAR);
        list.add(map);

        map = new HashMap<String, String>();
        map.put("certvalidid", "1");
        map.put("certvalidname", CommonConst.CERT_VALID_NAME_THREE_MONTH);
        map.put("certvaliddesc", CommonConst.CERT_VALID_DESC_THREE_MONTH);
        list.add(map);

        return list;
    }

    private List<Map<String, String>> getPayTypeData() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;

        map = new HashMap<String, String>();
        map.put("paytypeid", "0");
        map.put("paytypename", CommonConst.PAY_TYPE_BY_WX);
        map.put("paytypepic", R.drawable.weixin + "");
        map.put("paytypeext", R.drawable.tuijian + "");
        list.add(map);
		
		/*map = new HashMap<String, String>();
		map.put("paytypeid", "1");
		map.put("paytypename", CommonConst.PAY_TYPE_BY_ALIPAY);
		map.put("paytypepic", R.drawable.alipay+"");
		map.put("paytypeext", "");
		list.add(map);
	*/
        return list;
    }


//   private Boolean loginUMSPService(String act){    //重新登录UM Service
//	   closeProgDlg();
//
//	   String returnStr = "";
//		try {
//			//异步调用UMSP服务：用户登录
//			String timeout = PayActivity.this.getString(R.string.WebService_Timeout);
//			String urlPath = PayActivity.this.getString(R.string.UMSP_Service_Login);
//
//			Map<String,String> postParams = new HashMap<String,String>();
//			postParams.put("accountName", act);
//			postParams.put("pwdHash", getPWDHash(mAccountDao.getLoginAccount().getPassword()));    //账户口令需要HASH并转为BASE64字符串
//			postParams.put("appID", mAccountDao.getLoginAccount().getAppIDInfo());
//
//			String postParam = "";
//			String responseStr = "";
//			try {
//				//清空本地缓存
//				WebClientUtil.cookieStore = null;
//				//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
//				postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
//	                    "&pwdHash="+URLEncoder.encode(getPWDHash(mAccountDao.getLoginAccount().getPassword()), "UTF-8")+
//                        "&appID="+URLEncoder.encode(mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
//				responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
//			} catch (Exception e) {
//				if(null== e.getMessage())
//				   throw new Exception("用户登录失败：" + "连接服务异常,请重新点击登录");
//				else
//				  throw new Exception("用户登录失败：" + e.getMessage()+" 请重新点击登录");
//			}
//
//			net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
//			String resultStr = jb.getString(CommonConst.RETURN_CODE);
//			returnStr = jb.getString(CommonConst.RETURN_MSG);
//
//			if (resultStr.equals("0")) {
//				//若成功登录，注册已登录账号，并跳转到首页；
//				/*
//				Intent intent = new Intent(PayActivity.this, PayActivity.class);
//				intent.putExtra("loginAccount", strLoginAccount);
//				intent.putExtra("loginId", strLoginId);
//				if(mIsDao)
//					   intent.putExtra("message", "dao");
//				if(mIsDownload)
//					   intent.putExtra("download", "dao");
//				startActivity(intent);
//				PayActivity.this.finish();
//				*/
//				onPayBtnClick(handler);
//			} else if (resultStr.equals("10010")) {
//				//若账号未激活，显示修改初始密码页面；
//				if(!mIsDao){
//				   Intent intent = new Intent(PayActivity.this, PasswordActivity.class);
//				   intent.putExtra("Account", mAccount);
//				   if(mIsDao)
//				       intent.putExtra("message", "dao");
//				   startActivity(intent);
//				   PayActivity.this.finish();
//				}else{
//					Intent intent = new Intent(PayActivity.this, PayActivity.class);
//					intent.putExtra("loginAccount", strLoginAccount);
//					intent.putExtra("loginId", strLoginId);
//				    intent.putExtra("message", "dao");
//					startActivity(intent);
//					PayActivity.this.finish();
//				}
//			}else if(resultStr.equals("10009")){
//				//若账号口令错误,显示账户登录页面；
//				Account curAct = mAccountDao.getLoginAccount();
//				curAct.setStatus(-1);   //重置登录状态为未登录状态
//				mAccountDao.update(curAct);
//
//				Intent intent = new Intent(PayActivity.this, LoginActivity.class);
//				intent.putExtra("AccName", curAct.getName());
//				if(mIsDao)
//					 intent.putExtra("message", "dao");
//				startActivity(intent);
//				PayActivity.this.finish();
//			}
//			else {
//				throw new Exception(returnStr);
//			}
//		} catch (Exception exc) {
//			mError = exc.getMessage();
//			Log.e(CommonConst.TAG, mError, exc);
//			//Toast.makeText(AuthMainActivity.this, mError,Toast.LENGTH_LONG).show();
//			return false;
//		}
//
//		return true;
//   }

    private void showApplyCertReqActivity(final String strActName, final String strActIdentityCode, final String strReqNumber, final String strStatus) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                payController.getApplyCertRequest(PayActivity.this, AccountHelper.getToken(PayActivity.this),
                        strActName, strActIdentityCode, mCertType, mCertValid, new ThreadInterface() {
                            @Override
                            public void onReuslt(Object object) {
                                String resp = (String) object;
                                Log.d("unitrust", resp);

                                final APPResponse response = new APPResponse(resp);
                                String requestNumber = null;
                                if (response.getReturnCode() == 0) {
                                    JSONObject jbRet = response.getResult();
                                    requestNumber = jbRet.getString("requestNumber");

                                    Intent intent = new Intent();
//                                    intent.setClass(PayActivity.this, WXPayActivity.class);
//                                    intent.putExtra("loginAccount", strActName);
//                                    intent.putExtra("loginId", strActIdentityCode);
//                                    intent.putExtra("requestNumber", requestNumber);
//                                    intent.putExtra("applyStatus", strStatus);
//
//                                    PayActivity.this.startActivity(intent);
//                                    PayActivity.this.finish();
                                } else {
                                    Toast.makeText(PayActivity.this, response.getReturnMsg(), Toast.LENGTH_SHORT);

                                }
                            }
                        });

            }
        });


    }


    private String getPWDHash(String strPWD) {
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

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(this);
        progDialog.setMessage(strMsg);
        progDialog.setCancelable(false);
        progDialog.show();
    }

    private void changeProgDlg(String strMsg) {
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
