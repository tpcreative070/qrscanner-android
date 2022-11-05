package tpcreative.co.qrscanner.ui.review
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.print.PrintHelper
import kotlinx.android.synthetic.main.activity_review.scrollView
import kotlinx.android.synthetic.main.activity_review.toolbar
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
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


fun ReviewActivity.getIntentData(){
    viewModel.getIntent(this).observe(this, Observer {
        if (it){
            setView()
        }else{
            onCatch()
        }
    })
}

