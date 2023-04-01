package tpcreative.co.qrscanner.common.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import java.io.File

fun Bitmap.addPaddingTopForBitmap(paddingTop: Int): Bitmap? {
    try {
        val outputBitmap =
            Bitmap.createBitmap(width, height + paddingTop, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(this, 0F, paddingTop.toFloat(), null)
        return outputBitmap
    }catch (e : Exception){
        return null
    }
}

fun Bitmap.addPaddingBottomForBitmap(paddingBottom: Int): Bitmap? {
    try {
        val outputBitmap =
            Bitmap.createBitmap(width, height + paddingBottom, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(this, 0F, 0F, null)
        return outputBitmap
    }catch (e : Exception){
        return null
    }
}


fun Bitmap.addPaddingRightForBitmap(paddingRight: Int): Bitmap? {
    return try {
        val outputBitmap =
            Bitmap.createBitmap(width + paddingRight, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(this, 0F, 0F, null)
        outputBitmap
    }catch (e : Exception){
        null
    }
}

fun Bitmap.addPaddingLeftForBitmap(paddingLeft: Int): Bitmap? {
    return try {
        val outputBitmap =
            Bitmap.createBitmap(width + paddingLeft, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(this, paddingLeft.toFloat(), 0F, null)
        outputBitmap
    }catch (e : Exception){
        null
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.storeBitmap() : Uri?{
    val imageFolder = File(QRScannerApplication.getInstance().cacheDir, Constant.images_folder)
    imageFolder.mkdirs()
    val file = File(imageFolder, "shared_design_qr_code.png")
    val mUri = QRScannerApplication.getInstance().getUriForFile(file)
    mUri?.run {
        QRScannerApplication.getInstance().contentResolver?.openOutputStream(this)?.run {
            this@storeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            close()
        }
    }
    return mUri
}
