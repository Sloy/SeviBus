package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;

public class ContactoActivity extends BaseToolbarActivity {

    private Button mEnviar;
    private EditText mMensaje;
    private CheckBox[] mChecksTrue, mChecksFalse;
    private CheckBox mDeviceInfo;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        getSupportActionBar().setTitle("Contacto vía email");

        mEnviar = (Button) findViewById(R.id.contacto_enviar);
        mMensaje = (EditText) findViewById(R.id.contacto_texto);
        mDeviceInfo = (CheckBox) findViewById(R.id.contacto_device_info);

        mChecksTrue = new CheckBox[]{
                (CheckBox) findViewById(R.id.contacto_check_true1),
                (CheckBox) findViewById(R.id.contacto_check_true2),
                (CheckBox) findViewById(R.id.contacto_check_true3),
                (CheckBox) findViewById(R.id.contacto_check_true4),
                (CheckBox) findViewById(R.id.contacto_check_true5)
        };

        mChecksFalse = new CheckBox[]{
                (CheckBox) findViewById(R.id.contacto_check_false1),
                (CheckBox) findViewById(R.id.contacto_check_false2)
        };


        mEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYEnviar();
            }
        });

        mMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEnviar.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validarYEnviar() {
        boolean valido = true;

        if (TextUtils.isEmpty(mMensaje.getText().toString())) {
            errorMensajeVacio();
            valido = false;
        }

        // Valida que todas las casillas "válidas" estén marcadas
        for (CheckBox check : mChecksTrue) {
            if (!check.isChecked()) {
                errorChecksIncorrectos();
                valido = false;
            }
        }

        // Valida que no haya ninguna casilla "inválida" marcada
        for (CheckBox check : mChecksFalse) {
            if (check.isChecked()) {
                errorChecksIncorrectos();
                valido = false;
            }
        }

        if (valido) {
            enviar();
        }
    }

    private void errorMensajeVacio() {
        Snackbar.make(getContainerView(), "No querrás enviarme un mensaje vacío, ¿verdad?", Snackbar.LENGTH_LONG).show();
    }

    private void errorChecksIncorrectos() {
        Snackbar.make(getContainerView(), "Quizás deberías leer mejor...", Snackbar.LENGTH_LONG).show();
    }

    private void enviar() {
        StringBuilder texto = new StringBuilder(mMensaje.getText().toString().trim());
        texto.append("\n\n=====================");
        texto.append("\n Versión: " + BuildConfig.VERSION_NAME);
        texto.append("\n Base de datos: " + getSharedPreferences("datos", Context.MODE_MULTI_PROCESS).getLong("data_version", 0));
        if (mDeviceInfo.isChecked()) {
            texto.append("\n\nInformación del dispositivo");
            texto.append("\n---------- ");
            texto.append("\n- Release: " + Build.VERSION.RELEASE);
            texto.append("\n- SDK: " + Build.VERSION.SDK_INT);
            texto.append("\n- Codename: " + Build.VERSION.CODENAME);
            texto.append("\n- Incremental: " + Build.VERSION.INCREMENTAL);
            texto.append("\n- Brand: " + Build.BRAND);
            texto.append("\n- Device: " + Build.DEVICE);
            texto.append("\n- Display: " + Build.DISPLAY);
            if (Build.VERSION.SDK_INT >= 8) {
                texto.append("\n- Hardware: " + Build.HARDWARE);
            }
            texto.append("\n- Manufacturer: " + Build.MANUFACTURER);
            texto.append("\n- Model: " + Build.MODEL);
            texto.append("\n- Product: " + Build.PRODUCT);
        }
        mMensaje.setError(null);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_address)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, texto.toString());
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_intent_chooser)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_perfil:
                abrirPerfil();
                return true;
            case R.id.menu_preguntas_frecuentes:
                abrirPreguntasFrecuentes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirPerfil() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_perfil)));
        startActivity(intent);
    }

    private void abrirPreguntasFrecuentes() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_faq)));
        startActivity(intent);
    }
}
