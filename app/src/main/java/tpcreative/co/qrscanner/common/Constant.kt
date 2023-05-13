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

        const val defaultColor = R.color.white

        val mFontList = mutableMapOf<String,Int>().apply {
            put(EnumFont.brandon_bold.name,R.font.brandon_bold)
            put(EnumFont.brandon_regular.name,R.font.brandon_regular)
            put(EnumFont.roboto_bold.name,R.font.roboto_bold)
            put(EnumFont.roboto_light.name,R.font.roboto_light)
            put(EnumFont.roboto_medium.name,R.font.roboto_medium)
            put(EnumFont.roboto_regular.name,R.font.roboto_regular)
        }

        val  mList = mutableMapOf<String,Int>().apply {
            put(EnumIcon.ic_youtube_png.name,R.drawable.ic_youtube_png)
            put(EnumIcon.ic_twitter.name,R.drawable.ic_twitter)
            put(EnumIcon.ic_template.name, R.drawable.ic_template)
            put(EnumIcon.ic_paint.name,R.drawable.ic_paint)
            put(EnumIcon.ic_dots.name,R.drawable.ic_dots)
            put(EnumIcon.ic_eyes.name, R.drawable.ic_eyes)
            put(EnumIcon.ic_registered.name,R.drawable.ic_registered)
            put(EnumIcon.ic_design_text.name,R.drawable.ic_design_text)
            put(EnumIcon.ic_qr_background.name,R.drawable.ic_qr_background)
            put(EnumIcon.bg_white.name, R.drawable.bg_white)
            put(EnumIcon.ic_wifi.name,R.drawable.ic_wifi)
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
            put(EnumIcon.ic_more.name,R.drawable.ic_more)
            put(EnumIcon.ic_help.name,R.drawable.ic_help)
            put(EnumIcon.ic_frame_ball_default.name,R.drawable.ic_frame_ball_default)
            put(EnumIcon.ic_frame_ball_corner_10px.name,R.drawable.ic_frame_ball_corner_10px)
            put(EnumIcon.ic_frame_ball_corner_25px.name,R.drawable.ic_frame_ball_corner_25px)
            put(EnumIcon.ic_frame_ball_corner_top_right_bottom_left_25px.name,R.drawable.ic_frame_ball_corner_top_right_bottom_left_25px)
            put(EnumIcon.ic_frame_ball_corner_top_left_bottom_right_25px.name,R.drawable.ic_frame_ball_corner_top_left_bottom_right_25px)
            put(EnumIcon.ic_frame_ball_corner_top_left_top_right_bottom_left_25px.name,R.drawable.ic_frame_ball_corner_top_left_top_right_bottom_left_25px)
            put(EnumIcon.ic_frame_ball_circle.name,R.drawable.ic_frame_ball_circle)
            put(EnumIcon.ic_dark_default.name,R.drawable.ic_dark_default)
            put(EnumIcon.ic_dark_corner_0_5.name,R.drawable.ic_dark_corner_0_5px)
            put(EnumIcon.ic_dark_circle.name,R.drawable.ic_dark_circle)
            put(EnumIcon.ic_dark_star.name,R.drawable.ic_dark_star)
            put(EnumIcon.ic_facebook.name,R.drawable.ic_facebook)
            put(EnumIcon.ic_facebook_messenger.name,R.drawable.ic_facebook_messenger)
            put(EnumIcon.ic_tiktok.name,R.drawable.ic_tiktok)
            put(EnumIcon.ic_line.name,R.drawable.ic_line)
            put(EnumIcon.ic_linkedin.name,R.drawable.ic_linkedin)
            put(EnumIcon.ic_skype.name,R.drawable.ic_skype)
            put(EnumIcon.ic_restaurant.name,R.drawable.ic_restaurant)
            put(EnumIcon.ic_heart.name,R.drawable.ic_heart)
            put(EnumIcon.ic_card.name,R.drawable.ic_card)
            put(EnumIcon.ic_gift.name,R.drawable.ic_gift)
            put(EnumIcon.ic_viber.name,R.drawable.ic_viber)
            put(EnumIcon.ic_birthday.name,R.drawable.ic_birthday)
            put(EnumIcon.ic_visa.name,R.drawable.ic_visa)
            put(EnumIcon.ic_master_card.name,R.drawable.ic_master_card)
            put(EnumIcon.ic_snapchat.name,R.drawable.ic_snapchat)
            put(EnumIcon.ic_we_chat.name,R.drawable.ic_we_chat)
        }
    }
}

enum class EnumIcon {
    ic_youtube_png, ic_twitter,ic_template,ic_paint,ic_dots,ic_eyes,ic_registered,ic_design_text,bg_white,ic_wifi,ic_whatapp,ic_instagram,ic_paypal,ic_email,ic_more,ic_help,
    ic_message,ic_location,ic_calender,ic_contact,ic_phone,ic_text,ic_network,ic_gallery,ic_dark_corner_0_5,ic_frame_ball_corner_top_right_bottom_left_25px,ic_frame_ball_default,ic_dark_default,ic_frame_ball_corner_10px,ic_frame_ball_corner_25px,
    ic_frame_ball_corner_top_left_bottom_right_25px,ic_facebook,ic_facebook_messenger,ic_tiktok,ic_line,ic_linkedin,ic_skype,ic_restaurant,ic_heart,ic_card,ic_gift,ic_viber,ic_birthday,ic_visa,ic_master_card,ic_snapchat,ic_we_chat,
    ic_frame_ball_corner_top_left_top_right_bottom_left_25px,ic_qr_background,
    ic_frame_ball_circle,
    ic_dark_circle,
    ic_dark_star;
    companion object {
        fun fromValue(enumIcon: EnumIcon): Int {
            return Constant.mList[enumIcon.name] ?: R.drawable.icon
        }
    }
}

enum class EnumFont {
    brandon_bold,brandon_regular,roboto_bold,roboto_light,roboto_medium,roboto_regular;
    companion object {
        fun fromValue(enumFont: EnumFont): Int {
            return Constant.mFontList[enumFont.name] ?: R.font.roboto_regular
        }
    }
}