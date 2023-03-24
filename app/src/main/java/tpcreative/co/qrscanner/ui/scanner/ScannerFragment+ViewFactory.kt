package tpcreative.co.qrscanner.ui.scanner
import android.Manifest
import android.graphics.*
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.MoveUpCallback
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.isLandscape
import tpcreative.co.qrscanner.common.extension.openAppSystemSettings
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel


fun ScannerFragment.initUI(){
    setupViewModel()
    binding.rlLight.setOnClickListener { view ->
        if (Utils.isLight()) {
            viewModel.isLight = false
            binding.switchFlashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
            binding.tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
            Utils.setLight(false)
        } else {
            viewModel.isLight = true
            binding.switchFlashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            binding.tvLight.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
            Utils.setLight(true)
        }
    }

    binding.rlHelp.setOnClickListener { view ->
        Navigator.onMoveToHelp(context)
    }

    binding.rlGallery.setOnClickListener { view ->
        onAddPermissionGallery()
    }

    binding.btnDone.setOnClickListener {
        viewModel.isRequestDone = true
        doRefreshView()
        ResponseSingleton.getInstance()?.onScannerDone()
    }

    binding.seekbarZoom.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            Utils.Log(TAG,"onProgressChanged $p1")
            viewModel.zoom = p1 / 100.toFloat()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            QRScannerApplication.getInstance().getActivity()?.lock(true)
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            Utils.Log(TAG,"onStopTrackingTouch")
            QRScannerApplication.getInstance().getActivity()?.lock(false)
        }
    })

    binding.imgZoomIn.setOnClickListener {
        binding.seekbarZoom.progress = 100
    }

    binding.imgZoomOut.setOnClickListener {
        binding.seekbarZoom.progress = 0
    }

    binding.rlScanPermission.setOnClickListener {
        onAddPermissionCamera()
    }

    binding.rlGallery.setOnClickListener {
        onAddPermissionGallery()
    }
    requestCountContinueScan()
}

private fun ScannerFragment.requestCountContinueScan(){
    val mCount = Utils.getCountContinueScan()
    if (mCount > 0){
        binding.btnDone.visibility = View.VISIBLE
        binding.tvCount.visibility = View.VISIBLE
        binding.tvCount.text = String.format(R.string.total.toText(),mCount)
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
        binding.tvCount.text = String.format(R.string.total.toText(),it)
    })
}

fun ScannerFragment.doRefreshView() {
    viewModel.doRefreshView().observe(this, Observer {
        binding.btnDone.visibility = View.INVISIBLE
        binding.tvCount.visibility = View.INVISIBLE
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
        mRespect = RectF(binding.viewCrop.left.toFloat(),binding.viewCrop.top.toFloat(),binding.viewCrop.right.toFloat(),binding.viewCrop.bottom.toFloat())
        if (Utils.getFrameRectLandscape()==null){
            Utils.setFrameRectLandscape(mRespect)
            mRequestRectFocus = mRespect
        }
    }
    binding.cropImageView.setDebugAdvance(true)
    binding.cropImageView.load(mUri)
        ?.initialFrameRect(mRequestRectFocus)
        ?.initialFrameRectByRespectScaleView(mRespect)
        ?.useThumbnail(true)
        ?.execute(mLoadCallback)
    binding.cropImageView.moveUp()
        ?.execute(mMoveUpCallback)
}

private val ScannerFragment.mMoveUpCallback: MoveUpCallback
    get() = object : MoveUpCallback {
            override fun onSuccess(width: Int, height: Int,rectF: RectF) {
                if (isLandscape()){
                    Utils.setFrameRectLandscape(rectF)
                }else{
                    Utils.setFrameRectPortrait(rectF)
                }
                binding.overlay.setFrameRect(rectF.toRect())
                Utils.Log(TAG,"onSuccess ${rectF.toString()}")
            }
            override fun onError(e: Throwable) {}
             override fun onDown() {
                 Utils.Log(TAG,"onDown")
            }

            override fun onRelease() {
                isInitial = false
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

//val ScannerFragment.stateListener: CameraPreview.StateListener
//    get() = object : CameraPreview.StateListener {
//        override fun previewSized() {}
//        override fun previewStarted() {
//            if (mFrameRect==null){
//                val mRect = Rect(binding.zxingBarcodeScanner.barcodeView.defaultFramingRect)
//                mFrameRect = RectF(binding.zxingBarcodeScanner.barcodeView.framingRect.left.toFloat(),
//                    binding.zxingBarcodeScanner.barcodeView.framingRect.top.toFloat(),
//                    binding.zxingBarcodeScanner.barcodeView.framingRect.right.toFloat(),
//                    binding.zxingBarcodeScanner.barcodeView.framingRect.bottom.toFloat()
//                )
//                initCropView(mFrameRect,mRect)
//                if (binding.zxingBarcodeScanner.barcodeView?.cameraInstance!=null){
//                    binding.seekbarZoom.max =  binding.zxingBarcodeScanner.barcodeView?.cameraInstance?.maxZoom() ?:0
//                }
//            }
//        }
//
//        override fun previewStopped() {}
//        override fun cameraError(error: Exception) {
//
//        }
//        override fun cameraClosed() {
//            if (viewModel.isRequestDone){
//                viewModel.isRequestDone = false
//            }
//            Utils.Log(TAG,"camera close")
//        }
//    }

fun ScannerFragment.onAddPermissionCamera() {
    Dexter.withContext(requireContext())
        .withPermissions(
            Manifest.permission.CAMERA)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                   // binding.zxingBarcodeScanner?.resume()
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
        binding.rlPermission.visibility = View.INVISIBLE
        binding.rlScanner.visibility = View.VISIBLE
    }else{
        binding.rlPermission.visibility = View.VISIBLE
        binding.rlScanner.visibility = View.INVISIBLE
    }
}
