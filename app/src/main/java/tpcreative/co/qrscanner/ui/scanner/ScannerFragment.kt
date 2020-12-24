package tpcreative.co.qrscanner.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.result.*
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ScannerSingleton.SingletonScannerListener
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment
import java.io.File

class ScannerFragment : BaseFragment(), SingletonScannerListener, ScannerView {
    @BindView(R.id.zxing_status_view)
    var zxing_status_view: AppCompatTextView? = null

    @BindView(R.id.switch_flashlight)
    var switch_flashlight: AppCompatImageView? = null

    @BindView(R.id.imgGallery)
    var imgGallery: AppCompatImageView? = null

    @BindView(R.id.switch_camera)
    var switch_camera: AppCompatImageView? = null

    @BindView(R.id.imgCreate)
    var imgCreate: AppCompatImageView? = null

    @BindView(R.id.zxing_barcode_scanner)
    var barcodeScannerView: DecoratedBarcodeView? = null

    @BindView(R.id.btnDone)
    var btnDone: AppCompatButton? = null

    @BindView(R.id.tvCount)
    var tvCount: AppCompatTextView? = null
    private var beepManager: BeepManager? = null
    private val cameraSettings: CameraSettings? = CameraSettings()
    private var typeCamera = 0
    private var isTurnOnFlash = false
    private var mAnim: Animation? = null
    private var presenter: ScannerPresenter? = null
    private var isRunning = false
    private val callback: BarcodeCallback? = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            try {
                Utils.Log(TAG, "Call back :" + result.getText() + "  type :" + result.getBarcodeFormat().name)
                if (activity == null) {
                    return
                }
                // ResultHan resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), result.getResult());
                val parsedResult = ResultParser.parseResult(result.getResult())
                val create = Create()
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
                        if (addressResult != null) {
                            address = Utils.convertStringArrayToString(addressResult.addresses, ",")
                            fullName = Utils.convertStringArrayToString(addressResult.names, ",")
                            email = Utils.convertStringArrayToString(addressResult.emails, ",")
                            phone = Utils.convertStringArrayToString(addressResult.phoneNumbers, ",")
                        }
                    }
                    ParsedResultType.EMAIL_ADDRESS -> {
                        create.createType = ParsedResultType.EMAIL_ADDRESS
                        val emailAddress = parsedResult as EmailAddressParsedResult
                        if (emailAddress != null) {
                            email = Utils.convertStringArrayToString(emailAddress.tos, ",")
                            subject = if (emailAddress.subject == null) "" else emailAddress.subject
                            message = if (emailAddress.body == null) "" else emailAddress.body
                        }
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
                        if (urlResult != null) {
                            url = if (urlResult.uri == null) "" else urlResult.uri
                        }
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
                        val startTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.startTimestamp)
                        val endTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.endTimestamp)
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
                if (result.getBarcodeFormat() != null) {
                    create.barcodeFormat = result.getBarcodeFormat().name
                }
                doNavigation(create)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun doNavigation(create: Create?) {
            if (Utils.isMultipleScan()) {
                btnDone.setVisibility(View.VISIBLE)
                tvCount.setVisibility(View.VISIBLE)
                presenter.updateValue(1)
                presenter.doSaveItems(create)
                if (barcodeScannerView != null) {
                    barcodeScannerView.pauseAndWait()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        barcodeScannerView.resume()
                    }
                }
            } else {
                Navigator.onResultView(activity, create, ScannerResultFragment::class.java)
                if (barcodeScannerView != null) {
                    barcodeScannerView.pauseAndWait()
                }
            }
            beepManager.playBeepSoundAndVibrate()
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint?>?) {}
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater.inflate(R.layout.fragment_scanner, viewGroup, false)
    }

    override fun work() {
        super.work()
        ScannerSingleton.Companion.getInstance().setListener(this)
        presenter = ScannerPresenter()
        presenter.bindView(this)
        barcodeScannerView.decodeContinuous(callback)
        zxing_status_view.setVisibility(View.INVISIBLE)
        imgCreate.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        imgGallery.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        switch_camera.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        typeCamera = if (Utils.checkCameraBack(context)) {
            cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
            0
        } else {
            if (Utils.checkCameraFront(context)) {
                cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT)
                1
            } else {
                2
            }
        }
        barcodeScannerView.getBarcodeView().cameraSettings = cameraSettings
        beepManager = BeepManager(activity)
        onHandlerIntent()
        if (barcodeScannerView != null) {
            if (!barcodeScannerView.isActivated()) {
                barcodeScannerView.resume()
            }
        }
        onBeepAndVibrate()
    }

    fun switchCamera(type: Int) {
        if (typeCamera == 2) {
            return
        }
        cameraSettings.setRequestedCameraId(type) // front/back/etc
        barcodeScannerView.getBarcodeView().cameraSettings = cameraSettings
        barcodeScannerView.resume()
    }

    fun onAddPermissionGallery() {
        Dexter.withContext(activity)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            if (barcodeScannerView != null) {
                                barcodeScannerView.pauseAndWait()
                            }
                            onGetGallery()
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
    }

    fun onBeepAndVibrate() {
        if (beepManager == null) {
            return
        }
        val isBeep = PrefsController.getBoolean(getString(R.string.key_beep), false)
        val isVibrate = PrefsController.getBoolean(getString(R.string.key_vibrate), false)
        beepManager.setBeepEnabled(isBeep)
        beepManager.setVibrateEnabled(isVibrate)
    }

    override fun setVisible() {
        if (barcodeScannerView != null) {
            barcodeScannerView.resume()
        }
    }

    override fun setInvisible() {
        if (barcodeScannerView != null) {
            barcodeScannerView.pauseAndWait()
        }
    }

    @OnClick(R.id.switch_camera)
    fun switchCamera(view: View?) {
        Utils.Log(TAG, "on clicked here : " + cameraSettings.getRequestedCameraId())
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                barcodeScannerView.pauseAndWait()
                if (cameraSettings.getRequestedCameraId() == 0) {
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
                } else {
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    @OnClick(R.id.switch_flashlight)
    fun switchFlash(view: View?) {
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (isTurnOnFlash) {
                    barcodeScannerView.setTorchOff()
                    isTurnOnFlash = false
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.Companion.getInstance(), R.drawable.baseline_flash_off_white_48))
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
                } else {
                    barcodeScannerView.setTorchOn()
                    isTurnOnFlash = true
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.Companion.getInstance(), R.drawable.baseline_flash_on_white_48))
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    @OnClick(R.id.imgCreate)
    fun onClickCreate(view: View?) {
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (barcodeScannerView != null) {
                    barcodeScannerView.pauseAndWait()
                }
                Navigator.onMoveToHelp(context)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    @OnClick(R.id.imgGallery)
    fun onClickGallery(view: View?) {
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                onAddPermissionGallery()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    @OnClick(R.id.btnDone)
    fun onclickDone() {
        Log.d(TAG, "Done")
        ResponseSingleton.Companion.getInstance().onScannerDone()
        if (barcodeScannerView != null) {
            barcodeScannerView.pause()
        }
        presenter.doRefreshView()
    }

    override fun doRefreshView() {
        btnDone.setVisibility(View.INVISIBLE)
        tvCount.setVisibility(View.INVISIBLE)
    }

    override fun onStop() {
        super.onStop()
        if (barcodeScannerView != null) {
            barcodeScannerView.pauseAndWait()
        }
        Utils.Log(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        if (!isRunning) {
            ResponseSingleton.Companion.getInstance().setScannerPosition()
            isRunning = true
        }
        ResponseSingleton.Companion.getInstance().onResumeAds()
        Utils.Log(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (typeCamera != 2) {
            if (barcodeScannerView != null) {
                barcodeScannerView.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "onActivityResult : $requestCode - $resultCode")
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.SCANNER) {
            setVisible()
            Utils.Log(TAG, "Resume camera")
        } else if (requestCode == Crop.Companion.REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            beginCrop(data.getData())
        } else if (requestCode == Crop.Companion.REQUEST_CROP) {
            handleCrop(resultCode, data)
        } else {
            Utils.Log(TAG, "You haven't picked Image")
            setVisible()
            Utils.Log(TAG, "Resume camera!!!")
        }
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(activity.getCacheDir(), "cropped"))
        Crop.Companion.of(source, destination).asSquare().start(context, this)
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: String = Crop.Companion.getOutputString(result)
            val mResult = Gson().fromJson(mData, Result::class.java)
            mResult?.let { onFilterResult(it) }
            Utils.Log(TAG, "Result of cropped " + Gson().toJson(mResult))
        } else if (resultCode == Crop.Companion.RESULT_ERROR) {
            Toast.makeText(activity, Crop.Companion.getError(result).message, Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setVisible()
        }
    }

    fun onGetGallery() {
        pickImage(context, this)
    }

    fun onFilterResult(result: Result?) {
        if (activity == null) {
            return
        }
        //ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), result);
        val parsedResult = ResultParser.parseResult(result)
        val create = Create()
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
                if (addressResult != null) {
                    address = Utils.convertStringArrayToString(addressResult.addresses, ",")
                    fullName = Utils.convertStringArrayToString(addressResult.names, ",")
                    email = Utils.convertStringArrayToString(addressResult.emails, ",")
                    phone = Utils.convertStringArrayToString(addressResult.phoneNumbers, ",")
                }
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                create.createType = ParsedResultType.EMAIL_ADDRESS
                val emailAddress = parsedResult as EmailAddressParsedResult
                if (emailAddress != null) {
                    email = Utils.convertStringArrayToString(emailAddress.tos, ",")
                    subject = if (emailAddress.subject == null) "" else emailAddress.subject
                    message = if (emailAddress.body == null) "" else emailAddress.body
                }
            }
            ParsedResultType.PRODUCT -> {
                create.createType = ParsedResultType.PRODUCT
                val productResult = parsedResult as ProductParsedResult
                productId = if (productResult.productID == null) "" else productResult.productID
            }
            ParsedResultType.URI -> {
                create.createType = ParsedResultType.URI
                val urlResult = parsedResult as URIParsedResult
                if (urlResult != null) {
                    url = if (urlResult.uri == null) "" else urlResult.uri
                }
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
                val startTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.startTimestamp)
                val endTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.endTimestamp)
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
        beepManager.playBeepSoundAndVibrate()
        if (barcodeScannerView != null) {
            barcodeScannerView.pauseAndWait()
        }
        Navigator.onResultView(activity, create, ScannerResultFragment::class.java)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@ScannerFragment, true)
            Utils.Log(TAG, "isVisible")
        } else {
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@ScannerFragment, false)
            Utils.Log(TAG, "isInVisible")
        }
        if (barcodeScannerView != null) {
            if (menuVisible) {
                if (typeCamera != 2) {
                    onBeepAndVibrate()
                    barcodeScannerView.resume()
                }
            } else {
                if (typeCamera != 2) {
                    barcodeScannerView.pause()
                }
            }
        }
        Utils.Log(TAG, "Fragment visit...$menuVisible")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun updateValue(value: String?) {
        tvCount.setText(value)
    }

    /*Share File To QRScanner*/
    fun onHandlerIntent() {
        try {
            val intent = activity.getIntent()
            val action = intent.action
            val type = intent.type
            Utils.Log(TAG, "original type :$type")
            if (Intent.ACTION_SEND == action && type != null) {
                handleSendSingleItem(intent)
            }
        } catch (e: Exception) {
            //Utils.showGotItSnackbar(getView(),R.string.error_occurred_importing);
            Utils.onDropDownAlert(activity, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
    }

    fun handleSendSingleItem(intent: Intent?) {
        try {
            val imageUri = intent.getParcelableExtra<Parcelable?>(Intent.EXTRA_STREAM) as Uri?
            if (imageUri != null) {
                beginCrop(imageUri)
            } else {
                //Utils.showGotItSnackbar(getView(),R.string.can_not_support_this_format);
                Utils.onDropDownAlert(activity, getString(R.string.can_not_support_this_format))
            }
        } catch (e: Exception) {
            //Utils.showGotItSnackbar(getView(),R.string.error_occurred_importing);
            Utils.onDropDownAlert(activity, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ScannerFragment::class.java.simpleName
        fun newInstance(index: Int): ScannerFragment? {
            val fragment = ScannerFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}