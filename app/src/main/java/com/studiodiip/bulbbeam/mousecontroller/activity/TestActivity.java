package com.studiodiip.bulbbeam.mousecontroller.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.ble.BleManager;
import com.studiodiip.bulbbeam.mousecontroller.fragment.GalleryFragment;
import com.studiodiip.bulbbeam.mousecontroller.fragment.KeyboardFragment;
import com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment;
import com.studiodiip.bulbbeam.mousecontroller.fragment.ProjectorLightFragment;
import com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;
import com.studiodiip.bulbbeam.mousecontroller.service.ConnectionServiceController;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;
import com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer;
import com.studiodiip.bulbbeam.mousecontroller.util.ShowImage;
import com.studiodiip.bulbbeam.mousecontroller.util.Utils;
import com.studiodiip.bulbbeam.mousecontroller.view.Touchpad;
import com.studiodiip.bulbbeam.mousecontroller.view.TypefaceButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends FragmentActivity implements Touchpad.TouchpadListener, KeyboardFragment.keyPressListener, KeypadFragment.keyPressListener, ProjectorLightFragment.keyPressListener, TouchpadFragment.MTListener, TouchpadFragment.ScrollbarListener, ShowImage.IShowImageListener, GalleryFragment.IGalleryFragmentListener {
    public static final String ACTION_SHARE = "ACTION_SHARE";
    private static final String TAG = TestActivity.class.getSimpleName();
    private static final int UNBIND_TIME_INTERVAL_SECONDS = 10;
    private static boolean isTestingApp = false;
    private static UnbindTimerTask unbindTimerTask;
    private List<BeamBulb> beamBulbs = new ArrayList();
    private View bottomBar;
    private View bottomBarExtended;
    private View bottomBarTop;
    private SeekBar brightnessSlider;
    private View btnLight;
    private TextView btnMoreLess;
    private View btnOnOff;
    BroadcastReceiver connectionReceiver;
    private ImageView connectionTypeImageView;
    private FrameLayout contentFrame;
    private int currentScreenBrightness = 256;
    boolean currentTouchIsMulti = false;
    boolean currentTouchIsPinch = false;
    private int currentVolume = 0;
    private View defaultUI;
    long downTime = 0;
    private Fragment fragment;
    private String fragmentTag;
    private boolean isBottomBarExtended = false;
    private boolean isBrightnessProgessFirstCalled = true;
    private boolean isKeyStoneProgressFirstCalled = true;
    private boolean isProjectorLightActive = false;
    private boolean isShareIntentAvailable = false;
    private boolean isTitleDropDownVisible = false;
    private boolean isTitleEditable = false;
    private boolean isVolumeProgressFirstCalled = true;
    private SeekBar keystoneSlider;
    private float lastTimestamp = 0.0f;
    long lasttime;
    private BroadcastReceiver mBeamChangedReceiver = new BroadcastReceiver() {
        /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass2 */

        public void onReceive(Context context, Intent receiveIntent) {
            Log.d(TestActivity.TAG, "Beam changed notification");
            TestActivity.this.selectedBeam = BeamSettings.getInstance().getSelectedBeam();
            TestActivity.this.setSelectedBeam(TestActivity.this.selectedBeam, false);
            if (TestActivity.this.selectedBeam != null && TestActivity.this.selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
                BleManager.getInstance().sendCommand("user;" + Utils.getUserName(TestActivity.this.getApplicationContext()) + ";" + Utils.getMacAddress());
            }
            TestActivity.this.updateBeams();
        }
    };
    private BroadcastReceiver mWifiBeamsReceiver = new BroadcastReceiver() {
        /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass1 */

        public void onReceive(Context context, Intent receiveIntent) {
            ArrayList<BeamBulb> wifiBeamList = receiveIntent.getParcelableArrayListExtra(BulbDiscoverer.RECEIVER_WIFI_LIST);
            Log.d(TestActivity.TAG, "Beams discovered by wifi - " + wifiBeamList.size());
            Iterator<BeamBulb> it = wifiBeamList.iterator();
            while (it.hasNext()) {
                BulbDiscoverer.addWifiBeam(it.next());
            }
            TestActivity.this.updateBeams();
        }
    };
    private String oldBeamName = "";
    float oldX = 0.0f;
    float oldY = 0.0f;
    private ProjectorLightFragment projectorLightFragment;
    private View projectorLightUI;
    private BeamBulb selectedBeam;
    private int selectedTabId = R.id.tab_touchpad;
    private TextView tabGallery;
    private TextView tabKeyboard;
    private TextView tabKeypad;
    private TextView tabTouchpad;
    private ImageView titleDisclosure;
    private View titleDropDownWrapper;
    private TitleDropdownAdapter titleDropdownAdapter;
    private ListView titleDropdownListView;
    private EditText titleEditText;
    private KeyListener titleKeyListener;
    private BeamBulb toSwitchBeam;
    private TouchpadFragment touchpadFragment;
    private boolean usingBluetooth = false;
    private SeekBar volumeSlider;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSelectedBeamByPosition(int position) {
        Log.d(TAG, "setSelectedBeamByPosition");
        updateBeams();
        int numBeams = this.beamBulbs.size();
        Log.d(TAG, "setSelectedBeamByPosition beam list " + numBeams + " pos " + position);
        if (position == numBeams - 2) {
            findMoreBeams();
            finish();
        } else if (position > numBeams - 2) {
            showPrivacyPolicy();
        } else {
            setSelectedBeam(this.beamBulbs.get(position), true);
        }
    }

    private void showPrivacyPolicy() {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://beamlabsinc.com/privacy/")));
        } catch (Exception e) {
            Toast.makeText(this, "Please install a web browser to view the privacy policy", Toast.LENGTH_LONG).show();
        }
    }

    private void findMoreBeams() {
        BeamSettings.getInstance().setSelectedBeam(null);
        Intent splashIntent = new Intent(this, SplashActivity.class);
        splashIntent.putExtra(SplashActivity.SHOULD_SCAN_FULLY, true);

        splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(splashIntent);
        BeamSettings.isConnected = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSelectedBeam(BeamBulb beamBulb, boolean showSplash) {
        Log.d(TAG, "SetSelectedBeam ");
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (beamBulb == null) {
            Log.d(TAG, "Beam == null!");
            Intent splashIntent = new Intent(this, SplashActivity.class);
            BeamSettings.isConnected = false;
            startActivity(splashIntent);
            finish();
            return;
        }
        this.selectedBeam = beamBulb;
        BeamSettings.getInstance().setSelectedBeam(this.selectedBeam);
        BeamSettings.getInstance().setLastUsedBeam(this.selectedBeam);
        if (showSplash) {
            ConnectionServiceController.getInstance().unbindConnectionService();
            Intent intentone = new Intent(getApplicationContext(), SplashActivity.class);
            intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            BeamSettings.isConnected = false;
            startActivity(intentone);
            finish();
            return;
        }
        Log.i(TAG, "Beam found: " + beamBulb.title);
        BeamSettings.isConnected = true;
        this.currentVolume = this.selectedBeam.volume;
        this.volumeSlider.setProgress((int) (((double) this.currentVolume) * 6.667d));
        this.keystoneSlider.setProgress(40);
        if (!this.isTitleEditable) {
            this.titleEditText.setText(this.selectedBeam.title.toUpperCase());
        }
        if (this.selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
            this.connectionTypeImageView.setImageResource(R.drawable.bluetooth);
        } else {
            this.connectionTypeImageView.setImageResource(R.drawable.wifi);
        }
        Log.d(TAG, "Version: " + this.selectedBeam.version);
        if (this.selectedBeam.version >= 2) {
            findViewById(R.id.btn_sets).setVisibility(View.GONE);
            findViewById(R.id.btn_multitask).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_rotate).setVisibility(View.GONE);
            findViewById(R.id.btn_sets_bot).setVisibility(View.GONE);
            findViewById(R.id.btn_multitask_bot).setVisibility(View.GONE);
            findViewById(R.id.btn_rotate_bot).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn_sets).setVisibility(View.GONE);
            findViewById(R.id.btn_multitask).setVisibility(View.GONE);
            findViewById(R.id.btn_rotate).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_sets_bot).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_multitask_bot).setVisibility(View.GONE);
            findViewById(R.id.btn_rotate_bot).setVisibility(View.GONE);
        }
        updateBeams();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBeams() {
        this.beamBulbs.clear();
        if (BulbDiscoverer.beamBulbs != null) {
            Iterator<BeamBulb> it = BulbDiscoverer.beamBulbs.iterator();
            while (it.hasNext()) {
                BeamBulb beamBulb = it.next();
                if (!(beamBulb == null || this.selectedBeam == null)) {
                    if (!beamBulb.ip.equals(this.selectedBeam.ip) || !beamBulb.mac.equals(this.selectedBeam.mac)) {
                        this.beamBulbs.add(beamBulb);
                    }
                }
            }
            this.beamBulbs.add(new BeamBulb(getResources().getString(R.string.find_more_beams), "", "", 0, 0, null, null));
            this.beamBulbs.add(new BeamBulb(getResources().getString(R.string.privacy_policy), "", "", 0, 0, null, null));
            this.titleDropdownAdapter.notifyDataSetChanged();
        }
    }

    private void handleShareIntent() {
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
        setContentView(R.layout.activity_test);
        if (ConnectionServiceController.getInstance() == null) {
            ConnectionServiceController.init(getApplicationContext());
        }
        if (BeamSettings.getInstance() == null) {
            BeamSettings.init(getApplicationContext());
        }
        if (savedInstanceState != null) {
            initializeDataFromBundle(savedInstanceState);
        } else {
            this.selectedBeam = BeamSettings.getInstance().getSelectedBeam();
        }
        Intent intent = getIntent();
        Log.d(TAG, "Intent getAction " + intent.getAction());
        if (intent.hasExtra("bluetooth")) {
            this.usingBluetooth = true;
            Log.d(TAG, "bluetooth is set");
        } else {
            Log.d(TAG, "bluetooth not set ");
        }
        if (this.selectedBeam != null && this.selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
            BleManager.getInstance().sendCommand("user;" + Utils.getUserName(getApplicationContext()) + ";" + Utils.getMacAddress());
        }
        this.titleEditText = (EditText) findViewById(R.id.title_edit_text);
        this.titleDisclosure = (ImageView) findViewById(R.id.title_disclosure);
        this.titleDropdownListView = (ListView) findViewById(R.id.title_dropdown_listview);
        this.titleDropDownWrapper = findViewById(R.id.title_dropdown_wrapper);
        this.tabKeypad = (TextView) findViewById(R.id.tab_keypad);
        this.tabTouchpad = (TextView) findViewById(R.id.tab_touchpad);
        this.tabKeyboard = (TextView) findViewById(R.id.tab_keyboard);
        this.tabGallery = (TextView) findViewById(R.id.tab_gallery);
        this.btnOnOff = findViewById(R.id.btn_onoff);
        this.btnLight = findViewById(R.id.btn_light);
        this.btnMoreLess = (TextView) findViewById(R.id.btn_moreless);
        this.bottomBar = findViewById(R.id.bottom_bar);
        this.bottomBarTop = findViewById(R.id.bottom_bar_top);
        this.bottomBarExtended = findViewById(R.id.bottom_bar_extended);
        this.contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        this.defaultUI = findViewById(R.id.default_ui);
        this.projectorLightUI = findViewById(R.id.projector_light_ui);
        this.connectionTypeImageView = (ImageView) findViewById(R.id.connectionType);
        this.connectionReceiver = new BroadcastReceiver() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass3 */

            @SuppressLint("MissingPermission")
            public void onReceive(Context context, Intent intent) {
                boolean z;
                String action = intent.getAction();
                BeamBulb selectedBeam = BeamSettings.getInstance().getSelectedBeam();
                if (selectedBeam != null) {
                    TestActivity testActivity = TestActivity.this;
                    if (selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
                        z = true;
                    } else {
                        z = false;
                    }
                    testActivity.usingBluetooth = z;
                    Log.d(TestActivity.TAG, "onReceive " + action + ", usingBluetooth " + TestActivity.this.usingBluetooth);
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") && !TestActivity.this.usingBluetooth) {
                        NetworkInfo netInfo;
                        netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                        if (netInfo == null || netInfo.getType() != 1) {
                            Log.d(TestActivity.TAG, "Don't have Wifi Connection");
                            BulbDiscoverer.getInstance().setWifiState(BulbDiscoverer.STATE_WIFI_OFF);
                            BeamSettings.isConnected = false;
                            reconnect(context);
                        }
                    } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") && TestActivity.this.usingBluetooth) {
                        NetworkInfo netInfo2 = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                        if (netInfo2 == null || netInfo2.getType() != 1) {
                            ConnectionServiceController.getInstance().getBinder().closeImageSocket();
                        } else if (netInfo2 != null && netInfo2.getType() == 1) {
                            ConnectionServiceController.getInstance().getBinder().connectToImageSocket();
                        }
                    } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED") && TestActivity.this.usingBluetooth) {
                        int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
                        if (state == 10 || state == 0) {
                            Log.d(TestActivity.TAG, "Bluetooth turned off ");
                            BleManager.getInstance().setBluetoothState(11);
                            BeamSettings.isConnected = false;
                            reconnect(context);
                        }
                    }
                }
            }

            private void reconnect(Context context) {
                Log.d(TestActivity.TAG, "reconnect");
                BeamSettings.getInstance().setSelectedBeam(null);
                Intent intentone = new Intent(context.getApplicationContext(), SplashActivity.class);
                intentone.putExtra("button", TestActivity.this.getResources().getString(R.string.btn_reconnect));
                intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intentone);
                TestActivity.this.finish();
            }
        };
        handleProgressBars();
        this.titleDropdownAdapter = new TitleDropdownAdapter(this);
        this.titleDropdownListView.setAdapter((ListAdapter) this.titleDropdownAdapter);
        this.titleDropdownListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass4 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TestActivity.this.setSelectedBeamByPosition(position);
                TestActivity.this.setTitleDropDownVisible(false, true);
            }
        });
        updateBeams();
        this.titleKeyListener = this.titleEditText.getKeyListener();
        setSelectedTabId(this.selectedTabId, false);
        setTitleDropDownVisible(this.isTitleDropDownVisible, false);
        setTitleEditable(this.isTitleEditable);
        setBottomBarExtended(this.isBottomBarExtended, false);
        setProjectorLightActive(this.isProjectorLightActive, false);
        if (isTestingApp) {
            ((TextView) findViewById(R.id.btn_multitask)).setText("Test app");
        }
    }

    private void handleProgressBars() {
        this.volumeSlider = (SeekBar) findViewById(R.id.volumeSeekBar);
        this.volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass5 */

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (TestActivity.this.isVolumeProgressFirstCalled) {
                    Log.d(TestActivity.TAG, "onProgressChanged first time ignore");
                    TestActivity.this.isVolumeProgressFirstCalled = false;
                    return;
                }
                int newVolume = (int) (((float) progress) / 6.667f);
                if (TestActivity.this.currentVolume != newVolume) {
                    TestActivity.this.currentVolume = newVolume;
                    try {
                        ConnectionServiceController.getInstance().getBinder().sendSocket("vol", TestActivity.this.currentVolume + "", "3");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    TestActivity.this.selectedBeam.volume = TestActivity.this.currentVolume;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.keystoneSlider = (SeekBar) findViewById(R.id.keystoneSeekBar);
        this.keystoneSlider.setMax(80);
        this.keystoneSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass6 */

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (TestActivity.this.isKeyStoneProgressFirstCalled) {
                    Log.d(TestActivity.TAG, "onProgressChanged first time ignore");
                    TestActivity.this.isKeyStoneProgressFirstCalled = false;
                    return;
                }
                ConnectionServiceController.getInstance().getBinder().sendSocket("dlp", Integer.toString((progress * 1) - 40));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.keystoneSlider.setVisibility(View.GONE);
        ((TypefaceButton) findViewById(R.id.btn_keystone)).setVisibility(View.GONE);
        this.brightnessSlider = (SeekBar) findViewById(R.id.screenBrightnessSeekBar);
        this.brightnessSlider.setMax(246);
        this.brightnessSlider.setProgress(this.currentScreenBrightness);
        this.brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass7 */

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progress2;
                if (TestActivity.this.isBrightnessProgessFirstCalled) {
                    Log.d(TestActivity.TAG, "onProgressChanged for screen brightnessfirst time ignore");
                    TestActivity.this.isBrightnessProgessFirstCalled = false;
                    return;
                }
                if (progress < 20) {
                    progress2 = 0;
                } else if (progress < 40) {
                    progress2 = 20;
                } else if (progress < 80) {
                    progress2 = 50;
                } else if (progress < 100) {
                    progress2 = 80;
                } else if (progress < 120) {
                    progress2 = 100;
                } else if (progress < 160) {
                    progress2 = 140;
                } else if (progress < 200) {
                    progress2 = 180;
                } else if (progress < 240) {
                    progress2 = 200;
                } else {
                    progress2 = 256;
                }
                if (TestActivity.this.currentScreenBrightness != progress2) {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("screenBrightness", Integer.toString(progress2));
                    TestActivity.this.currentScreenBrightness = progress2;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.brightnessSlider.setVisibility(View.GONE);
        ((TypefaceButton) findViewById(R.id.btn_screenBrightness)).setVisibility(View.GONE);
    }

    /* access modifiers changed from: package-private */
    public void handleSendText(Intent intent) {
        Log.d(TAG, "handleSendText");
        String sharedText = intent.getStringExtra("android.intent.extra.TEXT");
        String sharedSubject = intent.getStringExtra("android.intent.extra.SUBJECT");
        if (sharedText != null) {
            int idxOfURL = sharedText.indexOf("http://");
            if (idxOfURL == -1) {
                idxOfURL = sharedText.indexOf("https://");
            }
            if (idxOfURL > -1) {
                String substring = sharedText.substring(idxOfURL);
                String[] splitStr = substring.split("\\r?\\n");
                if (splitStr.length < 2) {
                    splitStr = substring.split(" ");
                }
                if (splitStr.length > 0) {
                    Log.d(TAG, "Received subject: " + sharedSubject + " url: " + splitStr[0]);
                    ConnectionServiceController.getInstance().getBinder().sendSocket("ACTION_VIEW", sharedSubject + ";" + splitStr[0]);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleSendImage(Intent intent) {
        Log.d(TAG, "handleSendImage");
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (!(key == null || value == null)) {
                Log.d(TAG, key + ": " + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
            }
        }
        Uri imageUri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
        if (imageUri != null) {
            Log.d(TAG, "Received image:" + imageUri);
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass8 */

            public void run() {
                Toast.makeText(TestActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override // android.support.v4.app.FragmentActivity
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selectedTabId", this.selectedTabId);
        outState.putBoolean("isTitleDropDownVisible", this.isTitleDropDownVisible);
        outState.putBoolean("isTitleEditable", this.isTitleEditable);
        outState.putBoolean("isBottomBarExtended", this.isBottomBarExtended);
        outState.putBoolean("isProjectorLightActive", this.isProjectorLightActive);
        outState.putBoolean("BeamSettings.isConnected", BeamSettings.isConnected);
        outState.putInt("currentVolume", this.currentVolume);
        outState.putParcelable("selectedBeam", this.selectedBeam);
        outState.putParcelable("toSwitchBeam", this.toSwitchBeam);
        outState.putBoolean("usingBluetooth", this.usingBluetooth);
        outState.putString("fragmentTag", this.fragmentTag);
        getFragmentManager().putFragment(outState, "fragment", this.fragment);
        super.onSaveInstanceState(outState);
    }

    private void initializeDataFromBundle(Bundle savedInstanceState) {
        this.selectedTabId = savedInstanceState.getInt("selectedTabId", this.selectedTabId);
        this.isTitleDropDownVisible = savedInstanceState.getBoolean("isTitleDropDownVisible", this.isTitleDropDownVisible);
        this.isTitleEditable = savedInstanceState.getBoolean("isTitleEditable", this.isTitleEditable);
        this.isBottomBarExtended = savedInstanceState.getBoolean("isBottomBarExtended", this.isBottomBarExtended);
        this.isProjectorLightActive = savedInstanceState.getBoolean("isProjectorLightActive", this.isProjectorLightActive);
        BeamSettings.isConnected = savedInstanceState.getBoolean("BeamSettings.isConnected");
        this.currentVolume = savedInstanceState.getInt("currentVolume");
        this.selectedBeam = (BeamBulb) savedInstanceState.getParcelable("selectedBeam");
        this.toSwitchBeam = (BeamBulb) savedInstanceState.getParcelable("toSwitchBeam");
        this.usingBluetooth = savedInstanceState.getBoolean("usingBluetooth");
        this.fragmentTag = savedInstanceState.getString("fragmentTag");
        this.fragment = getFragmentManager().getFragment(savedInstanceState, "fragment");
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent " + intent);
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null) {
            BeamSettings.shareIntent = intent;
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.MTListener
    public void onMTTouched(MotionEvent event) {
        if (event.getAction() == 0) {
            ConnectionServiceController.getInstance().getBinder().sendSocket("mdr", "0;0");
        } else if (event.getAction() == 1) {
            ConnectionServiceController.getInstance().getBinder().sendSocket("mur", "0;0");
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.fragment.TouchpadFragment.ScrollbarListener
    public void onScroll(int scroll) {
        for (int i = 0; i < Math.abs(scroll); i++) {
            try {
                ConnectionServiceController.getInstance().getBinder().sendSocket("mw", "0", "" + (scroll / Math.abs(scroll)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class TitleDropdownAdapter extends ArrayAdapter<BeamBulb> {
        public TitleDropdownAdapter(TestActivity this$02) {
            this(this$02, R.layout.beambulb_item, this$02.beamBulbs);
        }

        public TitleDropdownAdapter(Context context, int resource, List<BeamBulb> beamBulbs) {
            super(context, resource, beamBulbs);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View convertView2 = super.getView(position, convertView, parent);
            ((TextView) convertView2).setText(((BeamBulb) getItem(position)).title.toUpperCase());
            return convertView2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceivers();
        stopUnbindTimerTask();
    }

    @Override // android.support.v4.app.FragmentActivity
    public void onResumeFragments() {
        super.onResumeFragments();
        Log.d(TAG, "onResumeFragments " + BeamSettings.shareIntent);
        this.isVolumeProgressFirstCalled = true;
        this.selectedBeam = BeamSettings.getInstance().getSelectedBeam();
        if (ConnectionServiceController.getInstance().getBinder() == null) {
            Log.d(TAG, "mService binder is null.. bind again");
            BeamSettings.getInstance().setSelectedBeam(null);
            setSelectedBeam(null, true);
            return;
        }
        Log.d(TAG, "already connected");
        setSelectedBeam(this.selectedBeam, false);
        this.projectorLightFragment = (ProjectorLightFragment) getFragmentManager().findFragmentById(R.id.projector_light_ui);
        if (BeamSettings.shareIntent == null) {
            return;
        }
        if (ConnectionServiceController.getInstance().getBinder() == null) {
            this.isShareIntentAvailable = true;
            return;
        }
        Intent shareIntent = BeamSettings.shareIntent;
        String type = shareIntent.getType();
        if ("text/plain".equals(shareIntent.getType())) {
            handleSendText(shareIntent);
        } else if (type.startsWith("image/")) {
            handleSendImage(shareIntent);
        }
        BeamSettings.shareIntent = null;
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mWifiBeamsReceiver, new IntentFilter(BulbDiscoverer.RECEIVER_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        registerReceiver(this.connectionReceiver, intentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mBeamChangedReceiver, new IntentFilter(BulbDiscoverer.BEAM_CHANGED_BROADCAST_ACTION));
    }

    private void unregisterReceivers() {
        unregisterReceiver(this.connectionReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mWifiBeamsReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBeamChangedReceiver);
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DESTROY");
    }

    private void startUnbindTimerTask() {
        Log.d(TAG, "startUnbindTimerTask");
        stopUnbindTimerTask();
        unbindTimerTask = new UnbindTimerTask();
        new Timer().schedule(unbindTimerTask, 10000);
        Log.d(TAG, "startUnbindTimerTask done");
    }

    private void stopUnbindTimerTask() {
        Log.d(TAG, "stopUnbindTimerTask");
        if (unbindTimerTask != null) {
            unbindTimerTask.cancel();
            unbindTimerTask = null;
            Log.d(TAG, "stopUnbindTimerTask done");
        }
    }

    /* access modifiers changed from: private */
    public class UnbindTimerTask extends TimerTask {
        private UnbindTimerTask() {
        }

        public void run() {
            Log.d(TestActivity.TAG, "Its ten seconds" + BeamSettings.isConnected);
            if (BeamSettings.isConnected) {
                if (TestActivity.this.selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
                    BleManager.getInstance().closeCurrentConnection();
                }
                ConnectionServiceController.getInstance().unbindConnectionService();
                cancel();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceivers();
        if (BeamSettings.isConnected) {
            startUnbindTimerTask();
        }
    }

    public void onTitleClick(View v) {
        updateBeams();
        if (!this.isTitleDropDownVisible) {
            setTitleDropDownVisible(true, true);
        } else if (!this.isTitleEditable) {
            if (v.getId() == R.id.title_disclosure) {
                setTitleEditable(true);
            } else {
                setTitleDropDownVisible(false, true);
            }
        }
    }

    public void onTitleDropdownWrapperClick(View v) {
        setTitleDropDownVisible(false, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTitleDropDownVisible(boolean visible, boolean animated) {
        this.isTitleDropDownVisible = visible;
        if (visible) {
            this.titleDropDownWrapper.setVisibility(View.VISIBLE);
            if (animated) {
                this.titleDropDownWrapper.setAlpha(0.0f);
                this.titleDropdownListView.setTranslationY((float) (-this.titleDropdownListView.getHeight()));
                this.titleDropdownListView.animate().translationY(0.0f);
                this.titleDropDownWrapper.animate().alpha(1.0f);
            }
            this.titleDisclosure.setImageResource(R.drawable.ic_edit);
            return;
        }
        if (animated) {
            this.titleDropdownListView.animate().translationY((float) (-this.titleDropdownListView.getHeight())).setListener(new AnimatorListenerAdapter() {
                /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass9 */

                public void onAnimationEnd(Animator animation) {
                    if (!TestActivity.this.isTitleDropDownVisible) {
                        TestActivity.this.titleDropDownWrapper.setVisibility(View.VISIBLE);
                    }
                }
            });
            this.titleDropDownWrapper.animate().alpha(0.0f);
        } else {
            this.titleDropDownWrapper.setVisibility(View.INVISIBLE);
        }
        this.titleDisclosure.setImageResource(R.drawable.ic_arrow_down);
        setTitleEditable(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTitleEditable(boolean editable) {
        if (editable) {
            Log.d(TAG, "setting editable true");
            this.oldBeamName = this.titleEditText.getText().toString();
            this.titleEditText.setKeyListener(this.titleKeyListener);
            this.titleEditText.setCursorVisible(true);
            this.titleEditText.requestFocus();
            this.titleEditText.setSelection(this.titleEditText.getText().length());
            this.titleEditText.setImeOptions(6);
            this.titleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass10 */

                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId != 6) {
                        return false;
                    }
                    String newBeamName = TestActivity.this.titleEditText.getText().toString();
                    Log.d(TestActivity.TAG, "Renamed beam to " + newBeamName);
                    TestActivity.this.selectedBeam.title = newBeamName;
                    TestActivity.this.setTitleEditable(false);
                    return true;
                }
            });
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.titleEditText, 1);
        } else {
            Log.d(TAG, "setting editable false");
            this.titleEditText.setKeyListener(null);
            this.titleEditText.setCursorVisible(false);
            this.titleDisclosure.requestFocus();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.titleEditText.getWindowToken(), 0);
            if (!this.oldBeamName.equals(this.titleEditText.getText().toString()) && !this.oldBeamName.equals("")) {
                this.oldBeamName = this.titleEditText.getText().toString();
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("name", this.oldBeamName, "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.isTitleEditable = editable;
    }

    public void onTabClick(View v) {
        if (v.getId() == R.id.tab_gallery) {
            if (this.selectedBeam.version < 3) {
                showToast(getString(R.string.update_beam_service));
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("ACTION_VIEW", "Beam update", Uri.parse("http://beamlabsinc.com/beam-updater").toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            } else if (this.selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH && !ConnectionServiceController.getInstance().getBinder().isConnectedToImageSocket()) {
                showToast(getString(R.string.turn_on_wifi_to_stream_photos));
                return;
            }
        }
        setSelectedTabId(v.getId(), true);
    }

    private void setSelectedTabId(int tabId, boolean animated) {
        int prevSelectedTabId = this.selectedTabId;
        this.selectedTabId = tabId;
        if (BeamSettings.getInstance().getSelectedBeam() != null) {
            int inactiveColor = getResources().getColor(R.color.dark_gray);
            switch (tabId) {
                case R.id.tab_keypad:
                    this.tabKeypad.setTextColor(-1);
                    this.tabTouchpad.setTextColor(inactiveColor);
                    this.tabKeyboard.setTextColor(inactiveColor);
                    this.tabGallery.setTextColor(inactiveColor);
                    break;
                case R.id.tab_touchpad:
                    this.tabKeypad.setTextColor(inactiveColor);
                    this.tabTouchpad.setTextColor(-1);
                    this.tabKeyboard.setTextColor(inactiveColor);
                    this.tabGallery.setTextColor(inactiveColor);
                    break;
                case R.id.tab_keyboard:
                    this.tabKeypad.setTextColor(inactiveColor);
                    this.tabTouchpad.setTextColor(inactiveColor);
                    this.tabKeyboard.setTextColor(-1);
                    this.tabGallery.setTextColor(inactiveColor);
                    break;
                case R.id.tab_gallery:
                    this.tabKeypad.setTextColor(inactiveColor);
                    this.tabTouchpad.setTextColor(inactiveColor);
                    this.tabKeyboard.setTextColor(inactiveColor);
                    this.tabGallery.setTextColor(-1);
                    break;
            }
            setContentFragmentByTabId(tabId, prevSelectedTabId, animated);
        }
    }

    private void setContentFragmentByTabId(int tabId, int prevSelectedTabId, boolean animated) {
        Log.i(TAG, "setContentFragmentByTabId: " + tabId);
        int inAnim = R.animator.card_flip_left_in;
        int outAnim = R.animator.card_flip_left_out;
        Log.d(TAG, "Fragment is " + this.fragment + ", fragmentTag " + this.fragmentTag);
        boolean isImageShownInFullScreen = BeamSettings.getInstance().isImageShownInFullScreen();
        if (isImageShownInFullScreen) {
            BeamSettings.getInstance().setImageShownInFullScreen(false);
        }
        switch (tabId) {
            case R.id.tab_touchpad:
                this.fragmentTag = "touchpad";
                this.fragment = getFragmentManager().findFragmentByTag(this.fragmentTag);
                if (this.fragment == null) {
                    Log.e("", "touch is null");
                    this.fragment = TouchpadFragment.newInstance();
                }
                if (prevSelectedTabId == R.id.tab_keypad) {
                    inAnim = R.animator.card_flip_right_in;
                    outAnim = R.animator.card_flip_right_out;
                    break;
                }
                break;
            case R.id.tab_keyboard:
                this.fragmentTag = "keyboard";
                this.fragment = getFragmentManager().findFragmentByTag(this.fragmentTag);
                if (this.fragment == null) {
                    Log.e("", "keyboard is null");
                    this.fragment = KeyboardFragment.newInstance();
                }
                inAnim = R.animator.card_flip_right_in;
                outAnim = R.animator.card_flip_right_out;
                break;
            case R.id.tab_gallery:
                this.fragmentTag = "gallery";
                this.fragment = getFragmentManager().findFragmentByTag(this.fragmentTag);
                if (this.fragment == null) {
                    this.fragment = GalleryFragment.newInstance(this);
                }
                if (!isImageShownInFullScreen) {
                    inAnim = R.animator.card_flip_right_in;
                    outAnim = R.animator.card_flip_right_out;
                    break;
                } else {
                    ((GalleryFragment) this.fragment).closeFullScreenView();
                    return;
                }
            default:
                this.fragmentTag = "keypad";
                this.fragment = getFragmentManager().findFragmentByTag(this.fragmentTag);
                if (this.fragment == null) {
                    Log.e("", "keypad is null");
                    this.fragment = KeypadFragment.newInstance();
                    break;
                }
                break;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animated) {
            transaction.setCustomAnimations(inAnim, outAnim);
        }
        transaction.replace(R.id.content_frame, this.fragment, this.fragmentTag).addToBackStack(null).commit();
        if (tabId == R.id.tab_keyboard) {
            getFragmentManager().executePendingTransactions();
            ((KeyboardFragment) this.fragment).onFragmentSelected();
        } else if (prevSelectedTabId == R.id.tab_keyboard) {
            this.fragmentTag = "keyboard";
            this.fragment = getFragmentManager().findFragmentByTag(this.fragmentTag);
            if (this.fragment != null) {
                ((KeyboardFragment) this.fragment).onFragmentDismissed();
            }
        }
    }

    public void onActionClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                ConnectionServiceController.getInstance().getBinder().sendSocket("b", "3");
                return;
            case R.id.btn_home:
                ConnectionServiceController.getInstance().getBinder().sendSocket("b", "2");
                return;
            case R.id.btn_rotate:
                Log.d("ROTATETEST", "Sending Rot");
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("rot", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_sets:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("sets", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_multitask:
                if (isTestingApp) {
                    try {
                        ConnectionServiceController.getInstance().getBinder().sendSocket("testapp", "0", "0");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                } else {
                    try {
                        ConnectionServiceController.getInstance().getBinder().sendSocket("recents", "0", "0");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            case R.id.btn_onoff:
                Log.d("MOUSECON", "Screen button pressed!");
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("screen", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tempDisablePowerButtons();
                return;
            case R.id.btn_moreless:
                setBottomBarExtended(!this.isBottomBarExtended, true);
                return;
            case R.id.bottom_bar_extended:
            default:
                return;
            case R.id.btn_all_apps:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("apps", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_settings:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("settings", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_rotate_bot:
                Log.d("ROTATETEST", "Sending Rot Bot");
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("rot", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_sets_bot:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("sets", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_multitask_bot:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("recents", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_actions:
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("actions", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            case R.id.btn_light:
                Log.d(TAG, "Set light percentage " + this.selectedBeam.led);
                this.projectorLightFragment.setLightPercentage(((float) this.selectedBeam.led) / 3.3f);
                setProjectorLightActive(true, true);
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("led", ((int) (this.projectorLightFragment.getLightPercentage() * 3.3f)) + "", "3");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.projectorLightFragment.setLightPercentage((float) this.selectedBeam.led);
                return;
        }
    }

    private void setBottomBarExtended(boolean extended, boolean animated) {
        this.isBottomBarExtended = extended;
        if (extended) {
            if (animated) {
                this.bottomBar.setTranslationY((float) this.bottomBarExtended.getHeight());
                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(this.bottomBar, "translationY", 0.0f);
                translateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass11 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        TestActivity.this.layoutContentFrame();
                    }
                });
                translateAnim.start();
            } else {
                this.bottomBarExtended.post(new Runnable() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass12 */

                    public void run() {
                        TestActivity.this.bottomBar.setTranslationY(0.0f);
                        TestActivity.this.layoutContentFrame();
                    }
                });
            }
            this.btnMoreLess.setText(R.string.btn_less);
            return;
        }
        if (animated) {
            ObjectAnimator translateAnim2 = ObjectAnimator.ofFloat(this.bottomBar, "translationY", (float) this.bottomBarExtended.getHeight());
            translateAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass13 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    TestActivity.this.layoutContentFrame();
                }
            });
            translateAnim2.start();
        } else {
            this.bottomBarExtended.post(new Runnable() {
                /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass14 */

                public void run() {
                    TestActivity.this.bottomBar.setTranslationY((float) TestActivity.this.bottomBarExtended.getHeight());
                    TestActivity.this.layoutContentFrame();
                }
            });
        }
        this.btnMoreLess.setText(R.string.btn_more);
    }

    private void setProjectorLightActive(boolean active, boolean animated) {
        this.isProjectorLightActive = active;
        if (active) {
            if (animated) {
                AnimatorSet animSet = new AnimatorSet();
                AnimatorSet inAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_in);
                inAnim.setTarget(this.projectorLightUI);
                AnimatorSet outAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
                outAnim.setTarget(this.defaultUI);
                animSet.play(inAnim).with(outAnim);
                animSet.addListener(new AnimatorListenerAdapter() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass15 */

                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        TestActivity.this.defaultUI.setVisibility(View.INVISIBLE);
                    }
                });
                animSet.start();
            } else {
                this.defaultUI.setVisibility(View.INVISIBLE);
            }
        } else if (animated) {
            Log.d(TAG, "anianiania");
            AnimatorSet animSet2 = new AnimatorSet();
            AnimatorSet inAnim2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
            inAnim2.setTarget(this.defaultUI);
            AnimatorSet outAnim2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out);
            outAnim2.setTarget(this.projectorLightUI);
            animSet2.play(inAnim2).with(outAnim2);
            animSet2.addListener(new AnimatorListenerAdapter() {
                /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass16 */

                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    TestActivity.this.defaultUI.setVisibility(View.VISIBLE);
                }
            });
            animSet2.start();
        } else {
            this.defaultUI.setVisibility(View.VISIBLE);
        }
        tempDisablePowerButtons();
    }

    private void tempDisablePowerButtons() {
        if (this.btnOnOff != null) {
            findViewById(R.id.btn_onoff).setAlpha(0.5f);
            findViewById(R.id.btn_light).setAlpha(0.5f);
            this.btnOnOff.setEnabled(false);
            this.btnLight.setEnabled(false);
        }
        if (this.projectorLightFragment != null) {
            this.projectorLightFragment.setSwitchToProjectorEnabled(false);
        }
        new Handler().postDelayed(new Runnable() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass17 */

            public void run() {
                if (TestActivity.this.btnOnOff != null) {
                    TestActivity.this.btnOnOff.setEnabled(true);
                    TestActivity.this.btnLight.setEnabled(true);
                    ((TypefaceButton) TestActivity.this.findViewById(R.id.btn_onoff)).setAlpha(0.6f);
                    ((TypefaceButton) TestActivity.this.findViewById(R.id.btn_light)).setAlpha(0.6f);
                    final Handler handje = new Handler();
                    handje.postDelayed(new Runnable() {
                        /* class com.studiodiip.bulbbeam.mousecontroller.activity.TestActivity.AnonymousClass17.AnonymousClass1 */

                        public void run() {
                            if (TestActivity.this.btnOnOff != null) {
                                TestActivity.this.btnOnOff.setEnabled(true);
                                TestActivity.this.btnLight.setEnabled(true);
                                TypefaceButton btn1 = (TypefaceButton) TestActivity.this.findViewById(R.id.btn_onoff);
                                TypefaceButton btn2 = (TypefaceButton) TestActivity.this.findViewById(R.id.btn_light);
                                if (btn1.getAlpha() < 1.0f) {
                                    btn1.setAlpha(btn1.getAlpha() + 0.1f);
                                    btn2.setAlpha(btn2.getAlpha() + 0.1f);
                                    handje.postDelayed(this, 30);
                                }
                            }
                            if (TestActivity.this.projectorLightFragment != null) {
                                TestActivity.this.projectorLightFragment.setSwitchToProjectorEnabled(true);
                            }
                        }
                    }, 30);
                }
                if (TestActivity.this.projectorLightFragment != null) {
                    TestActivity.this.projectorLightFragment.setSwitchToProjectorEnabled(true);
                }
            }
        }, 3000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void layoutContentFrame() {
        int defaultHeight = (this.defaultUI.getHeight() - ((int) this.contentFrame.getY())) - this.bottomBar.getHeight();
        this.contentFrame.getLayoutParams().height = ((int) this.bottomBar.getTranslationY()) + defaultHeight;
        this.contentFrame.requestLayout();
    }

    public void onBtnHideProjectorLightClick(View v) {
        switch (v.getId()) {
            case R.id.btnHideProjectorLight:
                Log.d(TAG, "onBtnHideProjectorLightClick");
                this.selectedBeam.led = (int) (this.projectorLightFragment.getLightPercentage() * 3.3f);
                Log.d(TAG, "New led level " + this.selectedBeam.led);
                setProjectorLightActive(false, true);
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("screen", "0", "0");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            default:
                return;
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.view.Touchpad.TouchpadListener
    public void onTouchpadTouched(Touchpad touchpad, MotionEvent event) {
        long nowtime = System.currentTimeMillis();
        float newX = event.getX();
        float newY = event.getY();
        float x = this.oldX - newX;
        float y = this.oldY - newY;
        int x_int = -((int) x);
        int y_int = -((int) y);
        float xAxisMultiplier = 854.0f / ((float) touchpad.getWidth());
        float yAxisMultiplier = 480.0f / ((float) touchpad.getHeight());
        if (ConnectionServiceController.getInstance().getBinder() != null) {
            if (event.getPointerCount() > 1) {
                MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                switch (event.getActionMasked()) {
                    case 2:
                        event.getPointerCoords(event.getActionIndex(), coords);
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            event.getPointerCoords(i, coords);
                            int mtx = (int) (coords.getAxisValue(0) * xAxisMultiplier);
                            ConnectionServiceController.getInstance().getBinder().sendSocket("mmtm" + i, mtx + ";" + ((int) (coords.getAxisValue(1) * yAxisMultiplier)));
                        }
                        return;
                    case 3:
                    case 4:
                    default:
                        return;
                    case 5:
                        if (event.getPointerCount() == 2) {
                            event.getPointerCoords(0, coords);
                            ConnectionServiceController.getInstance().getBinder().sendSocket("mmtd0", ((int) (coords.getAxisValue(0) * xAxisMultiplier)) + ";" + ((int) (coords.getAxisValue(1) * yAxisMultiplier)));
                            event.getPointerCoords(1, coords);
                            ConnectionServiceController.getInstance().getBinder().sendSocket("mmtd1", ((int) (coords.getAxisValue(0) * xAxisMultiplier)) + ";" + ((int) (coords.getAxisValue(1) * yAxisMultiplier)));
                            return;
                        }
                        return;
                    case 6:
                        event.getPointerCoords(0, coords);
                        ConnectionServiceController.getInstance().getBinder().sendSocket("mmtu0", ((int) (coords.getAxisValue(0) * xAxisMultiplier)) + ";" + ((int) (coords.getAxisValue(1) * yAxisMultiplier)));
                        event.getPointerCoords(1, coords);
                        ConnectionServiceController.getInstance().getBinder().sendSocket("mmtu1", ((int) (coords.getAxisValue(0) * xAxisMultiplier)) + ";" + ((int) (coords.getAxisValue(1) * yAxisMultiplier)));
                        return;
                }
            } else {
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
                                ConnectionServiceController.getInstance().getBinder().sendSocket("mtr", x_int + ";" + y_int);
                            }
                        } else if (!this.currentTouchIsPinch) {
                            ConnectionServiceController.getInstance().getBinder().sendSocket(Integer.toString((int) x), Integer.toString((int) y));
                        }
                        this.currentTouchIsMulti = false;
                        this.currentTouchIsPinch = false;
                        return;
                    case 2:
                        if (nowtime - this.lasttime > 30) {
                            if (!this.currentTouchIsPinch) {
                                ConnectionServiceController.getInstance().getBinder().sendSocket("mmr", x_int + ";" + y_int);
                            }
                            this.lasttime = nowtime;
                            this.oldX = newX;
                            this.oldY = newY;
                            return;
                        }
                        return;
                    case 3:
                    case 4:
                    case 5:
                    default:
                        return;
                    case 6:
                        if (event.getPointerCount() == 1) {
                        }
                        return;
                }
            }
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.fragment.KeypadFragment.keyPressListener, com.studiodiip.bulbbeam.mousecontroller.fragment.KeyboardFragment.keyPressListener, com.studiodiip.bulbbeam.mousecontroller.fragment.ProjectorLightFragment.keyPressListener
    public void onKeyPress(CharSequence keys) {
        if (keys.length() <= 0) {
            return;
        }
        if (keys.equals("BACKSPACE")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("BACKSPACE", "kand");
        } else if (keys.equals("ENTER")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("ENTER", "kand");
        } else if (keys.toString().contains("led;")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket(keys.toString(), "led");
        } else if (keys.equals("left")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("left", "nav");
        } else if (keys.equals("right")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("right", "nav");
        } else if (keys.equals("up")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("up", "nav");
        } else if (keys.equals("down")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("down", "nav");
        } else if (keys.equals("ok")) {
            ConnectionServiceController.getInstance().getBinder().sendKeyToSocket("ok", "nav");
        } else {
            String s = "" + keys.charAt(keys.length() - 1);
            if ("#".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "51", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "51", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (";".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "59", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "59", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("*".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "56", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "56", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("(".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "57", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "57", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (")".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "48", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "48", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("\\".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "92", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "92", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("\"".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "222", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "222", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("'".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "222", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "222", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("<".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "44", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "44", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (">".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "62", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "62", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("|".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "92", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "92", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("&".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "55", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "55", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("?".equals(s)) {
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "47", "down");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "47", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    ConnectionServiceController.getInstance().getBinder().sendSocket("key", "16", "up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                ConnectionServiceController.getInstance().getBinder().sendKeyToSocket(s, "kand");
            }
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.util.ShowImage.IShowImageListener
    public void onBitMapCalculated(String bitMap) {
        Log.d(TAG, "onBitMapCalculated ");
        if (bitMap != null) {
            ConnectionServiceController.getInstance().getBinder().sendSocket("img", bitMap);
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.fragment.GalleryFragment.IGalleryFragmentListener
    public void onImageTranslated(float translateX, float translateY, float scale, float px, float py) {
        ConnectionServiceController.getInstance().getBinder().sendSocket("trans", translateX + ":" + translateY + ":" + scale + ":" + px + ":" + py);
        Log.d(TAG, "onImageTranslated ");
    }

    @Override // android.support.v4.app.FragmentActivity
    public void onBackPressed() {
        Log.d(TAG, "OnBackPressed " + BeamSettings.isConnected);
        if (this.isProjectorLightActive) {
            setProjectorLightActive(false, true);
            return;
        }
        BeamSettings.isConnected = false;
        ConnectionServiceController.getInstance().unbindConnectionService();
        BeamSettings.shareIntent = null;
        finish();
    }
}
