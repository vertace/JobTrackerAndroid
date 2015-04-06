package com.tt.jobtracker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tt.data.Shared;
import com.tt.data.TaskLineItemViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageUploadAsync extends
        AsyncTask<TaskLineItemViewModel, Void, String> {
    DatabaseHelper dbHelper;
    Context context;
    AsyncResponse delegate;

    public ImageUploadAsync(Context _context, AsyncResponse _delegate) {
        this.context = _context;
        this.delegate = _delegate;
        dbHelper = new DatabaseHelper(_context);
    }

    @Override
    protected String doInBackground(TaskLineItemViewModel... params) {
        try {

            TaskLineItemViewModel taskLineItem = params[0];
            if (taskLineItem.NewImage.equals("")) {
                return UploadShopPhoto(taskLineItem, taskLineItem.PhotoID);

            } else {
                if (taskLineItem.SaveToDB) {
                    saveTaskLineItemToDatabase(taskLineItem);
                }
                if (taskLineItem.OldTakenNow) {
                    uploadFile(taskLineItem, taskLineItem.OldImage);
                    return uploadFile(taskLineItem, taskLineItem.NewImage);
                } else {
                    return uploadFile(taskLineItem, taskLineItem.NewImage);
                }
            }
        } catch (Exception e) {

            Log.e(e.getClass().getName(), e.getMessage(), e);
            return null;
        }

    }

    @Override
    protected void onProgressUpdate(Void... unsued) {

    }

    @Override
    protected void onPostExecute(String sResponse) {
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
            if (delegate != null) {
                if (sResponse.equals("-1") || sResponse.equals("-2"))
                    delegate.processFinish(ServerResult.ConnectionFailed);
                else
                    delegate.processFinish(ServerResult.UploadSuccess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String UploadShopPhoto(TaskLineItemViewModel taskLineItem, String path) {
        // String path = taskLineItem.PhotoID;
        String fileName = new File(path).getName();
        StringBuilder sbResult = new StringBuilder();
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(path);

        if (!sourceFile.isFile() && !path.equals("NOT_DONE")) {

            Log.e("uploadFile", "Source File not exist :");

            return "MISSING_SHOP_PHOTO:" + Integer.toString(taskLineItem.ID);

        } else {
            if (sourceFile.isFile()) {
                int serverResponseCode = 0;
                try {

                    List<NameValuePair> taskDetailRequest = new ArrayList<>(
                            5);
                    taskDetailRequest.add(new BasicNameValuePair("TaskID", String
                            .valueOf(taskLineItem.ID)));
                    taskDetailRequest.add(new BasicNameValuePair("Time",
                            taskLineItem.Time));
                    taskDetailRequest.add(new BasicNameValuePair("Employee",
                            Shared.LoggedInUser.ID));

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

                    conn.setRequestProperty("uploaded_file", Shared.GetLocationString() + fileName);

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
                    if (serverResponseCode == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null)
                            sbResult.append(inputLine);
                        in.close();

                    }

                    // close the streams //
                    // writer.close();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {

                    ex.printStackTrace();

                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    return "-1";
                } catch (Exception e) {

                    e.printStackTrace();

                    Log.e("Upload to server", "Exception : " + e.getMessage(), e);
                    return "-2";
                }

            } // End else block
        }

        return "TASKID:" + sbResult.toString();
    }

    public String uploadFile(TaskLineItemViewModel taskLineItem, String path) {

        // String path = taskLineItem.PhotoID;
        String fileName = new File(path).getName();
        StringBuilder sbResult = new StringBuilder();
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(path);

        if (!sourceFile.isFile() && !path.equals("NOT_DONE")) {

            Log.e("uploadFile", "Source File not exist :");
            return "MISSING_WALL_PHOTO:" + Integer.toString(taskLineItem.ID);

        } else if (path.equals("NOT_DONE")) {
            ArrayList<NameValuePair> taskDetailRequest = new ArrayList<NameValuePair>(
                    5);
            taskDetailRequest.add(new BasicNameValuePair("TaskLineItemID",
                    String.valueOf(taskLineItem.ID)));
            taskDetailRequest.add(new BasicNameValuePair("Lat",
                    taskLineItem.Lat));
            taskDetailRequest.add(new BasicNameValuePair("Lon",
                    taskLineItem.Lon));
            taskDetailRequest.add(new BasicNameValuePair("Time",
                    taskLineItem.Time));
            taskDetailRequest.add(new BasicNameValuePair("Employee",
                    Shared.LoggedInUser.ID));
            taskDetailRequest.add(new BasicNameValuePair("NotDoneReason",
                    taskLineItem.NotDoneReason));

            try {
                String result = CustomHttpClient.executeHttpPost(
                        Shared.UploadAPI, taskDetailRequest);
                sbResult.append(result);
            } catch (MalformedURLException ex) {
                return "-1";
            } catch (Exception e) {
                return "-2";
            }

        } else if (sourceFile.isFile()) {
            int serverResponseCode = 0;
            try {

                List<NameValuePair> taskDetailRequest = new ArrayList<NameValuePair>(
                        5);
                taskDetailRequest.add(new BasicNameValuePair("TaskLineItemID",
                        String.valueOf(taskLineItem.ID)));
                taskDetailRequest.add(new BasicNameValuePair("Lat",
                        taskLineItem.Lat));
                taskDetailRequest.add(new BasicNameValuePair("Lon",
                        taskLineItem.Lon));
                taskDetailRequest.add(new BasicNameValuePair("Time",
                        taskLineItem.Time));
                taskDetailRequest.add(new BasicNameValuePair("Employee",
                        Shared.LoggedInUser.ID));

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(
                        sourceFile);
                URL url = new URL(Shared.UploadAPI + "?"
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
                if (serverResponseCode == 200) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String inputLine = "";

                    while ((inputLine = in.readLine()) != null)
                        sbResult.append(inputLine);
                    in.close();

                }

                // close the streams //
                // writer.close();
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                return "-1";
            } catch (Exception e) {

                e.printStackTrace();

                Log.e("Upload to Server",
                        "Exception : " + e.getMessage(), e);
                return "-2";
            }

        } // End else block
        return sbResult.toString();
    }

    private void saveTaskLineItemToDatabase(TaskLineItemViewModel taskLineItem) {
        dbHelper.deleteTaskLineItem(String.valueOf(taskLineItem.ID));
        dbHelper.insertTaskLineItem(taskLineItem);
    }

}
