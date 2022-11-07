package tpcreative.co.qrscanner.ui.scanner
import android.graphics.PorterDuff
import android.hardware.Camera
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_scanner.*
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