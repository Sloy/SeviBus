package com.sloy.sevibus.resources.awareness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sloy.sevibus.resources.awareness.bonobus.BonobusNotifier;

/**
 * action: "com.sloy.sevibus.action.SETUP_FENCE_BONOBUS"
 */
public class AwarenessFenceSetupReceiver extends BroadcastReceiver {

    public static final String ACTION_BONOBUS = "com.sloy.sevibus.action.SETUP_FENCE_BONOBUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("Awareness", "AwarenessFenceSetupReceiver#onReceive");
        Log.d("Awareness", "action=" + action);

        if (ACTION_BONOBUS.equals(action)) {
            new BonobusNotifier().enableFence(context);
        }
    }
}
