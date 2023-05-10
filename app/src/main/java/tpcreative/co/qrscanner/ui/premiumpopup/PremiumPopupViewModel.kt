package tpcreative.co.qrscanner.ui.premiumpopup

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class PremiumPopupViewModel(val viewModel: ChangeDesignViewModel) : BaseViewModel<ItemNavigation>(){
    private val TAG = this::class.java.simpleName
    fun getIntent(activity: Activity?, callback: (result: Drawable) -> Unit) {
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND) {
            val mChangeDesignData = activity?.intent?.serializable(
                ConstantKey.KEY_PREMIUM_POPUP,
                ChangeDesignModel::class.java
            )
            val mShape: EnumShape = EnumShape.valueOf(
                activity?.intent?.serializable(
                    ConstantKey.KEY_PREMIUM_POPUP_TYPE_SHAPE,
                    String::class.java
                ) ?: EnumShape.SQUARE.name
            )
            if (mChangeDesignData != null) {
                viewModel.changeDesignReview = mChangeDesignData
                viewModel.changeDesignSave = mChangeDesignData
                viewModel.changeDesignOriginal = mChangeDesignData
                viewModel.shape = mShape
                viewModel.indexColor = mChangeDesignData.color ?: viewModel.defaultColor()
                viewModel.indexLogo = mChangeDesignData.logo ?: viewModel.defaultLogo()
                viewModel.indexBody = mChangeDesignData.body ?: viewModel.defaultBody()
                viewModel.indexPositionMarker = mChangeDesignData.positionMarker ?: viewModel.indexPositionMarker
                Utils.Log(TAG,"Data ${mChangeDesignData.toJson()}")
                Utils.Log(TAG,"Shape ${mShape.name}")
                viewModel.onGenerateQR {
                    callback.invoke(it)
                }
            }
        }
    }

    fun isBitMap() : Boolean{
        return viewModel.indexLogo.typeIcon == EnumTypeIcon.BITMAP
    }
}