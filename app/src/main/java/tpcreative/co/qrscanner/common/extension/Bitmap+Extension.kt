package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.EnumFont
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.model.TextModel
import vadiole.colorpicker.hexColor
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

suspend fun Bitmap.onDrawOnBitmap(map : HashMap<EnumImage,TextModel>,callback :((Bitmap?)->Unit)) = withContext(
    Dispatchers.IO){
    var mBm: Bitmap? = null
    this@onDrawOnBitmap.let {data ->
        if(map.size>1){
            val mTop = map[EnumImage.QR_TEXT_TOP]?.data
            val mBottom = map[EnumImage.QR_TEXT_BOTTOM]?.data
            mBm = data.addPaddingTopForBitmap(150,mTop?.currentBackgroundColor ?: Constant.defaultColor.hexColor)
            mBm = mBm?.addPaddingBottomForBitmap(150,mBottom?.currentBackgroundColor ?: Constant.defaultColor.hexColor)
        }else{
            map.forEach {
                val mData = it.value.data
                mBm = if (it.key == EnumImage.QR_TEXT_BOTTOM){
                    //mBm = data.addPaddingLeftForBitmap(50)
                    //mBm = mBm?.addPaddingRightForBitmap(50)
                    data.addPaddingBottomForBitmap(150, mData.currentBackgroundColor)
                }else{
                    //mBm = data.addPaddingLeftForBitmap(50)
                    data.addPaddingTopForBitmap(150,mData.currentBackgroundColor)
                    //mBm = mBm?.addPaddingRightForBitmap(50)
                }
            }
        }
        mBm?.let {
            val canvas = Canvas(it)
            val paintBottom = Paint()
            val paintTop = Paint()
            val mRectF = RectF(0F, 0F, it.width.toFloat(),it.height.toFloat())
            map.forEach {
                val mData = it.value.data
                if (it.key == EnumImage.QR_TEXT_BOTTOM){
                    paintBottom.style = Paint.Style.FILL
                    paintBottom.isAntiAlias = true
                    paintBottom.isLinearText = true
                    paintBottom.textAlign = Paint.Align.CENTER
                    val mEumFont = EnumFont.valueOf(mData.currentFont)
                    paintBottom.typeface = mEumFont.font.typeface
                    //paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                    paintBottom.color = mData.currentColor.toColorInt()
                    paintBottom.textSize = mData.currentFontSize.toFloat() // Text Size
                    paintBottom.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern
                    canvas.drawText(mData.currentText, (canvas.width /2).toFloat(), mRectF.bottom - 90 , paintBottom)
                }else{
                    paintTop.style = Paint.Style.FILL
                    paintTop.isAntiAlias = true
                    paintTop.isLinearText = true
                    paintTop.textAlign = Paint.Align.CENTER
                    val mEumFont = EnumFont.valueOf(mData.currentFont)
                    paintTop.typeface = mEumFont.font.typeface
                    //paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                    paintTop.color = mData.currentColor.toColorInt()
                    paintTop.textSize = mData.currentFontSize.toFloat() // Text Size
                    paintTop.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern
                    canvas.drawText(mData.currentText, (canvas.width /2).toFloat(), mRectF.top + 180 , paintTop)
                }
            }
            callback.invoke(mBm)
        }
    }
}
