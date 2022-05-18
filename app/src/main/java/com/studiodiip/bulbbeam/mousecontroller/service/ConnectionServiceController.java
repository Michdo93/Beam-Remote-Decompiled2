package com.studiodiip.bulbbeam.mousecontroller.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;
import com.studiodiip.bulbbeam.mousecontroller.util.Utils;

public class ConnectionServiceController {
    private static String TAG = ConnectionServiceController.class.getSimpleName();
    private static ConnectionServiceController instance;
    private static Context mContext;
    private Intent connectionServiceIntent = new Intent(mContext, ConnectionService.class);
    private ConnectionService mServiceBinder;
    private boolean mServiceBound = false;
    protected ServiceConnection mServiceConn = new ServiceConnection() {
        /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionServiceController.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionServiceController.this.mServiceBinder = ((ConnectionService.ConnectionBinder) service).getService();
            ConnectionServiceController.this.mServiceBound = true;
            ConnectionServiceController.this.mServiceStarted = true;
            Log.d(ConnectionServiceController.TAG, "onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            ConnectionServiceController.this.mServiceBound = false;
            ConnectionServiceController.this.mServiceStarted = false;
            Log.d(ConnectionServiceController.TAG, "onServiceDisconnected");
        }
    };
    private boolean mServiceStarted = false;

    public static void init(Context contxt) {
        mContext = contxt;
        if (instance == null) {
            instance = new ConnectionServiceController();
        }
    }

    public static ConnectionServiceController getInstance() {
        return instance;
    }

    private ConnectionServiceController() {
    }

    public void stopConnectionService() {
        Log.d(TAG, "stopConnectionService " + this.mServiceStarted);
        mContext.stopService(this.connectionServiceIntent);
    }

    public void unbindConnectionService() {
        Log.d(TAG, "unbindConnectionService " + this.mServiceBound);
        if (this.mServiceBound) {
            mContext.unbindService(this.mServiceConn);
            stopConnectionService();
            this.mServiceBound = false;
            this.mServiceBinder = null;
        }
    }

    public ConnectionService getBinder() {
        return this.mServiceBinder;
    }

    @SuppressLint("WrongConstant")
    public void startConnectionService(String ipAddress, String macAddress, BeamConnectionType connectionType) {
        Log.d(TAG, "startConnectionService " + ipAddress + ", Mac - " + macAddress);
        Log.d(TAG, "Service bound" + this.mServiceBound);
        String localMacAddress = Utils.getMacAddress();
        String userName = Utils.getUserName(mContext);
        this.connectionServiceIntent = new Intent(mContext, ConnectionService.class);
        this.connectionServiceIntent.putExtra(ConnectionService.IP, ipAddress);
        this.connectionServiceIntent.putExtra(ConnectionService.PORT, 13456);
        this.connectionServiceIntent.putExtra(ConnectionService.USER_NAME, userName);
        this.connectionServiceIntent.putExtra(ConnectionService.LOCAL_MAC, localMacAddress);
        this.connectionServiceIntent.putExtra(ConnectionService.BLUETOOTH_MAC_ADDRESS, macAddress);
        this.connectionServiceIntent.putExtra(ConnectionService.IS_BLUETOOTH, connectionType == BeamConnectionType.CONNECTION_TYPE_BLUETOOTH);
        Log.d(TAG, "Connection type " + connectionType);
        if (this.mServiceBound) {
            Log.d(TAG, "Unbinding from connection service");
            unbindConnectionService();
        }
        mContext.startService(this.connectionServiceIntent);
        Context context = mContext;
        Intent intent = this.connectionServiceIntent;
        ServiceConnection serviceConnection = this.mServiceConn;
        Context context2 = mContext;
        context.bindService(intent, serviceConnection, 1);
    }
}
