package com.sloy.sevibus.modules.endpoint;

public class Endpoint {

    public static final Endpoint CUSTOM = new Endpoint("Custom", null);

    private final String name;
    private final String url;

    public Endpoint(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String name() {
        return name;
    }

    public String url() {
        return url;
    }
}
