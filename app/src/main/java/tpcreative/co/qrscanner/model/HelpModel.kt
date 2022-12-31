package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.NonNull
import org.jetbrains.annotations.NotNull

class HelpModel(@DrawableRes var icon: Int,var color : Int, var type: EnumAction, title: String) : java.io.Serializable{
    var title : String? = title

}