package com.sheca.umandroid.model;

import java.util.List;

//import org.codehaus.jackson.annotate.JsonProperty;

public class GetAppInfoListResponse {
	//@JsonProperty(value = "Result") 
	private String Result;
	//@JsonProperty(value = "Return") 
	private String Return;
	//@JsonProperty(value = "AppInfoList") 
	private List<AppInfo> AppInfoList;

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

	public List<AppInfo> getAppInfoList() {
		return AppInfoList;
	}

	public void setAppInfoList(List<AppInfo> applications) {
		AppInfoList = applications;
	}
}
