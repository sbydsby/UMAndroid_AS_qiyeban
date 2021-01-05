package com.sheca.zhongmei;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sheca.zhongmei.util.GenSeedUtil;
import com.sheca.zhongmei.util.PKIUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GetRandomNumberActivity extends Activity /* implements SensorEventListener */{
	  final String tag = "IBMEyes";
	  //SensorManager sm = null;
	  TextView View1 = null;
	  TextView View2 = null;
	  TextView View3 = null;
	  TextView View4 = null;
	  TextView View5 = null;
	  TextView View6 = null;
	  TextView View7 = null;
	  TextView View8 = null;
	  TextView View9 = null;
	  TextView View10 = null;
	  TextView View11 = null;
	  TextView View12 = null;
	  
	  private String strInfo = "";
	  //private LogUtil logUtil = null;
	  
	  private static int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_get_random);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("随机数测试");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GetRandomNumberActivity.this.finish();
			}
		});
		
		//sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		View1 = (TextView) findViewById(R.id.edt1);
	    View2 = (TextView) findViewById(R.id.edt2);
	    View3 = (TextView) findViewById(R.id.edt3);
	    View4 = (TextView) findViewById(R.id.edt4);
	    View5 = (TextView) findViewById(R.id.edt5);
	    View6 = (TextView) findViewById(R.id.edt6);
	    View7 = (TextView) findViewById(R.id.edt7);
	    View8 = (TextView) findViewById(R.id.edt8);
	    View9 = (TextView) findViewById(R.id.edt9);
	    View10 = (TextView) findViewById(R.id.edt10);
	    View11 = (TextView) findViewById(R.id.edt11);
	    View12 = (TextView) findViewById(R.id.edt12);

	    try {
			strInfo = PKIUtil.getSHADigest(getBCDTime(),"SHA-512","SUN")+PKIUtil.getSHADigest(getBCDUserInfo(),"SHA-512","SUN");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    ((TextView) findViewById(R.id.edtbcd)).setText("bcd:" + strInfo);
	    
	   /*
	    logUtil = new LogUtil(GetRandomNumberActivity.this,true);
	    logUtil.removeServiceLog();
	    logUtil.init();
	    logUtil.recordLogServiceLog(strInfo);
	   */

	    
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			//logUtil.destory();
			GetRandomNumberActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	/*
	public void onSensorChanged(SensorEvent event) {
		StringBuffer strSensor = new StringBuffer();
		  
	    synchronized (this) {
	      count++;
	      
	      switch (event.sensor.getType()){
	      case Sensor.TYPE_ACCELEROMETER:
	          View1.setText("加速度：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	          strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	          logSeedNumber(count,strSensor.toString());
	          //strSensor.append("||");
	          break;
	      case Sensor.TYPE_MAGNETIC_FIELD:
	          View2.setText("磁场：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	          strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	          logSeedNumber(count,strSensor.toString());
	          //strSensor.append("||");
	          break;
	      case Sensor.TYPE_ORIENTATION:
	        View3.setText("定位：" +  "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	        strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	        logSeedNumber(count,strSensor.toString());
	       // strSensor.append("||");
	        break;
	      case Sensor.TYPE_GYROSCOPE:
	        View4.setText("陀螺仪：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	        strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	        logSeedNumber(count,strSensor.toString());
	       // strSensor.append("||");
	        break;
	      case Sensor.TYPE_LIGHT:
	        View5.setText("光线：" + "X：" + event.values[0] );
	        strSensor.append(getSensorInfo(event.values[0],0.0f,0.0f));
	        logSeedNumber(count,strSensor.toString());
	        //strSensor.append("||");
	        break;
	      case Sensor.TYPE_PRESSURE:
	        View6.setText("压力：" + "X：" + event.values[0]);
	        strSensor.append(getSensorInfo(event.values[0],0.0f,0.0f));
	        logSeedNumber(count,strSensor.toString());
	       // strSensor.append("||");
	        break;
	      case Sensor.TYPE_TEMPERATURE:
	        View7.setText("温度：" + "X：" + event.values[0]);
	        strSensor.append(getSensorInfo(event.values[0],0.0f,0.0f));
	        logSeedNumber(count,strSensor.toString());
	        //strSensor.append("||");
	        break;
	      case Sensor.TYPE_PROXIMITY:
	        View8.setText("距离：" + "X：" + event.values[0] );
	        strSensor.append(getSensorInfo(event.values[0],0.0f,0.0f));
	        logSeedNumber(count,strSensor.toString());
	       // strSensor.append("||");
	        break;
	      case Sensor.TYPE_GRAVITY:
	          View9.setText("重力：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);      
	          strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	          logSeedNumber(count,strSensor.toString());
		      //strSensor.append("||");
	        break;
	      case Sensor.TYPE_LINEAR_ACCELERATION:
	        View10.setText("线性加速度：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	        strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	        logSeedNumber(count,strSensor.toString());
	        //strSensor.append("||");
	        break;
	      case Sensor.TYPE_ROTATION_VECTOR:
	        View11.setText("旋转矢量：" + "X：" + event.values[0]  + "，Y：" + event.values[1] + "，Z：" + event.values[2]);
	        strSensor.append(getSensorInfo(event.values[0],event.values[1],event.values[2]));
	        logSeedNumber(count,strSensor.toString());
	        //strSensor.append("||");
	        break;
	      default:
	        View12.setText("NORMAL:" + "X：" + event.values[0]);
	        break;
	      }
	    }
	    
	   
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    Log.d(tag,"onAccuracyChanged: " + sensor.getName() + ", accuracy: " + accuracy);
	}
	*/

	@Override
	protected void onResume() {
	    super.onResume();
	    GenSeedUtil.registerSensor();
	    
	    GenSeedUtil.getSeedNumber();
	    /*         
                               最常用的一个方法 注册事件         
                              参数1 ：SensorEventListener监听器        
                              参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor         
                              参数3 ：模式 可选数据变化的刷新频率，多少微秒取一次。         
        */        
	    
	    /*
	    //加速度传感器        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 为磁场传感器注册监听器        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 为方向传感器注册监听器       
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 为陀螺仪传感器注册监听器        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 为重力传感器注册监听器       
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 为线性加速度传感器注册监听器       
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 为温度传感器注册监听器        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);        
	    
	    Sensor tempertureSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (tempertureSensor != null) {
	              //Toast.makeText(this, "你的设备不支持该功能", 0).show();
	       sm.registerListener(this, tempertureSensor, SensorManager.SENSOR_DELAY_NORMAL);      
	    }       
	    
	    // 为光传感器注册监听器        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 为距离传感器注册监听器       
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 为压力传感器注册监听器      
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 计步统计        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 单次计步        
	    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);    
	    */
	}
	  
	@Override  
	protected void onStop() {
		GenSeedUtil.unRegisterSensor();
	    //sm.unregisterListener(this);
	    super.onStop();
	}
	
    private  String  getBCDTime(){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	TimeZone tzChina = TimeZone.getTimeZone("UTC+8");
    	sdf.setTimeZone(tzChina);
    	
    	Date nowTime = new Date();
    	String strTime = sdf.format(nowTime) +nowTime.getTime();
    	try{
    	   strTime = new String(PKIUtil.str2cbcd(strTime));
    	}catch(Exception ex){
    	   strTime = "";
    	}
    	
    	return strTime;
    }
    
    private  String  getBCDUserInfo(){
    	String strInfo = android.os.Build.BOARD+android.os.Build.BRAND+android.os.Build.CPU_ABI+
    			         android.os.Build.CPU_ABI2+android.os.Build.DEVICE+android.os.Build.HARDWARE+
    	    			 android.os.Build.FINGERPRINT+android.os.Build.HOST+android.os.Build.ID+
    	    			 android.os.Build.MANUFACTURER+android.os.Build.MODEL+android.os.Build.PRODUCT+
    	    			 android.os.Build.RADIO+android.os.Build.SERIAL+android.os.Build.TYPE+
    	    	    	 android.os.Build.DISPLAY+android.os.Build.TIME+android.os.Build.USER+
    	    	    	 android.os.Build.VERSION.SDK+android.os.Build.VERSION.SDK_INT+android.os.Build.VERSION.CODENAME;
    	
    	try{
    		strInfo = new String(PKIUtil.str2cbcd(strInfo));
     	}catch(Exception ex){
     		strInfo = "";
     	}
     	
     	return strInfo;
    }
    
    private  String  getSensorInfo(float x,float y,float z){
    	  StringBuffer str = new StringBuffer(); 
    	  double fx = 0.0;
	      java.math.BigDecimal   bd = new BigDecimal(x);    
	      bd = bd.setScale(10,BigDecimal.ROUND_FLOOR);  
	      fx = bd.doubleValue()%16;
	      str.append(fx);
	      
	      bd  =  new   BigDecimal(y);   
	      bd = bd.setScale(10,BigDecimal.ROUND_FLOOR);  
	      fx = bd.doubleValue()%16;
	      str.append(fx);
	      
	      bd  =  new   BigDecimal(z);   
	      bd = bd.setScale(10,BigDecimal.ROUND_FLOOR);  
	      fx = bd.doubleValue()%16;
	      str.append(fx);

	      return  str.toString(); 	
    }
      
    /*
    private  void  logSeedNumber(int count,String strSensor){
    	 if(count <= 10){
  	       try {
  			   String bcdStr = new String(PKIUtil.str2cbcd(strSensor.toString()));
  			   String lgInfo = strInfo + PKIUtil.getSHADigest(bcdStr,"SHA-512","SUN");
  			   logUtil.recordLogServiceLog(new String(lgInfo.getBytes()));
  		   } catch (Exception e) {
  			  // TODO Auto-generated catch block
  			  e.printStackTrace();
  		   }
  	    }

    }
    */
    
}
