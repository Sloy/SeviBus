package com.sloy.sevibus.resources.actions.favorita;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.*;

public class ObtainFavoritasActionTest {


    @Mock
    FavoritaDataSource localDataSource;
    @Mock
    FavoritaDataSource remoteDataSource;

    private ObtainFavoritasAction obtainFavoritasAction;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        obtainFavoritasAction = new ObtainFavoritasAction(localDataSource, remoteDataSource);
        when(localDataSource.getFavoritas()).thenReturn(empty());
        when(remoteDataSource.getFavoritas()).thenReturn(empty());
        when(localDataSource.replaceFavoritas(anyListOf(Favorita.class))).then(i -> just(i.getArgumentAt(0, Favorita.class)));
    }

    @Test
    public void return_favoritas_from_local_first() throws Exception {
        List<Favorita> localFavs = singletonList(new Favorita());
        when(localDataSource.getFavoritas()).thenReturn(just(localFavs));

        List<List<Favorita>> results = toList(obtainFavoritasAction.getFavoritas());

        assertThat(results.get(0)).isSameAs(localFavs);
    }

    @Test
    public void return_favoritas_from_remote_second() throws Exception {
        List<Favorita> localFavs = singletonList(new Favorita());
        List<Favorita> remoteFavs = singletonList(new Favorita());
        when(localDataSource.getFavoritas()).thenReturn(just(localFavs));
        when(remoteDataSource.getFavoritas()).thenReturn(just(remoteFavs));

        List<List<Favorita>> results = toList(obtainFavoritasAction.getFavoritas());

        assertThat(results.get(1)).isSameAs(remoteFavs);
    }

    @Test
    public void return_one_result_when_remote_is_empty() throws Exception {
        List<Favorita> localFavs = singletonList(new Favorita());
        when(localDataSource.getFavoritas()).thenReturn(just(localFavs));
        when(remoteDataSource.getFavoritas()).thenReturn(empty());

        List<List<Favorita>> results = toList(obtainFavoritasAction.getFavoritas());

        assertThat(results).hasSize(1);
    }

    @Test
    public void replace_local_when_remote_returns_content() throws Exception {
        List<Favorita> remoteFavs = singletonList(new Favorita());
        when(remoteDataSource.getFavoritas()).thenReturn(just(remoteFavs));

        obtainFavoritasAction.getFavoritas().toBlocking().subscribe();

        verify(localDataSource).replaceFavoritas(remoteFavs);
    }

    public static <E> List<E> toList(Observable<E> observable) {
        return toList(observable.toBlocking().toIterable());
    }

    public static <E> List<E> toList(Iterable<E> iter) {
        List<E> list = new ArrayList<>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
}