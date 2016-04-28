package com.sloy.sevibus.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.user.LogInAction;
import com.sloy.sevibus.resources.actions.user.LogOutAction;
import com.sloy.sevibus.resources.actions.user.ObtainUserAction;
import com.sloy.sevibus.ui.LoginController;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloy.sevibus.ui.activities.BusquedaActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.mvp.presenter.FavoritasMainPresenter;
import com.sloy.sevibus.ui.mvp.presenter.LineasCercanasPresenter;
import com.sloy.sevibus.ui.mvp.presenter.ParadasCercanasMainPresenter;
import com.sloy.sevibus.ui.mvp.view.FavoritasMainViewContainer;
import com.sloy.sevibus.ui.mvp.view.LineasCercanasViewContainer;
import com.sloy.sevibus.ui.mvp.view.ParadasCercanasMainViewContainer;
import com.squareup.picasso.Picasso;

public class MainPageFragment extends BaseDBFragment {

    private static final int RC_SIGN_IN = 42;

    private ObtainUserAction obtainUserAction;
    private LogInAction logInAction;
    private LogOutAction logOutAction;

    private SignInButton signInButton;
    private LoginController loginController;

    private TextView signOutButton;
    private View loginForm;
    private View userProfile;
    private TextView userEmail;
    private TextView userName;
    private ImageView userPhoto;

    private FavoritasMainPresenter favoritasPresenter;
    private ParadasCercanasMainPresenter cercanasPresenter;
    private LineasCercanasPresenter lineasCercanasPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        signInButton = (SignInButton) v.findViewById(R.id.sign_in_button);
        signOutButton = (TextView) v.findViewById(R.id.sign_out_button);
        loginForm = v.findViewById(R.id.login_form);
        userProfile = v.findViewById(R.id.user_profile);
        userEmail = ((TextView) v.findViewById(R.id.user_email));
        userName = ((TextView) v.findViewById(R.id.user_name));
        userPhoto = ((ImageView) v.findViewById(R.id.user_photo));
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favoritasPresenter = new FavoritasMainPresenter(StuffProvider.getObtainFavoritasAction(getActivity()));
        LocationProvider locationProvider = ((LocationProviderActivity) getActivity()).getLocationProvider();
        cercanasPresenter = new ParadasCercanasMainPresenter(locationProvider, StuffProvider.getObtainCercanasAction(getActivity()));
        lineasCercanasPresenter = new LineasCercanasPresenter(locationProvider, StuffProvider.getObtainLineasCercanasAction(getActivity()));

        View view = getView();
        favoritasPresenter.initialize(new FavoritasMainViewContainer(view.findViewById(R.id.fragment_main_favoritas)));

        ParadasCercanasMainViewContainer paradasCercanasView = new ParadasCercanasMainViewContainer(view.findViewById(R.id.fragment_main_paradas_cercanas));
        cercanasPresenter.initialize(paradasCercanasView);
        paradasCercanasView.setupMapa(getChildFragmentManager());

        LineasCercanasViewContainer lineasCercanasView = new LineasCercanasViewContainer(view.findViewById(R.id.fragment_main_lineas_cercanas));
        lineasCercanasPresenter.initialize(lineasCercanasView);

        obtainUserAction = StuffProvider.getObtainUserAction(getActivity());
        logInAction = StuffProvider.getLoginAction(getActivity());
        logOutAction = StuffProvider.getLogoutAction(getActivity());
        loginController = new LoginController();
        setupLogin();

        view.findViewById(R.id.sign_in_more_info).setOnClickListener(v -> {
            Intent infoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sevibus.sloydev.com/beta_login_info.php"));
            startActivity(infoIntent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        favoritasPresenter.update();
        cercanasPresenter.update();
        lineasCercanasPresenter.update();
    }

    @Override
    public void onPause() {
        super.onPause();
        favoritasPresenter.pause();
        cercanasPresenter.pause();
        lineasCercanasPresenter.pause();
    }

    private void setupLogin() {
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(v -> {
            startActivityForResult(loginController.loginIntent(((LocationProviderActivity) getActivity()).getGoogleApiClient()), RC_SIGN_IN);
        });

        signOutButton.setOnClickListener(v -> {
            loginController.logout(((LocationProviderActivity) getActivity()).getGoogleApiClient());
            logOutAction.logOut().subscribe();
            loginForm.setVisibility(View.VISIBLE);
            userProfile.setVisibility(View.GONE);
        });

        obtainUserAction.obtainUser()
          .subscribe(optionalUser -> {
              if (optionalUser.isPresent()) {
                  showUserProfile(optionalUser.get());
              } else {
                  loginForm.setVisibility(View.VISIBLE);
                  userProfile.setVisibility(View.GONE);
              }
          });
    }

    private void handleSignInResult(Intent data) {
        loginController.obtainOAuthTokenFromSignInResult(getActivity().getApplicationContext(), data)
          .flatMap(oauthToken -> logInAction.logIn(oauthToken))
          .subscribe((sevibusUser) -> {
                showUserProfile(sevibusUser);
                Snackbar.make(getView(), "Sincronizando favoritas...", Snackbar.LENGTH_SHORT).show();
                favoritasPresenter.update();
            },
            throwable -> {
                StuffProvider.getCrashReportingTool().regiterHandledException(throwable);
                Log.e("Login", "Error!!", throwable);
                Snackbar.make(getView(), "Error!! ¿Qué habrá pasado?", Snackbar.LENGTH_SHORT).show();
            });

    }

    private void showUserProfile(SevibusUser sevibusUser) {
        loginForm.setVisibility(View.GONE);
        userProfile.setVisibility(View.VISIBLE);

        userName.setText(sevibusUser.getName());
        userEmail.setText(sevibusUser.getEmail());
        Picasso.with(getActivity()).load(sevibusUser.getPhotoUrl()).into(userPhoto);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            startActivity(BusquedaActivity.getIntent(getActivity()));
            getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(data);
        }
    }
}
