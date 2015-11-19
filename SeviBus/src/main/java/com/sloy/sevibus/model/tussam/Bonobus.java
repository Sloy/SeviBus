package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Bonobus {


    public enum TIPO {
        SALDO, SALDO_TRANSBORDO, JOVEN, MENSUAL, TERCERA_EDAD, UNKNOWN
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private long numero;

    @DatabaseField
    private int nfcTagId;

    @DatabaseField
    private String nombre;

    @DatabaseField
    private TIPO tipo;

    // Campos a rellenar mediante consulta a la web
    private boolean relleno = false; // Bandera para saber si los datos dinámicos (ej: saldo) están ya cargados o no.

    private String tipoTexto;

    private String saldo;

    private String caducidad;

    private boolean error;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public int getNfcTagId() {
        return nfcTagId;
    }

    public void setNfcTagId(int nfcTagId) {
        this.nfcTagId = nfcTagId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TIPO getTipo() {
        return tipo != null ? tipo : TIPO.UNKNOWN;
    }

    public void setTipo(TIPO tipo) {
        this.tipo = tipo;
    }

    public String getNumeroFormateado() {
        String numeroLargo = String.valueOf(getNumero());
        while (numeroLargo.length() < 12) {
            numeroLargo = "0" + numeroLargo;
        }
        return numeroLargo.substring(0, 4) + "  " + numeroLargo.substring(4, 8) + "  " + numeroLargo.substring(8, 12);
    }


    public String getTipoTexto() {
        return tipoTexto;
    }

    public void setTipoTexto(String tipoTexto) {
        this.tipoTexto = tipoTexto;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }


    public String getCaducidad() {
        return caducidad;
    }

    public void setCaducidad(String caducidad) {
        this.caducidad = caducidad;
    }

    public boolean isRelleno() {
        return relleno;
    }

    public void setRelleno(boolean relleno) {
        this.relleno = relleno;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return getNumeroFormateado();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bonobus bonobus = (Bonobus) o;

        if (numero != bonobus.numero) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (numero ^ (numero >>> 32));
    }


    public String getTipoRepresentacion() {
        switch (tipo) {
            case SALDO:
                return "Bonobús saldo sin transbordo";
            case SALDO_TRANSBORDO:
                return "Bonobús saldo con transbordo";
            case JOVEN:
                return "Tarjeta Joven";
            case MENSUAL:
                return "Tarjeta 30 días";
            case UNKNOWN:
            default:
                return "Tarjeta nueva";
        }
    }
}

