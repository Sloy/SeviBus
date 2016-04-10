package com.sloy.sevibus.resources.actions;

import android.location.Location;

import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.TipoLinea;
import com.sloy.sevibus.resources.datasource.LineaDataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ObtainLineasCercanasActionTest {

    private static final TipoLinea SOME_TYPE = new TipoLinea(1, "");
    private static final int P1_NUMBER = 1;
    private static final int P2_NUMBER = 2;
    private static final Linea LINEA_A = linea(1, "A");
    private static final Linea LINEA_B = linea(2, "B");
    private static final ParadaCercana PARADA_1 = cercana(P1_NUMBER);
    private static final ParadaCercana PARADA_2 = cercana(P2_NUMBER);
    private static final Location SOME_LOCATION = new Location("");

    @Mock
    ObtainCercanasAction obtainCercanasAction;

    @Mock
    LineaDataSource lineaDataSource;
    private ObtainLineasCercanasAction action;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new ObtainLineasCercanasAction(lineaDataSource, obtainCercanasAction);
    }

    @Test
    public void returns_distinct_stops_have_distinct_lines() throws Exception {
        when(lineaDataSource.getFromParada(P1_NUMBER)).thenReturn(lineas(LINEA_A));
        when(lineaDataSource.getFromParada(P2_NUMBER)).thenReturn(lineas(LINEA_B));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.just(PARADA_1, PARADA_2));

        List<Linea> results = action.obtainLineas(SOME_LOCATION).toList().toBlocking().single();

        assertThat(results).containsSequence(LINEA_A, LINEA_B);
    }

    @Test
    public void returns_distinct_when_stops_have_common_lines() throws Exception {
        when(lineaDataSource.getFromParada(P1_NUMBER)).thenReturn(lineas(LINEA_A, LINEA_B));
        when(lineaDataSource.getFromParada(P2_NUMBER)).thenReturn(lineas(LINEA_B));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.just(PARADA_1, PARADA_2));

        List<Linea> results = action.obtainLineas(SOME_LOCATION).toList().toBlocking().single();

        assertThat(results).containsSequence(LINEA_A, LINEA_B);
    }

    @Test
    public void returns_ordered_when_stops_have_unordered_lines() throws Exception {
        when(lineaDataSource.getFromParada(P1_NUMBER)).thenReturn(lineas(LINEA_B));
        when(lineaDataSource.getFromParada(P2_NUMBER)).thenReturn(lineas(LINEA_A));
        when(obtainCercanasAction.obtainCercanas(any(Location.class))).thenReturn(Observable.just(PARADA_1, PARADA_2));

        List<Linea> results = action.obtainLineas(SOME_LOCATION).toList().toBlocking().single();

        assertThat(results).containsSequence(LINEA_A, LINEA_B);
    }

    private Observable<List<Linea>> lineas(Linea... lineaA) {
        return Observable.just(asList(lineaA));
    }

    private static Linea linea(int id, String label) {
        return new Linea(id, label, label, "", emptyList(), SOME_TYPE);
    }

    private static ParadaCercana cercana(Integer numero) {
        return new ParadaCercana(new Parada(numero, "", 0.0, 0.0), 0);
    }

}