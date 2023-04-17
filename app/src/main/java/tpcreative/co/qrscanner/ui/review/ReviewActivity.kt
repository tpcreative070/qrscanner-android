package tpcreative.co.qrscanner.ui.review

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import tpcreative.co.qrscanner.ui.scanner.cpp.BarcodeEncoder
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Result
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.databinding.ActivityReviewBinding
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignActivity
import tpcreative.co.qrscanner.ui.create.BarcodeActivity
import java.util.*

class ReviewActivity : BaseActivitySlide() {
    lateinit var viewModel: ReviewViewModel
    var create: GeneralModel? = null
    var bitmap: Bitmap? = null
    var code: String? = null
    var type: String? = null
    var format: String? = null
    var mUri : Uri? = null
    var isRequestPrint : Boolean = false
    var isRequestExportPNG : Boolean = false
    var processDrawnDone : Boolean = false
    private var save: SaveModel = SaveModel()
    var dialog : Dialog? = null
    var isAlreadySaved  = false
    var viewAds : AdsView? = null
    lateinit var binding : ActivityReviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        dialog = ProgressDialog.progressDialog(this,R.string.waiting_for_export.toText())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ConstantKey.key_saved, isAlreadySaved)
        Utils.Log(TAG,"State saved")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isAlreadySaved = savedInstanceState.getBoolean(ConstantKey.key_saved)
        Utils.Log(TAG,"State restore")
    }

    fun onCatch() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.REVIEW_SMALL)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.REVIEW_LARGE)
        checkingShowAds()
    }

    override fun onPause() {
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.REVIEW_SMALL)
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.REVIEW_LARGE)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        QRScannerApplication.getInstance().setRequestClearCacheData(true)
        bitmap?.recycle()
    }

    override fun onStop() {
        super.onStop()
    }

    fun setView() {
        create = viewModel.create
        format = viewModel.create.barcodeFormat
        create?.let {
            save = Utils.onGeneralParse(it,SaveModel::class)
            code = save.code
            type = save.type
            create?.code = code
            binding.txtSubject.text = type
            binding.txtDisplay.text = code
            binding.txtFormat.text = format
            redesignLayout()
            CoroutineScope(Dispatchers.IO).launch {
                onGenerateReview(code)
                onGenerateQRCode(code)
                onDrawOnBitmap(Utils.getDisplay(GeneralModel(save))?:"",Utils.onTranslateCreateType(it
                    .createType ?: ParsedResultType.TEXT), BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))
            }
        }
    }

    private fun redesignLayout(){
        if (isBarCode()){
            //binding.imgResult.change(Utils.dpToSp(300f,this@ReviewActivity),Utils.dpToSp(200f,this@ReviewActivity))
            val params = LinearLayout.LayoutParams(
                300f.px,
                200f.px
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 10
            }
            binding.imgResult.layoutParams = params
            binding.llChangeDesign.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_review, menu)
        return super.onCreateOptionsMenu(menu)
    }

    val cropForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_CROP")
            handleCrop(result.resultCode, result.data)
        }else if (result.resultCode == RESULT_CANCELED){
            finish()
        }
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: String? = Crop.getOutputString(result)
            val mResult = Gson().fromJson(mData, Result::class.java)
            mResult?.let { onFilterResult(it) }
            Utils.Log(TAG, "Result of cropped " + Gson().toJson(mResult))
        } else if (resultCode == Crop.RESULT_ERROR) {
            Utils.onAlertNotify(this,"${Crop.getError(result)?.message}")
        }
    }

    private fun onFilterResult(result: Result?) {
        result?.let { mResult ->
            val parsedResult = ResultParser.parseResult(result)
            Utils.Log(TAG,"Text result ${mResult.text}")
            val create = Utils.onGeneralParse(mResult,GeneralModel::class)
            create.barcodeFormat = result.barcodeFormat.name
            onSaveFromTextOrCVFToQRCode(EnumAction.VIEW_CROP,"","",create)
            Utils.Log(TAG,"barcode ==> type ${parsedResult.type}")
            Utils.Log(TAG, "barcode ==> format ${mResult.barcodeFormat?.name}")
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showAds()
                return true
            }
//            R.id.menu_item_change_design ->{
//                Navigator.onGenerateView(this, create, ChangeDesignActivity::class.java)
//            }
            R.id.menu_item_png_export -> {
                if (!isRequestExportPNG){
                    isRequestExportPNG = true
                    dialog?.show()
                    shareToSocial()
                }
                return true
            }
            R.id.menu_item_print -> {
                if (!isRequestPrint){
                    isRequestPrint = true
                    dialog?.show()
                    onPhotoPrint()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSavedData() {
        save.favorite = false
        /*Detect rotation the screen*/
        if (create?.enumImplement == EnumImplement.CREATE && !isAlreadySaved) {
            val time = Utils.getCurrentDateTimeSort()
            save.createDatetime = time
            save.updatedDateTime = time
            Utils.Log(TAG, "Questing created")
            Utils.Log(TAG,"Questing created ${Gson().toJson(save)}")
            /*Doing for innovation save into history*/
            if(Utils.isInnovation()){
                SQLiteHelper.onInsert(HistoryModel(save))
            }else{
                SQLiteHelper.onInsert(save)
            }
            isAlreadySaved = true
        } else if (create?.enumImplement == EnumImplement.EDIT && !isAlreadySaved) {
            val time = Utils.getCurrentDateTimeSort()
            save.updatedDateTime = time
            save.createDatetime = create?.createdDateTime
            save.id = create?.id
            save.isSynced = create?.isSynced
            save.uuId = create?.uuId
            save.favorite = viewModel.getFavorite(create?.id)
            save.noted = viewModel.getTakeNote(create?.id)
            Utils.Log(TAG, "Questing updated")
            Utils.Log(TAG,"Questing updated ${Gson().toJson(save)}")
            SQLiteHelper.onUpdate(save, true)
            isAlreadySaved = true
        } else if (create?.enumImplement == EnumImplement.VIEW) {
            Utils.Log(TAG, "Questing view")
        }
        GenerateSingleton.getInstance()?.onCompletedGenerate()
    }

    suspend fun onGenerateReview(code: String?) =
        withContext(Dispatchers.Main) {
            mergeUUID()
            val mFile = create?.uuId?.findImageName()
            if (mFile!=null){
                binding.imgResult.setImageURI(mFile.toUri())
                onSavedData()
                return@withContext
            }
            try {
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> =
                    EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8
                val theme: Theme? = Theme.getInstance()?.getThemeInfo()
                Utils.Log(TAG, "barcode====================> " + code + "--" + create?.createType?.name)
                val mBitmap = if (isBarCode()) {
                    hints[EncodeHintType.MARGIN] = 5
                    var mFormatCode = BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name)
                    if(mFormatCode == BarcodeFormat.RSS_14){
                        mFormatCode = BarcodeFormat.CODABAR
                    }
                    var mWidth = Constant.QRCodeViewWidth + 100
                    var mHeight = Constant.QRCodeViewHeight - 100
                    if (mFormatCode== BarcodeFormat.AZTEC || mFormatCode == BarcodeFormat.DATA_MATRIX){
                        mWidth = Constant.QRCodeViewWidth
                        mHeight = Constant.QRCodeViewHeight
                        hints[EncodeHintType.MARGIN] = 2
                    }
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        mFormatCode,
                        mWidth,
                        mHeight,
                        hints
                    )

                } else {
                    hints[EncodeHintType.MARGIN] = 2
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        BarcodeFormat.QR_CODE,
                        Constant.QRCodeViewHeight,
                        Constant.QRCodeViewHeight,
                        hints
                    )
                }
                binding.imgResult.setImageBitmap(mBitmap)
                onSavedData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    fun isBarCode() : Boolean{
        if ((BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))) {
            return true
        }
        return false
    }

    suspend fun onGenerateQRCode(code: String?) =
        withContext(Dispatchers.IO) {
            mergeUUID()
            val mFile = create?.uuId?.findImageName()
            if (mFile!=null){
                mUri = FileProvider.getUriForFile(this@ReviewActivity, BuildConfig.APPLICATION_ID + ".provider", mFile)
                bitmap = BitmapFactory.decodeFile(mFile.absolutePath)
                isRequestPrint = false
                isRequestExportPNG = false
                return@withContext
            }
            try {
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> =
                    EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8
                val theme: Theme? = Theme.getInstance()?.getThemeInfo()
                Utils.Log(TAG, "barcode====================> " + code + "--" + create?.createType?.name)
                bitmap = if (BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))  {
                    hints[EncodeHintType.MARGIN] = 15
                    var mFormatCode =  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name)
                    if(mFormatCode == BarcodeFormat.RSS_14){
                        mFormatCode = BarcodeFormat.CODABAR
                    }
                    var mWidth = Constant.QRCodeExportWidth + 150
                    var mHeight = Constant.QRCodeExportHeight - 300
                    if (mFormatCode== BarcodeFormat.AZTEC || mFormatCode == BarcodeFormat.DATA_MATRIX){
                        mWidth = Constant.QRCodeViewWidth
                        mHeight = Constant.QRCodeViewHeight
                        hints[EncodeHintType.MARGIN] = 2
                    }
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        mFormatCode,
                        mWidth,
                        mHeight,
                        hints
                    )
                } else {
                    Utils.Log(TAG,"code $code")
                    hints[EncodeHintType.MARGIN] = 2
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        BarcodeFormat.QR_CODE,
                        Constant.QRCodeExportWidth,
                        Constant.QRCodeExportHeight,
                        hints
                    )
                }
                getImageUri(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadReviewSmallView(viewAds?.getSmallAds())
            QRScannerApplication.getInstance().loadReviewLargeView(viewAds?.getLargeAds())
        }
    }

    private val pickForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val mFile = create?.uuId?.findImageName()
            if (mFile!=null){
                Utils.Log(TAG, "Response data")
                mUri = FileProvider.getUriForFile(this@ReviewActivity, BuildConfig.APPLICATION_ID + ".provider", mFile)
                bitmap = BitmapFactory.decodeFile(mFile.absolutePath)
                binding.imgResult.setImageURI(null)
                binding.imgResult.setImageURI(mFile.toUri())
            }
        }
    }

    fun onOpenChangeDesign() {
        pickForResult.launch(Navigator.onResultView(this,create,ChangeDesignActivity::class.java))
    }

    fun mergeUUID() {
        if (create?.enumImplement == EnumImplement.CREATE){
            create?.uuId = save.uuId
        }
        else if (create?.enumImplement == EnumImplement.EDIT){
            viewModel.onDeleteChangeDesign(create)
        }
        else if (create?.enumImplement == EnumImplement.VIEW){
            Utils.Log(TAG,"Data change design . ${create?.toJson()}")
        }
    }

    companion object {
        protected val TAG = ReviewActivity::class.java.simpleName
    }
}