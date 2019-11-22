package net.sourceforge.simcpux;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Util {
	
	private static final String TAG = "SDK_Sample.Util";
	public static String mCookieStore = "";
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static byte[] httpGet(final String url) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpGet, url is null");
			return null;
		}

		HttpClient httpClient = getNewHttpClient();
		HttpGet httpGet = new HttpGet(url);

		try {
			HttpResponse resp = httpClient.execute(httpGet);
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
				return null;
			}

			return EntityUtils.toByteArray(resp.getEntity());

		} catch (Exception e) {
			Log.e(TAG, "httpGet exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] httpPost(String url, String entity) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpPost, url is null");
			return null;
		}
		
		HttpClient httpClient = getNewHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		
		try {
			httpPost.setEntity(new StringEntity(entity));
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse resp = httpClient.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
				return null;
			}

			return EntityUtils.toByteArray(resp.getEntity());
		} catch (Exception e) {
			Log.e(TAG, "httpPost exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	private static class SSLSocketFactoryEx extends SSLSocketFactory {      
	      
	    SSLContext sslContext = SSLContext.getInstance("TLS");      
	      
	    public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {      
	        super(truststore);      
	      
	        TrustManager tm = new X509TrustManager() {      
	      
	            public X509Certificate[] getAcceptedIssuers() {      
	                return null;      
	            }      
	      
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,	String authType) throws java.security.cert.CertificateException {
				}  
	        };      
	      
	        sslContext.init(null, new TrustManager[] { tm }, null);      
	    }      
	      
		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,	port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		} 
	}  

	private static HttpClient getNewHttpClient() { 
	   try { 
	       KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
	       trustStore.load(null, null); 

	       SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore); 
	       sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 

	       HttpParams params = new BasicHttpParams(); 
	       HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
	       HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 

	       SchemeRegistry registry = new SchemeRegistry(); 
	       registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
	       registry.register(new Scheme("https", sf, 443)); 

	       ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 

	       return new DefaultHttpClient(ccm, params); 
	   } catch (Exception e) { 
	       return new DefaultHttpClient(); 
	   } 
	}
	
	
	public static String  getHttpClientPost(String httpUrl,String postParam ,int timeout) {  
    	String result = "";  

        HttpURLConnection http = null;  
        URL url;  
        int retCode = -1;
        try {  
            url = new URL(httpUrl);  
            // �ж���http������https����  
            if (url.getProtocol().toLowerCase().equals("https")) {  
                trustAllHosts();  
                http = (HttpsURLConnection) url.openConnection();  
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);// ������������ȷ��  
            } else {  
                http = (HttpURLConnection) url.openConnection();  
            }  
            http.setConnectTimeout(timeout);// ���ó�ʱʱ��  
            http.setReadTimeout(timeout);  
            http.setRequestMethod("POST");// ������������Ϊpost  
            http.setDoInput(true);  
            http.setDoOutput(true);  

            // ���ݵ�����  
            String data = postParam;//"appName=" + URLEncoder.encode("Unitrust", "UTF-8") + "&currentVersion=" + URLEncoder.encode("2.2.0", "UTF-8");  
           
            // ���������ͷ  
            http.setRequestProperty("Connection", "keep-alive");  
            // ���������ͷ  
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
            // ���������ͷ  
            http.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));  
            // ���������ͷ  
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");  
            http.setRequestProperty("Cookie", mCookieStore);  
            http.connect(); 
            
            DataOutputStream out = new DataOutputStream(http.getOutputStream());  
            out.writeBytes(data);  
            out.flush();  
            out.close();  
            retCode = http.getResponseCode();// ����http����״̬200����403  
            BufferedReader in = null;  
            if (retCode == 200) {  
                getCookie(http);  
                in = new BufferedReader(new InputStreamReader(  
                        http.getInputStream()));  
            } else  
                in = new BufferedReader(new InputStreamReader(  
                        http.getErrorStream()));  
            result = in.readLine();// �õ����ؽ��  
            in.close();  
            http.disconnect();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            result = "";
        }  
        
        return result;
    }
	
	 /** �õ�cookie */  
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
     * ������������-�����κ�֤�鶼������� 
     */  
    private static void trustAllHosts() {  
        // Create a trust manager that does not validate certificate chains  
        // Android ����X509��֤����Ϣ����  
        // Install the all-trusting trust manager  
        try {  
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, xtmArray, new java.security.SecureRandom());  
            HttpsURLConnection  
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());  
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);//������������ȷ��  
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
        	if(!hostname.equals("umsp.sheca.com"))
        		return false;
        	
            return true;  
        }  
    };  

	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			Log.i(TAG, "readFromFile: file not found");
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len + " offset + len = " + (offset + len));

		if(offset <0){
			Log.e(TAG, "readFromFile invalid offset:" + offset);
			return null;
		}
		if(len <=0 ){
			Log.e(TAG, "readFromFile invalid len:" + len);
			return null;
		}
		if(offset + len > (int) file.length()){
			Log.e(TAG, "readFromFile invalid file len:" + file.length());
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len]; // ���������ļ���С������
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
			e.printStackTrace();
		}
		return b;
	}
	
	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;
	public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
//		Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth + "x" + options.outHeight + ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG, "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}
	
	public static String sha1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes());
			
			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static List<String> stringsToList(final String[] src) {
		if (src == null || src.length == 0) {
			return null;
		}
		final List<String> result = new ArrayList<String>();
		for (int i = 0; i < src.length; i++) {
			result.add(src[i]);
		}
		return result;
	}
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
          	    for (X509Certificate cert : chain) {  
      			    String strCert = cert.getSubjectDN().toString();
      			    if(strCert.indexOf(",")!=-1)
		                   strCert = strCert.substring(0,strCert.indexOf(","));

		                strCert = strCert.substring(strCert.indexOf("=")+1);
		  
		                if(strCert.equals("umsp.sheca.com")){   
		                	isCertValid = true;
		            	    if(verifyCert(cert))
		            		   isCertValid = true;
		                }	 
          	    }
          	    
          	    if(!isCertValid){   	
                		 throw new CertificateException("��Ч֤��");
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
           	    for (X509Certificate cert : chain) {  
       			    String strCert = cert.getSubjectDN().toString();
       			    if(strCert.indexOf(",")!=-1)
 		                   strCert = strCert.substring(0,strCert.indexOf(","));

 		                strCert = strCert.substring(strCert.indexOf("=")+1);
 		  
 		                if(strCert.equals("umsp.sheca.com")){   
 		                	isCertValid = true;
 		            	    if(verifyCert(cert))
 		            		   isCertValid = true;
 		                }	 
           	    }
           	    
           	    if(!isCertValid){   	
                 		 throw new CertificateException("��Ч֤��");
            	    }
           	}else {
            	     throw new CertificateException(error);
            	}      
         }
    } 

    private  boolean verifyCert(X509Certificate cert){
 			   return true;	   
    }
    
};  


