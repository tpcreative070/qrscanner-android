package tpcreative.co.qrscanner.ui.review

import android.content.ClipData
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Patterns
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.print.PrintHelper
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.HistoryModel
import java.io.File
import java.io.FileOutputStream


fun ReviewActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    getIntentData()
    if (QRScannerApplication.getInstance().isReviewSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableReviewSmallView()) {
        QRScannerApplication.getInstance().requestReviewSmallView(this)
    }
    if (QRScannerApplication.getInstance().isReviewLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableReviewLargeView()) {
        QRScannerApplication.getInstance().requestReviewLargeView(this)
    }
    checkingShowAds()
    /*Press back button*/
    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            finish()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })
    }
    onHandlerIntent()
}

/*Share File To QRScanner*/
private fun ReviewActivity.onHandlerIntent() {
    try {
        val intent = intent
        val action = intent?.action
        val type = intent?.type
        Utils.Log(TAG, "original type :$type")
        if (Intent.ACTION_SEND == action && Constant.textType == intent.type) {
            val message : String? = intent.getStringExtra(Intent.EXTRA_TEXT);
            val subject : String? = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            txtSubject.text = subject
            txtDisplay.text = message
            Utils.Log(TAG,"intent result")
            onSaveFromTextOrCVFToQRCode("$message",null)
        }
        else if (Intent.ACTION_SEND == action && Constant.cvfType == intent.type){
            val fileUri = intent.parcelable<Parcelable>(Intent.EXTRA_STREAM) as Uri?
            if (fileUri != null) {
                fileUri.let {
                    val mSave = Utils.readVCF(it)
                    txtSubject.text = "vCard"
                    txtDisplay.text = mSave?.code
                    onSaveFromTextOrCVFToQRCode("",mSave)
                    Utils.Log(TAG,"vCard result value ${Gson().toJson(mSave)}")
                }
            } else {
                Utils.onDropDownAlert(this, getString(R.string.can_not_support_this_format))
            }
        }
    } catch (e: Exception) {
        Utils.onDropDownAlert(this, getString(R.string.error_occurred_importing))
        e.printStackTrace()
    }
}

private fun ReviewActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(ReviewViewModel::class.java)
}

fun ReviewActivity.shareToSocial() {
    if (mUri == null){
        return
    }
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_STREAM,mUri)
    intent.clipData = ClipData.newRawUri("", mUri);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(intent, "Share"))
    isRequestExportPNG = false
}

suspend fun ReviewActivity.getImageUri(bitmap : Bitmap?) = withContext(Dispatchers.IO) {
    val imageFolder = File(cacheDir, Constant.images_folder)
    var uri: Uri? = null
    try {
        imageFolder.mkdirs()
        val file = File(imageFolder, "shared_code_${System.currentTimeMillis()}.png")
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.PNG, 30, outputStream)
        outputStream.flush()
        outputStream.close()
        uri = FileProvider.getUriForFile(this@getImageUri, BuildConfig.APPLICATION_ID + ".provider", file)
        mUri = uri
        if (isRequestExportPNG){
            shareToSocial()
        }
    } catch (e: java.lang.Exception) {
        Toast.makeText(this@getImageUri, "" + e.message, Toast.LENGTH_LONG).show()
    }
}

fun ReviewActivity.onPhotoPrint() {
    try {
        if (bitmap == null){
            return
        }
        if (!processDrawnDone){
            return
        }
        val photoPrinter = PrintHelper(this)
        photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
        Utils.getCurrentDate()?.let { bitmap?.let { it1 -> photoPrinter.printBitmap(it, it1) } }
        isRequestPrint = false
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ReviewActivity.onSaveFromTextOrCVFToQRCode(text : String, mSave : GeneralModel?){
    val history = HistoryModel()
    viewModel.isSharedIntent = true
    if (mSave!=null){
        code = mSave.code
        history.fullName = mSave.fullName
        history.phone = mSave.phone
        history.email = mSave.email
        history.address = mSave.address
        history.code = mSave.code
        history.createType = ParsedResultType.ADDRESSBOOK.name
    }else{
        if (Patterns.WEB_URL.matcher(text).matches()){
            code = text
            history.url = text
            history.code = text
            history.createType = ParsedResultType.URI.name
        }else{
            code = text
            history.textProductIdISNB = text
            history.code = text
            history.createType = ParsedResultType.TEXT.name
        }
    }

    history.favorite = false
    history.barcodeFormat = BarcodeFormat.QR_CODE.name
    format = BarcodeFormat.QR_CODE.name
    val time = Utils.getCurrentDateTimeSort()
    history.createDatetime = time
    history.updatedDateTime = time
    txtFormat.text = format
    SQLiteHelper.onInsert(history)
    viewModel.updateId(history.uuId)
    CoroutineScope(Dispatchers.Main).launch {
        onGenerateReview(code)
        onGenerateQRCode(code)
        onDrawOnBitmap(Utils.getDisplay(GeneralModel(history)) ?:"",Utils.onTranslateCreateType(ParsedResultType.valueOf(history.createType?:ParsedResultType.TEXT.name)),
            BarcodeFormat.valueOf(history.barcodeFormat ?: BarcodeFormat.QR_CODE.name))
    }
}

fun ReviewActivity.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

fun ReviewActivity.getIntentData(){
    viewModel.getIntent(this).observe(this, Observer {
        if (it){
            setView()
        }else{
            onCatch()
        }
    })
}

suspend fun ReviewActivity.onDrawOnBitmap(mValue  :String,mType : String,format: BarcodeFormat) = withContext(Dispatchers.IO){
     bitmap?.let {data ->
        var mBm = data.addPaddingLeftForBitmap(50)
        mBm = mBm?.addPaddingTopForBitmap(80)
        mBm = mBm?.addPaddingRightForBitmap(50)
        mBm = mBm?.addPaddingBottomForBitmap(80)
        mBm?.let {
            val canvas = Canvas(it)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
            paint.isLinearText = true
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.color = ContextCompat.getColor(this@onDrawOnBitmap, R.color.colorAccent) // Text Color
            paint.textSize = 50F // Text Size
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern
            val mRectF = RectF(0F, 0F, it.width.toFloat(),it.height.toFloat())
            if (BarcodeFormat.QR_CODE != format && !viewModel.isSharedIntent){
                canvas.drawText(mType, (canvas.width /2).toFloat(), mRectF.top + 52 , paint)
                canvas.drawText(mValue, (canvas.width /2).toFloat(), mRectF.bottom - 22 , paint)
            }else{
                canvas.drawText(mType, (canvas.width /2).toFloat(), mRectF.top + 70 , paint)
                canvas.drawText(mValue, (canvas.width /2).toFloat(), mRectF.bottom - 42 , paint)
            }
            Utils.Log(TAG,"Rect ${mRectF.centerY()} ${mRectF.bottom}")
            bitmap = it
            processDrawnDone = true
            if (isRequestPrint){
                onPhotoPrint()
            }
        }
    }
}

