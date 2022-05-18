package com.studiodiip.bulbbeam.mousecontroller.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public abstract class WifiUtil {
    @SuppressLint("MissingPermission")
    private static WifiInfo getWifiInfo(Context context) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
    }

    public static String getMyMacAddress(Context context) {
        return getWifiInfo(context).getMacAddress();
    }

    public static String getMyIpString(Context context) {
        int ipAddress = getWifiInfo(context).getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        try {
            return InetAddress.getByAddress(BigInteger.valueOf((long) ipAddress).toByteArray()).getHostAddress();
        } catch (UnknownHostException e) {
            Log.e("WIFIIP", "Unable to get host address.");
            return null;
        }
    }

    public static String getMyIpRangeBaseString(Context context) {
        String ipString = getMyIpString(context);
        if (ipString == null) {
            return null;
        }
        return ipString.substring(0, ipString.lastIndexOf(".")) + ".";
    }
}
