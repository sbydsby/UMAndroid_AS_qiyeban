package com.sheca.zhongmei.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sheca.zhongmei.R;
import com.sheca.zhongmei.companyCert.ICallback;
import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.MyAsycnTaks;
import com.sheca.zhongmei.util.ParamGen;
import com.sheca.umplus.dao.UniTrust;
import com.sheca.umplus.model.Cert;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CertController {

    ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private String responseStr="";

    public com.sheca.zhongmei.model.Cert convertCert(Cert certPlus){
        com.sheca.zhongmei.model.Cert cert = new com.sheca.zhongmei.model.Cert();

//        cert.setId(certPlus.getId());
        cert.setSdkID(certPlus.getId());
        cert.setCertsn(certPlus.getCertsn());
        cert.setEnvsn(certPlus.getEnvsn());
        cert.setPrivatekey(certPlus.getPrivatekey());
        cert.setCertificate(certPlus.getCertificate());
        cert.setKeystore(certPlus.getKeystore());
        cert.setEnccertificate(certPlus.getEnccertificate());
        cert.setEnckeystore(certPlus.getEnckeystore());
        cert.setCertchain(certPlus.getCertchain());
        cert.setStatus(certPlus.getStatus());
        cert.setAccountname(certPlus.getAccountname());
        cert.setNotbeforetime(certPlus.getNotbeforetime());
        cert.setValidtime(certPlus.getValidtime());
        cert.setUploadstatus(certPlus.getUploadstatus());
        cert.setCerttype(certPlus.getCerttype());
        cert.setSignalg(certPlus.getSignalg());
        cert.setContainerid(certPlus.getContainerid());
        cert.setAlgtype(certPlus.getAlgtype());
        cert.setSavetype(certPlus.getSavetype());
        cert.setDevicesn(certPlus.getDevicesn());
        cert.setCertname(certPlus.getCertname());
        cert.setCerthash(certPlus.getCerthash());
        cert.setFingertype(certPlus.getFingertype());
        cert.setSealsn(certPlus.getSealsn());
        cert.setSealstate(certPlus.getSealstate());

        return cert;
    }

    public Cert getCertDetailandSave(final Activity act, final String certid){
        final UniTrust uniTrust = new UniTrust(act, false);

        Cert result = null;


        Future<Cert> future =
                threadPool.submit(
                        new Callable<Cert>() {
                            public Cert call() throws Exception {

                                String params = ParamGen.getAccountCertByID(
                                        AccountHelper.getToken(act.getApplicationContext()),
                                        AccountHelper.getUsername(act.getApplicationContext()),
                                        certid);
                                
                                Cert responseStr = null;

                                try{
                                    responseStr = uniTrust.getAccountCertByID(params);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_getaccount), Toast.LENGTH_LONG).show();
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

    public String scan(final Activity act, final String qrcode) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getScanParams(AccountHelper.getToken(act.getApplicationContext()), qrcode);

                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.ScanUI(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_scan), Toast.LENGTH_LONG).show();
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

    /**
     * 扫描
     * @return
     */
    public String useSdkScan(final Activity act, final String qrcode){
        new MyAsycnTaks(){
            private String params;
            @Override
            public void preTask() {
                 params = ParamGen.getScanParams(AccountHelper.getToken(act.getApplicationContext()), qrcode);
            }
            @Override
            public void doinBack() {
                try {
                    UniTrust uniTrust = new UniTrust(act, false);
                    responseStr = uniTrust.ScanUI(params);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }
            @Override
            public void postTask() {}
        }.execute();
        return responseStr;
    }

    public void getCertInfoList(final Activity act, final String token, ICallback callback) {
        final UniTrust uniTrust = new UniTrust(act, false);


        String params = ParamGen.getCertInfoListParams(AccountHelper.getToken(act.getApplicationContext()));
        new Thread(new Runnable() {
            @Override
            public void run() {

                String result =uniTrust.GetCertInfoList(params);
                callback.onCallback(result);
            }
        }).start();

//        String result = null;
//
//        Future<String> future =
//                threadPool.submit(
//                        new Callable<String>() {
//                            public String call() throws Exception {
//
//                                String params = ParamGen.getCertInfoListParams(AccountHelper.getToken(act.getApplicationContext()));
//
//                                String responseStr =null;
//                                try{
//                                    responseStr = uniTrust.GetCertInfoList(params);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//
//                                    act.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(act, act.getString(R.string.fail_certlist), Toast.LENGTH_LONG).show();
//                                        }
//                                    });
//                                }
//
//                                return responseStr;
//                            }
//
//                            ;
//                        }
//                );
//
//        try {
//            result = future.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return result;
    }

    public String downloadCert(final Activity act, final String requestNumber, final String certType,final String certName, final String pwd) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getDownloadCertParams(AccountHelper.getToken(act.getApplicationContext()),
                                        requestNumber,
                                        certType, TextUtils.isEmpty(certName)?AccountHelper.getRealName(act.getApplicationContext()):certName,pwd);

                                Log.d("unitrust",params);

                                String responseStr = null;
                                try{
                                    uniTrust.setPwdHash(true);
                                    responseStr = uniTrust.DownloadCertNew(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_download), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                return responseStr;
                            }

                            ;
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

    public String renewCert(final Activity act, final String info) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {



                                Log.d("unitrust",info);

                                String responseStr = null;
                                try{
//                                    responseStr = uniTrust.RenewCert(info);
                                    responseStr = uniTrust.RenewCertNew(info);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_renew), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                return responseStr;
                            }

                            ;
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

    public String applyCert(final Activity act, final String realName, final String idcard, final String certType,final String psdhash) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getApplyCertParams(act.getApplicationContext(),
                                        certType, realName, idcard,psdhash);

                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.ApplyCert(params);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.applycert_fail), Toast.LENGTH_LONG).show();

                                        }
                                    });
                                }

                                return responseStr;
                            }

                            ;
                        }
                );

        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, act.getString(R.string.applycert_fail), Toast.LENGTH_LONG).show();

                }
            });
        }

        return result;
    }


    //申请证书
    public void applyNewCert(final Activity act, final String realName, final String idcard, final String certType, final String psdhash, final int time, int certlevel, String requestnumber, ICallback callBack) {
        final UniTrust uniTrust = new UniTrust(act, false);
        String params = ParamGen.getApplyCertParams(act.getApplicationContext(),
                certType, realName, idcard, psdhash, time, certlevel, requestnumber);
        new Thread(new Runnable() {
            @Override
            public void run() {

                String result = uniTrust.ApplyOrgCert(params);
                callBack.onCallback(result);
            }
        }).start();


    }



    public void applyCertLite(final Activity act, final String realName, final String idcard, final String certType, final String psdhash, final int time,final String picData) {
        final UniTrust uniTrust = new UniTrust(act, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String params = ParamGen.getApplyCertLiteParams(act.getApplicationContext(),
                        certType, realName, idcard, psdhash, time,picData);
                String result = uniTrust.ApplyOrgCertLite(params);
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                     Toast.makeText(act,result,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();


    }



}
