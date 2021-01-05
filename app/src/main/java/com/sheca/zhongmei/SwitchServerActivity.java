package com.sheca.zhongmei;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sheca.zhongmei.util.AccountHelper;
import com.sheca.zhongmei.util.CommonConst;


public class SwitchServerActivity extends Activity {
    EditText umsp_address;
    EditText ucm_address;
    TextView txt_address;
    LinearLayout ll_normal, ll_diy;
    ImageView img_normal, img_diy;

//    boolean isDiy = false;

    Button txt_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_switch_server);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("设置服务器");
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        ((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
        TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();

        txt_address = findViewById(R.id.txt_address);
        txt_address.setText(CommonConst.UM_SERVER);

        ll_normal = findViewById(R.id.ll_normal);
        ll_diy = findViewById(R.id.ll_diy);
        img_normal = findViewById(R.id.img_normal);
        img_diy = findViewById(R.id.img_diy);

        txt_btn = findViewById(R.id.txt_btn);
        txt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isDiy = true;
                umsp_address.setText(getResources().getString(R.string.UMSP_Base_Service));
            }
        });


        ll_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isDiy = false;
//                img_normal.setVisibility(View.VISIBLE);
//                img_diy.setVisibility(View.INVISIBLE);
                AccountHelper.setUMAddress(SwitchServerActivity.this, CommonConst.UM_SERVER);
                finish();

            }
        });
        ll_diy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isDiy = true;
//                img_normal.setVisibility(View.INVISIBLE);
//                img_diy.setVisibility(View.VISIBLE);
            }
        });


        tp.setFakeBoldText(true);

        ImageButton cancelScanButton = (ImageButton) this
                .findViewById(R.id.btn_goback);
        cancelScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check()) {
                    finish();
                }
            }
        });

        findViewById(R.id.default_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                AccountHelper.setUMSPAddress(SwitchServerActivity.this, CommonConst.UM_APP_UMSP_SERVER);
//                AccountHelper.setUCMAddress(SwitchServerActivity.this, CommonConst.UM_APP_UCM_SERVER);
                finish();
            }
        });

        umsp_address = findViewById(R.id.umsp_address);
        ucm_address = findViewById(R.id.ucm_address);
//        umsp_address.setText(AccountHelper.getUMAddress(this));
//        ucm_address.setText(AccountHelper.getUCMAddress(this));
        Button okBtn = ((Button) findViewById(R.id.btn_loign_ok));
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check()) {
                    finish();
                }

            }
        });

        umsp_address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
//                    isDiy = true;
//                    img_normal.setVisibility(View.INVISIBLE);
//                    img_diy.setVisibility(View.VISIBLE);

                }
            }
        });
        umsp_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                isDiy = true;
//                check();

            }
        });

//        if (!CommonConst.UM_APP_UMSP_SERVER.equals(AccountHelper.getUMSPAddress(this))) {
//            isDiy = true;
        umsp_address.setText(AccountHelper.getUMSPAddress(this));
//            img_normal.setVisibility(View.INVISIBLE);
//            img_diy.setVisibility(View.VISIBLE);
//        }
    }


    private boolean check() {
//        umsp_address.setError(null);
        String umspAddress = umsp_address.getText().toString().trim();
//        String ucmAddress = ucm_address.getText().toString().trim();
        if (umspAddress.length() == 0) {
            umsp_address.setError("服务器地址不能为空");

            return false;
        } else {
            umsp_address.setError("");
        }

        if (umspAddress.startsWith("https") || umspAddress.startsWith("http")) {
            umsp_address.setError(null);
        } else {
            umsp_address.setError("服务器地址必须以http或https开头");
//            Toast.makeText(this, "UMSP服务地址必须以http或https开头", Toast.LENGTH_LONG).show();
            return false;
        }
//        if ((ucmAddress.startsWith("http") || ucmAddress.startsWith("https"))) {
//
//        } else {
//            Toast.makeText(this, "UCM服务地址必须以http或https开头", Toast.LENGTH_LONG).show();
//            return;
//        }

        AccountHelper.setUMSPAddress(SwitchServerActivity.this, umsp_address.getText().toString().trim());
//        AccountHelper.setUCMAddress(SwitchServerActivity.this, ucm_address.getText().toString().trim());


//        finish();
        return true;
    }


}
