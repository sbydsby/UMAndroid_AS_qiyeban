package com.sheca.umee.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;

public class AlertDialogUtil {

    private static AlertDialogUtil mAlertDialogUtil;
    public static AlertDialog.Builder builder;

    public void showAlertDialog(int title,
            @Nullable int setMessage,
                                int setNegativeButton, int setPositiveButton,
                                DialogInterface.OnClickListener DialogInterface1,
                                DialogInterface.OnClickListener   DialogInterface2){
        builder.setTitle(title);
        builder.setMessage(setMessage);
        builder.setNegativeButton(setNegativeButton, DialogInterface1);
        builder.setPositiveButton(setPositiveButton,DialogInterface2 );
        builder.setCancelable(false);
        builder.show();
    }

    public void showAlertDialog(int title,
            @Nullable int setMessage,
                                int setPositiveButton,
                                DialogInterface.OnClickListener DialogInterface1){

        builder.setTitle(title);
        builder.setMessage(setMessage);
        builder.setPositiveButton(setPositiveButton,DialogInterface1 );
        builder.setCancelable(false);
        builder.show();
    }

    public void showAlertDialog(int title,
                                @Nullable String setMessage,
                                int setPositiveButton,
                                DialogInterface.OnClickListener DialogInterface1){

        builder.setTitle(title);
        builder.setMessage(setMessage);
        builder.setPositiveButton(setPositiveButton,DialogInterface1 );
        builder.setCancelable(false);
        builder.show();
    }

    public static AlertDialogUtil getInstance(Context mContent){
        if (mAlertDialogUtil==null){
            synchronized(AlertDialogUtil.class){
                if (mAlertDialogUtil==null)
                    mAlertDialogUtil=new AlertDialogUtil();
                builder = new AlertDialog.Builder(mContent);
            }
        }
        return mAlertDialogUtil;
    }
}
