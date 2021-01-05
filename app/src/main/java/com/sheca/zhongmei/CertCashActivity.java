package com.sheca.zhongmei;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sheca.zhongmei.dao.CertDao;
import com.sheca.zhongmei.model.Cert;
import com.sheca.zhongmei.util.CertCash;
import com.sheca.zhongmei.util.CommUtil;

import java.util.Hashtable;

public class CertCashActivity extends Activity {//证书开票

    int certID = 0;
    ImageView img;


    Bitmap bitmap=null;

    private int height=500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_cert_cash);


        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        ((TextView) findViewById(R.id.header_text)).setText("证书开票");
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
                CertCashActivity.this.finish();
            }
        });

        img = findViewById(R.id.img);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "证书开票", "证书开票");
                Toast.makeText(CertCashActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("CertId") != null) {
                certID = Integer.parseInt(extras.getString("CertId"));

                CertDao certDao = new CertDao(this);
                Cert mCert = certDao.getCertByID(certID);
                if (mCert != null) {
                    String CertSn = mCert.getCertsn().toUpperCase();
                    String UMSP_Cert_Cash=getResources().getString(R.string.UMSP_Cert_Cash);
                  String result=  CertCash.getQrCodeResult(CertSn,UMSP_Cert_Cash);
                    bitmap=  createQRImage(result, CommUtil.dip2px(this,height),CommUtil.dip2px(this,height));
                    img.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(CertCashActivity.this, "证书不存在", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }


    }


//private void makePic(String content){
//    HashMap hashMap = new HashMap();
//    // 设置二维码字符编码
//    hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//    // 设置二维码纠错等级
//    hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
//    // 设置二维码边距
////    hashMap.put(EncodeHintType., 2);
//
//    try {
//        // 开始生成二维码
//        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hashMap);
//        // 导出到指定目录
//        MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, new File("D://erweima.png").toPath());
//    } catch (WriterException e) {
//        e.printStackTrace();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//
//
//}

    public static Bitmap createQRImage(String context, final int width, final int height) {
        try {
            // 判断URL合法性
            if (context == null || "".equals(context) || context.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(context,BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onDestroy() {

        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
        super.onDestroy();
    }
}
