package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.sloy.sevibus.R;

public class ParadaInfoActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parada_info);
    }

    public static Intent getIntent(Context context, int idParada) {
        Intent i = new Intent(context, ParadaInfoActivity.class);
        i.putExtra("parada_numero", idParada);
        return i;
    }
}
