package com.tt.jobtracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTabHost;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tt.data.TaskViewModel;
import com.tt.fragments.TaskLineItemFragment;
import com.tt.jobtracker.R;
import com.tt.adapters.MeasurementPhotoListAdapter;
import com.tt.data.MeasurementPhoto;
import com.tt.data.Shared;
import com.tt.helpers.CameraHelper;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.ScaleView;
import com.tt.helpers.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import com.sst.sstracker.TaskDetail.MyLocationListener;

public class TakeMeasurementList extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    ListView listView;
    String TaskID;
    String IsDoneMenuHide;
    String ShopID, ShopName, ShopAddress;
    LocationManager mlocManager;
    LocationListener mlocListener;
    private FragmentTabHost mTabHost;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private Uri fileUri;
    String stringLongitude;
    String stringLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurementphotolist);
        LoadPhotos();
    /*    if(IsDoneMenuHide=="true") {

            ActionBar actionBar = getActionBar();
            actionBar.isShowing();
        }*/
        // actionBar.setDisplayShowTitleEnabled(false);
    }

    public void LoadPhotos() {
        try {
            Intent intent = getIntent();
            TaskID = intent.getStringExtra("TaskID");
            ShopID = intent.getStringExtra("ShopID");
            ShopName = intent.getStringExtra("ShopName");
            ShopAddress = intent.getStringExtra("ShopAddress");
            IsDoneMenuHide=intent.getStringExtra("IsDone");
            ArrayList<MeasurementPhoto> photoList = dbHelper
                    .getMeasurementPhotos(" TaskID='" + TaskID + "'");
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(TaskID));
            MeasurementPhotoListAdapter measurementAdapter = new MeasurementPhotoListAdapter(
                    this, R.layout.measurementphoto_row, size);

            listView = (ListView) findViewById(R.id.lstPhoto);

            listView.setAdapter(measurementAdapter);

            measurementAdapter.addAll(photoList);

        } catch (Exception e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("MyException Occured");
            dialog.setMessage(e.getMessage());
            dialog.setNeutralButton("Cool", null);
            dialog.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is
            // present.
            if(IsDoneMenuHide.equals("true"))
            {

            }else {
                getMenuInflater().inflate(R.menu.taskdetailmenu, menu);
                final TaskViewModel taskviewmeasurement=Shared.SelectedTask;
                MeasurementPhoto measurementPhoto = dbHelper.getMeasurementPhotoValidate(String.valueOf(taskviewmeasurement.ID));
                if(measurementPhoto.PhotoID!=null) {
                    MenuItem item = menu.findItem(R.id.mnuTaskList);
                    item.setVisible(true);
                    this.invalidateOptionsMenu();
                }
                else {
                    MenuItem item = menu.findItem(R.id.mnuTaskList);
                    item.setVisible(false);
                    this.invalidateOptionsMenu();
                }

            }
            return true;
        } catch (Exception e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("MyException Occured");
            dialog.setMessage(e.getMessage());
            dialog.setNeutralButton("Cool", null);
            dialog.create().show();
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.mnuTakePhoto:
                TakePhoto();
                break;
            case R.id.mnuTaskList:
                Moveto_Done();
                break;
            case R.id.mnuCheckin:
                Intent intent = new Intent(TakeMeasurementList.this, MapForSingleShop.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
    private void Moveto_Done()
    {
        final TaskViewModel taskviewmeasurement=Shared.SelectedTask;
        final SharedPreferences mainClassCall = getSharedPreferences(Shared.MainClassCall, 0);
        taskviewmeasurement.IsDone = true;
        // MainActivity mainActivity = (MainActivity) TakeMeasurementList;
        MeasurementPhoto measurementPhoto = dbHelper.getMeasurementPhotoValidate(String.valueOf(taskviewmeasurement.ID));
        if(measurementPhoto.PhotoID!=null)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Alert")
                    .setMessage("Are you sure to mark this as complete?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = mainClassCall.edit();
                            editor.putString("mainClassCall", "True"); // Storing string
                            editor.commit();
                            Toast.makeText(getApplicationContext(), " Move to Done list", Toast.LENGTH_LONG).show();
                            //TaskViewModel taskViewModel = Shared.SelectedTask;
                            taskviewmeasurement.IsDone = true;
                            dbHelper.saveTask(taskviewmeasurement, true);
                            Intent intent = new Intent(TakeMeasurementList.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences.Editor editor = mainClassCall.edit();
                            editor.putString("mainClassCall", "True"); // Storing string
                            editor.commit();
                            Intent intent = new Intent(TakeMeasurementList.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                            finish();
                        }
                    })                        //Do nothing on no
                    .show();

        }
        else
        {
            Toast.makeText(this, " Take Minimum one Measurement Photo", Toast.LENGTH_LONG).show();
        }

    }
    private void TakePhoto() {

        // create Intent to take a picture and return control to the
        // calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = CameraHelper
                .getOutputMediaFileUri(CameraHelper.MEDIA_TYPE_IMAGE); // create
        // a
        // file
        // to
        // save
        // the
        // image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the
        // image
        // file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    if (data != null) { // if the intent data is not null, use
                        // it
                        Toast.makeText(this,
                                "Image saved to:\n" + data.getData(),
                                Toast.LENGTH_LONG).show();

                    } else { // Use the fileUri global variable
                        Toast.makeText(this, "Image saved to:\n" + fileUri,
                                Toast.LENGTH_LONG).show();
                    }
                    GPSTracker gpsTracker = new GPSTracker(this);
                    if (gpsTracker.getIsGPSTrackingEnabled()) {
                        stringLatitude = String.valueOf(gpsTracker.latitude);
                        stringLongitude = String.valueOf(gpsTracker.longitude);
                    }
                    MeasurementPhoto measurementPhoto = new MeasurementPhoto();
                    //	Utility.getLocation(this);
                    measurementPhoto.Lat = String.valueOf(Shared.lat);
                    measurementPhoto.Lon = String.valueOf(Shared.lon);
                    measurementPhoto.PhotoID = fileUri.getPath();
                    measurementPhoto.TaskID = Integer.parseInt(TaskID);
                    measurementPhoto.ShopID = ShopID;
                    measurementPhoto.ShopName = ShopName;
                    measurementPhoto.ShopAddress = ShopAddress;
                    measurementPhoto.Lat=stringLatitude;
                    measurementPhoto.Lon=stringLongitude;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    measurementPhoto.Time = sdf.format(new Date());
                    // dbHelper.deleteMeasurementPhoto("0");


                    long MID = dbHelper.saveMeasurementPhoto(measurementPhoto,
                            true);
                    Shared.sharedCheckinID = MID;

                    finish();
                    Intent intent = new Intent(TakeMeasurementList.this,
                            TakeMeasurement.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("PhotoID", fileUri.getPath());
                    intent.putExtra("TaskID", TaskID);
                    intent.putExtra("ShopID", ShopID);
                    intent.putExtra("MID", String.valueOf(MID));
                    intent.putExtra("ShopName", ShopName);
                    intent.putExtra("IsDone",
                            String.valueOf(IsDoneMenuHide));
                    intent.putExtra("ShopAddress", ShopAddress);
                    getApplicationContext().startActivity(intent);

                    // startActivity(getIntent());
                    // MeasurementImageUploadAsync(getApplicationContext()).execute(measurementPhoto);

                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_LONG)
                            .show();
                } else {
                    // Image capture failed, advise user
                }
            }
        } catch (Exception e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("MyException Occured");
            dialog.setMessage(e.getMessage());
            dialog.setNeutralButton("Cool", null);
            dialog.create().show();
        }
    }

    public void ViewClickedImage(View v) {
        Intent intent = new Intent(TakeMeasurementList.this,
                TakeMeasurement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("TaskID", TaskID);
        intent.putExtra("IsDone",
                String.valueOf(IsDoneMenuHide));
        ScaleView sv = (ScaleView) v;
        intent.putExtra("PhotoID", sv.PhotoID);
        intent.putExtra("ShopID", sv.ShopID);
        intent.putExtra("MID", sv.MID);
        getApplicationContext().startActivity(intent);
    }


    public class MyMeasurementLocationListener implements LocationListener {
        Context contextReference;
        LocationManager mlocManagerr;
        int classID;

        public MyMeasurementLocationListener(final Context contextReference, LocationManager mlocManager, int classID) {
            this.contextReference = contextReference;
            this.mlocManagerr = mlocManager;
            this.classID = classID;
        }

        @Override
        public void onLocationChanged(Location loc) {
            DatabaseHelper dbHelper = new DatabaseHelper(contextReference);
            Shared.lat = loc.getLatitude();
            Shared.lon = loc.getLongitude();
            mlocManager.removeUpdates(mlocListener);
            mlocManager.removeUpdates(this);
            if (classID == 0) {
                MeasurementPhoto measurementPhoto = new MeasurementPhoto();

                if (Shared.sharedCheckinID > 0) {
                    measurementPhoto.ID = (int) Shared.sharedCheckinID;
                    measurementPhoto.Lat = String.valueOf(loc.getLatitude());
                    measurementPhoto.Lon = String.valueOf(loc.getLongitude());

                    dbHelper.saveMeasurementPhoto(measurementPhoto,
                            true);
                } else {
                    measurementPhoto.Lat = String.valueOf(loc.getLatitude());
                    measurementPhoto.Lon = String.valueOf(loc.getLongitude());

                    Shared.sharedCheckinID = dbHelper.saveMeasurementPhoto(measurementPhoto,
                            true);
                }

            }


        }

        @Override
        public void onProviderDisabled(String provider) {
            // Toast.makeText( contextReference, "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //  Toast.makeText( contextReference, "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


}
