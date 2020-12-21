package com.sheca.umee;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.sheca.thirdparty.lockpattern.ui.ValidateGestureFragment;

public class ValidateGestureActivity extends BaseActivity2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.mine_validate_gesture,R.layout.activity_validate_gesture);
        loadValidateGuestureFragment();
    }

    @SuppressLint("ResourceType")
    public void loadValidateGuestureFragment(){
        ValidateGestureFragment fragment = new ValidateGestureFragment();
        fragment.setGestureListener(new ValidateGestureFragment.OnGestureListener() {
            @Override
            public void onResult(boolean isSuccess) {
                //此处返回验证结果，true表示验证通过
                if (isSuccess){
                    Toast.makeText(getApplicationContext(), com.sheca.thirdparty.R.string.validate_correct, Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    ValidateGestureActivity.this.setResult(RESULT_OK, resultIntent);
                    ValidateGestureActivity.this.finish();
                }else{
                    Toast.makeText(getApplicationContext(), com.sheca.thirdparty.R.string.validate_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }
}
