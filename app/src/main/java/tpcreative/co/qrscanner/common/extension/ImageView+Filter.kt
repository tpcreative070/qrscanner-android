package tpcreative.co.qrscanner.common.extension

import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.core.content.ContextCompat
import tpcreative.co.qrscanner.R

fun ImageView.tintColor(){
    this.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
}