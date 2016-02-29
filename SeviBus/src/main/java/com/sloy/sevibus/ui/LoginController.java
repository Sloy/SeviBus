package com.sloy.sevibus.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginController implements GoogleApiClient.OnConnectionFailedListener {

    private final Firebase firebase;

    public LoginController(Firebase firebase) {
        this.firebase = firebase;
    }

    public Intent loginIntent(GoogleApiClient googleApiClient) {
        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    public void logout(GoogleApiClient googleApiClient) {
        Auth.GoogleSignInApi.signOut(googleApiClient);
    }

    public Observable<SevibusUser> handleSignInResult(Context context, Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (!result.isSuccess()) {
            //TODO mejora esto
            return Observable.error(new Exception("Oops"));
        }
        return Observable.just(result.getSignInAccount())
          .map(GoogleSignInAccount::getEmail)
          .flatMap(email -> getOauthToken(context, email))
          .flatMap(this::autenticateFirebase)
          .map(this::createSevibusUser);

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

    private Observable<AuthData> autenticateFirebase(String oauthToken) {
        return Observable.create(subscriber -> firebase.authWithOAuthToken("google", oauthToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d("FireLogin", "onAuthenticated!");
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(authData);
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.w("FireLogin", "onAuthenticationError :(!");
                Log.w("FireLogin", "message: " + firebaseError.getMessage());
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(firebaseError.toException());
                }
            }
        }));
    }

    private SevibusUser createSevibusUser(AuthData authData) {
        Log.d("authData", new Gson().toJson(authData));
        Map<String, Object> data = authData.getProviderData();
        SevibusUser user = new SevibusUser();
        user.setId((String) authData.getAuth().get("uid"));
        user.setEmail((String) data.get("email"));
        user.setName((String) data.get("displayName"));
        user.setPhotoUrl((String) data.get("profileImageURL"));
        user.setOauthToken((String) data.get("accessToken"));
        return user;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LOGIN", "onConnectionFailed");
    }
}
