package com.sloy.sevibus.ui.mvp.presenter;

import android.location.Location;

import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.actions.ObtainLineasCercanasAction;

import java.util.List;

import rx.Subscription;

public class LineasCercanasPresenter {

    private final LocationProvider locationProvider;
    private final ObtainLineasCercanasAction obtainLineasCercanasAction;

    private View view;
    private Subscription locationSubscription;

    public LineasCercanasPresenter(LocationProvider locationProvider, ObtainLineasCercanasAction obtainLineasCercanasAction) {
        this.locationProvider = locationProvider;
        this.obtainLineasCercanasAction = obtainLineasCercanasAction;
    }

    public void initialize(View view) {
        this.view = view;
        view.showLoading();
    }

    public void update() {
        locationSubscription = locationProvider.observe()
          .subscribe(locationOptional -> {
              if (locationOptional.isPresent()) {
                  obtainLineasCercanas(locationOptional.get());
              } else {
                  view.hideLoading();
                  view.showError();
              }
          }, throwable -> {
              view.hideLoading();
              view.showError();
          });
    }

    public void pause() {
        if (!locationSubscription.isUnsubscribed()) {
            locationSubscription.unsubscribe();
        }
    }

    private void obtainLineasCercanas(Location location) {
        obtainLineasCercanasAction.obtainLineas(location)
          .toList()
          .subscribe(lineas -> {
              view.hideLoading();
              if (!lineas.isEmpty()) {
                  view.hideEmpty();
                  view.showLineas(lineas);
              } else {
                  view.hideLineas();
                  view.showEmpty();
              }
          });
    }

    public interface View {

        void showLineas(List<Linea> lineas);

        void hideLineas();

        void showLoading();

        void hideLoading();

        void showEmpty();

        void hideEmpty();

        void showError();
    }
}
