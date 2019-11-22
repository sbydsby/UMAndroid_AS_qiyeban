package com.sheca.umandroid.util;

import android.util.Log;

import com.sheca.javasafeengine;
import com.sheca.umandroid.model.ShcaCciStd;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import BASE64.BASE64Decoder;

/**
 * @Title: PKIUtil.java
 * @Package com.sheca.msgw.test.util
 * @Description: PKI工具类
 * @author Renzirong
 * @date 2013-1-30 上午1:05:18
 * @version V1.0
 */
public class PKIUtil {

	private static String asAlg = "RSA/ECB/PKCS1Padding";
	private static String provider = "SUN";
	private static String algProvider = "SunJCE";
	private static String storeType = "PKCS12";
	private static String signAlg = CommonConst.USE_CERT_ALG_RSA;
	private static String signProvider = "SunRsaSign";
	private static String ranAlg = "SHA1PRNG";
	private static String encAlg = "TripleDES";
	private static String digestAlg = "SHA-1";

	public static String envelopeEncrypt(String data, String certificate)
			throws Exception {
		javasafeengine jse = new javasafeengine();

		Certificate cert = null;
		cert = jse.getCertFromBuffer(certificate.getBytes());
		if (cert == null) {
			cert = jse.getCertFromBuffer(Base64.decode(certificate));
		}
		
		//byte[] envelope = jse.envelope(1, data.getBytes(), cert, null, asAlg,provider, algProvider, ranAlg, encAlg);
		byte[] envelope = jse.encodeEnveloper_P7(data.getBytes(), certificate);
		
		return new String(Base64.encode(envelope));
	}

	public static String envelopeEncryptEx(String data, String certificate)
			throws Exception {
		javasafeengine jse = new javasafeengine();

		Certificate cert = null;
		cert = jse.getCertFromBuffer(certificate.getBytes());
		if (cert == null) {
			cert = jse.getCertFromBuffer(Base64.decode(certificate));
		}
		
		//byte[] envelope = jse.envelope(1, data.getBytes(), cert, null, asAlg,provider, algProvider, ranAlg, encAlg);
		//byte[] envelope = jse.encodeEnveloper_P7(data.getBytes(), certificate);
		
		//String strEncryptCert = "MIIEKjCCA82gAwIBAgIQTCLy9R1Y7/+4DewIO9rjszAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTEzMDMwNjE2MDAwMFoXDTE1MDMwNjE2MDAwMFowgY0xCzAJBgNVBAYTAkNOMQ8wDQYDVQQIHgZOCm13XgIxDTALBgNVBAceBE4KbXcxDjAMBgNVBAoTBVNIRUNBMQ0wCwYDVQQLHgR4FFPRMRwwGgYJKoZIhvcNAQkBFg13eWxAc2hlY2EuY29tMSEwHwYDVQQDHhhzi3OJZ5cAUwBNADIAXwB0AGUAcwB0ADEwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAARrcPSgHwU81iiToip4UQJB5D2D/FLt1BnWYJy1DMzKm1dKYcaLS7PY9wzmvTszsLhAkCK3GuHGA7psFJhFsngWo4ICYzCCAl8wIgYDVR0jAQH/BBgwFoAUiTEEkXtDqqqav4Qdm4bu8LhwmaAwIAYDVR0OAQH/BBYEFO/5vK92e2Q9aqSxtq1Hi2L1SYvlMA4GA1UdDwEB/wQEAwIEMDATBgNVHSUEDDAKBggrBgEFBQcDAjBCBgNVHSAEOzA5MDcGCSqBHAGG7zqBFTAqMCgGCCsGAQUFBwIBFhxodHRwOi8vd3d3LnNoZWNhLmNvbS9wb2xpY3kvMAkGA1UdEwQCMAAwgdwGA1UdHwSB1DCB0TCBl6CBlKCBkYaBjmxkYXA6Ly9sZGFwMi5zaGVjYS5jb206Mzg5L2NuPUNSTDAuY3JsLG91PVJBMjAxMzAyMTYsb3U9Q0E3MSxvdT1jcmwsbz1VbmlUcnVzdD9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0P2Jhc2U/b2JqZWN0Q2xhc3M9Y1JMRGlzdHJpYnV0aW9uUG9pbnQwNaAzoDGGL2h0dHA6Ly9sZGFwMi5zaGVjYS5jb20vQ0E3MS9SQTIwMTMwMjE2L0NSTDAuY3JsMH8GCCsGAQUFBwEBBHMwcTA2BggrBgEFBQcwAYYqaHR0cDovL29jc3AzLnNoZWNhLmNvbS9TaGVjYXNtMi9zaGVjYS5vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vbGRhcDIuc2hlY2EuY29tL3Jvb3Qvc2hlY2FzbTJzdWIuZGVyMB8GCCqBHNAUBAEBBBOgERMPMDAxMDcwMzIwMjA2ODE2MCIGCSqBHIbvOguBTQQVExMxQFNGMDAxMDcwMzIwMjA2ODE2MAwGCCqBHM9VAYN1BQADSQAwRgIhAK+uiLA6fBvqVXaPJVPJJFPmiL39yDHnOEP1wmoFi9LYAiEAw/cH2GXkjZfKJlC2GlmEp80KnEgw/CRb6xnfXd83YBY=";
		//String strEncryptCert = "MIIDszCCA1egAwIBAgIQdQbwFTwvsDDwL5/o4XUx1zAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE2MDQyNzE2MDAwMFoXDTIwMDQyNzE2MDAwMFowVTELMAkGA1UEBgwCQ04xETAPBgNVBAgMCFNoYW5naGFpMREwDwYDVQQKDAhVbmlUcnVzdDEgMB4GA1UEAwwXY29tbXVuaWNhdGlvbi5zaGVjYS5jb20wWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAATZQZIW/I6tjaHqZt0E1dEKbEPaK7MvrGYN1/WKECfxKdZLr6KqVm50KeIMSvK/0nLA3xxS9mos+k/JyTFWPlsco4ICJjCCAiIwHwYDVR0jBBgwFoAUiTEEkXtDqqqav4Qdm4bu8LhwmaAwHQYDVR0OBBYEFKsmrBkzJEiL/BZQ0BpFQ5TkVxtSMA4GA1UdDwEB/wQEAwIDODAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwQgYDVR0gBDswOTA3BgkqgRwBhu86gRUwKjAoBggrBgEFBQcCARYcaHR0cDovL3d3dy5zaGVjYS5jb20vcG9saWN5LzAJBgNVHRMEAjAAMIHgBgNVHR8EgdgwgdUwgZmggZaggZOGgZBsZGFwOi8vbGFkcDIuc2hlY2EuY29tOjM4OS9jbj1DUkw3NTIuY3JsLG91PVJBMjAxMzAyMTYsb3U9Q0E3MSxvdT1jcmwsbz1VbmlUcnVzdD9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0P2Jhc2U/b2JqZWN0Q2xhc3M9Y1JMRGlzdHJpYnV0aW9uUG9pbnQwN6A1oDOGMWh0dHA6Ly9sZGFwMi5zaGVjYS5jb20vQ0E3MS9SQTIwMTMwMjE2L0NSTDc1Mi5jcmwwfwYIKwYBBQUHAQEEczBxMDYGCCsGAQUFBzABhipodHRwOi8vb2NzcDMuc2hlY2EuY29tL1NoZWNhc20yL3NoZWNhLm9jc3AwNwYIKwYBBQUHMAKGK2h0dHA6Ly9sZGFwMi5zaGVjYS5jb20vcm9vdC9zaGVjYXNtMnN1Yi5kZXIwDAYIKoEcz1UBg3UFAANIADBFAiEA6inm33x2Eh+2gi4aFuQBq6T9xO7VP3Y2mldygb/wqH0CIAxPDpfu8izUWqepmiA/O2L4N/fYgip+ZTXla5Zjjj1g";
		
		String strEncryptCert = "MIIDvzCCA2OgAwIBAgIQZnhCMm2kkzXURSJlQxjQOzAMBggqgRzPVQGDdQUAMDQxCzAJBgNVBAYTAkNOMREwDwYDVQQKDAhVbmlUcnVzdDESMBAGA1UEAwwJU0hFQ0EgU00yMB4XDTE4MDMyNjAyMzQ0MVoXDTE5MDMyNjE1NTk1OVowNTELMAkGA1UEBgwCQ04xDjAMBgNVBAoMBXNoZWNhMRYwFAYDVQQDDA0xOTIuMTY4LjIuMjUzMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAESpJu0j5uaHc0Rbn7X97OwknoFk3CK+8taWM+Z16TyGbPXNKF7JW9PDgHmQVVTJ5I+WFMarNu9jHVDLbha52O16OCAlIwggJOMB8GA1UdIwQYMBaAFIkxBJF7Q6qqmr+EHZuG7vC4cJmgMB0GA1UdDgQWBBTO8t9VcTE2KHpOHWdPI9yZ+pw8XDAOBgNVHQ8BAf8EBAMCAzgwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMEIGA1UdIAQ7MDkwNwYJKoEcAYbvOoEVMCowKAYIKwYBBQUHAgEWHGh0dHA6Ly93d3cuc2hlY2EuY29tL3BvbGljeS8wCQYDVR0TBAIwADB/BggrBgEFBQcBAQRzMHEwNgYIKwYBBQUHMAGGKmh0dHA6Ly9vY3NwMy5zaGVjYS5jb20vU2hlY2FzbTIvc2hlY2Eub2NzcDA3BggrBgEFBQcwAoYraHR0cDovL2xkYXAyLnNoZWNhLmNvbS9yb290L3NoZWNhc20yc3ViLmRlcjAoBgkqgRyG7zoLgU0EGxMZOTM0MkBYWTkxMzEwMDAwNjMxMjkxMjg5WDCB4gYDVR0fBIHaMIHXMIGaoIGXoIGUhoGRbGRhcDovL2xkYXAyLnNoZWNhLmNvbTozODkvY249Q1JMMTMwNS5jcmwsb3U9UkEyMDEzMDIxNixvdT1DQTkxLG91PWNybCxvPVVuaVRydXN0P2NlcnRpZmljYXRlUmV2b2NhdGlvbkxpc3Q/YmFzZT9vYmplY3RDbGFzcz1jUkxEaXN0cmlidXRpb25Qb2ludDA4oDagNIYyaHR0cDovL2xkYXAyLnNoZWNhLmNvbS9DQTkxL1JBMjAxMzAyMTYvQ1JMMTMwNS5jcmwwDAYIKoEcz1UBg3UFAANIADBFAiEAt99fARDC/mlPnA/PiOlR0QyzJylONGE0QawZwWPw7VICIHcLm1eCR1AOuBrli8IWd0TVdltdeetgP9y/XiEiDIe2";
		
		//byte[] envelope = ShcaCciStd.gEsDev.doEncSM2Enveloper(data.getBytes(), strEncryptCert);
		byte[] envelope = ShcaCciStd.gSdk.doEncSM2Enveloper(data.getBytes(), strEncryptCert);
	    //strEncryptDate = new String(Base64.encode(envelope));	
		
		return new String(Base64.encode(envelope));
	}

	
	
	public static String envelopeDecrypt(String data, String p12, String pin)
			throws Exception {
		javasafeengine jse = new javasafeengine();
		String alias = "";

		InputStream sStroeStream = new ByteArrayInputStream(Base64.decode(p12));
		KeyStore keyStore = jse.getKeyStore(storeType, provider, sStroeStream,pin);
		/*Enumeration enu = jse.getAliasEnum(keyStore);
		while (enu.hasMoreElements()) {
			alias = (String) enu.nextElement();
		}
		Certificate cert = jse.getCertFromStore(keyStore, alias, pin);
		PrivateKey privateKey = (PrivateKey) jse.getPrivateKeyFromStore(
				keyStore, alias, pin);*/
		//byte[] envelope = jse.envelope(2, Base64.decode(data), cert,privateKey, asAlg, provider, algProvider, ranAlg, encAlg);
		byte[] envelope = jse.decodeEnveloper_P7(Base64.decode(data),keyStore, alias, pin);
		return new String(envelope,"UTF-8");
	}

	public static byte[] envelopeDecryptEnc(String data, String pvtKey,
			String cert) throws Exception {
		javasafeengine jse = new javasafeengine();
		KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec tmpEncodedKeySpec = new PKCS8EncodedKeySpec(
				Base64.decode(pvtKey));
		RSAPrivateKey tmpPrivKey = (RSAPrivateKey) rsaKeyFac
				.generatePrivate(tmpEncodedKeySpec);

		byte[] bPvtKey = jse.envelope(2, Base64.decode(data),
				jse.getCertFromBuffer(Base64.decode(cert)), tmpPrivKey, asAlg,
				provider, algProvider);
		return bPvtKey;
	}

	public static String sign(byte[] data, String p12, String pin) throws Exception {
		javasafeengine jse = new javasafeengine();
		String alias = "";
		InputStream sStroeStream = new ByteArrayInputStream(Base64.decode(p12));
		KeyStore keyStore = jse.getKeyStore(storeType, provider, sStroeStream,
				pin);
		Enumeration enu = jse.getAliasEnum(keyStore);
		while (enu.hasMoreElements()) {
			alias = (String) enu.nextElement();
		}

		PrivateKey privateKey = (PrivateKey) jse.getPrivateKeyFromStore(
				keyStore, alias, pin);
		byte[] sign = null;
		sign = jse.sign(data, privateKey, signAlg, signProvider);
		return new String(Base64.encode(sign));
	}

	public static boolean verifySign(String data, String sign,
			String certificate) throws Exception {
		javasafeengine jse = new javasafeengine();

		Certificate cert = null;
		cert = jse.getCertFromBuffer(certificate.getBytes());
		if (cert == null) {
			cert = jse.getCertFromBuffer(Base64.decode(certificate));
		}
		boolean result = jse.verifySign(data.getBytes(CommonConst.SIGN_STR_CODE), Base64.decode(sign),
				signAlg, cert, signProvider);
		return result;
	}

	public static String getCertSNFromCertificate(String certificate)
			throws Exception {
		javasafeengine jse = new javasafeengine();
		Certificate cert = jse.getCertFromBuffer(Base64.decode(certificate));
		X509Certificate x509Cert = (X509Certificate) cert;
		return new String(Hex.encode(x509Cert.getSerialNumber().toByteArray()));
	}

	public static String genP12(String certificate, PrivateKey pvtKey,
			String certChain, String pin) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		byte[] bCert = Base64.decode(certificate);
		ByteArrayInputStream certBIn = new ByteArrayInputStream(bCert);
		Certificate cert = cf.generateCertificate(certBIn);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ByteArrayInputStream bIn = new ByteArrayInputStream(
				Base64.decode(certChain));
		CertPath oCertPath = cf.generateCertPath(bIn, "PKCS7");
		List certs = oCertPath.getCertificates();
		Certificate[] chain = (Certificate[]) certs
				.toArray(new Certificate[certs.size() + 1]);
		chain[certs.size()] = cert;

		List certList = new ArrayList();
		for (Certificate c : chain) {
			certList.add(c);
		}
		Collections.reverse(certList);
		chain = (Certificate[]) certList.toArray(new Certificate[certList
				.size()]);
		ks.setKeyEntry("", pvtKey, pin.toCharArray(), chain);
		ByteArrayOutputStream outp12 = new ByteArrayOutputStream();
		ks.store(outp12, pin.toCharArray());
		return new String(Base64.encode(outp12.toByteArray()));
	}

	public static boolean verifyPin(String p12, String pin) {
		boolean flag = false;
		try {
			javasafeengine jse = new javasafeengine();
			ByteArrayInputStream bIn = new ByteArrayInputStream(
					Base64.decode(p12));
			KeyStore oKeyStore = jse.getKeyStore("PKCS12", "SUN", bIn, pin);
			Enumeration oEnum = jse.getAliasEnum(oKeyStore);
			String sAlias = "";
			while (oEnum.hasMoreElements()) {
				sAlias = (String) oEnum.nextElement();
			}
			PrivateKey oPrivateKey = (PrivateKey) jse.getPrivateKeyFromStore(
					oKeyStore, sAlias, pin);
			if (oPrivateKey != null) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception e) {
			Log.e(CommonConst.TAG, e.getMessage(),e);
			flag = false;
		}
		
		return flag;
	}
	
	public static int verifyCertificate(String certificate,String certchain) {
		   javasafeengine jse = new javasafeengine();
		   
		   Certificate cert = jse.getCertFromBuffer(Base64.decode(certificate));
		   X509Certificate x509Cert = (X509Certificate) cert;
		   
		   byte[] bChain = Base64.decode(certchain);
		   int iRtn = jse.verifyCert(x509Cert, bChain, 0);

		   return iRtn;
	 }
	   	   
	
	 public static int verifySM2Certificate(String certificate,String certchain) {
		   javasafeengine jse = new javasafeengine();
		   
		   int iRtn = jse.verifySM2Cert(certificate,certchain);
		   return iRtn;
	 }
	 
	 
	 /**
	  * 实例化私钥
	  * 
	  * @return
	  */
	 private static PrivateKey getPrivateKey(String priKey) {
	    PrivateKey privateKey = null;
	    //String priKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIN/+2cwN83idybSfOd/bx4/bqk+/d1B9PDrFEJSnN+OGT6R2PqCV08gbl1HfxkvJtZz8dmpj1sUa44AyKNpFPn8c51kNh/Hesd62s4ZQ9Yuter2docjg86AiC36h7oARDpIfcxver7lLY0terExr6MWroYpPK/JLtMjlw7WlhhnAgMBAAECgYBtNKn6BgcyCjLdoMxuDFyhiBhEobV+PDpIoVGPyr/QwFeiqMjDMd+ELPC6speQuKHOUze0haYyA4FWyrvVSX1K8Azmd3bfeD1qUoYZz45YBPdG0xxpwruLbLki9w5j9mpOHttK4TkhbCZ1fBi0/aeGscnZRiRMMuQeW8haGJI+MQJBAN0+ZWvd7aFzPg64kWwjqOCvDoQGZ+lxCUMhD6loVEZU9Rj15Ptb1boA8e7UPryCQr8CNV9JRJpn7PhwxmYKnN0CQQCYKGwc5RWLk6Vhjtek0X8fgHN4MHyzlLPysnGi2ySRWbYCz71ACsROEP0H2hjoggfcZdAA31xcVblJdNf4rQQTAkBor3eJ/K7OMBtzF74nrw/fkLWLnwRYHxZE30Xr4OPPi6+VqfQ5Q5DzvDK90UwGIpS2kLL0bVS6wuYMAEDAU9E5AkBO/AzDIxgAEHC1J9VBNY7r514JcgIJppOS1Jawl1lwKkWVSAlRiUVF0QPIY6qyZ211N1S2XzwuokFApGrIJ+gJAkAXTHdPlo/2xSNQfQYvlhHzvnoMr4YNoCzXKybf5ZkP9N+W8BuAQA1mO1iJIs8ruL5XtCa3FZA+LXNBeV0X4vnR";	  
	    PKCS8EncodedKeySpec priPKCS8;
	  
	    try {
	      priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(priKey));
	      KeyFactory keyf = KeyFactory.getInstance("RSA");
	      privateKey = keyf.generatePrivate(priPKCS8);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	  
	    return privateKey;
	 }
	 
	 
	 public static String getSign(byte[] data,String priKey)throws Exception {
			javasafeengine jse = new javasafeengine();
			PrivateKey privateKey = getPrivateKey(priKey);
			byte[] sign = null;
			sign = jse.sign(data, privateKey, CommonConst.UM_APP_SIGN_ALG, signProvider);
			return new String(Base64.encode(sign));
	 }
	 
	 public static String getDigest(byte[] data)throws Exception {
		   javasafeengine jse = new javasafeengine();
		   byte[] bDigest = jse.digest(data, digestAlg, provider);

		   return new String(Base64.encode(bDigest));
	 }
	 
	 public static String getSHADigest(String orgData,String digAlg,String strProvider)throws Exception {
		   String strSHAHash = "";
		 
		   javasafeengine jse = new javasafeengine();
		   byte[] bText = orgData.getBytes();   
		   byte[] bDigest = jse.digest(bText, digAlg, strProvider);
		   strSHAHash = new String(Base64.encode(bDigest));
		   //strSHAHash = new String(bDigest);
		   return strSHAHash;
	 }
	 
	 public static byte[] str2cbcd(String s) {
		  if (s.length() % 2 != 0) {
		     s = "0" + s;
		  }
		  
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  char[] cs = s.toCharArray();
		  for (int i = 0; i < cs.length; i += 2) {
		      int high = cs[i] - 48;
		      int low = cs[i + 1] - 48;
		      baos.write(high << 4 | low);
		  }
		  
		  return baos.toByteArray(); 
	 }
	 
}
