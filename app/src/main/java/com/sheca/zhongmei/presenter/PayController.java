package com.sheca.zhongmei.presenter;

import android.app.Activity;
import android.widget.Toast;

import com.sheca.zhongmei.R;
import com.sheca.zhongmei.interfaces.ThreadInterface;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PayController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public void getApplyCertRequest(final Activity act, final String token,
                                      final String name, final String idCardNo,
                                      final String certType, final String certExpire, final ThreadInterface threadInterface){

        new MyAsycnTaks(){
            String result;
            final UniTrust uniTrust = new UniTrust(act, false);

            String params = null;
            @Override
            public void preTask() {
                params = ParamGen.getApplyCertRequest(token,name,idCardNo,certType,certExpire);
            }

            @Override
            public void doinBack() {
                try{
                    result = uniTrust.getApplyCertRequest(params);
                    threadInterface.onReuslt(result);
                }catch (Exception e){
                    e.printStackTrace();

                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(act, act.getString(R.string.fail_payuni), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void postTask() {

            }
        }.execute();
    }

    public String getWeChatPayQueryOrder(final Activity act, final String reqNo){
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;


        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getWeChatPayQueryOrder(AccountHelper.getToken(act.getApplicationContext()),
                                        reqNo);
                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.weChatPayQueryOrder(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_payorder), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

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

    public String getWeChatPayUnifiedorder(final Activity act, final String reqNo){

        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getWeChatPayUnifiedorder(AccountHelper.getToken(act.getApplicationContext()),
                                        reqNo);

                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.weChatPayUnifiedorder(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_payuni), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

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
}
