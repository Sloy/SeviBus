package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.datasource.bonobus.BonobusApiModel;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface SevibusApi {

    @GET("/llegada/{parada}/")
    Observable<List<ArrivalTime>> getArrivals(@Path("parada") Integer parada, @Query("lineas") List<String> lineas);

    @GET("/legacy/card/{numero}/")
    Observable<BonobusApiModel> getBonobus(@Path("numero") Long numero);
}
