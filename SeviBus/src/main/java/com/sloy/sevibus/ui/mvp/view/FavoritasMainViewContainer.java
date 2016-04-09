package com.sloy.sevibus.ui.mvp.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.ui.activities.IMainController;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.mvp.presenter.FavoritasMainPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FavoritasMainViewContainer implements FavoritasMainPresenter.View {


    private final Activity activity;

    @Bind(R.id.main_favoritas_contenido)
    View contenido;
    @Bind(R.id.main_favoritas_mensaje)
    TextView mMensaje;
    @Bind(R.id.favoritas_main_1)
    View mFav1;
    @Bind(R.id.favoritas_main_2)
    View mFav2;
    @Bind(R.id.favoritas_main_3)
    View mFav3;
    @Bind(R.id.favoritas_main_4)
    View mFav4;

    public FavoritasMainViewContainer(View contentView) {
        ButterKnife.bind(this, contentView);
        activity = ((Activity) contentView.getContext());
    }

    @OnClick({R.id.favoritas_main_1, R.id.favoritas_main_2, R.id.favoritas_main_3, R.id.favoritas_main_4})
    public void onFavoritaClicked(View v) {
        Integer parada = (Integer) v.getTag();
        activity.startActivity(ParadaInfoActivity.getIntent(activity, parada));
    }

    @OnClick(R.id.main_favoritas_boton_mas)
    public void onMasClicked() {
        ((IMainController) activity).abrirFavoritas();
    }

    @Override
    public void showLoading() {
        mMensaje.setText(R.string.favoritas_main_cargando);
        mMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mMensaje.setText("");
        mMensaje.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        mMensaje.setText(R.string.favoritas_main_empty);
        mMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmpty() {
        mMensaje.setText("");
        mMensaje.setVisibility(View.GONE);
    }

    @Override
    public void showFavoritas(List<Favorita> favoritas) {
        contenido.setVisibility(View.VISIBLE);
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

    @Override
    public void hideFavoritas() {
        contenido.setVisibility(View.GONE);
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
