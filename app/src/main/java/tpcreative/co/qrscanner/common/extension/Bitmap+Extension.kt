package tpcreative.co.qrscanner.common.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import tpcreative.co.qrscanner.ui.review.ReviewActivity

fun Bitmap.addPaddingTopForBitmap(paddingTop: Int): Bitmap? {
    val outputBitmap =
        Bitmap.createBitmap(width, height + paddingTop, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    canvas.drawColor(Color.TRANSPARENT)
    canvas.drawBitmap(this, 0F, paddingTop.toFloat(), null)
    return outputBitmap
}

fun Bitmap.addPaddingBottomForBitmap(paddingBottom: Int): Bitmap? {
    val outputBitmap =
        Bitmap.createBitmap(width, height + paddingBottom, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    canvas.drawColor(Color.TRANSPARENT)
    canvas.drawBitmap(this, 0F, 0F, null)
    return outputBitmap
}


fun Bitmap.addPaddingRightForBitmap(paddingRight: Int): Bitmap? {
    val outputBitmap =
        Bitmap.createBitmap(width + paddingRight, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    canvas.drawColor(Color.TRANSPARENT)
    canvas.drawBitmap(this, 0F, 0F, null)
    return outputBitmap
}

fun Bitmap.addPaddingLeftForBitmap( paddingLeft: Int): Bitmap? {
    val outputBitmap =
        Bitmap.createBitmap(width + paddingLeft, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    canvas.drawColor(Color.TRANSPARENT)
    canvas.drawBitmap(this, paddingLeft.toFloat(), 0F, null)
    return outputBitmap
}
