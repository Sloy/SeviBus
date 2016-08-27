package com.sloy.sevibus.ui.activities;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.LocationServices;
import com.sloy.sevibus.resources.LocationProvider;

public class LocationProviderActivity extends BaseToolbarActivity {


    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = new LocationProvider();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestNewLocation();
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    private void requestNewLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
        locationProvider.sendNewLocation(lastLocation);
    }


}
