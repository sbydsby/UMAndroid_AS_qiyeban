package com.sheca.zhongmei.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class GeneralResponse {
	//@JsonProperty(value = "Result") 
	private String Result;
	//@JsonProperty(value = "Return") 
	private String Return;

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

}
