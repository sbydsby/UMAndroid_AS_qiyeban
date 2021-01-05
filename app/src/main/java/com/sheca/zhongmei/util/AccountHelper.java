package com.sheca.zhongmei.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
//import com.igexin.sdk.PushManager;
import com.sheca.zhongmei.DaoActivity;
import com.sheca.zhongmei.MainActivity;
import com.sheca.zhongmei.account.SetPwdActivityV33;
import com.sheca.zhongmei.companyCert.BaseActivity;
import com.sheca.zhongmei.dao.AccountDao;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.dao.SealInfoDao;
import com.sheca.zhongmei.event.RefreshEvent;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Account;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.OrgInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.excelsecu.util.LibUtil.getApplicationContext;

public class AccountHelper {

    private static String username;
    private static String token;
    public static String uid;
    private static String realName;
    private static String idcardno;


    public static boolean needChangePwd(Context context) {
        return SharePreferenceUtil.getInstance(context).getBoolean(CommonConst.NEED_CHANGE_PWD);
    }

    public static void setNeedChangePwd(Context context, boolean need) {
        SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.NEED_CHANGE_PWD, need);
    }


    public static boolean isLoadLicence(Context context) {
        return SharePreferenceUtil.getInstance(context).getBoolean(CommonConst.LOAD_LICENCE);
    }

    public static void setLoadLicence(Context context, boolean isLoadLicence) {
        SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.LOAD_LICENCE, isLoadLicence);
    }

    //隐私政策
    public static void setAgreeUserRules(Context context, boolean check) {
        SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.USER_RULES, check);
    }

    public static boolean isAgreeUserRules(Context context) {
        return SharePreferenceUtil.getInstance(context).getBoolean(CommonConst.USER_RULES);
    }


    public static String getUMAddress(Context context) {
        return SharePreferenceUtil.getInstance(context).getString(CommonConst.UM_ADDRESS, CommonConst.UM_SERVER);
    }

    public static void setUMAddress(Context context, String umspAddress) {
        SharePreferenceUtil.getInstance(context).setString(CommonConst.UM_ADDRESS, umspAddress);
    }


    public static String getUMSPAddress(Context context) {
//        return SharePreferenceUtil.getInstance(context).getString(CommonConst.UM_UMSP_ADDRESS,"");
        return SharePreferenceUtil.getInstance(context).getString(CommonConst.UM_UMSP_ADDRESS, CommonConst.UM_SERVER);
    }

    public static void setUMSPAddress(Context context, String umspAddress) {
        SharePreferenceUtil.getInstance(context).setString(CommonConst.UM_UMSP_ADDRESS, umspAddress);
    }

    public static String getUCMAddress(Context context) {
//        return SharePreferenceUtil.getInstance(context).getString(CommonConst.UM_UCM_ADDRESS, "");
        return SharePreferenceUtil.getInstance(context).getString(CommonConst.UM_UCM_ADDRESS, CommonConst.UM_APP_UCM_SERVER);
    }

    public static void setUCMAddress(Context context, String ucmAddress) {
        SharePreferenceUtil.getInstance(context).setString(CommonConst.UM_UCM_ADDRESS, ucmAddress);
    }


    public static int getActive(Context context) {
        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_ACTIVE);
    }

    public static String getMobile(Context context) {
        return SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_MOBILE);
    }

    public static int getStatus(Context context) {

        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_STATUS);
    }

    public static int getType(Context context) {
        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_TYPE);
    }

    public static void  setUsername(Context context,String username) {
//        if (TextUtils.isEmpty(username))
       SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_USERNAME,username);

    }

    public static String getUsername(Context context) {
//        if (TextUtils.isEmpty(username))
        username = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_USERNAME);
        return username;
    }

    public static String getToken(Context context) {
//        if (TextUtils.isEmpty(token))
            token = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        return token;
    }

    public static void setRealName(String realName) {
        AccountHelper.realName = realName;
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, realName);
    }

    public static void setIdcardno(String idcardno) {
        AccountHelper.idcardno = idcardno;
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, idcardno);
    }

    public static String getRealName(Context context) {
        String realname = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);
//        if (TextUtils.isEmpty(realName))
            realName = realname;
        return realName;
    }

    public static String getIdcardno(Context context) {
//        String idCardno = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);
////        if (TextUtils.isEmpty(idcardno))
////            idcardno = idCardno;
////        return idcardno;

        return SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);
    }

    public static void clearAllUserData(Context context) {
        token = null;
        username = null;
        uid = null;
        realName = null;
        idcardno = null;

        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_TOKEN, "");
        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_UID, "");

//        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_REALNAME, "");
        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_MOBILE, "");
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_STATUS, -1);
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_TYPE, -1);
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_ACTIVE, -1);
        SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.PARAM_HAS_LOGIN, false);


        DaoActivity.strAccountName = "";
        DaoActivity.strAccountPwd = "";
        EventBus.getDefault().post(new RefreshEvent());

    }


    //从sdk中取出用户信息保存到本地sp中
    //from判断来自短信验证码登录界面还是登录界面，登录界面则是已经设置过密码
    public static void savePersonInfoToLocal(final Activity activity, final String token, final String mobile) {
        new MyAsycnTaks() {


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                String params = ParamGen.getAccountParams(token, mobile);
                UniTrust uniTrust = new UniTrust(activity, false);
                uniTrust.setAppID(CommonConst.UM_APPID);
                Account mAccount = uniTrust.getAcountEx(params);
                if (mAccount != null) {
                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_STATUS, mAccount.getStatus());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_CCOUNT_TYPE, mAccount.getType());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_ACTIVE, mAccount.getActive());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_CERT_TYPE, mAccount.getCertType());

                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_REALNAME, mAccount.getIdentityName());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_IDCARD, mAccount.getIdentityCode());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_TOKEN, token);
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_USERNAME, mobile);
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.ACCOUNT_NAME, mAccount.getIdentityName());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.ACCOUNT_IDNO, mAccount.getIdentityCode());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_APPID, mAccount.getAppIDInfo());
                    SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.PARAM_ACCOUNT_UID, mAccount.getAccountuid());


//                    PushManager.getInstance().bindAlias(activity, mobile);//注册个推别名
                    DaoActivity.strAccountName = mobile;

                    int status = Integer.valueOf(mAccount.getStatus());
                    int active = Integer.valueOf(mAccount.getActive());
                    int type = Integer.valueOf(mAccount.getType());
                    int saveType = Integer.valueOf(mAccount.getSaveType());
                    int certType = Integer.valueOf(mAccount.getCertType());
                    int loginType = Integer.valueOf(mAccount.getLoginType());
                    com.sheca.zhongmei.model.Account account = new com.sheca.zhongmei.model.Account(
                            mAccount.getName(),
                            mAccount.getPassword(),
                            status,
                            active,
                            mAccount.getIdentityName(),
                            mAccount.getIdentityCode(),
                            mAccount.getCopyIDPhoto(),
                            type,
                            mAccount.getAppIDInfo(),
                            mAccount.getOrgName(),
                            saveType,
                            certType,
                            loginType);


                    AccountDao mAccountDao = new AccountDao(activity);


                    mAccountDao.add(account);


                    //非首次登录
                    setisFirstLogin(getApplicationContext(), false);
                }


            }

            @Override
            public void postTask() {

                //枚举印章及证书信息
                getCertList(activity, mobile, "");

            }
        }.execute();


    }

    //获取印章及证书信息保存到sp中,"fromWhere"有这个参数表明来自哪个界面，下载证书界面，跳入的是下载成功页面而不是主页
    public static void getCertList(final Activity activity, final String mobile, final String fromWhere) {

        new MyAsycnTaks() {
            @Override
            public void preTask() {

            }

            @Override
            public void doinBack() {
                String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
                String strInfoGetAllCert = ParamGen.getAcountAllCerts(mTokenID, mAccountName);
                UniTrust uniTrust = new UniTrust(activity, false);
                List<Cert> mCertList = uniTrust.getAcountAllCerts(strInfoGetAllCert);

                CertDao certDao = new CertDao(activity);

                if (mCertList != null && mCertList.size() != 0) {

                    for (int i = 0; i < mCertList.size(); i++) {
                        com.sheca.zhongmei.model.Cert mCert = new com.sheca.zhongmei.model.Cert();
                        mCert.setId(mCertList.get(i).getId());
                        mCert.setCertsn(mCertList.get(i).getCertsn());
                        mCert.setEnvsn(mCertList.get(i).getEnvsn());
                        mCert.setPrivatekey(mCertList.get(i).getPrivatekey());
                        mCert.setCertificate(mCertList.get(i).getCertificate());
                        mCert.setKeystore(mCertList.get(i).getKeystore());
                        mCert.setEnccertificate(mCertList.get(i).getEnccertificate());
                        mCert.setEnckeystore(mCertList.get(i).getEnckeystore());
                        mCert.setCertchain(mCertList.get(i).getCertchain());
                        mCert.setStatus(mCertList.get(i).getStatus());
                        mCert.setAccountname(mCertList.get(i).getAccountname());
                        mCert.setNotbeforetime(mCertList.get(i).getNotbeforetime());
                        mCert.setValidtime(mCertList.get(i).getValidtime());
                        mCert.setUploadstatus(mCertList.get(i).getUploadstatus());
                        mCert.setCerttype(mCertList.get(i).getCerttype());
                        mCert.setSignalg(mCertList.get(i).getSignalg());
                        mCert.setContainerid(mCertList.get(i).getContainerid());
                        mCert.setAlgtype(mCertList.get(i).getAlgtype());
                        mCert.setSavetype(mCertList.get(i).getSavetype());
                        mCert.setDevicesn(mCertList.get(i).getDevicesn());
                        mCert.setCertname(mCertList.get(i).getCertname());
                        mCert.setCerthash(mCertList.get(i).getCerthash());
                        mCert.setFingertype(mCertList.get(i).getFingertype());
                        mCert.setSealsn(mCertList.get(i).getSealsn());
                        mCert.setSealstate(mCertList.get(i).getSealstate());
                        mCert.setCertlevel(mCertList.get(i).getCertlevel());

                        certDao.addCert(mCert, mobile);
                    }

                }


                Gson mGson = new Gson();
                String mCertString = mGson.toJson(mCertList);
                SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.CERT_LIST, mCertString);

            }

            @Override
            public void postTask() {
                getSealList(activity, mobile, fromWhere);
            }
        }.execute();
    }

    //获取印章集合
    public static void getSealList(final Activity activity, final String mobile, final String fromWhere) {
        new MyAsycnTaks() {
            @Override
            public void preTask() {

            }

            @Override
            public void doinBack() {
                String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
                String strInfoGetAllSeal = ParamGen.getAcountAllCerts(mTokenID, mAccountName);
                UniTrust mUnitTrust = new UniTrust(activity, false);
                List<com.sheca.umplus.model.SealInfo> mSealList = mUnitTrust.getAcountAllSealInfos(strInfoGetAllSeal);


                SealInfoDao sealInfo = new SealInfoDao(activity);
                if (mSealList != null && mSealList.size() != 0) {

                    for (int i = 0; i < mSealList.size(); i++) {
                        com.sheca.zhongmei.model.SealInfo mSealInfo = new com.sheca.zhongmei.model.SealInfo();
                        mSealInfo.setId(mSealList.get(i).getId());
                        mSealInfo.setVid(mSealList.get(i).getVid());
                        mSealInfo.setSealname(mSealList.get(i).getSealname());
                        mSealInfo.setSealsn(mSealList.get(i).getSealsn());
                        mSealInfo.setIssuercert(mSealList.get(i).getIssuercert());
                        mSealInfo.setCert(mSealList.get(i).getCert());
                        mSealInfo.setPicdata(mSealList.get(i).getPicdata());
                        mSealInfo.setPictype(mSealList.get(i).getPictype());
                        mSealInfo.setPicwidth(mSealList.get(i).getPicwidth());
                        mSealInfo.setPicheight(mSealList.get(i).getPicheight());
                        mSealInfo.setNotbefore(mSealList.get(i).getNotbefore());
                        mSealInfo.setNotafter(mSealList.get(i).getNotafter());
                        mSealInfo.setSignal(mSealList.get(i).getSignal());
                        mSealInfo.setExtensions(mSealList.get(i).getExtensions());
                        mSealInfo.setAccountname(mSealList.get(i).getAccountname());
                        mSealInfo.setCertsn(mSealList.get(i).getCertsn());
                        mSealInfo.setState(mSealList.get(i).getState());
                        mSealInfo.setDownloadstatus(mSealList.get(i).getDownloadstatus());

                        sealInfo.addSeal(mSealInfo, mobile);
                    }

                }


                Gson mGson = new Gson();
                String mSealString = mGson.toJson(mSealList);
                SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.SEAL_LIST, mSealString);

            }

            @Override
            public void postTask() {
                DaoActivity.strAccountName = mobile;
                SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_V26_DBCHECK, 1);

                //如果已经设置过密码且来自登录页面，则跳入主页，未设置过密码则跳入设置密码界面再跳入主页

                EventBus.getDefault().post(new RefreshEvent());
                if (AccountHelper.needChangePwd(activity)) {
                    Intent intent = new Intent(activity, SetPwdActivityV33.class);
                    intent.putExtra("phone",mobile);
                    activity.startActivity(intent);
                    activity.finish();

                } else {
                    SharePreferenceUtil.getInstance(activity).setBoolean(CommonConst.PARAM_HAS_LOGIN, true);
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    activity.finish();

                }
//                if (fromWhere.equals("")) {
//                    Intent intent = new Intent(activity, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    activity.startActivity(intent);
//                    activity.finish();
//                    //已经设置过密码且来自下载证书页面
//                } else if (fromWhere.equals("fromDownload")) {
//                    Intent intent = new Intent(activity, AddCertResultActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    activity.startActivity(intent);
//                    activity.finish();
//                }
            }
        }.execute();
    }


    public static boolean hasLogin(Context context) {
//        String token = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
//        if (TextUtils.isEmpty(token)) {
//            return false;
//        } else {
//            return true;
//        }

        return SharePreferenceUtil.getInstance(context).getBoolean(CommonConst.PARAM_HAS_LOGIN);
    }

    public static void setLogin(Context context,boolean login) {
//        String token = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
//        if (TextUtils.isEmpty(token)) {
//            return false;
//        } else {
//            return true;
//        }

         SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.PARAM_HAS_LOGIN,login);
    }

    public static boolean hasAuth(Context context) {
        try {
//            int status = getStatus(context);
            AccountDao mAccountDao = new AccountDao(context);
            int status = mAccountDao.getLoginAccount().getStatus();
            if (5 == status || 3 == status || 4 == status) {//0：未实名认证；1：已实名认证
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }


    //获取单位信息集合
    public static void getOrgList(final BaseActivity activity, final String mobile, int type) {
        new MyAsycnTaks() {
            @Override
            public void preTask() {

            }

            @Override
            public void doinBack() {
                String mTokenID = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
                String mAccountName = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.PARAM_USERNAME); //name
                String strInfoGetAllSeal = ParamGen.getAcountAllOrg(mTokenID, mAccountName);

                UniTrust mUnitTrust = new UniTrust(activity, false);
                List<OrgInfo> orgList = mUnitTrust.getAcountAllAuthOrgInfos(strInfoGetAllSeal);

//                OrgInfoDao accountDao = new OrgInfoDao(activity);
//                accountDao.save(orgList);

//                Gson mGson = new Gson();
//                String mSealString = mGson.toJson(orgList);
//                SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.ORG_LIST, mSealString);

            }

            @Override
            public void postTask() {
//                DaoActivity.strAccountName=mobile;
                SharePreferenceUtil.getInstance(getApplicationContext()).setInt(CommonConst.PARAM_V26_DBCHECK, 1);
//                EventBus.getDefault().post(new RefreshEvent());

                activity.gotoNextActivity(type);


            }
        }.execute();
    }


    /**
     * 设置是首次登录
     *
     * @param mContext
     * @param isFirstLogin true 代表首次登录
     *                     是否是首次登录的键名应该为该用户的手机号和身份证号
     */
    public static void setisFirstLogin(Context mContext, boolean isFirstLogin) {
        String key = getAccountMobile(mContext) + getIDNumber(mContext);
        SharePreferenceUtil.getInstance(mContext).setBoolean(key, isFirstLogin);
    }

    /**
     * 获取是否是首次登录
     * 是否是首次登录的键名应该为该用户的手机号和身份证号
     */
    public static boolean isFirstLogin(Context mContext) {
        if (TextUtils.isEmpty(getIDNumber(mContext))) {
            return true;
        }
        String key = getAccountMobile(mContext) + getIDNumber(mContext);
        return SharePreferenceUtil.getInstance(mContext).getBoolean_true(key);
    }

    /**
     * 手机号保存(临时保存)
     */
    public static void saveAccountMobile(Context mContext, String mobile) {
        SharePreferenceUtil.getInstance(mContext).setString(CommonConst.ACCOUNT_MOBILE, mobile);
    }

    /**
     * 手机号获取（临时）
     */
    public static String getAccountMobile(Context mContext) {
//        String mobile = SharePreferenceUtil.getInstance(mContext).getString(CommonConst.ACCOUNT_MOBILE);
//        if (TextUtils.isEmpty(mobile)) {

        return getUsername(mContext);
//        } else {
//        return mobile;
//        }

    }

    public static void setIDNumber(Context mContext, String mNumber) {
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.ACCOUNT_IDNO, mNumber);
    }

    /**
     * 获取身份证号（临时）
     */
    public static String getIDNumber(Context mContext) {

//        String idNum = SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.ACCOUNT_IDNO);
//        if (TextUtils.isEmpty(idNum)) {
        return getIdcardno(mContext);
//        } else {
//        return idNum;
//        }

    }

    public static void setChangePassword(Context context, boolean isSetPWD) {
        String userName = getUsername(context);
        SharePreferenceUtil.getInstance(context).setBoolean(CommonConst.FIRST_SMS_LOGIN + userName, isSetPWD);
    }

    public static boolean getChangePassword(Context context) {
        String userName = getUsername(context);
        return SharePreferenceUtil.getInstance(context).getBoolean(CommonConst.FIRST_SMS_LOGIN + userName);
    }

    /**
     * 保存密码（临时）
     */
    public static void savaAcoountPWD(Context mContext, String pwd) {
        SharePreferenceUtil.getInstance(mContext).setString(CommonConst.ACCOUNT_PWD, pwd);
    }

    /**
     * 获取密码（临时）
     */
    public static String getAccountPWD(Context mContext) {
        return SharePreferenceUtil.getInstance(mContext).getString(CommonConst.ACCOUNT_PWD);
    }

    /**
     * 保存姓名（临时）
     */
    public static void setIDName(Context mContext, String mName) {
        SharePreferenceUtil.getInstance(getApplicationContext()).setString(CommonConst.ACCOUNT_NAME, mName);
    }

    /**
     * 获取用户姓名（临时）
     */
    public static String getIDName(Context mContext) {
        return SharePreferenceUtil.getInstance(getApplicationContext()).getString(CommonConst.ACCOUNT_NAME);
    }
}
