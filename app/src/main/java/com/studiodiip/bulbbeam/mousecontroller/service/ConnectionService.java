package com.studiodiip.bulbbeam.mousecontroller.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.activity.SplashActivity;
import com.studiodiip.bulbbeam.mousecontroller.ble.BleManager;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;
import com.studiodiip.bulbbeam.mousecontroller.util.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionService extends Service {
    private static final String ACTION_KEEP_ALIVE = "keepAlive";
    public static final String BLUETOOTH_MAC_ADDRESS = "bluetoothMacaddress";
    public static final String IP = "ip";
    public static final String IS_BLUETOOTH = "isBluetooth";
    public static final String LOCAL_MAC = "mac";
    public static final String PORT = "port";
    private static final String TAG = ConnectionService.class.getSimpleName();
    public static final String USER_NAME = "user";
    private static final int WRITE_TIMEOUT = 15000;
    private static volatile BlockingQueue<String> commQueue;
    private static volatile BlockingQueue<String> dummyQueue;
    private static volatile boolean isBluetooth;
    private final BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService.AnonymousClass5 */
        private final String TAG = "alarmReceiver";

        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ConnectionService.ACTION_KEEP_ALIVE)) {
                return;
            }
            if (ConnectionService.this.mDummyWriter != null) {
                Log.d("alarmReceiver", "active");
                ConnectionService.dummyQueue.add("ka");
                return;
            }
            Log.d("alarmReceiver", "pw == null");
        }
    };
    private CommHandlerThread commHandler;
    private Thread commHandlerThread;
    private Thread connectionThread;
    private DummyCommHandler dummyCommHandler;
    private Thread dummyCommHandlerThread;
    private Socket dummySocket;
    private boolean imageSocketIsConnecting = false;
    private Socket imgSocket;
    private boolean isRegistered = false;
    private IBinder mBinder = new ConnectionBinder();
    private PrintWriter mDummyWriter;
    private PrintWriter mImageWriter;
    private String mLocalMac;
    private PrintWriter mPrintWriter;
    private String mServerIp;
    private int mServerPort;
    private String mUserName;
    private boolean socketIsConnecting = false;
    private Socket wifiSocket;
    private Timer writeTimer = new Timer();
    private WriteTimerTask writerTask;

    @SuppressLint("WrongConstant")
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        registerReceiver(this.alarmReceiver, new IntentFilter(ACTION_KEEP_ALIVE));
        this.isRegistered = true;
        initializeData(intent);
        new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService.AnonymousClass1 */

            public void run() {
                if (!ConnectionService.isBluetooth) {
                    ConnectionService.this.connectDummySocket();
                    BlockingQueue unused = ConnectionService.dummyQueue = new ArrayBlockingQueue(300);
                    ConnectionService.this.dummyCommHandler = new DummyCommHandler();
                    ConnectionService.this.dummyCommHandlerThread = new Thread(ConnectionService.this.dummyCommHandler);
                    ConnectionService.this.dummyCommHandlerThread.start();
                }
            }
        }.start();
        stopConnectionThread();
        this.connectionThread = new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService.AnonymousClass2 */

            public void run() {
                ConnectionService.this.connectToSocket();
            }
        };
        this.connectionThread.start();
        commQueue = new ArrayBlockingQueue(300);
        this.commHandler = new CommHandlerThread();
        this.commHandlerThread = new Thread(this.commHandler);
        this.commHandlerThread.start();
        return 2;
    }

    public boolean onUnbind(Intent intent) {
        cleanUp();
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
        cleanUp();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanUp() {
        Log.d(TAG, "cleanup " + this.isRegistered);
        stopConnectionThread();
        if (this.commHandler != null) {
            this.commHandler.stopRunning();
        }
        if (this.dummyCommHandler != null) {
            this.dummyCommHandler.stopRunning();
            if (this.mDummyWriter != null) {
                this.mDummyWriter.close();
            }
        }
        if (this.mPrintWriter != null) {
            this.mPrintWriter.close();
            this.mPrintWriter = null;
        }
        close(this.wifiSocket);
        closeImageSocket();
        stopConnectionThread();
        if (this.isRegistered) {
            this.isRegistered = false;
            unregisterReceiver(this.alarmReceiver);
        }
        isBluetooth = false;
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    private void stopConnectionThread() {
        Log.d(TAG, "stopConnectionThread");
        try {
            if (this.connectionThread != null) {
                this.connectionThread.interrupt();
                this.connectionThread.join(1000);
                this.connectionThread = null;
                Log.d(TAG, "Connection thread joined");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initializeData(Intent intent) {
        String str;
        String str2;
        String str3;
        boolean z;
        if (intent == null) {
            Log.e(TAG, "Intent null");
            return;
        }
        if (intent.hasExtra(IP)) {
            str = intent.getStringExtra(IP);
        } else {
            str = null;
        }
        this.mServerIp = str;
        this.mServerPort = intent.getIntExtra(PORT, 13456);
        if (intent.hasExtra(USER_NAME)) {
            str2 = intent.getStringExtra(USER_NAME);
        } else {
            str2 = null;
        }
        this.mUserName = str2;
        if (intent.hasExtra(LOCAL_MAC)) {
            str3 = intent.getStringExtra(LOCAL_MAC);
        } else {
            str3 = null;
        }
        this.mLocalMac = str3;
        if (intent.hasExtra(BLUETOOTH_MAC_ADDRESS)) {
            intent.getStringExtra(BLUETOOTH_MAC_ADDRESS);
        }
        if (intent.hasExtra(IS_BLUETOOTH)) {
            z = intent.getBooleanExtra(IS_BLUETOOTH, false);
        } else {
            z = false;
        }
        isBluetooth = z;
        if (!isBluetooth) {
            Log.d(TAG, "Connected by wifi");
        } else {
            Log.d(TAG, "Connected by bluetooth");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @SuppressLint("WrongConstant")
    private void connectDummySocket() {
        if (this.dummySocket == null || !this.dummySocket.isConnected() || this.dummySocket.isClosed()) {
            try {
                InetAddress serverAddr = InetAddress.getByName(this.mServerIp);
                if (serverAddr.isReachable(1000)) {
                    ((AlarmManager) getSystemService("alarm")).setInexactRepeating(2, 600000, 600000, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_KEEP_ALIVE), 0));
                    this.wifiSocket = new Socket(serverAddr, this.mServerPort);
                    this.mDummyWriter = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(this.wifiSocket.getOutputStream())), true);
                    return;
                }
                Log.e("SOCKET", "SOCKET IS NOT REACHABLE!!");
                this.socketIsConnecting = false;
            } catch (UnknownHostException e1) {
                this.socketIsConnecting = false;
                e1.printStackTrace();
            } catch (IOException e12) {
                this.socketIsConnecting = false;
                e12.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToSocket() {
        if (!isBluetooth) {
            try {
                connectToWifiSocket();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            connectToImageSocket();
        }
    }

    @SuppressLint("WrongConstant")
    private void connectToWifiSocket() throws Throwable {
        Log.d(TAG, "connectToWifiSocket ");
        if (this.socketIsConnecting) {
            Log.e(TAG, "wifi socket already connecting");
            return;
        }
        try {
            this.socketIsConnecting = true;
            close(this.wifiSocket);
            this.wifiSocket = connectToIp(this.mServerIp);
            if (this.wifiSocket == null) {
                Log.d(TAG, "WiFi socket is not reachable");
                this.socketIsConnecting = false;
                Intent i = new Intent(this, SplashActivity.class);
                i.addFlags(268435456);
                startActivity(i);
                return;
            }
            Utils.readBeamInfo(this.wifiSocket.getInputStream(), BeamConnectionType.CONNECTION_TYPE_WIFI, "", "");
            this.mPrintWriter = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(this.wifiSocket.getOutputStream())), true);
            sendSocket(USER_NAME, this.mUserName + ";" + this.mLocalMac);
            this.socketIsConnecting = false;
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void closeImageSocket() {
        Log.d(TAG, "closeImageSocket");
        if (this.mImageWriter != null) {
            this.mImageWriter.close();
            this.mImageWriter = null;
        }
        close(this.imgSocket);
    }

    public boolean isConnectedToImageSocket() {
        return isConnectedToSocket(this.imgSocket);
    }

    public void connectToImageSocket() {
        new Thread(new Runnable() {
            /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService.AnonymousClass3 */

            public void run() {
                Log.d(ConnectionService.TAG, "connectToImageSocket ");
                if (!ConnectionService.isBluetooth) {
                    Log.d(ConnectionService.TAG, "Connected by wifi");
                } else if (ConnectionService.this.imageSocketIsConnecting) {
                    Log.e(ConnectionService.TAG, "image socket already connecting");
                } else {
                    try {
                        ConnectionService.this.imageSocketIsConnecting = true;
                        ConnectionService.this.close(ConnectionService.this.imgSocket);
                        ConnectionService.this.imgSocket = ConnectionService.this.connectToIp(ConnectionService.this.mServerIp);
                        if (ConnectionService.this.imgSocket == null) {
                            Log.d(ConnectionService.TAG, "Image socket is not reachable");
                            ConnectionService.this.imageSocketIsConnecting = false;
                            return;
                        }
                        Log.d(ConnectionService.TAG, "Image socket connected");
                        ConnectionService.this.mImageWriter = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(ConnectionService.this.imgSocket.getOutputStream())), true);
                        ConnectionService.this.imageSocketIsConnecting = false;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Socket connectToIp(String ipAddress) {
        try {
            InetAddress serverAddress = InetAddress.getByName(ipAddress);
            if (!serverAddress.isReachable(5000)) {
                Log.d(TAG, "socket is not reachable");
                return null;
            }
            Log.d(TAG, "socket connected");
            return new Socket(serverAddress, this.mServerPort);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "wifi socket close failed", e);
            }
        }
    }

    public void sendSocket(String x, String y) {
        try {
            sendSocket(x, y, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendSocket(String x, String y, String z) throws InterruptedException {
        if (z != null) {
            try {
                commQueue.put(x + ";" + y + ";" + z);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            commQueue.put(x + ";" + y);
        }
    }

    private void startWriteTimerTask() {
        stopWriteTimerTask();
        this.writerTask = new WriteTimerTask();
        this.writeTimer.schedule(this.writerTask, 15000);
    }

    /* access modifiers changed from: private */
    public class WriteTimerTask extends TimerTask {
        private WriteTimerTask() {
        }

        @SuppressLint("WrongConstant")
        public void run() {
            Log.d(ConnectionService.TAG, "Write timed out!!!");
            ConnectionService.this.mPrintWriter = null;
            ConnectionService.this.stopWriteTimerTask();
            ConnectionService.this.cleanUp();
            BeamSettings.getInstance().setSelectedBeam(null);
            BeamSettings.getInstance().setLastUsedBeam(null);
            Intent i = new Intent(ConnectionService.this, SplashActivity.class);
            i.addFlags(268435456);
            ConnectionService.this.startActivity(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopWriteTimerTask() {
        if (this.writerTask != null) {
            this.writerTask.cancel();
        }
    }

    /* access modifiers changed from: package-private */
    public class CommHandlerThread implements Runnable {
        String msg = null;
        List<String> msgToExecute = new ArrayList();
        private volatile boolean running = true;
        List<String> totalMessages = new ArrayList();

        CommHandlerThread() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopRunning() {
            this.running = false;
        }

        public void run() {
            while (this.running) {
                this.msgToExecute.clear();
                this.totalMessages.clear();
                if (!ConnectionService.commQueue.isEmpty()) {
                    ConnectionService.commQueue.drainTo(this.totalMessages);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (this.totalMessages.size() != 0) {
                    String lastImg = null;
                    String lastImgTrans = null;
                    for (int m = 0; m < this.totalMessages.size(); m++) {
                        if (this.totalMessages.get(m).contains("img")) {
                            lastImg = this.totalMessages.get(m);
                        } else if (this.totalMessages.get(m).contains("trans")) {
                            lastImgTrans = this.totalMessages.get(m);
                        } else {
                            this.msgToExecute.add(this.totalMessages.get(m));
                        }
                    }
                    if (lastImg != null) {
                        this.msgToExecute.add(lastImg);
                    }
                    if (lastImgTrans != null) {
                        this.msgToExecute.add(lastImgTrans);
                    }
                    if (this.msgToExecute.size() == 0) {
                        this.msgToExecute.add(this.totalMessages.get(this.totalMessages.size() - 1));
                    }
                    for (int m2 = 0; m2 < this.msgToExecute.size(); m2++) {
                        this.msg = this.msgToExecute.get(m2);
                        ConnectionService.this.executeSendKey(this.msg);
                    }
                    if (lastImg != null) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
            Log.d(ConnectionService.TAG, "thread stopped " + this.running);
        }
    }

    private boolean isConnectedToSocket(Socket socket) {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return false;
        }
        return true;
    }

    private void sendImage(String command) {
        Log.d(TAG, "sendImage ");
        while (this.imageSocketIsConnecting) {
            Log.d(TAG, "Waiting for socket connection");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isConnectedToSocket(this.imgSocket)) {
            Log.d(TAG, "Not connected to socket. Connect again");
            return;
        }
        this.imageSocketIsConnecting = false;
        if (this.mImageWriter == null || this.mImageWriter.checkError()) {
            Log.e(TAG, "mImageWriter is null");
            this.imgSocket = null;
            return;
        }
        this.mImageWriter.println(command);
        this.mImageWriter.flush();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @SuppressLint("WrongConstant")
    private void executeSendKey(String command) {
        if (!isBluetooth) {
            while (this.socketIsConnecting) {
                Log.d(TAG, "Waiting for socket connection");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isConnectedToSocket(this.wifiSocket)) {
                Log.d(TAG, "Not connected to socket. Connect again");
                connectToSocket();
                executeSendKey(command);
            }
            this.socketIsConnecting = false;
            if (this.mPrintWriter == null || this.mPrintWriter.checkError()) {
                Log.e(TAG, "mPrintwriter is null");
                BeamSettings.getInstance().setSelectedBeam(null);
                BeamSettings.getInstance().setLastUsedBeam(null);
                Intent i = new Intent(this, SplashActivity.class);
                i.addFlags(268435456);
                startActivity(i);
                return;
            }
            startWriteTimerTask();
            this.mPrintWriter.println(command);
            this.mPrintWriter.flush();
            stopWriteTimerTask();
        } else if (command.startsWith("img") || command.startsWith("trans")) {
            sendImage(command);
        } else {
            BleManager.getInstance().sendCommand(command);
        }
    }

    public void sendKeyToSocket(final String key, final String type) {
        new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService.AnonymousClass4 */

            public void run() {
                if (type.equals("led")) {
                    ConnectionService.this.executeSendKey(key);
                } else {
                    ConnectionService.this.executeSendKey(type + ";" + key + ";0");
                }
            }
        }.start();
    }

    public class ConnectionBinder extends Binder {
        public ConnectionBinder() {
        }

        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    /* access modifiers changed from: package-private */
    public class DummyCommHandler implements Runnable {
        String msg = null;
        List<String> msgToExecute = new ArrayList();
        private volatile boolean running = true;

        DummyCommHandler() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopRunning() {
            this.running = false;
        }

        public void run() {
            while (this.running) {
                this.msgToExecute.clear();
                if (!ConnectionService.dummyQueue.isEmpty()) {
                    ConnectionService.dummyQueue.drainTo(this.msgToExecute);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (this.msgToExecute.size() != 0) {
                    for (int m = 0; m < this.msgToExecute.size(); m++) {
                        if (ConnectionService.this.mDummyWriter != null) {
                            this.msg = this.msgToExecute.get(m);
                            ConnectionService.this.mDummyWriter.println(this.msg);
                        }
                    }
                }
            }
            Log.d(ConnectionService.TAG, "DummyCommHandler thread stopped " + this.running);
        }
    }
}
