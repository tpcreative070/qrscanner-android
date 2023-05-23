package tpcreative.co.qrscanner.common.extension

import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.utils.MDUtil.updatePadding
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.EnumImage


fun AppCompatImageView.loadBitmap(id : String?, formatBarCode : String?){
    try {
        var mDrawable = R.drawable.ic_barcode_review
        if (Utils.isQRCode(formatBarCode)){
            mDrawable = R.drawable.ic_qrcode_review
        }
        val mFile = id?.findImageName(EnumImage.QR_CODE)
        if (mFile!=null) {
            val padding = resources.getDimensionPixelOffset(R.dimen.margin_3)
            this.updatePadding(padding,padding,padding,padding)
            val mBitmap = BitmapFactory.decodeFile(mFile.absolutePath)
            this.colorFilter = null
            this.setImageBitmap(null)
            this.setImageDrawable(null)
            this.setImageBitmap(mBitmap.toResizeBitmap(200))
        }else{
            this.setImageBitmap(null)
            this.setImageDrawable(null)
            this.setImageDrawable(mDrawable.fromDrawableIntRes)
            this.setColorFilter(ContextCompat.getColor(context, R.color.change_design_image), PorterDuff.Mode.SRC_IN)
        }
    }
    catch (e : Exception){
        e.printStackTrace()
    }

}