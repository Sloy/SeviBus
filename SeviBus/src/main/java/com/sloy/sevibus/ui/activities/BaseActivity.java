package com.sloy.sevibus.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.ThemeSelector;

public class BaseActivity extends AppCompatActivity{
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSelector.selectTheme(this);
        super.onCreate(savedInstanceState);
        Debug.activateReports(this);
    }

    protected DBHelper getDBHelper() {
        if (dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return dbHelper;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //XXX Por algún motivo, usando ActionBarSherlock no funciona el método normal de JellyBean
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
