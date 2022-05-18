package com.studiodiip.bulbbeam.mousecontroller.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.studiodiip.bulbbeam.mousecontroller.R;

import java.util.ArrayList;
import java.util.Iterator;

public class MouseActivity extends Activity implements SensorEventListener {
    private static final float NS2S = 1.0E-9f;
    private static final int SHAKE_THRESHOLD = 600;
    private ArrayList<Float> accelX = new ArrayList<>();
    private ArrayList<Float> accelY = new ArrayList<>();
    private final float[] deltaRotationVector = new float[4];
    private float lastMotionX = 475.0f;
    private float lastMotionY = 240.0f;
    private long lastUpdate = 0;
    private float last_x;
    private float last_y;
    private float last_z;
    private Sensor senGyroscope;
    private SensorManager senSensorManager;
    private float timestamp;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);
        this.senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.senGyroscope = this.senSensorManager.getDefaultSensor(4);
        ((Button) findViewById(R.id.mouse_button_back)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MouseActivity.AnonymousClass1 */

            public void onClick(View v) {
                MainActivity.mss.sendSocket("b", "3");
            }
        });
        ((Button) findViewById(R.id.mouse_button_home)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.MouseActivity.AnonymousClass2 */

            public void onClick(View v) {
                MainActivity.mss.sendSocket("b", "2");
            }
        });
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        float avgX;
        float avgY;
        if (sensorEvent.sensor.getType() == 4) {
            float x = sensorEvent.values[0];
            float z = sensorEvent.values[2];
            this.accelX.add(Float.valueOf(z));
            this.accelY.add(Float.valueOf(x));
            if (this.accelX.size() > 5) {
                this.accelX.remove(0);
            }
            if (this.accelY.size() > 5) {
                this.accelY.remove(0);
            }
            float avgX2 = 0.0f;
            float avgY2 = 0.0f;
            if (this.accelX.isEmpty() || this.accelY.isEmpty()) {
                avgX = z;
                avgY = x;
            } else {
                Iterator<Float> it = this.accelX.iterator();
                while (it.hasNext()) {
                    avgX2 += it.next().floatValue();
                }
                Iterator<Float> it2 = this.accelY.iterator();
                while (it2.hasNext()) {
                    avgY2 += it2.next().floatValue();
                }
                avgX = avgX2 / ((float) this.accelX.size());
                avgY = avgY2 / ((float) this.accelY.size());
            }
            if (MainActivity.mss != null) {
                MainActivity.mss.sendSocket(Integer.toString((int) ((854.0f * avgX) / 4.0f)), Integer.toString((int) ((480.0f * avgY) / 4.0f)));
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.senSensorManager.unregisterListener(this);
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.senSensorManager.unregisterListener(this);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.senSensorManager.registerListener(this, this.senGyroscope, 2);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        MainActivity.mss.sendSocket("b", "1");
        return true;
    }
}
