package com.sloy.sevibus.ui.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sloy.sevibus.resources.LocationProvider;

public abstract class LocationProviderActivity extends BaseToolbarActivity {


    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = new LocationProvider();
        getGoogleApiClient().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                requestNewLocation();
            }

            @Override
            public void onConnectionSuspended(int i) {
                //NA
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestNewLocation();
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    protected void requestNewLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
        locationProvider.sendNewLocation(lastLocation);
    }


}
