package tpcreative.co.qrscanner.model

import android.graphics.drawable.Drawable

class FormatTypeModel(var id: String?, var name: String?,var res: Drawable?) {
    override fun toString(): String {
        return name ?:""
    }
}