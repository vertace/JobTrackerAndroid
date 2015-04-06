package com.tt.jobtracker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tt.data.MeasurementPhoto;
import com.tt.data.Shared;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;
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

class MeasurementImageUploadAsync extends
        AsyncTask<MeasurementPhoto, Void, String> {

    DatabaseHelper dbHelper;
    Context context;
    AsyncResponse delegate;

    public MeasurementImageUploadAsync(Context _context, AsyncResponse _delegate) {
        this.context = _context;
        this.delegate = _delegate;
        dbHelper = new DatabaseHelper(_context);
    }

    @Override
    protected String doInBackground(MeasurementPhoto... params) {
        try {
            return String.valueOf(uploadFile(params[0]));
        } catch (Exception e) {
            String msg = e.getMessage();

            Log.e(e.getClass().getName(), e.getMessage(), e);
            return null;
        }

    }

    @Override
    protected void onProgressUpdate(Void... unsued) {

    }

    @Override
    protected void onPostExecute(String sResponse) {
        dbHelper.deleteMeasurementPhoto(sResponse);
        if (delegate != null) {
            if (sResponse == null || sResponse.equals("-1")
                    || sResponse.equals("-2"))
                delegate.processFinish(ServerResult.ConnectionFailed);
            else
                delegate.processFinish(ServerResult.UploadSuccess);
        }
    }

    @SuppressWarnings("deprecation")
    public String uploadFile(MeasurementPhoto measurementPhoto) {

        if (measurementPhoto.Lat == null) {
            measurementPhoto.Lat = String.valueOf(Shared.lat);
            measurementPhoto.Lon = String.valueOf(Shared.lon);
        }
        String fileName = new File(measurementPhoto.PhotoID).getName();
        StringBuilder sb = new StringBuilder();
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
                        sb.append(inputLine);
                    in.close();
                }

                // close the streams //
                // writer.close();
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                return "-2";
            } catch (Exception e) {
                return "-3";
            }
            try {
                return sb.toString();//Integer.parseInt(sb.toString());
            } catch (NumberFormatException nex) {
                return "-1";
            }

        } // End else block
    }
}