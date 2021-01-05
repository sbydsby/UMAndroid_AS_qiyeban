package com.sheca.zhongmei;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @author xuchangqing
 * @time 2019/4/19 14:16
 * @descript  证书工具类测试页面
 */
public class CertTestActivity extends Activity implements View.OnClickListener {

    private TextView testEnumCert;
    private TextView mContent;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_test);
        initView();
    }

    private void initView() {
        testEnumCert=findViewById(R.id.test_enumCertIDs);
        mContent =findViewById(R.id.tv_content);

        testEnumCert.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.test_enumCertIDs:
                getCertList();
                break;
        }
    }

    private void getCertList() {
//        new CertUtils(CertTestActivity.this,null, CertEnum.GetCertList, new CertCallBack<Cert>() {
//            @Override
//            public void certCallBack(String strVal) {
//                if(strVal!=null && !strVal.equals("")){
//                    mContent.setText(strVal);
//                    Log.e("CERT_UTILS",strVal);
//                }else{
//                    mContent.setText("错误");
//                    Log.e("CERT_UTILS","错误");
//                }
//
//            }
//
//            @Override
//            public void certCallBackforList(List<Cert> mList) {
//
//            }
//        });

    }
}
