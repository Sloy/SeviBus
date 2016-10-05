package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import de.cketti.library.changelog.ChangeLog;

public class AcercaDeActivity extends BaseToolbarActivity {
    private TextView mVersion, mVersionDB;
    private View mAutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);

        getSupportActionBar().setTitle("Acerca de");

        mVersion = (TextView) findViewById(R.id.acerca_version);
        mVersionDB = (TextView) findViewById(R.id.acerca_db_version);
        mAutor = findViewById(R.id.acerca_autor);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mVersion) {
                    abreChangelog();
                } else if (v == mAutor) {
                    abrePerfil();
                }
            }
        };

        mVersion.setOnClickListener(listener);
        mAutor.setOnClickListener(listener);

        mVersion.setText(getString(R.string.version_text, BuildConfig.VERSION_NAME));
        mVersionDB.setText(getString(R.string.version_db_text, getSharedPreferences("datos", Context.MODE_MULTI_PROCESS).getInt("data_version", getResources().getInteger(R.integer.data_version_assets))));
    }

    private void abrePerfil() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_perfil)));
        startActivity(intent);

    }

    private void abreChangelog() {
        ChangeLog cl = new ChangeLog(this);
        cl.getFullLogDialog().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acerca, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_changelog:
                abreChangelog();
                return true;
            case R.id.menu_perfil:
                abrePerfil();
                return true;
            case R.id.menu_preguntas_frecuentes:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_faq)));
                startActivity(intent);
                return true;
            default:
                {
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
