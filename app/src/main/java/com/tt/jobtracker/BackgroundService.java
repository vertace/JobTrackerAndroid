package com.tt.jobtracker;

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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.tt.data.Shared;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.Utility;


public class BackgroundService extends Service {


    public static BackgroundService backgroundService;
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    public void onCreate() {
        backgroundService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //check wifi is on in mobile

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String status;
        SharedPreferences WifionSatusInMobile = getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);
        final SharedPreferences uploadonWifibynuser = getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
        if (mWifi.isConnected()) {

            SharedPreferences.Editor editor = WifionSatusInMobile.edit();
            editor.putBoolean("status", true); // Storing string
            editor.commit(); // commit changes
        }
        else {
            SharedPreferences.Editor editor = WifionSatusInMobile.edit();
            editor.putBoolean("status", false); // Storing string
            editor.commit();
        }
        boolean WifiOnStatusInMobile= WifionSatusInMobile.getBoolean("status", false); // getting String
        boolean UserOnWifiStatus= uploadonWifibynuser.getBoolean("status", false); // getting String



        if(UserOnWifiStatus)
        {
            if(WifiOnStatusInMobile) {
                String condition = " EmployeeID = " + String.valueOf(Shared.LoggedInUser.ID + " AND TaskRequest.IsPending = 1");
                List<TaskViewModel> taskViewModel = dbHelper.getPendingTasks(condition);
                for (TaskViewModel t : taskViewModel) {
                    List<TaskLineItemViewModel> tasklineitems = dbHelper.getTaskLineItems("ID="+String.valueOf(t.ID));
                    for( TaskLineItemViewModel tl:tasklineitems) {
                        ArrayList<TaskLineItemPhotoViewModel> tdl = dbHelper.getAllTaskLineItemPhotos(String.valueOf(tl.ID));
                        for (TaskLineItemPhotoViewModel tlp : tdl) {
                            // UploadMultiplePhotos(s, s.PhotoID);
                            Shared.SelecteduploadTasklineitemPhotos = tlp;
                            uploadMultipleImage obj = new uploadMultipleImage(this);
                            obj.execute();
                        }
                    }
                }
            }

        }
        else
        {
            String condition = " EmployeeID = " + String.valueOf(Shared.LoggedInUser.ID + " AND TaskRequest.IsPending = 1");
            List<TaskViewModel> taskViewModel = dbHelper.getPendingTasks(condition);
            for (TaskViewModel t : taskViewModel) {
                List<TaskLineItemViewModel> tasklineitems = dbHelper.getTaskLineItems("TaskID="+String.valueOf(t.ID));
                for( TaskLineItemViewModel tl:tasklineitems) {
                    ArrayList<TaskLineItemPhotoViewModel> tdl = dbHelper.getAllTaskLineItemPhotos(String.valueOf(tl.ID));
                    for (TaskLineItemPhotoViewModel tlp : tdl) {
                        // UploadMultiplePhotos(s, s.PhotoID);
                        Shared.SelecteduploadTasklineitemPhotos = tlp;
                        uploadMultipleImage obj = new uploadMultipleImage(this);
                        obj.execute();
                    }
                }
            }
        }
        //ProcessUpload();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        uploadMultipleImage obj = new uploadMultipleImage(this);
        obj.execute();
        // ProcessUpload();
        return null;
    }


    // private void ProcessUpload() {
//	 DatabaseHelper dbHelper = new DatabaseHelper(this);
//	 String condition="IsDone=True";
//	 ArrayList<TaskViewModel> taskListshopPhotos=dbHelper.getTasks(condition);
//	 for(TaskViewModel s:taskListshopPhotos){
//     UploadShopPhoto(s.TaskLineItemViewModelList.get(0), s.PhotoID);
//	}
//UploadMultiplePhotos(null, null);
// }
    private void ProcessUpload() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String condition = "IsDone=True";
        ArrayList<TaskLineItemPhotoViewModel> tdl = dbHelper.getAllTaskLineItemPhotos(String.valueOf(40197));
        for (TaskLineItemPhotoViewModel s : tdl) {
            UploadMultiplePhotos(s, s.PhotoID);
        }
    }

    public String UploadMultiplePhotos(TaskLineItemPhotoViewModel taskLineItemphotos, String path) {

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
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(path);
        int serverResponseCode = 0;
        try {

            ArrayList<NameValuePair> taskLineItemPhotos1 = new ArrayList<NameValuePair>(
                    5);
            taskLineItemPhotos1.add(new BasicNameValuePair("TaskID", String.valueOf(taskLineItemphotos.ID)));
            taskLineItemPhotos1.add(new BasicNameValuePair("TaskLineItemID", String.valueOf(taskLineItemphotos.TaskLineItemID)));
            taskLineItemPhotos1.add(new BasicNameValuePair("Lat", taskLineItemphotos.Lat));
            taskLineItemPhotos1.add(new BasicNameValuePair("Lon", taskLineItemphotos.Lon));
            taskLineItemPhotos1.add(new BasicNameValuePair("Time", taskLineItemphotos.Time));
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

            Log.e("Upload file Exception",
                    "Exception : " + e.getMessage(), e);
            return "-2";
        }
        return sbResult.toString();
    }


    class uploadMultipleImage extends AsyncTask<String, Integer, ServerResult> {
        Context context;

        public uploadMultipleImage(Context _context) {
            context = _context;

        }

        public uploadMultipleImage(AsyncTask<Void, Void, String> asyncTask) {
            // TODO Auto-generated constructor stub
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected ServerResult doInBackground(String... params) {
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
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {

                e.printStackTrace();

                Log.e("Upload file Exception",
                        "Exception : " + e.getMessage(), e);
                return ServerResult.ConnectionFailed;
            }
            return ServerResult.UploadSuccess;
        }
    }
}
// private String UploadShopPhoto(TaskLineItemViewModel taskLineItem,String path)
//	{
//		// String path = taskLineItem.PhotoID;
//		String fileName = new File(path).getName();
//		StringBuilder sbResult = new StringBuilder();
//		HttpURLConnection conn = null;
//		DataOutputStream dos = null;
//		String lineEnd = "\r\n";
//		String twoHyphens = "--";
//		String boundary = "*****";
//		int bytesRead, bytesAvailable, bufferSize;
//		byte[] buffer;
//		int maxBufferSize = 1 * 1024 * 1024;
//		File sourceFile = new File(path);
//
//		if (!sourceFile.isFile() && !path.equals("NOT_DONE"))
//		{
//
//			Log.e("uploadFile", "Source File not exist :");
//
//			return "MISSING_SHOP_PHOTO:" + Integer.toString(taskLineItem.ID);
//
//		}
//		else if (sourceFile.isFile())
//		{
//			int serverResponseCode = 0;
//			try {
//
//				List<NameValuePair> taskDetailRequest = new ArrayList<NameValuePair>(
//						5);
//				taskDetailRequest.add(new BasicNameValuePair("TaskID", String.valueOf(taskLineItem.ID)));
//				taskDetailRequest.add(new BasicNameValuePair("Time",taskLineItem.Time));
//				taskDetailRequest.add(new BasicNameValuePair("Employee",Shared.LoggedInUser.ID));
//
//				// open a URL connection to the Servlet
//				FileInputStream fileInputStream = new FileInputStream(
//						sourceFile);
//				URL url = new URL(Shared.UploadShopPhotoAPI + "?"
//						+ Utility.getQuery(taskDetailRequest));
//
//				// Open a HTTP connection to the URL
//				conn = (HttpURLConnection) url.openConnection();
//				conn.setDoInput(true); // Allow Inputs
//				conn.setDoOutput(true); // Allow Outputs
//				conn.setUseCaches(false); // Don't use a Cached Copy
//				conn.setRequestMethod("POST");
//				conn.setRequestProperty("Connection", "Keep-Alive");
//				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//				conn.setRequestProperty("Content-Type",
//						"multipart/form-data;boundary=" + boundary);
//
//				conn.setRequestProperty("uploaded_file",Shared.GetLocationString() + fileName);
//
//				dos = new DataOutputStream(conn.getOutputStream());
//
//				dos.writeBytes(twoHyphens + boundary + lineEnd);
//				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
//						+ fileName + "\"" + lineEnd);
//
//				dos.writeBytes(lineEnd);
//
//				// create a buffer of maximum size
//				bytesAvailable = fileInputStream.available();
//
//				bufferSize = Math.min(bytesAvailable, maxBufferSize);
//				buffer = new byte[bufferSize];
//
//				// read file and write it into form...
//				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//				while (bytesRead > 0)
//				{
//
//					dos.write(buffer, 0, bufferSize);
//					bytesAvailable = fileInputStream.available();
//					bufferSize = Math.min(bytesAvailable, maxBufferSize);
//					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//				}
//
//				// send multipart form data necesssary after file data...
//				dos.writeBytes(lineEnd);
//				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//				// Responses from the server (code and message)
//				serverResponseCode = conn.getResponseCode();
//				if (serverResponseCode == 200)
//				{
//					BufferedReader in = new BufferedReader(
//							new InputStreamReader(conn.getInputStream()));
//					String inputLine = "";
//
//					while ((inputLine = in.readLine()) != null)
//						sbResult.append(inputLine);
//					in.close();
//
//				}
//
//				// close the streams //
//				// writer.close();
//				fileInputStream.close();
//				dos.flush();
//				dos.close();
//
//			} catch (MalformedURLException ex) {
//
//				ex.printStackTrace();
//
//				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
//				return "-1";
//			} catch (Exception e) {
//
//				e.printStackTrace();
//
//				Log.e("Upload file to s verver Exception",
//						"Exception : " + e.getMessage(), e);
//				return "-2";
//			}
//
//		} // End else block
//
//		return "TASKID:" + sbResult.toString();
//	}

//	 public String UploadMultiplePhotos(TaskLineItemPhotosViewModel taskLineItemphotos, String path)
//	 {
//
//		   // String path = taskLineItem.PhotoID;
//			String fileName = new File(path).getName();
//			StringBuilder sbResult = new StringBuilder();
//			HttpURLConnection conn = null;
//			DataOutputStream dos = null;
//			String lineEnd = "\r\n";
//			String twoHyphens = "--";
//			String boundary = "*****";
//			int bytesRead, bytesAvailable, bufferSize;
//			byte[] buffer;
//			int maxBufferSize = 1 * 1024 * 1024;
//			File sourceFile = new File(path);
//			int serverResponseCode = 0;
//			try
//			{
//			TaskLineItemPhotosViewModel taskLineItemPhotos=new TaskLineItemPhotosViewModel();
//			ArrayList<NameValuePair> taskLineItemPhotos1 = new ArrayList<NameValuePair>(
//					5);
//			taskLineItemPhotos1.add(new BasicNameValuePair("TaskID",
//					String.valueOf(taskLineItemphotos.TaskID)));
//			taskLineItemPhotos1.add(new BasicNameValuePair("TaskLineItemID",
//					String.valueOf(taskLineItemphotos.TaskLineItemID)));
//			taskLineItemPhotos1.add(new BasicNameValuePair("Lat",
//					taskLineItemphotos.Lat));
//			taskLineItemPhotos1.add(new BasicNameValuePair("Lon",
//					taskLineItemphotos.Lon));
//			taskLineItemPhotos1.add(new BasicNameValuePair("Time",
//					taskLineItemphotos.Time));
//			FileInputStream fileInputStream = new FileInputStream(
//					sourceFile);
//			URL url = new URL(Shared.UploadAPI + "?"
//					+ Utility.getQuery(taskLineItemPhotos1));
//
//			// Open a HTTP connection to the URL
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setDoInput(true); // Allow Inputs
//			conn.setDoOutput(true); // Allow Outputs
//			conn.setUseCaches(false); // Don't use a Cached Copy
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("Connection", "Keep-Alive");
//			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//			conn.setRequestProperty("Content-Type",
//					"multipart/form-data;boundary=" + boundary);
//
//			conn.setRequestProperty("uploaded_file",
//					Shared.GetLocationString() + fileName);
//
//			dos = new DataOutputStream(conn.getOutputStream());
//
//			dos.writeBytes(twoHyphens + boundary + lineEnd);
//			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
//					+ fileName + "\"" + lineEnd);
//
//			dos.writeBytes(lineEnd);
//
//			// create a buffer of maximum size
//			bytesAvailable = fileInputStream.available();
//
//			bufferSize = Math.min(bytesAvailable, maxBufferSize);
//			buffer = new byte[bufferSize];
//
//			// read file and write it into form...
//			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//			while (bytesRead > 0) {
//
//				dos.write(buffer, 0, bufferSize);
//				bytesAvailable = fileInputStream.available();
//				bufferSize = Math.min(bytesAvailable, maxBufferSize);
//				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//			}
//
//			// send multipart form data necesssary after file data...
//			dos.writeBytes(lineEnd);
//			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//			// Responses from the server (code and message)
//			serverResponseCode = conn.getResponseCode();
//			if (serverResponseCode == 200) {
//				BufferedReader in = new BufferedReader(
//						new InputStreamReader(conn.getInputStream()));
//				String inputLine = "";
//
//				while ((inputLine = in.readLine()) != null)
//					sbResult.append(inputLine);
//				in.close();
//
//			}
//
//			// close the streams //
//			// writer.close();
//			fileInputStream.close();
//			dos.flush();
//			dos.close();
//
//		}
//		 catch (MalformedURLException ex) {
//
//			ex.printStackTrace();
//
//			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
//			return "-1";
//		} catch (Exception e) {
//
//			e.printStackTrace();
//
//			Log.e("Upload file to server Exception",
//					"Exception : " + e.getMessage(), e);
//			return "-2";
//		}
//		 return sbResult.toString();
//	 }



//}


