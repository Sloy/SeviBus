package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.MiAnuncio;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.BonobusInfoReader;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.activities.NuevoBonobusActivity;
import com.sloy.sevibus.ui.adapters.BonobusAdapter;
import com.sloy.sevibus.ui.widgets.BonobusView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class BonobusListaFragment extends BaseDBFragment implements LoaderManager.LoaderCallbacks<Bonobus> {

    private static final String PREF_SHOW_BONOBUS_WARNING = "pref_bono_warning";
    public static final String URL_ANUNCIO_PROPIO = "http://sevibus.sloydev.com/ads/getad.php?s=%s";

    private ListView mList;
    private List<Bonobus> mListaBonobuses;
    private BonobusAdapter mAdapter;
    private BonobusView mBonoExpandidoActual;

    private FrameLayout mAnuncioContainer;
    private BonobusInfoReader bonobusInfoReader;

    private class EliminarClickListener implements View.OnClickListener {
        Bonobus bonobusAsociado;

        @Override
        public void onClick(View v) {
            if (bonobusAsociado != null) {
                DBQueries.deleteBonobus(getDBHelper(), bonobusAsociado);
                cargarBonobuses();
            }
        }
    }

    private class TarifasClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent tussamIntent = new Intent(Intent.ACTION_VIEW);
            tussamIntent.setData(Uri.parse("http://tussam.es/index.php?id=10"));
            startActivity(tussamIntent);
        }
    }

    private EliminarClickListener mEliminarClickListener = new EliminarClickListener();

    private TarifasClickListener mTarifasClickListener = new TarifasClickListener();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bonobusInfoReader = new BonobusInfoReader(StuffProvider.getSevibusApi(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_bonobuses, container, false);

        mAnuncioContainer = (FrameLayout) v.findViewById(R.id.anuncio);
        mList = (ListView) v.findViewById(android.R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == -1) { // Botón de añadir más
                    final SharedPreferences sharedPreferences = getSharedPreferences(getActivity());
                    if (!sharedPreferences.getBoolean(PREF_SHOW_BONOBUS_WARNING, true)) {
                        startActivity(new Intent(getActivity(), NuevoBonobusActivity.class));
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Aviso importante")
                                .setMessage("La información de los bonobuses se extrae de la web de Tussam. Si los datos no son correctos no es responsabilidad de SeviBus.\n\nLa información sobre el saldo puede tardar hasta 24 horas en aparecer actualizada. Tenlo en cuenta.\n\nEste mensaje no se volverá a mostrar.")
                                .setPositiveButton("Entendido, no volver a mostrar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sharedPreferences.edit().putBoolean(PREF_SHOW_BONOBUS_WARNING, false).commit();
                                        startActivity(new Intent(getActivity(), NuevoBonobusActivity.class));
                                    }
                                })
                                .show();
                    }
                } else {
                    BonobusView bonobusView = (BonobusView) view;
                    if (mBonoExpandidoActual == bonobusView) {
                        mBonoExpandidoActual.mostrarOpciones(false);
                        mBonoExpandidoActual = null;
                    } else {
                        if (mBonoExpandidoActual != null) {
                            mBonoExpandidoActual.mostrarOpciones(false);
                        }
                        Bonobus bonoItem = mAdapter.getItem(position);
                        mEliminarClickListener.bonobusAsociado = bonoItem;
                        bonobusView.setEliminarListener(mEliminarClickListener);
                        bonobusView.setTarifaListener(mTarifasClickListener);

                        bonobusView.mostrarOpciones(true);
                        mBonoExpandidoActual = bonobusView;
                    }
                }

            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        cargarBonobuses();
        cargaAnuncio();
    }

    private void cargaAnuncio() {
        new AsyncTask<Void, Void, MiAnuncio>() {

            @Override
            protected MiAnuncio doInBackground(Void... params) {
                try {
                    URL url = new URL(String.format(URL_ANUNCIO_PROPIO, "bonolist"));

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10*1000 /* milliseconds */);
                    conn.setConnectTimeout(10*1000 /* milliseconds */);
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
                    Debug.registerHandledException(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(MiAnuncio miAnuncio) {
                cargaAnuncio(miAnuncio);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bonobus_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_recargar) {
            recargarBonobus();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void recargarBonobus() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Aviso importante")
                .setMessage("Esta opción te llevará a la página web de Tussam en tu navegador para recargar las tarjetas. A partir de ese momento todo lo que hagas será independiente a SeviBus, y si el sistema no funciona correctamente la aplicación no es responsable. \n\nEl saldo tras la recarga puede tardar en aparecer hasta 24 horas después de volver a usar la tarjeta.")
                .setPositiveButton("Entiendo, continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://recargas.tussam.es/")));
                    }
                })
                .show();

    }

    private void cargarBonobuses() {
        mListaBonobuses = DBQueries.getBonobuses(getDBHelper());

        mAdapter = new BonobusAdapter(getActivity(), mListaBonobuses);
        mList.setAdapter(mAdapter);
        actualizarBonobuses();
    }

    private void actualizarBonobuses() {
        LoaderManager loaderManager = getLoaderManager();
        for (int i = 0; i < mListaBonobuses.size(); i++) {
            Loader<Object> loader = loaderManager.getLoader(i);
            if (loader == null) {
                loaderManager.initLoader(i, new Bundle(), this);
            } else {
                loaderManager.restartLoader(i, new Bundle(), this);
            }
        }
    }


    public void cargaAnuncio(final MiAnuncio miAnuncio) {
        Log.d("SeviBus", "cargaAnuncio()");
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

    @Override
    public Loader<Bonobus> onCreateLoader(int id, Bundle args) {
        return new BonobusLoader(getActivity(), mListaBonobuses.get(id), bonobusInfoReader);
    }

    @Override
    public void onLoadFinished(Loader<Bonobus> loader, Bonobus data) {
        if (data == null) {
            Snackbar.make(getView(), "Ocurrió un error cargando los datos del bonobús.", Snackbar.LENGTH_LONG).show();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Bonobus> loader) {

    }

    public static class BonobusLoader extends AsyncTaskLoader<Bonobus> {

        private final Bonobus mBonobus;
        private final BonobusInfoReader bonobusInfoReader;

        public BonobusLoader(Context context, Bonobus bonobus, BonobusInfoReader bonobusInfoReader) {
            super(context);
            this.mBonobus = bonobus;
            this.bonobusInfoReader = bonobusInfoReader;
        }
        @Override
        public Bonobus loadInBackground() {
            try {
                return bonobusInfoReader.populateBonobusInfo(mBonobus);
            } catch (Exception e) {
                Log.e("SeviBus bonobus", "Error", e);
                mBonobus.setError(true);
                return null;
            }
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }
    }
}
