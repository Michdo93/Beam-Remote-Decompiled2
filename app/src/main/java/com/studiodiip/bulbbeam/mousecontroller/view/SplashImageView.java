package com.studiodiip.bulbbeam.mousecontroller.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.studiodiip.bulbbeam.mousecontroller.R;

public class SplashImageView extends AppCompatImageView {
    public SplashImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SplashImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SplashImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Log.i("SplashImageView", "init");
    }

    private void updateAnimationsState() {
        if (getVisibility() == View.VISIBLE && hasWindowFocus()) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    public void startAnimation() {
        setBackgroundResource(R.drawable.splash);
        updateAnimationState(getBackground(), true);
    }

    public void stopAnimation() {
        updateAnimationState(getBackground(), false);
        setBackgroundResource(R.drawable.splash_00045);
    }

    public boolean isRunning() {
        Drawable drawable = getDrawable();
        if (drawable instanceof AnimationDrawable) {
            return ((AnimationDrawable) drawable).isRunning();
        }
        return false;
    }

    private void updateAnimationState(Drawable drawable, boolean running) {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (running) {
                animationDrawable.start();
            } else {
                animationDrawable.stop();
            }
        }
    }
}
