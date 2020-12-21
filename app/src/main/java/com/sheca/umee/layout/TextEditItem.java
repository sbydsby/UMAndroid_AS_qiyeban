package com.sheca.umee.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sheca.umee.R;

public class TextEditItem extends LinearLayout {

    private TextView textView;
    private EditText editText;

    public TextEditItem(Context context) {
        super(context);
        init(context, null);
    }

    public TextEditItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
	public TextEditItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
    public TextEditItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.text_edit_item, this, true);
        textView = (TextView)findViewById(R.id.lefttext);
        editText = (EditText)findViewById(R.id.edittext);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TextEditItem);
        if (null != attributes) {
            int bgColor = attributes.getResourceId(R.styleable.TextEditItem_teitem_background_color, android.R.color.white);
            setBackgroundColor(getResources().getColor(bgColor));

            int mainText = attributes.getResourceId(R.styleable.TextEditItem_tv_text, 0);
            int mainTextColor = attributes.getResourceId(R.styleable.TextEditItem_tv_text_color, android.R.color.black);
            int mainTextSize = attributes.getResourceId(R.styleable.TextEditItem_tv_text_size, 18);

            if (mainText == 0) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(mainText);
                textView.setTextColor(getResources().getColor(mainTextColor));
                textView.setTextSize(mainTextSize);
            }

            int hint = attributes.getResourceId(R.styleable.TextEditItem_edit_hint, 0);
            int textColor = attributes.getResourceId(R.styleable.TextEditItem_edit_text_color, android.R.color.darker_gray);
            int textSize = attributes.getResourceId(R.styleable.TextEditItem_edit_text_size, 18);

            if (0 != hint){
                editText.setHint(hint);
            }

            editText.setTextColor(getResources().getColor(textColor));
            editText.setTextSize(textSize);

        }

        attributes.recycle();
    }

    public TextView getTextView(){
        return textView;
    }

    public EditText getEditText(){
        return editText;
    }

    public String getInputValue(){
        return editText.getText().toString().trim();
    }

}
