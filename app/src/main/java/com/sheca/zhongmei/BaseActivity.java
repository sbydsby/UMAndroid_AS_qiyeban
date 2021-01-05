package com.sheca.zhongmei;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity {

    private ProgressDialog progress;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintResource(R.color.bg_red);//通知栏所需颜色
//        }
	}

	@TargetApi(19)   
    private void setTranslucentStatus(boolean on) {  
        Window win = getWindow();  
        WindowManager.LayoutParams winParams = win.getAttributes();  
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;  
        if (on) {  
            winParams.flags |= bits;  
        } else {  
            winParams.flags &= ~bits;  
        }  
        win.setAttributes(winParams);  
    }

    public void showProgDialog(String msg){
        progress=new ProgressDialog(this);
        progress.setMessage(msg);
        progress.setCancelable(true);
        progress.show();
    }
    public void closeProgDialog(){
        if (null != progress && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeProgDialog();
    }
}
