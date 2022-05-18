package com.studiodiip.bulbbeam.mousecontroller.activity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.ble.BleManager;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;
import com.studiodiip.bulbbeam.mousecontroller.service.ConnectionServiceController;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;
import com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer;
import com.studiodiip.bulbbeam.mousecontroller.view.SplashImageView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity implements BleManager.IBleInterface {
    private static final int MAX_RETRY_COUNT = 4;
    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int SCANNING_TEXT_CHANGE_INTERVAL = 10000;
    public static final String SHOULD_SCAN_FULLY = "shouldScanFully";
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static boolean isConnectedToBeam = false;
    private int amountRetries = 0;
    private Button btnTryAgain;
    private ChangeScanningTextTimerTask changeScanningTextTimerTask;
    private ConnectionServiceController connectionServiceController;
    private SplashImageView imageView;
    private boolean isConnecting = false;
    private BroadcastReceiver mWifiBeamsReceiver = new BroadcastReceiver() {
        /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.AnonymousClass2 */

        public void onReceive(Context context, Intent receiveIntent) {
            ArrayList<BeamBulb> wifiBeamList = receiveIntent.getParcelableArrayListExtra(BulbDiscoverer.RECEIVER_WIFI_LIST);
            Log.d(SplashActivity.TAG, "Beams discovered by wifi - " + wifiBeamList.size());
            if (wifiBeamList.size() == 0) {
                Log.e(SplashActivity.TAG, "NO BULB FOUND!");
                BeamBulb bulb = SplashActivity.this.settings.getLastUsedBeam();
                if (bulb == null || bulb.connectionType != BeamConnectionType.CONNECTION_TYPE_WIFI) {
                    SplashActivity.this.onScanningCompletedWithError("No beams found", true);
                    if (SplashActivity.this.amountRetries >= 4) {
                        SplashActivity.this.stopChangeScanningTextTimerTask();
                        SplashActivity.this.showMobileDataOnDialog();
                        return;
                    }
                    return;
                }
                Log.e(SplashActivity.TAG, "Resetting the last used beam to null and searching again");
                SplashActivity.this.settings.setLastUsedBeam(null);
                BulbDiscoverer.getInstance().findBulbs(SplashActivity.this, true, (SplashActivity.this.amountRetries * 2000) + 3000);
                return;
            }
            Iterator<BeamBulb> it = wifiBeamList.iterator();
            while (it.hasNext()) {
                BulbDiscoverer.addWifiBeam(it.next());
            }
            Log.d(SplashActivity.TAG, "Total beam connections " + BulbDiscoverer.beamBulbs.size());
            if (!SplashActivity.this.shouldDoFullScan || BleManager.CURRENT_BLUETOOTH_STATE != 12) {
                SplashActivity.this.setUpDefaultConnection();
            } else {
                Log.d(SplashActivity.TAG, "full scan .. bluetooth is till scanning .. Lets wait");
            }
        }
    };
    private boolean marshmallowAlert = false;
    private boolean registered = false;
    private BeamSettings settings;
    private boolean shouldDoFullScan = false;
    private boolean shouldForceSearching = false;
    private TextView textView;

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart SplashActivity");
    }

    private void showMessage(final String message, final boolean startAnimation, final boolean showBtn) {
        runOnUiThread(new Runnable() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.AnonymousClass1 */

            public void run() {
                Log.d(SplashActivity.TAG, "Show Message " + message);
                if (!startAnimation || SplashActivity.this.imageView.isRunning()) {
                    Log.d(SplashActivity.TAG, "Stopping animation");
                    SplashActivity.this.imageView.stopAnimation();
                } else {
                    SplashActivity.this.imageView.startAnimation();
                }
                SplashActivity.this.textView.setText(message);
                SplashActivity.this.btnTryAgain.setVisibility(showBtn ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT /*{ENCODED_INT: 1001}*/:
                if (resultCode == 0) {
                    Log.d(TAG, "User denied turning bluetooth on");
                    return;
                } else if (resultCode == -1) {
                    Log.d(TAG, "User pressed allow");
                    return;
                } else {
                    return;
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                return;
        }
    }

    private BeamBulb selectBeam(BeamBulb selectedBeam) {
        if (selectedBeam == null) {
            return null;
        }
        for (int index = 0; index < BulbDiscoverer.beamBulbs.size(); index++) {
            BeamBulb bulb = BulbDiscoverer.beamBulbs.get(index);
            if (bulb.connectionType == selectedBeam.connectionType && selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH && !bulb.mac.isEmpty() && bulb.mac.equals(selectedBeam.mac)) {
                return bulb;
            }
            if (bulb.connectionType == selectedBeam.connectionType && selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_WIFI && !bulb.ip.isEmpty() && bulb.ip.equals(selectedBeam.ip)) {
                return bulb;
            }
        }
        return null;
    }

    private void startConnectionService(BeamBulb bulb) {
        Log.d(TAG, "startConnectionService " + this.isConnecting);
        if (this.isConnecting) {
            Log.d(TAG, "Already attempting to connect");
            return;
        }
        this.isConnecting = true;
        showMessage(getString(R.string.connecting_to_beam), true, false);
        this.settings.setSelectedBeam(bulb);
        if (BleManager.isScanningStopped) {
            Log.d(TAG, "Scanning paused ");
            return;
        }
        this.connectionServiceController.startConnectionService(bulb.ip, bulb.mac, bulb.connectionType);
        this.isConnecting = false;
        this.amountRetries = 0;
        startTestActivity();
    }

    private void showNoBluetoothDevicesDialog() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.no_bluetooth_devices_dialog)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.AnonymousClass3 */

            public void onClick(DialogInterface dialog, int which) {
                SplashActivity.this.finish();
            }
        }).show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMobileDataOnDialog() {
        if (!getMobileDataState()) {
            Log.d(TAG, "cellular data is off");
            return;
        }
        Log.d(TAG, "cellular data is on");
        @SuppressLint("MissingPermission") String SSID = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
        Log.d(TAG, "SSID: " + SSID);
        if (!SSID.startsWith("\"Beam_")) {
            Log.d(TAG, "ssid doesnt start with beam");
            return;
        }
        Log.d(TAG, "ssid starts with beam");
        this.marshmallowAlert = true;
        new AlertDialog.Builder(this).setMessage(getString(R.string.turn_off_data_dialog)).setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.AnonymousClass5 */

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.marshmallowAlert = false;
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.AnonymousClass4 */

            public void onClick(DialogInterface dialog, int which) {
                SplashActivity.this.finish();
            }
        }).show();
    }

    private void startTestActivity() {
        Intent startIntent = new Intent(this, TestActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        BeamBulb selectedBEam = this.settings.getSelectedBeam();
        if (selectedBEam != null && selectedBEam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
            startIntent.putExtra("bluetooth", true);
        }
        startActivity(startIntent);
        finish();
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null) {
            BeamSettings.shareIntent = intent;
        }
    }

    public boolean getMobileDataState() {
        try {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled", new Class[0]);
            if (getMobileDataEnabledMethod != null) {
                return ((Boolean) getMobileDataEnabledMethod.invoke(telephonyService, new Object[0])).booleanValue();
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        Log.d("SplashActivity", "On create!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(128);
        BulbDiscoverer.init(this);
        BeamSettings.init(this);
        ConnectionServiceController.init(getApplicationContext());
        BleManager.init(this);
        this.settings = BeamSettings.getInstance();
        BeamSettings.isConnected = false;
        this.connectionServiceController = ConnectionServiceController.getInstance();
        this.textView = (TextView) findViewById(R.id.textView);
        this.textView.setTypeface(Typeface.createFromAsset(getAssets(), "apercu-bold-webfont.ttf"));
        this.imageView = (SplashImageView) findViewById(R.id.imageView);
        this.btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
        this.amountRetries = 0;
        isConnectedToBeam = false;
        this.shouldForceSearching = true;
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d(TAG, "is Marshmallow");
            showMobileDataOnDialog();
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (intent != null) {
            if (intent.hasExtra(SHOULD_SCAN_FULLY)) {
                this.shouldDoFullScan = intent.getBooleanExtra(SHOULD_SCAN_FULLY, false);
            } else if (intent.hasExtra("button")) {
                this.btnTryAgain.setText(R.string.btn_try_again);
                showMessage(getString(R.string.lost_connectivity), true, true);
            } else if (!"android.intent.action.SEND".equals(action) || type == null) {
                BeamSettings.shareIntent = null;
            } else {
                Log.d(TAG, "action send " + action);
                BeamSettings.shareIntent = intent;
            }
            Log.d(TAG, "shouldDoFullScan" + this.shouldDoFullScan);
        }
    }

    /* access modifiers changed from: private */
    public class ChangeScanningTextTimerTask extends TimerTask {
        private ChangeScanningTextTimerTask() {
        }

        public void run() {
            Log.d(SplashActivity.TAG, "run(), Wifi - " + BulbDiscoverer.CURRENT_WIFI_STATE + ",Ble " + BleManager.CURRENT_BLUETOOTH_STATE);
            if (BleManager.CURRENT_BLUETOOTH_STATE == 12 || BulbDiscoverer.CURRENT_WIFI_STATE == BulbDiscoverer.STATE_WIFI_SCANNING) {
                Log.d(SplashActivity.TAG, "time to change scanning text");
                SplashActivity.this.runOnUiThread(new Runnable() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity.ChangeScanningTextTimerTask.AnonymousClass1 */

                    public void run() {
                        SplashActivity.this.textView.setText(R.string.still_scanning_for_beams);
                    }
                });
            }
            SplashActivity.this.stopChangeScanningTextTimerTask();
        }
    }

    private void startChangeScanningTextTimerTask() {
        Log.d(TAG, "startChangeScanningTextTimerTask");
        stopChangeScanningTextTimerTask();
        this.changeScanningTextTimerTask = new ChangeScanningTextTimerTask();
        new Timer().schedule(this.changeScanningTextTimerTask, 10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopChangeScanningTextTimerTask() {
        Log.d(TAG, "stopChangeScanningTextTimerTask");
        if (this.changeScanningTextTimerTask != null) {
            this.changeScanningTextTimerTask.cancel();
            this.changeScanningTextTimerTask = null;
            Log.d(TAG, "Stopped Scanning timer task ");
        }
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "On resume!");
        registerReceivers();
        if (this.marshmallowAlert) {
            Log.d(TAG, "Return from marshmallow alert dialog");
            return;
        }
        BleManager.isScanningStopped = false;
        BulbDiscoverer.CURRENT_WIFI_STATE = BulbDiscoverer.STATE_IDLE;
        BeamBulb selectedBeam = this.settings.getSelectedBeam();
        if (selectedBeam == null) {
            Log.d(TAG, "Selected beam is null");
            startSearch(this.shouldForceSearching);
            return;
        }
        Log.d(TAG, "Selected beam " + selectedBeam.ip + ",mac" + selectedBeam.mac);
        if (BeamSettings.shareIntent == null) {
            BleManager.getInstance().closeCurrentConnection();
        }
        if (selectedBeam.connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH) {
            showMessage(getString(R.string.connecting_to_beam), true, false);
            BleManager.getInstance().switchToBeam(selectedBeam);
            return;
        }
        startConnectionService(selectedBeam);
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "On pause!");
        unRegisterReceivers();
        this.isConnecting = false;
        stopChangeScanningTextTimerTask();
        if (12 == BleManager.CURRENT_BLUETOOTH_STATE) {
            BleManager.isScanningStopped = true;
            BleManager.getInstance().stopScan();
            BleManager.getInstance().setUpCurrentBeam(null);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unRegisterReceivers();
        this.isConnecting = false;
    }

    private void registerReceivers() {
        Log.d(TAG, "registerReceivers");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mWifiBeamsReceiver, new IntentFilter(BulbDiscoverer.RECEIVER_ACTION));
        this.registered = true;
        Log.d(TAG, "registerReceivers done");
    }

    private void unRegisterReceivers() {
        Log.d(TAG, "unRegisterReceivers " + this.registered);
        if (this.registered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mWifiBeamsReceiver);
            this.registered = false;
            Log.d(TAG, "unregisterReceivers done");
        }
    }

    private void startSearch(boolean forceSearch) {
        Log.d(TAG, "start search " + isConnectedToBeam + ",Should scan fully " + this.shouldDoFullScan);
        Log.d(TAG, "amountRetries: " + this.amountRetries);
        this.isConnecting = false;
        this.settings.setSelectedBeam(null);
        BulbDiscoverer.clear();
        showMessage(getString(R.string.scanning_for_beams), true, false);
        BleManager.getInstance().scanLeDevices(this.shouldDoFullScan);
        BeamBulb bulb = this.settings.getLastUsedBeam();
        if (bulb == null || bulb.connectionType != BeamConnectionType.CONNECTION_TYPE_WIFI || this.shouldDoFullScan) {
            BulbDiscoverer.getInstance().findBulbs(this, forceSearch, 5000);
            return;
        }
        String lastUsedBeamIp = bulb.ip;
        Log.d(TAG, "Last used beam ip address " + lastUsedBeamIp);
        BulbDiscoverer.getInstance().findLastBulb(this, lastUsedBeamIp);
    }

    public void onBtnTryAgainClick(View v) {
        Log.d(TAG, "onBtnTryAgainClick");
        this.btnTryAgain.setText(R.string.btn_try_again);
        this.settings.setSelectedBeam(null);
        this.settings.setLastUsedBeam(null);
        this.connectionServiceController.unbindConnectionService();
        startSearch(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setUpDefaultConnection() {
        Log.d(TAG, "setUpDefaultConnection " + isConnectedToBeam);
        if (isConnectedToBeam) {
            Log.d(TAG, "Already connected to beam");
        } else if (BulbDiscoverer.beamBulbs.size() == 0) {
            Log.d(TAG, "No beam found");
            showMessage(getString(R.string.connect_to_same_wifi_turn_on_bluetooth_in_beam), false, true);
        } else {
            isConnectedToBeam = true;
            BeamBulb beamToConnect = selectBeam(this.settings.getLastUsedBeam());
            if (beamToConnect == null) {
                Log.d(TAG, "getLastSelectedBeam is null");
                beamToConnect = BulbDiscoverer.beamBulbs.get(0);
            }
            if (beamToConnect.connectionType != BeamConnectionType.CONNECTION_TYPE_BLUETOOTH || BleManager.getInstance().setUpCurrentBeam(beamToConnect)) {
                this.settings.setSelectedBeam(beamToConnect);
                startConnectionService(beamToConnect);
                return;
            }
            showMessage(getString(R.string.beam_disconnected), false, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onScanningCompletedWithError(String errMess, boolean isWifiScanningFailed) {
        int wifiState = BulbDiscoverer.CURRENT_WIFI_STATE;
        int bluetoothState = BleManager.CURRENT_BLUETOOTH_STATE;
        Log.d(TAG, "onScanningCompletedWithError  Wifi " + wifiState + ",BLE " + bluetoothState);
        if (isWifiScanningFailed) {
            if (bluetoothState != 12) {
                if (bluetoothState == BleManager.STATE_BEAM_FOUND && BulbDiscoverer.beamBulbs.size() > 0) {
                    setUpDefaultConnection();
                } else if (bluetoothState != 11 && bluetoothState != BleManager.STATE_NO_BEAM) {
                } else {
                    if (bluetoothState == 11 && wifiState == BulbDiscoverer.STATE_WIFI_OFF) {
                        showMessage(getString(R.string.turn_on_wifi_bluetooth), false, true);
                        this.amountRetries++;
                    } else if (bluetoothState == 11 || bluetoothState == BleManager.STATE_NO_BEAM) {
                        showMessage(getString(R.string.connect_to_same_wifi_turn_on_bluetooth_in_beam), false, true);
                        this.amountRetries++;
                    }
                }
            }
        } else if (wifiState == BulbDiscoverer.STATE_WIFI_SCANNING) {
        } else {
            if (wifiState == BulbDiscoverer.STATE_BEAM_FOUND && BulbDiscoverer.beamBulbs.size() > 0) {
                setUpDefaultConnection();
            } else if (wifiState != BulbDiscoverer.STATE_WIFI_OFF && wifiState != BulbDiscoverer.STATE_NO_BEAM) {
            } else {
                if (bluetoothState == 11 && wifiState == BulbDiscoverer.STATE_WIFI_OFF) {
                    showMessage(getString(R.string.turn_on_wifi_bluetooth), false, true);
                    this.amountRetries++;
                } else if (wifiState == BulbDiscoverer.STATE_NO_BEAM || wifiState == BulbDiscoverer.STATE_WIFI_OFF) {
                    showMessage(getString(R.string.connect_to_same_wifi_turn_on_bluetooth_in_beam), false, true);
                    this.amountRetries++;
                }
            }
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.ble.BleManager.IBleInterface
    public void enableBle() {
        Log.d(TAG, "enableBle");
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.ble.BleManager.IBleInterface
    public void supportsBle(boolean isSupported) {
        Log.d(TAG, "supportsBle " + isSupported);
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.ble.BleManager.IBleInterface
    public void onScanningCompleted(String errorMessage, List<BeamBulb> beamList) {
        Log.d(TAG, "onScanningCompleted " + errorMessage);
        if (errorMessage != null) {
            onScanningCompletedWithError(errorMessage, false);
            return;
        }
        Log.d(TAG, "Bulbdiscover.beambulb before adding ble beams  " + BulbDiscoverer.beamBulbs.size());
        BeamBulb toSwitchBeam = null;
        for (BeamBulb b : beamList) {
            BeamBulb wifiBeam = BulbDiscoverer.isConnectedByWifi(b);
            BeamBulb selectedBeam = this.settings.getSelectedBeam();
            if (wifiBeam == null) {
                BulbDiscoverer.beamBulbs.add(b);
            } else {
                if (selectedBeam != null && !selectedBeam.ip.isEmpty() && selectedBeam.ip.equals(wifiBeam.ip)) {
                    Log.d(TAG, "current beam is discovered by BLE");
                    toSwitchBeam = b;
                }
                BulbDiscoverer.beamBulbs.remove(wifiBeam);
                BulbDiscoverer.beamBulbs.add(b);
            }
        }
        Log.d(TAG, "Bulbdiscover.beambulb after adding ble beams  " + BulbDiscoverer.beamBulbs.size());
        if (this.shouldDoFullScan && BulbDiscoverer.CURRENT_WIFI_STATE == BulbDiscoverer.STATE_WIFI_SCANNING) {
            Log.d(TAG, "Full scan.. Wifi is still scanning ..Lets wait ");
        } else if (toSwitchBeam != null) {
            if (BleManager.getInstance().setUpCurrentBeam(toSwitchBeam)) {
                ConnectionServiceController.getInstance().unbindConnectionService();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ConnectionServiceController.getInstance().startConnectionService(toSwitchBeam.ip, toSwitchBeam.mac, toSwitchBeam.connectionType);
                this.settings.setSelectedBeam(toSwitchBeam);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BulbDiscoverer.BEAM_CHANGED_BROADCAST_ACTION));
            }
        } else if (!isConnectedToBeam || toSwitchBeam != null) {
            setUpDefaultConnection();
        } else {
            BleManager.getInstance().setUpCurrentBeam(null);
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.ble.BleManager.IBleInterface
    public void onSwitchingCompleted(String errorMessage) {
        Log.d(TAG, " onSwitchingCompleted " + errorMessage);
        if (errorMessage == null) {
            startConnectionService(BleManager.mCurrentBeam);
            startTestActivity();
            return;
        }
        showMessage(errorMessage, false, true);
    }
}
