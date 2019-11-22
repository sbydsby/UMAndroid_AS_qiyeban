package com.sheca.thirdparty.lockpattern.ui;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.thirdparty.R;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;
import com.sheca.thirdparty.lockpattern.widget.LockPatternIndicator;
import com.sheca.thirdparty.lockpattern.widget.LockPatternView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("NewApi") public class CreateGestureFragment extends Fragment {


    LockPatternIndicator lockPatternIndicator;

    LockPatternView lockPatternView;

    TextView messageTv;

    boolean isFirstDraw = true;

    byte[] firstResult = null;

    private static final String TAG = "CreateGestureFragment";

    /**
     * 手势监听
     */
    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
            //updateStatus(Status.DEFAULT, null);
            lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
        }

        @SuppressLint("NewApi")
        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
        	if (null==pattern || pattern.size()<4){
                 Toast.makeText(getContext(), R.string.create_gesture_less_error, Toast.LENGTH_SHORT).show();
                 lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                 return;
            }
     	
            String value = LockPatternUtil.getPatternValue(pattern);
            Log.d(TAG, "onPatternDetected = " + value);
            //需要输入2遍，第二次才保存。
            if (isFirstDraw) {
                firstResult = LockPatternUtil.patternToHash(pattern);
                lockPatternIndicator.setIndicator(pattern);
                messageTv.setText(R.string.create_gesture_correct);
                isFirstDraw = false;
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
            } else {
                String second = LockPatternUtil.getPatternValue(pattern);
                if (LockPatternUtil.checkPattern(pattern,firstResult)) {
                    LockPatternUtil.savePattern(getContext(), second);
                    Toast.makeText(getContext(), R.string.create_gesture_confirm_correct, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else {
                    //与上次不一致
                    Toast.makeText(getContext(), R.string.create_gesture_confirm_error, Toast.LENGTH_SHORT).show();
                    lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                }
            }
        }
    };

    public CreateGestureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_gesture, container, false);
    }

    @SuppressLint("NewApi") @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lockPatternIndicator = (LockPatternIndicator)view.findViewById(R.id.lockPatterIndicator);
        lockPatternView = (LockPatternView)view.findViewById(R.id.lockPatternView);
        messageTv = (TextView)view.findViewById(R.id.messageTv);

        lockPatternView.setOnPatternListener(patternListener);
    }
}
