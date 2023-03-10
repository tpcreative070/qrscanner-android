package tpcreative.co.qrscanner.ui.main
import android.os.Build
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PremiumManager
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumScreens
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
    /*Doing for innovation only show 4 tabs*/
    if (Utils.isInnovation()){
        setupInnovationViewPager(viewpager)
        tabs.setupWithViewPager(viewpager)
        viewpager.currentItem = 0
        for (i in 0 until tabs.tabCount) {
            val tab = tabs.getTabAt(i)
            tab?.customView = getInnovationTabView(i)
        }
        setupInnovationTabIcons()
    }else{
        setupViewPager(viewpager)
        tabs.setupWithViewPager(viewpager)
        viewpager.currentItem = 2
        for (i in 0 until tabs.tabCount) {
            val tab = tabs.getTabAt(i)
            tab?.customView = getTabView(i)
        }
        setupTabIcons()
    }
    if(Utils.isHiddenAds(EnumScreens.MAIN_SMALL)){
        rlAdsRoot.visibility = View.GONE
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