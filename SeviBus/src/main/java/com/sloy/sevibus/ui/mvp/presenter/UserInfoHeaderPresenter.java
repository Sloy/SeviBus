package com.sloy.sevibus.ui.mvp.presenter;

import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.actions.user.ObtainUserAction;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloydev.gallego.Optional;

public class UserInfoHeaderPresenter implements Presenter<UserInfoHeaderPresenter.View> {

    private final ObtainUserAction obtainUserAction;
    private final AnalyticsTracker analyticsTracker;
    private final CrashReportingTool crashReportingTool;
    private View view;

    public UserInfoHeaderPresenter(ObtainUserAction obtainUserAction, AnalyticsTracker analyticsTracker, CrashReportingTool crashReportingTool) {
        this.obtainUserAction = obtainUserAction;
        this.analyticsTracker = analyticsTracker;
        this.crashReportingTool = crashReportingTool;
    }

    @Override
    public void initialize(View view) {
        this.view = view;
        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        obtainUserAction.obtainUser()
          .subscribe(optionalUser -> {
              if (optionalUser.isPresent()) {
                  view.showUserInfo(optionalUser.get());
              } else {
                  view.showSigninInstructions();
              }
          });
    }


    public void onProfileClick() {
        obtainUserAction.obtainUser()
          .map(Optional::isPresent)
          .subscribe(isLoggedIn -> {
              if (isLoggedIn) {
                //TODO logout menu
              } else {
                  view.navigateToLogin();
              }
          });
    }

    @Override
    public void update() {
        retrieveUserInfo();
    }

    @Override
    public void pause() {
        //NA
    }

    public interface View {

        void showUserInfo(SevibusUser user);

        void showSigninInstructions();

        void navigateToLogin();
    }

}
