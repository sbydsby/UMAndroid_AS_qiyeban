package com.sheca.umee.util;

import android.os.Build;

import com.sheca.javasafeengine;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("deprecation")
public class MySSLSocketFactory extends SSLSocketFactory {  
    SSLContext sslContext = SSLContext.getInstance("TLS");  
  
    @SuppressWarnings("deprecation")
	public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {  
        super(truststore);  
  
        TrustManager tm = new X509TrustManager() {  
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {       	
            }  
  
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { 
            	//throw new CertificateException("无效证书");
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
                 	 } 
                 	 
                 	 if (null == error) {
                 	    for (X509Certificate cert : chain) {  
             			    String strCert = cert.getSubjectDN().toString();
             			    if(strCert.indexOf(",")!=-1)
     		                   strCert = strCert.substring(0,strCert.indexOf(","));

     		                strCert = strCert.substring(strCert.indexOf("=")+1);
     		  
     		                if(strCert.equals(CommonConst.HTTPS_VALID_CERT_NAME)){   
     		            	    if(verifyCert(cert))
     		            		   isCertValid = true;
     		                }	 
                 	    }
                 	    
                 	    if(!isCertValid){
                       		 throw new CertificateException("无效证书");
                  	    }
                 	}else {
                  	     throw new CertificateException(error);
                  	}      
               }

            }  
  
            public X509Certificate[] getAcceptedIssuers() {  
                return null;  
            }  
        };  
  
        sslContext.init(null, new TrustManager[] { tm }, null);  
        setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }  
  
    @Override  
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {  
    	 injectHostname(socket, host);
         Socket sslSocket = sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
         // throw an exception if the hostname does not match the certificate
         getHostnameVerifier().verify(host, (SSLSocket) sslSocket);  
         
        //return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);    
         return  sslSocket; 
    }  
  
    @Override  
    public Socket createSocket() throws IOException {  
        return sslContext.getSocketFactory().createSocket();  
    }  
    
    private void injectHostname(Socket socket, String host) {
        try {
            if (Integer.valueOf(Build.VERSION.SDK) >= 4) {
                Field field = InetAddress.class.getDeclaredField("hostName");
                field.setAccessible(true);
                field.set(socket.getInetAddress(), host);
            }
        } catch (Exception ignored) {
        	
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
}  





/*
public class SecureSocketFactory extends SSLSocketFactory {
	 
    private static final String LOG_TAG = "SecureSocketFactory";
 
    private final SSLContext sslCtx;
    private final X509Certificate[] acceptedIssuers;
 
   
    public SecureSocketFactory(KeyStore store, String alias)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            UnrecoverableKeyException {
 
        super(store);
 
        // Loading the CA certificate from store.
        final Certificate rootca = store.getCertificate(alias);
 
        // Turn it to X509 format.
        InputStream is = new ByteArrayInputStream(rootca.getEncoded());
        X509Certificate x509ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
        AsyncHttpClient.silentCloseInputStream(is);
 
        if (null == x509ca) {
            throw new CertificateException("Embedded SSL certificate has expired.");
        }
 
        // Check the CA's validity.
        x509ca.checkValidity();
 
        // Accepted CA is only the one installed in the store.
        acceptedIssuers = new X509Certificate[]{x509ca};
 
        sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(
                null,
                new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }
 
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                Exception error = null;
 
                                if (null == chain || 0 == chain.length) {
                                    error = new CertificateException("Certificate chain is invalid.");
                                } else if (null == authType || 0 == authType.length()) {
                                    error = new CertificateException("Authentication type is invalid.");
                                } else {
                                    Log.i(LOG_TAG, "Chain includes " + chain.length + " certificates.");
                                    try {
                                        for (X509Certificate cert : chain) {
                                            Log.i(LOG_TAG, "Server Certificate Details:");
                                            Log.i(LOG_TAG, "---------------------------");
                                            Log.i(LOG_TAG, "IssuerDN: " + cert.getIssuerDN().toString());
                                            Log.i(LOG_TAG, "SubjectDN: " + cert.getSubjectDN().toString());
                                            Log.i(LOG_TAG, "Serial Number: " + cert.getSerialNumber());
                                            Log.i(LOG_TAG, "Version: " + cert.getVersion());
                                            Log.i(LOG_TAG, "Not before: " + cert.getNotBefore().toString());
                                            Log.i(LOG_TAG, "Not after: " + cert.getNotAfter().toString());
                                            Log.i(LOG_TAG, "---------------------------");
 
                                            // Make sure that it hasn't expired.
                                            cert.checkValidity();
 
                                            // Verify the certificate's public key chain.
                                            cert.verify(rootca.getPublicKey());
                                        }
                                    } catch (InvalidKeyException e) {
                                        error = e;
                                    } catch (NoSuchAlgorithmException e) {
                                        error = e;
                                    } catch (NoSuchProviderException e) {
                                        error = e;
                                    } catch (SignatureException e) {
                                        error = e;
                                    }
                                }
                                if (null != error) {
                                    Log.e(LOG_TAG, "Certificate error", error);
                                    throw new CertificateException(error);
                                }
                            }
 
                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return acceptedIssuers;
                            }
                        }
                },
                null
        );
 
        setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }
 
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {
 
        injectHostname(socket, host);
        Socket sslSocket = sslCtx.getSocketFactory().createSocket(socket, host, port, autoClose);
 
        // throw an exception if the hostname does not match the certificate
        getHostnameVerifier().verify(host, (SSLSocket) sslSocket);
 
        return sslSocket;
    }
 
    @Override
    public Socket createSocket() throws IOException {
        return sslCtx.getSocketFactory().createSocket();
    }
 
  
    private void injectHostname(Socket socket, String host) {
        try {
            if (Integer.valueOf(Build.VERSION.SDK) >= 4) {
                Field field = InetAddress.class.getDeclaredField("hostName");
                field.setAccessible(true);
                field.set(socket.getInetAddress(), host);
            }
        } catch (Exception ignored) {
        }
    }
 
 
} 
*/

