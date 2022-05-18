package com.studiodiip.bulbbeam.mousecontroller.ble;

import android.bluetooth.BluetoothDevice;

public interface IBleScanner {
    void onDeviceDiscovered(BluetoothDevice bluetoothDevice);
}
