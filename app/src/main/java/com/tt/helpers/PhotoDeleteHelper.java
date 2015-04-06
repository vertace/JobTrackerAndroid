package com.tt.helpers;

import android.util.Log;

import java.io.File;

public class PhotoDeleteHelper {
    public static boolean DeletePhoto(String path) {
        try {

            if (path.isEmpty())
                return false;

            File file = new File(path);
            if (file.exists())
                return file.delete();
            return false;
        } catch (Exception e) {
            String msg1 = e.getLocalizedMessage();
            String msg2 = e.getMessage();
            String msg3 = e.getStackTrace().toString();
            String msg4 = e.getMessage();
            Log.d("err", msg1 + msg2 + msg3 + msg4);
            return false;
        }

    }
}
