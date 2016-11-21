package com.sloy.sevibus.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.BonobusInfoReader;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.widgets.BonobusView;

public class NuevoBonobusActivity extends BaseToolbarActivity {

    private static final int ANIM_DURATION_SHOW = 500;
    private static final int ANIM_DURATION_HIDE = 250;


    private EditText mNumeroText;
    private ProgressBar mCargando;
    private VerificaBonobusTask mCurrentTask;
    private Button mGuardar;
    private EditText mNombrePropio;
    private BonobusView mBonobusView;
    private View mErrorView;
    private View mAyudaView;

    private Bonobus mBonobusConfigurado;

    private BonobusInfoReader bonobusInfoReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_bonobus);

        bonobusInfoReader = new BonobusInfoReader(StuffProvider.getSevibusApi());

        getSupportActionBar().setTitle("Nuevo Bonobús");

        mNumeroText = (EditText) findViewById(R.id.nuevo_bonobus_numero);
        mBonobusView = (BonobusView) findViewById(R.id.nuevo_bonobus_resultado);
        mCargando = (ProgressBar) findViewById(R.id.nuevo_bonobus_cargando);
        mGuardar = (Button) findViewById(R.id.nuevo_bonobus_guardar);
        mNombrePropio = (EditText) findViewById(R.id.nuevo_bonobus_nombre);
        mErrorView = findViewById(R.id.nuevo_bonobus_error);
        mAyudaView = findViewById(R.id.nuevo_bonobus_ayuda);

        mBonobusView.setCargando(false);
        reseteaInterfaz();

        mNumeroText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String numero = s.toString().replace(" ", "");
                if (numero.length() == 0) {
                    reseteaInterfaz();
                    setShowAyuda(true);
                    return;
                }
                if (numero.length() == 12) {
                    verificaNumero(numero);
                } else {
                    reseteaInterfaz();
                }
                String[] numeroPartes = splitStringEvery(numero, 4);
                String numeroSeparado = TextUtils.join("  ", numeroPartes);

                mNumeroText.removeTextChangedListener(this);
                mNumeroText.setText(numeroSeparado);
                mNumeroText.setSelection(numeroSeparado.length());
                mNumeroText.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBonobusConfigurado != null) {
                    String nombrePropio = mNombrePropio.getText().toString();
                    if (!TextUtils.isEmpty(nombrePropio)) {
                        mBonobusConfigurado.setNombre(nombrePropio);
                    }
                    DBQueries.saveBonobus(getDBHelper(), mBonobusConfigurado);
                    finish();
                } else {
                    Snackbar.make(getContainerView(), "Opps, ocurrió un error :S", Snackbar.LENGTH_LONG).show();
                    Debug.registerHandledException(new IllegalStateException("Se pulsó Guardar Bonobús con mBonobusConfigurado null"));
                }
            }
        });

        mNombrePropio.addTextChangedListener(new TextWatcher() {

            TextView nombreBonoText = (TextView) mBonobusView.findViewById(R.id.item_bonobus_nombre);

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String customName = mNombrePropio.getText().toString();
                if (!TextUtils.isEmpty(customName.trim())) {
                    nombreBonoText.setText(customName);
                } else {
                    if (mBonobusConfigurado != null) {
                        nombreBonoText.setText(mBonobusConfigurado.getTipoRepresentacion());
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nuevo_bonobus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_cancelar) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void verificaNumero(String numeroString) {
        Bonobus nuevoBono = new Bonobus();
        nuevoBono.setNumero(Long.parseLong(numeroString));

        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
            mCurrentTask = null;
        }
        mCurrentTask = new VerificaBonobusTask(nuevoBono);
        mCurrentTask.execute();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNumeroText.getWindowToken(), 0);
    }

    private void reseteaInterfaz() {
        setShowError(false);
        setShowResultado(false);
        setShowCargando(false);
        setShowGuardar(false);
    }

    private void setResultadoInfo(Bonobus bonobus) {
        mNombrePropio.setText("");
        mBonobusView.setBonobusInfo(bonobus);

    }

    private void setShowError(final boolean show) {
        mNumeroText.setError(show ? "¿Número incorrecto?" : null);
        boolean isErrorVisible = mErrorView.getVisibility() == View.VISIBLE;
        if (show != isErrorVisible) {
            float initAlpha = show ? 0f : 1f;
            float endAlpha = show ? 1f : 0f;
            ObjectAnimator animError = ObjectAnimator.ofFloat(mErrorView, "alpha", initAlpha, endAlpha);
            animError.setDuration(show ? ANIM_DURATION_SHOW : ANIM_DURATION_HIDE);
            animError.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (show) {
                        mErrorView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        mErrorView.setVisibility(View.GONE);
                    }
                }
            });
            animError.start();
        }
    }

    public static String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        }
        result[lastIndex] = s.substring(j);

        return result;
    }

    private void setShowResultado(final boolean show) {
        boolean isBonoVisible = mBonobusView.getVisibility() == View.VISIBLE;
        boolean isNombreVisible = mNombrePropio.getVisibility() == View.VISIBLE;
        if ((show && (!isBonoVisible || !isNombreVisible) || (!show && (isBonoVisible || isNombreVisible)))) {
            int duracion = show ? ANIM_DURATION_SHOW : ANIM_DURATION_HIDE;
            float initAlpha = show ? 0f : 1f;
            float endAlpha = show ? 1f : 0f;

            ObjectAnimator animBono = ObjectAnimator.ofFloat(mBonobusView, "alpha", initAlpha, endAlpha);
            ObjectAnimator animNombreAlpha = ObjectAnimator.ofFloat(mNombrePropio, "alpha", initAlpha, endAlpha);

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(duracion);

            if (show) {
                float alturaNombre = mNombrePropio.getHeight();
                float initTransY = -2 * alturaNombre;
                float endTransY = 0f;

                ObjectAnimator animNombreTranslationY = ObjectAnimator.ofFloat(mNombrePropio, "translationY", initTransY, endTransY);
                animNombreTranslationY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mNombrePropio.setVisibility(View.VISIBLE);
                    }
                });
                animBono.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBonobusView.setVisibility(View.VISIBLE);
                    }
                });
                animSet.play(animBono).before(animNombreAlpha).before(animNombreTranslationY);
            } else {
                animSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mBonobusView.setVisibility(View.GONE);
                        mNombrePropio.setVisibility(View.GONE);
                    }
                });
                animSet.playTogether(animBono, animNombreAlpha);
            }
            animSet.start();
        }
    }

    private void setShowCargando(final boolean show) {
        boolean isCargandoVisible = mCargando.getVisibility() == View.VISIBLE;
        if ((show && !isCargandoVisible) || (!show && isCargandoVisible)) {
            int duracion = show ? ANIM_DURATION_SHOW : ANIM_DURATION_HIDE;
            float initAlpha = show ? 0f : 1f;
            float endAlpha = show ? 1f : 0f;

            ObjectAnimator animCargando = ObjectAnimator.ofFloat(mCargando, "alpha", initAlpha, endAlpha);
            animCargando.setDuration(duracion);
            animCargando.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (show) {
                        mCargando.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        mCargando.setVisibility(View.GONE);
                    }
                }
            });
            animCargando.start();
        }
    }

    private void setShowGuardar(final boolean show) {
        boolean isGuardarVisible = mGuardar.getVisibility() == View.VISIBLE;
        if ((show && !isGuardarVisible) || (!show && isGuardarVisible)) {
            int duracion = show ? ANIM_DURATION_SHOW : ANIM_DURATION_HIDE;

            float alturaBoton = mGuardar.getHeight();
            float initTransY = show ? alturaBoton : 0f;
            float endTransY = show ? 0f : alturaBoton;

            ObjectAnimator animGuardar = ObjectAnimator.ofFloat(mGuardar, "translationY", initTransY, endTransY);
            animGuardar.setDuration(duracion);
            animGuardar.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (show) {
                        mGuardar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        mGuardar.setVisibility(View.GONE);
                    }
                }
            });
            animGuardar.start();
        }
    }

    private void setShowAyuda(final boolean show) {
        boolean isAyudaVisible = mAyudaView.getVisibility() == View.VISIBLE;
        if ((show && !isAyudaVisible) || (!show && isAyudaVisible)) {
            int duracion = show ? ANIM_DURATION_SHOW : ANIM_DURATION_HIDE;
            float initAlpha = show ? 0f : 1f;
            float endAlpha = show ? 1f : 0f;

            ObjectAnimator animCargando = ObjectAnimator.ofFloat(mAyudaView, "alpha", initAlpha, endAlpha);
            animCargando.setDuration(duracion);
            animCargando.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (show) {
                        mAyudaView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        mAyudaView.setVisibility(View.GONE);
                    }
                }
            });
            animCargando.start();
        }
    }

    private class VerificaBonobusTask extends AsyncTask<Void, Void, Bonobus> {

        Bonobus mBonobus;

        private VerificaBonobusTask(Bonobus bonobus) {
            this.mBonobus = bonobus;
        }

        @Override
        protected void onPreExecute() {
            setShowCargando(true);
            setShowResultado(false);
            setShowGuardar(false);
            setShowAyuda(false);
        }

        @Override
        protected Bonobus doInBackground(Void... params) {
            try {
                return bonobusInfoReader.populateBonobusInfo(mBonobus);
            } catch (Exception e) {
                mBonobus.setError(true);
                Log.w("SeviBus", "Error leyendo el bonobús", e);
                Debug.registerHandledException(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bonobus bonobus) {
            if (isCancelled()) {
                return;
            }
            if (bonobus == null || bonobus.isError() || !bonobus.isRelleno()) {
                setShowError(true);
                setShowCargando(false);
                setShowResultado(false);
                setShowGuardar(false);
            } else {
                bonobus.setNombre(bonobus.getTipoRepresentacion());
                mBonobusConfigurado = bonobus;
                setResultadoInfo(mBonobus);
                setShowCargando(false);
                setShowResultado(true);
                setShowGuardar(true);
            }
        }
    }

}
