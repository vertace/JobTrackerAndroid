package com.tt.jobtracker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tt.adapters.PendingMeasurementListAdapter;
import com.tt.data.MeasurementPhoto;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AppConstant;
import com.tt.helpers.AsycResponse;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.PhotoDeleteHelper;
import com.tt.helpers.Utility;


public class BackgroundService extends Service {


    public static BackgroundService backgroundService;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    final Handler handler = new Handler();
    Timer timer = new Timer();
    int count;
    String taskDelete;
    String shopPhotoUploaded;
    PendingMeasurementListAdapter pendingMeasurementListAdapter;
    int Taskid;
    String[] photopath={};
    @Override
    public void onCreate() {

        backgroundService = this;
        //check wifi is on in mobile
        SharedPreferences WifionSatusInMobile = getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);
        final SharedPreferences uploadonWifibynuser = getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String status;

        if (mWifi.isConnected()) {

            SharedPreferences.Editor editor = WifionSatusInMobile.edit();
            editor.putString("status", "true"); // Storing string
            editor.commit(); // commit changes
        }
        else {
            SharedPreferences.Editor editor = WifionSatusInMobile.edit();
            editor.putString("status", "false"); // Storing string
            editor.commit();
        }

    }
    private void uploadasych() {
        uploadMultipleImage obj = new uploadMultipleImage(this);
        obj.execute();
    }

    private void UploadMeasurementPhoto()
    {
        // PendingMeasurementUpload objx = new PendingMeasurementUpload();
        //objx.ShowPendingList();
        Intent intent;
        intent = new Intent(this, PendingMeasurementUpload.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        String condition = " EmployeeID = " + String.valueOf(Shared.LoggedInUser.ID + " AND TaskRequest.IsDone = 1");
                        List<TaskViewModel> taskViewModel = dbHelper.getPendingTasks(condition);
                        if (taskViewModel.size() > 0) {
                            uploadasych();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000 * 300);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        ProcessUpload();
        return null;
    }
    public interface OnTaskCompleted{
        void onTaskCompleted(String values);

    }

    private void ProcessUpload() {

        Toast.makeText(getApplicationContext(), "Process Upload", Toast.LENGTH_SHORT).show();

    }
    private void ShopPhotoUploadProcess(TaskViewModel tvm)
    {
        Shared.UploadShopPhoto=tvm;
        uploadMultipleImage objt = new uploadMultipleImage(this);
        objt.UploadShopPhoto();
        final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);
        final SharedPreferences IshopUloadResponse = getApplicationContext().getSharedPreferences(Shared.ShopUploadResponse, 0);
        String result= uploadResponse.getString("uploadResponse", null);
        if(result=="True") {
            SharedPreferences.Editor editor = uploadResponse.edit();
            editor.putString("uploadResponse", null); // Storing string
            editor.commit();
            SharedPreferences.Editor editor1 = IshopUloadResponse.edit();
            editor1.putString("shopUploadResponse", "true"); // Storing string
            editor1.commit();
            PhotoDeleteHelper.DeletePhoto(String.valueOf(tvm.PhotoID));
            PhotoDeleteHelper.DeletePhoto(String.valueOf(tvm.PhotoID));

        }

    }

    public void UploadMeasurementImage() {

        ArrayList<MeasurementPhoto> pendingList = LoadAllPendingUploads();
        if (pendingList.size() != 0) {

            UploadAllPendingItems(pendingList);

        }
    }
    private void UploadAllPendingItems(ArrayList<MeasurementPhoto> pendingList) {
        final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);
        try {
            for (MeasurementPhoto taskLineItem : pendingList) {
                Shared.MeasurementUploadPhoto = taskLineItem;
                uploadMultipleImage obj = new uploadMultipleImage(this);
                obj.uploadMeasurmentFile();
                //  dbHelper.DeleteRelatedJobPhoto(String.valueOf(taskLineItem.ID));
                String result= uploadResponse.getString("uploadResponse", null);
                if(result=="True")
                {
                    SharedPreferences.Editor editor = uploadResponse.edit();
                    editor.putString("uploadResponse", null); // Storing string
                    editor.commit();
                    PhotoDeleteHelper.DeletePhoto(String.valueOf(taskLineItem.PhotoID));
                    dbHelper.deleteMeasurementPhoto(String.valueOf(taskLineItem.ID));
                    dbHelper.deleteTaskLineItem(String.valueOf(taskLineItem.TaskID));
                    //String id=dbHelper.deleteTaskLineItem(String.valueOf(taskLineItem.TaskID));
                    dbHelper.deleteTask(String.valueOf(taskLineItem.TaskID));
                }
            }
            //dbHelper.deleteTaskLineItem(String.valueOf(pendingList.indexOf(0)));
        } catch (Exception e) {
        }
    }

    ArrayList<MeasurementPhoto> LoadAllPendingUploads() {


        pendingMeasurementListAdapter = new PendingMeasurementListAdapter(this,
                R.layout.row_tasklineitem);



        // listView.setAdapter(pendingMeasurementListAdapter);
        ArrayList<MeasurementPhoto> pendingList = dbHelper
                .getMeasurementPhotos(" MeasurementString not null");
        pendingMeasurementListAdapter.addAll(pendingList);


        return pendingList;
    }


    class uploadMultipleImage extends AsyncTask<String, Integer, ServerResult> {
        Context context;
        private OnTaskCompleted listener;
        public ArrayList<TaskLineItemPhotoViewModel> imageList;
        final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);

        public uploadMultipleImage(Context _context) {
            context = _context;

        }


        public uploadMultipleImage(AsyncTask<Void, Void, String> asyncTask) {
            // TODO Auto-generated constructor stub
        }

        public void ImageUploadProcess()
        {
            final SharedPreferences IshopUloadResponse = getApplicationContext().getSharedPreferences(Shared.ShopUploadResponse, 0);
            String condition = " EmployeeID = " + String.valueOf(Shared.LoggedInUser.ID + " AND TaskRequest.IsDone = 1");
            List<TaskViewModel> taskViewModel = dbHelper.getPendingTasks(condition);
            for (TaskViewModel t : taskViewModel)
            {
                if(t.IsShopPhoto==true && t.PhotoID!=null)
                {

                    ShopPhotoUploadProcess(t);
                }
                Taskid=t.ID;
                List<TaskLineItemViewModel> tasklineitems = dbHelper.getTaskLineItems("TaskID=" + String.valueOf(t.ID));
                for (TaskLineItemViewModel tl : tasklineitems) {
                    if(t.IsMeasurement==true) {
                        UploadMeasurementImage();
                    }
                    ArrayList<TaskLineItemPhotoViewModel> tdl = dbHelper.getAllTaskLineItemPhotos(String.valueOf(tl.ID));
                    for (TaskLineItemPhotoViewModel tlp : tdl)
                    {
                        Shared.SelecteduploadTasklineitemPhotos = tlp;
                      // String res= GetHaskKey(tlp.PhotoID);
                        UploadExecutionImage(tlp);

                        String result= uploadResponse.getString("uploadResponse", null);
                        shopPhotoUploaded=IshopUloadResponse.getString("shopUploadResponse",null);
                        taskDelete=result;
                        if(result=="True")
                        {
                            SharedPreferences.Editor editor = uploadResponse.edit();
                            editor.putString("uploadResponse", "False");


                            editor.commit();

                            PhotoDeleteHelper.DeletePhoto(String.valueOf(tlp.PhotoID));
                            dbHelper.deleteTaskLineItemPhoto(String.valueOf(tlp.ID));
                        }
                    }
                    imageList = dbHelper.getAllTaskLineItemPhotos(String.valueOf(tl.ID));
                    count=imageList.size();
                    if(count==0 && taskDelete=="True")
                    {
                        dbHelper.deleteTaskLineItem(String.valueOf(tl.ID));
                    }
                }
                String Condition="TaskID=" + String.valueOf(t.ID);
                List<TaskLineItemViewModel> tasklineitems1 = dbHelper.getTaskLineItems(Condition);
                int lineItemCount=tasklineitems.size();
                if(lineItemCount==1 && count==0 && taskDelete=="True") {
                    if(t.IsShopPhoto && shopPhotoUploaded=="true") {
                        taskDelete = "False";
                        dbHelper.deleteTask(String.valueOf(t.ID));
                    }
                    else
                    {
                        taskDelete = "False";
                        dbHelper.deleteTask(String.valueOf(t.ID));
                    }
                }
                else
                {
                    int taskLineItemCount=tasklineitems1.size();
                    if(taskLineItemCount==0)
                    {
                        if(t.IsShopPhoto && shopPhotoUploaded=="true") {
                            dbHelper.deleteTask(String.valueOf(t.ID));
                        }
                        else
                        {
                            dbHelper.deleteTask(String.valueOf(t.ID));
                        }
                    }
                }
                SharedPreferences.Editor editor = IshopUloadResponse.edit();
                editor.putString("shopUploadResponse", null); // Storing string
                editor.commit();
            }
        }
        private String GetHaskKey(String photopath)
        {
            String path = photopath;
            File f=new File(photopath);
            if(f.exists()) {
                File file = new File(path);
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Not Found",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Exception",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                String hash = "";
                MessageDigest m;
                try {
                    m = MessageDigest.getInstance("MD5");
                    byte[] digest = m.digest(bytes);
                    hash = new BigInteger(1, digest).toString(16);

                    //lstTasks.setText(hash);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }/* catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }*/
                return hash;
            }
            return "null";
        }

        protected ServerResult doInBackground(String... params) {

            SharedPreferences WifionSatusInMobile = getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);
            final SharedPreferences uploadonWifibynuser = getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
            String WifiOnStatusInMobile= WifionSatusInMobile.getString("status", null); // getting String
            String UserOnWifiStatus= uploadonWifibynuser.getString("status", null); // getting String
            if (UserOnWifiStatus == "true") {
                if (WifiOnStatusInMobile == "true") {
                    ImageUploadProcess();
                }
            }
            else
            {
                ImageUploadProcess();
            }
            taskDelete="False";
            return ServerResult.UploadSuccess;
        }

        //myChanges*************************************************************
        public String uploadMeasurmentFile() {
            MeasurementPhoto measurementPhoto=Shared.MeasurementUploadPhoto;
            if (measurementPhoto.Lat == null) {
                measurementPhoto.Lat = String.valueOf(Shared.lat);
                measurementPhoto.Lon = String.valueOf(Shared.lon);
            }

            String fileName = new File(measurementPhoto.PhotoID).getName();
            StringBuilder sbResult = new StringBuilder();
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(measurementPhoto.PhotoID);

            if (!sourceFile.isFile()) {

                Log.e("uploadFile", "Source File not exist :");
                return "-1";

            } else {
                int serverResponseCode = 0;
                try {
                    List<NameValuePair> measurementUploadRequest = new ArrayList<NameValuePair>(
                            5);
                    //measurementPhoto.MeasurementString="{\"PhotoObjectList\":[{\"Height\":\"4\",\"MeasurePoints\":[{\"x\":85,\"y\":123},{\"x\":97,\"y\":124},{\"x\":114,\"y\":124},{\"x\":128,\"y\":124}],\"Type\":\"Nonlit\",\"Unit\":\"feet\",\"Width\":\"8\"}],\"PhotoID\":\"/storage/emulated/0/Pictures/SSTracker/IMG_20140308_132400.jpg\",\"CanvasWidth\":475,\"ShopID\":4338,\"TaskID\":6847,\"CanvasHeight\":285}";
                    measurementUploadRequest.add(new BasicNameValuePair("EmployeeID", String.valueOf(Shared.LoggedInUser.ID)));
                    measurementUploadRequest.add(new BasicNameValuePair("TaskID",
                            String.valueOf(measurementPhoto.TaskID)));
                    measurementUploadRequest.add(new BasicNameValuePair("Lat",
                            measurementPhoto.Lat));
                    measurementUploadRequest.add(new BasicNameValuePair("Lon",
                            measurementPhoto.Lon));
                    measurementUploadRequest.add(new BasicNameValuePair("MID",
                            String.valueOf(measurementPhoto.ID)));
                    measurementUploadRequest.add(new BasicNameValuePair("q",
                            measurementPhoto.MeasurementString));
                    measurementUploadRequest.add(new BasicNameValuePair("PhotoID",
                            measurementPhoto.PhotoID));
                    measurementUploadRequest.add(new BasicNameValuePair("Time",
                            measurementPhoto.Time));
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    URL url = new URL(Shared.SaveMeasurementAPI + "?"
                            + Utility.getQuery(measurementUploadRequest));

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);

                    conn.setRequestProperty("uploaded_file",
                            Shared.GetLocationString() + fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();

                    if (serverResponseCode == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String inputLine = "";

                        while ((inputLine = in.readLine()) != null)
                            sbResult.append(inputLine);
                        in.close();
                    }
                    responseMessage=sbResult.toString();
                    // close the streams //
                    // writer.close();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    String values[]=responseMessage.split(",");
                  //  String LocalCheckSum=GetHaskKey(measurementPhoto.PhotoID);
                    final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);
                    SharedPreferences.Editor editor = uploadResponse.edit();
                    if(Integer.parseInt(values[1])==measurementPhoto.TaskID)
                    {
                        editor.putString("uploadResponse", "True");
                    }
                    else
                    {
                        editor.putString("uploadResponse", "False");
                    }
                    editor.commit();
                    //return ServerResult.UploadSuccess;

                } catch (MalformedURLException ex) {
                    return "-2";
                } catch (Exception e) {
                    return "-3";
                }
                try {
                    return sbResult.toString();//Integer.parseInt(sb.toString());
                } catch (NumberFormatException nex) {
                    return "-1";
                }

            } // End else block
        }
        //**********************************************************************


        protected void onPostExecute(String sResponse) {
            listener.onTaskCompleted(sResponse);
            try {
                if (sResponse.startsWith("TASKID:")) {
                    String taskId = sResponse.replace("TASKID:", "");
                    dbHelper.shopPhotoUploaded(taskId);
                } else if (sResponse.startsWith("MISSING_SHOP_PHOTO:")) {
                    String taskId = sResponse.replace("MISSING_SHOP_PHOTO:", "");
                    dbHelper.shopPhotoUploaded(taskId);
                } else if (sResponse.startsWith("MISSING_WALL_PHOTO:")) {
                    String taskLineItemID = sResponse.replace("MISSING_WALL_PHOTO:", "");
                    dbHelper.WallPhotoMissing(taskLineItemID);
                } else {
                    dbHelper.deleteTaskLineItem(sResponse);
                }
                if (sResponse != null) {
                    if (sResponse.equals("-1") || sResponse.equals("-2"))
                    {
                        //processFinish(ServerResult.ConnectionFailed);
                    }

                    else
                    {
                        //delegate.processFinish(ServerResult.UploadSuccess);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public ServerResult UploadExecutionImage(TaskLineItemPhotoViewModel tlp) {
            TaskLineItemPhotoViewModel tasklineItemPhotos=Shared.SelecteduploadTasklineitemPhotos;
            // String path = taskLineItem.PhotoID;

            String fileName = new File(tasklineItemPhotos.PhotoID).getName();
            StringBuilder sbResult = new StringBuilder();
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(tasklineItemPhotos.PhotoID);
            int serverResponseCode = 0;
            final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);
            if(tasklineItemPhotos.PhotoID.equals("NOT_DONE"))
            {
                ArrayList<NameValuePair> taskDetailRequest = new ArrayList<NameValuePair>(
                        5);

                taskDetailRequest.add(new BasicNameValuePair("TaskLineItemID", String.valueOf(tasklineItemPhotos.TaskLineItemID)));
                taskDetailRequest.add(new BasicNameValuePair("Lat", tasklineItemPhotos.Lat));
                taskDetailRequest.add(new BasicNameValuePair("Lon", tasklineItemPhotos.Lon));
                taskDetailRequest.add(new BasicNameValuePair("Time", tasklineItemPhotos.Time));
                taskDetailRequest.add(new BasicNameValuePair("Employee", Shared.LoggedInUser.ID));
                taskDetailRequest.add(new BasicNameValuePair("NotDoneReason", tasklineItemPhotos.NotDoneReason));
                try {
                    String result = CustomHttpClient.executeHttpPost(
                            Shared.UploadAPI, taskDetailRequest);
                    sbResult.append(result);
                    String value=result.replace("\n", "").replace("\r", "");
                        SharedPreferences.Editor editor = uploadResponse.edit();
                    if(Integer.parseInt(value)==tasklineItemPhotos.TaskLineItemID)
                    {
                        editor.putString("uploadResponse", "True");

                    }
                    else {
                        editor.putString("uploadResponse", "False");
                    }
                    editor.commit();

                } catch (MalformedURLException ex) {
                    return ServerResult.ConnectionFailed;
                } catch (Exception e) {
                   return  ServerResult.ConnectionFailed;
                }
            }
            else
            {
                try {


                    ArrayList<NameValuePair> taskLineItemPhotos1 = new ArrayList<NameValuePair>(
                            5);

                    taskLineItemPhotos1.add(new BasicNameValuePair("TaskLineItemID", String.valueOf(tasklineItemPhotos.TaskLineItemID)));
                    taskLineItemPhotos1.add(new BasicNameValuePair("Lat", tasklineItemPhotos.Lat));
                    taskLineItemPhotos1.add(new BasicNameValuePair("Lon", tasklineItemPhotos.Lon));
                    taskLineItemPhotos1.add(new BasicNameValuePair("Time", tasklineItemPhotos.Time));
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    URL url = new URL(Shared.UploadAPI + "?" + Utility.getQuery(taskLineItemPhotos1));

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);

                    conn.setRequestProperty("uploaded_file",
                            Shared.GetLocationString() + fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();
                    if (serverResponseCode == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String inputLine = "";

                        while ((inputLine = in.readLine()) != null)
                            sbResult.append(inputLine);
                        in.close();

                    }
                    responseMessage = sbResult.toString();


                    // close the streams //
                    // writer.close();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                    //Check md5 checksum
                    String values[] = responseMessage.split(",");
                    String LocalCheckSum = GetHaskKey(tlp.PhotoID);

                    SharedPreferences.Editor editor = uploadResponse.edit();
                    if (values[1].equals(LocalCheckSum)) {

                        editor.putString("uploadResponse", "True");

                    } else {
                        editor.putString("uploadResponse", "False");
                    }
                    editor.commit();
                    // Storing string

                    return ServerResult.UploadSuccess;


                } catch (MalformedURLException ex) {

                    ex.printStackTrace();

                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    return ServerResult.ConnectionFailed;
                } catch (Exception e) {

                    e.printStackTrace();

                    Log.e("Upload file Exception",
                            "Exception : " + e.getMessage(), e);
                    return ServerResult.ConnectionFailed;
                }
                // String result=sbResult.toString();
            }
            return ServerResult.UploadSuccess;

        }


        private String UploadShopPhoto()
        {
            TaskViewModel taskViewModel=Shared.UploadShopPhoto;
            // String path = taskLineItem.PhotoID;
            String fileName = new File(taskViewModel.PhotoID).getName();
            StringBuilder sbResult = new StringBuilder();
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1*1024*1024;
            File sourceFile = new File(taskViewModel.PhotoID);

            if (!sourceFile.isFile() && !taskViewModel.PhotoID.equals("NOT_DONE"))
            {

                Log.e("uploadFile", "Source File not exist :");

                return "MISSING_SHOP_PHOTO:" + Integer.toString(taskViewModel.ID);

            }
            else if (sourceFile.isFile())
            {
                int serverResponseCode = 0;
                try {

                    List<NameValuePair> taskDetailRequest = new ArrayList<NameValuePair>(
                            5);
                    taskDetailRequest.add(new BasicNameValuePair("TaskID", String
                            .valueOf(taskViewModel.ID)));
                    taskDetailRequest.add(new BasicNameValuePair("Time",
                            taskViewModel.StartTime));
                    taskDetailRequest.add(new BasicNameValuePair("Employee",
                            Shared.LoggedInUser.ID));
                    taskDetailRequest.add(new BasicNameValuePair("ActualLat",
                           String.valueOf(taskViewModel.ActualLat)));
                    taskDetailRequest.add(new BasicNameValuePair("ActualLon",
                            String.valueOf(taskViewModel.ActualLon)));

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    URL url = new URL(Shared.UploadShopPhotoAPI + "?"
                            + Utility.getQuery(taskDetailRequest));

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);

                    conn.setRequestProperty("uploaded_file",Shared.GetLocationString() + fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0)
                    {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();
                   if (serverResponseCode == 200)
                    {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String inputLine = "";

                        while ((inputLine = in.readLine()) != null)
                            sbResult.append(inputLine);
                        in.close();

                    }
                    responseMessage=sbResult.toString();
                    String resultFromServer=sbResult.toString();
                    // close the streams //
                    // writer.close();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    String values[]=responseMessage.split(",");
                    String LocalCheckSum=GetHaskKey(taskViewModel.PhotoID);
                    final SharedPreferences uploadResponse = getApplicationContext().getSharedPreferences(Shared.UploadResponse, 0);
                    SharedPreferences.Editor editor = uploadResponse.edit();
                    if(values[1].equals(LocalCheckSum))
                    {

                        editor.putString("uploadResponse", "True");

                    }
                    else
                    {
                        editor.putString("uploadResponse", "False");
                    }
                    editor.commit();


                } catch (MalformedURLException ex) {

                    ex.printStackTrace();

                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    return "-1";
                } catch (Exception e) {

                    e.printStackTrace();

                    Log.e("Upload file ","Exception : " + e.getMessage(), e);
                    return "-2";
                }

            } // End else block

            return "TASKID:" + sbResult.toString();
        }

    }



}



