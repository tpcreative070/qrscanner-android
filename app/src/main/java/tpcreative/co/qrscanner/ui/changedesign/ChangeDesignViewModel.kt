package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel
import java.util.TreeSet

class ChangeDesignViewModel  : BaseViewModel<ItemNavigation>(){
    val TAG = this::class.java.name
    var mList: MutableList<ChangeDesignCategoryModel> = mutableListOf()
    var create: GeneralModel = GeneralModel()
    var index  : Int = -1
    /*Logo area*/
    lateinit var indexLogo : LogoModel
    var mLogoList = mutableListOf<LogoModel>()
    lateinit var changeDesignReview :ChangeDesignModel
    lateinit var changeDesignSave :ChangeDesignModel
    lateinit var changeDesignOriginal :ChangeDesignModel
    var shape : EnumShape = EnumShape.SQUARE
    var uri : Uri? = null
    var bitmap : Bitmap? = null
    /*Color area*/
    var mColorList = mutableListOf<ColorModel>()
    var isOpenColorPicker : Boolean = false
    var enumType : EnumImage  = EnumImage.NONE
    //var mMapColor : HashMap<EnumImage,String> = hashMapOf()
    lateinit var indexColor : ColorModel
    private var isChangedCurrentBitmap : Boolean = false
    var mapSetView : TreeSet<EnumView> = TreeSet<EnumView>()

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
                indexLogo = defaultLogo()
                indexColor = defaultColor()
                val mDataStore = SQLiteHelper.getDesignQR(data.uuId)
                if (mDataStore!=null){
                    try {
                        val mReview = mDataStore.codeDesign?.toObject(ChangeDesignModel::class.java) ?:  ChangeDesignModel()
                        val mOriginal = mDataStore.codeDesign?.toObject(ChangeDesignModel::class.java) ?:  ChangeDesignModel()
                        val mSave = mDataStore.codeDesign?.toObject(ChangeDesignModel::class.java) ?:  ChangeDesignModel()
                        changeDesignSave = ChangeDesignModel(mSave)
                        changeDesignOriginal = ChangeDesignModel(mOriginal)
                        changeDesignReview =  ChangeDesignModel(mReview)

                        /*Logo area*/
                        indexLogo = mReview.logo ?: defaultLogo()
                        shape = mReview.logo?.enumShape ?: EnumShape.ORIGINAL
                        bitmap = create.uuId?.findImageName(EnumImage.LOGO)?.toBitmap
                        Utils.Log(TAG,"Data logo ${indexLogo.toJson()}")
                        /*Color area*/
                        indexColor = mReview.color ?: defaultColor()
                        Utils.Log(TAG,"onColorChanged original 1 ${changeDesignOriginal.toJson()}")
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }else{
                    indexLogo = defaultLogo()
                    indexColor = defaultColor()
                    changeDesignSave = ChangeDesignModel()
                    changeDesignReview =  ChangeDesignModel()
                    changeDesignOriginal = ChangeDesignModel()
                    Utils.Log(TAG,"Data logo not found")
                }
                Utils.Log(TAG,"Data change design ${create.toJson()}")
                callback.invoke(true)
            }
        }
        initializedLogoData()
        initializedColorData()
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
                EnumIcon.bg_white,false,R.color.transparent,EnumTypeIcon.NONE,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_gallery.icon,
                EnumIcon.ic_gallery,false,R.color.material_gray_700,EnumTypeIcon.BITMAP,EnumChangeDesignType.VIP,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.design_wifi.icon,
                EnumIcon.design_wifi,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_twitter.icon,
                EnumIcon.ic_twitter,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_youtube_png.icon,
                EnumIcon.ic_youtube_png,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_whatapp.icon,
                EnumIcon.ic_whatapp,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_instagram.icon,
                EnumIcon.ic_instagram,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_paypal.icon,
                EnumIcon.ic_paypal,false,R.color.transparent,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_email.icon,
                EnumIcon.ic_email,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_message.icon,
                EnumIcon.ic_message,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_location.icon,
                EnumIcon.ic_location,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_calender.icon,
                EnumIcon.ic_calender,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_contact.icon,
                EnumIcon.ic_contact,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_phone.icon,
                EnumIcon.ic_phone,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_text.icon,
                EnumIcon.ic_text,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_network.icon,
                EnumIcon.ic_network,false,R.color.black_color_picker,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
    }

    private fun initializedColorData(){
        mColorList.clear()
        mColorList.add(ColorModel(R.drawable.ic_qrcode_bg,R.color.transparent,EnumImage.QR_BACKGROUND,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qrcode,R.color.colorAccent,EnumImage.QR_FOREGROUND,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qrcode,R.color.colorAccent,EnumImage.QR_BALL,false,defaultColorMap()))
    }

    fun onGenerateQR(callback: (result: Drawable) -> Unit){
        val mDataResult = changeDesignReview.logo
        Utils.Log(TAG,"Data result of review ${changeDesignReview.color?.toJson()}")
        Utils.Log(TAG,"Generate icon => shape ${this.shape.name}")
        val options = QrVectorOptions.Builder()
            .setPadding(.15f)
            .setBackground(
                QrVectorBackground(
                    color =QrVectorColor
                        .Solid(indexColor.mapColor[EnumImage.QR_BACKGROUND]?.toColorInt() ?: R.color.black_color_picker)
                )
            )
            .setColors(
                QrVectorColors(
                    dark =  QrVectorColor.Solid(indexColor.mapColor[EnumImage.QR_FOREGROUND]?.toColorInt() ?: R.color.black_color_picker),
                    ball = QrVectorColor.Solid(indexColor.mapColor[EnumImage.QR_BALL]?.toColorInt() ?: R.color.black_color_picker)
                ))
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
        val mDrawable: Drawable?
        val shape : QrVectorLogoShape = when(this.shape){
            EnumShape.SQUARE ->{
                QrVectorLogoShape.RoundCorners(0.2f)
            }
            EnumShape.CIRCLE ->{
                QrVectorLogoShape.Circle
            }
            else -> {
                QrVectorLogoShape.Default
            }
        }
        if (mDataResult?.typeIcon == EnumTypeIcon.RES){
            mDrawable = ContextCompat
                .getDrawable(context, mDataResult.icon)
            mDrawable?.let {
                MyDrawableCompat.setColorFilter(it,ContextCompat.getColor(context, mDataResult.tint))
            }
            Utils.Log(TAG,"Result cropped bitmap  to res")
            options.setLogo(
                QrVectorLogo(
                    drawable = mDrawable,
                    size = .19f,
                    padding = QrVectorLogoPadding.Natural(.0f),
                    shape = shape,
                    backgroundColor = QrVectorColor
                        .Solid(R.color.white.stringHexNoTransparency.toColorInt())
                )
            )
        }
        else if (mDataResult?.typeIcon == EnumTypeIcon.BITMAP){
            if (bitmap!=null){
                mDrawable = when(this.shape){
                    EnumShape.CIRCLE ->{
                        bitmap?.toCircular(QRScannerApplication.getInstance(),0.06f,true)
                    }
                    EnumShape.SQUARE ->{
                        bitmap?.toCircular(QRScannerApplication.getInstance(),0.019f,false)
                    }
                    else -> {
                        bitmap?.toDrawable(QRScannerApplication.getInstance().resources)
                    }
                }
                options.setLogo(
                    QrVectorLogo(
                        drawable = mDrawable,
                        size = .19f,
                        padding = QrVectorLogoPadding.Natural(.0f),
                        shape = shape,
                        backgroundColor =  QrVectorColor
                            .Solid(R.color.transparent.stringHexNoTransparency.toColorInt())
                    )
                )
                Utils.Log(TAG,"Result cropped bitmap  to drawable :Shape ${this.shape}")
            }
            Utils.Log(TAG,"Result cropped bitmap nothing")
        }
        else{
           Utils.Log(TAG,"Nothing for icon")
        }
        val mData = QrData.Text(create.code?:"")
        val drawable : Drawable = QrCodeDrawable(mData, options.build(), Charsets.UTF_8)
        callback.invoke(drawable)
    }

    fun selectedIndexOnReview(){
       mapSetView.forEach {
           when(it){
               EnumView.TEMPLATE ->{

               }
               EnumView.COLOR ->{
                   Utils.Log(TAG,"data value result original ${changeDesignOriginal.color?.mapColor?.toJson()}")
                   changeDesignReview.color = indexColor
                   Utils.Log(TAG,"data value result review ${changeDesignReview.color?.mapColor?.toJson()}")
                   Utils.Log(TAG,"data value result after original ${changeDesignOriginal.color?.mapColor?.toJson()}")
               }
               EnumView.DOTS ->{

               }
               EnumView.EYES->{

               }
               EnumView.LOGO ->{
                   changeDesignReview.logo = indexLogo
               }
               EnumView.TEXT ->{

               }
               else -> {}
           }
       }
    }

    fun selectedIndexRestore(){
        val mData = ChangeDesignModel(changeDesignSave)
        Utils.Log(TAG,"Show data restore ${mData.toJson()}")
        mapSetView.forEach {
            when(it){
                EnumView.TEMPLATE ->{

                }
                EnumView.COLOR ->{
                    val mHashMap = HashMap(mData.color?.mapColor ?: defaultColorMap())
                    val mColor = mHashMap[enumType]
                    mColor?.putChangedDesignColor
                    changeDesignReview.color?.mapColor = mHashMap
                    indexColor.mapColor = mHashMap
                }
                EnumView.DOTS ->{

                }
                EnumView.EYES->{

                }
                EnumView.LOGO ->{
                    changeDesignReview = mData
                    indexLogo = mData.logo ?: defaultLogo()
                }
                EnumView.TEXT ->{

                }
                else -> {}
            }
        }
    }

    fun selectedIndexOnSave(){
        mapSetView.forEach {
            when(it){
                EnumView.TEMPLATE ->{

                }
                EnumView.COLOR ->{
                    val mHashMap = HashMap(indexColor.mapColor)
                    if (changeDesignSave.color==null){
                        val mIndex = defaultColor()
                        mIndex.mapColor = mHashMap
                        changeDesignSave.color = mIndex
                    }else{
                        changeDesignSave.color?.mapColor = mHashMap
                    }
                    Utils.Log(TAG,"SelectedIndexOnSave ${indexColor.mapColor.toJson()}")
                }
                EnumView.DOTS ->{

                }
                EnumView.EYES->{

                }
                EnumView.LOGO ->{
                    changeDesignSave.logo = indexLogo
                    changeDesignSave.logo?.enumShape = shape
                }
                EnumView.TEXT ->{

                }
                else -> {}
            }
        }
    }

    fun onSaveToDB(){
        val mData =  DesignQRModel()
        mData.uuIdQR = create.uuId
        mData.codeDesign = changeDesignSave.toJson()
        Utils.Log(TAG,"Preparing data store into db ${changeDesignSave.toJson()}")
        Utils.Log(TAG,"Preparing data model store ${mData.toJson()}")
        SQLiteHelper.onInsert(mData)
    }

    fun defaultLogo() : LogoModel {
        return LogoModel(
            EnumIcon.bg_white.icon,
            EnumIcon.bg_white,false,R.color.transparent,EnumTypeIcon.NONE,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
    }

    fun defaultColor() : ColorModel {
        return ColorModel(R.drawable.ic_qrcode_bg,R.color.transparent,EnumImage.QR_BACKGROUND,false,defaultColorMap())
    }

    fun onUpdateBitmap(bitmap: Bitmap?){
        this.bitmap = bitmap
        isChangedCurrentBitmap = true
    }

    fun isChangedSave() : Boolean{
        Utils.Log(TAG,"Data value original ${changeDesignOriginal.color?.mapColor?.toJson()}")
        Utils.Log(TAG,"Data value review ${indexColor.mapColor.toJson()}")
        val mChanged = changeDesignOriginal.toJson() != changeDesignSave.toJson()
        mapSetView.forEach {
            when(it){
                EnumView.COLOR ->{
                    if (mChanged || indexColor.toJson() != changeDesignOriginal.color?.toJson()){
                        Utils.Log(TAG,"Data value indexColor ${indexColor.toJson()}")
                        Utils.Log(TAG,"Data value changeDesignOriginal ${changeDesignOriginal.color?.toJson()}")
                        return true
                    }
                }
                EnumView.LOGO ->{
                    Utils.Log(TAG,"Check logo share ${shape.name}")
                    Utils.Log(TAG,"Check logo share original ${changeDesignOriginal.logo?.enumShape?.name}")
                    if (mChanged || shape != changeDesignOriginal.logo?.enumShape || isChangedCurrentBitmap){
                        return true
                    }
                }
                else -> {}
            }
        }
        return false
    }

    fun isChangedReview() : Boolean {
        Utils.Log(TAG,"Data value original ${changeDesignOriginal.color?.toJson()}")
        Utils.Log(TAG,"Data value review ${indexColor.toJson()}")
        val mChanged = changeDesignOriginal.toJson() != changeDesignReview.toJson()
        mapSetView.forEach {
            when(it){
                EnumView.COLOR ->{
                    if (mChanged || indexColor.toJson() != changeDesignOriginal.color?.toJson()){
                        return true
                    }
                }
                EnumView.LOGO ->{
                    if (mChanged || shape != changeDesignOriginal.logo?.enumShape){
                        return true
                    }
                }
                else -> {}
            }
        }
        return false
    }

    fun defaultColorMap() : HashMap<EnumImage,String>{
        val mMap = HashMap<EnumImage,String>()
        mMap[EnumImage.QR_BACKGROUND] = R.color.white.stringHexNoTransparency
        mMap[EnumImage.QR_FOREGROUND] = R.color.black_color_picker.stringHexNoTransparency
        mMap[EnumImage.QR_BALL] = R.color.black_color_picker.stringHexNoTransparency
        return mMap
    }

    val context = QRScannerApplication.getInstance()
}