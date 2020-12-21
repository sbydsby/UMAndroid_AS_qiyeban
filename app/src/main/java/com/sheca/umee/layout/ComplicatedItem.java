package com.sheca.umee.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sheca.umee.R;

public class ComplicatedItem extends LinearLayout {

    ImageView IvLeftIcon, IvRightIcon;
    TextView tvMainText, tvSubText;
    View itemWhole;

    public ComplicatedItem(Context context) {
        super(context);
        init(context, null);
    }

    public ComplicatedItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
	public ComplicatedItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
    public ComplicatedItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.complicated_item, this, true);
        itemWhole = findViewById(R.id.item_layout_whole);

        IvLeftIcon = (ImageView)findViewById(R.id.item_LeftIcon);
        IvRightIcon = (ImageView)findViewById(R.id.item_RightIcon);
        tvMainText = (TextView)findViewById(R.id.item_mainText);
        tvSubText = (TextView)findViewById(R.id.item_subText);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ComplicatedItem);
        if (null != attributes) {
            int bgColor = attributes.getResourceId(R.styleable.ComplicatedItem_item_background_color, android.R.color.white);
            setBackgroundColor(getResources().getColor(bgColor));

            int leftIconD = attributes.getResourceId(R.styleable.ComplicatedItem_left_icon_drawable, -1);
            if (-1 != leftIconD) {
                IvLeftIcon.setImageResource(leftIconD);
            }else{
                IvLeftIcon.setVisibility(View.GONE);
            }

            int rightIconD = attributes.getResourceId(R.styleable.ComplicatedItem_right_icon_drawable, -1);
            if (-1 != rightIconD) {
                IvRightIcon.setImageResource(rightIconD);
            }else{
                IvRightIcon.setVisibility(View.GONE);
            }

            int mainText = attributes.getResourceId(R.styleable.ComplicatedItem_main_text,0);
            int mainTextColor = attributes.getResourceId(R.styleable.ComplicatedItem_main_text_color,android.R.color.black);
            int mainTextSize = attributes.getResourceId(R.styleable.ComplicatedItem_main_text_size,18);
            if (mainText == 0){
                tvMainText.setVisibility(View.GONE);
            }else{
                tvMainText.setText(mainText);
                tvMainText.setTextColor(getResources().getColor(mainTextColor));
                tvMainText.setTextSize(mainTextSize);
            }

            int subText = attributes.getResourceId(R.styleable.ComplicatedItem_sub_text,0);
            int subTextColor = attributes.getResourceId(R.styleable.ComplicatedItem_sub_text_color,android.R.color.darker_gray);
            int subTextSize = attributes.getResourceId(R.styleable.ComplicatedItem_sub_text_size,15);
            if (subText == 0){
                tvSubText.setVisibility(View.GONE);
            }else{
                tvSubText.setText(subText);
                tvSubText.setTextColor(getResources().getColor(subTextColor));
                tvSubText.setTextSize(subTextSize);
            }

        }

        attributes.recycle();
    }


    public TextView getLeftTextView(){
        return tvMainText;
    }

    public TextView getRighttTextView(){
        return tvSubText;
    }

}
