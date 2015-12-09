package com.sloy.sevibus.model;

import android.support.annotation.Nullable;

public enum PaletaColores {
    RED(0xFFaa0000, 0xFF800000, 0xFFFB8C00),
    ORANGE(0xFFFF9800, 0xFFF57C00, 0xFF009688),
    INDIGO(0xFF3F51B5, 0xFF303F9F, 0xFFFF4081),
    BLUE(0xFF2196F3, 0xFF1976D2, 0xFFFF5252),
    GREEN(0xFF4CAF50, 0xFF388E3C, 0xFF795548),
    LIME(0xFFCDDC39, 0xFFAFB42B, 0xFFFF4081),
    PURPLE(0xFF673AB7, 0xFF512DA8, 0xFF607D8B),
    PINK(0xFFE91E63, 0xFFC2185B, 0xFFE040FB);

    public static final PaletaColores[] ALL_ORDERED = new PaletaColores[]{
      RED, INDIGO, GREEN, PURPLE,
      PINK, LIME, BLUE, ORANGE
    };

    public final int primary;
    public final int dark;
    public final int accent;

    PaletaColores(int primary, int dark, int accent) {
        this.primary = primary;
        this.dark = dark;
        this.accent = accent;
    }

    @Nullable
    public static PaletaColores fromPrimary(int primary) {
        for (PaletaColores paletaColores : values()) {
            if (paletaColores.primary == primary) {
                return paletaColores;
            }
        }
        return null;
    }
}
