package com.sloy.sevibus.resources.awareness;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.awareness.bonobus.BonobusNotifier;

import java.util.Random;

/**
 * action: "com.sloy.sevibus.action.TRIGGER_FENCE_BONOBUS"
 * extra: ignoreConditions=true|false
 */
public class AwarenessFenceTriggerReceiver extends BroadcastReceiver {

    public static final String ACTION_BONOBUS = "com.sloy.sevibus.action.TRIGGER_FENCE_BONOBUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean ignoreConditions = intent.getBooleanExtra("ignoreConditions", false);
        Log.d("Awareness", "AwarenessFenceTriggerReceiver#onReceive");
        Log.d("Awareness", "action=" + action + "; ignoreConditions=" + ignoreConditions);

        FenceState fenceState = FenceState.extract(intent);
        String fenceKey = fenceState.getFenceKey();
        int currentState = fenceState.getCurrentState();
        Log.d("Awareness", "FenceKey=" + fenceKey+"; FenceState=" + currentState);

        if (currentState == FenceState.TRUE || ignoreConditions) {
            if (ACTION_BONOBUS.equals(action)) {
                new BonobusNotifier().checkBonobusAndNotify(context);
            }
        } else {
            Log.w("Awareness", "OMG! This fence was not true!");
            showDebugFenceNotTrue(context, fenceKey, currentState);
        }

    }

    private void showDebugFenceNotTrue(Context context, String fenceKey, int currentState) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Fence triggered but not true")
                .setContentText("Key="+fenceKey+"; state="+currentState)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification_default)
                .setLights(Color.RED, 500, 5000)
                .setColor(Color.RED)
                .build();

        nm.notify(new Random().nextInt(1000), notification);
    }

}
