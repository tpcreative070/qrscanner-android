package tpcreative.co.qrscanner.ui.splashscreen
import android.Manifest
import android.os.Bundle
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.PrefsController

class SplashScreenActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Navigator.onGuides(this)
//        if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(), Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_DENIED) {
//            onAddPermissionCamera()
//        } else {
//            Navigator.onMoveMainTab(this@SplashScreenActivity)
//        }
    }

    private fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                            Utils.Log(TAG, "Permission is ready")
                            val isRefresh = PrefsController.getBoolean(getString(R.string.key_refresh), false)
                            if (!isRefresh) {
                                ScannerSingleton.getInstance()?.setVisible()
                                PrefsController.putBoolean(getString(R.string.key_refresh), true)
                            }
                            Navigator.onMoveMainTab(this@SplashScreenActivity)
                            // Do something here
                        } else {
                            finish()
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report?.isAnyPermissionPermanentlyDenied == true) {
                            /*Miss add permission in manifest*/
                            finish()
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
    }

    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }
}