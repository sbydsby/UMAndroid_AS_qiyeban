package com.sheca.umandroid.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GenSeedUtil {
	private static SensorManager sm = null;
	private static StringBuffer strSensor = new StringBuffer();

    //加速度传感器监听器        
    private static SensorEventListener myACCELEROMETERListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
          if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float headingAngle = sensorEvent.values[0];
            float pitchAngle =  sensorEvent.values[1];
            float rollAngle = sensorEvent.values[2];
	        strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
          }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
	
    
    //磁场传感器注册监听器        
    private static SensorEventListener myMAGNETICFIELDListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
          if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float headingAngle = sensorEvent.values[0];
            float pitchAngle =  sensorEvent.values[1];
            float rollAngle = sensorEvent.values[2];
	        strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
          }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
	
    //方向传感器监听器
  	private static SensorEventListener myOrientationListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                float headingAngle = sensorEvent.values[0];
                float pitchAngle =  sensorEvent.values[1];
                float rollAngle = sensorEvent.values[2];
    	          strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
      
    
    //陀螺仪传感器注册监听器        
  	private static SensorEventListener  myGYROSCOPEListener= new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float headingAngle = sensorEvent.values[0];
                float pitchAngle =  sensorEvent.values[1];
                float rollAngle = sensorEvent.values[2];
    	        strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
      
    //重力传感器注册监听器       
  	private static SensorEventListener myGRAVITYListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
                float headingAngle = sensorEvent.values[0];
                float pitchAngle =  sensorEvent.values[1];
                float rollAngle = sensorEvent.values[2];
    	        strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };


    //线性加速度传感器注册监听器       
  	private static SensorEventListener myLINEARACCELERATIONListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                float headingAngle = sensorEvent.values[0];
                float pitchAngle =  sensorEvent.values[1];
                float rollAngle = sensorEvent.values[2];
    	        strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    
    //温度传感器注册监听器        
  	private static SensorEventListener myTEMPERATUREListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
                float headingAngle = sensorEvent.values[0];
    	        strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    
    
    //压力传感器注册监听器        
  	private static SensorEventListener myPRESSUREListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
                float headingAngle = sensorEvent.values[0];
    	        strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    
    //光传感器注册监听器        
  	private static SensorEventListener myLIGHTListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
              if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
                float headingAngle = sensorEvent.values[0];
    	        strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
              }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    
    //距离传感器注册监听器        
   	private static SensorEventListener myPROXIMITYListener = new SensorEventListener() {
             public void onSensorChanged(SensorEvent sensorEvent) {
               if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                 float headingAngle = sensorEvent.values[0];
     	         strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
               }
             }
             public void onAccuracyChanged(Sensor sensor, int accuracy) {}
     };
     
     //计步传感器注册监听器        
     private static SensorEventListener mySTEPCOUNTERListener = new SensorEventListener() {
              public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                  float headingAngle = sensorEvent.values[0];
      	          strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
                }
              }
              public void onAccuracyChanged(Sensor sensor, int accuracy) {}
     };
     
     //单次传感器注册监听器        
     private static SensorEventListener mySTEPDETECTORListener = new SensorEventListener() {
              public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                  float headingAngle = sensorEvent.values[0];
      	          strSensor.append(getSensorInfo(headingAngle,0.0f,0.0f));
                }
              }
              public void onAccuracyChanged(Sensor sensor, int accuracy) {}
      };
      
      //旋转矢量传感器注册监听器        
      private static SensorEventListener myROTATIONVECTORListener = new SensorEventListener() {
               public void onSensorChanged(SensorEvent sensorEvent) {
                 if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                   float headingAngle = sensorEvent.values[0];
                   float pitchAngle =  sensorEvent.values[1];
                   float rollAngle = sensorEvent.values[2];
       	           strSensor.append(getSensorInfo(headingAngle,pitchAngle,rollAngle));
                 }
               }
               public void onAccuracyChanged(Sensor sensor, int accuracy) {}
      };
      
     
	public static void registerSensor() {
		 //加速度传感器        
	    sm.registerListener(myACCELEROMETERListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 磁场传感器注册监听器        
	    sm.registerListener(myMAGNETICFIELDListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 方向传感器注册监听器       
	    sm.registerListener(myOrientationListener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 陀螺仪传感器注册监听器        
	    sm.registerListener(myGYROSCOPEListener, sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 重力传感器注册监听器       
	    sm.registerListener(myGRAVITYListener, sm.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 线性加速度传感器注册监听器       
	    sm.registerListener(myLINEARACCELERATIONListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 温度传感器注册监听器        
	    Sensor tempertureSensor = sm.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
	    if (tempertureSensor != null) {
	              //Toast.makeText(this, "你的设备不支持该功能", 0).show();
	       sm.registerListener(myTEMPERATUREListener, tempertureSensor, SensorManager.SENSOR_DELAY_NORMAL);      
	    }       
	    
	    // 光传感器注册监听器        
	    sm.registerListener(myLIGHTListener, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 距离传感器注册监听器       
	    sm.registerListener(myPROXIMITYListener, sm.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 压力传感器注册监听器      
	    sm.registerListener(myPRESSUREListener, sm.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);       
	    // 计步统计        
	    sm.registerListener(mySTEPCOUNTERListener, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);        
	    // 单次计步        
	    sm.registerListener(mySTEPDETECTORListener, sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);    
	    // 旋转矢量      
	    sm.registerListener(myROTATIONVECTORListener, sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);    
				
	}
	
	public static void unRegisterSensor() {
		//注销
		sm.unregisterListener(myACCELEROMETERListener);
		sm.unregisterListener(myMAGNETICFIELDListener);
		sm.unregisterListener(myOrientationListener);
		sm.unregisterListener(myGYROSCOPEListener);
		sm.unregisterListener(myGRAVITYListener);
		sm.unregisterListener(myLINEARACCELERATIONListener);
		sm.unregisterListener(myTEMPERATUREListener);
		sm.unregisterListener(myLIGHTListener);
		sm.unregisterListener(myPROXIMITYListener);
		sm.unregisterListener(myPRESSUREListener);
		sm.unregisterListener(mySTEPCOUNTERListener);
		sm.unregisterListener(mySTEPDETECTORListener);
		sm.unregisterListener(myROTATIONVECTORListener);
	}
	
	public static String getSeedNumber() {
		try {
			strSensor.append(PKIUtil.getSHADigest(getBCDTime(),"SHA-512","SUN"));
			strSensor.append(PKIUtil.getSHADigest(getBCDUserInfo(),"SHA-512","SUN"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return strSensor.toString();		
	}
	
	private static String  getBCDTime(){
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
	    
	 private static String  getBCDUserInfo(){
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
	
	 private  static String  getSensorInfo(float x,float y,float z){
  	  StringBuffer str = new StringBuffer(); 
  	  double fx = 0.0;
	      BigDecimal   bd = new BigDecimal(x);
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
	
	
	
}