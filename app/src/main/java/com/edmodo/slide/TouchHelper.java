package com.edmodo.slide;

/**
 * Created by minhui.zhu on 2017/3/14.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;

public class TouchHelper {
    private boolean isIdle = true;
    private boolean isSlinding = false;
    private boolean isAnimating = false;
    private Window mWindow;
    private ViewGroup preContentView;
    private ViewGroup curContentView;
    private ViewGroup curView;
    private ViewGroup preView;
    private Activity preActivity;

    //左边触发的宽度
    private int triggerWidth = 50;
    //阴影宽度
    private int SHADOW_WIDTH = 30;

    public TouchHelper(Window window) {
        mWindow = window;
    }

    private Context getContext() {
        return mWindow.getContext();
    }

    //决定是否拦截事件
    public boolean processTouchEvent(MotionEvent event) {
        if (isAnimating) return true;
        float x = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x <= triggerWidth) {
                    isIdle = false;
                    isSlinding = true;
                    startSlide();
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (isSlinding) return true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSlinding) {
                    if (event.getActionIndex() != 0) return true;
                    sliding(x);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isSlinding) return false;
                int width = getContext().getResources().getDisplayMetrics().widthPixels;
                isAnimating = true;
                isSlinding = false;
                startAnimating(width / x <= 3, x);
                return true;
            default:
                break;
        }
        return false;
    }

    private void startSlide() {
        preActivity = ((MyApplication) getContext().getApplicationContext()).getHelper().getPreActivity();
        if (preActivity == null) return;
        preContentView = (ViewGroup) preActivity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        preView = (ViewGroup) preContentView.getChildAt(0);
        preContentView.removeView(preView);
        curContentView = (ViewGroup) mWindow.findViewById(Window.ID_ANDROID_CONTENT);
        curView = (ViewGroup) curContentView.getChildAt(0);
        preView.setX(-preView.getWidth() / 3);
        curContentView.addView(preView, 0);
        //        if(mShadowView==null){
//            mShadowView=new ShadowView(getContext());
//        }
//        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
//        curContentView.addView(mShadowView,1,params);
//        mShadowView.setX(-SHADOW_WIDTH);
    }

    private void sliding(float rawX) {
        if (preActivity == null) return;
        curView.setX(rawX);
        preView.setX(-preView.getWidth() / 3 + rawX / 3);
        //mShadowView.setX(-SHADOW_WIDTH+rawX);
    }

    private void startAnimating(final boolean isFinishing, float x) {
        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        ValueAnimator animator = ValueAnimator.ofFloat(x, isFinishing ? width : 0);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                sliding((Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                doEndWorks(isFinishing);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    private void doEndWorks(boolean isFinishing) {
        if (preActivity == null) return;
        if (isFinishing) {
            //更改当前activity的底view为preView,防止当前activity finish时的白屏闪烁
            BackView view = new BackView(getContext());
            view.cacheView(preView);
            curContentView.addView(view, 0);
        }
        //curContentView.removeView(mShadowView);
        if (curContentView == null || preContentView == null) return;
        curContentView.removeView(preView);
        preContentView.addView(preView);
        if (isFinishing) {
            ((Activity) getContext()).finish();
            ((Activity) getContext()).overridePendingTransition(0, 0);
        }
        isAnimating = false;
        isSlinding = false;
        isIdle = true;
        preView = null;
        curView = null;
    }

    class BackView extends View {

        private View mView;

        public BackView(Context context) {
            super(context);
        }

        public void cacheView(View view) {
            mView = view;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mView != null) {
                mView.draw(canvas);
                mView = null;
            }
        }
    }
}
