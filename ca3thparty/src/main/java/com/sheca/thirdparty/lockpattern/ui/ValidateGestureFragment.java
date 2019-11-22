package com.sheca.thirdparty.lockpattern.ui;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sheca.thirdparty.R;
import com.sheca.thirdparty.lockpattern.util.LockPatternUtil;
import com.sheca.thirdparty.lockpattern.widget.LockPatternView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("NewApi") public class ValidateGestureFragment extends Fragment {

    LockPatternView lockPatternView;

    TextView messageTv;

    private static final String TAG = "ValidateGestureFragment";

    private OnGestureListener listener;

    public ValidateGestureFragment() {
        // Required empty public constructor
    }

    public void setGestureListener(OnGestureListener listener){
        this.listener = listener;
    }

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
            boolean flag = LockPatternUtil.checkLocalPattern(getContext(),pattern);

            if (flag) {
//                Toast.makeText(getContext(), R.string.validate_correct, Toast.LENGTH_SHORT).show();
            } else {
                //与上次不一致
//                Toast.makeText(getContext(), R.string.validate_fail, Toast.LENGTH_SHORT).show();
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
            }

            if (null != listener){
                listener.onResult(flag);
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_validate_gesture, container, false);
    }

    @SuppressLint("NewApi") @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lockPatternView = (LockPatternView)view.findViewById(R.id.lockPatternView);
        messageTv = (TextView)view.findViewById(R.id.messageTv);
        lockPatternView.setOnPatternListener(patternListener);
    }


    public interface OnGestureListener{
        void onResult(boolean isSuccess);
    }
}
