package com.sheca.umandroid.model;

public class Cert {
    public static int STATUS_GEN_PRIVATEKEY = 0;
    public static int STATUS_UPLOAD_PKCS10 = 1;
    public static int STATUS_DOWNLOAD_CERT = 2;
    public static int STATUS_RENEW_CERT = 3;
    public static int STATUS_REVOKE_CERT = 4;

    public static int STATUS_UPLOAD_CERT = 1;
    public static int STATUS_UNUPLOAD_CERT = 0;

    public static int STATUS_IS_SEAL = 1;
    public static int STATUS_NO_SEAL = 0;
    public static int STATUS_RETURN = 5;//被否决
    private int id;
    private int sdkID;
    private String certsn;
    private String envsn;
    private String privatekey;
    private String certificate;
    private String keystore;
    private String enccertificate;
    private String enckeystore;
    private String certchain;
    private int status;
    private String accountname;
    private String notbeforetime;
    private String validtime;
    private int uploadstatus;
    private String certtype;
    private int signalg;
    private String containerid;
    private int algtype;
    private int savetype;
    private String devicesn;
    private String certname;
    private String certhash;
    private int fingertype;
    private String sealsn;
    private int sealstate;
    private int certlevel;

    public void setSdkID(int sdkID) {
        this.sdkID = sdkID;
    }

    public int getSdkID() {
        return sdkID;
    }

    public String getSealsn() {
        return sealsn;
    }

    public void setSealsn(String sealsn) {
        this.sealsn = sealsn;
    }

    public int getSealstate() {
        return sealstate;
    }

    public void setSealstate(int sealstate) {
        this.sealstate = sealstate;
    }


    public int getUploadstatus() {
        return uploadstatus;
    }

    public void setUploadstatus(int uploadstatus) {
        this.uploadstatus = uploadstatus;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getEnckeystore() {
        return enckeystore;
    }

    public void setEnckeystore(String enckeystore) {
        this.enckeystore = enckeystore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnvsn() {
        return envsn;
    }

    public void setEnvsn(String envsn) {
        this.envsn = envsn;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getCertchain() {
        return certchain;
    }

    public void setCertchain(String certchain) {
        this.certchain = certchain;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getEnccertificate() {
        return enccertificate;
    }

    public void setEnccertificate(String enccertificate) {
        this.enccertificate = enccertificate;
    }

    public String getCertsn() {
        return certsn;
    }

    public void setCertsn(String certsn) {
        this.certsn = certsn;
    }

    public String getNotbeforetime() {
        return notbeforetime;
    }

    public void setNotbeforetime(String notbeforetime) {
        this.notbeforetime = notbeforetime;
    }

    public String getValidtime() {
        return validtime;
    }

    public void setValidtime(String validtime) {
        this.validtime = validtime;
    }

    public String getCerttype() {
        return certtype;
    }

    public void setCerttype(String certtype) {
        this.certtype = certtype;
    }

    public int getSignalg() {
        return signalg;
    }

    public void setSignalg(int signalg) {
        this.signalg = signalg;
    }

    public String getContainerid() {
        return containerid;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }

    public int getAlgtype() {
        return algtype;
    }

    public void setAlgtype(int algtype) {
        this.algtype = algtype;
    }

    public int getSavetype() {
        return savetype;
    }

    public void setSavetype(int savetype) {
        this.savetype = savetype;
    }

    public String getDevicesn() {
        return devicesn;
    }

    public void setDevicesn(String devicesn) {
        this.devicesn = devicesn;
    }

    public String getCertname() {
        return certname;
    }

    public void setCertname(String certname) {
        this.certname = certname;
    }

    public String getCerthash() {
        return certhash;
    }

    public void setCerthash(String certhash) {
        this.certhash = certhash;
    }


    public int getFingertype() {
        return fingertype;
    }

    public void setFingertype(int fingertype) {
        this.fingertype = fingertype;
    }

    public int getCertlevel() {
        return certlevel;
    }

    public void setCertlevel(int certlevel) {
        this.certlevel = certlevel;
    }

}

