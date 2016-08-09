package com.sloy.sevibus.resources.datasource.bonobus;

import com.sloy.sevibus.model.tussam.Bonobus;

import java.util.Collections;
import java.util.List;

public class MockBonobusDataSource implements BonobusDataSource {
    @Override
    public List<Bonobus> obtainBonobuses() {
        Bonobus bonobus = new Bonobus();
        bonobus.setNombre("Bono de alguien");
        bonobus.setTipo(Bonobus.TIPO.SALDO);
        bonobus.setId(1);
        bonobus.setNumero(158162610277L);
        return Collections.singletonList(bonobus);
    }
}
