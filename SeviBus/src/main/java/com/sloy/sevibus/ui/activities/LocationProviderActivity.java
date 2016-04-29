package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.LocationProvider;

public class LocationProviderActivity extends BaseToolbarActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final int RESOLUTION_REQUEST_CODE = 600613;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private GoogleApiClient googleApiClient;

    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestEmail()
          .requestIdToken("952975778259-4tdh0qdnn97a6epq4sj27p3dms1802it.apps.googleusercontent.com")
          .build();

        googleApiClient = new GoogleApiClient.Builder(this)
          .enableAutoManage(this, this)
          .addConnectionCallbacks(this)
          .addApi(LocationServices.API)
          .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
          .build();

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
        locationProvider.sendNewLocation(lastLocation);
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
                connectionResult.startResolutionForResult(this, RESOLUTION_REQUEST_CODE);
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
