package com.sheca.zhongmei.presenter;

import android.app.ProgressDialog;
import android.content.Context;

public class BasePresenter {

    protected ProgressDialog progDialogCert = null;
    protected Context context = null;

    public BasePresenter(Context context){
        this.context = context;
    }

    public void showProgDlgCert(String strMsg) {
        if (null == progDialogCert)
            progDialogCert = new ProgressDialog(context);
        progDialogCert.setMessage(strMsg);
        progDialogCert.setCancelable(false);
        progDialogCert.show();
    }

    public void changeProgDlgCert(String strMsg) {
        if (progDialogCert.isShowing()) {
            progDialogCert.setMessage(strMsg);
        }
    }


    public void closeProgDlgCert() {
        if (null != progDialogCert && progDialogCert.isShowing()) {
            progDialogCert.dismiss();
            progDialogCert = null;
        }
    }
}
