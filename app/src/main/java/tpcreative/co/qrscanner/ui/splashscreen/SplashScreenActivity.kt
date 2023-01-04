package tpcreative.co.qrscanner.ui.splashscreen
import android.annotation.SuppressLint
import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.PrefsController

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Utils.setCountContinueScan(0)
        val isRefresh = PrefsController.getBoolean(getString(R.string.key_refresh), false)
        if (!isRefresh) {
            PrefsController.putBoolean(getString(R.string.key_refresh), true)
        }

        if (Utils.onIsIntro()){
            Navigator.onMoveMainTab(this@SplashScreenActivity)
        }else{
            Navigator.onIntro(this)
        }
    }
    companion object {
        private val TAG = SplashScreenActivity::class.java.simpleName
    }
}