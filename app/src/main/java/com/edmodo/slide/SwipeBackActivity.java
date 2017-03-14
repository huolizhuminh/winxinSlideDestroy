package com.edmodo.slide;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

/**
 * Created by minhui.zhu on 2017/3/14.
 */

public class SwipeBackActivity extends AppCompatActivity {
    private TouchHelper mTouchHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mTouchHelper == null)
            mTouchHelper = new TouchHelper(getWindow());
        boolean consume = mTouchHelper.processTouchEvent(ev);
        if (!consume) return super.dispatchTouchEvent(ev);
        return false;
        //return super.dispatchTouchEvent(ev)||mTouchHelper.processTouchEvent(ev);
    }


}