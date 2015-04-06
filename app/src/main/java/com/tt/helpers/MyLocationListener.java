package com.tt.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.tt.data.MeasurementPhoto;
import com.tt.data.Shared;

public class MyLocationListener implements LocationListener {
    Context contextReference;
    LocationManager mlocManager;
    int classID;

    public MyLocationListener(final Context contextReference, LocationManager mlocManager, int classID) {
        this.contextReference = contextReference;
        this.mlocManager = mlocManager;
        this.classID = classID;
    }

    @Override
    public void onLocationChanged(Location loc) {
        DatabaseHelper dbHelper = new DatabaseHelper(contextReference);
        Shared.lat = loc.getLatitude();
        Shared.lon = loc.getLongitude();

        if (classID == 0) {
            MeasurementPhoto measurementPhoto = new MeasurementPhoto();

            if (Shared.sharedCheckinID > 0) {
                measurementPhoto.ID = (int) Shared.sharedCheckinID;
                measurementPhoto.Lat = String.valueOf(loc.getLatitude());
                measurementPhoto.Lon = String.valueOf(loc.getLongitude());

                dbHelper.saveMeasurementPhoto(measurementPhoto,
                        true);
            } else {
                measurementPhoto.Lat = String.valueOf(loc.getLatitude());
                measurementPhoto.Lon = String.valueOf(loc.getLongitude());

                Shared.sharedCheckinID = dbHelper.saveMeasurementPhoto(measurementPhoto,
                        true);
            }

        } else {

        }
        mlocManager.removeUpdates(this);

    }

    @Override
    public void onProviderDisabled(String provider) {
        // Toast.makeText( contextReference, "Gps Disabled", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //  Toast.makeText( contextReference, "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
