package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ChangeDesignViewModel  : BaseViewModel<ItemNavigation>(){
    val TAG = this::class.java.name
    var mList: MutableList<ChangeDesignCategoryModel> = mutableListOf()
    var create: GeneralModel = GeneralModel()
    var enumView  = EnumView.ALL_HIDDEN
    var logoSelectedIndex : Int = -1
    var mLogoList = mutableListOf<LogoModel>()
    var changeDesignReview :ChangeDesignModel = ChangeDesignModel()
    var changeDesignSave :ChangeDesignModel = ChangeDesignModel()
    fun getIntent(activity: Activity?, callback: (result: Boolean) -> Unit)  {
        val bundle: Bundle? = activity?.intent?.extras
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND){
            Utils.Log(TAG,"type $bundle")
            val data  = activity?.intent?.serializable(
                QRScannerApplication.getInstance().getString(
                    R.string.key_data), GeneralModel::class.java)
            if (data != null) {
                create = data
                callback.invoke(true)
            }
        }
        initializedLogoData()
    }

    fun getData(callback: (result: MutableList<ChangeDesignCategoryModel>) -> Unit){
        mList.clear()
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_template,QRScannerApplication.getInstance().getString(R.string.template),EnumView.TEMPLATE,false,R.color.transparent,true)
        )
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_paint,QRScannerApplication.getInstance().getString(R.string.color),EnumView.COLOR,false,R.color.transparent,true))
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_dots,QRScannerApplication.getInstance().getString(R.string.dots),EnumView.DOTS,false,R.color.transparent,true)
        )
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_eyes,QRScannerApplication.getInstance().getString(R.string.eyes),EnumView.EYES,false,R.color.transparent,true)
        )
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_registered,QRScannerApplication.getInstance().getString(R.string.logo),EnumView.LOGO,false,R.color.transparent,true)
        )
        mList.add(
            ChangeDesignCategoryModel(R.drawable.ic_design_text,QRScannerApplication.getInstance().getString(R.string.text),EnumView.TEXT,false,R.color.transparent,true)
        )
        callback(mList)
    }

    private fun initializedLogoData(){
        mLogoList.clear()
        mLogoList.add(
            LogoModel(
                R.drawable.bg_white,
                QRScannerApplication.getInstance().getString(R.string.template),
                EnumView.TEMPLATE,false,R.color.transparent,false)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.design_wifi,
                QRScannerApplication.getInstance().getString(R.string.template),
                EnumView.TEMPLATE,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_twitter,
                QRScannerApplication.getInstance().getString(R.string.color),
                EnumView.COLOR,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_youtube_png,
                QRScannerApplication.getInstance().getString(R.string.dots),
                EnumView.DOTS,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_whatapp,
                QRScannerApplication.getInstance().getString(R.string.eyes),
                EnumView.EYES,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_instagram,
                QRScannerApplication.getInstance().getString(R.string.logo),
                EnumView.LOGO,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_paypal,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_email,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_message,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_location,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_calender,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_contact,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_phone,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_text,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                R.drawable.ic_network,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumView.TEXT,false,R.color.black,true)
        )
    }

    fun onGenerateQR(callback: (result: Drawable) -> Unit){
        val mDataResult = getDataResult()
        val options = QrVectorOptions.Builder()
            .setPadding(.15f)
            .setBackground(
                QrVectorBackground(
                    drawable = ContextCompat
                        .getDrawable(context, R.color.white),
                )
            )
//            .setColors(
//                QrVectorColors(
//                    dark = QrVectorColor
//                        .Solid(ContextCompat.getColor(context,R.color.colorAccent)),
//                    ball = QrVectorColor.Solid(
//                        ContextCompat.getColor(context, R.color.colorAccent)
//                    )
//                )
//            )
//            .setShapes(
//                QrVectorShapes(
//                    darkPixel = QrVectorPixelShape
//                        .RoundCorners(.5f),
//                    ball = QrVectorBallShape
//                        .RoundCorners(.25f),
//                    frame = QrVectorFrameShape
//                        .RoundCorners(.25f),
//                )
//            )
        var mDrawable : Drawable? = null
        if (mDataResult?.isRequestIcon == true){
            mDrawable = ContextCompat
                .getDrawable(context, mDataResult.icon)
            mDrawable?.let {
                MyDrawableCompat.setColorFilter(it,ContextCompat.getColor(context, mDataResult.tint?: R.color.transparent))
            }
            options.setLogo(
                QrVectorLogo(
                    drawable = mDrawable,
                    size = .22f,
                    padding = QrVectorLogoPadding.Natural(.06f),
                    shape = QrVectorLogoShape
                        .Circle
                )
            )
        }
        val mData = QrData.Text(create.code?:"")
        val drawable : Drawable = QrCodeDrawable(mData, options.build(), Charsets.UTF_8)
        callback.invoke(drawable)
    }

    fun onSave(){

    }

    fun onHandle(enumView: EnumView, position : Int){
        when(enumView){
            EnumView.TEMPLATE ->{

            }
            EnumView.COLOR ->{

            }
            EnumView.DOTS ->{

            }
            EnumView.EYES->{

            }
            EnumView.LOGO ->{
                changeDesignReview.logo = mLogoList[position]
            }
            EnumView.TEXT ->{

            }
            else -> {}
        }
    }

    fun onSaveToDB(){
        val mData =  DesignQRModel()
        val mDesign = ChangeDesignModel()
        mDesign.logo = getDataResult()
        mData.uuIdQR = create.uuId
        mData.codeDesign = mDesign.toJson()
        SQLiteHelper.onInsert(mData)
    }

    val context = QRScannerApplication.getInstance()
}