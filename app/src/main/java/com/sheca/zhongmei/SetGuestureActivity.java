package com.sheca.zhongmei;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.sheca.thirdparty.lockpattern.ui.CreateGestureFragment;

public class SetGuestureActivity extends BaseActivity2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavBar(R.string.mine_set_gesture,R.layout.activity_set_guesture);
        loadSetGuestureFragment();
    }

    @SuppressLint("ResourceType")
    public void loadSetGuestureFragment(){
        CreateGestureFragment fragment = new CreateGestureFragment();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }
}
