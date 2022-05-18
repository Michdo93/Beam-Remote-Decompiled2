package com.studiodiip.bulbbeam.mousecontroller.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.core.view.ViewCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.studiodiip.bulbbeam.mousecontroller.fragment.GalleryFragment;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ShowImage {
    private static final int MAX_BEAM_HEIGHT = 620;
    private static final int MAX_BEAM_WIDTH = 1200;
    private static final int MAX_BITMAP_HEIGHT = 2048;
    private static final int MAX_BITMAP_WIDTH = 2048;
    private IShowImageListener imageListener;
    private int mImageHeight;
    private int mImageWidth;
    private volatile BlockingQueue<Bitmap> sendImagesQueue;
    private SendImagesRunnable sendImagesRunnable;
    private Thread sendImagesThread;

    public interface IShowImageListener {
        void onBitMapCalculated(String str);
    }

    public void stopSendingImages() {
        this.sendImagesRunnable.stopRunning();
    }

    /* access modifiers changed from: package-private */
    public class SendImagesRunnable implements Runnable {
        private List<Bitmap> msgToExecute = new ArrayList();
        private volatile boolean running = true;

        SendImagesRunnable() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopRunning() {
            this.running = false;
        }

        public void run() {
            int resizedWidth;
            int resizedHeight;
            while (this.running) {
                this.msgToExecute.clear();
                if (!ShowImage.this.sendImagesQueue.isEmpty()) {
                    ShowImage.this.sendImagesQueue.drainTo(this.msgToExecute);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (ShowImage.this.imageListener != null) {
                    for (int m = 0; m < this.msgToExecute.size(); m++) {
                        Bitmap bitmap = this.msgToExecute.get(m);
                        if (bitmap != null) {
                            Log.d("TAG", "Original image  width - " + bitmap.getWidth() + ",height - " + bitmap.getHeight());
                            if (bitmap.getWidth() <= ShowImage.MAX_BEAM_WIDTH && bitmap.getHeight() <= ShowImage.MAX_BEAM_HEIGHT) {
                                resizedHeight = bitmap.getHeight();
                                resizedWidth = bitmap.getWidth();
                                Log.d("TAG", "Using same image  width - " + resizedWidth + ",height - " + resizedHeight);
                            } else if (bitmap.getWidth() < bitmap.getHeight()) {
                                resizedHeight = ShowImage.MAX_BEAM_HEIGHT;
                                resizedWidth = ShowImage.this.calculateBeamWidth(bitmap);
                                if (resizedWidth > ShowImage.MAX_BEAM_WIDTH) {
                                    resizedWidth = ShowImage.MAX_BEAM_WIDTH;
                                    resizedHeight = ShowImage.this.calculateBeamHeight(bitmap);
                                }
                                Log.d("TAG", "Portrait image  width - " + resizedWidth + ",height - " + resizedHeight);
                            } else {
                                resizedWidth = ShowImage.MAX_BEAM_WIDTH;
                                resizedHeight = ShowImage.this.calculateBeamHeight(bitmap);
                                if (resizedHeight > ShowImage.MAX_BEAM_HEIGHT) {
                                    resizedHeight = ShowImage.MAX_BEAM_HEIGHT;
                                    resizedWidth = ShowImage.this.calculateBeamWidth(bitmap);
                                }
                                Log.d("TAG", "Landscape image  width - " + resizedWidth + ",height - " + resizedHeight);
                            }
                            ShowImage.this.imageListener.onBitMapCalculated(BitMapToString(Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)));
                        }
                    }
                }
            }
        }

        private String BitMapToString(Bitmap bitmap) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return Base64.encodeToString(baos.toByteArray(), 2);
        }
    }

    public ShowImage(Context context) {
        if (context instanceof IShowImageListener) {
            this.imageListener = (IShowImageListener) context;
        }
        this.sendImagesQueue = new ArrayBlockingQueue(300);
        this.sendImagesRunnable = new SendImagesRunnable();
        this.sendImagesThread = new Thread(this.sendImagesRunnable);
        this.sendImagesThread.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateBeamWidth(Bitmap bitmap) {
        return (bitmap.getWidth() * MAX_BEAM_HEIGHT) / bitmap.getHeight();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateBeamHeight(Bitmap bitmap) {
        return (bitmap.getHeight() * MAX_BEAM_WIDTH) / bitmap.getWidth();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateWidth(Bitmap bitmap, int height) {
        return (bitmap.getWidth() * height) / bitmap.getHeight();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateHeight(Bitmap bitmap, int width) {
        return (bitmap.getHeight() * width) / bitmap.getWidth();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addBitMapToQueue(boolean isFullScreen, Bitmap bitmap) {
        if (isFullScreen && bitmap != null) {
            this.sendImagesQueue.add(bitmap);
        }
    }

    public void loadBitmap(String path, ImageView imageView, int width, int height, boolean isFullScreen) {
        if (cancelPotentialWork(path, imageView)) {
            Bitmap bitmap = BeamSettings.getInstance().getBitmapFromMemCache(path);
            if (bitmap != null) {
                setImage(imageView, bitmap, isFullScreen);
                if (isFullScreen) {
                    addBitMapToQueue(isFullScreen, bitmap);
                    return;
                }
                return;
            }
            this.mImageWidth = width;
            this.mImageHeight = height;
            BitmapWorkerTask task = new BitmapWorkerTask(imageView, isFullScreen);
            imageView.setImageDrawable(new AsyncDrawable(task));
            task.execute(path);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setImage(ImageView imageView, Bitmap bitmap, boolean isFullScreen) {
        if (!isFullScreen) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageBitmap(bitmap);
        float[] values = new float[9];
        Matrix m = new Matrix(imageView.getImageMatrix());
        m.getValues(values);
        BeamSettings.getInstance().setCurrentMinZoomLevel(values[0]);
        GalleryFragment.currentscale = values[0];
        GalleryFragment.mMatrix = new Matrix(m);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
    }

    /* access modifiers changed from: package-private */
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private Bitmap bitmap;
        private final WeakReference<ImageView> imageViewReference;
        private boolean mIsFullScreen = false;
        private String path = null;

        public BitmapWorkerTask(ImageView imageView, boolean isFullScreen) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.mIsFullScreen = isFullScreen;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(String... params) {
            this.path = params[0];
            this.bitmap = decodeSampledBitmapFromPath(this.path, ShowImage.this.mImageWidth, ShowImage.this.mImageHeight);
            ShowImage.this.addBitMapToQueue(this.mIsFullScreen, this.bitmap);
            this.bitmap = resizeBitmap(this.bitmap);
            BeamSettings.getInstance().addBitmapToMemoryCache(this.path, this.bitmap);
            return this.bitmap;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap2) {
            if (isCancelled()) {
                bitmap2 = null;
            }
            if (this.imageViewReference != null && bitmap2 != null) {
                ImageView imageView = this.imageViewReference.get();
                if (this == ShowImage.this.getBitmapWorkerTask(imageView) && imageView != null) {
                    ShowImage.this.setImage(imageView, bitmap2, this.mIsFullScreen);
                }
            }
        }

        private Bitmap resizeBitmap(Bitmap bm) {
            if (bm == null) {
                return bm;
            }
            int resizedHeight = bm.getHeight();
            int resizedWidth = bm.getWidth();
            Log.d("ShowImage", "device image width - " + resizedWidth + ",height - " + resizedHeight);
            if (resizedHeight > 2048 || resizedWidth > 2048) {
                if (resizedWidth > 2048) {
                    resizedWidth = 2048;
                    resizedHeight = ShowImage.this.calculateHeight(this.bitmap, 2048);
                }
                if (resizedHeight > 2048) {
                    resizedHeight = 2048;
                    resizedWidth = ShowImage.this.calculateWidth(this.bitmap, 2048);
                }
            }
            Log.d("ShowImage", "device image width - " + resizedWidth + ",height - " + resizedHeight);
            return Bitmap.createScaledBitmap(bm, resizedWidth, resizedHeight, false);
        }

        private Bitmap decodeSampledBitmapFromPath(String path2, int reqWidth, int reqHeight) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path2, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inDither = true;
            return BitmapFactory.decodeFile(path2, options);
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth) {
                int halfHeight = height / 2;
                int halfWidth = width / 2;
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
    }

    /* access modifiers changed from: package-private */
    public static class AsyncDrawable extends ColorDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapDownloaderTaskReference;

        public AsyncDrawable(BitmapWorkerTask bitmapDownloaderTask) {
            super(ViewCompat.MEASURED_STATE_MASK);
            this.bitmapDownloaderTaskReference = new WeakReference<>(bitmapDownloaderTask);
        }

        public BitmapWorkerTask getBitmapDownloaderTask() {
            return this.bitmapDownloaderTaskReference.get();
        }
    }

    private boolean cancelPotentialWork(String data, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask == null) {
            return true;
        }
        String bitmapData = bitmapWorkerTask.path;
        if (bitmapData != null && bitmapData.equals(data)) {
            return false;
        }
        bitmapWorkerTask.cancel(true);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).getBitmapDownloaderTask();
            }
        }
        return null;
    }
}
