package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.ThemeSelector;
import com.sloy.sevibus.ui.adapters.ParadasAdapter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusquedaActivity extends BaseToolbarActivity implements SearchView.OnQueryTextListener {

    private static final long RESULTS_LIMIT = 50;
    private static final String SCREEN_NAME = "Búsqueda";

    private SearchView mSearchView;
    private ParadasAdapter mAdapter;
    private ListView mListView;
    private TextView mEmptyView;
    private View mIndicadorRecientes;
    private String mCurrentQuery;
    private AnalyticsTracker analyticsTracker;

    public static Intent getIntent(Context context) {
        return new Intent(context, BusquedaActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda);

        analyticsTracker = StuffProvider.getAnalyticsTracker();
        mListView = (ListView) findViewById(R.id.busqueda_lista);
        mEmptyView = (TextView) findViewById(R.id.busqueda_texto_vacio);
        mIndicadorRecientes = findViewById(R.id.busqueda_indicador_recientes);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                analyticsTracker.searchPerformed(mCurrentQuery);
                startActivity(ParadaInfoActivity.getIntent(BusquedaActivity.this, mAdapter.getItem(position).getNumero()));
            }
        });

        mostrarRecientes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.busqueda, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.white_alpha));

        return true;
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("Sevibus search", "Query submit: " + query);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        if (query.equalsIgnoreCase("virgi")) {
            ImageView img = new ImageView(this);
            img.setImageResource(R.drawable.secret);
            Toast toast = new Toast(this);
            toast.setView(img);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
            ThemeSelector.setV();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.trim();
        Log.d("Sevibus search", "Query change: " + newText);
        if (TextUtils.isEmpty(newText)) {
            mostrarRecientes();
        } else {
            buscar(newText);
        }
        return true;
    }

    private void buscar(final String newText) {
        mCurrentQuery = newText;
        new AsyncTask<Void, Void, List<Parada>>() {
            @Override
            protected List<Parada> doInBackground(Void... params) {
                List<Parada> paradasByQuery = null;
                try {
                    paradasByQuery = DBQueries.getParadasByQuery(getDBHelper(), newText, RESULTS_LIMIT);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Debug.registerHandledException(e);
                }
                return paradasByQuery;
            }

            @Override
            protected void onPostExecute(List<Parada> paradas) {
                if (paradas != null && !paradas.isEmpty()) {
                    mostrarResultados(paradas);
                } else {
                    mostrarVacio(newText);
                }
            }
        }.execute();
    }

    private void mostrarResultados(List<Parada> paradas) {
        setParadas(paradas);
        mIndicadorRecientes.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    private void mostrarVacio(String query) {
        mIndicadorRecientes.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        String emptyText = query != null ? String.format("No hay resultados para \"%s\".", query) : "No hay resultados";
        mEmptyView.setText(emptyText);
    }

    private void mostrarRecientes() {
        mListView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mIndicadorRecientes.setVisibility(View.VISIBLE);
        try {
            List<Reciente> recientes = DBQueries.getParadasRecientes(getDBHelper());
            if (recientes.isEmpty()) {
                mostrarVacio(null);
                return;
            }
            List<Parada> paradas = new ArrayList<Parada>();
            for (Reciente r : recientes) {
                paradas.add(r.getParadaAsociada());
            }
            setParadas(paradas);
        } catch (SQLException e) {
            mostrarVacio(null);
            Debug.registerHandledException(e);
            e.printStackTrace();
        }
    }

    private void setParadas(List<Parada> paradas) {
        if (mAdapter == null) {
            mAdapter = new ParadasAdapter(this, paradas, getDBHelper());
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.changeParadas(paradas);
        }
    }
}
