package com.sloy.sevibus.ui.mvp.view;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.ui.activities.HomeActivity;
import com.sloy.sevibus.ui.fragments.MapContainerFragment;
import com.sloy.sevibus.ui.mvp.presenter.ParadasCercanasMainPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ParadasCercanasMainViewContainer implements ParadasCercanasMainPresenter.View {

    private final Activity activity;

    @Bind(R.id.main_paradas_cercanas_contenido)
    View mContenido;
    @Bind(R.id.main_paradas_cercanas_mensaje)
    TextView mMensaje;
    @Bind(R.id.main_paradas_cercanas_parada_1)
    View mParada1View;
    @Bind(R.id.main_paradas_cercanas_parada_2)
    View mParada2View;
    @Bind(R.id.main_paradas_cercanas_parada_3)
    View mParada3View;
    @Bind(R.id.main_paradas_cercanas_parada_4)
    View mParada4View;

    public ParadasCercanasMainViewContainer(View contentView) {
        ButterKnife.bind(this, contentView);
        this.activity = ((Activity) contentView.getContext());
    }

    @Deprecated
    public void setupMapa(FragmentManager fm){
        FragmentTransaction trans = fm.beginTransaction();
        Fragment f = fm.findFragmentByTag("mapa");
        if (f == null) {
            f = MapContainerFragment.getInstance(false);
        }
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.main_paradas_cercanas_mapa_content, f, "mapa");
        }

        trans.commit();
    }

    @OnClick(R.id.main_paradas_cercanas_mapa_trigger)
    public void onMapClick() {
        ((ParadasCercanasMainViewContainer.ParadasCercanasMainClickListener) activity).onParadaCercanaMas();
    }

    @OnClick({R.id.main_paradas_cercanas_parada_1, R.id.main_paradas_cercanas_parada_2, R.id.main_paradas_cercanas_parada_3, R.id.main_paradas_cercanas_parada_4})
    public void onCercanaClick(View v) {
        Integer parada = (Integer) v.getTag();
        ((ParadasCercanasMainClickListener) activity).onParadaCercanaClick(parada);
    }
    @OnClick(R.id.main_paradas_cercanas_boton_mas)
    public void onMasClick() {
        ((HomeActivity) activity).getMapOptions().setMostrarCercanas(true);
        ((ParadasCercanasMainClickListener) activity).onParadaCercanaMas();
    }

    private void bindCercanaView(ParadaCercana paradaCercana, View v) {
        if (paradaCercana != null) {
            Parada parada = paradaCercana.getParada();
            ((TextView) v.findViewById(R.id.item_parada_numero)).setText(parada.getNumero().toString());
            ((TextView) v.findViewById(R.id.item_parada_nombre)).setText(parada.getDescripcion());
            ((TextView) v.findViewById(R.id.item_parada_distancia)).setText(paradaCercana.getDistancia() + "m");
            v.setTag(parada.getNumero());
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }


    @Override
    public void showEmpty() {
        mMensaje.setText(R.string.paradas_cercanas_empty);
        mMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmpty() {
        mMensaje.setVisibility(View.GONE);
    }

    @Override
    public void showParadas(List<ParadaCercana> paradas) {
        mMensaje.setVisibility(View.GONE);
        mContenido.setVisibility(View.VISIBLE);

        bindCercanaView(paradas.get(0), mParada1View);
        bindCercanaView(paradas.get(1), mParada2View);
        bindCercanaView(paradas.get(2), mParada3View);
        bindCercanaView(paradas.get(3), mParada4View);
    }

    @Override
    public void hideParadas() {
        mContenido.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        mMensaje.setText(R.string.paradas_cercanas_cargando);
        mMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mMensaje.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        mMensaje.setText(R.string.ubicacion_error);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    public interface ParadasCercanasMainClickListener {
        void onParadaCercanaClick(int idParada);

        void onParadaCercanaMas();
    }
}
