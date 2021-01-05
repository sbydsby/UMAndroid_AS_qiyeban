package com.sheca.zhongmei.util;


import android.os.Handler;
import android.os.Looper;

/**
 * @author xuchangqing
 * @time 2019/2/25 14:29
 * @descript
 */
public abstract class MyAsycnTaks {
    private Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(android.os.Message msg) {

            postTask();

        }
    };
    /**
     * 在子线程之前执行的方法
     */
    public abstract void preTask();
    /**
     * 在子线程之中执行的方法
     */
    public abstract void doinBack();
    /**
     * 在子线程之后执行的方法
     */
    public abstract void postTask();
    /**
     * 执行
     */
    public void execute(){
        preTask();
        new Thread(){
            public void run() {
                doinBack();
                handler.sendEmptyMessage(0);

            }
        }.start();
    }

}
