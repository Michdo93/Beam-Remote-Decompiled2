package com.studiodiip.bulbbeam.mousecontroller.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class Keyboard extends TypefaceEditText {
    public Keyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public Keyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Keyboard(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        addTextChangedListener(new TextWatcher() {
            /* class com.studiodiip.bulbbeam.mousecontroller.view.Keyboard.AnonymousClass1 */

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("TextWatcherTest", "onTextChanged:\t" + s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("TextWatcherTest", "beforeTextChanged:\t" + s.toString());
            }

            public void afterTextChanged(Editable s) {
                Log.e("TextWatcherTest", "afterTextChanged:\t" + s.toString());
                String text = Keyboard.this.getText().toString();
                int length = text.length();
                if (length > 4) {
                    Keyboard.this.setText(text.substring(length - 4));
                    Keyboard.this.setSelection(4);
                }
            }
        });
        setTextIsSelectable(false);
        setKeyListener(new KeyListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.view.Keyboard.AnonymousClass2 */

            public int getInputType() {
                return Keyboard.this.getInputType();
            }

            public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
                if (keyCode == 67 || keyCode == 4) {
                    return true;
                }
                return false;
            }

            public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
                if (keyCode == 67 || keyCode == 4) {
                    return true;
                }
                return false;
            }

            public boolean onKeyOther(View view, Editable text, KeyEvent event) {
                return false;
            }

            public void clearMetaKeyState(View view, Editable content, int states) {
            }
        });
        setImeActionLabel("Enter", 66);
        setOnEditorActionListener(new OnEditorActionListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.view.Keyboard.AnonymousClass3 */

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 66) {
                }
                return true;
            }
        });
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.view.TypefaceEditText
    public void onSelectionChanged(int start, int end) {
        CharSequence text = getText();
        if (text == null || (start == text.length() && end == text.length())) {
            super.onSelectionChanged(start, end);
        } else {
            setSelection(text.length(), text.length());
        }
    }
}
