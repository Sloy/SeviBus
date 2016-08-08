package com.sloy.sevibus.resources.actions;

import android.util.Pair;

import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.datasource.bonobus.BonobusDataSource;
import com.sloy.sevibus.resources.datasource.bonobus.BonobusSaldoDataSource;

import rx.Observable;


public class HasExpiringBonobusAction {

    private static final Double SALDO_THRESHOLD_EURO = 1.0d;

    private final BonobusDataSource bonobusDataSource;
    private final BonobusSaldoDataSource bonobusSaldoDataSource;

    public HasExpiringBonobusAction(BonobusDataSource bonobusDataSource, BonobusSaldoDataSource bonobusSaldoDataSource) {
        this.bonobusDataSource = bonobusDataSource;
        this.bonobusSaldoDataSource = bonobusSaldoDataSource;
    }

    public Observable<Boolean> hasExpiringBonobus() {
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
        return saldo <= SALDO_THRESHOLD_EURO;
    }

    private Double getSaldo(Bonobus bonobus) {
        return bonobusSaldoDataSource.getSaldo(bonobus.getNumero());
    }
}
