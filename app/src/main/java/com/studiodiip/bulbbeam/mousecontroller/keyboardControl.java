package com.studiodiip.bulbbeam.mousecontroller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity;

public class keyboardControl extends Activity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_keyboard_control);
        findViewById(R.id.fullscreen_content);
        ((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.keyboardControl.AnonymousClass1 */

            public void onClick(View view) {
                keyboardControl.this.finish();
            }
        });
        ((Button) findViewById(R.id.backspace)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.keyboardControl.AnonymousClass2 */

            public void onClick(View view) {
                MainActivity.mss.sendSocket("b", "4");
            }
        });
        ((EditText) findViewById(R.id.customEditText)).addTextChangedListener(new TextWatcher() {
            /* class com.studiodiip.bulbbeam.mousecontroller.keyboardControl.AnonymousClass3 */

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String.valueOf(s.charAt(before));
            }

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.keyboardControl.AnonymousClass4 */

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MainActivity.mss.sendSocket("led", progress + ";3");
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ((Button) findViewById(R.id.flipButton)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.keyboardControl.AnonymousClass5 */

            public void onClick(View view) {
                MainActivity.mss.sendSocket("f", "s");
            }
        });
    }

    public int convertStringToKeyCode(String text) {
        KeyEvent[] events = KeyCharacterMap.load(-1).getEvents(text.toCharArray());
        for (KeyEvent event2 : events) {
            if (event2.getAction() == 0) {
                return event2.getKeyCode();
            }
        }
        return -1;
    }
}
