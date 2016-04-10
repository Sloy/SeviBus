package com.sloy.sevibus.model.tussam;

import android.graphics.Color;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.ArrayList;
import java.util.Collection;

@DatabaseTable
public class Linea implements Comparable<Linea>{

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String numero;

    @DatabaseField
    private String nombre;

    @DatabaseField
    private String color;

    @ForeignCollectionField
    private Collection<Seccion> trayectos;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private TipoLinea tipo;

    public Linea() {
    }

    public Linea(Integer macro, String label, String nombre, String color, Collection<Seccion> secciones, TipoLinea tipo) {
        super();
        this.id = macro;
        this.numero = label;
        this.nombre = nombre;
        this.color = color;
        this.trayectos = secciones;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    public int getColorInt() {
        return Color.parseColor(getColor());
    }

    public Collection<Seccion> getSecciones() {
        return trayectos;
    }

    public TipoLinea getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return numero + ": " + nombre;
    }

    @Override
    public int compareTo(Linea another) {
        int compTipo = this.tipo.getId() - another.getTipo().getId();
        if (compTipo != 0) {
            return compTipo;
        } else {
            return this.getNombre().compareTo(another.getNombre());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Linea) {
            return id == ((Linea) o).getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
