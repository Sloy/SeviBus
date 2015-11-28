package com.sloy.sevibus.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.AppContainer;
import com.sloy.sevibus.ui.ThemeSelector;

public class BaseActivity extends AppCompatActivity{
    private DBHelper dbHelper;
    private AppContainer appContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSelector.selectTheme(this);
        super.onCreate(savedInstanceState);
        appContainer = StuffProvider.getAppContainer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        appContainer.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appContainer.onStop();
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID, getRootView(), true);
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ViewGroup getRootView() {
        return appContainer.get(this);
    }
}
