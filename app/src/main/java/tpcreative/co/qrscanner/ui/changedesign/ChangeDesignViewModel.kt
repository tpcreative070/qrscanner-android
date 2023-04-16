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
import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.icon
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.extension.toObject
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
    var index  : Int = -1
    lateinit var indexLogo : LogoModel
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
                indexLogo = defaultObject()
                val mDataStore = SQLiteHelper.getDesignQR(data.uuId)
                if (mDataStore!=null){
                    try {
                        val mDesign = mDataStore.codeDesign?.toObject(ChangeDesignModel::class.java) ?:  ChangeDesignModel()
                        indexLogo = mDesign.logo ?: defaultObject()
                        Utils.Log(TAG,"Data logo ${indexLogo.toJson()}")
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }else{
                    Utils.Log(TAG,"Data logo not found")
                }
                callback.invoke(true)
            }
        }
        initializedLogoData()
    }

    fun getData(callback: (result: MutableList<ChangeDesignCategoryModel>) -> Unit){
        mList.clear()
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_template.icon,QRScannerApplication.getInstance().getString(R.string.template),EnumView.TEMPLATE,R.color.transparent,EnumIcon.ic_template)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_paint.icon,QRScannerApplication.getInstance().getString(R.string.color),EnumView.COLOR,R.color.transparent,EnumIcon.ic_paint))
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_dots.icon,QRScannerApplication.getInstance().getString(R.string.dots),EnumView.DOTS,R.color.transparent,EnumIcon.ic_dots)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_eyes.icon,QRScannerApplication.getInstance().getString(R.string.eyes),EnumView.EYES,R.color.transparent,EnumIcon.ic_eyes)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_registered.icon,QRScannerApplication.getInstance().getString(R.string.logo),EnumView.LOGO,R.color.transparent,EnumIcon.ic_registered)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_design_text.icon,QRScannerApplication.getInstance().getString(R.string.text),EnumView.TEXT,R.color.transparent,EnumIcon.ic_design_text)
        )
        callback(mList)
    }

    private fun initializedLogoData(){
        mLogoList.clear()
        mLogoList.add(
            LogoModel(
                EnumIcon.bg_white.icon,
                QRScannerApplication.getInstance().getString(R.string.template),
                EnumIcon.bg_white,false,R.color.transparent,false)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.design_wifi.icon,
                QRScannerApplication.getInstance().getString(R.string.template),
                EnumIcon.design_wifi,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_twitter.icon,
                QRScannerApplication.getInstance().getString(R.string.color),
                EnumIcon.ic_twitter,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_youtube_png.icon,
                QRScannerApplication.getInstance().getString(R.string.dots),
                EnumIcon.ic_youtube_png,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_whatapp.icon,
                QRScannerApplication.getInstance().getString(R.string.eyes),
                EnumIcon.ic_whatapp,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_instagram.icon,
                QRScannerApplication.getInstance().getString(R.string.logo),
                EnumIcon.ic_instagram,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_paypal.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_paypal,false,R.color.transparent,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_email.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_email,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_message.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_message,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_location.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_location,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_calender.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_calender,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_contact.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_contact,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_phone.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_phone,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_text.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_text,false,R.color.black,true)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_network.icon,
                QRScannerApplication.getInstance().getString(R.string.text),
                EnumIcon.ic_network,false,R.color.black,true)
        )
    }

    fun onGenerateQR(callback: (result: Drawable) -> Unit){
        val mDataResult = indexLogo
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
        mDesign.logo = indexLogo
        mData.uuIdQR = create.uuId
        mData.codeDesign = mDesign.toJson()
        SQLiteHelper.onInsert(mData)
    }

    fun defaultObject() : LogoModel {
        return LogoModel(
            EnumIcon.bg_white.icon,
            QRScannerApplication.getInstance().getString(R.string.template),
            EnumIcon.bg_white,false,R.color.transparent,false)
    }

    val context = QRScannerApplication.getInstance()
}