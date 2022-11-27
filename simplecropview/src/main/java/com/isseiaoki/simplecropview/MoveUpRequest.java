package com.isseiaoki.simplecropview;

import com.isseiaoki.simplecropview.callback.MoveUpCallback;



public class MoveUpRequest {
    private CropImageView cropImageView;

    public MoveUpRequest(CropImageView cropImageView) {
        this.cropImageView = cropImageView;
    }

    private void build() {
    }

    public void execute(MoveUpCallback callback) {
        build();
        cropImageView.pointMoveUp(callback);
    }

}
