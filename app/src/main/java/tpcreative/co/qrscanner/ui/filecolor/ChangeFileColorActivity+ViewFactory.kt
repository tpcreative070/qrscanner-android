package tpcreative.co.qrscanner.ui.filecolor
import android.os.Build
import android.view.LayoutInflater
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_chage_file_color.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration

fun ChangeFileColorActivity.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    getData()
    if (QRScannerApplication.getInstance().isRequestInterstitialAd() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableInterstitialAd()) {
        QRScannerApplication.getInstance().requestInterstitialAd()
    }
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
}

fun ChangeFileColorActivity.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
        onGenerateReview("123")
    })
}

fun ChangeFileColorActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ChangeFileColorAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 4)
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = adapter
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
