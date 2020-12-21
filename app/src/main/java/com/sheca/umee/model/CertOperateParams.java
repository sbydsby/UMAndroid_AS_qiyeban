package com.sheca.umee.model;

/**
 * @author xuchangqing
 * @time 2019/4/19 18:23
 * @descript 证书操作类，证书的基本信息
 */
public class CertOperateParams {
     //id
      private String mCertID;

      private String mCert;
      private String itemNo;

      private String oid;

      private String message;
      private String msgWrapper;
      private String signAlg;
      private String signature;
      private String certID;

      private String encryptData;
      private String certPwd;

      private String accountName;

     public String getAccountName() {
      return accountName;
     }

     public void setAccountName(String accountName) {
      this.accountName = accountName;
     }


 //根据证书ID获取证书
      public CertOperateParams(String mCertID){
        this.mCertID=mCertID;
      }
      public CertOperateParams(){

      }

      public String getCertID() {
       return mCertID;
      }

      public void setCertID(String certID) {
       mCertID = certID;
      }

      public String getEncryptData() {
       return encryptData;
      }

      public void setEncryptData(String encryptData) {
       this.encryptData = encryptData;
      }

      public String getCertPwd() {
       return certPwd;
      }

      public void setCertPwd(String certPwd) {
       this.certPwd = certPwd;
      }

      public String getCert() {
       return mCert;
      }

      public void setCert(String cert) {
       mCert = cert;
      }

      public String getItemNo() {
       return itemNo;
      }

      public void setItemNo(String itemNo) {
       this.itemNo = itemNo;
      }

      public String getOid() {
       return oid;
      }

      public void setOid(String oid) {
       this.oid = oid;
      }

      public String getMessage() {
       return message;
      }

      public void setMessage(String message) {
       this.message = message;
      }

      public String getMsgWrapper() {
       return msgWrapper;
      }

      public void setMsgWrapper(String msgWrapper) {
       this.msgWrapper = msgWrapper;
      }

      public String getSignAlg() {
       return signAlg;
      }

      public void setSignAlg(String signAlg) {
       this.signAlg = signAlg;
      }

      public String getSignature() {
       return signature;
      }

      public void setSignature(String signature) {
       this.signature = signature;
      }
}
