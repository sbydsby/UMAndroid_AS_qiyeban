package com.sheca.umandroid.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class DownloadCertResponse {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "UserCert")
	private String UserCert;
	//@JsonProperty(value = "EncCert")
	private String EncCert;
	//@JsonProperty(value = "EncKey")
	private String EncKey;
	//@JsonProperty(value = "CertChain")
	private String CertChain;
	//@JsonProperty(value = "EncAlgorithm")
	private String EncAlgorithm;

	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	public String getReturn() {
		return Return;
	}

	public void setReturn(String return1) {
		Return = return1;
	}

	public String getUserCert() {
		return UserCert;
	}

	public void setUserCert(String userCert) {
		UserCert = userCert;
	}

	public String getEncCert() {
		return EncCert;
	}

	public void setEncCert(String encCert) {
		EncCert = encCert;
	}

	public String getEncKey() {
		return EncKey;
	}

	public void setEncKey(String encKey) {
		EncKey = encKey;
	}

	public String getCertChain() {
		return CertChain;
	}

	public void setCertChain(String certChain) {
		CertChain = certChain;
	}

	public String getEncAlgorithm() {
		return EncAlgorithm;
	}

	public void setEncAlgorithm(String encAlgorithm) {
		EncAlgorithm = encAlgorithm;
	}

}
