package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.sheca.umandroid.R;
import com.sheca.umandroid.companyCert.ICallback;
import com.sheca.umandroid.model.SealInfo;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SealController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();


    private static SealController sealController;

    public static SealController getInstance() {
//        if (sealController == null) {
        sealController = new SealController();
//        }
        return sealController;
    }

    public SealInfo getAccountSealInfoBySN(final Activity act, final String sealid, final String accountName){
        final UniTrust uniTrust = new UniTrust(act, false);

        SealInfo result = null;


        Future<SealInfo> future =
                threadPool.submit(
                        new Callable<SealInfo>() {
                            public SealInfo call() throws Exception {
                                String params = ParamGen.getSealBySN(accountName,sealid, AccountHelper.getToken(act.getApplicationContext()));

                                com.sheca.umplus.model.SealInfo sealPlus = null;
                                try{
                                    sealPlus = uniTrust.getAccountSealInfoByID(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_accountseal), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                Log.d("unitrustseal",sealPlus.toString()+"   ");

                                SealInfo sealInfo = new SealInfo();
//                                sealInfo.setId(sealPlus.getId());
                                sealInfo.setSdkID(sealPlus.getId());
                                sealInfo.setVid(sealPlus.getVid());
                                sealInfo.setSealname(sealPlus.getSealname());
                                sealInfo.setSealsn(sealPlus.getSealsn());
                                sealInfo.setIssuercert(sealPlus.getIssuercert());
                                sealInfo.setCert(sealPlus.getCert());
                                sealInfo.setPicdata(sealPlus.getPicdata());
                                sealInfo.setPictype(sealPlus.getPictype());
                                sealInfo.setPicwidth(sealPlus.getPicwidth());
                                sealInfo.setPicheight(sealPlus.getPicheight());
                                sealInfo.setNotbefore(sealPlus.getNotbefore());
                                sealInfo.setNotafter(sealPlus.getNotafter());
                                sealInfo.setSignal(sealPlus.getSignal());
                                sealInfo.setExtensions(sealPlus.getExtensions());
                                sealInfo.setAccountname(sealPlus.getAccountname());
                                sealInfo.setCertsn(sealPlus.getCertsn());

                                return sealInfo;
                            }
                        }
                );

        try {
            result = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }




    public void applySeal(final Activity act, final String picData, final String idNumber, final String CERT_ID, final String CERT_PWD, final String sealName, final String companyName,ICallback iCallback) {
        final UniTrust uniTrust = new UniTrust(act, false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                String result = uniTrust.ApplySeal(ParamGen.getApplySeal(act.getApplicationContext(), picData, idNumber, CERT_ID, CERT_PWD, sealName, companyName));
                iCallback.onCallback(result);
            }
        }).start();

    }


    public String applySeal(final Activity act, final String picData, final String CERT_ID, final String CERT_PWD){

        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getApplySeal(act.getApplicationContext(),picData,CERT_ID,CERT_PWD);

                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.MakeSeal(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_applyseal), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                Log.d("unitrust",responseStr);

                                return responseStr;
                            }
                        }
                );

        try {
            result = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    public void makeSeal(final Activity act, final String picData, final String idNumber, final String CERT_ID, final String CERT_PWD, final String sealName, final String companyName, String picType, ICallback iCallback) {
        final UniTrust uniTrust = new UniTrust(act, false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                String result = uniTrust.MakeSeal(ParamGen.makeApplySeal(act.getApplicationContext(), picData, idNumber, CERT_ID, CERT_PWD, sealName, companyName,picType));
                iCallback.onCallback(result);
            }
        }).start();

    }
}
