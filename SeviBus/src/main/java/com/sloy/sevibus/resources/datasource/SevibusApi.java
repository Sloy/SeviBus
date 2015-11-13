package com.sloy.sevibus.resources.datasource;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface SevibusApi {

    @GET("/llegada/{parada}/{linea}")
    Observable<ArrivalTimesApiModel> getArrival(@Path("parada") Integer parada, @Path("linea") String linea);
}
