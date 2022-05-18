package com.studiodiip.bulbbeam.mousecontroller.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.studiodiip.bulbbeam.mousecontroller.service.ConnectionServiceController;

import java.util.TimerTask;

public class UnbindTimerTask extends TimerTask implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        /* class com.studiodiip.bulbbeam.mousecontroller.util.UnbindTimerTask.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UnbindTimerTask createFromParcel(Parcel in) {
            return new UnbindTimerTask(in);
        }

        @Override // android.os.Parcelable.Creator
        public UnbindTimerTask[] newArray(int size) {
            return new UnbindTimerTask[size];
        }
    };
    private static final String TAG = UnbindTimerTask.class.getSimpleName();

    public void run() {
        Log.d(TAG, "Its ten seconds");
        ConnectionServiceController.getInstance().unbindConnectionService();
        cancel();
    }

    public UnbindTimerTask() {
    }

    private UnbindTimerTask(Parcel in) {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
    }
}
