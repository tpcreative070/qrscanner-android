package tpcreative.co.qrscanner.ui.changedesign

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel
import java.util.*


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
    lateinit var indexColor : ColorModel
    private var isChangedCurrentBitmap : Boolean = false
    var mapSetView : TreeSet<EnumView> = TreeSet<EnumView>()
    var isEmptyChangeDesignLogo : Boolean = true
    var isEmptyChangeDesignPositionMarker : Boolean = true
    var isEmptyChangeDesignBody : Boolean = true

    /*Position marker*/
    var mPositionMarkerList  = mutableListOf<PositionMarkerModel>()
    lateinit var indexPositionMarker : PositionMarkerModel

    /*Body*/
    var mBodyList  = mutableListOf<BodyModel>()
    lateinit var indexBody : BodyModel


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
                        mReview.logo?.let {
                            indexLogo = it
                            isEmptyChangeDesignLogo = false
                        }
                        if (mReview.logo ==null){
                            indexLogo = defaultLogo()
                            isEmptyChangeDesignLogo = true
                        }
                        shape = mReview.logo?.enumShape ?: EnumShape.ORIGINAL
                        bitmap = create.uuId?.findImageName(EnumImage.LOGO)?.toBitmap
                        Utils.Log(TAG,"Data logo ${indexLogo.toJson()}")

                        /*Color area*/
                        indexColor = mReview.color ?: defaultColor()
                        Utils.Log(TAG,"onColorChanged original 1 ${changeDesignOriginal.toJson()}")

                        /*Position marker*/
                        mReview.positionMarker?.let {
                            indexPositionMarker = it
                            isEmptyChangeDesignPositionMarker = false
                        }
                        if (mReview.positionMarker ==null){
                            indexPositionMarker = defaultPositionMarker()
                            isEmptyChangeDesignPositionMarker = true
                        }

                        /*Body*/
                        mReview.body?.let {
                            indexBody = it
                            isEmptyChangeDesignBody = false
                        }
                        if (mReview.body ==null){
                            indexBody = defaultBody()
                            isEmptyChangeDesignBody = true
                        }
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }else{
                    indexLogo = defaultLogo()
                    indexColor = defaultColor()
                    indexPositionMarker = defaultPositionMarker()
                    indexBody = defaultBody()
                    changeDesignSave = ChangeDesignModel()
                    changeDesignReview =  ChangeDesignModel()
                    changeDesignOriginal = ChangeDesignModel()
                    Utils.Log(TAG,"Data logo not found")
                    isEmptyChangeDesignLogo = true
                    isEmptyChangeDesignPositionMarker = true
                    isEmptyChangeDesignBody = true
                }
                Utils.Log(TAG,"Data change design ${create.toJson()}")
                callback.invoke(true)
            }
        }
        initializedLogoData()
        initializedColorData()
        initializedPositionMarkerData()
        initializedBodyData()
    }

    fun getData(callback: (result: MutableList<ChangeDesignCategoryModel>) -> Unit){
        mList.clear()
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_template.icon,QRScannerApplication.getInstance().getString(R.string.template),EnumView.TEMPLATE,R.color.transparent,EnumIcon.ic_template)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_paint.icon,QRScannerApplication.getInstance().getString(R.string.color),EnumView.COLOR,R.color.transparent,EnumIcon.ic_paint))
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_dots.icon,QRScannerApplication.getInstance().getString(R.string.dots),EnumView.BODY,R.color.transparent,EnumIcon.ic_dots)
        )
        mList.add(
            ChangeDesignCategoryModel(EnumIcon.ic_eyes.icon,QRScannerApplication.getInstance().getString(R.string.eyes),EnumView.POSITION_MARKER,R.color.transparent,EnumIcon.ic_eyes)
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
                EnumIcon.bg_white,false,false,false,R.color.transparent.stringHex,EnumTypeIcon.NONE,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_gallery,false,false,false,R.color.material_gray_700.stringHex,EnumTypeIcon.BITMAP,EnumChangeDesignType.VIP,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_facebook,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_twitter,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_youtube_png,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_whatapp,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_instagram,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_paypal,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_tiktok,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_linkedin,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_skype,false,true,false,R.color.transparent.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.VIP,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_wifi,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_restaurant,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_email,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_message,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_location,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_calender,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_contact,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_phone,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_text,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
        mLogoList.add(
            LogoModel(
                EnumIcon.ic_network,false,true,true,R.color.black_color_picker.stringHex,EnumTypeIcon.RES,EnumChangeDesignType.NORMAL,EnumShape.ORIGINAL)
        )
    }

    private fun initializedColorData(){
        mColorList.clear()
        mColorList.add(ColorModel(R.drawable.ic_qr_background,R.color.transparent.stringHex,EnumImage.QR_BACKGROUND,EnumChangeDesignType.NORMAL,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qr_foreground,R.color.transparent.stringHex,EnumImage.QR_FOREGROUND,EnumChangeDesignType.NORMAL,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qr_frame,R.color.transparent.stringHex,EnumImage.QR_FRAME,EnumChangeDesignType.NORMAL,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qr_ball,R.color.transparent.stringHex,EnumImage.QR_BALL,EnumChangeDesignType.NORMAL,false,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qr_background_icon,R.color.transparent.stringHex,EnumImage.QR_BACKGROUND_ICON,EnumChangeDesignType.VIP,true,defaultColorMap()))
        mColorList.add(ColorModel(R.drawable.ic_qr_foreground_icon,R.color.transparent.stringHex,EnumImage.QR_FOREGROUND_ICON,EnumChangeDesignType.NORMAL,true,defaultColorMap()))
    }

    private fun initializedPositionMarkerData(){
        mPositionMarkerList.clear()
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_default,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumPositionMarker.DEFAULT))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_corner_10px,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumPositionMarker.CORNER_10PX))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_corner_25px,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumPositionMarker.CORNER_25PX))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_circle,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumPositionMarker.CIRCLE))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_corner_top_right_bottom_left_25px,false,R.color.transparent.stringHex,EnumChangeDesignType.VIP,EnumPositionMarker.CORNER_TOP_RIGHT_BOTTOM_LEFT))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_corner_top_left_bottom_right_25px,false,R.color.transparent.stringHex,EnumChangeDesignType.VIP,EnumPositionMarker.CORNER_TOP_LEFT_BOTTOM_RIGHT))
        mPositionMarkerList.add(PositionMarkerModel(EnumIcon.ic_frame_ball_corner_top_left_top_right_bottom_left_25px,false,R.color.transparent.stringHex,EnumChangeDesignType.VIP,EnumPositionMarker.CORNER_TOP_LEFT_TOP_RIGHT_BOTTOM_LEFT))
    }

    private fun initializedBodyData(){
        mBodyList.clear()
        mBodyList.add(BodyModel(EnumIcon.ic_dark_default,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumBody.DEFAULT))
        mBodyList.add(BodyModel(EnumIcon.ic_dark_corner_0_5,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumBody.CORNER_5PX))
        mBodyList.add(BodyModel(EnumIcon.ic_dark_circle,false,R.color.transparent.stringHex,EnumChangeDesignType.VIP,EnumBody.CIRCLE))
        mBodyList.add(BodyModel(EnumIcon.ic_dark_star,false,R.color.transparent.stringHex,EnumChangeDesignType.VIP,EnumBody.STAR))
    }

    fun onGenerateQR(callback: (result: Drawable) -> Unit){
        val mDataResult = changeDesignReview.logo
        Utils.Log(TAG,"Data result of review ${changeDesignReview.toJson()}")
        Utils.Log(TAG,"Generate icon => shape ${this.shape.name}")
        val options = QrVectorOptions.Builder()
            .setPadding(.15f)
            .setBackground(
                QrVectorBackground(
//                    color =QrVectorColor
//                        .Solid(Color(R.color.transparent.toColorLong()))
                    color =QrVectorColor
                        .Solid(indexColor.mapColor[EnumImage.QR_BACKGROUND]?.toColorIntThrowDefaultColor() ?: R.color.white)
                )
            )
            .setColors(
                QrVectorColors(
                    dark =  QrVectorColor.Solid(indexColor.mapColor[EnumImage.QR_FOREGROUND]?.toColorIntThrowDefaultColor() ?: R.color.black_color_picker),
                    ball = QrVectorColor.Solid(indexColor.mapColor[EnumImage.QR_BALL]?.toColorIntThrowDefaultColor() ?: R.color.black_color_picker),
                    frame = QrVectorColor.Solid(indexColor.mapColor[EnumImage.QR_FRAME]?.toColorIntThrowDefaultColor() ?: R.color.black_color_picker)
                ))
            .setShapes(
                qrShapes()
            )
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
        when(mDataResult?.typeIcon){
            EnumTypeIcon.RES ->{
                mDrawable = ContextCompat
                    .getDrawable(context, mDataResult.enumIcon.icon)
//                mDrawable?.let {
//                    Utils.Log(TAG,"Color result RES ${mDataResult.tintColorHex}")
//                    MyDrawableCompat.setColorFilter(it,indexColor.mapColor[EnumImage.QR_FOREGROUND_ICON]?.toColorIntThrowDefaultColor() ?: R.color.transparent)
//                }
                tintFGColor(mDataResult,mDrawable)
                Utils.Log(TAG,"Result cropped bitmap  to res ${mDataResult.typeIcon.name}")
                options.setLogo(
                    QrVectorLogo(
                        drawable = mDrawable,
                        size = .19f,
                        padding = QrVectorLogoPadding.Natural(.0f),
                        shape = shape,
                        backgroundColor =  QrVectorColor
                            .Solid(tintBGColor(mDataResult))
                    )
                )
            }
            EnumTypeIcon.BITMAP ->{
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
                                .Solid(mDataResult.tintColorHex?.toColorIntThrowDefaultColor() ?: R.color.transparent)
                        )
                    )
                    Utils.Log(TAG,"Result cropped bitmap  to drawable :Shape ${this.shape}")
                }
                Utils.Log(TAG,"Result cropped bitmap nothing")
            }
            else -> {
                Utils.Log(TAG,"Nothing for icon")
            }
        }
        val mData = QrData.Text(create.code?:"")
        val drawable : Drawable = QrCodeDrawable(mData, options.build(), Charsets.UTF_8)
        callback.invoke(drawable)
    }

    @SuppressLint("ResourceAsColor")
    @ColorInt
    private fun tintBGColor(mDataResult: LogoModel) : Int{
        if (mDataResult.isSupportedBGColor){
            return indexColor.mapColor[EnumImage.QR_BACKGROUND_ICON]?.toColorIntThrowDefaultColor() ?: R.color.transparent
        }
        return R.color.transparent
    }

    private fun tintFGColor(mDataResult: LogoModel ,drawable: Drawable?){
        if (mDataResult.isSupportedFGColor){
            ContextCompat
                .getDrawable(context, mDataResult.enumIcon.icon)
            drawable?.let {
                Utils.Log(TAG,"Color result RES ${mDataResult.tintColorHex}")
                MyDrawableCompat.setColorFilter(it,indexColor.mapColor[EnumImage.QR_FOREGROUND_ICON]?.toColorIntThrowDefaultColor() ?: R.color.transparent)
            }
        }
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
               EnumView.LOGO ->{
                   changeDesignReview.logo = indexLogo
               }
               EnumView.POSITION_MARKER->{
                   changeDesignReview.positionMarker = indexPositionMarker
               }
               EnumView.BODY ->{
                   changeDesignReview.body = indexBody
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
                EnumView.BODY ->{

                }
                EnumView.POSITION_MARKER->{

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
                EnumView.LOGO ->{
                    changeDesignSave.logo = indexLogo
                    changeDesignSave.logo?.enumShape = shape
                }
                EnumView.POSITION_MARKER->{
                    changeDesignSave.positionMarker = indexPositionMarker
                }
                EnumView.BODY ->{
                    changeDesignSave.body = indexBody
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
            EnumIcon.bg_white,false,false,false,R.color.transparent.stringHex,EnumTypeIcon.NONE,EnumChangeDesignType.NORMAL,EnumShape.SQUARE)
    }

    fun defaultColor() : ColorModel {
        return ColorModel(R.drawable.ic_qrcode_bg,R.color.transparent.stringHex,EnumImage.QR_BACKGROUND,EnumChangeDesignType.NORMAL,false,defaultColorMap())
    }

    fun defaultPositionMarker() : PositionMarkerModel {
        return PositionMarkerModel(EnumIcon.ic_frame_ball_default,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumPositionMarker.DEFAULT)
    }

    fun defaultBody() : BodyModel {
        return BodyModel(EnumIcon.ic_dark_default,false,R.color.transparent.stringHex,EnumChangeDesignType.NORMAL,EnumBody.DEFAULT)
    }

    private fun qrShapes() : QrVectorShapes{
        val body :  QrVectorPixelShape
        val ball : QrVectorBallShape
        val frame : QrVectorFrameShape
        Utils.Log(TAG,"enum marker position ${indexPositionMarker.enumPositionMarker}")
        Utils.Log(TAG,"enum body ${indexBody.enumBody}")
        try {
            when(indexPositionMarker.enumPositionMarker){
                EnumPositionMarker.CORNER_10PX ->{
                    ball = QrVectorBallShape.RoundCorners(.10f)
                    frame = QrVectorFrameShape.RoundCorners(.10f)
                }
                EnumPositionMarker.CORNER_25PX ->{
                    ball = QrVectorBallShape.RoundCorners(.25f)
                    frame = QrVectorFrameShape.RoundCorners(.25f)
                }
                EnumPositionMarker.CIRCLE ->{
                    ball = QrVectorBallShape.Circle(1f)
                    frame = QrVectorFrameShape.Circle()
                }
                EnumPositionMarker.CORNER_TOP_RIGHT_BOTTOM_LEFT -> {
                    ball = QrVectorBallShape
                        .RoundCorners(.25f, topLeft = false, topRight = true, bottomLeft = true, bottomRight = false)
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f, topLeft = false, topRight = true, bottomLeft = true, bottomRight = false)
                }
                EnumPositionMarker.CORNER_TOP_LEFT_BOTTOM_RIGHT -> {
                    ball = QrVectorBallShape
                        .RoundCorners(.25f, topLeft = true, topRight = false, bottomLeft = false, bottomRight = true)
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f, topLeft = true, topRight = false, bottomLeft = false, bottomRight = true)
                }
                EnumPositionMarker.CORNER_TOP_LEFT_TOP_RIGHT_BOTTOM_LEFT -> {
                    ball = QrVectorBallShape
                        .RoundCorners(.25f, topLeft = true, topRight = true, bottomLeft = true, bottomRight = false)
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f, topLeft = true, topRight = true, bottomLeft = true, bottomRight = false)
                }
                else -> {
                    ball = QrVectorBallShape.Default
                    frame = QrVectorFrameShape.Default
                    Utils.Log(TAG,"enum marker position else")
                }
            }
            when(indexBody.enumBody){
                EnumBody.DEFAULT ->{
                    body = QrVectorPixelShape
                        .Default
                }
                EnumBody.CORNER_5PX ->{
                    body = QrVectorPixelShape
                        .RoundCorners(0.5f)
                }
                EnumBody.CIRCLE ->{
                    body = QrVectorPixelShape
                        .Circle()
                }
                else -> {
                    body = QrVectorPixelShape
                        .Star
                }
            }

            return QrVectorShapes(
                darkPixel = body,
                ball = ball,
                frame = frame
            )
        }catch (e : Exception){
            return QrVectorShapes(
                darkPixel = QrVectorPixelShape
                    .Default,
                ball = QrVectorBallShape.Default ,
                frame = QrVectorFrameShape.Default
            )
        }
    }

    fun onUpdateBitmap(bitmap: Bitmap?){
        this.bitmap = bitmap
        isChangedCurrentBitmap = true
    }

    fun isChangedSave() : Boolean{
        Utils.Log(TAG,"Data value original ${changeDesignOriginal.toJson()}")
        Utils.Log(TAG,"Data value save ${changeDesignSave.toJson()}")
        Utils.Log(TAG,"Data value review ${changeDesignReview.toJson()}")
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
                EnumView.POSITION_MARKER -> {
                    if (mChanged || indexPositionMarker.toJson() != changeDesignOriginal.positionMarker?.toJson()){
                        return true
                    }
                }
                EnumView.BODY ->{
                    if (mChanged || indexBody.toJson() != changeDesignOriginal.body?.toJson()){
                        return true
                    }
                }
                else -> {}
            }
        }
        return false
    }

    fun isChangedReview() : Boolean {
        Utils.Log(TAG,"Data value original ${changeDesignOriginal.toJson()}")
        Utils.Log(TAG,"Data value save ${changeDesignSave.toJson()}")
        Utils.Log(TAG,"Data value review ${changeDesignReview.toJson()}")
        val mChanged = changeDesignOriginal.toJson() != changeDesignReview.toJson()
        Utils.Log(TAG,"Data value isChanged ${mChanged}")
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
                EnumView.POSITION_MARKER ->{
                    if (mChanged || indexPositionMarker.toJson() != changeDesignOriginal.positionMarker?.toJson()){
                        return true
                    }
                }
                EnumView.BODY ->{
                    if (mChanged || indexBody.toJson() != changeDesignOriginal.body?.toJson()){
                        return true
                    }
                }
                else -> {}
            }
        }
        return false
    }

    private fun defaultColorMap() : HashMap<EnumImage,String>{
        val mMap = HashMap<EnumImage,String>()
        mMap[EnumImage.QR_BACKGROUND] = R.color.white.stringHexNoTransparency
        mMap[EnumImage.QR_FOREGROUND] = R.color.black_color_picker.stringHexNoTransparency
        mMap[EnumImage.QR_FRAME] = R.color.black_color_picker.stringHexNoTransparency
        mMap[EnumImage.QR_BALL] = R.color.black_color_picker.stringHexNoTransparency
        mMap[EnumImage.QR_BACKGROUND_ICON] = R.color.transparent.stringHex
        mMap[EnumImage.QR_FOREGROUND_ICON] = R.color.black_color_picker.stringHexNoTransparency
        return mMap
    }

    fun isAllowNavigation(selected : ColorModel): Boolean {
        if (indexLogo.isSupportedBGColor && selected.type == EnumImage.QR_BACKGROUND_ICON){
            return true
        }
        else if(indexLogo.isSupportedFGColor && selected.type == EnumImage.QR_FOREGROUND_ICON){
            return true
        }else{
            if (selected.type == EnumImage.QR_BACKGROUND || selected.type == EnumImage.QR_FOREGROUND || selected.type == EnumImage.QR_FRAME || selected.type == EnumImage.QR_BALL){
                return true
            }
        }
        return false
    }

    //private fun

    val context = QRScannerApplication.getInstance()
}