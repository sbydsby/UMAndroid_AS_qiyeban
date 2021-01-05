package com.sheca.zhongmei;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CertResultActivity extends Activity {

    int type = 0;// 0下载证书   1下载印章   2更新证书   3撤销证书
    ImageView img;
    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_result);

        type = getIntent().getIntExtra("type", 0);


        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);

        cancelScanButton.setVisibility(View.GONE);
        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CertResultActivity.this.finish();
            }
        });

        img = findViewById(R.id.img);
        txt = findViewById(R.id.txt);
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        initView();


//		LinearLayout ll_update=findViewById(R.id.ll_update);
//		ll_update.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(CertResultActivity.this,CertRenewActivity.class);
//				intent.putExtra("CertId","1");
//				startActivity(intent);
//
//			}
//		});
//		LinearLayout ll_revoke=findViewById(R.id.ll_revoke);
//		ll_revoke.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(CertResultActivity.this,CertRevokeActivity.class);
//				intent.putExtra("CertId","1");
//				startActivity(intent);
//			}
//		});


    }

    private void initView() {

        switch (type) {// 0下载证书   1下载印章   2更新证书   3撤销证书
            case 0:
                ((TextView) findViewById(R.id.header_text)).setText("申领完成");
                img.setImageResource(R.drawable.cert_success);
                txt.setText("证书下载成功");
                break;
            case 1:
                ((TextView) findViewById(R.id.header_text)).setText("申领印章");
                img.setImageResource(R.drawable.seal_success);
                txt.setText("印章下载成功");
                break;
            case 2:
                ((TextView) findViewById(R.id.header_text)).setText("更新完成");
                img.setImageResource(R.drawable.cert_success);
                txt.setText("证书更新成功");
                break;
            case 3:
                ((TextView) findViewById(R.id.header_text)).setText("撤销完成");
                img.setImageResource(R.drawable.cert_success);
                txt.setText("证书撤销成功");
                break;


        }


    }

    public void finish(){
        Intent intent=new Intent(this,MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("type",type);
        startActivity(intent);
//        finish();

    }

    @Override
    public void onBackPressed() {
//		super.onBackPressed();
        finish();
    }
}
