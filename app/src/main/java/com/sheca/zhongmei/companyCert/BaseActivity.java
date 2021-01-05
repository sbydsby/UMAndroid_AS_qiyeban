package com.sheca.zhongmei.companyCert;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * @author xuchangqing
 * @time 2019/7/9 13:55
 * activity 基类
 */
public class BaseActivity extends Activity {

//    private PermissionUtil mPermissionUtil;
//    public ProgressDialog progDialog;

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
//        overridePendingTransition(R.anim.fade_out,R.anim.fade_in);
    }

    @Override
    public void finish() {
//        dismissDg();
        super.finish();
//        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!CommUtil.isNetWorkAvailable(this)) {
//            Toast.makeText(this,"网络连接异常,请检查网络连接",Toast.LENGTH_LONG).show();
//        }
    }

    //全屏设置
    public void setFullScreen() {
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    //导航栏颜色设置（设备号需大于5.0）
    public void setNavigatColor(int color) {
        //设置底部导航栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(color);
        }
    }

    public void gotoNextActivity(int type) {
    }

    public void showTips() {
    }





    public static LinearLayout mFloatLayout;

    public static WindowManager.LayoutParams wmParams;
    public static WindowManager mWindowManager;

//    public void showFloatView(Activity activity) {
//        showDg();
//
//    }
//
//
//    public void disMissFloat() {
//        dismissDg();
////        try {
////            if (mFloatLayout != null && mFloatLayout.isAttachedToWindow()) {
////                mWindowManager.removeView(mFloatLayout);
////            }
////        } catch (Exception e) {
////        }
//
//
//    }


    @Override
    protected void onDestroy() {
//        dismissDg();
        super.onDestroy();

//        if (mFloatLayout != null && mFloatLayout.isAttachedToWindow()) {
//            mWindowManager.removeView(mFloatLayout);
//        }
//        wmParams=null;


    }

//    public void showDg() {
////        showProgDlg("请稍候...");
//
//        if (dialog == null) {
//            dialog = new MyDialog(this);
//            DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            };
//            dialog.setOnKeyListener(keylistener);
//        }
//        LayoutInflater inflater = getLayoutInflater();
//        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_launch_activity, null);
//
//        ((GifImageView) layout.findViewById(R.id.launch_loading)).setBackgroundResource(R.drawable.launchloading);
//
//
//        dialog.show();
//        dialog.setCancelable(false);
//        dialog.setContentView(layout);// show方法要在前面
//
//    }
//
//
//    public void dismissDg() {
////        closeProgDlg();
//
//        if (dialog != null && dialog.isShowing()) {
//            dialog.dismiss();
//            dialog = null;
//        }
//
//    }
//
//    MyDialog dialog = null;
//
//
//    public class MyDialog extends AlertDialog {
//        Context mContext;
//
//        public MyDialog(Context context) {
//            super(context, R.style.FullDialog); // 自定义全屏style
//            this.mContext = context;
//        }
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//        }
//
//        @Override
//        public void show() {
//            super.show();
//            /**
//             * 设置宽度全屏，要设置在show的后面
//             */
//
//            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//            layoutParams.gravity = Gravity.BOTTOM;
//            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
////解决切换黑屏
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.dimAmount = 0f;
//
//            getWindow().getDecorView().setPadding(0, 0, 0, 0);
//            getWindow().setAttributes(layoutParams);
//        }
//    }
//
//
//    public void showProgDlg(String strMsg) {
//
//        showDg();
//
////        progDialog = new ProgressDialog(this);
////        progDialog.setCancelable(false);
////
//////        progDialog.setMessage(strMsg);
////        progDialog.setMessage("加载中...");
////        progDialog.show();
//    }
//
//    public void changeProgDlg(String strMsg) {
//        if (progDialog.isShowing()) {
//            progDialog.setMessage(strMsg);
//        }
//    }
//
//    public void closeProgDlg() {
//        dismissDg();
////        if (null != progDialog) {
////            progDialog.dismiss();
//////            progDialog = null;
////        }
//
//    }


}
