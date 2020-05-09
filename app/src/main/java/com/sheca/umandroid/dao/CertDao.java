package com.sheca.umandroid.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.sheca.umandroid.model.Cert;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommUtil;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umplus.util.PKIUtil;

import java.util.ArrayList;
import java.util.List;

public class CertDao {
	private DBHelper db;

	public CertDao(Context context) {
		db = new DBHelper(context);
	}

	public int addCert(Cert cert,String accountName) {
		int id = 0;
		ContentValues values = new ContentValues();
		values.put("certsn", cert.getCertsn());
		values.put("sdkcertid", cert.getSdkID());
		values.put("envsn", cert.getEnvsn());
		values.put("keystore", cert.getKeystore());
		values.put("privatekey", cert.getPrivatekey());
		values.put("certificate", cert.getCertificate());
		values.put("certchain", cert.getCertchain());
		values.put("status", cert.getStatus());
		values.put("enccertificate", cert.getEnccertificate());
		values.put("enckeystore", cert.getEnckeystore());
		values.put("accountname", accountName);
		values.put("notbeforetime",cert.getNotbeforetime());
		values.put("validtime",cert.getValidtime());
		values.put("uploadstatus",cert.getUploadstatus());
	    values.put("certtype",cert.getCerttype());
		values.put("signalg",cert.getSignalg());
		values.put("containerid",cert.getContainerid());
		values.put("algtype",cert.getAlgtype());
		values.put("savetype",cert.getSavetype());
		values.put("devicesn",cert.getDevicesn());
		values.put("certname",cert.getCertname());
		values.put("certhash",cert.getCerthash());
		values.put("fingertype",cert.getFingertype());
		values.put("sealsn",cert.getSealsn());
		values.put("sealstate",cert.getSealstate());
		values.put("certlevel",cert.getCertlevel());
		
		id = (int) db.insert(DBHelper.TBL_CERT, values);
		db.close();
		return id;
	}

	public Cert getCertByID(int id) {
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "id=" + id);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
		}
		result.close();
		db.close();
		return cert;
	}

	public Cert getCertByEnvsn(String envsn,String accountName) {
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "envsn='" + envsn + "' and accountname='"+accountName+"'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
		}
		result.close();
		db.close();
		return cert;
	}


		//根据Sm2证书idcert取出配套的rsa证书
		public Cert getOtherCert(int certId,Context context) {
			Cert sm2Cert = getCertByID(certId);
			Cert rsaCert = null;
			List<Cert> rsaList = getAllCerts(AccountHelper.getUsername(context));
			for (int i = 0; i < rsaList.size(); i++) {
				if (CommUtil.getCertDetail(rsaList.get(i), 17).equals(CommUtil.getCertDetail(sm2Cert, 17))) {
					rsaCert = rsaList.get(i);
					return rsaCert;
				}
			}
			return null;
		}
	
	public Cert getCertByCertsn(String certsn,String accountName) {
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "certsn='" + certsn.toLowerCase() + "' and accountname='"+accountName+"'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
		}
		result.close();
		db.close();
		return cert;
	}

	public Cert getCertByDevicesn(String devicesn,String accountName,String certType) {
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "devicesn='" + devicesn + "' and accountname='"+accountName+"' and certtype='"+certType+"'");
		result.moveToFirst();
		if (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
		}
		result.close();
		db.close();
		return cert;
	}
	
	public List<Cert> getAllCerts(String accountName) {
		List<Cert> certList = new ArrayList<Cert>();
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "accountname = '"+accountName+"' and status =" + Cert.STATUS_DOWNLOAD_CERT);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
			certList.add(cert);
			result.moveToNext();
		}
		result.close();
		db.close();
		return certList;
	}

	public List<Cert> getAllCertsNoSeal(String accountName) {
		List<Cert> certList = new ArrayList<Cert>();
		Cert cert = null;
		Cursor result = db.query(DBHelper.TBL_CERT, "accountname = '"+accountName+"' and status =" + Cert.STATUS_DOWNLOAD_CERT+" and sealstate =" + Cert.STATUS_NO_SEAL);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			cert = new Cert();
			cert.setId(result.getInt(0));
			cert.setCertsn(result.getString(1));
			cert.setEnvsn(result.getString(2));
			cert.setPrivatekey(result.getString(3));
			cert.setCertificate(result.getString(4));
			cert.setKeystore(result.getString(5));
			cert.setEnccertificate(result.getString(6));
			cert.setEnckeystore(result.getString(7));
			cert.setCertchain(result.getString(8));
			cert.setStatus(result.getInt(9));
			cert.setAccountname(result.getString(10));
			cert.setNotbeforetime(result.getString(11));
			cert.setValidtime(result.getString(12));
			cert.setUploadstatus(result.getInt(13));
			cert.setCerttype(result.getString(14));
			cert.setSignalg(result.getInt(15));
			cert.setContainerid(result.getString(16));
			cert.setAlgtype(result.getInt(17));
			cert.setSavetype(result.getInt(18));
			cert.setDevicesn(result.getString(19));
			cert.setCertname(result.getString(20));
			cert.setCerthash(result.getString(21));
			cert.setFingertype(result.getInt(22));
			cert.setSealsn(result.getString(23));
			cert.setSealstate(result.getInt(24));
			cert.setSdkID(result.getInt(25));
            cert.setCertlevel(result.getInt(26));
			certList.add(cert);
			result.moveToNext();
		}
		result.close();
		db.close();
		return certList;
	}


	//查询所有的证书
	public List<com.sheca.umplus.model.Cert> getAllCert(){
		List<com.sheca.umplus.model.Cert> certList = new ArrayList<com.sheca.umplus.model.Cert>();
		com.sheca.umplus.model.Cert mCert=null;
		Cursor result = db.query(DBHelper.TBL_CERT, "");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			mCert = new com.sheca.umplus.model.Cert();
//			mCert.setId(result.getInt(0));
			mCert.setCertsn(result.getString(1));
			mCert.setEnvsn(result.getString(2));
			mCert.setPrivatekey(result.getString(3));
			mCert.setCertificate(result.getString(4));
			mCert.setKeystore(result.getString(5));
			mCert.setEnccertificate(result.getString(6));
			mCert.setEnckeystore(result.getString(7));
			mCert.setCertchain(result.getString(8));
			mCert.setStatus(result.getInt(9));
			mCert.setAccountname(result.getString(10));
			mCert.setNotbeforetime(result.getString(11));
			mCert.setValidtime(result.getString(12));
			mCert.setUploadstatus(result.getInt(13));
			mCert.setCerttype(result.getString(14));
			mCert.setSignalg(result.getInt(15));
			mCert.setContainerid(result.getString(16));
			mCert.setAlgtype(result.getInt(17));
			mCert.setSavetype(result.getInt(18));
			mCert.setDevicesn(result.getString(19));
			mCert.setCertname(result.getString(20));
			mCert.setCerthash(result.getString(21));
			mCert.setFingertype(result.getInt(22));
			mCert.setSealsn(result.getString(23));
			mCert.setSealstate(result.getInt(24));
			mCert.setSdkid(result.getInt(25));
            mCert.setCertlevel(result.getInt(26));

			certList.add(mCert);
			result.moveToNext();
		}
		result.close();
		db.close();
		return certList;
	}





	//查询所有的证书
	public List<Cert> getAllCertLocal(){
		List<Cert> certList = new ArrayList<Cert>();
		Cert mCert=null;
		Cursor result = db.query(DBHelper.TBL_CERT, "");
		result.moveToFirst();
		while (!result.isAfterLast()) {
			mCert = new Cert();
//			mCert.setId(result.getInt(0));
			mCert.setCertsn(result.getString(1));
			mCert.setEnvsn(result.getString(2));
			mCert.setPrivatekey(result.getString(3));
			mCert.setCertificate(result.getString(4));
			mCert.setKeystore(result.getString(5));
			mCert.setEnccertificate(result.getString(6));
			mCert.setEnckeystore(result.getString(7));
			mCert.setCertchain(result.getString(8));
			mCert.setStatus(result.getInt(9));
			mCert.setAccountname(result.getString(10));
			mCert.setNotbeforetime(result.getString(11));
			mCert.setValidtime(result.getString(12));
			mCert.setUploadstatus(result.getInt(13));
			mCert.setCerttype(result.getString(14));
			mCert.setSignalg(result.getInt(15));
			mCert.setContainerid(result.getString(16));
			mCert.setAlgtype(result.getInt(17));
			mCert.setSavetype(result.getInt(18));
			mCert.setDevicesn(result.getString(19));
			mCert.setCertname(result.getString(20));
			mCert.setCerthash(result.getString(21));
			mCert.setFingertype(result.getInt(22));
			mCert.setSealsn(result.getString(23));
			mCert.setSealstate(result.getInt(24));
			mCert.setSdkID(result.getInt(25));
			mCert.setCertlevel(result.getInt(26));

			certList.add(mCert);
			result.moveToNext();
		}
		result.close();
		db.close();
		return certList;
	}



	public void updateCert(Cert cert,String accountName) {
		ContentValues values = new ContentValues();
		values.put("certsn", cert.getCertsn());
		values.put("envsn", cert.getEnvsn());
		values.put("keystore", cert.getKeystore());
		values.put("privatekey", cert.getPrivatekey());
		values.put("certificate", cert.getCertificate());
		values.put("certchain", cert.getCertchain());
		values.put("status", cert.getStatus());
		values.put("enccertificate", cert.getEnccertificate());
		values.put("enckeystore", cert.getEnckeystore());
		values.put("accountname", accountName);
		values.put("notbeforetime",cert.getNotbeforetime());
		values.put("validtime",cert.getValidtime());
		values.put("uploadstatus",cert.getUploadstatus());
		values.put("certtype",cert.getCerttype());
		values.put("signalg",cert.getSignalg());
		values.put("containerid",cert.getContainerid());
		values.put("algtype",cert.getAlgtype());
		values.put("savetype",cert.getSavetype());
		values.put("devicesn",cert.getDevicesn());
		values.put("certname",cert.getCertname());
		values.put("certhash",cert.getCerthash());
		values.put("fingertype",cert.getFingertype());
		values.put("sealsn",cert.getSealsn());
		values.put("sealstate",cert.getSealstate());
		values.put("sdkcertid",cert.getSdkID());
        values.put("certlevel",cert.getCertlevel());
		
		db.update(DBHelper.TBL_CERT, values, "id=" + cert.getId());
		db.close();
	}

	public void deleteCert(int id) {
		db.delete(DBHelper.TBL_CERT, "id=" + id);
		db.close();
	}

	public void deleteAllCert(){
        db.delete(DBHelper.TBL_CERT, "" );
        db.close();
    }


	//验证过期
	public int verifyCert(final Cert cert) {
/*
        if (CommonConst.CERT_TYPE_RSA.equals(cert.getCerttype())) {
            int i = -1;
            if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
                i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
            else
                i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
            if (i == CommonConst.RET_VERIFY_CERT_OK) {
                return 0;
            }else if(i == 0){
			if(bShow)
			  Toast.makeText(DaoActivity.this, "证书过期", Toast.LENGTH_LONG).show();
		  } else {
                return -1;
            }
        } else if (CommonConst.CERT_TYPE_SM2.equals(cert.getCerttype())) {
            String strSignCert = "";

            int i = -1;
            try {
                strSignCert = cert.getCertificate();
                if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
                    i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
                else
                    i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());
                //Toast.makeText(DaoActivity.this, "verifySM2Cert:"+i, Toast.LENGTH_LONG).show();
                // LaunchActivity.logUtil.recordLogServiceLog("Cert="+strSignCert+"\nCertchain="+cert.getCertchain()+"\nresult="+i);
                //Toast.makeText(DaoActivity.this,"证书链:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
                // Toast.makeText(DaoActivity.this,"verifySM2Cert:"+i, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i == 0) {
                //Toast.makeText(DaoActivity.this, "验证证书通过", Toast.LENGTH_LONG).show();
                return 0;
            } else if (i == 1) {
                return 1;
            } else {
                return -1;
            }
        } */
		if (!cert.getCerttype().contains("SM2")) {
			int i = -1;
			try {
				if (CommonConst.INPUT_RSA_SIGN.equals(cert.getEnvsn()))
					i = PKIUtil.verifyCertificate(cert.getCertificate(), CommonConst.RSA_CERT_CHAIN);
				else
					i = PKIUtil.verifyCertificate(cert.getCertificate(), cert.getCertchain());
			}catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
			// Toast.makeText(DaoActivity.this, "verifyCert:"+i+"\nCert:"+cert.getCertificate()+"\nCertchain:"+cert.getCertchain(), Toast.LENGTH_LONG).show();
//            Log.d("DaoActivity", "verifyCert-Cert:" + cert.getCertificate() + " Certchain:" + cert.getCertchain());
			if (i == CommonConst.RET_VERIFY_CERT_OK) {
				return 0;
			} else {
				return -1;
			}
		} else if (cert.getCerttype().contains("SM2")) {
			String strSignCert = "";

			int i = -1;
			try {
				strSignCert = cert.getCertificate();
				if (CommonConst.INPUT_SM2_SIGN.equals(cert.getEnvsn()))
					i = PKIUtil.verifySM2Certificate(cert.getCertificate(), CommonConst.SM2_CERT_CHAIN);
				else
					i = PKIUtil.verifySM2Certificate(strSignCert, cert.getCertchain());

			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}

			if (i == 0) {
				return 0;
			} else if (i == 1) {
				return 1;
			} else {
				return -1;
			}

		}

		return -1;
	}
}
