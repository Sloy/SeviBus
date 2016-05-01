package com.sloy.sevibus.ui.mvp.view;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthCredential;
import com.sloy.sevibus.R;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloy.sevibus.ui.mvp.presenter.SignInCardPresenter;
import com.sloy.sevibus.ui.mvp.presenter.SignInFlow;
import com.squareup.picasso.Picasso;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

public class SignInCardViewContainer implements SignInCardPresenter.View {

    private final View contentView;
    private final SignInCardPresenter presenter;
    private final Picasso picasso;
    private final SignInFlow signInFlow;

    @Bind(R.id.landing_signin_form_card)
    View formCard;
    @Bind(R.id.landing_signin_form_buttons)
    View formButtons;
    @Bind(R.id.landing_signin_form_button)
    SignInButton signInButton;
    @Bind(R.id.landing_signin_form_reject)
    View rejectButton;
    @Bind(R.id.landing_signin_form_progress)
    DilatingDotsProgressBar formProgress;
    @Bind(R.id.landing_signin_result_card)
    View resultCard;
    @Bind(R.id.landing_signin_result_avatar)
    ImageView avatar;
    @Bind(R.id.landing_signin_result_name)
    TextView name;
    @Bind(R.id.landing_signin_result_email)
    TextView email;

    public SignInCardViewContainer(View contentView, SignInCardPresenter presenter, Picasso picasso, SignInFlow signInFlow) {
        this.contentView = contentView;
        this.presenter = presenter;
        this.picasso = picasso;
        this.signInFlow = signInFlow;
        ButterKnife.bind(this, contentView);
    }

    @OnClick(R.id.landing_signin_form_button)
    public void onSignInClick() {
        presenter.onSignInClick();
    }

    @OnClick(R.id.landing_signin_form_reject)
    public void onRejectClick() {
        presenter.onRejectClick();
    }

    @OnClick(R.id.landing_signin_result_continue)
    public void onContinueClick() {
        presenter.onContinueClick();
    }

    @Override
    public void showSignInButtons() {
        formButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSignInButtons() {
        formButtons.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoading() {
        formProgress.hide();
    }

    @Override
    public void showLoading() {
        formProgress.showNow();
    }

    @Override
    public void showUserInfo(SevibusUser user) {
        picasso.load(user.getPhotoUrl()).into(avatar);
        name.setText(user.getName());
        email.setText(user.getEmail());
        resultCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoginForm() {
        formCard.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        Snackbar.make(contentView, "Erró", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public Observable<AuthCredential> startSignInFlow() {
        return signInFlow.startSignInFlow();
    }
}
