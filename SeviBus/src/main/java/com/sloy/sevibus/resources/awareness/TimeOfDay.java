package com.sloy.sevibus.resources.awareness;

import org.joda.time.LocalDateTime;

public class TimeOfDay {

    private final int hour;
    private final int minute;

    public TimeOfDay(int hour, int minute) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getNextOccurrenceMillis() {
        LocalDateTime dateTime = new LocalDateTime()
          .withHourOfDay(hour)
          .withMinuteOfHour(minute)
          .withSecondOfMinute(0);

        if (dateTime.isBefore(LocalDateTime.now())) {
            return dateTime.plusDays(1).toDate().getTime();
        }
        return dateTime.toDate().getTime();
    }
}
