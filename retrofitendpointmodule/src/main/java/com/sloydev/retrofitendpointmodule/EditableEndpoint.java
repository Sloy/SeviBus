package com.sloydev.retrofitendpointmodule;

import retrofit.Endpoint;

public class EditableEndpoint implements Endpoint {

    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getName() {
        return url;
    }
}
