package com.tt.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tt.data.Shared;
import com.tt.jobtracker.DirectionsJSONParser;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        final SharedPreferences userswitchstatus = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
        final SharedPreferences mobilewifistatus = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);

        final ToggleButton tgbutton;
        tgbutton = (ToggleButton) rootView.findViewById(R.id.toggleButton);
        String result = userswitchstatus.getString("toggleButton", null);
        Boolean boolean1 = Boolean.valueOf(result);
        tgbutton.setChecked(boolean1);
        tgbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = userswitchstatus.edit();
                if (isChecked) {
                    editor.putString("toggleButton", "true");
                } else {
                    editor.putString("toggleButton", "false");
                }
                editor.commit();
            }
        });

        // String temp= mobilewifistatus.getString("status", null); // getting String

        //  String temp2= userswitchstatus.getString("toggleButton", null); // getting String

        return rootView;

    }
}