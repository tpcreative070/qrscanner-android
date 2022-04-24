package tpcreative.co.qrscanner.ui.main
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.CustomViewPager
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
    storage = Storage(applicationContext)
    setupViewPager(viewpager)
    tabs.setupWithViewPager(viewpager)
    viewpager.currentItem = 2
    for (i in 0 until tabs.tabCount) {
        val tab = tabs.getTabAt(i)
        tab?.customView = getTabView(i)
    }
    setupTabIcons()
    ServiceManager.getInstance()?.onStartService()
    Theme.getInstance()?.getList()
    viewpager.setOnSwipeOutListener(object : CustomViewPager.OnSwipeOutListener {
        override fun onSwipeOutAtStart() {
            Utils.Log(TAG, "Start swipe")
        }

        override fun onSwipeOutAtEnd() {
            Utils.Log(TAG, "End swipe")
        }

        override fun onSwipeMove() {
            Utils.Log(TAG, "Move swipe")
        }
    })
    if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED) {
        onAddPermissionCamera()
    }
//    if (QRScannerApplication.getInstance().isRequestAds() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableBannerAds()) {
//        QRScannerApplication.getInstance().getAdsView(this)
//    }
    val mCountRating = Utils.onGetCountRating()
    if (mCountRating > 3) {
        showEncourage()
        Utils.Log(TAG, "rating.......")
        Utils.onSetCountRating(0)
    }
    showAds()
}

private fun MainActivity.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(MainViewModel::class.java)
}

fun MainActivity.showAds(){
//    viewModel.doShowAds().observe(this, Observer {
//        doShowAds(it)
//    })
}

fun MainActivity.newWaysLoadingGoogleAdmod(){
    // Set your test devices. Check your logcat output for the hashed device ID to
    // get test ads on a physical device. e.g.
    // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
    // to get test ads on this device."
    // Create an ad request.
    val adRequest = AdRequest.Builder().build()

    // Start loading the ad in the background.
    ad_view.loadAd(adRequest)
}