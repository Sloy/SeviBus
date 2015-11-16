package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.ArrivalTime;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface SevibusApi {

    @GET("/llegada/{parada}/{linea}")
    Observable<ArrivalTime> getArrival(@Path("parada") Integer parada, @Path("linea") String linea);
}
