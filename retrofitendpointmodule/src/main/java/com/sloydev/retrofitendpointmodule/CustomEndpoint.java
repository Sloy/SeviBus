package com.sloydev.retrofitendpointmodule;

import retrofit.Endpoint;

public class CustomEndpoint implements Endpoint {

    private static final CustomEndpoint singleInstance = new CustomEndpoint();

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getName() {
        return "Custom";
    }

    public static Endpoint get() {
        return singleInstance;
    }
}
