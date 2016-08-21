package com.sloy.sevibus.ui.mvp.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloy.sevibus.ui.activities.LoginActivity;
import com.sloy.sevibus.ui.mvp.presenter.SignInCardPresenter;
import com.sloy.sevibus.ui.mvp.presenter.SignInFlow;
import com.sloy.sevibus.ui.mvp.presenter.UserInfoHeaderPresenter;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoHeaderViewContainer implements UserInfoHeaderPresenter.View {

    private final View contentView;
    private final UserInfoHeaderPresenter presenter;
    private final Picasso picasso;

    @Bind(R.id.header_signin_avatar)
    ImageView avatar;
    @Bind(R.id.header_signin_name)
    TextView name;
    @Bind(R.id.header_signin_email)
    TextView email;

    public UserInfoHeaderViewContainer(View contentView, UserInfoHeaderPresenter presenter, Picasso picasso) {
        this.contentView = contentView;
        this.presenter = presenter;
        this.picasso = picasso;
        ButterKnife.bind(this, contentView);
    }

    @Override
    public void showUserInfo(SevibusUser user) {
        name.setText(user.getName());
        email.setText(user.getEmail());
        avatar.setVisibility(View.VISIBLE);
        picasso.load(user.getPhotoUrl()).into(avatar);
    }

    @Override
    public void showSigninInstructions() {
        name.setText(R.string.drawer_signin_title);
        email.setText(R.string.drawer_signin_subtitle);
        avatar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void navigateToLogin() {
        getContext().startActivity(LoginActivity.newIntent(getContext()));
    }

    @OnClick(R.id.header_signin_root)
    public void onProfileClick() {
        presenter.onProfileClick();
    }

    private Context getContext() {
        return contentView.getContext();
    }

}
