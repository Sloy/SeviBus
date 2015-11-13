package com.sloy.sevibus.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.ui.fragments.BaseDBFragment;
import com.sloy.sevibus.ui.fragments.MainPageFragment;
import de.cketti.library.changelog.ChangeLog;
import java.util.List;

/**
 * Created by rafa on 17/07/13.
 */
public class NewVersionMainFragment extends BaseDBFragment {


    private MainPageFragment mainPage;

    public static NewVersionMainFragment getInstance() {
        return new NewVersionMainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_newversion, container, false);

        v.findViewById(R.id.main_newversion_ocultar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPage.dismissNewVersionCard(false);
            }
        });

        v.findViewById(R.id.main_newversion_ocultar_siempre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPage.dismissNewVersionCard(true);
            }
        });

        List<ChangeLog.ReleaseItem> logArray = new ChangeLog(getActivity()).getChangeLog(true);
        StringBuilder sb = new StringBuilder();
        ChangeLog.ReleaseItem latestVersion = logArray.get(0);

        // Título
        TextView titulo = (TextView) v.findViewById(R.id.main_newversion_title);
        titulo.setText(String.format(titulo.getText().toString(), latestVersion.versionName));

        // Contenido
        for (String s : latestVersion.changes) {
            sb.append("\n * ").append(s).append("\n");
        }
        if (sb.length() > 2) {
            sb.deleteCharAt(0);
            sb.setLength(sb.length() - 1);
        }
        ((TextView) v.findViewById(R.id.main_newversion_text)).setText(sb.toString());
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setMainPage(MainPageFragment mainPage) {
        this.mainPage = mainPage;
    }
}
