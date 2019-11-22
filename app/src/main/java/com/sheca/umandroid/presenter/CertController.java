package com.sheca.umandroid.presenter;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.sheca.umandroid.R;
import com.sheca.umandroid.util.AccountHelper;
import com.sheca.umandroid.util.MyAsycnTaks;
import com.sheca.umandroid.util.ParamGen;
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

    public com.sheca.umandroid.model.Cert convertCert(Cert certPlus){
        com.sheca.umandroid.model.Cert cert = new com.sheca.umandroid.model.Cert();

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

    public String getCertInfoList(final Activity act, final String token) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getCertInfoListParams(AccountHelper.getToken(act.getApplicationContext()));

                                String responseStr =null;
                                try{
                                    responseStr = uniTrust.GetCertInfoList(params);
                                }catch (Exception e){
                                    e.printStackTrace();

                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(act, act.getString(R.string.fail_certlist), Toast.LENGTH_LONG).show();
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

    public String downloadCert(final Activity act, final String requestNumber, final String certType) {
        final UniTrust uniTrust = new UniTrust(act, false);

        String result = null;

        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {

                                String params = ParamGen.getDownloadCertParams(AccountHelper.getToken(act.getApplicationContext()),
                                        requestNumber,
                                        certType,
                                        AccountHelper.getRealName(act.getApplicationContext()));

                                Log.d("unitrust",params);

                                String responseStr = null;
                                try{
                                    responseStr = uniTrust.DownloadCert(params);
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
                                    responseStr = uniTrust.RenewCert(info);
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
}
