package com.sloy.sevibus.resources.datasource.bonobus;

public class MockBonobusSaldoDataSource implements BonobusSaldoDataSource {

    @Override
    public double getSaldo(long idBonobus) {
        return 0.70d;
        //return 1.20d;
    }
}
