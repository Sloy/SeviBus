package com.sloy.sevibus.ui.mvp.presenter;

import android.location.Location;

import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.actions.ObtainCercanasAction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParadasCercanasMainPresenterTest {

    @Mock
    ParadasCercanasMainPresenter.View view;
    @Mock
    LocationProvider locationProvider;
    @Mock
    ObtainCercanasAction obtainCercanasAction;

    private ParadasCercanasMainPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new ParadasCercanasMainPresenter(locationProvider, obtainCercanasAction);
    }

    @Test
    public void shows_loading_when_initialized() throws Exception {
        presenter.initialize(view);

        verify(view).showLoading();
    }

    @Test
    public void shows_paradas_when_updated_with_cercanas_available() throws Exception {
        when(locationProvider.observe()).thenReturn(Observable.just(new Location("test")));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.just(paradaCercana()));

        presenter.initialize(view);
        presenter.update();

        verify(view).showParadas(anyListOf(ParadaCercana.class));
        verify(view).hideEmpty();
        verify(view).hideLoading();
    }

    @Test
    public void shows_empty_when_updated_with_no_cercanas() throws Exception {
        when(locationProvider.observe()).thenReturn(Observable.just(new Location("test")));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.empty());

        presenter.initialize(view);
        presenter.update();

        verify(view).showEmpty();
        verify(view).hideParadas();
        verify(view).hideLoading();
    }

    @Test
    public void shows_error_when_updated_and_error_occurrs_retrieving_cercanas() throws Exception {
        when(locationProvider.observe()).thenReturn(Observable.just(new Location("test")));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.error(new RuntimeException()));

        presenter.initialize(view);
        presenter.update();

        verify(view).hideLoading();
        verify(view).showError();
    }

    @Test
    public void shows_error_when_updated_and_error_occurrs_retrieving_location() throws Exception {
        when(locationProvider.observe()).thenReturn(Observable.error(new RuntimeException()));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.just(paradaCercana()));

        presenter.initialize(view);
        presenter.update();

        verify(view).hideLoading();
        verify(view).showError();
    }

    private ParadaCercana paradaCercana() {
        return new ParadaCercana(new Parada(), 0);
    }

}