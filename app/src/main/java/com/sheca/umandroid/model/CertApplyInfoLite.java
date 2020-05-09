package com.sheca.umandroid.model;

//import org.codehaus.jackson.annotate.JsonProperty;


public class CertApplyInfoLite {
	private String RequestNumber;
	private String CommonName;	
	private String ApplyTime;
//	private int ApplyStatus;
private int status;
	private String BizSN;
	private String CertType;
	private int SignAlg;
	private int PayStatus;

	public int getPayStatus() {
		return PayStatus;
	}

	public void setPayStatus(int payStatus) {
		PayStatus = payStatus;
	}

	public String getBizSN() {
		return BizSN;
	}

	public void setBizSN(String bizSN) {
		BizSN = bizSN;
	}

	public int getApplyStatus() {
		return status;
	}

	public void setApplyStatus(int applyStatus) {
		status = applyStatus;
	}

	public String getRequestNumber() {
		return RequestNumber;
	}

	public void setRequestNumber(String requestNumber) {
		this.RequestNumber = requestNumber;
	}
	
	public String getCommonName() {
		return CommonName;
	}

	public void setCommonName(String commonName) {
		this.CommonName = commonName;
	}

	public String getApplyTime() {
		return ApplyTime;
	}

	public void setApplyTime(String applyTime) {
		this.ApplyTime = applyTime;
	}
	
	public String getCertType() {
		return CertType;
	}

	public void setCertType(String certType) {
		CertType = certType;
	}

	public int getSignAlg() {
		return SignAlg;
	}

	public void setSignAlg(int signAlg) {
		SignAlg = signAlg;
	}
	
}
