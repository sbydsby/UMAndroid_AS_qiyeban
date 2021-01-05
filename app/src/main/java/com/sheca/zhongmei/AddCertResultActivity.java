package com.sheca.zhongmei;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AddCertResultActivity extends BaseActivity2 {

    private ImageView imageView;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.selected_xzzs,R.layout.activity_add_cert_result);

        imageView = (ImageView)findViewById(R.id.image_cert); //R.drawable.cert_failure
        textView = (TextView)findViewById(R.id.tv_cert);
        button = (Button)findViewById(R.id.btn_ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(AddCertResultActivity.this, MainActivity.class);	
		       startActivity(intent);	
		       AddCertResultActivity.this.finish();
            }
        });
    }
}
