package com.sloy.sevibus.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginController implements GoogleApiClient.OnConnectionFailedListener {

    public Intent loginIntent(GoogleApiClient googleApiClient) {
        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    public void logout(GoogleApiClient googleApiClient) {
        Auth.GoogleSignInApi.signOut(googleApiClient);
    }

    public Observable<String> obtainOAuthTokenFromSignInResult(Context context, Intent data) {
        return Observable.just(data)
          .map(Auth.GoogleSignInApi::getSignInResultFromIntent)
          .flatMap(this::successOrFail)
          .map(GoogleSignInResult::getSignInAccount)
          .map(GoogleSignInAccount::getEmail)
          .flatMap(email -> getOauthToken(context, email));
    }

    private Observable<String> getOauthToken(Context context, String email) {
        return Observable.defer(() -> {
            try {
                String token = GoogleAuthUtil.getToken(context, email, "oauth2:profile email");
                Log.d("token", token);
                return Observable.just(token);
            } catch (IOException | GoogleAuthException e) {
                return Observable.error(e);
            }
        })
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LOGIN", "onConnectionFailed");
    }

    @NonNull
    private Observable<GoogleSignInResult> successOrFail(GoogleSignInResult signInResult) {
        return signInResult.isSuccess() ? Observable.just(signInResult) : Observable.error(new Exception("Oops"));
    }
}
