package com.sheca.umee.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.sheca.umee.util.UpdateUtil;

import java.io.File;

public class ReplaceBroadcastReceiver extends BroadcastReceiver {
	//private final String APP_NAME = "UniTrust.apk";  //apk安装包名称
	//private static final String TAG="ApkDelete";
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		File downLoadApk = new File(Environment.getExternalStorageDirectory(),
				UpdateUtil.APP_NAME);
		if(downLoadApk.exists()){
			downLoadApk.delete();
		}
		//Log.i(TAG, "downLoadApkFile was deleted!");
	}

}
