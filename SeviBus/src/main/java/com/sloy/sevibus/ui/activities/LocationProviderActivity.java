package com.sloy.sevibus.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.LocationProvider;

public abstract class LocationProviderActivity extends BaseToolbarActivity {


    private LocationProvider locationProvider;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
              .addOnSuccessListener(this, location -> locationProvider.sendNewLocation(location));
        } else {
            PermissionListener dialogPermissionListener =
              DialogOnDeniedPermissionListener.Builder
                .withContext(this)
                .withTitle("Localización")
                .withMessage("Necesitas aceptar el permiso de localización para usar la aplicación")
                .withButtonText(android.R.string.ok)
                .withIcon(R.drawable.ic_launcher)
                .build();

            Dexter.withActivity(this)
              .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
              .withListener(dialogPermissionListener)
              .check();
        }
    }


}
