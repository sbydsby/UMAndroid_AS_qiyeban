package com.sheca.umee.companyCert;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author xuchangqing
 * @time 2019/10/18 13:54
 * @descript
 */
public class BitmapUtils {
    /**
     * path to Bitmap
     */
    public static Bitmap uriToBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }


    /**
     * 裁剪成圆形
     *
     * @param source
     * @return
     */
    public static Bitmap getCircleBitmap(Bitmap source) {
        int diameter = source.getWidth() < source.getHeight() ? source.getWidth() : source.getHeight();
        Bitmap target = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        target.setHasAlpha(true);
        Canvas canvas = new Canvas(target);//创建画布
//        Paint mBgPaint = new Paint();
//        mBgPaint.setAntiAlias(true);
//        mBgPaint.setColor(Color.TRANSPARENT);
//        mBgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//        canvas.drawRect(0,0,diameter,diameter,mBgPaint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);//绘制圆形
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//取相交裁剪
        canvas.drawBitmap(source, 0, 0, paint);

        return target;
    }

    /**
     * Bitmap 转file
     */


    public static File bitmapToFile(Context context, Bitmap bitmap) {


        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "sheca_seal_pic" + ".png");


        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        recycleBitmap(bitmap);
        return file;
    }


    public static void recycleBitmap(Bitmap... bitmaps) {
        if (bitmaps == null) {
            return;
        }
        for (Bitmap bm : bitmaps) {
            if (null != bm && !bm.isRecycled()) {
                bm.recycle();
            }
        }

    }


}
