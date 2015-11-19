package com.sloy.sevibus.resources;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.sloy.sevibus.ui.activities.HomeActivity;

public class SecretCodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            String numero = intent.getData().getHost();
            if (numero.equals("33284")) {
                Debug.setDebugEnabled(context, true);
                Toast.makeText(context, "SeviBus: Activado modo debug ;)", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
