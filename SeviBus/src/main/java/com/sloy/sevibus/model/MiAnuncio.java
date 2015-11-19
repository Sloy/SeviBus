package com.sloy.sevibus.model;

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
