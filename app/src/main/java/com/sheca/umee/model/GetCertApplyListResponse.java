package com.sheca.umee.model;

import java.util.List;

//import org.codehaus.jackson.annotate.JsonProperty;

public class GetCertApplyListResponse {
	//@JsonProperty(value = "Result") 
	private String Result;
	//@JsonProperty(value = "Return") 
	private String Return;
	//@JsonProperty(value = "CertApplyInfoList") 
	private List<CertApplyInfoLite> CertApplyInfoList;

	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	public String getReturn() {
		return Return;
	}

	public void setReturn(String rtn) {
		Return = rtn;
	}

	public List<CertApplyInfoLite> getCertApplyInfoList() {
		return CertApplyInfoList;
	}

	public void setCertApplyInfoList(List<CertApplyInfoLite> applications) {
		CertApplyInfoList = applications;
	}
}
