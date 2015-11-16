package com.sloy.sevibus.resources;

public class TimeTracker {

    private long startTime;

    public TimeTracker() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long calculateInterval() {
        long end = System.currentTimeMillis();
        return end - startTime;
    }
}
