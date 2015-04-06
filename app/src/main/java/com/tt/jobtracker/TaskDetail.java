package com.tt.jobtracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sstracker.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.adapters.TaskDetailAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;
import com.tt.helpers.CameraHelper;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.helpers.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.sst.sstracker.mapactivity.MyLocationListener;

@SuppressLint("SimpleDateFormat")
public class TaskDetail extends Activity implements AsyncResponse {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_SHOP_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    ListView listView;
    GetTaskDetail taskDetailRetriever;
    int taskID;
    String notDoneReason;
    LocationManager mlocManager;
    LocationListener mlocListener;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private ProgressDialog m_ProgressDialog = null;
    private Uri fileUri;
    private TaskLineItemViewModel selectedTaskLineItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if (extras == null) {
                taskID = 0;
            } else {
                taskID = extras.getInt("TaskID");
            }
        } else {
            taskID = savedInstanceState.getInt("TaskID");
        }

        m_ProgressDialog = ProgressDialog.show(TaskDetail.this,
                "Please wait...", "Reading data from database...", true);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = "
                + String.valueOf(taskID));
        processFinish(null);
        m_ProgressDialog.dismiss();

        ListView lstTaskDetail = (ListView) findViewById(R.id.lstTaskDetail);
        registerForContextMenu(lstTaskDetail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.taskdetailmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.mnuTakePhoto:
                TakeShopPhoto();
                break;

            case R.id.mnuCheckin:

                Utility.getLocation(this);

                /** mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                 mlocListener = new MyExecutionLocationListener(this,mlocManager,1);
                 isGPSEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                 isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                 if(isGPSEnabled)
                 {
                 mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
                 }
                 else
                 {
                 mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
                 }
                 */
                break;
            case R.id.mnuTaskList:
                finish();
                // startActivity(getIntent());

                Intent intent = new Intent(TaskDetail.this, TaskList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void TakeShopPhoto() {

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
        startActivityForResult(intent, CAPTURE_SHOP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void ViewAfterImage(View v) {
        View ParentView = (View) v.getParent();
        TaskLineItemViewModel taskLineItem = (TaskLineItemViewModel) ParentView
                .getTag();
        if (taskLineItem.NewImage == null)
            return;
        String photoPath = taskLineItem.NewImage;
        showPhoto(photoPath);
    }

    public void ViewBeforeImage(View v) {
        if (Shared.SelectedTask.StartTime == null
                || Shared.SelectedTask.StartTime.isEmpty()) {
            SstAlert.Show(TaskDetail.this, "Mark Started",
                    "Take Shop Photo to Start");
            return;
        }

        View ParentView = (View) v.getParent();
        TaskLineItemViewModel taskLineItem = (TaskLineItemViewModel) ParentView
                .getTag();
        if (taskLineItem.OldImage == null)
            return;
        String photoPath = taskLineItem.OldImage;
        showPhoto(photoPath);
    }

    private void showPhoto(String photoPath) {
        // Get screen size
        Display display = this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Get target image size
        Bitmap bitmap = BitmapFactory.decodeFile(new File(photoPath).getPath());
        if (bitmap == null) {
            String sdCard = Environment.getExternalStorageDirectory().getPath()
                    + "/Pictures/SSTracker/Thumb_" + photoPath;
            FileInputStream fis;
            try {
                fis = new FileInputStream(sdCard);

                bitmap = BitmapFactory.decodeStream(fis);

                fis.close();
            } catch (IOException e) {
                return;
            }
            if (bitmap == null) {
                return;
            }
        }
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
        Context context = getBaseContext();
        // Create resized bitmap image
        BitmapDrawable resizedBitmap = new BitmapDrawable(
                context.getResources(), Bitmap.createScaledBitmap(bitmap,
                bitmapWidth, bitmapHeight, false));

        AlertDialog dialog = new AlertDialog.Builder(TaskDetail.this).create();
        LayoutInflater inflater = LayoutInflater.from(TaskDetail.this);
        dialog.setTitle("Photo");
        View view = inflater.inflate(R.layout.popup_imageview, null); // xml
        // Layout
        // file
        // for
        // imageView
        ImageView img = (ImageView) view.findViewById(R.id.imageview);
        img.setImageDrawable(resizedBitmap);
        dialog.setView(view);
        dialog.show();
    }

    @Override
    public void processFinish(ServerResult result) {
        TaskDetailAdapter taskDetailAdapter = new TaskDetailAdapter(this,
                R.layout.row_tasklineitem);

        listView = (ListView) findViewById(R.id.lstTaskDetail);

        listView.setAdapter(taskDetailAdapter);

        taskDetailAdapter.addAll(Shared.TaskDetail);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (Shared.SelectedTask.StartTime == null
                        || Shared.SelectedTask.StartTime.isEmpty()) {
                    SstAlert.Show(TaskDetail.this, "Mark Started",
                            "Take Shop Photo to Start");
                } else {
                    TaskLineItemViewModel taskLineItemViewModel = (TaskLineItemViewModel) parent
                            .getItemAtPosition(position);
                    selectedTaskLineItem = taskLineItemViewModel;

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
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set
                    // the
                    // image
                    // file
                    // name

                    // start the image capture Intent
                    startActivityForResult(intent,
                            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

        if (Shared.SelectedTask.StartTime == null
                || Shared.SelectedTask.StartTime.isEmpty()) {
            SstAlert.Show(TaskDetail.this, "Mark Started",
                    "Take Shop Photo to Start");
        } else {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.jobnotdone, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        selectedTaskLineItem = Shared.TaskDetail.get(info.position);
        notDoneReason = "";

        switch (item.getItemId()) {
            case R.id.mnuStructuralDamage:
            case R.id.mnuNoSpace:
            case R.id.mnuMaterialDamage:
            case R.id.mnuMaterialMissing:
                notDoneReason = (String) item.getTitle();
                notDoneTaskLineItem();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) { // if the intent data is not null, use it
                    Toast.makeText(this, "Image saved to:\n" + data.getData(),
                            Toast.LENGTH_LONG).show();

                } else { // Use the fileUri global variable
                    Toast.makeText(this, "Image saved to:\n" + fileUri,
                            Toast.LENGTH_LONG).show();
                }
                updateTaskLineItem();

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_LONG)
                        .show();
            } else {
                // Image capture failed, advise user
            }
        }

        if (requestCode == CAPTURE_SHOP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Image saved to:\n" + fileUri,
                        Toast.LENGTH_LONG).show();
                markTaskStart();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }

    private void markTaskStart() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Shared.SelectedTask.StartTime = sdf.format(new Date());
        Shared.SelectedTask.PhotoID = fileUri.getPath();
        dbHelper.saveTask(Shared.SelectedTask, true);
    }

    private void updateTaskLineItem() {
        selectedTaskLineItem.Uri = fileUri;
        selectedTaskLineItem.Lat = String.valueOf(Shared.lat);
        selectedTaskLineItem.Lon = String.valueOf(Shared.lon);
        selectedTaskLineItem.SaveToDB = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        selectedTaskLineItem.Time = sdf.format(new Date());

        selectedTaskLineItem.PhotoID = fileUri.getPath();
        if (selectedTaskLineItem.OldImage == null
                || selectedTaskLineItem.OldImage.isEmpty()) {
            selectedTaskLineItem.OldTakenNow = true;
            selectedTaskLineItem.OldImage = fileUri.getPath();
        } else {
            selectedTaskLineItem.NewImage = fileUri.getPath();
        }

        dbHelper.saveTaskLineItem(selectedTaskLineItem, true);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = "
                + String.valueOf(selectedTaskLineItem.TaskID));
        processFinish(null);
    }

    private void notDoneTaskLineItem() {
        selectedTaskLineItem.Uri = Uri.EMPTY;
        selectedTaskLineItem.Lat = String.valueOf(Shared.lat);
        selectedTaskLineItem.Lon = String.valueOf(Shared.lon);
        selectedTaskLineItem.SaveToDB = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        selectedTaskLineItem.Time = sdf.format(new Date());

        selectedTaskLineItem.PhotoID = "NOT_DONE";
        if (selectedTaskLineItem.OldImage == null
                || selectedTaskLineItem.OldImage.isEmpty()) {
            selectedTaskLineItem.OldTakenNow = true;
            selectedTaskLineItem.OldImage = "NOT_DONE";
        }
        selectedTaskLineItem.NewImage = "NOT_DONE";
        selectedTaskLineItem.NotDoneReason = notDoneReason;

        dbHelper.saveTaskLineItem(selectedTaskLineItem, true);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = "
                + String.valueOf(selectedTaskLineItem.TaskID));
        processFinish(null);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public class MyExecutionLocationListener implements LocationListener {
        Context contextReference;
        LocationManager mlocManagerr;
        int classID;

        public MyExecutionLocationListener(final Context contextReference, LocationManager mlocManager, int classID) {
            this.contextReference = contextReference;
            this.mlocManagerr = mlocManager;
            this.classID = classID;
        }

        @Override
        public void onLocationChanged(Location loc) {
            DatabaseHelper dbHelper = new DatabaseHelper(contextReference);
            Shared.lat = loc.getLatitude();
            Shared.lon = loc.getLongitude();


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

    public class GetTaskDetail extends
            AsyncTask<Integer, Integer, ServerResult> {

        public AsyncResponse delegate = null;

        protected ServerResult doInBackground(Integer... params) {
            int taskID = params[0];

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("TaskID", String
                    .valueOf(taskID)));

            String response = null;
            try {
                response = CustomHttpClient.executeHttpPost(
                        Shared.TaskDetailAPI, postParameters);
                String res = response.toString();

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Type listType = new TypeToken<ArrayList<TaskLineItemViewModel>>() {
                }.getType();
                List<TaskLineItemViewModel> taskDetail = gson.fromJson(res,
                        listType);

                if (taskDetail == null || taskDetail.size() == 0) {
                    return ServerResult.NoTasks;
                } else {
                    Shared.TaskDetail = taskDetail;
                    return ServerResult.TaskDetailReceived;
                }
            } catch (UnknownHostException e) {
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ServerResult result) {
            m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    SstAlert.Show(TaskDetail.this, "No Internet",
                            "No internet connection");
                    break;
                case TaskDetailReceived:
                    // SstAlert.Show(TaskDetail.this, "Tasks",
                    // Shared.TaskDetail.size() + " items received");
                    delegate.processFinish(result);

                    break;
                case NoTasks:
                    SstAlert.Show(TaskDetail.this, "No Tasks",
                            "No open tasks in your name");
                    break;
                case UnknownError:
                    SstAlert.Show(TaskDetail.this, "Unknown Error",
                            "Some error occured");
                    break;
                default:
                    break;

            }
        }
    }

}
