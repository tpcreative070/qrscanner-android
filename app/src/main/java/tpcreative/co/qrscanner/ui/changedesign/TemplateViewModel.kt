package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class TemplateViewModel(val viewModel: ChangeDesignViewModel)  : BaseViewModel<ItemNavigation>() {
    private val TAG = this::class.simpleName
    /*Template*/
    var mTemplateList  = mutableListOf<TemplateModel>()


    fun getIntent(activity: Activity?, callback: (result: Boolean) -> Unit)  {
        val bundle: Bundle? = activity?.intent?.extras
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND){
            Utils.Log(TAG,"type $bundle")
            val data  = activity?.intent?.serializable(
                QRScannerApplication.getInstance().getString(
                    R.string.key_data), GeneralModel::class.java)
        }
        initializedTemplateDate()
        viewModel.uuId = ""
        viewModel.dataCode = ""
        callback.invoke(true)
    }

    private fun initializedTemplateDate(){
        viewModel.initializedTemplateData()
        var count = 0
        mTemplateList.clear()
        var mModel = ChangeDesignModel()
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_help,EnumChangeDesignType.NONE,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.getOrNull(2)
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(1)
        var mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.colorAccent.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.ColorBlueV1.stringHex
        var mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.getOrNull(8)
        mModel.body = viewModel.mBodyList.getOrNull(2)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.md_indigo_700.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.ColorBlueV1.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.ColorBlueV1.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.VIP,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.getOrNull(11)
        mModel.body = viewModel.mBodyList.getOrNull(2)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryColorIndigo.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.black.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryColorIndigo.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1


        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.getOrNull(15)
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(1)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryColorIndigo.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.colorAccent.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.black.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryColorIndigo.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.CIRCLE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.getOrNull(4)
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(4)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.twitter.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.twitter.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.twitter.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.twitter.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.VIP,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_dog }
        mModel.body = viewModel.mBodyList.firstOrNull { it.enumBody == EnumBody.STAR }
        mModel.positionMarker = viewModel.mPositionMarkerList.firstOrNull { it.enumPositionMarker == EnumPositionMarker.CORNER_25PX}
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorBrown.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.transparent.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorBrown.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorBrown.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.CIRCLE,EnumIcon.ic_restaurant,EnumChangeDesignType.VIP,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_restaurant }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.firstOrNull { it.enumPositionMarker == EnumPositionMarker.DEFAULT}
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.inbox_primary_dark.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.colorAccent.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.inbox_primary_dark.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.inbox_primary_dark.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_wifi }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker =viewModel.mPositionMarkerList.firstOrNull { it.enumPositionMarker == EnumPositionMarker.DEFAULT}
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.colorPrimaryDark.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorBrown.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.colorPrimaryDark.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.colorPrimaryDark.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_heart }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorPink.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.colorRed.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorPink.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorPink.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_message }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorPurple.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.colorAccent.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorPurple.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorPurple.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_location }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorDeepPurple.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.secondaryColorLightGreen.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorDeepPurple.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorDeepPurple.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1


        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_contact }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorCyan.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorPink.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorCyan.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorCyan.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1


        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_calender }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorTeal.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorLightGreen.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorTeal.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorTeal.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1


        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_phone }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorLightGreen.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorAmber.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorLightGreen.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorLightGreen.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1


        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_text }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorLimeStrong.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorBrown.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorLimeStrong.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorLimeStrong.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.ORIGINAL,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_birthday }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorAmberStrong.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.primaryColorPink.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorAmberStrong.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorAmberStrong.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

        mModel = ChangeDesignModel()
        mModel.logo = viewModel.mLogoList.firstOrNull { it.enumIcon == EnumIcon.ic_network }
        mModel.body = viewModel.mBodyList.getOrNull(1)
        mModel.positionMarker = viewModel.mPositionMarkerList.getOrNull(2)
        mMapColor = viewModel.defaultColorMap()
        mMapColor[EnumImage.QR_BACKGROUND] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FOREGROUND] = R.color.primaryDarkColorBrown.stringHex
        mMapColor[EnumImage.QR_BACKGROUND_ICON] = R.color.colorPrimary.stringHex
        mMapColor[EnumImage.QR_FOREGROUND_ICON] = R.color.white.stringHex
        mMapColor[EnumImage.QR_FRAME] = R.color.primaryDarkColorBrown.stringHex
        mMapColor[EnumImage.QR_BALL] = R.color.primaryDarkColorBrown.stringHex
        mColor = viewModel.defaultColor()
        mModel.color = mColor
        mModel.color?.mapColor = mMapColor
        mTemplateList.add(TemplateModel("$count",EnumShape.CIRCLE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))
        count+=1

//        mModel = ChangeDesignModel()
//        mTemplateList.add(TemplateModel("6",EnumShape.SQUARE,EnumIcon.ic_more,EnumChangeDesignType.MORE,mModel))
    }

    val content : Context get()  = QRScannerApplication.getInstance()
}