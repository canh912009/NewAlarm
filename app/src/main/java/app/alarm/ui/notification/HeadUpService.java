package app.alarm.ui.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import app.alarm.R;

public class HeadUpService extends Service {
    private static final String TAG = "HeadUpService";

    private static final int HORIZONTAL_FLING_THRESHOLD = 50;

    private static final int EXPAND_NOTIFICATIONS_PANEL_THRESHOLD = 150;

    protected static final double SPRING_TENSION = 400;

    protected static final double SPRING_FRACTION = 20;

    private static final double SPRING_ENDVALUE = 1;

    private static final float SPRING_MOVE_DISTANCE = -50f;

    protected Context mContext;

    private GestureDetector mGestureDetector;

    private WindowManager.LayoutParams mWindowAttributes;

    private WindowManager mWindowManager;

    private HeadUpView mHeadUpNotificationView;

    protected boolean mIsAnimationRunning;

    private int mMinFlingVelocity;

    private float mStartPointX;

    private float mStartPointY;

    private float mViewPointX;

    private SpringSystem mSpringSystem = SpringSystem.create();


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mContext = this;
        ViewConfiguration viewConfig = ViewConfiguration.get(mContext);
        mMinFlingVelocity = viewConfig.getScaledMinimumFlingVelocity();

        showHeadUp();
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }

            int distanceX = Math.abs((int) e1.getX() - (int) e2.getX());
            int distanceY = Math.abs((int) e1.getY() - (int) e2.getY());

            if (distanceX < HORIZONTAL_FLING_THRESHOLD || distanceX < distanceY) {
                return false;
            }

            if (Math.abs(velocityX) > mMinFlingVelocity) {
                animateForSlideOut(velocityX < 0);
            }

            return false;
        }
    };

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "onTouch " + event);

            if (mGestureDetector != null) {
                mGestureDetector.onTouchEvent(event);
            }

            if (mIsAnimationRunning) {
                Log.d(TAG, "Animation is running...");

                if (mHeadUpNotificationView.getAlpha() == 0f) {
                    Log.d(TAG, "Animation is running and alpha is 0f");
                    return false;
                }

                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP:
                    resetViewLayout();
                    break;

                case MotionEvent.ACTION_DOWN:
                    mStartPointX = event.getRawX();
                    mStartPointY = event.getRawY();
                    mViewPointX = mWindowAttributes.x;
                    break;

                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) (event.getRawX() - mStartPointX);
                    int deltaY = (int) (event.getRawY() - mStartPointY);
                    mWindowAttributes.x = (int) (mViewPointX + deltaX);
                    if (mWindowManager != null && mHeadUpNotificationView != null) {
                        final float maxX = (float) getMaxX();
                        final float absX = Math.abs((float) mWindowAttributes.x);
                        float alpha = 1f;
                        if (absX > 0f && absX < maxX) {
                            final float ratio = absX / maxX;
                            if (ratio > 0.4f) {
                                animateForSlideOut(mWindowAttributes.x < 0);
                                break;
                            }
                            alpha = 1f - ratio;
                        }
                        mHeadUpNotificationView.setAlpha(alpha);
                        if (deltaY > Math.abs(deltaX)
                                && deltaY > EXPAND_NOTIFICATIONS_PANEL_THRESHOLD) {
                        }
                    }
                    updateViewLayout();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void resetViewLayout() {
        if (mHeadUpNotificationView != null) {
            mHeadUpNotificationView.setAlpha(1f);
        }

        setDefaultPosition();
        updateViewLayout();
    }

    private void setDefaultPosition() {
        getLayoutParams().x = 0;
        getLayoutParams().y = 0;
    }

    protected void showHeadUp() {
        Log.d(TAG, "showHeadUp");

        mHeadUpNotificationView = new HeadUpView(this);
        mWindowAttributes = createLayoutParams();
        onCreateCustomView(mHeadUpNotificationView);
        setDefaultPosition();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mHeadUpNotificationView, mWindowAttributes);

        final ViewTreeObserver observer = mHeadUpNotificationView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                animateForShow();

                // Remove the listener so we don't continually re-layout.
                if (mHeadUpNotificationView != null) {
                    ViewTreeObserver observer = mHeadUpNotificationView.getViewTreeObserver();

                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });

        mHeadUpNotificationView.setOnTouchListener(mViewTouchListener);
        mGestureDetector = new GestureDetector(this, mGestureListener);
    }

    private class HeadUpView extends RelativeLayout {
        public HeadUpView(HeadUpService context) {
            super(context);
        }
    }

    private void updateViewLayout() {
        if ((mWindowManager != null) && (mHeadUpNotificationView != null)) {
            mWindowManager.updateViewLayout(mHeadUpNotificationView, mWindowAttributes);
        }
    }

    public void animateForSlideOut(boolean toLeft) {
        if (mHeadUpNotificationView != null) {
            mIsAnimationRunning = true;
            Log.d(TAG, "animateForSlideOut:" + toLeft);
            final int duration = getResources()
                    .getInteger(R.integer.head_up_notification_slide_out_duration);
            float destination = toLeft ? -(float) getMaxX() : (float) getMaxX();
            ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.TRANSLATION_X, destination);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.ALPHA, 0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(moveAnimator).with(alphaAnimator);
            // animatorSet.setInterpolator(AnimUtils.EASE_OUT);
            animatorSet.setDuration(duration).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimationRunning = false;
                    // stopSelf();
                }
            });
            animatorSet.start();
        }
    }

    public void animateForShow() {
        if (mHeadUpNotificationView != null) {
            mHeadUpNotificationView.setVisibility(View.VISIBLE);
            mIsAnimationRunning = true;
            Log.d(TAG, "animateForShow");
            final int duration = 257;
            final float height = mHeadUpNotificationView.getHeight();
            mHeadUpNotificationView.setTranslationY(-height);
            mHeadUpNotificationView.setAlpha(0f);
            ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.TRANSLATION_Y, 0f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.ALPHA, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(moveAnimator).with(alphaAnimator);
            // animatorSet.setInterpolator(AnimUtils.EASE_IN);
            animatorSet.setDuration(duration).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d(TAG, "onAnimationEnd");
                    if (mHeadUpNotificationView != null) {
                        mHeadUpNotificationView.setTranslationY(0f);
                        mHeadUpNotificationView.setAlpha(1f);
                    }
                    mIsAnimationRunning = false;
                    doAfterShowAnimation();
                }
            });
            animatorSet.start();
        }
    }

    private void doAfterShowAnimation() {
        SpringConfig config = new SpringConfig(HeadUpService.SPRING_TENSION,
                HeadUpService.SPRING_FRACTION);
        Spring spring = mSpringSystem.createSpring();
        spring.setSpringConfig(config);
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {

                float value = (float) spring.getCurrentValue();
                float moveDistance = SPRING_MOVE_DISTANCE;
                if (mHeadUpNotificationView != null) {
                    mHeadUpNotificationView
                            .setTranslationY(-1 * SPRING_MOVE_DISTANCE + moveDistance * value);
                }
            }
        });

        spring.setEndValue(HeadUpService.SPRING_ENDVALUE);
    }

    public void animateForHide() {
        if (mHeadUpNotificationView != null) {
            mIsAnimationRunning = true;
            Log.d(TAG, "animateForHide");
            final int duration = 247;
            final float height = mHeadUpNotificationView.getHeight();
            ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.TRANSLATION_Y, -height);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mHeadUpNotificationView,
                    View.ALPHA, 0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(moveAnimator).with(alphaAnimator);
            // animatorSet.setInterpolator(AnimUtils.EASE_OUT);
            animatorSet.setDuration(duration).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimationRunning = false;

                    if (mHeadUpNotificationView != null && mHeadUpNotificationView.isShown()) {
                        mHeadUpNotificationView.setVisibility(View.INVISIBLE);
                    }

                    // stopSelf();
                }
            });
            animatorSet.start();
        }
    }

    private WindowManager.LayoutParams createLayoutParams() {
        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGBA_8888);
        params.gravity = Gravity.TOP;

        params.setTitle(getClass().getName());

        return params;
    }

    public Context getContext() {
        return mContext;
    }

    private WindowManager.LayoutParams getLayoutParams() {
        return mWindowAttributes;
    }

    private int getMaxX() {
        DisplayMetrics matrix = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(matrix);
        return matrix.widthPixels;
    }

    protected void onCreateCustomView(ViewGroup root) {
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (mHeadUpNotificationView != null) {
            mHeadUpNotificationView.setVisibility(View.INVISIBLE);
        }

        removeHeadUpNotification();
        super.onDestroy();
    }

    protected void removeHeadUpNotification() {
        Log.d(TAG, "removeHeadUpNotification");

        if (mWindowManager != null) {
            mWindowManager.removeView(mHeadUpNotificationView);
        }

        mWindowManager = null;

        if (mHeadUpNotificationView != null) {
            mHeadUpNotificationView.setOnTouchListener(null);
            mHeadUpNotificationView.removeAllViews();
        }

        mHeadUpNotificationView = null;
        mGestureDetector = null;
    }
}
