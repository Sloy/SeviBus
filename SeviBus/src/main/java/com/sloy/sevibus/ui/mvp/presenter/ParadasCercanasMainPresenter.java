package com.sloy.sevibus.ui.mvp.presenter;

import android.location.Location;

import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.actions.ObtainCercanasAction;

import java.util.List;

import rx.Subscription;

public class ParadasCercanasMainPresenter implements Presenter<ParadasCercanasMainPresenter.View> {

    private final LocationProvider locationProvider;
    private final ObtainCercanasAction obtainCercanasAction;
    private View view;
    private Subscription locationSubscription;

    public ParadasCercanasMainPresenter(LocationProvider locationProvider, ObtainCercanasAction obtainCercanasAction) {
        this.locationProvider = locationProvider;
        this.obtainCercanasAction = obtainCercanasAction;
    }

    @Override
    public void initialize(View view) {
        this.view = view;
        view.showLoading();
    }

    @Override
    public void update() {
        locationSubscription = locationProvider.observe()
          .subscribe(locationOptional -> {
              if (locationOptional.isPresent()) {
                  obtainParadasCercanas(locationOptional.get());
              } else {
                  view.hideLoading();
                  view.showError();
              }
          },throwable -> {
              view.hideLoading();
              view.showError();
          });
    }

    private void obtainParadasCercanas(Location location) {
        obtainCercanasAction.obtainCercanas(location)
          .toList()
          .subscribe(paradas -> {
                view.hideLoading();
                if (paradas.isEmpty()) {
                    view.showEmpty();
                    view.hideParadas();
                } else {
                    view.hideEmpty();
                    view.showParadas(paradas);
                }
            },
            throwable -> {
                view.showError();
                view.hideLoading();
            });
    }

    @Override
    public void pause() {
        locationSubscription.unsubscribe();
    }

    public interface View {
        void showEmpty();

        void hideEmpty();

        void showParadas(List<ParadaCercana> paradas);

        void hideParadas();

        void showLoading();

        void hideLoading();

        void showError();
    }

}
