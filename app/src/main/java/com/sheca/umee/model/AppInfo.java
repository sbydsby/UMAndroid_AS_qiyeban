package com.sheca.umee.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class AppInfo {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "AppID")
	private String AppID;
	//@JsonProperty(value = "Name")
	private String Name;
	//@JsonProperty(value = "Description")
	private String Description;
	//@JsonProperty(value = "ContactPerson")
	private String ContactPerson;
	//@JsonProperty(value = "ContactPhone")
	private String ContactPhone;
	//@JsonProperty(value = "ContactEmail")
	private String ContactEmail;
	//@JsonProperty(value = "AssignTime")
	private String AssignTime;
	//@JsonProperty(value = "Visibility")
	private int    Visibility;
	
	
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
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getContactPerson() {
		return ContactPerson;
	}
	public void setContactPerson(String contactPerson) {
		ContactPerson = contactPerson;
	}
	public String getContactPhone() {
		return ContactPhone;
	}
	public void setContactPhone(String contactPhone) {
		ContactPhone = contactPhone;
	}
	public String getContactEmail() {
		return ContactEmail;
	}
	public void setContactEmail(String contactEmail) {
		ContactEmail = contactEmail;
	}
	public String getAssignTime() {
		return AssignTime;
	}
	public void setAssignTime(String assignTime) {
		AssignTime = assignTime;
	}
	public int getVisibility() {
		return Visibility;
	}
	public void setVisibility(int visibility) {
		Visibility = visibility;
	}
}
