package com.sheca.umandroid.util;

import android.content.Context;
import android.text.TextUtils;

import com.sheca.umandroid.DaoActivity;
import com.sheca.umandroid.dao.AccountDao;

public class AccountHelper {

    private static String username;
    private static String token;
    public static String uid;
    private static String realName;
    private static String idcardno;

    public static int getActive(Context context){
        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_ACTIVE);
    }

    public static String getMobile(Context context){
        return SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_MOBILE);
    }

    public static int getStatus(Context context){

        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_STATUS);
    }

    public static int getType(Context context){
        return SharePreferenceUtil.getInstance(context).getInt(CommonConst.PARAM_TYPE);
    }

    public static String getUsername(Context context){
        if (TextUtils.isEmpty(username))
            username = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_USERNAME);
        return username;
    }

    public static String getToken(Context context){
        if (TextUtils.isEmpty(token))
            token = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        return token;
    }

    public static void setRealName(String realName) {
        AccountHelper.realName = realName;
    }

    public static void setIdcardno(String idcardno) {
        AccountHelper.idcardno = idcardno;
    }

    public static String getRealName(Context context) {
        String realname = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_REALNAME);
        if (TextUtils.isEmpty(realName))
            realName = realname;
        return realName;
    }

    public static String getIdcardno(Context context) {
        String idCardno = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_IDCARD);
        if (TextUtils.isEmpty(idcardno))
            idcardno = idCardno;
        return idcardno;
    }

    public static void clearAllUserData(Context context){
        token = null;
        username = null;
        uid = null;
        realName = null;
        idcardno = null;

        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_TOKEN,"");
        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_UID,"");

        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_REALNAME,"");
        SharePreferenceUtil.getInstance(context).setString(CommonConst.PARAM_MOBILE,"");
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_STATUS,-1);
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_TYPE,-1);
        SharePreferenceUtil.getInstance(context).setInt(CommonConst.PARAM_ACTIVE,-1);
        
        DaoActivity.strAccountName = "";
        DaoActivity.strAccountPwd = "";

    }

    public static boolean hasLogin(Context context){
        String token = SharePreferenceUtil.getInstance(context).getString(CommonConst.PARAM_TOKEN);
        if (TextUtils.isEmpty(token)){
            return false;
        }else{
            return true;
        }
    }

    public static boolean hasAuth(Context context){
        try{
//            int status = getStatus(context);
            AccountDao mAccountDao = new AccountDao(context);
            int status = mAccountDao.getLoginAccount().getStatus();
            if (2==status || 3 == status || 4 == status){//0：未实名认证；1：已实名认证
                return true;
            }
        }catch (Exception e ){
            return false;
        }

        return false;
    }

}
