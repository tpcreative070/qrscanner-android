package tpcreative.co.qrscanner.ui.scanner
import android.graphics.*
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.isseiaoki.simplecropview.CropImageView
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.MoveUpCallback
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.fragment_scanner.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.viewmodel.ScannerViewModel


fun ScannerFragment.initUI(){
    setupViewModel()
    switch_camera.setOnClickListener {view ->
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                zxing_barcode_scanner.pauseAndWait()
                if (cameraSettings.requestedCameraId == 0) {
                    switchCamera(Constant.CAMERA_FACING_FRONT)
                } else {
                    switchCamera(Constant.CAMERA_FACING_BACK)
                }
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    switch_flashlight.setOnClickListener { view ->
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (isTurnOnFlash) {
                    zxing_barcode_scanner.setTorchOff()
                    isTurnOnFlash = false
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.getInstance(), R.drawable.baseline_flash_off_white_48))
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_ATOP)
                } else {
                    zxing_barcode_scanner.setTorchOn()
                    isTurnOnFlash = true
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.getInstance(), R.drawable.baseline_flash_on_white_48))
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
    }

    imgCreate.setOnClickListener { view ->
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Utils.Log(TAG, "start")
            }
            override fun onAnimationEnd(animation: Animation?) {
                if (zxing_barcode_scanner != null) {
                    zxing_barcode_scanner.pauseAndWait()
                }
                Navigator.onMoveToHelp(context)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(mAnim)
        zxing_barcode_scanner.statusView.visibility = View.GONE
    }

    imgGallery.setOnClickListener { view ->
        mAnim = AnimationUtils.loadAnimation(context, R.anim.anomation_click_item)
        mAnim?.setAnimationListener(object : Animation.AnimationListener {
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

    btnDone.setOnClickListener {
        ResponseSingleton.getInstance()?.onScannerDone()
        if (zxing_barcode_scanner != null) {
            zxing_barcode_scanner.pause()
        }
        doRefreshView()
    }
    seekbarZoom.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            Utils.Log(TAG,"onProgressChanged $p1")
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            Utils.Log(TAG,"onStopTrackingTouch")
        }
    })

    imgZoomIn.setOnClickListener {
        seekbarZoom.max = 100
        seekbarZoom.progress = 100
    }

    imgZoomOut.setOnClickListener {
        seekbarZoom.max = 100
        seekbarZoom.progress = 0
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
        tvCount.text = it
    })
}

fun ScannerFragment.doRefreshView() {
    viewModel.doRefreshView().observe(this, Observer {
        btnDone.visibility = View.INVISIBLE
        tvCount.visibility = View.INVISIBLE
    })
}

fun ScannerFragment.initCropView(requestRectFocus : RectF?, rectBitMap : Rect){
    val mBitmap = Bitmap.createBitmap(rectBitMap.width(), rectBitMap.height(), Bitmap.Config.ARGB_8888)
    mBitmap.eraseColor(Color.TRANSPARENT)
    val mUri = Utils.getImageUri(mBitmap)
    // load image
    cropImageView.setDebugAdvance(true)
    cropImageView?.load(mUri)
        ?.initialFrameRect(requestRectFocus)
        ?.useThumbnail(true)
        ?.execute(mLoadCallback)
    cropImageView?.moveUp()
        ?.execute(mMoveUpCallback)
}

private val ScannerFragment.mMoveUpCallback: MoveUpCallback
    get() = object : MoveUpCallback {
            override fun onSuccess(width: Int, height: Int) {
                zxing_barcode_scanner.barcodeView.framingRectSize = Size(width,height)
                Utils.Log(TAG,"rect $width $height")
            }
            override fun onError(e: Throwable) {}
             override fun onDown() {
                 zxing_barcode_scanner.pause()
            }

            override fun onRelease() {
                zxing_barcode_scanner.resume()
            }
        }

// Callbacks ///////////////////////////////////////////////////////////////////////////////////
private val ScannerFragment.mLoadCallback: LoadCallback
    get() = object : LoadCallback {
        override fun onSuccess() {}
        override fun onError(e: Throwable) {}
    }

val ScannerFragment.stateListener: CameraPreview.StateListener
    get() = object : CameraPreview.StateListener {
        override fun previewSized() {}
        override fun previewStarted() {
            if (mFrameRect==null){
                Utils.Log(TAG,"rect ${zxing_barcode_scanner.barcodeView.framingRect}")
                Utils.Log(TAG,"rect ${zxing_barcode_scanner.barcodeView.containerKeepSize}")
                //val mRect = Rect(20,250,zxing_barcode_scanner.barcodeView.containerKeepSize.width-20,zxing_barcode_scanner.barcodeView.containerKeepSize.height-250)
                val mRect = Rect(zxing_barcode_scanner.barcodeView.framingRect)
                Utils.Log(TAG,"rect $mRect")
                Utils.Log(TAG,"rect ${zxing_barcode_scanner.barcodeView.containerKeepSize}")
                mFrameRect = RectF(zxing_barcode_scanner.barcodeView.framingRect.left.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.top.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.right.toFloat(),
                    zxing_barcode_scanner.barcodeView.framingRect.bottom.toFloat()
                )
                Utils.Log(TAG,"rect ${mFrameRect}")
                initCropView(mFrameRect,mRect)
            }
        }

        override fun previewStopped() {}
        override fun cameraError(error: Exception) {

        }
        override fun cameraClosed() {
        }
    }
