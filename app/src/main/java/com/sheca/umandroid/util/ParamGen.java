package com.sheca.umandroid.util;

import android.content.Context;

import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.Account;
import com.sheca.umplus.util.PKIUtil;

import java.net.URLEncoder;

import static com.sheca.umandroid.util.CommonConst.UM_APPID;

public class ParamGen {

    public static String getApplyCertRequest(String token,
                                             String name, String idCardNo,
                                             String certType, String certExpire) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(name),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO),
                URLEncoder.encode(idCardNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                URLEncoder.encode(certType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
                URLEncoder.encode(certExpire)
        );

        return strInfo;
    }

    public static String getWeChatPayQueryOrder(String token, String reqNo) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_REQUEST_NUMBER),
                URLEncoder.encode(reqNo)
        );

        return strInfo;
    }

    public static String getWeChatPayUnifiedorder(String token, String reqNo) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_REQUEST_NUMBER),
                URLEncoder.encode(reqNo)
        );

        return strInfo;
    }

    public static String getApplySeal(Context context, String picData, String CERT_ID, String CERT_PWD) {
        AccountDao accountDao = new AccountDao(context);
        Account account = accountDao.getLoginAccount();

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(AccountHelper.getToken(context)),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_NAME),
                URLEncoder.encode(account.getIdentityName()),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SEAL_ID),
                URLEncoder.encode(account.getIdentityCode()),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_SEALNAME),
                URLEncoder.encode(account.getIdentityName()),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_DATA),
                URLEncoder.encode(picData),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_TYPE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.SEAL_PIC_TYPE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(CERT_ID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(CERT_PWD));

        return strInfo;
    }

    public static String getSealBySN(String username, String sealid, String token) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(username),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_SEALID),
                URLEncoder.encode(sealid),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token)
        );

        return strInfo;
    }


    public static String getAccountCertByID(String token, String username, String certid) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(username),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(certid)
        );

        return strInfo;
    }

    public static String getAccountParams(String token, String username) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(username)
        );

        return strInfo;
    }

    /**
     * 3.1.13 获取账户信息
     *
     * @param token
     * @return
     */
    public static String getPersonalInfoParams(String token) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token));

        return strInfo;
    }

    /**
     * 3.1.14 设置账户实名认证结果
     *
     * @param token
     * @param commName
     * @param paperNo
     * @param verifyResult
     * @return
     */
    public static String getAccountVerification(String token, String commName, String paperNo, String verifyResult) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(commName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                "1",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERTIFICATION),
                URLEncoder.encode(verifyResult));

        return strInfo;

    }



    public static String getLoadLisenceParams() {
        String strActName = CommonConst.UM_APPID;
        String appSecret = CommonConst.UM_APP_PRIVATE_KEY;
        String mURL = CommonConst.SERVICE_URL;
        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                strActName,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
                CommonConst.PACKAGE_NAME,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                CommonConst.UM_APP_AUTH_KEY,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_VERSION),
                com.sheca.umplus.util.CommonConst.APP_VERSION,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APP_SECRET),
                URLEncoder.encode(appSecret),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_UMSP_SERVICE_URL),
                mURL
        );

//        String strSign = "";

//        try {
//            //byte[] bDate = strOrgDate.getBytes("GBK");
//            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
//                URLEncoder.encode(strActName),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
//                URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
//                URLEncoder.encode(CommonConst.PACKAGE_NAME),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_VERSION),
//                com.sheca.umplus.util.CommonConst.APP_VERSION,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
//                URLEncoder.encode(strSign),
//
//        );


        return strOrgDate;
    }


    public static String getLoadLisenceParams(Context context) {
        String strActName = UM_APPID;
        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                strActName,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
                CommonConst.PACKAGE_NAME,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                CommonConst.UM_APP_AUTH_KEY,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
                com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256,

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APP_SECRET),
                CommonConst.UM_APP_PRIVATE_KEY,

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_UMSP_SERVICE_URL),
                AccountHelper.getUMAddress(context)


        );

        String strSign = "";

        try {
            //byte[] bDate = strOrgDate.getBytes("GBK");
            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
                URLEncoder.encode(CommonConst.PACKAGE_NAME),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APP_SECRET),
                URLEncoder.encode(  CommonConst.UM_APP_PRIVATE_KEY),

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_UMSP_SERVICE_URL),
                URLEncoder.encode( AccountHelper.getUMAddress(context)),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
                URLEncoder.encode(strSign)

        );


        return strInfo;
    }

//    public static String getLoadLisenceParams() {
//        String strActName = UM_APPID;
//        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s%s=%s",
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
//                strActName,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
//                CommonConst.PACKAGE_NAME,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
//                CommonConst.UM_APP_AUTH_KEY,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256,
//                URLEncoder.encode("version"),
//                URLEncoder.encode("1.0")
//
//        );
//
//        String strSign = "";
//
//        try {
//            //byte[] bDate = strOrgDate.getBytes("GBK");
//            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s%s=%s",
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
//                URLEncoder.encode(strActName),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
//                URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PACKAGE_NAME),
//                URLEncoder.encode(CommonConst.PACKAGE_NAME),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
//                URLEncoder.encode(strSign) ,
//                URLEncoder.encode("version"),
//                URLEncoder.encode("1.0")
//        );
//
//
//        return strInfo;
//    }



    public static String getLoginByPassword(String username, String password) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(username),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                URLEncoder.encode(password),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(UM_APPID));

        return strInfo;
    }

    /**
     * 获取短信验证码参数
     */
    public static String getValidationCodeParams(String phone, String codeType) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MOBILE),
                phone,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MAC_TYPE),
                codeType,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                UM_APPID
        );
        return strInfo;
    }


    /**
     * 检测账户是否已经存才参数
     */
    public static String getCheckIsAccountExistedParams(String accountName) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(accountName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                UM_APPID);
        return strInfo;
    }

    /**
     * 检测账户是否已经存在参数
     */
    public static String getCheckIsAccountExistedParams(String accountName, String accountID) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(accountName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                CommonConst.UM_APPID,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                CommonConst.PAPER_TYPE,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                accountID
        );
        return strInfo;
    }

    /**
     * 短信验证码登录
     */

    public static String getUserLoginByValidationCodeParams(String accountName, String valiCode) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(accountName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_MAC),
                URLEncoder.encode(valiCode),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(UM_APPID));
        return strInfo;
    }

    /**
     * 注册参数
     *
     * @param accountName
     * @return
     */
    public static String getRegisterAccountParams(String accountName, String msgCode) {
        //String ranPwdHash = CommUtil.getPWDHash(CommUtil.getStringRandom(8));
        String ranPwdHash = accountName + "" + accountName;

        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                accountName,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                UM_APPID,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_NOTIFY_MODE),
                "0",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD_HASH),
                ranPwdHash,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
                com.sheca.umplus.util.CommonConst.CERT_ALG_RSA,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SOURCE),
                "1");

        String strSign = "";

        try {
            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(accountName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_MAC),
                URLEncoder.encode(msgCode),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                URLEncoder.encode(ranPwdHash)

                );

        return strInfo;
    }

    /**
     * 重置密码参数
     */
    public static String getResetUserPwdParams(String newPwd, String tokenID, String actName) {

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                tokenID,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                actName,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                newPwd,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                UM_APPID
        );

        return strInfo;
    }

    /**
     * 修改账户密码参数
     */
    public static String getChangeUserPwdParams(Context context, String oldPwd, String newPwd) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                AccountHelper.getToken(context),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OLD_PWD),
                oldPwd,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_NEW_PWD),
                newPwd,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                UM_APPID
        );

        return strInfo;
    }

    /**
     * 撤销证书
     */

    public static String doRevokeCertParams(String mTokenId, String mCertID, String mCertPwd) {

        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(mCertID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(mCertPwd));

        return strInfo;
    }

    /**
     * 获取删除证书的参数
     */

    public static String getDelteCerParams(String mTokenId, String mCertID, String mCertPwd) {

        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(mCertID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(mCertPwd));

        return strInfo;
    }

    /**
     * 获取修改证书密码的参数
     */

    public static String fixCertPassWord(String mTokenId, String mCertID, String mOldPwd, String mNewPwd) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(mCertID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OLD_CERT_PWD),
                URLEncoder.encode(mOldPwd),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_NEW_CERT_PWD),
                URLEncoder.encode(mNewPwd)
        );

        return strInfo;
    }

    /**
     * 重置证书口令
     */

    public static String ResetCertPWD(String tokenId, String mCertID, String mNewPwd) {
//        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(tokenId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(mCertID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_NEW_CERT_PWD),
                URLEncoder.encode(mNewPwd)
        );

        return strInfo;
    }

    /**
     * 获取更新证书的参数
     */

    public static String getUpDateCertParams(String mTokenId, String mCertID, String mCertPwd) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(mCertID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(mCertPwd));
        return strInfo;

    }


    /**
     * 获取证书枚举参数
     *
     * @param
     * @return
     */
    public static String getEnumCertIDs(String mTokenID) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenID));
        return strInfo;
    }

    /**
     * 获取登录账户所有证书对象的集合
     */
    public static String getAcountAllCerts(String mTokenID, String mAccountName) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(mAccountName));
        return strInfo;
    }

    /**
     * 根据证书ID获取证书参数
     */
    public static String getCertByIdParams(String strCertID) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(strCertID));
        return strInfo;
    }

    /**
     * 获取证书基本项参数
     */
    public static String getCertItemParams(String mStrPemCert, String strCertItem) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT),
                URLEncoder.encode(mStrPemCert),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ITEM_NO),
                URLEncoder.encode(strCertItem));
        return strInfo;
    }

    /**
     * 获取证书扩展项参数
     *
     * @param mStrPemCert
     * @param strCertOid
     * @return
     */
    public static String getCertExtParams(String mStrPemCert, String strCertOid) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT),
                URLEncoder.encode(mStrPemCert),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_OID),
                URLEncoder.encode(strCertOid));
        return strInfo;
    }

    /**
     * 验证证书参数
     *
     * @param strCertID
     * @return
     */
    public static String getVerifyCertParams(String strCertID) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(strCertID));
        return strInfo;
    }

    public static String getFaceAuthOCR() {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OCR_FLAG),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_TRUE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OCR_KEY),
                URLEncoder.encode(CommonConst.OCR_KEY)
        );


        return strInfo;
    }




//    public static String getIdAuth(Context context) {
//        String strInfo;
//        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
//
//            strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
//                    URLEncoder.encode(strActName),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTH_TYPE),
//                    URLEncoder.encode(realname),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PERSON_ID),
//                    URLEncoder.encode(idcard),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_INDENTITY_TYPE),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ID_AUTH_NUMBER),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
//                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE),
//
//
//                    );
//
//
//        return strInfo;
//    }

    public static String getFaceAuth(Context context) {
        String strInfo;
        if (AccountHelper.hasAuth(context)) {
            String realname = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);
            String idcard = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);


            strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OCR_FLAG),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE),
                    //URLEncoder.encode(CommonConst.PARAM_OCR_KEY),
                    //URLEncoder.encode(OCR_KEY),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PERSON_NAME),
                    URLEncoder.encode(realname),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PERSON_ID),
                    URLEncoder.encode(idcard),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTH_FLAG),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE));
        } else {
            strInfo = String.format("%s=%s",
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_OCR_FLAG),
                    URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_FLAG_FALSE));
        }

        return strInfo;
    }

    public static String getApplyCertParams(Context context, String certType, String commonName, String paperNo, String psdHash) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        ;   //账户注册名称(可选参数)

        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                CommonConst.UM_APP_AUTH_KEY,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                certType,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                commonName,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO),
                paperNo,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                "1",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
                com.sheca.umplus.util.CommonConst.CERT_ALG_RSA,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
                CommonConst.DEFAULT_CERT_VALIDITY);//com.sheca.umplus.util.CommonConst.DEFAULT_CERT_VALIDITY);


        String strSign = "";
        try {
            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                //URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AUTHKEYID),
                //URLEncoder.encode(CommonConst.UM_APP_AUTH_KEY),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(commonName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                URLEncoder.encode(certType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(psdHash),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
                URLEncoder.encode(CommonConst.DEFAULT_CERT_VALIDITY+""));//,
        //URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
        //URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA),
        //URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
        //URLEncoder.encode(strSign));

        return strInfo;
    }

    public static String getDownloadCertParams(String strLoginTokenID, String reqNumber, final String certType, final String commonName) {
        String strActName = strLoginTokenID;   //账户注册名称(可选参数)
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_REQUEST_NUMBER),
                URLEncoder.encode(reqNumber),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                URLEncoder.encode(certType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(commonName));
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
//                URLEncoder.encode(ACCOUNT_PWD));

        return strInfo;
    }









    public static String getCertInfoListParams(String token) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token));

        return strInfo;
    }

    public static String getScanParams(String token, String QRContent) {
        String strinfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                com.sheca.umplus.util.CommonConst.PARAM_APPID, UM_APPID,
                com.sheca.umplus.util.CommonConst.PARAM_SCAN_MODE, com.sheca.umplus.util.CommonConst.PARAM_IMPLICIT,
                com.sheca.umplus.util.CommonConst.PARAM_QRCONTENT, QRContent);

        return strinfo;
    }

    public static String getLogout(String token) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token));

        return strInfo;
    }

    //上传用证记录
    public static String UploadRecord(Context act, String type ,String time,String msg) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_BIZ_TYPE),
                URLEncoder.encode(type),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_BIZ_TIME),
                URLEncoder.encode(time),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MESSAGE),
                URLEncoder.encode(msg));
        return strInfo;
    }

    //判断应用是否合法
    public static String IsValidApplication(Context act, String appId) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(appId));
        return strInfo;
    }

    //扫码签章
    public static String Scan(Context act, String qrContent, String certId, String pwd) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);


        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_QRCONTENT),
                URLEncoder.encode(qrContent),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(certId),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(pwd),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SCAN_MODE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_IMPLICIT)
        );
        return strInfo;
    }

    //自动登录
    public static String getAutoLoginParam(String token) {
        String strInfo = String.format("%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token));

        return strInfo;

    }

    //获取当前app版本信息
    public static String GetClientLatestInfo(String clientName, String currentVersion) {

        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(CommonConst.PARAM_CLIENT_NAME),
                URLEncoder.encode(clientName),
                URLEncoder.encode(CommonConst.PARAM_CURENT_VERSION),
                URLEncoder.encode(currentVersion));
        return strInfo;
    }

    //获取二维码信息
    public static String GetQRCodeInfo(Context act, String qrCodeSN) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_QRCODE_SN),
                URLEncoder.encode(qrCodeSN));
        return strInfo;
    }


    //根据certSn取cert
    public static String getAccountCertByCertSN(Context act, String userName,String certSn) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ENCRYPT_CERTSN),
                URLEncoder.encode(certSn),
                URLEncoder.encode(CommonConst.PARAM_ACCOUNT_NAME_PWD),
                URLEncoder.encode(userName)


                );
        return strInfo;
    }

    //获取待签名数据
    public static String GetSignBizData(Context act, String qrCodeSN) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGN_BIZ_NO),
                URLEncoder.encode(qrCodeSN));
        return strInfo;
    }

    //上传签名结果
    public static String UploadSignValue(Context act, String qrCodeSN, String msg, boolean isSuccess) {
        String token = SharePreferenceUtil.getInstance(act).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(token),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGN_BIZ_NO),
                URLEncoder.encode(qrCodeSN),
                (isSuccess ? URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGN_VALUE) : URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ERR_MSG)),
                URLEncoder.encode(msg)
        );


        return strInfo;
    }



    //获取单位信息
    public static String GetOrgInfo(Context context, String orgPaperNo) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(orgPaperNo)
//                ,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRNAME),
//                URLEncoder.encode(lrName),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRPAPERTYPE),
//                URLEncoder.encode("1")
//                ,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRPAPERNO),
//                URLEncoder.encode(lrNo)
        );
        return strInfo;
    }

    /**
     * 单位信息认证
     *
     * @param paperNo
     * @param
     * @return
     */
    public static String getVerifyOrg(Context context, String orgName, String paperNo, String lrName, String cardNo) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ORG_NAME),
                URLEncoder.encode(orgName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERTIFICATION),
                URLEncoder.encode("1"),


                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRNAME),
                URLEncoder.encode(lrName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRPAPERTYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_LRPAPERNO),
                URLEncoder.encode(cardNo));

        return strInfo;

    }


    //申请单位证书
    public static String getApplyCertParams(Context context, String certType, String commonName, String paperNo, String psdHash, int time, int certlevel, String requestnumber) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        //账户注册名称(可选参数)
        String idCard = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);
        String agentName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);
        String agentMobile = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_USERNAME);





        String exInfo = "{\"1.2.156.10260.4.1.1\":\"" + idCard + "\"}";

//        String strOrgDate = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
//                certType,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
//                commonName,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
//                paperNo,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
//                "1",
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256,
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
//                com.sheca.umplus.util.CommonConst.DEFAULT_CERT_VALIDITY);
//

//        String strSign = "";
//        try {
//            strSign = PKIUtil.getSign(strOrgDate.getBytes("UTF-8"), CommonConst.UM_APP_PRIVATE_KEY);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(commonName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                URLEncoder.encode(certType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(psdHash),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
                URLEncoder.encode(time + ""),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
//                URLEncoder.encode(strSign));

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AGENT_NAME),
                URLEncoder.encode(agentName),
//                URLEncoder.encode(agentName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AGENT_MOBILE),
                URLEncoder.encode(agentMobile),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_LEVEL),
                URLEncoder.encode(certlevel + ""),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PARTNER_REQ_NO),
                URLEncoder.encode(requestnumber),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_EXT_INFOS),
                URLEncoder.encode(exInfo)

        );
        return strInfo;
    }

    //申请印章
    public static String getApplySeal(Context context, String picData, String idNumber, String CERT_ID, String CERT_PWD, String strSealName, String compName) {

//        String idNumber = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);     //id_no
//        String idName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);//id_name

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(AccountHelper.getToken(context)),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_TYPE),
                URLEncoder.encode("2"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_NAME),
                URLEncoder.encode(compName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SEAL_ID),
                URLEncoder.encode(idNumber),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_SEALNAME),
                URLEncoder.encode(strSealName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_DATA),
                URLEncoder.encode(picData),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_TYPE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.SEAL_PIC_TYPE),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(CERT_ID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(CERT_PWD));

        return strInfo;
    }

    //申请印章（已有印模图片）
    public static String makeApplySeal(Context context, String picData, String idNumber, String CERT_ID, String CERT_PWD, String strSealName, String compName, String picType) {

//        String idNumber = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);     //id_no
//        String idName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);//id_name

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(AccountHelper.getToken(context)),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_TYPE),
                URLEncoder.encode("2"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_USER_NAME),
                URLEncoder.encode(compName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SEAL_ID),
                URLEncoder.encode(idNumber),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_SEALNAME),
                URLEncoder.encode(strSealName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_DATA),
                URLEncoder.encode(picData),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PIC_TYPE),
                URLEncoder.encode(picType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_ID),
                URLEncoder.encode(CERT_ID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(CERT_PWD));

        return strInfo;
    }


    /**
     * 获取登录账户所有单位对象的集合
     */
    public static String getAcountAllOrg(String mTokenID, String mAccountName) {
        String strInfo = String.format("%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(mTokenID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(mAccountName));
        return strInfo;
    }



    //付费证书
    public static String getApplyCertLiteParams(Context context, String certType, String commonName, String paperNo, String psdHash, int time) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);

//        String agentName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);
//        String agentMobile = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_USERNAME);
//        String idCard = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);

                String agentName = "邵博阳";
        String agentMobile ="13701766266";
        String idCard = "310109199205263514";
        String exInfo = "{\"1.2.156.10260.4.1.1\":\"" + idCard + "\"}";

        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(commonName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_TYPE),
                URLEncoder.encode(certType),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_PWD),
                URLEncoder.encode(psdHash),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_VALIDITY),
                URLEncoder.encode(time + ""),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIG_ALG),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.CERT_ALG_RSA256),
//                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SIGNATURE),
//                URLEncoder.encode(strSign));

                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AGENT_NAME),
                URLEncoder.encode(agentName),
//                URLEncoder.encode(agentName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_AGENT_MOBILE),
                URLEncoder.encode(agentMobile),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_EXT_INFOS),
                URLEncoder.encode(exInfo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_CERT_LEVEL),
                URLEncoder.encode("1")


        );
        return strInfo;
    }


    //查印章
    public static String querySealByID(Context context, String sealId) {
        String strActName = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID),
                URLEncoder.encode(strActName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_SEAL_TYPE),
                URLEncoder.encode("2"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_SEALID),
                URLEncoder.encode(sealId)

        );

        return strInfo;
    }



    /**
     * 账户注册参数
     */
    public static String registerAccountParams(String phone, String name, String paperNo, String pwd) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(phone),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_COMMON_NAME),
                URLEncoder.encode(name),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                URLEncoder.encode("1"),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(paperNo),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                URLEncoder.encode(pwd)
        );
        return strInfo;
    }

    //验证短信验证码
    public static String verifyMAC(String mobile,String mac){
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MOBILE),
                URLEncoder.encode(mobile),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MAC_TYPE),
                CommonConst.MAC_TYPE_CERT_PWD,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_MAC),
                URLEncoder.encode(mac));
        return strInfo;
    }

    //通过验证码重置账户密码
    public static String resetPWD(String mobile,String pwd){
        String strInfo = String.format("%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(mobile),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                URLEncoder.encode(pwd));
        return strInfo;
    }

    public static String getLoginByPassword(String accountName, String password, String accountID) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_ACCOUNT_NAME),
                URLEncoder.encode(accountName),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PWD),
                URLEncoder.encode(password),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_TYPE),
                CommonConst.PAPER_TYPE,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_PAPER_NO1),
                URLEncoder.encode(accountID)
        );

        return strInfo;
    }

    /**
     * 验证短信验证码
     */
    public static String verifyMac(String phone, String codeType, String mac) {
        String strInfo = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MOBILE),
                phone,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_MAC_TYPE),
                codeType,
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.PARAM_APPID),
                URLEncoder.encode(CommonConst.UM_APPID),
                URLEncoder.encode(com.sheca.umplus.util.CommonConst.RESULT_PARAM_MAC),
                URLEncoder.encode(mac)
        );
        return strInfo;
    }
}
