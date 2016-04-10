package com.sloy.sevibus.ui.mvp.presenter;

public interface Presenter<T> {

    void initialize(T view);

    void update();

    void pause();

}
