package com.studiodiip.bulbbeam.mousecontroller.ble;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@TargetApi(21)
public class BleManager implements IBleScanner {
    public static int STATE_IDLE = 0;
    public static int CURRENT_BLUETOOTH_STATE = STATE_IDLE;
    public static int STATE_BEAM_FOUND = 4;
    public static final int STATE_BLUETOOTH_OFF = 11;
    public static final int STATE_BLUETOOTH_ON = 10;
    public static final int STATE_BLUETOOTH_SCANNING = 12;
    public static int STATE_NO_BEAM = 3;
    private static final String TAG = BleManager.class.getSimpleName();
    public static boolean isScanningStopped;
    private static IBleInterface mBleInterface;
    private static Context mContext;
    public static BeamBulb mCurrentBeam;
    private static BleManager manager;
    private final UUID BEAM_IP_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b86080cde");
    private final UUID BEAM_LED_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b66080cde");
    private final UUID BEAM_MAC_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b96080cde");
    private final UUID BEAM_MOUSE_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b45080cde");
    private final UUID BEAM_NAME_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b56080cde");
    private final UUID BEAM_SERVICE_UUID = UUID.fromString("06437bf5-4322-4461-80c6-1d2b45082929");
    private final UUID BEAM_VERSION_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b76080cde");
    private final UUID BEAM_VOLUME_CHARACTERISTIC = UUID.fromString("06437bf5-4322-4461-80c6-1d2b46080cde");
    private final int CHARACTERISTICS_COUNT = 7;
    private final String END_MESSAGE = "BEAMEPS";
    private final int PACKET_SIZE = 20;
    private final int READ_TIMEOUT_MS = 20000;
    private final int SCANNING_TIME_MS = 20000;
    private final String START_MESSAGE = "BEAMSPS";
    private int beamIndex = 0;
    private BleReadTimeOutTask bleReadTimeOutTask;
    private BleScanningTimerTask bleScanningTimerTask;
    private BleTimerTask bleTimer;
    private ArrayList<BeamBulb> mBeamList = new ArrayList<>();
    private ArrayList<BluetoothDevice> mBleDeviceTempList = new ArrayList<>();
    private final BluetoothGattCallback mBluetoothGattCallBack = new BluetoothGattCallback() {
        /* class com.studiodiip.bulbbeam.mousecontroller.ble.BleManager.AnonymousClass1 */

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case 0:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    if (BleManager.this.mGatt != null && gatt != null && gatt.getDevice().getAddress().equals(BleManager.this.mGatt.getDevice().getAddress())) {
                        BleManager.this.beamDisconnected();
                        return;
                    }
                    return;
                case 1:
                default:
                    Log.e("gattCallback", "STATE_OTHER");
                    return;
                case 2:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    return;
            }
        }

        @SuppressLint("MissingPermission")
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(BleManager.TAG, "onServicesDiscovered " + gatt.getDevice().getName());
            if (services.size() == 0) {
                Log.d(BleManager.TAG, "Invalid device");
                return;
            }
            BluetoothGattService service = null;
            Iterator<BluetoothGattService> it = services.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                BluetoothGattService s = it.next();
                if (s.getUuid().equals(BleManager.this.BEAM_SERVICE_UUID)) {
                    Log.d(BleManager.TAG, "Beam service UUID matches");
                    service = s;
                    break;
                }
            }
            if (service == null) {
                Log.d(BleManager.TAG, "Beam service UUID does not match");
                return;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            Log.i(BleManager.TAG, "Characteristics discovered " + characteristics.size());
            int count = 0;
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID gattCharacteristicUUID = gattCharacteristic.getUuid();
                if (gattCharacteristicUUID.equals(BleManager.this.BEAM_NAME_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_VOLUME_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_LED_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_VERSION_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_IP_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_MAC_CHARACTERISTIC)) {
                    count++;
                } else if (gattCharacteristicUUID.equals(BleManager.this.BEAM_MOUSE_CHARACTERISTIC)) {
                    count++;
                }
            }
            if (count == 7) {
                Log.d(BleManager.TAG, "Do a full scan " + BleManager.this.mScanForAllDevices);
                BleManager.this.onBeamDiscovered(service, gatt);
            }
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != 0) {
                Log.e(BleManager.TAG, "Read of " + characteristic.getUuid() + " failed ");
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_NAME_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam name " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.title = characteristic.getStringValue(0);
                BleManager.this.dequeueCommand();
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_VOLUME_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam volume level " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.volume = Integer.parseInt(characteristic.getStringValue(0));
                BleManager.this.dequeueCommand();
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_LED_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam led level " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.led = Integer.parseInt(characteristic.getStringValue(0));
                BleManager.this.dequeueCommand();
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_VERSION_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam version " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.version = Integer.parseInt(characteristic.getStringValue(0));
                BleManager.this.dequeueCommand();
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_IP_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam ip " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.ip = characteristic.getStringValue(0);
                BleManager.this.dequeueCommand();
            } else if (characteristic.getUuid().equals(BleManager.this.BEAM_MAC_CHARACTERISTIC)) {
                Log.d(BleManager.TAG, "Beam mac " + characteristic.getStringValue(0));
                BleManager.this.mTempBeam.mac = characteristic.getStringValue(0);
                BleManager.this.mTempBeam.connectionType = BeamConnectionType.CONNECTION_TYPE_BLUETOOTH;
                BleManager.this.dequeueCommand();
                BleManager.this.onBeamInfoRead();
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0 && characteristic.getUuid().equals(BleManager.this.BEAM_MOUSE_CHARACTERISTIC)) {
                BleManager.this.dequeueCommand();
            }
        }
    };
    Executor mCommandExecutor = Executors.newSingleThreadExecutor();
    Semaphore mCommandLock = new Semaphore(1, true);
    final LinkedList<BluetoothCommand> mCommandQueue = new LinkedList<>();
    private BluetoothGatt mGatt;
    private ArrayList<BluetoothGatt> mGattList = new ArrayList<>();
    private BluetoothGattCharacteristic mMouseCharacteristic;
    private boolean mScanForAllDevices;
    private boolean mSwitchingToNewBeam;
    private BeamBulb mTempBeam;
    private BluetoothGatt mTempGatt;
    private ArrayList<BluetoothGatt> mTempGattList = new ArrayList<>();
    private BeamBulb mToSwitchBeam;
    private ReleaseTimerTask releaseTimer;

    public interface IBleInterface {
        void enableBle();

        void onScanningCompleted(String str, List<BeamBulb> list);

        void onSwitchingCompleted(String str);

        void supportsBle(boolean z);
    }

    public static BleManager getInstance() {
        return manager;
    }

    public static void init(Context context) {
        Log.d(TAG, "init");
        mContext = context;
        mBleInterface = (IBleInterface) context;
        if (manager == null) {
            manager = new BleManager();
        }
    }

    private BleManager() {
        CURRENT_BLUETOOTH_STATE = STATE_IDLE;
        enableBLE();
    }

    @SuppressLint("MissingPermission")
    private void enableBLE() {
        if (!mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Log.d(TAG, "Device doesnt support BLE");
            mBleInterface.supportsBle(false);
            return;
        }
        Log.d(TAG, "Supports BLE");
        mBleInterface.supportsBle(true);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled");
            mBleInterface.enableBle();
            CURRENT_BLUETOOTH_STATE = 11;
            return;
        }
        Log.d(TAG, "Bluetooth enabled");
        CURRENT_BLUETOOTH_STATE = 10;
    }

    public void setBluetoothState(int state) {
        CURRENT_BLUETOOTH_STATE = state;
    }

    @SuppressLint("MissingPermission")
    public void scanLeDevices(boolean scanForAllDevices) {
        Log.d(TAG, "startScan ");
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Log.d(TAG, "BLE not turned on");
            CURRENT_BLUETOOTH_STATE = 11;
            mBleInterface.onScanningCompleted("Bluetooth turned off", null);
            return;
        }
        CURRENT_BLUETOOTH_STATE = 12;
        closeConnection(this.mGatt);
        reset();
        stopBleTimer();
        this.mScanForAllDevices = scanForAllDevices;
        startBleScanningTimer();
        isScanningStopped = false;
        if (Build.VERSION.SDK_INT >= 21) {
            Log.d(TAG, "startScan >= 21");
            BleL21Scanner.getScanner(this).startScan();
            return;
        }
        Log.d(TAG, "startScan < 21");
        BleScanner.getScanner(this).startScan();
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        Log.d(TAG, "stop scan");
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled");
        } else if (Build.VERSION.SDK_INT >= 21) {
            Log.d(TAG, "stopScan >= 21");
            BleL21Scanner.getScanner(this).stopScan();
        } else {
            Log.d(TAG, "stopScan < 21");
            BleScanner.getScanner(this).stopScan();
        }
    }

    @Override // com.studiodiip.bulbbeam.mousecontroller.ble.IBleScanner
    public void onDeviceDiscovered(BluetoothDevice device) {
        if (checkBleDevice(device)) {
            connectToDevice(device);
        }
    }

    @SuppressLint("MissingPermission")
    private boolean checkBleDevice(BluetoothDevice device) {
        Iterator<BluetoothDevice> it = this.mBleDeviceTempList.iterator();
        while (it.hasNext()) {
            if (device.getAddress().equals(it.next().getAddress())) {
                return false;
            }
        }
        Log.d(TAG, "Adding device " + device.getName() + " address " + device.getAddress());
        this.mBleDeviceTempList.add(device);
        return true;
    }

    private void connectToDevice(BluetoothDevice device) {
        this.mTempGattList.add(device.connectGatt(mContext, false, this.mBluetoothGattCallBack));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void beamDisconnected() {
        Log.d(TAG, "beamDisconnected");
        BeamSettings.getInstance().setSelectedBeam(null);
        BeamSettings.getInstance().setLastUsedBeam(null);
        Intent i = new Intent(mContext, SplashActivity.class);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBeamDiscovered(BluetoothGattService service, BluetoothGatt gatt) {
        Log.d(TAG, "onBeamDiscovered " + gatt.getDevice().getAddress());
        if (this.mSwitchingToNewBeam) {
            this.mGatt = gatt;
            mCurrentBeam = this.mToSwitchBeam;
            setMouseCharacteristic(this.mGatt);
            switchToNewBeamCompleted(null);
            return;
        }
        this.mGattList.add(gatt);
        if (!this.mScanForAllDevices && this.mTempGatt == null) {
            Log.d(TAG, "Read beam data ");
            this.mTempGatt = gatt;
            stopScan();
            readBeamData(this.mTempGatt.getService(this.BEAM_SERVICE_UUID));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readBeamData(BluetoothGattService service) {
        Log.d(TAG, "readBeamData ");
        this.mTempBeam = new BeamBulb();
        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
            if (c.getUuid().equals(this.BEAM_NAME_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam name");
                readCharacteristic(c, this.mTempGatt);
            } else if (c.getUuid().equals(this.BEAM_VOLUME_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam volume");
                readCharacteristic(c, this.mTempGatt);
            } else if (c.getUuid().equals(this.BEAM_LED_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam led");
                readCharacteristic(c, this.mTempGatt);
            } else if (c.getUuid().equals(this.BEAM_VERSION_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam version");
                readCharacteristic(c, this.mTempGatt);
            } else if (c.getUuid().equals(this.BEAM_IP_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam ip");
                readCharacteristic(c, this.mTempGatt);
            } else if (c.getUuid().equals(this.BEAM_MAC_CHARACTERISTIC)) {
                Log.d(TAG, "Read beam mac");
                readCharacteristic(c, this.mTempGatt);
            }
        }
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        queueCommand(new BluetoothCommand(characteristic, BluetoothCommandType.READ_CHARACTERISTIC), gatt);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBeamInfoRead() {
        Log.d(TAG, "onBeamInfoRead ");
        this.mBeamList.add(this.mTempBeam);
        if (this.mScanForAllDevices) {
            this.beamIndex++;
            if (this.beamIndex < this.mGattList.size()) {
                this.mTempGatt = this.mGattList.get(this.beamIndex);
                readBeamData(this.mTempGatt.getService(this.BEAM_SERVICE_UUID));
                return;
            }
            onBeamConnected();
        } else if (isScanningStopped) {
            Log.d(TAG, "isScanningStopped ");
            closeAllConnections(true);
        } else {
            onBeamConnected();
            stopBleScanningTimer();
        }
    }

    private void onBeamConnected() {
        Log.d(TAG, "onBeamConnected ");
        stopBleReadTimer();
        mBleInterface.onScanningCompleted(null, this.mBeamList);
        CURRENT_BLUETOOTH_STATE = STATE_BEAM_FOUND;
    }

    private void setMouseCharacteristic(BluetoothGatt gatt) {
        Log.d(TAG, "setMouseCharacteristic ");
        for (BluetoothGattCharacteristic c : gatt.getService(this.BEAM_SERVICE_UUID).getCharacteristics()) {
            if (c.getUuid().equals(this.BEAM_MOUSE_CHARACTERISTIC)) {
                this.mMouseCharacteristic = c;
                return;
            }
        }
        this.mMouseCharacteristic = null;
    }

    public boolean setUpCurrentBeam(BeamBulb bulb) {
        boolean isBeamFound;
        Log.d(TAG, "setUpCurrentBeam ");
        if (bulb != null) {
            Log.d(TAG, "setUpCurrentBeam " + bulb.title);
            if (this.mBeamList.size() == 0 || this.mGattList.size() == 0) {
                return false;
            }
            for (int index = 0; index < this.mBeamList.size(); index++) {
                BeamBulb b = this.mBeamList.get(index);
                if (b.mac.isEmpty() && b.title.equals(bulb.title)) {
                    Log.d(TAG, "beam found");
                    isBeamFound = true;
                } else if (b.mac.isEmpty() || !b.mac.equals(bulb.mac)) {
                    isBeamFound = false;
                } else {
                    Log.d(TAG, " beam found");
                    isBeamFound = true;
                }
                if (isBeamFound) {
                    this.mGatt = this.mGattList.get(index);
                    mCurrentBeam = this.mBeamList.get(index);
                    setMouseCharacteristic(this.mGatt);
                    CURRENT_BLUETOOTH_STATE = STATE_BEAM_FOUND;
                    closeAllConnections(false);
                    startBleTimer();
                    return true;
                }
            }
            return false;
        }
        this.mGatt = null;
        mCurrentBeam = null;
        this.mMouseCharacteristic = null;
        closeAllConnections(true);
        stopBleTimer();
        return true;
    }

    public void sendCommand(String commandStr) {
        int packetLength;
        if (this.mGatt == null || this.mMouseCharacteristic == null) {
            Log.e(TAG, "Invalid characteristic");
            return;
        }
        int length = commandStr.length();
        if (length > 20) {
            commandStr = "BEAMSPS" + commandStr + "BEAMEPS";
            length = commandStr.length();
        }
        int toSend = length;
        int sent = 0;
        while (toSend > 0) {
            if (toSend > 20) {
                packetLength = 20;
            } else {
                packetLength = toSend;
            }
            StringBuilder data = new StringBuilder(packetLength);
            if (packetLength + sent == length) {
                data.append(commandStr.substring(sent));
            } else {
                data.append(commandStr.substring(sent, sent + packetLength));
            }
            BluetoothCommand bluetoothCommand = new BluetoothCommand(this.mMouseCharacteristic, BluetoothCommandType.WRITE_CHARACTERISTIC);
            this.mMouseCharacteristic.setValue(data.toString());
            queueCommand(bluetoothCommand, this.mGatt);
            toSend -= packetLength;
            sent += packetLength;
            sleep(20);
        }
    }

    public void switchToBeam(BeamBulb beam) {
        if (beam == null) {
            Log.d(TAG, "the beam to switch is null");
            switchToNewBeamCompleted(mContext.getString(R.string.beam_disconnected));
            return;
        }
        Log.d(TAG, "switchToBeam " + beam.title);
        this.mSwitchingToNewBeam = true;
        this.mScanForAllDevices = false;
        this.mToSwitchBeam = null;
        stopBleTimer();
        int index = 0;
        while (true) {
            if (index >= this.mBeamList.size()) {
                break;
            }
            BeamBulb b = this.mBeamList.get(index);
            if (b.mac.isEmpty() && b.title.equals(beam.title)) {
                Log.d(TAG, "Switch beam found");
                this.mToSwitchBeam = b;
            } else if (b.mac.isEmpty() || !b.mac.equals(beam.mac)) {
                this.mToSwitchBeam = null;
            } else {
                Log.d(TAG, "Switch beam found");
                this.mToSwitchBeam = b;
            }
            if (this.mToSwitchBeam != null && !b.mac.isEmpty()) {
                connectToDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(b.mac));
                startBleReadTimer();
                break;
            }
            index++;
        }
        if (index == this.mBeamList.size()) {
            Log.d(TAG, "No beams found for switching");
            switchToNewBeamCompleted(mContext.getString(R.string.beam_disconnected));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchToNewBeamCompleted(String errMessage) {
        Log.d(TAG, "switchToNewBeamCompleted " + errMessage);
        stopBleReadTimer();
        this.mSwitchingToNewBeam = false;
        if (errMessage != null) {
            CURRENT_BLUETOOTH_STATE = STATE_NO_BEAM;
            mBleInterface.onSwitchingCompleted(errMessage);
            stopBleTimer();
            return;
        }
        CURRENT_BLUETOOTH_STATE = STATE_BEAM_FOUND;
        mBleInterface.onSwitchingCompleted(null);
        startBleTimer();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeAllConnections(boolean shouldDisconnectCurrent) {
        Log.d(TAG, "closeAllConnections");
        if (this.mGattList.size() == 0) {
            Log.d(TAG, "No BLE connections");
            return;
        }
        Iterator<BluetoothGatt> it = this.mGattList.iterator();
        while (it.hasNext()) {
            BluetoothGatt gatt = it.next();
            if (shouldDisconnectCurrent || gatt != this.mGatt) {
                closeConnection(gatt);
            }
        }
        if (this.mTempGattList.size() > 0) {
            Iterator<BluetoothGatt> it2 = this.mTempGattList.iterator();
            while (it2.hasNext()) {
                BluetoothGatt gatt2 = it2.next();
                if ((shouldDisconnectCurrent || gatt2 != this.mGatt) && !this.mGattList.contains(gatt2)) {
                    closeConnection(gatt2);
                }
            }
        }
    }

    public void closeCurrentConnection() {
        Log.d(TAG, "closeCurrentConnection");
        if (this.mGatt != null) {
            closeConnection(this.mGatt);
            stopBleTimer();
        }
    }

    private void closeConnection(BluetoothGatt gatt) {
        if (gatt != null) {
            try {
                Log.d(TAG, "closeConnection " + gatt.getDevice().getAddress());
                gatt.close();
                gatt.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void reset() {
        this.mSwitchingToNewBeam = false;
        this.mBleDeviceTempList.clear();
        this.mTempGattList.clear();
        this.mGattList.clear();
        this.mBeamList.clear();
        this.mGatt = null;
        this.mTempGatt = null;
        this.mMouseCharacteristic = null;
        this.beamIndex = 0;
        this.mCommandQueue.clear();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public class CommandHandlerRunnable implements Runnable {
        BluetoothCommand mCommand = null;
        BluetoothGatt mGatt = null;

        public CommandHandlerRunnable(BluetoothCommand command, BluetoothGatt gatt) {
            this.mCommand = command;
            this.mGatt = gatt;
        }

        public void run() {
            BleManager.this.startReleaseTimerTask();
            BleManager.this.mCommandLock.acquireUninterruptibly();
            this.mCommand.execute(this.mGatt);
        }
    }

    private void queueCommand(BluetoothCommand command, BluetoothGatt gatt) {
        synchronized (this.mCommandQueue) {
            this.mCommandQueue.add(command);
            this.mCommandExecutor.execute(new CommandHandlerRunnable(command, gatt));
        }
    }

    /* access modifiers changed from: protected */
    public void dequeueCommand() {
        if (this.mCommandQueue.size() != 0) {
            this.mCommandQueue.pop();
            this.mCommandLock.release();
            stopReleaseTimerTask();
        }
    }

    private void startBleScanningTimer() {
        Log.d(TAG, "startBleScanningTimer");
        stopBleScanningTimer();
        this.bleScanningTimerTask = new BleScanningTimerTask();
        new Timer().schedule(this.bleScanningTimerTask, 20000);
    }

    private void stopBleScanningTimer() {
        Log.d(TAG, "stopBleScanningTimer");
        if (this.bleScanningTimerTask != null) {
            this.bleScanningTimerTask.cancel();
            this.bleScanningTimerTask = null;
            Log.d(TAG, "stopBleScanningTimer done");
        }
    }

    /* access modifiers changed from: private */
    public class BleScanningTimerTask extends TimerTask {
        private BleScanningTimerTask() {
        }

        public void run() {
            Log.d(BleManager.TAG, "BleScanningTimerTask ");
            if (BleManager.isScanningStopped) {
                Log.d(BleManager.TAG, "isScanningStopped ");
                BleManager.this.closeAllConnections(true);
                return;
            }
            BleManager.this.stopScan();
            if (BleManager.this.mGattList.size() == 0) {
                BleManager.CURRENT_BLUETOOTH_STATE = BleManager.STATE_NO_BEAM;
                BleManager.mBleInterface.onScanningCompleted("No Beams found. Please try again", null);
                return;
            }
            BleManager.this.beamIndex = 0;
            BleManager.this.mTempGatt = (BluetoothGatt) BleManager.this.mGattList.get(BleManager.this.beamIndex);
            BleManager.this.startBleReadTimer();
            BleManager.this.readBeamData(BleManager.this.mTempGatt.getService(BleManager.this.BEAM_SERVICE_UUID));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startBleReadTimer() {
        Log.d(TAG, "startBleReadTimer");
        stopBleReadTimer();
        this.bleReadTimeOutTask = new BleReadTimeOutTask();
        new Timer().schedule(this.bleReadTimeOutTask, 20000);
    }

    private void stopBleReadTimer() {
        Log.d(TAG, "stopBleReadTimer");
        if (this.bleReadTimeOutTask != null) {
            this.bleReadTimeOutTask.cancel();
            this.bleReadTimeOutTask = null;
            Log.d(TAG, "stopBleReadTimer done");
        }
    }

    /* access modifiers changed from: private */
    public class BleReadTimeOutTask extends TimerTask {
        private BleReadTimeOutTask() {
        }

        public void run() {
            Log.d(BleManager.TAG, "BleReadTimeOutTask ");
            BleManager.CURRENT_BLUETOOTH_STATE = BleManager.STATE_NO_BEAM;
            BleManager.this.closeAllConnections(true);
            if (BleManager.this.mSwitchingToNewBeam) {
                BleManager.this.switchToNewBeamCompleted(BleManager.mContext.getString(R.string.beam_disconnected));
            } else {
                BleManager.mBleInterface.onScanningCompleted("No Beams found. Please try again", null);
            }
        }
    }

    private void startBleTimer() {
        Log.d(TAG, "startBleTimer");
        stopBleTimer();
        this.bleTimer = new BleTimerTask();
        new Timer().scheduleAtFixedRate(this.bleTimer, 0, 120000);
    }

    private void stopBleTimer() {
        Log.d(TAG, "stopBleTimer");
        if (this.bleTimer != null) {
            this.bleTimer.cancel();
            this.bleTimer = null;
            Log.d(TAG, "stopBleTimer done");
        }
    }

    /* access modifiers changed from: private */
    public class BleTimerTask extends TimerTask {
        private BleTimerTask() {
        }

        public void run() {
            Log.d(BleManager.TAG, "BleTimerTask ");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startReleaseTimerTask() {
        stopReleaseTimerTask();
        this.releaseTimer = new ReleaseTimerTask();
        new Timer().schedule(this.releaseTimer, 400);
    }

    private void stopReleaseTimerTask() {
        if (this.releaseTimer != null) {
            this.releaseTimer.cancel();
            this.releaseTimer = null;
        }
    }

    /* access modifiers changed from: private */
    public class ReleaseTimerTask extends TimerTask {
        private ReleaseTimerTask() {
        }

        public void run() {
            Log.d(BleManager.TAG, "ReleaseTimerTask ");
            Log.d(BleManager.TAG, "Lock released ");
            BleManager.this.mCommandLock.release();
        }
    }
}
