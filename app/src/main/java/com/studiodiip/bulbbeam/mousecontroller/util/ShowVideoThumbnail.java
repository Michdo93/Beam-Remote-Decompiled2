package com.studiodiip.bulbbeam.mousecontroller.util;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import androidx.core.view.ViewCompat;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ShowVideoThumbnail {
    public void loadvideoThumbnail(String path, ImageView imageView) {
        if (cancelPotentialWork(path, imageView)) {
            Bitmap bitmap = BeamSettings.getInstance().getVideoBitmapFromMemCache(path);
            if (bitmap != null) {
                setThumbnail(imageView, bitmap);
                return;
            }
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            imageView.setImageDrawable(new AsyncDrawable(task));
            task.execute(path);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setThumbnail(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    /* access modifiers changed from: package-private */
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private Bitmap bitmap;
        private final WeakReference<ImageView> imageViewReference;
        private String path = null;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference<>(imageView);
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(String... params) {
            this.path = params[0];
            this.bitmap = ThumbnailUtils.createVideoThumbnail(this.path, 3);
            BeamSettings.getInstance().addBitmapToMemoryCache(this.path, this.bitmap);
            return this.bitmap;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap2) {
            if (isCancelled()) {
                bitmap2 = null;
            }
            if (!(this.imageViewReference == null || bitmap2 == null)) {
                ImageView imageView = this.imageViewReference.get();
                if (this == ShowVideoThumbnail.this.getBitmapWorkerTask(imageView) && imageView != null) {
                    ShowVideoThumbnail.this.setThumbnail(imageView, bitmap2);
                }
            }
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
