package com.sloy.sevibus.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import rx.Observable;

public class LoginController implements GoogleApiClient.OnConnectionFailedListener {

    public Intent loginIntent(GoogleApiClient googleApiClient) {
        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    public void logout(GoogleApiClient googleApiClient) {
        Auth.GoogleSignInApi.signOut(googleApiClient);
    }

    public Observable<AuthCredential> obtainOAuthTokenFromSignInResult(Context context, Intent data) {
        return Observable.just(data)
          .map(Auth.GoogleSignInApi::getSignInResultFromIntent)
          .flatMap(this::successOrFail)
          .map(GoogleSignInResult::getSignInAccount)
          .map(googleSignInAccount -> GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LOGIN", "onConnectionFailed");
    }

    @NonNull
    private Observable<GoogleSignInResult> successOrFail(GoogleSignInResult signInResult) {
        if (signInResult.isSuccess()) {
            return Observable.just(signInResult);
        } else {
            Status status = signInResult.getStatus();
            return Observable.error(new Exception(status.toString()));
        }
    }
}
