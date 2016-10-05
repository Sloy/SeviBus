package com.sloy.sevibus.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sloy.sevibus.R;

public class InitialFragment extends BaseDBFragment {

    public static interface ApplicationReadyListener {
        public void onApplicationReady();
    }


    private ProgressBar mProgressBar;
    private Button mReintentar;
    private TextView mInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_initializing, container, false);

        mProgressBar = (ProgressBar) v.findViewById(R.id.initial_progress);
        mReintentar = (Button) v.findViewById(R.id.initial_button);
        mInfo = (TextView) v.findViewById(R.id.initial_info);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mReintentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDatabase();
            }
        });
        setupDatabase();
    }

    private void setupDatabase() {
        mProgressBar.setVisibility(View.VISIBLE);
        mReintentar.setVisibility(View.INVISIBLE);
        mInfo.setText(R.string.initial_info);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                getDBHelper().updateFromAssets();
                return null;
            }

            @Override
            protected void onPostExecute(Void res) {
                arrancaAplicacion();
            }
        }.execute();
    }

    private void arrancaAplicacion() {
        ApplicationReadyListener activity = ((ApplicationReadyListener) getActivity());
        if (activity != null) {
            activity.onApplicationReady();
        } else {
            Log.w("SeviBus", "La aplicación se cerró antes de acabar la carga inicial. Bueh, pues nada.");
        }
    }

}
