package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sheca.umandroid.R;
import com.sheca.umandroid.dao.AccountDao;
import com.sheca.umandroid.model.APPResponse;
import com.sheca.umandroid.model.Account;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.CommonConst;
import com.sheca.umandroid.util.ParamGen;
import com.sheca.umandroid.util.SharePreferenceUtil;
import com.sheca.umplus.dao.UniTrust;

import net.sf.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.excelsecu.util.LibUtil.getApplicationContext;

public class AuthController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public void faceAuth(final Activity act, final boolean needPay) {
        final UniTrust uniTrust = new UniTrust(act, false);
        uniTrust.setFaceAuth(true);
        uniTrust.setFaceAuthActionNumber(3);

        String result = null;
        Future<String> future =threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String info = ParamGen.getFaceAuth(getApplicationContext());

                                String responseStr = null;
                                try {
                                    responseStr = uniTrust.FaceAuth(info);
                                } catch (Exception e) {
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();

                                        }
                                    });
                                    e.printStackTrace();
                                }

                                return responseStr;
                            }
                        }
                );

        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();

                }
            });
        }
        final String finalResult = result;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("unitrust", finalResult);
                APPResponse response = new APPResponse(finalResult);
                final int retCode = response.getReturnCode();

                if (retCode != com.sheca.umplus.util.CommonConst.RETURN_CODE_OK) {//不成功
                    Toast.makeText(act, act.getString(R.string.auth_fail), Toast.LENGTH_LONG).show();

                } else {

                    //人脸验证成功
                    JSONObject jbRet = response.getResult();
                    String personName = jbRet.getString("personName");
                    String personId = jbRet.getString("personID");
                    AccountHelper.setRealName(personName);
                    AccountHelper.setIdcardno(personId);
                    String authMsg = AccountHelper.getUsername(getApplicationContext());
                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_HAS_AUTH, authMsg);
                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_REALNAME, jbRet.getString("personName"));
                    SharePreferenceUtil.getInstance(act.getApplicationContext()).setString(CommonConst.PARAM_IDCARD, jbRet.getString("personID"));

                    AccountDao mAccountDao = new AccountDao(act.getApplicationContext());
                    Account acount = mAccountDao.getLoginAccount();
                    acount.setStatus(2);
                    acount.setIdentityName(personName);
                    acount.setIdentityCode(personId);

                    mAccountDao.update(acount);

                    if (needPay) {
                        Intent intent = new Intent();
                        intent.setClass(act, com.sheca.umandroid.PayActivity.class);
                        intent.putExtra("loginAccount", AccountHelper.getRealName(act));
                        intent.putExtra("loginId", AccountHelper.getIdcardno(act));
                        act.startActivity(intent);

                    } else {
//                            setAccountVal(personName, personId);
                    }
                }
            }
        });

    }


}
