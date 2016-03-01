package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.ui.fragments.main.ILocationSensitiveFragment;

import java.util.LinkedList;
import java.util.List;

public class LocationProviderActivity extends BaseToolbarActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private List<ILocationSensitiveFragment> fragmentsToNotify;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private GoogleApiClient googleApiClient;

    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(this)
          .enableAutoManage(this, this)
          .addConnectionCallbacks(this)
          .addApi(LocationServices.API)
          .build();

        fragmentsToNotify = new LinkedList<>();
        locationProvider = new LocationProvider();
    }

    @Override
    protected void onPause() {
        googleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        googleApiClient.connect();
        super.onResume();
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    private void requestNewLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            Debug.useFakeLocation(this, lastLocation);
            onLocationUpdated(lastLocation);
        }
    }

    private void onLocationUpdated(Location location) {
        for (ILocationSensitiveFragment f : fragmentsToNotify) {
            f.updateLocation(location);
        }
        locationProvider.sendNewLocation(location);
    }

    public void suscribeForUpdates(ILocationSensitiveFragment fragment) {
        fragmentsToNotify.add(fragment);
//        requestNewLocation();
    }

    public void unsuscribe(ILocationSensitiveFragment fragment) {
        fragmentsToNotify.remove(fragment);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Sevibus Location", "onConnected()");
        requestNewLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        /* no-op */
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 1);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Error de ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }
}
