package com.sloy.sevibus.ui.activities;

import android.os.Bundle;
import com.sloy.sevibus.R;
import com.sloy.sevibus.ui.fragments.AjustesFragment;

public class PreferenciasActivity extends BaseToolbarActivity {

    public static final String PREFS_CONFIG_VALUES = AjustesFragment.PREFS_CONFIG_VALUES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        getToolbar().setTitle("Ajustes");
    }


}
