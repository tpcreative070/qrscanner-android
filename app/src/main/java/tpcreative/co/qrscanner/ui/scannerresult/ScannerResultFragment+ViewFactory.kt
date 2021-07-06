package tpcreative.co.qrscanner.ui.scannerresult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_review.*
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.viewmodel.ScannerResultViewModel

fun ScannerResultFragment.initUI(){
    TAG = this::class.java.name
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    mList.add(llEmail)
    mList.add(llSMS)
    mList.add(llContact)
    mList.add(llLocation)
    mList.add(llEvent)
    mList.add(llWifi)
    mList.add(llTelephone)
    mList.add(llText)
    mList.add(llURL)
    mList.add(llProduct)
    mList.add(llISBN)
    initRecycleView()
    setupViewModel()
    getDataIntent()
    if (QRScannerApplication.getInstance().isRequestLargeAds() && !Utils.isPremium() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableReviewAds()) {
        QRScannerApplication.getInstance().getAdsLargeView(this)
    }
    checkingShowAds()
}

fun ScannerResultFragment.initRecycleView() {
    adapter = ScannerResultAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.adapter = adapter
}

fun ScannerResultFragment.getDataIntent() {
    viewModel.getIntent(this).observe(this, Observer {
        setView()
    })
}

fun ScannerResultFragment.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        //Disable ads for review
        doShowAds(it)
    })
}

private fun ScannerResultFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ScannerResultViewModel::class.java)
}