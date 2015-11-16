package com.sloy.sevibus.ui.widgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.HomeActivity;
import com.sloy.sevibus.ui.adapters.TwitterAdapter;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlegadasList extends LinearLayout {

    private Map<String, View> mLlegadasMap;

    LayoutInflater mInflater;
    private OnClickListener mWarningListener;
    private SparseArray<List<LineaWarning>> mWarnings;

    public LlegadasList(Context context) {
        super(context);
        init(context);
    }

    public LlegadasList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("NewApi")
    public LlegadasList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mInflater = LayoutInflater.from(context);

        if (isInEditMode()) {
            for (int i = 5; i > 0; i--) {
                mInflater.inflate(R.layout.list_item_llegada, this, true);
            }
        }

        mWarningListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                final int lineaId = (int) view.getTag();
                final List<LineaWarning> lineaWarnings = mWarnings.get(lineaId);

                AlertAdapter adapter = new AlertAdapter(getContext(), lineaWarnings);

                if (lineaWarnings != null) {
                    new AlertDialog.Builder(getContext())
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    TweetHolder tweet = lineaWarnings.get(i).getTweet();
                                    String url = String.format("https://twitter.com/%s/status/%d", tweet.getUsername(), tweet.getId());
                                    getContext().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                                }
                            })
                            .setTitle("Alertas de la línea " + lineaWarnings.get(0).getLinea().getNumero())
                            .setNegativeButton("Cerrar", null)
                            .setPositiveButton("Ver todas", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getContext().startActivity(new Intent(getContext(), HomeActivity.class).putExtra(HomeActivity.EXTRA_DRAWER_ID, R.id.nav_alertas));
                                }
                            })
                            .create().show();
                }
            }
        };
    }

    public void setLineas(List<Linea> lineas) {
        removeAllViews();
        View vistaLlegada;
        mLlegadasMap = new HashMap<>();

        for (Linea l : lineas) {
            // Crea la vista de la llegada y el separador
            vistaLlegada = mInflater.inflate(R.layout.list_item_llegada, this, false);

            // Pone la información básica de la línea en la vista (el número, vaya)
            LineaBadge numero = (LineaBadge) vistaLlegada.findViewById(R.id.item_llegada_linea);
            numero.setLinea(l);

            // Añade la vista de llegada y el separador a la lista
            this.addView(vistaLlegada);

            // Y la guarda en el mapa para actualizar los datos posteriormente
            mLlegadasMap.put(l.getNumero(), vistaLlegada);
        }
    }

    public void setLlegadaInfo(String lineaNumero, ArrivalTime llegada) {
        View vistaLlegada = mLlegadasMap.get(lineaNumero);

        //TODO mejorar el caso cuando no hay información disponible
        //TODO contemplar todos los casos, como se hacía antes en el getDisplayText de la llegada

        TextView tiempo1Text = (TextView) vistaLlegada.findViewById(R.id.item_llegada_tiempo_1);
        TextView tiempo2Text = (TextView) vistaLlegada.findViewById(R.id.item_llegada_tiempo_2);
        TextView distancia1Text = (TextView) vistaLlegada.findViewById(R.id.item_llegada_distancia_1);
        TextView distancia2Text = (TextView) vistaLlegada.findViewById(R.id.item_llegada_distancia_2);
        View progress = vistaLlegada.findViewById(R.id.item_llegada_progress);
        View container = vistaLlegada.findViewById(R.id.item_llegada_container);


        if (llegada==null) {
            //TODO whut?
            tiempo1Text.setText("Error");
            tiempo2Text.setText("Sin conexión a Internet o servidor no disponible");
            distancia1Text.setText("");
            distancia2Text.setText("");
        } else {
            if (llegada.getNextBus().getStatus() == ArrivalTime.Status.ESTIMATE) {
                int tiempo1 = llegada.getNextBus().getTimeInMinutes();
                int distancia1 = llegada.getNextBus().getDistanceInMeters();
                if (tiempo1 > 0) {
                    tiempo1Text.setText(String.format("%d minutos", tiempo1));
                    distancia1Text.setText(String.format("%d metros", distancia1));
                } else if (tiempo1 == 0) {
                    tiempo1Text.setText("Llegada inminente");
                    distancia1Text.setText("");
                } else {
                    tiempo1Text.setText("No disponible");
                    distancia1Text.setText("");
                }
            } else {
                tiempo1Text.setText("No disponible");
                distancia1Text.setText("");
            }

            // Segunda llegada
            if (llegada.getSecondBus().getStatus() == ArrivalTime.Status.ESTIMATE) {
                int tiempo2 = llegada.getSecondBus().getTimeInMinutes();
                int distancia2 = llegada.getSecondBus().getDistanceInMeters();
                if (tiempo2 > 0) {
                    tiempo2Text.setText(String.format("%d minutos", tiempo2));
                    distancia2Text.setText(String.format("%d metros", distancia2));
                } else if (tiempo2 == 0) {
                    tiempo2Text.setText("Llegada inminente");
                    distancia2Text.setText("");
                } else {
                    tiempo2Text.setText("No disponible");
                    distancia2Text.setText("o erróneo");
                }
            } else {
                tiempo2Text.setText("No disponible");
                distancia2Text.setText("");
            }
        }

        //TODO una animación o algo..
        progress.setVisibility(View.GONE);
        container.setVisibility(VISIBLE);

        if (Debug.isDebugEnabled(getContext())) {
            distancia2Text.setText(llegada.getDataSource());
        }
    }

    public void setLlegadaCargando(String linea) {
        View vistaLlegada = mLlegadasMap.get(linea);
        View progress = vistaLlegada.findViewById(R.id.item_llegada_progress);
        View container = vistaLlegada.findViewById(R.id.item_llegada_container);

        progress.setVisibility(VISIBLE);
        container.setVisibility(GONE);
    }

    public void setWarnings(SparseArray<List<LineaWarning>> warningsTodas) {
        mWarnings = warningsTodas;
        if (mLlegadasMap != null) {
            for (int i = 0; i < warningsTodas.size(); i++) {
                List<LineaWarning> warnings = warningsTodas.valueAt(i);
                int lineaId = warningsTodas.keyAt(i);
                View viewLlegada = mLlegadasMap.get(lineaId);
                if (viewLlegada != null) {
                    TextView numAlertas = (TextView) viewLlegada.findViewById(R.id.item_llegada_alertas);
                    numAlertas.setText(String.valueOf(warnings.size()));
                    numAlertas.setVisibility(VISIBLE);

                    numAlertas.setTag(lineaId);
                    numAlertas.setOnClickListener(mWarningListener);
                }
            }
        }
    }

    private static class AlertAdapter extends BaseAdapter {

        private final List<LineaWarning> mWarnings;
        private final LayoutInflater mInflater;
        private final Context mContext;

        public AlertAdapter(Context context, List<LineaWarning> warnings) {
            this.mContext = context;
            this.mWarnings = warnings;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mWarnings.size();
        }

        @Override
        public LineaWarning getItem(int i) {
            return mWarnings.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TweetHolder tweet = getItem(i).getTweet();
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_alerta_dialog, viewGroup, false);
            }
            TextView fecha = (TextView) view.findViewById(R.id.item_alerta_dialog_fecha);
            TextView texto = (TextView) view.findViewById(R.id.item_alerta_dialog_texto);
            ImageView avatar = (ImageView) view.findViewById(R.id.item_alerta_dialog_avatar);

            fecha.setText(DateFormat.format(TwitterAdapter.DATE_FORMAT, tweet.getFecha()));
            texto.setText(tweet.getTexto());
            Picasso.with(mContext).load(tweet.getAvatarUrl()).into(avatar);

            return view;
        }
    }
}
