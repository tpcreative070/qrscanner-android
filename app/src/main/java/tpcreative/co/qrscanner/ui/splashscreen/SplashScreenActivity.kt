package tpcreative.co.qrscanner.ui.splashscreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.snatik.storage.Storage
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.ui.splashscreen.SplashScreenActivity

class SplashScreenActivity : BaseActivity() {
    private var storage: Storage? = null
    private val LOADING_APP = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        storage = Storage(this)
        Utils.onObserveData(LOADING_APP.toLong()) {
            if (ContextCompat.checkSelfPermission(QRScannerApplication.Companion.getInstance(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                onAddPermissionCamera()
            } else {
                Navigator.onMoveMainTab(this@SplashScreenActivity)
            }
        }
    }

    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG, "Permission is ready")
                            val isRefresh = PrefsController.getBoolean(getString(R.string.key_refresh), false)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isRefresh) {
                                ScannerSingleton.Companion.getInstance().setVisible()
                                PrefsController.putBoolean(getString(R.string.key_refresh), true)
                            }
                            storage.createDirectory(QRScannerApplication.Companion.getInstance().getPathFolder())
                            Navigator.onMoveMainTab(this@SplashScreenActivity)
                            // Do something here
                        } else {
                            finish()
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            finish()
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

    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }
}