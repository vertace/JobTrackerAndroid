package com.tt.jobtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.sstracker.R;
import com.google.gson.Gson;
import com.tt.data.MeasurementPhoto;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.MeasurementView;
import com.tt.helpers.MeasurementView.MeasurementObject;

import java.io.File;
import java.util.List;

public class TakeMeasurement extends Activity implements AsyncResponse {
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    Paint paint;
    int CanvasWidth;
    int CanvasHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takemeasurement);

        String PhotoID = getIntent().getStringExtra("PhotoID");
        LoadMeasurementPhoto(PhotoID);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.takemeasurement, menu);
        return true;
    }

    @Override
    public void processFinish(ServerResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        MeasurementView mView = (MeasurementView) findViewById(R.id.imgMeasurementImage);

        switch (item.getItemId()) {
            case R.id.mnuClear:
                AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("Clear");
                newDialog
                        .setMessage("Clear all measurements? You will lose current markings.");
                newDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MeasurementView i = (MeasurementView) findViewById(R.id.imgMeasurementImage);
                                i.CleanSlate();
                                dialog.dismiss();
                            }
                        });
                newDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                newDialog.show();
                break;
            case R.id.mnuDelete:
                mView.RemoveLastPoint();
                break;
            case R.id.mnuConnect:
                if (mView.CurrentObject.Points.size() > 1)
                    showPopup(TakeMeasurement.this);

                break;
            case R.id.mnuUpload:
                String measurementString = getMeasurementString(mView);
                String MID = getIntent().getStringExtra("MID");

                MeasurementPhoto measurementPhoto = dbHelper
                        .getMeasurementPhotoInfo(MID);
                measurementPhoto.MeasurementString = measurementString;
                dbHelper.saveMeasurementPhoto(measurementPhoto, true);

                Intent intent = new Intent(TakeMeasurement.this,
                        TakeMeasurementList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("TaskID", getIntent().getStringExtra("TaskID"));
                intent.putExtra("ShopID", getIntent().getStringExtra("ShopID"));
                intent.putExtra("ShopName", getIntent().getStringExtra("ShopName"));
                intent.putExtra("ShopAddress", getIntent().getStringExtra("ShopAddress"));
                getApplicationContext().startActivity(intent);
                finish();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private String getMeasurementString(MeasurementView mView) {
        MeasurementViewModel viewModel = new MeasurementViewModel();
        viewModel.PhotoObjectList = mView.MeasurementObjectList;
        viewModel.TaskID = Integer.parseInt(getIntent()
                .getStringExtra("TaskID"));
        viewModel.ShopID = Integer.parseInt(getIntent()
                .getStringExtra("ShopID"));
        viewModel.PhotoID = getIntent().getStringExtra("PhotoID");

        viewModel.CanvasHeight = CanvasHeight;
        viewModel.CanvasWidth = CanvasWidth;
        Gson gson = new Gson();
        String uploadString = gson.toJson(viewModel);
        return uploadString.replace("Points", "MeasurePoints");
    }

    private void showPopup(final Activity context) {
        Intent intent = new Intent(TakeMeasurement.this, MeasurementInput.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String height = data.getStringExtra("Height");
            String width = data.getStringExtra("Width");
            String unit = data.getStringExtra("Unit");
            String type = data.getStringExtra("Type");
            MeasurementView i = (MeasurementView) findViewById(R.id.imgMeasurementImage);
            i.SetObjectValues(width, height, unit, type);
            i.Connect();
        }
    }

    private void LoadMeasurementPhoto(String photoID) {
        Bitmap bitmap = BitmapFactory.decodeFile(new File(photoID).getPath());
        MeasurementView i = (MeasurementView) findViewById(R.id.imgMeasurementImage);
        i.setImageBitmap(bitmap);
        scaleImage();

    }

    private void scaleImage() {
        // Get the ImageView and its bitmap
        ImageView view = (ImageView) findViewById(R.id.imgMeasurementImage);
        Drawable drawing = view.getDrawable();
        if (drawing == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int s_height = size.y;

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = s_height - 5;// dpToPx(s_height - 50);

        // Determine how much to scale: the dimension requiring less scaling
        // is
        // closer to the its side. This way the image always stays inside
        // your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the
        // ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                .getLayoutParams();
        params.width = CanvasWidth = width;
        params.height = CanvasHeight = height;
        view.setLayoutParams(params);

    }

    public class MeasurementViewModel {
        public List<MeasurementObject> PhotoObjectList;
        public int ShopID;
        public int TaskID;
        public String PhotoID;

        public int CanvasWidth;
        public int CanvasHeight;

    }
}
