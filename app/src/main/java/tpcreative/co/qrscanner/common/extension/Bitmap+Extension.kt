package tpcreative.co.qrscanner.common.extension

import android.R.attr
import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.toColorInt
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.EnumFont
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import tpcreative.co.qrscanner.ui.review.onPhotoPrint
import java.io.File


fun Bitmap.addPaddingTopForBitmap(paddingTop: Int,bg: String = Constant.defaultColor.hexColor): Bitmap? {
    try {
        val outputBitmap =
            Bitmap.createBitmap(width, height + paddingTop, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(bg.toColorInt())
        canvas.drawBitmap(this, 0F, paddingTop.toFloat(), null)
        return outputBitmap
    }catch (e : Exception){
        return null
    }
}

fun Bitmap.addPaddingBottomForBitmap(paddingBottom: Int,bg: String = Constant.defaultColor.hexColor): Bitmap? {
    try {
        val outputBitmap =
            Bitmap.createBitmap(width, height + paddingBottom, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(bg.toColorInt())
        canvas.drawBitmap(this, 0F, 0F, null)
        return outputBitmap
    }catch (e : Exception){
        return null
    }
}


fun Bitmap.addPaddingRightForBitmap(paddingRight: Int,bg: String = Constant.defaultColor.hexColor): Bitmap? {
    return try {
        val outputBitmap =
            Bitmap.createBitmap(width + paddingRight, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(bg.toColorInt())
        canvas.drawBitmap(this, 0F, 0F, null)
        outputBitmap
    }catch (e : Exception){
        null
    }
}

fun Bitmap.addPaddingLeftForBitmap(paddingLeft: Int,bg: String = Constant.defaultColor.hexColor): Bitmap? {
    return try {
        val outputBitmap =
            Bitmap.createBitmap(width + paddingLeft, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(bg.toColorInt())
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
            this@storeBitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            close()
        }
    }
    return mUri
}

fun Bitmap.storeBitmap(fileName : String,enumImage : EnumImage) : Uri?{
    val imageFolder = QRScannerApplication.getInstance().getPathFolder()?.let { File(it) }
    imageFolder?.mkdirs()
    var file = File(imageFolder, "$fileName shared_design_qr_code.png")
    if (enumImage == EnumImage.LOGO){
        file = File(imageFolder, "$fileName shared_design_logo_code.png")
    }
    if (enumImage == EnumImage.QR_TEMPLATE){
        file = File(imageFolder, "$fileName shared_design_template_code.png")
    }
    val mUri = QRScannerApplication.getInstance().getUriForFile(file)
    mUri?.run {
        QRScannerApplication.getInstance().contentResolver?.openOutputStream(this)?.run {
            this@storeBitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            close()
        }
    }
    return mUri
}

fun Bitmap.toCircular(context: Context, newCornerRadius: Float, isCircle :Boolean): RoundedBitmapDrawable {
    return RoundedBitmapDrawableFactory.create(context.resources, this).apply {
        isCircular = isCircle
        val roundPx = this@toCircular.width  * newCornerRadius
        cornerRadius = roundPx
    }
}

suspend fun Bitmap.onDrawOnBitmap(text : String, enumImage: EnumImage,fontName : String,fontSize : Int = 70,color : String,backgroundColor : String,callback :((Bitmap?)->Unit)) = withContext(
    Dispatchers.IO){
    var mBm: Bitmap?
    this@onDrawOnBitmap.let {data ->
        if (enumImage == EnumImage.QR_TEXT_BOTTOM){
            //mBm = data.addPaddingLeftForBitmap(50)
            //mBm = mBm?.addPaddingRightForBitmap(50)
            mBm = data.addPaddingBottomForBitmap(150, backgroundColor)
        }else{
            //mBm = data.addPaddingLeftForBitmap(50)
            mBm = data.addPaddingTopForBitmap(150,backgroundColor)
            //mBm = mBm?.addPaddingRightForBitmap(50)
        }
        mBm?.let {
            val canvas = Canvas(it)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
            paint.isLinearText = true
            paint.textAlign = Paint.Align.CENTER
            val mEumFont = EnumFont.valueOf(fontName)
            paint.typeface = mEumFont.font.typeface
            //paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.color = color.toColorInt()
            paint.textSize = fontSize.toFloat() // Text Size
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern
            val mRectF = RectF(0F, 0F, it.width.toFloat(),it.height.toFloat())
            if (enumImage == EnumImage.QR_TEXT_BOTTOM){
                canvas.drawText(text, (canvas.width /2).toFloat(), mRectF.bottom - 90 , paint)
            }else{
                canvas.drawText(text, (canvas.width /2).toFloat(), mRectF.top + 180 , paint)
            }
            callback.invoke(mBm)
        }
    }
}
