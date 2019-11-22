package com.sheca.umandroid.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	 private  String TAG = "LogService";
	 private  String LOG_SERVICE_LOG_PATH = "";        //本服务产生的日志，记录日志服务开启失败信息
	 private  String logServiceLogName = "Log.log";    //输出日志文件名称
     private  SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private  OutputStreamWriter writer = null;
     private  String LOG_PATH_MEMORY_DIR = "";                //日志文件在内存中的路径(日志文件在安装目录中的路径)
     
     private Context mContext = null;
     private boolean mLog = false;
	
     public LogUtil(Context context,boolean log){
    	 mContext = context;
    	 mLog = log;
     }
     
     public   void init(){
    	 if(mLog){
    	     LOG_PATH_MEMORY_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"Unitrust" + File.separator + "log";
    	//mContext.getFilesDir().getAbsolutePath() + File.separator + "log";
    	     LOG_SERVICE_LOG_PATH = LOG_PATH_MEMORY_DIR + File.separator + logServiceLogName;
    	
    	     try {
    		    createLogDir();
    		
                writer = new OutputStreamWriter(new FileOutputStream(
                            LOG_SERVICE_LOG_PATH, true));
    	     } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
             }
    	 }
     }
    
     private  void createLogDir() throws Exception{
    	 File file = new File(LOG_PATH_MEMORY_DIR);
         boolean mkOk;
         if (!file.isDirectory()) {
                 mkOk = file.mkdirs();
                 if (!mkOk) {
                         mkOk = file.mkdirs();
                 }
         }

        
         file = new File(LOG_SERVICE_LOG_PATH);
         if (!file.exists()) {
                 try {
                         mkOk = file.createNewFile();
                         if (!mkOk) {
                                 file.createNewFile();
                         }
                 } catch (IOException e) {
                         Log.e(TAG, e.getMessage(), e);
                 }
         }
     }
     
     public void recordLogServiceLog(String msg) {
    	 if(mLog){
            if (writer != null) { 
                 try {
                         Date time = new Date();
                         writer.write(myLogSdf.format(time) + ":" + msg);
                         writer.write("\r\n");
                         writer.flush();
                 } catch (IOException e) {
                         e.printStackTrace();
                         Log.e(TAG, e.getMessage(), e);
                 }
        	}
         }
     }
     
     public void removeServiceLog(){
    	 if(mLog){
    	    File file = new File(LOG_SERVICE_LOG_PATH);
    	    
    	    if (file.exists()) {
                try {
                	file.delete() ;
                } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                }
        }
    	 }
     }
     
     
     public void destory(){
    	 if (writer != null) {
             try {
                     writer.close();
             } catch (IOException e) {
                     e.printStackTrace();
             }
         }
     }
     
     
}
