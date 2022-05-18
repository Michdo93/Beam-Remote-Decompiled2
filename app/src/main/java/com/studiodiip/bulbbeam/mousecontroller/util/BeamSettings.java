package com.studiodiip.bulbbeam.mousecontroller.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.LruCache;

import com.google.gson.Gson;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;

import java.util.ArrayList;

public class BeamSettings {
    private static final String KEY_LAST_USED_BEAM = "last_used";
    private static BeamSettings beamSettings;
    public static boolean isConnected = false;
    public static Intent shareIntent;
    private BeamBulb beamBulb;
    private SharedPreferences beamPreferences;
    private float currentMinZoomLevel = 1.0f;
    private boolean isImageShownInFullScreen;
    private boolean isVideoPlayedInFullScreen;
    private LruCache<String, Bitmap> mImageBitMapCache;
    private ArrayList<String> mImagePathList = null;
    private LruCache<String, Bitmap> mVideoBitMapCache;
    private ArrayList<String> mVideoPathList = null;

    public static BeamSettings getInstance() {
        return beamSettings;
    }

    public static void init(Context context) {
        if (beamSettings == null) {
            beamSettings = new BeamSettings(context);
        }
    }

    private BeamSettings(Context context) {
        this.beamPreferences = context.getSharedPreferences(context.getPackageName(), 0);
    }

    public void setLastUsedBeam(BeamBulb value) {
        this.beamPreferences.edit().putString(KEY_LAST_USED_BEAM, new Gson().toJson(value)).commit();
    }

    public BeamBulb getLastUsedBeam() {
        Gson gson = new Gson();
        String json = this.beamPreferences.getString(KEY_LAST_USED_BEAM, null);
        if (json == null) {
            return null;
        }
        return (BeamBulb) gson.fromJson(json, BeamBulb.class);
    }

    public void setImageShownInFullScreen(boolean value) {
        this.isImageShownInFullScreen = value;
    }

    public boolean isImageShownInFullScreen() {
        return this.isImageShownInFullScreen;
    }

    public void setVideoPlayedInFullScreen(boolean value) {
        this.isVideoPlayedInFullScreen = value;
    }

    public boolean isVideoPlayedInFullScreen() {
        return this.isVideoPlayedInFullScreen;
    }

    public ArrayList<String> getVideoPathList() {
        return this.mVideoPathList;
    }

    public void setVideoPathList(ArrayList<String> list) {
        this.mVideoPathList = list;
    }

    public void setSelectedBeam(BeamBulb value) {
        this.beamBulb = value;
    }

    public BeamBulb getSelectedBeam() {
        return this.beamBulb;
    }

    public ArrayList<String> getImagePathList() {
        return this.mImagePathList;
    }

    public void setImagePathList(ArrayList<String> list) {
        this.mImagePathList = list;
    }

    private void initImageMemoryCache() {
        this.mImageBitMapCache = new LruCache<String, Bitmap>(((int) (Runtime.getRuntime().maxMemory() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) / 10) {
            /* class com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings.AnonymousClass1 */

            /* access modifiers changed from: protected */
            public int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void initVideoMemoryCache() {
        this.mVideoBitMapCache = new LruCache<String, Bitmap>(((int) (Runtime.getRuntime().maxMemory() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) / 10) {
            /* class com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings.AnonymousClass2 */

            /* access modifiers changed from: protected */
            public int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void setCurrentMinZoomLevel(float val) {
        this.currentMinZoomLevel = val;
    }

    public float getCurrentMinZoomLevel() {
        return this.currentMinZoomLevel;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (this.mImageBitMapCache == null) {
            initImageMemoryCache();
        }
        if (key != null && bitmap != null && getBitmapFromMemCache(key) == null) {
            this.mImageBitMapCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if (this.mImageBitMapCache == null) {
            initImageMemoryCache();
        }
        if (key == null) {
            return null;
        }
        return this.mImageBitMapCache.get(key);
    }

    public void addVideoBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (this.mVideoBitMapCache == null) {
            initVideoMemoryCache();
        }
        if (key != null && bitmap != null && getBitmapFromMemCache(key) == null) {
            this.mVideoBitMapCache.put(key, bitmap);
        }
    }

    public Bitmap getVideoBitmapFromMemCache(String key) {
        if (this.mVideoBitMapCache == null) {
            initVideoMemoryCache();
        }
        if (key == null) {
            return null;
        }
        return this.mVideoBitMapCache.get(key);
    }
}
