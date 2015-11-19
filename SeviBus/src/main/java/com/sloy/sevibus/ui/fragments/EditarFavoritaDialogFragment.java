package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.fourmob.colorpicker.ColorPickerPalette;
import com.fourmob.colorpicker.ColorPickerSwatch;
import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;

public class EditarFavoritaDialogFragment extends DialogFragment implements ColorPickerSwatch.OnColorSelectedListener {

    public static final String TAG = "edit_fav";

    public static final int SIZE = 2;
    public static final int COLUMNS = 4;
    public static final int[] COLORS = new int[]{Favorita.COLOR_ROJO, Favorita.COLOR_AZUL_OSCURO, Favorita.COLOR_AZUL, Favorita.COLOR_MORADO, Favorita.COLOR_ROSA, Favorita.COLOR_NARANJA, Favorita.COLOR_VERDE_CLARO, Favorita.COLOR_VERDE};
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SELECTED_COLOR = "selected_color";
    private static final String EXTRA_NOMBRE_PROPIO = "nombre_propio";
    private static final String EXTRA_PARADA_ID = "parada";

    protected AlertDialog mAlertDialog;
    private ColorPickerPalette mPalette;
    private EditText mNombrePropioField;
    private OnGuardarFavoritaListener mListener;

    protected int mSelectedColor;
    protected String mTitle;
    protected String mNombrePropio;
    protected int mParadaId;

    public static EditarFavoritaDialogFragment getInstanceNewFavorita(OnGuardarFavoritaListener listener, Parada parada) {
        EditarFavoritaDialogFragment f = new EditarFavoritaDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_PARADA_ID, parada.getNumero());
        arguments.putString(EXTRA_TITLE, "Nueva favorita, nº " + parada.getNumero());
        arguments.putInt(EXTRA_SELECTED_COLOR, Favorita.COLOR_ROJO);
        f.setArguments(arguments);
        f.setOnGuardarFavoritaListener(listener);
        return f;
    }

    public static EditarFavoritaDialogFragment getInstanceEditFavorita(OnGuardarFavoritaListener listener, Favorita favorita) {
        EditarFavoritaDialogFragment f = new EditarFavoritaDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_PARADA_ID, favorita.getParadaAsociada().getNumero());
        arguments.putString(EXTRA_TITLE, "Editar favorita, nº " + favorita.getParadaAsociada().getNumero());
        arguments.putInt(EXTRA_SELECTED_COLOR, favorita.getColor());
        arguments.putString(EXTRA_NOMBRE_PROPIO, favorita.getNombrePropio());
        f.setArguments(arguments);
        f.setOnGuardarFavoritaListener(listener);
        return f;
    }

    private void refreshPalette() {
        if (this.mPalette != null) {
            this.mPalette.drawPalette(COLORS, this.mSelectedColor);
        }
    }

    @Override
    public void onColorSelected(int selectedColor) {
        if (selectedColor != this.mSelectedColor) {
            this.mSelectedColor = selectedColor;
            this.mPalette.drawPalette(COLORS, this.mSelectedColor);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            bundle = getArguments();
        }
        this.mParadaId = bundle.getInt(EXTRA_PARADA_ID);
        this.mTitle = bundle.getString(EXTRA_TITLE);
        this.mSelectedColor = bundle.getInt(EXTRA_SELECTED_COLOR);
        this.mNombrePropio = bundle.getString(EXTRA_NOMBRE_PROPIO);
    }

    public Dialog onCreateDialog(Bundle bundle) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_guardarfavorita, null);
        this.mNombrePropioField = ((EditText) view.findViewById(R.id.editar_favorita_nombre));
        if(mNombrePropio!=null) {
            mNombrePropioField.setText(mNombrePropio);
        }
        this.mPalette = ((ColorPickerPalette) view.findViewById(R.id.editar_favorita_color));
        this.mPalette.init(SIZE, COLUMNS, this);
        refreshPalette();
        this.mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(this.mTitle).setView(view)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onGuardarFavorita(mParadaId, mNombrePropioField.getText().toString().trim(), mSelectedColor);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Descartar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return this.mAlertDialog;
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(EXTRA_SELECTED_COLOR, this.mSelectedColor);
        bundle.putString(EXTRA_TITLE, this.mTitle);
        if(mNombrePropioField!=null) {
            bundle.putString(EXTRA_NOMBRE_PROPIO, mNombrePropioField.getText().toString().trim());
        }
    }

    public void setOnGuardarFavoritaListener(OnGuardarFavoritaListener mListener) {
        this.mListener = mListener;
    }

    public static interface OnGuardarFavoritaListener{
        public void onGuardarFavorita(int paradaID, String nombrePropio, int color);
    }
}
