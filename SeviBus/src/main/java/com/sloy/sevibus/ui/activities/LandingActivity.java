package com.sloy.sevibus.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.sloy.sevibus.R;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LandingActivity extends Activity {

    @Bind(R.id.landing_color_padding)
    View colorPadding;

    @Bind(R.id.landing_install_container)
    View installLayoutContainer;

    @Bind(R.id.landing_permission_card)
    View permissionCard;

    @Bind(R.id.landing_install_logo)
    View logo;

    @BindColor(R.color.primary)
    int colorPrimary;

    @BindColor(R.color.primary_dark)
    int colorPrimaryDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        ButterKnife.bind(this);
        findViewById(R.id.landing_install_logo).setOnClickListener(v -> initAnimation());
    }

    @OnClick(R.id.landing_permission_allow)
    public void onAllow() {
        int screenWidth = findViewById(android.R.id.content).getMeasuredWidth();
        installLayoutContainer.setAlpha(0f);
        installLayoutContainer.animate().alpha(1f).setDuration(750).start();
        permissionCard.animate().translationX(-screenWidth).setInterpolator(new AnticipateInterpolator()).setDuration(750).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                permissionCard.setVisibility(View.GONE);
            }
        }).start();

        ObjectAnimator logoAnimation = ObjectAnimator.ofFloat(logo, "alpha", 0.8f);
        logoAnimation.setRepeatCount(ValueAnimator.INFINITE);
        logoAnimation.setRepeatMode(ValueAnimator.REVERSE);
        logoAnimation.setInterpolator(new DecelerateInterpolator());
        logoAnimation.setDuration(500);
        logoAnimation.start();

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 1.02f, 0.95f);
        logoScaleY.setRepeatCount(ValueAnimator.INFINITE);
        logoScaleY.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.98f, 1.05f);
        logoScaleX.setRepeatCount(ValueAnimator.INFINITE);
        logoScaleX.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet logoSqueezeAnimation = new AnimatorSet();
        logoSqueezeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        logoSqueezeAnimation.setDuration(500);
        logoSqueezeAnimation.playTogether(logoScaleX, logoScaleY);
        logoSqueezeAnimation.start();
    }

    private void initAnimation() {
        ObjectAnimator installLayoutFadeOut = ObjectAnimator.ofFloat(installLayoutContainer, "alpha", 1f, 0f);

        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
          new int[]{android.R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        ValueAnimator heightAnimation = ValueAnimator.ofInt(colorPadding.getMeasuredHeight(), toolbarHeight);
        heightAnimation.addUpdateListener(valueAnimator -> {
            int height = (Integer) valueAnimator.getAnimatedValue();
            updateColorPaddingHeight(height);
        });

        ValueAnimator colorAnimation = ValueAnimator.ofArgb(colorPrimaryDark, colorPrimary);
        colorAnimation.addUpdateListener(valueAnimator -> {
            int color = (Integer) valueAnimator.getAnimatedValue();
            colorPadding.setBackgroundColor(color);
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(heightAnimation).with(colorAnimation).after(installLayoutFadeOut);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openNextScreen();
            }
        });
        animatorSet.start();
    }

    private void updateColorPaddingHeight(int val) {
        ViewGroup.LayoutParams layoutParams = colorPadding.getLayoutParams();
        layoutParams.height = val;
        colorPadding.setLayoutParams(layoutParams);
    }

    private void openNextScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
