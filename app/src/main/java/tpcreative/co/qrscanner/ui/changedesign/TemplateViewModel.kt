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
        mTemplateList.clear()
        var mModel = ChangeDesignModel()
        mTemplateList.add(TemplateModel("0",EnumShape.SQUARE,EnumIcon.ic_help,EnumChangeDesignType.NONE,mModel))

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
        mTemplateList.add(TemplateModel("1",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))

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
        mTemplateList.add(TemplateModel("2",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.VIP,mModel))

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
        mTemplateList.add(TemplateModel("3",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))


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
        mTemplateList.add(TemplateModel("4",EnumShape.CIRCLE,EnumIcon.ic_restaurant,EnumChangeDesignType.NORMAL,mModel))


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
        mTemplateList.add(TemplateModel("5",EnumShape.SQUARE,EnumIcon.ic_restaurant,EnumChangeDesignType.VIP,mModel))

//        mModel = ChangeDesignModel()
//        mTemplateList.add(TemplateModel("6",EnumShape.SQUARE,EnumIcon.ic_more,EnumChangeDesignType.MORE,mModel))
    }

    val content : Context get()  = QRScannerApplication.getInstance()
}