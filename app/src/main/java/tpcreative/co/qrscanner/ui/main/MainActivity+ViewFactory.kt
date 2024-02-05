package tpcreative.co.qrscanner.ui.main
import android.os.Build
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.ResponseSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumScreens
import tpcreative.co.qrscanner.viewmodel.MainViewModel

fun MainActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    if (QRScannerApplication.getInstance().getDeviceId_() == "66801ac00252fe84") {
        finish()
    }
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    supportActionBar?.hide()
    ResponseSingleton.getInstance()?.setListener(this)
    /*Doing for innovation only show 4 tabs*/
    if (Utils.isInnovation()){
        setupInnovationViewPager(binding.viewpager)
        binding.tabs.setupWithViewPager(binding.viewpager)
        binding.viewpager.currentItem = 0
        for (i in 0 until binding.tabs.tabCount) {
            val tab = binding.tabs.getTabAt(i)
            tab?.customView = getInnovationTabView(i)
        }
        setupInnovationTabIcons()
    }else{
        setupViewPager(binding.viewpager)
        binding.tabs.setupWithViewPager(binding.viewpager)
        binding.viewpager.currentItem = 2
        for (i in 0 until binding.tabs.tabCount) {
            val tab = binding.tabs.getTabAt(i)
            tab?.customView = getTabView(i)
        }
        setupTabIcons()
    }
    if(Utils.isHiddenAds(EnumScreens.MAIN_SMALL)){
        binding.rlAdsRoot.visibility = View.GONE
    }

    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //showAds()
                finish()
            }
        })
}

private fun MainActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(MainViewModel::class.java)
}