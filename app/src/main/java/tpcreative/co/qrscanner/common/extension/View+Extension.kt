package tpcreative.co.qrscanner.common.extension

import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.View

val View.screenLocation get(): IntArray {
    val point = IntArray(2)
    getLocationOnScreen(point)
    return point
}

fun View.screenLocationSafe(callback: (Int, Int) -> Unit) {
    post {
        val (x, y) = screenLocation
        callback(x, y)
    }
}

fun View.rotateBitmap(context: Context, angle: Int?, color :Int): Bitmap? {
    val keyguardManager: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val orientation = context.resources.configuration.orientation
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        val matrix = Matrix()
        val mPaint = Paint()
        mPaint.color = color
        mPaint.style = Paint.Style.FILL
        matrix.postRotate(angle?.toFloat() ?:0F)
        val mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap);
        canvas.drawBitmap(mBitmap, matrix,mPaint)
        return mBitmap
    } else {
        val mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap);
        canvas.drawColor(color)
        return mBitmap
    }
}
