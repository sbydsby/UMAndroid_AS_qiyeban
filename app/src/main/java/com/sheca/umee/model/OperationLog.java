package com.sheca.umee.model;

public class OperationLog {
	public static int LOG_TYPE_SIGN = 1;

	public static int LOG_TYPE_LOGIN = 2;

	public static int LOG_TYPE_APPLYCERT = 3;
	
	public static int LOG_TYPE_DAO_SIGN = 4;
	
	public static int LOG_TYPE_DAO_LOGIN = 5;
	
	public static int LOG_TYPE_DAO_LOGIN_INTERNET = 6;  //登录上网状态，已不使用
	
	public static int LOG_TYPE_INPUTCERT = 7;
	
	public static int LOG_TYPE_RENEWCERT = 8;
	
	public static int LOG_TYPE_REVOKECERT = 9;
	
    public static int LOG_TYPE_DAO_SIGNEX = 10;
	
	public static int LOG_TYPE_DAO_ENVELOP_DECRYPT = 11;
	
	public static int LOG_TYPE_APPLYSEAL = 12;

	private int id;
	private String certsn;
	private int type;
	private String sign;
	private String message;
	private String createtime;
	private String accountname;
	private String invoker;
	private int signalg;
	private int isupload;
	private String invokerid;
	
	public String getInvoker() {
		return invoker;
	}

	public void setInvoker(String invoker) {
		this.invoker = invoker;
	}

	public int getSignalg() {
		return signalg;
	}

	public void setSignalg(int signalg) {
		this.signalg = signalg;
	}

	public int getIsupload() {
		return isupload;
	}

	public void setIsupload(int isupload) {
		this.isupload = isupload;
	}

	
	public String getAccountname() {
		return accountname;
	}

	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCertsn() {
		return certsn;
	}

	public void setCertsn(String certsn) {
		this.certsn = certsn;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	
	public String getInvokerid() {
		return invokerid;
	}

	public void setInvokerid(String invokerid) {
		this.invokerid = invokerid;
	}

}
