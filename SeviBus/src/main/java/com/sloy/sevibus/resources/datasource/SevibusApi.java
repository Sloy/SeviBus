package com.sloy.sevibus.resources.datasource;

import retrofit.http.GET;
import retrofit.http.Path;

public interface SevibusApi {

    @GET("/llegada/{parada}/{linea}")
    ArrivalTimesApiModel getArrival(@Path("parada") Integer parada, @Path("linea") String linea);
}
