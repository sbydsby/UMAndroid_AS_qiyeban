package com.sheca.zhongmei.presenter;

import android.app.Activity;
import android.widget.Toast;


import com.sheca.zhongmei.interfaces.ResponseCallback;
import com.sheca.zhongmei.model.APPResponse;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.util.CommonConst;

import net.sf.json.JSONObject;

/**
 * @author xuchangqing
 * @time 2020/4/30 14:37
 * @descript V33新账户体系
 */
public class AccountController {


    /**
     * 判断账户是否存在接口
     *
     * @param mobile
     * @param name
     * @param idNo
     */
    public void accountIsExicted(Activity context, String mobile, String name, String idNo, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String responseStr = "";
            private int retCode =0;
            private String retResult = "";
            private String retMsg = "";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.getCheckIsAccountExistedParams(mobile, idNo);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.IsAccountExisted(strInfo);

                APPResponse response = new APPResponse(responseStr);
              retCode = response.getReturnCode();
                retMsg = response.getReturnMsg();

                if (retCode==0) {
                    JSONObject res=JSONObject.fromObject(response.getResultStr());
                    retResult = res.optString("exist");
                }
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode+"", retMsg, retResult);
            }
        }.execute();
    }

    //登录
    public void accountLogin(Activity context, String mobile, String pwd, String name, String idNo, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String strMsg = "";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(mobile, pwd, idNo));
            }

            @Override
            public void postTask() {
                APPResponse response = new APPResponse(strMsg);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                final String tokenID;

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                    JSONObject jbRet = response.getResult();
                    tokenID = jbRet.optString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                    AccountHelper.clearAllUserData(context);
                    AccountHelper.savePersonInfoToLocal(context, tokenID, mobile);

                } else {
                    callback.responseCallback(retCode + "", retMsg, "");

                }
            }
        }.execute();
    }

    //登录
    public void accountLogin(Activity context, String mobile, String pwd, String name, String idNo) {
        new MyAsycnTaks() {
            private String strMsg = "";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(mobile, pwd, idNo));
            }

            @Override
            public void postTask() {
                APPResponse response = new APPResponse(strMsg);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                final String tokenID;

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                    JSONObject jbRet = response.getResult();
                    tokenID = jbRet.optString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);

                    AccountHelper.setNeedChangePwd(context,false);
                    AccountHelper.clearAllUserData(context);
                    AccountHelper.savePersonInfoToLocal(context, tokenID, mobile);

                } else {
                    Toast.makeText(context, retMsg + retCode, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    //验证码登录
    public void smsLogin(Activity context, String mobile, String name, String idNo, String sms) {
        new MyAsycnTaks() {
            private String strMsg = "";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByMACNew(ParamGen.getUserLoginByValidationCodeNewParams(mobile, sms,idNo));
            }

            @Override
            public void postTask() {
                APPResponse response = new APPResponse(strMsg);
                int retCode = response.getReturnCode();
                final String retMsg = response.getReturnMsg();
                final String tokenID;

                if (retCode == com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {
                    JSONObject jbRet = response.getResult();
                    tokenID = jbRet.optString(com.sheca.umplus.util.CommonConst.RESULT_PARAM_TOKENID);
                    AccountHelper.setNeedChangePwd(context,true);
                    AccountHelper.clearAllUserData(context);
                    AccountHelper.savePersonInfoToLocal(context, tokenID, mobile);

                } else {
                    Toast.makeText(context, retMsg + retCode, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    /**
     * 账户注册接口
     *
     * @param mobile
     * @param name
     * @param idNo
     */
    public void registerAccount(Activity context, String mobile, String name, String idNo, String pwd, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String responseStr = "";
            private String retCode = "";
            private String retResult = "";
            private String retMsg = "";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.registerAccountParams(mobile, name, idNo, pwd);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.RegisterAccount(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode, retMsg, retResult);
            }
        }.execute();
    }


    /**
     * 获取短信验证码(注册)
     *
     * @param mobile
     */
    public void getMac(Activity context, String mobile, String type, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String responseStr = "";
            private String retCode = "";
            private String retResult = "";
            private String retMsg = "";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.getValidationCodeParams(mobile, type);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.GetMAC(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
                if (retCode.equals("0")) {
                    JSONObject jbResult = jb.getJSONObject(CommonConst.RETURN_RESULT);

                    retResult = jbResult.optString("MAC");

                } else {
                }
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode, retMsg, retResult);
            }
        }.execute();
    }


    /**
     * 验证短信验证码
     *
     * @param mobile
     */
    public void verifyMac(Activity context, String mobile, String type, String mac, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String responseStr = "";
            private String retCode = "";
            private String retResult = "";
            private String retMsg = "";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.verifyMac(mobile, type, mac);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.VerifyMAC(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode, retMsg, retResult);
            }
        }.execute();
    }

    /**
     * 重置账户密码
     *
     * @param mobile
     */
    public void resetPWD(Activity context, String mobile, String pwd, ResponseCallback callback) {
        new MyAsycnTaks() {
            private String responseStr = "";
            private String retCode = "";
            private String retResult = "";
            private String retMsg = "";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.resetPWD(mobile, pwd);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.SetNotLoginAccountPassword(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
                if (retCode.equals("0")) {
                    retResult = jb.optString(CommonConst.RETURN_RESULT);
                }
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode, retMsg, retResult);
            }
        }.execute();
    }


}
