package tpcreative.co.qrscanner.ui.scanner
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.MoveUpCallback
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.Size
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_scanner.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.isLandscape
import tpcreative.co.qrscanner.common.extension.openAppSystemSettings
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel


fun ScannerFragment.initUI(){
    setupViewModel()
    rl_light.setOnClickListener { view ->
        if (Utils.isLight()) {
            zxing_barcode_scanner.setTorchOff()
            switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
            tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
            Utils.setLight(false)
        } else {
            zxing_barcode_scanner.setTorchOn()
            switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
            Utils.setLight(true)
        }
    }

    rl_help.setOnClickListener { view ->
        if (zxing_barcode_scanner != null) {
            if (viewModel.isResume){
                zxing_barcode_scanner.pauseAndWait()
                viewModel.isResume = false
            }
        }
        Navigator.onMoveToHelp(context)
        zxing_barcode_scanner.statusView.visibility = View.GONE
    }

    rl_gallery.setOnClickListener { view ->
        onAddPermissionGallery()
    }

    btnDone.setOnClickListener {
        if (zxing_barcode_scanner != null) {
            if (viewModel.isResume){
                zxing_barcode_scanner.pauseAndWait()
                viewModel.isResume = false
            }
        }
        viewModel.isRequestDone = true
        doRefreshView()
        ResponseSingleton.getInstance()?.onScannerDone()
    }

    seekbarZoom.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            Utils.Log(TAG,"onProgressChanged $p1")
            if (zxing_barcode_scanner?.barcodeView?.cameraInstance!=null && zxing_barcode_scanner?.barcodeView?.cameraInstance?.isCheckReadyCamera() == true){
                zxing_barcode_scanner?.barcodeView?.cameraInstance?.setZoom(p1)
                zxing_barcode_scanner?.barcodeView?.cameraInstance?.cameraSettings?.zoom = p1
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            zxing_barcode_scanner?.barcodeView?.stopDecoding()
            QRScannerApplication.getInstance().getActivity()?.lock(true)
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            Utils.Log(TAG,"onStopTrackingTouch")
            QRScannerApplication.getInstance().getActivity()?.lock(false)
            zxing_barcode_scanner.decodeContinuous(callback)
        }
    })

    imgZoomIn.setOnClickListener {
        if (zxing_barcode_scanner?.barcodeView?.cameraInstance!=null && zxing_barcode_scanner?.barcodeView?.cameraInstance?.isCheckReadyCamera() == true) {
            seekbarZoom.progress = 100
        }
    }

    imgZoomOut.setOnClickListener {
        if (zxing_barcode_scanner?.barcodeView?.cameraInstance!=null && zxing_barcode_scanner?.barcodeView?.cameraInstance?.isCheckReadyCamera() == true) {
            seekbarZoom.progress = 0
        }
    }

    rlScanPermission.setOnClickListener {
        onAddPermissionCamera()
    }

    rlGallery.setOnClickListener {
        onAddPermissionGallery()
    }
    requestCountContinueScan()
}

private fun ScannerFragment.requestCountContinueScan(){
    val mCount = Utils.getCountContinueScan()
    if (mCount > 0){
        btnDone.visibility = View.VISIBLE
        tvCount.visibility = View.VISIBLE
        tvCount.text = String.format(R.string.total.toText(),mCount)
    }
}

private fun ScannerFragment.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(ScannerViewModel::class.java)
}

fun ScannerFragment.updateValue(mValue : Int) {
    viewModel.updateValue(mValue).observe(this, Observer {
        tvCount.text = String.format(R.string.total.toText(),it)
    })
}

fun ScannerFragment.doRefreshView() {
    viewModel.doRefreshView().observe(this, Observer {
        btnDone.visibility = View.INVISIBLE
        tvCount.visibility = View.INVISIBLE
        Utils.setCountContinueScan(0)
    })
}

fun ScannerFragment.initCropView(requestRectFocus : RectF?, rectBitMap : Rect){
    val mBitmap = Bitmap.createBitmap(rectBitMap.width(), rectBitMap.height(), Bitmap.Config.ARGB_8888)
    mBitmap.eraseColor(Color.TRANSPARENT)
    val mUri = Utils.getImageUri(mBitmap)
    var mRequestRectFocus = requestRectFocus
    // load image
    var mRespect : RectF? =  null
    if (isLandscape()){
        mRespect = RectF(viewCrop.left.toFloat(),viewCrop.top.toFloat(),viewCrop.right.toFloat(),viewCrop.bottom.toFloat())
        if (Utils.getFrameLandscapeSize()==null){
            Utils.setFrameRectLandscape(mRespect)
            mRequestRectFocus = mRespect
            zxing_barcode_scanner?.barcodeView?.framingRectSize = Utils.getFrameLandscapeSize()
            zxing_barcode_scanner.pause()
            zxing_barcode_scanner.resume()
        }
    }
    cropImageView.setDebugAdvance(true)
    cropImageView?.load(mUri)
        ?.initialFrameRect(mRequestRectFocus)
        ?.initialFrameRectByRespectScaleView(mRespect)
        ?.useThumbnail(true)
        ?.execute(mLoadCallback)
    cropImageView?.moveUp()
        ?.execute(mMoveUpCallback)
}

private val ScannerFragment.mMoveUpCallback: MoveUpCallback
    get() = object : MoveUpCallback {
            override fun onSuccess(width: Int, height: Int,rectF: RectF) {
                if (viewModel.isResume){
                    zxing_barcode_scanner?.pauseAndWait()
                    viewModel.isResume = false
                }
                zxing_barcode_scanner?.barcodeView?.framingRectSize = Size(width,height)
                if (isLandscape()){
                    Utils.setFrameRectLandscape(rectF)
                }else{
                    Utils.setFrameRectPortrait(rectF)
                }
                Utils.Log(TAG,"onSuccess ${rectF.toString()}")
            }
            override fun onError(e: Throwable) {}
             override fun onDown() {
                 Utils.Log(TAG,"onDown")
                 zxing_barcode_scanner?.barcodeView?.stopDecoding()
            }

            override fun onRelease() {
                zxing_barcode_scanner?.decodeContinuous(callback)
                if (!viewModel.isResume){
                    zxing_barcode_scanner.resume()
                    viewModel.isResume = true
                }
                Utils.Log(TAG,"onRelease")
            }
        }

// Callbacks ///////////////////////////////////////////////////////////////////////////////////
private val ScannerFragment.mLoadCallback: LoadCallback
    get() = object : LoadCallback {
        override fun onSuccess() {
        }
        override fun onError(e: Throwable) {}
    }

val ScannerFragment.stateListener: CameraPreview.StateListener
    get() = object : CameraPreview.StateListener {
        override fun previewSized() {}
        override fun previewStarted() {
            if (mFrameRect==null){
                val mRect = Rect(zxing_barcode_scanner.barcodeView.defaultFramingRect)
                mFrameRect = RectF(zxing_barcode_scanner.barcodeView.framingRect.left.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.top.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.right.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.bottom.toFloat()
                )
                initCropView(mFrameRect,mRect)
                if (zxing_barcode_scanner?.barcodeView?.cameraInstance!=null){
                    seekbarZoom.max =  zxing_barcode_scanner?.barcodeView?.cameraInstance?.maxZoom() ?:0
                }
            }
        }

        override fun previewStopped() {}
        override fun cameraError(error: Exception) {

        }
        override fun cameraClosed() {
            if (viewModel.isRequestDone){
                viewModel.isRequestDone = false
            }
            Utils.Log(TAG,"camera close")
        }
    }

fun ScannerFragment.onAddPermissionCamera() {
    Dexter.withContext(requireContext())
        .withPermissions(
            Manifest.permission.CAMERA)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    zxing_barcode_scanner?.resume()
                }
                if (report?.isAnyPermissionPermanentlyDenied ==true && viewModel.isAnyPermissionPermanentlyDenied){
                    onAlert()
                }
                if (report?.isAnyPermissionPermanentlyDenied == true){
                    viewModel.isAnyPermissionPermanentlyDenied = true
                }
                checkVisit()
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        })
        .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
}

fun ScannerFragment.onAlert(){
    MaterialDialog(requireContext()).show {
       message(res = R.string.scanning_using_the_camera_will_require_permission_to_access_the_camera)
        positiveButton(res = R.string.settings){
            requireContext().openAppSystemSettings()
            viewModel.isRequestSettings = true
        }
        negativeButton(R.string.cancel)
    }
}

fun ScannerFragment.checkVisit(){
    if (Utils.checkPermission(Manifest.permission.CAMERA)){
        rlPermission.visibility = View.INVISIBLE
        rlScanner.visibility = View.VISIBLE
    }else{
        rlPermission.visibility = View.VISIBLE
        rlScanner.visibility = View.INVISIBLE
    }
}
