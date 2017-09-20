package com.sloy.sevibus.domain.model

import com.sloy.sevibus.model.tussam.Linea
import rx.Observable
import rx.Single

interface LineaCollection {
    fun getById(lineaId: Int): Single<Linea>
    fun getAll(): Observable<Linea>
    fun getByParada(paradaId: Int): Observable<Linea>
}