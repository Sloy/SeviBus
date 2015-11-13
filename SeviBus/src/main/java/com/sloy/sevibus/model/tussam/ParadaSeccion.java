package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;

public class ParadaSeccion {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true)
    private Seccion seccion;

    @DatabaseField(foreign = true)
    private Parada parada;

    public ParadaSeccion() {
    }

    public ParadaSeccion(Seccion seccion, Parada parada) {
        this.seccion = seccion;
        this.parada = parada;
    }
    
    @Override
    public String toString(){
        return parada.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
    }
}
