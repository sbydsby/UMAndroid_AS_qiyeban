package com.sheca.zhongmei.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class SealInfo {
	private int id;
	private int sdkID;
	private String vid;
	private String sealname;	
	private String sealsn;
	private String issuercert;
	private String cert;
	private String picdata;
	private String pictype;
	private String picwidth;
	private String picheight;
	private String notbefore;
	private String notafter;
	private String signal;
	private String extensions;
	private String accountname;
	private String certsn;
	private int state;
	private int downloadstatus;

	public int getSdkID() {
		return sdkID;
	}

	public void setSdkID(int sdkID) {
		this.sdkID = sdkID;
	}
	
	public String getCertsn() {
		return certsn;
	}
	public void setCertsn(String certsn) {
		this.certsn = certsn;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getVid() {
		return vid;
	}
	public void setVid(String vid) {
		this.vid = vid;
	}
	public String getSealname() {
		return sealname;
	}
	public void setSealname(String sealname) {
		this.sealname = sealname;
	}
	public String getSealsn() {
		return sealsn;
	}
	public void setSealsn(String sealsn) {
		this.sealsn = sealsn;
	}
	public String getIssuercert() {
		return issuercert;
	}
	public void setIssuercert(String issuercert) {
		this.issuercert = issuercert;
	}
	public String getCert() {
		return cert;
	}
	public void setCert(String cert) {
		this.cert = cert;
	}
	public String getPicdata() {
		return picdata;
	}
	public void setPicdata(String picdata) {
		this.picdata = picdata;
	}
	public String getPictype() {
		return pictype;
	}
	public void setPictype(String pictype) {
		this.pictype = pictype;
	}
	public String getPicwidth() {
		return picwidth;
	}
	public void setPicwidth(String picwidth) {
		this.picwidth = picwidth;
	}
	public String getPicheight() {
		return picheight;
	}
	public void setPicheight(String picheight) {
		this.picheight = picheight;
	}
	public String getNotbefore() {
		return notbefore;
	}
	public void setNotbefore(String notbefore) {
		this.notbefore = notbefore;
	}
	public String getNotafter() {
		return notafter;
	}
	public void setNotafter(String notafter) {
		this.notafter = notafter;
	}
	public String getSignal() {
		return signal;
	}
	public void setSignal(String signal) {
		this.signal = signal;
	}
	public String getExtensions() {
		return extensions;
	}
	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}
	public String getAccountname() {
		return accountname;
	}
	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}

	public int getDownloadstatus() {
		return downloadstatus;
	}
	public void setDownloadstatus(int downloadstatus) {
		this.downloadstatus = downloadstatus;
	}

	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

}
