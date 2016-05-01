package com.sloy.sevibus.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.RemoteConfiguration;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.TimeTracker;
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

import de.cketti.mailto.EmailIntentBuilder;

public class MainPageFragment extends BaseDBFragment {

    private static final int RC_SIGN_IN = 42;

    private ObtainUserAction obtainUserAction;
    private LogInAction logInAction;
    private LogOutAction logOutAction;
    private AnalyticsTracker analyticsTracker;

    private SignInButton signInButton;
    private TextView signInConfirmationButton;
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
    private RemoteConfiguration remoteConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        signInConfirmationButton = (TextView) v.findViewById(R.id.sign_in_firts_confirmation);
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
        remoteConfiguration = StuffProvider.getRemoteConfiguration();
        favoritasPresenter = new FavoritasMainPresenter(StuffProvider.getObtainFavoritasAction(getActivity()));
        LocationProvider locationProvider = ((LocationProviderActivity) getActivity()).getLocationProvider();
        cercanasPresenter = new ParadasCercanasMainPresenter(locationProvider, StuffProvider.getObtainCercanasAction(getActivity()));
        lineasCercanasPresenter = new LineasCercanasPresenter(locationProvider, StuffProvider.getObtainLineasCercanasAction(getActivity()));

        View view = getView();
        analyticsTracker = StuffProvider.getAnalyticsTracker();

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

        view.findViewById(R.id.sign_in_more_info).setOnClickListener(v -> showLoginMoreInfo());


        signInConfirmationButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
              .setTitle("El que avisa no es traidor")
              .setMessage("Esta función es MUY experimenta. Si decides probarla ayudarás a refinarla y que funcione perfecta cuanto antes y para todos, pero el bienestar de tus paradas favoritas no está garantizado. Por errores de programación o cambios de la funcionalidad se podrían llegar a perder.\n\n¿Deseas seguir adelante?")
              .setPositiveButton("Sí, continuar", (dialog, which) -> {
                  signInConfirmationButton.setVisibility(View.GONE);
                  signInButton.setVisibility(View.VISIBLE);
                  analyticsTracker.betaSignInConfirmationAccepted();
                  Snackbar.make(getView(), "¡Ole la gente valiente!", Snackbar.LENGTH_SHORT).show();
              })
              .setNegativeButton("No", (dialog1, which1) -> {
                  analyticsTracker.betaSignInConfirmationRejected();
                  Snackbar.make(getView(), "No hay problema", Snackbar.LENGTH_SHORT).show();
              })
              .setNeutralButton("Más info", (dialog2, which2) -> showLoginMoreInfo())
              .show();
        });

        view.findViewById(R.id.sign_in_feedback_mail).setOnClickListener(v -> {
            Intent emailIntent = EmailIntentBuilder.from(getActivity())
              .to("sevibus@sloydev.com")
              .subject("Feedback: Sincronización de datos")
              .build();
            startActivity(Intent.createChooser(emailIntent, "Enviar mail vía..."));
            analyticsTracker.betaSignInFeedbackMail();
        });

        view.findViewById(R.id.sign_in_feedback_twitter).setOnClickListener(v -> {
            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/sevibus"));
            startActivity(twitterIntent);
            analyticsTracker.betaSignInFeedbackTwitter();
        });

        view.findViewById(R.id.sign_in_feedback_gplus).setOnClickListener(v -> {
            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/111285770934051376534"));
            startActivity(twitterIntent);
            analyticsTracker.betaSignInFeedbackGplus();
        });
    }

    private void showLoginMoreInfo() {
        analyticsTracker.betaSignInConfirmationMoreInfo();
        Intent infoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sevibus.sloydev.com/beta_login_info.php"));
        startActivity(infoIntent);
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
        if (!remoteConfiguration.isLoginEnabled()) {
            getView().findViewById(R.id.main_login_root).setVisibility(View.GONE);
            return;
        }
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(v -> {
            startActivityForResult(loginController.loginIntent(((LocationProviderActivity) getActivity()).getGoogleApiClient()), RC_SIGN_IN);
        });

        signOutButton.setOnClickListener(v -> {
            analyticsTracker.signInLogout();
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
        TimeTracker timeTracker = new TimeTracker();
        loginController.obtainOAuthTokenFromSignInResult(getActivity().getApplicationContext(), data)
          .flatMap(oauthToken -> logInAction.logIn(oauthToken))
          .subscribe((sevibusUser) -> {
                analyticsTracker.signInSuccess(timeTracker.calculateInterval());
                showUserProfile(sevibusUser);
                Snackbar.make(getView(), "Sincronizando favoritas...", Snackbar.LENGTH_SHORT).show();
                favoritasPresenter.update();
            },
            throwable -> {
                analyticsTracker.signInFailure();
                StuffProvider.getCrashReportingTool().registerHandledException(throwable);
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
