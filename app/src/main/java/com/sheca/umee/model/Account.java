package com.sheca.umee.model;

public class Account {
	private int id;
	private String name;
	private String password;
	private int    status;
	private int    active;
	private String email;
	private String mobile;
	private int    type;
	private int    isValid;
	private int    source;
	private String registerTime;
	private int    notifyMode;
	private String countryName;
	private String provinceName;
	private String localityName;
	private String orgName;
	private String orgUnitName;
	private String postalAddress;
	private String postalCode;
	private String weChatNo;
	private int    identityType;
	private String identityCode;
	private String identityName;
	private String copyIDPhoto;
	private String appIDInfo;
	private int    saveType;
	private int    certType;
	private int    loginType;

	public Account() { }
	
	public Account(String name, String password, int status,int active,String identityName,String identityCode,String copyIDPhoto, int accounttype,String appIDInfo,String orgName,int saveType,int certType,int loginType) {
		this.name = name;
		this.password = password;
		this.status = status;
		this.active = active;
		this.identityName = identityName;
		this.identityCode = identityCode;
		this.copyIDPhoto = copyIDPhoto;
		this.type = accounttype;
		this.appIDInfo = appIDInfo;
		this.orgName = orgName;
		this.saveType = saveType;
		this.certType = certType;
		this.loginType = loginType;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAppIDInfo() {
		return appIDInfo;
	}

	public void setAppIDInfo(String appIDInfo) {
		this.appIDInfo = appIDInfo;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getActive() {
		return 1;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getIsValid() {
		return isValid;
	}

	public void setIsValid(int isValid) {
		this.isValid = isValid;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public String getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}

	public int getNotifyMode() {
		return notifyMode;
	}

	public void setNotifyMode(int notifyMode) {
		this.notifyMode = notifyMode;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getLocalityName() {
		return localityName;
	}

	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgUnitName() {
		return orgUnitName;
	}

	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getWeChatNo() {
		return weChatNo;
	}

	public void setWeChatNo(String weChatNo) {
		this.weChatNo = weChatNo;
	}

	public int getIdentityType() {
		return identityType;
	}

	public void setIdentityType(int identityType) {
		this.identityType = identityType;
	}

	public String getIdentityCode() {
		return identityCode;
	}

	public void setIdentityCode(String identityCode) {
		this.identityCode = identityCode;
	}

	public String getIdentityName() {
		return identityName;
	}

	public void setIdentityName(String identityName) {
		this.identityName = identityName;
	}

	public String getCopyIDPhoto() {
		return copyIDPhoto;
	}

	public void setCopyIDPhoto(String copyIDPhoto) {
		this.copyIDPhoto = copyIDPhoto;
	}
	
	public int getSaveType() {
		return saveType;
	}

	public void setSaveType(int saveType) {
		this.saveType = saveType;
	}

	public int getCertType() {
		return certType;
	}

	public void setCertType(int certType) {
		this.certType = certType;
	}
	
	public int getLoginType() {
		return loginType;
	}

	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}


}
