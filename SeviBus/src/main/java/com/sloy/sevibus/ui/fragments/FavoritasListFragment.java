package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.adapters.DragFavoritaCallback;
import com.sloy.sevibus.ui.adapters.FavoritasAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoritasListFragment extends BaseDBFragment implements EditarFavoritaDialogFragment.OnGuardarFavoritaListener {

    private View emptyIndicator;
    private RecyclerView list;
    private FavoritasAdapter favoritasAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favoritas_list, container, false);
        list = (RecyclerView) v.findViewById(android.R.id.list);
        emptyIndicator = v.findViewById(R.id.favoritas_emtpy_indicator);

        favoritasAdapter = new FavoritasAdapter(favorita -> {
            Integer numero = favorita.getParadaAsociada().getNumero();
            startActivity(ParadaInfoActivity.getIntent(getActivity(), numero));
        }, this::guardarNuevoOrden);

        list.setAdapter(favoritasAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new DragFavoritaCallback(favoritasAdapter));
        touchHelper.attachToRecyclerView(list);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        recargaListaFavoritas();
    }

    private void recargaListaFavoritas() {
        try {
            List<Favorita> favoritas = DBQueries.getParadasFavoritas(getDBHelper());
            if (favoritas.isEmpty()) {
                emptyIndicator.setVisibility(View.VISIBLE);
                list.setVisibility(View.GONE);
            } else {
                emptyIndicator.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }

            for (Favorita favorita : favoritas) {
                List<Linea> lineas = DBQueries.getLineasDeParada(getDBHelper(), favorita.getParadaAsociada().getNumero());
                List<String> numeroLineas = new ArrayList<>(lineas.size());
                for (Linea linea : lineas) {
                    numeroLineas.add(linea.getNumero());
                }
                favorita.getParadaAsociada().setNumeroLineas(numeroLineas);
            }

            favoritasAdapter.setFavoritas(favoritas);
        } catch (Exception e) {
            Debug.registerHandledException(getActivity(), e);
            Snackbar.make(getView(), "Ocurrió un error. ¡Estamos en ello!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void eliminarFavoritaSeleccionada() {
        //TODO
//        getDBHelper().getDaoFavorita().delete();
        Snackbar.make(getView(), "Favorita eliminada", Snackbar.LENGTH_LONG).show();
        recargaListaFavoritas();
    }

    private void editarFavoritaSeleccionada() {
        //TODO
//        EditarFavoritaDialogFragment.getInstanceEditFavorita(this, fav).show(getFragmentManager(), EditarFavoritaDialogFragment.TAG);
    }

    private void guardarNuevoOrden(List<Favorita> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            Favorita favorita = ordered.get(i);
            favorita.setOrden(i);
        }
        try {
            DBQueries.setParadasFavoritas(getDBHelper(), ordered);
        } catch (SQLException e) {
            Snackbar.make(getView(), "Oops ocurrió un error", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onGuardarFavorita(int id, String nombrePropio, int color) {
        try {
            Favorita currentFav = DBQueries.getFavoritaById(getDBHelper(), id);
            if (currentFav == null) {
                Parada parada = DBQueries.getParadaById(getDBHelper(), id);
                DBQueries.setNewParadaFavorita(getDBHelper(), parada, nombrePropio, color);
            } else {
                currentFav.setNombrePropio(nombrePropio);
                currentFav.setColor(color);
                //Guarda
                DBQueries.updateParadaFavorita(getDBHelper(), currentFav);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Snackbar.make(getView(), "Favorita guardada", Snackbar.LENGTH_LONG).show();
        recargaListaFavoritas();
    }

}
