package com.isseiaoki.simplecropview.callback;

import android.graphics.RectF;

public interface MoveUpCallback extends Callback{
    void onSuccess(int width, int height, final RectF rect);
    void onDown();
    void onRelease();
}
