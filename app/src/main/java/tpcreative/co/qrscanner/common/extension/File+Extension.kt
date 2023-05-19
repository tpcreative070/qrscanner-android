package tpcreative.co.qrscanner.common.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

val File.toBitmap : Bitmap
    get() = BitmapFactory.decodeFile(this.absolutePath)