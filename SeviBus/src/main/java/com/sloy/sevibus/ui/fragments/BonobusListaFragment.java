package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.BonobusInfoReader;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.activities.NuevoBonobusActivity;
import com.sloy.sevibus.ui.adapters.BonobusAdapter;
import com.sloy.sevibus.ui.widgets.BonobusView;

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
