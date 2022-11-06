package tpcreative.co.qrscanner.ui.review
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_review.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.GenerateSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.util.*

class ReviewActivity : BaseActivitySlide() {
    lateinit var viewModel : ReviewViewModel
    private var create: CreateModel? = null
    var bitmap: Bitmap? = null
    var code: String? = null
    var type : String? = null
    var format : String? = null
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
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }

    fun setView() {
        create = viewModel.create
        format = viewModel.create.barcodeFormat
        when (create?.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                code = "MECARD:N:" + create?.fullName + ";TEL:" + create?.phone + ";EMAIL:" + create?.email + ";ADR:" + create?.address + ";"
                type = Constant.addressBook
                save = SaveModel()
                save.fullName = create?.fullName
                save.phone = create?.phone
                save.email = create?.email
                save.address = create?.address
                save.createType = create?.createType?.name
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                code = "MATMSG:TO:" + create?.email + ";SUB:" + create?.subject + ";BODY:" + create?.message + ";"
                type = Constant.email
                save = SaveModel()
                save.email = create?.email
                save.subject = create?.subject
                save.message = create?.message
                save.createType = create?.createType?.name
            }
            ParsedResultType.PRODUCT -> {
                code = create?.productId
                type = Constant.barCode
                save = SaveModel()
                save.text = create?.productId
                save.createType = create?.createType?.name
                save.barcodeFormat = create?.barcodeFormat
            }
            ParsedResultType.URI -> {
                code = create?.url
                type = Constant.webSite
                save = SaveModel()
                save.url = create?.url
                save.createType = create?.createType?.name
            }
            ParsedResultType.WIFI -> {
                code = "WIFI:S:" + create?.ssId + ";T:" + create?.networkEncryption + ";P:" + create?.password + ";H:" + create?.hidden + ";"
                type = Constant.wifi
                save = SaveModel()
                save.ssId = create?.ssId
                save.password = create?.password
                save.networkEncryption = create?.networkEncryption
                save.hidden = create?.hidden
                save.createType = create?.createType?.name
                Utils.Log(TAG, "wifi " + create?.networkEncryption)
            }
            ParsedResultType.GEO -> {
                code = "geo:" + create?.lat + "," + create?.lon + "?q=" + create?.query + ""
                type = Constant.location
                save = SaveModel()
                save.lat = create?.lat
                save.lon = create?.lon
                save.query = create?.query
                save.createType = create?.createType?.name
            }
            ParsedResultType.TEL -> {
                code = "tel:" + create?.phone + ""
                type = Constant.phoneNumber
                save = SaveModel()
                save.phone = create?.phone
                save.createType = create?.createType?.name
            }
            ParsedResultType.SMS -> {
                code = "smsto:" + create?.phone + ":" + create?.message
                type = Constant.sms
                save = SaveModel()
                save.phone = create?.phone
                save.message = create?.message
                save.createType = create?.createType?.name
            }
            ParsedResultType.CALENDAR -> {
                val builder = StringBuilder()
                builder.append("BEGIN:VEVENT")
                builder.append("\n")
                builder.append("SUMMARY:" + create?.title)
                builder.append("\n")
                builder.append("DTSTART:" + create?.startEvent)
                builder.append("\n")
                builder.append("DTEND:" + create?.endEvent)
                builder.append("\n")
                builder.append("LOCATION:" + create?.location)
                builder.append("\n")
                builder.append("DESCRIPTION:" + create?.description)
                builder.append("\n")
                builder.append("END:VEVENT")
                save = SaveModel()
                save.title = create?.title
                save.startEvent = create?.startEvent
                save.endEvent = create?.endEvent
                save.startEventMilliseconds = create?.startEventMilliseconds
                save.endEventMilliseconds = create?.endEventMilliseconds
                save.location = create?.location
                save.description = create?.description
                save.createType = create?.createType?.name
                code = builder.toString()
                type = Constant.calendar
            }
            ParsedResultType.ISBN -> {
            }
            else -> {
                code = create?.text
                type = Constant.text
                save = SaveModel()
                save.text = create?.text
                save.createType = create?.createType?.name
            }
        }
        txtSubject.text = type
        txtDisplay.text = code
        txtFormat.text = format
        onGenerateReview(code)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_review, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_png_export -> {
                getImageUri()?.let { shareToSocial(it) }
                return true
            }
            R.id.menu_item_print -> {
                onPhotoPrint()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSavedData(){
        if (save.createType !== ParsedResultType.PRODUCT.name) {
            save.barcodeFormat = BarcodeFormat.QR_CODE.name
        }
        save.favorite = false
        if (create?.enumImplement == EnumImplement.CREATE) {
            val time = Utils.getCurrentDateTimeSort()
            save.createDatetime = time
            save.updatedDateTime = time
            Utils.Log(TAG,"Questing created")
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
            Utils.Log(TAG,"Questing updated")
            SQLiteHelper.onUpdate(save, true)
        }else if(create?.enumImplement == EnumImplement.VIEW){
            Utils.Log(TAG,"Questing view")
        }
        GenerateSingleton.getInstance()?.onCompletedGenerate()
    }

    fun onGenerateReview(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme? = Theme.getInstance()?.getThemeInfo()
            Utils.Log(TAG, "barcode====================> " + code + "--" + create?.createType?.name)
            bitmap = if (create?.createType == ParsedResultType.PRODUCT) {
                barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?:0, code, BarcodeFormat.valueOf(create?.barcodeFormat ?: ""), Constant.QRCodeExportWidth, Constant.QRCodeExportHeight, hints)
            } else {
                barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?: 0, code, BarcodeFormat.QR_CODE, Constant.QRCodeExportWidth, Constant.QRCodeExportHeight, hints)
            }
            imgResult.setImageBitmap(bitmap)
            onSavedData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        protected val TAG = ReviewActivity::class.java.simpleName
    }
}