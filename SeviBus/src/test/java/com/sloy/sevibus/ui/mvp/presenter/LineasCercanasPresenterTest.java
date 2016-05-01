package com.sloy.sevibus.ui.mvp.presenter;

import android.location.Location;

import com.sloydev.gallego.Optional;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.resources.LocationProvider;
import com.sloy.sevibus.resources.actions.ObtainLineasCercanasAction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.error;

public class LineasCercanasPresenterTest {

    @Mock
    LineasCercanasPresenter.View view;
    @Mock
    LocationProvider locationProvider;
    @Mock
    ObtainLineasCercanasAction obtainLineasCercanasAction;

    private LineasCercanasPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new LineasCercanasPresenter(locationProvider, obtainLineasCercanasAction);
    }

    @Test
    public void shows_loading_when_initialized() throws Exception {
        presenter.initialize(view);

        verify(view).showLoading();
    }

    @Test
    public void shows_lineas_when_updated_with_cercanas_available() throws Exception {
        when(locationProvider.observe()).thenReturn(aLocation());
        when(obtainLineasCercanasAction.obtainLineas(any(Location.class))).thenReturn(Observable.just(lineaCercana()));

        presenter.initialize(view);
        presenter.update();

        verify(view).showLineas(anyListOf(Linea.class));
        verify(view).hideEmpty();
        verify(view).hideLoading();
    }

    @Test
    public void shows_empty_when_updated_with_no_cercanas() throws Exception {
        when(locationProvider.observe()).thenReturn(aLocation());
        when(obtainLineasCercanasAction.obtainLineas(any(Location.class))).thenReturn(empty());

        presenter.initialize(view);
        presenter.update();

        verify(view).showEmpty();
        verify(view).hideLineas();
        verify(view).hideLoading();
    }


    @Test
    public void shows_error_when_updated_and_location_is_abstent() throws Exception {
        when(locationProvider.observe()).thenReturn(Observable.just(Optional.absent()));
        when(obtainLineasCercanasAction.obtainLineas(any(Location.class))).thenReturn(empty());

        presenter.initialize(view);
        presenter.update();

        verify(view).hideLoading();
        verify(view).showError();
    }

    @Test
    public void shows_error_when_location_fails() throws Exception {
        when(locationProvider.observe()).thenReturn(error(new Exception()));

        presenter.initialize(view);
        presenter.update();

        verify(view).hideLoading();
        verify(view).showError();
    }

    @Test
    public void shows_error_when_obtain_lineas_fails() throws Exception {
        when(locationProvider.observe()).thenReturn(aLocation());
        when(obtainLineasCercanasAction.obtainLineas(any(Location.class))).thenReturn(error(new Exception()));

        presenter.initialize(view);
        presenter.update();

        verify(view).hideLoading();
        verify(view).showError();
    }


    private Linea lineaCercana() {
        return new Linea();
    }

    private static Observable<Optional<Location>> aLocation() {
        return Observable.just(Optional.of(new Location("test")));
    }
}