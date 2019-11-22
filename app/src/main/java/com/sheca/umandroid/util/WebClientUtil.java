package com.sheca.umandroid.util;

import android.util.Log;

import com.sheca.javasafeengine;
import com.sheca.umandroid.LaunchActivity;
import com.sheca.umandroid.MainActivity;

import net.sf.json.JSONObject;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.spongycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebClientUtil {
	//本地cookie
	public static CookieStore cookieStore = null;
	public static String mCookieStore = "";
	public static Certificate mCert = null;
	public static List<String>     mCertChainList = new ArrayList<String>();
	public static String     mCertChain = "";
	public static String     mEncodeCert = "";
	
	public static boolean mBScanPost = false;
	
	protected static final String TAG = "UniTrust";

	@SuppressWarnings({ "deprecation"})
	public static String httpPost(String url, Map<String,String> map, int timeout)
			throws Exception {
		String sReturn = "";
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);

		DefaultHttpClient client = new DefaultHttpClient(params);
		client = (DefaultHttpClient)getNewHttpClient(params);
		//client = (DefaultHttpClient)getHttpsClient(client);
		
		if (cookieStore != null) {  
			client.setCookieStore(cookieStore); 
			System.out.println("cookieStore:" + cookieStore);	
		}

		HttpPost httpPost = new HttpPost(url);
		UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(getParam(map), "UTF-8");
		httpPost.setEntity(postEntity);
		//System.out.println("request line:" + httpPost.getRequestLine());	
	
		HttpResponse httpResponse = client.execute(httpPost);
		//printResponse(httpResponse);
		//cookieStore = null;
		// cookie store
		if (cookieStore == null) {  
			setCookieStore(httpResponse);
		}		
		
		sReturn = EntityUtils.toString(httpResponse.getEntity());
		return sReturn;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked"})
	public static String postJson(String url, Map<String,String> map, int timeout)
			throws Exception {
		String sReturn = "";
		
		@SuppressWarnings("rawtypes")
		List dataList = new ArrayList();
		if (map != null) {
			Set<String> key = map.keySet();
			for (Iterator<String> it = key.iterator(); it.hasNext();) {
				String k = (String) it.next();
				dataList.add(new BasicNameValuePair(k, (String)map.get(k)));
			}
		}

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		HttpClient client = new DefaultHttpClient(params);
		
		if(url.indexOf("https://") != -1){
		//DefaultHttpClient client = new DefaultHttpClient(params);
		   client = (DefaultHttpClient)getNewHttpClient(params);
		}
		
		HttpPost httpPost = new HttpPost(url);
		HttpEntity entity = new UrlEncodedFormEntity(dataList, "UTF-8");
		httpPost.setEntity(entity);
		
		HttpResponse response = client.execute(httpPost);
	
		sReturn = EntityUtils.toString(response.getEntity());

		return sReturn;
	}
	
	@SuppressWarnings({ "deprecation"})
	public static void printResponse(HttpResponse httpResponse)
		      throws ParseException, IOException {
		    // 获取响应消息实体
		    HttpEntity entity = httpResponse.getEntity();
		    // 响应状态
		    System.out.println("status:" + httpResponse.getStatusLine());
		    System.out.println("headers:");
		    HeaderIterator iterator = httpResponse.headerIterator();
		    while (iterator.hasNext()) {
		      System.out.println("\t" + iterator.next());
		    }
		    // 判断响应实体是否为空
		    if (entity != null) {
		      String responseString = EntityUtils.toString(entity);
		      System.out.println("response length:" + responseString.length());
		      System.out.println("response content:" + responseString.replace("\r\n", ""));
		    }
	}
	
	@SuppressWarnings("deprecation")
	public static void setCookieStore(HttpResponse httpResponse) {
		    System.out.println("----setCookieStore");
		    cookieStore = new BasicCookieStore();
		    // SessionId
		    String setCookie = httpResponse.getFirstHeader("Set-Cookie").getValue();
		    System.out.println("setCookie:" + setCookie);
		    String sessionId = setCookie.substring("ASP.NET_SessionId=".length(), setCookie.indexOf(";"));
		    System.out.println("ASP.NET_SessionId:" + sessionId);
		    // 新建一个Cookie
		    BasicClientCookie cookie = new BasicClientCookie("ASP.NET_SessionId", sessionId);
		    cookie.setVersion(0);
		    cookie.setDomain(CommonConst.WEB_DOMAIN_CONFIG);      //外网配置
		    //cookie.setDomain("192.168.2.133");       //内网配置
		    //cookie.setPath("/");
		    // cookie.setAttribute(ClientCookie.VERSION_ATTR, "0");
		    // cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "127.0.0.1");
		    // cookie.setAttribute(ClientCookie.PORT_ATTR, "8080");
		    // cookie.setAttribute(ClientCookie.PATH_ATTR, "/CwlProWeb");
		    cookieStore.addCookie(cookie);
	}

	@SuppressWarnings("deprecation")
	public static List<NameValuePair> getParam(Map<String,String> parameterMap) {
		    List<NameValuePair> param = new ArrayList<NameValuePair>();
		    Iterator<Entry<String,String>> it = parameterMap.entrySet().iterator();
		    while (it.hasNext()) {
		      Entry<String,String> parmEntry = (Entry<String,String>) it.next();
		      param.add(new BasicNameValuePair((String) parmEntry.getKey(), (String) parmEntry.getValue()));
		    }
		    return param;
	}
	
	
    @SuppressWarnings("deprecation")
	public static HttpClient getNewHttpClient(HttpParams params) {  
        try {  
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            trustStore.load(null, null);  
      
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);  
            //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);   //允许所有主机的验证
           
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);  
            
            // 设置http https支持
            SchemeRegistry registry = new SchemeRegistry();  
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));  
            registry.register(new Scheme("https", sf, 443));  
      
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);  
      
            return new DefaultHttpClient(ccm, params);  
        } catch (Exception e) {  
        	MainActivity.strErr = e.getMessage();
            return new DefaultHttpClient();  
        }  
    } 
    
    @SuppressWarnings({ "deprecation"})
    public static HttpClient getHttpsClient(HttpClient mHttpClient) {    	
    	 try {
			  KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
			  keyStore.load(null, null);
			  keyStore.setCertificateEntry("trust", mCert);
				 	 
			  SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
			  Scheme sch = new Scheme("https", socketFactory, 443);
			  mHttpClient.getConnectionManager().getSchemeRegistry().register(sch);
		 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
    		 		 	       
    	 return mHttpClient;		
    }
    
    /* @author suncat
     * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * @return
     */
    public static final boolean isPing() { 
      //return true; 
    	
      String result = null; 
      try { 
          String ip = "www.bing.com";  // ping 的地址，可以换成任何一种可靠的外网 
          Process p = Runtime.getRuntime().exec("ping -c 1 -w 5 " + ip);// ping网址3次 
          // 读取ping的内容，可以不加 
          InputStream input = p.getInputStream(); 
          BufferedReader in = new BufferedReader(new InputStreamReader(input)); 
          StringBuffer stringBuffer = new StringBuffer(); 
          String content = ""; 
          
          while ((content = in.readLine()) != null) { 
              stringBuffer.append(content); 
          } 
          
          Log.d("------ping-----", "result content : " + stringBuffer.toString()); 
          // ping的状态 
          int status = p.waitFor();
          if (status == 0) { 
              result = "success"; 
              return true; 
          } else { 
              result = "failed"; 
          } 
      } catch (IOException e) { 
          result = "IOException"; 
      } catch (InterruptedException e) { 
          result = "InterruptedException"; 
      } finally { 
          Log.d("----result---", "result = " + result); 
      } 
      return false;
    
    }
    
    
    public static String  getHttpClientPost(String httpUrl,String postParam ,int timeout) {  
    	String result = "";  

        HttpURLConnection http = null;  
        URL url;  
        int retCode = -1;
        try {  
            url = new URL(httpUrl);  
            // 判断是http请求还是https请求  
            if (url.getProtocol().toLowerCase().equals("https")) {  
                trustAllHosts();  
                http = (HttpsURLConnection) url.openConnection();  
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认  
            } else {  
                http = (HttpURLConnection) url.openConnection();  
            }  
            
            http.setConnectTimeout(timeout);// 设置超时时间  
            http.setReadTimeout(timeout);  
            http.setRequestMethod("POST");// 设置请求类型为post  
            http.setDoInput(true);  
            http.setDoOutput(true);  

            // 传递的数据  
            String data = postParam;//"appName=" + URLEncoder.encode("Unitrust", "UTF-8") + "&currentVersion=" + URLEncoder.encode("2.2.0", "UTF-8");  
            //国密安审对参数加密
            if(LaunchActivity.isGMCheck){
            	String strEnlope = PKIUtil.envelopeEncryptEx(postParam, mEncodeCert);
            	if(LaunchActivity.LOG_FLAG)
                    LaunchActivity.logUtil.recordLogServiceLog("\n"+httpUrl+"\n"+strEnlope);
            	
            	Log.i(TAG, "UM:"+httpUrl+"\n"+strEnlope); 
                data = URLEncoder.encode(strEnlope, "UTF-8");
            }
            // 设置请求的头  
            http.setRequestProperty("Connection", "keep-alive");  
            // 设置请求的头  
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
            // 设置请求的头  
            http.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));  
            // 设置请求的头  
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");  
            http.setRequestProperty("Cookie", mCookieStore);  
            http.connect(); 
            
            DataOutputStream out = new DataOutputStream(http.getOutputStream());  
            out.writeBytes(data);  
            out.flush();  
            out.close();  
            retCode = http.getResponseCode();// 设置http返回状态200还是403  
            BufferedReader in = null;  
            if (retCode == 200) {  
                getCookie(http);  
                in = new BufferedReader(new InputStreamReader(  
                        http.getInputStream()));  
            } else  
                in = new BufferedReader(new InputStreamReader(  
                        http.getErrorStream()));  
            result = in.readLine();// 得到返回结果  
            in.close();  
            http.disconnect();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            
            JSONObject jo = new JSONObject();
            jo.put(CommonConst.RETURN_CODE,CommonConst.NET_CONNECT_ERROR_CODE);
            jo.put(CommonConst.RETURN_MSG,CommonConst.NET_CONNECT_ERROR_MSG);
            
            result = jo.toString();
        }  
        
        return result;
    }
    
    
    public static String  postHttpClientJson(String httpUrl,String postParam ,int timeout) {  
    	String result = "";  

        HttpURLConnection http = null;  
        URL url;  
        int retCode = -1;
        try {  
            url = new URL(httpUrl);  
            // 判断是http请求还是https请求  
            if (url.getProtocol().toLowerCase().equals("https")) {  
                trustAllHosts();  
                http = (HttpsURLConnection) url.openConnection();  
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认  
            } else {  
                http = (HttpURLConnection) url.openConnection();  
            }  
            http.setConnectTimeout(timeout);// 设置超时时间  
            http.setReadTimeout(timeout);  
            http.setRequestMethod("POST");// 设置请求类型为post  
            http.setDoInput(true);  
            http.setDoOutput(true);  

            // 传递的数据  
            String data = postParam;//"appName=" + URLEncoder.encode("Unitrust", "UTF-8") + "&currentVersion=" + URLEncoder.encode("2.2.0", "UTF-8");  
            // 设置请求的头  
            http.setRequestProperty("Connection", "keep-alive");  
            // 设置请求的头  
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
            // 设置请求的头  
            http.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));  
            // 设置请求的头  
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");  
           // http.setRequestProperty("Cookie", mCookieStore);  
            http.connect(); 
            
            DataOutputStream out = new DataOutputStream(http.getOutputStream());  
            out.writeBytes(data);  
            out.flush();  
            out.close();  
            retCode = http.getResponseCode();// 设置http返回状态200还是403  
            BufferedReader in = null;  
            if (retCode == 200) {  
                //getCookie(http);  
                in = new BufferedReader(new InputStreamReader(  
                        http.getInputStream()));  
            } else  
                in = new BufferedReader(new InputStreamReader(  
                        http.getErrorStream()));  
            result = in.readLine();// 得到返回结果  
            in.close();  
            http.disconnect();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            result = "";
        }  
        
        return result;
    }
    
    public static String postJsonArray(String url, JSONObject json, int timeout)
			throws Exception {
		String sReturn = "";
		
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		        
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.addHeader("Authorization", "Basic YWRtaW46");
		
		StringEntity s = new StringEntity(json.toString(), "UTF-8");
	    s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	    httpPost.setEntity(s);

		HttpResponse response = client.execute(httpPost);
		sReturn = EntityUtils.toString(response.getEntity());

		return sReturn;
	}
    
    public static String postJsonArray(String httpUrl, String json, int timeout)
			throws Exception {
    	String result = "";  

        HttpURLConnection http = null;  
        URL url;  
        int retCode = -1;
        try {  
            url = new URL(httpUrl);  
            // 判断是http请求还是https请求  
            if (url.getProtocol().toLowerCase().equals("https")) {  
                trustAllHosts();  
                http = (HttpsURLConnection) url.openConnection();  
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认  
            } else {  
                http = (HttpURLConnection) url.openConnection();  
            }  
            http.setConnectTimeout(timeout);// 设置超时时间  
            http.setReadTimeout(timeout);  
            http.setRequestMethod("POST");// 设置请求类型为post  
            http.setDoInput(true);  
            http.setDoOutput(true);  

            // 传递的数据  
            String data = json;//"appName=" + URLEncoder.encode("Unitrust", "UTF-8") + "&currentVersion=" + URLEncoder.encode("2.2.0", "UTF-8");  
            // 设置请求的头  
            http.setRequestProperty("Connection", "keep-alive");  
            http.setRequestProperty("Charset", "UTF-8");
            // 设置请求的头  
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");  
            http.setRequestProperty("accept","application/json");
            // 设置请求的头  
            http.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));  
            // 设置请求的头  
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");  
           // http.setRequestProperty("Cookie", mCookieStore);  
            http.connect(); 
            
            DataOutputStream out = new DataOutputStream(http.getOutputStream());  
            out.writeBytes(data);  
            out.flush();  
            out.close();  
            retCode = http.getResponseCode();// 设置http返回状态200还是403  
            BufferedReader in = null;  
            if (retCode == 200) {  
                //getCookie(http);  
                in = new BufferedReader(new InputStreamReader(  
                        http.getInputStream()));  
            } else  
                in = new BufferedReader(new InputStreamReader(  
                        http.getErrorStream()));  
            result = in.readLine();// 得到返回结果  
            in.close();  
            http.disconnect();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            result = "";
        }  
        
        return result;
  
    }
    
    /** 得到cookie */  
    private static void getCookie(HttpURLConnection http) {  
        String cookieVal = null;  
        String key = null;  

        if("".equals(mCookieStore)){
           for (int i = 1; (key = http.getHeaderFieldKey(i)) != null; i++) {  
               if (key.equalsIgnoreCase("set-cookie")) {  
                  cookieVal = http.getHeaderField(i);  
                  cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));  
                  mCookieStore = mCookieStore + cookieVal  + ";";  
               }  
           }  
        }
    }  

    static  TrustManager[] xtmArray = new MytmArray[] { new MytmArray() };
    
    /** 
     * 信任所有主机-对于任何证书都不做检查 
     */  
    private static void trustAllHosts() {  
        // Create a trust manager that does not validate certificate chains  
        // Android 采用X509的证书信息机制  
        // Install the all-trusting trust manager  
        try {  
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, xtmArray, new java.security.SecureRandom());  
            HttpsURLConnection  
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());  
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);//不进行主机名确认  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {  
        @Override  
        public boolean verify(String hostname, SSLSession session) {  
            // TODO Auto-generated method stub  
            // System.out.println("Warning: URL Host: " + hostname + " vs. "  
            // + session.getPeerHost());
        	if(!mBScanPost){
        	   if(!hostname.equals(CommonConst.HTTPS_VALID_CERT_NAME))
        	      return false;
        	}
        	
            return true;  
        }  
    };  

}

class MytmArray implements X509TrustManager {  
    public X509Certificate[] getAcceptedIssuers() {  
        // return null;  
        return new X509Certificate[] {};  
    }  
  
    @Override  
    public void checkClientTrusted(X509Certificate[] chain, String authType)  
            throws CertificateException {  
        // TODO Auto-generated method stub  
        Exception error = null;
   	    boolean isCertValid = false;
   		
   	    if (null == chain || 0 == chain.length) {
               error = new CertificateException("Certificate chain is invalid.");
        } else if (null == authType || 0 == authType.length()) {
               error = new CertificateException("Authentication type is invalid.");
        } else {
          	 try {                    	
          		 for (X509Certificate cert : chain) {  
          			 // Make sure that it hasn't expired.
                      cert.checkValidity();
          		 }     		            
          	 } catch (Exception e) {
                   error = e;
                   isCertValid = false;
          	 } 
          	 
          	 if (null == error) {
          		if(WebClientUtil.mBScanPost)
          			isCertValid = true;
          		else{
          			isCertValid = true;
          			/*
          	        for (X509Certificate cert : chain) {  
      			        String strCert = cert.getSubjectDN().toString();
      			        if(strCert.indexOf(",")!=-1)
		                   strCert = strCert.substring(0,strCert.indexOf(","));

		                strCert = strCert.substring(strCert.indexOf("=")+1);
		  
		                if(strCert.equals(CommonConst.HTTPS_VALID_CERT_NAME)){   
		                	isCertValid = true;
		            	    if(verifyCert(cert))
		            		   isCertValid = true;
		                }	 
          	        }
          	        */
          		}
          	    
          	    if(!isCertValid){   	
                		 throw new CertificateException("无效证书");
           	    }
          	}else {
           	     throw new CertificateException(error);
           	}      
        }
    	
    }  
  
    @Override  
    public void checkServerTrusted(X509Certificate[] chain, String authType)  throws CertificateException {  
        // TODO Auto-generated method stub  
        // System.out.println("cert: " + chain[0].toString() + ", authType: "  
        // + authType);  
    	 Exception error = null;
    	 boolean isCertValid = false;
    		
    	 if (null == chain || 0 == chain.length) {
                error = new CertificateException("Certificate chain is invalid.");
         } else if (null == authType || 0 == authType.length()) {
                error = new CertificateException("Authentication type is invalid.");
         } else {
           	 try {                    	
           		 for (X509Certificate cert : chain) {  
           			 // Make sure that it hasn't expired.
                       cert.checkValidity();
           		 }     		            
           	 } catch (Exception e) {
                    error = e;
                    isCertValid = false;
           	 } 
           	 
           	 if (null == error) {
           		if(WebClientUtil.mBScanPost)
          			isCertValid = true;
          		else{
          		   isCertValid = true;
          			/*
           	       for (X509Certificate cert : chain) {  
       			       String strCert = cert.getSubjectDN().toString();
       			       if(strCert.indexOf(",")!=-1)
 		                   strCert = strCert.substring(0,strCert.indexOf(","));

 		                strCert = strCert.substring(strCert.indexOf("=")+1);
 		  
 		                if(strCert.equals(CommonConst.HTTPS_VALID_CERT_NAME)){   
 		                	isCertValid = true;
 		            	    if(verifyCert(cert))
 		            		   isCertValid = true;
 		                }
           	       }
           	       */
           	    }
           	    
           	    if(!isCertValid){   	
                 		 throw new CertificateException("无效证书");
            	}
           	}else {
            	     throw new CertificateException(error);
            	}      
         }
    } 

    private  boolean verifyCert(X509Certificate cert){
 	   javasafeengine jse = new javasafeengine();

 	   for(int i = 0;i < WebClientUtil.mCertChainList.size();i++){
 		   if(i == 1) 
 			   continue;
 		   
 		   if(null == WebClientUtil.mCertChainList.get(i))
 			   continue;
 		   
 		   byte[] bChain = Base64.decode(WebClientUtil.mCertChainList.get(i));
 		   //byte[] bChain = Base64.decode(WebClientUtil.mCertChain);
 		   if(null == bChain)
 			   continue;
 		   
 		   int iRtn = -1;
 		   try{
 		      iRtn = jse.verifyCert(cert, bChain, 0);
 		   }catch(Exception ex){
 			   continue;
 		   }
 		   
 		   if(iRtn == CommonConst.RET_VERIFY_CERT_OK)
 			   return true;	   
 	   }
 	   
		   return false;
    }
    
};  
