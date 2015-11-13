package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class TipoLinea implements Comparable<TipoLinea> {

    @DatabaseField(id = true)
    private Integer id;
    @DatabaseField
    private String nombre;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TipoLinea other = (TipoLinea) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int compareTo(TipoLinea another) {
        return this.id - another.getId();
    }
    
    
}
