package com.sheca.zhongmei.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.zhongmei.R;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CommonConst;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


@SuppressLint("ValidFragment")
public class CertSignFragment extends Fragment {
	private CertDao certDao = null;	
	private javasafeengine jse = null;
	
	private int certID = 0;	
	private Cert mCert = null;
	private View view = null;
	private Context context = null;
	private Activity activity = null;
	private TextView tvUserName;

	public CertSignFragment( int certID){
		this.certID = certID;	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {		    
		    activity = getActivity(); 
			view = inflater.inflate(R.layout.certdetail, container, false);
			context = view.getContext();

			jse = new javasafeengine();
			certDao = new CertDao(context);

			mCert = certDao.getCertByID(certID);

			tvUserName = (TextView)view.findViewById(R.id.usernamevalue);
//			if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype()))
//				viewSM2SignCert();
//			else
//				viewSignCert();

		if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2"))
			viewSM2SignCert();
		else
			viewSignCert();

		    return view;
	}
	
	
	private void viewSignCert(){	
		Cert cert = mCert;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);
		// sdf2.setTimeZone(tzChina);
		if (cert != null) {
			String certificate = cert.getCertificate();
			byte[] bCert = Base64.decode(certificate);
			// byte[] bEncCert = Base64.decode(cert.getEnccertificate());
			Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
			// X509Certificate oEncX509Cert = (X509Certificate) jse
			// .getCertFromBuffer(bEncCert);
			try {
				ASN1InputStream asn1Input = new ASN1InputStream(
						new ByteArrayInputStream(bCert));
				ASN1Object asn1X509 = asn1Input.readObject();
				X509CertificateStructure x509 = X509CertificateStructure
						.getInstance(asn1X509);
				((TextView) view.findViewById(R.id.tvversion))
						.setText(jse.getCertDetail(1, bCert));
				((TextView) view.findViewById(R.id.tvsignalg))
						.setText(oX509Cert.getSigAlgName());
				((TextView) view.findViewById(R.id.tvcertsn))
						.setText(new String(Hex.encode(oX509Cert
								.getSerialNumber().toByteArray())));
				((TextView) view.findViewById(R.id.tvsubject))
						.setText(x509.getSubject().toString());
				((TextView) view.findViewById(R.id.tvissue))
						.setText(x509.getIssuer().toString());

				tvUserName.setText(x509.getSubject().toString()+" "+x509.getIssuer().toString());

				String strNotBeforeTime = jse.getCertDetail(11, bCert);
				String strValidTime = jse.getCertDetail(12, bCert);
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				((TextView) view.findViewById(R.id.tvaftertime))
						.setText(sdf2.format(toDate));
				((TextView) view.findViewById(R.id.tvbeforetime))
						.setText(sdf2.format(fromDate));

				RelativeLayout relativeLayout1 = (RelativeLayout) view
						.findViewById(R.id.rl_certchainURL);
				String sCertChainPath = jse.getCertExtInfo(
						"1.2.156.1.8888.144", oX509Cert);
				if ("".equals(sCertChainPath)|| null == sCertChainPath) {
					relativeLayout1.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
					((TextView) view
							.findViewById(R.id.tvcertchainpath))
							.setText(sCertChainPath);
				}

				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
					if(!"".equals(cert.getDevicesn())){
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
					    ((TextView) view.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
					}else
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);
					if(!"".equals(cert.getDevicesn())){
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
					    ((TextView) view.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
					}else
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}else{
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
					view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}

				RelativeLayout relativeLayout2 = (RelativeLayout) view
						.findViewById(R.id.rl_subjectUID);
	
				if(CommonConst.CERT_TYPE_SM2.equals(mCert.getCerttype()) || CommonConst.CERT_TYPE_SM2_COMPANY.equals(mCert.getCerttype())||mCert.getCerttype().contains("SM2")){
					view.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
				}else{
					PublicKey publickey = oX509Cert.getPublicKey();
				    String algorithm = publickey.getAlgorithm(); // 获取算法
				    KeyFactory keyFact = KeyFactory.getInstance(algorithm);
				    BigInteger prime = null;
				    if ("RSA".equals(algorithm)) { // 如果是RSA加密
				       RSAPublicKeySpec keySpec = (RSAPublicKeySpec)keyFact.getKeySpec(publickey, RSAPublicKeySpec.class);
				       prime = keySpec.getModulus();
				    } else if ("DSA".equals(algorithm)) { // 如果是DSA加密
				       DSAPublicKeySpec keySpec = (DSAPublicKeySpec)keyFact.getKeySpec(publickey, DSAPublicKeySpec.class);
				       prime = keySpec.getP();
				    }
				    
				    int len = prime.toString(2).length(); // 转换为二进制，获取公钥长度
				    
				    view.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.VISIBLE);  
				    ((TextView) view.findViewById(R.id.tvcertKeySize)).setText(len+"位");   
				}
				
				//String sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.205",oX509Cert);
				String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
				if("".equals(sCertUnicode) || null== sCertUnicode )
					 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
				
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					relativeLayout2.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertunicode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3",oX509Cert);  //获取工商注册号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcerticnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5",oX509Cert);  //获取税号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcerttaxnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4",oX509Cert);  //获取组织机构代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertorgcode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2",oX509Cert);  //获取社会保险号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertinsnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201",oX509Cert);  //获取住房公积金账号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertaccfundnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202",oX509Cert);  //获取事业单位证书号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertinstinumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203",oX509Cert);  //获取社会组织法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertassnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204",oX509Cert);  //获取政府机关法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertgovnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207",oX509Cert);  //获取律师事务所执业许可证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertlawlicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208",oX509Cert);  //获取个体工商户营业执照
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertindilicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209",oX509Cert);  //外国企业常驻代表机构登记证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertforeigncode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210",oX509Cert);  //获取统一社会信用代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertcrednumber))
							.setText(sCertUnicode);
				}

			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}

		} else {
			Toast.makeText(context, "证书不存在", Toast.LENGTH_SHORT).show();
			return;
		}

	}

	
	private void viewSM2SignCert(){
		Cert cert = mCert;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
		sdf.setTimeZone(tzChina);
		// sdf2.setTimeZone(tzChina);
		if (cert != null) {
			String certificate = cert.getCertificate();
			byte[] bCert = Base64.decode(certificate);
			Certificate oCert = jse.getCertFromBuffer(bCert);
			X509Certificate oX509Cert = (X509Certificate) oCert;
		
			try {
				((TextView) view.findViewById(R.id.tvversion))
						.setText(jse.getCertDetail(1, bCert));
				((TextView) view.findViewById(R.id.tvsignalg))
						.setText(CommonConst.CERT_ALG_SM2);
				((TextView) view.findViewById(R.id.tvcertsn))
						.setText(jse.getCertDetail(2, bCert));

				String issue = getSM2CertIssueInfo(cert);
				String sub = getSM2CertSubjectInfo(cert);
				((TextView) view.findViewById(R.id.tvsubject))
				        .setText(issue);
		       ((TextView) view.findViewById(R.id.tvissue))
				        .setText(sub);

		       tvUserName.setText(issue+" "+sub);

				String strNotBeforeTime = jse.getCertDetail(11, bCert);
				String strValidTime = jse.getCertDetail(12, bCert);
				Date fromDate = sdf.parse(strNotBeforeTime);
				Date toDate = sdf.parse(strValidTime);

				((TextView) view.findViewById(R.id.tvaftertime))
						.setText(sdf2.format(toDate));
				((TextView) view.findViewById(R.id.tvbeforetime))
						.setText(sdf2.format(fromDate));

				RelativeLayout relativeLayout1 = (RelativeLayout) view
						.findViewById(R.id.rl_certchainURL);
				String sCertChainPath = jse.getCertExtInfo(
						"1.2.156.1.8888.144", oX509Cert);
				if ("".equals(sCertChainPath)|| null == sCertChainPath) {
					relativeLayout1.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
					((TextView) view
							.findViewById(R.id.tvcertchainpath))
							.setText(sCertChainPath);
				}

				if(CommonConst.SAVE_CERT_TYPE_BLUETOOTH == cert.getSavetype()){
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_BLUETOOTH_NAME);
					if(!"".equals(cert.getDevicesn())){
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
					    ((TextView) view.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
					}else
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}else if(CommonConst.SAVE_CERT_TYPE_SIM == cert.getSavetype()){
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_SIM_NAME);
					if(!"".equals(cert.getDevicesn())){
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.VISIBLE);
					    ((TextView) view.findViewById(R.id.tvdevicesn)).setText(cert.getDevicesn());
					}else
						view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}else{
					((TextView) view.findViewById(R.id.tvsavetype)).setText(CommonConst.SAVE_CERT_TYPE_PHONE_NAME);
					view.findViewById(R.id.relativeLayoutsn).setVisibility(RelativeLayout.GONE);
				}
				
				RelativeLayout relativeLayout2 = (RelativeLayout) view
						.findViewById(R.id.rl_subjectUID);
				
				//String sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.205",oX509Cert);
				String sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.1",oX509Cert);
				if("".equals(sCertUnicode) || null== sCertUnicode )
					 sCertUnicode = jse.getCertExtInfo("1.2.156.1.8888.148",oX509Cert);
				
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					relativeLayout2.setVisibility(RelativeLayout.GONE);
				} else {
					relativeLayout2.setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertunicode))
							.setText(sCertUnicode);
				}
				
				view.findViewById(R.id.relativeLayoutKeySize).setVisibility(RelativeLayout.GONE);
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.3",oX509Cert);  //获取工商注册号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout12).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcerticnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.5",oX509Cert);  //获取税号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout13).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcerttaxnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.4",oX509Cert);  //获取组织机构代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout14).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertorgcode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.10260.4.1.2",oX509Cert);  //获取社会保险号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout15).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertinsnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.201",oX509Cert);  //获取住房公积金账号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout16).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertaccfundnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.202",oX509Cert);  //获取事业单位证书号
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout17).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertinstinumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.203",oX509Cert);  //获取社会组织法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout18).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertassnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.204",oX509Cert);  //获取政府机关法人编码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout19).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertgovnumber))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.207",oX509Cert);  //获取律师事务所执业许可证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout20).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertlawlicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.208",oX509Cert);  //获取个体工商户营业执照
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout21).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertindilicence))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.209",oX509Cert);  //外国企业常驻代表机构登记证
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout22).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertforeigncode))
							.setText(sCertUnicode);
				}
				
				sCertUnicode = jse.getCertExtInfo("1.2.156.112570.11.210",oX509Cert);  //获取统一社会信用代码
				if ("".equals(sCertUnicode) || null== sCertUnicode) {
					view.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.GONE);
				} else {
					view.findViewById(R.id.relativeLayout23).setVisibility(RelativeLayout.VISIBLE);
					((TextView) view.findViewById(R.id.tvcertcrednumber))
							.setText(sCertUnicode);
				}
				
			} catch (Exception e) {
				Log.e(CommonConst.TAG, e.getMessage(), e);
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}

		} else {
			Toast.makeText(context, "证书不存在", Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	
	private String getSM2CertSubjectInfo(Cert cert){
		String certInfo = "";
		String certItem = "";
		
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		
		try {
			certItem = jse.getCertDetail(4, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"C="+certItem+",";
			
			certItem = jse.getCertDetail(5, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"O="+certItem+",";
			
			certItem = jse.getCertDetail(8, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"CN="+certItem+",";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(certInfo.length() > 0)
			certInfo = certInfo.substring(0,certInfo.length()-1);
		
		return certInfo;
	}
	
	private String getSM2CertIssueInfo(Cert cert){
		String certInfo = "";
		String certItem = "";
		
		String certificate = cert.getCertificate();
		byte[] bCert = Base64.decode(certificate);
		
		try {
			certItem = jse.getCertDetail(13, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"C="+certItem+",";
			
			certItem = jse.getCertDetail(18, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"ST="+certItem+",";
			
			certItem = jse.getCertDetail(16, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"L="+certItem+",";
			
			certItem = jse.getCertDetail(19, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"E="+certItem+",";
			
			certItem = jse.getCertDetail(17, bCert);
			if(!"".equals(certItem))
				certInfo = certInfo+"CN="+certItem+",";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(certInfo.length() > 0)
			certInfo = certInfo.substring(0,certInfo.length()-1);
		
		return certInfo;
	}
	
	
	
	public int getCertID() {
		return certID;
	}

	public void setCertID(int certID) {
		this.certID = certID;
	}
	
}
