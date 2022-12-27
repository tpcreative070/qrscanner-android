package tpcreative.co.qrscanner.ui.scanner

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.view.animation.Animation
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.result.ResultParser
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ScannerSingleton.SingletonScannerListener
import tpcreative.co.qrscanner.common.extension.isLandscape
import tpcreative.co.qrscanner.common.extension.onGeneralParse
import tpcreative.co.qrscanner.common.extension.parcelable
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.common.view.crop.Crop.Companion.getImagePicker
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumImplement
import tpcreative.co.qrscanner.model.EnumRotation
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel
import java.io.File
import kotlin.coroutines.CoroutineContext


class ScannerFragment : BaseFragment(), SingletonScannerListener{
    lateinit var viewModel : ScannerViewModel
    private var beepManager: BeepManager? = null
    private val cameraSettings: CameraSettings = CameraSettings()
    var typeCamera = 0
    var isTurnOnFlash = false
    var mAnim: Animation? = null
    var isRunning = false
    var mFrameRect: RectF? = null
    private var mRotation  : Int = 0
    private var orientationEventListener: OrientationEventListener? = null
    val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            try {
                Utils.Log(TAG, "Call back :" + result?.text + "  type :" + result?.barcodeFormat?.name)
                if (activity == null) {
                    return
                }
                result?.result?.let { mResult ->
                    Utils.Log(TAG,"Text result ${mResult.text}")
                    val create = Utils.onGeneralParse(mResult,GeneralModel::class)
                    create.fragmentType = EnumFragmentType.SCANNER
                    create.enumImplement = EnumImplement.VIEW
                    create.barcodeFormat = BarcodeFormat.QR_CODE.name
                    if (mResult.barcodeFormat != null) {
                        create.barcodeFormat = mResult.barcodeFormat.name
                    }
                    val mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(mBitmap);
                    canvas.drawColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
                    zxing_barcode_scanner?.viewFinder?.addResultPoint(mResult.resultPoints?.toMutableList())
                    zxing_barcode_scanner?.viewFinder?.drawResultBitmap(mBitmap,zxing_barcode_scanner?.barcodeView?.cameraInstance?.displayConfiguration?.realtimeRotation ?:0)
                    zxing_barcode_scanner?.viewFinder?.addTransferResultPoint(result.transformedResultPoints)
                    Utils.Log(TAG, "barcode ==> format ${result.barcodeFormat?.name}")
                    doNavigation(create)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun doNavigation(create: GeneralModel?) {
            if (Utils.isMultipleScan()) {
                if (viewModel.isRequestDone){
                    if (viewModel.isResume){
                        zxing_barcode_scanner?.pauseAndWait()
                        viewModel.isResume = false
                    }
                    return
                }
                btnDone.visibility = View.VISIBLE
                tvCount.visibility = View.VISIBLE
                updateValue(1)
                viewModel.doSaveItems(create)
                if (zxing_barcode_scanner != null) {
                    if (viewModel.isResume){
                        zxing_barcode_scanner?.pauseAndWait()
                        viewModel.isResume = false
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        zxing_barcode_scanner?.viewFinder?.drawViewfinder()
                        if (!viewModel.isResume){
                            zxing_barcode_scanner?.resume()
                            viewModel.isResume = true
                        }
                    }
                }
            } else {
                scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
                if (zxing_barcode_scanner != null) {
                    if (viewModel.isResume){
                        zxing_barcode_scanner?.pauseAndWait()
                        viewModel.isResume = false
                    }
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
        zxing_barcode_scanner?.decodeContinuous(callback)
        zxing_barcode_scanner?.statusView?.visibility = View.GONE
        zxing_barcode_scanner?.barcodeView?.marginFraction = 0.2
        Utils.Log(TAG,"view ${viewCrop.left}")
        if (isLandscape()){
            zxing_barcode_scanner?.barcodeView?.framingRectSize = Utils.getFrameLandscapeSize()
        }else{
            zxing_barcode_scanner?.barcodeView?.framingRectSize = Utils.getFramePortraitSize()
        }
        imgCreate.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        imgGallery.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
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
        zxing_barcode_scanner?.barcodeView?.cameraSettings = cameraSettings
        beepManager = BeepManager(activity)
        onHandlerIntent()
        if (zxing_barcode_scanner != null) {
            if (!zxing_barcode_scanner.isActivated) {
                if (!viewModel.isResume){
                    zxing_barcode_scanner?.resume()
                    viewModel.isResume = true
                }
                zxing_barcode_scanner?.barcodeView?.addStateListener(stateListener)
            }
        }
        onBeepAndVibrate()

        val handleClickEventsDebounced = debounce<Unit>(600, coroutineContext) {
            Utils.Log(TAG,"Rotation $mRotation")
            zxing_barcode_scanner?.barcodeView?.cameraInstance?.displayConfiguration?.realtimeRotation = mRotation
        }

        var enumRotation : EnumRotation = EnumRotation.PORTRAIT
        orientationEventListener =
            object : OrientationEventListener(activity) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation in 0..90 && enumRotation != EnumRotation.PORTRAIT){
                        mRotation = 0
                        enumRotation = EnumRotation.PORTRAIT
                        Utils.Log(TAG, "orientation... ${enumRotation.name}  = $orientation")
                        handleClickEventsDebounced(Unit)
                    }else if (orientation in 90..180  && enumRotation != EnumRotation.LANDSCAPE){
                        mRotation = 90
                        enumRotation = EnumRotation.LANDSCAPE
                        Utils.Log(TAG, "orientation... ${enumRotation.name} = $orientation")
                        handleClickEventsDebounced(Unit)
                    }else if (orientation in 180..270  && enumRotation != EnumRotation.REVERSE_PORTRAIT){
                        mRotation = 180
                        enumRotation = EnumRotation.REVERSE_PORTRAIT
                        Utils.Log(TAG, "orientation... ${enumRotation.name} = $orientation")
                        handleClickEventsDebounced(Unit)
                    } else if (orientation in 270..360  && enumRotation != EnumRotation.REVERSE_LANDSCAPE){
                        mRotation = 270
                        enumRotation = EnumRotation.REVERSE_LANDSCAPE
                        Utils.Log(TAG, "orientation... ${enumRotation.name} = $orientation")
                        handleClickEventsDebounced(Unit)
                    }else{
                        //Utils.Log(TAG, "orientation nothing")
                    }
                }
            }
        orientationEventListener?.enable()
    }

    private fun <T> debounce(delayMs: Long = 500L,
                             coroutineContext: CoroutineContext,
                             f: (T) -> Unit): (T) -> Unit {
        var debounceJob: Job? = null
        return { param: T ->
            if (debounceJob?.isCompleted != false) {
                debounceJob = CoroutineScope(coroutineContext).launch {
                    delay(delayMs)
                    f(param)
                }
            }
        }
    }

    fun onAddPermissionGallery() {
        if (zxing_barcode_scanner != null) {
            if (viewModel.isResume){
                zxing_barcode_scanner?.pauseAndWait()
                viewModel.isResume = false
            }
        }
        onGetGallery()
    }

    private fun onBeepAndVibrate() {
        if (beepManager == null) {
            return
        }
        beepManager?.isBeepEnabled = Utils.getBeep()
        beepManager?.isVibrateEnabled = Utils.getVibrate()
    }

    override fun setVisible() {
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner?.viewFinder?.drawViewfinder()
            if (!viewModel.isResume){
                zxing_barcode_scanner?.resume()
                viewModel.isResume = true
            }
        }
    }

    override fun setInvisible() {
        if (zxing_barcode_scanner != null) {
            if (viewModel.isResume){
                zxing_barcode_scanner?.pauseAndWait()
                viewModel.isResume = false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (zxing_barcode_scanner != null) {
            if (viewModel.isResume){
                zxing_barcode_scanner?.pauseAndWait()
                viewModel.isResume = false
            }
        }
        orientationEventListener?.disable()
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
        orientationEventListener?.enable()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (typeCamera != 2) {
            if (zxing_barcode_scanner != null) {
                if (viewModel.isResume){
                    zxing_barcode_scanner?.pauseAndWait()
                    viewModel.isResume = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
         if (zxing_barcode_scanner != null && !zxing_barcode_scanner.isActivated) {
             zxing_barcode_scanner?.viewFinder?.drawViewfinder()
             if (!viewModel.isResume){
                 zxing_barcode_scanner?.resume()
                 viewModel.isResume = true
             }
             if (!Utils.isMultipleScan()) {
                 btnDone.visibility = View.INVISIBLE
                 tvCount.visibility = View.INVISIBLE
             }
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
        result?.let { mResult ->
            val parsedResult = ResultParser.parseResult(result)
            Utils.Log(TAG,"Text result ${mResult.text}")
            val create = Utils.onGeneralParse(mResult,GeneralModel::class)
            create.enumImplement = EnumImplement.VIEW
            create.fragmentType = EnumFragmentType.SCANNER
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            if (mResult.barcodeFormat != null) {
                create.barcodeFormat = result.barcodeFormat.name
            }
            beepManager?.playBeepSoundAndVibrate()
            if (zxing_barcode_scanner != null) {
                if (viewModel.isResume){
                    zxing_barcode_scanner?.pauseAndWait()
                    viewModel.isResume = false
                }
            }
            Utils.Log(TAG,"barcode ==> type ${parsedResult.type}")
            Utils.Log(TAG, "barcode ==> format ${mResult.barcodeFormat?.name}")
            scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
        }
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
                    if (!viewModel.isResume){
                        zxing_barcode_scanner.resume()
                        viewModel.isResume = true
                    }
                    Utils.Log(TAG, "Fragment visit...resume...")
                }
            } else {
                if (typeCamera != 2) {
                    if (viewModel.isResume){
                        //Using pause in able to reduce lag when swipe page
                        zxing_barcode_scanner?.pause()
                        viewModel.isResume = false
                    }
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