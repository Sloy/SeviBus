package com.sloy.sevibus.resources;

public class TimeTracker {

    public static TimeTracker NULL = new TimeTracker(null);

    private final TimeTrackerAction endAction;
    private long startTime;

    public TimeTracker(TimeTrackerAction endAction) {
        this.endAction = endAction;
        start();
    }

    public void start() {
        if (endAction != null) {
            startTime = System.currentTimeMillis();
        }
    }

    public void end() {
        if (endAction != null) {
            endAction.doOnEnd(calculateInterval());
        }
    }

    private long calculateInterval() {
        long end = System.currentTimeMillis();
        return end - startTime;
    }

    public interface TimeTrackerAction {
        void doOnEnd(long interval);
    }
}
