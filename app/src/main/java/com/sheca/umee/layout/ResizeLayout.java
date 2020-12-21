package com.sheca.umee.layout;

/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ResizeLayout extends RelativeLayout{     
    private static final String TAG = ResizeLayout.class.getSimpleName();  
    public static final byte KEYBOARD_STATE_SHOW = -3;  
    public static final byte KEYBOARD_STATE_HIDE = -2;  
    public static final byte KEYBOARD_STATE_INIT = -1;  
    private boolean mHasInit;  
    private boolean mHasKeybord;  
    private int mHeight;  
    private onKybdsChangeListener mListener;  
       
    public ResizeLayout(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        }  
      
    public ResizeLayout(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
       
    public ResizeLayout(Context context) {  
    super(context);  
    }  
    /** 
    * set keyboard state listener 
    */  
    public void setOnkbdStateListener(onKybdsChangeListener listener){  
    mListener = listener;  
    }  
    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
    super.onLayout(changed, l, t, r, b);  
    if (!mHasInit) {  
    mHasInit = true;  
    mHeight = b;  
    if (mListener != null) {  
    mListener.onKeyBoardStateChange(KEYBOARD_STATE_INIT);  
    }  
    } else {  
    mHeight = mHeight < b ? b : mHeight;  
    }  
    if (mHasInit && mHeight > b) {  
    mHasKeybord = true;  
    if (mListener != null) {  
    mListener.onKeyBoardStateChange(KEYBOARD_STATE_SHOW);  
    }  
   
    }  
    if (mHasInit && mHasKeybord && mHeight == b) {  
    mHasKeybord = false;  
    if (mListener != null) {  
    mListener.onKeyBoardStateChange(KEYBOARD_STATE_HIDE);  
    }  
   
    }  
    }  
       
    public interface onKybdsChangeListener{  
    public void onKeyBoardStateChange(int state);  
    }  
}  