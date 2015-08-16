package com.crispkeys.imageviewtest;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Behzodbek Qodirov on 8/15/15.
 */
public class AnimatedView extends FrameLayout {

    private static final long MAX_ANIMATION_DURATION = 1000;
    private static final long MIN_ANIMATION_DURATION = 100;
    private static final long MIN_HOLD_DURATION = 2000;
    private long mDuration = MIN_ANIMATION_DURATION;
    private long mHoldDuration = 2000;
    //Timer
    private ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;

    //Current page index
    private int currentPageIndex;

    private View previousView;
    private View currentView;
    private View nextView;

    //Animation Queue
    private Queue<OnViewOutingAnimation> mAnimationQueue = new LinkedList<>();
    private BaseAdapter mAdapter;

    public AnimatedView(Context context) {
        super(context);
    }

    public AnimatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Matrix matrix = new Matrix();

        super.dispatchDraw(canvas);
    }

    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
        init();
    }

    public void setAnimationDuration(long duration) {
        if (duration < MIN_ANIMATION_DURATION || duration > MAX_ANIMATION_DURATION) {
            throw new IllegalArgumentException("Wrong animation duration argument. Duration must be within " +
                MIN_ANIMATION_DURATION + "-" + MAX_ANIMATION_DURATION);
        }
        mDuration = duration;
    }

    private void init() {
        checkAdapter();
        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(new ViewChangerRunnable(), 0, mHoldDuration,
            TimeUnit.MILLISECONDS);
    }

    public void setViewHangingPeriod(long hangingPeriod) {
        if (mScheduledFuture != null) {
            throw new IllegalStateException("You have to set hanging period before setting adapter");
        }
        mHoldDuration = hangingPeriod;
    }

    public int getCurrentPageIndex() {
        checkAdapter();
        return currentPageIndex;
    }

    private int getNextPageIndex() {
        checkAdapter();
        if (currentPageIndex == mAdapter.getCount() - 1) {
            return 0;
        }
        return currentPageIndex + 1;
    }

    private int getPreviousPageIndex() {
        checkAdapter();
        if (currentPageIndex == 0) {
            return mAdapter.getCount() - 1;
        }
        return currentPageIndex - 1;
    }

    private void checkAdapter() {
        if (mAdapter == null) {
            throw new NullPointerException("Adapter might now be null");
        }
    }

    private class ViewChangerRunnable implements Runnable {

        @Override
        public void run() {
            checkAdapter();

            previousView = currentView;
            currentView = nextView;
            nextView = mAdapter.getView(getCurrentPageIndex());

            previousView.setDrawingCacheEnabled(true);
            previousView.buildDrawingCache();
            final Bitmap bm = previousView.getDrawingCache();

            final OnViewOutingAnimation onViewOutingAnimation = mAnimationQueue.poll();
            mAnimationQueue.offer(onViewOutingAnimation);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1).setDuration(mDuration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    onViewOutingAnimation.onViewOuting(bm, value);
                }
            });
            valueAnimator.start();
        }
    }
}
