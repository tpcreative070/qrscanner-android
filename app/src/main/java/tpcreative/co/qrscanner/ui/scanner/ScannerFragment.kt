package tpcreative.co.qrscanner.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.camera2.*
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRect
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.window.layout.WindowMetricsCalculator
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.client.result.ResultParser
import com.zxingcpp.BarcodeReader
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
import tpcreative.co.qrscanner.ui.scanner.cpp.BeepManager
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ScannerFragment : BaseFragment(), SingletonScannerListener {
    lateinit var viewModel: ScannerViewModel
    private var beepManager: BeepManager? = null
    var isRunning = false
    var mFrameRect: RectF? = null
    lateinit var binding: FragmentScannerBinding
    private val executor = Executors.newSingleThreadExecutor()
    private var isStop: Boolean = false
    var isCropViewFinder : Boolean = false
    var isInitial: Boolean = false
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private fun doNavigation(create: GeneralModel?){
        if (Utils.isMultipleScan()) {
            Utils.Log(TAG,"Stopping call...")
            binding.btnDone.visibility = View.VISIBLE
            binding.tvCount.visibility = View.VISIBLE
            updateValue(1)
            viewModel.doSaveItems(create)
            Utils.Log(TAG,"Stopping call $isStop")
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                Utils.Log(TAG,"Stopping call delay")
                isStop = false
            }
        } else {
            scanForResult.launch(
                Navigator.onResultView(
                    activity,
                    create,
                    ScannerResultActivity::class.java
                )
            )
        }
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
        binding.overlay.viewTreeObserver.addOnGlobalLayoutListener {
            if (isLandscape()) {
                var mRespect: RectF? = Utils.getFrameRectLandscape()
                if (mRespect==null){
                    mRespect = RectF(binding.viewCrop.left.toFloat(),binding.viewCrop.top.toFloat(),binding.viewCrop.right.toFloat(),binding.viewCrop.bottom.toFloat())
                    Utils.setFrameRectLandscape(mRespect)
                }
                Utils.getFrameRectLandscape()?.toRect()?.let {
                    binding.overlay.setFrameRect(it)
                }
                Utils.getFrameRectLandscape()?.toRect()?.let {
                    binding.overlay.setFrameRect(it)
                }
            } else {
                Utils.getFrameRectPortrait()?.toRect()?.let {
                    binding.overlay.setFrameRect(it)
                }
            }
        }
        if (!Utils.checkPermission(Manifest.permission.CAMERA)) {
            binding.rlScanner.visibility = View.INVISIBLE
            binding.rlPermission.visibility = View.VISIBLE
        } else {
            binding.rlScanner.visibility = View.VISIBLE
            binding.rlPermission.visibility = View.INVISIBLE
        }
        ScannerSingleton.getInstance()?.setListener(this)
        binding.imgCreate.setColorFilter(
            ContextCompat.getColor(
                QRScannerApplication.getInstance(),
                R.color.white
            ), PorterDuff.Mode.SRC_ATOP
        )
        binding.imgGallery.setColorFilter(
            ContextCompat.getColor(
                QRScannerApplication.getInstance(),
                R.color.white
            ), PorterDuff.Mode.SRC_ATOP
        )
        if (Utils.isLight()) {
            binding.switchFlashlight.setColorFilter(
                ContextCompat.getColor(
                    QRScannerApplication.getInstance(),
                    R.color.colorAccent
                ), PorterDuff.Mode.SRC_ATOP
            )
            binding.tvLight.setTextColor(
                ContextCompat.getColor(
                    context(),
                    R.color.colorAccent
                )
            )
        } else {
            binding.switchFlashlight.setColorFilter(
                ContextCompat.getColor(
                    QRScannerApplication.getInstance(),
                    R.color.white
                ), PorterDuff.Mode.SRC_ATOP
            )
            binding.tvLight.setTextColor(ContextCompat.getColor(context(), R.color.white))
        }
        beepManager = BeepManager(activity())
        onHandlerIntent()
        if (!viewModel.isRequiredStartService) {
            ServiceManager.getInstance().onStartService()
            PremiumManager.getInstance().onStartInAppPurchase()
            Utils.onCheckingNewApp()
            viewModel.isRequiredStartService = true
        }
        binding.overlay.viewTreeObserver.addOnGlobalLayoutListener {
            if (!isInitial) {
                val mRectDefault = Rect(binding.overlay.getDefaultFrameRect())
                val mRect = binding.overlay.getFrameRect()
                mFrameRect = RectF(mRect?.left?.toFloat() ?:0F,mRect?.top?.toFloat() ?:0F,mRect?.right?.toFloat() ?:0F,mRect?.bottom?.toFloat() ?:0F)
                initCropView(mFrameRect,mRectDefault)
                Utils.Log(TAG,"mRect $mRectDefault")
                Utils.Log(TAG,"mRect default $mRect")
                isInitial = true
            }
        }
        bindCameraUseCases()
    }

    fun onAddPermissionGallery() {
        onGetGallery()
    }

    override fun setVisible() {
        //bindCameraUseCases()
    }

    override fun setInvisible() {
       //cameraProvider?.unbindAll()
    }

    override fun onStop() {
        super.onStop()
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
        if (viewModel.isRequestSettings) {
            checkVisit()
            viewModel.isRequestSettings = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        if (this::viewModel.isInitialized) {
            isStop = false
            bindCameraUseCases()
            if (!Utils.isMultipleScan()) {
                binding.btnDone.visibility = View.INVISIBLE
                binding.tvCount.visibility = View.INVISIBLE
            }
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
        cropForResult.launch(Crop.of(source, destination)?.asSquare()?.start(context()))
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: String? = Crop.getOutputString(result)
            val mResult = Gson().fromJson(mData, Result::class.java)
            mResult?.let { onFilterResult(it) }
            Utils.Log(TAG, "Result of cropped " + Gson().toJson(mResult))
        } else if (resultCode == Crop.RESULT_ERROR) {
            Utils.onAlertNotify(activity(), "${Crop.getError(result)?.message}")
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
            Utils.Log(TAG, "barcode ==> type ${parsedResult.type}")
            Utils.Log(TAG, "barcode ==> format ${mResult.barcodeFormat?.name}")
            scanForResult.launch(
                Navigator.onResultView(
                    activity,
                    create,
                    ScannerResultActivity::class.java
                )
            )
            cameraProvider?.unbindAll()
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (this::viewModel.isInitialized) {
            if (menuVisible) {
                Utils.Log(TAG, "Fragment visit...resume...")
            } else {
                cameraProvider?.unbindAll()
            }
            Utils.Log(TAG, "Fragment visit...$menuVisible")
        }
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG,"onPause")
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


    /**
     *  Detecting the most suitable aspect ratio for current dimensions
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun getTargetResolution(): android.util.Size {
        return when (resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> android.util.Size(1200, 1600)
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> android.util.Size(1600, 1200)
            else -> android.util.Size(1600, 1200)
        }
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun  bindCameraUseCases() = binding.viewFinder.post {
        val viewFinder = binding.viewFinder
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context())
        cameraProviderFuture.addListener({
            // Set up the view finder use case to display camera preview
            // The display information
            //val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            //val metrics: WindowMetrics = requireContext().getSystemService(WindowManager::class.java).currentWindowMetrics
            if (!isAdded){
                Utils.Log(TAG,"Stop working")
                return@addListener
            }
            val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity())
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.bounds.width(), metrics.bounds.height())
            // The display rotation
            val rotation = viewFinder.display.rotation

            // The Configuration of camera preview
            preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()

            // The Configuration of image analyzing
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
                .setTargetRotation(rotation) // set the analyzer rotation
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // in our analysis, we care about the latest image
                .build()

            val readerCpp = BarcodeReader()

            // Create a new camera selector each time, enforcing lens facing
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            // Camera provider is now guaranteed to be available
            cameraProvider = cameraProviderFuture.get()

            // Apply declared configs to CameraX using the same lifecycle owner
            cameraProvider?.unbindAll()
            val camera = cameraProvider?.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageAnalyzer
            )

//             Reduce exposure time to decrease effect of motion blur
//            camera?.let {
//                val camera2 = Camera2CameraControl.from(it.cameraControl)
//                camera2.captureRequestOptions = CaptureRequestOptions.Builder()
//                    .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, 1600)
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -1)
//                    .build()
//
//                viewFinder.afterMeasured {
//                    //val factory = DisplayOrientedMeteringPointFactory(metrics.bounds.width(), camera?.cameraInfo, previewView.width.toFloat(), previewView.height.toFloat())
//                   Utils.Log(TAG,"Call auto focus")
//                    val mRect = binding.overlay.getFrameRect()
//                    val mX = mRect?.centerX() ?: .5f
//                    val mY = mRect?.centerY() ?: .5f
//                    val autoFocusPoint = SurfaceOrientedMeteringPointFactory(metrics.bounds.width().toFloat(), metrics.bounds.height().toFloat())
//                        .createPoint(.5f,.5f)
//                    try {
//                        val autoFocusAction = FocusMeteringAction.Builder(
//                            autoFocusPoint,
//                            FocusMeteringAction.FLAG_AF
//                        ).apply {
//                            //start auto-focusing after 2 seconds
//                            setAutoCancelDuration(1, TimeUnit.SECONDS)
//                        }.build()
//                        camera.cameraControl.startFocusAndMetering(autoFocusAction)
//                        Utils.Log(TAG,"Call auto focus...")
//                    } catch (e: CameraInfoUnavailableException) {
//                        Utils.Log("ERROR", "cannot access camera ${e.message}")
//                    }
//                }
//
//            }

            // Use the camera object to link our preview use case with the view
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            imageAnalyzer?.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
//                if (isStop) {
//                    image.close()
//                    return@Analyzer
//                }
                Utils.Log(TAG,"Area focus ${binding.overlay.getFrameRect()}")

                if (!isInitial) {
                    val mRectDefault = Rect(binding.overlay.getDefaultFrameRect())
                    val mRect = binding.overlay.getFrameRect()
                    mFrameRect = RectF(mRect?.left?.toFloat() ?:0F,mRect?.top?.toFloat() ?:0F,mRect?.right?.toFloat() ?:0F,mRect?.bottom?.toFloat() ?:0F)
                    initCropView(mFrameRect,mRectDefault)
                    Utils.Log(TAG,"mRect $mRectDefault")
                    Utils.Log(TAG,"mRect default $mRect")
                    isInitial = true
                }
                val resultText: String
                var resultPoints: List<PointF>? = null

                readerCpp.options = BarcodeReader.Options(
                    formats = setOf(),
                    tryHarder = true,
                    tryRotate = true,
                    tryInvert = false,
                    tryDownscale = false
                )

                resultText = try {
                    image.setCropRect(crop(image))
                    val result = readerCpp.read(image)
                    //RSS-14
                    Utils.Log(TAG,"Result text first ${result?.text}")
                    Utils.Log(TAG,"Result format type first ${result?.format?.name}")
                    resultPoints = result?.position?.let {
                        listOf(
                            it.topLeft,
                            it.topRight,
                            it.bottomRight,
                            it.bottomLeft
                        ).map { p ->
                            p.toPointF()
                        }
                    }
                    (result?.let {
                        val mResultPoint = Array(1) { i ->
                            ResultPoint(
                                (resultPoints?.get(0)?.x ?: 0).toFloat(),
                                (resultPoints?.get(0)?.y ?: 0).toFloat()
                            )
                        }
                        val mResult = Result(
                            it.text,
                            it.bytes,
                            mResultPoint,
                            it.format.cppFormatToJavaFormat()
                        )

                        val parsedResult = ResultParser.parseResult(mResult)
                        Utils.Log(TAG,"Parse result 3 ${parsedResult.type}")
                        if (parsedResult != null) {
                            val create = Utils.onGeneralParse(mResult, GeneralModel::class)
                            create.fragmentType = EnumFragmentType.SCANNER
                            create.enumImplement = EnumImplement.VIEW
                            create.barcodeFormat = BarcodeFormat.QR_CODE.name
                            if (mResult.barcodeFormat != null) {
                                create.barcodeFormat = mResult.barcodeFormat.name
                            }
                            if (!isStop && !isCropViewFinder){
                                showResult("", resultPoints, image)
                                doNavigation(create)
                                beepManager?.playBeepSoundAndVibrate()
                                isStop = true
                            }
                            Utils.Log(TAG, "Type result ${parsedResult.type}")
                        }
                        "${it.format} (${it.contentType}): " +
                                "${
                                    if (it.contentType != BarcodeReader.ContentType.BINARY) it.text else it.bytes!!.joinToString(
                                        separator = ""
                                    ) { v -> "%02x".format(v) }
                                }"
                    }
                        ?: "")
                } catch (e: Throwable) {
                    e.message ?: "Error"
                }
                camera?.cameraControl?.setLinearZoom(viewModel.zoom)
                camera?.cameraControl?.enableTorch(viewModel.isLight)
                if (!isCropViewFinder){
                    showResult(resultText, resultPoints, image)
                }
                Utils.Log(TAG,"Result text $resultText")
            })

        }, ContextCompat.getMainExecutor(context()))
    }

    private fun crop(image : ImageProxy) : Rect{
        val mCropped = binding.overlay.getFrameRect()
        val mRect =  Rect(0, 0, image.width, image.height)
        var mCropWidthSize = mCropped?.height() ?: image.height
        var mCropHeightSize = mCropped?.width() ?: image.width
        if (isLandscape()){
            mCropWidthSize = (mCropped?.width() ?: image.width)
            mCropHeightSize = (mCropped?.height() ?: image.height)
        }
        mRect.left = (image.width - mCropWidthSize)/2
        mRect.top = (image.height - mCropHeightSize)/2
        mRect.right = (image.width - mCropWidthSize)/2 + mCropWidthSize
        mRect.bottom = (image.height - mCropHeightSize)/2 + mCropHeightSize

        Utils.Log(TAG,"Area focus detect ${mRect} Rotation ${image.imageInfo.rotationDegrees}")
        return mRect
    }

    private fun showResult(
        resultText: String,
        points: List<PointF>?,
        image: ImageProxy
    ) =
        binding.viewFinder.post {
            // Update the text and UI
            Utils.Log(TAG, "Result text $resultText")
            binding.overlay.update(binding.viewFinder, image, points)
        }

   fun activity(): FragmentActivity {
        return this.activity
            ?: throw IllegalStateException("Fragment $this not attached to an activity.")
    }

   fun context(): Context {
        return this.context
            ?: throw java.lang.IllegalStateException("Fragment $this not attached to a context.")
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
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}