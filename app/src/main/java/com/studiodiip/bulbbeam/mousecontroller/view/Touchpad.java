package com.studiodiip.bulbbeam.mousecontroller.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment;

public class Touchpad extends FrameLayout implements View.OnTouchListener {
    private static float DISTANCE_NEARER_1_FROM_TOUCH;
    private static float DISTANCE_NEARER_2_FROM_TOUCH;
    private static float DISTANCE_NEAREST_FROM_TOUCH;
    private static float DISTANCE_NEAR_FROM_TOUCH;
    private static float DOT_OFFSET_X;
    private static float DOT_OFFSET_Y;
    private static float DOT_SPACING;
    private static float DOT_SPACING_HALF;
    private static float DOT_WIDTH;
    private static float MAX_DISTANCE_FROM_TOUCH;
    private static float MIN_DISTANCE_FROM_TOUCH;
    public boolean MTTouched = false;
    private Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.driehoek);
    private Bitmap bmp_size1;
    private Bitmap bmp_size2;
    private Bitmap bmp_size3;
    private Bitmap bmp_size4;
    private Bitmap bmp_size5;
    private float dotWidthPercentage = 0.6666667f;
    private boolean isTouchDown = false;
    private TouchpadListener listener;
    private float mx;
    private float my;
    public Paint paint;

    public interface TouchpadListener {
        void onTouchpadTouched(Touchpad touchpad, MotionEvent motionEvent);
    }

    public void setTouchpadListener(TouchpadListener listener2) {
        this.listener = listener2;
    }

    public Touchpad(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public Touchpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Touchpad(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        if (DOT_WIDTH == 0.0f) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            Log.i("", "metrics " + metrics.density);
            DOT_WIDTH = 3.0f * metrics.density;
            DOT_SPACING = 25.0f * metrics.density;
            DOT_SPACING_HALF = DOT_SPACING / 2.0f;
            DOT_OFFSET_X = 20.0f;
            DOT_OFFSET_Y = -10.0f;
            MAX_DISTANCE_FROM_TOUCH = 500.0f * metrics.density;
            MIN_DISTANCE_FROM_TOUCH = 17.0f * metrics.density;
            DISTANCE_NEAR_FROM_TOUCH = 80.0f * metrics.density;
            DISTANCE_NEARER_1_FROM_TOUCH = 60.0f * metrics.density;
            DISTANCE_NEARER_2_FROM_TOUCH = 45.0f * metrics.density;
            DISTANCE_NEAREST_FROM_TOUCH = 30.0f * metrics.density;
        }
        int width = (int) (DOT_WIDTH * 1.0f * this.dotWidthPercentage * 4.0f);
        this.bmp_size1 = Bitmap.createScaledBitmap(this.bitmap, width, width, true);
        int width2 = (int) (((double) DOT_WIDTH) * 1.25d * ((double) this.dotWidthPercentage) * 4.0d);
        this.bmp_size2 = Bitmap.createScaledBitmap(this.bitmap, width2, width2, true);
        int width3 = (int) (((double) DOT_WIDTH) * 1.5d * ((double) this.dotWidthPercentage) * 4.0d);
        this.bmp_size3 = Bitmap.createScaledBitmap(this.bitmap, width3, width3, true);
        int width4 = (int) (((double) DOT_WIDTH) * 2.0d * ((double) this.dotWidthPercentage) * 4.0d);
        this.bmp_size4 = Bitmap.createScaledBitmap(this.bitmap, width4, width4, true);
        int width5 = (int) (((double) DOT_WIDTH) * 2.5d * ((double) this.dotWidthPercentage) * 4.0d);
        this.bmp_size5 = Bitmap.createScaledBitmap(this.bitmap, width5, width5, true);
        this.paint = new Paint();
        this.paint.setColor(-1);
        this.paint.setStyle(Paint.Style.FILL);
        setOnTouchListener(this);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (float dy = DOT_SPACING_HALF; dy < ((float) canvas.getHeight()); dy += DOT_SPACING_HALF) {
            for (float dx = DOT_WIDTH / 2.0f; dx < ((float) canvas.getWidth()); dx += DOT_SPACING) {
                float newX = dx + DOT_OFFSET_X;
                float newY = dy + DOT_OFFSET_Y;
                float distance = MAX_DISTANCE_FROM_TOUCH;
                if (this.isTouchDown) {
                    float distanceX = this.mx - newX;
                    float distanceY = this.my - newY;
                    distance = (float) Math.sqrt((double) ((distanceX * distanceX) + (distanceY * distanceY)));
                    if (distance < MIN_DISTANCE_FROM_TOUCH) {
                        distance = MIN_DISTANCE_FROM_TOUCH;
                    }
                    newX = (float) Math.round(newX - (distanceX / distance));
                    newY = (float) Math.round(newY - (distanceY / distance));
                }
                if (dy % DOT_SPACING == 0.0f) {
                    newX += DOT_SPACING_HALF;
                }
                Bitmap cur_bmp = this.bmp_size1;
                float width = DOT_WIDTH;
                if (distance < DISTANCE_NEAREST_FROM_TOUCH) {
                    cur_bmp = this.bmp_size5;
                    width *= 3.0f;
                } else if (distance < DISTANCE_NEARER_2_FROM_TOUCH) {
                    cur_bmp = this.bmp_size4;
                    width *= 2.25f;
                } else if (distance < DISTANCE_NEARER_1_FROM_TOUCH) {
                    cur_bmp = this.bmp_size3;
                    width *= 1.75f;
                } else if (distance < DISTANCE_NEAR_FROM_TOUCH) {
                    cur_bmp = this.bmp_size2;
                    width = (float) (((double) width) * 1.5d);
                }
                if (this.MTTouched) {
                    canvas.drawBitmap(cur_bmp, newX - ((float) (cur_bmp.getWidth() / 2)), newY - ((float) (cur_bmp.getWidth() / 2)), this.paint);
                } else {
                    canvas.drawCircle(newX, newY, this.dotWidthPercentage * width, this.paint);
                }
            }
        }
    }

    public void setDotWidthPercentage(float percentage) {
        this.dotWidthPercentage = percentage;
        invalidate();
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (this.listener == null || TouchpadFragment.scrollbarTouched) {
            return false;
        }
        event.getActionMasked();
        int index = event.getActionIndex();
        if (event.getPointerCount() > 1) {
            int xPos = (int) event.getX(index);
            int yPos = (int) event.getY(index);
            this.listener.onTouchpadTouched(this, event);
        } else {
            int xPos2 = (int) event.getX(index);
            int yPos2 = (int) event.getY(index);
            if (event.getAction() == 0) {
                this.isTouchDown = true;
            } else if (event.getAction() == 1) {
                this.isTouchDown = false;
            }
            this.mx = event.getX();
            this.my = event.getY();
            invalidate(new Rect(0, 0, 0, 0));
            invalidate();
            this.listener.onTouchpadTouched(this, event);
        }
        return true;
    }
}
