package com.sheca.umandroid.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.custle.dyrz.DYRZResult;
import com.custle.dyrz.DYRZResultBean;
import com.custle.dyrz.DYRZSDK;
import com.custle.dyrz.config.Config;
import com.custle.dyrz.config.DYErrMacro;
import com.custle.dyrz.utils.T;
import com.facefr.util.CheckPermServer;
import com.sheca.umandroid.BaseActivity2;
import com.sheca.umandroid.LaunchActivity;
import com.sheca.umandroid.MainActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umplus.dao.UniTrust;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class MainActivityNew extends Activity implements DYRZResult, ActivityCompat.OnRequestPermissionsResultCallback {

    //    private TabLayout mEnvTL;
    private TextView mTV;
    private ImageView mFaceIV;

    private EditText mNameET;
    private EditText mIdET;
    private EditText mPhoneET;
    private EditText mBankET;

    public static final int auth_type_dyrz = 0;
    public static final int auth_type_mobile = 1;
    public static final int auth_type_bank = 2;
    public static final int auth_type_face = 3;
    public static final int auth_type_alipay = 4;
    public static final int auth_type_face_alipay = 8;

    private String mAppId;
    private String mAppKey;
    private Integer mBuild;
    private String transaction_id = "app" + UUID.randomUUID().toString();
    private String mServerUrl;

    //测试环境
    private final String appID_T = "";          // 需向多源平台申请
    private final String priKey_T = "";         // 需向多源平台申请

    //多元手机号码实名认证
//    //正式环境
//    private final String appID = "21e610b1-8d02-4389-9c17-2d6b85ca595f";            // 需向多源平台申请
//    private final String priKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDC+TfSJXbTcB+QDQUVkCOxA/zgeIz3Zn2isPQoO9JTtZN+nZGYb5mFD5JoCSTc3QXeSFRBFTSLHmcCdWYcMLqr36TisKqH0+/DT3AZeWtFdDOss3UaWib4iATvgG/aaMd2MX0cv4v9NUKFxll6mxN2jU5BJTEo5jd3YwzhiZVyqous6DFalMt/s1O2g+Ck5D5glO2aGKb2FI5pXQXcoYojPIEbCKzMRAnqmXyWlWhFePl9bwZG8RwYZNm0Zl8P7ACnR5CVLTh1TfCDdRssWUeQ5u1ZwMjENjqrA4WK6jmZ4P6QJ1dxvl4ipsK/uicWS4CDCMskhMW6C7wV0w+McXPbAgMBAAECggEAdjD9VbbAQYxGldxOqLOn7zarpKdvTMokfusmFv2sknIP50E9cVq1haPa7JYecoTJeeX+rTVdlLfpWeQw8gXYIzh/i6vstEoVniAZpFemX4QBjz96TW85EI/j7gu9wnih8VQus66p/eS7XrzOoTRAqC0gsv0Iv/JOzWCN/mqY3djV0IfK5XO9HnGDiXeo9DMPNVzBHJg+hwWeMBWgVf+JQj4Z1M0kKysKwItUgg0GtL6zfKinv/WsIHfvZxp4r3EGH/0jJrLldC+4/6ngc9kqYTRDBVCtLuOA9PDhLdfxs4JPUqzvvOPJhaaZrpOTvcUUFpLpfaoibHJn0qbcqDPtAQKBgQDiDyGrkqor4X0Mh4YTkdLNSWZkMnTIm6dvC44RVFWh03xja0NQb4v/+OtGVRnP9XiSQXIsoRrmcK5R6FZmW88tUlip4o50DtLjYh2CXmEhdjDeSWw0FJy4WpPXwipLaIBcZymz3gWGdy6/1ofzSosFR7hMkPBP/4EcRQK+YNhNuQKBgQDczBVSP4XGKew9HKudKL67lBAu7MlTYLBPlLyZuoHv7faUIYyzU3HkVzImFfbY8gqpCu+6Wke/xWFERah1mSQBuIIBlRgkV39mUQ84HEOhWK8FT4Ht3wWPWoc/4vtR0V1Q4mQAt5QY3zmJUkYV/bL2kZCfdkaeNHqduJfs2+W4MwKBgQCiO4vFbw8zSLMOn+AoAToQ28Fg3RkUsyh5OAiwBR8jcPxO+Tao7jTB8ikfI4nPxfHOvKssvj3o7SsdWylOckr/0p4Q5aeoQM82Ij7dRdBdTE4L6RN/WN+UKmT5rb3eulOMfPjfvdGnS7dAM70DbBbTJkJsqIPeVZaZ7Cjo6eWx+QKBgDlg072fAl2f8WNkOvjJaN+IN7hqEluXidn0dhqhDDlUprqSCWVkrvk+66pYFOEF7V1GmUvdQD4GxiMe0wtUc7X6w9Yzb6WqE1J8iC71sWGRkVIY+lPdnC1HwlQI4XS+qrhlTMWe716TS/lypwH5/vLymxnFe86LJr4sBVcpQgZfAoGBAN47LZTCOb8U/EJuX7J0Sfr6j1MVb/oz4sBiAWxWIh4VEGgWmayXVElj8sWY036alTkXBz0UYdH1lvBzarj343VgAaAhXl90dXpS8XPoWrc+T0wd9AQqEUTtCVPDU5wD7MtcNq+S3QYA1NNrZeWOd4LUrYcdrvt17la9orV3QNdS";           // 需向多源平台申请
    //正式环境
//    private final String appID = "21e610b1-8d02-4389-9c17-2d6b85ca595f";            // 需向多源平台申请
//    private final String priKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDC+TfSJXbTcB+QDQUVkCOxA/zgeIz3Zn2isPQoO9JTtZN+nZGYb5mFD5JoCSTc3QXeSFRBFTSLHmcCdWYcMLqr36TisKqH0+/DT3AZeWtFdDOss3UaWib4iATvgG/aaMd2MX0cv4v9NUKFxll6mxN2jU5BJTEo5jd3YwzhiZVyqous6DFalMt/s1O2g+Ck5D5glO2aGKb2FI5pXQXcoYojPIEbCKzMRAnqmXyWlWhFePl9bwZG8RwYZNm0Zl8P7ACnR5CVLTh1TfCDdRssWUeQ5u1ZwMjENjqrA4WK6jmZ4P6QJ1dxvl4ipsK/uicWS4CDCMskhMW6C7wV0w+McXPbAgMBAAECggEAdjD9VbbAQYxGldxOqLOn7zarpKdvTMokfusmFv2sknIP50E9cVq1haPa7JYecoTJeeX+rTVdlLfpWeQw8gXYIzh/i6vstEoVniAZpFemX4QBjz96TW85EI/j7gu9wnih8VQus66p/eS7XrzOoTRAqC0gsv0Iv/JOzWCN/mqY3djV0IfK5XO9HnGDiXeo9DMPNVzBHJg+hwWeMBWgVf+JQj4Z1M0kKysKwItUgg0GtL6zfKinv/WsIHfvZxp4r3EGH/0jJrLldC+4/6ngc9kqYTRDBVCtLuOA9PDhLdfxs4JPUqzvvOPJhaaZrpOTvcUUFpLpfaoibHJn0qbcqDPtAQKBgQDiDyGrkqor4X0Mh4YTkdLNSWZkMnTIm6dvC44RVFWh03xja0NQb4v/+OtGVRnP9XiSQXIsoRrmcK5R6FZmW88tUlip4o50DtLjYh2CXmEhdjDeSWw0FJy4WpPXwipLaIBcZymz3gWGdy6/1ofzSosFR7hMkPBP/4EcRQK+YNhNuQKBgQDczBVSP4XGKew9HKudKL67lBAu7MlTYLBPlLyZuoHv7faUIYyzU3HkVzImFfbY8gqpCu+6Wke/xWFERah1mSQBuIIBlRgkV39mUQ84HEOhWK8FT4Ht3wWPWoc/4vtR0V1Q4mQAt5QY3zmJUkYV/bL2kZCfdkaeNHqduJfs2+W4MwKBgQCiO4vFbw8zSLMOn+AoAToQ28Fg3RkUsyh5OAiwBR8jcPxO+Tao7jTB8ikfI4nPxfHOvKssvj3o7SsdWylOckr/0p4Q5aeoQM82Ij7dRdBdTE4L6RN/WN+UKmT5rb3eulOMfPjfvdGnS7dAM70DbBbTJkJsqIPeVZaZ7Cjo6eWx+QKBgDlg072fAl2f8WNkOvjJaN+IN7hqEluXidn0dhqhDDlUprqSCWVkrvk+66pYFOEF7V1GmUvdQD4GxiMe0wtUc7X6w9Yzb6WqE1J8iC71sWGRkVIY+lPdnC1HwlQI4XS+qrhlTMWe716TS/lypwH5/vLymxnFe86LJr4sBVcpQgZfAoGBAN47LZTCOb8U/EJuX7J0Sfr6j1MVb/oz4sBiAWxWIh4VEGgWmayXVElj8sWY036alTkXBz0UYdH1lvBzarj343VgAaAhXl90dXpS8XPoWrc+T0wd9AQqEUTtCVPDU5wD7MtcNq+S3QYA1NNrZeWOd4LUrYcdrvt17la9orV3QNdS";           // 需向多源平台申请

    private final String appID = "623a9e95-e15b-4f60-8a95-cf038e5a64af";            // 需向多源平台申请
    private final String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCQRuFvohUB+E/rR82kVc73msHxzzaVPc2vL4VWgtPY3kKQ6y/dFwEhZAj3AeLForWg7iLKxhHQFzsn+BJ1wLPg/XAQwRSkovB31CEPHOTq8x14kyVsBCJGeWnvta8c2kyY7LiDErlzs3db7yzxxCgjVX2WH1nANWrX8n9lJwrZThCFvRS4i52eY5RWiXkLFIFzzBFTHdXS5uM7G9pD6ebrgZJQkJ9SH1aVRAwXA8TcwA+fQ+zARGV2dtlICciBBsZ/ETeO4kmvEE8mMvRvtKwtSEX0aiPJeECqFfLDKkZOSBDxG3rVoJG8lRzHtDWV86d7Hk+vKshXAjR9XQ+H949BAgMBAAECggEAEMygDU6TIaKXM68tq7fuHHihopVdJq2GmKJw5Szgm/ztRVCNRzIZiJjMTm6iyB51BaMU3AWKJ2+9DJ9fDuT8TPNVrC2/SJhMZbeGjerwYMckJFkF33jvwML8adP+6t4kUx4lMeXpQCaifEryMciEX/DhaayG19GgejqbSNzt73Vxkf7JEaCA8SZXTWAl0lPl1P75ZS+DQoksnRQZTnYDkG6cnt8ufXcb3i7cX5JPx1eiaN7lg7Rt90I/w2lvCxb9V2zkUd7ry/zVaxmLSbAFCDigHGPI3zf9+badi7OdBtkHTH67nXDNKejJcjH/lh2cNUGcB2sIqYjMKIc8mo8QfQKBgQDTsIgjwo5jR42h0gN9IjahwQtPkElePjdfGopsWHE4tnapwrcfaL16nMrDKCN67xqfCzPuaSEe4B75biOiwIKaW5uRlEv1o6IQYmD40Q+QmwVgfS0ECwMZxecTCoek10dA3yrs2su9ZJU2bkErxoxkmApwlnvcIYyRvFZPq6Rx9wKBgQCuegNbKHVxTSoi1dxUDmHPbmxn2Ua26qcoZnT34aGbgaAXgRMfNNgAfnAB1OvIpfmm6H2uNYZz65R10b2KNXYaAk6S/77UxNv/oYk9OwhREkGLFrFj8hdylYc6n7zftpttfYZoMxwhgy0+cgZRkG2ocHxlQF4jTt9872i8pZ66hwKBgQCSIcYJMZBLlqR99dU0t76Q8QtW1FrhdP+SZmbyHieip8rIq8LwKsTKdJxAFmBPx+lPq1MhHG+hucOIGnD9M/m0htKgr4e0PU5uEwuwF9mv0GPo1OCTbuqoCwbWDSnQMFBexvAB65RD3MBof7n7dyeJda+XQzqjnoFERYgrnWh6xwKBgAh3+aO6EgE+2pW4Rap6zDqSRIbB4BHOz/BBENpbREnU/91EMZZpLTbQ7ETafdtOWxDD5h3HkVAdFial2IpVz/axN/kgmrWfHIKK56tmKyAsP6wtnMyaGpNAOMEascM2DNNCrXxvRqVFxbNrO21IElqDozYS6r7R/D0HLdFCRLMPAoGAP7hc705norLps8CmZNU63s99IDrno7gKwC7kwyLy9Abn3WEH/LLJSpkBhvnOHmOYa1bfg+n50M6ozbFPVBc6X02NJ1NHnmmdrwW3VxLF3sbDSBq77f4YaTlNHaHCqDxBniB/cusdbkqFh7tkQ4IIee/SKOXYs0Jl/Y6eiebBzcU=";           // 需向多源平台申请


//    //正式环境
//    private final String appID = "";            // 需向多源平台申请
//    private final String priKey = "";           // 需向多源平台申请


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        mAppId = appID;
        mAppKey = priKey;
        mBuild = DYRZSDK.BUILD_RELEASE;
        mServerUrl = Config.server_url;

//        mEnvTL = findViewById(R.id.env_tab_layout);
        mTV = findViewById(R.id.textView);
        mFaceIV = findViewById(R.id.face_photo_iv);

        mNameET = findViewById(R.id.main_name_et);
        mIdET = findViewById(R.id.main_id_et);
        mPhoneET = findViewById(R.id.main_phone_et);
        mBankET = findViewById(R.id.main_bank_et);


//        mEnvTL.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                if (tab.getPosition() == 0) {
//                    mAppId = appID_T;
//                    mAppKey = priKey_T;
//                    mBuild = DYRZSDK.BUILD_DEBUG;
//                    Toast.makeText(MainActivityNew.this, "已切换到测试环境", Toast.LENGTH_SHORT).show();
//                    mServerUrl = Config.server_url_t;
//                } else {
//                    mAppId = appID;
//                    mAppKey = priKey;
//                    mBuild = DYRZSDK.BUILD_RELEASE;
//                    Toast.makeText(MainActivityNew.this, "已切换到生产环境", Toast.LENGTH_SHORT).show();
//                    mServerUrl = Config.server_url;
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                URL url = null;
//                try {
//                    url = new URL("https://umapi.sheca.com/ucm/umspservice/v4");
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//
//
//                HttpsURLConnection   http = null;
//                try {
//                    http = (HttpsURLConnection)url.openConnection();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                ((HttpsURLConnection)http).setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
//
//            }
//        });
//        HttpsURLConnection.setDefaultHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
//        HttpsURLConnection.setDefaultSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
    }

    public void onClick(View v) {
        String strName = mNameET.getText().toString();
        String strIdNo = mIdET.getText().toString();
        String strMobile = mPhoneET.getText().toString();
        String strBank = mBankET.getText().toString();
        mTV.setText("服务地址: " + mServerUrl + "\r\n正在认证中...");

        switch (v.getId()) {
            case R.id.dyrz_mobile_btn: {
                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).mobileAuth(MainActivityNew.this, strName, strIdNo, strMobile, transaction_id, this);
                break;
            }
            case R.id.dyrz_bank_btn: {
                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).bankAuth(MainActivityNew.this, strName, strIdNo, strMobile, strBank, transaction_id, this);
                break;
            }

            case R.id.dyrz_face_btn: {
          faceAuth(strName, strIdNo);
//                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).faceAuth(MainActivityNew.this, strName, strIdNo, transaction_id, this);
                break;
            }

            case R.id.dyrz_alipay_btn: {
                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).alipayAuth(MainActivityNew.this, transaction_id, this);
                break;
            }

            case R.id.dyrz_alipay_user_btn: {
                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).alipayAuth(MainActivityNew.this, strName, strIdNo, transaction_id, this);
                break;
            }

            case R.id.dyrz_face_with_alipay_btn: {
                DYRZSDK.getInstance(mAppId, mAppKey, mBuild).faceWithAlipayAuth(MainActivityNew.this, strName, strIdNo, transaction_id, this);
                break;
            }
        }
    }

    //获取当前app版本信息
    public  String IDAuth(String name,String id) {

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode("tokenID"),
                URLEncoder.encode("13701766266"),
                URLEncoder.encode("authType"),
                URLEncoder.encode("1"),
                URLEncoder.encode("commonName"),
                URLEncoder.encode(name),
                URLEncoder.encode("IDNumber"),
                URLEncoder.encode(id)

        );
        return strInfo;
    }
    private void faceAuth(String name,String id) {
        final UniTrust uniTrust=new UniTrust(this,false);


        new Thread(new Runnable() {
            @Override
            public void run() {
//                uniTrust.setDYFaceAuth(true);
                String result=     uniTrust. IDAuth(IDAuth(name,id));
                Log.e("结果",result);

            }
        }).start();

    }

    @Override
    public void dyrzResultCallBack(DYRZResultBean bean) {
        String result = "code: " + bean.getCode() + "\r\n";
        result += "msg: " + bean.getMsg() + "\r\n";
        String authType;
        switch (bean.getAuthType()) {
            case auth_type_dyrz:
                authType = "多源认证";
                break;
            case auth_type_mobile:
                authType = "手机号认证";
                break;
            case auth_type_bank:
                authType = "银行卡认证";
                break;
            case auth_type_face:
                authType = "人脸识别认证";
                break;
            case auth_type_alipay:
                authType = "支付宝认证";
                break;
            case auth_type_face_alipay:
                authType = "人脸支付宝认证";
                break;
            default:
                authType = "其它";
                break;
        }
        result += "type: " + authType + "\r\n";
        result += "token: " + bean.getToken() + "\r\n";
        result += "data: " + bean.getData();
        mTV.setText(result);

        if (bean.getData() != null && bean.getData().length() > 0) {
            byte[] bytes = Base64.decode(bean.getData(), Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mFaceIV.setImageBitmap(bitmap);
            mFaceIV.setVisibility(View.VISIBLE);
        }

        if (bean.getAuthType() == auth_type_face && bean.getCode().equals(DYErrMacro.camera_permission_err)) {
            if ((Build.VERSION.SDK_INT > 22) && (ContextCompat.checkSelfPermission(MainActivityNew.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivityNew.this, new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }
        }

    }


    @SuppressLint("Override")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // 回调中加载下一个Activity
            T.showShort(MainActivityNew.this, "权限关闭");
        } else {
            T.showShort(MainActivityNew.this, "权限开启，请再次认证");
        }
    }
}