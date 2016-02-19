package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.actions.favorita.SaveFavoritaAction;
import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SaveFavoritaActionTest {

    private static final int PARADA_ID = 1;
    private static final String STUB_NAME = "name";
    private static final int STUB_COLOR = 0;

    @Mock
    FavoritaDataSource favoritaLocalDataSource;
    @Mock
    FavoritaDataSource favoritaRemoteDataSource;

    @Captor
    ArgumentCaptor<Favorita> favoritaCaptor;

    private TestableSaveFavoritaAction saveFavoritaAction;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        saveFavoritaAction = new TestableSaveFavoritaAction(favoritaLocalDataSource, favoritaRemoteDataSource);
    }

    @Test
    public void saves_favorita_with_order_1_when_there_arent_favoritas() throws Exception {
        givenThereAreNoFavoritas();
        givenFavoritaDoesntExist();
        saveFavoritaAction.setStubParada(new Parada(PARADA_ID, "", 0.0, 0.0));

        saveFavoritaAction.saveFavorita(PARADA_ID, STUB_NAME, STUB_COLOR)
          .subscribe();

        verify(favoritaLocalDataSource).saveFavorita(favoritaCaptor.capture());
        assertThat(favoritaCaptor.getValue().getOrden())
          .isEqualTo(1);

    }

    @Test
    public void saves_favorita_with_order_4_when_last_order_is_3_and_there_are_2_favoritas() throws Exception {
        givenFavoritaDoesntExist();
        givenThereAreFavoritas(favoritaWithOrder(1), favoritaWithOrder(3));
        saveFavoritaAction.setStubParada(new Parada(PARADA_ID, "", 0.0, 0.0));

        saveFavoritaAction.saveFavorita(PARADA_ID, STUB_NAME, STUB_COLOR)
          .subscribe();

        verify(favoritaLocalDataSource).saveFavorita(favoritaCaptor.capture());
        assertThat(favoritaCaptor.getValue().getOrden())
          .isEqualTo(4);
    }

    private void givenThereAreFavoritas(Favorita... favoritas) {
        when(favoritaLocalDataSource.getFavoritas()).thenReturn(Observable.just(asList(favoritas)));
    }

    private void givenFavoritaDoesntExist() {
        when(favoritaLocalDataSource.getFavoritaById(PARADA_ID)).thenReturn(Observable.empty());
    }

    private void givenThereAreNoFavoritas() {
        when(favoritaLocalDataSource.getFavoritas()).thenReturn(Observable.just(emptyList()));
    }

    private Favorita favoritaWithOrder(int orden) {
        Favorita fav3 = new Favorita();
        fav3.setOrden(orden);
        return fav3;
    }

    private static class TestableSaveFavoritaAction extends SaveFavoritaAction {

        private Parada stubParada;

        public TestableSaveFavoritaAction(FavoritaDataSource favoritaLocalDataSource, FavoritaDataSource favoritaRemoteDataSource) {
            super(favoritaLocalDataSource, favoritaRemoteDataSource, null);
        }

        public void setStubParada(Parada stubParada) {
            this.stubParada = stubParada;
        }


        @Override
        protected Parada getParadaById(Integer id) {
            return stubParada;
        }

    }
}