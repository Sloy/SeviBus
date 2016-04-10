package com.sloy.sevibus.ui.mvp.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.ui.mvp.presenter.LineasCercanasPresenter;
import com.sloy.sevibus.ui.widgets.LineaBadge;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LineasCercanasViewContainer implements LineasCercanasPresenter.View {

    private final Activity activity;

    @Bind(R.id.main_lineas_cercanas_contenido)
    View contenido;
    @Bind(R.id.main_lineas_cercanas_mensaje)
    TextView mensaje;
    @Bind(R.id.main_lineas_cercanas_linea_1)
    View linea1View;
    @Bind(R.id.main_lineas_cercanas_linea_2)
    View linea2View;
    @Bind(R.id.main_lineas_cercanas_linea_3)
    View linea3View;
    @Bind(R.id.main_lineas_cercanas_linea_4)
    View linea4View;

    public LineasCercanasViewContainer(View contentView) {
        ButterKnife.bind(this, contentView);
        this.activity = ((Activity) contentView.getContext());
    }


    @OnClick(R.id.main_lineas_cercanas_boton_todas)
    public void onTodasClick() {
        ((LineasCercanasMainClickListener) activity).onLineaCercanaMas();
    }

    @OnClick({R.id.main_lineas_cercanas_linea_1, R.id.main_lineas_cercanas_linea_2, R.id.main_lineas_cercanas_linea_3, R.id.main_lineas_cercanas_linea_4})
    public void onLineaClick(View view) {
        Linea linea = (Linea) view.getTag();
        ((LineasCercanasMainClickListener) activity).onLineaCercanaClick(linea.getId());
    }

    @Override
    public void showLineas(List<Linea> lineas) {
        contenido.setVisibility(View.VISIBLE);
        Linea l1 = null, l2 = null, l3 = null, l4 = null;
        int count = lineas.size();
        if (count > 0) {
            l1 = lineas.get(0);
        }
        if (count > 1) {
            l2 = lineas.get(1);
        }
        if (count > 2) {
            l3 = lineas.get(2);
        }
        if (count > 3) {
            l4 = lineas.get(3);
        }

        bindView(l1, linea1View);
        bindView(l2, linea2View);
        bindView(l3, linea3View);
        bindView(l4, linea4View);
    }

    private void bindView(Linea l, View v) {
        if (l != null) {
            ((LineaBadge) v.findViewById(R.id.item_linea_numero)).setLinea(l);
            ((TextView) v.findViewById(R.id.item_linea_nombre)).setText(l.getNombre());
            v.setTag(l);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLineas() {
        contenido.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        mensaje.setText(R.string.lineas_cercanas_cargando);
        mensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmpty() {
        mensaje.setText(R.string.lineas_cercanas_empty);
        mensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmpty() {
        mensaje.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        mensaje.setText(R.string.ubicacion_error);
        mensaje.setVisibility(View.VISIBLE);
    }

    public interface LineasCercanasMainClickListener {
        void onLineaCercanaClick(int idParada);

        void onLineaCercanaMas();
    }
}
