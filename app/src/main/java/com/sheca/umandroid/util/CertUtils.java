package com.sheca.umandroid.util;

import android.app.Activity;
import android.util.Log;

import com.sheca.umandroid.interfaces.CertCallBack;
import com.sheca.umandroid.model.CertOperateParams;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.SealInfo;

import java.util.List;

/**
 * @author xuchangqing
 * @time 2019/4/18 21:38
 * @descript 证书操作工具类
 */
public  class CertUtils {

    private Activity mActivity;
    private String strInfo="";
    private String mStrVal="";
    private UniTrust mUnitTrust=null;
    private List<Cert> mCertList;
    private List<SealInfo> mSealList;

    public CertUtils(Activity mActivity, CertOperateParams certOperateParams, CertEnum mOperation,
                     CertCallBack mCallBack){
        this.mActivity=mActivity;
        excuteTask(mOperation,certOperateParams,mCallBack);
        getUniTrustInstance(mActivity);
    }

    /**
     *UnitTrust 实例化
     * @return
     */
    private UniTrust getUniTrustInstance(Activity mActivity){
        synchronized (CertUtils.class){
            if(mUnitTrust == null){
                synchronized (CertUtils.class){
                     mUnitTrust=new UniTrust(mActivity,false);
                }
            }
        }
        return mUnitTrust;

    }

    /**
     * 线程操作
     * @param mOperation
     * @param certOperateParams
     * @param mCallBack
     *
     */
    private void excuteTask(final CertEnum mOperation, final CertOperateParams certOperateParams, final CertCallBack mCallBack){
        try {
        new MyAsycnTaks(){
            @Override
            public void preTask() {
                Log.e("TEST_DATABASE","登录界面同步开始执行3.");
                initParams(mOperation,certOperateParams);
                Log.e("TEST_DATABASE","登录界面同步开始执行3."+mOperation);
            }

            @Override
            public void doinBack() {
                doinThread(mOperation);
            }
            @Override
            public void postTask() {
                doinMainThread(mCallBack);
            }
        }.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化参数
     * @param mOperation
     * @param certOperateParams
     */
    private void initParams(CertEnum mOperation, CertOperateParams certOperateParams) {
        switch (mOperation){

            case GetCertList: case GetSealList:
                String mTokenID = SharePreferenceUtil.getInstance(mActivity.getApplicationContext()).getString(CommonConst.PARAM_TOKEN);
//                strInfo = ParamGen.getEnumCertIDs(mTokenID);
                String mAccountName = certOperateParams.getAccountName();
                strInfo = ParamGen.getAcountAllCerts(mTokenID,mAccountName);
                Log.e("TEST_DATABASE","登录界面同步开始执行4."+strInfo);
                break;
            case GetCertByID:
                String mCertID = certOperateParams.getCertID();
                strInfo=ParamGen.getCertByIdParams(mCertID);
                break;
            case GetCertItem:
                String mCert = certOperateParams.getCert();
                String mItemNo = certOperateParams.getItemNo();
                strInfo= ParamGen.getCertItemParams(mCert,mItemNo);
                break;

        }

    }

    /**
     * 子线程运行
     * @param mOperation
     */
    private void doinThread(CertEnum mOperation) {
        switch (mOperation){
            case GetCertList:
//                 mStrVal = mUnitTrust.enumCertIDs(strInfo);
                   mCertList = mUnitTrust.getAcountAllCerts(strInfo);
                Log.e("TEST_DATABASE","登录界面同步开始执行4."+mCertList);
                Log.e("TEST_DATABASE","登录界面同步开始执行5."+mCertList.size());
                break;
            case GetSealList:
//                mStrVal = mUnitTrust.enumSeals(strInfo);
                 mSealList= mUnitTrust.getAcountAllSealInfos(strInfo);
                break;
            case GetCertByID:
                mStrVal = mUnitTrust.GetCertByID(strInfo);
                break;
            case GetCertItem:
                mStrVal= mUnitTrust.GetCertItem(strInfo);
                break;
            case GetCertExt:
                mStrVal= mUnitTrust.GetCertExt(strInfo);
                break;

        }

    }

    /**
     * 主线程运行
     *
     * @param mCallBack
     */
    private void doinMainThread( CertCallBack mCallBack) {
//        mCallBack.certCallBack(mStrVal);
        Log.e("TEST_DATABASE","登录界面同步开始执行6."+mCertList+"");
        if(mCertList!=null){
             mCallBack.certCallBackforList(mCertList);
            Log.e("TEST_DATABASE","登录界面同步开始执行6."+mCertList.size());
        }

         if(mSealList!=null){
             mCallBack.sealCallBackfoirList(mSealList);
         }


        }
    }





