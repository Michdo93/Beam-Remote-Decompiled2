package com.studiodiip.bulbbeam.mousecontroller.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class TypefaceTextView extends AppCompatTextView {
    private static Typeface tf;
    private static Typeface tfb;

    public static Typeface getTf(Context ctx) {
        if (tf == null) {
            tf = Typeface.createFromAsset(ctx.getAssets(), "apercu-regular-webfont.ttf");
        }
        return tf;
    }

    public static Typeface getTfb(Context ctx) {
        if (tfb == null) {
            tfb = Typeface.createFromAsset(ctx.getAssets(), "apercu-bold-webfont.ttf");
        }
        return tfb;
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TypefaceTextView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        boolean bold = false;
        if (attrs != null && "0x1".equalsIgnoreCase(attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textStyle"))) {
            bold = true;
        }
        if (bold) {
            setTypeface(getTfb(context), Typeface.BOLD);
        } else {
            setTypeface(getTf(context), Typeface.NORMAL);
        }
    }
}
