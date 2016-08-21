package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.RemoteConfiguration;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.user.ObtainUserAction;
import com.sloy.sevibus.ui.activities.BusquedaActivity;
import com.sloy.sevibus.ui.activities.HomeActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.mvp.presenter.FavoritasMainPresenter;
import com.sloy.sevibus.ui.mvp.presenter.LineasCercanasPresenter;
import com.sloy.sevibus.ui.mvp.presenter.ParadasCercanasMainPresenter;
import com.sloy.sevibus.ui.mvp.view.FavoritasMainViewContainer;
import com.sloy.sevibus.ui.mvp.view.LineasCercanasViewContainer;
import com.sloy.sevibus.ui.mvp.view.ParadasCercanasMainViewContainer;
import com.sloydev.gallego.Optional;

public class MainPageFragment extends BaseDBFragment {

    private RemoteConfiguration remoteConfiguration;
    private ObtainUserAction obtainUserAction;

    private FavoritasMainPresenter favoritasPresenter;
    private ParadasCercanasMainPresenter cercanasPresenter;
    private LineasCercanasPresenter lineasCercanasPresenter;

    private View loginSuggestionCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home, container, false);
        loginSuggestionCard = view.findViewById(R.id.main_login_suggestion_root);
        loginSuggestionCard.findViewById(R.id.login_suggestion_button)
          .setOnClickListener(v -> ((HomeActivity) getActivity()).openNavigationMenu());
        return view;
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

        favoritasPresenter.initialize(new FavoritasMainViewContainer(view.findViewById(R.id.fragment_main_favoritas)));

        ParadasCercanasMainViewContainer paradasCercanasView = new ParadasCercanasMainViewContainer(view.findViewById(R.id.fragment_main_paradas_cercanas));
        cercanasPresenter.initialize(paradasCercanasView);
        paradasCercanasView.setupMapa(getChildFragmentManager());

        LineasCercanasViewContainer lineasCercanasView = new LineasCercanasViewContainer(view.findViewById(R.id.fragment_main_lineas_cercanas));
        lineasCercanasPresenter.initialize(lineasCercanasView);

        obtainUserAction = StuffProvider.getObtainUserAction(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        favoritasPresenter.update();
        cercanasPresenter.update();
        lineasCercanasPresenter.update();
        updateLoginSuggestion();
    }

    @Override
    public void onPause() {
        super.onPause();
        favoritasPresenter.pause();
        cercanasPresenter.pause();
        lineasCercanasPresenter.pause();
    }

    private void updateLoginSuggestion() {
        boolean loginSuggestionEnabled = remoteConfiguration.isLoginSuggestionEnabled();
        obtainUserAction.obtainUser()
          .map(Optional::isPresent)
          .map(isLoggedIn -> !isLoggedIn && loginSuggestionEnabled)
          .map(shouldShowLoginSuggestion -> shouldShowLoginSuggestion ? View.VISIBLE : View.GONE)
          .subscribe(loginSuggestionCard::setVisibility);
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


}
