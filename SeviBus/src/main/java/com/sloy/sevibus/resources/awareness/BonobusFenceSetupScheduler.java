package com.sloy.sevibus.resources.awareness;

import android.util.Log;

public class BonobusFenceSetupScheduler {

    private static final int BONOBUS_REQUEST_CODE = 1242548912;

    private final AlarmManagerWrapper alarmManager;

    public BonobusFenceSetupScheduler(AlarmManagerWrapper alarmManager) {
        this.alarmManager = alarmManager;
    }

    public void schedule() {
        Log.d("SeviBus", "Scheduled bonobus fence setup");
        if (!alarmManager.isAlreadySet(AwarenessFenceSetupReceiver.ACTION_BONOBUS, BONOBUS_REQUEST_CODE)) {
            alarmManager.setRepeatingDaily(AwarenessFenceSetupReceiver.ACTION_BONOBUS, BONOBUS_REQUEST_CODE, getTriggerTime());
        }
    }

    private TimeOfDay getTriggerTime() {
        return new TimeOfDay(7, 30);
    }
}
