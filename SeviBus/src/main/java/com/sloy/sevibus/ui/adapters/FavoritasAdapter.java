package com.sloy.sevibus.ui.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;

import java.util.Collections;
import java.util.List;

public class FavoritasAdapter extends RecyclerView.Adapter<FavoritasAdapter.FavoritaViewHolder> {

    private final OnFavoritaClickListener onFavoritaClickListener;

    private List<Favorita> favoritas = Collections.emptyList();

    public FavoritasAdapter(OnFavoritaClickListener onFavoritaClickListener) {
        this.onFavoritaClickListener = onFavoritaClickListener;
    }

    public void setFavoritas(List<Favorita> favoritas) {
        this.favoritas = favoritas;
    }

    @Override
    public FavoritaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorita, parent, false);
        return new FavoritaViewHolder(view, onFavoritaClickListener);
    }

    @Override
    public void onBindViewHolder(FavoritaViewHolder holder, int position) {
        holder.bind(favoritas.get(position));
    }

    @Override
    public int getItemCount() {
        return favoritas.size();
    }

    public static class FavoritaViewHolder extends RecyclerView.ViewHolder {

        private final TextView numeroBadge;
        private final TextView nombre;
        private final TextView lineas;
        private final OnFavoritaClickListener onFavoritaClickListener;

        public FavoritaViewHolder(View itemView, OnFavoritaClickListener onFavoritaClickListener) {
            super(itemView);
            this.onFavoritaClickListener = onFavoritaClickListener;
            numeroBadge = (TextView) itemView.findViewById(R.id.favorita_numero);
            nombre = (TextView) itemView.findViewById(R.id.favorita_nombre);
            lineas = (TextView) itemView.findViewById(R.id.favorita_lineas);
        }

        public void bind(Favorita favorita) {
            Parada parada = favorita.getParadaAsociada();

            boolean hasNombrePropio = favorita.getNombrePropio() != null && !TextUtils.isEmpty(favorita.getNombrePropio());
            nombre.setText(hasNombrePropio ? favorita.getNombrePropio() : parada.getDescripcion());

            numeroBadge.setText(parada.getNumero().toString());
            Drawable background = numeroBadge.getBackground();
            ((GradientDrawable) background).setColor(favorita.getColor());

            StringBuilder sbLineas = new StringBuilder();
            for (String linea : parada.getNumeroLineas()) {
                sbLineas.append(linea);
                sbLineas.append("  ");
            }
            sbLineas.setLength(sbLineas.length() - 2);
            lineas.setText(sbLineas.toString());

            itemView.setOnClickListener(v -> onFavoritaClickListener.onFavoritaClick(favorita));
        }
    }

    public interface OnFavoritaClickListener {
        void onFavoritaClick(Favorita favorita);
    }
}
