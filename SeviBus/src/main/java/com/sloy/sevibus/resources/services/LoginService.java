package com.sloy.sevibus.resources.services;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sloy.sevibus.resources.UserMapper;
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
          .map(UserMapper::mapFirebaseUser);
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

}
