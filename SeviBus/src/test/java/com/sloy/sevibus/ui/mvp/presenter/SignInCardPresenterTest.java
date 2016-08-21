package com.sloy.sevibus.ui.mvp.presenter;

import com.google.firebase.auth.AuthCredential;
import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.actions.user.LogInAction;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloy.sevibus.ui.other.CardWizardManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;

public class SignInCardPresenterTest {

    @Mock
    SignInCardPresenter.View view;
    @Mock
    LogInAction logInAction;
    @Mock
    CardWizardManager cardManager;
    @Mock
    AuthCredential authCredential;
    @Mock
    AnalyticsTracker analyticsTracker;
    @Mock
    CrashReportingTool crashReportingTool;

    private SignInCardPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new SignInCardPresenter(logInAction, cardManager, analyticsTracker, crashReportingTool);
        when(view.startSignInFlow()).thenReturn(empty()); //don't crash
    }

    @Test
    public void should_hide_buttons_when_sign_in_clicked() throws Exception {
        presenter.initialize(view);
        presenter.onSignInClick();

        verify(view).hideSignInButtons();
    }

    @Test
    public void should_show_loading_when_sign_in_clicked() throws Exception {
        presenter.initialize(view);
        presenter.onSignInClick();

        verify(view).showLoading();
    }

    @Test
    public void should_login_when_signin_flow_received() throws Exception {
        when(view.startSignInFlow()).thenReturn(just(authCredential));

        presenter.initialize(view);
        presenter.onSignInClick();

        verify(logInAction).logIn(authCredential);
    }

    @Test
    public void should_show_user_info_when_login_received() throws Exception {
        when(view.startSignInFlow()).thenReturn(just(authCredential));
        when(logInAction.logIn(authCredential)).thenReturn(just(new SevibusUser()));

        presenter.initialize(view);
        presenter.onSignInClick();

        verify(view).showUserInfo(any(SevibusUser.class));
    }

    @Test
    public void should_hide_login_form_when_login_received() throws Exception {
        when(view.startSignInFlow()).thenReturn(just(authCredential));
        when(logInAction.logIn(authCredential)).thenReturn(just(new SevibusUser()));

        presenter.initialize(view);
        presenter.onSignInClick();

        verify(view).hideLoginForm();
    }

    @Test
    public void should_show_error_and_show_buttons_and_hide_loading_when_login_fails() throws Exception {
        when(view.startSignInFlow()).thenReturn(just(authCredential));
        when(logInAction.logIn(authCredential)).thenReturn(error(new Exception()));

        presenter.initialize(view);
        presenter.onSignInClick();

        verify(view).showError();
        verify(view).showSignInButtons();
        verify(view).hideLoading();
    }

    @Test
    public void should_trigger_next_card_when_continue_clicked() throws Exception {
        presenter.initialize(view);
        presenter.onContinueClick();

        verify(cardManager).next();
    }

    @Test
    public void should_trigger_next_card_when_reject_clicked() throws Exception {
        presenter.initialize(view);
        presenter.onRejectClick();

        verify(cardManager).next();
    }
}