package com.tt.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.tt.data.Shared;
import com.tt.jobtracker.FullScreenImageViewActivity;
import com.tt.jobtracker.FullScreenViewActivity;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Vasanth on 05-Apr-2015.
 */
public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private Activity _activity;
    public ImageAdapter(Context c) {
        super(c, 0);
        mContext = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            Resources r = Resources.getSystem();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, r.getDisplayMetrics());
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
           // imageView.setLayoutParams(new GridView.LayoutParams(params));
            imageView.setLayoutParams(new GridView.LayoutParams((int)px, (int)px));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        if(Shared.admin_mian_activity==true)
        {
            UrlImageViewHelper.setUrlDrawable(imageView, getItem(position),R.drawable.image_loading);
        }
        else {
              Picasso.with(mContext).load(Uri.fromFile(new File(getItem(position)))).placeholder(R.drawable.photo)
              .error(R.drawable.camera).into(imageView);
        }
        //imageView.setImageBitmap(getThumbnailImage(getItem(position)));
        imageView.setOnClickListener(new OnImageClickListener(position));
        return imageView;
    }

    class OnImageClickListener implements View.OnClickListener {

        int _postion;

        // constructor
        public OnImageClickListener(int position) {
            this._postion = position;
        }

        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity


            Intent i = new Intent(v.getContext(), FullScreenViewActivity.class);
           // Intent i = new Intent((MainActivity)mContext, FullScreenViewActivity.class);
            i.putExtra("position", _postion);
            mContext.startActivity(i);
        }

    }



    public Bitmap getThumbnailImage(String path) {
        Bitmap imgthumBitmap = null;
        try {
            final int THUMBNAIL_SIZE = 120;

            FileInputStream fis = new FileInputStream(path);
            imgthumBitmap = BitmapFactory.decodeStream(fis);

            imgthumBitmap = Bitmap.createScaledBitmap(imgthumBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
            imgthumBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearroutstream);
        } catch (Exception ex) {

        }
        return imgthumBitmap;
    }
}
