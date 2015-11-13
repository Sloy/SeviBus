package com.sloy.sevibus.model;

public class Llegada {


    private String lineaNumero;
    private Bus bus1;
    private Bus bus2;

    public Llegada(String lineaNumero, Bus bus1, Bus bus2) {
        this.lineaNumero = lineaNumero;
        this.bus1 = bus1;
        this.bus2 = bus2;
    }

    public Llegada(String lineaNumero) {
        this(lineaNumero, null, null);
    }

    /**
     * Devuelve una cadena con la informaci�n de la llegada del bus 1
     *
     * @return Error | Sin estimaciones | Llegada inminente | x minutos (y metros)
     */
    public String getTexto1() {
        return getTextoDisplay(getBus1());
    }

    /**
     * Devuelve una cadena con la informaci�n de la llegada del bus 2
     *
     * @return Error | Sin estimaciones | Llegada inminente | x minutos (y metros)
     */
    public String getTexto2() {
        return getTextoDisplay(getBus2());
    }

    private String getTextoDisplay(Bus bus) {
        String texto = null;
        if (bus == null) {
            return "No disponible";
        }
        int tiempo = bus.getTiempo();
        int distancia = bus.getDistancia();

        if (tiempo > 0) {
            texto = tiempo + " minutos (" + distancia + " metros)";
        } else if (tiempo == 0) {
            texto = "Llegada inminente";
        } else {
            texto = "Sin estimaciones";
        }
        return texto;
    }

    public Bus getBus1() {
        return bus1;
    }

    public Bus getBus2() {
        return bus2;
    }

    public String getLineaNumero() {
        return lineaNumero;
    }

    public void setBus1(Bus bus1) {
        this.bus1 = bus1;
    }

    public void setBus2(Bus bus2) {
        this.bus2 = bus2;
    }

    @Override
    public String toString() {
        return "Llegada [lineaNumero=" + lineaNumero + ", bus1=" + bus1 + ", bus2=" + bus2 + "]";
    }

    public static class Bus {
        private int tiempo;
        private int distancia;

        public Bus(int tiempo, int distancia) {
            this.tiempo = tiempo;
            this.distancia = distancia;
        }

        public Bus() {
        }

        public int getTiempo() {
            return tiempo;
        }

        public int getDistancia() {
            return distancia;
        }

        public void setTiempo(int tiempo) {
            this.tiempo = tiempo;
        }

        public void setDistancia(int distancia) {
            this.distancia = distancia;
        }

        @Override
        public String toString() {
            return "Bus [tiempo=" + tiempo + ", distancia=" + distancia + "]";
        }

    }
}