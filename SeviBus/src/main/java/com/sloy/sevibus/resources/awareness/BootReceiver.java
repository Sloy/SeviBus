package com.sloy.sevibus.resources.awareness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sloy.sevibus.resources.StuffProvider;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        StuffProvider.getBonobusFenceSetupScheduler(context).schedule();
    }
}
