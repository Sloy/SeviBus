package com.sloy.sevibus.ui.mvp.presenter;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.actions.ObtainFavoritasAction;

import java.util.List;

public class FavoritasMainPresenter implements Presenter<FavoritasMainPresenter.View> {

    private final ObtainFavoritasAction obtainFavoritasAction;

    private View view;

    public FavoritasMainPresenter(ObtainFavoritasAction obtainFavoritasAction) {
        this.obtainFavoritasAction = obtainFavoritasAction;
    }

    @Override
    public void initialize(View view) {
        this.view = view;
        view.showLoading();
        loadFavoritas();
    }

    @Override
    public void update() {
        loadFavoritas();
    }

    @Override
    public void pause() {
    }

    private void loadFavoritas() {
        obtainFavoritasAction.getFavoritas()
          .subscribe(favoritas -> {
              view.hideLoading();
              if (favoritas.isEmpty()) {
                  view.showEmpty();
                  view.hideFavoritas();
              } else {
                  view.hideEmpty();
                  view.showFavoritas(favoritas);
              }
          });
    }

    public interface View {

        void showLoading();

        void hideLoading();

        void showEmpty();

        void hideEmpty();

        void showFavoritas(List<Favorita> favoritas);

        void hideFavoritas();
    }

}
