package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.widget.Toast;


import com.sheca.umandroid.interfaces.ResponseCallback;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
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
     * @param mobile
     * @param name
     * @param idNo
     */
    public void accountIsExicted(Activity context, String mobile, String name, String idNo, ResponseCallback callback){
        new MyAsycnTaks(){
            private String responseStr="";
            private String retCode="";
            private String  retResult="";
            private String retMsg="";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.getCheckIsAccountExistedParams(mobile,idNo);
                UniTrust dao = new UniTrust(context, false);
                 responseStr = dao.IsAccountExisted(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                  retCode = jb.getString(CommonConst.RETURN_CODE);
                  retResult = jb.getString(CommonConst.RETURN_RESULT) ;
                  retMsg = jb.getString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode,retMsg,retResult);
            }
        }.execute();
    }

    //登录
    public void accountLogin(Activity context,String mobile, String pwd,String name, String idNo, ResponseCallback callback){
        new MyAsycnTaks(){
            private String strMsg="";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(mobile, pwd,idNo));
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
                    AccountHelper.savePersonInfoToLocal(context,tokenID,mobile);

                }else{
                    callback.responseCallback(retCode+"",retMsg,"");

                }
            }
        }.execute();
    }

    //登录
    public void accountLogin(Activity context,String mobile, String pwd,String name, String idNo){
        new MyAsycnTaks(){
            private String strMsg="";

            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                UniTrust uniTrust = new UniTrust(context, false);
                strMsg = uniTrust.LoginByPWD(ParamGen.getLoginByPassword(mobile, pwd,idNo));
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
                    AccountHelper.savePersonInfoToLocal(context,tokenID,mobile);

                }else{
                    Toast.makeText(context, retMsg+retCode, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * 账户注册接口
     * @param mobile
     * @param name
     * @param idNo
     */
    public void registerAccount(Activity context, String mobile, String name, String idNo,String pwd, ResponseCallback callback){
        new MyAsycnTaks(){
            private String responseStr="";
            private String retCode="";
            private String retResult="";
            private String retMsg="";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.registerAccountParams(mobile,name,idNo,pwd);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.RegisterAccount(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode,retMsg,retResult);
            }
        }.execute();
    }


    /**
     * 获取短信验证码(注册)
     * @param mobile
     */
    public void getMac(Activity context, String mobile,String type, ResponseCallback callback){
        new MyAsycnTaks(){
            private String responseStr="";
            private String retCode="";
            private String retResult="";
            private String retMsg="";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.getValidationCodeParams(mobile,type);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.GetMAC(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode,retMsg,retResult);
            }
        }.execute();
    }


    /**
     * 验证短信验证码
     * @param mobile
     */
    public void verifyMac(Activity context, String mobile,String type,String mac, ResponseCallback callback){
        new MyAsycnTaks(){
            private String responseStr="";
            private String retCode="";
            private String retResult="";
            private String retMsg="";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.verifyMac(mobile,type,mac);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.VerifyMAC(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode,retMsg,retResult);
            }
        }.execute();
    }

    /**
     * 重置账户密码
     * @param mobile
     */
    public void resetPWD(Activity context, String mobile,String pwd, ResponseCallback callback){
        new MyAsycnTaks(){
            private String responseStr="";
            private String retCode="";
            private String retResult="";
            private String retMsg="";


            @Override
            public void preTask() {
            }

            @Override
            public void doinBack() {
                final String strInfo = ParamGen.resetPWD(mobile,pwd);
                UniTrust dao = new UniTrust(context, false);
                responseStr = dao.SetNotLoginAccountPassword(strInfo);
                JSONObject jb = JSONObject.fromObject(responseStr);
                retCode = jb.optString(CommonConst.RETURN_CODE);
                retResult = jb.optString(CommonConst.RETURN_RESULT);
                retMsg = jb.optString(CommonConst.RETURN_MSG);
            }

            @Override
            public void postTask() {
                callback.responseCallback(retCode,retMsg,retResult);
            }
        }.execute();
    }


}
