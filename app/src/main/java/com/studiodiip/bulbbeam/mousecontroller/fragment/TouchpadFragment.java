package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.view.Touchpad;

public class TouchpadFragment extends Fragment {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    public static boolean scrollbarTouched = false;
    private static Typeface tf;
    private ImageButton btnMultiTouch;
    private float defaultScrollBarPos;
    float diffY = 0.0f;
    private Handler h;
    private TextView holdToGrab;
    private MTListener mtListener;
    private View scrollBar;
    private ScrollbarListener scrollbarListener;
    private boolean touched = false;
    private Touchpad touchpad;
    private float touchpadHeigt;

    public interface MTListener {
        void onMTTouched(MotionEvent motionEvent);
    }

    public interface ScrollbarListener {
        void onScroll(int i);
    }

    public static Typeface getTf(Context ctx) {
        if (tf == null) {
            tf = Typeface.createFromAsset(ctx.getAssets(), "apercu-regular-webfont.ttf");
        }
        return tf;
    }

    public void setScrollbarListener(ScrollbarListener scrollbarListener2) {
        this.scrollbarListener = scrollbarListener2;
    }

    public void setMTListener(MTListener listener) {
        this.mtListener = listener;
    }

    public static TouchpadFragment newInstance() {
        TouchpadFragment f = new TouchpadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_KEY_FEED_ITEM, 1);
        f.setArguments(args);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getArguments();
        }
        this.h = new Handler();
        this.touched = false;
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_touchpad, container, false);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.AnonymousClass1 */

            public void onGlobalLayout() {
                if (!TouchpadFragment.this.touched) {
                    View scrollBar = v.findViewById(R.id.scrollbar);
                    TouchpadFragment.this.touchpadHeigt = (float) v.getHeight();
                    TouchpadFragment.this.defaultScrollBarPos = scrollBar.getY();
                    Log.d("scrollbar", "defaultScrollBarPos: " + TouchpadFragment.this.defaultScrollBarPos);
                }
            }
        });
        this.touchpad = (Touchpad) v.findViewById(R.id.touchpad);
        this.btnMultiTouch = (ImageButton) v.findViewById(R.id.btnMultiTouch);
        this.scrollBar = v.findViewById(R.id.scrollbar);
        this.holdToGrab = (TextView) v.findViewById(R.id.holdToGrab);
        this.holdToGrab.setTypeface(getTf(getActivity()));
        this.btnMultiTouch.setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.AnonymousClass2 */

            public boolean onTouch(View v, MotionEvent event) {
                if (TouchpadFragment.scrollbarTouched) {
                    return false;
                }
                if (event.getAction() == 0) {
                    TouchpadFragment.this.touchpad.MTTouched = true;
                    TouchpadFragment.this.touchpad.invalidate();
                } else if (event.getAction() == 1) {
                    TouchpadFragment.this.touchpad.MTTouched = false;
                    TouchpadFragment.this.touchpad.invalidate();
                }
                TouchpadFragment.this.mtListener.onMTTouched(event);
                return true;
            }
        });
        this.scrollBar.setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.AnonymousClass3 */
            float prevY;
            private Runnable scrollbarRunnable = new Runnable() {
                /* class com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.AnonymousClass3.AnonymousClass1 */

                public void run() {
                    if (TouchpadFragment.this.scrollbarListener != null) {
                        Log.d("scrollbar", "diffY: " + TouchpadFragment.this.diffY);
                        float part = TouchpadFragment.this.touchpadHeigt / 5.0f;
                        if (((double) TouchpadFragment.this.diffY) >= ((double) part) * 1.5d) {
                            TouchpadFragment.this.scrollbarListener.onScroll(-2);
                        } else if (((double) TouchpadFragment.this.diffY) >= ((double) part) * 0.5d) {
                            TouchpadFragment.this.scrollbarListener.onScroll(-1);
                        } else if (((double) TouchpadFragment.this.diffY) <= ((double) part) * -1.5d) {
                            TouchpadFragment.this.scrollbarListener.onScroll(2);
                        } else if (((double) TouchpadFragment.this.diffY) <= ((double) part) * -0.5d) {
                            TouchpadFragment.this.scrollbarListener.onScroll(1);
                        }
                    }
                    TouchpadFragment.this.h.postDelayed(this, 200);
                }
            };

            public boolean onTouch(View view, MotionEvent event) {
                if (TouchpadFragment.this.touchpad.MTTouched) {
                    return false;
                }
                if (Build.VERSION.SDK_INT >= 19) {
                }
                if (event.getAction() == 0) {
                    TouchpadFragment.scrollbarTouched = true;
                    v.findViewById(R.id.btnMultiTouch).setBackgroundResource(R.drawable.beambutton_bg_pressed);
                    TouchpadFragment.this.touchpad.paint.setColor(Color.rgb(160, 160, 160));
                    TouchpadFragment.this.touchpad.invalidate();
                    TouchpadFragment.this.touched = true;
                    TouchpadFragment.this.h.postDelayed(this.scrollbarRunnable, 200);
                    TouchpadFragment.this.defaultScrollBarPos = TouchpadFragment.this.scrollBar.getY();
                    this.prevY = event.getRawY();
                    return true;
                } else if (event.getAction() == 2) {
                    float tempDiffY = event.getRawY() - this.prevY;
                    this.prevY = event.getRawY();
                    TouchpadFragment.this.scrollBar.setY(TouchpadFragment.this.scrollBar.getY() + tempDiffY);
                    TouchpadFragment.this.diffY = -(TouchpadFragment.this.scrollBar.getY() - TouchpadFragment.this.defaultScrollBarPos);
                    return true;
                } else if (event.getAction() != 1) {
                    return false;
                } else {
                    TouchpadFragment.scrollbarTouched = false;
                    v.findViewById(R.id.btnMultiTouch).setBackgroundResource(R.drawable.beambutton_bg_default);
                    TouchpadFragment.this.touchpad.paint.setColor(Color.rgb((int) MotionEventCompat.ACTION_MASK, (int) MotionEventCompat.ACTION_MASK, (int) MotionEventCompat.ACTION_MASK));
                    TouchpadFragment.this.touchpad.invalidate();
                    TouchpadFragment.this.scrollBar.setY(TouchpadFragment.this.defaultScrollBarPos);
                    TouchpadFragment.this.diffY = 0.0f;
                    TouchpadFragment.this.h.removeCallbacks(this.scrollbarRunnable);
                    return true;
                }
            }
        });
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof Touchpad.TouchpadListener) {
            this.touchpad.setTouchpadListener((Touchpad.TouchpadListener) activity);
            setScrollbarListener((ScrollbarListener) activity);
        }
        if (activity instanceof Touchpad.TouchpadListener) {
            setMTListener((MTListener) activity);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_KEY_FEED_ITEM, 1);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
