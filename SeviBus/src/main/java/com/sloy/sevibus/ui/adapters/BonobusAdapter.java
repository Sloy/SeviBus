package com.sloy.sevibus.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.ui.widgets.BonobusView;
import java.util.List;

/**
 * Created by rafa on 03/02/14.
 */
public class BonobusAdapter extends BaseAdapter {


    public class Holder {
        TextView nombrePropio;
        TextView numero;
        TextView descripcion;
        ProgressBar cargando;
        ImageView imagen;
    }

    private List<Bonobus> mListaBonobuses;
    private LayoutInflater mLayoutInflater;

    public BonobusAdapter(Context context, List<Bonobus> listaBonobuses) {
        mLayoutInflater = LayoutInflater.from(context);
        mListaBonobuses = listaBonobuses;
    }

    @Override
    public int getCount() {
        return mListaBonobuses != null ? mListaBonobuses.size()+1 : 1;
    }

    @Override
    public Bonobus getItem(int position) {
        return mListaBonobuses != null ? mListaBonobuses.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == 0) {
            return mListaBonobuses != null ? mListaBonobuses.get(position).getNumero() : 0;
        } else {
            return -1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // Tipo 1 sólo si es el último elemento
        return position == getCount() - 1 ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            return getBonobusView(position, convertView, parent);
        } else {
            return getBotonNuevoBonobusView(position, convertView, parent);
        }
    }

    private View getBonobusView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_bonobus, parent, false);
            assert convertView != null;

        }
        BonobusView bonobusView = (BonobusView) convertView;
        Bonobus item = getItem(position);
        bonobusView.setBonobusInfo(item);
        if (item.isError()) {
            bonobusView.setError("Error leyendo la información");
            bonobusView.setCargando(false);
        } else {
            if (item.isRelleno()) {
                bonobusView.setCargando(false);
            } else {
                bonobusView.setCargando(true);
            }
        }

        return convertView;
    }

    private View getBotonNuevoBonobusView(int position, View convertView, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.list_item_bonobus_nuevo, parent, false);
    }


}
