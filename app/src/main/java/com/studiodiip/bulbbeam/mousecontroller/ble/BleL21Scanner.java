package com.studiodiip.bulbbeam.mousecontroller.ble;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class BleL21Scanner {
    private static BleL21Scanner scanner;
    private List<ScanFilter> filters;
    private IBleScanner mCallback;
    private BluetoothLeScanner mLEScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        /* class com.studiodiip.bulbbeam.mousecontroller.ble.BleL21Scanner.AnonymousClass1 */

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            BleL21Scanner.this.mCallback.onDeviceDiscovered(device);
        }
    };
    private ScanCallback mScanCallback = new ScanCallback() {
        /* class com.studiodiip.bulbbeam.mousecontroller.ble.BleL21Scanner.AnonymousClass2 */

        public void onScanResult(int callbackType, ScanResult result) {
            BleL21Scanner.this.mCallback.onDeviceDiscovered(result.getDevice());
        }
    };
    private ScanSettings scanSettings;

    public static BleL21Scanner getScanner(IBleScanner callback) {
        if (scanner == null) {
            scanner = new BleL21Scanner(callback);
        }
        return scanner;
    }

    private BleL21Scanner(IBleScanner callback) {
        this.mCallback = callback;
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        this.mLEScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        this.scanSettings = new ScanSettings.Builder().setScanMode(2).build();
        this.filters = new ArrayList();
        this.mLEScanner.startScan(this.filters, this.scanSettings, this.mScanCallback);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (this.mLEScanner != null) {
            this.mLEScanner.stopScan(this.mScanCallback);
        }
    }
}
