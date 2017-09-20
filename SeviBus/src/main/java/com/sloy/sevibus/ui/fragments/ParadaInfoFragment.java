package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.domain.model.LineaCollection;
import com.sloy.sevibus.domain.model.ParadaCollection;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.resources.AlertasManager;
import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.TimeTracker;
import com.sloy.sevibus.resources.actions.favorita.DeleteFavoritaAction;
import com.sloy.sevibus.resources.actions.favorita.ObtainSingleFavoritaAction;
import com.sloy.sevibus.resources.actions.favorita.SaveFavoritaAction;
import com.sloy.sevibus.resources.actions.llegada.ObtainLlegadasAction;
import com.sloy.sevibus.ui.ParadaInfoViewModel;
import com.sloy.sevibus.ui.activities.BaseActivity;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.widgets.LlegadasList;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ParadaInfoFragment extends BaseDBFragment implements EditarFavoritaDialogFragment.OnGuardarFavoritaListener {

    private ObtainLlegadasAction obtainLlegadasAction;
    private SaveFavoritaAction saveFavoritaAction;

    private LlegadasList mViewLlegadas;

    private TextView mViewNombreParada;
    private FloatingActionButton favoritaButton;

    @Nullable
    private ParadaInfoViewModel paradaInfoViewModel;

    private AnalyticsTracker analyticsTracker;
    private ObtainSingleFavoritaAction obtainSingleFavoritaAction;
    private DeleteFavoritaAction deleteFavoritaAction;
    private ParadaCollection paradaCollection;
    private LineaCollection lineaCollection;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_parada_info, container, false);
        mViewLlegadas = (LlegadasList) v.findViewById(R.id.parada_info_tiempos_llegadas);

        mViewNombreParada = (TextView) v.findViewById(R.id.parada_info_nombre);
        favoritaButton = (FloatingActionButton) v.findViewById(R.id.parada_info_favorita_fab);

        Toolbar toolbar = ((Toolbar) v.findViewById(R.id.toolbar));
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (!isNetworkAvailable()) {
            Snackbar.make(getView(), "No hay conexión a Internet, y es necesaria", Snackbar.LENGTH_LONG).show();
        }

        lineaCollection = StuffProvider.getLineaCollection(getActivity());
        paradaCollection = StuffProvider.getParadaCollection(getActivity());
        obtainLlegadasAction = StuffProvider.getObtainLlegadaAction(getActivity());
        obtainSingleFavoritaAction = StuffProvider.getObtainSingleFavoritaAction(getActivity());
        saveFavoritaAction = StuffProvider.getSaveFavoritaAction(getActivity());
        deleteFavoritaAction = StuffProvider.getDeleteFavoritaAction(getActivity());
        analyticsTracker = StuffProvider.getAnalyticsTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.parada_info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_actualizar:
                if (paradaInfoViewModel != null) {
                    updateLlegadas(paradaInfoViewModel);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCrearFavoritaClick(Parada parada) {
        EditarFavoritaDialogFragment.getInstanceNewFavorita(this, parada).show(
                getFragmentManager(), EditarFavoritaDialogFragment.TAG);
    }

    private void onEliminarFavoritaClick(final Parada parada) {
        new AlertDialog.Builder(getActivity()).setMessage("Esta parada está guardada como favorita. ¿Quieres eliminarla?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarFavorita(parada);
                    }
                })
                .setNegativeButton("No no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void guardarFavorita(String nombrePropio, int color, Parada parada) {
        saveFavoritaAction.saveFavorita(parada.getNumero(), nombrePropio, color)
                .subscribe();
        Snackbar.make(getView(), "Favorita guardada", Snackbar.LENGTH_LONG).show();
        updateFavoritaButton(parada);
    }

    private void eliminarFavorita(Parada parada) {
        deleteFavoritaAction
                .deleteFavorita(parada.getNumero())
                .subscribe();
        Snackbar.make(getView(), "Quitada favorita", Snackbar.LENGTH_LONG).show();
        updateFavoritaButton(parada);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle extras = getActivity().getIntent().getExtras();
        int paradaId = extras.getInt("parada_numero");
        analyticsTracker.paradaViewed(paradaId);

        paradaCollection.getById(paradaId)
                .zipWith(lineaCollection.getByParada(paradaId).toSortedList().toSingle(), ParadaInfoViewModel::new)
                .subscribe(paradaInfo -> {
                    paradaInfoViewModel = paradaInfo;
                    guardaReciente(paradaInfo.getParada());
                    cargaInfoDeParada(paradaInfo);
                });

        EditarFavoritaDialogFragment f = (EditarFavoritaDialogFragment) getFragmentManager().findFragmentByTag(EditarFavoritaDialogFragment.TAG);
        if (f != null) {
            f.setOnGuardarFavoritaListener(this);
        }
    }

    private void guardaReciente(final Parada parada) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Reciente reciente = new Reciente();
                reciente.setCreatedAt(System.currentTimeMillis());
                reciente.setParadaAsociada(parada);
                try {
                    DBQueries.setParadaReciente(getDBHelper(), reciente);
                } catch (SQLException e) {
                    Debug.registerHandledException(e);
                }
                return null;
            }
        }.execute();
    }

    private void cargaInfoDeParada(ParadaInfoViewModel paradaInfo) {
        mViewLlegadas.setLineas(paradaInfo.getLineas());
        mViewNombreParada.setText(paradaInfo.getParada().getDescripcion());

        updateFavoritaButton(paradaInfo.getParada());

        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(String.format(getString(R.string.parada_info_titulo), paradaInfo.getParada().getNumero()));

        updateLlegadas(paradaInfo);

        cargaAlertas(paradaInfo.getLineas());
    }

    private void cargaAlertas(final List<Linea> lineas) {
        if (getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE).getBoolean("pref_alertas", true)) {
            new AsyncTask<Void, Void, SparseArray<List<LineaWarning>>>() {
                @Override
                protected SparseArray<List<LineaWarning>> doInBackground(Void... voids) {
                    SparseArray<List<LineaWarning>> res = new SparseArray<>();
                    for (Linea linea : lineas) {
                        try {
                            List<LineaWarning> warnings = AlertasManager.getWarnings(getActivity(), getDBHelper(), linea);
                            if (warnings != null && !warnings.isEmpty()) {
                                res.put(linea.getId(), warnings);
                            }
                        } catch (Exception e) {
                            Debug.registerHandledException(e);
                        }
                    }
                    return res;
                }

                @Override
                protected void onPostExecute(SparseArray<List<LineaWarning>> res) {
                    if (mViewLlegadas != null && res != null && res.size() > 0) {
                        mViewLlegadas.setWarnings(res);
                    }
                }
            }.execute();
        }
    }

    private void updateFavoritaButton(Parada parada) {
        obtainSingleFavoritaAction
                .obtainFavorita(parada.getNumero())
                .subscribe(favorita -> {
                            if (favorita.isPresent()) {
                                colorizeScreenFromFavorita(favorita.get());
                                favoritaButton.setImageResource(R.drawable.ic_fab_star_outline);
                                favoritaButton.setOnClickListener(v -> onEliminarFavoritaClick(parada));
                            } else {
                                favoritaButton.setImageResource(R.drawable.ic_fab_star);
                                favoritaButton.setOnClickListener(v -> onCrearFavoritaClick(parada));
                            }
                        },
                        throwable -> {
                            Debug.registerHandledException(throwable);
                            if (isAdded()) {
                                Snackbar.make(getView(), R.string.error_message_generic, Snackbar.LENGTH_SHORT).show();
                            }
                        });
    }

    private void colorizeScreenFromFavorita(Favorita fav) {
        PaletaColores paleta = PaletaColores.fromPrimary(fav.getColor());
        // Toolbar
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) getView().findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setContentScrimColor(paleta.primary);
        collapsingToolbar.setBackgroundColor(paleta.primary);
        getView().findViewById(R.id.toolbar).setBackgroundColor(paleta.primary);

        // Status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(paleta.dark);
        }

        // FAB
        favoritaButton.setBackgroundTintList(ColorStateList.valueOf(paleta.accent));

        // Other content
        ((TextView) getView().findViewById(R.id.parada_info_tiempos_llegadas_title))
                .setTextColor(paleta.primary);

        analyticsTracker.favoritaColorized(paleta, fav.getParadaAsociada().getNumero());

    }

    private void updateLlegadas(ParadaInfoViewModel paradaInfo) {
        TimeTracker timeTracker = new TimeTracker();

        Observable.from(paradaInfo.getLineas())
                .map(Linea::getNumero)
                .doOnNext(mViewLlegadas::setLlegadaCargando)
                .toList()
                .flatMap(lineas -> obtainLlegadasAction.getLlegadas(paradaInfo.getParada().getNumero(), lineas))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(llegada -> {
                            mViewLlegadas.setLlegadaInfo(llegada.getBusLineName(), llegada);
                            long responseTime = timeTracker.calculateInterval();
                            analyticsTracker.trackTiempoRecibido(llegada.getBusStopNumber(), llegada.getBusLineName(), responseTime, llegada.getDataSource());
                        },
                        error -> {
                            if (!isAdded()) {
                                return;
                            }
                            Debug.registerHandledException(error);
                            Snackbar.make(getView(), "Se produjo un error", Snackbar.LENGTH_LONG)
                                    .setAction("Reintentar", (view) -> updateLlegadas(paradaInfo))
                                    .show();
                            for (final Linea l : paradaInfo.getLineas()) {
                                mViewLlegadas.setLlegadaInfo(l.getNumero(), null);
                            }
                        });
    }

    @Override
    public void onGuardarFavorita(int id, String nombrePropio, int color) {
        if (paradaInfoViewModel != null) {
            guardarFavorita(nombrePropio, color, paradaInfoViewModel.getParada());
        }
    }
}
