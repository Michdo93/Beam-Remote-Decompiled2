package com.studiodiip.bulbbeam.mousecontroller;

import android.os.AsyncTask;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.service.ConnectionService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class mouseSocketSender {
    public static long lastSendTime = 0;
    public static String receivedAppList;
    public static Socket socket;
    int SERVERPORT;
    String SERVER_IP;
    private String mAccountName;
    private String mLocalMac;
    private PrintWriter mPrintWriter;
    private String mUserName;
    boolean socketIsConnecting = false;

    /* access modifiers changed from: private */
    public class SendToSocketTask extends AsyncTask<String, Void, Void> {
        private SendToSocketTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(String... params) {
            while (mouseSocketSender.this.socketIsConnecting) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mouseSocketSender.socket != null && mouseSocketSender.socket.isConnected() && !mouseSocketSender.socket.isClosed()) {
                mouseSocketSender.this.socketIsConnecting = false;
                StringBuilder sb = new StringBuilder();
                sb.append(params[0] + ";" + params[1]);
                if (params[2] != null) {
                    sb.append(";" + params[2]);
                }
                mouseSocketSender.this.mPrintWriter.println(sb.toString());
                mouseSocketSender.lastSendTime = System.currentTimeMillis();
                return null;
            } else if (mouseSocketSender.this.socketIsConnecting) {
                return null;
            } else {
                Log.d("SOCKET", "going to reconnect to server");
                mouseSocketSender.this.connectToSocket();
                mouseSocketSender.this.sendSocket(params[0], params[1]);
                return null;
            }
        }
    }

    public mouseSocketSender(String ip, int port, String accountName, String userName, String macAddress) {
        this.SERVER_IP = ip;
        this.SERVERPORT = port;
        this.mAccountName = accountName;
        this.mUserName = userName;
        this.mLocalMac = macAddress;
        new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.mouseSocketSender.AnonymousClass1 */

            public void run() {
                mouseSocketSender.this.connectToSocket();
            }
        }.start();
    }

    public void connectToSocket() {
        if (!this.socketIsConnecting) {
            this.socketIsConnecting = true;
            try {
                InetAddress serverAddr = InetAddress.getByName(this.SERVER_IP);
                if (serverAddr.isReachable(1000)) {
                    socket = new Socket(serverAddr, this.SERVERPORT);
                    new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                    sendSocket(ConnectionService.USER_NAME, this.mUserName + ";" + this.mLocalMac);
                    this.mPrintWriter = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                } else {
                    Log.e("SOCKET", "SOCKET IS NOT REACHABLE!!");
                    this.socketIsConnecting = false;
                }
            } catch (UnknownHostException e1) {
                this.socketIsConnecting = false;
                e1.printStackTrace();
            } catch (IOException e12) {
                this.socketIsConnecting = false;
                e12.printStackTrace();
            }
        }
        this.socketIsConnecting = false;
    }

    public void sendSocket(String x, String y, int nr) {
        new SendToSocketTask().execute(x, y);
    }

    public void sendSocket(String x, String y) {
        sendSocket(x, y, (String) null);
    }

    public void sendSocket(String x, String y, String z) {
        new SendToSocketTask().execute(x, y, z);
    }

    public void sendKeyToSocket(final String key, final String type) {
        new Thread() {
            /* class com.studiodiip.bulbbeam.mousecontroller.mouseSocketSender.AnonymousClass2 */

            public void run() {
                while (mouseSocketSender.this.socketIsConnecting) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mouseSocketSender.socket != null && mouseSocketSender.socket.isConnected() && !mouseSocketSender.socket.isClosed()) {
                    mouseSocketSender.this.socketIsConnecting = false;
                    StringBuilder sb = new StringBuilder();
                    if (type.equals("led")) {
                        sb.append(key);
                    } else {
                        sb.append(type + ";" + key + ";0");
                    }
                    mouseSocketSender.this.mPrintWriter.println(sb.toString());
                    mouseSocketSender.lastSendTime = System.currentTimeMillis();
                } else if (!mouseSocketSender.this.socketIsConnecting) {
                    mouseSocketSender.this.connectToSocket();
                    mouseSocketSender.this.sendKeyToSocket(key, type);
                }
            }
        }.start();
    }
}
