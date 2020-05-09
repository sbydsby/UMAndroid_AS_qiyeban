package com.sheca.umandroid.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ifaa.sdk.api.AuthenticatorManager;
import com.sheca.jshcaesstd.JShcaEsStd;
import com.sheca.umandroid.AboutActivity;
import com.sheca.umandroid.CSActivity;
import com.sheca.umandroid.LaunchActivity;
import com.sheca.umandroid.LocalPasswordActivity;
import com.sheca.umandroid.LoginActivity;
import com.sheca.umandroid.PasswordActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.SettingFingerTypeActivity;
import com.sheca.umandroid.SettingLogUploadTypeActivity;
import com.sheca.umandroid.SettingVersionActivity;
import com.sheca.umandroid.UserProtocolActivity;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.presenter.LoginController;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;
import com.suke.widget.SwitchButton;
import com.tencent.android.tpush.XGPushManager;

import java.util.List;
import java.util.Map;

public class MineFragmentV3 extends Fragment {

    private ProgressDialog progDialog = null;
    private SharedPreferences sharedPrefs;
    private AccountDao accountDao = null;
    private Context context = null;

    private List<String> m_Devlst = null;
    private Handler handler = null;
    protected Handler workHandler = null;
    private HandlerThread ht = null;
    private List<Map<String, String>> mData = null;
    private AlertDialog certListDialog = null;

    private View view;
    private boolean isLogined = false;
    private SwitchButton switchButton;
    private boolean isUserNotificationFinger = false;
    private boolean isNotificationFinger = false;

    private int nShowView = 0;

    private UniTrust uniTrust;

    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible = false;
    /**
     * Fragment的view是否已创建
     */
    protected boolean mIsViewCreated = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mine_settings_v3, container, false);

        mIsViewCreated = true;

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!mIsViewCreated)//view没有创建的时候不进行操作
            return;

        if (getUserVisibleHint()) {
            if (!isVisible) {//确保在一个可见周期中只调用一次onVisible()
                isVisible = true;
                onVisible();
            }
        } else {
            if (isVisible) {
                isVisible = false;
                onHidden();
            }
        }

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uniTrust = new UniTrust(getActivity(),false);

        sharedPrefs = getContext().getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        accountDao = new AccountDao(getContext());
        context = view.getContext();

        ht = new HandlerThread("es_device_working_thread");
        ht.start();
        workHandler = new Handler(ht.getLooper());

        TextView tv_title = (TextView) getActivity().findViewById(R.id.tv_title);
        tv_title.setText(R.string.menu_me);

        CommUtil.setTitleColor(getActivity(), R.color.bg_yellow,
                R.color.black);

        isLogined = false;
        showSettingInfo();

        if (!AccountHelper.hasLogin(getContext())) {
            AccountHelper.clearAllUserData(getContext());
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void setAccountLogoutStatus(){
        new LoginController().setLogout(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mIsViewCreated = false;
    }

    /**
     * 可见
     */
    protected void onVisible() {
        //相当于Fragment的onResume，为true时，Fragment已经可见
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        isNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
        switchButton = (SwitchButton) view.findViewById(R.id.switch_button);
        if (!String.valueOf(isNotificationFinger).equals(String.valueOf(isUserNotificationFinger)))
            nShowView++;

        switchButton.setChecked(isNotificationFinger);
    }

    /**
     * fragment不可见的时候操作,onPause的时候,以及不可见的时候调用
     */
    protected void onHidden() {
        //相当于Fragment的onPause，为false时，Fragment不可见
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        isNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);
        switchButton = (SwitchButton) view.findViewById(R.id.switch_button);
        //if(!String.valueOf(isNotificationFinger).equals(String.valueOf(isUserNotificationFinger)))
        //switchButton.setChecked(isNotificationFinger);
    }

    @Override
    public void onResume() {//和activity的onResume绑定，Fragment初始化的时候必调用，但切换fragment的hide和visible的时候可能不会调用！
        super.onResume();
//        if(getActivity()!=null) {
//            ImageView tv_right = (ImageView) getActivity().findViewById(R.id.tv_right);
//
////        if (isVisibleToUser) {
//            tv_right.setVisibility(View.GONE);
////        } else {
////            tv_right.setVisibility(View.GONE);
////        }
//        }
        if (isAdded() && !isHidden()) {//用isVisible此时为false，因为mView.getWindowToken为null
            onVisible();
            isVisible = true;
        }
    }

    @Override
    public void onPause() {

        if (isVisible() || isVisible) {
            onHidden();
            isVisible = false;
        }
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {//默认fragment创建的时候是可见的，但是不会调用该方法！切换可见状态的时候会调用，但是调用onResume，onPause的时候却不会调用
        super.onHiddenChanged(hidden);

        if (!hidden) {
            onVisible();
            isVisible = true;
        } else {
            onHidden();
            isVisible = false;
        }
    }

    private void showSettingInfo() {
        if (!AccountHelper.hasLogin(getContext()))
            isLogined = false;
        else
            isLogined = true;

        final Account curAct = accountDao.getLoginAccount();
        //TODO 显示手机号和实名信息
        TextView showname = (TextView) view.findViewById(R.id.showName);
        if (isLogined) {
            showname.setText(AccountHelper.getUsername(getContext()));
            if (curAct.getType() == CommonConst.ACCOUNT_TYPE_COMPANY) {
                view.findViewById(R.id.shiming).setVisibility(RelativeLayout.GONE);
            } else {
                view.findViewById(R.id.shiming).setVisibility(RelativeLayout.VISIBLE);
                if (accountDao.getLoginAccount().getStatus() == 5 || accountDao.getLoginAccount().getStatus() == 3 || accountDao.getLoginAccount().getStatus() == 4) {  //账户已实名认证
                    ((ImageView) view.findViewById(R.id.shiming)).setImageDrawable(getResources().getDrawable((R.drawable.yishiming)));
                } else {
                    ((ImageView) view.findViewById(R.id.shiming)).setImageDrawable(getResources().getDrawable((R.drawable.weishiming)));
                }
            }
        } else {
            showname.setText("账户未登录");
            ((ImageView) view.findViewById(R.id.shiming)).setImageDrawable(getResources().getDrawable((R.drawable.weishiming)));
        }

        //修改用户密码
        view.findViewById(R.id.item_changepwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogined) {
                    Intent intent = new Intent(getContext(), PasswordActivity.class);
                    intent.putExtra("Account", AccountHelper.getUsername(getContext()));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        //设置证书介质
//        view.findViewById(R.id.item_certtype).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getContext(), SettingSaveTypeActivity.class);
//                i.putExtra("saveType", curAct.getSaveType());
//                startActivity(i);
//            }
//        });

//        if (CommonConst.SAVE_CERT_TYPE_PHONE == curAct.getSaveType())
//            ((TextView) view.findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
//        else if (CommonConst.SAVE_CERT_TYPE_BLUETOOTH == curAct.getSaveType())
//            ((TextView) view.findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
//        else if (CommonConst.SAVE_CERT_TYPE_AUDIO == curAct.getSaveType())
//            ((TextView) view.findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_AUDIO_NAME);
//        else if (CommonConst.SAVE_CERT_TYPE_SIM == curAct.getSaveType())
//            ((TextView) view.findViewById(R.id.textSaveType)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);

        //版本信息
        view.findViewById(R.id.item_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SettingVersionActivity.class);
                startActivity(i);
            }
        });

        //关于
        view.findViewById(R.id.item_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AboutActivity.class);
                //Intent i = new Intent(getContext(), net.sourceforge.simcpux.wxapi.WXEntryActivity.class);
                //Intent i = new Intent(getContext(), com.sheca.umandroid.PayActivity.class);
                startActivity(i);
            }
        });

        //用户协议
        view.findViewById(R.id.item_protocol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), UserProtocolActivity.class);
                startActivity(i);
            }
        });

        //客户服务
        view.findViewById(R.id.item_cs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CSActivity.class);
                startActivity(i);
            }
        });


        view.findViewById(R.id.item_localfp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LocalPasswordActivity.class);
                startActivity(intent);
            }
        });


        final boolean isNotification = sharedPrefs.getBoolean(CommonConst.SETTINGS_NOTIFICATION_ENABLED, true);   //上传使用记录开关默认开启
//        if (!isNotification)
//            ((TextView) view.findViewById(R.id.textLogType)).setText("不上传");
//        else
//            ((TextView) view.findViewById(R.id.textLogType)).setText("上传");


        //设置使用记录开关
        view.findViewById(R.id.item_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SettingLogUploadTypeActivity.class);
                i.putExtra("logType", isNotification);
                startActivity(i);
            }
        });

        //ifaa指纹开关默认关闭
        final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
        isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);

        //指纹开关
        switchButton = (SwitchButton) view.findViewById(R.id.switch_button);

        if (LaunchActivity.isIFAAFingerUsed) {
            switchButton.setVisibility(RelativeLayout.GONE);
            view.findViewById(R.id.item_ifaa).setVisibility(RelativeLayout.GONE);
        } else {
            if (AuthenticatorManager.isSupportIFAA(getContext(), com.ifaa.sdk.auth.Constants.TYPE_FINGERPRINT)) {
                switchButton.setVisibility(RelativeLayout.GONE);
                view.findViewById(R.id.item_ifaa).setVisibility(RelativeLayout.GONE);
            } else {
                switchButton.setVisibility(RelativeLayout.GONE);
                view.findViewById(R.id.item_ifaa).setVisibility(RelativeLayout.GONE);
            }
        }


        switchButton.setChecked(isUserNotificationFinger);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                //TODO 
                nShowView++;
                final String mUserName = sharedPrefs.getString(CommonConst.PARAM_USERNAME, "");
                isUserNotificationFinger = sharedPrefs.getBoolean(mUserName+CommonConst.SETTINGS_FINGER_ENABLED, false);

                if (nShowView % 2 != 0) {
                    if (isChecked) {
                        //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, true);
                        Intent i = new Intent(getContext(), SettingFingerTypeActivity.class);
                        i.putExtra("fingerType", isChecked);
                        startActivity(i);
                    } else {
                        //SharePreferenceUtil.getInstance(getContext()).setBoolean(CommonConst.SETTINGS_FINGER_ENABLED, false);
                        Intent i = new Intent(getContext(), SettingFingerTypeActivity.class);
                        i.putExtra("fingerType", isChecked);
                        startActivity(i);
                    }
                }
            }
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAccount();
            }
        });

    }

    private void doLogout(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                unregisterXGPush();

                String res = null;
                try{
                    res = uniTrust.Logout(ParamGen.getLogout(AccountHelper.getToken(getContext())));
                }catch(Exception e){
                    AccountHelper.clearAllUserData(getContext());
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }

                AccountHelper.clearAllUserData(getContext());

                Log.d("unitrust",res);

                APPResponse response = new APPResponse(res);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();

                if(retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK){

                    setAccountLogoutStatus();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "账户退出成功", Toast.LENGTH_SHORT).show();
                            if (isUserNotificationFinger){
                                Toast.makeText(getContext(), "使用指纹登录需重启应用", Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);

                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString(CommonConst.SETTINGS_BLUEBOOTH_DEVICE, "");
                            editor.commit();

                            JShcaEsStd gEsDev = JShcaEsStd.getIntence(getContext());
                            gEsDev.disconnect();
                        }
                    });
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),retMsg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                });

            }
        }).start();
    }

    private void logoutAccount() {
        final Handler handler = new Handler(getContext().getMainLooper());

        if (!AccountHelper.hasLogin(getContext())) {  //账户未登录
            Toast.makeText(getContext(), "账户未登录", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setIcon(R.drawable.alert);
            builder.setTitle("提示");
            builder.setMessage("确定退出此账户？");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            doLogout();
                        }

                    });

            builder.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        }
    }

    private void unregisterXGPush() {    //反注册信鸽推送SDK
        XGPushManager.unregisterPush(getContext().getApplicationContext());
    }

    //检测网络是否连接
    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();

            return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showProgDlg(String strMsg) {
        progDialog = new ProgressDialog(getContext());
        progDialog.setMessage(strMsg);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
