package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.datasource.DBLineaDataSource
import com.sloy.sevibus.domain.model.LineaCollection
import com.sloy.sevibus.model.tussam.Linea
import rx.Observable
import rx.Single

class LineaRepository(private val dbDataSource: DBLineaDataSource) : LineaCollection {

    override fun getById(lineaId: Int): Single<Linea> {
        return Single.just(dbDataSource.getById(lineaId))
    }

    override fun getAll(): Observable<Linea> {
        return Observable.from(dbDataSource.getAll())
    }

    override fun getByParada(paradaId: Int): Observable<Linea> {
        return Observable.from(dbDataSource.getByParada(paradaId))
    }
}