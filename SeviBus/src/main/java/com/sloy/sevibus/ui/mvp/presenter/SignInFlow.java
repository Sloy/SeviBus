package com.sloy.sevibus.ui.mvp.presenter;


import com.google.firebase.auth.AuthCredential;

import rx.Observable;

public interface SignInFlow {

    Observable<AuthCredential> startSignInFlow();

}
