package tpcreative.co.qrscanner.ui.main
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PremiumManager
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.Theme
import tpcreative.co.qrscanner.viewmodel.MainViewModel

fun MainActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    if (QRScannerApplication.getInstance().getDeviceId() == "66801ac00252fe84") {
        finish()
    }
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    supportActionBar?.hide()
    ResponseSingleton.getInstance()?.setListener(this)
    setupViewPager(viewpager)
    tabs.setupWithViewPager(viewpager)
    viewpager.currentItem = 2
    for (i in 0 until tabs.tabCount) {
        val tab = tabs.getTabAt(i)
        tab?.customView = getTabView(i)
    }
    setupTabIcons()
    ServiceManager.getInstance().onStartService()
    PremiumManager.getInstance().onStartInAppPurchase()
    Theme.getInstance()?.getList()
    if (QRScannerApplication.getInstance().isMainView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableMainView() && !Utils.isPremium()) {
        QRScannerApplication.getInstance().requestMainView(this)
    }

    val mCountRating = Utils.onGetCountRating()
    if (mCountRating > 3) {
        showEncourage()
        Utils.Log(TAG, "rating.......")
        Utils.onSetCountRating(0)
    }

    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            //showAds()
            finish()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //showAds()
                    finish()
                }
            })
    }
}

private fun MainActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(MainViewModel::class.java)
}