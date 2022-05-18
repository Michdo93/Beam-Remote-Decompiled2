package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;
import com.studiodiip.bulbbeam.mousecontroller.util.VideoThumbnailAdapter;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

public class VideoGalleryFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, View.OnTouchListener {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    private static final String STATE_CURRENT_POS = "currentPos";
    private static final String STATE_VIDEO_LIST = "videoList";
    private static final String TAG = VideoGalleryFragment.class.getSimpleName();
    private int currentPos = -1;
    private GestureDetector gestureDetector;
    private GridView gridView;
    private boolean isVideoPlaying;
    private Context mContext;
    MediaRecorder mMediaRecorder;
    private VideoThumbnailAdapter mVideoAdapter;
    private ArrayList<String> mVideoPathList = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView videoView;

    public static VideoGalleryFragment newInstance(Context c) {
        Bundle args = new Bundle();
        VideoGalleryFragment f = new VideoGalleryFragment();
        args.putInt(ARG_KEY_FEED_ITEM, 1);
        f.setArguments(args);
        return f;
    }

    private void loadVideos(Context c) {
        if (!Boolean.valueOf(Environment.getExternalStorageState().equals("mounted")).booleanValue()) {
            Log.d(TAG, "No sd card mounted");
            return;
        }
        ArrayList<String> videoList = new ArrayList<>();
        Cursor cursor = c.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_data"}, null, null, "datetaken DESC");
        int count = cursor.getCount();
        Log.i(TAG, "Total videos " + count);
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            videoList.add(cursor.getString(cursor.getColumnIndex("_data")));
        }
        BeamSettings.getInstance().setVideoPathList(videoList);
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POS, this.currentPos);
        outState.putStringArrayList(STATE_VIDEO_LIST, this.mVideoPathList);
    }

    public void onStop() {
        super.onStop();
    }

    public void onCreate(Bundle save) {
        Log.i(TAG, "onCreate");
        super.onCreate(save);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated " + savedInstanceState);
        if (savedInstanceState != null) {
            this.currentPos = savedInstanceState.getInt(STATE_CURRENT_POS);
            this.mVideoPathList = savedInstanceState.getStringArrayList(STATE_VIDEO_LIST);
            if (this.currentPos == -1) {
                Log.i(TAG, "No full screen");
            } else {
                this.videoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.fragment.VideoGalleryFragment.AnonymousClass1 */

                    public void onGlobalLayout() {
                        Log.d(VideoGalleryFragment.TAG, "onGlobalLayout");
                        VideoGalleryFragment.this.videoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void closeFullScreenView() {
        Log.d(TAG, "closeFullScreenView");
        if (this.videoView != null && this.gridView != null) {
            stopVideo();
            this.videoView.setVisibility(View.INVISIBLE);
            this.gridView.setVisibility(View.VISIBLE);
            this.currentPos = -1;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView " + savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_video_gallery, container, false);
        this.mContext = getActivity();
        this.gridView = (GridView) v.findViewById(R.id.gridVideoView);
        this.videoView = (SurfaceView) v.findViewById(R.id.videoView);
        this.vidHolder = this.videoView.getHolder();
        this.vidHolder.addCallback(this);
        this.gestureDetector = new GestureDetector(this.mContext, new GestureListener());
        this.mVideoPathList = BeamSettings.getInstance().getVideoPathList();
        if (this.mVideoPathList == null) {
            loadVideos(this.mContext);
            this.mVideoPathList = BeamSettings.getInstance().getVideoPathList();
        }
        this.mVideoAdapter = new VideoThumbnailAdapter(getActivity(), this.mVideoPathList);
        this.gridView.setAdapter((ListAdapter) this.mVideoAdapter);
        this.videoView.setVisibility(View.INVISIBLE);
        this.gridView.setVisibility(View.VISIBLE);
        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.VideoGalleryFragment.AnonymousClass2 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                VideoGalleryFragment.this.currentPos = position;
                Log.d(VideoGalleryFragment.TAG, "On item selected " + VideoGalleryFragment.this.currentPos);
                VideoGalleryFragment.this.gridView.setVisibility(View.INVISIBLE);
                VideoGalleryFragment.this.videoView.setVisibility(View.VISIBLE);
                BeamSettings.getInstance().setVideoPlayedInFullScreen(true);
                VideoGalleryFragment.this.prepareMediaPlayer(position);
                VideoGalleryFragment.this.videoView.setOnTouchListener(VideoGalleryFragment.this);
            }
        });
        return v;
    }

    private void startVideo() {
        Log.d(TAG, "startVideo ");
        if (this.mediaPlayer != null) {
            this.mediaPlayer.start();
            this.isVideoPlaying = true;
        }
    }

    private void stopVideo() {
        Log.d(TAG, "stopVideo ");
        if (this.mediaPlayer != null && this.isVideoPlaying) {
            Log.d(TAG, "isVideoPlaying");
            this.isVideoPlaying = false;
            this.mediaPlayer.stop();
            this.mediaPlayer.reset();
        }
    }

    private void initRecorder() {
        if (this.mMediaRecorder == null) {
            this.mMediaRecorder = new MediaRecorder();
        } else {
            this.mMediaRecorder.reset();
        }
        this.mMediaRecorder.setVideoSource(0);
        this.mMediaRecorder.setOutputFormat(2);
        this.mMediaRecorder.setVideoEncoder(2);
        this.mMediaRecorder.setOutputFile("/sdcard/test.mp4");
        this.mMediaRecorder.setVideoSize(320, 240);
        this.mMediaRecorder.setVideoFrameRate(30);
        this.mMediaRecorder.setPreviewDisplay(this.vidHolder.getSurface());
    }

    private void prepareMediaRecorder() {
        Log.d(TAG, "prepareMediaRecorder ");
        try {
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareMediaPlayer(int videoID) {
        try {
            if (this.mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
            } else {
                this.mediaPlayer.reset();
            }
            this.vidHolder = this.videoView.getHolder();
            this.vidHolder.addCallback(this);
            this.mediaPlayer.setDisplay(this.vidHolder);
            this.mediaPlayer.setOnPreparedListener(this);
            this.mediaPlayer.setAudioStreamType(3);
            this.mediaPlayer.setDataSource(this.mVideoPathList.get(videoID));
            this.mediaPlayer.prepare();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean onTouch(View v, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch ");
        this.gestureDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & MotionEventCompat.ACTION_MASK) {
            case 0:
                if (this.mediaPlayer == null || !this.isVideoPlaying) {
                    if (this.mediaPlayer != null) {
                        this.mediaPlayer.start();
                        this.isVideoPlaying = true;
                        break;
                    }
                } else {
                    this.mediaPlayer.pause();
                    this.isVideoPlaying = false;
                    break;
                }
                break;
        }
        return true;
    }

    public void onSwipeRight() {
        Log.d(TAG, "onSwipeRight");
        if (this.currentPos == 0) {
            Log.d(TAG, "First Image");
            return;
        }
        this.currentPos--;
        stopVideo();
        prepareMediaPlayer(this.currentPos);
    }

    public void onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft");
        if (this.currentPos == this.mVideoPathList.size() - 1) {
            Log.d(TAG, "Last Image");
            return;
        }
        this.currentPos++;
        stopVideo();
        prepareMediaPlayer(this.currentPos);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private GestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                int SWIPE_THRESHOLD = VideoGalleryFragment.this.videoView.getWidth() / 4;
                int SWIPE_VELOCITY_THRESHOLD = VideoGalleryFragment.this.videoView.getWidth();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > ((float) SWIPE_THRESHOLD) && Math.abs(velocityX) > ((float) SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffX > 0.0f) {
                        VideoGalleryFragment.this.onSwipeRight();
                        return true;
                    }
                    VideoGalleryFragment.this.onSwipeLeft();
                    return true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    private void captureScreen() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/myImages");
        if (!folder.exists()) {
            folder.mkdir();
        }
        new File(folder, "test.jpg");
        try {
            Process sh = Runtime.getRuntime().exec("su", (String[]) null, (File) null);
            OutputStream os = sh.getOutputStream();
            os.write("/system/bin/screencap -p /sdcard/myImages/t.jpg".getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
            int i = 0 + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated ");
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged ");
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed ");
    }

    public void onPrepared(MediaPlayer mediaPlayer2) {
        Log.d(TAG, "onPrepared " + this.currentPos);
        startVideo();
    }
}
