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
import com.sloy.sevibus.ui.fragments.main.ILocationSensitiveFragment;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rafa on 05/09/13.
 */
//TODO escuchar actualizaciones de posición, no sólo la inicial pisha
public class LocationProviderActivity extends BaseToolbarActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private List<ILocationSensitiveFragment> mFragmentsToNotify;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();

        mFragmentsToNotify = new LinkedList<ILocationSensitiveFragment>();
    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    public void suscribeForUpdates(ILocationSensitiveFragment fragment) {
        mFragmentsToNotify.add(fragment);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
            if (lastLocation != null) {
                Debug.useFakeLocation(this, lastLocation);
                fragment.updateLocation(lastLocation);
            }
        }
    }

    public void unsuscribe(ILocationSensitiveFragment fragment) {
        mFragmentsToNotify.remove(fragment);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Sevibus Location", "onConnected()");
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
            if (currentLocation != null) {
                Debug.useFakeLocation(this, currentLocation);
                for (ILocationSensitiveFragment f : mFragmentsToNotify) {
                    f.updateLocation(currentLocation);
                }
            } else {
                Log.w("SeviBus", "LocationProviderActivity#onConnected(): Recibida localización nula");
            }
    }

    @Override public void onConnectionSuspended(int i) {
        /* no-op */
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
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

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }
}
