package com.studiodiip.bulbbeam.mousecontroller.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class BeamBulb implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        /* class com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BeamBulb createFromParcel(Parcel in) {
            return new BeamBulb(in);
        }

        @Override // android.os.Parcelable.Creator
        public BeamBulb[] newArray(int size) {
            return new BeamBulb[size];
        }
    };
    public BeamConnectionType connectionType;
    public String ip;
    public int led;
    public String mac;
    public String screenState;
    public String title;
    public int version;
    public int volume;

    public BeamBulb(String title2, String ip2, String screenState2, int volume2, int led2, BeamConnectionType connectionType2, String macAddress) {
        this(title2, ip2, screenState2, volume2, led2, 0, connectionType2, macAddress);
    }

    public BeamBulb() {
    }

    private BeamBulb(Parcel in) {
        this.title = in.readString();
        this.ip = in.readString();
        this.screenState = in.readString();
        this.mac = in.readString();
        this.connectionType = BeamConnectionType.values()[in.readInt()];
        this.volume = in.readInt();
        this.led = in.readInt();
        this.version = in.readInt();
    }

    public BeamBulb(String title2, String ip2, String screenState2, int volume2, int led2, int version2, BeamConnectionType connectionType2, String macAddress) {
        this.title = title2;
        this.ip = ip2;
        this.screenState = screenState2;
        this.volume = volume2;
        this.led = led2;
        this.version = version2;
        this.mac = macAddress;
        this.connectionType = connectionType2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.title);
        parcel.writeString(this.ip);
        parcel.writeString(this.screenState);
        parcel.writeString(this.mac);
        parcel.writeInt(this.connectionType.ordinal());
        parcel.writeInt(this.volume);
        parcel.writeInt(this.led);
        parcel.writeInt(this.version);
    }
}
