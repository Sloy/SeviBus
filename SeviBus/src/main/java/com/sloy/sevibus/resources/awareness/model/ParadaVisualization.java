package com.sloy.sevibus.resources.awareness.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ParadaVisualization {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private Integer paradaNumero;
    @DatabaseField
    private Long timestamp;

    public ParadaVisualization() {
    }

    public ParadaVisualization(Integer paradaNumero, Long timestamp) {
        this.paradaNumero = paradaNumero;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return id;
    }

    public Integer getParadaNumero() {
        return paradaNumero;
    }

    public void setParadaNumero(Integer paradaNumero) {
        this.paradaNumero = paradaNumero;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
