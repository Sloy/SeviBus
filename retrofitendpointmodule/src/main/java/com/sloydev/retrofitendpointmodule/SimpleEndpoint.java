package com.sloydev.retrofitendpointmodule;

import retrofit.Endpoint;

public class SimpleEndpoint implements Endpoint {

    private final String name;
    private final String url;

    public SimpleEndpoint(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
