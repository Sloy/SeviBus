package com.sloy.sevibus.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.AbstractBackupHelper.BackupException;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.FavoritasLocalBackupHelper;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;

import java.sql.SQLException;
import java.util.List;

public class FavoritasListFragment extends BaseDBFragment implements EditarFavoritaDialogFragment.OnGuardarFavoritaListener {

    private ListView listView;
    private FavoritasAdapter mAdapter;
    private View emptyIndicator;

    private int mCurrentlySelectedItem = -1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favoritas_list, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        emptyIndicator = v.findViewById(R.id.favoritas_emtpy_indicator);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Integer numero = mAdapter.getItem(position).getParadaAsociada().getNumero();
                    startActivity(ParadaInfoActivity.getIntent(getActivity(), numero));
            }
        });
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
                listView.setVisibility(View.GONE);
            } else {
                emptyIndicator.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

            // Me gustaría reciclar el adapter cambiándole los items, pero por algún puto motivo si lo hago no me muestra nada...
            mAdapter = new FavoritasAdapter(getActivity(), favoritas, getDBHelper());
            listView.setAdapter(mAdapter);
        } catch (Exception e) {
            Debug.registerHandledException(getActivity(), e);
            e.printStackTrace();
            Snackbar.make(getView(), "Error desconocido. ¡Estamos en ello!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void eliminarFavoritaSeleccionada() {
        Favorita fav = mAdapter.getItem(mCurrentlySelectedItem);
        getDBHelper().getDaoFavorita().delete(fav);
        Snackbar.make(getView(), "Favorita eliminada", Snackbar.LENGTH_LONG).show();
        recargaListaFavoritas();
    }

    private void editarFavoritaSeleccionada() {
        Favorita fav = mAdapter.getItem(mCurrentlySelectedItem);
        EditarFavoritaDialogFragment.getInstanceEditFavorita(this, fav).show(getFragmentManager(), EditarFavoritaDialogFragment.TAG);
    }

    private void exportar() {
        new AsyncTask<Void, Void, BackupException>() {

            @Override
            protected BackupException doInBackground(Void... params) {
                try {
                    new FavoritasLocalBackupHelper(getDBHelper()).exportar();
                } catch (BackupException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(BackupException result) {
                super.onPostExecute(result);
                getActivity().setProgressBarIndeterminateVisibility(false);
                if (result == null) {
                    Toast.makeText(getActivity(), "Copia creada con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_LONG).show();
                    // TODO probar esto, anda...
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                getActivity().setProgressBarIndeterminateVisibility(true);
            }

        }.execute();
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

    private class FavoritasAdapter extends BaseAdapter {

        private List<Favorita> mItems;
        private Context mContext;
        private DBHelper dbHelper;

        public FavoritasAdapter(Context context, List<Favorita> items, DBHelper helper) {
            mItems = items;
            mContext = context;
            dbHelper = helper;
        }

        public int getCount() {
            return mItems.size();
        }

        public Favorita getItem(int position) {
            return mItems.get(position);
        }

        public long getItemId(int position) {
            return getItem(position).getOrden();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Favorita item = getItem(position);
            Parada parada = item.getParadaAsociada();
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.list_item_favorita, null);
            }
            TextView numeroText = (TextView) convertView.findViewById(R.id.item_parada_numero_text);
            TextView nombre = (TextView) convertView.findViewById(R.id.item_parada_nombre);
            TextView lineas = (TextView) convertView.findViewById(R.id.item_parada_lineas);

            // El nombre en sí de la parada siempre es fijo
            nombre.setText(parada.getDescripcion());

            // Según si tiene un nombre personalizado o no, pongo el título de
            // una forma u otra
            String numeroTextStyled;
            if (item.getNombrePropio() != null && !TextUtils.isEmpty(item.getNombrePropio())) {
                // Tiene un nombre especial
                numeroTextStyled = String.format("%s <b>(%d)</b>", item.getNombrePropio(), parada.getNumero());
            } else {
                numeroTextStyled = String.format("Parada nº <b>%d</b>", parada.getNumero());
            }
            numeroText.setText(Html.fromHtml(numeroTextStyled));

            // TODO mostrar pequeños iconos
            // TODO WTF!! Quita la llamada a la BBDD de aquí, pedazo de loco!!!
            List<Linea> lineasList = null;
            try {
                lineasList = DBQueries.getLineasDeParada(dbHelper, parada.getNumero());
            } catch (SQLException e) {
                // TODO hacer algo más
                e.printStackTrace();
                Log.e("sevibus", "Error cargando las l√≠neas de la parada " + parada.getNumero(), e);
            }

            StringBuilder sbLineas = new StringBuilder();
            for (Linea l : lineasList) {
                sbLineas.append(l.getNumero());
                sbLineas.append("  ");
            }
            sbLineas.setLength(sbLineas.length() - 2);
            lineas.setText(sbLineas.toString());

            View color = convertView.findViewById(R.id.favorita_color);
            color.setBackgroundColor(item.getColor());

            return convertView;
        }

        public void setFavoritas(List<Favorita> favoritas) {
            this.mItems = favoritas;
            this.notifyDataSetChanged();
        }

    }
}
