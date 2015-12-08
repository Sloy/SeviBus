package com.sloy.sevibus.ui.fragments.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.IMainController;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.fragments.BaseDBFragment;
import java.sql.SQLException;
import java.util.List;

public class FavoritasMainFragment extends BaseDBFragment {

    private TextView mMensaje;
    private View mContenido;

    public static FavoritasMainFragment getInstance() {
        return new FavoritasMainFragment();
    }

    private View mFav1, mFav2, mFav3, mFav4;
    private View mButtonMas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_favoritas, container, false);

        mMensaje = (TextView) v.findViewById(R.id.main_favoritas_mensaje);
        mContenido = v.findViewById(R.id.main_favoritas_contenido);

        mButtonMas = v.findViewById(R.id.main_favoritas_boton_mas);
        mFav1 = v.findViewById(R.id.favoritas_main_1);
        mFav2 = v.findViewById(R.id.favoritas_main_2);
        mFav3 = v.findViewById(R.id.favoritas_main_3);
        mFav4 = v.findViewById(R.id.favoritas_main_4);

        View.OnClickListener favListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer parada = (Integer) v.getTag();
                startActivity(ParadaInfoActivity.getIntent(getActivity(), parada));
            }
        };
        mFav1.setOnClickListener(favListener);
        mFav2.setOnClickListener(favListener);
        mFav3.setOnClickListener(favListener);
        mFav4.setOnClickListener(favListener);

        mButtonMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IMainController)getActivity()).abrirFavoritas();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        muestraCargando();
        new AsyncTask<Void, Void, List<Favorita>>(){

            @Override
            protected List<Favorita> doInBackground(Void... params) {
                List<Favorita> paradasFavoritas = null;
                try {
                    paradasFavoritas = DBQueries.getParadasFavoritas(getDBHelper());
                } catch (SQLException e) {
                    e.printStackTrace();
                    Debug.registerHandledException(e);
                }
                return paradasFavoritas;
            }

            @Override
            protected void onPostExecute(List<Favorita> favoritas) {
                if (favoritas!=null && favoritas.size() > 0) {
                    muestraFavoritas(favoritas);
                } else {
                    muestraNoDatos();
                }
            }
        }.execute();
    }

    private void muestraCargando() {
        mMensaje.setText(R.string.favoritas_main_cargando);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraNoDatos() {
        mMensaje.setText(R.string.favoritas_main_empty);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraFavoritas(List<Favorita> favoritas) {
        mMensaje.setVisibility(View.GONE);
        mContenido.setVisibility(View.VISIBLE);

        Favorita fav1 = null, fav2 = null, fav3 = null, fav4 = null;
        int count = favoritas.size();
        if (count >= 1) {
            fav1 = favoritas.get(0);
        }
        if (count >= 2) {
            fav2 = favoritas.get(1);
        }
        if (count >= 3) {
            fav3 = favoritas.get(2);
        }
        if (count >= 4) {
            fav4 = favoritas.get(3);
        }

        bindViewFavorita(fav1, mFav1);
        bindViewFavorita(fav2, mFav2);
        bindViewFavorita(fav3, mFav3);
        bindViewFavorita(fav4, mFav4);
    }

    private void bindViewFavorita(Favorita fav, View v) {
        if (fav != null) {
            Integer numero = fav.getParadaAsociada().getNumero();
            TextView text = (TextView) v.findViewById(R.id.favoritas_main_numero);
            View color = v.findViewById(R.id.favoritas_main_color);
            text.setText(numero.toString());
            color.setBackgroundColor(fav.getColor());
            v.setTag(numero);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.INVISIBLE);
        }
    }
}
