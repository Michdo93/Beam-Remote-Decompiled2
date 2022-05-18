package com.studiodiip.bulbbeam.mousecontroller.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class VideoThumbnailAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mVideoPathList = new ArrayList<>();
    private ShowVideoThumbnail showVideoThumbnail = new ShowVideoThumbnail();

    public VideoThumbnailAdapter(Context c, ArrayList<String> imageList) {
        this.mContext = c;
        if (imageList == null) {
            this.mVideoPathList = new ArrayList<>();
        } else {
            this.mVideoPathList = imageList;
        }
    }

    public int getCount() {
        return this.mVideoPathList.size();
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
        this.showVideoThumbnail.loadvideoThumbnail(this.mVideoPathList.get(position), imageView);
        return imageView;
    }
}
