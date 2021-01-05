package com.sheca.zhongmei.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class FaceRecognitionResponse {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "BizSN")
	private String BizSN;
	//@JsonProperty(value = "PersonName")
	private String PersonName;
	//@JsonProperty(value = "PersonID")
	private String PersonID;
	//@JsonProperty(value = "IDPhoto")
	private String IDPhoto;
	//@JsonProperty(value = "SignatureAlgorithm")
	private String SignatureAlgorithm;
	//@JsonProperty(value = "SignatureValue")
	private String SignatureValue;
	
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
	public String getBizSN() {
		return BizSN;
	}
	public void setBizSN(String bizSN) {
		BizSN = bizSN;
	}
	public String getPersonName() {
		return PersonName;
	}
	public void setPersonName(String personName) {
		PersonName = personName;
	}
	public String getPersonID() {
		return PersonID;
	}
	public void setPersonID(String personID) {
		PersonID = personID;
	}
	public String getIDPhoto() {
		return IDPhoto;
	}
	public void setIDPhoto(String iDPhoto) {
		IDPhoto = iDPhoto;
	}
	public String getSignatureAlgorithm() {
		return SignatureAlgorithm;
	}
	public void setSignatureAlgorithm(String signatureAlgorithm) {
		SignatureAlgorithm = signatureAlgorithm;
	}
	public String getSignatureValue() {
		return SignatureValue;
	}
	public void setSignatureValue(String signatureValue) {
		SignatureValue = signatureValue;
	}
}
