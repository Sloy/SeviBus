package com.sloy.sevibus.resources.services;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class LoginService {

    private final FirebaseAuth firebaseAuth;

    public LoginService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public Observable<SevibusUser> logUserIn(AuthCredential credential) {
        return Observable.just(credential)
          .flatMap(this::autenticateFirebase)
          .map(this::createSevibusUser);
    }

    private Observable<FirebaseUser> autenticateFirebase(AuthCredential credential) {
        return Observable.create(subscriber -> firebaseAuth.signInWithCredential(credential)
          .addOnCompleteListener(task -> {
              if (!subscriber.isUnsubscribed()) {
                  if (task.isSuccessful()) {
                      subscriber.onNext(firebaseAuth.getCurrentUser());
                      subscriber.onCompleted();
                  } else {
                      subscriber.onError(task.getException());
                  }
              }
          }));
    }

    private SevibusUser createSevibusUser(FirebaseUser firebaseUser) {
        SevibusUser user = new SevibusUser();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        if (firebaseUser.getPhotoUrl() != null) {
            user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
        }
        return user;
    }
}
