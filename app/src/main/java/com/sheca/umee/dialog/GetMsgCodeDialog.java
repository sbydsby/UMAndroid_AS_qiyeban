package com.sheca.umee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheca.umee.R;

public class GetMsgCodeDialog extends Dialog{
    private Button positiveButton, negativeButton;
    private TextView contenttv;
    private ImageView exitBtn;
 
    public GetMsgCodeDialog(Context context) {
        super(context,R.style.mydialog);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mydialoglayout, null);  //通过LayoutInflater获取布局
        contenttv = (TextView) view.findViewById(R.id.title);
        positiveButton = (Button) view.findViewById(R.id.acceptbtn);
        negativeButton = (Button) view.findViewById(R.id.refusebtn);
        exitBtn = (ImageView) view.findViewById(R.id.login_exit);
        
      
        
        setContentView(view);  //设置view
        
        /*
        VerificationCodeView verificationcodeview = (VerificationCodeView)view.findViewById(R.id.verificationcodeview);
        
        verificationcodeview.setOnCodeFinishListener(new VerificationCodeView.OnCodeFinishListener() {
            @Override
            public void onComplete(String content) {
                textView.setText(content);
            }
        });*/
    }
    //设置内容
    public void setContent(String content) {
        contenttv.setText(content);
    }
    
    //确定按钮监听
    public void setOnPositiveListener(View.OnClickListener listener){
        positiveButton.setOnClickListener(listener);
       
    }
 
    //否定按钮监听
    public void setOnNegativeListener(View.OnClickListener listener){
        negativeButton.setOnClickListener(listener);
    }
    
    public void closeDialog(View.OnClickListener listener){
    	exitBtn.setOnClickListener(listener);
       
    }
}