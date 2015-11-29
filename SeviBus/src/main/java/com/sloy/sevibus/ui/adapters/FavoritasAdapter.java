package com.sloy.sevibus.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;

import java.util.Collections;
import java.util.List;

public class FavoritasAdapter extends RecyclerView.Adapter<FavoritasAdapter.FavoritaViewHolder> implements DragFavoritaCallback.FavoritaDragHelperAdapter {

    private final OnFavoritaClickListener onFavoritaClickListener;
    private final OnFavoritasReorderedListener onFavoritasReorderedListener;
    private final OnStartDragListener mDragStartListener;

    private List<Favorita> favoritas = Collections.emptyList();

    public FavoritasAdapter(OnFavoritaClickListener onFavoritaClickListener, OnFavoritasReorderedListener onFavoritasReorderedListener, OnStartDragListener mDragStartListener) {
        this.onFavoritaClickListener = onFavoritaClickListener;
        this.onFavoritasReorderedListener = onFavoritasReorderedListener;
        this.mDragStartListener = mDragStartListener;
    }

    public void setFavoritas(List<Favorita> favoritas) {
        this.favoritas = favoritas;
        this.notifyDataSetChanged();
    }

    @Override
    public FavoritaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorita, parent, false);
        return new FavoritaViewHolder(view, onFavoritaClickListener, mDragStartListener);
    }

    @Override
    public void onBindViewHolder(FavoritaViewHolder holder, int position) {
        holder.bind(favoritas.get(position));
    }

    @Override
    public int getItemCount() {
        return favoritas.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(favoritas, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(favoritas, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        onFavoritasReorderedListener.onFavoritasReordered(favoritas);
        return true;
    }

    public static class FavoritaViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private final TextView numeroBadge;
        private final TextView nombre;
        private final TextView lineas;
        private final ImageView handleView;
        private final OnFavoritaClickListener onFavoritaClickListener;
        private final OnStartDragListener mDragStartListener;
        private final float selectedCardElevation;
        private final float numeroOneDigitSize;
        private final float numeroTwoDigitSize;
        private final float numeroThreeDigitSize;
        private final float numeroFourDigitSize;

        public FavoritaViewHolder(View itemView, OnFavoritaClickListener onFavoritaClickListener, OnStartDragListener mDragStartListener) {
            super(itemView);
            this.onFavoritaClickListener = onFavoritaClickListener;
            this.mDragStartListener = mDragStartListener;
            numeroBadge = (TextView) itemView.findViewById(R.id.favorita_numero);
            nombre = (TextView) itemView.findViewById(R.id.favorita_nombre);
            lineas = (TextView) itemView.findViewById(R.id.favorita_lineas);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
            selectedCardElevation = itemView.getResources().getDimension(R.dimen.favorita_selected_elevation);
            numeroOneDigitSize = itemView.getResources().getDimension(R.dimen.favorita_numero_1_digit);
            numeroTwoDigitSize = itemView.getResources().getDimension(R.dimen.favorita_numero_2_digits);
            numeroThreeDigitSize = itemView.getResources().getDimension(R.dimen.favorita_numero_3_digits);
            numeroFourDigitSize = itemView.getResources().getDimension(R.dimen.favorita_numero_4_digits);
        }

        public void bind(Favorita favorita) {
            Parada parada = favorita.getParadaAsociada();

            boolean hasNombrePropio = favorita.getNombrePropio() != null && !TextUtils.isEmpty(favorita.getNombrePropio());
            nombre.setText(hasNombrePropio ? favorita.getNombrePropio() : parada.getDescripcion());

            String numeroParada = parada.getNumero().toString();
            numeroBadge.setText(numeroParada);
            numeroBadge.setTextSize(TypedValue.COMPLEX_UNIT_PX, numeroSizeFor(numeroParada));
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
            handleView.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(this);
                }
                return false;
            });
        }

        private float numeroSizeFor(String numeroParada) {
            switch (numeroParada.length()) {
                case 1:
                    return numeroOneDigitSize;
                case 2:
                    return numeroTwoDigitSize;
                case 3:
                    return numeroThreeDigitSize;
                case 4:
                default:
                    return numeroFourDigitSize;
            }
        }

        @Override
        public void onItemSelected() {
            CardView cardView = (CardView) this.itemView;
            // This value is being ignored for some reason :/
            cardView.setCardElevation(selectedCardElevation);
            cardView.setCardBackgroundColor(Color.WHITE);
        }

        @Override
        public void onItemClear() {
            CardView cardView = (CardView) this.itemView;
            cardView.setCardElevation(0f);
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    public interface OnFavoritaClickListener {
        void onFavoritaClick(Favorita favorita);
    }

    public interface OnFavoritasReorderedListener {
        void onFavoritasReordered(List<Favorita> ordered);
    }
}
