package com.sloy.sevibus.resources;

import android.support.annotation.NonNull;

import com.sloy.sevibus.model.PaletaColores;

public class LegacyColorConverter {

    public final static int COLOR_VERDE_CLARO = 0xFFAACC00;
    public final static int COLOR_VERDE = 0xFF669900;
    public final static int COLOR_MORADO = 0xFF9933CC;
    public final static int COLOR_AZUL = 0xFF0099CC;
    public final static int COLOR_AZUL_OSCURO = 0xFF0041CC;
    public final static int COLOR_NARANJA = 0xFFFF8800;
    public final static int COLOR_ROJO = 0xFFCC0000;
    public final static int COLOR_ROSA = 0xFFD687AB;

    @NonNull
    public static PaletaColores paletaFromLegacyColor(int legacyColor) {
        switch (legacyColor) {
            case COLOR_NARANJA:
                return PaletaColores.ORANGE;
            case COLOR_AZUL:
                return PaletaColores.BLUE;
            case COLOR_AZUL_OSCURO:
                return PaletaColores.INDIGO;
            case COLOR_VERDE:
                return PaletaColores.GREEN;
            case COLOR_VERDE_CLARO:
                return PaletaColores.LIME;
            case COLOR_MORADO:
                return PaletaColores.PURPLE;
            case COLOR_ROSA:
                return PaletaColores.PINK;
            case COLOR_ROJO:
            default:
                return PaletaColores.RED;
        }
    }
}
