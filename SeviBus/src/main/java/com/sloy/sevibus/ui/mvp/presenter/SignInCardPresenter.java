package com.sloy.sevibus.ui.mvp.presenter;

import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.actions.user.LogInAction;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloy.sevibus.ui.other.CardWizardManager;

public class SignInCardPresenter implements Presenter<SignInCardPresenter.View> {

    private final LogInAction logInAction;
    private final CardWizardManager cardManager;
    private final AnalyticsTracker analyticsTracker;
    private final CrashReportingTool crashReportingTool;
    private View view;

    public SignInCardPresenter(LogInAction logInAction, CardWizardManager cardManager, AnalyticsTracker analyticsTracker, CrashReportingTool crashReportingTool) {
        this.logInAction = logInAction;
        this.cardManager = cardManager;
        this.analyticsTracker = analyticsTracker;
        this.crashReportingTool = crashReportingTool;
    }

    @Override
    public void initialize(View view) {
        this.view = view;
    }

    public void onSignInClick() {
        view.startSignInFlow()
          .doOnSubscribe(() -> {
              view.hideSignInButtons();
              view.showLoading();
          })
          .flatMap(logInAction::logIn)
          .subscribe(sevibusUser -> {
              view.hideLoginForm();
              view.showUserInfo(sevibusUser);
              analyticsTracker.signInSuccess(cardManager);
          }, error -> {
              view.showSignInButtons();
              view.hideLoading();
              view.showError();
              analyticsTracker.signInFailure(cardManager);
              crashReportingTool.registerHandledException(error);
          });
    }

    public void onContinueClick() {
        cardManager.next();
    }

    public void onRejectClick() {
        cardManager.next();
    }

    @Override
    public void update() {
        //NA
    }

    @Override
    public void pause() {
        //NA
    }

    public interface View extends SignInFlow {

        void showSignInButtons();

        void hideSignInButtons();

        void hideLoading();

        void showLoading();

        void showUserInfo(SevibusUser user);

        void hideLoginForm();

        void showError();
    }

}
