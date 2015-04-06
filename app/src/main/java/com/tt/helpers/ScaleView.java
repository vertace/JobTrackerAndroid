package com.tt.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ScaleView extends ImageView {

    public String PhotoID;
    public String ShopID;
    public String MID;

    public ScaleView(Context context) {
        super(context);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * getDrawable().getIntrinsicHeight()
                / getDrawable().getIntrinsicWidth();
        setMeasuredDimension(width, height);
    }
}