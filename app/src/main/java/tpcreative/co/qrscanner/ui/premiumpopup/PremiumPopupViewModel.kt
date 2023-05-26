package tpcreative.co.qrscanner.ui.premiumpopup

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.onDrawOnBitmap
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class PremiumPopupViewModel(val viewModel: ChangeDesignViewModel) : BaseViewModel<ItemNavigation>(){
    private val TAG = this::class.java.simpleName
    private lateinit var dataCode : String
    private lateinit var uuId : String
    private lateinit var enumFontSize : EnumFontSize
    var enumView : EnumView = EnumView.LOGO
    var index : Int = 0
    var font : FontModel = FontModel()
    fun getIntent(activity: AppCompatActivity?, callback: (result: Bitmap?) -> Unit){
        val bundle: Bundle? = activity?.intent?.extras
        val mChangeDesignData :ChangeDesignModel? = bundle?.serializable(ConstantKey.KEY_PREMIUM_POPUP)
        val mShape: EnumShape = EnumShape.valueOf(bundle?.getString(ConstantKey.KEY_PREMIUM_POPUP_TYPE_SHAPE) ?: EnumShape.SQUARE.name)
        enumFontSize  = EnumFontSize.valueOf(bundle?.getString(ConstantKey.KEY_PREMIUM_POPUP_ENUM_FONT_SIZE) ?: EnumFontSize.NONE.name)
        enumView = EnumView.valueOf(bundle?.getString(ConstantKey.KEY_CHANGE_DESIGN_CURRENT_VIEW) ?: EnumView.LOGO.name)
        index = bundle?.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX) ?: 0
        font  = bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_OBJECT) ?: FontModel()

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

    fun getText() : String? {
        if (enumFontSize == EnumFontSize.FREEDOM_INCREASE){
            return R.string.freedom_increase_font_size.toText()
        }else if (enumFontSize == EnumFontSize.FREEDOM_DECREASE){
            return R.string.freedom_decrease_font_size.toText()
        }
        return null
    }
}