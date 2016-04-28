package com.sloy.sevibus.resources.actions.user;

import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import java.util.Map;

import rx.Observable;

public class LogInAction {

    private final UserDataSource userDataSource;
    private final Firebase firebase;

    public LogInAction(UserDataSource userDataSource, Firebase firebase) {
        this.userDataSource = userDataSource;
        this.firebase = firebase;
    }

    public Observable<SevibusUser> logIn(String oauthToken) {
        return Observable.just(oauthToken)
          .flatMap(this::autenticateFirebase)
          .map(this::createSevibusUser)
          .flatMap(userDataSource::setCurrentUser);
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
}
