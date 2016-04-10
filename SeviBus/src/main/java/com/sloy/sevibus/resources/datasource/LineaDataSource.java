package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.tussam.Linea;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.Observable;

public class LineaDataSource {
    public Observable<List<Linea>> getFromParada(int numero) {
        return Observable.just(Collections.emptyList());
    }
}
