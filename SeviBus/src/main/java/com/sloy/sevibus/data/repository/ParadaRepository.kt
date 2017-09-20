package com.sloy.sevibus.data.repository

import android.util.Log
import com.sloy.sevibus.data.datasource.DBParadaDataSource
import com.sloy.sevibus.domain.model.ParadaCollection
import com.sloy.sevibus.model.tussam.Parada
import rx.Observable
import rx.Single

class ParadaRepository(private val dbDataSource: DBParadaDataSource) : ParadaCollection {

    override fun getById(paradaId: Int): Single<Parada> {
        return Single.just(dbDataSource.getById(paradaId))
    }

    override fun getByLinea(lineaId: Int): Observable<Parada> {
        return Observable.from(dbDataSource.getByLinea(lineaId))
    }

    override fun getBySeccion(seccionId: Int): Observable<Parada> {
        return Observable.from(dbDataSource.getBySeccion(seccionId))
    }

    override fun getByQuery(query: String): Observable<Parada> {
        return Observable.from(dbDataSource.getByQuery(query))
    }

    override fun getByLocation(minLatitud: Double, maxLatitud: Double, minLongitud: Double, maxLongitud: Double): Observable<Parada> {
        return Observable.from(dbDataSource.getByLocation(minLatitud, maxLatitud, minLongitud, maxLongitud))
    }
}