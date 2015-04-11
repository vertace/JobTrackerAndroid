package com.tt.fragments;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.tt.data.Shared;
import com.tt.jobtracker.R;


public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch mySwitch = (Switch) rootView.findViewById(R.id.switch1);

        final SharedPreferences uploadonWifibynuser = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
        //final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    SharedPreferences.Editor editor = uploadonWifibynuser.edit();
                    editor.putBoolean("status1", true); // Storing string
                    editor.commit(); // commit changes
                    Toast.makeText(getActivity(), "upload only on Wifi", Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences.Editor editor = uploadonWifibynuser.edit();
                    editor.putBoolean("status1", false); // Storing string
                    editor.commit();
                    Toast.makeText(getActivity(), "upload in both", Toast.LENGTH_LONG).show();
                }

            }
        });


        boolean temp= uploadonWifibynuser.getBoolean("status", false); // getting String

        return rootView;
    }
}