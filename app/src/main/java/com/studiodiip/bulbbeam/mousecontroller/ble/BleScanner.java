package com.studiodiip.bulbbeam.mousecontroller.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BleScanner {
    private static BleScanner scanner;
    private IBleScanner mCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        /* class com.studiodiip.bulbbeam.mousecontroller.ble.BleScanner.AnonymousClass1 */

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            BleScanner.this.mCallback.onDeviceDiscovered(device);
        }
    };

    public static BleScanner getScanner(IBleScanner callback) {
        if (scanner == null) {
            scanner = new BleScanner(callback);
        }
        return scanner;
    }

    BleScanner(IBleScanner callback) {
        this.mCallback = callback;
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        BluetoothAdapter.getDefaultAdapter().startLeScan(this.mLeScanCallback);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (this.mLeScanCallback != null) {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(this.mLeScanCallback);
        }
    }
}
