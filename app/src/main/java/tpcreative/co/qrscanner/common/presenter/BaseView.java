package tpcreative.co.qrscanner.common.presenter;

import android.content.Context;

import java.util.List;

public interface BaseView <T> {
    void startLoading();
    void stopLoading();
    Context getContext();
    void showListObjects(List<T> list);
    void showObjects(T object);
    List<T> getListObjects();
    T getObjects();
}
