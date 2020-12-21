package com.sheca.umee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.sheca.umee.util.PermissionUtil;


public class CSActivity extends BaseActivity2 {
    public static int RequestCallPhone=1006;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.mine_cs,R.layout.activity_cs);

        //热线电话
        findViewById(R.id.cs_hotline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PermissionUtil mPerUtils = new PermissionUtil(CSActivity.this);
                boolean flag = mPerUtils.requestCallPhone(RequestCallPhone);
                if(flag){
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ "021-962600".replace("-", "")));
                    startActivity(intent);
                }

//                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ "021-962600".replace("-", "")));
//                startActivity(intent);
            }
        });

        //服务网点
        findViewById(R.id.cs_shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CSActivity.this, NetworkOnlineActivity.class);
                startActivity(intent);
            }
        });

        //在线客服
        findViewById(R.id.cs_online).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CSActivity.this, MeChatActivity.class);
                startActivity(intent);
            }
        });

        //常见问题
        findViewById(R.id.cs_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CSActivity.this, FAQsActivity.class);
                startActivity(intent);
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==RequestCallPhone) {
            if (grantResults[0] == 0) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ "021-962600".replace("-", "")));
                startActivity(intent);
            }
        }
    }
}
