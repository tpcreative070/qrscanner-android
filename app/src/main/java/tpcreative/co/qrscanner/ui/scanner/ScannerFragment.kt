package tpcreative.co.qrscanner.ui.scanner

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.result.*
import com.isseiaoki.simplecropview.CropImageView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview.StateListener
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ScannerSingleton.SingletonScannerListener
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.extension.parcelable
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.common.view.crop.Crop.Companion.getImagePicker
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel
import java.io.File


class ScannerFragment : BaseFragment(), SingletonScannerListener{
    lateinit var viewModel : ScannerViewModel
    var beepManager: BeepManager? = null
    val cameraSettings: CameraSettings = CameraSettings()
    var typeCamera = 0
    var isTurnOnFlash = false
    var mAnim: Animation? = null
    var isRunning = false
    var mFrameRect: RectF? = null
    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            try {
                Utils.Log(TAG, "Call back :" + result?.text + "  type :" + result?.barcodeFormat?.name)
                if (activity == null) {
                    return
                }
                val parsedResult = ResultParser.parseResult(result?.result)
                val create = CreateModel()
                var address: String? = ""
                var fullName: String? = ""
                var email: String? = ""
                var phone: String? = ""
                var subject = ""
                var message = ""
                var url = ""
                var ssId = ""
                var networkEncryption = ""
                var password = ""
                var lat = 0.0
                var lon = 0.0
                var startEventMilliseconds: Long = 0
                var endEventMilliseconds: Long = 0
                var query: String? = ""
                var title = ""
                var location = ""
                var description = ""
                var startEvent: String? = ""
                var endEvent: String? = ""
                var text = ""
                var productId = ""
                var ISBN = ""
                var hidden = false
                Utils.Log(TAG, "Type response " + parsedResult.type)
                when (parsedResult.type) {
                    ParsedResultType.ADDRESSBOOK -> {
                        create.createType = ParsedResultType.ADDRESSBOOK
                        val addressResult = parsedResult as AddressBookParsedResult
                        address = Utils.convertStringArrayToString(addressResult.addresses, ",")
                        fullName = Utils.convertStringArrayToString(addressResult.names, ",")
                        email = Utils.convertStringArrayToString(addressResult.emails, ",")
                        phone = Utils.convertStringArrayToString(addressResult.phoneNumbers, ",")
                    }
                    ParsedResultType.EMAIL_ADDRESS -> {
                        create.createType = ParsedResultType.EMAIL_ADDRESS
                        val emailAddress = parsedResult as EmailAddressParsedResult
                        email = Utils.convertStringArrayToString(emailAddress.tos, ",")
                        subject = if (emailAddress.subject == null) "" else emailAddress.subject
                        message = if (emailAddress.body == null) "" else emailAddress.body
                    }
                    ParsedResultType.PRODUCT -> {
                        create.createType = ParsedResultType.PRODUCT
                        val productResult = parsedResult as ProductParsedResult
                        productId = if (productResult.productID == null) "" else productResult.productID
                        Utils.Log(TAG, "Product " + Gson().toJson(productResult))
                    }
                    ParsedResultType.URI -> {
                        create.createType = ParsedResultType.URI
                        val urlResult = parsedResult as URIParsedResult
                        url = if (urlResult.uri == null) "" else urlResult.uri
                    }
                    ParsedResultType.WIFI -> {
                        create.createType = ParsedResultType.WIFI
                        val wifiResult = parsedResult as WifiParsedResult
                        hidden = wifiResult.isHidden
                        ssId = if (wifiResult.ssid == null) "" else wifiResult.ssid
                        networkEncryption = if (wifiResult.networkEncryption == null) "" else wifiResult.networkEncryption
                        password = if (wifiResult.password == null) "" else wifiResult.password
                        Utils.Log(TAG, "method : " + wifiResult.networkEncryption + " :" + wifiResult.phase2Method + " :" + wifiResult.password)
                    }
                    ParsedResultType.GEO -> {
                        create.createType = ParsedResultType.GEO
                        try {
                            val geoParsedResult = parsedResult as GeoParsedResult
                            lat = geoParsedResult.latitude
                            lon = geoParsedResult.longitude
                            query = geoParsedResult.query
                            val strNew = query.replace("q=", "")
                            query = strNew
                        } catch (e: Exception) {
                        }
                    }
                    ParsedResultType.TEL -> {
                        create.createType = ParsedResultType.TEL
                        val telParsedResult = parsedResult as TelParsedResult
                        phone = telParsedResult.number
                    }
                    ParsedResultType.SMS -> {
                        create.createType = ParsedResultType.SMS
                        val smsParsedResult = parsedResult as SMSParsedResult
                        phone = Utils.convertStringArrayToString(smsParsedResult.numbers, ",")
                        message = if (smsParsedResult.body == null) "" else smsParsedResult.body
                    }
                    ParsedResultType.CALENDAR -> {
                        create.createType = ParsedResultType.CALENDAR
                        val calendarParsedResult = parsedResult as CalendarParsedResult
                        val startTime = Utils.getCurrentDatetimeEvent(calendarParsedResult.startTimestamp)
                        val endTime = Utils.getCurrentDatetimeEvent(calendarParsedResult.endTimestamp)
                        title = if (calendarParsedResult.summary == null) "" else calendarParsedResult.summary
                        description = if (calendarParsedResult.description == null) "" else calendarParsedResult.description
                        location = if (calendarParsedResult.location == null) "" else calendarParsedResult.location
                        startEvent = startTime
                        endEvent = endTime
                        startEventMilliseconds = calendarParsedResult.startTimestamp
                        endEventMilliseconds = calendarParsedResult.endTimestamp
                        Utils.Log(TAG, "$startTime : $endTime")
                    }
                    ParsedResultType.ISBN -> {
                        create.createType = ParsedResultType.ISBN
                        val isbParsedResult = parsedResult as ISBNParsedResult
                        ISBN = if (isbParsedResult.isbn == null) "" else isbParsedResult.isbn
                        Utils.Log(TAG, "Result filter " + Gson().toJson(isbParsedResult))
                    }
                    else -> try {
                        Utils.Log(TAG, "Default value")
                        create.createType = ParsedResultType.TEXT
                        val textParsedResult = parsedResult as TextParsedResult
                        text = if (textParsedResult.text == null) "" else textParsedResult.text
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                create.address = address
                create.fullName = fullName
                create.email = email
                create.phone = phone
                create.subject = subject
                create.message = message
                create.url = url
                create.hidden = hidden
                create.ssId = ssId
                create.networkEncryption = networkEncryption
                create.password = password
                create.lat = lat
                create.lon = lon
                create.query = query
                create.title = title
                create.location = location
                create.description = description
                create.startEvent = startEvent
                create.endEvent = endEvent
                create.startEventMilliseconds = startEventMilliseconds
                create.endEventMilliseconds = endEventMilliseconds
                create.text = text
                create.productId = productId
                create.ISBN = ISBN

                /*Adding new columns*/create.barcodeFormat = BarcodeFormat.QR_CODE.name
                create.favorite = false
                if (result?.barcodeFormat != null) {
                    create.barcodeFormat = result.barcodeFormat.name
                }
                doNavigation(create)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun doNavigation(create: CreateModel?) {
            if (Utils.isMultipleScan()) {
                btnDone.visibility = View.VISIBLE
                tvCount.visibility = View.VISIBLE
                updateValue(1)
                viewModel.doSaveItems(create)
                if (zxing_barcode_scanner != null) {
                    zxing_barcode_scanner.pauseAndWait()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        zxing_barcode_scanner.resume()
                    }
                }
            } else {
                scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
                if (zxing_barcode_scanner != null) {
                    zxing_barcode_scanner.pauseAndWait()
                }
            }
            beepManager?.playBeepSoundAndVibrate()
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint?>?) {}
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_scanner, viewGroup, false)
    }

    override fun work() {
        super.work()
        initUI()
        ScannerSingleton.getInstance()?.setListener(this)
        zxing_barcode_scanner.decodeContinuous(callback)
        zxing_barcode_scanner.statusView.visibility = View.GONE
        imgCreate.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        imgGallery.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        switch_camera.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        typeCamera = if (Utils.checkCameraBack(context)) {
            cameraSettings.requestedCameraId = Constant.CAMERA_FACING_BACK
            0
        } else {
            if (Utils.checkCameraFront(context)) {
                cameraSettings.requestedCameraId = Constant.CAMERA_FACING_FRONT
                1
            } else {
                2
            }
        }
        zxing_barcode_scanner.barcodeView.cameraSettings = cameraSettings
        beepManager = BeepManager(activity)
        onHandlerIntent()
        if (zxing_barcode_scanner != null) {
            if (!zxing_barcode_scanner.isActivated) {
                zxing_barcode_scanner.resume()
                zxing_barcode_scanner.barcodeView.addStateListener(stateListener)
            }
        }
        onBeepAndVibrate()
    }

    fun switchCamera(type: Int) {
        if (typeCamera == 2) {
            return
        }
        cameraSettings.requestedCameraId = type // front/back/etc
        zxing_barcode_scanner.barcodeView.cameraSettings = cameraSettings
        zxing_barcode_scanner.resume()
    }

    fun onAddPermissionGallery() {
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.pauseAndWait()
        }
        onGetGallery()
    }

    private fun onBeepAndVibrate() {
        if (beepManager == null) {
            return
        }
        val isBeep = PrefsController.getBoolean(getString(R.string.key_beep), false)
        val isVibrate = PrefsController.getBoolean(getString(R.string.key_vibrate), false)
        beepManager?.isBeepEnabled = isBeep
        beepManager?.isVibrateEnabled = isVibrate
    }

    override fun setVisible() {
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.resume()
        }
    }

    override fun setInvisible() {
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.pauseAndWait()
        }
    }

    override fun onStop() {
        super.onStop()
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.pauseAndWait()
        }
        Utils.Log(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        if (!isRunning) {
            ResponseSingleton.getInstance()?.setScannerPosition()
            isRunning = true
        }
        ResponseSingleton.getInstance()?.onResumeAds()
        Utils.Log(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (typeCamera != 2) {
            if (zxing_barcode_scanner != null) {
                zxing_barcode_scanner.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
         if (zxing_barcode_scanner != null && !zxing_barcode_scanner.isActivated) {
             zxing_barcode_scanner.resume()
         }
        Utils.Log(TAG, "onResume")
    }

    private val scanForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            setVisible()
        }
    }

    private val pickGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_PICK")
            beginCrop(result.data?.data)
        }
    }

    private val cropForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_CROP")
            handleCrop(result.resultCode, result.data)
        }
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(activity?.cacheDir, "cropped"))
        cropForResult.launch(Crop.of(source, destination)?.asSquare()?.start(requireContext()))
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: String? = Crop.getOutputString(result)
            val mResult = Gson().fromJson(mData, Result::class.java)
            mResult?.let { onFilterResult(it) }
            Utils.Log(TAG, "Result of cropped " + Gson().toJson(mResult))
        } else if (resultCode == Crop.RESULT_ERROR) {
            Utils.onAlertNotify(requireActivity(),"${Crop.getError(result)?.message}")
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setVisible()
        }
    }

    private fun onGetGallery() {
        pickGalleryForResult.launch(getImagePicker())
    }

    private fun onFilterResult(result: Result?) {
        if (activity == null) {
            return
        }
        val parsedResult = ResultParser.parseResult(result)
        val create = CreateModel()
        var address: String? = ""
        var fullName: String? = ""
        var email: String? = ""
        var phone: String? = ""
        var subject = ""
        var message = ""
        var url = ""
        var ssId = ""
        var networkEncryption = ""
        var password = ""
        var productId = ""
        var ISBN = ""
        var lat = 0.0
        var lon = 0.0
        var startEventMilliseconds: Long = 0
        var endEventMilliseconds: Long = 0
        var query: String? = ""
        var title = ""
        var location = ""
        var description = ""
        var startEvent: String? = ""
        var endEvent: String? = ""
        var text = ""
        var hidden = false
        Utils.Log(TAG, "Type " + parsedResult.type.name)
        when (parsedResult.type) {
            ParsedResultType.ADDRESSBOOK -> {
                create.createType = ParsedResultType.ADDRESSBOOK
                val addressResult = parsedResult as AddressBookParsedResult
                address = Utils.convertStringArrayToString(addressResult.addresses, ",")
                fullName = Utils.convertStringArrayToString(addressResult.names, ",")
                email = Utils.convertStringArrayToString(addressResult.emails, ",")
                phone = Utils.convertStringArrayToString(addressResult.phoneNumbers, ",")
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                create.createType = ParsedResultType.EMAIL_ADDRESS
                val emailAddress = parsedResult as EmailAddressParsedResult
                email = Utils.convertStringArrayToString(emailAddress.tos, ",")
                subject = if (emailAddress.subject == null) "" else emailAddress.subject
                message = if (emailAddress.body == null) "" else emailAddress.body
            }
            ParsedResultType.PRODUCT -> {
                create.createType = ParsedResultType.PRODUCT
                val productResult = parsedResult as ProductParsedResult
                productId = if (productResult.productID == null) "" else productResult.productID
            }
            ParsedResultType.URI -> {
                create.createType = ParsedResultType.URI
                val urlResult = parsedResult as URIParsedResult
                url = if (urlResult.uri == null) "" else urlResult.uri
            }
            ParsedResultType.WIFI -> {
                create.createType = ParsedResultType.WIFI
                val wifiResult = parsedResult as WifiParsedResult
                hidden = wifiResult.isHidden
                ssId = if (wifiResult.ssid == null) "" else wifiResult.ssid
                networkEncryption = if (wifiResult.networkEncryption == null) "" else wifiResult.networkEncryption
                password = if (wifiResult.password == null) "" else wifiResult.password
                Utils.Log(TAG, "method : " + wifiResult.networkEncryption + " :" + wifiResult.phase2Method + " :" + wifiResult.password)
            }
            ParsedResultType.GEO -> {
                create.createType = ParsedResultType.GEO
                try {
                    val geoParsedResult = parsedResult as GeoParsedResult
                    lat = geoParsedResult.latitude
                    lon = geoParsedResult.longitude
                    query = geoParsedResult.query
                    val strNew = query.replace("q=", "")
                    query = strNew
                } catch (e: Exception) {
                }
            }
            ParsedResultType.TEL -> {
                create.createType = ParsedResultType.TEL
                val telParsedResult = parsedResult as TelParsedResult
                phone = telParsedResult.number
            }
            ParsedResultType.SMS -> {
                create.createType = ParsedResultType.SMS
                val smsParsedResult = parsedResult as SMSParsedResult
                phone = Utils.convertStringArrayToString(smsParsedResult.numbers, ",")
                message = if (smsParsedResult.body == null) "" else smsParsedResult.body
            }
            ParsedResultType.CALENDAR -> {
                create.createType = ParsedResultType.CALENDAR
                val calendarParsedResult = parsedResult as CalendarParsedResult
                val startTime = Utils.getCurrentDatetimeEvent(calendarParsedResult.startTimestamp)
                val endTime = Utils.getCurrentDatetimeEvent(calendarParsedResult.endTimestamp)
                title = if (calendarParsedResult.summary == null) "" else calendarParsedResult.summary
                description = if (calendarParsedResult.description == null) "" else calendarParsedResult.description
                location = if (calendarParsedResult.location == null) "" else calendarParsedResult.location
                startEvent = startTime
                endEvent = endTime
                startEventMilliseconds = calendarParsedResult.startTimestamp
                endEventMilliseconds = calendarParsedResult.endTimestamp
                Utils.Log(TAG, "$startTime : $endTime")
            }
            ParsedResultType.ISBN -> {
                create.createType = ParsedResultType.ISBN
                val isbParsedResult = parsedResult as ISBNParsedResult
                ISBN = if (isbParsedResult.isbn == null) "" else isbParsedResult.isbn
                Utils.Log(TAG, "Result filter " + Gson().toJson(isbParsedResult))
            }
            else -> {
                create.createType = ParsedResultType.TEXT
                val textParsedResult = parsedResult as TextParsedResult
                text = if (textParsedResult.text == null) "" else textParsedResult.text
            }
        }
        create.address = address
        create.fullName = fullName
        create.email = email
        create.phone = phone
        create.subject = subject
        create.message = message
        create.url = url
        create.hidden = hidden
        create.ssId = ssId
        create.networkEncryption = networkEncryption
        create.password = password
        create.lat = lat
        create.lon = lon
        create.query = query
        create.title = title
        create.location = location
        create.description = description
        create.startEvent = startEvent
        create.endEvent = endEvent
        create.startEventMilliseconds = startEventMilliseconds
        create.endEventMilliseconds = endEventMilliseconds
        create.text = text
        create.productId = productId
        create.ISBN = ISBN
        create.fragmentType = EnumFragmentType.SCANNER
        create.barcodeFormat = result?.barcodeFormat?.name
        beepManager?.playBeepSoundAndVibrate()
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.pauseAndWait()
        }
        Utils.Log(TAG,"barcode format ${parsedResult.type}")
        scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
            Utils.Log(TAG, "isVisible")
        } else {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
            Utils.Log(TAG, "isInVisible")
        }
        if (zxing_barcode_scanner != null) {
            if (menuVisible) {
                if (typeCamera != 2) {
                    onBeepAndVibrate()
                    zxing_barcode_scanner.resume()
                    Utils.Log(TAG, "Fragment visit...resume...")
                }
            } else {
                if (typeCamera != 2) {
                    zxing_barcode_scanner.pause()
                }
            }
        }
        Utils.Log(TAG, "Fragment visit...$menuVisible")
    }

    override fun onPause() {
        super.onPause()
    }

    /*Share File To QRScanner*/
    private fun onHandlerIntent() {
        try {
            val intent = activity?.intent
            val action = intent?.action
            val type = intent?.type
            Utils.Log(TAG, "original type :$type")
            if (Intent.ACTION_SEND == action && type != null) {
                handleSendSingleItem(intent)
            }
        } catch (e: Exception) {
            Utils.onDropDownAlert(activity, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
    }

    private fun handleSendSingleItem(intent: Intent?) {
        try {
            val imageUri = intent?.parcelable<Parcelable>(Intent.EXTRA_STREAM) as Uri?
            if (imageUri != null) {
                beginCrop(imageUri)
            } else {
                Utils.onDropDownAlert(activity, getString(R.string.can_not_support_this_format))
            }
        } catch (e: Exception) {
            Utils.onDropDownAlert(activity, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ScannerFragment::class.java.simpleName
        fun newInstance(index: Int): ScannerFragment {
            val fragment = ScannerFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}