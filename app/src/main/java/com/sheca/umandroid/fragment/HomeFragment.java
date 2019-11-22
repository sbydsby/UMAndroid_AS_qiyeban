package com.sheca.umandroid.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.excelsecu.slotapi.EsIBankDevice;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sheca.PKCS10CertificationRequest;
import com.sheca.javasafeengine;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.shcaesdeviceinfo.shcaEsDeviceInfo;
import com.sheca.umandroid.ApplicationActivity;
import com.sheca.umandroid.AuthChoiceActivity;
import com.sheca.umandroid.DaoActivity;
import com.sheca.umandroid.GetRandomNumberActivity;
import com.sheca.umandroid.LaunchActivity;
import com.sheca.umandroid.LoginActivity;
import com.sheca.umandroid.LogsActivity;
import com.sheca.umandroid.PasswordActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.ScanBlueToothActivity;
import com.sheca.umandroid.ScanBlueToothSimActivity;
import com.sheca.umandroid.SealListActivity;
import com.sheca.umandroid.SealSignActivity;
import com.sheca.umandroid.adapter.CertAdapter;
import com.sheca.umandroid.adapter.ImageCycleEdgeView;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.dao.CertDao;
import com.sheca.umandroid.dao.LogDao;
import com.sheca.umandroid.dao.SealInfoDao;
import com.sheca.umandroid.dialog.ChangePasswordDialog;
import com.sheca.umandroid.dialog.PasswordDialog;
import com.sheca.umandroid.model.ADInfo;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.model.DownloadCertResponse;
import com.sheca.umandroid.model.OperationLog;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.presenter.AuthController;
import com.sheca.umandroid.presenter.CertController;
import com.sheca.umandroid.presenter.SealPresenter;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.PKIUtil;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SealSignUtil;
import com.sheca.umandroid.util.WebClientUtil;
import com.sheca.umandroid.util.WebUtil;
import com.sheca.umplus.activity.CaptureActivity;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.security.auth.x500.X500Principal;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.excelsecu.util.LibUtil.getApplicationContext;
import static com.sheca.umplus.util.WebClientUtil.mScanToken;


public class HomeFragment extends Fragment {
	private javasafeengine jse = null;

	private WakeLock wakeLock = null;

	private CertDao certDao = null;

	private LogDao logDao = null;
	
	private  AccountDao accountDao = null;
	
	private SealInfoDao sealDao = null;

	private List<Map<String, String>> mData = null;
	
	private List<Map<String, String>> mSealData = null;

	private AlertDialog certListDialog = null;

	private ProgressDialog progDialog = null;
	
	private ProgressDialog progDialogCert = null;
	
	private static final int CAPTURE_CODE = 0;
	
	private static final int SCAN_CODE = 1;
	
	private static final int REQUEST_SEARCH_BT = 2;
	
	private static final int SCAN_SEAL_CODE = 3;

	private final static int LOGIN_SIGN = 0;

	private final static int LOGIN_SIGN_FAILURE = 1;

	private final static int LOGIN_UPLOAD = 2;

	private final static int LOGIN_UPLOAD_FAILURE = 3;

	private final static int FAILURE = -1;

	private final static int UPLOAD_P10 = 4;

	private final static int DOWNLOAD_CERT = 5;

	private final static int SAVE_CERT = 6;

	private final static int LOGIN_SUCCESS = 7;

	private final static int SIGN_SUCCESS = 8;
	
	private final static int SIGNEX_SUCCESS = 9;

	private final static int ENVELOP_DECRYPT_SUCCESS = 10;
	
	private final static int SEALINFO_SCAN_FAILURE = 11;
	
	private final static int SEAL_SCAN_FAILURE = 12;
	
	private final static int SEALINFO_SCAN_SUCCESS = 13;
	
	private final static int SEAL_SCAN_SUCCESS = 14;
	
	private final static int SEAL_SIGN_SCAN_SUCCESS = 15;

	private final static int SEAL_SIGN = 16;

	private View view = null;

	private Context context = null;

	private Activity activity = null;

	private int operatorType = 0;

	boolean mState = false;
	
	private boolean isJSONDate = false;
	
	private boolean isSignEx =   false;    //批量签名标志
	
	private boolean isSealSign =   false;   
	
	private String sCertUnicode = "";
	
	private String strScanResult = "";
	
	private String strScanSealResult = "";
	
	private String strScanErr = "";
	
	private int scanSignCount = 0;
	
	private int scanSealNum = 0;
	
	private String mStrBTDevicePwd = "";
	
	private SharedPreferences sharedPrefs;

	private static long WAIT_TIME = 5000;
	
	private  JShcaEsStd gEsDev = null;
	//private  JShcaKsStd gKsSdk = null;
	
	protected Handler workHandler = null;
	
	private HandlerThread ht = null;
	
	private ImageCycleEdgeView mAdView;
	
	private ArrayList<ADInfo> infos = new ArrayList<ADInfo>();
	
	private int[] imageIds = {/*R.drawable.banner_newyear,*/ R.drawable.banner_new1};//banner_new1
	
	private String strPrint = "";
	
	private String strScanAppName = "";   //扫码应用appid名称

	private SealPresenter sealPresenter;

	private CertController certController;
	//下载证书点击监听
	private View.OnClickListener dlCertListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {

			if(!AccountHelper.hasLogin(getContext())){
				Intent intent = new Intent(context, LoginActivity.class);
				startActivity(intent);
				//activity.finish();
			}else{
				//跳转到证书申请页面；
				Intent i = new Intent(context, ApplicationActivity.class);
				startActivity(i);
			}
		}
	};

	//申请证书点击监听
	private View.OnClickListener applyCertListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			if(!AccountHelper.hasLogin(getContext())){
				Intent intent = new Intent(context, LoginActivity.class);
				startActivity(intent);
				//activity.finish();
			}else{
				try{
					final Handler handler = new Handler(context.getMainLooper());

					if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
						showProgDlgCert("正在连接蓝牙key设备...");
					else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
						showProgDlgCert("正在连接蓝牙sim卡...");

					workHandler.post(new Runnable() {
						@Override
						public void run() {
							if(checkBTDevice(false)){
								handler.post(new Runnable() {
									@Override
									public void run() {
										closeProgDlgCert();	
									}
								});

								if (AccountHelper.hasAuth(getContext())){ //账户已实名认证
									applyByFace();
								}else{
									Intent intent = new Intent(getContext(),AuthChoiceActivity.class);
									Bundle bundle = new Bundle();
									intent.putExtra("isPayAndAuth","isPayAndAuth");
									intent.putExtras(bundle);
									startActivity(intent);
								}
//
							}else{
								handler.post(new Runnable() {
									@Override
									public void run() {
										closeProgDlgCert();
										Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					});
				}catch( Exception e ) {
					closeProgDlgCert();
					Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
	};

	//扫一扫点击监听
	private View.OnClickListener scanClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			if(!AccountHelper.hasLogin(getContext())){
				Intent intent = new Intent(context, LoginActivity.class);
				startActivity(intent);
				getActivity().finish();
			} else{
				if(accountDao.getLoginAccount().getActive() == 0){
					Intent intent = new Intent(context, PasswordActivity.class);
					intent.putExtra("Account", accountDao.getLoginAccount().getName());
					startActivity(intent);
					// activity.finish();
				} else{
					try {
						mData = getData("");
					} catch (Exception e) {
						e.printStackTrace();
					}

					if(mData.size() == 0){   //进行人脸识并下载证书
						if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
						Toast.makeText(context, "无证书,请先下载证书", Toast.LENGTH_SHORT).show();
						else
							showFaceReg();   //进行人脸识别
					} else{
						String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
						mState = sharedPrefs.getBoolean(mUserName + CommonConst.FACE_NOPASS, false);
						if(mState){
							showFaceAuthView();
						}else{
							Intent i = new Intent(activity, CaptureActivity.class);
							startActivityForResult(i, CAPTURE_CODE);
						}

					}
				}
			}

		}
	};

	private  void  showFaceAuthView(){
		AlertDialog.Builder builder = new Builder(context);
		builder.setMessage("已开启人脸免密,是否继续使用?");
		builder.setIcon(R.drawable.alert);
		builder.setTitle("提示");
		builder.setNegativeButton("继续使用", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try{
					faceAuthNoPass();
				}catch( Exception e ) {
					Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
					return;
				}

			}
		});

		builder.setPositiveButton("使用其他方式",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
										int which) {
						mState = false;
						Intent i = new Intent(activity, CaptureActivity.class);
						startActivityForResult(i, CAPTURE_CODE);

						dialog.dismiss();
					}
				});

		builder.show();
	}

	private int retCode=-1;
	private List<Cert> mCertList=new ArrayList<Cert>();

	//人脸免密识别
	private void  faceAuthNoPass(){
		new MyAsycnTaks(){
			@Override
			public void preTask() {

			}

			@Override
			public void doinBack() {
				UniTrust uniTrust = new UniTrust(activity, false);
				uniTrust.setFaceAuth(true);
				uniTrust.setFaceAuthActionNumber(1);
				String info = ParamGen.getFaceAuth(getApplicationContext());
				String responseStr = uniTrust.FaceAuth(info);
				APPResponse response = new APPResponse(responseStr);
				retCode = response.getReturnCode();
			}

			@Override
			public void postTask() {
				if(0==retCode){
					String strActName = accountDao.getLoginAccount().getName();
				    mCertList = certDao.getAllCerts(strActName);


					Intent i = new Intent(activity, CaptureActivity.class);
					startActivityForResult(i, CAPTURE_CODE);
				}else{
					Toast.makeText(activity,"人脸识别失败",Toast.LENGTH_SHORT).show();
				}

			}
		}.execute();
	}

	//申请印章
	private View.OnClickListener applySealListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null == sealPresenter){
				sealPresenter = new SealPresenter(getContext(),accountDao,certDao,workHandler,getActivity());
			}

			sealPresenter.applySeal(mData);
		}
	};

	//实名认证
	private View.OnClickListener idCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	if (!AccountHelper.hasLogin(getContext())){
				Intent intent = new Intent(context, LoginActivity.class);
				startActivity(intent);
				return;
			}

        	if(AccountHelper.hasAuth(getContext())){  //账户已实名认证
        		Toast.makeText(context, "账户已实名认证", Toast.LENGTH_SHORT).show();
        	}else{
        		Intent intent = new Intent(getContext(),AuthChoiceActivity.class);

        		Bundle bundle = new Bundle();
				bundle.putString("isFaceAuth", "isFaceAuth");
				intent.putExtras(bundle);
				startActivity(intent);
        	}
        }
    };

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		certController = new CertController();
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activity = getActivity();
		view = inflater.inflate(R.layout.context_home_v3, container, false);
		context = view.getContext();

		ImageView iv_unitrust = (ImageView) activity.findViewById(R.id.iv_unitrust);
		ImageButton ib_account = (ImageButton) activity.findViewById(R.id.ib_account);
		ib_account.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(context, LoginActivity.class);
				startActivity(i);
			}
		});

		TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
		tv_title.setText("移证通");

		CommUtil.setTitleColor(activity,R.color.bg_yellow,
				R.color.black);

		Typeface typeFace = Typeface.createFromAsset(activity.getAssets(),"fonts/font.ttf");
		tv_title.setTypeface(typeFace);
		
		iv_unitrust.setVisibility(ImageButton.GONE);
		ib_account.setVisibility(ImageView.GONE);
		tv_title.setVisibility(TextView.VISIBLE);		

		jse = new javasafeengine();
		certDao = new CertDao(context);
		logDao = new LogDao(context);
		accountDao = new AccountDao(context);
		sealDao  = new SealInfoDao(context);
		
		sharedPrefs = context.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
		gEsDev = JShcaEsStd.getIntence(context); 
//		if(null == ScanBlueToothSimActivity.gKsSdk)
//		   ScanBlueToothSimActivity.gKsSdk = JShcaKsStd.getIntence(activity.getApplication(), context);
		ht = new HandlerThread("es_device_working_thread");
      	ht.start();
        workHandler = new Handler(ht.getLooper()); 
       
        infos = new ArrayList<ADInfo>();
        for(int i=0;i < imageIds.length; i ++){
			ADInfo info = new ADInfo();
			info.setPath(imageIds[i]);
			info.setContent("top-->" + i);
			infos.add(info);
		}
        	
        mAdView = (ImageCycleEdgeView) view.findViewById(R.id.ad_view);
        mAdView.setImageResources(infos, mAdCycleViewListener);
		mAdView.setSlide(false);
	       
		Button buttonScan = (Button) view.findViewById(R.id.button_scan);		
		buttonScan.setOnClickListener(scanClickListener);

		Button button_log = (Button) view.findViewById(R.id.button_log);
		button_log.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(accountDao.count() == 0){
					Intent intent = new Intent(context, LoginActivity.class);	
					startActivity(intent);	
					//activity.finish();
				}else{
					Intent i = new Intent(context, LogsActivity.class);
					startActivity(i);
				}
			}
		});
		
		Button button_apply_cert = (Button) view.findViewById(R.id.button_apply_cert);
		if(accountDao.count() == 0){
			button_apply_cert.setVisibility(RelativeLayout.VISIBLE);
		}else{
		   if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			   button_apply_cert.setVisibility(RelativeLayout.GONE);
		   else
			   button_apply_cert.setVisibility(RelativeLayout.VISIBLE);
		}
		
		button_apply_cert.setOnClickListener(applyCertListener);
		
		Button button_download_cert = (Button) view.findViewById(R.id.button_download_cert);
		button_download_cert.setOnClickListener(dlCertListener);

		Button button_input_cert = (Button) view.findViewById(R.id.button_input_cert);
		if(LaunchActivity.isBlueToothUsed){
			button_input_cert.setVisibility(RelativeLayout.VISIBLE);
		}else{
			if(checkBlueToothUsed())
			    button_input_cert.setVisibility(RelativeLayout.VISIBLE);
			else
				button_input_cert.setVisibility(RelativeLayout.GONE);
		}
		
		button_input_cert.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{ 
					if(accountDao.count() == 0){
						Intent intent = new Intent(context, LoginActivity.class);	
						startActivity(intent);	
						//activity.finish();
					}else{	
						String strDeviceSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
						if(!"".equals(strDeviceSN)){	
							if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType()){
					    	   shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strDeviceSN);
	        				   if(null != devInfo)	{
	        					   doInputCert();
	        				   }else{
	        					   Intent intent = new Intent();
	 						       intent.setClass(context, ScanBlueToothActivity.class);
	 						       intent.putExtra("input", "dao");
	 						       startActivityForResult(intent, REQUEST_SEARCH_BT); 		 
	        				   }
							}else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType()){
								if (ScanBlueToothSimActivity.gKsSdk.isConnected()){
									doInputCert();
								}else{
									Intent intent = new Intent();
		 						    intent.setClass(context, ScanBlueToothSimActivity.class);
		 						    intent.putExtra("input", "dao");
		 						    startActivityForResult(intent, REQUEST_SEARCH_BT); 		
								}
							}
					    }else{												
						   Intent intent = new Intent();
						   if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
						       intent.setClass(context, ScanBlueToothActivity.class);
						   else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
							   intent.setClass(context, ScanBlueToothSimActivity.class);
						   else 
							   intent.setClass(context, ScanBlueToothActivity.class);
						   intent.putExtra("input", "dao");
						   startActivityForResult(intent, REQUEST_SEARCH_BT); 		 
					    }
					}
				}catch( Exception e ) {	
					   closeProgDlgCert();
					   Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
					   return; 
				}
			}
		});
		
		Button button_test = (Button) view.findViewById(R.id.button_Test);  //洛安测试
		button_test.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					Intent i = new Intent(context, GetRandomNumberActivity.class);
					startActivity(i);
			}
		});
		button_test.setVisibility(RelativeLayout.GONE);

		initV3View(view);

		return view;
	}

	//TODO
	private void initV3View(View view){
		//扫一扫系列
		view.findViewById(R.id.scan_login).setOnClickListener(scanClickListener);
		view.findViewById(R.id.scan_sign).setOnClickListener(scanClickListener);
		view.findViewById(R.id.scan_decrypt).setOnClickListener(scanClickListener);
		view.findViewById(R.id.scan_seal).setOnClickListener(scanClickListener);

		//证书印章系列
		//实名认证
		view.findViewById(R.id.selected_smrz).setOnClickListener(idCheck);

		//申请证书
		view.findViewById(R.id.selected_sqzs).setOnClickListener(applyCertListener);

		//申请印章
		view.findViewById(R.id.selected_sqyz).setOnClickListener(applySealListener);

		//下载证书
		view.findViewById(R.id.selected_xzzs).setOnClickListener(dlCertListener);
		
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY){
		   view.findViewById(R.id.scan_seal).setVisibility(RelativeLayout.GONE);
		   view.findViewById(R.id.selected_smrz).setVisibility(RelativeLayout.GONE);
		   view.findViewById(R.id.selected_sqzs).setVisibility(RelativeLayout.GONE);
		   view.findViewById(R.id.selected_sqyz).setVisibility(RelativeLayout.GONE);
		}
	}
	
	private  void   inputBTCert(){
		String signcert = "";
		boolean isCompanyAct = false;   //是否企业账户
		
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY){
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
			isCompanyAct = true;
		}
		
		int saveType = accountDao.getLoginAccount().getSaveType();
		
		if(CommonConst.SAVE_CERT_TYPE_RSA == accountDao.getLoginAccount().getCertType()){
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   signcert = gEsDev.readRSASignatureCert();
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   signcert = ScanBlueToothSimActivity.gKsSdk.readRSASignatureCert();
			if(null == signcert || "".equals(signcert)  ){
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   Toast.makeText(context, "蓝牙key内无RSA证书", Toast.LENGTH_SHORT).show();
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   Toast.makeText(context, "蓝牙sim卡内无RSA证书", Toast.LENGTH_SHORT).show();
				return ;
			}
				
			byte[] bCert = Base64.decode(signcert);
			javasafeengine jse = new javasafeengine();
		    Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
			String certsn = new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray()));
			
			if(null != certDao.getCertByCertsn(certsn, strActName)){
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   Toast.makeText(context, "蓝牙key内证书已导入,不能重复导入", Toast.LENGTH_SHORT).show();
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   Toast.makeText(context, "蓝牙sim卡内证书已导入,不能重复导入", Toast.LENGTH_SHORT).show();
				return;
			}
			
			Cert curcert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,CommonConst.CERT_TYPE_RSA);
		    if(isCompanyAct)
		    	 curcert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,CommonConst.CERT_TYPE_RSA_COMPANY);
			if(null != curcert){
		    	certDao.deleteCert(curcert.getId());
			}
			
			Cert cert = new Cert(); 
			cert.setEnvsn(CommonConst.INPUT_RSA_SIGN);
			cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
			cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
			cert.setCerttype(CommonConst.CERT_TYPE_RSA);
			if(isCompanyAct)
				cert.setCerttype(CommonConst.CERT_TYPE_RSA_COMPANY);
			cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
			cert.setSignalg(1);
			cert.setContainerid("");
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
			cert.setPrivatekey("");
			cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			cert.setCertsn(certsn);
			cert.setCertchain("");
			cert.setNotbeforetime(getCertNotbeforetime(signcert));
			cert.setValidtime(getCertValidtime(signcert));
			cert.setCertificate(signcert);
			cert.setKeystore("");
				
			certDao.addCert(cert,strActName);
			saveLog(OperationLog.LOG_TYPE_INPUTCERT,cert.getCertsn(), "","", "",1);
			
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   Toast.makeText(context, "蓝牙key内证书导入成功", Toast.LENGTH_SHORT).show();
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   Toast.makeText(context, "蓝牙sim卡内证书导入成功", Toast.LENGTH_SHORT).show();
		}else{
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   signcert = gEsDev.readSM2SignatureCert();
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   signcert = ScanBlueToothSimActivity.gKsSdk.readSM2SignatureCert();
			if( null == signcert || "".equals(signcert) ){
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   Toast.makeText(context, "蓝牙key内无SM2证书", Toast.LENGTH_SHORT).show();
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   Toast.makeText(context, "蓝牙sim卡内无SM2证书", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			byte[] bCert = Base64.decode(signcert);
			javasafeengine jse = new javasafeengine();
		    Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
			String certsn = new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray()));
			
			if(null != certDao.getCertByCertsn(certsn, strActName)){
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   Toast.makeText(context, "蓝牙key内证书已导入,不能重复导入", Toast.LENGTH_SHORT).show();
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   Toast.makeText(context, "蓝牙sim卡内证书已导入,不能重复导入", Toast.LENGTH_SHORT).show();
				return;
			}
			
			Cert curcert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,CommonConst.CERT_TYPE_SM2);
			if(isCompanyAct)
				curcert = certDao.getCertByDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""),strActName,CommonConst.CERT_TYPE_SM2_COMPANY);
			if(null != curcert){
		    	certDao.deleteCert(curcert.getId());
		    	Cert curenccert = certDao.getCertByEnvsn(curcert.getEnvsn()+"-e",strActName);
		    	if(null != curenccert)
		    		certDao.deleteCert(curenccert.getId());   	
			}
				
			Cert cert = new Cert(); 
			cert.setEnvsn(CommonConst.INPUT_SM2_SIGN);
			cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
			cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
			cert.setCerttype(CommonConst.CERT_TYPE_SM2);
			if(isCompanyAct)
				cert.setCerttype(CommonConst.CERT_TYPE_SM2_COMPANY);
			cert.setAlgtype(CommonConst.CERT_ALG_TYPE_SIGN);
			cert.setSignalg(2);
			cert.setContainerid("");
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   cert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   cert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
			cert.setPrivatekey("");
			cert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
			cert.setCertsn(certsn);
			cert.setCertchain("");
			cert.setNotbeforetime(getCertNotbeforetime(signcert));
			cert.setValidtime(getCertValidtime(signcert));
			cert.setCertificate(signcert);
			cert.setEnccertificate("");
			cert.setKeystore("");
			
			String enccert = "";
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				enccert = gEsDev.readSM2EncryptCert();
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				enccert = ScanBlueToothSimActivity.gKsSdk.readSM2EncryptCert();
			if(!"".equals(enccert) && null != enccert)
				cert.setEnccertificate(enccert);
				
			certDao.addCert(cert,strActName);	
			saveLog(OperationLog.LOG_TYPE_INPUTCERT,cert.getCertsn(), "","", "",1);
						
			if("".equals(enccert) || null == enccert){
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   Toast.makeText(context, "蓝牙key内证书导入成功", Toast.LENGTH_SHORT).show();
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   Toast.makeText(context, "蓝牙sim卡内证书导入成功", Toast.LENGTH_SHORT).show();
				return;
			}
			
			bCert = Base64.decode(enccert);
		    oCert = jse.getCertFromBuffer(bCert);
			oX509Cert = (X509Certificate) oCert;
			certsn = new String(Hex.encode(oX509Cert.getSerialNumber().toByteArray()));
			
			if(null == certDao.getCertByCertsn(certsn, strActName)){
				Cert encCert = new Cert(); 
				encCert.setEnvsn(CommonConst.INPUT_SM2_ENC);
				encCert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
				encCert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);
				encCert.setCerttype(CommonConst.CERT_TYPE_SM2);
				encCert.setAlgtype(CommonConst.CERT_ALG_TYPE_ENC);
				encCert.setSignalg(2);
				encCert.setContainerid("");
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
				   encCert.setSavetype(CommonConst.SAVE_CERT_TYPE_BLUETOOTH);
				else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
				   encCert.setSavetype(CommonConst.SAVE_CERT_TYPE_SIM);
				encCert.setPrivatekey("");
				encCert.setDevicesn(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""));
				encCert.setCertsn(certsn);
				encCert.setCertchain("");
				encCert.setNotbeforetime(getCertNotbeforetime(enccert));
				encCert.setValidtime(getCertValidtime(enccert));
				encCert.setCertificate(enccert);
				encCert.setKeystore("");
				encCert.setEnccertificate(enccert);
					
				certDao.addCert(encCert,strActName);	
			}
			if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == saveType)
			   Toast.makeText(context, "蓝牙key内证书导入成功", Toast.LENGTH_SHORT).show();
			else if(CommonConst.SAVE_CERT_TYPE_SIM == saveType)
			   Toast.makeText(context, "蓝牙sim卡内证书导入成功", Toast.LENGTH_SHORT).show();
		}
		
	}

	private  void  applyByFace(){
		Intent intent = null;
		intent = new Intent(context, AuthChoiceActivity.class);
		intent.putExtra("needPay","true");
		startActivity(intent);

//		Intent intent = new Intent();
//		intent.setClass(getActivity(), com.sheca.umandroid.PayActivity.class);
//		intent.putExtra("loginAccount", AccountHelper.getRealName(getActivity()));
//		intent.putExtra("loginId", AccountHelper.getIdcardno(getActivity()));
//		getActivity().startActivity(intent);

        //AuthController controller = new AuthController();
        //controller.faceAuth(getActivity(),true);
//        controller.sdkFaceAuth(getActivity(),true);
	}

	String scanMsg="";

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_CODE) {
			// 处理扫描结果（在界面上显示）
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();

				final String str = bundle.getString("result");


				try {


					final JSONObject jb = JSONObject.fromObject(str);

					if (jb.containsKey("randomNumber")) {
						scanMsg = jb.optString("randomNumber");
					} else if (jb.containsKey("message")) {
						scanMsg = jb.optString("message");
					} else if (jb.containsKey("messages")) {
						scanMsg = jb.optString("messages");
					}


					if (jb.containsKey("appID") && jb.optString("appID").length() > 0) {

						final UniTrust uniTrust = new UniTrust(getActivity(), false);
						new Thread(new Runnable() {
							@Override
							public void run() {
								String param = ParamGen.IsValidApplication(getActivity(), jb.optString("appID"));
								String result = uniTrust.IsValidApplication(param);
								final APPResponse response = new APPResponse(result);
								final int retCode = response.getReturnCode();
								final String retMsg = response.getReturnMsg();
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {

										if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
//                                            String authRestult="true";
											String authRestult =JSONObject.fromObject(response.getResult()).getString(com.sheca.umplus.util.CommonConst.RETURN_RESULT);


											if (authRestult.equals("true")) {
												String strReslut = str;
												if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
													sealScan(strReslut);

													return;
												}


												if (strReslut.indexOf("?") == -1) {
													strReslut = parseJSONScanResult(strReslut);
												} else {
													isJSONDate = false;
												}


												final String scanResult = strReslut;
												strScanResult = scanResult;


												try {
//
//					certController.scan(getActivity(),strScanResult);
													if ("-1".equals(scanResult)) {
														throw new Exception("二维码格式解析异常");
													}

													final String urlPath = WebUtil.getUrlPath(scanResult);

													if ("".equals(urlPath)) {
														throw new Exception("二维码格式错误");
													}

													Map<String, String> params = WebUtil
															.getURLRequest(scanResult);

													String type = params.get(CommonConst.PARAM_TYPE);

													if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
														showScanCert(scanResult);
													} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
														showScanCert(scanResult);
													} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
														showScanCert(scanResult);
													} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
														showScanCert(scanResult);
													} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
														showScanSeal(scanResult);
													} else {
														throw new Exception("二维码内容错误");
													}

												} catch (Exception e) {
													Toast.makeText(getActivity(), "扫码失败", Toast.LENGTH_LONG).show();
													//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setMessage(e.getMessage());
//                    builder.setIcon(R.drawable.warning);
//                    builder.setTitle("异常");
//                    builder.setNegativeButton("二维码内容",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int which) {
//                                    dialog.dismiss();
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(
//                                            getContext());
//                                    builder.setMessage(scanResult);
//                                    builder.setIcon(R.drawable.alert);
//                                    builder.setTitle("二维码内容");
//                                    builder.setNegativeButton(
//                                            "复制",
//                                            new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                                                    cmb.setPrimaryClip(ClipData.newPlainText("scanResult", scanResult));
//                                                }
//                                            });
//                                    builder.setPositiveButton(
//                                            "关闭",
//                                            new DialogInterface.OnClickListener() {
//
//                                                public void onClick(
//                                                        DialogInterface dialog,
//                                                        int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                    builder.show();
//                                }
//
//                            });
//                    builder.setPositiveButton("关闭",
//                            new DialogInterface.OnClickListener() {
//
//                                public void onClick(DialogInterface dialog,
//                                                    int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    builder.show();
												}
											} else {

												Toast.makeText(getActivity(), "应用不合法", Toast.LENGTH_LONG).show();

											}

										} else {


											Toast.makeText(getActivity(), "获取应用状态失败", Toast.LENGTH_LONG).show();
										}
									}
								});

							}
						}).start();

					} else if (jb.containsKey("appID") && jb.optString("appID").length() == 0) {
						Toast.makeText(getActivity(), "应用不合法", Toast.LENGTH_LONG).show();
					} else {
						String strReslut = str;

						if (jb.containsKey("type") && jb.optString("type").equals("DIGEST")) {
							sealScan(strReslut);

							return;
						}


						if (strReslut.indexOf("?") == -1) {
							strReslut = parseJSONScanResult(strReslut);
						} else {
							isJSONDate = false;
						}


						final String scanResult = strReslut;
						strScanResult = scanResult;


						try {
//
//					certController.scan(getActivity(),strScanResult);
							if ("-1".equals(scanResult)) {
								throw new Exception("二维码格式解析异常");
							}

							final String urlPath = WebUtil.getUrlPath(scanResult);

							if ("".equals(urlPath)) {
								throw new Exception("二维码格式错误");
							}

							Map<String, String> params = WebUtil
									.getURLRequest(scanResult);

							String type = params.get(CommonConst.PARAM_TYPE);

							if (urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase()) != -1) {
								showScanCert(scanResult);
							} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase()) != -1) {
								showScanCert(scanResult);
							} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase()) != -1) {
								showScanCert(scanResult);
							} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase()) != -1) {
								showScanCert(scanResult);
							} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase()) != -1) {
								showScanSeal(scanResult);
							} else {
								throw new Exception("二维码内容错误");
							}

						} catch (Exception e) {
							Toast.makeText(getActivity(), "扫码失败", Toast.LENGTH_LONG).show();
							//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setMessage(e.getMessage());
//                    builder.setIcon(R.drawable.warning);
//                    builder.setTitle("异常");
//                    builder.setNegativeButton("二维码内容",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int which) {
//                                    dialog.dismiss();
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(
//                                            getContext());
//                                    builder.setMessage(scanResult);
//                                    builder.setIcon(R.drawable.alert);
//                                    builder.setTitle("二维码内容");
//                                    builder.setNegativeButton(
//                                            "复制",
//                                            new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                                                    cmb.setPrimaryClip(ClipData.newPlainText("scanResult", scanResult));
//                                                }
//                                            });
//                                    builder.setPositiveButton(
//                                            "关闭",
//                                            new DialogInterface.OnClickListener() {
//
//                                                public void onClick(
//                                                        DialogInterface dialog,
//                                                        int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                    builder.show();
//                                }
//
//                            });
//                    builder.setPositiveButton("关闭",
//                            new DialogInterface.OnClickListener() {
//
//                                public void onClick(DialogInterface dialog,
//                                                    int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    builder.show();
						}


					}


				} catch (Exception e) {

				}


			}
		} else if (requestCode == SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				doScan(bundle.getString("ServiecNo"),
						bundle.getString("OriginInfo"),
						bundle.getString("Sign"),
						bundle.getString("Cert"),
						bundle.getString("CertSN"),
						bundle.getString("UniqueID"),
						bundle.getString("CertType"),
						bundle.getString("SaveType"),
						bundle.getString("AppID"),
						bundle.getString("MsgWrapper"));
			}
			if (resultCode == RESULT_CANCELED) {

			}
		} else if (requestCode == REQUEST_SEARCH_BT) {
			if (resultCode == RESULT_OK) {
				doInputCert();
			}
			if (resultCode == RESULT_CANCELED) {

			}
		} else if (requestCode == SCAN_SEAL_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				doSealScan(bundle.getString("ServiecNo"),
						bundle.getString("OriginInfo"),
						bundle.getString("CertSN"),
						bundle.getString("SealSN"),
						bundle.getString("AppID"),
						bundle.getString("MsgWrapper"));
			}
			if (resultCode == RESULT_CANCELED) {
				Bundle bundle = data.getExtras();
				if (null != bundle.getString("SealSN") && ("".equals(bundle.getString("SealSN"))))
					Toast.makeText(getContext(), "签章证书不匹配或不存在", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getContext(), "操作取消", Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == SEAL_SIGN) {
			if (resultCode == RESULT_OK) {
//                doInputCert();

				uploadLogRecord("6", data.getStringExtra("result"));

			} else {

			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}


	//扫码签章
	private void sealScan(String result) {


		JSONObject jb = JSONObject.fromObject(result);


		String certSn = jb.optString("certSn");
		Cert cert = certDao.getCertByCertsn(certSn, "");

		if (cert == null) {

			Toast.makeText(getActivity(), "证书不存在", Toast.LENGTH_LONG).show();
		} else {
			if (certDao.verifyCert(cert) == 0) {

				mScanToken = "";
				Intent intent = new Intent(getActivity(), SealSignActivity.class);
				intent.putExtra("certSn", certSn);
				intent.putExtra("result", result);
				getActivity().startActivityForResult(intent, SEAL_SIGN);
			} else {
				Toast.makeText(getActivity(), "证书已过期,无法使用", Toast.LENGTH_LONG).show();
			}

		}
	}
	
	
	private  String  parseJSONScanResult(String scanResult){
		String  strReturn = "";
		if(!scanResult.startsWith("{"))
		     scanResult ="{"+scanResult;
		if(!scanResult.endsWith("}"))
			 scanResult +="}";

		try{
		   JSONObject jb = JSONObject.fromObject(scanResult);
		   String serviceURL = jb.getString(CommonConst.QR_SERVICEURL);
		   String actionName =  jb.getString(CommonConst.QR_ACTIONNAME).replace("_", "");
		   String bizSN =  jb.getString(CommonConst.PARAM_BIZSN);
		   String message  =  "";
		   String certSN  =  "";
		   boolean isWrapper = false;
		   
		   if(jb.containsKey(CommonConst.PARAM_APPID)){
		      if(null != jb.getString(CommonConst.PARAM_APPID) && !"".equals(jb.getString(CommonConst.PARAM_APPID)))
		         strScanAppName = jb.getString(CommonConst.PARAM_APPID);
		      else
		    	 strScanAppName = ""; 
		   }else
			   strScanAppName = CommonConst.UM_APPID;
		   		   
		   if(actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase())){
		        message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_SignEx.toLowerCase())){
			    JSONArray transitListArray = jb.getJSONArray(CommonConst.PARAM_MESSAGES);
		    	for (int i = 0; i < transitListArray.size(); i++) {
		    		message += transitListArray.getString(i)+CommonConst.UM_SPLIT_STR;    
	            }
		    	
		    	if(jb.containsKey(CommonConst.PARAM_MSGWRAPPER)){
		    		String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
		    		if(Integer.parseInt(mWrapper) == 1)
		    		   isWrapper = true;
		    		else
		    		   isWrapper = false;
		    	}
		    	
		    	isSignEx = true;
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_EnvelopeDecrypt.toLowerCase())){
		        message = jb.getString(CommonConst.PARAM_ENCRYPT_DATE);
		        certSN = jb.getString(CommonConst.PARAM_ENCRYPT_CERTSN);
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_Sign.toLowerCase())){
			   if(!isSignEx)
		           message = jb.getString(CommonConst.PARAM_MESSAGE);
			   
			   if(jb.containsKey(CommonConst.PARAM_MSGWRAPPER)){
		    		String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
		    		if(Integer.parseInt(mWrapper) == 1)
		    		   isWrapper = true;
		    		else
		    		   isWrapper = false;
		       }
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_SEAL.toLowerCase())){
			   if(jb.containsKey(CommonConst.PARAM_RANDOM_NUMBER))
			      message = jb.getString(CommonConst.PARAM_RANDOM_NUMBER);
				   
			   if(jb.containsKey(CommonConst.PARAM_MSGWRAPPER)){
			      String mWrapper = jb.getString(CommonConst.PARAM_MSGWRAPPER);
			      if(Integer.parseInt(mWrapper) == 1)
			         isWrapper = true;
			      else
			         isWrapper = false;
			   }
				   
			   if(jb.containsKey(CommonConst.PARAM_ENCRYPT_CERTSN))
			      certSN = jb.getString(CommonConst.PARAM_ENCRYPT_CERTSN);
		   }
		
		   if(actionName.toLowerCase().equals(CommonConst.QR_Login.toLowerCase())){
		        strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_RANDOM_NUMBER+"=%s", 
		    		                      serviceURL,CommonConst.QR_Login,bizSN,message);
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_SignEx.toLowerCase())){
			   if(isWrapper)
			       strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGES+"=%s&"+CommonConst.PARAM_MSGWRAPPER+"=%s", 
                                              serviceURL,CommonConst.QR_SignEx,bizSN,message,"1");
			    else
			       strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGES+"=%s", 
		                                      serviceURL,CommonConst.QR_SignEx,bizSN,message);
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_EnvelopeDecrypt.toLowerCase())){
			    strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_ENCRYPT_DATE+"=%s&"+CommonConst.PARAM_ENCRYPT_CERTSN+"=%s", 
		                                   serviceURL,CommonConst.QR_EnvelopeDecrypt,bizSN,message,certSN);
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_Sign.toLowerCase())){
			   if(!isSignEx){
			    	if(isWrapper)
			    	   strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s&"+CommonConst.PARAM_MSGWRAPPER+"=%s",  
                                                 serviceURL,CommonConst.QR_Sign,bizSN,message,"1");
			    	else
			           strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s", 
		                                        serviceURL,CommonConst.QR_Sign,bizSN,message);
			    }
		   }else if(actionName.toLowerCase().equals(CommonConst.QR_SEAL.toLowerCase())){
			    if(!isSignEx){
		    		if(isWrapper)
				       strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s&"+CommonConst.PARAM_MSGWRAPPER+"=%s&"+CommonConst.PARAM_ENCRYPT_CERTSN+"=%s",  
	                                                  serviceURL,CommonConst.QR_SEAL,bizSN,message,"1",certSN);
				    else
				       strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s&"+CommonConst.PARAM_ENCRYPT_CERTSN+"=%s", 
			                                        serviceURL,CommonConst.QR_SEAL,bizSN,message,certSN);
			    }

			    strScanSealResult = serviceURL;
		   }	 
					 
		   isJSONDate = true;					
		}catch(Exception ex){
			strReturn = "-1";
		}
		
		return  strReturn;
		
	}
	
	
	private  void  doInputCert(){
		final Handler handler = new Handler(context.getMainLooper());
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
		   showProgDlgCert("导入蓝牙key内证书中...");
		else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
		   showProgDlgCert("导入蓝牙sim卡内证书中...");
		
		workHandler.post(new Runnable() {
			@Override
			public void run() {    	
				if(checkBTDevice(true)){
					handler.post(new Runnable() {
						@Override
					    public void run() {
							closeProgDlgCert();
					        setBlueToothPwd(handler); 
						}
				    }); 
				}else{	
					handler.post(new Runnable() {
							@Override
						    public void run() {
					           closeProgDlgCert();
					           Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
							}
					    }); 
				}
			}
		  });
		
	}
			
	
	private  void   doScan(final String token,final String orgDate,final String signDate,final String cert,final String certSN,final String uniID,final String certType,final String saveType,final String appID,final String msgWrapper){
		wakeLock = ((PowerManager) activity
				.getSystemService(activity.POWER_SERVICE))
				.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ON_AFTER_RELEASE,
						"Login");
		wakeLock.acquire();

		progDialog = new ProgressDialog(context);
		if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN)
		     progDialog.setMessage("正在签名...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN)
		     progDialog.setMessage("正在登录...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT)
			 progDialog.setMessage("正在解密...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX)
			 progDialog.setMessage("正在批量签名...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
			 progDialog.setMessage("正在签章...");
		
		progDialog.setCancelable(false);
		progDialog.show();
		
		new Thread() {
			@Override
			public void run() {	
				if (signDate != null) {
				  Map<String, String> postParams = new HashMap<String, String>();
				  String postHttpParams = "";
				  JSONObject jo = null;
					
				  try{
					if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN){
					   postParams.put("bizSN", token);
					   postParams.put("appID", appID);
					   postParams.put("idNumber", uniID);
					   postParams.put("cert", cert);
					   postParams.put("message", orgDate);
					   postParams.put("signatureValue", signDate);
					   postParams.put("msgWrapper", msgWrapper);
					   
					   postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
					                     "&appID="+URLEncoder.encode(appID, "UTF-8")+
					                     "&idNumber="+URLEncoder.encode(uniID, "UTF-8")+
					                     "&cert="+URLEncoder.encode(cert, "UTF-8")+
					                     "&message="+URLEncoder.encode(orgDate, "UTF-8")+
					                     "&signatureValue="+URLEncoder.encode(signDate, "UTF-8")+
					                     "&msgWrapper="+URLEncoder.encode(msgWrapper, "UTF-8");
					   
					   if(isJSONDate){					   
					      if(CommonConst.CERT_TYPE_SM2.equals(certType)){
					         postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
					         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
					         
					         postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
			        		                   "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
					      }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
						     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
					            postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
					            postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
						     }else{
							    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
							    postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
						     }
					         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
					         postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
					      }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
						     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
						        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
						        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
						     }else{
						        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
						        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
						     }
						     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
						     postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
					      }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
						     postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
						     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
						     
						     postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
	        		                           "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
					      }
					   }else{
						   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
							   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
							   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
							   
							   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
  		                                         "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
							   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
							       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
							   }else{
						           postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
						           postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
							   }
						       postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						       postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
							   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
							       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
							   }else{
							       postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
							       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
							   }
							   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							   postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
							   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
							   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							   
							   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
                                                 "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						   }					   
					   }
					}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN){
						postParams.put("bizSN", token);
						postParams.put("appID", appID);
						postParams.put("idNumber", uniID);
						postParams.put("randomNumber", orgDate);
						postParams.put("message", orgDate);
						postParams.put("cert", cert);
						postParams.put("signatureValue", signDate);
						
						postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
					                      "&appID="+URLEncoder.encode(appID, "UTF-8")+
					                      "&idNumber="+URLEncoder.encode(uniID, "UTF-8")+
					                      "&randomNumber="+URLEncoder.encode(orgDate, "UTF-8")+
					                      "&message="+URLEncoder.encode(orgDate, "UTF-8")+
					                      "&cert="+URLEncoder.encode(cert, "UTF-8")+
					                      "&signatureValue="+URLEncoder.encode(signDate, "UTF-8");
						
						if(isJSONDate){					   
						      if(CommonConst.CERT_TYPE_SM2.equals(certType)){
						         postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         
						         postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
		        		                           "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
						            postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
						            postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
								    postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
								    postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
						         postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
						         postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
							     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH, "UTF-8");
							     }else{
							        postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_RSA);
							        postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA, "UTF-8");
							     }
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
							     postParams.put("signatureAlgorithm", CommonConst.USE_CERT_SCAN_ALG_SM2);
							     postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
							     
							     postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2, "UTF-8")+
      		                                       "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
						      }						  
						}else{
							   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
		                                             "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
							           postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
							           postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
							       postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_PERSONAL);
							       postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								       postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_RSA, "UTF-8");
								   }else{
								       postParams.put("signatureAlgorithm", CommonConst.USE_CERT_ALG_RSA);
								       postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA, "UTF-8");
								   }
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   postHttpParams += "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
								   postParams.put("signatureAlgorithm", CommonConst.CERT_ALG_SM2);
								   postParams.put("certType", ""+CommonConst.ACCOUNT_TYPE_COMPANY);
								   
								   postHttpParams += "&signatureAlgorithm="+URLEncoder.encode(CommonConst.CERT_ALG_SM2, "UTF-8")+
                                                     "&certType="+URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY, "UTF-8");
							   }					  
						}
					}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX){
						   jo = new JSONObject();
		    	  		   jo.put("bizSN", URLEncoder.encode(token,"UTF-8"));
		    	  	       jo.put("appID", URLEncoder.encode(appID,"UTF-8"));    	  	    
		    	  	       jo.put("idNumber", URLEncoder.encode(uniID,"UTF-8"));
		    	  	       jo.put("cert", URLEncoder.encode(cert,"UTF-8"));
		    	  	       jo.put("msgWrapper", URLEncoder.encode(msgWrapper,"UTF-8"));

		    	  	       String orgMsg = orgDate;
		    	  	       if(orgMsg.endsWith(CommonConst.UM_SPLIT_STR))
		    	  	          orgMsg = orgMsg.substring(0,orgMsg.lastIndexOf(CommonConst.UM_SPLIT_STR));
		    	  	       ArrayList<String> arrayList = new ArrayList<String>();
			    	  	   for(int i = 0;i<orgMsg.split(CommonConst.UM_SPLIT_STR).length;i++){
			    	  	       arrayList.add(i,URLEncoder.encode(orgMsg.split(CommonConst.UM_SPLIT_STR)[i],"UTF-8"));
			    	  	   }
			    	  	   jo.element("messages", arrayList);
			    	  	    
			    	  	   orgMsg = signDate;
		    	  	       arrayList = new ArrayList<String>();
			    	  	   for(int i = 0;i<orgMsg.split(CommonConst.UM_SPLIT_STR).length;i++){
			    	  	       arrayList.add(i,URLEncoder.encode(orgMsg.split(CommonConst.UM_SPLIT_STR)[i],"UTF-8"));
			    	  	   }
			    	  	   jo.element("signatureValues", arrayList);
						   
						   if(isJSONDate){	
							   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
								     jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2,"UTF-8"));
								     jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL,"UTF-8"));
							   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
								     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								    	 jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH,"UTF-8"));
							         }else{
							        	 jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA,"UTF-8"));
								     }
								     jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL,"UTF-8"));
							   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
								     if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
								    	 jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA_BLUETOOTH,"UTF-8"));
								     }else{
								    	 jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_RSA,"UTF-8"));
								     }
								     jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY,"UTF-8"));								   
							   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
								   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_SCAN_ALG_SM2,"UTF-8"));
								   jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY,"UTF-8")); 
							   }
						   }else{
							   if(CommonConst.CERT_TYPE_SM2.equals(certType)){
								   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_SM2,"UTF-8"));
								   jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL,"UTF-8"));
							   }else if(CommonConst.CERT_TYPE_RSA.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
									   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_RSA,"UTF-8"));
								   }else{
									   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA,"UTF-8"));
								   }
								   jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_PERSONAL,"UTF-8"));
							   }else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(certType)){
								   if((""+CommonConst.SAVE_CERT_TYPE_BLUETOOTH).equals(saveType) || (""+CommonConst.SAVE_CERT_TYPE_SIM).equals(saveType)){
									   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_RSA,"UTF-8"));
								   }else{
									   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.USE_CERT_ALG_RSA,"UTF-8"));
								   }
								   jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY,"UTF-8"));
							   }else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType)){
								   jo.put("signatureAlgorithm", URLEncoder.encode(CommonConst.CERT_ALG_SM2,"UTF-8"));
								   jo.put("certType", URLEncoder.encode(""+CommonConst.ACCOUNT_TYPE_COMPANY,"UTF-8"));
							   }					   
						   }
					}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT){
						   postParams.put("bizSN", token);
						   postParams.put("appID", appID);
						   postParams.put("encryptData", orgDate);
						   postParams.put("message", signDate);
						   
						   postHttpParams += "bizSN="+URLEncoder.encode(token, "UTF-8")+
				                             "&appID="+URLEncoder.encode(appID, "UTF-8")+
				                             "&encryptData="+URLEncoder.encode(orgDate, "UTF-8")+			                          
				                             "&message="+URLEncoder.encode(signDate, "UTF-8");
					}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL){
						   String sealSignDate = "";
						   sealSignDate = String.format("%s&%s&%s&%s&%s", 
								                     CommonConst.SEAL_SCAN_APP_CODE,
								                     token,
								                     signDate,
								                     certSN,
								                     CommonConst.SEAL_SCAN_APP_PWD);
						   sealSignDate = PKIUtil.getSHADigest(sealSignDate,"SHA-256","SUN");
						   postHttpParams += "AppCode="+URLEncoder.encode(CommonConst.SEAL_SCAN_APP_CODE, "UTF-8")+
				                             "&BizSN="+URLEncoder.encode(token, "UTF-8")+
				                             "&HashSign="+URLEncoder.encode(signDate, "UTF-8")+		
				                             "&CertSN="+URLEncoder.encode(certSN, "UTF-8")+
				                             "&SignatureValue="+URLEncoder.encode(sealSignDate, "UTF-8");
					}
				  }catch(Exception e){
						Log.e(CommonConst.TAG, e.getMessage(),e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
				  }

				  try {
						final String urlPath = WebUtil.getUrlPath(strScanResult);
						String strPostUrlPath = urlPath.substring(0,urlPath.lastIndexOf("/"));
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
							strPostUrlPath = strScanSealResult;
						
						String sResp = null;
						//sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX){
							sResp = WebClientUtil.postJsonArray(strPostUrlPath, jo.toString(), 5000);
						}else{
							WebClientUtil.mBScanPost = true;
						    sResp = WebClientUtil.postHttpClientJson(strPostUrlPath,postHttpParams, 5000);
						}
						
						strPrint = sResp;
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL){
							if("".equals(strPrint) || null == strPrint || "null".equals(strPrint)){
								handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
							}else{
							   JSONObject jb = JSONObject.fromObject(strPrint);
							   String resultStr = "";
							   String returnStr = "";
							   String returnStrBizSN = "";
							   String returnStrPdfHash = "";
							   String returnStrCertSN = "";
							   String returnStrSignUrl = "";
							   String strReturn = "";
				
							   if(jb.containsKey("RetCode"))
		            		      resultStr = jb.getString("RetCode");
							   if(jb.containsKey("RetMsg"))
		            		      returnStr = jb.getString("RetMsg");	
							
							   if("1".equals(resultStr)){
								   if(jb.containsKey("BizSN"))
									   returnStrBizSN = jb.getString("BizSN");
								   /*
								   if(!returnStrBizSN.equals(token))
									   handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
								   */
								   if(jb.containsKey("PdfHash"))
									   returnStrPdfHash = jb.getString("PdfHash");
								   if(jb.containsKey("CertSN"))
									   returnStrCertSN = jb.getString("CertSN");   
								   if(jb.containsKey("SignUrl"))
									   returnStrSignUrl = jb.getString("SignUrl");   
								   
								   if(null == returnStrPdfHash || "null".equals(returnStrPdfHash)){
									   handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
								   }else{
								       if(!"".equals(returnStrPdfHash) ){
								    	   if(scanSignCount < 0){
								    		   handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
								    		   return;
								    	   }
								    	   
								    	  scanSealNum++;
								    	  if(scanSealNum <= scanSignCount){
								    	     isSealSign = true;
								    	     SealSignUtil.context = context;
								    	     SealSignUtil.activity = activity;
								    	     strScanSealResult = returnStrSignUrl;
								    	     SealSignUtil.strBizSN = returnStrBizSN;
								    	     SealSignUtil.strOrgDate = returnStrPdfHash;
								    	     SealSignUtil.strCertSN = certSN;
								    	     SealSignUtil.strUniID = uniID;
								    	     SealSignUtil.strAccountName = accountDao.getLoginAccount().getName();
								    	     SealSignUtil.strAppID = appID ;
								    	     //SealSignUtil.strMsgWrapper ="1";
										     handler.sendEmptyMessage(SEAL_SIGN_SCAN_SUCCESS);
										     return;
								    	  }else
								    		  isSealSign = false;
									   }else{
										   isSealSign = false;
									   }
								   }
							   }else{
								   strScanErr = returnStr;
								   handler.sendEmptyMessage(SEAL_SCAN_FAILURE);
							   }
							}
						}

						int signALg = 1;
						if(CommonConst.CERT_TYPE_SM2.equals(certType) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(certType) )
							signALg = 2;
						/*if (!"ok".equals(sResp)) {
							Message msg = new Message();
							msg.what = FAILURE;
							Bundle data = new Bundle();
							data.putString("result", sResp);
							msg.setData(data);
							handler.sendMessage(msg);
						} else {*/
						//showMessage();
						WebClientUtil.mBScanPost = false;
						
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN){
							if(isJSONDate){
							   saveLog(OperationLog.LOG_TYPE_SIGN,
									   certSN,
									   orgDate,
									   orgDate,
									   signDate,signALg);
							}else{
							   saveLog(OperationLog.LOG_TYPE_SIGN,
									   certSN,
									   new String(Base64.decode(URLDecoder.decode(orgDate,"UTF-8"))),
									   new String(Base64.decode(URLDecoder.decode(orgDate,"UTF-8"))),
									   signDate,signALg);
							}
							
							handler.sendEmptyMessage(SIGN_SUCCESS);
						}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN){
							saveLog(OperationLog.LOG_TYPE_LOGIN,
									certSN,
									token, 
									urlPath, 
									signDate,signALg);
							
							handler.sendEmptyMessage(LOGIN_SUCCESS);
						}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX){
							if(isJSONDate){
							     saveLog(OperationLog.LOG_TYPE_DAO_SIGNEX,
									    certSN,
									    orgDate.substring(0,orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),
									    orgDate.substring(0,orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),
									    signDate,signALg);
							}else{
								 saveLog(OperationLog.LOG_TYPE_DAO_SIGNEX,
										 certSN,
										 new String(Base64.decode(URLDecoder.decode(orgDate.substring(0,orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),"UTF-8"))),
										 new String(Base64.decode(URLDecoder.decode(orgDate.substring(0,orgDate.lastIndexOf(CommonConst.UM_SPLIT_STR)),"UTF-8"))),
										 signDate,signALg);

							}
							
							handler.sendEmptyMessage(SIGNEX_SUCCESS);					
						}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT){
							 saveLog(OperationLog.LOG_TYPE_DAO_ENVELOP_DECRYPT,
									 certSN,
									 orgDate, 
									 orgDate, 
									 signDate,signALg);
							 
							 handler.sendEmptyMessage(ENVELOP_DECRYPT_SUCCESS);
						}else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL){
							 handler.sendEmptyMessage(SEAL_SCAN_SUCCESS);
						}

						//}
				  } catch (Exception e) {
						Log.e(CommonConst.TAG, e.getMessage(),
								e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(LOGIN_UPLOAD_FAILURE);
				  }
				}
				
				if (progDialog.isShowing()) {
					progDialog.dismiss();
				}

				if (wakeLock != null) {
					wakeLock.release();
				}
			}
		}.start();
	}
	
	private  void   doSealScan(final String serviceNo,final String orgDate,final String certSN,final String sealSN,final String appID,final String msgWrapper){
		wakeLock = ((PowerManager) activity
				.getSystemService(activity.POWER_SERVICE))
				.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ON_AFTER_RELEASE,
						"Login");
		wakeLock.acquire();

		progDialog = new ProgressDialog(context);
		if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGN)
		     progDialog.setMessage("正在签名...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_LOGIN)
		     progDialog.setMessage("正在登录...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT)
			 progDialog.setMessage("正在解密...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX)
			 progDialog.setMessage("正在批量签名...");
		else if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL)
			 progDialog.setMessage("正在提交签章数据...");
		
		progDialog.setCancelable(false);
		progDialog.show();
		
		new Thread() {
			@Override
			public void run() {	
				if (sealSN != null) {
				  String postHttpParams = "";
				  JSONObject jo = null;
					
				  try{
					  if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SEAL){
						   String signDate = "";
						   signDate = String.format("%s&%s&%s&%s", 
								                     CommonConst.SEAL_SCAN_APP_CODE,
								                     serviceNo,
								                     sealSN,
								                     CommonConst.SEAL_SCAN_APP_PWD);
						   signDate = PKIUtil.getSHADigest(signDate,"SHA-256","SUN");
						   postHttpParams += "AppCode="+URLEncoder.encode(CommonConst.SEAL_SCAN_APP_CODE, "UTF-8")+
				                             "&BizSN="+URLEncoder.encode(serviceNo, "UTF-8")+
				                             "&SealSN="+URLEncoder.encode(sealSN, "UTF-8")+			                          
				                             "&SignatureValue="+URLEncoder.encode(signDate, "UTF-8");
					  }
				  }catch(Exception e){
						Log.e(CommonConst.TAG, e.getMessage(),e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
				  }

				  try {
						final String urlPath = WebUtil.getUrlPath(strScanResult);
						String strPostUrlPath = strScanSealResult;
						String sResp = null;
						//sResp = WebClientUtil.postJson(strPostUrlPath,postParams, 5000);
						if(operatorType == CommonConst.CERT_OPERATOR_TYPE_SIGNEX){
							sResp = WebClientUtil.postJsonArray(strPostUrlPath, jo.toString(), 5000);
						}else{
							WebClientUtil.mBScanPost = true;
						    sResp = WebClientUtil.postHttpClientJson(strPostUrlPath,postHttpParams, 5000);
						}
						
						strPrint = sResp;
						WebClientUtil.mBScanPost = false;
						
						if("".equals(strPrint) || null == strPrint || "null".equals(strPrint)){
							handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
						}else{
						   JSONObject jb = JSONObject.fromObject(strPrint);
						   String resultStr = "";
						   String returnStr = "";
						   String returnStrBizSN = "";
						   String returnStrPdfHash = "";
						   String returnStrCertSN = "";
						   String returnStrSignUrl = "";
						   String strReturn = "";
		
						   if(jb.containsKey("RetCode"))
	            		      resultStr = jb.getString("RetCode");
						   if(jb.containsKey("RetMsg"))
	            		      returnStr = jb.getString("RetMsg");	
						
						   if("1".equals(resultStr)){
							  if(jb.containsKey("BizSN"))
								  returnStrBizSN = jb.getString("BizSN");
							  if(jb.containsKey("PdfHash"))
								  returnStrPdfHash = jb.getString("PdfHash");
							  if(jb.containsKey("CertSN"))
								  returnStrCertSN = jb.getString("CertSN");
							  if(jb.containsKey("SignUrl"))
								 returnStrSignUrl = jb.getString("SignUrl");
							  if(jb.containsKey("SignCount"))
								  scanSignCount = jb.getInt("SignCount");
							
							  if(!"".equals(returnStrSignUrl))
								 strScanSealResult = returnStrSignUrl;
		
							  if("1".equals(msgWrapper))
							     strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s&"+CommonConst.PARAM_MSGWRAPPER+"=%s&"+CommonConst.PARAM_CERTSN+"=%s",  
									                        returnStrSignUrl,CommonConst.QR_SEAL,returnStrBizSN,returnStrPdfHash,msgWrapper,certSN);
							  else
							     strReturn = String.format("%s/%s?"+CommonConst.PARAM_BIZSN+"=%s&"+CommonConst.PARAM_MESSAGE+"=%s&"+CommonConst.PARAM_CERTSN+"=%s", 
							                            returnStrSignUrl,CommonConst.QR_SEAL,returnStrBizSN,returnStrPdfHash,certSN);
						   }else{
								strScanErr = returnStr;
								handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
						   }
						   
						   strScanErr = strReturn;
						   handler.sendEmptyMessage(SEALINFO_SCAN_SUCCESS);
						}	
						
						//}
				  } catch (Exception e) {
						Log.e(CommonConst.TAG, e.getMessage(),
								e);
						// Toast.makeText(UniTrustMobileActivity.this,
						// "上传签名错误", Toast.LENGTH_LONG).show();
						handler.sendEmptyMessage(SEALINFO_SCAN_FAILURE);
				  }
				}
				
				if (progDialog.isShowing()) {
					progDialog.dismiss();
				}

				if (wakeLock != null) {
					wakeLock.release();
				}
			}
		}.start();	
	}
	
	
	private void verifyUrl(String scanResult) throws Exception {
		String urlPath = WebUtil.getUrlPath(scanResult);
		Map<String, String> params = WebUtil.getURLRequest(scanResult);

		String envsn = params.get(CommonConst.PARAM_ENVSN);
		String authcode = params.get(CommonConst.PARAM_AUTHCODE);

		String data = urlPath + "?envsn=" + envsn + "&authcode=" + authcode;
		String signdate = params.get(CommonConst.PARAM_SIGNDATE);

		BufferedInputStream bis = null;
		byte[] bCert = null;
		try {
			bis = new BufferedInputStream(activity.getAssets().open(CommonConst.CERT_FILENAME));
			bCert = new byte[bis.available()];
			bis.read(bCert);
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			throw new Exception("读取证书文件错误");
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
				}
			}
		}
		boolean verifyResult = false;
		try {
			verifyResult = PKIUtil
					.verifySign(data, signdate, new String(bCert));
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
		}

		if (!verifyResult) {
			throw new Exception("验证签名错误");
		}
	}

	private void applyCert(String scanResult) throws Exception {
		Map<String, String> params = WebUtil.getURLRequest(scanResult);
		String envsn = params.get(CommonConst.PARAM_ENVSN);

		Cert cert = null;
		cert = certDao.getCertByEnvsn(envsn,accountDao.getLoginAccount().getName());
		String p10 = "";
		if (cert != null) {
			int status = cert.getStatus();
			if (status == Cert.STATUS_DOWNLOAD_CERT) {
				throw new Exception("该证书已存在");
			} else if (status == Cert.STATUS_GEN_PRIVATEKEY) {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024);
				KeyPair keypair = keyGen.genKeyPair();
				String dn = "CN=" + envsn;
				X500Principal subjectName = new X500Principal(dn);
				
				com.sheca.PKCS10CertificationRequest kpGen = null;
				
				/*
				PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
						"SHA1withRSA", subjectName, keypair.getPublic(), null,
						keypair.getPrivate());
						*/
				p10 = new String(Base64.encode(kpGen.getEncoded()));
				uploadP10(scanResult, p10);

				cert.setPrivatekey(new String(Base64.encode(keypair
						.getPrivate().getEncoded())));
				cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
				cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
				certDao.updateCert(cert,accountDao.getLoginAccount().getName());
			}
		} else {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keypair = keyGen.genKeyPair();

			cert = new Cert();
			cert.setStatus(Cert.STATUS_GEN_PRIVATEKEY);
			cert.setUploadstatus(Cert.STATUS_UNUPLOAD_CERT);
			cert.setEnvsn(envsn);
			cert.setPrivatekey(new String(Base64.encode(keypair.getPrivate()
					.getEncoded())));
			certDao.addCert(cert,accountDao.getLoginAccount().getName());

			String dn = "CN=" + envsn;
			X500Principal subjectName = new X500Principal(dn);
			
			PKCS10CertificationRequest kpGen =  null;
			
			/* PKCS10CertificationRequest kpGen = new PKCS10CertificationRequest(
					"SHA1withRSA", subjectName, keypair.getPublic(), null,
					keypair.getPrivate()); */
			p10 = new String(Base64.encode(kpGen.getEncoded()));
			uploadP10(scanResult, p10);

			cert = certDao.getCertByEnvsn(envsn,accountDao.getLoginAccount().getName());
			cert.setStatus(Cert.STATUS_UPLOAD_PKCS10);
			certDao.updateCert(cert,accountDao.getLoginAccount().getName());
		}

		Thread.sleep(WAIT_TIME);

		downloadCert(scanResult);
	}

	private void uploadP10(String scanResult, String p10) throws Exception {
		handler.sendEmptyMessage(UPLOAD_P10);

		String urlPath = WebUtil.getUrlPath(scanResult);
		urlPath += CommonConst.WEBSERVICE_UPLOADPKCS10;

		Map<String, String> params = WebUtil.getURLRequest(scanResult);

		String envsn = params.get(CommonConst.PARAM_ENVSN);
		String authcode = params.get(CommonConst.PARAM_AUTHCODE);

		// urlPath += "?Envsn=" + envsn + "&AuthenticationCode=" + authcode
		// + "&P10=" + p10;
		String responseStr = "";

		Map postParams = new HashMap();
		postParams.put("Envsn", envsn);
		postParams.put("AuthenticationCode", authcode);
		postParams.put("P10", p10);

		try {
			responseStr = WebClientUtil.postJson(urlPath, postParams, 5000);
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			throw new Exception("上传P10失败,网络连接异常或无法访问更新服务");
		}

	}

	private void downloadCert(String scanResult) throws Exception {
		handler.sendEmptyMessage(DOWNLOAD_CERT);

		String urlPath = WebUtil.getUrlPath(scanResult);
		urlPath += CommonConst.WEBSERVICE_DOWNLOADCERT;

		Map<String, String> params = WebUtil.getURLRequest(scanResult);

		final String envsn = params.get(CommonConst.PARAM_ENVSN);
		String authcode = params.get(CommonConst.PARAM_AUTHCODE);

		// urlPath += "?Envsn=" + envsn + "&AuthenticationCode=" + authcode;
		String responseStr = "";

		Map postParams = new HashMap();
		postParams.put("Envsn", envsn);
		postParams.put("AuthenticationCode", authcode);

		try {
			responseStr = WebClientUtil.postJson(urlPath, postParams, 5000);
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			if(e.getMessage().indexOf("peer")!=-1)
				throw new Exception("无效的服务器请求");
			else				   
			    throw new Exception("下载证书失败,网络连接异常或无法访问更新服务");
		}

		final DownloadCertResponse responseObj = null;
		String resultStr = responseObj.getResult();

		if (!"0".equals(resultStr)) {
			String returnStr = responseObj.getReturn();
			throw new Exception("下载证书失败：" + returnStr);
		}

		Message msg = new Message();
		msg.what = SAVE_CERT;
		Bundle data = new Bundle();
		data.putString("responseStr", responseStr);
		data.putString("envsn", envsn);
		msg.setData(data);
		handler.sendMessage(msg);

	}

	private  void  showScanCert(final String scanResult){
		Map<String, String> params = WebUtil.getURLRequest(scanResult);
		final String urlPath = WebUtil.getUrlPath(scanResult);
	    String certsn = "";

	    if(null != params.get(CommonConst.PARAM_CERTSN))
			  certsn = params.get(CommonConst.PARAM_CERTSN);
	    if(null != params.get(CommonConst.PARAM_ENCRYPT_CERTSN))
			  certsn = params.get(CommonConst.PARAM_ENCRYPT_CERTSN);
	    
        Intent intent = new Intent(context, DaoActivity.class);

		Bundle extras = new Bundle();
		if(mState) {
			String strCertHash = mCertList.get(0).getCerthash();
			if(!"".equals(strCertHash)) {
				DaoActivity.strPwd = strCertHash;
				extras.putString("certhash", strCertHash);
			}
		}
		try {
			JSONObject jb = JSONObject.fromObject(scanResult);
			if (jb.containsKey("appAlg")) {
				if (jb.optString("appAlg").equals("SM2")) {
					extras.putBoolean("isSM2", true);
				} else if (jb.optString("appAlg").equals("RSA")) {
					extras.putBoolean("isSM2", false);
				}
			}
		} catch (Exception e) {

		}
		extras.putString("ScanDao", "scan");
		extras.putString("ServiecNo", params.get(CommonConst.PARAM_BIZSN));
		if(isJSONDate)
			extras.putString("IsJson", "isJson");
		 
		if(urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase())!=-1){			
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_RANDOM_NUMBER));
            if("".equals(strScanAppName))
               extras.putString("AppName", "扫码登录");
            else
               extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "1");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
		}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase())!=-1) {
			if(isSignEx){
		           try {
		        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
		      	   }catch (Exception e) {
					   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
				   }

		           if("".equals(strScanAppName))
		              extras.putString("AppName", "批量签名");
		           else
		              extras.putString("AppName", strScanAppName);
		           extras.putString("OperateState", "3");	 
		           if(null != params.get(CommonConst.PARAM_MSGWRAPPER))
		              extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));	 
		           else
		              extras.putString("MsgWrapper", "0");	
				}
				
		        if(isSignEx)
		        	extras.putString("IsSignEx", "isSignEx");
		        operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGNEX;
			}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase())!=-1) {
		        try {
		        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
		      	}catch (Exception e) {
					   extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
				}
		        
		        extras.putString("CertSN", certsn);
		        
		        if("".equals(strScanAppName))
		           extras.putString("AppName", "扫码解密");
		        else
		           extras.putString("AppName", strScanAppName);
		        extras.putString("OperateState", "4");	 
		        operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT;
			}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase())!=-1) {
	           try {
	        	   //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
	        	   //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
	        	 //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");    
	        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
	        	//extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
			   } catch (Exception e) {
				   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
			   }
	           extras.putString("CertSN", certsn);
	        
	           if("".equals(strScanAppName))
	              extras.putString("AppName", "扫码签名");
	           else
	              extras.putString("AppName", strScanAppName);
	           extras.putString("OperateState", "2");	 
	           if(null != params.get(CommonConst.PARAM_MSGWRAPPER))	        
	        	   extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
	           else
	        	   extras.putString("MsgWrapper", "0");

	           operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
		}else if(urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase())!=-1){			
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            if("".equals(strScanAppName))
               extras.putString("AppName", "扫码签章");
            else
               extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "5");
            extras.putString("CertSN", certsn);
            if(null != params.get(CommonConst.PARAM_MSGWRAPPER))	        
	        	   extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
	        else
	        	   extras.putString("MsgWrapper", "0");
                      
            operatorType = CommonConst.CERT_OPERATOR_TYPE_SEAL;
		}
		
		
		intent.putExtras(extras);
		startActivityForResult(intent, SCAN_CODE);
	}
	
	private  void  showScanSeal(final String scanResult){
		try {
			mSealData = getSealData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(context, "无印章,请先申请印章", Toast.LENGTH_SHORT).show();
			return;
		}
	    
   		if(mSealData.size() == 0){ 
   			Toast.makeText(context, "无印章,请先申请印章", Toast.LENGTH_SHORT).show();
			return;
   		}
		
		Map<String, String> params = WebUtil.getURLRequest(scanResult);
		final String urlPath = WebUtil.getUrlPath(scanResult);
	    String certsn = "";

	    if(null != params.get(CommonConst.PARAM_CERTSN))
			  certsn = params.get(CommonConst.PARAM_CERTSN);
	    if(null != params.get(CommonConst.PARAM_ENCRYPT_CERTSN))
			  certsn = params.get(CommonConst.PARAM_ENCRYPT_CERTSN);
	    
        Intent intent = new Intent(context, SealListActivity.class);	
		Bundle extras = new Bundle();
		extras.putString("ScanDao", "scan");
		extras.putString("ServiecNo", params.get(CommonConst.PARAM_BIZSN));
		if(isJSONDate)
			extras.putString("IsJson", "isJson");
		 
		if(urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase())!=-1){			
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_RANDOM_NUMBER));
            if("".equals(strScanAppName))
               extras.putString("AppName", "扫码登录");
            else
               extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "1");
            operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
		}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_SignEx.toLowerCase())!=-1) {
			   if(isSignEx){
		           try {
		        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
		      	   }catch (Exception e) {
					// TODO Auto-generated catch block
					   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGES));
				   }

		           if("".equals(strScanAppName))
		              extras.putString("AppName", "批量签名");
		           else
		              extras.putString("AppName", strScanAppName);
		           extras.putString("OperateState", "3");	 
		           if(null != params.get(CommonConst.PARAM_MSGWRAPPER))
		              extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));	 
		           else
		              extras.putString("MsgWrapper", "0");	
				}
				
		        if(isSignEx)
		        	extras.putString("IsSignEx", "isSignEx");
		        operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGNEX;
			}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_EnvelopeDecrypt.toLowerCase())!=-1) {
		        try {
		        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
		      	}catch (Exception e) {
					// TODO Auto-generated catch block
					   extras.putString("OriginInfo", params.get(CommonConst.PARAM_ENCRYPT_DATE));
				}
		        
		        extras.putString("CertSN", certsn);
		        
		        if("".equals(strScanAppName))
		           extras.putString("AppName", "扫码解密");
		        else
		           extras.putString("AppName", strScanAppName);
		        extras.putString("OperateState", "4");	 
		        operatorType = CommonConst.CERT_OPERATOR_TYPE_ENVELOP_DECRYPT;
			}else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase())!=-1) {
	           try {
	        	   //String   zhongguo =  URLEncoder.encode("你好!","UTF-8");
	        	   //zhongguo = URLDecoder.decode(zhongguo,"UTF-8");
	        	 //  zhongguo = URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8");    
	        	   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
	        	//extras.putString("OriginInfo", URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));//URLDecoder.decode(params.get(CommonConst.PARAM_MESSAGE),"UTF-8"));
			   } catch (Exception e) {
				// TODO Auto-generated catch block
				   extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
			   }
	           extras.putString("CertSN", certsn);
	        
	           if("".equals(strScanAppName))
	              extras.putString("AppName", "扫码签名");
	           else
	              extras.putString("AppName", strScanAppName);
	           extras.putString("OperateState", "2");	 
	           if(null != params.get(CommonConst.PARAM_MSGWRAPPER))	        
	        	   extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
	           else
	        	   extras.putString("MsgWrapper", "0");

	           operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
		}else if(urlPath.toLowerCase().indexOf(CommonConst.QR_SEAL.toLowerCase())!=-1){			
            extras.putString("OriginInfo", params.get(CommonConst.PARAM_MESSAGE));
            if("".equals(strScanAppName))
               extras.putString("AppName", "扫码签章");
            else
               extras.putString("AppName", strScanAppName);
            extras.putString("OperateState", "5");
            if(null != params.get(CommonConst.PARAM_MSGWRAPPER))	        
	        	   extras.putString("MsgWrapper", params.get(CommonConst.PARAM_MSGWRAPPER));
	        else
	        	   extras.putString("MsgWrapper", "0");
            
            extras.putString("CertSN", certsn);
                      
            operatorType = CommonConst.CERT_OPERATOR_TYPE_SEAL;
		}
		
		intent.putExtras(extras);
		startActivityForResult(intent, SCAN_SEAL_CODE);
	}

	private void selectCert(final String scanResult) {
		Map<String, String> params = WebUtil.getURLRequest(scanResult);
		final String urlPath = WebUtil.getUrlPath(scanResult);
	    String certsn = "";

		if(urlPath.toLowerCase().indexOf(CommonConst.QR_Login.toLowerCase())!=-1){
			operatorType = CommonConst.CERT_OPERATOR_TYPE_LOGIN;
		} else if (urlPath.toLowerCase().indexOf(CommonConst.QR_Sign.toLowerCase())!=-1) {
			operatorType = CommonConst.CERT_OPERATOR_TYPE_SIGN;
		}

		if(null != params.get(CommonConst.PARAM_CERTSN))
		  certsn = params.get(CommonConst.PARAM_CERTSN);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certListView = inflater.inflate(R.layout.certlist, null);
		ListView list = (ListView) certListView.findViewById(R.id.certlist);
		CertAdapter adapter = null;
		try {
			mData = getData(certsn);
			if (mData.size() == 0) {
				Toast.makeText(context, "不存在证书", Toast.LENGTH_LONG).show();
				return;
			}

			if (mData.size() == 1) {
				int certId = Integer.valueOf(mData.get(0).get("id"));
				viewCert(certId, scanResult);
				return;
			}
			adapter = new CertAdapter(activity, mData);
			list.setAdapter(adapter);
			AlertDialog.Builder builder = new Builder(context);
			builder.setIcon(R.drawable.view);
			builder.setTitle("请选择证书");
			builder.setView(certListView);
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			// builder.show();
			certListDialog = builder.create();
			certListDialog.show();
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(), e);
			Toast.makeText(context, "获取证书错误！", Toast.LENGTH_LONG).show();
		}

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int certId = Integer.valueOf(mData.get(position).get("id"));
				viewCert(certId, scanResult);

				certListDialog.dismiss();
			}

		});

	}

	private void sign(final Cert cert, final String scanResult) {
		final PasswordDialog.Builder builder = new PasswordDialog.Builder(
				context);
		builder.setMessage("请输入证书口令");
		builder.setTitle("提示");
		builder.setIcon(R.drawable.alert);
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

				final String sPwd = builder.getEditTextValue();
				if (sPwd != null && !"".equals(sPwd)) {
					final String sKeyStore = cert.getKeystore();
					byte[] bKeyStore = Base64.decode(sKeyStore);
					ByteArrayInputStream kis = new ByteArrayInputStream(
							bKeyStore);
					KeyStore oStore = null;
					try {
						oStore = KeyStore.getInstance("PKCS12");
						oStore.load(kis, sPwd.toCharArray());

					} catch (Exception e) {
						Toast.makeText(context, "口令错误", Toast.LENGTH_LONG)
								.show();
						return;
					}
					String url = scanResult;
					final String urlPath = WebUtil.getUrlPath(url);
					Map<String, String> params = WebUtil.getURLRequest(url);
					if (params.containsKey(CommonConst.PARAM_BIZSN)
							&& !"".equals(params.get(CommonConst.PARAM_BIZSN))
							&& params.containsKey(CommonConst.PARAM_MESSAGE)
							&& !"".equals(params.get(CommonConst.PARAM_MESSAGE))) {
						final String msgId = params
								.get(CommonConst.PARAM_BIZSN);
						final String message = params
								.get(CommonConst.PARAM_MESSAGE);

						wakeLock = ((PowerManager) activity
								.getSystemService(activity.POWER_SERVICE))
								.newWakeLock(
										PowerManager.SCREEN_BRIGHT_WAKE_LOCK
												| PowerManager.ON_AFTER_RELEASE,
										"Login");
						wakeLock.acquire();

						progDialog = new ProgressDialog(context);
						progDialog.setMessage("正在签名...");
						progDialog.setCancelable(false);
						progDialog.show();

						new Thread() {
							@Override
							public void run() {
								String sSign = null;
								try {
									sSign = PKIUtil.sign(message.getBytes(),
											sKeyStore, sPwd);
								} catch (Exception e) {
									Log.e(CommonConst.TAG, e.getMessage(), e);
									// Toast.makeText(UniTrustMobileActivity.this,
									// "数字签名错误", Toast.LENGTH_LONG).show();
									handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
								}
								if (sSign != null) {
									String sCert = cert.getCertificate();
									Map<String, String> postParams = new HashMap<String, String>();
									postParams.put("msgid", msgId);
									if ("".equals(sCertUnicode) || null == sCertUnicode) 
										 postParams.put("idNumber", accountDao.getLoginAccount().getIdentityCode());
									else
										 postParams.put("idNumber", getPersonID(sCertUnicode));
									postParams.put("cert", sCert);
									postParams.put("signatureValue", sSign);
									postParams.put("signatureAlgorithm", "SHA1withRSA");
									String sResp = null;
									try {
										String strPostUrlPath = urlPath.substring(0,urlPath.lastIndexOf("/"));
										sResp = WebClientUtil.postJson(strPostUrlPath,
												postParams, 5000);
										/*if (!"ok".equals(sResp)) {
											Message msg = new Message();
											msg.what = FAILURE;
											Bundle data = new Bundle();
											data.putString("result", sResp);
											msg.setData(data);
											handler.sendMessage(msg);
										} else {*/
										//showMessage();
											saveLog(OperationLog.LOG_TYPE_SIGN,
													cert.getCertsn(),
													new String(Base64.decode(message)),
													new String(Base64.decode(message)),
													sSign,1);
											handler.sendEmptyMessage(SIGN_SUCCESS);
										//}
									} catch (Exception e) {
										Log.e(CommonConst.TAG, e.getMessage(),
												e);
										// Toast.makeText(UniTrustMobileActivity.this,
										// "上传签名错误", Toast.LENGTH_LONG).show();
										handler.sendEmptyMessage(LOGIN_UPLOAD_FAILURE);
									}
								}
								if (progDialog.isShowing()) {
									progDialog.dismiss();
								}

								if (wakeLock != null) {
									wakeLock.release();
								}
							}
						}.start();

					} else {
						Toast.makeText(context, "URL参数错误", Toast.LENGTH_LONG)
								.show();
					}

					dialog.dismiss();
				} else {
					Toast.makeText(context, "请输入证书口令", Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.setPositiveButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.show();
	}

	private List<Map<String, String>> getData(String certsn) throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);

		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		if (certsn != null && !"".equals(certsn)) {
			certList.add(certDao.getCertByCertsn(certsn,strActName));
		} else {
			certList = certDao.getAllCerts(strActName);
		}

		for (Cert cert : certList) {
			 if(null == cert.getCertificate() ||"".equals(cert.getCertificate()))
					continue;
			 
			if(verifyCert(cert,false)){
	    		 if(verifyDevice(cert,false)){
	    			 if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
	    				 Map<String, String> map = new HashMap<String, String>();
				         map.put("id", String.valueOf(cert.getId()));

				         byte[] bCert = Base64.decode(cert.getCertificate());
				         String commonName = jse.getCertDetail(17, bCert);
				         String organization = jse.getCertDetail(14, bCert);

				         String strNotBeforeTime = jse.getCertDetail(11, bCert);
				         String strValidTime = jse.getCertDetail(12, bCert);
				         Date fromDate = sdf.parse(strNotBeforeTime);
				         Date toDate = sdf.parse(strValidTime);

				         map.put("organization", organization);
				         map.put("commonname", commonName);
				         map.put("validtime",
						 sdf2.format(fromDate) + " ~ " + sdf2.format(toDate));
				         list.add(map);
	    			 }
	    		 }
			}
		}

		return list;
	}
	
	private List<Map<String, String>> getSealData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		List<SealInfo> sealList = new ArrayList<SealInfo>();
		
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");
		
		sealList = sealDao.getAllSealInfos(strActName);

		for (SealInfo sealInfo : sealList) {
			 Cert cert = certDao.getCertByCertsn(sealInfo.getCertsn(), strActName);
			 if(null == cert.getCertificate() ||"".equals(cert.getCertificate()))
					continue;
			 
			if(verifyCert(cert,false)){
	    		 if(verifyDevice(cert,false)){
	    			 if (cert.getStatus() == Cert.STATUS_DOWNLOAD_CERT) {
	    				 Map<String, String> map = new HashMap<String, String>();
				         map.put("id", String.valueOf(sealInfo.getId()));
				         map.put("commonname", sealInfo.getSealname());
				       
				         list.add(map);
	    			 }
	    		 }
			}
		}

		return list;
	}
	
	private void login(final Cert cert, final String scanResult) {
		final PasswordDialog.Builder builder = new PasswordDialog.Builder(
				context);
		builder.setMessage("请输入证书口令");
		builder.setTitle("提示");
		builder.setIcon(R.drawable.alert);
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

				final String sPwd = builder.getEditTextValue();
				if (sPwd != null && !"".equals(sPwd)) {
					final String sKeyStore = cert.getKeystore();
					byte[] bKeyStore = Base64.decode(sKeyStore);
					ByteArrayInputStream kis = new ByteArrayInputStream(
							bKeyStore);
					KeyStore oStore = null;
					try {
						oStore = KeyStore.getInstance("PKCS12");
						oStore.load(kis, sPwd.toCharArray());

					} catch (Exception e) {
						Toast.makeText(context, "口令错误", Toast.LENGTH_LONG)
								.show();
						return;
					}
					String url = scanResult;
					final String urlPath = WebUtil.getUrlPath(url);
					Map<String, String> params = WebUtil.getURLRequest(url);
					if (params.containsKey(CommonConst.PARAM_BIZSN)
							&& !"".equals(params.get(CommonConst.PARAM_BIZSN))) {
						final String token = params
								.get(CommonConst.PARAM_BIZSN);

						final String randomNumber = params
								.get(CommonConst.PARAM_RANDOM_NUMBER);
						
						wakeLock = ((PowerManager) activity
								.getSystemService(activity.POWER_SERVICE))
								.newWakeLock(
										PowerManager.SCREEN_BRIGHT_WAKE_LOCK
												| PowerManager.ON_AFTER_RELEASE,
										"Login");
						wakeLock.acquire();

						progDialog = new ProgressDialog(context);
						progDialog.setMessage("正在登录...");
						progDialog.setCancelable(false);
						progDialog.show();

						new Thread() {
							@Override
							public void run() {
								String sSign = "";
								String sOrgMessage = "";
								
								try {
									sOrgMessage = String.format("%s=%s&%s=%s",CommonConst.PARAM_BIZSN,token,CommonConst.PARAM_RANDOM_NUMBER,randomNumber);
									sSign = PKIUtil.sign(sOrgMessage.getBytes(),
											sKeyStore, sPwd);
								} catch (Exception e) {
									Log.e(CommonConst.TAG, e.getMessage(), e);
									// Toast.makeText(UniTrustMobileActivity.this,
									// "数字签名错误", Toast.LENGTH_LONG).show();
									handler.sendEmptyMessage(LOGIN_SIGN_FAILURE);
								}
								if (sSign != null) {
									String sCert = cert.getCertificate();
									Map<String, String> postParams = new HashMap<String, String>();
									postParams.put("bizSN", token);
									if ("".equals(sCertUnicode) || null == sCertUnicode) 
									   postParams.put("idNumber", accountDao.getLoginAccount().getIdentityCode());
									else
									   postParams.put("idNumber", getPersonID(sCertUnicode));
									postParams.put("randomNumber", randomNumber);
									postParams.put("cert", sCert);
									postParams.put("signatureValue", sSign);
									postParams.put("signatureAlgorithm", "SHA1withRSA");
									String sResp = null;
									try {	
										String strPostUrlPath = urlPath.substring(0,urlPath.lastIndexOf("/"));
										sResp = WebClientUtil.postJson(strPostUrlPath,
												postParams, 5000);
										/*if (!"ok".equals(sResp)) {
											Message msg = new Message();
											msg.what = FAILURE;
											Bundle data = new Bundle();
											data.putString("result", sResp);
											msg.setData(data);
											handler.sendMessage(msg);
										} else {*/
										//showMessage();
											saveLog(OperationLog.LOG_TYPE_LOGIN,
													cert.getCertsn(),token, urlPath, sSign,1);
											handler.sendEmptyMessage(LOGIN_SUCCESS);
										//}
									} catch (Exception e) {
										Log.e(CommonConst.TAG, e.getMessage(),
												e);
										// Toast.makeText(UniTrustMobileActivity.this,
										// "上传签名错误", Toast.LENGTH_LONG).show();
										handler.sendEmptyMessage(LOGIN_UPLOAD_FAILURE);
									}
								}
								if (progDialog.isShowing()) {
									progDialog.dismiss();
								}

								if (wakeLock != null) {
									wakeLock.release();
								}
							}
						}.start();

					} else {
						Toast.makeText(context, "URL参数错误", Toast.LENGTH_LONG)
								.show();
					}

					dialog.dismiss();
				} else {
					Toast.makeText(context, "请输入口令", Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.setPositiveButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.show();
	}

	private String genP12(String privateKey, String pin, String cert,
			String chain) throws Exception {
		String p12 = "";
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream certBIn = new ByteArrayInputStream(
				Base64.decode(cert));
		Certificate certificate = cf.generateCertificate(certBIn);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ByteArrayInputStream bIn = new ByteArrayInputStream(
				Base64.decode(chain));
		CertPath oCertPath = cf.generateCertPath(bIn, "PKCS7");
		List certs = oCertPath.getCertificates();
		Certificate[] bChain = (Certificate[]) certs
				.toArray(new Certificate[certs.size() + 1]);
		bChain[certs.size()] = certificate;

		List certList = new ArrayList();
		for (Certificate c : bChain) {
			certList.add(c);
		}
		Collections.reverse(certList);
		bChain = (Certificate[]) certList.toArray(new Certificate[certList
				.size()]);

		KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(
				Base64.decode(privateKey));
		RSAPrivateKey privKey = (RSAPrivateKey) rsaKeyFac
				.generatePrivate(encodedKeySpec);
		ks.setKeyEntry("", privKey, pin.toCharArray(), bChain);

		ByteArrayOutputStream outp12 = new ByteArrayOutputStream();

		ks.store(outp12, pin.toCharArray());
		p12 = new String(Base64.encode(outp12.toByteArray()));
		outp12.close();
		return p12;
	}

	private void closeProgDlg() {
		if (progDialog.isShowing()) {
			progDialog.dismiss();
		}
		if (wakeLock != null) {
			wakeLock.release();
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle data = null;
			switch (msg.what) {
			case LOGIN_SIGN:
				break;
			case LOGIN_SIGN_FAILURE:
				progDialog.dismiss();
				Toast.makeText(context, "数字签名错误", Toast.LENGTH_LONG).show();
				break;
			case SEALINFO_SCAN_FAILURE:
				progDialog.dismiss();
				if(!"".equals(strScanErr))
				   Toast.makeText(context, "提交印章数据错误,"+strScanErr, Toast.LENGTH_LONG).show();
				else
				   Toast.makeText(context, "提交印章数据错误", Toast.LENGTH_LONG).show();
				break;
			case SEAL_SCAN_FAILURE:
				progDialog.dismiss();
				if(!"".equals(strScanErr))
				   Toast.makeText(context, "扫码签章错误,"+strScanErr, Toast.LENGTH_LONG).show();
				else
				   Toast.makeText(context, "扫码签章错误", Toast.LENGTH_LONG).show();	
				break;
			case LOGIN_UPLOAD:
				break;
			case LOGIN_UPLOAD_FAILURE:
				progDialog.dismiss();
				Toast.makeText(context, "上传签名日志错误,网络连接异常或无法访问更新服务", Toast.LENGTH_LONG).show();
				break;
			case FAILURE:
				progDialog.dismiss();
				data = msg.getData();
				Toast.makeText(context, data.getString("result"),
						Toast.LENGTH_LONG).show();
				break;
			case UPLOAD_P10:
				progDialog.setMessage("正在上传P10...");
				break;
			case DOWNLOAD_CERT:
				progDialog.setMessage("正在下载证书...");
				break;
			case LOGIN_SUCCESS:
				progDialog.dismiss();
				Toast.makeText(context, "扫码登录成功", Toast.LENGTH_LONG).show();
				uploadLogRecord("1", scanMsg);
				/*new Thread() {
					@Override
					public void run() {	
						try {
							Map<String, String> postParams = new HashMap<String, String>();
					        WebClientUtil.postJson(strPrint,postParams, 5000);
						} catch (Exception e1) {
								e1.printStackTrace();
						}
					}
				}.start();*/
				
				//Toast.makeText(context, "返回数据:\n"+strPrint, Toast.LENGTH_LONG).show();
				
				break;
			case SIGN_SUCCESS:
				progDialog.dismiss();
				Toast.makeText(context, R.string.scan_success_sign, Toast.LENGTH_LONG).show();
				uploadLogRecord("2", scanMsg);
				break;
			case SIGNEX_SUCCESS:
				progDialog.dismiss();
				Toast.makeText(context, R.string.scan_success_sign_batch, Toast.LENGTH_LONG).show();
				uploadLogRecord("2", scanMsg);
				break;
			case ENVELOP_DECRYPT_SUCCESS:
				progDialog.dismiss();
				Toast.makeText(context, R.string.scan_success_decypt, Toast.LENGTH_LONG).show();
				uploadLogRecord("5", scanMsg);
				break;
			case SEALINFO_SCAN_SUCCESS:
				progDialog.dismiss();
				showScanCert(strScanErr);
				break;
			case SEAL_SCAN_SUCCESS:				
				   progDialog.dismiss();
				   Toast.makeText(context, R.string.scan_success_seal, Toast.LENGTH_LONG).show();
				break;
			case SEAL_SIGN_SCAN_SUCCESS:
				progDialog.dismiss();
				SealSignUtil.sealPdfSign();
				doScan(SealSignUtil.strBizSN,
					   SealSignUtil.strOrgDate,
					   SealSignUtil.strSignDate,
					   SealSignUtil.strCert,
					   SealSignUtil.strCertSN,
					   SealSignUtil.strUniID,
					   SealSignUtil.strCertType,
					   SealSignUtil.strSaveType,
					   SealSignUtil.strAppID,
					   SealSignUtil.strMsgWrapper);
				break;
			case SAVE_CERT:
				data = msg.getData();
				String responseStr = data.getString("responseStr");
				final String envsn = data.getString("envsn");
				try {
					final DownloadCertResponse responseObj = null;
					final ChangePasswordDialog.Builder builder = new ChangePasswordDialog.Builder(
							context);
					builder.setMessage1("请输入证书口令");
					builder.setMessage2("请输入重复证书口令");
					builder.setTitle("提示");
					builder.setIcon(R.drawable.alert);
					builder.setNegativeButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String sOldPwd = builder
											.getEditText1Value();
									String sNewPwd = builder
											.getEditText2Value();

									if (sOldPwd == null || "".equals(sOldPwd)) {
										Toast.makeText(context, "请输入口令",
												Toast.LENGTH_LONG).show();
										return;
									}

									if (sNewPwd == null || "".equals(sNewPwd)) {
										Toast.makeText(context, "请输入重复口令",
												Toast.LENGTH_LONG).show();
										return;
									}
									
									if (sOldPwd.length() < 8) {
										Toast.makeText(context, "口令长度不能小于8位",
												Toast.LENGTH_LONG).show();
										return;
									}
									
									if (!sOldPwd.equals(sNewPwd)) {
										Toast.makeText(context, "口令与重复口令不一致",
												Toast.LENGTH_LONG).show();
										return;
									}

									try {
										Cert cert = null;

										cert = certDao.getCertByEnvsn(envsn,accountDao.getLoginAccount().getName());

										String p12 = genP12(
												cert.getPrivatekey(), sOldPwd,
												responseObj.getUserCert(),
												responseObj.getCertChain());

										cert.setStatus(Cert.STATUS_DOWNLOAD_CERT);
										cert.setUploadstatus(Cert.STATUS_UPLOAD_CERT);

										cert.setCertificate(responseObj
												.getUserCert());

										byte[] bCert = Base64
												.decode(responseObj
														.getUserCert());
										Certificate oCert = jse
												.getCertFromBuffer(bCert);
										X509Certificate oX509Cert = (X509Certificate) oCert;
										cert.setCertsn(new String(Hex
												.encode(oX509Cert
														.getSerialNumber()
														.toByteArray())));

										cert.setCertchain(responseObj
												.getCertChain());
										cert.setKeystore(p12);
										cert.setPrivatekey("");
										cert.setNotbeforetime(getCertNotbeforetime(responseObj.getUserCert()));
										cert.setValidtime(getCertValidtime(responseObj.getUserCert()));
										certDao.updateCert(cert,accountDao.getLoginAccount().getName());

										Toast.makeText(context, "保存证书成功",
												Toast.LENGTH_LONG).show();

										saveLog(OperationLog.LOG_TYPE_APPLYCERT,
												cert.getCertsn(), "","", "",1);
									} catch (Exception e) {
										Log.e(CommonConst.TAG, e.getMessage(),
												e);
										Toast.makeText(context, "保存证书失败",
												Toast.LENGTH_LONG).show();
									}
									dialog.dismiss();

									closeProgDlg();
								}
							});

					builder.setPositiveButton(
							"取消",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									closeProgDlg();
								}
							});

					builder.show();
				} catch (Exception e) {
					Log.e(CommonConst.TAG, e.getMessage(), e);
					closeProgDlg();
					Toast.makeText(context, "保存证书失败", Toast.LENGTH_LONG).show();
				}

				break;
			}
		}
	};

	private void viewCert(final int certId, final String scanResult) {
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certDetailView = inflater.inflate(R.layout.certdetail, null);

		final Cert cert = certDao.getCertByID(certId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);
		// sdf2.setTimeZone(tzChina);
		if (cert != null) {
		/*	String certificate = cert.getCertificate();
			byte[] bCert = Base64.decode(certificate);
			// byte[] bEncCert = Base64.decode(cert.getEnccertificate());
			Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
			// X509Certificate oEncX509Cert = (X509Certificate) jse
			// .getCertFromBuffer(bEncCert);
			try {
				ASN1InputStream asn1Input = new ASN1InputStream(
						new ByteArrayInputStream(bCert));
				ASN1Object asn1X509 = asn1Input.readObject();
				X509CertificateStructure x509 = X509CertificateStructure
						.getInstance(asn1X509);
				((TextView) certDetailView.findViewById(R.id.tvversion))
						.setText(jse.getCertDetail(1, bCert));
				((TextView) certDetailView.findViewById(R.id.tvsignalg))
						.setText(oX509Cert.getSigAlgName());
				((TextView) certDetailView.findViewById(R.id.tvcertsn))
						.setText(new String(Hex.encode(oX509Cert
								.getSerialNumber().toByteArray())));
				((TextView) certDetailView.findViewById(R.id.tvsubject))
						.setText(x509.getSubject().toString());
				((TextView) certDetailView.findViewById(R.id.tvissue))
						.setText(x509.getIssuer().toString());

				String strNotBeforeTime = jse.getCertDetail(11, bCert);
				String strValidTime = jse.getCertDetail(12, bCert);
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				((TextView) certDetailView.findViewById(R.id.tvaftertime))
						.setText(sdf2.format(toDate));
				((TextView) certDetailView.findViewById(R.id.tvbeforetime))
						.setText(sdf2.format(fromDate));

				RelativeLayout relativeLayout_certchainURL = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_certchainURL);
				String sCertChainPath = jse.getCertExtInfo(
						"1.2.156.1.8888.144", oX509Cert);
				if ("".equals(sCertChainPath) || null == sCertChainPath) {
					relativeLayout_certchainURL.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout_certchainURL.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView
							.findViewById(R.id.tvcertchainpath))
							.setText(sCertChainPath);
				}

				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
					((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
					if(!"".equals(cert.getDevicesn())){
					    certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
					    ((TextView) certDetailView.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
					}else
						certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}
				else{
					((TextView) certDetailView.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
					certDetailView.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}
					
				
				RelativeLayout relativeLayout_subjectUID = (RelativeLayout) certDetailView
						.findViewById(R.id.rl_subjectUID);
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
				if("".equals(sCertUnicode) || null == sCertUnicode )
					 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
				
				if ("".equals(sCertUnicode) || null == sCertUnicode) {
					relativeLayout_subjectUID.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout_subjectUID.setVisibility(RelativeLayout.VISIBLE);
					((TextView) certDetailView.findViewById(R.id.tvcertunicode))
							.setText(sCertUnicode);
				}

			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
				return;
			}
*/
		} else {
			Toast.makeText(context, "证书不存在", Toast.LENGTH_LONG).show();
			return;
		}

		String buttonTitle = "关闭";
		switch (operatorType) {
		case CommonConst.CERT_OPERATOR_TYPE_LOGIN:
			buttonTitle = "登录";
			break;
		case CommonConst.CERT_OPERATOR_TYPE_SIGN:
			buttonTitle = "签名";
			break;
		default:
			break;
		}

		AlertDialog.Builder builder = new Builder(context);
		builder.setIcon(R.drawable.view);
		builder.setTitle("证书明细");
		builder.setView(certDetailView);
		builder.setNegativeButton(buttonTitle,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
						switch (operatorType) {
						case CommonConst.CERT_OPERATOR_TYPE_LOGIN:
							if(verifyCert(cert,true))
								if(verifyDevice(cert,true))
							       login(cert, scanResult);
							break;
						case CommonConst.CERT_OPERATOR_TYPE_SIGN:
							if(verifyCert(cert,true))
								if(verifyDevice(cert,true))
							       sign(cert, scanResult);
							break;
						default:
							break;
						}
					}
				});
		builder.show();
	}

	private void saveLog(int type, String certsn, String message,String invoker,String sign,int signAlg) {
		OperationLog log = new OperationLog();
		log.setType(type);
		log.setCertsn(certsn);
		log.setMessage(message);
		log.setSign(sign);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		log.setCreatetime(sdf.format(date));
		log.setInvoker(invoker);
		log.setSignalg(signAlg);
		log.setIsupload(0);
		log.setInvokerid(CommonConst.UM_APPID);
		
		String strActName = accountDao.getLoginAccount().getName();
		if(accountDao.getLoginAccount().getType() == CommonConst.ACCOUNT_TYPE_COMPANY)
			strActName = accountDao.getLoginAccount().getName()+"&"+accountDao.getLoginAccount().getAppIDInfo().replace("-", "");

		logDao.addLog(log,strActName);
	}
	
	private boolean verifyCert(final Cert cert,boolean bShow){	
		if(CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())){
			int i = -1;
			if(CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))		
				 i = PKIUtil.verifyCertificate(cert.getCertificate(),CommonConst.RSA_CERT_CHAIN);
		    else
		    	 i = PKIUtil.verifyCertificate(cert.getCertificate(),
				                          cert.getCertchain());
			if(i == CommonConst.RET_VERIFY_CERT_OK){
				return true;
			}else{
				if(bShow)
					Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
			}
		}else if(CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())){
			String strSignCert = "";
			int i = -1;		
			
			if(cert.getEnvsn().indexOf("-e")!=-1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
				return false;
			
			if(!"".equals(cert.getContainerid())){
			   try {
			        javasafeengine jse = new javasafeengine();
			        strSignCert = cert.getCertificate();		
			        if(CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
						 i = jse.verifySM2Cert(cert.getCertificate(),CommonConst.SM2_CERT_CHAIN);
					else
			             i = jse.verifySM2Cert(strSignCert,cert.getCertchain());
			   } catch (Exception e) {
					   e.printStackTrace();
			   }
			   
			   if(i == 0){
				     return true;
			   }else if(i == 1){
					if(bShow)
					   Toast.makeText(context, "证书过期", Toast.LENGTH_SHORT).show();
			   }else{
					if(bShow)
				       Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
			   } 
			}
		}else if(CommonConst.CERT_TYPE_RSA_COMPANY.equals(cert.getCerttype())){
			int i = -1;
			if(CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))		
				 i = PKIUtil.verifyCertificate(cert.getCertificate(),CommonConst.RSA_CERT_CHAIN);
		    else
		    	 i = PKIUtil.verifyCertificate(cert.getCertificate(),
				                          cert.getCertchain());
			if(i == CommonConst.RET_VERIFY_CERT_OK){
				return true;
			}else{
				if(bShow)
					Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
			}
		}else if(CommonConst.CERT_TYPE_SM2_COMPANY.equals(cert.getCerttype())){
			String strSignCert = "";
			int i = -1;		
			
			if(cert.getEnvsn().indexOf("-e")!=-1 || CommonConst.INPUT_SM2_ENC.equals(cert.getEnvsn()))
				return false;
			
			if(!"".equals(cert.getContainerid())){
			   try {
			        javasafeengine jse = new javasafeengine();
			        strSignCert = cert.getCertificate();		
			        if(CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
						 i = jse.verifySM2Cert(cert.getCertificate(),CommonConst.SM2_CERT_CHAIN);
					else
			             i = jse.verifySM2Cert(strSignCert,cert.getCertchain());
			   } catch (Exception e) {
					   e.printStackTrace();
			   }
			   
			   if(i == 0){
				     return true;
			   }else if(i == 1){
					if(bShow)
					   Toast.makeText(context, "证书过期", Toast.LENGTH_SHORT).show();
			   }else{
					if(bShow)
				       Toast.makeText(context, "验证证书失败", Toast.LENGTH_SHORT).show();
			   } 
			}
		}
		
		return false;
	}
	
	
	private boolean verifyDevice(final Cert cert,boolean bShow){
		/*
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		Certificate oCert = jse.getCertFromBuffer(bCert);
		X509Certificate oX509Cert = (X509Certificate) oCert;
		String sDeciceID = jse.getCertExtInfo("1.2.156.112570.12.102",oX509Cert);
		
		//获取设备唯一标识符
		String deviceID = android.os.Build.SERIAL;
		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype() || CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype())
			deviceID = cert.getDevicesn();
		if(sDeciceID.equals(deviceID))
			return true;
		
		if(bShow)
		   Toast.makeText(context, "用户证书与当前设备不匹配", Toast.LENGTH_SHORT).show();
		*/
		return true;
	}

	 private  String  getCertNotbeforetime(String strCert){
		   String strNotBeforeTime = "";
		   
		   try{
			   byte[] bCert = Base64.decode(strCert);
		       javasafeengine jse = new javasafeengine();
		       strNotBeforeTime = jse.getCertDetail(11, bCert);
		   }catch (Exception e) {
			   
		   }
		   
		   return strNotBeforeTime;
	   }
	   
       private  String  getCertValidtime(String strCert){
    	   String strValidTime = "";
    	   
    	   try{
    		   byte[] bCert = Base64.decode(strCert);
    	       javasafeengine jse = new javasafeengine();
    	       strValidTime = jse.getCertDetail(12, bCert);
    	   }catch (Exception e) {
    		   
    	   }
    	   
    	   return strValidTime;
	   }
	
       private   String  getPersonID(String strCertUnicode){
 		  String strRet = "";
 		  
 		  strRet = strCertUnicode;
 		  if(strCertUnicode.indexOf("SF")!=-1)
 			  strRet = strCertUnicode.substring(2);
 		  
 		  return strRet;
 	  }

       
      private  void  showFaceReg(){
    	AlertDialog.Builder builder = new Builder(context);
  		builder.setMessage("无证书,是否需要自助申请证书?");
  		builder.setIcon(R.drawable.alert);
  		builder.setTitle("提示");
  		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				try{ 
  					final Handler handler = new Handler(context.getMainLooper());
  					
  					if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
					    showProgDlgCert("正在连接蓝牙key设备...");
  					else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
  						showProgDlgCert("正在连接蓝牙sim卡...");
  					
					workHandler.post(new Runnable() {
	    				@Override
	    				public void run() {    				
	    					if(checkBTDevice(false)){
	    						handler.post(new Runnable() {
		   							   @Override
		   						       public void run() {
	    						         closeProgDlgCert();
		   							   }
	  						    }); 

	    						applyByFace();
	    					}else{
	    						handler.post(new Runnable() {
		   							@Override
		   						    public void run() {
	    						        closeProgDlgCert();
	    						        Toast.makeText(context, "请确认蓝牙设备是否正确连接", Toast.LENGTH_SHORT).show();
		   							}
	  						    }); 
	    					}
	    				}
	    			});
				}catch( Exception e ) {	
					closeProgDlgCert();
					Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
					return;
		    	}

  			}
  		});
  		
  		builder.setPositiveButton("取消",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				});
  		
  		builder.show();  
     }
      
     private  boolean  checkBTDevice(boolean isInput){	
    	    if(!isInput){
    	       if(CommonConst.SAVE_CERT_TYPE_PHONE == accountDao.getLoginAccount().getSaveType())
    	    	  return  true;
    	    }
    	 
			if(!"".equals(sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, ""))){
				String strBTDevSN = sharedPrefs.getString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
				
				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType()){
				   shcaEsDeviceInfo devInfo =  gEsDev.getDeviceInfo(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
				   if(null == devInfo){
					   int nRet = gEsDev.connect(EsIBankDevice.TYPE_BLUETOOTH, strBTDevSN);
					   if(nRet == 0)	
						  return  true;	
					   else
						  return  false;
				   }else{
					  return  true;	
				   }	
				}else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType()){
					if(ScanBlueToothSimActivity.gKsSdk.isConnected())
						return true;
					
					try{
						ScanBlueToothSimActivity.gKsSdk.connect(strBTDevSN, "778899", 500);
					    return true;
					}catch(Exception ex){
						return  false;
					}
				}
			}
			
			if(isInput)
			   return  false;
			
			return  true;	
	} 
        
    private void setBlueToothPwd(final Handler handler) {
 		Builder builder = new Builder(context);		
 		builder.setIcon(R.drawable.alert);
 		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
 		   builder.setTitle("请输入蓝牙key密码");
 		else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
 		   builder.setTitle("请输入蓝牙sim卡密码");
 		builder.setCancelable(false); 
 	   // 通过LayoutInflater来加载一个xml布局文件作为一个View对象
        View view = LayoutInflater.from(context).inflate(R.layout.set_prikey_pwd, null);
         //设置自定义的布局文件作为弹出框的内容
         builder.setView(view);	
 		final EditText prikeyPasswordView = (EditText)view.findViewById(R.id.et_prikey_password);
 		final EditText prikeyPassword2View = (EditText)view.findViewById(R.id.et_prikey_password2);
 		prikeyPassword2View.setVisibility(RelativeLayout.GONE);
 		if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
 		   prikeyPasswordView.setHint("输入蓝牙key密码");
 		else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
 		   prikeyPasswordView.setHint("输入蓝牙sim卡密码");
 		
 		prikeyPassword2View.setText("");
 		prikeyPasswordView.setText("");
 		
 		prikeyPasswordView.requestFocus();
 		prikeyPasswordView.setFocusable(true);   
 		prikeyPasswordView.setFocusableInTouchMode(true);   
 			
 		builder.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						try {
 							try {
 						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
 						          field.setAccessible(true);
 						          field.set(dialog, false);
 						    } catch (Exception e) {
 						            e.printStackTrace();
 						    }
 							
 							String prikeyPassword = prikeyPasswordView.getText().toString().trim();										
 							// 检查用户输入的私钥口令是否有效
 							if (TextUtils.isEmpty(prikeyPassword) || !isPasswordValid(prikeyPassword)) {
 								handler.post(new Runnable() {
									 @Override
								     public void run() {
										 if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
 								            Toast.makeText(context, "无效的蓝牙key密码", Toast.LENGTH_SHORT).show();
										 else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
											Toast.makeText(context, "无效的蓝牙sim卡密码", Toast.LENGTH_SHORT).show();
									 }
 							    });
 								
 								return;
 							} 									
 														
 							try {
 						          java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
 						          field.setAccessible(true);
 						          field.set(dialog, true);
 						    } catch (Exception e) {
 						           e.printStackTrace();
 						    }
 							
 							mStrBTDevicePwd = prikeyPassword;
 							int nRet = -1;
 							
 							if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType()){
 								nRet = gEsDev.verifyUserPin(mStrBTDevicePwd);
 							}else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType()){
 								if(CommonConst.SAVE_CERT_TYPE_RSA == accountDao.getLoginAccount().getCertType())
 								   nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInRSA(mStrBTDevicePwd);
 								else
 								   nRet = ScanBlueToothSimActivity.gKsSdk.verifyUserPinInSM2(mStrBTDevicePwd);
 							}
 					        //nRet = gEsDev.verifyUserPin(mStrBTDevicePwd);
 					        if(nRet != 0){
 					        	handler.post(new Runnable() {
									 @Override
								     public void run() {
										 if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType())
 					        	            Toast.makeText(context, "蓝牙key密码错误", Toast.LENGTH_SHORT).show();
										 else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
											Toast.makeText(context, "蓝牙sim卡密码错误", Toast.LENGTH_SHORT).show();
									 }
 							    });
 					        	
 								return;
 					        }
 					        /*}else{*/
 					        handler.post(new Runnable() {
 									 @Override
 								     public void run() {
 										 if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == accountDao.getLoginAccount().getSaveType()) 
 										    showProgDlgCert("导入蓝牙key内证书中...");
 										 else if(CommonConst.SAVE_CERT_TYPE_SIM == accountDao.getLoginAccount().getSaveType())
 											showProgDlgCert("导入蓝牙sim卡内证书中...");
 										 inputBTCert();		
 							             closeProgDlgCert();
 									 }
 							}); 
 							    	
 					       // }
 						} catch (Exception e) {
 							Log.e(CommonConst.TAG, e.getMessage(), e);						
 						}
 						
 						dialog.dismiss();	
 					}	
 		});
 		
 		builder.show();
    }
    
    /**
	 * 密码由8-16位英文、数字或符号组成。
	 */	
	private boolean isPasswordValid(String password) {
		boolean isValid = false;
		if (password.length() > 7 && password.length() < 17) {
			isValid = true;
		}
		return isValid;
	}
     
    
    private ImageCycleEdgeView.ImageCycleEdgeViewListener mAdCycleViewListener = new ImageCycleEdgeView.ImageCycleEdgeViewListener() {
 		@Override
 		public void onImageClick(ADInfo info, int position, View imageView) {
 			
 		}

 		@Override
 		public void displayImage(String imageURL, ImageView imageView) {
 			ImageLoader.getInstance().displayImage(imageURL, imageView);// 使用ImageLoader对图片进行加装！
 		}
 	};
 	
	@SuppressLint("NewApi")
	private boolean checkBlueToothUsed(){
		if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {  
			return false;
		}  
		
		final BluetoothManager bluetoothManager =  (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);  
		BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();          
		// Checks if Bluetooth is supported on the device.  
		if (mBluetoothAdapter == null) {  
			return false;
		}  
		
		return true;
	}
	
	private boolean  checkShcaCciStdServiceState(int actCertType){
    	if(CommonConst.SAVE_CERT_TYPE_RSA == actCertType)
    		return true;
    /*
    	try{
    		if(null == ShcaCciStd.gSdk || ShcaCciStd.errorCode != 0){
    			 ShcaCciStd.gSdk = ShcaCciStd.getInstance(activity);
    			 ShcaCciStd.errorCode = ShcaCciStd.gSdk.initService(CommonConst.JSHECACCISTD_APPID, false, CommonConst.JSHECACCISTD_SERVICE_URL, CommonConst.JSHECACCISTD_TIMEOUT, true);    		
    			 if(ShcaCciStd.errorCode != 0 )
    			    	ShcaCciStd.gSdk = null;
    		}
    		
    		if(null == ShcaCciStd.gSdk)
    			return false;
    			
    		if(ShcaCciStd.errorCode != 0)
    			return false;		

    	}catch(Exception ex){
    		return false;
    	}
    	 */ 	
    	return true;
	}


 	@Override  
	public void onResume() {
 		super.onResume();
 		mAdView.startImageCycle();
 	};

 	@Override
 	public void onPause() {
 		super.onPause();
 		mAdView.pushImageCycle();
 	}

 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mAdView.pushImageCycle();
 	}
 

    private void showProgDlgCert(String strMsg){
    	 progDialogCert = new ProgressDialog(context);
    	 progDialogCert.setMessage(strMsg);
    	 progDialogCert.setCancelable(false);
    	 progDialogCert.show();	
	 }
		
	private void closeProgDlgCert() {
			if (null != progDialogCert && progDialogCert.isShowing()) {
				progDialogCert.dismiss();
				progDialogCert = null;
			}
	}
     
     private   void showMessage(){  
         LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View certDetailView = inflater.inflate(R.layout.certdetail, null);
		
		String strCode = "";
		if ("".equals(sCertUnicode) || null == sCertUnicode) 
			strCode =  accountDao.getLoginAccount().getIdentityCode();
	    else
		    strCode = getPersonID(sCertUnicode);
		
		AlertDialog.Builder builder = new Builder(context);
		builder.setIcon(R.drawable.view);
		builder.setTitle(strCode+"");
		builder.setView(certDetailView);
		builder.setNegativeButton("11",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						 dialog.dismiss();
						
					}
				});
		
		builder.show();
       }


	private void uploadLogRecord(final String type, final String msg) {

		new Thread(new Runnable() {
			@Override
			public void run() {

				final UniTrust uniTrust = new UniTrust(getActivity(), false);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				String resStr = uniTrust.UploadRecord(ParamGen.UploadRecord(getActivity(), type,
						sdf.format(date) + "", msg));
//				getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						dismissDg();
//					}
//				});
			}
		}).start();

	}
}