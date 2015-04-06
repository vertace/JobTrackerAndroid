package com.tt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.sstracker.R;
import com.tt.data.MeasurementPhoto;
import com.tt.helpers.ImageLoader;
import com.tt.helpers.ScaleView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Adapts NewsEntry objects onto views for lists
 */
public final class MeasurementPhotoListAdapter extends
        ArrayAdapter<MeasurementPhoto> {

    private final int measurementPhotoListLayoutResource;
    public ImageLoader imageLoader;
    public Point _size;
    public Context _context;
    HashMap<String, ScaleView> imageViewList;

    public MeasurementPhotoListAdapter(final Context context,
                                       final int _measurementPhotoListLayoutResource, Point size) {
        super(context, 0);
        _size = size;
        _context = context;
        this.measurementPhotoListLayoutResource = _measurementPhotoListLayoutResource;
        imageLoader = new ImageLoader(getContext());
        // imageViewList = new HashMap<String, ScaleView>();
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup
        // efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final MeasurementPhoto entry = getItem(position);

        viewHolder.imgMeasurementPhoto.MID = String.valueOf(entry.ID);
        viewHolder.imgMeasurementPhoto.PhotoID = entry.PhotoID;
        viewHolder.imgMeasurementPhoto.ShopID = String.valueOf(entry.ShopID);

        // Bitmap bitmap = BitmapFactory.decodeFile(new File(entry.PhotoID)
        // .getPath());
        showPhoto(entry.PhotoID, viewHolder.imgMeasurementPhoto);

        return view;
    }

    private void showPhoto(String photoPath, ScaleView scaleView) {
        // Get screen size
        int screenWidth = _size.x;
        int screenHeight = _size.y;

        // Get target image size
        Bitmap bitmap = BitmapFactory.decodeFile(new File(photoPath).getPath());
        if (bitmap == null)
            return;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();

        // Scale the image down to fit perfectly into the screen
        // The value (50/20 in this case) must be adjusted for phone/tables
        // displays
        while (bitmapHeight > (screenHeight - 50)
                || bitmapWidth > (screenWidth - 20)) {
            bitmapHeight = bitmapHeight / 2;
            bitmapWidth = bitmapWidth / 2;
        }

        // Create resized bitmap image
        BitmapDrawable resizedBitmap = new BitmapDrawable(
                _context.getResources(), Bitmap.createScaledBitmap(bitmap,
                bitmapWidth, bitmapHeight, false));

        scaleView.setImageDrawable(resizedBitmap);
    }

    private View getWorkingView(final View convertView) {
        // The workingView is basically just the convertView re-used if possible
        // or inflated new if not possible
        View workingView = null;

        if (null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(measurementPhotoListLayoutResource,
                    null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    private ViewHolder getViewHolder(final View workingView) {
        // The viewHolder allows us to avoid re-looking up view references
        // Since views are recycled, these references will never change
        final Object tag = workingView.getTag();
        ViewHolder viewHolder = null;

        if (null == tag || !(tag instanceof ViewHolder)) {
            viewHolder = new ViewHolder();

            viewHolder.imgMeasurementPhoto = (ScaleView) workingView
                    .findViewById(R.id.imgMeasurementPhoto);

            workingView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) tag;
        }

        return viewHolder;
    }

    /**
     * ViewHolder allows us to avoid re-looking up view references Since views
     * are recycled, these references will never change
     */
    private static class ViewHolder {
        public ScaleView imgMeasurementPhoto;
    }

    public class GetPhoto extends AsyncTask<String, Integer, BitmapResult> {

        protected BitmapResult doInBackground(String... params) {
            String photoUrl = params[0];
            String photoID = params[1];
            try {
                BitmapResult result = new BitmapResult();
                result.Image = BitmapFactory
                        .decodeStream((InputStream) new URL(photoUrl)
                                .getContent());
                result.PhotoId = photoID;
                return result;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(BitmapResult result) {
            imageViewList.get(result.PhotoId).setImageBitmap(result.Image);
        }
    }

    public class BitmapResult {
        public Bitmap Image;
        public String PhotoId;
    }
}