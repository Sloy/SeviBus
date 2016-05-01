package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.AuthCredential;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.LoginController;
import com.sloy.sevibus.ui.mvp.presenter.SignInCardPresenter;
import com.sloy.sevibus.ui.mvp.presenter.SignInFlow;
import com.sloy.sevibus.ui.mvp.view.SignInCardViewContainer;
import com.sloy.sevibus.ui.other.CardWizardManager;
import com.squareup.picasso.Picasso;

import rx.Observable;
import rx.Subscriber;


public class LoginActivity extends BaseActivity implements SignInFlow {

    private static final int RC_SIGN_IN = 42;

    private SignInCardPresenter presenter;
    private LoginController loginController;
    private Subscriber<? super AuthCredential> signInFlowSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginController = new LoginController();
        presenter = new SignInCardPresenter(StuffProvider.getLoginAction(this), new LoginCardManager(), StuffProvider.getAnalyticsTracker(), StuffProvider.getCrashReportingTool());

        SignInCardViewContainer viewContainer = new SignInCardViewContainer(findViewById(android.R.id.content), presenter, Picasso.with(this), this);

        presenter.initialize(viewContainer);
    }

    @Override
    public Observable<AuthCredential> startSignInFlow() {
        return Observable.create(new Observable.OnSubscribe<AuthCredential>() {
            @Override
            public void call(Subscriber<? super AuthCredential> subscriber) {
                startActivityForResult(loginController.loginIntent(getGoogleApiClient()), RC_SIGN_IN);
                signInFlowSubscriber = subscriber;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            loginController.obtainOAuthTokenFromSignInResult(this, data)
              .subscribe(token -> {
                  if (!signInFlowSubscriber.isUnsubscribed()) {
                      signInFlowSubscriber.onNext(token);
                      signInFlowSubscriber.onCompleted();
                  }
              }, throwable -> {
                  signInFlowSubscriber.onError(throwable);
              });

        }
    }

    private class LoginCardManager implements CardWizardManager {

        @Override
        public void next() {
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public String getDescription() {
            return "Login";
        }
    }
}
