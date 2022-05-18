package com.studiodiip.bulbbeam.mousecontroller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.view.MotionEventCompat;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.util.BeamSettings;
import com.studiodiip.bulbbeam.mousecontroller.util.ImageAdapter;
import com.studiodiip.bulbbeam.mousecontroller.util.ShowImage;

import java.util.ArrayList;

public class GalleryFragment extends Fragment implements View.OnTouchListener {
    private static final String ARG_KEY_FEED_ITEM = "somekey";
    private static final int DRAG = 1;
    private static final int NONE = 0;
    private static final String STATE_CURRENT_POS = "currentPos";
    private static final String STATE_IMAGE_LIST = "imageList";
    private static final String TAG = GalleryFragment.class.getSimpleName();
    private static final int ZOOM = 2;
    public static float currentscale = 1.0f;
    public static Matrix mMatrix = new Matrix();
    private int MODE = 0;
    private int currentPos = -1;
    float[] dragMatrixValues = new float[9];
    public ImageView fullScreenView;
    private GestureDetector gestureDetector;
    private GridView gridView;
    PointF lastDelta = new PointF();
    float lastScale = 1.0f;
    private Context mContext = getActivity();
    private IGalleryFragmentListener mGalleryFragmentListener;
    private ImageAdapter mImageAdpater;
    private ArrayList<String> mImagePathList = new ArrayList<>();
    private ShowImage mShowImage;
    PointF mid = new PointF();
    float oldDist = 1.0f;
    float oldScaleEvent = 1.0f;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    float[] valuesSE = new float[9];
    float[] zoomValues = new float[9];

    public interface IGalleryFragmentListener {
        void onImageTranslated(float f, float f2, float f3, float f4, float f5);
    }

    public static GalleryFragment newInstance(Context c) {
        Bundle args = new Bundle();
        GalleryFragment f = new GalleryFragment();
        args.putInt(ARG_KEY_FEED_ITEM, 1);
        f.setArguments(args);
        return f;
    }

    private void loadImages(Context c) {
        if (!Boolean.valueOf(Environment.getExternalStorageState().equals("mounted")).booleanValue()) {
            Log.d(TAG, "No sd card mounted");
            return;
        }
        ArrayList<String> imageList = new ArrayList<>();
        Cursor cursor = c.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_data"}, null, null, "datetaken DESC");
        int count = cursor.getCount();
        Log.i(TAG, "Total images " + count);
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            imageList.add(cursor.getString(cursor.getColumnIndex("_data")));
        }
        BeamSettings.getInstance().setImagePathList(imageList);
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POS, this.currentPos);
        outState.putStringArrayList(STATE_IMAGE_LIST, this.mImagePathList);
    }

    public void onStop() {
        super.onStop();
        if (this.mShowImage != null) {
            this.mShowImage.stopSendingImages();
        }
    }

    public void onCreate(Bundle save) {
        Log.i(TAG, "onCreate");
        super.onCreate(save);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated ");
        if (savedInstanceState != null) {
            this.currentPos = savedInstanceState.getInt(STATE_CURRENT_POS);
            this.mImagePathList = savedInstanceState.getStringArrayList(STATE_IMAGE_LIST);
            Log.i(TAG, "Saved state " + this.currentPos + ", list size " + this.mImagePathList.size());
            if (this.currentPos == -1) {
                Log.i(TAG, "No full screen");
            } else {
                this.fullScreenView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    /* class com.studiodiip.bulbbeam.mousecontroller.fragment.GalleryFragment.AnonymousClass1 */

                    public void onGlobalLayout() {
                        Log.d(GalleryFragment.TAG, "onGlobalLayout");
                        GalleryFragment.this.fullScreenView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        GalleryFragment.this.fullScreenView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        GalleryFragment.this.showImageInFullScreen(GalleryFragment.this.currentPos);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showImageInFullScreen(int currentPos2) {
        Log.i(TAG, "showImageInFullScreen " + currentPos2);
        this.fullScreenView.setVisibility(View.VISIBLE);
        this.gridView.setVisibility(View.INVISIBLE);
        this.mShowImage.loadBitmap(this.mImagePathList.get(currentPos2), this.fullScreenView, this.fullScreenView.getWidth(), this.fullScreenView.getHeight(), true);
        this.fullScreenView.setOnTouchListener(this);
    }

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mGalleryFragmentListener = (IGalleryFragmentListener) activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView ");
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        this.mContext = getActivity();
        this.mShowImage = new ShowImage(this.mContext);
        this.gridView = (GridView) v.findViewById(R.id.gridview);
        this.fullScreenView = (ImageView) v.findViewById(R.id.fullScreenImageview);
        this.gridView.setVisibility(View.VISIBLE);
        this.fullScreenView.setVisibility(View.INVISIBLE);
        this.gestureDetector = new GestureDetector(this.mContext, new GestureListener());
        this.mImagePathList = BeamSettings.getInstance().getImagePathList();
        if (this.mImagePathList == null) {
            loadImages(this.mContext);
            this.mImagePathList = BeamSettings.getInstance().getImagePathList();
        }
        this.mImageAdpater = new ImageAdapter(getActivity(), this.mImagePathList);
        this.gridView.setAdapter((ListAdapter) this.mImageAdpater);
        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.fragment.GalleryFragment.AnonymousClass2 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GalleryFragment.this.currentPos = position;
                Log.d(GalleryFragment.TAG, "On item selected " + GalleryFragment.this.currentPos);
                BeamSettings.getInstance().setImageShownInFullScreen(true);
                GalleryFragment.this.showImageInFullScreen(GalleryFragment.this.currentPos);
            }
        });
        return v;
    }

    public void closeFullScreenView() {
        if (this.fullScreenView != null && this.gridView != null) {
            this.fullScreenView.setVisibility(View.INVISIBLE);
            this.gridView.setVisibility(View.VISIBLE);
            this.currentPos = -1;
        }
    }

    public void onSwipeRight() {
        Log.d(TAG, "onSwipeRight");
        if (this.currentPos == 0) {
            Log.d(TAG, "First Image");
            return;
        }
        this.currentPos--;
        this.mShowImage.loadBitmap(this.mImagePathList.get(this.currentPos), this.fullScreenView, this.fullScreenView.getWidth(), this.fullScreenView.getHeight(), true);
    }

    public void onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft");
        if (this.currentPos == this.mImagePathList.size() - 1) {
            Log.d(TAG, "Last Image");
            return;
        }
        this.currentPos++;
        this.mShowImage.loadBitmap(this.mImagePathList.get(this.currentPos), this.fullScreenView, this.fullScreenView.getWidth(), this.fullScreenView.getHeight(), true);
    }

    private void limitDrag(Matrix m) {
        m.getValues(this.dragMatrixValues);
        float translateX = this.dragMatrixValues[2];
        float translateY = this.dragMatrixValues[5];
        float scale = this.dragMatrixValues[0];
        float imageWidth = (float) this.fullScreenView.getDrawable().getIntrinsicWidth();
        float imageHeight = (float) this.fullScreenView.getDrawable().getIntrinsicHeight();
        if (translateX > ((float) this.fullScreenView.getLeft()) && imageWidth * scale >= ((float) this.fullScreenView.getWidth())) {
            translateX = (float) this.fullScreenView.getLeft();
        }
        if (Math.abs(translateX) > (imageWidth * scale) - ((float) this.fullScreenView.getRight())) {
            translateX = ((imageWidth * scale) - ((float) this.fullScreenView.getRight())) * -1.0f;
        }
        if (imageWidth * scale < ((float) this.fullScreenView.getWidth())) {
            translateX = (((float) this.fullScreenView.getRight()) - (imageWidth * scale)) / 2.0f;
        }
        if (translateY > ((float) this.fullScreenView.getTop()) && imageHeight * scale >= ((float) this.fullScreenView.getHeight())) {
            translateY = (float) this.fullScreenView.getTop();
        }
        if (Math.abs(translateY) > (imageHeight * scale) - ((float) this.fullScreenView.getBottom())) {
            translateY = ((imageHeight * scale) - ((float) this.fullScreenView.getBottom())) * -1.0f;
        }
        if (imageHeight * scale < ((float) this.fullScreenView.getHeight())) {
            translateY = (((float) this.fullScreenView.getBottom()) - (imageHeight * scale)) / 2.0f;
        }
        this.dragMatrixValues[2] = translateX;
        this.dragMatrixValues[5] = translateY;
        m.setValues(this.dragMatrixValues);
    }

    public boolean onTouch(View v, MotionEvent motionEvent) {
        this.gestureDetector.onTouchEvent(motionEvent);
        ImageView view = (ImageView) v;
        float rdx = motionEvent.getX() - this.lastDelta.x;
        float rdy = motionEvent.getY() - this.lastDelta.y;
        switch (motionEvent.getAction() & MotionEventCompat.ACTION_MASK) {
            case 0:
                mMatrix = new Matrix(view.getImageMatrix());
                this.savedMatrix.set(mMatrix);
                mMatrix.getValues(this.valuesSE);
                this.oldScaleEvent = this.valuesSE[0];
                this.start.set(motionEvent.getX(), motionEvent.getY());
                this.MODE = 1;
                break;
            case 1:
                break;
            case 2:
                if (this.MODE != 1) {
                    if (this.MODE == 2) {
                        float newDist = spacing(motionEvent);
                        if (newDist > 10.0f) {
                            mMatrix.set(this.savedMatrix);
                            float scaleBeam = currentscale * (newDist / this.oldDist);
                            float scale = newDist / this.oldDist;
                            float originalZoom = BeamSettings.getInstance().getCurrentMinZoomLevel();
                            mMatrix.postScale(scale, scale, this.mid.x, this.mid.y);
                            mMatrix.getValues(this.zoomValues);
                            float scaleMatrix = this.zoomValues[0];
                            if (scaleMatrix < originalZoom) {
                                this.zoomValues[0] = originalZoom;
                                this.zoomValues[4] = originalZoom;
                                mMatrix.setValues(this.zoomValues);
                            }
                            float deltaScale = scaleMatrix - this.oldScaleEvent;
                            this.oldScaleEvent = scaleMatrix;
                            if (this.mGalleryFragmentListener != null) {
                                float imageWidth = (float) this.fullScreenView.getDrawable().getIntrinsicWidth();
                                float imageHeight = (float) this.fullScreenView.getDrawable().getIntrinsicHeight();
                                float imageX = (this.mid.x - this.zoomValues[2]) / this.zoomValues[0];
                                this.mGalleryFragmentListener.onImageTranslated(rdx, rdy, deltaScale, imageX / imageWidth, ((this.mid.y - this.zoomValues[5]) / this.zoomValues[0]) / imageHeight);
                            }
                            this.lastScale = scaleBeam;
                            break;
                        }
                    }
                } else {
                    float dx = motionEvent.getX() - this.start.x;
                    float dy = motionEvent.getY() - this.start.y;
                    mMatrix.set(this.savedMatrix);
                    mMatrix.postTranslate(dx, dy);
                    if (this.mGalleryFragmentListener != null) {
                        this.mGalleryFragmentListener.onImageTranslated(rdx, rdy, 0.0f, -1.0f, -1.0f);
                        break;
                    }
                }
                break;
            case 3:
            case 4:
            default:
                return true;
            case 5:
                this.lastScale = currentscale;
                this.oldDist = spacing(motionEvent);
                if (this.oldDist > 10.0f) {
                    this.savedMatrix.set(mMatrix);
                    midPoint(this.mid, motionEvent);
                    this.MODE = 2;
                    break;
                }
                break;
            case 6:
                currentscale = this.lastScale;
                this.MODE = 0;
                break;
        }
        this.lastDelta.x = motionEvent.getX();
        this.lastDelta.y = motionEvent.getY();
        limitDrag(mMatrix);
        view.setImageMatrix(mMatrix);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt((x * x) + (y * y));
    }

    private void midPoint(PointF point, MotionEvent event) {
        Log.d("MOTION_EVENT", " X0-" + event.getX(0) + ", X1-" + event.getX(1) + ",Y0-" + event.getY(0) + ",Y1-" + event.getY(0));
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2.0f, y / 2.0f);
        Log.d("MOTION_EVENT", " x/2 " + (x / 2.0f) + ",y/2 " + (y / 2.0f));
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
                int SWIPE_THRESHOLD = GalleryFragment.this.fullScreenView.getWidth() / 4;
                int SWIPE_VELOCITY_THRESHOLD = GalleryFragment.this.fullScreenView.getWidth();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > ((float) SWIPE_THRESHOLD) && Math.abs(velocityX) > ((float) SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffX > 0.0f) {
                        GalleryFragment.this.onSwipeRight();
                        return true;
                    }
                    GalleryFragment.this.onSwipeLeft();
                    return true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }
}
