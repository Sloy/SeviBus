package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.ObtainFavoritasAction;
import com.sloy.sevibus.resources.actions.ReorderFavoritasAction;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.adapters.DragFavoritaCallback;
import com.sloy.sevibus.ui.adapters.FavoritasAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FavoritasListFragment extends BaseDBFragment {

    private View emptyIndicator;
    private RecyclerView list;
    private FavoritasAdapter favoritasAdapter;
    private ItemTouchHelper itemTouchHelper;
    private ObtainFavoritasAction obtainFavoritasAction;
    private ReorderFavoritasAction reorderFavoritasAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favoritas_list, container, false);
        list = (RecyclerView) v.findViewById(android.R.id.list);
        emptyIndicator = v.findViewById(R.id.favoritas_emtpy_indicator);

        //noinspection Convert2MethodRef
        favoritasAdapter = new FavoritasAdapter(favorita -> {
            Integer numero = favorita.getParadaAsociada().getNumero();
            startActivity(ParadaInfoActivity.getIntent(getActivity(), numero));
        },
          (ordered) -> guardarNuevoOrden(ordered),
          (viewHolder) -> itemTouchHelper.startDrag(viewHolder));

        list.setAdapter(favoritasAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        itemTouchHelper = new ItemTouchHelper(new DragFavoritaCallback(favoritasAdapter));
        itemTouchHelper.attachToRecyclerView(list);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        obtainFavoritasAction = StuffProvider.getObtainFavoritasAction(getActivity());
        reorderFavoritasAction = StuffProvider.getReorderFavoritasAction(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        recargaListaFavoritas();
    }

    private void recargaListaFavoritas() {
        obtainFavoritasAction.getFavoritas()
          .subscribe(this::showFavoritasInList);
    }

    private void showFavoritasInList(List<Favorita> favoritas) {
        if (favoritas.isEmpty()) {
            emptyIndicator.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        } else {
            emptyIndicator.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
        try {
            for (Favorita favorita : favoritas) {
                List<Linea> lineas = DBQueries.getLineasDeParada(getDBHelper(), favorita.getParadaAsociada().getNumero());
                List<String> numeroLineas = new ArrayList<>(lineas.size());
                for (Linea linea : lineas) {
                    numeroLineas.add(linea.getNumero());
                }
                favorita.getParadaAsociada().setNumeroLineas(numeroLineas);
            }
        } catch (Exception e) {
            Debug.registerHandledException(e);
            Snackbar.make(getView(), "Ocurrió un error. ¡Estamos en ello!", Snackbar.LENGTH_LONG).show();
        }
        favoritasAdapter.setFavoritas(favoritas);
    }


    private void guardarNuevoOrden(List<Favorita> ordered) {
        reorderFavoritasAction
          .setNewOrder(ordered)
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe();
    }

}
