package tpcreative.co.qrscanner.ui.review

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.getDisplay
import tpcreative.co.qrscanner.common.extension.onGeneralParse
import tpcreative.co.qrscanner.common.extension.onTranslateCreateType
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        initUI()
        dialog = ProgressDialog.progressDialog(this,R.string.waiting_for_export.toText())
    }

    fun onCatch() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        checkingShowAds()
    }

    override fun onPause() {
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
            txtSubject.text = type
            txtDisplay.text = code
            txtFormat.text = format
            CoroutineScope(Dispatchers.IO).launch {
                onGenerateReview(code)
                onGenerateQRCode(code)
                onDrawOnBitmap(Utils.getDisplay(GeneralModel(save))?:"",Utils.onTranslateCreateType(it
                    .createType ?: ParsedResultType.TEXT), BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))
            }
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
        if (create?.enumImplement == EnumImplement.CREATE) {
            val time = Utils.getCurrentDateTimeSort()
            save.createDatetime = time
            save.updatedDateTime = time
            Utils.Log(TAG, "Questing created")
            Utils.Log(TAG,"Questing created ${Gson().toJson(save)}")
            SQLiteHelper.onInsert(save)
        } else if (create?.enumImplement == EnumImplement.EDIT) {
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
        } else if (create?.enumImplement == EnumImplement.VIEW) {
            Utils.Log(TAG, "Questing view")
        }
        GenerateSingleton.getInstance()?.onCompletedGenerate()
    }

    suspend fun onGenerateReview(code: String?) =
        withContext(Dispatchers.Main) {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> =
                    EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)

                val theme: Theme? = Theme.getInstance()?.getThemeInfo()
                Utils.Log(TAG, "barcode====================> " + code + "--" + create?.createType?.name)
                val mBitmap = if ((BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))) {
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
                imgResult.setImageBitmap(mBitmap)
                onSavedData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    suspend fun onGenerateQRCode(code: String?) =
        withContext(Dispatchers.IO) {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> =
                    EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
                val theme: Theme? = Theme.getInstance()?.getThemeInfo()
                Utils.Log(TAG, "barcode====================> " + code + "--" + create?.createType?.name)
                bitmap = if (BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name))  {
                    hints[EncodeHintType.MARGIN] = 15
                    var mFormatCode =  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name)
                    if(mFormatCode == BarcodeFormat.RSS_14){
                        mFormatCode = BarcodeFormat.CODABAR
                    }
                    var mWidth = Constant.QRCodeExportWidth + 150
                    var mHeight = Constant.QRCodeExportHeight - 200
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
            QRScannerApplication.getInstance().loadReviewSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadReviewLargeView(llLargeAds)
        }
    }

    companion object {
        protected val TAG = ReviewActivity::class.java.simpleName
    }
}