package com.studiodiip.bulbbeam.mousecontroller.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

public class BluetoothCommand {
    private static final UUID BEAM_NOTIFICATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BleManager";
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothCommandType mType;

    public BluetoothCommand(BluetoothGattCharacteristic characteristic, BluetoothCommandType type) {
        this.mCharacteristic = characteristic;
        this.mType = type;
    }

    public void execute(BluetoothGatt gatt) {
        if (this.mType == BluetoothCommandType.READ_CHARACTERISTIC) {
            gatt.readCharacteristic(this.mCharacteristic);
        } else if (this.mType == BluetoothCommandType.WRITE_CHARACTERISTIC) {
            gatt.writeCharacteristic(this.mCharacteristic);
        } else if (this.mType == BluetoothCommandType.NOTIFY_CHARACTERISTIC) {
            gatt.setCharacteristicNotification(this.mCharacteristic, true);
            BluetoothGattDescriptor descriptor = this.mCharacteristic.getDescriptor(BEAM_NOTIFICATION_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }
}
