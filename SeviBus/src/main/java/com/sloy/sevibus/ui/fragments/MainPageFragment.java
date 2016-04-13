package com.sloy.sevibus.ui.fragments;

import android.content.Context;
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
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.activities.BusquedaActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.mvp.presenter.FavoritasMainPresenter;
import com.sloy.sevibus.ui.mvp.presenter.LineasCercanasPresenter;
import com.sloy.sevibus.ui.mvp.presenter.ParadasCercanasMainPresenter;
import com.sloy.sevibus.ui.mvp.view.FavoritasMainViewContainer;
import com.sloy.sevibus.ui.mvp.view.LineasCercanasViewContainer;
import com.sloy.sevibus.ui.mvp.view.ParadasCercanasMainViewContainer;

public class MainPageFragment extends BaseDBFragment {

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
