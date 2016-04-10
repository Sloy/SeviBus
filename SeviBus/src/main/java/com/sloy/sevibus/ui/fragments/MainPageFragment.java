package com.sloy.sevibus.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.activities.BusquedaActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.mvp.presenter.FavoritasMainPresenter;
import com.sloy.sevibus.ui.mvp.presenter.LineasCercanasPresenter;
import com.sloy.sevibus.ui.mvp.presenter.ParadasCercanasMainPresenter;
import com.sloy.sevibus.ui.mvp.view.FavoritasMainViewContainer;
import com.sloy.sevibus.ui.mvp.view.LineasCercanasViewContainer;
import com.sloy.sevibus.ui.mvp.view.ParadasCercanasMainViewContainer;

import de.cketti.library.changelog.ChangeLog;

public class MainPageFragment extends BaseDBFragment {

    private static final String FRAG_PARADAS_CERCANAS = "f_p_cercanas";
    private static final String FRAG_LINEAS_CERCANAS = "f_l_cercanas";

    private static final String PREF_SHOW_NEW_VERSION_LATEST_SEEN = "newversion_last_seen";
    private static final int NEW_VERSION_SNACKBAR_DURATION = 10000;

    private FavoritasMainPresenter favoritasPresenter;
    private ParadasCercanasMainPresenter cercanasPresenter;
    private LineasCercanasPresenter lineasCercanasPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        favoritasPresenter = new FavoritasMainPresenter(StuffProvider.getObtainFavoritasAction(getActivity()));
        LocationProvider locationProvider = ((LocationProviderActivity) getActivity()).getLocationProvider();
        cercanasPresenter = new ParadasCercanasMainPresenter(locationProvider, StuffProvider.getObtainCercanasAction(context));
        lineasCercanasPresenter = new LineasCercanasPresenter(locationProvider, StuffProvider.getObtainLineasCercanasAction(context));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoritasPresenter.initialize(new FavoritasMainViewContainer(view.findViewById(R.id.fragment_main_favoritas)));

        ParadasCercanasMainViewContainer paradasCercanasView = new ParadasCercanasMainViewContainer(view.findViewById(R.id.fragment_main_paradas_cercanas));
        cercanasPresenter.initialize(paradasCercanasView);
        paradasCercanasView.setupMapa(getChildFragmentManager());

        LineasCercanasViewContainer lineasCercanasView = new LineasCercanasViewContainer(view.findViewById(R.id.fragment_main_lineas_cercanas));
        lineasCercanasPresenter.initialize(lineasCercanasView);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    private void setupUI() {
        setupNewVersion();
    }

    private void setupNewVersion() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE);
        int lastVersion = prefs.getInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, 0);
        int currentVersion = BuildConfig.VERSION_CODE;

        if (lastVersion < currentVersion) {
            ChangeLog cl = new ChangeLog(getActivity());
            boolean hasChangelog = !cl.getChangeLog(false).isEmpty();
            if (hasChangelog) {
                new Handler().postDelayed(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }
                    //noinspection ResourceType
                    Snackbar.make(getView(), "SeviBus se ha actualizado!", NEW_VERSION_SNACKBAR_DURATION)
                      .setAction("Ver cambios", v -> {
                          cl.getFullLogDialog().show();
                      })
                      .show();
                }, 1000);
            }
        }

        prefs.edit().putInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, currentVersion).apply();
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
