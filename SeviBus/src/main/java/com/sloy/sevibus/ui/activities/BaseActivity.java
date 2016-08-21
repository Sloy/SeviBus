package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.ui.ThemeSelector;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RESOLUTION_REQUEST_CODE = 600613;

    private DBHelper dbHelper;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSelector.selectTheme(this);
        super.onCreate(savedInstanceState);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestEmail()
          .requestIdToken("255022367225-g00bjo9aoo9tke4siolik1ikck2unefj.apps.googleusercontent.com")
          .build();

        googleApiClient = new GoogleApiClient.Builder(this)
          .enableAutoManage(this, this)
          .addConnectionCallbacks(this)
          .addApi(LocationServices.API)
          .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
          .build();
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

    protected DBHelper getDBHelper() {
        if (dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return dbHelper;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        //NA
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
}
