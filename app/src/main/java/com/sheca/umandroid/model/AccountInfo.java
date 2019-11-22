package com.sheca.umandroid.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class AccountInfo {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "AppID")
	private String AppID;
	//@JsonProperty(value = "AppName")
	private String AppName;	
	//@JsonProperty(value = "AccountUID")
	private String AccountUID;
	//@JsonProperty(value = "AccountType")
	private String AccountType;
	

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
	public String getAppID() {
		return AppID;
	}
	public void setAppID(String appID) {
		AppID = appID;
	}
	
	public String getAppName() {
		return AppName;
	}
	public void setAppName(String appName) {
		AppName = appName;
	}
	public String getAccountUID() {
		return AccountUID;
	}
	public void setAccountUID(String accountUID) {
		AccountUID = accountUID;
	}
	public String getAccountType() {
		return AccountType;
	}
	public void setAccountType(String accountType) {
		AccountType = accountType;
	}
}
