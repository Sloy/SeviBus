package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Seccion {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String nombreSeccion;
    @DatabaseField
    private int numeroSeccion;
    @DatabaseField
    private String horaInicio;
    @DatabaseField
    private String horaFin;

    @DatabaseField(foreign = true)
    private Linea linea;

    //private List<Parada> paradas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Seccion() {
    }

    public Seccion(Linea linea, String nombreSeccion, int numeroSeccion, String horaInicio, String horaFin) {
        super();
        this.setLinea(linea);
        this.nombreSeccion = nombreSeccion;
        this.numeroSeccion = numeroSeccion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public String getNombreSeccion() {
        return nombreSeccion;
    }

    public int getNumeroSeccion() {
        return numeroSeccion;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public Linea getLinea() {
        return linea;
    }

    public void setLinea(Linea linea) {
        this.linea = linea;
    }
}
