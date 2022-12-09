package tpcreative.co.qrscanner.ui.review

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.coroutines.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.GenerateSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.onGeneralParse
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.util.*

class ReviewActivity : BaseActivitySlide() {
    lateinit var viewModel: ReviewViewModel
    var create: GeneralModel? = null
    var bitmap: Bitmap? = null
    var code: String? = null
    var type: String? = null
    var format: String? = null
    var mUri : Uri? = null
    private var save: SaveModel = SaveModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        initUI()
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
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_review, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_item_png_export -> {
                mUri?.let { shareToSocial(it) }
                return true
            }
            R.id.menu_item_print -> {
                onPhotoPrint()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSavedData() {
        if (save.createType !== ParsedResultType.PRODUCT.name) {
            save.barcodeFormat = BarcodeFormat.QR_CODE.name
        }
        save.favorite = false
        if (create?.enumImplement == EnumImplement.CREATE) {
            val time = Utils.getCurrentDateTimeSort()
            save.createdDatetime = time
            save.updatedDateTime = time
            Utils.Log(TAG, "Questing created")
            Utils.Log(TAG,"Questing created ${Gson().toJson(save)}")
            SQLiteHelper.onInsert(save)
        } else if (create?.enumImplement == EnumImplement.EDIT) {
            val time = Utils.getCurrentDateTimeSort()
            save.updatedDateTime = time
            save.createdDatetime = create?.createdDateTime
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
                val mBitmap = if ((BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name)) && !viewModel.isSharedIntent) {
                    hints[EncodeHintType.MARGIN] = 5
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name),
                        Constant.QRCodeViewWidth + 100,
                        Constant.QRCodeViewHeight - 100,
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
                bitmap = if (BarcodeFormat.QR_CODE !=  BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name) && !viewModel.isSharedIntent)  {
                    hints[EncodeHintType.MARGIN] = 15
                    barcodeEncoder.encodeBitmap(
                        this@ReviewActivity,
                        theme?.getPrimaryDarkColor() ?: 0,
                        code,
                        BarcodeFormat.valueOf(create?.barcodeFormat ?: BarcodeFormat.QR_CODE.name),
                        Constant.QRCodeExportWidth + 150,
                        Constant.QRCodeExportHeight - 200,
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