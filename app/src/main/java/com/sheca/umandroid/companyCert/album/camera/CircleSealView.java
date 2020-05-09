package com.sheca.umandroid.companyCert.album.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.sheca.umandroid.R;


/**
 * @author xuchangqing
 * @time 2019/10/15 15:11
 * @descript
 */
public class CircleSealView extends View {
    private Paint paint;
    private Paint mBgPaint;
    private Paint mStrokePaint;

    public CircleSealView(Context context) {
        super(context);
        initView();
    }

    public CircleSealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CircleSealView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        paint = new Paint();
        mBgPaint = new Paint();
        mStrokePaint= new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setColor(Color.TRANSPARENT);
        mStrokePaint.setColor(Color.GREEN);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStrokeWidth(4);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setColor(getResources().getColor(R.color.camera_background));
        paint.setAntiAlias(true);
        mBgPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawRect(0,0,getWidth(),getHeight(),mBgPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 380, paint);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 384, mStrokePaint);

    }
}
