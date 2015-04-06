package com.tt.helpers;

import android.app.AlertDialog;
import android.content.Context;

import com.example.sstracker.R;

public class SstAlert {
    public static void Show(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_launcher);

        // Showing Alert Message
        alertDialog.show();
    }
}
