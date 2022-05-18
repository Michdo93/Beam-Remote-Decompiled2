package com.studiodiip.bulbbeam.mousecontroller.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.studiodiip.bulbbeam.mousecontroller.fragment.KeyboardFragment;

public class customTextWatcher implements TextWatcher {
    int charCount = 0;
    public EditText editText;
    private boolean insertedSpace = false;

    public customTextWatcher(EditText et, Context context) {
        this.editText = et;
        this.editText.setOnKeyListener(new View.OnKeyListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.util.customTextWatcher.AnonymousClass1 */

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
    }

    public void afterTextChanged(Editable s) {
        if (s.length() > 4) {
            s.delete(0, 1);
        }
        if (s.length() == 0) {
            s.append(" ");
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (this.insertedSpace) {
            this.insertedSpace = false;
            return;
        }
        int nbLines = this.editText.getLineCount();
        if (!(KeyboardFragment.listener == null || (before == 0 && start == 0 && count == 1))) {
            if (before > count && s.length() > 0 && start > 0) {
                KeyboardFragment.listener.onKeyPress("BACKSPACE");
            } else if (s.length() == 0 && before == 1 && count == 0 && start == 0) {
                KeyboardFragment.listener.onKeyPress("BACKSPACE");
                this.editText.setText(" ");
                this.insertedSpace = true;
            } else if (start > 0) {
                KeyboardFragment.listener.onKeyPress(s);
            }
        }
        if (nbLines > 1 && this.editText.getText().toString().contains("\n")) {
            KeyboardFragment.listener.onKeyPress("ENTER");
            this.editText.setText(s.toString().replace("\n", ""));
            count--;
        }
        this.charCount = count;
    }
}
