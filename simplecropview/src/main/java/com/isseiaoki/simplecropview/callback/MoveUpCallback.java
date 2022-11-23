package com.isseiaoki.simplecropview.callback;

public interface MoveUpCallback extends Callback{
    void onSuccess(int width, int height);
    void onDown();
    void onRelease();
}
