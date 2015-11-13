package com.sloy.sevibus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

@DatabaseTable
public class TweetHolder {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField
    private String texto;

    @DatabaseField
    private String autor;

    @DatabaseField
    private String username;

    @DatabaseField
    private String avatarUrl;

    @DatabaseField
    private Date fecha;

    // No se guarda en la bbdd
    private boolean nuevo = false;

    public TweetHolder() {
    }

    public TweetHolder(long id, String texto, String autor, String username, String avatarUrl, Date fecha, boolean nuevo) {
        super();
        this.id = id;
        this.texto = texto;
        this.autor = autor;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.fecha = fecha;
        this.nuevo = nuevo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }

    @Override
    public String toString() {
        return texto;
    }
}
