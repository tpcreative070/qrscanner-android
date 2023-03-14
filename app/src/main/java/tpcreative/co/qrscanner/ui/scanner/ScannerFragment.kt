package tpcreative.co.qrscanner.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
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
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.coroutines.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ScannerSingleton.SingletonScannerListener
import tpcreative.co.qrscanner.common.controller.PremiumManager
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.common.view.crop.Crop.Companion.getImagePicker
import tpcreative.co.qrscanner.databinding.FragmentScannerBinding
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumImplement
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
    var isRunning = false
    var mFrameRect: RectF? = null
    lateinit var binding: FragmentScannerBinding
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
                    binding.zxingBarcodeScanner.viewFinder?.addResultPoint(mResult.resultPoints?.toMutableList())
                    var mIsBarcode = false
                    if (mResult.barcodeFormat != BarcodeFormat.QR_CODE && mResult.barcodeFormat != BarcodeFormat.DATA_MATRIX   && mResult.barcodeFormat != BarcodeFormat.AZTEC){
                        mIsBarcode = true
                    }
                    Utils.Log(TAG,"Result meta ${result.resultMetadata.toJson()}")
                    binding.zxingBarcodeScanner.viewFinder?.drawResultBitmap(mBitmap,mIsBarcode,result.degree)
                    binding.zxingBarcodeScanner.viewFinder?.addTransferResultPoint(result.transformedResultPoints)
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
                        binding.zxingBarcodeScanner.pauseAndWait()
                        viewModel.isResume = false
                    }
                    return
                }
                binding.btnDone.visibility = View.VISIBLE
                binding.tvCount.visibility = View.VISIBLE
                updateValue(1)
                viewModel.doSaveItems(create)
                if (viewModel.isResume){
                    binding.zxingBarcodeScanner.pauseAndWait()
                    viewModel.isResume = false
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    binding.zxingBarcodeScanner.viewFinder?.drawViewfinder()
                    if (!viewModel.isResume){
                        binding.zxingBarcodeScanner.resume()
                        viewModel.isResume = true
                    }
                }
            } else {
                scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
                if (viewModel.isResume){
                    binding.zxingBarcodeScanner.pauseAndWait()
                    viewModel.isResume = false
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
        binding = FragmentScannerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun work() {
        super.work()
        initUI()
        if (!Utils.checkPermission(Manifest.permission.CAMERA)){
            binding.rlScanner.visibility = View.INVISIBLE
            binding.rlPermission.visibility = View.VISIBLE
        }else{
            binding.rlScanner.visibility = View.VISIBLE
            binding.rlPermission.visibility = View.INVISIBLE
        }
        ScannerSingleton.getInstance()?.setListener(this)
        binding.zxingBarcodeScanner.decodeContinuous(callback)
        binding.zxingBarcodeScanner.statusView?.visibility = View.GONE
        binding.zxingBarcodeScanner.barcodeView?.marginFraction = 0.2
        Utils.Log(TAG,"view ${binding.viewCrop.left}")
        if (isLandscape()){
            var mSize: Size? = Utils.getFrameLandscapeSize()
            if (mSize==null){
                val width: Int = (requireContext().resources?.displayMetrics?.widthPixels ?:0) - (Utils.spToPx(80F,requireContext())*2)
                val height: Int = (requireContext().resources?.displayMetrics?.heightPixels ?:0) - (Utils.spToPx(100F,requireContext())*2)
                mSize = Size(width,height)
                Utils.Log(TAG,"viewCrop - with: $width height: $height")
            }
            binding.zxingBarcodeScanner.barcodeView?.framingRectSize = mSize
        }else{
            binding.zxingBarcodeScanner.barcodeView?.framingRectSize = Utils.getFramePortraitSize()
        }
        binding.imgCreate.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        binding.imgGallery.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        if (Utils.isLight()) {
            binding.zxingBarcodeScanner.setTorchOn()
            binding.switchFlashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            binding.tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
        } else {
            binding.zxingBarcodeScanner.setTorchOff()
            binding.switchFlashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
            binding.tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
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
        binding.zxingBarcodeScanner.barcodeView?.cameraSettings = cameraSettings
        beepManager = BeepManager(activity)
        onHandlerIntent()
        if (!binding.zxingBarcodeScanner.isActivated) {
            if (!viewModel.isResume){
                binding.zxingBarcodeScanner.resume()
                viewModel.isResume = true
            }
            binding.zxingBarcodeScanner.barcodeView?.addStateListener(stateListener)
        }
        onBeepAndVibrate()
        if (!viewModel.isRequiredStartService){
            ServiceManager.getInstance().onStartService()
            PremiumManager.getInstance().onStartInAppPurchase()
            Utils.onCheckingNewApp()
            viewModel.isRequiredStartService = true
        }
    }

    fun onAddPermissionGallery() {
        if (viewModel.isResume){
            binding.zxingBarcodeScanner?.pauseAndWait()
            viewModel.isResume = false
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
        binding.zxingBarcodeScanner.viewFinder?.drawViewfinder()
        if (!viewModel.isResume){
            binding.zxingBarcodeScanner.resume()
            viewModel.isResume = true
        }
    }

    override fun setInvisible() {
        if (viewModel.isResume){
            binding.zxingBarcodeScanner.pauseAndWait()
            viewModel.isResume = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isResume){
            binding.zxingBarcodeScanner.pauseAndWait()
            viewModel.isResume = false
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
        if (viewModel.isRequestSettings){
            checkVisit()
            viewModel.isRequestSettings = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (typeCamera != 2) {
            if (viewModel.isResume){
                binding.zxingBarcodeScanner?.pauseAndWait()
                viewModel.isResume = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::viewModel.isInitialized){
            if (!binding.zxingBarcodeScanner.isActivated) {
                binding.zxingBarcodeScanner.viewFinder?.drawViewfinder()
                if (!viewModel.isResume){
                    binding.zxingBarcodeScanner.resume()
                    viewModel.isResume = true
                }
                if (!Utils.isMultipleScan()) {
                    binding.btnDone.visibility = View.INVISIBLE
                    binding.tvCount.visibility = View.INVISIBLE
                }
            }
            Utils.Log(TAG, "onResume")
        }
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
            if (viewModel.isResume){
                binding.zxingBarcodeScanner.pauseAndWait()
                viewModel.isResume = false
            }
            Utils.Log(TAG,"barcode ==> type ${parsedResult.type}")
            Utils.Log(TAG, "barcode ==> format ${mResult.barcodeFormat?.name}")
            scanForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (this::viewModel.isInitialized){
            if (menuVisible) {
                if (typeCamera != 2) {
                    onBeepAndVibrate()
                    if (!viewModel.isResume){
                        binding.zxingBarcodeScanner.resume()
                        viewModel.isResume = true
                        Utils.Log(TAG, "Request scanner resume...")
                    }
                    Utils.Log(TAG, "Fragment visit...resume...")
                }
            } else {
                if (typeCamera != 2) {
                    if (viewModel.isResume){
                        //Using pause in able to reduce lag when swipe page
                        binding.zxingBarcodeScanner.pause()
                        viewModel.isResume = false
                        Utils.Log(TAG, "Request scanner pause...")
                    }
                }
            }
            Utils.Log(TAG, "Fragment visit...$menuVisible")
        }
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