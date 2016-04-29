package com.sloy.sevibus.resources.actions.user;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.datasource.favorita.AuthException;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import java.util.Map;

import rx.Observable;

public class LogInAction {

    private final UserDataSource userDataSource;
    private final Firebase firebase;
    private final CrashReportingTool crashReportingTool;

    public LogInAction(UserDataSource userDataSource, Firebase firebase, CrashReportingTool crashReportingTool) {
        this.userDataSource = userDataSource;
        this.firebase = firebase;
        this.crashReportingTool = crashReportingTool;
    }

    public Observable<SevibusUser> logIn(String oauthToken) {
        return Observable.just(oauthToken)
          .flatMap(this::autenticateFirebase)
          .map(this::createSevibusUser)
          .flatMap(userDataSource::setCurrentUser)
          .map(this::sendUserToFirebase)
          .map(this::associateWithCrashReporting);
    }

    private Observable<AuthData> autenticateFirebase(String oauthToken) {
        return Observable.create(subscriber -> firebase.authWithOAuthToken("google", oauthToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(authData);
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(firebaseError.toException());
                }
            }
        }));
    }

    private SevibusUser createSevibusUser(AuthData authData) {
        Map<String, Object> data = authData.getProviderData();
        SevibusUser user = new SevibusUser();
        user.setId((String) authData.getAuth().get("uid"));
        user.setEmail((String) data.get("email"));
        user.setName((String) data.get("displayName"));
        user.setPhotoUrl((String) data.get("profileImageURL"));
        user.setOauthToken((String) data.get("accessToken"));
        return user;
    }


    private SevibusUser sendUserToFirebase(SevibusUser sevibusUser) {
        AuthData auth = firebase.getAuth();
        firebase.child(auth.getUid())
          .child("user")
          .setValue(sevibusUser);
        return sevibusUser;
    }

    private SevibusUser associateWithCrashReporting(SevibusUser sevibusUser) {
        crashReportingTool.associateUser(sevibusUser);
        return sevibusUser;
    }
}
