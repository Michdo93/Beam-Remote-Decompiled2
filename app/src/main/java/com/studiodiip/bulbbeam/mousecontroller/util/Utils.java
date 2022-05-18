package com.studiodiip.bulbbeam.mousecontroller.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamConnectionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Collections;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String getUserName(Context context) {
        Log.d(TAG, "getUserName");
        String userName = "";
        @SuppressLint("MissingPermission") Account[] accs = AccountManager.get(context).getAccountsByType("com.google");
        if (accs.length > 0) {
            String[] parts = accs[0].name.split("@");
            if (parts.length > 1) {
                String userName2 = parts[0];
                userName = userName2.substring(0, 1).toUpperCase() + userName2.substring(1);
            }
        }
        Log.d(TAG, "UserName  " + userName);
        return userName;
    }

    private static boolean timeout(InputStream in, int timeoutInMs) {
        int timeout = 0;
        int maxTimeout = timeoutInMs / 250;
        while (true) {
            try {
                if (!(in.available() == 0 && timeout < maxTimeout)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                timeout++;
                Thread.sleep(250);
            } catch (Exception ex) {
                ex.printStackTrace();
                return true;
            }
        }
        if (timeout != maxTimeout) {
            return false;
        }
        return true;
    }

    public static BeamBulb readBeamInfo(InputStream in, BeamConnectionType connectionType, String ip, String remoteMacAddress) throws Throwable {
        Throwable th;
        Exception e2;
        InterruptedException e1;
        IOException e;
        int version;
        Log.d(TAG, "readBeamInfo " + connectionType + ", ip " + ip + ",mac address " + remoteMacAddress);
        BufferedReader reader = null;
        BeamBulb bulb = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
            try {
                if (timeout(in, 4000)) {
                    Log.d(TAG, "Timed out while trying to read beam identity " + connectionType);
                    try {
                        reader2.close();
                        in.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Exception while closing the input stream");
                    }
                    return null;
                }
                String fullBeamName = reader2.readLine();
                if (fullBeamName == null) {
                    Log.d(TAG, "Not a beam device " + connectionType);
                    try {
                        reader2.close();
                        in.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "Exception while closing the input stream");
                    }
                    return null;
                }
                int volume = Integer.parseInt(reader2.readLine().split(";")[1]);
                Thread.sleep(50);
                int led = Integer.parseInt(reader2.readLine().split(";")[1]);
                Thread.sleep(50);
                if (timeout(in, 250)) {
                    Log.d(TAG, "Timed out while reading beam version. Could be old beam");
                    version = 1;
                } else {
                    version = Integer.parseInt(reader2.readLine().split(";")[1]);
                }
                Log.d(TAG, "Read beam version" + version);
                String beamIp = ip;
                String bluetoothMac = remoteMacAddress;
                if (version > 2) {
                    Log.d(TAG, "New beam service.. " + version);
                    String[] beamIpCommand = reader2.readLine().split(";");
                    Thread.sleep(50);
                    String[] bluetoothMacCommand = reader2.readLine().split(";");
                    if (beamIpCommand.length > 1) {
                        beamIp = beamIpCommand[1];
                    }
                    if (bluetoothMacCommand.length > 1) {
                        bluetoothMac = bluetoothMacCommand[1];
                    }
                }
                BeamBulb bulb2 = new BeamBulb();
                try {
                    bulb2.title = fullBeamName.split(";")[1];
                    bulb2.volume = volume;
                    bulb2.led = led;
                    bulb2.version = version;
                    bulb2.ip = beamIp;
                    bulb2.mac = bluetoothMac;
                    bulb2.connectionType = connectionType;
                    Log.d(TAG, "Read beam info successfully - for" + bulb2.title + "Beam IP " + beamIp + " bluetooth mac: " + bluetoothMac + ", connection type " + connectionType);
                    try {
                        reader2.close();
                        in.close();
                        bulb = bulb2;
                    } catch (IOException e5) {
                        Log.e(TAG, "Exception while closing the input stream");
                        bulb = bulb2;
                    }
                } catch (Exception e11) {
                    e2 = e11;
                    bulb = bulb2;
                    reader = reader2;
                    Log.e(TAG, "Exception occurred while reading Beam identity info" + e2);
                    try {
                        reader.close();
                        in.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "Exception while closing the input stream");
                    }
                    return bulb;
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    reader.close();
                    in.close();
                    throw th;
                }
                return bulb;
            } catch (IOException e13) {
                e = e13;
                reader = reader2;
                Log.e(TAG, "I/O exception occurred while reading Beam identity info" + e);
                reader.close();
                in.close();
                return bulb;
            } catch (InterruptedException e14) {
                e1 = e14;
                reader = reader2;
                Log.e(TAG, "Interrupted exception occurred while reading Beam identity info" + e1);
                reader.close();
                in.close();
                return bulb;
            } catch (Exception e15) {
                e2 = e15;
                reader = reader2;
                Log.e(TAG, "Exception occurred while reading Beam identity info" + e2);
                reader.close();
                in.close();
                return bulb;
            } catch (Throwable th4) {
                th = th4;
                reader = reader2;
                reader.close();
                in.close();
                throw th;
            }
        } catch (IOException e16) {
            e = e16;
            Log.e(TAG, "I/O exception occurred while reading Beam identity info" + e);
            reader.close();
            in.close();
            return bulb;
        } catch (InterruptedException e17) {
            e1 = e17;
            Log.e(TAG, "Interrupted exception occurred while reading Beam identity info" + e1);
            reader.close();
            in.close();
            return bulb;
        } catch (Exception e18) {
            e2 = e18;
            Log.e(TAG, "Exception occurred while reading Beam identity info" + e2);
            reader.close();
            in.close();
            return bulb;
        }
    }

    public static String getMacAddress() {
        Log.d(TAG, "getMacAddress");
        String macAddress = "";
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        break;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    int length = macBytes.length;
                    for (int i = 0; i < length; i++) {
                        stringBuilder.append(String.format("%02X:", Byte.valueOf(macBytes[i])));
                    }
                    if (stringBuilder.length() > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                    macAddress = stringBuilder.toString();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception while reading mac address");
            macAddress = "02:00:00:00:00:00";
        }
        Log.d(TAG, "local mac address " + macAddress);
        return macAddress;
    }
}
