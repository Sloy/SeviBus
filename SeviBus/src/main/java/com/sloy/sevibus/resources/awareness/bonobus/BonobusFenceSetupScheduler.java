package com.sloy.sevibus.resources.awareness.bonobus;

import android.util.Log;

import com.sloy.sevibus.resources.FeatureToggle;
import com.sloy.sevibus.resources.awareness.AlarmManagerWrapper;
import com.sloy.sevibus.resources.awareness.AwarenessFenceSetupReceiver;
import com.sloy.sevibus.resources.awareness.TimeOfDay;

public class BonobusFenceSetupScheduler {

    private static final int BONOBUS_REQUEST_CODE = 1242548912;

    private final AlarmManagerWrapper alarmManager;
    private final FeatureToggle featureToggle;

    public BonobusFenceSetupScheduler(AlarmManagerWrapper alarmManager, FeatureToggle featureToggle) {
        this.alarmManager = alarmManager;
        this.featureToggle = featureToggle;
    }

    public void schedule() {
        if (!featureToggle.isAwarenessBonobusEnabled()) {
            return;
        }
        Log.d("SeviBus", "Scheduled bonobus fence setup");
        if (!alarmManager.isAlreadySet(AwarenessFenceSetupReceiver.ACTION_BONOBUS, BONOBUS_REQUEST_CODE)) {
            alarmManager.setRepeatingDaily(AwarenessFenceSetupReceiver.ACTION_BONOBUS, BONOBUS_REQUEST_CODE, getTriggerTime());
        }
    }

    private TimeOfDay getTriggerTime() {
        return new TimeOfDay(7, 30);
    }
}
