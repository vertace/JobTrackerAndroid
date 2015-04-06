package com.tt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.sstracker.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Vasanth on 05-Apr-2015.
 */
public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;

    public ImageAdapter(Context c) {
        super(c, 0);
        mContext = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(256, 256));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        Picasso.with(mContext).load(Uri.fromFile(new File(getItem(position)))).placeholder(R.drawable.photo)
                .error(R.drawable.camera).into(imageView);

        //imageView.setImageBitmap(getThumbnailImage(getItem(position)));
        return imageView;
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
