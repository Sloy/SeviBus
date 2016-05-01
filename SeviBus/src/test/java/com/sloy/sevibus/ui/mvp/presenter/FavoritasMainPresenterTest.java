package com.sloy.sevibus.ui.mvp.presenter;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.actions.ObtainFavoritasAction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FavoritasMainPresenterTest {

    @Mock
    FavoritasMainPresenter.View view;
    @Mock
    ObtainFavoritasAction obtainFavoritasAction;

    private FavoritasMainPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new FavoritasMainPresenter(obtainFavoritasAction);
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.empty());
    }

    @Test
    public void shows_loading_when_initialized() throws Exception {
        presenter.initialize(view);

        verify(view).showLoading();
    }

    @Test
    public void hides_loading_when_received_empty_list() throws Exception {
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.just(emptyList()));

        presenter.initialize(view);

        verify(view).hideLoading();
    }

    @Test
    public void hides_loading_when_received_any_list() throws Exception {
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.just(favoritasListOf(4)));

        presenter.initialize(view);

        verify(view).hideLoading();
    }

    @Test
    public void shows_empty_when_received_empty_list() throws Exception {
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.just(emptyList()));

        presenter.initialize(view);

        verify(view).showEmpty();
    }

    @Test
    public void hides_empty_when_received_some_favoritas() throws Exception {
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.just(favoritasListOf(4)));

        presenter.initialize(view);

        verify(view).hideEmpty();
    }

    @Test
    public void shows_favoritas_when_received_some_favoritas() throws Exception {
        List<Favorita> favoritas = favoritasListOf(4);
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.just(favoritas));

        presenter.initialize(view);

        verify(view).showFavoritas(favoritas);
    }

    @Test
    public void shows_error_when_received_an_error() throws Exception {
        when(obtainFavoritasAction.getFavoritas()).thenReturn(Observable.error(new Exception()));

        presenter.initialize(view);

        verify(view).showError();
    }

    private List<Favorita> favoritasListOf(int count) {
        List<Favorita> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Parada p = new Parada(i, String.valueOf(i), 0d, 0d);
            Favorita f = new Favorita();
            f.setOrden(i);
            f.setParadaAsociada(p);
            res.add(f);
        }
        return res;
    }
}