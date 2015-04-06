package com.tt.jobtracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.sstracker.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadFileAsync extends AsyncTask<String, String, String> {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public Context context;
    private ProgressDialog mProgressDialog;

    public DownloadFileAsync(Context _context) {
        context = _context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        // Set your ProgressBar Title
        mProgressDialog.setTitle("Download");
        mProgressDialog.setIcon(R.drawable.download);
        // Set your ProgressBar Message
        mProgressDialog.setMessage("Downloading old images. Please Wait!");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Show ProgressBar
        mProgressDialog.setCancelable(false);
        // mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    @SuppressWarnings("unused")
    @Override
    protected String doInBackground(String... aurl) {
        int count;

        try {

            URL url = new URL(aurl[0]);
            String filename = aurl[1];
            URLConnection conexion = url.openConnection();
            conexion.connect();

            int lenghtOfFile = conexion.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream());
            String sdCard = Environment.getExternalStorageDirectory().getPath()
                    + "/Pictures/SSTracker/";

            File dir = new File(sdCard);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(sdCard, filename);
            // if (!file.exists()) {
            // return null;
            // }
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            publishProgress("120786");// Decompress code 120786
            Decompress(sdCard + filename, sdCard);
        } catch (Exception e) {
            String msg1 = e.getLocalizedMessage();
            String msg2 = e.getMessage();
            String msg3 = e.getStackTrace().toString();
            String msg4 = e.getMessage();
            Log.d("err", msg1 + msg2 + msg3 + msg4);
        }

        return null;

    }

    public void Decompress(String zipFile, String location) {

        File f = new File(location);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
        try {

            FileInputStream fin = new FileInputStream(zipFile);
            BufferedInputStream in = new BufferedInputStream(fin);
            ZipInputStream zin = new ZipInputStream(in);


            int lenghtOfFile = fin.available();
            int done = 0;
            ZipEntry ze = null;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zin.getNextEntry()) != null) {

                FileOutputStream fout = new FileOutputStream(location
                        + ze.getName());
                // BufferedOutputStream out = new BufferedOutputStream(fout);
                //
                // byte b[] = new byte[1024];
                // int n;
                // while ((n = in.read(b, 0, 1024)) >= 0) {
                // done += n;
                // out.write(b, 0, n);
                // }
                // publishProgress("" + (int) ((done * 100) / lenghtOfFile));
                // out.close();
                // zin.closeEntry();
                // fout.close();

                while ((count = zin.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zin.closeEntry();
            }
            zin.close();

        } catch (Exception e) {

            String msg1 = e.getLocalizedMessage();
            String msg2 = e.getMessage();
            String msg3 = e.getStackTrace().toString();
            String msg4 = e.getMessage();
            Log.d("err", msg1 + msg2 + msg3 + msg4);
        }
    }

    protected void onProgressUpdate(String... progress) {
        if (progress.equals("120786")) {
            mProgressDialog.setMessage("Decompressing files. Please Wait!");
        }
        mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String unused) {
        mProgressDialog.dismiss();
    }
}