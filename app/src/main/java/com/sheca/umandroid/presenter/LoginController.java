package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Account;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private String responseStr;

    public void setLogout(Context context){
        AccountHelper.clearAllUserData(context);

        AccountDao accountDao = new AccountDao(context);

        com.sheca.umandroid.model.Account curAct = accountDao.getLoginAccount();
        if (null == curAct){
            return;
        }
        curAct.setStatus(-1);   //重置登录状态为未登录状态
        curAct.setCopyIDPhoto("");
        accountDao.update(curAct);
    }

    public Account getPersonInfo(final Activity act, final String token, final String username){
        final UniTrust uniTrust = new UniTrust(act, false);

        Account result = null;


        Future<Account> future =
                threadPool.submit(
                        new Callable<Account>() {
                            public Account call() throws Exception {

//                                String params = ParamGen.getPersonalInfoParams(token);
//                                String responseStr = uniTrust.getPersonalInfo(params);

                                String params = ParamGen.getAccountParams(token,username);

                                Account account = null;
                                try{
                                    account = uniTrust.getAcountEx(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_personal), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                return account;
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



    public String getPersonActiveInfo(final Activity act,final String token,final String username){
        final UniTrust uniTrust = new UniTrust(act, false);

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {


                            public String call() throws Exception {

                                String params = ParamGen.getPersonalInfoParams(token);
                                 responseStr = uniTrust.getPersonalInfo(params);
//
//                                String params = ParamGen.getAccountParams(token,username);
//                                Account account = uniTrust.getAcount(params);

                                return responseStr;
                            }
                        }
                );

        try {
            responseStr = future.get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        return responseStr;
    }
}
