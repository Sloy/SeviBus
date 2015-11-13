package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Reciente {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Parada paradaAsociada;

    @DatabaseField
    private long createdAt;


    public Parada getParadaAsociada() {
        return paradaAsociada;
    }

    public void setParadaAsociada(Parada paradaAsociada) {
        this.paradaAsociada = paradaAsociada;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getId() {
        return id;
    }
}
