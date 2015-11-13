package com.sloy.sevibus.model;

/**
 * Created by rafa on 15/01/14.
 */
public class MiAnuncio {

    private String enlace;
    private String imagenUrl;

    public MiAnuncio(String enlace, String imagenUrl) {
        this.enlace = enlace;
        this.imagenUrl = imagenUrl;
    }

    public String getEnlace() {
        return enlace;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
}
