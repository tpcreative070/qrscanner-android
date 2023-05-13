package tpcreative.co.qrscanner.common.extension

import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumFont
import tpcreative.co.qrscanner.common.EnumIcon

val EnumIcon.icon : Int
    get() = EnumIcon.fromValue(this)

val EnumFont.font : Int
    get() = EnumFont.fromValue(this)