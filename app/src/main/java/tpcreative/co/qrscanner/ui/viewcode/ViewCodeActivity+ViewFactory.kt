package tpcreative.co.qrscanner.ui.viewcode

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.print.PrintHelper
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_result.toolbar
import kotlinx.android.synthetic.main.activity_view_code.*
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.model.Theme
import java.io.File
import java.io.FileOutputStream
import java.util.*


fun ViewCodeActivity.initUI(){
    TAG = this::class.java.name
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    setupViewModel()
    getDataIntent()
}

fun ViewCodeActivity.getDataIntent() {
    viewModel.getIntent(this).observe(this, Observer {
        Utils.Log(TAG,Gson().toJson(viewModel.result))
        setView()
    })
}

fun ViewCodeActivity.setView() {
    when (viewModel.result?.createType) {
        ParsedResultType.ADDRESSBOOK -> {
            code = "MECARD:N:" + viewModel.result?.fullName + ";TEL:" + viewModel.result?.phone + ";EMAIL:" + viewModel.result?.email + ";ADR:" + viewModel.result?.address + ";"
            onGenerateReview(code)
        }
        ParsedResultType.EMAIL_ADDRESS -> {
            code = "MATMSG:TO:" + viewModel.result?.email + ";SUB:" + viewModel.result?.subject + ";BODY:" + viewModel.result?.message + ";"
            onGenerateReview(code)
        }
        ParsedResultType.PRODUCT -> {
            code = viewModel.result?.productId
            onGenerateReview(code)
        }
        ParsedResultType.URI -> {
            code = viewModel.result?.url
            onGenerateReview(code)
        }
        ParsedResultType.WIFI -> {
            code = "WIFI:S:" + viewModel.result?.ssId + ";T:" + viewModel.result?.networkEncryption + ";P:" + viewModel.result?.password + ";H:" + viewModel.result?.hidden + ";"
            onGenerateReview(code)
        }
        ParsedResultType.GEO -> {
            code = "geo:" + viewModel.result?.lat + "," + viewModel.result?.lon + "?q=" + viewModel.result?.query + ""
            onGenerateReview(code)
        }
        ParsedResultType.TEL -> {
            code = "tel:" + viewModel.result?.phone + ""
            onGenerateReview(code)
        }
        ParsedResultType.SMS -> {
            code = "smsto:" + viewModel.result?.phone + ":" + viewModel.result?.message
            onGenerateReview(code)
        }
        ParsedResultType.CALENDAR -> {
            val builder = StringBuilder()
            builder.append("BEGIN:VEVENT")
            builder.append("\n")
            builder.append("SUMMARY:" + viewModel.result?.title)
            builder.append("\n")
            builder.append("DTSTART:" + viewModel.result?.startEvent)
            builder.append("\n")
            builder.append("DTEND:" + viewModel.result?.endEvent)
            builder.append("\n")
            builder.append("LOCATION:" + viewModel.result?.location)
            builder.append("\n")
            builder.append("DESCRIPTION:" + viewModel.result?.description)
            builder.append("\n")
            builder.append("END:VEVENT")
            code = builder.toString()
            onGenerateReview(code)
        }
        ParsedResultType.ISBN -> {
        }
        else -> {
            code = viewModel.result?.text
            onGenerateReview(code)
        }
    }
}

private fun ViewCodeActivity.onGenerateReview(code: String?) {
    try {
        val barcodeEncoder = BarcodeEncoder()
        val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 2
        val theme: Theme? = Theme.getInstance()?.getThemeInfo()
        Utils.Log(TAG, "barcode====================> " + code + "--" + viewModel.result?.createType?.name)
        bitmap = if (viewModel.result?.createType == ParsedResultType.PRODUCT) {
            barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?:0, code, BarcodeFormat.valueOf(viewModel.result?.barcodeFormat ?: ""), 200, 200, hints)
        } else {
            barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?: 0, code, BarcodeFormat.QR_CODE, 200, 200, hints)
        }
        imgViewCode.setImageBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ViewCodeActivity.onPhotoPrint() {
    try {
        val photoPrinter = PrintHelper(this)
        photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
        Utils.getCurrentDate()?.let { bitmap?.let { it1 -> photoPrinter.printBitmap(it, it1) } }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ViewCodeActivity.shareToSocial(value : Uri) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_STREAM,value)
    intent.clipData = ClipData.newRawUri("", value);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(intent, "Share"))
}

fun ViewCodeActivity.getImageUri(): Uri? {
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

private fun ViewCodeActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    )[ViewCodeViewModel::class.java]
}