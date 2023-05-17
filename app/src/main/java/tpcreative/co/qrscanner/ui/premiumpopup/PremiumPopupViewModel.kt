package tpcreative.co.qrscanner.ui.premiumpopup

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.onDrawOnBitmap
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class PremiumPopupViewModel(val viewModel: ChangeDesignViewModel) : BaseViewModel<ItemNavigation>(){
    private val TAG = this::class.java.simpleName
    private lateinit var dataCode : String
    private lateinit var uuId : String
    fun getIntent(activity: AppCompatActivity?, callback: (result: Bitmap?) -> Unit){
        val bundle: Bundle? = activity?.intent?.extras
        val mChangeDesignData :ChangeDesignModel? = bundle?.serializable(ConstantKey.KEY_PREMIUM_POPUP)
        val mShape: EnumShape = EnumShape.valueOf(bundle?.getString(ConstantKey.KEY_PREMIUM_POPUP_TYPE_SHAPE) ?: EnumShape.SQUARE.name)
        dataCode = bundle?.getString(ConstantKey.KEY_DATA_CODE) ?: ""
        uuId = bundle?.getString(ConstantKey.KEY_DATA_UUID) ?: ""
        if (mChangeDesignData != null) {
            viewModel.changeDesignReview = mChangeDesignData
            viewModel.changeDesignSave = mChangeDesignData
            viewModel.changeDesignOriginal = mChangeDesignData
            viewModel.shape = mShape
            viewModel.indexColor = mChangeDesignData.color ?: viewModel.defaultColor()
            viewModel.indexLogo = mChangeDesignData.logo ?: viewModel.defaultLogo()
            viewModel.indexBody = mChangeDesignData.body ?: viewModel.defaultBody()
            viewModel.indexPositionMarker = mChangeDesignData.positionMarker ?: viewModel.indexPositionMarker
            viewModel.indexText = mChangeDesignData.text ?: viewModel.defaultText()
            viewModel.dataCode = dataCode
            viewModel.uuId = uuId
            Utils.Log(TAG,"Data ${mChangeDesignData.toJson()}")
            Utils.Log(TAG,"Shape ${mShape.name}")
            Utils.Log(TAG,"Index text ${viewModel.indexText.toJson()}")
            viewModel.onGenerateQR {
                val mBitmap = it.toBitmap(1024,1024)
                mBitmap.onDrawOnBitmap(viewModel.indexText){
                    callback.invoke(it)
                }
            }
        }
    }

    fun isBitMap() : Boolean{
        return viewModel.indexLogo.typeIcon == EnumTypeIcon.BITMAP
    }
}