package com.sloydev.retrofitendpointmodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Endpoint;

public class SelectableEndpoint implements Endpoint {

    private final List<Endpoint> endpoints;
    private Endpoint selectedEndpoint;

    public SelectableEndpoint(Endpoint... endpoints) {
        this(Arrays.asList(endpoints));
    }

    public SelectableEndpoint(List<Endpoint> endpoints) {
        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("Endpoints list can't be empty");
        }
        this.endpoints = endpoints;
        this.selectedEndpoint = getDefault();
    }

    public List<Endpoint> getAllEndpoints() {
        return new ArrayList<>(endpoints);
    }

    public Endpoint getDefault() {
        return endpoints.get(0);
    }

    public Endpoint getSelected() {
        return selectedEndpoint;
    }

    public void selectByPosition(int position) {
        if (position < 0 || position >= endpoints.size()) {
            throw new IllegalArgumentException("Position " + position + " not allowed in endpoint list with size " + endpoints.size());
        }
        selectedEndpoint = endpoints.get(position);
    }

    public void selectByEndpoint(Endpoint newSelectedEndpoint) {
        if (!endpoints.contains(newSelectedEndpoint)) {
            throw new IllegalArgumentException("Endpoint not found in current endpoint list");
        }
        selectedEndpoint = newSelectedEndpoint;
    }

    public void selectByUrl(String url) {

    }

    @Override
    public String getUrl() {
        return selectedEndpoint.getUrl();
    }

    @Override
    public String getName() {
        return selectedEndpoint.getName();
    }

}
