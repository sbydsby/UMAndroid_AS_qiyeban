package com.sheca.fingerui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ifaa.sdk.api.AuthenticatorManager;
import com.ifaa.sdk.auth.AuthenticatorCallback;
import com.ifaa.sdk.auth.Constants;
import com.ifaa.sdk.auth.IAuthenticator;
import com.ifaa.sdk.auth.message.AuthenticatorMessage;
import com.ifaa.sdk.auth.message.AuthenticatorResponse;
import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;

public class MainActivity extends Activity {
    public static String TAG = MainActivity.class.getSimpleName();

    private int authType = Constants.TYPE_FINGERPRINT;
    private String userid = "test";
    private String secData = "";
    
    private static final int FINGER_CODE = 0;	

    public enum Process {
        REG_GETREQ, REG_SENDRESP, AUTH_GETREQ, AUTH_SENDRESP, DEREG_GETREQ
    };

    private Handler handler;
    private final int MSG_REGENABLE = 1;
    private final int MSG_REGDISABLE = 2;
    private String token = "";

    private Process curProcess = Process.REG_GETREQ;
    public  static IAuthenticator authenticator;
    private Button regBtn;
    private Button authBtn;
    private Button deregBtn;
    private Button deviceBtn;
    private Button userstatusBtn;
    private Button enrollBtn;
    private TextView textView;
    
    private AccountDao mAccountDao = null;

    private AuthenticatorCallback regCallback = new AuthenticatorCallback() {
        @Override
        public void onStatus(int status) {
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, status);
        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            curProcess = Process.REG_SENDRESP;
            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                  IFAAFingerprintOpenAPI.getInstance().sendIFAARegResponeAsyn(MainActivity.this,data, secData, callback);
                //IFAAFingerprintOpenAPI.getInstance().sendRegResponeAsyn(data, secData, callback);
            } else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGFAIL).show("注册指纹失败");
                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, 0);
                    }
                });
            }
        }
    };

    private AuthenticatorCallback authCallback = new AuthenticatorCallback() {
        @Override
        public void onStatus(int status) {
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, status);
        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            curProcess = Process.AUTH_SENDRESP;
            if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                  IFAAFingerprintOpenAPI.getInstance().sendIFAAAuthResponeAsyn(MainActivity.this,data, secData, callback);
                //IFAAFingerprintOpenAPI.getInstance().sendAuthResponeAsyn(data, secData, callback);
            } else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_AUTHTEEFAIL).show("验证指纹失败");
                        FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, 0);
                    }
                });

            }
        }
    };

    private AuthenticatorCallback deregCallback = new AuthenticatorCallback() {
        @Override
        public void onStatus(int status) {
            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, status);

        }

        @Override
        public void onResult(final AuthenticatorResponse response) {
            String data = response.getData();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response.getResult() == AuthenticatorResponse.RESULT_SUCCESS) {
                        new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_DEREGSUCCESS).show(null);
                        saveToken("");
                        handler.sendEmptyMessage(MSG_REGENABLE);
                    } else {
                        new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_DEREGFAIL).show("注销指纹失败");
                    }
                }
            });
        }
    };
    private IFAAFingerprintOpenAPI.Callback callback = new IFAAFingerprintOpenAPI.Callback() {
        @Override
        public void onCompeleted(int status, final String info) {
            Log.i(TAG, "opanapi resp:" + info);

            switch (curProcess) {
                case REG_GETREQ:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	if (info.equals("isReg")) {
                        		saveToken(IFAAFingerprintOpenAPI.getInstance().getToken());
                                token = IFAAFingerprintOpenAPI.getInstance().getToken();
                                handler.sendEmptyMessage(MSG_REGDISABLE);
                        	}else if(info.equals("init")){
                        		saveToken("");
                                handler.sendEmptyMessage(MSG_REGENABLE);
                        	}else {
                               startFPActivity(false);
                               AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_REGISTER_REQUEST, 2);
                               requestMessage.setData(info);
                               authenticator.process(requestMessage, regCallback);
                        	}

                        }
                    });
                    break;
                case REG_SENDRESP:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.equals("OK")) {//"Success"
                                new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGSUCCESS).show("");
                                saveToken(IFAAFingerprintOpenAPI.getInstance().getToken());
                                token = IFAAFingerprintOpenAPI.getInstance().getToken();
                                handler.sendEmptyMessage(MSG_REGDISABLE);
                            } else {
                                new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGFAIL).show("ifaa注册指纹失败");
                            }
                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, 0);
                        }
                    });
                    break;
                case AUTH_GETREQ:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startFPActivity(false);
                            AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST, 2);
                            requestMessage.setData(info);
                            authenticator.process(requestMessage, authCallback);

                        }
                    });
                    break;
                case AUTH_SENDRESP:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.equals("OK")) {  //"Success"
                                new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_AUTHSUCCESS).show("");
                            } else {
                                new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_AUTHFAIL).show("ifaa验证指纹失败");
                            }
                            FingerprintBroadcastUtil.sendIdentifyStatusChangeMessage(MainActivity.this, 0);
                        }
                    });
                    break;
                case DEREG_GETREQ:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AuthenticatorMessage requestMessage = new AuthenticatorMessage(AuthenticatorMessage.MSG_DEREGISTER_REQUEST, 2);
                            requestMessage.setData(info);
                            authenticator.process(requestMessage, deregCallback);

                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthenticatorManager.isSupportIFAA(this, authType)) {
        
            regBtn.setEnabled(false);
            authBtn.setEnabled(false);
            deregBtn.setEnabled(false);
            deviceBtn.setEnabled(false);
            userstatusBtn.setEnabled(false);
            enrollBtn.setEnabled(false);
            Toast.makeText(this, "该设备不支持IFAA协议", Toast.LENGTH_LONG).show();
         
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.ifaa_activity_main);

        regBtn = (Button) findViewById(R.id.button);
        authBtn = (Button) findViewById(R.id.button2);
        deregBtn = (Button) findViewById(R.id.button3);
        deviceBtn = (Button) findViewById(R.id.button4);
        userstatusBtn = (Button) findViewById(R.id.button5);
        enrollBtn = (Button) findViewById(R.id.button6);
        textView = (TextView) findViewById(R.id.textView);
        token = getToken();
        authenticator = AuthenticatorManager.create(this, authType);  //"IFAA-appname"

        if (token.isEmpty()) {
            regBtn.setEnabled(true);
        } else {
            regBtn.setEnabled(false);
            IFAAFingerprintOpenAPI.getInstance().setToken(token);
        }

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("ifaa指纹测试");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 
		
		mAccountDao = new AccountDao(MainActivity.this);
		regIFAARegRequest(false);

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.this.finish();
			}
		});
        
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_REGDISABLE:
                        regBtn.setEnabled(false);
                        break;
                    case MSG_REGENABLE:
                        regBtn.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        };

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	regIFAARegRequest(true);
            }
        });

        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curProcess = Process.AUTH_GETREQ;
                String info = AuthenticatorManager.getAuthData(MainActivity.this, mAccountDao.getLoginAccount().getName());
                IFAAFingerprintOpenAPI.getInstance().getIFAAAuthRequestAsyn(MainActivity.this,mAccountDao.getLoginAccount().getName(),info, callback);
                //IFAAFingerprintOpenAPI.getInstance().getAuthRequestAsyn(info, callback);
                secData = info;
                Log.i(TAG, "auth info:" + info);
            }
        });

        deregBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curProcess = Process.DEREG_GETREQ;
                String info = AuthenticatorManager.getAuthData(MainActivity.this, mAccountDao.getLoginAccount().getName());
                IFAAFingerprintOpenAPI.getInstance().getIFAADeregRequestAsyn(MainActivity.this,mAccountDao.getLoginAccount().getName(),info, callback);
               // IFAAFingerprintOpenAPI.getInstance().getDeregRequestAsyn(info, callback);
                secData = info;
                Log.i(TAG, "auth info:" + info);
            }
        });

        deviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = authenticator.getDeviceId();
                textView.setText(deviceId);

            }
        });

        userstatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userStatus = authenticator.checkUserStatus(token);
                textView.setText(String.valueOf(userStatus));

            }
        });

        enrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticator.startSystemEnrollManger();

            }
        });
    }

    public void startFPActivity(boolean isAuthenticate) {
        Intent intent = new Intent();
//        if (isAuthenticate) {
//            intent.putExtra(AuthenticatorMessage.KEY_OPERATIONT_TYPE,
//                    AuthenticatorMessage.MSG_AUTHENTICATOR_REQUEST);
//        }
        intent.setClass(this, FingerPrintAuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        
       // this.startActivityForResult(intent, FINGER_CODE);   	
    }
    
    
    private void regIFAARegRequest(boolean regFlag) {
    	curProcess = Process.REG_GETREQ;
        String info = AuthenticatorManager.getAuthData(MainActivity.this, mAccountDao.getLoginAccount().getName());
        String deviceId = authenticator.getDeviceId();
        IFAAFingerprintOpenAPI.getInstance().getIFAARegRequestAsyn(MainActivity.this,mAccountDao.getLoginAccount().getName(),info,deviceId,regFlag, callback);
        //IFAAFingerprintOpenAPI.getInstance().getRegRequestAsyn(info, callback);
        secData = info;

        Log.i(TAG, "reg info:" + info);	
    }

    private final String TOKENFILE = "user";
    private final String KEY_TOKEN = "token";

    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(TOKENFILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.commit();

    }

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(TOKENFILE, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, "");
    }


    /**
     *
     * @author qiyi.wxc
     * @version $Id: FingerprintBroadcastUtil.java, v 0.1 2015年12月14日 下午7:44:55 qiyi.wxc Exp $
     */
    public static class FingerprintBroadcastUtil {

        //The is the broadcast for update UI status
        public final static String BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_ACTION";
        public final static String FINGERPRINTSENSOR_STATUS_VALUE            = "com.ifaa.sdk.authenticatorservice.broadcast.FINGERPRINTSENSOR_STATUS_VALUE";

        //Send the UI Status of the FingerPrint Result and Change the UI
        public static void sendIdentifyStatusChangeMessage(Context context, int resultCode) {
            Intent broadcastIntent = new Intent(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            broadcastIntent.putExtra(FINGERPRINTSENSOR_STATUS_VALUE, resultCode);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        public static IntentFilter getIdentifyChangeBroadcastFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BROADCAST_FINGERPRINTSENSOR_STATUS_ACTION);
            return filter;
        }

    }
    
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FINGER_CODE) {
			if (resultCode == MainActivity.RESULT_OK) {				
				new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGSUCCESS).show("input pwd");
			}
			if (resultCode == MainActivity.RESULT_CANCELED) {
				//new FingerPrintToast(MainActivity.this, FingerPrintToast.ST_REGSUCCESS).show("cancel");
				//MainActivity.this.finish();
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
