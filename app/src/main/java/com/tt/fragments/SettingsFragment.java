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
import android.widget.ToggleButton;

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
        final SharedPreferences userswitchstatus = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_switchstatus, 0);
        final SharedPreferences mobilewifistatus = getActivity().getApplicationContext().getSharedPreferences(Shared.sharedprefs_uploadstatus, 0);

        final ToggleButton tgbutton;
        tgbutton = (ToggleButton) rootView.findViewById(R.id.toggleButton);
        String result=userswitchstatus.getString("toggleButton", null);
        Boolean boolean1 = Boolean.valueOf(result);
        tgbutton.setChecked(boolean1);
        tgbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = userswitchstatus.edit();
                if(isChecked)
                {
                    editor.putString("toggleButton", "true");
                }
                else {
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