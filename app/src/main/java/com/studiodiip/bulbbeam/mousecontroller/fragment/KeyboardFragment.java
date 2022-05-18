package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.util.customTextWatcher;

public class KeyboardFragment extends Fragment {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    public static keyPressListener listener;
    private EditText editText;

    public interface keyPressListener {
        void onKeyPress(CharSequence charSequence);
    }

    public void setKeyPressListener(keyPressListener listener2) {
        listener = listener2;
    }

    public static KeyboardFragment newInstance() {
        KeyboardFragment f = new KeyboardFragment();
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
        View v = inflater.inflate(R.layout.fragment_keyboard, container, false);
        this.editText = (EditText) v.findViewById(R.id.customEditText);
        this.editText.setText(" ");
        this.editText.addTextChangedListener(new customTextWatcher(this.editText, getActivity()));
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onFragmentSelected() {
        if (this.editText != null) {
            this.editText.requestFocus();
            ((InputMethodManager) this.editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.editText, 2);
        }
    }

    public void onFragmentDismissed() {
        if (this.editText != null) {
            ((InputMethodManager) this.editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
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
