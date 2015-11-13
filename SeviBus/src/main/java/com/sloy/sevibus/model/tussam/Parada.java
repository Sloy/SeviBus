package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Parada implements Comparable<Parada> {

    @DatabaseField(id = true)
    private Integer numero;
    @DatabaseField
    private String descripcion;
    @DatabaseField
    private Double latitud;
    @DatabaseField
    private Double longitud;

    public Parada() {
    }

    public Parada(Integer codigoNodo, String descripcion, Double latitud, Double longitud) {
        super();
        this.numero = codigoNodo;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return numero + ": " + descripcion;
    }

    @Override
    public int compareTo(Parada another) {
        return this.getNumero().compareTo(another.getNumero());
    }
}
