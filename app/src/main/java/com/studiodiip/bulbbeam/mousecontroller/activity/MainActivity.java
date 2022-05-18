package com.studiodiip.bulbbeam.mousecontroller.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.appListActivity;
import com.studiodiip.bulbbeam.mousecontroller.keyboardControl;
import com.studiodiip.bulbbeam.mousecontroller.mouseSocketSender;

import java.io.IOException;

public class MainActivity extends Activity {
    public static int SERVERPORT = 3000;
    public static String SERVER_IP = "192.168.0.59";
    public static Context fContext;
    public static String localMacAddress = null;
    public static mouseSocketSender mss = null;
    public static ToggleButton screenToggle;
    public static boolean startingNewActivity = false;
    private ScaleGestureDetector SGD;
    private boolean buttonDown = false;
    boolean currentTouchIsMulti = false;
    boolean currentTouchIsPinch = false;
    long downTime = 0;
    public double heightRatio;
    long lasttime;
    private GestureDetector mGestureDetector;
    float oldX = 0.0f;
    float oldY = 0.0f;
    boolean screenToggleChecked = true;
    public double widthRatio;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.fullscreen_content);
        fContext = getApplicationContext();
        screenToggle = (ToggleButton) findViewById(R.id.screenToggle);
        this.screenToggleChecked = screenToggle.isChecked();
        v.setOnKeyListener(new View.OnKeyListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass1 */

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("KEY2", Integer.toString(keyCode));
                MainActivity.mss.sendSocket("keya", Integer.toString(keyCode));
                return true;
            }
        });
        ((Button) findViewById(R.id.keyboard)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass2 */

            public void onClick(View view) {
                MainActivity.startingNewActivity = true;
                MainActivity.this.startActivity(new Intent(MainActivity.this, keyboardControl.class));
            }
        });
        ((Button) findViewById(R.id.click)).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass3 */

            public boolean onTouch(View view, MotionEvent event) {
                Log.d("Button TOUCH", "Event: " + event + " GETACTIOn " + event.getAction());
                switch (event.getAction()) {
                    case 0:
                        MainActivity.this.buttonDown = true;
                        MainActivity.mss.sendSocket("mdr", "0;0");
                        break;
                    case 1:
                        MainActivity.this.buttonDown = false;
                        int totalTimes = 0;
                        for (int times = 0; times < 1000; times++) {
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes);
                            MainActivity.this.sleepie(10);
                            int totalTimes2 = totalTimes + 1;
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes2);
                            MainActivity.this.sleepie(10);
                            int totalTimes3 = totalTimes2 + 1;
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes3);
                            MainActivity.this.sleepie(10);
                            int totalTimes4 = totalTimes3 + 1;
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes4);
                            MainActivity.this.sleepie(10);
                            int totalTimes5 = totalTimes4 + 1;
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes5);
                            MainActivity.this.sleepie(10);
                            int totalTimes6 = totalTimes5 + 1;
                            MainActivity.mss.sendSocket("mmr", "1;1", totalTimes6);
                            MainActivity.this.sleepie(10);
                            int totalTimes7 = totalTimes6 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes7);
                            MainActivity.this.sleepie(10);
                            int totalTimes8 = totalTimes7 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes8);
                            MainActivity.this.sleepie(10);
                            int totalTimes9 = totalTimes8 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes9);
                            MainActivity.this.sleepie(10);
                            int totalTimes10 = totalTimes9 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes10);
                            MainActivity.this.sleepie(10);
                            int totalTimes11 = totalTimes10 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes11);
                            MainActivity.this.sleepie(10);
                            int totalTimes12 = totalTimes11 + 1;
                            MainActivity.mss.sendSocket("mmr", "-1;-1", totalTimes12);
                            MainActivity.this.sleepie(10);
                            totalTimes = totalTimes12 + 1;
                        }
                        break;
                    case 2:
                        MainActivity.this.motionHandler(event, true);
                        break;
                    case 261:
                        MainActivity.this.motionHandler(event, true);
                        break;
                }
                return false;
            }
        });
        ((Button) findViewById(R.id.menu)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass4 */

            public void onClick(View view) {
                MainActivity.mss.sendSocket("b", "2");
            }
        });
        ((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass5 */

            public void onClick(View view) {
                MainActivity.mss.sendSocket("b", "3");
            }
        });
        ((Button) findViewById(R.id.appsButton)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass6 */

            public void onClick(View view) {
                MainActivity.startingNewActivity = true;
                MainActivity.this.startActivity(new Intent(MainActivity.this, appListActivity.class));
            }
        });
        ((ImageView) findViewById(R.id.imageViewMouse)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass7 */

            public void onClick(View v) {
                MainActivity.startingNewActivity = true;
                MainActivity.this.startActivity(new Intent(MainActivity.this, MouseActivity.class));
            }
        });
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        this.widthRatio = ((double) screenWidth) / ((double) 854);
        this.heightRatio = ((double) screenHeight) / ((double) 480);
        Log.d("SIZES", "Ratios: " + this.widthRatio + " ; " + this.heightRatio + " screensize: " + screenWidth + " ; " + screenHeight);
        screenToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity.AnonymousClass8 */

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (MainActivity.mss != null && isChecked != MainActivity.this.screenToggleChecked) {
                    MainActivity.this.screenToggleChecked = isChecked;
                    if (isChecked) {
                        MainActivity.mss.sendSocket("c", "s_on");
                    } else {
                        MainActivity.mss.sendSocket("c", "s_off");
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sleepie(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("MOUSECONTROLLER", "DESTROY");
        try {
            if (mouseSocketSender.socket != null && mouseSocketSender.socket.isConnected()) {
                mouseSocketSender.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("MOUSECONTROLLER", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        startingNewActivity = false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("PINCHED", "FACTOR: " + detector.getScaleFactor());
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        motionHandler(event, false);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void motionHandler(MotionEvent event, boolean fromButton) {
        long nowtime = System.currentTimeMillis();
        float newX = event.getX();
        float newY = event.getY();
        if (fromButton) {
            Log.d("MOTIONEVENT: ", "event.getPointerCount(): " + event.getPointerCount());
            if (event.getPointerCount() > 1) {
                MotionEvent.PointerCoords pc = new MotionEvent.PointerCoords();
                event.getPointerCoords(1, pc);
                newX = pc.x;
                newY = pc.y;
                if (event.getAction() == 261) {
                    this.oldX = newX;
                    this.oldY = newY;
                }
            } else {
                this.oldX = newX;
                this.oldY = newY;
                return;
            }
        }
        float x = this.oldX - newX;
        float y = this.oldY - newY;
        int x_int = -((int) x);
        int y_int = -((int) y);
        if (this.buttonDown) {
            mss.sendSocket("mmr", x_int + ";" + y_int);
            this.oldX = newX;
            this.oldY = newY;
            return;
        }
        switch (event.getAction()) {
            case 0:
                Log.d("TOUCH DOWN", x + " , " + y);
                this.oldX = newX;
                this.oldY = newY;
                this.lasttime = System.currentTimeMillis();
                this.downTime = System.currentTimeMillis();
                return;
            case 1:
                Log.d("TOUCH UP", "Time " + System.currentTimeMillis() + " dt " + this.downTime + " min " + (System.currentTimeMillis() - this.downTime));
                if (System.currentTimeMillis() - this.downTime < 150) {
                    if (!this.currentTouchIsPinch) {
                        mss.sendSocket("mtr", x_int + ";" + y_int);
                    }
                } else if (!this.currentTouchIsPinch) {
                    mss.sendSocket(Integer.toString((int) x), Integer.toString((int) y));
                }
                this.currentTouchIsMulti = false;
                this.currentTouchIsPinch = false;
                return;
            case 2:
                if (nowtime - this.lasttime > 30) {
                    if (!this.currentTouchIsPinch) {
                        mss.sendSocket("mmr", x_int + ";" + y_int);
                    }
                    this.lasttime = System.currentTimeMillis();
                    this.oldX = newX;
                    this.oldY = newY;
                    return;
                }
                return;
            default:
                return;
        }
    }
}
