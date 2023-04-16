package tpcreative.co.qrscanner.common

import tpcreative.co.qrscanner.R

class Constant {
    companion object {
        const val textType = "text/plain"
        const val cvfType = "text/x-vcard"
        const val email = "Email"
        const val addressBook = "Address book"
        const val barCode = "Barcode"
        const val webSite = "Website"
        const val wifi = "Wifi"
        const val location = "Location"
        const val phoneNumber = "Phone number"
        const val sms = "Message"
        const val calendar = "Calendar"
        const val text = "Text"
        const val CAMERA_FACING_BACK = 0
        const val CAMERA_FACING_FRONT = 1
        const val QRCodeExportWidth = 800
        const val QRCodeExportHeight = 800
        const val QRCodeViewWidth = 250
        const val QRCodeViewHeight = 250
        const val images_folder = "images"
        const val countLimitHistorySave = 3
        const val files_folder = "files"
        const val youtube_id = "eNhq6s1kmhw"
        const val LOG_TAKE_TIME = "LOG_TAKE_TIME"

        val  mList = mutableMapOf<String,Int>().apply {
            put(EnumIcon.ic_youtube_png.name,R.drawable.ic_youtube_png)
            put(EnumIcon.ic_twitter.name,R.drawable.ic_twitter)
            put(EnumIcon.ic_template.name, R.drawable.ic_template)
            put(EnumIcon.ic_paint.name,R.drawable.ic_paint)
            put(EnumIcon.ic_dots.name,R.drawable.ic_dots)
            put(EnumIcon.ic_eyes.name, R.drawable.ic_eyes)
            put(EnumIcon.ic_registered.name,R.drawable.ic_registered)
            put(EnumIcon.ic_design_text.name,R.drawable.ic_design_text)
        }
    }
}

enum class EnumIcon {
    ic_youtube_png, ic_twitter,ic_template,ic_paint,ic_dots,ic_eyes,ic_registered,ic_design_text;
    companion object {
        fun fromValue(enumIcon: EnumIcon): Int {
            return Constant.mList[enumIcon.name] ?: R.drawable.icon
        }
    }
}