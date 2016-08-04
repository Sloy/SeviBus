package com.sloy.sevibus.resources.actions;

import android.util.Pair;

import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.datasource.BonobusDataSource;
import com.sloy.sevibus.resources.datasource.BonobusSaldoDataSource;

import rx.Observable;


public class HasBonobusesExpiringAction {

    private static final Double SALDO_THRESHOLD = 1.0d;

    private final BonobusDataSource bonobusDataSource;
    private final BonobusSaldoDataSource bonobusSaldoDataSource;

    public HasBonobusesExpiringAction(BonobusDataSource bonobusDataSource, BonobusSaldoDataSource bonobusSaldoDataSource) {
        this.bonobusDataSource = bonobusDataSource;
        this.bonobusSaldoDataSource = bonobusSaldoDataSource;
    }

    public Observable<Boolean> obtainBonobuses() {
        return Observable.from(bonobusDataSource.obtainBonobuses())
                .filter(this::hasTypeWithSaldo)
                .map(bonobus -> Pair.create(bonobus, getSaldo(bonobus)))
                .filter(this::isExpiring)
                .count()
                .map(count -> count > 0);
    }

    private boolean hasTypeWithSaldo(Bonobus bonobus) {
        return bonobus.getTipo() == Bonobus.TIPO.SALDO || bonobus.getTipo() == Bonobus.TIPO.SALDO_TRANSBORDO;
    }

    private Boolean isExpiring(Pair<Bonobus, Double> bonobusSaldo) {
        Double saldo = bonobusSaldo.second;
        return saldo <= SALDO_THRESHOLD;
    }

    private Double getSaldo(Bonobus bonobus) {
        return bonobusSaldoDataSource.getSaldo(bonobus.getNumero());
    }
}
