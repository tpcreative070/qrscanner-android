/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tpcreative.co.qrscanner.common.view.crop

import android.graphics.Bitmap
import android.graphics.Matrix

/*
 * Modified from original in AOSP.
 */
internal class RotateBitmap(private var bitmap: Bitmap?, rotation: Int) {
    private var rotation: Int
    fun setRotation(rotation: Int) {
        this.rotation = rotation
    }

    fun getRotation(): Int {
        return rotation
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    fun getRotateMatrix(): Matrix? {
        // By default this is an identity matrix
        val matrix = Matrix()
        if (bitmap != null && rotation != 0) {
            // We want to do the rotation at origin, but since the bounding
            // rectangle will be changed after rotation, so the delta values
            // are based on old & new width/height respectively.
            val cx = bitmap.getWidth() / 2
            val cy = bitmap.getHeight() / 2
            matrix.preTranslate(-cx.toFloat(), -cy.toFloat())
            matrix.postRotate(rotation.toFloat())
            matrix.postTranslate((getWidth() / 2).toFloat(), (getHeight() / 2).toFloat())
        }
        return matrix
    }

    fun isOrientationChanged(): Boolean {
        return rotation / 90 % 2 != 0
    }

    fun getHeight(): Int {
        if (bitmap == null) return 0
        return if (isOrientationChanged()) {
            bitmap.getWidth()
        } else {
            bitmap.getHeight()
        }
    }

    fun getWidth(): Int {
        if (bitmap == null) return 0
        return if (isOrientationChanged()) {
            bitmap.getHeight()
        } else {
            bitmap.getWidth()
        }
    }

    fun recycle() {
        if (bitmap != null) {
            bitmap.recycle()
            bitmap = null
        }
    }

    init {
        this.rotation = rotation % 360
    }
}