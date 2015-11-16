package com.sloy.sevibus.model;

public class ArrivalTime {

    private String busLineName;
    private Integer busStopNumber;

    private BusArrival nextBus;
    private BusArrival secondBus;

    private boolean loading;

    private String dataSource;


    public boolean isAvailable() {
        return nextBus!= null && nextBus.status != Status.NOT_AVAILABLE;
    }

    public String getBusLineName() {
        return busLineName;
    }

    public void setBusLineName(String busLineName) {
        this.busLineName = busLineName;
    }

    public Integer getBusStopNumber() {
        return busStopNumber;
    }

    public void setBusStopNumber(Integer busStopNumber) {
        this.busStopNumber = busStopNumber;
    }

    public BusArrival getNextBus() {
        return nextBus;
    }

    public void setNextBus(BusArrival nextBus) {
        this.nextBus = nextBus;
    }

    public BusArrival getSecondBus() {
        return secondBus;
    }

    public void setSecondBus(BusArrival secondBus) {
        this.secondBus = secondBus;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public enum Status {
        ESTIMATE, IMMINENT, NO_ESTIMATION, NOT_AVAILABLE,
    }

    public static class BusArrival {
        private Status status;
        private int timeInMinutes;

        private int distanceInMeters;

        public BusArrival(Status status) {
            this.status = status;
        }

        public void setTimeInMinutes(int timeInMinutes) {
            this.timeInMinutes = timeInMinutes;
        }

        public void setDistanceInMeters(int distanceInMeters) {
            this.distanceInMeters = distanceInMeters;
        }

        public Status getStatus() {
            return status;
        }

        public int getTimeInMinutes() {
            return timeInMinutes;
        }
        public int getDistanceInMeters() {
            return distanceInMeters;
        }

        @Override public String toString() {
            return "BusArrival{" +
                    "status=" + status +
                    ", timeInMinutes=" + timeInMinutes +
                    ", distanceInMeters=" + distanceInMeters +
                    '}';
        }
    }

    @Override public String toString() {
        return "Arrivals{" +
                "busLineName='" + busLineName + '\'' +
                ", busStopNumber=" + busStopNumber +
                ", nextBus=" + nextBus +
                ", secondBus=" + secondBus +
                ", available=" + isAvailable() +
                '}';
    }
}
