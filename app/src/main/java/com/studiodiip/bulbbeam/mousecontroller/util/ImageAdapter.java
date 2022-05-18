package com.studiodiip.bulbbeam.mousecontroller.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mImagePathList = new ArrayList<>();
    private ShowImage mShowImage;

    public ImageAdapter(Context c, ArrayList<String> imageList) {
        this.mContext = c;
        if (imageList == null) {
            this.mImagePathList = new ArrayList<>();
        } else {
            this.mImagePathList = imageList;
        }
        this.mShowImage = new ShowImage(this.mContext);
    }

    public int getCount() {
        return this.mImagePathList.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(this.mContext);
            imageView.setLayoutParams(new AbsListView.LayoutParams(350, 350));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        this.mShowImage.loadBitmap(this.mImagePathList.get(position), imageView, 350, 350, false);
        return imageView;
    }
}
