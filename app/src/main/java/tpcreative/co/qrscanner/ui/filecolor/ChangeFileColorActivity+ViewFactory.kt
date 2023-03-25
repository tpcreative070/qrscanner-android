package tpcreative.co.qrscanner.ui.filecolor
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.model.EnumScreens

fun ChangeFileColorActivity.initUI(){
    setupViewModel()
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    llSmallAds = AdsView(this)
    if(Utils.isHiddenAds(EnumScreens.CHANGE_COLOR_SMALL)){
        binding.rlAdsRoot.visibility = View.GONE
    }else{
        binding.rlAdsRoot.addView(llSmallAds.getRootSmallAds())
    }
    if(Utils.isHiddenAds(EnumScreens.CHANGE_COLOR_LARGE)){
        binding.rlBannerLarger.visibility = View.GONE
    }else{
        binding.rlBannerLarger.addView(llSmallAds.getRootLargeAds())
    }
    initRecycleView(layoutInflater)
    getData()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            showAds()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAds()
                }
            })
    }

    if (QRScannerApplication.getInstance().isRequestInterstitialAd() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableInterstitialAd()) {
        QRScannerApplication.getInstance().requestInterstitialAd()
    }
    if (QRScannerApplication.getInstance().isChangeColorSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableChangeColorSmallView()  && !Utils.isHiddenAds(EnumScreens.CHANGE_COLOR_SMALL)) {
        QRScannerApplication.getInstance().requestChangeColorSmallView(this)
    }

    if (QRScannerApplication.getInstance().isChangeColorLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableChangeColorLargeView()  && !Utils.isHiddenAds(EnumScreens.CHANGE_COLOR_LARGE)) {
        QRScannerApplication.getInstance().requestChangeColorLargeView(this)
    }
    checkingShowAds()
}

fun ChangeFileColorActivity.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

fun ChangeFileColorActivity.getData(){
    viewModel.getData { data ->
        adapter?.setDataSource(data)
        onGenerateReview("123")
    }
}

fun ChangeFileColorActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ChangeFileColorAdapter(layoutInflater, applicationContext, this)
    var mNoOfColumns = Utils.calculateNoOfColumns(this,100F)
    if (mNoOfColumns>=5){
        mNoOfColumns = 4
    }
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, mNoOfColumns)
    binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 20, true))
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.itemAnimator = DefaultItemAnimator()
    binding.recyclerView.adapter = adapter
}

fun ChangeFileColorActivity.showAds(){
    if (QRScannerApplication.getInstance().isRequestInterstitialAd()){
        // Back is pressed... Finishing the activity
        finish()
    }else{
        QRScannerApplication.getInstance().loadInterstitialAd(this)
    }
}

private fun ChangeFileColorActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(ChangeFileColorViewModel::class.java)
}
