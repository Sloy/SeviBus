package com.sloy.sevibus.domain.model

import com.sloy.sevibus.model.tussam.Parada
import rx.Observable
import rx.Single

interface ParadaCollection {

    fun getById(paradaId: Int): Single<Parada>

    fun getByLinea(lineaId: Int): Observable<Parada>

    fun getBySeccion(seccionId: Int): Observable<Parada>

    fun getByQuery(query: String): Observable<Parada>

}