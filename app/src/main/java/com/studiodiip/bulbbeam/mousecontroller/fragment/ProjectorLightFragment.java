package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.view.Touchpad;

public class ProjectorLightFragment extends Fragment {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    private static final String TAG = ProjectorLightFragment.class.getSimpleName();
    private View btnHideProjectorLight;
    private int currentState;
    private float lightPercentage;
    public keyPressListener listener;
    private SeekBar seekBar;
    private Touchpad touchpad;

    public interface keyPressListener {
        void onKeyPress(CharSequence charSequence);
    }

    public void setKeyPressListener(keyPressListener listener2) {
        this.listener = listener2;
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getArguments();
        }
        setKeyPressListener((keyPressListener) getActivity());
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        setLightPercentage(this.lightPercentage);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_projector_light, container, false);
        this.btnHideProjectorLight = v.findViewById(R.id.btnHideProjectorLight);
        this.touchpad = (Touchpad) v.findViewById(R.id.touchpad_projector_light);
        this.seekBar = (SeekBar) v.findViewById(R.id.seekBar_light);
        return v;
    }

    public void setLightPercentage(float percentage) {
        Log.d(TAG, "setLightPercentage " + percentage);
        this.lightPercentage = percentage;
        if (this.touchpad != null) {
            this.touchpad.setDotWidthPercentage(percentage);
            this.seekBar.setProgress((int) (100.0f * percentage));
            int newLightState = (int) (3.3f * percentage);
            if (this.currentState != newLightState) {
                String ledString = "led;" + newLightState + ";3";
                if (this.listener != null) {
                    this.listener.onKeyPress(ledString);
                }
                this.currentState = newLightState;
            }
        }
    }

    public float getLightPercentage() {
        return this.lightPercentage;
    }

    public void setSwitchToProjectorEnabled(boolean enabled) {
        Log.d(TAG, "setSwitchToProjectorEnabled " + enabled);
        if (getView() != null) {
            View switchButton = getView().findViewById(R.id.btnHideProjectorLight);
            if (enabled) {
                switchButton.setAlpha(0.6f);
                new Handler().post(new Runnable() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.fragment.ProjectorLightFragment.AnonymousClass1 */

                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.ProjectorLightFragment.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                if (ProjectorLightFragment.this.getView() != null) {
                                    View switchButton = ProjectorLightFragment.this.getView().findViewById(R.id.btnHideProjectorLight);
                                    if (switchButton.getAlpha() < 1.0f) {
                                        switchButton.setAlpha(switchButton.getAlpha() + 0.1f);
                                        handler.postDelayed(this, 30);
                                        return;
                                    }
                                    return;
                                }
                                handler.postDelayed(this, 30);
                            }
                        }, 30);
                    }
                });
            } else {
                switchButton.setAlpha(0.5f);
            }
            if (this.btnHideProjectorLight != null) {
                this.btnHideProjectorLight.setEnabled(enabled);
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.ProjectorLightFragment.AnonymousClass2 */

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progress2;
                ProjectorLightFragment.this.touchpad.setDotWidthPercentage(((float) progress) / 100.0f);
                if (progress < 17) {
                    progress2 = 0;
                } else if (progress < 50) {
                    progress2 = 33;
                } else if (progress < 83) {
                    progress2 = 66;
                } else {
                    progress2 = 100;
                }
                ProjectorLightFragment.this.setLightPercentage(((float) progress2) / 100.0f);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
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
