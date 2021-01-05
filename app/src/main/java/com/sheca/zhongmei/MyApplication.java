package com.sheca.zhongmei;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * MyApplication
 * 
 * @author minking
 */
public class MyApplication extends Application {
	protected static MyApplication instance;  
	private String strErr ="";

    private static GetuiHandler handler;

    public static MainActivity mainActivityNew;
	
	@Override
	public void onCreate() {
		super.onCreate();

        try {
            closeAndroidPDialog();


            // 初始化ImageLoader
            @SuppressWarnings("deprecation")
            DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.icon_stub) // 设置图片下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.icon_empty) // 设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.icon_error) // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                    .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
                    // .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
                    .build(); // 创建配置过得DisplayImageOption对象

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options)
                    .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                    .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();
            ImageLoader.getInstance().init(config);

//            dexTool();

            instance = this;
            Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
        }catch(Exception ex){
            strErr = ex.getMessage();
            strErr += "\n"+ex.getLocalizedMessage();
        }
					
					/*
					 
						
					
					new Thread(new Runnable(){
						@Override
						public void run() {
							try{
								Thread.sleep(1000);
					
				} catch (Exception e) {
					//ShcaCciStd.gSdk = null;
					Log.e(CommonConst.TAG, e.getMessage(), e);
				}
				
			}
		}).start();
		*/

        if (handler == null) {//个推
            handler = new GetuiHandler();
        }
	}
    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }
    public static class GetuiHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
//                    payloadData.append((String) msg.obj);
//                    payloadData.append("\n");
                    if (mainActivityNew != null) {
//                        if (MainActivityNew.tLogView != null) {
//                            MainActivityNew.tLogView.append(msg.obj + "\n");
//                        }
                        mainActivityNew.showMsg(msg.obj+"");

                    }
                    break;

                case 1:
                    if (mainActivityNew != null) {
//                        if (MainActivityNew.tLogView != null) {
//                            MainActivityNew.tView.setText((String) msg.obj);
                        mainActivityNew.showMsg(msg.obj+"");
                    }
//                    }
                    break;
            }
        }
    }
	

    @SuppressLint("NewApi")
    private void dexTool() {
        File dexDir = new File(getFilesDir(), "dlibs");
        dexDir.mkdir();
        File dexFile = new File(dexDir, "libs.apk");
        File dexOpt = new File(dexDir, "opt");
        dexOpt.mkdir();
        try {
           InputStream ins = getAssets().open("libs.apk");
           if (dexFile.length() != ins.available()) {
               FileOutputStream fos = new FileOutputStream(dexFile);
               byte[] buf = new byte[4096];
               int l;
               while ((l = ins.read(buf)) != -1) {
                   fos.write(buf, 0, l);
               }
               fos.close();
           }
           ins.close();
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
 
        ClassLoader cl =getClassLoader();
        ApplicationInfo ai =getApplicationInfo();
        String nativeLibraryDir= null;
        if(Build.VERSION.SDK_INT > 8) {
           nativeLibraryDir = ai.nativeLibraryDir;
        } else {
           nativeLibraryDir = "/data/data/" + ai.packageName +"/lib/";
        }
        DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
               dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());
 
        try {
           Field f = ClassLoader.class.getDeclaredField("parent");
           f.setAccessible(true);
           f.set(cl, dcl);
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    //关闭Android P弹出警告框
    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 创建服务用于捕获崩溃异常    
    private UncaughtExceptionHandler restartHandler = new UncaughtExceptionHandler() {    
        public void uncaughtException(Thread thread, Throwable ex) {  
        	strErr = ex.getLocalizedMessage()+"\n"+ex.getMessage();
        	strErr +="";
            restartApp();//发生崩溃异常时,重启应用    
        }    
    };    
    public void restartApp(){  
        Intent intent = new Intent(instance,com.sheca.zhongmei.LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        instance.startActivity(intent);  
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前  
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
