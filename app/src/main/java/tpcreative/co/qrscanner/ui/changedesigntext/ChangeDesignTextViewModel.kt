package tpcreative.co.qrscanner.ui.changedesigntext

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ChangeDesignTextViewModel(val viewModel: ChangeDesignViewModel)  : BaseViewModel<ItemNavigation>() {

    val TAG = this::class.java.simpleName
    var mapColor : HashMap<EnumImage,String> = hashMapOf()
    var mapText : HashMap<EnumImage, TextModel> = hashMapOf()
    lateinit var enumImage: EnumImage
    private lateinit var dataCode : String
    private lateinit var uuId : String
    fun getIntent(activity: Activity?, callback: (result: Drawable) -> Unit) {
        val bundle: Bundle? = activity?.intent?.extras
        enumImage = EnumImage.valueOf(bundle?.getString(ConstantKey.KEY_POPUP_TEXT_TEXT_TYPE) ?: EnumImage.QR_TEXT_BOTTOM.name)
        mapColor = bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_MAP) ?: hashMapOf()
        mapText = bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_TEXT) ?: hashMapOf()
        dataCode = bundle?.getString(ConstantKey.KEY_DATA_CODE) ?: ""
        uuId = bundle?.getString(ConstantKey.KEY_DATA_UUID) ?: ""
        val mChangeDesignData = activity?.intent?.serializable(
            ConstantKey.KEY_CHANGE_DESIGN_TEXT,
            ChangeDesignModel::class.java
        )
        Utils.Log(TAG,"Map color ${mapColor.toJson()}")
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
            viewModel.indexPositionMarker = mChangeDesignData.positionMarker ?: viewModel.defaultPositionMarker()
            viewModel.indexText = mChangeDesignData.text ?: viewModel.defaultText()
            viewModel.dataCode = dataCode
            viewModel.uuId = uuId
            Utils.Log(TAG,"Data ${mChangeDesignData.toJson()}")
            Utils.Log(TAG,"Shape ${mShape.name}")
            viewModel.onGenerateQR {
                callback.invoke(it)
            }
        }
    }

    fun onDrawable(callback: (result: Drawable) -> Unit){
        viewModel.onGenerateQR() {
            callback.invoke(it)
        }
    }
}