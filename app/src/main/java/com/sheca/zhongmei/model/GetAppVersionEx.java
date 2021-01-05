package com.sheca.zhongmei.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class GetAppVersionEx {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "Version")
	private String Version;
	//@JsonProperty(value = "DownloadURL")
	private String DownloadURL;
	//@JsonProperty(value = "Description")
	private String Description;
	//@JsonProperty(value = "Compulsion")
	private String Compulsion;
	
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

	public String getVersion() {
		return Version;
	}

	public void setVersion(String version) {
		Version = version;
	}

	public String getDownloadURL() {
		return DownloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		DownloadURL = downloadURL;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}
	
	public String getCompulsion() {
		return Compulsion;
	}

	public void setCompulsion(String compulsion) {
		Compulsion = compulsion;
	}
	

}
