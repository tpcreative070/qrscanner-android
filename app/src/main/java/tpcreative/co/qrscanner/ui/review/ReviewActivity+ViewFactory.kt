package tpcreative.co.qrscanner.ui.review
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.print.PrintHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_review.*
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.ui.scannerresult.initUI
import tpcreative.co.qrscanner.ui.scannerresult.showAds
import java.io.File
import java.io.FileOutputStream

fun ReviewActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    getIntentData()
    /*Press back button*/
    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            //showAds()
            finish()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //showAds()
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
            onSaveQRCode("$message")
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

fun ReviewActivity.shareToSocial(value : Uri) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_STREAM,value)
    intent.clipData = ClipData.newRawUri("", value);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(intent, "Share"))
}

fun ReviewActivity.getImageUri(): Uri? {
    val imagefolder = File(cacheDir, "images")
    var uri: Uri? = null
    try {
        imagefolder.mkdirs()
        val file = File(imagefolder, "shared_image.png")
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
    } catch (e: java.lang.Exception) {
        Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
    }
    return uri
}

fun ReviewActivity.onPhotoPrint() {
    try {
        val photoPrinter = PrintHelper(this)
        photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
        Utils.getCurrentDate()?.let { bitmap?.let { it1 -> photoPrinter.printBitmap(it, it1) } }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ReviewActivity.onSaveQRCode(text : String){
    val history = HistoryModel()
    if (Patterns.WEB_URL.matcher(text).matches()){
        code = text
        history.url = text
        history.createType = ParsedResultType.URI.name
    }else{
        code = text
        history.text = text
        history.createType = ParsedResultType.TEXT.name
    }
    history.favorite = false
    history.barcodeFormat = BarcodeFormat.QR_CODE.name
    format = BarcodeFormat.QR_CODE.name
    val time = Utils.getCurrentDateTimeSort()
    history.createDatetime = time
    history.updatedDateTime = time
    txtFormat.text = format
    SQLiteHelper.onInsert(history)
    onGenerateReview(code)
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

