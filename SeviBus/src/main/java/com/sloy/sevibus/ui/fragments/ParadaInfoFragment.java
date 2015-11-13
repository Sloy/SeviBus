package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.model.MiAnuncio;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.resources.AlertasManager;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.ObtainLlegadasAction;
import com.sloy.sevibus.resources.datasource.ApiLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.TussamLlegadaDataSource;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;
import com.sloy.sevibus.ui.activities.BaseActivity;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.widgets.LlegadasList;
import com.squareup.picasso.Picasso;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RestAdapter;

public class ParadaInfoFragment extends BaseDBFragment implements LoaderManager.LoaderCallbacks<Llegada>, EditarFavoritaDialogFragment.OnGuardarFavoritaListener {

    public static final String LOADER_EXTRA_LINEA = "linea";
    public static final String LOADER_EXTRA_PARADA = "parada";
    private static final String SCREEN_NAME = "Parada Info";

    public static final String URL_ANUNCIO_PROPIO = "http://sevibus.sloydev.com/ads/getad.php?p=%d";
    private static final int AD_NET_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
    private static final int AD_NET_READ_TIMEOUT_MILLIS = 10 * 1000;

    private ObtainLlegadasAction obtainLlegadasAction;

    private LlegadasList mViewLlegadas;

    private TextView mViewNombreParada;
    private FloatingActionButton favoritaButton;

    private FrameLayout mAnuncioContainer;

    private Parada mParada;
    private List<Linea> mLineas;

    private SparseArray<Llegada> mLlegadas;

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

        mLlegadas = new SparseArray<>();

        if (!isNetworkAvailable()) {
            Snackbar.make(getView(), "No hay conexión a Internet, y es necesaria", Snackbar.LENGTH_LONG).show();
        }

        obtainLlegadasAction = StuffProvider.getObtainLlegadaAction();
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
        //TODO cambiar AlertDialog por la historia de deshacer
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
        DBQueries.setNewParadaFavorita(getDBHelper(), mParada, nombrePropio, color);
        Snackbar.make(getView(), "Favorita guardada", Snackbar.LENGTH_LONG).show();
        updateFavoritaButton();
    }

    private void eliminarFavorita() {
        try {
            Favorita favCurrent = DBQueries.getFavoritaById(getDBHelper(), mParada.getNumero());
            getDBHelper().getDaoFavorita().delete(favCurrent);
            Snackbar.make(getView(), "Quitada favorita", Snackbar.LENGTH_LONG).show();
            updateFavoritaButton();
        } catch (SQLException e) {
            Debug.registerHandledException(getActivity(), e);
            Snackbar.make(getView(), "Error desconocido. Estamos en ello.", Snackbar.LENGTH_LONG).show();
        }
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

            mParada = DBQueries.getParadaById(getDBHelper(), parada_numero);
            try {
                mLineas = DBQueries.getLineasDeParada(getDBHelper(), parada_numero);
                Collections.sort(mLineas);
            } catch (SQLException e) {
                Debug.registerHandledException(getActivity(), e);
                e.printStackTrace();
            }

            // Guarda el registro de reciente
            //TODO opcional?
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Reciente reciente = new Reciente();
                    reciente.setCreatedAt(System.currentTimeMillis());
                    reciente.setParadaAsociada(mParada);
                    try {
                        DBQueries.setParadaReciente(getDBHelper(), reciente);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Debug.registerHandledException(getActivity(), e);
                    }
                    return null;
                }
            }.execute();
        }
        cargaInfoDeParada();

        // Inicia la carga de anuncios
        new AsyncTask<Void, Void, MiAnuncio>() {

            @Override
            protected MiAnuncio doInBackground(Void... params) {
                try {
                    URL url = new URL(String.format(URL_ANUNCIO_PROPIO, mParada.getNumero()));

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(AD_NET_READ_TIMEOUT_MILLIS /* milliseconds */);
                    conn.setConnectTimeout(AD_NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    // Starts the query
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
                    e.printStackTrace();
                    Debug.registerHandledException(getActivity(), e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(MiAnuncio miAnuncio) {
                cargaAnuncio(miAnuncio);
            }
        }.execute();

        // En versiones anteriores, invalida el menú, porque cuando se creó aún no se había cargado la parada y no se sabía si había que ocultar las favoritas. Ay señor...
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        // Y algunos capullos como HTC arrastran el problema a versiones superiores. Toca ejecutarlo siempre, qué bien... ¬¬
        getActivity().supportInvalidateOptionsMenu();
        //}

        // Restaura el listener del dialog (el de guardar favorita), que se va al carajo al girar la pantalla
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

        // Carga las alertas
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
                        } catch (SQLException e) {
                            e.printStackTrace();
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
        Favorita fav = null;
        try {
            fav = DBQueries.getFavoritaById(getDBHelper(), mParada.getNumero());
        } catch (SQLException e) {

        }
        final boolean isFavorita = fav != null;

        if (isFavorita) {
            favoritaButton.setImageResource(R.drawable.ic_fab_star_outline);
        } else {
            favoritaButton.setImageResource(R.drawable.ic_fab_star);
        }
        favoritaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorita) {
                    onEliminarFavoritaClick();
                } else {
                    onCrearFavoritaClick();
                }
            }
        });
    }

    //TODO indicar última comprobación, y actualizar automáticamente cuando... te parezca xD
    private void updateLlegadas() {
        LoaderManager loaderManager = getLoaderManager();
        for (Linea l : mLineas) {
            Bundle info = new Bundle();
            info.putString(LOADER_EXTRA_LINEA, l.getNumero());
            info.putInt(LOADER_EXTRA_PARADA, mParada.getNumero());
            Loader<Object> loader = loaderManager.getLoader(l.getId());
            if (loader == null) {
                loaderManager.initLoader(l.getId(), info, this);
            } else {
                loaderManager.restartLoader(l.getId(), info, this);
            }
        }
    }

    @Override
    public Loader<Llegada> onCreateLoader(int id, Bundle bundle) {
        mViewLlegadas.setLlegadaCargando(id);
        return new LlegadaLoader(getActivity(), bundle.getString(LOADER_EXTRA_LINEA), bundle.getInt(LOADER_EXTRA_PARADA), obtainLlegadasAction);
    }

    @Override
    public void onLoadFinished(Loader<Llegada> llegadaLoader, Llegada llegada) {
        mLlegadas.append(llegadaLoader.getId(), llegada);
        mViewLlegadas.setLlegadaInfo(llegadaLoader.getId(), llegada);
    }

    @Override
    public void onLoaderReset(Loader<Llegada> llegadaLoader) {

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
        Log.d("SeviBus", "cargaAnuncio()");
        final FragmentActivity activity = getActivity();
        if (mAnuncioContainer == null || activity == null)
            return; // Por si la activity se fue al carajo o algo

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

    public static class LlegadaLoader extends AsyncTaskLoader<Llegada> {

        private Llegada mData;

        private String linea;
        private Integer parada;
        private ObtainLlegadasAction obtainLlegadasAction;

        public LlegadaLoader(Context context, String linea, Integer parada, ObtainLlegadasAction obtainLlegadasAction) {
            // Loaders may be used across multiple Activitys (assuming they aren't
            // bound to the LoaderManager), so NEVER hold a reference to the context
            // directly. Doing so will cause you to leak an entire Activity's context.
            // The superclass constructor will store a reference to the Application
            // Context instead, and can be retrieved with a call to getContext().
            super(context);

            this.linea = linea;
            this.parada = parada;
            this.obtainLlegadasAction = obtainLlegadasAction;
        }


        /****************************************************/
        /** (1) A task that performs the asynchronous load **/
        /**
         * ************************************************
         */
        @Override
        public Llegada loadInBackground() {
            // This method is called on a background thread and should generate a
            // new set of data to be delivered back to the client.
            try {
                return obtainLlegadasAction.getLlegada(linea, parada);
            } catch (ServerErrorException e) {
                return null;
            }
        }

        /********************************************************/
        /** (2) Deliver the results to the registered listener **/
        /**
         * ****************************************************
         */

        @Override
        public void deliverResult(Llegada data) {
            if (isReset()) {
                // The Loader has been reset; ignore the result and invalidate the data.
                releaseResources(data);
                return;
            }

            // Hold a reference to the old data so it doesn't get garbage collected.
            // We must protect it until the new data has been delivered.
            Llegada oldData = mData;
            mData = data;

            if (isStarted()) {
                // If the Loader is in a started state, deliver the results to the
                // client. The superclass method does this for us.
                super.deliverResult(data);
            }

            // Invalidate the old data as we don't need it any more.
            if (oldData != null && oldData != data) {
                releaseResources(oldData);
            }
        }

        /*********************************************************/
        /** (3) Implement the Loader’s state-dependent behavior **/
        /**
         * *****************************************************
         */

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                // Deliver any previously loaded data immediately.
                deliverResult(mData);
            }

            // Begin monitoring the underlying data source.
            /*if (mObserver == null) {
                mObserver = new SampleObserver();
                // TODO: register the observer
            }*/

            if (takeContentChanged() || mData == null) {
                // When the observer detects a change, it should call onContentChanged()
                // on the Loader, which will cause the next call to takeContentChanged()
                // to return true. If this is ever the case (or if the current data is
                // null), we force a new load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // The Loader is in a stopped state, so we should attempt to cancel the
            // current load (if there is one).
            cancelLoad();

            // Note that we leave the observer as is. Loaders in a stopped state
            // should still monitor the data source for changes so that the Loader
            // will know to force a new load if it is ever started again.
        }

        @Override
        protected void onReset() {
            // Ensure the loader has been stopped.
            onStopLoading();

            // At this point we can release the resources associated with 'mData'.
            if (mData != null) {
                releaseResources(mData);
                mData = null;
            }

            // The Loader is being reset, so we should stop monitoring for changes.
            /*if (mObserver != null) {
                // TODO: unregister the observer
                mObserver = null;
            }*/
        }

        @Override
        public void onCanceled(Llegada data) {
            // Attempt to cancel the current asynchronous load.
            super.onCanceled(data);

            // The load has been canceled, so we should release the resources
            // associated with 'data'.
            releaseResources(data);
        }

        private void releaseResources(Llegada data) {
            // For a simple List, there is nothing to do. For something like a Cursor, we
            // would close it in this method. All resources associated with the Loader
            // should be released here.
        }
    }
}
