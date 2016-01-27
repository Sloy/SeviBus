package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.MiAnuncio;
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
import com.sloy.sevibus.resources.actions.DeleteFavoritaAction;
import com.sloy.sevibus.resources.actions.ObtainLlegadasAction;
import com.sloy.sevibus.resources.actions.ObtainSingleFavoritaAction;
import com.sloy.sevibus.resources.actions.SaveFavoritaAction;
import com.sloy.sevibus.ui.activities.BaseActivity;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.widgets.LlegadasList;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ParadaInfoFragment extends BaseDBFragment implements EditarFavoritaDialogFragment.OnGuardarFavoritaListener {

    public static final String URL_ANUNCIO_PROPIO = "http://sevibus.sloydev.com/ads/getad.php?p=%d";
    private static final int AD_NET_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
    private static final int AD_NET_READ_TIMEOUT_MILLIS = 10 * 1000;

    private ObtainLlegadasAction obtainLlegadasAction;
    private SaveFavoritaAction saveFavoritaAction;

    private LlegadasList mViewLlegadas;

    private TextView mViewNombreParada;
    private FloatingActionButton favoritaButton;

    private FrameLayout mAnuncioContainer;

    private Parada mParada;
    private List<Linea> mLineas;

    private Map<String, ArrivalTime> mLlegadas;
    private AnalyticsTracker analyticsTracker;
    private ObtainSingleFavoritaAction obtainSingleFavoritaAction;
    private DeleteFavoritaAction deleteFavoritaAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_parada_info, container, false);
        mViewLlegadas = (LlegadasList) v.findViewById(R.id.parada_info_tiempos_llegadas);

        mViewNombreParada = (TextView) v.findViewById(R.id.parada_info_nombre);
        favoritaButton = (FloatingActionButton) v.findViewById(R.id.parada_info_favorita_fab);

        mAnuncioContainer = (FrameLayout) v.findViewById(R.id.anuncio);

        Toolbar toolbar = ((Toolbar) v.findViewById(R.id.toolbar));
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLlegadas = new HashMap<>();

        if (!isNetworkAvailable()) {
            Snackbar.make(getView(), "No hay conexión a Internet, y es necesaria", Snackbar.LENGTH_LONG).show();
        }

        obtainLlegadasAction = StuffProvider.getObtainLlegadaAction();
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
                updateLlegadas();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCrearFavoritaClick() {
        EditarFavoritaDialogFragment.getInstanceNewFavorita(this, mParada).show(
          getFragmentManager(), EditarFavoritaDialogFragment.TAG);
    }

    private void onEliminarFavoritaClick() {
        new AlertDialog.Builder(getActivity()).setMessage("Esta parada está guardada como favorita. ¿Quieres eliminarla?")
          .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  eliminarFavorita();
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

    private void guardarFavorita(String nombrePropio, int color) {
        saveFavoritaAction.saveFavorita(mParada.getNumero(), nombrePropio, color)
          .subscribe();
        Snackbar.make(getView(), "Favorita guardada", Snackbar.LENGTH_LONG).show();
        updateFavoritaButton();
    }

    private void eliminarFavorita() {
        deleteFavoritaAction
          .deleteFavorita(mParada.getNumero())
          .subscribe();
        Snackbar.make(getView(), "Quitada favorita", Snackbar.LENGTH_LONG).show();
        updateFavoritaButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mParada == null || mLineas == null) {
            int parada_numero = 0;
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras != null) {
                parada_numero = extras.getInt("parada_numero");
            }

            if (parada_numero < 1) {
                Log.e("SeviBus", "No se recibió parada");
            }

            analyticsTracker.paradaViewed(parada_numero);

            mParada = DBQueries.getParadaById(getDBHelper(), parada_numero);
            try {
                mLineas = DBQueries.getLineasDeParada(getDBHelper(), parada_numero);
                Collections.sort(mLineas);
            } catch (SQLException e) {
                Debug.registerHandledException(e);
            }

            guardaReciente();
        }
        cargaInfoDeParada();

        cargaAnuncio();
    }

    private void guardaReciente() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Reciente reciente = new Reciente();
                reciente.setCreatedAt(System.currentTimeMillis());
                reciente.setParadaAsociada(mParada);
                try {
                    DBQueries.setParadaReciente(getDBHelper(), reciente);
                } catch (SQLException e) {
                    Debug.registerHandledException(e);
                }
                return null;
            }
        }.execute();
    }

    private void cargaAnuncio() {
        new AsyncTask<Void, Void, MiAnuncio>() {

            @Override
            protected MiAnuncio doInBackground(Void... params) {
                try {
                    URL url = new URL(String.format(URL_ANUNCIO_PROPIO, mParada.getNumero()));

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(AD_NET_READ_TIMEOUT_MILLIS);
                    conn.setConnectTimeout(AD_NET_CONNECT_TIMEOUT_MILLIS);
                    conn.setRequestMethod("GET");
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        return null;
                    }

                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    inputStream.close();

                    JSONObject json = new JSONObject(stringBuilder.toString());
                    return new MiAnuncio(json.getString("enlace"), json.getString("imagen"));
                } catch (IOException | JSONException e) {
                    Debug.registerHandledException(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(MiAnuncio miAnuncio) {
                cargaAnuncio(miAnuncio);
            }
        }.execute();

        EditarFavoritaDialogFragment f = (EditarFavoritaDialogFragment) getFragmentManager().findFragmentByTag(EditarFavoritaDialogFragment.TAG);
        if (f != null) {
            f.setOnGuardarFavoritaListener(this);
        }
    }

    private void cargaInfoDeParada() {
        mViewLlegadas.setLineas(mLineas);
        mViewNombreParada.setText(mParada.getDescripcion());

        updateFavoritaButton();

        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(String.format(getString(R.string.parada_info_titulo), mParada.getNumero()));

        updateLlegadas();

        cargaAlertas();
    }

    private void cargaAlertas() {
        if (getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE).getBoolean("pref_alertas", true)) {
            new AsyncTask<Void, Void, SparseArray<List<LineaWarning>>>() {
                @Override
                protected SparseArray<List<LineaWarning>> doInBackground(Void... voids) {
                    SparseArray<List<LineaWarning>> res = new SparseArray<>();
                    for (Linea linea : mLineas) {
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

    private void updateFavoritaButton() {
        obtainSingleFavoritaAction
          .obtainFavorita(mParada.getNumero())
          .subscribe(favorita -> {
              if (favorita.isPresent()) {
                  colorizeScreenFromFavorita(favorita.get());
                  favoritaButton.setImageResource(R.drawable.ic_fab_star_outline);
                  favoritaButton.setOnClickListener(v -> onEliminarFavoritaClick());
              } else {
                  favoritaButton.setImageResource(R.drawable.ic_fab_star);
                  favoritaButton.setOnClickListener(v -> onCrearFavoritaClick());
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

    private void updateLlegadas() {
        TimeTracker timeTracker = new TimeTracker();

        Observable.from(mLineas)
          .map(Linea::getNumero)
          .doOnNext(mViewLlegadas::setLlegadaCargando)
          .toList()
          .flatMap(lineas -> obtainLlegadasAction.getLlegadas(mParada.getNumero(), lineas))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(llegada -> {
                mLlegadas.put(llegada.getBusLineName(), llegada);
                mViewLlegadas.setLlegadaInfo(llegada.getBusLineName(), llegada);
                long responseTime = timeTracker.calculateInterval();
                analyticsTracker.trackTiempoRecibido(llegada.getBusStopNumber(), llegada.getBusLineName(), responseTime, llegada.getDataSource());
            },
            error -> {
                Debug.registerHandledException(error);
                Snackbar.make(getView(), "Se produjo un error", Snackbar.LENGTH_LONG)
                  .setAction("Reintentar", (view) -> updateLlegadas())
                  .show();
                for (final Linea l : mLineas) {
                    mViewLlegadas.setLlegadaInfo(l.getNumero(), null);
                }
            });
    }

    @Override
    public void onGuardarFavorita(int id, String nombrePropio, int color) {
        guardarFavorita(nombrePropio, color);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void cargaAnuncio(final MiAnuncio miAnuncio) {
        final FragmentActivity activity = getActivity();
        if (mAnuncioContainer == null || activity == null)
            return;

        if (miAnuncio != null) {
            ImageView anuncioImagen = (ImageView) mAnuncioContainer.findViewById(R.id.anuncio_imagen);
            anuncioImagen.setVisibility(View.VISIBLE);
            Picasso.with(activity).load(miAnuncio.getImagenUrl()).into(anuncioImagen);
            anuncioImagen.setClickable(true);
            anuncioImagen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent clickIntent = new Intent(Intent.ACTION_VIEW);
                    clickIntent.setData(Uri.parse(miAnuncio.getEnlace()));
                    startActivity(clickIntent);
                }
            });
        }
    }

}
