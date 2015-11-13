package com.sloy.sevibus.resources.syncadapter;

import android.content.Intent;

public class IntentFactory {

    public static Intent shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Prueba SeviBus para Android! https://play.google.com/store/apps/details?id=com.sloy.sevibus");
        sendIntent.setType("text/plain");
        return Intent.createChooser(sendIntent, "Compartir vía...");
    }
}
