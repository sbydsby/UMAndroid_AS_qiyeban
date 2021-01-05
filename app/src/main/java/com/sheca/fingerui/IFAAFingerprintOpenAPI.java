package com.sheca.fingerui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePayRequest;
import com.sheca.javasafeengine;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CommonConst;
import com.sheca.zhongmei.util.WebClientUtil;

import org.spongycastle.util.encoders.Base64;

import java.net.URLEncoder;

/**
 * Created by frank on 16/12/19.
 */

public class IFAAFingerprintOpenAPI {
    public  static final String TAG = "IFAAFingerprintOpenAPI";
    private static final boolean checkFingerId = true;

    //private String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGgrh0QTHCGZxs70KFMOt9TUKu+ypQ8fHeddyBtDcyghgyzC5JNYYgmHQ+6g4fw167o0p7AonyLdV6zOdbMmhpv5ElcqxsGgp7g4WiyF59Q7Z8nQ+/68yUP3C6s5UJlrQUN/LB0V7WP4Eq59in1UX1jZCn+UoBaiEWj+yDlEn+zNX8/tMr8V+owQgsP5V7V4P2qHj6d8bNGuCHE0DxRB2Q94wRlk0YuSxjE8guWFScybbkfpVl6NMNhGpGEzUDb1MXQcwjtlsQcxAurAU/9kMlNUKg6qNcK14WNpHCLhYfUEFY8L2m5Qt7XEeug1Q9LpnMbTksDSY4cV5Xb3nZSud3AgMBAAECggEAGT63d7o0qIU+AoP1LCI+UEoL1eN10bNW/bWJKR61rVkVSZNgrITLI5r5VkV/WR7FzNmcxPF5CmbnxiBm24reZdp6V1jqW2+XwA6zKhYJBeGLgl/H8PFEeB5f/epuclpCefj2KsIs1nEcwPy6gOVaN/fVynDTHujYRO/GJ/vQ1205K2ApxVsQlE7wLh1yf+wiTeRLBhI1dYI+5O0VM+kSyQhsqpF/ndgagJTh1yl2CaNglwKoJdIkFulH3QhwKvOVJB8tAX+Ow49cQ3nKqs5arUhbuqSi7zlXPEGz+ox9iD8/Q7lLXqwGQEHg7qidKQsk5DRiigsp7aALoLGKkxBT4QKBgQD+JTZPwtsnHO/Q6wW0IQb8IYbptMQQoxZxDpvrY+9KkTxsPQH7Yw32F8hPW57V8jAAMTbjNSDo2TFCwiwdBYhfjTQ6lKhOU8lFFwzICafo0LoKoF9SIYLwcb4PRtPA5ncdl9rGZQEL5U/TkocuDptJUy/qRghYA7xdnyEpz5jpxQKBgQCHfgKEIM6AJ+zDGKYDCpZqfFSS+CswIl1ByE4p0vdGrOlgicYkRIj9AKFrwGx2MYXFIu/Mp53/0/lEhaUMw/bEbL3fd/t6peQoG+iKH+PXtxJiJgAA+iHCMXrrSnliXEwe8nFP6+dke1Jxmb8hEa4/a6SRHdhuyFrl5by4Vw8sCwKBgQCYNwl0dCx2HKq14k2kOTPJxvra9t2HaWaDiFByh4RGkP4zhcauHgG6pzvd6+4SDeAZ/V/2bVesnokWht2M9Eddk2D4xRiY8S3XJ5sMJFxg4MLMooj01AVspXNECxPAsUgefVkXAl/CFDHfch9swmrXW++SzScqL/Kd0DU4qZHYUQKBgGZFWyjYioZ5nGD27fhFuZvPFReKcy72AWZyB23SbAvQ4KeZ1xHxGWW/YBixOi1M3CgdYtwbeUtteS2boyWrlUxWuEtBiXTeWwVba3826wgci6fB/ya9p1Wpe5q2sEDqYhrJ7XXiHtrdqzFMV9RiQCBm4+UVnhSg7tuxOW37aNxZAoGAcWyVqVX9olD0DVbBV+t3E6XsYaG3mKC6FFjDl5xP/buTkvkTDoUGh6+B6/UdQEZAghO8DopdvevWxwJbioE8IFPg3gBgVDKP2glU/7YtvzMIegJzMbVhEPNrlxBcoKfXQyblsV9DO7Ah2a7zY9+f80kkeVRFGrHzXExMqxSMvwI=";//填入openapi开放平台你的账号的app的私钥
    //private String aliPubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
    private String aid = "2017042506956807";//填入openapi开放平台你的账号的app的ID
    private String url = "https://openapi.alipay.com/gateway.do";
  
    
    private String priKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCBjBzIzektfPtGGmFyfc2dilMuU47foe5F0uG5+W9DFT7TyxQo0CUxzUdoIVjaxVfkOXyWHnt3lrwg2KgjNTsb7nLTtnmIjFpvlCaNEI01oV1DAvyNvwYcXuQKC5IGm2K9sxXceQsYhgrTTJWxvSqXcdw7AINDj2k73r7lbkNUbAByGwAW15LyqxBjfTSxmGOcAuSYAZ1kH2jmmhhh9AHYE05EvA7VTs1MW1b41Kw338vZCCUklJ7FyeuwJIr+3+cVcqxBjXg/MJN2fbAa2b1PZp9z12uGlDK0h/Pak8aJaHWQvnjFNx1LmTBji0IJkYQZq0XVptdsHaGWYaZYxAE3AgMBAAECggEAQzM3S0LlDl+iyL6uhgGUQ4CZnZJirscFHttBlIM7oATJTnwtvO4cHTVrMSz7NqDf8tePNSAi0+oxNKiur3TRcK/EiOZDLP4Be5fSV6KaXZOTGUvXToAk31qp2DwgR6UbF2Rqlq+DffkqbGlxzX1fNqgtob2mWcluwUvOop0L+CI9b+bYFbiJzAk3cUDkLl92NCTDN+/Pk0Uef1Haf+Vk2RFLLOae8gOkD5rYi6OwIYlHtya//5cFi2n/eeBYfYDJb2FnhFAgSwCH/C0wUQr71IZlTsXOSy45DLl/HFDGdcroclD7V4hpLWbb0XxcQVbfs3zPVCOxhjs7JQhS7PKfoQKBgQDeaRWvZjkQDm8LEVGbQS6HK+0iB+U9oRjWZeBZIUkt5ZS5ziVQWDMqk6tbP3diJMwOq5YwdNh1ScgNIM+t7h/cT57KjKbJn4kWtjM1lBw+Krv1UXUG/285JUV+ddEP9SnLsI8bBhbUpUgIyqBcEkPMos/usVppFmJtvxQhYuI3PQKBgQCVHLfIibKqfyADEsr1tLNxXCcN678uyqvVfTLhfLrRETOyYx31k1kY7DPxbx0pvsmSOLEgWDXQIGmGVftqeHKjfGaj8f1UJWqPTOr9oId7l7Z9CLpO9CYtjoR8RxeS5XDy+Hto4zaRcF6jyM8VVXgPKM8XCWhe9WXjkOng+JyBgwKBgQDQGlFhCevwmyDJoU3TUB0B+1bpVavoi3ja+v39R8jaOgt8A2gkkjJl70ARpAPHgBiyzVg9RMdYj02xAvdvjfSfxpwUd6CD3VN/PcISqt/2RzQRaUNv5cejF727gu+317Rg03ZDWeI42+HX4AhFvg1URy7571ZiJ8C3YWeriYOTHQKBgHS7uL0L341OXFdLnQxh3KEATaq+Rjtxerh1fSG9rdsCJoCey9N2+Sdjsv5tyFzn+ZG5RCFYLTi0Ryhdo6ekh8qpFwl95p67dAKfatAHnkkJalnUPVaWGFVN3zYTyca8TIJKOQLR7eRKHi0Ghefjs0va3NTiDo1Ye919kzsGJi/VAoGBAM+X8rGMHsA+SPrt56yG0iQiU5xuCL8xrWAJF0dtTaM4pxSAFn3bEpUxN01HdkbWUJmFV1Jl0DevwCAH5K45zPITTIHTND9T5WSqOgDLKI38SaBhy4MOSSLVNxoqx3BnZBmxPOFHjvqszG+FWPWK1tIq6oQgv73begm3w8tOb1i2";
    private String aliPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl6zGUN7EqDNci37aSzqM4C1xx4smjZSDh+CHQnVNzt49PwwZnLyCAyB8xWTfc3xgI9UtSXpvd83jUY81oo8bgYejrKGvf0rgpmQb79Bsx2/ql9qQfozv7pghbPLIuSuejSz7/Aupovpm5EhhBdtWLoH5uBOVrQ1gtMfzV5/A4eAZQpkCi5DBA3YUHrONkXyQSRPhrrYE5rDydj5+9GeTbINlYYxcjfjnDZNcG/pZbcdJMHBMmQ6QtnUVUe4V58IBIccbeNx7R8/Kzlkx/UFd8orkHaKWtw8iPR3rcMyu/PJcHsEs+9gKoxouXjsPdl/1zEvNIVyLulO6akGI8IMU1wIDAQAB";
 /* 
    private String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCJ6q9iU7wBZ29yhs+2ay8rhPHyrIA97JCZsSq0xBwIX5bE859COIqzAy7CTKUFiNY1bDj4Sr5PRtTi76qeAw7glI0Pm2WtM07Jc5tmQxnUD86OR/tNMEvHpbUZ2jIfZsJjduApc+4f5+ckoUEU0X8gOjfcIANZS28Hwdh9H6sUJdxdAF1AUy3whsrFTIffeMPxkNyqxVzGQV2+v/R43vwwr8SAWSMJCJNAD00Y88WRvGQRPHeO1lvbjxUbEh5xyFlRj6kgMiIUM9krdwki0fMIB5XYw3uWquRn83dCfEetEG/EGaI1LLYu0r9B+x/d1RffyTocTteVi62bkF753HM7AgMBAAECggEARiX+au7YM4ae4wu63rgJ2ALY5WvkFpsauXGpnSQadg/YqfiYSqBju8WkpONDFFuTSLzxBSlPPxdx9LOefpxjlss4LHYH816jdke9BPigTIJ1t1zZb1tfT/7y7GFVGS+1M84lGjCDL+tOYA09Aw/Dr2qtOuN1kJeTs8hfCtVuuOIBmK2jXkGgIzVrXKUO2XFdilxFYsPieeJYfBJ0Ea+p5T6NH3HhxCEgWI7dvx0pmp7A8pCLm4zXiLQCvxBLm3fOVXWWJ962W5PTg5p1GfaJgy3l4kTeaTwgpTHCOX1FdplD7ARuUX0O/OnDJ0yRqvETiO+I6OouXWzy16Zq5sE2IQKBgQDGCan670FYyHOoFs/5tt6p/7jWZSrUrIEgTIKSOzQ/L4EE6JhrETKCd1Xaox49Tf0SRuX29rXPk1IStPwsadGcZf2NBNk9cnb2IECWSdb4daq/wN5YvB0+LNihOpAc4FywliqsfIvqZAoF9Savyyf3ku74toYBsxHoVA4mPOIcMwKBgQCySFiEyN4dEHCIi+yL9VRkOe2nJC/Bwz0kaCA633fPC5fSPU6l83uIKXstNB5NDgBDevpvXCTi7gDtLcTjHmDMVOSMWCJdS5lpuNLR+3c2RuucoaZ+fSWajB/FXxe4TzkkSE2mV8o/iwE/On4UAr4YUCWMw5+1L9kDlskmAXdE2QKBgQCCHUlcQKRLo/AxaUOeCyOCyCx2WKPqdailQv+q+lEajqiTRAsYlYT9KVD0RKYSm1ICFyjyuSe9SUrsVZM1zxTUUEJly+C7uaAlmA03cS5Xsl/MevjbjkUcW7S9Qc/72QGtWAyTpYT8LFxVi7uuVqlcxWJTcQDczv2eCtP9FZVt9wKBgH3L5IHVRjyV1AHVUZS0B9cstM8iDnyXKbZdrpg8BErYsEjNr4gTVNAQDPUpeFQAlBkJVlprVy+0pny0td9YGQ80u4t3yM+xv09E9aTq3gQzaJ69xUAtR/JT+zsGTTNk2zoJtpiwC2pox/l4aWvvP3jiXIyFoFR1rZgFtmsVz7WJAoGACwiOrjW7HdD9pkAU43LtcXVGVIgqwCsZ1tom7Iu5Zs9eJz7XwtcDSVTkH+xW1+Axx6RlP5CQuuWMprhVajIpVD9PqJ/AF566akxgVfC6VvKIYKa1vnGbZ4yZNF4vO0oJMSYunf2FLVF2ca7VcwopjgsMmFJ06np1H4Qhy6n1ARE=";//填入openapi开放平台你的账号的app的私钥
    private String aliPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgdXEWB9gm5XCQqSOOv2ZBq6srv5WjJ+MEXBwn1qHbqFn9vXaZ9FQoflSzY9sQ/oQVLqDveEgFfL5Di0ujBdDn+QRP8+AReXn3K5tNeMY//x1+Fi/I+fpA6IhMyQ8r179ORR6OQpVqDE/eYy0Kgbpy98b1AHqktx6ek4c7M5tKYFPg+ni/1TTPT/GZ6ge2dGNJu6E50FB3uSwmlW5feoNQt82l9iBlQ6rM2c7/hNFsv61avWQptsj4JMADTVMdm8uZL+265hZU8oGe2fxb16OVFba9YYIsvjBOPBFV/jeaWaMKbU2eUiQfovAaL2Sbc9pXNndT05nIA1orw83C39FOwIDAQAB";
    private String aid = "2017011605132139";//填入openapi开放平台你的账号的app的ID
    private String url = "https://openapi.alipay.com/gateway.do";
 */ 


    AlipayClient alipayClient = null;
    String token = "";
    private ProgressDialog progDialog = null;


    public interface Callback {
       void onCompeleted(int status, String info);
    }

    private static IFAAFingerprintOpenAPI instance;
    public static IFAAFingerprintOpenAPI getInstance() {
        if (instance == null) {
            instance = new IFAAFingerprintOpenAPI();
        }
        return instance;
    }
    public IFAAFingerprintOpenAPI() {
         alipayClient = new DefaultAlipayClient(url,aid,priKey,"json","GBK",aliPubKey, "RSA2");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public void getRegRequestAsyn(final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getRegRequest(secData, callback);
            }
        }).start();
    }
    
    public void getIFAARegRequestAsyn(final Context context,final String act,final String secData,final String devInfo,final boolean regFlag, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getIFAARegRequest(context,act,secData,devInfo,regFlag, callback);
            }
        }).start();
    }

    public void getAuthRequestAsyn(final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getAuthRequest(secData, callback);
            }
        }).start();
    }

    
    public void getIFAAAuthRequestAsyn(final Context context,final String act,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getIFAAAuthRequest(context,act,secData, callback);
            }
        }).start();
    }

    
    public void sendRegResponeAsyn(final String regResponse,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.sendRegResponse(regResponse, secData, callback);
            }
        }).start();
    }
    
    public void sendIFAARegResponeAsyn(final Context context,final String regResponse,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.sendIFAARegResponse(context,regResponse, secData, callback);
            }
        }).start();
    }

    public void sendAuthResponeAsyn(final String regResponse,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.sendAuthResponse(regResponse, secData, callback);
            }
        }).start();
    }
    
    public void sendIFAAAuthResponeAsyn(final Context context,final String regResponse,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.sendIFAAAuthResponse(context,regResponse, secData, callback);
            }
        }).start();
    }

    public void getDeregRequestAsyn(final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getDeregRequest(secData, callback);
            }
        }).start();
    }
    
    public void getIFAADeregRequestAsyn(final Context context,final String act,final String secData, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IFAAFingerprintOpenAPI.this.getIFAADeregRequest(context,act,secData, callback);
            }
        }).start();
    }

    public class AlipaySecurityProdFingerprintApplyInitializeRequest extends AlipayTradePayRequest {

        public String getApiMethodName() {
            return "alipay.security.prod.fingerprint.apply.initialize";
        }

    }

    public class AlipaySecurityProdFingerprintApplyRequest extends AlipayTradePayRequest {

        public String getApiMethodName() {
            return "alipay.security.prod.fingerprint.apply";
        }

    }

    public class AlipaySecurityProdFingerprintVerifyInitializeRequest extends AlipayTradePayRequest {

        public String getApiMethodName() {
            return "alipay.security.prod.fingerprint.verify.initialize";
        }

    }

    public class AlipaySecurityProdFingerprintVerifyRequest extends AlipayTradePayRequest {

        public String getApiMethodName() {
            return "alipay.security.prod.fingerprint.verify";
        }

    }

    public class AlipaySecurityProdFingerprintDeleteRequest extends AlipayTradePayRequest {

        public String getApiMethodName() {
            return "alipay.security.prod.fingerprint.delete";
        }

    }


    private void getRegRequest(String secData, Callback callback) {
        AlipaySecurityProdFingerprintApplyInitializeRequest request = new AlipaySecurityProdFingerprintApplyInitializeRequest();
        secData = secData.replace("\"", "\\\"");
        String bizContent = "{" +
                "    \"auth_type\":\"1\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"" +
                "  }";
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        	//response = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "register resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_apply_initialize_response");
           
 
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("server_response"));
                }
            }
        } else {
            System.out.println("调用失败");
        }
    }
     
    private void getIFAARegRequest(final Context context,String act,String secData, String devInfo, boolean regFlag,Callback callback) {
      Boolean isIFAAFPCheckReg = getIFAAFPCheckStatus(context,act,devInfo);
      if(isIFAAFPCheckReg){
          if (callback != null) {
             callback.onCompeleted(0,"isReg");
           }
      }else{  
       if(!regFlag){
    	   if (callback != null) {
    	      callback.onCompeleted(0,"init");
    	   }
       }else{
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPApplyInitialize);										

		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹注册中...");
				}
		}); 
		
    	String postParam = "";
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		handler.post(new Runnable() {
			   @Override
			   public void run() {
		           closeProgDlg();
			   }
		}); 
		
        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "register resp body:" + respStr);
 
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0,respStr);
                }
            }
        }else if("10012".equals(resultStr)){
        	try{
        	    if(loginUMSPService(context,act)){
        	    	getIFAARegRequest(context,act,secData,devInfo,regFlag,callback);
        	    }else{
        	    	 if (callback != null) {
                         callback.onCompeleted(0,returnStr);
                     }
        	    }
        	    	
        	}catch(Exception ex){
        		 if (callback != null) {
                     callback.onCompeleted(0,ex.getMessage());
                 }
        	}
        }else {
            System.out.println("调用失败:"+returnStr);
            
            if (callback != null) {
                callback.onCompeleted(0,returnStr);
            }
            //if("0".equals(resultStr))
              //  Toast.makeText(context,"账户未登录，请先登录",Toast.LENGTH_SHORT).show();	
           // else
            	//Toast.makeText(context,"网络连接异常或无法访问服务",Toast.LENGTH_SHORT).show();	
        }
       }
      }

    }
    


    private void getAuthRequest(String secData, Callback callback) {
        AlipaySecurityProdFingerprintVerifyInitializeRequest request = new AlipaySecurityProdFingerprintVerifyInitializeRequest();
        secData = secData.replace("\"", "\\\"");
        String bizContent = "{" +
                "    \"token\":\"" + token + "\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"" +
                "  }";
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "verify init resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_verify_initialize_response");
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("server_response"));
                }
            }
        } else {
            System.out.println("调用失败");
        }
    }
    
    private void getIFAAAuthRequest(final Context context,String act,String secData, Callback callback) {
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPVerifyInitialize);										

		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹验证中...");
				}
		}); 
		
		String postParam = "";
		try {
			if(null == token)
				token = "";
			
		    postParam = "token="+URLEncoder.encode(token, "UTF-8");
		}catch (Exception e) {
			if (callback != null) {
                callback.onCompeleted(0, e.getMessage());
            }
		}
		
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		handler.post(new Runnable() {
			   @Override
			   public void run() {
		           closeProgDlg();
			   }
		}); 
		
        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "verify init resp body:" + respStr);
            
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, respStr);
                }
            }
        }else if("10012".equals(resultStr)){
        	try{
        	    if(loginUMSPService(context,act)){
        	    	getIFAAAuthRequest(context,act,secData,callback);
        	    }else{
        	    	 if (callback != null) {
                         callback.onCompeleted(0,returnStr);
                     }
        	    }
        	    	
        	}catch(Exception ex){
        		 if (callback != null) {
                     callback.onCompeleted(0,ex.getMessage());
                 }
        	}
        }else {
        	System.out.println("调用失败:"+returnStr);
        	
        	if (callback != null) {
                callback.onCompeleted(0,returnStr);
            }
          //  if("0".equals(resultStr))
            //    Toast.makeText(context,"账户未登录，请先登录",Toast.LENGTH_SHORT).show();	
           // else
            	//Toast.makeText(context,"网络连接异常或无法访问服务",Toast.LENGTH_SHORT).show();	
        }

    }

    private void sendRegResponse(String regResponse, String secData, Callback callback) {
        AlipaySecurityProdFingerprintApplyRequest request = new AlipaySecurityProdFingerprintApplyRequest();
        secData = secData.replace("\"", "\\\"");
        regResponse = regResponse.replace("\"", "\\\"");
        String bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + regResponse + "\"" +
                "  }";
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "register resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_apply_response");
            if (resp != null) {
                token = resp.getString("token");
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("msg"));
                }
            }
        } else {
            System.out.println("调用失败");
        }

    }
    
    private void sendIFAARegResponse(final Context context,String regResponse, String secData, Callback callback) {
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPApply);										

		regResponse = regResponse.replace("\"", "\\\"");
		
		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹注册中...");
				}
		}); 
		
    	String postParam = "";
    	try {
		    postParam = "ifafMessage="+URLEncoder.encode(regResponse, "UTF-8");
		}catch (Exception e) {
			 if (callback != null) {
                 callback.onCompeleted(0, e.getMessage());
             }
		}
    	
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));   	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   closeProgDlg();
				}
		}); 

        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "register resp body:" + respStr);
            
            if (resp != null) {
                token = resp.getString("token");
                if (callback != null) {
                    callback.onCompeleted(0, returnStr);
                }
            }
        } else {
        	System.out.println("调用失败:"+returnStr);
            //if("0".equals(resultStr))
               // Toast.makeText(context,"账户未登录，请先登录",Toast.LENGTH_SHORT).show();	
           // else
            	//Toast.makeText(context,"网络连接异常或无法访问服务",Toast.LENGTH_SHORT).show();	
        	 if (callback != null) {
                 callback.onCompeleted(0, returnStr);
             }
        }

    }


/*
    private void sendAuthResponse(String verifyResponse, String secData, Callback callback) {
        secData = secData.replace("\"", "\\\"");
        verifyResponse = verifyResponse.replace("\"", "\\\"");
        AlipaySecurityProdFingerprintVerifyRequest request = new AlipaySecurityProdFingerprintVerifyRequest();
        String bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + verifyResponse + "\"" +
                "  }";
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "verify resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_verify_response");
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("msg"));
                }
            }
        } else {
            System.out.println("调用失败");
        }

    }
*/
    
    private void sendAuthResponse(String verifyResponse, String secData, Callback callback) {
        secData = secData.replace("\"", "\\\"");
        verifyResponse = verifyResponse.replace("\"", "\\\"");
        AlipaySecurityProdFingerprintVerifyRequest request = new AlipaySecurityProdFingerprintVerifyRequest();
        String bizContent;
        if (checkFingerId) {
          bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + verifyResponse + "\"," +
                "    \"extend_param\": {\"needAuthData\" : true, \"subAction\" : \"authenticate\"}"+
                "  }";
        } else {
          bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + verifyResponse + "\"" +
                "  }";

        }
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "verify resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_verify_response");
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("msg"));
                }
            }
        } else {
            System.out.println("调用失败");
        }

    }
    
    
    private void sendIFAAAuthResponse(final Context context,String verifyResponse, String secData, Callback callback) {
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPVerify);										

		verifyResponse = verifyResponse.replace("\"", "\\\"");
		
		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹验证中...");
				}
		}); 
				
    	String postParam = "";
    	try {
		    postParam = "ifafMessage="+URLEncoder.encode(verifyResponse, "UTF-8")+
		    		    "&isUpdate="+URLEncoder.encode(String.valueOf(Boolean.FALSE), "UTF-8");
		}catch (Exception e) {
			if (callback != null) {
                callback.onCompeleted(0, e.getMessage());
            }
		}
    	
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));   	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
    	
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   closeProgDlg();
				}
		}); 
		
		/*
        if (checkFingerId) {
          bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + verifyResponse + "\"," +
                "    \"extend_param\": {\"needAuthData\" : true, \"subAction\" : \"authenticate\"}"+
                "  }";
        } else {
          bizContent = "{" +
                "    \"out_biz_no\":\"20161126200040011100190086557102\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"," +
                "    \"ifaf_message\": \"" + verifyResponse + "\"" +
                "  }";

        }      
        */
		
        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "verify resp body:" + respStr);
            
            if (resp != null) {
                boolean isAuthVerify = resp.getBoolean("auth_result");
                if (callback != null) {
                	if(isAuthVerify)
                        callback.onCompeleted(0,returnStr);
                	else
                		callback.onCompeleted(0,String.valueOf(isAuthVerify));
                }
            }
        } else {
        	System.out.println("调用失败:"+returnStr);
        	
        	if (callback != null) {
                 callback.onCompeleted(0, returnStr);
            }
        
          //  if("0".equals(resultStr))
            //    Toast.makeText(context,"账户未登录，请先登录",Toast.LENGTH_SHORT).show();	
          //  else
            //	Toast.makeText(context,"网络连接异常或无法访问服务",Toast.LENGTH_SHORT).show();	
        }

    }
    
    private void getDeregRequest(String secData, Callback callback) {
        secData = secData.replace("\"", "\\\"");
        AlipaySecurityProdFingerprintDeleteRequest request = new AlipaySecurityProdFingerprintDeleteRequest();
        String bizContent = "{" +
                "    \"token\":\"" + token + "\"," +
                "    \"ifaa_version\":\"2.0\"," +
                "    \"sec_data\": \"" + secData + "\"" +
                "  }";
        Log.i(TAG , "bizcontent:" + bizContent);
        request.setBizContent(bizContent);
        AlipayResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
            Log.i(TAG, "dereg resp body:" + response.getBody());
            JSONObject jo = JSON.parseObject(response.getBody());
            JSONObject resp = jo.getJSONObject("alipay_security_prod_fingerprint_delete_response");
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, resp.getString("server_response"));
                }
            }
        } else {
            System.out.println("调用失败");
        }

    }
    
    private void getIFAADeregRequest(final Context context,String act,String secData, Callback callback) {
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPDelete);										

		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹注销中...");
				}
		}); 
				
		
		String postParam = "";
		try {
			if(null == token)
				token = "";
			
		    postParam = "token="+URLEncoder.encode(token, "UTF-8");
		}catch (Exception e) {
			if (callback != null) {
                callback.onCompeleted(0, e.getMessage());
            }
		}
		
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   closeProgDlg();
				}
		}); 
    	
        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "dereg resp body:" + respStr);
            
            if (resp != null) {
                if (callback != null) {
                    callback.onCompeleted(0, respStr);
                }
            }
        }else if("10012".equals(resultStr)){
        	try{
        	    if(loginUMSPService(context,act)){
        	    	getIFAADeregRequest(context,act,secData,callback);
        	    }else{
        	    	 if (callback != null) {
                         callback.onCompeleted(0,returnStr);
                     }
        	    }
        	    	
        	}catch(Exception ex){
        		 if (callback != null) {
                     callback.onCompeleted(0,ex.getMessage());
                 }
        	}
        }else {
        	System.out.println("调用失败:"+returnStr);
        	
        	if (callback != null) {
                callback.onCompeleted(0, returnStr);
           }
            //if("0".equals(resultStr))
              //  Toast.makeText(context,"账户未登录，请先登录",Toast.LENGTH_SHORT).show();	
           // else
            //	Toast.makeText(context,"网络连接异常或无法访问服务",Toast.LENGTH_SHORT).show();	
        }
    }
    
    
    private  Boolean  getIFAAFPCheckStatus(final Context context,String act,String strDevInfo){
    	String timeout = context.getString(R.string.WebService_Timeout);				
		String urlPath = context.getString(R.string.UMSP_Service_IFAAFPCheck);										

		final Handler handler = new Handler(context.getMainLooper());		
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   showProgDlg(context,"指纹查询中...");
				}
		}); 
		
		String postParam = "";
		try {
			if(null == strDevInfo)
				strDevInfo = "";
			
		    postParam = "deviceID="+URLEncoder.encode(strDevInfo, "UTF-8");
		}catch (Exception e) {
			return false;
		}
		
        String responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
    	
        net.sf.json.JSONObject jb = net.sf.json.JSONObject.fromObject(responseStr);
		final String resultStr = jb.getString(CommonConst.RETURN_CODE);
		final String returnStr = jb.getString(CommonConst.RETURN_MSG);
    	
		handler.post(new Runnable() {
			   @Override
				public void run() {
				   closeProgDlg();
				}
		}); 
		
        if("0".equals(resultStr)){
            System.out.println("调用成功");
            net.sf.json.JSONObject resp =  net.sf.json.JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));
            String respStr = jb.getString(CommonConst.RETURN_RESULT);
            Log.i(TAG, "IFFA check result:" + respStr);
            
            if (resp != null) {
            	token = resp.getString("token");
                if(token.isEmpty())
                   return false;
                else
                   return true;
            }
        }else if("10012".equals(resultStr)){
        	try{
        	    if(loginUMSPService(context,act)){
        	    	getIFAAFPCheckStatus(context,act,strDevInfo);
        	    }else{
        	    	return false;
        	    }
        	    	
        	}catch(Exception ex){
        		return false;
        	}
        }else {
        	System.out.println("调用失败:"+returnStr);
        	
        	return false;
        }
    	
        return false;
    }
    
    private  Boolean loginUMSPService(final Context context,String act) throws Exception{    //重新登录UM Service
		   String returnStr = "";
			try {
				 AccountDao mAccountDao = null;
				 mAccountDao = new AccountDao(context);
				
				//异步调用UMSP服务：用户登录
				String timeout = context.getString(R.string.WebService_Timeout);				
				String urlPath = context.getString(R.string.UMSP_Service_Login);
				String strPass = getPWDHash(mAccountDao.getLoginAccount().getPassword(),null);

				String responseStr = "";
				try {
					//清空本地缓存
					WebClientUtil.cookieStore = null;
					//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
					String postParam = "accountName="+URLEncoder.encode(act, "UTF-8")+
      		                       "&pwdHash="+URLEncoder.encode(strPass, "UTF-8")+
      		                       "&appID="+URLEncoder.encode( mAccountDao.getLoginAccount().getAppIDInfo(), "UTF-8");
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

				if (!resultStr.equals("0")) 
					return false;
					
			} catch (Exception exc) {
				//closeProgDlg();
				return false;
			}
			
			//closeProgDlg();
			return true;
	}
	
	private  String   getPWDHash(String strPWD,Cert cert){
		String strPWDHash = "";
		
		if(null != cert && (CommonConst.USE_NO_FINGER_TYPE == cert.getFingertype())){
			if(!"".equals(cert.getCerthash())) {
                //return cert.getCerthash();
                if(!"".equals(strPWD) && strPWD.length() > 0)
                    return strPWD;
            }else
			    return strPWD;
		}

        if(null != cert) {
            if (!"".equals(strPWD) && strPWD.length() > 0)
                return strPWD;
        }

		javasafeengine oSE = new javasafeengine();
		byte[] bText = strPWD.getBytes();
		byte[] bDigest = oSE.digest(bText, "SHA-1", "SUN");   //做摘要
		strPWDHash = new String(Base64.encode(bDigest));
		
		return strPWDHash;
	}
	
	private void showProgDlg(final Context context,String strMsg){
		progDialog = new ProgressDialog(context);
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
