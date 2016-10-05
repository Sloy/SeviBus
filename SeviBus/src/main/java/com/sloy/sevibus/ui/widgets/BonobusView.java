package com.sloy.sevibus.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Bonobus;

public class BonobusView extends FrameLayout {

    TextView nombrePropio;
    TextView numero;
    TextView descripcion;
    ProgressBar cargando;
    ImageView imagen;
    View panelOpciones;

    boolean isCargando;

    public BonobusView(Context context) {
        super(context);
        init(context);
    }

    public BonobusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BonobusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.view_bonobus, this, true);
        assert v != null;
        if (!isInEditMode()) {
            nombrePropio = (TextView) v.findViewById(R.id.item_bonobus_nombre);
            numero = (TextView) v.findViewById(R.id.item_bonobus_numero);
            descripcion = (TextView) v.findViewById(R.id.item_bonobus_descripcion);
            cargando = (ProgressBar) v.findViewById(R.id.item_bonobus_cargando);
            imagen = (ImageView) v.findViewById(R.id.item_bonobus_imagen);
            panelOpciones = v.findViewById(R.id.item_bonobus_opciones_panel);
        }
    }

    public void setCargando(boolean carga) {
        isCargando = carga;
        if (!carga) { // Sólo animo la transición a NO cargando
            // Oculto el progreso
            ObjectAnimator progAnim = ObjectAnimator.ofFloat(cargando, "alpha", 0f);
            progAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cargando.setVisibility(GONE);
                }
            });
            // Muestro la descripción
            ObjectAnimator descAnim = ObjectAnimator.ofFloat(descripcion, "alpha", 1f);
            descAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    descripcion.setVisibility(VISIBLE);
                }
            });
            AnimatorSet set = new AnimatorSet();
            set.setDuration(300);
            set.playSequentially(progAnim, descAnim);
            set.start();
        } else {
            descripcion.setVisibility(carga ? GONE : VISIBLE);
            cargando.setVisibility(carga ? VISIBLE : GONE);
        }
    }

    public void setBonobusInfo(Bonobus bonobus) {
        String nombre = bonobus.getNombre();
        nombrePropio.setText(nombre);
        numero.setText(bonobus.getNumeroFormateado());
        if (bonobus.isRelleno()) {
            switch (bonobus.getTipo()) {
                case SALDO:
                    descripcion.setText(Html.fromHtml(String.format("Saldo: <b>%s</b>", bonobus.getSaldo())));
                    imagen.setImageResource(R.drawable.bonobus_normal);
                    break;
                case SALDO_TRANSBORDO:
                    descripcion.setText(Html.fromHtml(String.format("Saldo: <b>%s</b>", bonobus.getSaldo())));
                    imagen.setImageResource(R.drawable.bonobus_transbordo);
                    break;
                case JOVEN:
                    descripcion.setText(Html.fromHtml(String.format("Caducidad: <b>%s</b>", bonobus.getCaducidad())));
                    imagen.setImageResource(R.drawable.bonobus_joven);
                    break;
                case MENSUAL:
                    descripcion.setText(Html.fromHtml(String.format("Fecha fin: <b>%s</b>", bonobus.getCaducidad())));
                    imagen.setImageResource(R.drawable.bonobus_mensual);
                    break;
                default:
                    descripcion.setText("Tipo de tarjeta no reconocido aún");
                    imagen.setImageResource(R.drawable.bonobus_unknown);
                    break;
            }
        }
    }

    public void setError(String mensaje) {
        descripcion.setText(mensaje);
        imagen.setImageResource(R.drawable.bonobus_unknown);
    }

    public void mostrarOpciones(boolean mostrar) {
        if (mostrar) {
            expandirOpcionesAnimacion();
        } else {
            contraerOpcionesAnimacion();
        }
    }

    public void setEliminarListener(OnClickListener listener) {
        panelOpciones.findViewById(R.id.item_bonobus_opciones_eliminar).setOnClickListener(listener);
    }

    public void setTarifaListener(OnClickListener listener) {
        panelOpciones.findViewById(R.id.item_bonobus_opciones_tarifas).setOnClickListener(listener);
    }

    private void expandirOpcionesAnimacion() {
        panelOpciones.setVisibility(VISIBLE);
    }

    private void contraerOpcionesAnimacion() {
        panelOpciones.setVisibility(GONE);
    }
}
