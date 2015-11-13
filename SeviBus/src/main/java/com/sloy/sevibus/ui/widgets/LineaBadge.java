package com.sloy.sevibus.ui.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Linea;

public class LineaBadge extends FrameLayout {

    private TextView badge;
    private float size3digits;
    private float size2digits;

    public LineaBadge(Context context) {
        super(context);
        init(context);
    }

    public LineaBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LineaBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        size3digits = getResources().getDimension(R.dimen.linea_badge_text_size_3digits);
        size2digits = getResources().getDimension(R.dimen.linea_badge_text_size_2digits);

        badge = (TextView) LayoutInflater.from(context).inflate(R.layout.include_linea_badge_content, this, false);
        if (isInEditMode()) {
            badge.setText("01");
        }
        this.addView(badge);
    }

    public void setLinea(Linea linea) {
        String numero = linea.getNumero();
        badge.setText(numero);
        if (numero.length() > 2) {
            badge.setTextSize(TypedValue.COMPLEX_UNIT_PX, size3digits);
        } else {
            badge.setTextSize(TypedValue.COMPLEX_UNIT_PX, size2digits);
        }

        Drawable background = badge.getBackground();
        ((GradientDrawable) background).setColor(linea.getColorInt());
    }
}
