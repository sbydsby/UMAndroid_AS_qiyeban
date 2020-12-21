package com.sheca.umee.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.sheca.umee.R;
import com.sheca.umee.model.GetAppVersionEx;
import com.sheca.umee.service.MyApp;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UpdateUtil {
	private static final String TAG = "Config";
	public  static final String APP_PACK_NAME = "com.sheca.umandroid";
	public  static final String APP_NAME = "UniTrust.apk";       //apk安装包名称
	public  static final String UPDATE_APP_NAME = "Unitrust";    //更新服务接口参数名称
	private static final int    BYTE_LENGTH = 102400;             //下载数据大小

	public static int     newVerCode = 0;      //最新版本号
	public static String  newVerName = "";     //最新版本名称
	public static String  Description = "";    //最新信息描述
	public  static String  strDownPath = "";    //最新版本下载地址
	public  static int     isCompulsion = 0;    //是否强制更新
	
	private Handler handler=new Handler();
	
	private Activity activity = null;
	private boolean  bShowErr = false;
	private ProgressDialog pBar;
	private MyApp app;
	
	public UpdateUtil(Activity context,boolean bShow){
		activity = context;	
		bShowErr = bShow;
		//app = (MyApp) activity.getApplication();
	}
	
	public void  setActicity(Activity context){
		activity = context;
	}
	
	//检测最新版本及下载更新
	public boolean checkToUpdate(){
		
//  		try {
//  			//Thread.sleep(5000);
//			//if(getServerVersion()){
//				int currentCode = getVerCode(activity);
//				if(newVerCode == 0){
//					//Toast.makeText(activity, "网络连接异常或无法访问更新服务", Toast.LENGTH_SHORT).show();
//					return true;
//				}
//
//				if(newVerCode > currentCode)
//				{
//					//弹出更新提示对话框
//					if(isCompulsion == 0){
//						showUpdateDialog();
//					}
//					else{
//					  showUpdateDialogCompulsion();
//					}
//
//
//					return true;
//				}
//
//			//}
//		} catch (Exception ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//			if(ex.getMessage().indexOf("peer")!=-1)
//			   Toast.makeText(activity, "无效的服务器请求", Toast.LENGTH_SHORT).show();
//			else
//			   Toast.makeText(activity, "网络连接异常或无法访问更新服务", Toast.LENGTH_SHORT).show();
//			return true;
//		}
  		
  		return false;
  	}
	
  	//获取最新版本
	
  	public boolean getServerVersion() {
		try{
			    String responseStr = GetAppLatestVersionEx(UPDATE_APP_NAME,bShowErr);
  
			    JSONObject jb = JSONObject.fromObject(responseStr);
				String resultStr = jb.getString(CommonConst.RETURN_CODE);
			    String returnStr = jb.getString(CommonConst.RETURN_MSG);
			    
			    if(!"0".equals(resultStr)){
			    	throw new Exception("调用UMSP服务之GetAppLatestVersion失败：" + resultStr + "，" + returnStr);
			    }
			    
			    JSONObject  jbRet =  JSONObject.fromObject(jb.getString(CommonConst.RETURN_RESULT));		    	
			    GetAppVersionEx dcResponse = new GetAppVersionEx();
		    	dcResponse.setReturn(resultStr);
		    	dcResponse.setResult(returnStr);
		    	dcResponse.setVersion(jbRet.getString(CommonConst.RESULT_PARAM_VERSION));
		    	dcResponse.setDownloadURL(jbRet.getString(CommonConst.RESULT_PARAM_DOWNLOADURL));
		    	dcResponse.setDescription(jbRet.getString(CommonConst.RESULT_PARAM_DESCRIPTION));
		    	dcResponse.setCompulsion(jbRet.getString(CommonConst.RESULT_PARAM_COMPULSION));

			    final  GetAppVersionEx fDcResponse = dcResponse;

				try{
					if (resultStr.equals("0")) {
						if(!"".equals(fDcResponse.getVersion()) || null != fDcResponse.getVersion()){
					        newVerCode = Integer.parseInt(fDcResponse.getVersion().replace(".", ""));
					        newVerName = fDcResponse.getVersion();
						}
						
						Description = fDcResponse.getDescription();
					    strDownPath = fDcResponse.getDownloadURL();
					    
					    isCompulsion = Integer.parseInt(fDcResponse.getCompulsion());
					}
					else
						throw new Exception("调用UMSP服务之GetAppLatestVersion失败：" + resultStr + "，" + returnStr);
						
				}catch(Exception e){
					if(bShowErr)
					  if(!"".equals(e.getLocalizedMessage()))
					     Toast.makeText(activity, "获取数据异常", Toast.LENGTH_SHORT).show();
					newVerCode = -1;
					newVerName = "";
					return false;
			    }
		}catch(Exception e){
			if(bShowErr){
			   if(!"".equals(e.getLocalizedMessage())){
				   if(e.getMessage().indexOf("peer")!=-1)
					   Toast.makeText(activity, "无效的服务器请求", Toast.LENGTH_SHORT).show();
					else				
					   Toast.makeText(activity, "网络连接异常或无法访问更新服务", Toast.LENGTH_SHORT).show();
			   }
			}
			
			return false;
		}
		//Toast.makeText(MainActivity.this, "version:"+newVerName+",url:"+strDownPath, Toast.LENGTH_SHORT).show();
		return true;
	}

  
	/*
  	public boolean getServerVersion() {
		try{
			    String responseStr = GetAppLatestVersion(UPDATE_APP_NAME,bShowErr);
			    final  GetAppVersion dcResponse = JsonUtil.parseJson(responseStr, GetAppVersion.class);
			    String resultStr  = dcResponse.getResult();
			    String returnStr = dcResponse.getReturn();
			    
				try{
					if (resultStr.equals("0")) {
						if(!"".equals(dcResponse.getVersion()) || null != dcResponse.getVersion()){
					        newVerCode = Integer.parseInt(dcResponse.getVersion().replace(".", ""));
					        newVerName = dcResponse.getVersion();
						}
						
						Description = dcResponse.getDescription();
					    strDownPath = dcResponse.getDownloadURL();					   
					}
					else
						throw new Exception("调用UMSP服务之GetAppLatestVersion失败：" + resultStr + "，" + returnStr);
						
				}catch(Exception e){
					if(bShowErr)
					  if(!"".equals(e.getLocalizedMessage()))
					     Toast.makeText(activity, "获取数据异常", Toast.LENGTH_SHORT).show();
					newVerCode = -1;
					newVerName = "";
					return false;
			    }
		}catch(Exception e){
			if(bShowErr){
			   if(!"".equals(e.getLocalizedMessage())){
				   if(e.getMessage().indexOf("peer")!=-1)
					   Toast.makeText(activity, "无效的服务器请求", Toast.LENGTH_SHORT).show();
					else				
					   Toast.makeText(activity, "网络连接异常或无法访问更新服务", Toast.LENGTH_SHORT).show();
			   }
			}
			
			return false;
		}
		//Toast.makeText(MainActivity.this, "version:"+newVerName+",url:"+strDownPath, Toast.LENGTH_SHORT).show();
		return true;
	}
  	*/
  	
  //显示更新界面
  	private void showUpdateDialog() throws Exception {
  		StringBuffer sb = new StringBuffer();
  		sb.append("当前版本：");
  		sb.append(getVerName(activity));
  		sb.append("\n");
  		sb.append("发现新版本：");
  		sb.append(newVerName);
  		sb.append("\n");
  		//sb.append("\n");
  		sb.append("更新内容:\n");
  		sb.append(Description);
  		Dialog dialog = new AlertDialog.Builder(activity)
  		.setTitle("软件更新")
  		.setMessage(sb.toString())
  		.setNegativeButton("更新", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				showProgressBar();
  			}
  		})
  		.setPositiveButton("暂不更新", new DialogInterface.OnClickListener() {
  			public void onClick(DialogInterface dialog, int which) {
  				dialog.dismiss();
  			}
  		}).create();
  		dialog.setCancelable(false); 
  		dialog.show();
  	}

	private void startInstallPermissionSettingActivity() {
		Uri packageURI = Uri.parse("package:" + activity.getBaseContext().getPackageName());
		//注意这个是8.0新API
		Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
		activity.startActivityForResult(intent, 10086);
	}

  	private boolean checkAPKInstallPermission(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			return activity.getBaseContext().getPackageManager().canRequestPackageInstalls();
		}else{
			return true;
		}
	}
  	
  	
  //显示更新界面(强制更新)
  	private void showUpdateDialogCompulsion() throws Exception {
  		StringBuffer sb = new StringBuffer();
  		sb.append("当前版本：");
  		sb.append(getVerName(activity));
  		sb.append("\n");
  		sb.append("发现新版本：");
  		sb.append(newVerName);
  		sb.append("\n");
  		//sb.append("\n");
  		sb.append("更新内容:\n");
  		sb.append(Description);
  		Dialog dialog = new AlertDialog.Builder(activity)
  		.setTitle("软件更新")
  		.setMessage(sb.toString())
  		.setNegativeButton("更新", new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				showProgressBar();//更新当前版本
  				//haveDownLoad();
  			}
  		}).create();
  		dialog.setCancelable(false); 
  		dialog.show();
  	}
  	
  	
  	protected void showProgressBar() {
  		pBar = new ProgressDialog(activity);
  		pBar.setIcon(R.drawable.alert);
  		pBar.setTitle("正在下载");
  		pBar.setMessage("请稍候...");
  		pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
  		pBar.setCancelable(false);
  		//downAppFile(strDownPath);
  		//strDownPath = "http://202.96.220.165/d/UniTrustP.apk";
  		downAppFileEx(strDownPath);
  		
  		/*
  		 try {
			Intent it = new Intent(activity, NotificationUpdateActivity.class);
			activity.startActivity(it);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
//		MapApp.isDownload = true;
		//app.setDownload(true);	
  	}
	
  	//下载更新apk安装包
  	protected void downAppFile(final String url) {
		pBar.show();
		final Handler handler = new Handler(activity.getMainLooper());
		
		new Thread(){
			@SuppressWarnings("deprecation")
			public void run(){
				
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					final long length = entity.getContentLength();
				    //Log.isLoggable("DownTag", (int) length);
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					
					if(is == null){
						throw new RuntimeException("isStream is null");
					}
					
					File file = new File(Environment.getExternalStorageDirectory(),APP_NAME);
					fileOutputStream = new FileOutputStream(file);
					
					int count = 0;
					byte[] buf = new byte[BYTE_LENGTH];
					int ch = -1;
					
					do{
						ch = is.read(buf);
						count += ch;
						
						final int progress = count;
						// 更新进度
						handler.post(new Runnable() {
							 @Override
								public void run() {
								    pBar.setMessage("当前进度 :" + (int) (((float) progress / length) * 100) + "%  ");
								}
						}); 
							
						if(ch <= 0)break;
						fileOutputStream.write(buf, 0, ch);
					}while(true);
					
					is.close();
					fileOutputStream.close();
					
					handler.post(new Runnable() {
						 @Override
							public void run() {
							    pBar.dismiss();
							    pBar = null;;
							}
					}); 
											
					haveDownLoad();
				}catch(Exception e){
						e.printStackTrace();
						
						handler.post(new Runnable() {
							 @Override
								public void run() {
								    pBar.dismiss();
								    pBar = null;
								    
								    Toast.makeText(activity, "保存文件异常，确认是否允许文件保存权限", Toast.LENGTH_SHORT).show();
								}
						}); 
				}
				
			     //haveDownLoad();
			}
		}.start();
	}
  	
	//下载更新apk安装包
  	protected void downAppFileEx(final String url) {
		pBar.show();
		final Handler handler = new Handler(activity.getMainLooper());
		
		new Thread(new Runnable() {
			@Override
			 public void run() {
				try {
					URL httpurl = new URL(url);
			        HttpURLConnection httpConn=(HttpURLConnection)httpurl.openConnection();
			        //httpConn.setDoOutput(true);// 使用 URL 连接进行输出
			        httpConn.setDoInput(true);// 使用 URL 连接进行输入
			        httpConn.setUseCaches(false);// 忽略缓存
			        httpConn.setRequestMethod("GET");// 设置URL请求方法
			        //可设置请求头
			        httpConn.setRequestProperty("Content-Type", "application/octet-stream");
			        httpConn.setRequestProperty("Connection", "keep-alive");// 维持长连接
			        httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 7.0; MI 5 Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/62.0.3202.84 Mobile Safari/537.36");  
			        httpConn.setRequestProperty("Charset", "UTF-8");
			        httpConn.connect(); 
			        long conLength = httpConn.getContentLength();
			        
			        InputStream inStream = null;
			        int retCode = httpConn.getResponseCode();// 设置http返回状态200还是403  
			        if (retCode == 200) {  
			        	inStream = httpConn.getInputStream(); // 得到网络返回的正确输入流
			        } else {
			        	inStream = httpConn.getErrorStream(); // 得到网络返回的错误输入流
			        }
			       
			        String fileName = Environment.getExternalStorageDirectory()+File.separator+APP_NAME;
			        input2byte(inStream,handler,conLength,fileName);
			        
			        //writeBytesToFile(file,fileName);						
					haveDownLoad();
				}catch(Exception e){
						e.printStackTrace();
						
						handler.post(new Runnable() {
							 @Override
								public void run() {
								    pBar.dismiss();
								    pBar = null;
								    
								    Toast.makeText(activity, "保存文件异常，确认是否允许文件保存权限", Toast.LENGTH_SHORT).show();
								}
						}); 
				}
				
			     //haveDownLoad();
			  }
	   }).start();
	}
  	
  	protected  void input2byte(InputStream inStream,Handler handler,final long fileLength,final String fileName ) throws IOException {
         ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
         byte[] buff = new byte[BYTE_LENGTH];
         int rc = 0;
         int count  = 0;
         File file = null;
         FileOutputStream outputStream = null;
         BufferedInputStream bfi = null;
         
        // final long length = inStream.available();
         try {
        	file = new File(fileName);
        	//创建一个文件输出流
            outputStream = new FileOutputStream(file);
            bfi = new BufferedInputStream(inStream);

            //while ((rc = inStream.read(buff, 0, BYTE_LENGTH)) > 0) {
            while ((rc = bfi.read(buff)) != -1) {
        	   count += rc;
        	   final int progress = count;
			   // 更新进度
			   handler.post(new Runnable() {
					 @Override
						public void run() {
						    pBar.setMessage("当前进度 :" + (int) (((float) progress / fileLength) * 100) + "%  ");
						}
			   }); 
        	 
               //swapStream.write(buff, 0, rc);
               outputStream.write(buff, 0, rc);
            }
            
            //关闭打开的流对象
            outputStream.close();
            inStream.close();
            bfi.close();
            
            handler.post(new Runnable() {
				 @Override
					public void run() {
					    pBar.dismiss();
					    pBar = null;
					}
			}); 

         } catch (Exception var13) {
             var13.printStackTrace();
             
             handler.post(new Runnable() {
				 @Override
					public void run() {
					    pBar.dismiss();
					    pBar = null;
					    
					    Toast.makeText(activity, "保存文件异常，确认是否允许文件保存权限", Toast.LENGTH_SHORT).show();
					}
			}); 
         } finally {
             try {
            	 outputStream.close();
                 inStream.close();
                 bfi.close();
             } catch (Exception var12) {
                 var12.printStackTrace();
             }

         }
         
        // byte[] in2b = swapStream.toByteArray();
         //return in2b;
    }

  	protected File writeBytesToFile(byte[] b, String outputFile) {
         File file = null;
         FileOutputStream os = null;
         
         try {
             file = new File(outputFile);
             os = new FileOutputStream(file);
             os.write(b);
         } catch (Exception var13) {
             var13.printStackTrace();
         } finally {
             try {
                 if(os != null) {
                     os.close();
                 }
             } catch (IOException var12) {
                 var12.printStackTrace();
             }

         }

         return file;
    }

  	
	//完成下载
	protected void haveDownLoad() {
		installNewApk();  //安装更新apk
		//activity.finish();
		openApk();        //打开已更新好的apk
		
		/*handler.post(new Runnable(){
			public void run(){
				pBar.cancel();
				//弹出警告框 提示是否安装新的版本
				Dialog installDialog = new AlertDialog.Builder(activity)
				.setTitle("下载完成")
				.setMessage("是否安装新的应用")
				.setNegativeButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						installNewApk();  //安装更新apk
						activity.finish();
						}
					})
					.setPositiveButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							}
						})
						.create();
				
				installDialog.setCancelable(false); 
				installDialog.show();
				}
			});*/
		}
	
	//安装新的应用
	public void installNewApk() {
		if (checkAPKInstallPermission()){
			install();
		}else{
			startInstallPermissionSettingActivity();
		}


	}

	private void install(){
		Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),APP_NAME));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
		} else {//Android7.0之后获取uri要用contentProvider
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.getBaseContext().startActivity(intent);
	}
	
	 /**
     * 打开已经安装好的apk
     */
	protected void openApk() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(),APP_NAME)),
                        "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }
 
	
	private String GetAppLatestVersion(String appName,boolean bTimeout) throws Exception{
		String responseStr = "";
				
		String timeout = activity.getString(R.string.WebService_Timeout);				
		String urlPath = activity.getString(R.string.UMSP_Service_GetAppLatestVersion);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("AppName", appName);
		
		String postParam = "AppName="+URLEncoder.encode(appName, "UTF-8");

		
		if(!bTimeout)
			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		else
			responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			
    	return responseStr;
	}
	
	
	private String GetAppLatestVersionEx(String appName,boolean bTimeout) throws Exception{
		String responseStr = "";
				
		String timeout = activity.getString(R.string.WebService_Timeout);				
		String urlPath = activity.getString(R.string.UMSP_Service_GetAppLatestVersion);										
		Map<String,String> postParams = new HashMap<String,String>();	
		postParams.put("appName", appName);
		postParams.put("currentVersion", getVerName(activity));
		
		String postParam = "appName="+URLEncoder.encode(appName, "UTF-8")+
				           "&currentVersion="+URLEncoder.encode(getVerName(activity), "UTF-8");
		
		if(!bTimeout)
    	    //responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
		     responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
		else
			//responseStr = WebClientUtil.httpPost(urlPath, postParams, Integer.parseInt(timeout));
		    responseStr = WebClientUtil.getHttpClientPost(urlPath,postParam,Integer.parseInt(timeout));
			
    	return responseStr;
	}
	
	
	public  int getVerCode(Context context)throws NameNotFoundException{
		int verCode = -1;
		try{
			verCode = Integer.parseInt(context.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName.replace(".", ""));
		}catch(Exception e){
			//Log.e(TAG, e.getMessage());
		}
		return verCode;
	}
	
	public  String getVerName(Context context){
		String verName = "";
		try{
			verName = context.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
		}catch(Exception e){
			//Log.e(TAG, e.getMessage());
		}
		return verName;
	}
	
	private void  setShowUpdate(){
		SharedPreferences preferences = activity.getSharedPreferences(
				CommonConst.PREFERENCES_NAME, Activity.MODE_PRIVATE);
			
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(CommonConst.SHOW_UPDATE, 1);
		editor.commit();
	}
}
