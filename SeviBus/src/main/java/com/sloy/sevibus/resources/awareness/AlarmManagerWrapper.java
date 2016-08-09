package com.sloy.sevibus.resources.awareness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerWrapper {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmManagerWrapper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public boolean isAlreadySet(String action, int requestCode) {
        Intent afternoonIntent = new Intent(action);
        return PendingIntent.getBroadcast(context, requestCode, afternoonIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public void setRepeatingDaily(String action, int requestCode, TimeOfDay timeOfDay) {
        long triggerTimeMillis = timeOfDay.getNextOccurrenceMillis();
        Intent afternoonIntent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, afternoonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

}
