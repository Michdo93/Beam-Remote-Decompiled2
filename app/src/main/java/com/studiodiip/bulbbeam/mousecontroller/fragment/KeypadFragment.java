package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.studiodiip.bulbbeam.mousecontroller.R;

public class KeypadFragment extends Fragment {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    public keyPressListener listener;

    public interface keyPressListener {
        void onKeyPress(CharSequence charSequence);
    }

    public void setKeyPressListener(keyPressListener listener2) {
        this.listener = listener2;
    }

    public static KeypadFragment newInstance() {
        KeypadFragment f = new KeypadFragment();
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
        setKeyPressListener((keyPressListener) getActivity());
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_keypad, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTouchListeners();
    }

    private void setTouchListeners() {
        getView().findViewById(R.id.btnTop).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.AnonymousClass1 */

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                Log.d("KEYPAD", "Btn UP Touched!");
                if (KeypadFragment.this.listener == null) {
                    return false;
                }
                KeypadFragment.this.listener.onKeyPress("up");
                return false;
            }
        });
        getView().findViewById(R.id.btnRight).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.AnonymousClass2 */

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                Log.d("KEYPAD", "Btn Right Touched!");
                if (KeypadFragment.this.listener == null) {
                    return false;
                }
                KeypadFragment.this.listener.onKeyPress("right");
                return false;
            }
        });
        getView().findViewById(R.id.btnBottom).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.AnonymousClass3 */

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                Log.d("KEYPAD", "Btn Bottom Touched!");
                if (KeypadFragment.this.listener == null) {
                    return false;
                }
                KeypadFragment.this.listener.onKeyPress("down");
                return false;
            }
        });
        getView().findViewById(R.id.btnLeft).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.AnonymousClass4 */

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                Log.d("KEYPAD", "Btn Left Touched!");
                if (KeypadFragment.this.listener == null) {
                    return false;
                }
                KeypadFragment.this.listener.onKeyPress("left");
                return false;
            }
        });
        getView().findViewById(R.id.btnOk).setOnTouchListener(new View.OnTouchListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.AnonymousClass5 */

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 1) {
                    return false;
                }
                Log.d("KEYPAD", "Btn OK Touched!");
                if (KeypadFragment.this.listener == null) {
                    return false;
                }
                KeypadFragment.this.listener.onKeyPress("ok");
                return false;
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_KEY_FEED_ITEM, 1);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
