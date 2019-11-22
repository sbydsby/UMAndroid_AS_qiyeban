package com.sheca.umandroid.model;

import android.content.Context;

import com.sheca.JShcaCciStd.JShcaCciStd;
import com.sheca.jshcaesstd.JShcaEsStd;

public  class ShcaCciStd {
	public static JShcaCciStd gSdk = null;
	public static JShcaEsStd gEsDev = null;
	public static int errorCode = -1;
	
	private ShcaCciStd(){}
	
	public static JShcaCciStd getInstance(Context paramContext){
		if(null == gSdk)
			gSdk = JShcaCciStd.getIntence(paramContext);
		
		return gSdk;
	}
	
	
	public static JShcaEsStd getInstanceJShcaEsStd(Context paramContext){
		if(null == gEsDev)
			gEsDev = JShcaEsStd.getIntence(paramContext);
		
		return gEsDev;
	}
	
}
