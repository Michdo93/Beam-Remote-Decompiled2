package com.studiodiip.bulbbeam.mousecontroller.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class BulbDiscoverer {
    public static final String BEAM_CHANGED_BROADCAST_ACTION = "beamchanged";
    public static int STATE_IDLE = 0;
    public static int CURRENT_WIFI_STATE = STATE_IDLE;
    public static final String RECEIVER_ACTION = "beams-discovered";
    public static final String RECEIVER_WIFI_LIST = "wifi_bulbs";
    public static int STATE_BEAM_FOUND = 4;
    public static int STATE_NO_BEAM = 3;
    public static int STATE_WIFI_OFF = 2;
    public static int STATE_WIFI_SCANNING = 1;
    private static final String TAG = BulbDiscoverer.class.getSimpleName();
    public static ArrayList<BeamBulb> beamBulbs = new ArrayList<>();
    private static BulbDiscoverer instance;
    public static ArrayList<BeamBulb> wifiBeamList = new ArrayList<>();
    private Context mContext;
    private ArrayList<Thread> threadList = new ArrayList<>();

    public static void clear() {
        beamBulbs.clear();
        wifiBeamList.clear();
    }

    public static BulbDiscoverer getInstance() {
        return instance;
    }

    public static BulbDiscoverer init(Context context) {
        if (instance == null) {
            instance = new BulbDiscoverer(context);
        }
        return instance;
    }

    private BulbDiscoverer(Context context) {
        this.mContext = context;
    }

    public void setWifiState(int state) {
        CURRENT_WIFI_STATE = state;
    }

    private boolean checkBeam(BeamBulb currentBulb) {
        Iterator<BeamBulb> it = wifiBeamList.iterator();
        while (it.hasNext()) {
            BeamBulb beambulb = it.next();
            Log.d(TAG, "Beams in list" + beambulb.title + ", ip" + beambulb.ip);
            if (!beambulb.ip.isEmpty() && beambulb.ip.equals(currentBulb.ip)) {
                Log.d(TAG, "Beam already in list " + beambulb.title);
                return true;
            }
        }
        return false;
    }

    public static void addWifiBeam(BeamBulb bulb) {
        if (!isAlreadyAdded(bulb)) {
            beamBulbs.add(bulb);
        }
    }

    private static boolean isAlreadyAdded(BeamBulb bulb) {
        Iterator<BeamBulb> it = beamBulbs.iterator();
        while (it.hasNext()) {
            BeamBulb b = it.next();
            if (!b.mac.isEmpty() && b.mac.equals(bulb.mac)) {
                return true;
            }
            if (!b.ip.isEmpty() && b.ip.equals(bulb.ip)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000c  */
    public static BeamBulb isConnectedByWifi(BeamBulb bleBeam) {
        Iterator<BeamBulb> it = beamBulbs.iterator();
        while (it.hasNext()) {
            BeamBulb b = it.next();
            if ((!b.ip.isEmpty() && bleBeam.ip.equals(b.ip)) || b.title.equals(bleBeam.title)) {
                return b;
            }
            while (it.hasNext()) {
            }
        }
        return null;
    }

    public boolean findLastBulb(Context context, final String ip) {
        Log.d(TAG, "FindLastBulb");
        if (CURRENT_WIFI_STATE == STATE_WIFI_SCANNING) {
            return false;
        }
        CURRENT_WIFI_STATE = STATE_WIFI_SCANNING;
        this.threadList.clear();
        Thread thread = new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer.AnonymousClass1 */

            public void run() {
                try {
                    BulbDiscoverer.this.connectToIP(ip, 4000);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };
        this.threadList.add(thread);
        thread.start();
        new WaitTask(this.threadList).execute(new Void[0]);
        return true;
    }

    @SuppressLint("MissingPermission")
    public boolean findBulbs(Context context, boolean forceSearch, final int timeout) {
        Log.d(TAG, "FindBulbs forceSearch " + forceSearch);
        if (!forceSearch && CURRENT_WIFI_STATE > STATE_WIFI_SCANNING) {
            sendWifiBeamListBroadcast(context);
            return false;
        } else if (CURRENT_WIFI_STATE == STATE_WIFI_SCANNING) {
            return false;
        } else {
            CURRENT_WIFI_STATE = STATE_WIFI_SCANNING;
            final String ipRangeBaseString = WifiUtil.getMyIpRangeBaseString(context);
            if (ipRangeBaseString == null) {
                CURRENT_WIFI_STATE = STATE_WIFI_OFF;
                sendWifiBeamListBroadcast(context);
                return false;
            }
            this.threadList.clear();
            if (!((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID().startsWith("\"Beam_") || !ipRangeBaseString.equals("192.168.43.")) {
                for (int i = 0; i < 255; i += 2) {
                    int finalI = i;
                    Thread thread = new Thread() {
                        /* class com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer.AnonymousClass3 */

                        public void run() {
                            for (int thisI = finalI; thisI < finalI + 2; thisI++) {
                                try {
                                    BulbDiscoverer.this.connectToIP(ipRangeBaseString + thisI, timeout);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        }
                    };
                    this.threadList.add(thread);
                    thread.start();
                }
            } else {
                Thread thread2 = new Thread() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer.AnonymousClass2 */

                    public void run() {
                        try {
                            BulbDiscoverer.this.connectToIP(ipRangeBaseString + "1", timeout);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                };
                this.threadList.add(thread2);
                thread2.start();
            }
            new WaitTask(this.threadList).execute(new Void[0]);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0089 A[SYNTHETIC, Splitter:B:24:0x0089] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0099 A[SYNTHETIC, Splitter:B:30:0x0099] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a9 A[SYNTHETIC, Splitter:B:36:0x00a9] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b9 A[SYNTHETIC, Splitter:B:42:0x00b9] */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:60:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
    private void connectToIP(String ip, int TIMEOUT) throws Throwable {
        Throwable th;
        Socket socket = null;
        try {
            Log.d(TAG, "Searching IP at: " + ip);
            Socket socket2 = new Socket();
            try {
                socket2.connect(new InetSocketAddress(ip, 13456), TIMEOUT);
                Log.d(TAG, "Connected to: " + ip);
                BeamBulb bulb = Utils.readBeamInfo(socket2.getInputStream(), BeamConnectionType.CONNECTION_TYPE_WIFI, ip, "");
                if (bulb == null) {
                    socket2.close();
                    if (socket2 != null) {
                        try {
                            socket2.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while closing the wifi socket");
                        }
                    }
                    return;
                }
                if (!checkBeam(bulb)) {
                    wifiBeamList.add(bulb);
                    Log.d(TAG, "Adding beam wifi");
                }
                if (socket2 != null) {
                    try {
                        socket2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Exception while closing the wifi socket");
                    }
                }
            } catch (UnknownHostException e3) {
                socket = socket2;
                if (socket == null) {
                    try {
                        socket.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "Exception while closing the wifi socket");
                    }
                }
            } catch (SocketTimeoutException e5) {
                socket = socket2;
                if (socket == null) {
                    try {
                        socket.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "Exception while closing the wifi socket");
                    }
                }
            } catch (IOException e7) {
                socket = socket2;
                if (socket == null) {
                    try {
                        socket.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "Exception while closing the wifi socket");
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                socket = socket2;
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "Exception while closing the wifi socket");
                    }
                }
                throw th;
            }
        } catch (UnknownHostException e10) {
            if (socket == null) {
            }
        } catch (SocketTimeoutException e11) {
            if (socket == null) {
            }
        } catch (IOException e12) {
            if (socket == null) {
            }
        } catch (Throwable th3) {
            th = th3;
            if (socket != null) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public class WaitTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = WaitTask.class.getSimpleName();
        int num = 0;
        private int threadId = 0;
        ArrayList<Thread> threadList = new ArrayList<>();

        public WaitTask(ArrayList<Thread> list) {
            this.threadList.clear();
            this.threadList.addAll(list);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            SystemClock.sleep(1000);
            Log.d(this.TAG, "Stopping all threads " + this.threadId + ", thread count " + this.threadList.size());
            Log.d(this.TAG, "do in background start");
            Iterator<Thread> it = this.threadList.iterator();
            while (it.hasNext()) {
                Thread t = it.next();
                try {
                    if (BulbDiscoverer.wifiBeamList.size() > this.num) {
                        this.num = BulbDiscoverer.wifiBeamList.size();
                        BulbDiscoverer.this.sendWifiBeamListBroadcast(BulbDiscoverer.this.mContext);
                    }
                    t.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(this.TAG, "do in background end");
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void v) {
            int numBulbs = BulbDiscoverer.wifiBeamList.size();
            if (BulbDiscoverer.CURRENT_WIFI_STATE == BulbDiscoverer.STATE_WIFI_SCANNING) {
                BulbDiscoverer.CURRENT_WIFI_STATE = numBulbs == 0 ? BulbDiscoverer.STATE_NO_BEAM : BulbDiscoverer.STATE_BEAM_FOUND;
            }
            BulbDiscoverer.this.sendWifiBeamListBroadcast(BulbDiscoverer.this.mContext);
            Log.d(this.TAG, "Stopped all threads " + this.threadId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendWifiBeamListBroadcast(Context context) {
        Log.i(TAG, "sendWifiBeamListBroadcast " + wifiBeamList.size());
        Intent intent = new Intent(RECEIVER_ACTION);
        intent.putParcelableArrayListExtra(RECEIVER_WIFI_LIST, wifiBeamList);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
