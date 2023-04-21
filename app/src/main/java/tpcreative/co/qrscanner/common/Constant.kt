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

            put(EnumIcon.bg_white.name, R.drawable.bg_white)
            put(EnumIcon.design_wifi.name,R.drawable.design_wifi)
            put(EnumIcon.ic_whatapp.name,R.drawable.ic_whatapp)

            put(EnumIcon.ic_instagram.name, R.drawable.ic_instagram)
            put(EnumIcon.ic_paypal.name,R.drawable.ic_paypal)
            put(EnumIcon.ic_email.name,R.drawable.ic_email)


            put(EnumIcon.ic_message.name, R.drawable.ic_message)
            put(EnumIcon.ic_location.name,R.drawable.ic_location)
            put(EnumIcon.ic_calender.name,R.drawable.ic_calender)

            put(EnumIcon.ic_contact.name, R.drawable.ic_contact)
            put(EnumIcon.ic_phone.name,R.drawable.ic_phone)
            put(EnumIcon.ic_text.name,R.drawable.ic_text)
            put(EnumIcon.ic_network.name,R.drawable.ic_network)
            put(EnumIcon.ic_gallery.name,R.drawable.ic_gallery)
        }
    }
}

enum class EnumIcon {
    ic_youtube_png, ic_twitter,ic_template,ic_paint,ic_dots,ic_eyes,ic_registered,ic_design_text,bg_white,design_wifi,ic_whatapp,ic_instagram,ic_paypal,ic_email,
    ic_message,ic_location,ic_calender,ic_contact,ic_phone,ic_text,ic_network,ic_gallery;
    companion object {
        fun fromValue(enumIcon: EnumIcon): Int {
            return Constant.mList[enumIcon.name] ?: R.drawable.icon
        }
    }
}